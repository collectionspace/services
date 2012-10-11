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
package org.collectionspace.services.servicegroup.nuxeo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.collectionspace.services.ServiceGroupListItemJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.servicegroup.ServicegroupsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceGroupDocumentModelHandler 
	extends DocHandlerBase<ServicegroupsCommon> {
	
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected static final int NUM_META_FIELDS = 3;
    protected static final String DOC_TYPE_FIELD = "docType";
    protected static final String DOC_NUMBER_FIELD = "docNumber";
    protected static final String DOC_NAME_FIELD = "docName";

    public AbstractCommonList getItemsForGroup(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		List<String> serviceGroupNames) throws Exception {
        CommonList commonList = new CommonList();
        AbstractCommonList list = (AbstractCommonList)commonList;
    	RepositoryInstance repoSession = null;
    	boolean releaseRepoSession = false;
        
    	try { 
    		RepositoryJavaClientImpl repoClient = (RepositoryJavaClientImpl)this.getRepositoryClient(ctx);
    		repoSession = this.getRepositorySession();
    		if (repoSession == null) {
    			repoSession = repoClient.getRepositorySession(ctx);
    			releaseRepoSession = true;
    		}
            DocumentFilter myFilter = getDocumentFilter();
	        // Make sure we pick up workflow state, etc. 
	        int pageSize = myFilter.getPageSize();
	        int pageNum = myFilter.getStartPage();
	        list.setPageNum(pageNum);
	        list.setPageSize(pageSize);

    		try {
    	        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
    	        RepositoryJavaClientImpl nuxeoRepoClient = (RepositoryJavaClientImpl)repoClient;
    	        // Get the service bindings for this tenant
    	        TenantBindingConfigReaderImpl tReader =
    	                ServiceMain.getInstance().getTenantBindingConfigReader();
    	        // We need to get all the procedures, authorities, and objects.
    	        List<ServiceBindingType> servicebindings = 
    	        		tReader.getServiceBindingsByType(ctx.getTenantId(), serviceGroupNames);
    	        if (servicebindings == null || servicebindings.isEmpty()) {
                    Response response = Response.status(Response.Status.NOT_FOUND).entity(
                            ServiceMessages.READ_FAILED + 
                            ServiceMessages.resourceNotFoundMsg(implode(serviceGroupNames, ","))).type("text/plain").build();
                    throw new WebApplicationException(response);
    	        }
    	        servicebindings = SecurityUtils.getReadableServiceBindingsForCurrentUser(servicebindings);
    	        // Build the list of docTypes for allowed serviceBindings
    	        ArrayList<String> docTypes = new ArrayList<String>();
            	for(ServiceBindingType binding:servicebindings) {
            		ServiceObjectType serviceObj = binding.getObject();
            		if(serviceObj!=null) {
    	                String docType = serviceObj.getName();
                		docTypes.add(docType);
                        queriedServiceBindings.put(docType, binding);
            		}
            	}
            	
            	// This should be "Document" but CMIS is gagging on that right now.
            	ctx.getQueryParams().add(IQueryManager.SELECT_DOC_TYPE_FIELD, "CollectionSpaceDocument");
    	        
    	        // Now we have to issue the search
            	// findDocs qill build a QueryContext, which wants to see a docType for our context
            	ctx.setDocumentType("Document");
    	        DocumentWrapper<DocumentModelList> docListWrapper = 
    	        		nuxeoRepoClient.findDocs(ctx, this, repoSession, docTypes );
    	        // Now we gather the info for each document into the list and return
    	        DocumentModelList docList = docListWrapper.getWrappedObject();
    	        
    	        if (docList == null) { // found no authRef fields - nothing to process
    	            return list;
    	        }
    	        processDocList(docList, queriedServiceBindings, commonList);
    	        list.setItemsInPage(docList.size());
    	        list.setTotalItems(docList.totalSize());
    		} catch (DocumentException de) {
    			throw de;
    		} catch (Exception e) {
    			if (logger.isDebugEnabled()) {
    				logger.debug("Caught exception ", e);
    			}
    			throw new DocumentException(e);
    		} finally {
    			if (releaseRepoSession && repoSession != null) {
    				repoClient.releaseRepositorySession(ctx, repoSession);
    			}
    		}
    	} catch (Exception e) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("Caught exception ", e);
    		}
    		throw new DocumentException(e);
    	}	        
        return list;
    }
    
    // Move this to a Utils class!
    public static String implode(List<String> stringList, String sep) {
    	StringBuilder sb = new StringBuilder();
    	boolean fFirst = false;
    	for(String name:stringList) {
    		if(fFirst) {
    			fFirst = false;
    		} else {
    			sb.append(sep);
    		}
    		sb.append(name);
    	}
    	return sb.toString();
    }
    
    private String getUriFromServiceBinding(ServiceBindingType sb, String csid) {
        return "/" + sb.getName().toLowerCase() + "/" + csid;
    }
    
    private void processDocList(	
		DocumentModelList docList,
		Map<String, ServiceBindingType> queriedServiceBindings,
		CommonList list ) {
        int nFields = NUM_META_FIELDS+NUM_STANDARD_LIST_RESULT_FIELDS;
        String fields[] = new String[nFields];
        fields[0] = STANDARD_LIST_CSID_FIELD;
        fields[1] = STANDARD_LIST_URI_FIELD;
        fields[2] = STANDARD_LIST_UPDATED_AT_FIELD;
        fields[3] = STANDARD_LIST_WORKFLOW_FIELD;
        fields[4] = DOC_NAME_FIELD;
        fields[5] = DOC_NUMBER_FIELD;
        fields[6] = DOC_TYPE_FIELD;
        list.setFieldsReturned(fields);
        Iterator<DocumentModel> iter = docList.iterator();
		HashMap<String, Object> item = new HashMap<String, Object>();
        while (iter.hasNext()) {
            DocumentModel docModel = iter.next();
            String docType = docModel.getDocumentType().getName();
            docType = ServiceBindingUtils.getUnqualifiedTenantDocType(docType);
            ServiceBindingType sb = queriedServiceBindings.get(docType);
            if (sb == null) {
                throw new RuntimeException(
                        "processDocList: No Service Binding for docType: " + docType);
            }
            String csid = NuxeoUtils.getCsid(docModel);
            item.put(STANDARD_LIST_CSID_FIELD, csid);
            // Need to get the URI for the document, by it's type.
            item.put(STANDARD_LIST_URI_FIELD, getUriFromServiceBinding(sb, csid));
            try {
            	item.put(STANDARD_LIST_UPDATED_AT_FIELD, getUpdatedAtAsString(docModel));
                item.put(STANDARD_LIST_WORKFLOW_FIELD, docModel.getCurrentLifeCycleState());
            } catch(Exception e) {
            	logger.error("Error getting core values for doc ["+csid+"]: "+e.getLocalizedMessage());
            }

            String value = ServiceBindingUtils.getMappedFieldInDoc(sb, 
            						ServiceBindingUtils.OBJ_NUMBER_PROP, docModel);
            item.put(DOC_NUMBER_FIELD, value);
            value = ServiceBindingUtils.getMappedFieldInDoc(sb, 
            						ServiceBindingUtils.OBJ_NAME_PROP, docModel);
            item.put(DOC_NAME_FIELD, value);
            item.put(DOC_TYPE_FIELD, docType);
            
            list.addItem(item);
            item.clear();
        }

    }
    

}

