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

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefName.RefNameInterface;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthRefConfigInfo;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
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
import org.nuxeo.ecm.core.lifecycle.LifeCycle;
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
    private CoreSessionInterface repositorySession;

    protected String oldRefNameOnUpdate = null;  // FIXME: REM - We should have setters and getters for these
    protected String newRefNameOnUpdate = null;  // FIXME: two fields.
    
    
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
	    	docTypeName = this.getServiceContext().getTenantQualifiedDoctype();
	    	result = getLifecycle(docTypeName);
	    	if (result == null) {
	    	    //
	    	    // Get the lifecycle of the generic type if one for the tenant qualified type doesn't exist
	    	    //
            docTypeName = this.getServiceContext().getDocumentType();
            result = getLifecycle(docTypeName);
	    	}
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
    	return NuxeoUtils.getLifecycle(docTypeName);
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
    
    public String getUri(Specifier specifier) {
        return getServiceContextPath() + specifier.value;
    }
    
        
    public RepositoryClient<PoxPayloadIn, PoxPayloadOut> getRepositoryClient(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient = 
        		(RepositoryClient<PoxPayloadIn, PoxPayloadOut>) RepositoryClientFactory.getInstance().getClient(ctx.getRepositoryClientName());
        return repositoryClient;
    }

    /**
     * getRepositorySession returns Nuxeo Repository Session
     * @return
     */
    public CoreSessionInterface getRepositorySession() {
    	
        return repositorySession;
    }

    /**
     * setRepositorySession sets repository session
     * @param repoSession
     */
    public void setRepositorySession(CoreSessionInterface repoSession) {
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
    		List<AuthRefConfigInfo> authRefConfigInfoList) throws PropertyException, Exception;    

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
    	        documentModel.setProperty(CommonAPI.NUXEO_DUBLINCORE_SCHEMANAME, CommonAPI.NUXEO_DUBLINCORE_TITLE,
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
			if (ctx.shouldUpdateCoreValues() == true) { // Ensure that our caller wants us to record this update
				documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
						CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_AT, now);
				documentModel.setProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
						CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_BY, userId);
			} else {
				logger.debug(String.format("Document with CSID=%s updated %s by user %s", documentModel.getName(), now, userId));
			}
		}		
    }
    
    protected boolean hasRefNameUpdate() {
    	boolean result = false;
    	
    	//
    	// Check to see if the request contains a query parameter asking us to force a refname update
    	//
    	if (getServiceContext().shouldForceUpdateRefnameReferences() == true) {
    		return true;
    	}
    	
    	if (Tools.notBlank(newRefNameOnUpdate) && Tools.notBlank(oldRefNameOnUpdate)) {
    		// CSPACE-6372: refNames are different if:
    		//   - any part of the refName is different, using a case insensitive comparison, or
    		//   - the display name portions are different, using a case sensitive comparison
    		if (newRefNameOnUpdate.equalsIgnoreCase(oldRefNameOnUpdate) == false) {
    			result = true; // refNames are different so updates are needed
    		}
    		else {
    			String newDisplayNameOnUpdate = RefNameUtils.getDisplayName(newRefNameOnUpdate);
    			String oldDisplayNameOnUpdate = RefNameUtils.getDisplayName(oldRefNameOnUpdate);
    			
    			if (StringUtils.equals(newDisplayNameOnUpdate, oldDisplayNameOnUpdate) == false) {
    				result = true; // display names are different so updates are needed
    			}
    		}
    	}
    	
    	return result;
    }
    
    protected void handleRefNameChanges(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, DocumentModel docModel) throws ClientException {
    	// First get the old refName
    	this.oldRefNameOnUpdate = (String)docModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
            		CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
    	// Next, get the new refName
        RefNameInterface refName = getRefName(ctx, docModel); // Sub-classes may override the getRefName() method called here.
        if (refName != null) {
        	this.newRefNameOnUpdate = refName.toString();
        } else {
        	logger.error(String.format("The refName for document is missing.  Document CSID=%s", docModel.getName())); // FIXME: REM - We should probably be throwing an exception here?
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
    
    @Override
    public String getDocumentsToIndexQuery(String indexId, String csid) throws DocumentException, Exception {
    	String result = null;
    	
    	ServiceContext<PoxPayloadIn,PoxPayloadOut> ctx = this.getServiceContext();
    	String selectClause = "SELECT ecm:uuid, ecm:primaryType FROM ";
    	String docFilterWhereClause = this.getDocumentFilter().getWhereClause();
    	//
    	// The where clause could be a combination of the document filter's where clause plus a CSID qualifier
    	//
    	String whereClause = (csid == null) ? null : String.format("ecm:name = '%s'", csid); // AND ecm:currentLifeCycleState <> 'deleted'"
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            // Due to an apparent bug/issue in how Nuxeo translates the NXQL query string
            // into SQL, we need to parenthesize our 'where' clause
        	if (docFilterWhereClause != null && !docFilterWhereClause.trim().isEmpty()) {
        		whereClause = whereClause + IQueryManager.SEARCH_QUALIFIER_AND + "(" + docFilterWhereClause + ")";
        	}
        } else {
        	whereClause = docFilterWhereClause;
        }
    	String orderByClause = "ecm:uuid";
    	
    	try {
    		QueryContext queryContext = new QueryContext(ctx, selectClause, whereClause, orderByClause);
    		result = NuxeoUtils.buildNXQLQuery(queryContext);
    	} catch (DocumentException de) {
    		throw de;
    	} catch (Exception x) {
    		throw x;
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
	 * @throws DocumentException 
	 */
    @Override
    public String getCMISQuery(QueryContext queryContext) throws DocumentException {
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
	    	
	    	String matchObjDocTypes = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_MATCH_OBJ_DOCTYPES);
	    	String selectDocType = (String)queryParams.getFirst(IQueryManager.SELECT_DOC_TYPE_FIELD);

	    	String docType = NuxeoUtils.getTenantQualifiedDocType(this.getServiceContext()); // Fixed for https://issues.collectionspace.org/browse/DRYD-302
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
	    	
	    	String relSubjectCsidCol = IRelationsManager.CMIS_CSPACE_RELATIONS_SUBJECT_ID;
	    	String relObjectCsidCol = IRelationsManager.CMIS_CSPACE_RELATIONS_OBJECT_ID;
	    	
	    	String targetCsidCol = IQueryManager.CMIS_TARGET_CSID;
	    	String tenantID = this.getServiceContext().getTenantId();

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
	    	
	    	// Qualify the search for predicate types
	    	theWhereClause = addWhereClauseForPredicates(theWhereClause, queryParams);
	    	
	    	// Qualify the query with the current tenant ID.
    		theWhereClause += IQueryManager.SEARCH_QUALIFIER_AND + IQueryManager.CMIS_JOIN_TENANT_ID_FILTER + " = '" + tenantID + "'";
    		
	    	// This could later be in control of a queryParam, to omit if we want to see versions, or to
	    	// only see old versions.
    		theWhereClause += IQueryManager.SEARCH_QUALIFIER_AND + IQueryManager.CMIS_JOIN_NUXEO_IS_VERSION_FILTER;
	    	
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

	private String addWhereClauseForPredicates(String theWhereClause, MultivaluedMap<String, String> queryParams) {
		if (queryParams.containsKey(IQueryManager.SEARCH_RELATED_PREDICATE)) {
			List<String> predicateList = queryParams.get(IQueryManager.SEARCH_RELATED_PREDICATE);
			
			if (predicateList.size() == 1) {
		    	String predicate = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_PREDICATE);
		    	if (predicate != null && !predicate.trim().isEmpty()) {
		    		theWhereClause += IQueryManager.SEARCH_QUALIFIER_AND + IRelationsManager.CMIS_CSPACE_RELATIONS_PREDICATE + " = '" + predicate + "'";
		    	}
			} else if (predicateList.size() > 1) {
				StringBuffer partialClause = new StringBuffer();
				for (String predicate : predicateList) {
					if (!predicate.trim().isEmpty()) {
						partialClause.append("'" + predicate + "', ");
					}
				}
				String inValues = partialClause.toString().replaceAll(", $", ""); // remove the last ', ' squence
				if (!inValues.trim().isEmpty()) {
					theWhereClause += IQueryManager.SEARCH_QUALIFIER_AND + IRelationsManager.CMIS_CSPACE_RELATIONS_PREDICATE + " IN (" + inValues + ")";
				}
			}
		}
		
		return theWhereClause;
	}
    
}
