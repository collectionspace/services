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
package org.collectionspace.services.nuxeo.client.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayload;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.XmlTools;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ReflectionMapper;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.context.AbstractServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.relation.nuxeo.RelationsUtils;
import org.collectionspace.services.config.service.DocHandlerParams;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.Document;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * This class is generified by the marker type T,
 * where T is expected to map to something like BlobCommon, MediaCommon, ObjectexitCommon, etc.,
 * and so on for every JAXB-generated schema class.
 *
 * User: laramie
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 */
public abstract class NuxeoDocumentModelHandler<T> extends RemoteDocumentModelHandlerImpl<T, AbstractCommonList> {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private AbstractCommonList commonList;

	protected static final int NUM_STANDARD_LIST_RESULT_FIELDS = 5;
	protected static final String STANDARD_LIST_CSID_FIELD = "csid";
	protected static final String STANDARD_LIST_URI_FIELD = CollectionSpaceClient.COLLECTIONSPACE_CORE_URI;
	protected static final String STANDARD_LIST_REFNAME_FIELD = CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME;
	protected static final String STANDARD_LIST_UPDATED_AT_FIELD = CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_AT;
	protected static final String STANDARD_LIST_WORKFLOW_FIELD = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE;
	protected static final String STANDARD_LIST_MARK_RT_FIELD = "related";

	@Override
	public AbstractCommonList getCommonPartList() {
		return commonList;
	}

	@Override
	public void setCommonPartList(AbstractCommonList aCommonList) {
		this.commonList = aCommonList;
	}

	private T commonPart;

	@Override
	public T getCommonPart() {
		return (T) commonPart;
	}

	@Override
	public void setCommonPart(T commonPart) {
		this.commonPart = commonPart;
	}

	protected String getCsid(ListItem item) {
        String result = null;
        
        for (Element ele : item.getAny()) {
            String elementName = ele.getTagName().toLowerCase();
            if (elementName.equals("csid")) {
                result = ele.getTextContent();
                break;
            }
        }
        
        return result;
    }
	
    /**
     * The entity type expected from the JAX-RS Response object.  By default it is of type String.  Child classes
     * can override this if they need to.
     */
    protected Class<String> getEntityResponseType() {
    	return String.class;
    }
    
    protected String getWorkflowState(PoxPayload payload) {
    	String result = null;
    	
		Document document = payload.getDOMDocument();
		result = XmlTools.getElementValue(document, "//" + WorkflowClient.WORKFLOWSTATE_XML_ELEMENT_NAME);
		
		return result;
    }
    
    protected Long getRevision(PoxPayload payload) {
    	Long result = null;
    	
		Document document = payload.getDOMDocument();
		String xmlRev = XmlTools.getElementValue(document, "//rev");
		result = Long.valueOf(xmlRev);
		
		return result;
    }
    
    protected List getItemList(PoxPayloadIn payloadIn) {
    	List result = null;
    	
		Document document = payloadIn.getDOMDocument();
		result = XmlTools.getElementNodes(document, "//list-item");
		
		return result;
    }

	/**
	 * Subclass DocHandlers may override this method to control exact creation of the common list.
	 * This class instantiates an AbstractCommonList from the classname returned by getDocHandlerParams().AbstractCommonListClassname.
	 * 
	 * @return
	 * @throws Exception
	 */
	public AbstractCommonList createAbstractCommonListImpl() throws Exception {
		// String classname = this.commonList.getClass().getName();
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		String classname = ServiceConfigUtils.getDocHandlerParams(ctx).getAbstractCommonListClassname();
		if (classname == null) {
			throw new Exception(
					"in createAbstractCommonListImpl. getDocHandlerParams().getAbstractCommonListClassname() is null");
		}
		classname = classname.trim();
		return (AbstractCommonList) (ReflectionMapper.instantiate(classname));
	}

	/** DocHandlerBase calls this method with the CSID as id */
	public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		return createItemForCommonList(ServiceConfigUtils.getDocHandlerParams(ctx).getCommonListItemClassname(),
				docModel, label, id, true);
	}

	public String getSummaryFields(AbstractCommonList theCommonList) throws DocumentException {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		return ServiceConfigUtils.getDocHandlerParams(ctx).getSummaryFields();
	}

	public void setListItemArrayExtended(boolean isExtended) throws DocumentException {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		ServiceConfigUtils.getDocHandlerParams(ctx).getListResultsFields().setExtended(isExtended);
	}

	public boolean isListItemArrayExtended() throws DocumentException {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		return ServiceConfigUtils.getDocHandlerParams(ctx).getListResultsFields().isExtended();
	}

	public List<ListResultField> getListItemsArray() throws DocumentException {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		return ServiceConfigUtils.getDocHandlerParams(ctx).getListResultsFields().getListResultField();
	}

	@Override
	public T extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fillCommonPart(T objectexitObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
		throw new UnsupportedOperationException();
	}

	protected static String getRefname(DocumentModel docModel) throws Exception {
		String result = (String) docModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
				CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
		return result;
	}

	public static String getUpdatedAtAsString(DocumentModel docModel) throws Exception {
		GregorianCalendar cal = (GregorianCalendar) docModel.getProperty(
				CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
				CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_AT);
		String updatedAt = GregorianCalendarDateTimeUtils.formatAsISO8601Timestamp(cal);
		return updatedAt;
	}

	@Override
	public AbstractCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
		CommonList commonList = new CommonList();
		CoreSessionInterface repoSession = null;
		NuxeoRepositoryClientImpl repoClient = null;
		boolean releaseRepoSession = false;

		AbstractServiceContextImpl ctx = (AbstractServiceContextImpl) getServiceContext();
		MultivaluedMap<String, String> queryParams = getServiceContext().getQueryParams();
		String markRtSbj = queryParams.getFirst(IQueryManager.MARK_RELATED_TO_CSID_AS_SUBJECT);
		if (Tools.isBlank(markRtSbj)) {
			markRtSbj = null;
		}
		
		//
		// We may be being asked to mark the record as related independent of whether it is the subject or object of a relationship.
		//
		String markRtSbjOrObj = queryParams.getFirst(IQueryManager.MARK_RELATED_TO_CSID_AS_EITHER);
		if (Tools.isBlank(markRtSbjOrObj)) {
			markRtSbjOrObj = null;
		} else {
			if (Tools.isBlank(markRtSbj) == false) {
				logger.warn(String.format("Ignoring query param %s=%s since overriding query param %s=%s exists.",
						IQueryManager.MARK_RELATED_TO_CSID_AS_SUBJECT, markRtSbj, IQueryManager.MARK_RELATED_TO_CSID_AS_EITHER, markRtSbjOrObj));
			}
			markRtSbj = markRtSbjOrObj; // Mark the record as related independent of whether it is the subject or object of a relationship
		}

		try {
			if (markRtSbj != null) {
				repoClient = (NuxeoRepositoryClientImpl) this.getRepositoryClient(ctx);
				NuxeoRepositoryClientImpl nuxeoRepoClient = (NuxeoRepositoryClientImpl) repoClient;
				repoSession = this.getRepositorySession();
				if (repoSession == null) {
					repoSession = repoClient.getRepositorySession(ctx);
					releaseRepoSession = true;
				}
			}

			String commonSchema = getServiceContext().getCommonPartLabel();
			extractPagingInfo(commonList, wrapDoc);
			List<ListResultField> resultsFields = getListItemsArray(); // Get additional list result fields defined in the service bindings
			int baseFields = NUM_STANDARD_LIST_RESULT_FIELDS;
			int nFields = resultsFields.size() + NUM_STANDARD_LIST_RESULT_FIELDS;
			if (markRtSbj != null) {
				nFields++;
				baseFields++;
			}
			
			String fields[] = new String[nFields]; // REM - Why can't this just be a static array defined once at the top? Then there'd be no need for these hardcoded "[x]" statements and no need for NUM_STANDARD_LIST_RESULT_FIELDS constant as well.
			fields[0] = STANDARD_LIST_CSID_FIELD;
			fields[1] = STANDARD_LIST_URI_FIELD;
			fields[2] = STANDARD_LIST_REFNAME_FIELD;
			fields[3] = STANDARD_LIST_UPDATED_AT_FIELD;
			fields[4] = STANDARD_LIST_WORKFLOW_FIELD;
			
			if (markRtSbj != null) {
				fields[5] = STANDARD_LIST_MARK_RT_FIELD;
			}
			
			for (int i = baseFields; i < nFields; i++) {
				ListResultField field = resultsFields.get(i - baseFields);
				fields[i] = field.getElement();
			}
			commonList.setFieldsReturned(fields);
			
			Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
			HashMap<String, Object> item = new HashMap<String, Object>();
			while (iter.hasNext()) {
				DocumentModel docModel = iter.next();
				String id = NuxeoUtils.getCsid(docModel);
				item.put(STANDARD_LIST_CSID_FIELD, id);
				
				//
				// If the mark-related query param was set, check to see if the doc we're processing
				// is related to the value specified in the mark-related query param.
				//
				if (markRtSbj != null) {
					String relationClause = RelationsUtils.buildWhereClause(markRtSbj, null, null, id, null, markRtSbj == markRtSbjOrObj);
					String whereClause = relationClause + IQueryManager.SEARCH_QUALIFIER_AND
							+ NuxeoUtils.buildWorkflowNotDeletedWhereClause();
					QueryContext queryContext = new QueryContext(ctx, whereClause);
					queryContext.setDocType(IRelationsManager.DOC_TYPE);
					String query = NuxeoUtils.buildNXQLQuery(queryContext);
					// Search for 1 relation that matches. 1 is enough to fail
					// the filter
					DocumentModelList docList = repoSession.query(query, null, 1, 0, false);
					item.put(STANDARD_LIST_MARK_RT_FIELD, docList.isEmpty() ? "false" : "true");
				}
				
				String uri = getUri(docModel);
				item.put(STANDARD_LIST_URI_FIELD, uri);
				item.put(STANDARD_LIST_REFNAME_FIELD, getRefname(docModel));
				item.put(STANDARD_LIST_UPDATED_AT_FIELD, getUpdatedAtAsString(docModel));
				item.put(STANDARD_LIST_WORKFLOW_FIELD, docModel.getCurrentLifeCycleState());

				for (ListResultField field : resultsFields) {
					String schema = field.getSchema();
					if (schema == null || schema.trim().isEmpty()) {
						schema = commonSchema;
					}
					Object value = getListResultValue(docModel, schema, field);
					if (value != null && value instanceof String) { // If it is String that is either null or empty, we set our value to null
						String strValue = (String) value;
						if (strValue.trim().isEmpty() == true) {
							value = null; // We found an "empty" string value, so just set the value to null so we don't return anything.
						}
					}
					if (value != null) {
						item.put(field.getElement(), value);
					}
				}
				commonList.addItem(item);
				item.clear();
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			// If we got/aquired a new session then we're responsible for releasing it.
			if (releaseRepoSession && repoSession != null) {
				repoClient.releaseRepositorySession(ctx, repoSession);
			}
		}

		return commonList;
	}

	// TODO - get rid of this if we can - appears to be unused.
	@Override
	public String getQProperty(String prop) throws DocumentException {
		ServiceContext ctx = this.getServiceContext();
		return ServiceConfigUtils.getDocHandlerParams(ctx).getSchemaName() + ":" + prop;
	}

	// ============= dublin core handling =======================================

	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		super.fillAllParts(wrapDoc, action);
		fillDublinCoreObject(wrapDoc);
	}

	/**
	 * Fill dublin core object, but only if there are document handler parameters in the service
	 * bindings.
	 *
	 * @param wrapDoc
	 *            the wrap doc
	 * @throws Exception
	 *             the exception
	 */
	// TODO - Remove this?
	// This look like it is never used in a sensible way. It just stuffs a static
	// String that matches the service name into a bogus field.
	protected void fillDublinCoreObject(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
		DocHandlerParams.Params docHandlerParams = null;
		try {
			docHandlerParams = ServiceConfigUtils.getDocHandlerParams(getServiceContext());
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}

		if (docHandlerParams != null) {
			String title = docHandlerParams.getDublinCoreTitle();
			if (Tools.isEmpty(title) == false) {
				DocumentModel docModel = wrapDoc.getWrappedObject();
				docModel.setPropertyValue("dublincore:title", title);
			}
		}
	}

	// ================== UTILITY METHODS ================================================
	public static ReflectionMapper.STATUS callPropertySetterWithXPathValue(DocumentModel docModel, Object listItem,
			String setterName, String schema, String xpath) throws Exception {
		// Object prop = docModel.getProperty(label, elementName);
		String value = (String) NuxeoUtils.getXPathValue(docModel, schema, xpath);
		return ReflectionMapper.callSetter(listItem, setterName, value);
	}

	public static ReflectionMapper.STATUS callSimplePropertySetter(Object target, String name, Object arg) {
		return ReflectionMapper.callSetter(target, name, arg);
	}

	/**
	 * @param commonListClassname
	 *            is a package-qualified java classname, including inner class $ notation, such as
	 *            "org.collectionspace.services.objectexit.ObjectexitCommonList$ObjectexitListItem".
	 * @param includeStdFields
	 *            set to true to have the method set Uri and Csid automatically, based on id param.
	 */
	public Object createItemForCommonList(String commonListClassname, DocumentModel docModel, String schema, String id,
			boolean includeStdFields) throws Exception {
		// createItemForCommonList(docModel, label, id);
		Object item = ReflectionMapper.instantiate(commonListClassname);
		List<ListResultField> resultsFields = getListItemsArray();
		for (ListResultField field : resultsFields) {
			callPropertySetterWithXPathValue(docModel, item, field.getSetter(), schema, field.getXpath());
		}
		if (includeStdFields) {
			callSimplePropertySetter(item, "setCsid", id);
			callSimplePropertySetter(item, "setUri", getServiceContextPath() + id);
		}
		
		return item;
	}

	/**
	 * Subclasses should override this method if they don't want to automatically
	 * call List createItemsList(AbstractCommonList commonList, String listItemMethodName)
	 * which will use introspection to create a summary list, and will find the primary
	 * field for you if specified.
	 */
	public List createItemsList(AbstractCommonList commonList) throws Exception {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		return createItemsList(commonList, ServiceConfigUtils.getDocHandlerParams(ctx).getListResultsItemMethodName());
	}

	/** e.g. createItemsList(commonList, "getObjectexitListItem" */
	public List createItemsList(AbstractCommonList commonList, String listItemMethodName) throws Exception {
		Class commonListClass = commonList.getClass();
		Class[] types = new Class[] {};
		try {
			Method m = commonListClass.getMethod(listItemMethodName, types);
			return (List) (ReflectionMapper.fireGetMethod(m, commonList));
		} catch (NoSuchMethodException nsm) {
			return new ArrayList();
		}
	}
	
	@Override
	public boolean supportsWorkflowStates() {
		return true;
	}

}
