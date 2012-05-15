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

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.collectionspace.services.relation.RelationshipType;
import org.collectionspace.services.vocabulary.VocabularyItemJAXBSchema;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;

//import org.collectionspace.services.common.authority.AuthorityItemRelations;
/**
 * AuthorityItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityItemDocumentModelHandler<AICommon>
        extends DocHandlerBase<AICommon> {

    private final Logger logger = LoggerFactory.getLogger(AuthorityItemDocumentModelHandler.class);
    private String authorityItemCommonSchemaName;
    private String authorityItemTermGroupXPathBase;
    /**
     * inVocabulary is the parent Authority for this context
     */
    protected String inAuthority = null;
    protected String authorityRefNameBase = null;
    // Used to determine when the displayName changes as part of the update.
    protected String oldDisplayNameOnUpdate = null;
    protected String oldRefNameOnUpdate = null;
    protected String newRefNameOnUpdate = null;

    public AuthorityItemDocumentModelHandler(String authorityItemCommonSchemaName) {
        this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }

    public void setInAuthority(String inAuthority) {
        this.inAuthority = inAuthority;
    }

    /** Subclasses may override this to customize the URI segment. */
    public String getAuthorityServicePath() {
        return getServiceContext().getServiceName().toLowerCase();    // Laramie20110510 CSPACE-3932
    }

    @Override
    public String getUri(DocumentModel docModel) {
        // Laramie20110510 CSPACE-3932
        String authorityServicePath = getAuthorityServicePath();
        if(inAuthority==null) {	// Only happens on queries to wildcarded authorities
        	try {
	        	inAuthority = (String) docModel.getProperty(authorityItemCommonSchemaName,
	                AuthorityItemJAXBSchema.IN_AUTHORITY);
    		} catch (ClientException pe) {
    			throw new RuntimeException("Could not get parent specifier for item!");
    		}
        }
        return "/" + authorityServicePath + '/' + inAuthority + '/' + AuthorityClient.ITEMS + '/' + getCsid(docModel);
    }

    protected String getAuthorityRefNameBase() {
        return this.authorityRefNameBase;
    }

    public void setAuthorityRefNameBase(String value) {
        this.authorityRefNameBase = value;
    }

    /*
     * Note: the Vocabulary service's VocabularyItemDocumentModelHandler class overrides this method.
     */
    protected ListResultField getListResultsDisplayNameField() {
    	ListResultField result = new ListResultField();
    	// Per CSPACE-5132, the name of this element remains 'displayName'
        // for backwards compatibility, although its value is obtained
        // from the termDisplayName field.
        //
        // Update: this name is now being changed to 'termDisplayName', both
        // because this is the actual field name and because the app layer
        // work to convert over to this field is underway. Per Patrick, the
        // app layer treats lists, in at least some context(s), as sparse record
        // payloads, and thus fields in list results must all be present in
        // (i.e. represent a strict subset of the fields in) record schemas.
        // - ADR 2012-05-11
        // 
        //
        // In CSPACE-5134, these list results will change substantially
        // to return display names for both the preferred term and for
        // each non-preferred term (if any).
    	result.setElement(AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
    	result.setXpath(NuxeoUtils.getPrimaryXPathPropertyName(
                authorityItemCommonSchemaName, getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_DISPLAY_NAME));
    	
    	return result;
    }
    
    /*
     * Note: the Vocabulary service's VocabularyItemDocumentModelHandler class overrides this method.
     */    
    protected ListResultField getListResultsTermStatusField() {
    	ListResultField result = new ListResultField();
        
    	result.setElement(AuthorityItemJAXBSchema.TERM_STATUS);
    	result.setXpath(NuxeoUtils.getPrimaryXPathPropertyName(
                authorityItemCommonSchemaName, getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_STATUS));

        return result;
    }    
    
    @Override
    public List<ListResultField> getListItemsArray() throws DocumentException {
        List<ListResultField> list = super.getListItemsArray();
        int nFields = list.size();
        // Ensure that each item in a list of Authority items includes
        // a set of common fields, so we do not depend upon configuration
        // for general logic.
        boolean hasDisplayName = false;
        boolean hasShortId = false;
        boolean hasRefName = false;
        boolean hasTermStatus = false;
        for (int i = 0; i < nFields; i++) {
            ListResultField field = list.get(i);
            String elName = field.getElement();
            if (AuthorityItemJAXBSchema.TERM_DISPLAY_NAME.equals(elName) || VocabularyItemJAXBSchema.DISPLAY_NAME.equals(elName)) {
                hasDisplayName = true;
            } else if (AuthorityItemJAXBSchema.SHORT_IDENTIFIER.equals(elName)) {
                hasShortId = true;
            } else if (AuthorityItemJAXBSchema.REF_NAME.equals(elName)) {
                hasRefName = true;
            } else if (AuthorityItemJAXBSchema.TERM_STATUS.equals(elName)) {
                hasTermStatus = true;
            }
        }
        ListResultField field;
        if (!hasDisplayName) {
        	field = getListResultsDisplayNameField();
            list.add(field);  //Note: We're updating the "global" service and tenant bindings instance here -the list instance here is a reference to the tenant bindings instance in the singleton ServiceMain.
        }
        if (!hasShortId) {
            field = new ListResultField();
            field.setElement(AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
            field.setXpath(AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
            list.add(field);
        }
        if (!hasRefName) {
            field = new ListResultField();
            field.setElement(AuthorityItemJAXBSchema.REF_NAME);
            field.setXpath(AuthorityItemJAXBSchema.REF_NAME);
            list.add(field);
        }
        if (!hasTermStatus) {
            field = getListResultsTermStatusField();
            list.add(field);
        }
        
        return list;
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // first fill all the parts of the document
        super.handleCreate(wrapDoc);
        // Ensure we have required fields set properly
        handleInAuthority(wrapDoc.getWrappedObject());
        
        // FIXME: This call to synthesize a shortIdentifier from the termDisplayName
        // of the preferred term may have been commented out, in the course of
        // adding support for preferred / non-preferred terms, in CSPACE-4813
        // and linked issues. Revisit this to determine whether we want to
        // re-enable it.
        //
        // CSPACE-3178:
        // handleDisplayNameAsShortIdentifier(wrapDoc.getWrappedObject(), authorityItemCommonSchemaName);
        // refName includes displayName, so we force a correct value here.
        updateRefnameForAuthorityItem(wrapDoc, authorityItemCommonSchemaName, getAuthorityRefNameBase());
    }

    /*
     * Note that the Vocabulary service's document-model for items overrides this method.
     */
	protected String getPrimaryDisplayName(DocumentModel docModel, String schema,
			String complexPropertyName, String fieldName) {
		String result = null;

		result = getStringValueInPrimaryRepeatingComplexProperty(docModel, schema, complexPropertyName, fieldName);
		
		return result;
	}
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // First, get a copy of the old displayName
        // oldDisplayNameOnUpdate = (String) wrapDoc.getWrappedObject().getProperty(authorityItemCommonSchemaName,
        //        AuthorityItemJAXBSchema.DISPLAY_NAME);
        oldDisplayNameOnUpdate = getPrimaryDisplayName(wrapDoc.getWrappedObject(), authorityItemCommonSchemaName,
                getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
        oldRefNameOnUpdate = (String) wrapDoc.getWrappedObject().getProperty(authorityItemCommonSchemaName,
                AuthorityItemJAXBSchema.REF_NAME);
        super.handleUpdate(wrapDoc);

        // Now, check the new display and handle the refname update.
        String newDisplayName = (String) getPrimaryDisplayName(wrapDoc.getWrappedObject(), authorityItemCommonSchemaName,
                authorityItemTermGroupXPathBase,
                AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
        if (newDisplayName != null && !newDisplayName.equals(oldDisplayNameOnUpdate)) {
            // Need to update the refName, and then fix all references.
            newRefNameOnUpdate = handleItemRefNameUpdateForDisplayName(wrapDoc.getWrappedObject(), newDisplayName);
        } else {
            // Mark as not needing attention in completeUpdate phase.
            newRefNameOnUpdate = null;
            oldRefNameOnUpdate = null;
        }
    }

    /**
     * Handle display name.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
//    protected void handleComputedDisplayNames(DocumentModel docModel) throws Exception {
//        // Do nothing by default.
//    }

    /**
     * Handle refName updates for changes to display name.
     * Assumes refName is already correct. Just ensures it is right.
     *
     * @param docModel the doc model
     * @param newDisplayName the new display name
     * @throws Exception the exception
     */
    protected String handleItemRefNameUpdateForDisplayName(DocumentModel docModel,
            String newDisplayName) throws Exception {
        RefName.AuthorityItem authItem = RefName.AuthorityItem.parse(oldRefNameOnUpdate);
        if (authItem == null) {
            String err = "Authority Item has illegal refName: " + oldRefNameOnUpdate;
            logger.debug(err);
            throw new IllegalArgumentException(err);
        }
        authItem.displayName = newDisplayName;
        String updatedRefName = authItem.toString();
        docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REF_NAME, updatedRefName);
        return updatedRefName;
    }
    
    /*
     * Note: The Vocabulary document handler overrides this method.
     */
    protected String getRefPropName() {
    	return ServiceBindingUtils.AUTH_REF_PROP;
    }

    /**
     * Checks to see if the refName has changed, and if so, 
     * uses utilities to find all references and update them.
     * @throws Exception 
     */
    protected void handleItemRefNameReferenceUpdate() throws Exception {
        if (newRefNameOnUpdate != null && oldRefNameOnUpdate != null) {
            // We have work to do.
            if (logger.isDebugEnabled()) {
                String eol = System.getProperty("line.separator");
                logger.debug("Need to find and update references to Item." + eol
                        + "   Old refName" + oldRefNameOnUpdate + eol
                        + "   New refName" + newRefNameOnUpdate);
            }
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
            RepositoryClient repoClient = getRepositoryClient(ctx);
            String refNameProp = getRefPropName();

            int nUpdated = RefNameServiceUtils.updateAuthorityRefDocs(ctx, repoClient, this.getRepositorySession(),
                    oldRefNameOnUpdate, newRefNameOnUpdate, refNameProp);
            if (logger.isDebugEnabled()) {
                logger.debug("Updated " + nUpdated + " instances of oldRefName to newRefName");
            }
        }
    }

    /**
     * If no short identifier was provided in the input payload, generate a
     * short identifier from the preferred term display name or term name.
     */
	private void handleDisplayNameAsShortIdentifier(DocumentModel docModel,
			String schemaName) throws Exception {
		String shortIdentifier = (String) docModel.getProperty(schemaName,
				AuthorityItemJAXBSchema.SHORT_IDENTIFIER);

		String termDisplayName = getPrimaryDisplayName(
				docModel, authorityItemCommonSchemaName,
				getItemTermInfoGroupXPathBase(),
				AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);

		String termName = getPrimaryDisplayName(
				docModel, authorityItemCommonSchemaName,
				getItemTermInfoGroupXPathBase(),
				AuthorityItemJAXBSchema.TERM_NAME);

		if (Tools.isEmpty(shortIdentifier)) {
			String generatedShortIdentifier = AuthorityIdentifierUtils.generateShortIdentifierFromDisplayName(termDisplayName,
							termName);
			docModel.setProperty(schemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER,
					generatedShortIdentifier);
		}
	}

    /**
     * Generate a refName for the authority item from the short identifier
     * and display name.
     * 
     * All refNames for authority items are generated.  If a client supplies
     * a refName, it will be overwritten during create (per this method) 
     * or discarded during update (per filterReadOnlyPropertiesForPart).
     * 
     * @see #filterReadOnlyPropertiesForPart(Map<String, Object>, org.collectionspace.services.common.service.ObjectPartType)
     * 
     */
    protected void updateRefnameForAuthorityItem(DocumentWrapper<DocumentModel> wrapDoc,
            String schemaName,
            String authorityRefBaseName) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
        String displayName = getPrimaryDisplayName(docModel, authorityItemCommonSchemaName,
                    getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
        
        if (Tools.isEmpty(authorityRefBaseName)) {
            throw new Exception("Could not create the refName for this authority term, because the refName for its authority parent was empty.");
        }
        
        RefName.Authority authority = RefName.Authority.parse(authorityRefBaseName);
        String refName = RefName.buildAuthorityItem(authority, shortIdentifier, displayName).toString();
        docModel.setProperty(schemaName, AuthorityItemJAXBSchema.REF_NAME, refName);
    }

    /**
     * Check the logic around the parent pointer. Note that we only need do this on
     * create, since we have logic to make this read-only on update. 
     * 
     * @param docModel
     * 
     * @throws Exception the exception
     */
    private void handleInAuthority(DocumentModel docModel) throws Exception {
        if(inAuthority==null) {	// Only happens on queries to wildcarded authorities
        	throw new IllegalStateException("Trying to Create an object with no inAuthority value!");
        }
        docModel.setProperty(authorityItemCommonSchemaName,
                AuthorityItemJAXBSchema.IN_AUTHORITY, inAuthority);
    }
    
    
    public AuthorityRefDocList getReferencingObjects(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		List<String> serviceTypes,
    		String propertyName,
            String itemcsid) throws Exception {
        AuthorityRefDocList authRefDocList = null;
    	RepositoryInstance repoSession = null;
    	boolean releaseRepoSession = false;
        
    	try { 
    		RepositoryJavaClientImpl repoClient = (RepositoryJavaClientImpl)this.getRepositoryClient(ctx);
    		repoSession = this.getRepositorySession();
    		if (repoSession == null) {
    			repoSession = repoClient.getRepositorySession();
    			releaseRepoSession = true;
    		}
            DocumentFilter myFilter = getDocumentFilter();

    		try {
    			DocumentWrapper<DocumentModel> wrapper = repoClient.getDoc(repoSession, ctx, itemcsid);
    			DocumentModel docModel = wrapper.getWrappedObject();
    			String refName = (String) docModel.getPropertyValue(AuthorityItemJAXBSchema.REF_NAME);
                authRefDocList = RefNameServiceUtils.getAuthorityRefDocs(
                		repoSession, ctx, repoClient,
                        serviceTypes,
                        refName,
                        propertyName,
                        myFilter.getPageSize(), myFilter.getStartPage(), true /*computeTotal*/);
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
        return authRefDocList;
    }




    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);

        // Add the CSID to the common part, since they may have fetched via the shortId.
        if (partMeta.getLabel().equalsIgnoreCase(authorityItemCommonSchemaName)) {
            String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            unQObjectProperties.put("csid", csid);
        }

        return unQObjectProperties;
    }

    /**
     * Filters out selected values supplied in an update request.
     * 
     * For example, filters out AuthorityItemJAXBSchema.IN_AUTHORITY, to ensure
     * that the link to the item's parent remains untouched.
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
            objectProps.remove(AuthorityItemJAXBSchema.IN_AUTHORITY);
            objectProps.remove(AuthorityItemJAXBSchema.CSID);
            objectProps.remove(AuthorityJAXBSchema.SHORT_IDENTIFIER);
            objectProps.remove(AuthorityItemJAXBSchema.REF_NAME);
        }
    }

    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        super.extractAllParts(wrapDoc);

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

        List<RelationsCommonList.RelationListItem> toRemoveList = newList();


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

    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        /*
        ServiceContext ctx = getServiceContext();
        PoxPayloadIn input = (PoxPayloadIn) ctx.getInput();
        DocumentModel documentModel = (wrapDoc.getWrappedObject());
        String itemCsid = documentModel.getName();
        
        //UPDATE and CREATE will call.   Updates relations part
        RelationsCommonList relationsCommonList = updateRelations(itemCsid, input, wrapDoc);
        
        PayloadOutputPart payloadOutputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, relationsCommonList);
        ctx.setProperty(RelationClient.SERVICE_COMMON_LIST_NAME, payloadOutputPart);
         */
    }

    public void completeCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.completeCreate(wrapDoc);
        handleRelationsPayload(wrapDoc, false);
    }

    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.completeUpdate(wrapDoc);
        handleRelationsPayload(wrapDoc, true);
        handleItemRefNameReferenceUpdate();
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

    /**  updateRelations strategy:
    
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
        List<RelationsCommonList.RelationListItem> actionList = newList();
        List<RelationsCommonList.RelationListItem> childList = null;
        List<RelationsCommonList.RelationListItem> parentList = null;
        DocumentModel docModel = wrapDoc.getWrappedObject();
		String itemRefName = (String) docModel.getPropertyValue(AuthorityItemJAXBSchema.REF_NAME);

        ServiceContext ctx = getServiceContext();
        //Do magic replacement of ${itemCSID} and fix URI's.
        fixupInboundListItems(ctx, inboundList, docModel, itemCSID);

        String HAS_BROADER = RelationshipType.HAS_BROADER.value();
        UriInfo uriInfo = ctx.getUriInfo();
        MultivaluedMap queryParams = uriInfo.getQueryParameters();

        if (fUpdate) {
            //Run getList() once as sent to get childListOuter:
            String predicate = RelationshipType.HAS_BROADER.value();
            queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
            queryParams.putSingle(IRelationsManager.SUBJECT_QP, null);
            queryParams.putSingle(IRelationsManager.SUBJECT_TYPE_QP, null);
            queryParams.putSingle(IRelationsManager.OBJECT_QP, itemCSID);
            queryParams.putSingle(IRelationsManager.OBJECT_TYPE_QP, null);
            RelationsCommonList childListOuter = (new RelationResource()).getList(ctx.getUriInfo());    //magically knows all query params because they are in the context.

            //Now run getList() again, leaving predicate, swapping subject and object, to get parentListOuter.
            queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
            queryParams.putSingle(IRelationsManager.SUBJECT_QP, itemCSID);
            queryParams.putSingle(IRelationsManager.OBJECT_QP, null);
            RelationsCommonList parentListOuter = (new RelationResource()).getList(ctx.getUriInfo());


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

    private void ensureChildHasNoOtherParents(ServiceContext ctx, MultivaluedMap queryParams, String childCSID) {
        logger.trace("ensureChildHasNoOtherParents for: " + childCSID );
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, childCSID);
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, RelationshipType.HAS_BROADER.value());
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);  //null means ANY
        RelationsCommonList parentListOuter = (new RelationResource()).getList(ctx.getUriInfo());
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();
        //logger.warn("ensureChildHasNoOtherParents preparing to delete relations on "+childCSID+"\'s parent list: \r\n"+dumpList(parentList, "duplicate parent list"));
        deleteRelations(parentList, ctx, "parentList-delete");
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
    private final static String CR = "\r\n";
    private final static String T = " ";

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

    // this method calls the RelationResource to have it create the relations and persist them.
    private void createRelations(List<RelationsCommonList.RelationListItem> inboundList, ServiceContext ctx) throws Exception {
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
            Object res = relationResource.create(ctx.getResourceMap(),
                    ctx.getUriInfo(), payloadOut.toXML());    //NOTE ui recycled from above to pass in unknown query params.
        }
    }

    private void deleteRelations(List<RelationsCommonList.RelationListItem> list, ServiceContext ctx, String listName) {
        try {
            for (RelationsCommonList.RelationListItem item : list) {
                RelationResource relationResource = new RelationResource();
                if(logger.isTraceEnabled()) {
                	StringBuilder sb = new StringBuilder();
                	itemToString(sb, "==== TO DELETE: ", item);
                	logger.trace(sb.toString());
                }
                Object res = relationResource.delete(item.getCsid());
            }
        } catch (Throwable t) {
            String msg = "Unable to deleteRelations: " + Tools.errorToString(t, true);
            logger.error(msg);
        }
    }

    private List<RelationsCommonList.RelationListItem> newList() {
        List<RelationsCommonList.RelationListItem> result = new ArrayList<RelationsCommonList.RelationListItem>();
        return result;
    }

    protected List<RelationsCommonList.RelationListItem> cloneList(List<RelationsCommonList.RelationListItem> inboundList) {
        List<RelationsCommonList.RelationListItem> result = newList();
        for (RelationsCommonList.RelationListItem item : inboundList) {
            result.add(item);
        }
        return result;
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

        boolean isEqual = 
        		   (subj1Csid.equals(subj2Csid) || ((subj2Csid==null)  && subj1RefName.equals(subj2RefName)))
                && (obj1Csid.equals(obj1Csid)   || ((obj2Csid==null)   && obj1RefName.equals(obj2RefName)))
                // predicate is proper, but still allow relationshipType
                && (item1.getPredicate().equals(item2.getPredicate())
                	||  ((item2.getPredicate()==null)  && item1.getRelationshipType().equals(item2.getRelationshipType())))
                // Allow missing docTypes, so long as they do not conflict
                && (obj1.getDocumentType().equals(obj2.getDocumentType()) || obj2.getDocumentType()==null)
                && (subj1.getDocumentType().equals(subj2.getDocumentType()) || subj2.getDocumentType()==null);
        return isEqual;
    }

    private void removeFromList(List<RelationsCommonList.RelationListItem> list, RelationsCommonList.RelationListItem item) {
        list.remove(item);
    }

    /* don't even THINK of re-using this method.
     * String example_uri = "/locationauthorities/7ec60f01-84ab-4908-9a6a/items/a5466530-713f-43b4-bc05";
     */
    private String extractInAuthorityCSID(String uri) {
        String IN_AUTHORITY_REGEX = "/(.*?)/(.*?)/(.*)";
        Pattern p = Pattern.compile(IN_AUTHORITY_REGEX);
        Matcher m = p.matcher(uri);
        if (m.find()) {
            if (m.groupCount() < 3) {
                logger.warn("REGEX-WRONG-GROUPCOUNT looking in " + uri);
                return "";
            } else {
                //String service = m.group(1);
                String inauth = m.group(2);
                //String theRest = m.group(3);
                return inauth;
                //print("service:"+service+", inauth:"+inauth+", rest:"+rest);
            }
        } else {
            logger.warn("REGEX-NOT-MATCHED looking in " + uri);
            return "";
        }
    }

    //ensures CSPACE-4042
    protected void uriPointsToSameAuthority(String thisURI, String inboundItemURI) throws Exception {
        String authorityCSID = extractInAuthorityCSID(thisURI);
        String authorityCSIDForInbound = extractInAuthorityCSID(inboundItemURI);
        if (Tools.isBlank(authorityCSID)
                || Tools.isBlank(authorityCSIDForInbound)
                || (!authorityCSID.equalsIgnoreCase(authorityCSIDForInbound))) {
            throw new Exception("Item URI " + thisURI + " must point to same authority as related item: " + inboundItemURI);
        }
    }

    //================= TODO: move this to common, refactoring this and  CollectionObjectResource.java
    public RelationsCommonList getRelations(String subjectCSID, String objectCSID, String predicate) throws Exception {
        ServiceContext ctx = getServiceContext();
        MultivaluedMap queryParams = ctx.getQueryParams();
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, subjectCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, objectCSID);

        RelationResource relationResource = new RelationResource();
        RelationsCommonList relationsCommonList = relationResource.getList(ctx.getUriInfo());
        return relationsCommonList;
    }
    //============================= END TODO refactor ==========================

    public String getItemTermInfoGroupXPathBase() {
        return authorityItemTermGroupXPathBase;
    }
        
    public void setItemTermInfoGroupXPathBase(String itemTermInfoGroupXPathBase) {
        authorityItemTermGroupXPathBase = itemTermInfoGroupXPathBase;
    }
    
    protected String getAuthorityItemCommonSchemaName() {
    	return authorityItemCommonSchemaName;
    }
}
