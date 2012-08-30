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
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;

import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ObjectPartType;

import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsDocListItem;

import org.collectionspace.services.vocabulary.VocabularyItemJAXBSchema;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    private boolean isTermDisplayName(String elName) {
    	return AuthorityItemJAXBSchema.TERM_DISPLAY_NAME.equals(elName) || VocabularyItemJAXBSchema.DISPLAY_NAME.equals(elName);
    }
    
    /*
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocHandlerBase#getListItemsArray()
     * 
     * Note: We're updating the "global" service and tenant bindings instance here -the list instance here is
     * a reference to the tenant bindings instance in the singleton ServiceMain.
     */
    @Override
    public List<ListResultField> getListItemsArray() throws DocumentException {
        List<ListResultField> list = super.getListItemsArray();
        
        // One-time initialization for each authority item service.
        if (isListItemArrayExtended() == false) {
        	synchronized(AuthorityItemDocumentModelHandler.class) {
        		if (isListItemArrayExtended() == false) {        			
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
        	            if (isTermDisplayName(elName) == true) {
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
        	            list.add(field);
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
        		}
        		
        		setListItemArrayExtended(true);
        	} // end of synchronized block
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
        handleDisplayNameAsShortIdentifier(wrapDoc.getWrappedObject(), authorityItemCommonSchemaName);
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
    
    /**
     * If no short identifier was provided in the input payload, generate a
     * short identifier from the preferred term display name or term name.
     */
	private void handleDisplayNameAsShortIdentifier(DocumentModel docModel,
			String schemaName) throws Exception {
		String shortIdentifier = (String) docModel.getProperty(schemaName,
				AuthorityItemJAXBSchema.SHORT_IDENTIFIER);

		if (Tools.isEmpty(shortIdentifier)) {
			String termDisplayName = getPrimaryDisplayName(
					docModel, authorityItemCommonSchemaName,
					getItemTermInfoGroupXPathBase(),
					AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);

			String termName = getPrimaryDisplayName(
					docModel, authorityItemCommonSchemaName,
					getItemTermInfoGroupXPathBase(),
					AuthorityItemJAXBSchema.TERM_NAME);

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
    			repoSession = repoClient.getRepositorySession(ctx);
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
                        myFilter, true /*computeTotal*/);
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
    			// If we got/aquired a new seesion then we're responsible for releasing it.
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
    
    protected List<String> getPartialTermDisplayNameMatches(List<String> termDisplayNameList, String partialTerm) {
    	List<String> result = new ArrayList<String>();
    	
    	for (String termDisplayName : termDisplayNameList) {
    		if (termDisplayName.toLowerCase().contains(partialTerm.toLowerCase()) == true) {
    			result.add(termDisplayName);
    		}
    	}
    	
    	return result;
    }
    
    @SuppressWarnings("unchecked")
	private List<String> getPartialTermDisplayNameMatches(DocumentModel docModel, // REM - CSPACE-5133
			String schema, ListResultField field, String partialTerm) {
    	List<String> result = null;
    	  
    	String xpath = field.getXpath(); // results in something like "persons_common:personTermGroupList/[0]/termDisplayName"
    	int endOfTermGroup = xpath.lastIndexOf("/[0]/");
    	String propertyName = endOfTermGroup != -1 ? xpath.substring(0, endOfTermGroup) : xpath; // it may not be multivalued so the xpath passed in would be the property name
    	Object value = null;
    	
		try {
			value = docModel.getProperty(schema, propertyName);
		} catch (Exception e) {
			logger.error("Could not extract term display name with property = "
					+ propertyName, e);
		}
		
		if (value != null && value instanceof ArrayList) {
			ArrayList<HashMap<String, Object>> termGroupList = (ArrayList<HashMap<String, Object>>)value;
			int arrayListSize = termGroupList.size();
			if (arrayListSize > 1) { // if there's only 1 element in the list then we've already matched the primary term's display name
				List<String> displayNameList = new ArrayList<String>();
				for (int i = 1; i < arrayListSize; i++) { // start at 1, skip the primary term's displayName since we will always return it
					HashMap<String, Object> map = (HashMap<String, Object>)termGroupList.get(i);
					String termDisplayName = (String) map.get(AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
					displayNameList.add(i - 1, termDisplayName);
				}
				
				result = getPartialTermDisplayNameMatches(displayNameList, partialTerm);
			}
		}

    	return result;
    }

    @Override
	protected Object getListResultValue(DocumentModel docModel, // REM - CSPACE-5133
			String schema, ListResultField field) {
		Object result = null;		

		result = NuxeoUtils.getXPathValue(docModel, schema, field.getXpath());
		String elName = field.getElement();
		//
		// If the list result value is the termDisplayName element, we need to check to see if a partial term query was made.
		//
		if (isTermDisplayName(elName) == true) {
			MultivaluedMap<String, String> queryParams = this.getServiceContext().getQueryParams();
	        String partialTerm = queryParams != null ? queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM) : null;
	        if (partialTerm != null && partialTerm.trim().isEmpty() == false) {
				String primaryTermDisplayName = (String)result;
	        	List<String> matches = getPartialTermDisplayNameMatches(docModel, schema, field, partialTerm);
	        	if (matches != null && matches.isEmpty() == false) {
		        	matches.add(0, primaryTermDisplayName); // insert the primary term's display name at the beginning of the list
		        	result = matches; // set the result to a list of matching term display names with the primary term's display name at the beginning
	        	}
	        }
		}
		
		return result;
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

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
    	//
    	// We currently don't override this method with any AuthorityItemDocumentModelHandler specific functionality, so
    	// we could remove this method.
    	//
        super.fillAllParts(wrapDoc, action);
    }

    protected List<RelationsCommonList.RelationListItem> cloneList(List<RelationsCommonList.RelationListItem> inboundList) {
        List<RelationsCommonList.RelationListItem> result = newRelationsCommonList();
        for (RelationsCommonList.RelationListItem item : inboundList) {
            result.add(item);
        }
        return result;
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
