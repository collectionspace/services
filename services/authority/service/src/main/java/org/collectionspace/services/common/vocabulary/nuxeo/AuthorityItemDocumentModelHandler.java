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

import org.collectionspace.services.common.UriTemplateRegistry;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
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

    @Override
    protected String getRefnameDisplayName(DocumentWrapper<DocumentModel> docWrapper) {
    	String result = null;
    	
    	DocumentModel docModel = docWrapper.getWrappedObject();
    	ServiceContext ctx = this.getServiceContext();
    	RefName.AuthorityItem refname = (RefName.AuthorityItem)getRefName(ctx, docModel);
    	result = refname.getDisplayName();
    	
    	return result;
    }
    
    /*
     * After calling this method successfully, the document model will contain an updated refname and short ID
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getRefName(org.collectionspace.services.common.context.ServiceContext, org.nuxeo.ecm.core.api.DocumentModel)
     */
    @Override
    public RefName.RefNameInterface getRefName(ServiceContext ctx,
    		DocumentModel docModel) {
    	RefName.RefNameInterface refname = null;
    	
    	try {
	        String displayName = getPrimaryDisplayName(docModel, authorityItemCommonSchemaName,
                    getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
	        if (Tools.isEmpty(displayName)) {
	            throw new Exception("The displayName for this authority term was empty or not set.");
	        }
        
	        String shortIdentifier = (String) docModel.getProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
	        if (Tools.isEmpty(shortIdentifier)) {
	        	// We didn't find a short ID in the payload request, so we need to synthesize one.
	        	shortIdentifier = handleDisplayNameAsShortIdentifier(docModel); // updates the document model with the new short ID as a side-effect
	        }
	        
	        String authorityRefBaseName = getAuthorityRefNameBase();
	        if (Tools.isEmpty(authorityRefBaseName)) {
	            throw new Exception("Could not create the refName for this authority term, because the refName for its authority parent was empty.");
	        }
	        
	        // Create the items refname using the parent's as a base
	        RefName.Authority parentsRefName = RefName.Authority.parse(authorityRefBaseName);
	        refname = RefName.buildAuthorityItem(parentsRefName, shortIdentifier, displayName);
	        // Now update the document model with the refname value
	        String refNameStr = refname.toString();
	        docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REF_NAME, refNameStr); // REM - This field is deprecated now that the refName is part of the collection_space core schema

    	} catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}

        return refname;
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
        	        boolean hasTermStatus = false;
        	        for (int i = 0; i < nFields; i++) {
        	            ListResultField field = list.get(i);
        	            String elName = field.getElement();
        	            if (isTermDisplayName(elName) == true) {
        	                hasDisplayName = true;
        	            } else if (AuthorityItemJAXBSchema.SHORT_IDENTIFIER.equals(elName)) {
        	                hasShortId = true;
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
        // first fill all the parts of the document, refname and short ID get set as well
        super.handleCreate(wrapDoc);
        // Ensure we have required fields set properly
        handleInAuthority(wrapDoc.getWrappedObject());        
    }

    /*
     * This method gets called after the primary update to an authority item has happened.  If the authority item's refName
     * has changed, then we need to updated all the records that use that refname with the new/updated version
     * 
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// Must call our super class' version first
    	super.completeUpdate(wrapDoc);
    	
    	//
    	// Look for and update authority references with the updated refName
    	//
        if (hasRefNameUpdate() == true) {
            // We have work to do.
            if (logger.isDebugEnabled()) {
                final String EOL = System.getProperty("line.separator");
                logger.debug("Need to find and update references to authority item." + EOL
                        + "   Old refName" + oldRefNameOnUpdate + EOL
                        + "   New refName" + newRefNameOnUpdate);
            }
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = getRepositoryClient(ctx);
            RepositoryInstance repoSession = this.getRepositorySession();
            
            // Update all the existing records that have a field with the old refName in it
            int nUpdated = RefNameServiceUtils.updateAuthorityRefDocs(ctx, repoClient, repoSession,
                    oldRefNameOnUpdate, newRefNameOnUpdate, getRefPropName());
            
            // Finished so log a message.
            if (logger.isDebugEnabled()) {
                logger.debug("Updated " + nUpdated + " instances of oldRefName to newRefName");
            }
        }
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
    // FIXME: Once we remove the refName field from the authority item schemas, we can remove this override method since our super does everthing for us now.
    @Deprecated
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// Must call our super's version first, this updates the core schema and the relationship records to deal with possible refName changes/update
        super.handleUpdate(wrapDoc);
        if (this.hasRefNameUpdate() == true) {
        	DocumentModel docModel = wrapDoc.getWrappedObject();
            docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REF_NAME, this.newRefNameOnUpdate); // This field is deprecated since it is now a duplicate of what is in the collectionspace_core:refName field        	
        }
    }
    
    /**
     * If no short identifier was provided in the input payload, generate a
     * short identifier from the preferred term display name or term name.
     */
	private String handleDisplayNameAsShortIdentifier(DocumentModel docModel) throws Exception {
		String result = (String) docModel.getProperty(authorityItemCommonSchemaName,
				AuthorityItemJAXBSchema.SHORT_IDENTIFIER);

		if (Tools.isEmpty(result)) {
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
			docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER,
					generatedShortIdentifier);
			result = generatedShortIdentifier;
		}
		
		return result;
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
    protected String updateRefnameForAuthorityItem(DocumentModel docModel,
            String schemaName) throws Exception {
    	String result = null;
    	
        RefName.RefNameInterface refname = getRefName(getServiceContext(), docModel);
        String refNameStr = refname.toString();
        docModel.setProperty(schemaName, AuthorityItemJAXBSchema.REF_NAME, refNameStr);
        result = refNameStr;
        
        return result;
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
                UriTemplateRegistry uriTemplateRegistry, 
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
                		repoSession, ctx, uriTemplateRegistry, repoClient,
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
