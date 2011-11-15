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
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.impl.primitives.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
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

    private static final Logger logger = LoggerFactory.getLogger(RefNameServiceUtils.class);
    
    private static ArrayList<String> refNameServiceTypes = null;

    public static AuthorityRefDocList getAuthorityRefDocs(ServiceContext ctx,
            RepositoryClient repoClient,
            List<String> serviceTypes,
            String refName,
            String refPropName,
            int pageSize, int pageNum, boolean computeTotal) throws DocumentException, DocumentNotFoundException {
        AuthorityRefDocList wrapperList = new AuthorityRefDocList();
        AbstractCommonList commonList = (AbstractCommonList) wrapperList;
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        List<AuthorityRefDocList.AuthorityRefDocItem> list =
                wrapperList.getAuthorityRefDocItem();
        
        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        Map<String, Map<String, String>> authRefFieldsByService = new HashMap<String, Map<String, String>>();

        DocumentModelList docList = findAuthorityRefDocs(ctx, repoClient, serviceTypes, refName, refPropName,
        		queriedServiceBindings, authRefFieldsByService, pageSize, pageNum, computeTotal);

        if (docList == null) { // found no authRef fields - nothing to process
            return wrapperList;
        }
        // Set num of items in list. this is useful to our testing framework.
        commonList.setItemsInPage(docList.size());
        // set the total result size
        commonList.setTotalItems(docList.totalSize());
        
        int nRefsFound = processRefObjsDocList(docList, refName, queriedServiceBindings, authRefFieldsByService,
				       			list, null);
        if(logger.isDebugEnabled()  && (nRefsFound < docList.size())) {
        	logger.debug("Internal curiosity: got fewer matches of refs than # docs matched...");
        }
        return wrapperList;
    }
    
    private static ArrayList<String> getRefNameServiceTypes() {
    	if(refNameServiceTypes == null) {
    		refNameServiceTypes = new ArrayList<String>();
    		refNameServiceTypes.add(ServiceBindingUtils.SERVICE_TYPE_AUTHORITY);
    		refNameServiceTypes.add(ServiceBindingUtils.SERVICE_TYPE_OBJECT);
    		refNameServiceTypes.add(ServiceBindingUtils.SERVICE_TYPE_PROCEDURE);
    	}
    	return refNameServiceTypes;
    }
    
    public static int updateAuthorityRefDocs(ServiceContext ctx,
            RepositoryClient repoClient,
            String oldRefName,
            String newRefName,
            String refPropName ) {
        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        Map<String, Map<String, String>> authRefFieldsByService = new HashMap<String, Map<String, String>>();
        int nRefsFound = 0;
        if(!(repoClient instanceof RepositoryJavaClientImpl)) {
    		throw new InternalError("updateAuthorityRefDocs() called with unknown repoClient type!");
        }
        try {
        	final int pageSize = 100;	// Seems like a good value - no real data to set this well.
        	int pageNumProcessed = 1;
        	while(true) {	// Keep looping until we find all the refs.
        		logger.debug("updateAuthorityRefDocs working on page: "+pageNumProcessed);
        		// Note that we always ask the Repo for the first page, since each page we process
        		// should not be found in successive searches.
		        DocumentModelList docList = findAuthorityRefDocs(ctx, repoClient, getRefNameServiceTypes(), oldRefName, refPropName,
		        		queriedServiceBindings, authRefFieldsByService, pageSize, 0, false);
		
		        if((docList == null) 			// found no authRef fields - nothing to do
		        	|| (docList.size() == 0)) {	// No more to handle
	        		logger.debug("updateAuthorityRefDocs no more results");
		            break;
		        }
        		logger.debug("updateAuthorityRefDocs curr page result list size: "+docList.size());
		        int nRefsFoundThisPage = processRefObjsDocList(docList, oldRefName, queriedServiceBindings, authRefFieldsByService,
						       			null, newRefName);
		        if(nRefsFoundThisPage>0) {
		        	((RepositoryJavaClientImpl)repoClient).saveDocListWithoutHandlerProcessing(ctx, docList, true);
		        	nRefsFound += nRefsFoundThisPage;
		        }
		        pageNumProcessed++;
        	}
        } catch(Exception e) {
    		logger.error("Internal error updating the AuthorityRefDocs: " + e.getLocalizedMessage());
    		logger.debug(Tools.errorToString(e, true));
        }
		logger.debug("updateAuthorityRefDocs replaced a total of: "+nRefsFound);
        return nRefsFound;
    }
    
    private static DocumentModelList findAuthorityRefDocs(ServiceContext ctx,
            RepositoryClient repoClient,
            List<String> serviceTypes,
            String refName,
            String refPropName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, Map<String, String>> authRefFieldsByService,
            int pageSize, int pageNum, boolean computeTotal) throws DocumentException, DocumentNotFoundException {

        // Get the service bindings for this tenant
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        // We need to get all the procedures, authorities, and objects.
        List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), serviceTypes);
        if (servicebindings == null || servicebindings.isEmpty()) {
        	logger.error("RefNameServiceUtils.getAuthorityRefDocs: No services bindings found, cannot proceed!");
            return null;
        }
        
        // Need to escape the quotes in the refName
        // TODO What if they are already escaped?
        String escapedRefName = refName.replaceAll("'", "\\\\'");
        ArrayList<String> docTypes = new ArrayList<String>();
        
        String query = computeWhereClauseForAuthorityRefDocs(escapedRefName, refPropName, docTypes, servicebindings, 
        											queriedServiceBindings, authRefFieldsByService );
        if (query == null) { // found no authRef fields - nothing to query
            return null;
        }
        // Now we have to issue the search
        DocumentWrapper<DocumentModelList> docListWrapper = repoClient.findDocs(ctx,
                docTypes, query, pageSize, pageNum, computeTotal);
        // Now we gather the info for each document into the list and return
        DocumentModelList docList = docListWrapper.getWrappedObject();
        return docList;
    }
    
    private static String computeWhereClauseForAuthorityRefDocs(
    		String escapedRefName,
    		String refPropName,
    		ArrayList<String> docTypes,
    		List<ServiceBindingType> servicebindings,
    		Map<String, ServiceBindingType> queriedServiceBindings,
    		Map<String, Map<String, String>> authRefFieldsByService ) {
        StringBuilder whereClause = new StringBuilder();
        boolean fFirst = true;
        List<String> authRefFieldPaths = new ArrayList<String>();
        for (ServiceBindingType sb : servicebindings) {
        	// Gets the property names for each part, qualified with the part label (which
        	// is also the table name, the way that the repository works).
            authRefFieldPaths =
                    ServiceBindingUtils.getAllPartsPropertyValues(sb,
                    		refPropName, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
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
        if (fFirst) { // found no authRef fields - nothing to query
            return null;
        } else {
        	return whereClause.toString(); 
        }
    }
    
    /*
     * Runs through the list of found docs, processing them. 
     * If list is non-null, then processing means gather the info for items.
     * If list is null, and newRefName is non-null, then processing means replacing and updating. 
     *   If processing/updating, this must be called in teh context of an open session, and caller
     *   must release Session after calling this.
     * 
     */
    private static int processRefObjsDocList(
    		DocumentModelList docList,
    		String refName,
    		Map<String, ServiceBindingType> queriedServiceBindings,
    		Map<String, Map<String, String>> authRefFieldsByService,
   			List<AuthorityRefDocList.AuthorityRefDocItem> list, 
   			String newAuthorityRefName) {
    	if(newAuthorityRefName==null) {
    		if(list==null) {
        		throw new InternalError("processRefObjsDocList() called with neither an itemList nor a new RefName!");
    		}
    	} else if(list!=null) {
    		throw new InternalError("processRefObjsDocList() called with both an itemList and a new RefName!");
    	}

        Iterator<DocumentModel> iter = docList.iterator();
        int nRefsFoundTotal = 0;
        while (iter.hasNext()) {
            DocumentModel docModel = iter.next();
            AuthorityRefDocList.AuthorityRefDocItem ilistItem;

            String docType = docModel.getDocumentType().getName();
            ServiceBindingType sb = queriedServiceBindings.get(docType);
            if (sb == null) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: No Service Binding for docType: " + docType);
            }
            String serviceContextPath = "/" + sb.getName().toLowerCase() + "/";
            
            if(list == null) {
            	ilistItem = null;
            } else {
            	ilistItem = new AuthorityRefDocList.AuthorityRefDocItem();
                String csid = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
                ilistItem.setDocId(csid);
                ilistItem.setUri(serviceContextPath + csid);
                // The id and URI are the same on all doctypes
                ilistItem.setDocType(docType);
                ilistItem.setDocNumber(
                        ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NUMBER_PROP, docModel));
                ilistItem.setDocName(
                        ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NAME_PROP, docModel));
            }
            // Now, we have to loop over the authRefFieldsByService to figure
            // out which field(s) matched this.
            Map<String,String> matchingAuthRefFields = authRefFieldsByService.get(docType);
            if (matchingAuthRefFields == null || matchingAuthRefFields.isEmpty()) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: internal logic error: can't fetch authRefFields for DocType.");
            }
            String authRefAncestorField = "";
            String authRefDescendantField = "";
            String sourceField = "";
            int nRefsFoundInDoc = 0;
            // Use this if we go to qualified field names
            for (String path : matchingAuthRefFields.keySet()) {
                try {
                	// This is the field name we show in the return info
                	// Returned as a schema-qualified property path
                    authRefAncestorField = (String) matchingAuthRefFields.get(path);
                    // This is the qualified field we have to get from the doc model
                    authRefDescendantField = DocumentUtils.getDescendantOrAncestor(path);
                    // The ancestor field is part-schema (tablename) qualified
                    //String[] strings = authRefAncestorField.split(":");
                    //if (strings.length != 2) {
                    //   throw new RuntimeException(
                    //            "getAuthorityRefDocs: Bad configuration of path to authority reference field.");
                    //}
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
                    // Change this to an XPath model
                    //Object fieldValue = docModel.getProperty(strings[0], strings[1]);
                    // This will have to handle repeating complex fields by iterating over the possibilities
                    // and finding the one that matches.
                    Property fieldValue = docModel.getProperty(authRefAncestorField);
                    // We know this doc should have a match somewhere, but it may not be in this field
                    // If we are just building up the refItems, then it is enough to know we found a match.
                    // If we are patching refName values, then we have to replace each match.
                    int nRefsMatchedInField = refNameFoundInField(refName, fieldValue, newAuthorityRefName);
                    if (nRefsMatchedInField > 0) {
                        sourceField = authRefDescendantField;
                        // Handle multiple fields matching in one Doc. See CSPACE-2863.
                    	if(nRefsFoundInDoc > 0) {
                    		// We already added ilistItem, so we need to clone that and add again
                    		if(ilistItem != null) {
                    			ilistItem = cloneAuthRefDocItem(ilistItem, sourceField);
                    		}
                    	} else {
                    		if(ilistItem != null) {
                    			ilistItem.setSourceField(sourceField);
                    		}
                    	}
                		if(ilistItem != null) {
                			list.add(ilistItem);
                		}
                		nRefsFoundInDoc += nRefsMatchedInField;
                    }

                } catch (ClientException ce) {
                    throw new RuntimeException(
                            "getAuthorityRefDocs: Problem fetching: " + sourceField, ce);
                }
            }
            if (nRefsFoundInDoc == 0) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: Could not find refname in object:"
                        + docType + ":" + NuxeoUtils.getCsid(docModel));
            }
            nRefsFoundTotal += nRefsFoundInDoc;
        }
        return nRefsFoundTotal;
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
     * If passed a new RefName, will set that into fields in which the old one was found.
     *
     * Only works for:
     * * Scalar fields
     * * Repeatable scalar fields (aka multi-valued fields)
     *
     * Does not work for:
     * * Structured fields (complexTypes)
     * * Repeatable structured fields (repeatable complexTypes)
     */
    private static int refNameFoundInField(String oldRefName, Property fieldValue, String newRefName) {
    	int nFound = 0;
    	if (fieldValue instanceof List) {
    		List<Property> fieldValueList = (List) fieldValue;
    		for (Property listItemValue : fieldValueList) {
    			try {
    				if ((listItemValue instanceof StringProperty)
    						&& oldRefName.equalsIgnoreCase((String)listItemValue.getValue())) {
    					nFound++;
        				if(newRefName!=null) {
        					fieldValue.setValue(newRefName);
        				} else {
        					// We cannot quit after the first, if we are replacing values.
        					// If we are just looking (not replacing), finding one is enough.
        					break;
        				}
    				}
    			} catch( PropertyException pe ) {}
    		}
    	} else {
    		try {
    			if ((fieldValue instanceof StringProperty)
    					&& oldRefName.equalsIgnoreCase((String)fieldValue.getValue())) {
					nFound++;
    				if(newRefName!=null) {
    					fieldValue.setValue(newRefName);
    				}
    			}
    		} catch( PropertyException pe ) {}
    	}
    	return nFound;
    }
}

