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

import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefName.Authority;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityInfo;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.RepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

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

    public AuthorityDocumentModelHandler(String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
        this.authorityCommonSchemaName = authorityCommonSchemaName;
        this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
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
    public void handleSync(DocumentWrapper<Specifier> wrapDoc) throws Exception {
    	
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
        Specifier specifier = wrapDoc.getWrappedObject();
        //
        // Get the rev number of the authority so we can compare with rev number of shared authority
        //
        DocumentModel docModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), authorityCommonSchemaName, specifier);
        Long rev = (Long) NuxeoUtils.getProperyValue(docModel, AuthorityItemJAXBSchema.REV);
        String shortId = (String) NuxeoUtils.getProperyValue(docModel, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
        String refName = (String) NuxeoUtils.getProperyValue(docModel, AuthorityItemJAXBSchema.REF_NAME);
        AuthorityInfo authorityInfo = RefNameUtils.parseAuthorityInfo(refName);
        //
        // Using the short ID of the local authority, created a URN specifier to retrieve the SAS authority
        //
        Specifier sasSpecifier = new Specifier(SpecifierForm.URN_NAME, RefNameUtils.createShortIdRefName(shortId));
        Long sasRev = getRevFromSASInstance(sasSpecifier);
        
        AuthorityClient client = ctx.getAuthorityClient();
        Response res = client.read(sasSpecifier.value);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(client.getClass().getCanonicalName() + ": status = " + statusCode);
	        }
	        
            PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(getEntityResponseType())); // Get the entire response!
	        
			PayloadInputPart payloadInputPart = extractPart(res, client.getCommonPartName());
			if (payloadInputPart != null) {
//				result = (client.getc) payloadInputPart.getBody();
			}
        } finally {
        	res.close();
        }
        
    }
    
    private Long getRevFromSASInstance(Specifier specifier) {
    	Long result = null;
    	
    	VocabularyClient client = new VocabularyClient();
    	String uri = getUri(specifier);
    	
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
    	// Update the record's revision number on both CREATE and UPDATE actions
    	//
    	updateRevNumbers(wrapDoc);
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
    
    public String getShortIdentifier(String authCSID, String schemaName) throws Exception {
        String shortIdentifier = null;
        CoreSessionInterface repoSession = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
    	RepositoryClientImpl nuxeoRepoClient = (RepositoryClientImpl)this.getRepositoryClient(ctx);
        try {
        	repoSession = nuxeoRepoClient.getRepositorySession(ctx);
            DocumentWrapper<DocumentModel> wrapDoc = nuxeoRepoClient.getDocFromCsid(ctx, repoSession, authCSID);
            DocumentModel docModel = wrapDoc.getWrappedObject();
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
