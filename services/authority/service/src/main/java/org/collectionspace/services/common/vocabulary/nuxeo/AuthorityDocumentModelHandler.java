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
package org.collectionspace.services.common.vocabulary.nuxeo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.XmlTools;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefName.Authority;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityInfo;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentReferenceException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.AuthorityServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.RepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityDocumentModelHandler<AuthCommon>
        extends NuxeoDocumentModelHandler<AuthCommon> {
    
	private final Logger logger = LoggerFactory.getLogger(AuthorityDocumentModelHandler.class);	
    
	protected String authorityCommonSchemaName;
    protected String authorityItemCommonSchemaName;
    protected boolean shouldUpdateRevNumber = true; // default to updating the revision number

    public AuthorityDocumentModelHandler(String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
        this.authorityCommonSchemaName = authorityCommonSchemaName;
        this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }
    
    public void setShouldUpdateRevNumber(boolean flag) {
    	this.shouldUpdateRevNumber = flag;
    }
    
    public boolean getShouldUpdateRevNumber() {
    	return this.shouldUpdateRevNumber;
    }
    
    /**
     * The entity type expected from the JAX-RS Response object
     */
    public Class<String> getEntityResponseType() {
    	return String.class;
    }
    
    @Override
    public void prepareSync() throws Exception {
    	this.setShouldUpdateRevNumber(AuthorityServiceUtils.DONT_UPDATE_REV);  // Never update rev nums on sync operations
    }

    protected PayloadInputPart extractPart(Response res, String partLabel)
            throws Exception {
            PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(getEntityResponseType()));
            PayloadInputPart payloadInputPart = input.getPart(partLabel);
            if (payloadInputPart == null) {
            	logger.error("Part " + partLabel + " was unexpectedly null.");
            }
            return payloadInputPart;
    }
    
    @Override
    public boolean handleSync(DocumentWrapper<Object> wrapDoc) throws Exception {
    	boolean result = false;
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        Specifier specifier = (Specifier) wrapDoc.getWrappedObject();
        //
        // Get the rev number of the authority so we can compare with rev number of shared authority
        //
        DocumentModel docModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), authorityCommonSchemaName, specifier);
        Long localRev = (Long) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.REV);
        String shortId = (String) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        //
        // Using the short ID of the local authority, create a URN specifier to retrieve the SAS authority
        //
        Specifier sasSpecifier = new Specifier(SpecifierForm.URN_NAME, shortId);
        PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.requestPayloadIn(ctx, sasSpecifier, getEntityResponseType());
        //
        // If the authority on the SAS is newer, synch all the items and then the authority record as well
        //
        //
        Long sasRev = getRevision(sasPayloadIn);
        if (sasRev > localRev) {
        	//
        	// First, sync all the authority items
        	//
        	syncAllItems(ctx, sasSpecifier);
        	//
        	// Next, sync the authority resource/record itself
        	//
        	AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();
        	ctx.setProperty(AuthorityServiceUtils.SHOULD_UPDATE_REV_PROPERTY, AuthorityServiceUtils.DONT_UPDATE_REV); // Don't update the rev number, use the rev number for the SAS instance instead
        	PoxPayloadOut payloadOut = authorityResource.update(ctx, ctx.getResourceMap(), ctx.getUriInfo(), docModel.getName(), 
        			sasPayloadIn);
        	if (payloadOut != null) {
        		ctx.setOutput(payloadOut);
        		result = true;
        	}
        }
        
        return result;
    }
    
    /*
     * Get the list of authority items from the remote shared authority server (SAS) and try
     * to synchronize them with the local items.  If items exist on the remote but not the local, we'll create them.
     */
    protected int syncAllItems(ServiceContext ctx, Specifier sasAuthoritySpecifier) throws Exception {
    	int result = -1;
    	int created = 0;
    	int synched = 0;
    	int alreadySynched = 0;
    	int deprecated = 0;
    	int totalItemsProcessed = 0;
    	ArrayList<String> itemsInRemoteAuthority = new ArrayList<String>();
    	//
    	// Iterate over the list of items/terms in the remote authority
    	//
        PoxPayloadIn sasPayloadInItemList = requestPayloadInItemList(ctx, sasAuthoritySpecifier);
        List<Element> itemList = getItemList(sasPayloadInItemList);
        if (itemList != null) {
        	for (Element e:itemList) {
        		String remoteRefName = XmlTools.getElementValue(e, "refName");
        		itemsInRemoteAuthority.add(remoteRefName);
        		long status = syncRemoteItemWithLocalItem(ctx, remoteRefName);
        		if (status == 1) {
        			created++;
        		} else if (status == 0) {
        			synched++;
        		} else {
        			alreadySynched++;
        		}
        		totalItemsProcessed++;
        	}
        }
        //
        // Now see if we need to deprecate or delete items that have been hard-deleted from the SAS but still exist
        // locally.  Subtract (remove) the list of remote items from the list of local items to determine which
        // of the remote items have been hard deleted.
        //
    	ArrayList<String> itemsInLocalAuthority = getItemsInLocalAuthority(ctx, sasAuthoritySpecifier);
    	if (itemsInLocalAuthority.removeAll(itemsInRemoteAuthority) == true) {
    		ArrayList<String> remainingItems = itemsInLocalAuthority; // now a subset of local items
        	//
        	// We now need to either hard-deleted or deprecate the remaining authorities
        	//
    		long processed = deleteOrDeprecateItems(ctx, remainingItems);
    		if (processed != remainingItems.size()) {
    			throw new Exception("Encountered unexpected exception trying to delete or deprecated authority items during synchronization.");
    		}
    	}

        
        logger.info(String.format("Total number of items processed during sync: %d", totalItemsProcessed));
        logger.info(String.format("Number of items synchronized: %d", synched));
        logger.info(String.format("Number of items created during sync: %d", created));
        logger.info(String.format("Number not needing synchronization: %d", alreadySynched));

        return result;
    }

    /**
     * 
     * @param ctx
     * @param refNameList
     * @return
     * @throws Exception
     */
    private long deleteOrDeprecateItems(ServiceContext ctx, ArrayList<String> refNameList) throws Exception {
    	long result = 0;
    	
    	ArrayList<String> failureList = new ArrayList<String>();
    	for (String refName:refNameList) {
    		AuthorityTermInfo itemInfo = RefNameUtils.parseAuthorityTermInfo(refName);
    		AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();
    		try {
    			authorityResource.deleteAuthorityItem(ctx, itemInfo.inAuthority.csid, itemInfo.csid);
    			result++;
    		} catch (DocumentReferenceException de) {
    			logger.info(String.format("Authority item '%s' has existing references and cannot be removed during sync.",
    					refName), de);
    			boolean marked = markAuthorityItemAsDeprecated(ctx, itemInfo);
    			if (marked == true) {
    				result++;
    			}
    		} catch (Exception e) {
    			logger.warn(String.format("Unable to delete authority item '%s'", refName), e);
    			throw e;
    		}
    	}

    	if (logger.isWarnEnabled() == true) {
    		if (result != refNameList.size()) {
    			logger.warn(String.format("Unable to delete or deprecate some authority items during synchronization with SAS.  Deleted or deprecated %d of %d.  See the services log file for details.",
    					result, refNameList.size()));
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Mark the authority item as deprecated.
     * 
     * @param ctx
     * @param itemInfo
     * @throws Exception
     */
    private boolean markAuthorityItemAsDeprecated(ServiceContext ctx, AuthorityTermInfo itemInfo) throws Exception {
    	boolean result = false;
    	
    	try {
	    	String itemCsid = itemInfo.csid;
	    	DocumentModel docModel = NuxeoUtils.getDocFromCsid(ctx, (CoreSessionInterface)ctx.getCurrentRepositorySession(), itemCsid);
	    	docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.DEPRECATED,
	    			new Boolean(AuthorityServiceUtils.DEPRECATED));
	    	CoreSessionInterface session = (CoreSessionInterface) ctx.getCurrentRepositorySession();
	    	session.saveDocument(docModel);
	    	result = true;
    	} catch (Exception e) {
    		logger.warn(String.format("Could not mark item '%s' as deprecated.", itemInfo.name), e);
    		throw e;
    	}
    	
    	return result;
    }
    
    /**
     * Gets the list of SAS related items in the local authority.  We exlude items with the "proposed" flags because
     * we want a list with only SAS created items.
     * 
     * We need to add pagination support to this call!!!
     * 
     * @param ctx
     * @param specifier
     * @return
     * @throws Exception
     */
    private ArrayList<String> getItemsInLocalAuthority(ServiceContext ctx, Specifier specifier) throws Exception {
    	ArrayList<String> result = new ArrayList<String>();
    	
    	ResourceMap resourceMap = ctx.getResourceMap();
    	String resourceName = ctx.getClient().getServiceName();
    	AuthorityResource authorityResource = (AuthorityResource) resourceMap.get(resourceName);
    	AbstractCommonList acl = authorityResource.getAuthorityItemList(ctx, specifier.value, ctx.getUriInfo());
    	List<ListItem> listItemList = acl.getListItem();
    	for (ListItem listItem:listItemList) {
    		Boolean proposed = getBooleanValue(listItem, AuthorityItemJAXBSchema.PROPOSED);
    		if (proposed == false) { // exclude "proposed" (i.e., local-only items)
    			result.add(AbstractCommonListUtils.ListItemGetElementValue(listItem, AuthorityItemJAXBSchema.REF_NAME));
    		}
    	}
    	
    	return result;
    }
    
    private Boolean getBooleanValue(ListItem listItem, String name) {
    	Boolean result = null;
    	
		String value = AbstractCommonListUtils.ListItemGetElementValue(listItem, name);
		if (value != null) {
			result = Boolean.valueOf(value);
		}
		
		return result;
    }
    
    private String getStringValue(ListItem listItem, String name) {
    	return AbstractCommonListUtils.ListItemGetElementValue(listItem, AuthorityItemJAXBSchema.REF_NAME);
    }
    
    /**
     * This is a sync method.
     * @param ctx
     * @param parentIdentifier - Must be in short-id-refname form -i.e., urn:cspace:name(shortid)
     * @param itemIdentifier   - Must be in short-id-refname form -i.e., urn:cspace:name(shortid)
     * @throws Exception 
     */
    protected void createLocalItem(ServiceContext ctx, String parentIdentifier, String itemIdentifier) throws Exception {
    	//
    	// Create a URN short ID specifier for the getting a copy of the remote authority item
    	//
        Specifier authoritySpecifier = Specifier.getSpecifier(parentIdentifier);
        Specifier itemSpecifier = Specifier.getSpecifier(itemIdentifier);
        AuthorityItemSpecifier sasAuthorityItemSpecifier = new AuthorityItemSpecifier(authoritySpecifier, itemSpecifier);
        //
        // Get the remote payload
        //
        PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.requestPayloadIn(sasAuthorityItemSpecifier, 
        		ctx.getServiceName(), getEntityResponseType());
        //
        // Using the payload from the remote server, create a local copy of the item
        //
    	AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();
    	Response response = authorityResource.createAuthorityItemWithParentContext(ctx, authoritySpecifier.getURNValue(),
    			sasPayloadIn, AuthorityServiceUtils.DONT_UPDATE_REV, AuthorityServiceUtils.NOT_PROPOSED);
    	//
    	// Check the response for successful POST result
    	//
    	if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
    		throw new DocumentException(String.format("Could not create new authority item '%s' during synchronization of the '%s' authority.",
    				itemIdentifier, parentIdentifier));
    	}
    	//
    	// Handle the workflow state
    	//
    	authorityResource.updateItemWorkflowWithTransition(ctx, parentIdentifier, itemIdentifier, 
    			WorkflowClient.WORKFLOWTRANSITION_LOCK, AuthorityServiceUtils.DONT_UPDATE_REV);
    	}
    
    /**
     * Try to synchronize a remote item (using its refName) with a local item.  If the local doesn't yet
     * exist, we'll create it.
     * Result values:
     * 	-1 = sync not needed; i.e., already in sync
     *   0 = sync succeeded
     *   1 = local item was missing so we created it
     * @param ctx
     * @param refName
     * @return
     * @throws Exception
     */
    protected long syncRemoteItemWithLocalItem(ServiceContext ctx, String itemRefName) throws Exception {
    	long result = -1;
    	//
    	// Using the item refname (with no local CSID), create specifiers that we'll use to find the local versions
    	//
    	AuthorityTermInfo authorityTermInfo = RefNameUtils.parseAuthorityTermInfo(itemRefName);
    	String parentIdentifier = Specifier.createShortIdURNValue(authorityTermInfo.inAuthority.name);
    	String itemIdentifier = Specifier.createShortIdURNValue(authorityTermInfo.name);
    	//
    	// We'll use the Authority JAX-RS resource to peform sync operations (creates and updates)
    	//
    	AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();    	
    	PoxPayloadOut localItemPayloadOut;
    	try {
    		localItemPayloadOut = authorityResource.getAuthorityItemWithExistingContext(ctx, parentIdentifier, itemIdentifier);
    	} catch (DocumentNotFoundException dnf) {
    		//
    		// Document not found, means we need to create an item/term that exists only on the SAS
    		//
    		logger.info(String.format("Remote item with refname='%s' doesn't exist locally, so we'll create it.", itemRefName));
    		createLocalItem(ctx, parentIdentifier, itemIdentifier);
    		return 1; // exit with status of 1 means we created a new authority item
    	}
    	//
    	// If we get here, we know the item exists both locally and remotely, so we need to synchronize them.
    	//
    	//
    	try {
	    	PoxPayloadOut theUpdate = authorityResource.synchronizeItemWithExistingContext(ctx, parentIdentifier, itemIdentifier);
	    	if (theUpdate != null) {
	    		result = 0; // means we needed to sync this item with SAS
	    		logger.debug(String.format("Sync'd authority item parent='%s' id='%s with SAS.  Updated payload is: \n%s",
	    				parentIdentifier, itemIdentifier, theUpdate.getXmlPayload()));
	    	}
    	} catch (DocumentReferenceException de) { // Exception for items that still have records/resource referencing them.
    		result = -1;
    		logger.error(String.format("Could not sync authority item = '%s' because it has existing records referencing it.",
    				itemIdentifier));
    	}
    	
    	return result; // -1 = no sync needed/possible, 0 = sync'd, 1 = created new item
    }
        
    private PoxPayloadIn requestPayloadInItemList(ServiceContext ctx, Specifier specifier) throws Exception {
    	PoxPayloadIn result = null;
    	
        AuthorityClient client = (AuthorityClient) ctx.getClient();
        Response res = client.readItemList(specifier.getURNValue(),
        		null,	// partial term string
        		null	// keyword string
        		);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(client.getClass().getCanonicalName() + ": status = " + statusCode);
	        }
	        
            result = new PoxPayloadIn((String)res.readEntity(getEntityResponseType())); // Get the entire response!	        
        } finally {
        	res.close();
        }
    	
    	return result;
    }
    

    /*
     * Non standard injection of CSID into common part, since caller may access through
     * shortId, and not know the CSID.
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);

        // Add the CSID to the common part
        if (partMeta.getLabel().equalsIgnoreCase(authorityCommonSchemaName)) {
            String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            unQObjectProperties.put("csid", csid);
        }

        return unQObjectProperties;
    }
    
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
    	super.fillAllParts(wrapDoc, action);
    	//
    	// Update the record's revision number on both CREATE and UPDATE actions, but not on SYNC
    	//
    	if (this.getShouldUpdateRevNumber() == true) { // We won't update rev numbers on synchronization with SAS
    		updateRevNumbers(wrapDoc);
    	}
    }
    
    protected void updateRevNumbers(DocumentWrapper<DocumentModel> wrapDoc) {
    	DocumentModel documentModel = wrapDoc.getWrappedObject();
    	Long rev = (Long)documentModel.getProperty(authorityCommonSchemaName, AuthorityJAXBSchema.REV);
    	if (rev == null) {
    		rev = (long)0;
    	} else {
    		rev++;
    	}
    	documentModel.setProperty(authorityCommonSchemaName, AuthorityJAXBSchema.REV, rev);
    }
    
    /*
     * We consider workflow state changes as changes that should bump the revision number
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#handleWorkflowTransition(org.collectionspace.services.common.document.DocumentWrapper, org.collectionspace.services.lifecycle.TransitionDef)
     */
    @Override
    public void handleWorkflowTransition(DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef) throws Exception {
    	// Update the revision number
    	updateRevNumbers(wrapDoc);
    }
    
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.handleCreate(wrapDoc);
        // CSPACE-3178:
        // Uncomment once debugged and App layer is read to integrate
        // Experimenting with this uncommented now ...
        handleDisplayNameAsShortIdentifier(wrapDoc.getWrappedObject(), authorityCommonSchemaName);
        updateRefnameForAuthority(wrapDoc, authorityCommonSchemaName);//CSPACE-3178
    }
    
    protected String buildWhereForShortId(String name) {
        return authorityCommonSchemaName
                + ":" + AuthorityJAXBSchema.SHORT_IDENTIFIER
                + "='" + name + "'";
    }
    
    private boolean isUnique(DocumentModel docModel, String schemaName) throws DocumentException {
    	return true;
    }
    
    private boolean temp_isUnique(DocumentModel docModel, String schemaName) throws DocumentException {
    	boolean result = true;
    	
    	ServiceContext ctx = this.getServiceContext();
        String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
    	String nxqlWhereClause = buildWhereForShortId(shortIdentifier);
    	try {
			DocumentWrapper<DocumentModel> searchResultWrapper = getRepositoryClient(ctx).findDoc(ctx, nxqlWhereClause);
			if (searchResultWrapper != null) {
				result = false;
				if (logger.isInfoEnabled() == true) {
					DocumentModel searchResult = searchResultWrapper.getWrappedObject();
					String debugMsg = String.format("Could not create a new authority with a short identifier of '%s', because one already exists with the same short identifer: CSID = '%s'",
							shortIdentifier, searchResult.getName());
					logger.trace(debugMsg);
				}
			}
		} catch (DocumentNotFoundException e) {
			// Not a problem, just means we couldn't find another authority with that short ID
		}
    	
    	return result;
    }

    /**
     * If no short identifier was provided in the input payload,
     * generate a short identifier from the display name. Either way though,
     * the short identifier needs to be unique.
     */
    private void handleDisplayNameAsShortIdentifier(DocumentModel docModel, String schemaName) throws Exception {
        String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        String displayName = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.DISPLAY_NAME);
        String shortDisplayName = "";
        String generateShortIdentifier = null;
        if (Tools.isEmpty(shortIdentifier)) {
        	generateShortIdentifier = AuthorityIdentifierUtils.generateShortIdentifierFromDisplayName(displayName, shortDisplayName);
            docModel.setProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);
        }
        
        if (isUnique(docModel, schemaName) == false) {
        	String shortId = generateShortIdentifier == null ? shortIdentifier : generateShortIdentifier;
        	String errMsgVerb = generateShortIdentifier == null ? "supplied" : "generated";
        	String errMsg = String.format("The %s short identifier '%s' was not unique, so the new authority could not be created.",
        			errMsgVerb, shortId);
        	throw new DocumentException(errMsg);
        }
    }
 
    /**
     * Generate a refName for the authority from the short identifier
     * and display name.
     * 
     * All refNames for authorities are generated.  If a client supplies
     * a refName, it will be overwritten during create (per this method) 
     * or discarded during update (per filterReadOnlyPropertiesForPart).
     * 
     * @see #filterReadOnlyPropertiesForPart(Map<String, Object>, org.collectionspace.services.common.service.ObjectPartType)
     * 
     */
    protected void updateRefnameForAuthority(DocumentWrapper<DocumentModel> wrapDoc, String schemaName) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        RefName.Authority authority = (Authority) getRefName(getServiceContext(), docModel);
        String refName = authority.toString();
        docModel.setProperty(schemaName, AuthorityJAXBSchema.REF_NAME, refName);
    }
    
    @Override
    public RefName.RefNameInterface getRefName(ServiceContext ctx,
    		DocumentModel docModel) {
    	RefName.RefNameInterface refname = null;

    	try {
	        String shortIdentifier = (String) docModel.getProperty(authorityCommonSchemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
	        String displayName = (String) docModel.getProperty(authorityCommonSchemaName, AuthorityJAXBSchema.DISPLAY_NAME);
	        RefName.Authority authority = RefName.Authority.buildAuthority(ctx.getTenantName(),
	                ctx.getServiceName(),
	                null,	// Only use shortId form!!!
	                shortIdentifier,
	                displayName);
	        refname = authority;
    	} catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	
    	return refname;
    }
    
    @Override
    protected String getRefnameDisplayName(DocumentWrapper<DocumentModel> docWrapper) {
    	String result = null;
    	
    	DocumentModel docModel = docWrapper.getWrappedObject();
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
    	RefName.Authority refname = (RefName.Authority)getRefName(ctx, docModel);
    	result = refname.getDisplayName();
    	
    	return result;
    }    
    
    public String getShortIdentifier(ServiceContext ctx, String authCSID, String schemaName) throws Exception {
        String shortIdentifier = null;
        CoreSessionInterface repoSession = null;
        boolean releaseSession = false;

    	RepositoryClientImpl nuxeoRepoClient = (RepositoryClientImpl)this.getRepositoryClient(ctx);
        try {
        	repoSession = nuxeoRepoClient.getRepositorySession(ctx);
            DocumentWrapper<DocumentModel> wrapDoc = nuxeoRepoClient.getDocFromCsid(ctx, repoSession, authCSID);
            DocumentModel docModel = wrapDoc.getWrappedObject();
            if (docModel == null) {
            	throw new DocumentNotFoundException(String.format("Could not find authority resource with CSID='%s'.", authCSID));
            }
            shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        } catch (ClientException ce) {
            throw new RuntimeException("AuthorityDocHandler Internal Error: cannot get shortId!", ce);
        } finally {
        	if (repoSession != null) {
        		nuxeoRepoClient.releaseRepositorySession(ctx, repoSession);
        	}
        }
        
        return shortIdentifier;
    }

    /**
     * Filters out selected values supplied in an update request.
     * 
     * @param objectProps the properties filtered out from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
            Map<String, Object> objectProps, ObjectPartType partMeta) {
        super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
        String commonPartLabel = getServiceContext().getCommonPartLabel();
        if (partMeta.getLabel().equalsIgnoreCase(commonPartLabel)) {
            objectProps.remove(AuthorityJAXBSchema.CSID);
            objectProps.remove(AuthorityJAXBSchema.SHORT_IDENTIFIER);
            objectProps.remove(AuthorityJAXBSchema.REF_NAME);
        }
    }    
}
