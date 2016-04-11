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

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
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
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.config.service.ObjectPartType;
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
    protected boolean shouldUpdateRevNumber;

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
        Long rev = (Long) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.REV);
        String shortId = (String) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        String refName = (String) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.REF_NAME);
        //
        // Using the short ID of the local authority, create a URN specifier to retrieve the SAS authority
        //
        Specifier sasSpecifier = new Specifier(SpecifierForm.URN_NAME, RefNameUtils.createShortIdRefName(shortId));
        PoxPayloadIn sasPayloadIn = getPayloadIn(ctx, sasSpecifier);

        Long sasRev = getRevision(sasPayloadIn);
        if (sasRev > rev) {
        	//
        	// First, sync all the authority items
        	//
        	syncAllItems(ctx, sasSpecifier);
        	//
        	// Next, sync the authority resource/record itself
        	//
        	ResourceMap resourceMap = ctx.getResourceMap();
        	String resourceName = ctx.getClient().getServiceName();
        	AuthorityResource authorityResource = (AuthorityResource) resourceMap.get(resourceName);
        	ctx.setProperty(AuthorityServiceUtils.SHOULD_UPDATE_REV_PROPERTY, // Since it is a sync, don't update the rev.  Instead use the rev from the SAS
        			new Boolean(AuthorityServiceUtils.DONT_UPDATE_REV));
        	PoxPayloadOut payloadOut = authorityResource.update(ctx, resourceMap, ctx.getUriInfo(), docModel.getName(), 
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
    protected int syncAllItems(ServiceContext ctx, Specifier sasSpecifier) throws Exception {
    	int result = -1;
    	int created = 0;
    	int synched = 0;
    	int alreadySynched = 0;
    	int totalItemsProcessed = 0;
    	
        PoxPayloadIn sasPayloadInItemList = getPayloadInItemList(ctx, sasSpecifier);
        List<Element> itemList = getItemList(sasPayloadInItemList);
        if (itemList != null) {
        	for (Element e:itemList) {
        		String remoteRefName = XmlTools.getElementValue(e, "refName");
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
        
        logger.info(String.format("Total number of items processed during sync: %d", totalItemsProcessed));
        logger.info(String.format("Number of items synchronized: %d", synched));
        logger.info(String.format("Number of items created during sync: %d", created));
        logger.info(String.format("Number not needing synchronization: %d", alreadySynched));

        return result;
    }
    
    /**
     * 
     * @param ctx
     * @param parentIdentifier - Must be in short-id-refname form -i.e., urn:cspace:name(shortid)
     * @param itemIdentifier   - Must be in short-id-refname form -i.e., urn:cspace:name(shortid)
     * @throws Exception 
     */
    protected void createLocalItem(ServiceContext ctx, String parentIdentifier, String itemIdentifier) throws Exception {
    	//
    	// Create a URN short ID specifier for the getting to the remote item payload
        Specifier authoritySpecifier = new Specifier(SpecifierForm.URN_NAME, parentIdentifier);
        Specifier itemSpecifier = new Specifier(SpecifierForm.URN_NAME, itemIdentifier);
        AuthorityItemSpecifier sasAuthorityItemSpecifier = new AuthorityItemSpecifier(authoritySpecifier, itemSpecifier);
        //
        // Get the remote payload
        //
        PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.getPayloadIn(sasAuthorityItemSpecifier, 
        		ctx.getServiceName(), getEntityResponseType());
        //
        // Using the payload from the remote server, create a local copy of the item
        //
    	ResourceMap resourceMap = ctx.getResourceMap();
    	String resourceName = ctx.getClient().getServiceName();
    	AuthorityResource authorityResource = (AuthorityResource) resourceMap.get(resourceName);
    	Response response = authorityResource.createAuthorityItemWithParentContext(ctx, authoritySpecifier.value,
    			sasPayloadIn, AuthorityServiceUtils.DONT_UPDATE_REV);
    	if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
    		throw new DocumentException(String.format("Could not create new authority item '%s' during synchronization of the '%s' authority.",
    				itemIdentifier, parentIdentifier));
    	}
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
    protected long syncRemoteItemWithLocalItem(ServiceContext ctx, String remoteRefName) throws Exception {
    	long result = -1;
    	
    	AuthorityTermInfo authorityTermInfo = RefNameUtils.parseAuthorityTermInfo(remoteRefName);
    	String parentIdentifier = RefNameUtils.createShortIdRefName(authorityTermInfo.inAuthority.name);
    	String itemIdentifier = RefNameUtils.createShortIdRefName(authorityTermInfo.name);

    	ResourceMap resourceMap = ctx.getResourceMap();
    	String resourceName = ctx.getClient().getServiceName();
    	AuthorityResource authorityResource = (AuthorityResource) resourceMap.get(resourceName);
    	
    	PoxPayloadOut localItemPayloadOut;
    	try {
    		localItemPayloadOut = authorityResource.getAuthorityItemWithParentContext(ctx, parentIdentifier, itemIdentifier);
    	} catch (DocumentNotFoundException dnf) {
    		logger.info(String.format("Remote item with refname='%s' doesn't exist locally, so we'll create it.", remoteRefName));
    		createLocalItem(ctx, parentIdentifier, itemIdentifier);
    		return 1; // exit with status of 1 means we created a new authority item
    	}
    	//
    	// If we get here, we know the item exists both locally and remotely, so we need to synchronize them
    	//
    	PoxPayloadOut theUpdate = authorityResource.synchronizeItemWithParentContext(ctx, parentIdentifier, itemIdentifier);
    	if (theUpdate != null) {
    		result = 0; // mean we neeed to sync this item with SAS
    		logger.debug(String.format("Sync'd authority item parent='%s' id='%s with SAS.  Updated payload is: \n%s",
    				parentIdentifier, itemIdentifier, theUpdate.getXmlPayload()));
    	}
    	
    	return result; // -1 = no sync needed, 0 = sync'd, 1 = created new item
    }
        
    private PoxPayloadIn getPayloadInItemList(ServiceContext ctx, Specifier specifier) throws Exception {
    	PoxPayloadIn result = null;
    	
        AuthorityClient client = (AuthorityClient) ctx.getClient();
        Response res = client.readItemList(specifier.value,
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
    
    private PoxPayloadIn getPayloadIn(ServiceContext ctx, Specifier specifier) throws Exception {
    	PoxPayloadIn result = null;
    	
        AuthorityClient client = (AuthorityClient) ctx.getClient();
        Response res = client.read(specifier.value);
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
