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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistry;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.AbstractServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.query.nuxeo.QueryManagerNuxeoImpl;
import org.collectionspace.services.common.relation.nuxeo.RelationsUtils;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.servicegroup.ServicegroupsCommon;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceGroupDocumentModelHandler 
	extends NuxeoDocumentModelHandler<ServicegroupsCommon> {
	
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected static final int NUM_META_FIELDS = 4; // the number of meta fields -i.e., DOC_TYPE_FIELD, DOC_NUMBER_FIELD, DOC_NAME_FIELD, STANDARD_LIST_MARK_RT_FIELD
    protected static final String DOC_TYPE_FIELD = "docType";
    protected static final String DOC_NUMBER_FIELD = "docNumber";
    protected static final String DOC_NAME_FIELD = "docName";
	protected static final String STANDARD_LIST_MARK_RT_FIELD = "related";
    
    //
    // Returns a service payload for an authority item
    //
    private PoxPayloadOut getAuthorityItem(ServiceContext ctx, String termRefName) throws Exception {
    	PoxPayloadOut result = null;
    	
    	RefName.AuthorityItem item = RefName.AuthorityItem.parse(termRefName, true);
    	AuthorityResource authorityResource = (AuthorityResource) ctx.getResourceMap().get(item.inAuthority.resource);
    	
    	AuthorityTermInfo authorityTermInfo = RefNameUtils.parseAuthorityTermInfo(termRefName);
    	String parentIdentifier = Specifier.createShortIdURNValue(authorityTermInfo.inAuthority.name);
    	String itemIdentifier = Specifier.createShortIdURNValue(authorityTermInfo.name);
    	
    	result = authorityResource.getAuthorityItemWithExistingContext(ctx, parentIdentifier, itemIdentifier);
    	
    	return result;
    }

    public PoxPayloadOut getResourceItemForCsid(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		List<String> serviceGroupNames,
    		String csid) throws DocumentException {
    	PoxPayloadOut result = null;
        CoreSessionInterface repoSession = null;
    	boolean releaseRepoSession = false;
        
    	try { 
    		NuxeoRepositoryClientImpl repoClient = (NuxeoRepositoryClientImpl)this.getRepositoryClient(ctx);
    		repoSession = this.getRepositorySession();
    		if (repoSession == null) {
    			repoSession = repoClient.getRepositorySession(ctx);
    			releaseRepoSession = true;
    		}
    		try {
    	        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
    	        DocumentModelList docList = this.getDocListForGroup(ctx, serviceGroupNames, queriedServiceBindings,
    	        		repoSession, repoClient);
    	        if (docList == null || docList.isEmpty()) { // found no authRef fields - nothing to process
    	            throw new DocumentNotFoundException();
    	        }
    	        DocumentModel docModel = docList.get(0);
    	        //
    	        // Determine if the docModel is an authority term, object, or some other procedure record.
    	        //
    	    	String termRefName = (String) NuxeoUtils.getProperyValue(docModel, CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
    	        if (isAuthorityTermDocument(termRefName) == true) {
                	result = getAuthorityItem(ctx, termRefName);
    	        } else {    	        
	                TenantBindingConfigReaderImpl bindingReader = ServiceMain.getInstance().getTenantBindingConfigReader();
	                String serviceName = ServiceBindingUtils.getServiceNameFromObjectName(bindingReader, ctx.getTenantId(),
	                		docModel.getDocumentType().getName());
	                NuxeoBasedResource resource = (NuxeoBasedResource) ctx.getResourceMap().get(serviceName);
                	result = resource.getWithParentCtx(ctx, csid);
    	        }
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
    	
        return result;
    }
    
    private boolean isAuthorityTermDocument(String termRefName) {
		boolean result = true;
		
		try {
	    	//String inAuthorityCsid = (String) NuxeoUtils.getProperyValue(docModel, "inAuthority"); //docModel.getPropertyValue("inAuthority"); // AuthorityItemJAXBSchema.IN_AUTHORITY
	    	//String refName = (String) NuxeoUtils.getProperyValue(docModel, CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
	    	RefName.AuthorityItem item = RefName.AuthorityItem.parse(termRefName, true);
		} catch (IllegalArgumentException e) {
			result = false;
		}
		
    	return result;
	}

	private DocumentModelList getDocListForGroup(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		List<String> serviceGroupNames,
    		Map<String, ServiceBindingType> queriedServiceBindings,
    		CoreSessionInterface repoSession,
    		NuxeoRepositoryClientImpl repoClient) throws Exception {
        
        NuxeoRepositoryClientImpl nuxeoRepoClient = (NuxeoRepositoryClientImpl)repoClient;
        // Get the service bindings for this tenant
        TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        // We need to get all the procedures, authorities, and objects.
        List<ServiceBindingType> servicebindings = 
        		tReader.getServiceBindingsByType(ctx.getTenantId(), serviceGroupNames);
        if (servicebindings == null || servicebindings.isEmpty()) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + 
                    ServiceMessages.resourceNotFoundMsg(implode(serviceGroupNames, ","))).type("text/plain").build();
            throw new CSWebApplicationException(response);
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
    	
    	// This should be type "Document" but CMIS is gagging on that right now.
    	ctx.getQueryParams().add(IQueryManager.SELECT_DOC_TYPE_FIELD, QueryManagerNuxeoImpl.COLLECTIONSPACE_DOCUMENT_TYPE);
        
        // Now we have to issue the search
    	// The findDocs() method will build a QueryContext, which wants to see a docType for our context
    	ctx.setDocumentType(QueryManagerNuxeoImpl.NUXEO_DOCUMENT_TYPE);
        DocumentWrapper<DocumentModelList> docListWrapper = 
        		nuxeoRepoClient.findDocs(ctx, this, repoSession, docTypes );
        // Now we gather the info for each document into the list and return
        DocumentModelList docList = docListWrapper.getWrappedObject();
    	
        return docList;
    }
    
    public AbstractCommonList getItemListForGroup(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		List<String> serviceGroupNames) throws Exception {
        CommonList commonList = new CommonList();
        AbstractCommonList list = (AbstractCommonList)commonList;
        CoreSessionInterface repoSession = null;
    	boolean releaseRepoSession = false;
        
    	try { 
            DocumentFilter myFilter = getDocumentFilter();
	        int pageSize = myFilter.getPageSize();
	        int pageNum = myFilter.getStartPage();
	        list.setPageNum(pageNum);
	        list.setPageSize(pageSize);

    		NuxeoRepositoryClientImpl repoClient = (NuxeoRepositoryClientImpl)this.getRepositoryClient(ctx);
    		repoSession = this.getRepositorySession();
    		if (repoSession == null) {
    			repoSession = repoClient.getRepositorySession(ctx);
    			releaseRepoSession = true;
    		}
    		try {
    	        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
    	        DocumentModelList docList = this.getDocListForGroup(ctx, serviceGroupNames, queriedServiceBindings,
    	        		repoSession, repoClient);
    	        if (docList == null) { // found no authRef fields - nothing to process
    	            return list;
    	        }
    	        processDocList(ctx, docList, queriedServiceBindings, commonList);
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
    	for (String name:stringList) {
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
    
    private void processDocList(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			DocumentModelList docList,
			Map<String, ServiceBindingType> queriedServiceBindings,
			CommonList list) throws DocumentException {
    	
		String tenantId = ctx.getTenantId();
		CoreSessionInterface repoSession = null;
		NuxeoRepositoryClientImpl repoClient = null;
		boolean releaseRepoSession = false;
		
		MultivaluedMap<String, String> queryParams = getServiceContext().getQueryParams();
		String markRtSbj = queryParams.getFirst(IQueryManager.MARK_RELATED_TO_CSID_AS_SUBJECT);
		if (Tools.isBlank(markRtSbj)) {
			markRtSbj = null;
		}
		
		String markRtSbjOrObj = queryParams.getFirst(IQueryManager.MARK_RELATED_TO_CSID_AS_EITHER);
		if (Tools.isBlank(markRtSbjOrObj)) {
			markRtSbjOrObj = null;
		} else {
			if (Tools.isBlank(markRtSbj) == false) {
				logger.warn(String.format("Ignoring query param %s='%s' since overriding query param %s='%s' exists.",
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
	    				
	        String fields[] = new String[NUM_META_FIELDS + NUM_STANDARD_LIST_RESULT_FIELDS];
	        fields[0] = STANDARD_LIST_CSID_FIELD;
	        fields[1] = STANDARD_LIST_URI_FIELD;
	        fields[2] = STANDARD_LIST_UPDATED_AT_FIELD;
	        fields[3] = STANDARD_LIST_WORKFLOW_FIELD;
	        fields[4] = STANDARD_LIST_REFNAME_FIELD;
	        fields[5] = DOC_NAME_FIELD;
	        fields[6] = DOC_NUMBER_FIELD;
	        fields[7] = DOC_TYPE_FIELD;	        
			if (markRtSbj != null) {
		        fields[8] = STANDARD_LIST_MARK_RT_FIELD;
			}

	        list.setFieldsReturned(fields);
	        
	        Iterator<DocumentModel> iter = docList.iterator();
			HashMap<String, Object> item = new HashMap<String, Object>();
	        while (iter.hasNext()) {
	            DocumentModel docModel = iter.next();
	            String docType = docModel.getDocumentType().getName();
	            docType = ServiceBindingUtils.getUnqualifiedTenantDocType(docType);
	            ServiceBindingType sb = queriedServiceBindings.get(docType);
	            if (sb == null) {
	                throw new RuntimeException("processDocList: No Service Binding for docType: " + docType);
	            }
	            
	            String csid = NuxeoUtils.getCsid(docModel);
	            item.put(STANDARD_LIST_CSID_FIELD, csid);

				//
				// If the mark-related query param was set, check to see if the doc we're processing
				// is related to the value specified in the mark-related query param.
				//	            
				if (markRtSbj != null) {
					String relationClause = RelationsUtils.buildWhereClause(markRtSbj, null, null, csid, null, markRtSbj == markRtSbjOrObj);
					String whereClause = relationClause + IQueryManager.SEARCH_QUALIFIER_AND
							+ NuxeoUtils.buildWorkflowNotDeletedWhereClause();
					QueryContext queryContext = new QueryContext(ctx, whereClause);
					queryContext.setDocType(IRelationsManager.DOC_TYPE);
					String query = NuxeoUtils.buildNXQLQuery(queryContext);
					// Search for 1 relation that matches. 1 is enough to fail
					// the filter
					DocumentModelList queryDocList = repoSession.query(query, null, 1, 0, false);
					item.put(STANDARD_LIST_MARK_RT_FIELD, queryDocList.isEmpty() ? "false" : "true");
				}

	            UriTemplateRegistry uriTemplateRegistry = ServiceMain.getInstance().getUriTemplateRegistry();            
	            StoredValuesUriTemplate storedValuesResourceTemplate = uriTemplateRegistry.get(new UriTemplateRegistryKey(tenantId, docType));
		 	    Map<String, String> additionalValues = new HashMap<String, String>();
		 	    if (storedValuesResourceTemplate.getUriTemplateType() == UriTemplateFactory.ITEM) {
	                try {
	                    String inAuthorityCsid = (String) NuxeoUtils.getProperyValue(docModel, "inAuthority"); //docModel.getPropertyValue("inAuthority"); // AuthorityItemJAXBSchema.IN_AUTHORITY
	                    additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, inAuthorityCsid);
	                    additionalValues.put(UriTemplateFactory.ITEM_IDENTIFIER_VAR, csid);
	                } catch (Exception e) {
	                	String msg = String.format("Could not extract inAuthority property from authority item with CSID = ", docModel.getName());
	                    logger.warn(msg, e);
	                }
		 	    } else {
	                additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, csid);
	            }
		 	    
	            String uriStr = storedValuesResourceTemplate.buildUri(additionalValues);
	            item.put(STANDARD_LIST_URI_FIELD, uriStr);
	            try {
	            	item.put(STANDARD_LIST_UPDATED_AT_FIELD, getUpdatedAtAsString(docModel));
	                item.put(STANDARD_LIST_WORKFLOW_FIELD, docModel.getCurrentLifeCycleState());
	                item.put(STANDARD_LIST_REFNAME_FIELD, getRefname(docModel));
	            } catch(Exception e) {
	            	logger.error("Error getting core values for doc ["+csid+"]: "+e.getLocalizedMessage());
	            }
	
	            String value = ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NUMBER_PROP, docModel);
	            if (value != null) {
	            	item.put(DOC_NUMBER_FIELD, value);
	            }
	            
	            value = ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NAME_PROP, docModel);
	            if (value != null) {
	            	item.put(DOC_NAME_FIELD, value);
	            }
	            
	            item.put(DOC_TYPE_FIELD, docType);
	            // add the item to the list
	            list.addItem(item);
	            item.clear();
	        }
	    } catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			// If we got/acquired a new session then we're responsible for releasing it.
			if (releaseRepoSession && repoSession != null) {
				repoClient.releaseRepositorySession(ctx, repoSession);
			}
		}
    }
}

