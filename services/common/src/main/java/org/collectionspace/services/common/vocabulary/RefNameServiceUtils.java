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
package org.collectionspace.services.common.vocabulary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

/**
 * RefNameServiceUtils is a collection of services utilities related to refName usage.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RefNameServiceUtils {

    private final Logger logger = LoggerFactory.getLogger(RefNameServiceUtils.class);

    public static AuthorityRefDocList getAuthorityRefDocs(ServiceContext ctx,
            RepositoryClient repoClient,
            String serviceType,
            String refName,
            int pageSize, int pageNum, boolean computeTotal) throws DocumentException, DocumentNotFoundException {
        AuthorityRefDocList wrapperList = new AuthorityRefDocList();
        AbstractCommonList commonList = (AbstractCommonList) wrapperList;
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        
        List<AuthorityRefDocList.AuthorityRefDocItem> list =
                wrapperList.getAuthorityRefDocItem();
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), serviceType);
        if (servicebindings == null || servicebindings.isEmpty()) {
            return null;
        }
        // Need to escape the quotes in the refName
        // TODO What if they are already escaped?
        String escapedRefName = refName.replaceAll("'", "\\\\'");
//    	String domain = 
//    		tReader.getTenantBinding(ctx.getTenantId()).getRepositoryDomain();
        ArrayList<String> docTypes = new ArrayList<String>();
        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        Map<String, Map<String, String>> authRefFieldsByService = new HashMap<String, Map<String, String>>();
        StringBuilder whereClause = new StringBuilder();
        boolean fFirst = true;
        List<String> authRefFieldPaths = new ArrayList<String>();
        for (ServiceBindingType sb : servicebindings) {
        	// Gets the property names for each part, qualified with the part label (which
        	// is also the table name, the way that the repository works).
            authRefFieldPaths =
                    ServiceBindingUtils.getAllPartsPropertyValues(sb,
                    ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            if (authRefFieldPaths.isEmpty()) {
                continue;
            }
            String authRefPath = "";
            String ancestorAuthRefFieldName = "";
            Map<String, String> authRefFields = new HashMap<String, String>();
            for (int i = 0; i < authRefFieldPaths.size(); i++) {
                // fieldName = DocumentUtils.getDescendantOrAncestor(authRefFields.get(i));
            	// For simple field values, we just search on the item.
            	// For simple repeating scalars, we just search the group field 
            	// For repeating complex types, we will need to do more.
                authRefPath = authRefFieldPaths.get(i);
                ancestorAuthRefFieldName = DocumentUtils.getAncestorAuthRefFieldName(authRefFieldPaths.get(i));
                authRefFields.put(authRefPath, ancestorAuthRefFieldName);
            }

            String docType = sb.getObject().getName();
            queriedServiceBindings.put(docType, sb);
            authRefFieldsByService.put(docType, authRefFields);
            docTypes.add(docType);
            Collection<String> fields = authRefFields.values();
            for (String field : fields) {
                // Build up the where clause for each authRef field
                if (fFirst) {
                    fFirst = false;
                } else {
                    whereClause.append(" OR ");
                }
                //whereClause.append(prefix);
                whereClause.append(field);
                whereClause.append("='");
                whereClause.append(escapedRefName);
                whereClause.append("'");
            }
        }
        String whereClauseStr = whereClause.toString(); // for debugging
        if (fFirst) // found no authRef fields - nothing to query
        {
            return wrapperList;
        }
        String fullQuery = whereClause.toString(); // for debug
        // Now we have to issue the search
        DocumentWrapper<DocumentModelList> docListWrapper = repoClient.findDocs(ctx,
                docTypes, whereClause.toString(), pageSize, pageNum, computeTotal);
        // Now we gather the info for each document into the list and return
        DocumentModelList docList = docListWrapper.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        commonList.setItemsInPage(docList.size());
        // set the total result size
        commonList.setTotalItems(docList.totalSize());
        Iterator<DocumentModel> iter = docList.iterator();
        while (iter.hasNext()) {
            DocumentModel docModel = iter.next();
            AuthorityRefDocList.AuthorityRefDocItem ilistItem = new AuthorityRefDocList.AuthorityRefDocItem();
            String csid = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            String docType = docModel.getDocumentType().getName();
            ServiceBindingType sb = queriedServiceBindings.get(docType);
            if (sb == null) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: No Service Binding for docType: " + docType);
            }
            String serviceContextPath = "/" + sb.getName().toLowerCase() + "/";
            // The id and URI are the same on all doctypes
            ilistItem.setDocId(csid);
            ilistItem.setUri(serviceContextPath + csid);
            ilistItem.setDocType(docType);
            ilistItem.setDocNumber(
                    ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NUMBER_PROP, docModel));
            ilistItem.setDocName(
                    ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NAME_PROP, docModel));
            // Now, we have to loop over the authRefFieldsByService to figure
            // out which field matched this. Ignore multiple matches.
            Map<String,String> matchingAuthRefFields = authRefFieldsByService.get(docType);
            if (matchingAuthRefFields == null || matchingAuthRefFields.isEmpty()) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: internal logic error: can't fetch authRefFields for DocType.");
            }
            String authRefAncestorField = "";
            String authRefDescendantField = "";
            String sourceField = "";
            boolean fRefFound = false;
            // Use this if we go to qualified field names
            for (String path : matchingAuthRefFields.keySet()) {
                try {
                	// This is the field name we show in the return info
                    authRefAncestorField = (String) matchingAuthRefFields.get(path);
                    // This is the qualified field we have to get from the doc model
                    authRefDescendantField = DocumentUtils.getDescendantOrAncestor(path);
                    // The ancestor field is part-schema (tablename) qualified
                    String[] strings = authRefAncestorField.split(":");
                    if (strings.length != 2) {
                        throw new RuntimeException(
                                "getAuthorityRefDocs: Bad configuration of path to authority reference field.");
                    }
                    // strings[0] holds a schema name, such as "intakes_common"
                    //
                    // strings[1] holds:
                    // * The name of an authority reference field, such as "depositor";
                    //   or
                    // * The name of an ancestor (e.g. parent, grandparent ...) field,
                    //   such as "fieldCollectors", of a repeatable authority reference
                    //   field, such as "fieldCollector".
                    // TODO - if the value is not simple, or repeating scalar, need a more
                    // sophisticated fetch. 
                    Object fieldValue = docModel.getProperty(strings[0], strings[1]);
                    // We cannot be sure why we have this doc, so look for matches
                    boolean fRefMatches = refNameFoundInField(refName, fieldValue);
                    if (fRefMatches) {
                        sourceField = authRefDescendantField;
                        // Handle multiple fields matching in one Doc. See CSPACE-2863.
                    	if(fRefFound) {
                    		// We already added ilistItem, so we need to clone that and add again
                            ilistItem = cloneAuthRefDocItem(ilistItem, sourceField);
                    	} else {
                    		ilistItem.setSourceField(sourceField);
                            fRefFound = true;
                    	}
                        list.add(ilistItem);
                    }

                } catch (ClientException ce) {
                    throw new RuntimeException(
                            "getAuthorityRefDocs: Problem fetching: " + sourceField, ce);
                }
            }
            if (!fRefFound) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: Could not find refname in object:"
                        + docType + ":" + csid);
            }
        }
        return wrapperList;
    }
    
    private static AuthorityRefDocList.AuthorityRefDocItem cloneAuthRefDocItem(
    		AuthorityRefDocList.AuthorityRefDocItem ilistItem, String sourceField) {
    	AuthorityRefDocList.AuthorityRefDocItem newlistItem = new AuthorityRefDocList.AuthorityRefDocItem();
    	newlistItem.setDocId(ilistItem.getDocId());
    	newlistItem.setDocName(ilistItem.getDocName());
    	newlistItem.setDocNumber(ilistItem.getDocNumber());
    	newlistItem.setDocType(ilistItem.getDocType());
    	newlistItem.setUri(ilistItem.getUri());
    	newlistItem.setSourceField(sourceField);
    	return newlistItem;
    }

    /*
     * Identifies whether the refName was found in the supplied field.
     *
     * Only works for:
     * * Scalar fields
     * * Repeatable scalar fields (aka multi-valued fields)
     *
     * Does not work for:
     * * Structured fields (complexTypes)
     * * Repeatable structured fields (repeatable complexTypes)
     */
    private static boolean refNameFoundInField(String refName, Object fieldValue) {

        boolean result = false;
        if (fieldValue instanceof List) {
            List<String> fieldValueList = (List) fieldValue;
            for (String listItemValue : fieldValueList) {
                if (refName.equalsIgnoreCase(listItemValue)) {
                    result = true;
                    break;
                }

            }
        } else if (fieldValue instanceof String){
            if (refName.equalsIgnoreCase((String)fieldValue)) {
                result = true;
            }
        }
        return result;
    }
}

