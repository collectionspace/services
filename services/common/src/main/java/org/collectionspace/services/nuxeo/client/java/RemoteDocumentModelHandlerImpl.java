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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.collectionspace.services.authorization.AccountPermission;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.context.JaxRsContext;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.common.relation.RelationResource;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthRefConfigInfo;
import org.collectionspace.services.config.service.DocHandlerParams;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.collectionspace.services.relation.RelationshipType;
import org.dom4j.Element;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoteDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 * @param <T> 
 * @param <TL> 
 */
public abstract class   RemoteDocumentModelHandlerImpl<T, TL>
        extends DocumentModelHandler<T, TL> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RemoteDocumentModelHandlerImpl.class);
    private final static String CR = "\r\n";
    private final static String EMPTYSTR = "";
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#setServiceContext(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public void setServiceContext(ServiceContext ctx) {  //FIXME: Apply proper generics to ServiceContext<PoxPayloadIn, PoxPayloadOut>
        if (ctx instanceof MultipartServiceContext) {
            super.setServiceContext(ctx);
        } else {
            throw new IllegalArgumentException("setServiceContext requires instance of "
                    + MultipartServiceContext.class.getName());
        }
    }
    
    @Override
    protected String getRefnameDisplayName(DocumentWrapper<DocumentModel> docWrapper) {
    	return getRefnameDisplayName(docWrapper.getWrappedObject());
    }
    	
	private String getRefnameDisplayName(DocumentModel docModel) { // Look in the tenant bindings to see what field should be our display name for our refname value
		String result = null;
		ServiceContext ctx = this.getServiceContext();
		
    	DocHandlerParams.Params params = null;
    	try {
			params = ServiceConfigUtils.getDocHandlerParams(ctx);
			ListResultField field = params.getRefnameDisplayNameField();
			
			String schema = field.getSchema();
			if (schema == null || schema.trim().isEmpty()) {
				schema = ctx.getCommonPartLabel();
			}
			
			result = getStringValue(docModel, schema, field);
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn(String.format("Call failed to getRefnameDisplayName() for class %s", this.getClass().getName()));
			}
		}
		
		return result;
	}
	
    @Override
    public boolean supportsHierarchy() {
    	boolean result = false;
    	
    	DocHandlerParams.Params params = null;
    	try {
        	ServiceContext ctx = this.getServiceContext();
			params = ServiceConfigUtils.getDocHandlerParams(ctx);
			Boolean bool = params.isSupportsHierarchy();
			if (bool != null) {
				result = bool.booleanValue();
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			String errMsg = String.format("Could not get document handler params from config bindings for class %s", this.getClass().getName());
			if (logger.isWarnEnabled() == true) {
				logger.warn(errMsg);
			}
		}
    	
    	return result;
    }

	@Override
	public void handleWorkflowTransition(DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef)
			throws Exception {
		// Do nothing by default, but children can override if they want.  The really workflow transition happens in the WorkflowDocumemtModelHandler class
	}
    	
    @Override
    public void completeCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.completeCreate(wrapDoc);
        if (supportsHierarchy() == true) {
        	handleRelationsPayload(wrapDoc, false);
        }
    }
	
    /* NOTE: The authority item doc handler overrides (after calling) this method.  It performs refName updates.  In this
     * method we just update any and all relationship records that use refNames that have changed.
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        // We need to return at least those document part(s) and corresponding payloads that were received
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        PoxPayloadIn input = ctx.getInput();
        if (input != null) {
	        List<PayloadInputPart> inputParts = ctx.getInput().getParts();
	        for (PayloadInputPart part : inputParts) {
	            String partLabel = part.getLabel();
                try{
                    ObjectPartType partMeta = partsMetaMap.get(partLabel);
                    // CSPACE-4030 - generates NPE if the part is missing.
                    if(partMeta!=null) {
	                    Map<String, Object> unQObjectProperties = extractPart(docModel, partLabel, partMeta);
	                    if(unQObjectProperties!=null) {
	                    	addOutputPart(unQObjectProperties, partLabel, partMeta);
	                    }
                    }
                } catch (Throwable t){
                    logger.error("Unable to addOutputPart: " + partLabel
                                               + " in serviceContextPath: "+this.getServiceContextPath()
                                               + " with URI: " + this.getServiceContext().getUriInfo().getPath()
                                               + " error: " + t);
                }
	        }
        } else {
        	if (logger.isWarnEnabled() == true) {
        		logger.warn("MultipartInput part was null for document id = " +
        				docModel.getName());
        	}
        }
        //
        //  If the resource's service supports hierarchy then we need to perform a little more work
        //
        if (supportsHierarchy() == true) {
            handleRelationsPayload(wrapDoc, true); // refNames in relations payload should refer to pre-updated record refName value
            handleRefNameReferencesUpdate(); // if our refName changed, we need to update any and all relationship records that used the old one
        }
    }

    /**
     * Adds the output part.
     *
     * @param unQObjectProperties the un q object properties
     * @param schema the schema
     * @param partMeta the part meta
     * @throws Exception the exception
     * MediaType.APPLICATION_XML_TYPE
     */
    protected void addOutputPart(Map<String, Object> unQObjectProperties, String schema, ObjectPartType partMeta)
            throws Exception {
        Element doc = DocumentUtils.buildDocument(partMeta, schema,
                unQObjectProperties);
        if (logger.isTraceEnabled() == true) {
            logger.trace(doc.asXML());
        }
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        ctx.addOutputPart(schema, doc, partMeta.getContent().getContentType());
    }
    
    /**
     * Extract paging info.
     *
     * @param commonsList the commons list
     * @return the tL
     * @throws Exception the exception
     */
    public TL extractPagingInfo(TL theCommonList, DocumentWrapper<DocumentModelList> wrapDoc)
            throws Exception {
        AbstractCommonList commonList = (AbstractCommonList) theCommonList;

        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        DocumentModelList docList = wrapDoc.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        commonList.setItemsInPage(docList.size());
        // set the total result size
        commonList.setTotalItems(docList.totalSize());

        return (TL) commonList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractAllParts(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {

        DocumentModel docModel = wrapDoc.getWrappedObject();
        String[] schemas = docModel.getDeclaredSchemas();
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        for (String schema : schemas) {
            ObjectPartType partMeta = partsMetaMap.get(schema);
            if (partMeta == null) {
                continue; // unknown part, ignore
            }
            Map<String, Object> unQObjectProperties = extractPart(docModel, schema, partMeta);
            if(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA.equals(schema)) {
            	addExtraCoreValues(docModel, unQObjectProperties);
            }
            addOutputPart(unQObjectProperties, schema, partMeta);
        }
        
        if (supportsHierarchy() == true) {
            MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
            String showSiblings = ctx.getQueryParams().getFirst(CommonAPI.showSiblings_QP);
            if (Tools.isTrue(showSiblings)) {
                showSiblings(wrapDoc, ctx);
                return;   // actual result is returned on ctx.addOutputPart();
            }

            String showRelations = ctx.getQueryParams().getFirst(CommonAPI.showRelations_QP);
            if (Tools.isTrue(showRelations)) {
                showRelations(wrapDoc, ctx);
                return;   // actual result is returned on ctx.addOutputPart();
            }

            String showAllRelations = ctx.getQueryParams().getFirst(CommonAPI.showAllRelations_QP);
            if (Tools.isTrue(showAllRelations)) {
                showAllRelations(wrapDoc, ctx);
                return;   // actual result is returned on ctx.addOutputPart();
            }
        }
        
        addAccountPermissionsPart();
    }
    
    private void addExtraCoreValues(DocumentModel docModel, Map<String, Object> unQObjectProperties)
    		throws Exception {
        unQObjectProperties.put(CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE, docModel.getCurrentLifeCycleState());
    }
    
    private void addAccountPermissionsPart() throws Exception {
    	Profiler profiler = new Profiler("addAccountPermissionsPart():", 1);
    	profiler.start();
    	
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        String currentServiceName = ctx.getServiceName();
        String workflowSubResource = "/";
        JaxRsContext jaxRsContext = ctx.getJaxRsContext();
        if (jaxRsContext != null) { // If not null then we're dealing with an authority item
        	String resourceName = SecurityUtils.getResourceName(jaxRsContext.getUriInfo());
        	workflowSubResource = workflowSubResource + resourceName + WorkflowClient.SERVICE_PATH + "/";
        } else {
        	workflowSubResource = workflowSubResource + currentServiceName + WorkflowClient.SERVICE_AUTHZ_SUFFIX;
        }
        AccountPermission accountPermission = JpaStorageUtils.getAccountPermissions(JpaStorageUtils.CS_CURRENT_USER,
        		currentServiceName, workflowSubResource);
        org.collectionspace.services.authorization.ObjectFactory objectFactory =
        	new org.collectionspace.services.authorization.ObjectFactory();
        JAXBElement<AccountPermission> ap = objectFactory.createAccountPermission(accountPermission);
        PayloadOutputPart accountPermissionPart = new PayloadOutputPart("account_permission", ap); // REM - "account_permission" should be using a constant and not a literal
        ctx.addOutputPart(accountPermissionPart);
        
        profiler.stop();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillAllParts(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {

        //TODO filling extension parts should be dynamic
        //Nuxeo APIs lack to support stream/byte[] input, get/setting properties is
        //not an ideal way of populating objects.
        DocumentModel docModel = wrapDoc.getWrappedObject();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        PoxPayloadIn input = ctx.getInput();
        if (input.getParts().isEmpty()) {
            String msg = "No payload found!";
            logger.error(msg + "Ctx=" + getServiceContext().toString());
            throw new BadRequestException(msg);
        }

        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();

        //iterate over parts received and fill those parts
        List<PayloadInputPart> inputParts = input.getParts();
        for (PayloadInputPart part : inputParts) {

            String partLabel = part.getLabel();
            if (partLabel == null) {
                String msg = "Part label is missing or empty!";
                logger.error(msg + "Ctx=" + getServiceContext().toString());
                throw new BadRequestException(msg);
            }

            //skip if the part is not in metadata
            ObjectPartType partMeta = partsMetaMap.get(partLabel);
            if (partMeta == null) {
                continue;
            }
            fillPart(part, docModel, partMeta, action, ctx);
        }//rof

    }

    /**
     * fillPart fills an XML part into given document model
     * @param part to fill
     * @param docModel for the given object
     * @param partMeta metadata for the object to fill
     * @throws Exception
     */
    protected void fillPart(PayloadInputPart part, DocumentModel docModel,
            ObjectPartType partMeta, Action action, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception {
        //check if this is an xml part
        if (part.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
        	Element element = part.getElementBody();
            Map<String, Object> objectProps = DocumentUtils.parseProperties(partMeta, element, ctx);
                if (action == Action.UPDATE) {
                    this.filterReadOnlyPropertiesForPart(objectProps, partMeta);
                }
                docModel.setProperties(partMeta.getLabel(), objectProps);
            }
        }

    /**
     * Filters out read only properties, so they cannot be set on update.
     * TODO: add configuration support to do this generally
     * @param objectProps the properties parsed from the update payload
     * @param partMeta metadata for the object to fill
     */
    public void filterReadOnlyPropertiesForPart(
            Map<String, Object> objectProps, ObjectPartType partMeta) {
    	// Should add in logic to filter most of the core items on update
    	if(partMeta.getLabel().equalsIgnoreCase(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA)) {
        	objectProps.remove(CollectionSpaceClient.COLLECTIONSPACE_CORE_CREATED_AT);
        	objectProps.remove(CollectionSpaceClient.COLLECTIONSPACE_CORE_CREATED_BY);
        	objectProps.remove(CollectionSpaceClient.COLLECTIONSPACE_CORE_URI);
        	objectProps.remove(CollectionSpaceClient.COLLECTIONSPACE_CORE_TENANTID);
        	// Note that the updatedAt/updatedBy fields are set internally
        	// in DocumentModelHandler.handleCoreValues().
    	}
    }

    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema)
            throws Exception {
        return extractPart(docModel, schema, (Map<String, Object>)null);
    }
    
    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    @Deprecated
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        return extractPart(docModel, schema, partMeta, null);
    }    

    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    protected Map<String, Object> extractPart(
            DocumentModel docModel, 
            String schema,
            Map<String, Object> addToMap)
            throws Exception {
        Map<String, Object> result = null;

        Map<String, Object> objectProps = docModel.getProperties(schema);
        if (objectProps != null) {
	        //unqualify properties before sending the doc over the wire (to save bandwidh)
	        //FIXME: is there a better way to avoid duplication of a Map/Collection?
	        Map<String, Object> unQObjectProperties =
	                (addToMap != null) ? addToMap : (new HashMap<String, Object>());
	        Set<Entry<String, Object>> qualifiedEntries = objectProps.entrySet();
	        for (Entry<String, Object> entry : qualifiedEntries) {
	            String unqProp = getUnQProperty(entry.getKey());
	            unQObjectProperties.put(unqProp, entry.getValue());
	        }
	        result = unQObjectProperties;
        }

        return result;
    }
    
    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    @Deprecated
    protected Map<String, Object> extractPart(
            DocumentModel docModel, String schema, ObjectPartType partMeta,
            Map<String, Object> addToMap)
            throws Exception {
        Map<String, Object> result = null;

        result = this.extractPart(docModel, schema, addToMap);

        return result;
    }
    
    /* 
    public String getStringPropertyFromDoc(
    		ServiceContext ctx,
    		String csid,
    		String propertyXPath ) throws DocumentNotFoundException, DocumentException {
    	RepositoryInstance repoSession = null;
    	boolean releaseRepoSession = false;
    	String returnValue = null;

    	try{ 
    		RepositoryJavaClientImpl repoClient = (RepositoryJavaClientImpl)this.getRepositoryClient(ctx);
    		repoSession = this.getRepositorySession();
    		if (repoSession == null) {
    			repoSession = repoClient.getRepositorySession();
    			releaseRepoSession = true;
    		}

    		try {
    			DocumentWrapper<DocumentModel> wrapper = repoClient.getDoc(repoSession, ctx, csid);
    			DocumentModel docModel = wrapper.getWrappedObject();
    			returnValue = (String) docModel.getPropertyValue(propertyXPath);
    		} catch (PropertyException pe) {
    			throw pe;
    		} catch (DocumentException de) {
    			throw de;
    		} catch (Exception e) {
    			if (logger.isDebugEnabled()) {
    				logger.debug("Caught exception ", e);
    			}
    			throw new DocumentException(e);
    		} finally {
    			if (releaseRepoSession && repoSession != null) {
    				repoClient.releaseRepositorySession(repoSession);
    			}
    		}
    	} catch (Exception e) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("Caught exception ", e);
    		}
    		throw new DocumentException(e);
    	}	        


    	if (logger.isWarnEnabled() == true) {
    		logger.warn("Returned DocumentModel instance was created with a repository session that is now closed.");
    	}
    	return returnValue;
    }
     */

    

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getAuthorityRefs(org.collectionspace.services.common.document.DocumentWrapper, java.util.List)
     */
    @Override
    public AuthorityRefList getAuthorityRefs(
            String csid,
            List<AuthRefConfigInfo> authRefsInfo) throws PropertyException {

        AuthorityRefList authRefList = new AuthorityRefList();
        AbstractCommonList commonList = (AbstractCommonList) authRefList;
        
        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        
        List<AuthorityRefList.AuthorityRefItem> list = authRefList.getAuthorityRefItem();

        try {
        	int iFirstToUse = (int)(pageSize*pageNum);
        	int nFoundInPage = 0;
        	int nFoundTotal = 0;
        	
        	ArrayList<RefNameServiceUtils.AuthRefInfo> foundProps 
        		= new ArrayList<RefNameServiceUtils.AuthRefInfo>();
        	
        	boolean releaseRepoSession = false;
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
        	RepositoryJavaClientImpl repoClient = (RepositoryJavaClientImpl)this.getRepositoryClient(ctx);
        	RepositoryInstance repoSession = this.getRepositorySession();
        	if (repoSession == null) {
        		repoSession = repoClient.getRepositorySession(ctx);
        		releaseRepoSession = true;
        	}
        	
        	try {
        		DocumentModel docModel = repoClient.getDoc(repoSession, ctx, csid).getWrappedObject();
	           	RefNameServiceUtils.findAuthRefPropertiesInDoc(docModel, authRefsInfo, null, foundProps);
	           	// Slightly goofy pagination support - how many refs do we expect from one object?
	           	for(RefNameServiceUtils.AuthRefInfo ari:foundProps) {
	       			if((nFoundTotal >= iFirstToUse) && (nFoundInPage < pageSize)) {
	       				if(appendToAuthRefsList(ari, list)) {
	           				nFoundInPage++;
	               			nFoundTotal++;
	       				}
	       			} else {
	       				nFoundTotal++;
	       			}
	           	}
        	} finally {
        		if (releaseRepoSession == true) {
        			repoClient.releaseRepositorySession(ctx, repoSession);
        		}
        	}
        	
            // Set num of items in list. this is useful to our testing framework.
            commonList.setItemsInPage(nFoundInPage);
            // set the total result size
            commonList.setTotalItems(nFoundTotal);
            
        } catch (PropertyException pe) {
            String msg = "Attempted to retrieve value for invalid or missing authority field. "
                    + "Check authority field properties in tenant bindings.";
            logger.warn(msg, pe);
            throw pe;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Failed to retrieve authority references").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        return authRefList;
    }

    private boolean appendToAuthRefsList(RefNameServiceUtils.AuthRefInfo ari, 
    						List<AuthorityRefList.AuthorityRefItem> list)
            throws Exception {
    	String fieldName = ari.getQualifiedDisplayName();
    	try {
	   		String refNameValue = (String)ari.getProperty().getValue();
	   		AuthorityRefList.AuthorityRefItem item = authorityRefListItem(fieldName, refNameValue);
	   		if(item!=null) {	// ignore garbage values.
	   			list.add(item);
	   			return true;
	   		}
    	} catch(PropertyException pe) {
			logger.debug("PropertyException on: "+ari.getProperty().getPath()+pe.getLocalizedMessage());
    	}
    	return false;
    }

    private AuthorityRefList.AuthorityRefItem authorityRefListItem(String authRefFieldName, String refName) {

        AuthorityRefList.AuthorityRefItem ilistItem = new AuthorityRefList.AuthorityRefItem();
        try {
            RefNameUtils.AuthorityTermInfo termInfo = RefNameUtils.parseAuthorityTermInfo(refName);
            ilistItem.setRefName(refName);
            ilistItem.setAuthDisplayName(termInfo.inAuthority.displayName);
            ilistItem.setItemDisplayName(termInfo.displayName);
            ilistItem.setSourceField(authRefFieldName);
            ilistItem.setUri(termInfo.getRelativeUri());
        } catch (Exception e) {
        	logger.error("Trouble parsing refName from value: "+refName+" in field: "+authRefFieldName+e.getLocalizedMessage());
        	ilistItem = null;
        }
        return ilistItem;
    }

    /**
     * Returns the primary value from a list of values.
     *
     * Assumes that the first value is the primary value.
     * This assumption may change when and if the primary value
     * is identified explicitly.
     *
     * @param values a list of values.
     * @param propertyName the name of a property through
     *     which the value can be extracted.
     * @return the primary value.
    protected String primaryValueFromMultivalue(List<Object> values, String propertyName) {
        String primaryValue = "";
        if (values == null || values.size() == 0) {
            return primaryValue;
        }
        Object value = values.get(0);
        if (value instanceof String) {
            if (value != null) {
                primaryValue = (String) value;
            }
       // Multivalue group of fields
       } else if (value instanceof Map) {
            if (value != null) {
                Map map = (Map) value;
                if (map.values().size() > 0) {
                    if (map.get(propertyName) != null) {
                      primaryValue = (String) map.get(propertyName);
                    }
                }
            }
       } else {
            logger.warn("Unexpected type for property " + propertyName
                    + " in multivalue list: not String or Map.");
       }
       return primaryValue;
    }
     */

    /**
     * Gets a simple property from the document.
     *
     * For completeness, as this duplicates DocumentModel method. 
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param propertyName The simple scalar property type
     * @return property value as String
     */
    protected String getSimpleStringProperty(DocumentModel docModel, String schema, String propName) {
    	String xpath = "/"+schema+":"+propName;
    	try {
	    	return (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Not a simple String property?"
    				+pe.getLocalizedMessage());
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a scalar String property?"
    				+cce.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    }

    /**
     * Gets first of a repeating list of scalar values, as a String, from the document.
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param listName The name of the scalar list property
     * @return first value in list, as a String, or empty string if the list is empty
     */
    protected String getFirstRepeatingStringProperty(
    		DocumentModel docModel, String schema, String listName) {
    	String xpath = "/"+schema+":"+listName+"/[0]";
    	try {
	    	return (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Not a repeating scalar?"
    				+pe.getLocalizedMessage());
    	} catch(IndexOutOfBoundsException ioobe) {
    		// Nuxeo sometimes handles missing sub, and sometimes does not. Odd.
    		return "";	// gracefully handle missing elements
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a repeating String property?"
    				+cce.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    }
   

    /**
     * Gets first of a repeating list of scalar values, as a String, from the document.
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param listName The name of the scalar list property
     * @return first value in list, as a String, or empty string if the list is empty
     */
    protected String getStringValueInPrimaryRepeatingComplexProperty(
    		DocumentModel docModel, String schema, String complexPropertyName, String fieldName) {    	
    	String result = null;
    	
    	String xpath = "/" + NuxeoUtils.getPrimaryXPathPropertyName(schema, complexPropertyName, fieldName);
    	try {
	    	result = (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Bad propertyNames?"
    				+pe.getLocalizedMessage());
    	} catch(IndexOutOfBoundsException ioobe) {
    		// Nuxeo sometimes handles missing sub, and sometimes does not. Odd.
    		result = "";	// gracefully handle missing elements
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a String property?"
    				+cce.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    	
    	return result;
    }
   
    /**
     * Gets XPath value from schema. Note that only "/" and "[n]" are
     * supported for xpath. Can omit grouping elements for repeating complex types, 
     * e.g., "fieldList/[0]" can be used as shorthand for "fieldList/field[0]" and
     * "fieldGroupList/[0]/field" can be used as shorthand for "fieldGroupList/fieldGroup[0]/field".
     * If there are no entries for a list of scalars or for a list of complex types, 
     * a 0 index expression (e.g., "fieldGroupList/[0]/field") will safely return an empty
     * string. A non-zero index will throw an IndexOutOfBoundsException if there are not
     * that many elements in the list. 
     * N.B.: This does not follow the XPath spec - indices are 0-based, not 1-based.
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param xpath The XPath expression (without schema prefix)
     * @return value the indicated property value as a String
     */
	protected Object getListResultValue(DocumentModel docModel, // REM - CSPACE-5133
			String schema, ListResultField field) {
		Object result = null;

		result = NuxeoUtils.getXPathValue(docModel, schema, field.getXpath());
		
		return result;
	}
	
	protected String getStringValue(DocumentModel docModel,
			String schema, ListResultField field) {
		String result = null;
		
		Object value = getListResultValue(docModel, schema, field);
		if (value != null && value instanceof String) {
			String strValue = (String) value;
			if (strValue.trim().isEmpty() == false) {
				result = strValue;
			}
		}
		
		return result;
	}
   
    protected void removeFromList(List<RelationsCommonList.RelationListItem> list, RelationsCommonList.RelationListItem item) {
        list.remove(item);
    }
	
    private void itemToString(StringBuilder sb, String prefix, RelationsCommonList.RelationListItem item ) {
    	sb.append(prefix);
   		sb.append((item.getCsid()!= null)?item.getCsid():"NO CSID");
    	sb.append(": ["); 
    	sb.append((item.getSubject().getCsid()!=null)?item.getSubject().getCsid():item.getSubject().getRefName());
    	sb.append("]--");
    	sb.append(item.getPredicate());
    	sb.append("-->["); 
    	sb.append((item.getObject().getCsid()!=null)?item.getObject().getCsid():item.getObject().getRefName());
    	sb.append("]");
    }
    
    private String dumpList(List<RelationsCommonList.RelationListItem> list, String label) {
        StringBuilder sb = new StringBuilder();
        String s;
        if (list.size() > 0) {
            sb.append("=========== " + label + " ==========" + CR);
        }
        for (RelationsCommonList.RelationListItem item : list) {
        	itemToString(sb, "==  ", item);
        	sb.append(CR);
        }
        return sb.toString();
    }
    
    /** @return null on parent not found
     */
    protected String getParentCSID(String thisCSID) throws Exception {
        String parentCSID = null;
        try {
            String predicate = RelationshipType.HAS_BROADER.value();
            RelationsCommonList parentListOuter = getRelations(thisCSID, null, predicate);
            List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();
            if (parentList != null) {
                if (parentList.size() == 0) {
                    return null;
                }
                RelationsCommonList.RelationListItem relationListItem = parentList.get(0);
                parentCSID = relationListItem.getObjectCsid();
            }
            return parentCSID;
        } catch (Exception e) {
            logger.error("Could not find parent for this: " + thisCSID, e);
            return null;
        }
    }
    
    protected List<RelationsCommonList.RelationListItem> newRelationsCommonList() {
        List<RelationsCommonList.RelationListItem> result = new ArrayList<RelationsCommonList.RelationListItem>();
        return result;
    }

    public void showSiblings(DocumentWrapper<DocumentModel> wrapDoc,
            MultipartServiceContext ctx) throws Exception {
        String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());
        String parentCSID = getParentCSID(thisCSID);
        if (parentCSID == null) {
            logger.warn("~~~~~\r\n~~~~ Could not find parent for this: " + thisCSID);
            return;
        }

        String predicate = RelationshipType.HAS_BROADER.value();
        RelationsCommonList siblingListOuter = getRelations(null, parentCSID, predicate);
        List<RelationsCommonList.RelationListItem> siblingList = siblingListOuter.getRelationListItem();

        List<RelationsCommonList.RelationListItem> toRemoveList = newRelationsCommonList();


        RelationsCommonList.RelationListItem item = null;
        for (RelationsCommonList.RelationListItem sibling : siblingList) {
            if (thisCSID.equals(sibling.getSubjectCsid())) {
                toRemoveList.add(sibling);   //IS_A copy of the main item, i.e. I have a parent that is my parent, so I'm in the list from the above query.
            }
        }
        //rather than create an immutable iterator, I'm just putting the items to remove on a separate list, then looping over that list and removing.
        for (RelationsCommonList.RelationListItem self : toRemoveList) {
            removeFromList(siblingList, self);
        }

        long siblingSize = siblingList.size();
        siblingListOuter.setTotalItems(siblingSize);
        siblingListOuter.setItemsInPage(siblingSize);
        if(logger.isTraceEnabled()) {
            String dump = dumpList(siblingList, "Siblings of: "+thisCSID);
            logger.trace("~~~~~~~~~~~~~~~~~~~~~~ showSiblings ~~~~~~~~~~~~~~~~~~~~~~~~" + CR + dump);
        }

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, siblingListOuter);
        ctx.addOutputPart(relationsPart);
    }
    
    public void showRelations(DocumentWrapper<DocumentModel> wrapDoc,
            MultipartServiceContext ctx) throws Exception {
        String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());

        String predicate = RelationshipType.HAS_BROADER.value();
        RelationsCommonList parentListOuter = getRelations(thisCSID, null, predicate);
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();

        RelationsCommonList childrenListOuter = getRelations(null, thisCSID, predicate);
        List<RelationsCommonList.RelationListItem> childrenList = childrenListOuter.getRelationListItem();

        if(logger.isTraceEnabled()) {
            String dump = dumpLists(thisCSID, parentList, childrenList, null);
            logger.trace("~~~~~~~~~~~~~~~~~~~~~~ showRelations ~~~~~~~~~~~~~~~~~~~~~~~~" + CR + dump);
        }
        
        //Assume that there are more children than parents.  Will be true for parent/child, but maybe not for other relations.
        //Now add all parents to our childrenList, to be able to return just one list of consolidated results.
        //Not optimal, but that's the current design spec.
        long added = 0;
        for (RelationsCommonList.RelationListItem parent : parentList) {
            childrenList.add(parent);
            added++;
        }
        long childrenSize = childrenList.size();
        childrenListOuter.setTotalItems(childrenSize);
        childrenListOuter.setItemsInPage(childrenListOuter.getItemsInPage() + added);

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, childrenListOuter);
        ctx.addOutputPart(relationsPart);
    }
    
    public void showAllRelations(DocumentWrapper<DocumentModel> wrapDoc, MultipartServiceContext ctx) throws Exception {
        String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());

        RelationsCommonList subjectListOuter = getRelations(thisCSID, null, null);   //  nulls are wildcards:  predicate=*, and object=*
        List<RelationsCommonList.RelationListItem> subjectList = subjectListOuter.getRelationListItem();

        RelationsCommonList objectListOuter = getRelations(null, thisCSID, null);   //  nulls are wildcards:  subject=*, and predicate=*
        List<RelationsCommonList.RelationListItem> objectList = objectListOuter.getRelationListItem();

        if(logger.isTraceEnabled()) {
            String dump = dumpLists(thisCSID, subjectList, objectList, null);
            logger.trace("~~~~~~~~~~~~~~~~~~~~~~ showAllRelations ~~~~~~~~~~~~~~~~~~~~~~~~" + CR + dump);
        }
        //  MERGE LISTS:
        subjectList.addAll(objectList);

        //now subjectList actually has records BOTH where thisCSID is subject and object.
        long relatedSize = subjectList.size();
        subjectListOuter.setTotalItems(relatedSize);
        subjectListOuter.setItemsInPage(relatedSize);

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, subjectListOuter);
        ctx.addOutputPart(relationsPart);
    }

    private String dumpLists(String itemCSID,
            List<RelationsCommonList.RelationListItem> parentList,
            List<RelationsCommonList.RelationListItem> childList,
            List<RelationsCommonList.RelationListItem> actionList) {
    	StringBuilder sb = new StringBuilder();
        sb.append("itemCSID: " + itemCSID + CR);
        if(parentList!=null) {
        	sb.append(dumpList(parentList, "parentList"));
        }
        if(childList!=null) {
        	sb.append(dumpList(childList, "childList"));
        }
        if(actionList!=null) {
        	sb.append(dumpList(actionList, "actionList"));
        }
        return sb.toString();
    }
    
    //================= TODO: move this to common, refactoring this and  CollectionObjectResource.java
    public RelationsCommonList getRelations(String subjectCSID, String objectCSID, String predicate) throws Exception {
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, subjectCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, objectCSID);

        RelationResource relationResource = new RelationResource(); //is this still acting like a singleton as it should be?
        RelationsCommonList relationsCommonList = relationResource.getList(ctx);
        return relationsCommonList;
    }
    //============================= END TODO refactor ==========================
    
    // this method calls the RelationResource to have it create the relations and persist them.
    private void createRelations(List<RelationsCommonList.RelationListItem> inboundList,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
        for (RelationsCommonList.RelationListItem item : inboundList) {
            RelationsCommon rc = new RelationsCommon();
            //rc.setCsid(item.getCsid());
            //todo: assignTo(item, rc);
            RelationsDocListItem itemSubject = item.getSubject();
            RelationsDocListItem itemObject = item.getObject();

            // Set at least one of CSID and refName for Subject and Object
            // Either value might be null for for each of Subject and Object 
            String subjectCsid = itemSubject.getCsid();
            rc.setSubjectCsid(subjectCsid);

            String objCsid = itemObject.getCsid();
            rc.setObjectCsid(objCsid);

            rc.setSubjectRefName(itemSubject.getRefName());
            rc.setObjectRefName(itemObject.getRefName());

            rc.setRelationshipType(item.getPredicate());
            rc.setRelationshipMetaType(item.getRelationshipMetaType());
            //RelationshipType  foo = (RelationshipType.valueOf(item.getPredicate())) ;
            //rc.setPredicate(foo);     //this must be one of the type found in the enum in  services/jaxb/src/main/resources/relations_common.xsd

            // This is superfluous, since it will be fetched by the Relations Create logic.
            rc.setSubjectDocumentType(itemSubject.getDocumentType());
            rc.setObjectDocumentType(itemObject.getDocumentType());

            // This is superfluous, since it will be fetched by the Relations Create logic.
            rc.setSubjectUri(itemSubject.getUri());
            rc.setObjectUri(itemObject.getUri());
            // May not have the info here. Only really require CSID or refName. 
            // Rest is handled in the Relation create mechanism
            //uriPointsToSameAuthority(itemSubject.getUri(), itemObject.getUri());

            PoxPayloadOut payloadOut = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
            PayloadOutputPart outputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMONPART_NAME, rc);
            payloadOut.addPart(outputPart);
            RelationResource relationResource = new RelationResource();
            Response res = relationResource.create(ctx, ctx.getResourceMap(),
                    ctx.getUriInfo(), payloadOut.toXML());    //NOTE ui recycled from above to pass in unknown query params.
        }
    }
    
    // Note that item2 may be sparse (only refName, no CSID for subject or object)
    // But item1 must not be sparse 
    private boolean itemsEqual(RelationsCommonList.RelationListItem item1, RelationsCommonList.RelationListItem item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        RelationsDocListItem subj1 = item1.getSubject();
        RelationsDocListItem subj2 = item2.getSubject();
        RelationsDocListItem obj1 = item1.getObject();
        RelationsDocListItem obj2 = item2.getObject();
        
        String subj1Csid = subj1.getCsid();
        String subj2Csid = subj2.getCsid();
        String subj1RefName = subj1.getRefName();
        String subj2RefName = subj2.getRefName();

        String obj1Csid = obj1.getCsid();
        String obj2Csid = obj2.getCsid();
        String obj1RefName = obj1.getRefName();
        String obj2RefName = obj2.getRefName();
        
        String item1Metatype = item1.getRelationshipMetaType();
        item1Metatype = item1Metatype != null ? item1Metatype : EMPTYSTR;
        
        String item2Metatype = item2.getRelationshipMetaType();
        item2Metatype = item2Metatype != null ? item2Metatype : EMPTYSTR;
        
        boolean isEqual = (subj1Csid.equals(subj2Csid) || ((subj2Csid==null)  && subj1RefName.equals(subj2RefName)))
                && (obj1Csid.equals(obj1Csid)   || ((obj2Csid==null)   && obj1RefName.equals(obj2RefName)))
                // predicate is proper, but still allow relationshipType
                && (item1.getPredicate().equals(item2.getPredicate())
                	||  ((item2.getPredicate()==null)  && item1.getRelationshipType().equals(item2.getRelationshipType())))
                // Allow missing docTypes, so long as they do not conflict
                && (obj1.getDocumentType().equals(obj2.getDocumentType()) || obj2.getDocumentType()==null)
                && (subj1.getDocumentType().equals(subj2.getDocumentType()) || subj2.getDocumentType()==null)
                && (item1Metatype.equalsIgnoreCase(item2Metatype));
        return isEqual;
    }
    
    // Note that the item argument may be sparse (only refName, no CSID for subject or object)
    // But the list items must not be sparse
    private RelationsCommonList.RelationListItem findInList(
    		List<RelationsCommonList.RelationListItem> list, 
    		RelationsCommonList.RelationListItem item) {
    	RelationsCommonList.RelationListItem foundItem = null;
        for (RelationsCommonList.RelationListItem listItem : list) {
            if (itemsEqual(listItem, item)) {   //equals must be defined, else
            	foundItem = listItem;
            	break;
            }
        }
        return foundItem;
    }
    
    /**  updateRelations strategy:
     *
     *
    go through inboundList, remove anything from childList that matches  from childList
    go through inboundList, remove anything from parentList that matches  from parentList
    go through parentList, delete all remaining
    go through childList, delete all remaining
    go through actionList, add all remaining.
    check for duplicate children
    check for more than one parent.
    
    inboundList                           parentList                      childList          actionList
    ----------------                          ---------------                  ----------------       ----------------
    child-a                                   parent-c                        child-a             child-b
    child-b                                   parent-d                        child-c
    parent-a
     *
     *
     */
    private RelationsCommonList updateRelations(
            String itemCSID, PoxPayloadIn input, DocumentWrapper<DocumentModel> wrapDoc, boolean fUpdate)
            throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("AuthItemDocHndler.updateRelations for: " + itemCSID);
        }
        PayloadInputPart part = input.getPart(RelationClient.SERVICE_COMMON_LIST_NAME);        //input.getPart("relations_common");
        if (part == null) {
            return null;  //nothing to do--they didn't send a list of relations.
        }
        RelationsCommonList relationsCommonListBody = (RelationsCommonList) part.getBody();
        List<RelationsCommonList.RelationListItem> inboundList = relationsCommonListBody.getRelationListItem();
        List<RelationsCommonList.RelationListItem> actionList = newRelationsCommonList();
        List<RelationsCommonList.RelationListItem> childList = null;
        List<RelationsCommonList.RelationListItem> parentList = null;
        DocumentModel docModel = wrapDoc.getWrappedObject();
		String itemRefName = (String) docModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
        		CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);

		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        //Do magic replacement of ${itemCSID} and fix URI's.
        fixupInboundListItems(ctx, inboundList, docModel, itemCSID);

        String HAS_BROADER = RelationshipType.HAS_BROADER.value();
        UriInfo uriInfo = ctx.getUriInfo();
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        if (fUpdate) {
            //Run getList() once as sent to get childListOuter:
            String predicate = RelationshipType.HAS_BROADER.value();
            queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
            queryParams.putSingle(IRelationsManager.SUBJECT_QP, null);
            queryParams.putSingle(IRelationsManager.SUBJECT_TYPE_QP, null);
            queryParams.putSingle(IRelationsManager.OBJECT_QP, itemCSID);
            queryParams.putSingle(IRelationsManager.OBJECT_TYPE_QP, null);
            
            RelationResource relationResource = new RelationResource();
            RelationsCommonList childListOuter = relationResource.getList(ctx);    // Knows all query params because they are in the context.

            //Now run getList() again, leaving predicate, swapping subject and object, to get parentListOuter.
            queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
            queryParams.putSingle(IRelationsManager.SUBJECT_QP, itemCSID);
            queryParams.putSingle(IRelationsManager.OBJECT_QP, null);
            RelationsCommonList parentListOuter = relationResource.getList(ctx);


            childList = childListOuter.getRelationListItem();
            parentList = parentListOuter.getRelationListItem();

            if (parentList.size() > 1) {
                throw new Exception("Too many parents for object: " + itemCSID + " list: " + dumpList(parentList, "parentList"));
            }

            if (logger.isTraceEnabled()) {
                logger.trace("AuthItemDocHndler.updateRelations for: " + itemCSID + " got existing relations.");
            }
        }

        for (RelationsCommonList.RelationListItem inboundItem : inboundList) {
            // Note that the relations may specify the other (non-item) bit with a refName, not a CSID,
            // and so the CSID for those may be null
            if(inboundItem.getPredicate().equals(HAS_BROADER)) {
            	// Look for parents and children
            	if(itemCSID.equals(inboundItem.getObject().getCsid())
            			|| itemRefName.equals(inboundItem.getObject().getRefName())) {
            		//then this is an item that says we have a child.  That child is inboundItem
            		RelationsCommonList.RelationListItem childItem =
            				(childList == null) ? null : findInList(childList, inboundItem);
            		if (childItem != null) {
                        if (logger.isTraceEnabled()) {
                        	StringBuilder sb = new StringBuilder();
                        	itemToString(sb, "== Child: ", childItem);
                            logger.trace("Found inboundChild in current child list: " + sb.toString());
                        }
            			removeFromList(childList, childItem);    //exists, just take it off delete list
            		} else {
                        if (logger.isTraceEnabled()) {
                        	StringBuilder sb = new StringBuilder();
                        	itemToString(sb, "== Child: ", inboundItem);
                            logger.trace("inboundChild not in current child list, will add: " + sb.toString());
                        }
            			actionList.add(inboundItem);   //doesn't exist as a child, but is a child.  Add to additions list
            			String newChildCsid = inboundItem.getSubject().getCsid();
            			if(newChildCsid == null) {
            				String newChildRefName = inboundItem.getSubject().getRefName();
            				if(newChildRefName==null) {
            					throw new RuntimeException("Child with no CSID or refName!");
            				}
                            if (logger.isTraceEnabled()) {
                            	logger.trace("Fetching CSID for child with only refname: "+newChildRefName);
                            }
                        	DocumentModel newChildDocModel = 
                        		ResourceBase.getDocModelForRefName(this.getRepositorySession(), 
                        				newChildRefName, getServiceContext().getResourceMap());
                        	newChildCsid = getCsid(newChildDocModel);
            			}
                		ensureChildHasNoOtherParents(ctx, queryParams, newChildCsid);
            		}

            	} else if (itemCSID.equals(inboundItem.getSubject().getCsid())
                			|| itemRefName.equals(inboundItem.getSubject().getRefName())) {
            		//then this is an item that says we have a parent.  inboundItem is that parent.
            		RelationsCommonList.RelationListItem parentItem =
            				(parentList == null) ? null : findInList(parentList, inboundItem);
            		if (parentItem != null) {
            			removeFromList(parentList, parentItem);    //exists, just take it off delete list
            		} else {
            			actionList.add(inboundItem);   //doesn't exist as a parent, but is a parent. Add to additions list
            		}
                } else {
                    logger.error("Parent/Child Element didn't link to this item. inboundItem: " + inboundItem);
            	}
            } else {
                logger.warn("Non-parent relation ignored. inboundItem: " + inboundItem);
            }
        }
        if (logger.isTraceEnabled()) {
            String dump = dumpLists(itemCSID, parentList, childList, actionList);
            logger.trace("~~~~~~~~~~~~~~~~~~~~~~dump~~~~~~~~~~~~~~~~~~~~~~~~" + CR + dump);
        }
        if (fUpdate) {
            if (logger.isTraceEnabled()) {
                logger.trace("AuthItemDocHndler.updateRelations for: " + itemCSID + " deleting "
                        + parentList.size() + " existing parents and " + childList.size() + " existing children.");
            }
            deleteRelations(parentList, ctx, "parentList");               //todo: there are items appearing on both lists....april 20.
            deleteRelations(childList, ctx, "childList");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("AuthItemDocHndler.updateRelations for: " + itemCSID + " adding "
                    + actionList.size() + " new parents and children.");
        }
        createRelations(actionList, ctx);
        if (logger.isTraceEnabled()) {
            logger.trace("AuthItemDocHndler.updateRelations for: " + itemCSID + " done.");
        }
        //We return all elements on the inbound list, since we have just worked to make them exist in the system
        // and be non-redundant, etc.  That list came from relationsCommonListBody, so it is still attached to it, just pass that back.
        return relationsCommonListBody;
    }
    
    /** Performs substitution for ${itemCSID} (see CommonAPI.AuthorityItemCSID_REPLACE for constant)
     *   and sets URI correctly for related items.
     *   Operates directly on the items in the list.  Does not change the list ordering, does not add or remove any items.
     */
    protected void fixupInboundListItems(ServiceContext ctx,
            List<RelationsCommonList.RelationListItem> inboundList,
            DocumentModel docModel,
            String itemCSID) throws Exception {
        String thisURI = this.getUri(docModel);
        // WARNING:  the two code blocks below are almost identical  and seem to ask to be put in a generic method.
        //                    beware of the little diffs in  inboundItem.setObjectCsid(itemCSID); and   inboundItem.setSubjectCsid(itemCSID); in the two blocks.
        for (RelationsCommonList.RelationListItem inboundItem : inboundList) {
            RelationsDocListItem inboundItemObject = inboundItem.getObject();
            RelationsDocListItem inboundItemSubject = inboundItem.getSubject();

            if (CommonAPI.AuthorityItemCSID_REPLACE.equalsIgnoreCase(inboundItemObject.getCsid())) {
                inboundItem.setObjectCsid(itemCSID);
                inboundItemObject.setCsid(itemCSID);
                //inboundItemObject.setUri(getUri(docModel));
            } else {
                /*
                String objectCsid = inboundItemObject.getCsid();
                DocumentModel itemDocModel = NuxeoUtils.getDocFromCsid(getRepositorySession(), ctx, objectCsid);    //null if not found.
                DocumentWrapper wrapper = new DocumentWrapperImpl(itemDocModel);
                String uri = this.getRepositoryClient(ctx).getDocURI(wrapper);
                inboundItemObject.setUri(uri);    //CSPACE-4037
                 */
            }
            //uriPointsToSameAuthority(thisURI, inboundItemObject.getUri());    //CSPACE-4042

            if (CommonAPI.AuthorityItemCSID_REPLACE.equalsIgnoreCase(inboundItemSubject.getCsid())) {
                inboundItem.setSubjectCsid(itemCSID);
                inboundItemSubject.setCsid(itemCSID);
                //inboundItemSubject.setUri(getUri(docModel));
            } else {
                /*
                String subjectCsid = inboundItemSubject.getCsid();
                DocumentModel itemDocModel = NuxeoUtils.getDocFromCsid(getRepositorySession(), ctx, subjectCsid);    //null if not found.
                DocumentWrapper wrapper = new DocumentWrapperImpl(itemDocModel);
                String uri = this.getRepositoryClient(ctx).getDocURI(wrapper);
                inboundItemSubject.setUri(uri);    //CSPACE-4037
                 */
            }
            //uriPointsToSameAuthority(thisURI, inboundItemSubject.getUri());  //CSPACE-4042

        }
    }

    private void ensureChildHasNoOtherParents(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		MultivaluedMap<String, String> queryParams, String childCSID) {
        logger.trace("ensureChildHasNoOtherParents for: " + childCSID );
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, childCSID);
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, RelationshipType.HAS_BROADER.value());
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);  //null means ANY
        
        RelationResource relationResource = new RelationResource();
        RelationsCommonList parentListOuter = relationResource.getList(ctx);
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();
        //logger.warn("ensureChildHasNoOtherParents preparing to delete relations on "+childCSID+"\'s parent list: \r\n"+dumpList(parentList, "duplicate parent list"));
        deleteRelations(parentList, ctx, "parentList-delete");
    }

    private void deleteRelations(List<RelationsCommonList.RelationListItem> list,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String listName) {
        try {
            for (RelationsCommonList.RelationListItem item : list) {
                RelationResource relationResource = new RelationResource();
                if(logger.isTraceEnabled()) {
                	StringBuilder sb = new StringBuilder();
                	itemToString(sb, "==== TO DELETE: ", item);
                	logger.trace(sb.toString());
                }
                Response res = relationResource.deleteWithParentCtx(ctx, item.getCsid());
                if (logger.isDebugEnabled()) {
                	logger.debug("Status of authority item deleteRelations method call was: " + res.getStatus());
                }
            }
        } catch (Throwable t) {
            String msg = "Unable to deleteRelations: " + Tools.errorToString(t, true);
            logger.error(msg);
        }
    }
    
    // Note that we must do this after we have completed the Update, so that the repository has the
    // info for the item itself. The relations code must call into the repo to get info for each end.
    // This could be optimized to pass in the parent docModel, since it will often be one end.
    // Nevertheless, we should complete the item save before we do work on the relations, especially
    // since a save on Create might fail, and we would not want to create relations for something
    // that may not be created...
    private void handleRelationsPayload(DocumentWrapper<DocumentModel> wrapDoc, boolean fUpdate) throws Exception {
        ServiceContext ctx = getServiceContext();
        PoxPayloadIn input = (PoxPayloadIn) ctx.getInput();
        DocumentModel documentModel = (wrapDoc.getWrappedObject());
        String itemCsid = documentModel.getName();

        //Updates relations part
        RelationsCommonList relationsCommonList = updateRelations(itemCsid, input, wrapDoc, fUpdate);

        PayloadOutputPart payloadOutputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, relationsCommonList);  //FIXME: REM - We should check for a null relationsCommonList and not create the new common list payload
        ctx.setProperty(RelationClient.SERVICE_COMMON_LIST_NAME, payloadOutputPart);

        //now we add part for relations list
        //ServiceContext ctx = getServiceContext();
        //PayloadOutputPart foo = (PayloadOutputPart) ctx.getProperty(RelationClient.SERVICE_COMMON_LIST_NAME);
        ((PoxPayloadOut) ctx.getOutput()).addPart(payloadOutputPart);
    }

    /**
     * Checks to see if the refName has changed, and if so, 
     * uses utilities to find all references and update them to use the new refName.
     * @throws Exception 
     */
    protected void handleRefNameReferencesUpdate() throws Exception {
        if (hasRefNameUpdate() == true) {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = getRepositoryClient(ctx);
            RepositoryInstance repoSession = this.getRepositorySession();
            
            // Update all the relationship records that referred to the old refName
            RefNameServiceUtils.updateRefNamesInRelations(ctx, repoClient, repoSession,
                    oldRefNameOnUpdate, newRefNameOnUpdate);
        }
    }
    
    protected String getRefNameUpdate() {
    	String result = null;
    	
    	if (hasRefNameUpdate() == true) {
    		result = newRefNameOnUpdate;
    		if (logger.isDebugEnabled() == true) {
    			logger.debug(String.format("There was a refName update.  New: %s Old: %s" ,
    					newRefNameOnUpdate, oldRefNameOnUpdate));
    		}
    	}
    	
    	return result;
    }
    
    /*
     * Note: The Vocabulary document handler overrides this method.
     */
    protected String getRefPropName() {
    	return ServiceBindingUtils.AUTH_REF_PROP;
    }

    
    
}
