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
package org.collectionspace.services.vocabulary;

import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayload;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.AuthorityServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityIdentifierUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.vocabulary.nuxeo.VocabularyItemDocumentModelHandler;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;

@Path("/" + VocabularyClient.SERVICE_PATH_COMPONENT)
public class VocabularyResource extends 
	AuthorityResource<VocabulariesCommon, VocabularyItemDocumentModelHandler> {

	private enum Method {
        POST, PUT;
    }
	
    private final static String vocabularyServiceName = VocabularyClient.SERVICE_PATH_COMPONENT;

	private final static String VOCABULARIES_COMMON = "vocabularies_common";
    
    private final static String vocabularyItemServiceName = "vocabularyitems";
	private final static String VOCABULARYITEMS_COMMON = "vocabularyitems_common";

    final Logger logger = LoggerFactory.getLogger(VocabularyResource.class);

	public VocabularyResource() {
		super(VocabulariesCommon.class, VocabularyResource.class,
				VOCABULARIES_COMMON, VOCABULARYITEMS_COMMON);
	}

	@POST
    @Override
    public Response createAuthority(
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
    		String xmlPayload) {
    	//
    	// Requests to create new authorities come in on new threads. Unfortunately, we need to synchronize those threads on this block because, as of 8/27/2015, we can't seem to get Nuxeo
    	// transaction code to deal with a database level UNIQUE constraint violations on the 'shortidentifier' column of the vocabularies_common table.
    	// Therefore, to prevent having multiple authorities with the same shortid, we need to synchronize
    	// the code that creates new authorities.  The authority document model handler will first check for authorities with the same short id before
    	// trying to create a new authority.
    	//
    	synchronized(AuthorityResource.class) {
	        try {
	            PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
	            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
				RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = this.getRepositoryClient(ctx);
				
				CoreSessionInterface repoSession = repoClient.getRepositorySession(ctx);
				try {
		            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);		            
		            String csid = repoClient.create(ctx, handler);
		            //
		            // Handle any supplied list of items/terms
		            //
		            handleItemsPayload(Method.POST, ctx, csid, resourceMap, uriInfo, input);
		            UriBuilder path = UriBuilder.fromResource(resourceClass);
		            path.path("" + csid);
		            Response response = Response.created(path.build()).build();
		            return response;
	            } catch (Throwable t) {
	            	repoSession.setTransactionRollbackOnly();
	            	throw t;
	            } finally {
	            	repoClient.releaseRepositorySession(ctx, repoSession);
	            }
	        } catch (Exception e) {
	            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
	        }
    	}
    }
        
    @PUT
    @Path("{csid}")
    @Override
    public byte[] updateAuthority(
    		@Context Request request,
    		@Context ResourceMap resourceMap,
    		@Context UriInfo ui,
            @PathParam("csid") String specifier,
            String xmlPayload) {
        PoxPayloadOut result = null;
        try {
        	UriInfoWrapper uriInfo = new UriInfoWrapper(ui); // We need to make the queryParams maps read-write instead of read-only
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            Specifier spec = Specifier.getSpecifier(specifier, "updateAuthority", "UPDATE");
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(theUpdate, uriInfo);
			RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = this.getRepositoryClient(ctx);

			CoreSessionInterface repoSession = repoClient.getRepositorySession(ctx);
			try {
	            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
	            String csid;
	            if (spec.form == SpecifierForm.CSID) {
	                csid = spec.value;
	            } else {
	                String whereClause = RefNameServiceUtils.buildWhereForAuthByName(authorityCommonSchemaName, spec.value);
	                csid = getRepositoryClient(ctx).findDocCSID(null, ctx, whereClause);
	            }
	            getRepositoryClient(ctx).update(ctx, csid, handler);
	            if (handleItemsPayload(Method.PUT, ctx, csid, resourceMap, uriInfo, theUpdate) == true) {
	            	ctx.setOutput(new PoxPayloadOut(getServiceName())); // Clear the "vocabularies_common" result since we're going to create a new one with the items-list payload
	            	result = this.getAuthority(ctx, request, uriInfo, specifier, true);
	            } else {
	            	result = ctx.getOutput();
	            }
            } catch (Throwable t) {
            	repoSession.setTransactionRollbackOnly();
            	throw t;
            } finally {
            	repoClient.releaseRepositorySession(ctx, repoSession);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED);
        }
        return result.getBytes();
    }
    
    private void updateWithItemsPayload(
    		AbstractCommonList itemsList,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
    		String parentIdentifier,
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		PoxPayloadIn input) throws Exception {
    	
    	CoreSessionInterface repoSession = (CoreSessionInterface) existingCtx.getCurrentRepositorySession();
		Set<String> shortIdsInPayload = getListOfShortIds(itemsList); // record the list of existing or new terms/items

    	//
    	// First try to update and/or create items in the incoming payload
    	//
		for (ListItem item : itemsList.getListItem()) {
			String errMsg = null;
			boolean success = true;
			Response response = null;
			PoxPayloadOut payloadOut = null;
			PoxPayloadIn itemXmlPayload = getItemXmlPayload(item);
			String itemSpecifier = getSpecifier(item);
			if (itemSpecifier != null) {
				try {
					payloadOut = updateAuthorityItem(repoSession, resourceMap, uriInfo, parentIdentifier, itemSpecifier, itemXmlPayload);
					if (payloadOut == null) {
    					success = false;
    					errMsg = String.format("Could not update the term list payload of vocabuary '%s'.", parentIdentifier);
    				}
				} catch (DocumentNotFoundException dnf) {
					//
					// Since the item doesn't exist, we're being ask to create it
					//
        			response = this.createAuthorityItem(repoSession, resourceMap, uriInfo, parentIdentifier, itemXmlPayload);
        			if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
        				success = false;
    					errMsg = String.format("Could not create the term list payload of vocabuary '%s'.", parentIdentifier);
        			}
				}
			} else {
				//
				// Since the item was supplied with neither a CSID nor a short identifier, we'll assume we're being
				// asked to create it.
				//
    			response = this.createAuthorityItem(repoSession, resourceMap, uriInfo, parentIdentifier, itemXmlPayload);
    			if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
    				String shortId = getShortId(itemXmlPayload);
    				shortIdsInPayload.add(shortId); // add the new short ID to the list of incoming items
    			} else {
    				success = false;
					errMsg = String.format("Could not create the term list payload of vocabuary '%s'.", parentIdentifier);
    			}
			}
			//
			// Throw an exception as soon as we have problems with any item
			//
			if (success == false) {
				throw new DocumentException(errMsg);
			}
		}

		//
		// Next, delete the items that were omitted from the incoming payload
		//
		if (shouldDeleteOmittedItems(uriInfo) == true) {
			UriInfo uriInfoCopy = new UriInfoWrapper(uriInfo);
			String omittedItemAction = getOmittedItemAction(uriInfoCopy);
			long itemsProcessed = 0;
			long currentPage = 0;
			while (true) {
				AbstractCommonList abstractCommonList = this.getAuthorityItemList(existingCtx, parentIdentifier, uriInfoCopy);
				if (abstractCommonList != null && !Tools.isEmpty(abstractCommonList.getListItem())) {
					if (omittedItemAction.equalsIgnoreCase(VocabularyClient.DELETE_OMITTED_ITEMS)) {
						deleteAuthorityItems(existingCtx, abstractCommonList, shortIdsInPayload, parentIdentifier);
					} else {
						sotfDeleteAuthorityItems(existingCtx, abstractCommonList, shortIdsInPayload, parentIdentifier);
					}
				}
				itemsProcessed = itemsProcessed + abstractCommonList.getItemsInPage();
				if (itemsProcessed >= abstractCommonList.getTotalItems()) {
					break;
				}
				ArrayList<String> pageNum = new ArrayList<String>();
				pageNum.add(Long.toString(++currentPage));
				uriInfoCopy.getQueryParameters().put(IClientQueryParams.START_PAGE_PARAM, pageNum);
			}
		}
	}
    
    private String getShortId(PoxPayloadIn itemXmlPayload) {
		String result = null;
		
		VocabularyitemsCommon vocabularyItemsCommon = (VocabularyitemsCommon) itemXmlPayload.getPart(VOCABULARYITEMS_COMMON).getBody();
		result = vocabularyItemsCommon.getShortIdentifier();
		
		return result;
	}

	private void deleteAuthorityItems(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
    		AbstractCommonList abstractCommonList,
    		Set<String> shortIdsInPayload,
    		String parentIdentifier) throws Exception {
    	
		for (ListItem item : abstractCommonList.getListItem()) {
			String shortId = getShortId(item);
			if (shortIdsInPayload.contains(shortId) == false) {
				deleteAuthorityItem(existingCtx, parentIdentifier, getCsid(item), AuthorityServiceUtils.UPDATE_REV);
			}
		}    	
    }
    
    private void sotfDeleteAuthorityItems(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
    		AbstractCommonList abstractCommonList,
    		Set<String> shortIdsInPayload,
    		String parentIdentifier) throws Exception {
    	
		for (ListItem item : abstractCommonList.getListItem()) {
			String shortId = getShortId(item);
			if (shortIdsInPayload.contains(shortId) == false) {
				//deleteAuthorityItem(existingCtx, parentIdentifier, getCsid(item), AuthorityServiceUtils.UPDATE_REV);
				this.updateItemWorkflowWithTransition(existingCtx, parentIdentifier, getCsid(item), 
						WorkflowClient.WORKFLOWTRANSITION_DELETE, AuthorityServiceUtils.UPDATE_REV);
			}
		}    	
    }
        
    private boolean shouldDeleteOmittedItems(UriInfo uriInfo) throws DocumentException {
    	boolean result = false;
    	
		String omittedItemAction = getOmittedItemAction(uriInfo);		
		if (Tools.isEmpty(omittedItemAction) == false) {
			switch (omittedItemAction) {
				case VocabularyClient.DELETE_OMITTED_ITEMS:
				case VocabularyClient.SOFTDELETE_OMITTED_ITEMS:
					result = true;
					break;
				case VocabularyClient.IGNORE_OMITTED_ITEMS:
					// do nothing
					break;
				default:
					String msg = String.format("Unknown value '%s' for update on a vocabulary/termlist resource.", omittedItemAction);
					throw new DocumentException(msg);
			}
		}
		
		return result;
	}
    
    private String getOmittedItemAction(UriInfo uriInfo) {
		MultivaluedMap<String,String> queryParams = uriInfo.getQueryParameters();
		String omittedItemAction = queryParams.getFirst(VocabularyClient.OMITTED_ITEM_ACTION_QP);
		return omittedItemAction;
    }

	/*
     * Returns the set of short identifiers in the abstract common list of authority items
     */
    private Set<String> getListOfShortIds(AbstractCommonList itemsList) {
		HashSet<String> result = new HashSet<String>();
		
		for (ListItem item : itemsList.getListItem()) {
			String shortId = getShortId(item);
			if (Tools.isEmpty(shortId) == false) {
				result.add(shortId);
			}
		}
		
		return result;
	}

	private void createWithItemsPayload(
			AbstractCommonList itemsList,
			ServiceContext<PoxPayloadIn, 
			PoxPayloadOut> existingCtx, 
			String parentIdentifier, 
			ResourceMap resourceMap,
			UriInfo uriInfo, 
			PoxPayloadIn input) throws Exception {

		for (ListItem item : itemsList.getListItem()) {
			String errMsg = null;
			boolean success = true;
			Response response = null;
			PoxPayloadIn itemXmlPayload = getItemXmlPayload(item);

			CoreSessionInterface repoSession = (CoreSessionInterface) existingCtx.getCurrentRepositorySession();
			response = this.createAuthorityItem(repoSession, resourceMap, uriInfo, parentIdentifier, itemXmlPayload);
			if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
				success = false;
				errMsg = String.format("Could not create the term list payload of vocabuary '%s'.", parentIdentifier);
			}
			//
			// Throw an exception as soon as we have problems with any item
			//
			if (success == false) {
				throw new DocumentException(errMsg);
			}
		}
	}   
    
    private boolean handleItemsPayload(
    		Method method,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
    		String parentIdentifier,
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		PoxPayloadIn input) throws Exception {
    	boolean result = false;
    	
    	PayloadInputPart abstractCommonListPart  = input.getPart(PoxPayload.ABSTRACT_COMMON_LIST_ROOT_ELEMENT_LABEL);
    	if (abstractCommonListPart != null) {
    		AbstractCommonList itemsList = (AbstractCommonList) abstractCommonListPart.getBody();
			switch (method) {
    			case POST:
		            createWithItemsPayload(itemsList, existingCtx, parentIdentifier, resourceMap, uriInfo, input);
        			break;
    			case PUT:
		            updateWithItemsPayload(itemsList, existingCtx, parentIdentifier, resourceMap, uriInfo, input);
        			break;	        			
			}
			result = true; // mark that we've handled an items-list payload
    	}
    	
    	return result;
	}
    
    private String getFieldValue(ListItem item, String lookingFor) {
    	String result = null;
    	
		for (Element ele : item.getAny()) {
			String fieldName = ele.getTagName();
			String fieldValue = ele.getTextContent();
			if (fieldName.equalsIgnoreCase(lookingFor)) {
				result = fieldValue;
				break;
			}
		}    	
    	
    	return result;
    }
    
    public String getCsid(ListItem item) {
    	return getFieldValue(item, "csid");
    }
    
    private String getShortId(ListItem item) {
    	return getFieldValue(item, "shortIdentifier");
    }
    
    private String getDisplayName(ListItem item) {
    	return getFieldValue(item, "displayName");
    }
    
    /**
     * We'll return null if we can create a specifier from the list item.
     * 
     * @param item
     * @return
     */
    private String getSpecifier(ListItem item) {
		String result = null;

		String csid = result = getCsid(item);
		if (csid == null) {
			String shortId = getShortId(item);			
			if (shortId != null) {
				result = Specifier.createShortIdURNValue(shortId);
			}
		}

		return result;
	}

	/**
     * This is very brittle.  If the class VocabularyitemsCommon changed with new fields we'd have to
     * update this method.
     * 
     * @param item
     * @return
     * @throws DocumentException 
     */
	private PoxPayloadIn getItemXmlPayload(ListItem item) throws DocumentException {
		PoxPayloadIn result = null;

		VocabularyitemsCommon vocabularyItem = new VocabularyitemsCommon();
		for (Element ele : item.getAny()) {
			String fieldName = ele.getTagName();
			String fieldValue = ele.getTextContent();
			switch (fieldName) {
				case "displayName":
					vocabularyItem.setDisplayName(fieldValue);
					break;
					
				case "shortIdentifier":
					vocabularyItem.setShortIdentifier(fieldValue);
					break;
					
				case "order":
					vocabularyItem.setOrder(fieldValue);
					break;
					
				case "source":
					vocabularyItem.setSource(fieldValue);
					break;
					
				case "sourcePage":
					vocabularyItem.setSourcePage(fieldValue);
					break;
					
				case "description":
					vocabularyItem.setDescription(fieldValue);
					break;
					
				case "csid":
					vocabularyItem.setCsid(fieldValue);
					break;
					
				case "termStatus":
					vocabularyItem.setTermStatus(fieldValue);
					break;

				default:
					// ignore other fields
					break;
			}
		}
		//
		// We need to create a short ID if one wasn't supplied
		//
		if (Tools.isEmpty(vocabularyItem.getShortIdentifier())) {
			vocabularyItem.setShortIdentifier(AuthorityIdentifierUtils.generateShortIdentifierFromDisplayName(
					vocabularyItem.getDisplayName() , null)); ;
		}
		
		result = new PoxPayloadIn(VocabularyClient.SERVICE_ITEM_PAYLOAD_NAME, vocabularyItem, 
    			VOCABULARYITEMS_COMMON);

		return result; 
	}
    
	private Response createAuthorityItem(
    		CoreSessionInterface repoSession,
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		String parentIdentifier, // Either a CSID or a URN form -e.g., a8ad38ec-1d7d-4bf2-bd31 or urn:cspace:name(bugsbunny)
    		PoxPayloadIn input) throws Exception {
    	Response result = null;
    	
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), input, resourceMap, uriInfo);
        ctx.setCurrentRepositorySession(repoSession);
        
        result = createAuthorityItem(ctx, parentIdentifier, AuthorityServiceUtils.UPDATE_REV,
        		AuthorityServiceUtils.PROPOSED, AuthorityServiceUtils.NOT_SAS_ITEM);

        return result;
    }
	
	private PoxPayloadOut updateAuthorityItem(
    		CoreSessionInterface repoSession,
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		String parentSpecifier, // Either a CSID or a URN form -e.g., a8ad38ec-1d7d-4bf2-bd31 or urn:cspace:name(bugsbunny)
    		String itemSpecifier, 	// Either a CSID or a URN form.
    		PoxPayloadIn theUpdate) throws Exception {
    	PoxPayloadOut result = null;
    	
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), theUpdate, resourceMap, uriInfo);
        ctx.setCurrentRepositorySession(repoSession);
        
        result = updateAuthorityItem(ctx, resourceMap, uriInfo, parentSpecifier, itemSpecifier, theUpdate,
        		AuthorityServiceUtils.UPDATE_REV,			// passing TRUE so rev num increases, passing
        		AuthorityServiceUtils.NO_CHANGE,	// don't change the state of the "proposed" field -we could be performing a sync or just a plain update
        		AuthorityServiceUtils.NO_CHANGE);	// don't change the state of the "sas" field -we could be performing a sync or just a plain update

        return result;
    }

	@GET
    @Path("{csid}")
    @Override
    public Response get(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String specifier) {
    	Response result = null;
    	uriInfo = new UriInfoWrapper(uriInfo);
        
        try {
        	MultivaluedMap<String,String> queryParams = uriInfo.getQueryParameters();
        	String showItemsValue = (String)queryParams.getFirst(VocabularyClient.SHOW_ITEMS_QP);
            boolean showItems = Tools.isTrue(showItemsValue);
            if (showItems == true) {
            	//
            	// We'll honor paging params if we find any; otherwise we'll set the page size to 0 to get ALL the items
            	//
            	if (queryParams.containsKey(IClientQueryParams.PAGE_SIZE_PARAM) == false) {
            		queryParams.add(IClientQueryParams.PAGE_SIZE_PARAM, "0");
            	}
            }

            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(request, uriInfo);
            PoxPayloadOut payloadout = getAuthority(ctx, request, uriInfo, specifier, showItems);
            result = buildResponse(ctx, payloadout);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, specifier);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "GET request failed. The requested Authority specifier:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new CSWebApplicationException(response);
        }

        return result;
    }
    
    @Override
    public String getServiceName() {
        return vocabularyServiceName;
    }

    @Override
    public String getItemServiceName() {
        return vocabularyItemServiceName;
    }
    
	@Override
	public Class<VocabulariesCommon> getCommonPartClass() {
		return VocabulariesCommon.class;
	}

    /**
     * @return the name of the property used to specify references for items in this type of
     * authority. For most authorities, it is ServiceBindingUtils.AUTH_REF_PROP ("authRef").
     * Some types (like Vocabulary) use a separate property.
     */
	@Override
    protected String getRefPropName() {
    	return ServiceBindingUtils.TERM_REF_PROP;
    }
	
	@Override
	protected String getOrderByField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
		String result = null;

		result = ctx.getCommonPartLabel() + ":" + AuthorityItemJAXBSchema.DISPLAY_NAME;

		return result;
	}
	
	@Override
	protected String getPartialTermMatchField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
		return getOrderByField(ctx);
	}

	/*
	 * The item schema for the Vocabulary service does not support a multi-valued term list.  Only authorities that support
	 * term lists need to implement this method.
	 */
	@Override
	public String getItemTermInfoGroupXPathBase() {
		return null;
	}
}
