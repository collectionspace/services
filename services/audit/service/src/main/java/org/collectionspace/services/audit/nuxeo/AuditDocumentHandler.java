/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.audit.nuxeo;

import java.io.Serializable;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.collectionspace.services.audit.AuditCommon;
import org.collectionspace.services.audit.AuditCommonList;
import org.collectionspace.services.audit.AuditCommonList.AuditListItem;
import org.collectionspace.services.audit.FieldChangedGroup;
import org.collectionspace.services.audit.FieldChangedGroupList;
import org.collectionspace.services.common.api.RefName.RefNameInterface;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.elasticsearch.ESDocumentFilter;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.audit.api.ExtendedInfo;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** AuditDocumentModelHandler
 *  $LastChangedRevision$
 *  $LastChangedDate$
 */
public class AuditDocumentHandler
        extends AbstractDocumentHandlerImpl<AuditCommon, AuditCommonList, LogEntry, List<LogEntry>> {

    private final Logger logger = LoggerFactory.getLogger(AuditDocumentHandler.class);
    private AuditCommon auditCommon;
    private AuditCommonList auditCommonList;

	public enum FieldType {
		QUALIFIED,
		QUALIFIED_SCALAR_LIST,
		UNQUALIFIED
	}

	enum Accessor {
		OLD_VALUE, NEW_VALUE, COMMENT_VALUE;

		public static Accessor valueOfLabel(String label) {
			Accessor result = null;
			
			switch (label) {
				case "newValue":
					result = NEW_VALUE;
				break;
				
				case "oldValue":
					result = OLD_VALUE;
				break;
				
				case "commentValue":
					result = COMMENT_VALUE;
					break;
				}
			
			return result;
		}
	}

    //
    // Inner class
    //
	class FieldPart {
		String key;
		String origin;
		String schema;
		String fieldName;
		String listItemIndex;
		FieldType fieldType;
		Accessor fieldAccessor;
		String value;

		protected void updateFieldChangedGroup(FieldChangedGroup fieldChangedGroup) {
			fieldChangedGroup.setKey(key);
			fieldChangedGroup.setFieldName(fieldName);
			switch (fieldAccessor) {
				case OLD_VALUE:
					fieldChangedGroup.setOriginalValue(value);
					break;
				case NEW_VALUE:
					fieldChangedGroup.setNewValue(value);
					break;
				case COMMENT_VALUE:
					fieldChangedGroup.setChangeReason(value);
					break;
			}
		}
		
		//
		// An audit entry's key has a max of five regex groups
		//	1 - the original string
		//	2 - an optional schema name
		//  3 - the field name
		//  4 - for scalar lists, the index value
		//  5 - the accessor (old value, new value, or comment value)
		//
		int qualifiedGroupCount(Matcher matcher) {
			int result = 0;
			
			for (int i = 0; i < 5; i++) { // An audit entry's key has a max of five regex groups
				if (!matcher.group(i).isEmpty()) {
					result++;
				}
			}

			return result;
		}
		
		boolean isQualified(Matcher matcher) {
			return qualifiedGroupCount(matcher) == 4; // Ex, collectionspace_common:objectNumber.oldValue
		}
		
		boolean isQualifiedList(Matcher matcher) {
			return qualifiedGroupCount(matcher) == 5; // Ex, collectionspace_common:briefDescriptions.38E.oldValue
		}
		
		boolean isUnqualifiedList(Matcher matcher) {
			return qualifiedGroupCount(matcher) == 2; // Ex, version
		}
		
		String getFieldTypeDescription(Matcher matcher) {
			String result = null;
			
			if (isQualified(matcher)) {
				result = "qualified";
			} else if (isQualifiedList(matcher)) {
				result = "a qualified list";
			} else {
				result = "unqualified";
			}
			
			return result;
		}

		FieldPart(String s, String value) {
//			String regex_s = "(\\w+):?(\\w*)\\.?(\\w*)\\.(oldValue|newValue|commentValue)";
			String regex_s = "(\\w+):?([\\w/]*)\\.?(\\w*)\\.(oldValue|newValue|commentValue)";
			
			Pattern pattern = Pattern.compile(regex_s);
			Matcher matcher = pattern.matcher(s);

			this.origin = s;
			if (matcher.find()) {
				this.fieldAccessor = Accessor.valueOfLabel(matcher.group(4));
				this.value = value;
				if (isQualified(matcher)) {
					this.schema = matcher.group(1);
					this.fieldName = matcher.group(2);
					this.fieldType = FieldType.QUALIFIED;
					this.key = schema + ":" + fieldName;
				} else if (isQualifiedList(matcher)) {
					this.schema = matcher.group(1);
					this.fieldName = matcher.group(2);
					this.listItemIndex = matcher.group(3);
					this.fieldType = FieldType.QUALIFIED_SCALAR_LIST;
					this.key = schema + ":" + fieldName + ":" + listItemIndex;
				} else {
					this.fieldName = matcher.group(1);
					this.fieldType = FieldType.UNQUALIFIED;
					this.key = fieldName;
				}
			}
		}
	}

	@Override
	public Lifecycle getLifecycle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lifecycle getLifecycle(String serviceObjectName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc,
			TransitionDef transitionDef) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AuditCommonList extractPagingInfo(AuditCommonList theCommonList, DocumentWrapper<List<LogEntry>> wrapDoc)
			throws Exception {
        AbstractCommonList commonList = (AbstractCommonList) theCommonList;

        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        List docList = (List)wrapDoc.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        commonList.setItemsInPage(docList.size());
        // set the total result size
        commonList.setTotalItems(docFilter.getTotalItemsResult());

        return (AuditCommonList) commonList;
    }

	@Override
	public boolean supportsWorkflowStates() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected String getRefnameDisplayName(DocumentWrapper<LogEntry> docWrapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RefNameInterface getRefName(DocumentWrapper<LogEntry> docWrapper, String tenantName, String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentFilter createDocumentFilter() {
        DocumentFilter filter = new ESDocumentFilter(this.getServiceContext());
        return filter;
	}

	@Override
	public void handleCreate(DocumentWrapper<LogEntry> wrapDoc) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleUpdate(DocumentWrapper<LogEntry> wrapDoc) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleGet(DocumentWrapper<LogEntry> wrapDoc) throws Exception {
		try {
	        setCommonPart(extractCommonPart(wrapDoc));
	        sanitize(getCommonPart());
	        getServiceContext().setOutput(getCommonPart());
		} catch (Throwable t) {
			logger.debug(t.getLocalizedMessage());
		}
    }

	@Override
	public void handleGetAll(DocumentWrapper<List<LogEntry>> wrapDoc) throws Exception {
        AuditCommonList accList = extractCommonPartList(wrapDoc);
        setCommonPartList(accList);
        getServiceContext().setOutput(getCommonPartList());
	}

	//
	//
	//
	List<FieldChangedGroup> createFieldChangeGroupList(AuditCommon auditCommon) {
		List<FieldChangedGroup> result = null;
		
		FieldChangedGroupList tempFieldChangedGroupList = auditCommon.getFieldChangedGroupList();
		if (tempFieldChangedGroupList == null) {
			tempFieldChangedGroupList = new FieldChangedGroupList();
			auditCommon.setFieldChangedGroupList(tempFieldChangedGroupList);
		}

		result = auditCommon.getFieldChangedGroupList().getFieldChangedGroup();
		
		return result;
	}
	
	private String formatFieldValue(ExtendedInfo extendedInfo) {
		String result = null;
		
		if (extendedInfo != null) {
			Serializable value = extendedInfo.getSerializableValue();
			if (value != null && value instanceof Calendar) {
				Calendar calendar = (Calendar) value;
				Instant instant = calendar.getTime().toInstant();
				result = instant.toString();
			} else if (value != null && value instanceof Date) {
				Date date = (Date)value;
				result = date.toInstant().toString();
			} else if (value != null) {
				result = value.toString();
			}
		}

		return result;
	}

	@Override
	public AuditCommon extractCommonPart(DocumentWrapper<LogEntry> wrapDoc) throws Exception {
		AuditCommon result = new AuditCommon();
		LogEntry logEntry = wrapDoc.getWrappedObject();
		
		result.setIdNumber(Long.toString(logEntry.getId())); // set ES csid
		result.setCsid(logEntry.getEventId()); // set cspace csid
		result.setEventComment(logEntry.getComment()); // We may need to consolidate "Event Message" and "Save Comment"
		result.setSaveMessage(logEntry.getComment());  // (see comment above)
		result.setEventType(logEntry.getCategory());
		result.setResourceType(logEntry.getDocType());
		result.setResourceCSID(logEntry.getDocPath());
		result.setPrincipal(logEntry.getPrincipalName());
		
		Map<String, ExtendedInfo> extendedInfoMap = logEntry.getExtendedInfos();
		if (extendedInfoMap != null && !extendedInfoMap.isEmpty()) {
			
			Map<String, FieldChangedGroup> resultMap = new HashMap<String, FieldChangedGroup>();
			for (String fieldKey : extendedInfoMap.keySet()) {
				String extendedInfoValue = formatFieldValue(extendedInfoMap.get(fieldKey));
				FieldPart fieldPart = new FieldPart(fieldKey, extendedInfoValue);
				
				FieldChangedGroup fieldChangedGroup = resultMap.get(fieldPart.key);
				if (fieldChangedGroup == null) {
					fieldChangedGroup = new FieldChangedGroup();
					resultMap.put(fieldPart.key, fieldChangedGroup);
				}
				fieldPart.updateFieldChangedGroup(fieldChangedGroup);
			}
			
			List<FieldChangedGroup> fcgl = createFieldChangeGroupList(result);
			fcgl.addAll(resultMap.values());
		}

		Date date = logEntry.getEventDate();
		if (date != null) {
			result.setEventDate(date.toInstant().toString());
		}
		
		return result;
	}

	@Override
	public void fillCommonPart(AuditCommon obj, DocumentWrapper<LogEntry> wrapDoc) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AuditCommonList extractCommonPartList(DocumentWrapper<List<LogEntry>> wrapDoc) throws Exception {

		AuditCommonList accList = this.extractPagingInfo(new AuditCommonList(), wrapDoc);
        List<AuditListItem> list = accList.getAuditListItem();

        for (LogEntry logEntry : wrapDoc.getWrappedObject()) {
        	AuditListItem accListItem = new AuditCommonList.AuditListItem();
					accListItem.setCsid(logEntry.getEventId());
					accListItem.setIdNumber(Long.toString(logEntry.getId()));
					accListItem.setPrincipal(logEntry.getPrincipalName());
					accListItem.setEventDate(logEntry.getEventDate().toInstant().toString());
					accListItem.setResourceCSID(logEntry.getDocPath());
					accListItem.setEventType(logEntry.getCategory());
					accListItem.setResourceType(logEntry.getDocType());
					list.add(accListItem);
        }
        return accList;
    }

	@Override
	public AuditCommon getCommonPart() {
		return auditCommon;
	}

	@Override
	public void setCommonPart(AuditCommon auditCommon) {
		this.auditCommon = auditCommon;
	}

	@Override
	public AuditCommonList getCommonPartList() {
		return auditCommonList;
	}

	@Override
	public void setCommonPartList(AuditCommonList obj) {
		auditCommonList = obj;
	}

	@Override
	public String getQProperty(String prop) throws DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
