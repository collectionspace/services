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
 */
package org.collectionspace.services.nuxeo.client.java;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.profile.Profiler;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepositoryJavaClient is used to perform CRUD operations on documents in Nuxeo
 * repository using Remote Java APIs. It uses @see DocumentHandler as IOHandler
 * with the client.
 * 
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class RepositoryJavaClientImpl implements RepositoryClient<PoxPayloadIn, PoxPayloadOut> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RepositoryJavaClientImpl.class);
//    private final Logger profilerLogger = LoggerFactory.getLogger("remperf");
//    private String foo = Profiler.createLogger();

    public static final String NUXEO_CORE_TYPE_DOMAIN = "Domain";
    public static final String NUXEO_CORE_TYPE_WORKSPACEROOT = "WorkspaceRoot";
    
    /**
     * Instantiates a new repository java client impl.
     */
    public RepositoryJavaClientImpl() {
        //Empty constructor
    	
    }

    public void assertWorkflowState(ServiceContext ctx,
    		DocumentModel docModel) throws DocumentNotFoundException, ClientException {
    	MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
    	if (queryParams != null) {
	    	//
	    	// Look for the workflow "delete" query param and see if we need to assert that the
	    	// docModel is in a non-deleted workflow state.
	    	//
    		String currentState = docModel.getCurrentLifeCycleState();
	        String includeDeletedStr = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_NONDELETED);
	        boolean includeDeleted = includeDeletedStr == null ? true : Boolean.parseBoolean(includeDeletedStr);
	    	if (includeDeleted == false) {
	    		//
	    		// We don't wanted soft-deleted object, so throw an exception if this one is soft-deleted.
	    		//
	    		if (currentState.equalsIgnoreCase(WorkflowClient.WORKFLOWSTATE_DELETED)) {
	    			String msg = "The GET assertion that docModel not be in 'deleted' workflow state failed.";
	    			logger.debug(msg);
	    			throw new DocumentNotFoundException(msg);
	    		}
	    	}
    	}
    }
    
    /**
     * create document in the Nuxeo repository
     *
     * @param ctx service context under which this method is invoked
     * @param handler
     *            should be used by the caller to provide and transform the
     *            document
     * @return id in repository of the newly created document
     * @throws DocumentException
     */
    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {

    	String docType = NuxeoUtils.getTenantQualifiedDocType(ctx); //ctx.getDocumentType();
        if (docType == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.create: docType is missing");
        }
    	
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.create: handler is missing");
        }
        String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
        if (nuxeoWspaceId == null) {
            throw new DocumentNotFoundException(
                    "Unable to find workspace for service " + ctx.getServiceName()
                    + " check if the workspace exists in the Nuxeo repository");
        }
        
        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.CREATE);
            repoSession = getRepositorySession();
            DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
            DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
            String wspacePath = wspaceDoc.getPathAsString();
            //give our own ID so PathRef could be constructed later on
            String id = IdUtils.generateId(UUID.randomUUID().toString());
            // create document model
            DocumentModel doc = repoSession.createDocumentModel(wspacePath, id, docType);
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModel> wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
            handler.handle(Action.CREATE, wrapDoc);
            // create document with documentmodel
            doc = repoSession.createDocument(doc);
            repoSession.save();
// TODO for sub-docs need to call into the handler to let it deal with subitems. Pass in the id,
// and assume the handler has the state it needs (doc fragments). 
            handler.complete(Action.CREATE, wrapDoc);
            return id;
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception e) {
                logger.error("Caught exception ", e);
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }

    }

    /**
     * get document from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param id
     *            of the document to retrieve
     * @param handler
     *            should be used by the caller to provide and transform the
     *            document
     * @throws DocumentException
     */
    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {

        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.get: handler is missing");
        }
        
        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET);
            repoSession = getRepositorySession();
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
            DocumentModel docModel = null;
            try {
            	docModel = repoSession.getDocument(docRef);
                assertWorkflowState(ctx, docModel);
            } catch (ClientException ce) {
                String msg = logException(ce, "Could not find document with CSID=" + id);
                throw new DocumentNotFoundException(msg, ce);
            }
            //
            // Set repository session to handle the document
            //
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModel> wrapDoc = new DocumentWrapperImpl<DocumentModel>(docModel);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }

    /**
     * get document from the Nuxeo repository, using the docFilter params.
     * @param ctx service context under which this method is invoked
     * @param handler
     *            should be used by the caller to provide and transform the
     *            document. Handler must have a docFilter set to return a single item.
     * @throws DocumentException
     */
    @Override
    public void get(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        QueryContext queryContext = new QueryContext(ctx, handler);
        RepositoryInstance repoSession = null;

        try {
            handler.prepare(Action.GET);
            repoSession = getRepositorySession();

            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            String query = NuxeoUtils.buildNXQLQuery(ctx, queryContext);
            docList = repoSession.query(query, null, 1, 0, false);
            if (docList.size() != 1) {
                throw new DocumentNotFoundException("No document found matching filter params: " + query);
            }
            DocumentModel doc = docList.get(0);

            if (logger.isDebugEnabled()) {
                logger.debug("Executed NXQL query: " + query);
            }

            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModel> wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }
    
    public DocumentWrapper<DocumentModel> getDoc(
    		RepositoryInstance repoSession,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String csid) throws DocumentNotFoundException, DocumentException {
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, csid);
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (ClientException ce) {
            	String msg = logException(ce, "Could not find document with CSID=" + csid);
            	throw new DocumentNotFoundException(msg, ce);
            }
            wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
            throw de;
        }

        return wrapDoc;
    }
    
    /**
     * Get wrapped documentModel from the Nuxeo repository.  The search is restricted to the workspace
     * of the current context.
     * 
     * @param ctx service context under which this method is invoked
     * @param id
     *            of the document to retrieve
     * @throws DocumentException
     */
    @Override
    public DocumentWrapper<DocumentModel> getDoc(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String csid) throws DocumentNotFoundException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
        	// Open a new repository session
            repoSession = getRepositorySession();
            wrapDoc = getDoc(repoSession, ctx, csid);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        
        if (logger.isWarnEnabled() == true) {
        	logger.warn("Returned DocumentModel instance was created with a repository session that is now closed.");
        }
        return wrapDoc;
    }

    public DocumentWrapper<DocumentModel> findDoc(
            RepositoryInstance repoSession,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String whereClause)
            throws DocumentNotFoundException, DocumentException {
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            QueryContext queryContext = new QueryContext(ctx, whereClause);
            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            String query = NuxeoUtils.buildNXQLQuery(ctx, queryContext);
            docList = repoSession.query(query,
                    null, //Filter
                    1, //limit
                    0, //offset
                    false); //countTotal
            if (docList.size() != 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("findDoc: Query found: " + docList.size() + " items.");
                    logger.debug(" Query: " + query);
                }
                throw new DocumentNotFoundException("No document found matching filter params: " + query);
            }
            DocumentModel doc = docList.get(0);
            wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }
        
        return wrapDoc;
    }
    
    /**
     * find wrapped documentModel from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param whereClause where NXQL where clause to get the document
     * @throws DocumentException
     */
    @Override
    public DocumentWrapper<DocumentModel> findDoc(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String whereClause)
            		throws DocumentNotFoundException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            repoSession = getRepositorySession();
            wrapDoc = findDoc(repoSession, ctx, whereClause);
        } catch (Exception e) {
			throw new DocumentException("Unable to create a Nuxeo repository session.", e);
		} finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        
        if (logger.isWarnEnabled() == true) {
        	logger.warn("Returned DocumentModel instance was created with a repository session that is now closed.");
        }
        
        return wrapDoc;
    }

    /**
     * find doc and return CSID from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param whereClause where NXQL where clause to get the document
     * @throws DocumentException
     */
    @Override
    public String findDocCSID(RepositoryInstance repoSession, 
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String whereClause)
            throws DocumentNotFoundException, DocumentException {
        String csid = null;
        boolean releaseSession = false;
        try {
        	if(repoSession== null) {
        		repoSession = this.getRepositorySession();
        		releaseSession = true;
        	}
            DocumentWrapper<DocumentModel> wrapDoc = findDoc(repoSession, ctx, whereClause);
            DocumentModel docModel = wrapDoc.getWrappedObject();
            csid = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
        } catch (DocumentNotFoundException dnfe) {
            throw dnfe;
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
        	if(releaseSession && (repoSession != null)) {
        		this.releaseRepositorySession(repoSession);
        	}
        }
        return csid;
    }

    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryInstance repoSession,
            List<String> docTypes,
            String whereClause,
            int pageSize, int pageNum, boolean computeTotal)
            		throws DocumentNotFoundException, DocumentException {
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        try {
            if (docTypes == null || docTypes.size() < 1) {
                throw new DocumentNotFoundException(
                        "The findDocs() method must specify at least one DocumentType.");
            }
            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            QueryContext queryContext = new QueryContext(ctx, whereClause);
            String query = NuxeoUtils.buildNXQLQuery(docTypes, queryContext);
            if (logger.isDebugEnabled()) {
                logger.debug("findDocs() NXQL: "+query);
            }
            docList = repoSession.query(query, null, pageSize, pageNum, computeTotal);
            wrapDoc = new DocumentWrapperImpl<DocumentModelList>(docList);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }
                
        return wrapDoc;
    }
    
    /**
     * Find a list of documentModels from the Nuxeo repository
     * @param docTypes a list of DocType names to match
     * @param  whereClause where the clause to qualify on
     * @return
     */
    @Override
    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            List<String> docTypes,
            String whereClause,
            int pageSize, int pageNum, boolean computeTotal)
            throws DocumentNotFoundException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        try {
            repoSession = getRepositorySession();
            wrapDoc = findDocs(ctx, repoSession, docTypes, whereClause,
            		pageSize, pageNum, computeTotal);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        
        if (logger.isWarnEnabled() == true) {
        	logger.warn("Returned DocumentModelList instance was created with a repository session that is now closed.");
        }
        
        return wrapDoc;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#get(org.collectionspace.services.common.context.ServiceContext, java.util.List, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void get(ServiceContext ctx, List<String> csidList, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.getAll: handler is missing");
        }

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession();
            DocumentModelList docModelList = new DocumentModelListImpl();
            //FIXME: Should be using NuxeoUtils.createPathRef for security reasons
            for (String csid : csidList) {
                DocumentRef docRef = NuxeoUtils.createPathRef(ctx, csid);
                DocumentModel docModel = repoSession.getDocument(docRef);
                docModelList.add(docModel);
            }

            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModelList> wrapDoc = new DocumentWrapperImpl<DocumentModelList>(docModelList);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }

    /**
     * getAll get all documents for an entity entity service from the Nuxeo
     * repository
     *
     * @param ctx service context under which this method is invoked
     * @param handler
     *            should be used by the caller to provide and transform the
     *            document
     * @throws DocumentException
     */
    @Override
    public void getAll(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.getAll: handler is missing");
        }
        String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
        if (nuxeoWspaceId == null) {
            throw new DocumentNotFoundException(
                    "Unable to find workspace for service "
                    + ctx.getServiceName()
                    + " check if the workspace exists in the Nuxeo repository.");
        }
        
        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession();
            DocumentRef wsDocRef = new IdRef(nuxeoWspaceId);
            DocumentModelList docList = repoSession.getChildren(wsDocRef);
            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModelList> wrapDoc = new DocumentWrapperImpl<DocumentModelList>(docList);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }
    
    private boolean isClauseEmpty(String theString) {
    	boolean result = true;
    	if (theString != null && !theString.isEmpty()) {
    		result = false;
    	}
    	return result;
    }
    
    public DocumentWrapper<DocumentModel> getDocFromCsid(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		RepositoryInstance repoSession,
    		String csid)
            throws Exception {
        DocumentWrapper<DocumentModel> result = null;

        result = new DocumentWrapperImpl(NuxeoUtils.getDocFromCsid(ctx, repoSession, csid));
        
        return result;
    }    

    /*
     * A method to find a CollectionSpace document (of any type) given just a service context and
     * its CSID.  A search across *all* service workspaces (within a given tenant context) is performed to find
     * the document
     * 
     * This query searches Nuxeo's Hierarchy table where our CSIDs are stored in the "name" column.
     */
    @Override
    public DocumentWrapper<DocumentModel> getDocFromCsid(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid)
            throws Exception {
        DocumentWrapper<DocumentModel> result = null;
        RepositoryInstance repoSession = null;
        try {
        	repoSession = getRepositorySession();
        	result = getDocFromCsid(ctx, repoSession, csid);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        
        if (logger.isWarnEnabled() == true) {
        	logger.warn("Returned DocumentModel instance was created with a repository session that is now closed.");
        }
        
        return result;
    }

    /**
     * find doc and return CSID from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param whereClause where NXQL where clause to get the document
     * @throws DocumentException
     */
    @Override
    public String getDocURI(DocumentWrapper<DocumentModel> wrappedDoc) throws ClientException {
    	DocumentModel docModel = wrappedDoc.getWrappedObject();
        String uri = (String)docModel.getProperty(DocumentModelHandler.COLLECTIONSPACE_CORE_SCHEMA,
        			DocumentModelHandler.COLLECTIONSPACE_CORE_URI);
        return uri;
    }


    /**
     * getFiltered get all documents for an entity service from the Document repository,
     * given filter parameters specified by the handler. 
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the document
     * @throws DocumentNotFoundException if workspace not found
     * @throws DocumentException
     */
    @Override
    public void getFiltered(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {

        DocumentFilter filter = handler.getDocumentFilter();
        String oldOrderBy = filter.getOrderByClause();
        if (isClauseEmpty(oldOrderBy) == true){
            filter.setOrderByClause(DocumentFilter.ORDER_BY_LAST_UPDATED);  //per http://issues.collectionspace.org/browse/CSPACE-705 (Doesn't this conflict with what happens with the QueryContext instance that we create below?)
        }
        QueryContext queryContext = new QueryContext(ctx, handler);

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession();
            DocumentModelList docList = null;
            String query = NuxeoUtils.buildNXQLQuery(ctx, queryContext);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing NXQL query: " + query.toString());
            }

            // If we have limit and/or offset, then pass true to get totalSize
            // in returned DocumentModelList.
        	Profiler profiler = new Profiler(this, 2);
        	profiler.log("Executing NXQL query: " + query.toString());
        	profiler.start();
            if ((queryContext.getDocFilter().getOffset() > 0) || (queryContext.getDocFilter().getPageSize() > 0)) {
                docList = repoSession.query(query, null,
                        queryContext.getDocFilter().getPageSize(), queryContext.getDocFilter().getOffset(), true);
            } else {
                docList = repoSession.query(query);
            }
            profiler.stop();

            //set repoSession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModelList> wrapDoc = new DocumentWrapperImpl<DocumentModelList>(docList);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }

    private String logException(Exception e, String msg) {
    	String result = null;
    	
    	String exceptionMessage = e.getMessage();
    	exceptionMessage = exceptionMessage != null ? exceptionMessage : "<No details provided>";
        result = msg = msg + ". Caught exception:" + exceptionMessage;
        
        if (logger.isTraceEnabled() == true) {
        	logger.error(msg, e);
        } else {
        	logger.error(msg);
        }
    	
    	return result;
    }
    
    /**
     * update given document in the Nuxeo repository
     *
     * @param ctx service context under which this method is invoked
     * @param id
     *            of the document
     * @param handler
     *            should be used by the caller to provide and transform the
     *            document
     * @throws DocumentException
     */
    @Override
    public void update(ServiceContext ctx, String csid, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.update: document handler is missing.");
        }
        
        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.UPDATE);
            repoSession = getRepositorySession();
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, csid);
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (ClientException ce) {
            	String msg = logException(ce, "Could not find document to update with CSID=" + csid);
                throw new DocumentNotFoundException(msg, ce);
            }
            //
            // Set reposession to handle the document
            //
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModel> wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
            handler.handle(Action.UPDATE, wrapDoc);
            repoSession.saveDocument(doc);
            repoSession.save();
            handler.complete(Action.UPDATE, wrapDoc);
        } catch (BadRequestException bre) {
            throw bre;
        } catch (DocumentException de) {
            throw de;
        } catch (WebApplicationException wae){
            throw wae;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }
    
    /**
     * Save a documentModel to the Nuxeo repository.
     * @param ctx service context under which this method is invoked
     * @param docModel the document to save
     * @param fSaveSession if TRUE, will call CoreSession.save() to save accumulated changes.
     * @throws DocumentException
     */
    public void saveDocWithoutHandlerProcessing(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryInstance repoSession,
            DocumentModel docModel,
            boolean fSaveSession)
            throws ClientException, DocumentException {

        try {
            repoSession.saveDocument(docModel);
            if (fSaveSession) {
            	repoSession.save();
            }
        } catch (ClientException ce) {
            throw ce;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }
    }


    /**
     * Save a list of documentModels to the Nuxeo repository.
     * 
     * @param ctx service context under which this method is invoked
     * @param docModel the document to save
     * @param fSaveSession if TRUE, will call CoreSession.save() to save accumulated changes.
     * @throws DocumentException
     */
    public void saveDocListWithoutHandlerProcessing(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryInstance repoSession,
            DocumentModelList docList, 
            boolean fSaveSession)
            throws ClientException, DocumentException {
        try {
            repoSession = getRepositorySession();
            DocumentModel[] docModelArray = new DocumentModel[docList.size()];
            repoSession.saveDocuments(docList.toArray(docModelArray));
            if (fSaveSession) {
            	repoSession.save();
            }
        } catch (ClientException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Caught exception ", e);
            throw new DocumentException(e);
        }
    }

    /**
     * delete a document from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param id
     *            of the document
     * @throws DocumentException
     */
    @Override
    public void delete(ServiceContext ctx, String id) throws DocumentNotFoundException,
            DocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("Deleting document with CSID=" + id);
        }
        RepositoryInstance repoSession = null;
        try {
            repoSession = getRepositorySession();
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
            try {
                repoSession.removeDocument(docRef);
            } catch (ClientException ce) {
            	String msg = logException(ce, "Could not find document to delete with CSID=" + id);
                throw new DocumentNotFoundException(msg, ce);
            }
            repoSession.save();
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#delete(org.collectionspace.services.common.context.ServiceContext, java.lang.String, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void delete(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Hashtable<String, String> retrieveWorkspaceIds(String domainName) throws Exception {
        return NuxeoConnectorEmbedded.getInstance().retrieveWorkspaceIds(domainName);
    }

    @Override
    public String createDomain(String domainName) throws Exception {
        RepositoryInstance repoSession = null;
        String domainId = null;
        try {
        	//
        	// First create the top-level domain directory
        	//
            repoSession = getRepositorySession();
            DocumentRef parentDocRef = new PathRef("/");
            DocumentModel parentDoc = repoSession.getDocument(parentDocRef);
            DocumentModel domainDoc = repoSession.createDocumentModel(parentDoc.getPathAsString(),
                    domainName, NUXEO_CORE_TYPE_DOMAIN);
            domainDoc.setPropertyValue("dc:title", domainName);
            domainDoc.setPropertyValue("dc:description", "A CollectionSpace domain "
                    + domainName);
            domainDoc = repoSession.createDocument(domainDoc);
            domainId = domainDoc.getId();
            repoSession.save();
            //
            // Next, create a "Workspaces" root directory to contain the workspace folders for the individual service documents
            //
            DocumentModel workspacesRoot = repoSession.createDocumentModel(domainDoc.getPathAsString(),
            		NuxeoUtils.Workspaces, NUXEO_CORE_TYPE_WORKSPACEROOT);
            workspacesRoot.setPropertyValue("dc:title", NuxeoUtils.Workspaces);
            workspacesRoot.setPropertyValue("dc:description", "A CollectionSpace workspaces directory for "
                    + domainDoc.getPathAsString());
            workspacesRoot = repoSession.createDocument(workspacesRoot);
            String workspacesRootId = workspacesRoot.getId();
            repoSession.save();
            
            if (logger.isDebugEnabled()) {
                logger.debug("Created tenant domain name=" + domainName
                        + " id=" + domainId + " " +
                        NuxeoUtils.Workspaces + " id=" + workspacesRootId);
                logger.debug("Path to Domain: "+domainDoc.getPathAsString());
                logger.debug("Path to Workspaces root: "+workspacesRoot.getPathAsString());
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not create tenant domain name=" + domainName + " caught exception ", e);
            }
            throw e;
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        
        return domainId;
    }

    @Override
    public String getDomainId(String domainName) throws Exception {
        String domainId = null;
        RepositoryInstance repoSession = null;
        
        if (domainName != null && !domainName.isEmpty()) {
	        try {
	            repoSession = getRepositorySession();
	            DocumentRef docRef = new PathRef(
	                    "/" + domainName);
	            DocumentModel domain = repoSession.getDocument(docRef);
	            domainId = domain.getId();
	        } catch (Exception e) {
	            if (logger.isTraceEnabled()) {
	                logger.trace("Caught exception ", e);
	            }
	            //there is no way to identify if document does not exist due to
	            //lack of typed exception for getDocument method
	            return null;
	        } finally {
	            if (repoSession != null) {
	                releaseRepositorySession(repoSession);
	            }
	        }
        }
        
        return domainId;
    }

    /*
	 * Returns the workspaces root directory for a given domain.
	 */
	private DocumentModel getWorkspacesRoot(RepositoryInstance repoSession,
			String domainName) throws Exception {
		DocumentModel result = null;
		
		String domainPath = "/" + domainName;
		DocumentRef parentDocRef = new PathRef(domainPath);
		DocumentModelList domainChildrenList = repoSession.getChildren(
				parentDocRef);
		Iterator<DocumentModel> witer = domainChildrenList.iterator();
		while (witer.hasNext()) {
			DocumentModel childNode = witer.next();
			if (NuxeoUtils.Workspaces.equalsIgnoreCase(childNode.getName())) {
				result = childNode;
				logger.trace("Found workspaces directory at: " + result.getPathAsString());
				break;
			}
		}
		
		if (result == null) {
			throw new ClientException("Could not find workspace root directory in: "
					+ domainPath);
		}

		return result;
	}
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.repository.RepositoryClient#createWorkspace(java.lang.String, java.lang.String)
     */
    @Override
    public String createWorkspace(String domainName, String workspaceName) throws Exception {
        RepositoryInstance repoSession = null;
        String workspaceId = null;
        try {
            repoSession = getRepositorySession();
            DocumentModel parentDoc = getWorkspacesRoot(repoSession, domainName);            
            DocumentModel doc = repoSession.createDocumentModel(parentDoc.getPathAsString(),
                    workspaceName, NuxeoUtils.WORKSPACE_DOCUMENT_TYPE);
            doc.setPropertyValue("dc:title", workspaceName);
            doc.setPropertyValue("dc:description", "A CollectionSpace workspace for "
                    + workspaceName);
            doc = repoSession.createDocument(doc);
            workspaceId = doc.getId();
            repoSession.save();
            if (logger.isDebugEnabled()) {
                logger.debug("Created workspace name=" + workspaceName
                        + " id=" + workspaceId);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("createWorkspace caught exception ", e);
            }
            throw e;
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        return workspaceId;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.repository.RepositoryClient#getWorkspaceId(java.lang.String, java.lang.String)
     */
    @Override
    public String getWorkspaceId(String tenantDomain, String workspaceName) throws Exception {
        String workspaceId = null;
        
        RepositoryInstance repoSession = null;
        try {
            repoSession = getRepositorySession();
            DocumentRef docRef = new PathRef(
                    "/" + tenantDomain
                    + "/" + NuxeoUtils.Workspaces
                    + "/" + workspaceName);
            DocumentModel workspace = repoSession.getDocument(docRef);
            workspaceId = workspace.getId();
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        
        return workspaceId;
    }


    /**
     * Gets the repository session. - Package access only.
     *
     * @return the repository session
     * @throws Exception the exception
     */
    public RepositoryInstance getRepositorySession() throws Exception {
        // FIXME: is it possible to reuse repository session?
        // Authentication failures happen while trying to reuse the session
    	Profiler profiler = new Profiler("getRepositorySession():", 2);
    	profiler.start();
    	
        NuxeoClientEmbedded client = NuxeoConnectorEmbedded.getInstance().getClient();
        RepositoryInstance repoSession = client.openRepository();
        if (logger.isTraceEnabled()) {
            logger.trace("Testing call to getRepository() repository root: " + repoSession.getRootDocument());
        }
        
        profiler.stop();
        return repoSession;
    }

    /**
     * Release repository session. - Package access only.
     *
     * @param repoSession the repo session
     */
    public void releaseRepositorySession(RepositoryInstance repoSession) {
        try {
            NuxeoClientEmbedded client = NuxeoConnectorEmbedded.getInstance().getClient();
            // release session
            client.releaseRepository(repoSession);
        } catch (Exception e) {
            logger.error("Could not close the repository session", e);
            // no need to throw this service specific exception
        }
    }

}
