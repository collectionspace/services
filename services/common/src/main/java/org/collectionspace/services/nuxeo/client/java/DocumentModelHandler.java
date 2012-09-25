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

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefName.RefNameInterface;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthRefConfigInfo;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.State;
import org.collectionspace.services.lifecycle.StateList;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.lifecycle.TransitionDefList;
import org.collectionspace.services.lifecycle.TransitionList;

import org.nuxeo.ecm.core.NXCore;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DocumentModelHandler is a base abstract Nuxeo document handler
 * using Nuxeo Java Remote APIs for CollectionSpace services
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class DocumentModelHandler<T, TL>
        extends AbstractMultipartDocumentHandlerImpl<T, TL, DocumentModel, DocumentModelList> {

    private final Logger logger = LoggerFactory.getLogger(DocumentModelHandler.class);
    private RepositoryInstance repositorySession;

    protected String oldRefNameOnUpdate = null;
    protected String newRefNameOnUpdate = null;
    
    /*
     * Map Nuxeo's life cycle object to our JAX-B based life cycle object
     */
    private Lifecycle createCollectionSpaceLifecycle(org.nuxeo.ecm.core.lifecycle.LifeCycle nuxeoLifecyle) {
    	Lifecycle result = null;
    	
    	if (nuxeoLifecyle != null) {
    		//
    		// Copy the life cycle's name
    		result = new Lifecycle();
    		result.setName(nuxeoLifecyle.getName());
    		
    		// We currently support only one initial state, so take the first one from Nuxeo
    		Collection<String> initialStateNames = nuxeoLifecyle.getInitialStateNames();
    		result.setDefaultInitial(initialStateNames.iterator().next());
    		
    		// Next, we copy the state and corresponding transition lists
    		StateList stateList = new StateList();
    		List<State> states = stateList.getState();
    		Collection<org.nuxeo.ecm.core.lifecycle.LifeCycleState> nuxeoStates = nuxeoLifecyle.getStates();
    		for (org.nuxeo.ecm.core.lifecycle.LifeCycleState nuxeoState : nuxeoStates) {
    			State tempState = new State();
    			tempState.setDescription(nuxeoState.getDescription());
    			tempState.setInitial(nuxeoState.isInitial());
    			tempState.setName(nuxeoState.getName());
    			// Now get the list of transitions
    			TransitionList transitionList = new TransitionList();
    			List<String> transitions = transitionList.getTransition();
    			Collection<String> nuxeoTransitions = nuxeoState.getAllowedStateTransitions();
    			for (String nuxeoTransition : nuxeoTransitions) {
    				transitions.add(nuxeoTransition);
    			}
    			tempState.setTransitionList(transitionList);
    			states.add(tempState);
    		}
    		result.setStateList(stateList);
    		
    		// Finally, we create the transition definitions
    		TransitionDefList transitionDefList = new TransitionDefList();
    		List<TransitionDef> transitionDefs = transitionDefList.getTransitionDef();
    		Collection<org.nuxeo.ecm.core.lifecycle.LifeCycleTransition> nuxeoTransitionDefs = nuxeoLifecyle.getTransitions();
    		for (org.nuxeo.ecm.core.lifecycle.LifeCycleTransition nuxeoTransitionDef : nuxeoTransitionDefs) {
    			TransitionDef tempTransitionDef = new TransitionDef();
    			tempTransitionDef.setDescription(nuxeoTransitionDef.getDescription());
    			tempTransitionDef.setDestinationState(nuxeoTransitionDef.getDestinationStateName());
    			tempTransitionDef.setName(nuxeoTransitionDef.getName());
    			transitionDefs.add(tempTransitionDef);
    		}
    		result.setTransitionDefList(transitionDefList);
    	}
    	
    	return result;
    }
    
    /*
     * Returns the the life cycle definition of the related Nuxeo document type for this handler.
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getLifecycle()
     */
    @Override
    public Lifecycle getLifecycle() {
    	Lifecycle result = null;
    	
    	String docTypeName = null;
    	try {
	    	docTypeName = this.getServiceContext().getDocumentType();
	    	result = getLifecycle(docTypeName);
    	} catch (Exception e) {
    		if (logger.isTraceEnabled() == true) {
    			logger.trace("Could not retrieve lifecycle definition for Nuxeo doctype: " + docTypeName);
    		}
    	}
    	
    	return result;
    }
    
    /*
     * Returns the the life cycle definition of the related Nuxeo document type for this handler.
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getLifecycle(java.lang.String)
     */
    @Override
    public Lifecycle getLifecycle(String docTypeName) {
    	org.nuxeo.ecm.core.lifecycle.LifeCycle nuxeoLifecyle;
    	Lifecycle result = null;
    	
    	try {
    		LifeCycleService lifeCycleService = null;
			try {
				lifeCycleService = NXCore.getLifeCycleService();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	    	String lifeCycleName; 
	    	lifeCycleName = lifeCycleService.getLifeCycleNameFor(docTypeName);
	    	nuxeoLifecyle = lifeCycleService.getLifeCycleByName(lifeCycleName);
	    	
	    	result = createCollectionSpaceLifecycle(nuxeoLifecyle);	
//			result = (Lifecycle)FileTools.getJaxbObjectFromFile(Lifecycle.class, "default-lifecycle.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Could not retreive life cycle information for Nuxeo doctype: " + docTypeName, e);
		}
    	
    	return result;
    }
    
    /*
     * We're using the "name" field of Nuxeo's DocumentModel to store
     * the CSID.
     */
    public String getCsid(DocumentModel docModel) {
    	return NuxeoUtils.getCsid(docModel);
    }

    public String getUri(DocumentModel docModel) {
        return getServiceContextPath()+getCsid(docModel);
    }
        
    public RepositoryClient<PoxPayloadIn, PoxPayloadOut> getRepositoryClient(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient = RepositoryClientFactory.getInstance().getClient(ctx.getRepositoryClientName());
        return repositoryClient;
    }

    /**
     * getRepositorySession returns Nuxeo Repository Session
     * @return
     */
    public RepositoryInstance getRepositorySession() {
    	
        return repositorySession;
    }

    /**
     * setRepositorySession sets repository session
     * @param repoSession
     */
    public void setRepositorySession(RepositoryInstance repoSession) {
        this.repositorySession = repoSession;
    }

    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// TODO for sub-docs - check to see if the current service context is a multipart input, 
    	// OR a docfragment, and call a variant to fill the DocModel.
        fillAllParts(wrapDoc, Action.CREATE);
        handleCoreValues(wrapDoc, Action.CREATE);
    }
    
    // TODO for sub-docs - Add completeCreate in which we look for set-aside doc fragments 
    // and create the subitems. We will create service contexts with the doc fragments
    // and then call 


    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// TODO for sub-docs - check to see if the current service context is a multipart input, 
    	// OR a docfragment, and call a variant to fill the DocModel.
        fillAllParts(wrapDoc, Action.UPDATE);
        handleCoreValues(wrapDoc, Action.UPDATE);
    }

    @Override
    public void handleGet(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        extractAllParts(wrapDoc);
    }

    @Override
    public void handleGetAll(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
    	Profiler profiler = new Profiler(this, 2);
    	profiler.start();
        setCommonPartList(extractCommonPartList(wrapDoc));
        profiler.stop();
    }

    @Override
    public abstract void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract T extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception;

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception;

    @Override
    public abstract T getCommonPart();

    @Override
    public abstract void setCommonPart(T obj);

    @Override
    public abstract TL getCommonPartList();

    @Override
    public abstract void setCommonPartList(TL obj);
    
    @Override
    public DocumentFilter createDocumentFilter() {
    	DocumentFilter filter = new NuxeoDocumentFilter(this.getServiceContext());
    	return filter;
    }
    
    /**
     * Gets the authority refs.
     *
     * @param docWrapper the doc wrapper
     * @param authRefFields the auth ref fields
     * @return the authority refs
     * @throws PropertyException the property exception
     */
    abstract public AuthorityRefList getAuthorityRefs(String csid,
    		List<AuthRefConfigInfo> authRefsInfo) throws PropertyException;    

    /*
     * Subclasses should override this method if they need to customize their refname generation
     */
    protected RefName.RefNameInterface getRefName(ServiceContext ctx,
    		DocumentModel docModel) {
    	return getRefName(new DocumentWrapperImpl<DocumentModel>(docModel), ctx.getTenantName(), ctx.getServiceName());
    }
    
    /*
     * By default, we'll use the CSID as the short ID.  Sub-classes can override this method if they want to use
     * something else for a short ID.
     * 
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getRefName(org.collectionspace.services.common.document.DocumentWrapper, java.lang.String, java.lang.String)
     */
    @Override
	protected RefName.RefNameInterface getRefName(DocumentWrapper<DocumentModel> docWrapper,
			String tenantName, String serviceName) {
    	String csid = docWrapper.getWrappedObject().getName();
    	String refnameDisplayName = getRefnameDisplayName(docWrapper);
    	RefName.RefNameInterface refname = RefName.Authority.buildAuthority(tenantName, serviceName,
        		csid, null, refnameDisplayName);
    	return refname;
	}

    private void handleCoreValues(DocumentWrapper<DocumentModel> docWrapper, 
    		Action action)  throws ClientException {
    	DocumentModel documentModel = docWrapper.getWrappedObject();
        String now = GregorianCalendarDateTimeUtils.timestampUTC();
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
    	String userId = ctx.getUserId();
    	if (action == Action.CREATE) {
            //
            // Add the tenant ID value to the new entity
            //
        	String tenantId = ctx.getTenantId();
            documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
            		CollectionSpaceClient.COLLECTIONSPACE_CORE_TENANTID, tenantId);
            //
            // Add the uri value to the new entity
            //
            documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
            		CollectionSpaceClient.COLLECTIONSPACE_CORE_URI, getUri(documentModel));
        	//
        	// Add the CSID to the DublinCore title so we can see the CSID in the default
        	// Nuxeo webapp.
        	//
        	try {
    	        documentModel.setProperty("dublincore", "title",
    	                documentModel.getName());
        	} catch (Exception x) {
        		if (logger.isWarnEnabled() == true) {
        			logger.warn("Could not set the Dublin Core 'title' field on document CSID:" +
        					documentModel.getName());
        		}
        	}
        	//
        	// Add createdAt timestamp and createdBy user
        	//
            documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
            		CollectionSpaceClient.COLLECTIONSPACE_CORE_CREATED_AT, now);
            documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
            		CollectionSpaceClient.COLLECTIONSPACE_CORE_CREATED_BY, userId);
    	}
    	
		if (action == Action.CREATE || action == Action.UPDATE) {
            //
            // Add/update the resource's refname
            //
			handleRefNameChanges(ctx, documentModel);
            //
            // Add updatedAt timestamp and updateBy user
            //
			documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
					CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_AT, now);
			documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
					CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_BY, userId);
		}		
    }
    
    protected boolean hasRefNameUpdate() {
    	boolean result = false;
    	
    	if (Tools.notBlank(newRefNameOnUpdate) && Tools.notBlank(oldRefNameOnUpdate)) {
    		if (newRefNameOnUpdate.equalsIgnoreCase(oldRefNameOnUpdate) == false) {
    			result = true; // refNames are different so updates are needed
    		}
    	}
    	
    	return result;
    }
    
    private void handleRefNameChanges(ServiceContext ctx, DocumentModel docModel) throws ClientException {
    	// First get the old refName
    	this.oldRefNameOnUpdate = (String)docModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
            		CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
    	// Next, get the new refName
        RefNameInterface refName = getRefName(ctx, docModel); // Sub-classes may override the getRefName() method called here.
        if (refName != null) {
        	this.newRefNameOnUpdate = refName.toString();
        } else {
        	logger.error(String.format("refName for document is missing.  Document CSID=%s", docModel.getName()));
        }
        //
        // Set the refName if it is an update or if the old refName was empty or null
        //
        if (hasRefNameUpdate() == true || this.oldRefNameOnUpdate == null) {
	        docModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
	        		CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME, this.newRefNameOnUpdate);
        }
    }
        
    /*
     * If we see the "rtSbj" query param then we need to perform a CMIS query.  Currently, we have only one
     * CMIS query, but we could add more.  If we do, this method should look at the incoming request and corresponding
     * query params to determine if we need to do a CMIS query
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#isCMISQuery()
     */
    public boolean isCMISQuery() {
    	boolean result = false;
    	
    	MultivaluedMap<String, String> queryParams = getServiceContext().getQueryParams();
    	//
    	// Look the query params to see if we need to make a CMSIS query.
    	//
    	String asSubjectCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT);    	
    	String asOjectCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_OBJECT);    	
    	String asEither = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_EITHER);    	
    	if (asSubjectCsid != null || asOjectCsid != null || asEither != null) {
    		result = true;
    	}
    	
    	return result;
    }
    
	/**
	 * Creates the CMIS query from the service context. Each document handler is
	 * responsible for returning (can override) a valid CMIS query using the information in the
	 * current service context -which includes things like the query parameters,
	 * etc.
	 * 
	 * This method implementation supports three mutually exclusive cases. We will build a query
	 * that can find a document(s) 'A' in a relationship with another document
	 * 'B' where document 'B' has a CSID equal to the query param passed in and:
	 * 		1. Document 'B' is the subject of the relationship
	 * 		2. Document 'B' is the object of the relationship
	 * 		3. Document 'B' is either the object or the subject of the relationship
	 */
    @Override
    public String getCMISQuery(QueryContext queryContext) {
    	String result = null;
    	
    	if (isCMISQuery() == true) {
	    	//
	    	// Build up the query arguments
	    	//
	    	String theOnClause = "";
	    	String theWhereClause = "";
	    	MultivaluedMap<String, String> queryParams = getServiceContext().getQueryParams();
	    	String asSubjectCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT);
	    	String asObjectCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_OBJECT);
	    	String asEitherCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_EITHER);
	    	String matchObjDocTypes = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_MATCH_OBJ_DOCTYPES);
	    	String selectDocType = (String)queryParams.getFirst(IQueryManager.SELECT_DOC_TYPE_FIELD);

	    	String docType = this.getServiceContext().getDocumentType();
	    	if (selectDocType != null && !selectDocType.isEmpty()) {  
	    		docType = selectDocType;
	    	}
	    	String selectFields = IQueryManager.CMIS_TARGET_CSID + ", "
	    			+ IQueryManager.CMIS_TARGET_TITLE + ", "
	    			+ IRelationsManager.CMIS_CSPACE_RELATIONS_TITLE + ", "
	    			+ IRelationsManager.CMIS_CSPACE_RELATIONS_OBJECT_ID + ", "
	    			+ IRelationsManager.CMIS_CSPACE_RELATIONS_SUBJECT_ID;
	    	String targetTable = docType + " " + IQueryManager.CMIS_TARGET_PREFIX;
	    	String relTable = IRelationsManager.DOC_TYPE + " " + IQueryManager.CMIS_RELATIONS_PREFIX;
	    	String relObjectCsidCol = IRelationsManager.CMIS_CSPACE_RELATIONS_OBJECT_ID;
	    	String relSubjectCsidCol = IRelationsManager.CMIS_CSPACE_RELATIONS_SUBJECT_ID;
	    	String targetCsidCol = IQueryManager.CMIS_TARGET_CSID;

	    	//
	    	// Create the "ON" and "WHERE" query clauses based on the params passed into the HTTP request.  
	    	//
	    	// First come, first serve -the first match determines the "ON" and "WHERE" query clauses.
	    	//
	    	if (asSubjectCsid != null && !asSubjectCsid.isEmpty()) {  
	    		// Since our query param is the "subject" value, join the tables where the CSID of the document is the other side (the "object") of the relationship.
	    		theOnClause = relObjectCsidCol + " = " + targetCsidCol;
	    		theWhereClause = relSubjectCsidCol + " = " + "'" + asSubjectCsid + "'";
	    	} else if (asObjectCsid != null && !asObjectCsid.isEmpty()) {
	    		// Since our query param is the "object" value, join the tables where the CSID of the document is the other side (the "subject") of the relationship.
	    		theOnClause = relSubjectCsidCol + " = " + targetCsidCol; 
	    		theWhereClause = relObjectCsidCol + " = " + "'" + asObjectCsid + "'";
	    	} else if (asEitherCsid != null && !asEitherCsid.isEmpty()) {
	    		theOnClause = relObjectCsidCol + " = " + targetCsidCol
	    				+ " OR " + relSubjectCsidCol + " = " + targetCsidCol;
	    		theWhereClause = relSubjectCsidCol + " = " + "'" + asEitherCsid + "'"
	    				+ " OR " + relObjectCsidCol + " = " + "'" + asEitherCsid + "'";
	    	} else {
	    		//Since the call to isCMISQuery() return true, we should never get here.
	    		logger.error("Attempt to make CMIS query failed because the HTTP request was missing valid query parameters.");
	    	}
	    	
	    	// Now consider a constraint on the object doc types (for search by service group)
	    	if (matchObjDocTypes != null && !matchObjDocTypes.isEmpty()) {  
	    		// Since our query param is the "subject" value, join the tables where the CSID of the document is the other side (the "object") of the relationship.
	    		theWhereClause += " AND (" + IRelationsManager.CMIS_CSPACE_RELATIONS_OBJECT_TYPE 
	    							+ " IN " + matchObjDocTypes + ")";
	    	}
	    	
	    	StringBuilder query = new StringBuilder();
	    	// assemble the query from the string arguments
	    	query.append("SELECT ");
	    	query.append(selectFields);
	    	query.append(" FROM " + targetTable + " JOIN " + relTable);
	    	query.append(" ON " + theOnClause);
	    	query.append(" WHERE " + theWhereClause);
	    	
	        try {
				NuxeoUtils.appendCMISOrderBy(query, queryContext);
			} catch (Exception e) {
				logger.error("Could not append ORDER BY clause to CMIS query", e);
			}
	        
	    	// An example:
	        // SELECT D.cmis:name, D.dc:title, R.dc:title, R.relations_common:subjectCsid
	        // FROM Dimension D JOIN Relation R
	        // ON R.relations_common:objectCsid = D.cmis:name
	        // WHERE R.relations_common:subjectCsid = '737527ec-a560-4776-99de'
	        // ORDER BY D.collectionspace_core:updatedAt DESC
	        
	        result = query.toString();
	        if (logger.isDebugEnabled() == true && result != null) {
	        	logger.debug("The CMIS query is: " + result);
	        }
    	}
        
        return result;
    }
    
}
