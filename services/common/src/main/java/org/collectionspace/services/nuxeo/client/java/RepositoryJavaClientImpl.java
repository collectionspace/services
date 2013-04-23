/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.nuxeo.client.java;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.rowset.CachedRowSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.PreparedStatementSimpleBuilder;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.config.tenant.RepositoryDomainType;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.runtime.transaction.TransactionRuntimeException;

//
// CSPACE-5036 - How to make CMISQL queries from Nuxeo
//
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingUtils;
import org.collectionspace.services.common.storage.PreparedStatementBuilder;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.nuxeo.ecm.core.opencmis.impl.server.NuxeoCmisService;
import org.nuxeo.ecm.core.opencmis.impl.server.NuxeoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepositoryJavaClient is used to perform CRUD operations on documents in Nuxeo
 * repository using Remote Java APIs. It uses
 *
 * @see DocumentHandler as IOHandler with the client.
 *
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class RepositoryJavaClientImpl implements RepositoryClient<PoxPayloadIn, PoxPayloadOut> {

    /**
     * The logger.
     */
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
     * @param handler should be used by the caller to provide and transform the
     * document
     * @return id in repository of the newly created document
     * @throws BadRequestException
     * @throws TransactionException
     * @throws DocumentException
     */
    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            TransactionException, DocumentException {

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
            repoSession = getRepositorySession(ctx);
            DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
            DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
            String wspacePath = wspaceDoc.getPathAsString();
            //give our own ID so PathRef could be constructed later on
            String id = IdUtils.generateId(UUID.randomUUID().toString());
            // create document model
            DocumentModel doc = repoSession.createDocumentModel(wspacePath, id, docType);
            /* Check for a versioned document, and check In and Out before we proceed.
             * This does not work as we do not have the uid schema on our docs.
             if(((DocumentModelHandler) handler).supportsVersioning()) {
             doc.setProperty("uid","major_version",1);
             doc.setProperty("uid","minor_version",0);
             }
             */
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
                releaseRepositorySession(ctx, repoSession);
            }
        }

    }

    /**
     * get document from the Nuxeo repository
     *
     * @param ctx service context under which this method is invoked
     * @param id of the document to retrieve
     * @param handler should be used by the caller to provide and transform the
     * document
     * @throws DocumentNotFoundException if the document cannot be found in the
     * repository
     * @throws TransactionException
     * @throws DocumentException
     */
    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, TransactionException, DocumentException {

        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.get: handler is missing");
        }

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET);
            repoSession = getRepositorySession(ctx);
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
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

    /**
     * get a document from the Nuxeo repository, using the docFilter params.
     *
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the
     * document. Handler must have a docFilter set to return a single item.
     * @throws DocumentNotFoundException if the document cannot be found in the
     * repository
     * @throws TransactionException
     * @throws DocumentException
     */
    @Override
    public void get(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, TransactionException, DocumentException {
        QueryContext queryContext = new QueryContext(ctx, handler);
        RepositoryInstance repoSession = null;

        try {
            handler.prepare(Action.GET);
            repoSession = getRepositorySession(ctx);

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
                releaseRepositorySession(ctx, repoSession);
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
     * Get wrapped documentModel from the Nuxeo repository. The search is
     * restricted to the workspace of the current context.
     *
     * @param ctx service context under which this method is invoked
     * @param csid of the document to retrieve
     * @throws DocumentNotFoundException
     * @throws TransactionException
     * @throws DocumentException
     * @return a wrapped documentModel
     */
    @Override
    public DocumentWrapper<DocumentModel> getDoc(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String csid) throws DocumentNotFoundException, TransactionException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            // Open a new repository session
            repoSession = getRepositorySession(ctx);
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
                releaseRepositorySession(ctx, repoSession);
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
     *
     * @param ctx service context under which this method is invoked
     * @param whereClause where NXQL where clause to get the document
     * @throws DocumentNotFoundException
     * @throws TransactionException
     * @throws DocumentException
     * @return a wrapped documentModel retrieved by the repository query
     */
    @Override
    public DocumentWrapper<DocumentModel> findDoc(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String whereClause)
            throws DocumentNotFoundException, TransactionException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            repoSession = getRepositorySession(ctx);
            wrapDoc = findDoc(repoSession, ctx, whereClause);
        } catch (Exception e) {
            throw new DocumentException("Unable to create a Nuxeo repository session.", e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

        if (logger.isWarnEnabled() == true) {
            logger.warn("Returned DocumentModel instance was created with a repository session that is now closed.");
        }

        return wrapDoc;
    }

    /**
     * find doc and return CSID from the Nuxeo repository
     *
     * @param repoSession
     * @param ctx service context under which this method is invoked
     * @param whereClause where NXQL where clause to get the document
     * @throws DocumentNotFoundException
     * @throws TransactionException
     * @throws DocumentException
     * @return the CollectionSpace ID (CSID) of the requested document
     */
    @Override
    public String findDocCSID(RepositoryInstance repoSession,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String whereClause)
            throws DocumentNotFoundException, TransactionException, DocumentException {
        String csid = null;
        boolean releaseSession = false;
        try {
            if (repoSession == null) {
                repoSession = this.getRepositorySession(ctx);
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
            if (releaseSession && (repoSession != null)) {
                this.releaseRepositorySession(ctx, repoSession);
            }
        }
        return csid;
    }

    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryInstance repoSession,
            List<String> docTypes,
            String whereClause,
            String orderByClause,
            int pageSize,
            int pageNum,
            boolean computeTotal)
            throws DocumentNotFoundException, DocumentException {
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        try {
            if (docTypes == null || docTypes.size() < 1) {
                throw new DocumentNotFoundException(
                        "The findDocs() method must specify at least one DocumentType.");
            }
            DocumentModelList docList = null;
            QueryContext queryContext = new QueryContext(ctx, whereClause, orderByClause);
            String query = NuxeoUtils.buildNXQLQuery(docTypes, queryContext);
            if (logger.isDebugEnabled()) {
                logger.debug("findDocs() NXQL: " + query);
            }
            docList = repoSession.query(query, null, pageSize, pageSize * pageNum, computeTotal);
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

    protected static String buildInListForDocTypes(List<String> docTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (String docType : docTypes) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("'");
            sb.append(docType);
            sb.append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            DocumentHandler handler,
            RepositoryInstance repoSession,
            List<String> docTypes)
            throws DocumentNotFoundException, DocumentException {
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        DocumentFilter filter = handler.getDocumentFilter();
        String oldOrderBy = filter.getOrderByClause();
        if (isClauseEmpty(oldOrderBy) == true) {
            filter.setOrderByClause(DocumentFilter.ORDER_BY_LAST_UPDATED);
        }
        QueryContext queryContext = new QueryContext(ctx, handler);

        try {
            if (docTypes == null || docTypes.size() < 1) {
                throw new DocumentNotFoundException(
                        "The findDocs() method must specify at least one DocumentType.");
            }
            DocumentModelList docList = null;
            if (handler.isCMISQuery() == true) {
                String inList = buildInListForDocTypes(docTypes);
                ctx.getQueryParams().add(IQueryManager.SEARCH_RELATED_MATCH_OBJ_DOCTYPES, inList);
                docList = getFilteredCMIS(repoSession, ctx, handler, queryContext);
            } else {
                String query = NuxeoUtils.buildNXQLQuery(docTypes, queryContext);
                if (logger.isDebugEnabled()) {
                    logger.debug("findDocs() NXQL: " + query);
                }
                docList = repoSession.query(query, null, filter.getPageSize(), filter.getOffset(), true);
            }
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
     *
     * @param docTypes a list of DocType names to match
     * @param whereClause where the clause to qualify on
     * @throws DocumentNotFoundException
     * @throws TransactionException
     * @throws DocumentException
     * @return a list of documentModels
     */
    @Override
    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            List<String> docTypes,
            String whereClause,
            int pageSize, int pageNum, boolean computeTotal)
            throws DocumentNotFoundException, TransactionException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        try {
            repoSession = getRepositorySession(ctx);
            wrapDoc = findDocs(ctx, repoSession, docTypes, whereClause, null,
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
                releaseRepositorySession(ctx, repoSession);
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
            throws DocumentNotFoundException, TransactionException, DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.getAll: handler is missing");
        }

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession(ctx);
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
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

    /**
     * getAll get all documents for an entity entity service from the Nuxeo
     * repository
     *
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the
     * document
     * @throws DocumentNotFoundException
     * @throws TransactionException
     * @throws DocumentException
     */
    @Override
    public void getAll(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, TransactionException, DocumentException {
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
            repoSession = getRepositorySession(ctx);
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
                releaseRepositorySession(ctx, repoSession);
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
            repoSession = getRepositorySession(ctx);
            result = getDocFromCsid(ctx, repoSession, csid);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

        if (logger.isWarnEnabled() == true) {
            logger.warn("Returned DocumentModel instance was created with a repository session that is now closed.");
        }

        return result;
    }

    /**
     * Returns a URI value for a document in the Nuxeo repository
     *
     * @param wrappedDoc a wrapped documentModel
     * @throws ClientException
     * @return a document URI
     */
    @Override
    public String getDocURI(DocumentWrapper<DocumentModel> wrappedDoc) throws ClientException {
        DocumentModel docModel = wrappedDoc.getWrappedObject();
        String uri = (String) docModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
                CollectionSpaceClient.COLLECTIONSPACE_CORE_URI);
        return uri;
    }

    /*
     * See CSPACE-5036 - How to make CMISQL queries from Nuxeo
     */
    private IterableQueryResult makeCMISQLQuery(RepositoryInstance repoSession, String query, QueryContext queryContext) {
        IterableQueryResult result = null;

        // the NuxeoRepository should be constructed only once, then cached
        // (its construction is expensive)
        try {
            NuxeoRepository repo = new NuxeoRepository(
                    repoSession.getRepositoryName(), repoSession
                    .getRootDocument().getId());
            logger.debug("Repository ID:" + repo.getId() + " Root folder:"
                    + repo.getRootFolderId());

            CallContextImpl callContext = new CallContextImpl(
                    CallContext.BINDING_LOCAL, repo.getId(), false);
            callContext.put(CallContext.USERNAME, repoSession.getPrincipal()
                    .getName());
            NuxeoCmisService cmisService = new NuxeoCmisService(repo,
                    callContext, repoSession);

            result = repoSession.queryAndFetch(query, "CMISQL", cmisService);
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            logger.error("Encounter trouble making the following CMIS query: " + query, e);
        }

        return result;
    }

    /**
     * getFiltered get all documents for an entity service from the Document
     * repository, given filter parameters specified by the handler.
     *
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the
     * document
     * @throws DocumentNotFoundException if workspace not found
     * @throws TransactionException
     * @throws DocumentException
     */
    @Override
    public void getFiltered(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, TransactionException, DocumentException {

        DocumentFilter filter = handler.getDocumentFilter();
        String oldOrderBy = filter.getOrderByClause();
        if (isClauseEmpty(oldOrderBy) == true) {
            filter.setOrderByClause(DocumentFilter.ORDER_BY_LAST_UPDATED);
        }
        QueryContext queryContext = new QueryContext(ctx, handler);

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession(ctx); //Keeps a refcount here for the repository session so you need to release this when finished

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
            if (handler.isJDBCQuery() == true) {
                docList = getFilteredJDBC(repoSession, ctx, handler);
            } else if (handler.isCMISQuery() == true) {
                docList = getFilteredCMIS(repoSession, ctx, handler, queryContext); //FIXME: REM - Need to deal with paging info in CMIS query
            } else if ((queryContext.getDocFilter().getOffset() > 0) || (queryContext.getDocFilter().getPageSize() > 0)) {
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
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

    /**
     * Perform a database query, via JDBC and SQL, to retrieve matching records
     * based on filter criteria.
     * 
     * Although this method currently has a general-purpose name, it is
     * currently dedicated to a specific task: improving performance for
     * partial term matching queries on authority items / terms, via
     * the use of a hand-tuned SQL query, rather than the generated SQL
     * produced by Nuxeo from an NXQL query.
     * 
     * @param repoSession a repository session.
     * @param ctx the service context.
     * @param handler a relevant document handler.
     * @return a list of document models matching the search criteria.
     * @throws Exception 
     */
    private DocumentModelList getFilteredJDBC(RepositoryInstance repoSession, ServiceContext ctx, 
            DocumentHandler handler) throws Exception {
        DocumentModelList result = new DocumentModelListImpl();

        // FIXME: Get all of the following values from appropriate external constants.
        //
        // At present, the two constants below are duplicated in both RepositoryJavaClientImpl
        // and in AuthorityItemDocumentModelHandler.
        final String TERM_GROUP_LIST_NAME = "TERM_GROUP_LIST_NAME";
        final String TERM_GROUP_TABLE_NAME_PARAM = "TERM_GROUP_TABLE_NAME";
        final String IN_AUTHORITY_PARAM = "IN_AUTHORITY";
        // Get this from a constant in AuthorityResource or equivalent
        final String PARENT_WILDCARD = "_ALL_";
        
        // Build two SQL statements, to be executed within a single transaction:
        // the first statement to control join order, and the second statement
        // representing the actual 'get filtered' query
        
        // Build the join control statement
        //
        // Per http://www.postgresql.org/docs/9.2/static/runtime-config-query.html#GUC-JOIN-COLLAPSE-LIMIT
        // "Setting [this value] to 1 prevents any reordering of explicit JOINs.
        // Thus, the explicit join order specified in the query will be the
        // actual order in which the relations are joined."
        // See CSPACE-5945 for further discussion of why this setting is needed.
        //
        // Adding this statement is commented out here for now.  It significantly
        // improved query performance for authority item / term queries where
        // large numbers of rows were retrieved, but appears to have resulted
        // in consistently slower-than-desired query performance where zero or
        // very few records were retrieved. See notes on CSPACE-5945. - ADR 2013-04-09
        // String joinControlSql = "SET LOCAL join_collapse_limit TO 1;";
        
        // Build the query statement
        //
        // Start with the default query
        String selectStatement =
                "SELECT DISTINCT commonschema.id"
                + " FROM " + handler.getServiceContext().getCommonPartLabel() + " commonschema";
        
        String joinClauses =
                " INNER JOIN misc"
                + "  ON misc.id = commonschema.id"
                + " INNER JOIN hierarchy hierarchy_termgroup"
                + "  ON hierarchy_termgroup.parentid = misc.id"
                + " INNER JOIN "  + handler.getJDBCQueryParams().get(TERM_GROUP_TABLE_NAME_PARAM) + " termgroup"
                + "  ON termgroup.id = hierarchy_termgroup.id ";

        String whereClause;
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        String partialTerm = queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);
        // If the value of the partial term query parameter is blank ('pt='),
        // return all records, subject to restriction by any limit clause
        if (Tools.isBlank(partialTerm)) {
           whereClause = "";
        } else {
           // Otherwise, return records that match the supplied partial term
           whereClause =
                " WHERE (termgroup.termdisplayname ILIKE ?)";
        }
        
        // At present, results are ordered in code, below, rather than in SQL,
        // and the orderByClause below is thus intentionally blank.
        //
        // To implement the orderByClause below in SQL; e.g. via
        // 'ORDER BY termgroup.termdisplayname', the relevant column
        // must be returned by the SELECT statement.
        String orderByClause = "";
        
        String limitClause;
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tReader.getTenantBinding(ctx.getTenantId());
        String maxListItemsLimit = TenantBindingUtils.getPropertyValue(tenantBinding,
                IQueryManager.MAX_LIST_ITEMS_RETURNED_LIMIT_ON_JDBC_QUERIES);
        limitClause =
                " LIMIT " + getMaxItemsLimitOnJdbcQueries(maxListItemsLimit); // implicit int-to-String conversion
        
        List<String> params = new ArrayList<>();
        
        // Read tenant bindings configuration to determine whether
        // to automatically insert leading, as well as trailing, wildcards
        // into the term matching string.
        String usesStartingWildcard = TenantBindingUtils.getPropertyValue(tenantBinding,
                IQueryManager.TENANT_USES_STARTING_WILDCARD_FOR_PARTIAL_TERM);
        // Handle user-provided leading wildcard characters, in the
        // configuration where a leading wildcard is not automatically inserted.
        // (The user-provided wildcard must be in the first, or "starting"
        // character position in the partial term value.)
        if (Tools.notBlank(usesStartingWildcard) && usesStartingWildcard.equalsIgnoreCase(Boolean.FALSE.toString())) {
            partialTerm = handleProvidedStartingWildcard(partialTerm);
            // Otherwise, automatically insert a leading wildcard
        } else {
            partialTerm = JDBCTools.SQL_WILDCARD + partialTerm;
        }
        // Automatically insert a trailing wildcard
        params.add(partialTerm + JDBCTools.SQL_WILDCARD); // Value for replaceable parameter 1 in the query
        
        // Optionally add restrictions to the default query, based on variables
        // in the current request
        
        // Restrict the query to filter out deleted records, if requested
        String includeDeleted = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_NONDELETED);
        if (includeDeleted != null && includeDeleted.equalsIgnoreCase(Boolean.FALSE.toString())) {
            whereClause = whereClause
                + "  AND (misc.lifecyclestate <> '" + WorkflowClient.WORKFLOWSTATE_DELETED + "')";
        }

        // If a particular authority is specified, restrict the query further
        // to return only records within that authority
        String inAuthorityValue = (String) handler.getJDBCQueryParams().get(IN_AUTHORITY_PARAM);
        if (Tools.notBlank(inAuthorityValue)) {
            // Handle the '_ALL_' case for inAuthority
            if (inAuthorityValue.equals(PARENT_WILDCARD)) {
                // Add nothing to the query here if it should match within all authorities
            } else {
                whereClause = whereClause
                    + "  AND (commonschema.inauthority = ?)";
                params.add(inAuthorityValue); // Value for replaceable parameter 2 in the query
            }
        }
        
        // Restrict the query further to return only records pertaining to
        // the current tenant, unless:
        // * Data for this service, in this tenant, is stored in its own,
        //   separate repository, rather than being intermingled with other
        //   tenants' data in the default repository; or
        // * Restriction by tenant ID in JDBC queries has been disabled,
        //   via configuration for this tenant, 
        if (restrictJDBCQueryByTenantID(tenantBinding, ctx)) {
                joinClauses = joinClauses
                    + " INNER JOIN collectionspace_core core"
                    + "  ON core.id = hierarchy_termgroup.parentid";
                whereClause = whereClause
                    + "  AND (core.tenantid = ?)";
                params.add(ctx.getTenantId()); // Value for replaceable parameter 3 in the query
        }
        
        // Piece together the SQL query from its parts
        String querySql = selectStatement + joinClauses + whereClause + orderByClause + limitClause;
        
        // Note: PostgreSQL 9.2 introduced a change that may improve performance
        // of certain queries using JDBC PreparedStatements.  See comments on
        // CSPACE-5943 for details.
        //
        // See a comment above for the reason that the joinControl SQL statement,
        // along with its corresponding prepared statement builder, is commented out for now.
        // PreparedStatementBuilder joinControlBuilder = new PreparedStatementBuilder(joinControlSql);
        PreparedStatementSimpleBuilder queryBuilder = new PreparedStatementSimpleBuilder(querySql, params);
        List<PreparedStatementBuilder> builders = new ArrayList<>();
        // builders.add(joinControlBuilder);
        builders.add(queryBuilder);
        String dataSourceName = JDBCTools.NUXEO_DATASOURCE_NAME;
        String repositoryName = ctx.getRepositoryName();
        final Boolean EXECUTE_WITHIN_TRANSACTION = true;
        Set<String> docIds = new HashSet<>();
        try {
            List<CachedRowSet> resultsList = JDBCTools.executePreparedQueries(builders,
                dataSourceName, repositoryName, EXECUTE_WITHIN_TRANSACTION);

            // At least one set of results is expected, from the second prepared
            // statement to be executed.
            // If fewer results are returned, return an empty list of document models
            if (resultsList == null || resultsList.size() < 1) {
                return result; // return an empty list of document models
            }
            // The join control query (if enabled - it is currently commented
            // out as per comments above) will not return results, so query results
            // will be the first set of results (rowSet) returned in the list
            CachedRowSet queryResults = resultsList.get(0);
            
            // If the result from executing the query is null or contains zero rows,
            // return an empty list of document models
            if (queryResults == null) {
                return result; // return an empty list of document models
            }
            queryResults.last();
            if (queryResults.getRow() == 0) {
                return result; // return an empty list of document models
            }

            // Otherwise, get the document IDs from the results of the query
            String id;
            queryResults.beforeFirst();
            while (queryResults.next()) {
                id = queryResults.getString(1);
                if (Tools.notBlank(id)) {
                    docIds.add(id);
                }
            }
        } catch (SQLException sqle) {
            logger.warn("Could not obtain document IDs via SQL query '" + querySql + "': " + sqle.getMessage());
            return result; // return an empty list of document models
        } 

        // Get a list of document models, using the IDs obtained from the query
        DocumentModel docModel;
        for (String docId : docIds) {
            docModel = NuxeoUtils.getDocumentModel(repoSession, docId);
            if (docModel == null) {
                logger.warn("Could not obtain document model for document with ID " + docId);
            } else {
                result.add(NuxeoUtils.getDocumentModel(repoSession, docId));
            }
        }
        
        // Order the results
        final String COMMON_PART_SCHEMA = handler.getServiceContext().getCommonPartLabel();
        final String DISPLAY_NAME_XPATH =
                "//" + handler.getJDBCQueryParams().get(TERM_GROUP_LIST_NAME) + "/[0]/termDisplayName";
        Collections.sort(result, new Comparator<DocumentModel>() {
            @Override
            public int compare(DocumentModel doc1, DocumentModel doc2) {
                String termDisplayName1 = (String) NuxeoUtils.getXPathValue(doc1, COMMON_PART_SCHEMA, DISPLAY_NAME_XPATH);
                String termDisplayName2 = (String) NuxeoUtils.getXPathValue(doc2, COMMON_PART_SCHEMA, DISPLAY_NAME_XPATH);
                return termDisplayName1.compareTo(termDisplayName2);
            }
        });

        return result;
    }
    

    private DocumentModelList getFilteredCMIS(RepositoryInstance repoSession, ServiceContext ctx, DocumentHandler handler, QueryContext queryContext)
            throws DocumentNotFoundException, DocumentException {

        DocumentModelList result = new DocumentModelListImpl();
        try {
            String query = handler.getCMISQuery(queryContext);

            DocumentFilter docFilter = handler.getDocumentFilter();
            int pageSize = docFilter.getPageSize();
            int offset = docFilter.getOffset();
            if (logger.isDebugEnabled()) {
                logger.debug("Executing CMIS query: " + query.toString()
                        + "with pageSize: " + pageSize + " at offset: " + offset);
            }

            // If we have limit and/or offset, then pass true to get totalSize
            // in returned DocumentModelList.
            Profiler profiler = new Profiler(this, 2);
            profiler.log("Executing CMIS query: " + query.toString());
            profiler.start();
            //
            IterableQueryResult queryResult = makeCMISQLQuery(repoSession, query, queryContext);
            try {
                int totalSize = (int) queryResult.size();
                ((DocumentModelListImpl) result).setTotalSize(totalSize);
                // Skip the rows before our offset
                if (offset > 0) {
                    queryResult.skipTo(offset);
                }
                int nRows = 0;
                for (Map<String, Serializable> row : queryResult) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(" Hierarchy Table ID is:" + row.get(IQueryManager.CMIS_TARGET_NUXEO_ID)
                                + " nuxeo:pathSegment is: " + row.get(IQueryManager.CMIS_TARGET_NAME));
                    }
                    String nuxeoId = (String) row.get(IQueryManager.CMIS_TARGET_NUXEO_ID);
                    DocumentModel docModel = NuxeoUtils.getDocumentModel(repoSession, nuxeoId);
                    result.add(docModel);
                    nRows++;
                    if (nRows >= pageSize && pageSize != 0) { // A page size of zero means that they want all of them
                        logger.debug("Got page full of items - quitting");
                        break;
                    }
                }
            } finally {
                queryResult.close();
            }
            //
            profiler.stop();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }

        //
        // Since we're not supporting paging yet for CMIS queries, we need to perform
        // a workaround for the paging information we return in our list of results
        //
        /*
         if (result != null) {
         docFilter.setStartPage(0);
         if (totalSize > docFilter.getPageSize()) {
         docFilter.setPageSize(totalSize);
         ((DocumentModelListImpl)result).setTotalSize(totalSize);
         }
         }
         */

        return result;
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
     * @param csid of the document
     * @param handler should be used by the caller to provide and transform the
     * document
     * @throws BadRequestException
     * @throws DocumentNotFoundException
     * @throws TransactionException if the transaction times out or otherwise
     * cannot be successfully completed
     * @throws DocumentException
     */
    @Override
    public void update(ServiceContext ctx, String csid, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException, TransactionException,
            DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.update: document handler is missing.");
        }

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.UPDATE);
            repoSession = getRepositorySession(ctx);
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, csid);
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (ClientException ce) {
                String msg = logException(ce, "Could not find document to update with CSID=" + csid);
                throw new DocumentNotFoundException(msg, ce);
            }
            // Check for a versioned document, and check In and Out before we proceed.
            if (((DocumentModelHandler) handler).supportsVersioning()) {
                /* Once we advance to 5.5 or later, we can add this. 
                 * See also https://jira.nuxeo.com/browse/NXP-8506
                 if(!doc.isVersionable()) {
                 throw new DocumentException("Configuration for: "
                 +handler.getServiceContextPath()+" supports versioning, but Nuxeo config does not!");
                 }
                 */
                /* Force a version number - Not working. Apparently we need to configure the uid schema??
                 if(doc.getProperty("uid","major_version") == null) {
                 doc.setProperty("uid","major_version",1);
                 }
                 if(doc.getProperty("uid","minor_version") == null) {
                 doc.setProperty("uid","minor_version",0);
                 }
                 */
                doc.checkIn(VersioningOption.MINOR, null);
                doc.checkOut();
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
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

    /**
     * Save a documentModel to the Nuxeo repository.
     *
     * @param ctx service context under which this method is invoked
     * @param repoSession
     * @param docModel the document to save
     * @param fSaveSession if TRUE, will call CoreSession.save() to save
     * accumulated changes.
     * @throws ClientException
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
     * @param repoSession a repository session
     * @param docModelList a list of document models
     * @param fSaveSession if TRUE, will call CoreSession.save() to save
     * accumulated changes.
     * @throws ClientException
     * @throws DocumentException
     */
    public void saveDocListWithoutHandlerProcessing(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryInstance repoSession,
            DocumentModelList docList,
            boolean fSaveSession)
            throws ClientException, DocumentException {
        try {
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
     *
     * @param ctx service context under which this method is invoked
     * @param id of the document
     * @throws DocumentException
     */
    @Override
    public void delete(ServiceContext ctx, String id, DocumentHandler handler) throws DocumentNotFoundException,
            DocumentException, TransactionException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, ix, handler): ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, ix, handler): handler is missing");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting document with CSID=" + id);
        }
        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.DELETE);
            repoSession = getRepositorySession(ctx);
            DocumentWrapper<DocumentModel> wrapDoc = null;
            try {
                DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
                wrapDoc = new DocumentWrapperImpl<DocumentModel>(repoSession.getDocument(docRef));
                ((DocumentModelHandler) handler).setRepositorySession(repoSession);
                handler.handle(Action.DELETE, wrapDoc);
                repoSession.removeDocument(docRef);
            } catch (ClientException ce) {
                String msg = logException(ce, "Could not find document to delete with CSID=" + id);
                throw new DocumentNotFoundException(msg, ce);
            }
            repoSession.save();
            handler.complete(Action.DELETE, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#delete(org.collectionspace.services.common.context.ServiceContext, java.lang.String, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    @Deprecated
    public void delete(@SuppressWarnings("rawtypes") ServiceContext ctx, String id)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException();
        // Use the other delete instead
    }

    @Override
    public Hashtable<String, String> retrieveWorkspaceIds(RepositoryDomainType repoDomain) throws Exception {
        return NuxeoConnectorEmbedded.getInstance().retrieveWorkspaceIds(repoDomain);
    }

    @Override
    public String createDomain(RepositoryDomainType repositoryDomain) throws Exception {
        RepositoryInstance repoSession = null;
        String domainId = null;
        try {
            //
            // Open a connection to the domain's repo/db
            //
            String repoName = repositoryDomain.getRepositoryName();
            repoSession = getRepositorySession(repoName); // domainName=storageName=repoName=databaseName
            //
            // First create the top-level domain directory
            //
            String domainName = repositoryDomain.getStorageName();
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
                        + " id=" + domainId + " "
                        + NuxeoUtils.Workspaces + " id=" + workspacesRootId);
                logger.debug("Path to Domain: " + domainDoc.getPathAsString());
                logger.debug("Path to Workspaces root: " + workspacesRoot.getPathAsString());
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not create tenant domain name=" + repositoryDomain.getStorageName() + " caught exception ", e);
            }
            throw e;
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(null, repoSession);
            }
        }

        return domainId;
    }

    @Override
    public String getDomainId(RepositoryDomainType repositoryDomain) throws Exception {
        String domainId = null;
        RepositoryInstance repoSession = null;

        String repoName = repositoryDomain.getRepositoryName();
        String domainStorageName = repositoryDomain.getStorageName();
        if (domainStorageName != null && !domainStorageName.isEmpty()) {
            try {
                repoSession = getRepositorySession(repoName);
                DocumentRef docRef = new PathRef("/" + domainStorageName);
                DocumentModel domain = repoSession.getDocument(docRef);
                domainId = domain.getId();
            } catch (Exception e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Caught exception ", e);  // The document doesn't exist, this let's us know we need to create it
                }
                //there is no way to identify if document does not exist due to
                //lack of typed exception for getDocument method
                return null;
            } finally {
                if (repoSession != null) {
                    releaseRepositorySession(null, repoSession);
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
    public String createWorkspace(RepositoryDomainType repositoryDomain, String workspaceName) throws Exception {
        RepositoryInstance repoSession = null;
        String workspaceId = null;
        try {
            String repoName = repositoryDomain.getRepositoryName();
            repoSession = getRepositorySession(repoName);

            String domainStorageName = repositoryDomain.getStorageName();
            DocumentModel parentDoc = getWorkspacesRoot(repoSession, domainStorageName);
            if (logger.isTraceEnabled()) {
                for (String facet : parentDoc.getFacets()) {
                    logger.trace("Facet: " + facet);
                }
            }

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
                releaseRepositorySession(null, repoSession);
            }
        }
        return workspaceId;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.repository.RepositoryClient#getWorkspaceId(java.lang.String, java.lang.String)
     */
    @Override
    @Deprecated
    public String getWorkspaceId(String tenantDomain, String workspaceName) throws Exception {
        String workspaceId = null;

        RepositoryInstance repoSession = null;
        try {
            repoSession = getRepositorySession((ServiceContext) null);
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
                releaseRepositorySession(null, repoSession);
            }
        }

        return workspaceId;
    }

    public RepositoryInstance getRepositorySession(ServiceContext ctx) throws Exception {
        return getRepositorySession(ctx, ctx.getRepositoryName());
    }

    public RepositoryInstance getRepositorySession(String repoName) throws Exception {
        return getRepositorySession(null, repoName);
    }

    /**
     * Gets the repository session. - Package access only. If the 'ctx' param is
     * null then the repo name must be non-mull and vice-versa
     *
     * @return the repository session
     * @throws Exception the exception
     */
    public RepositoryInstance getRepositorySession(ServiceContext ctx, String repoName) throws Exception {
        RepositoryInstance repoSession = null;

        Profiler profiler = new Profiler("getRepositorySession():", 2);
        profiler.start();
        //
        // To get a connection to the Nuxeo repo, we need either a valid ServiceContext instance or a repository name
        //
        if (ctx != null) {
            repoName = ctx.getRepositoryName(); // Notice we are overriding the passed in 'repoName' since we have a valid service context passed in to us
            repoSession = (RepositoryInstance) ctx.getCurrentRepositorySession(); // Look to see if one exists in the context before creating one
        } else if (repoName == null || repoName.trim().isEmpty()) {
            String errMsg = String.format("We can't get a connection to the Nuxeo repo because the service context passed in was null and no repository name was passed in either.");
            logger.error(errMsg);
            throw new Exception(errMsg);
        }
        //
        // If we couldn't find a repoSession from the service context (or the context was null) then we need to create a new one using
        // just the repo name
        //
        if (repoSession == null) {
            NuxeoClientEmbedded client = NuxeoConnectorEmbedded.getInstance().getClient();
            repoSession = client.openRepository(repoName);
        } else {
            if (logger.isDebugEnabled() == true) {
                logger.warn("Reusing the current context's repository session.");
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Testing call to getRepository() repository root: " + repoSession.getRootDocument());
        }

        profiler.stop();

        if (ctx != null) {
            ctx.setCurrentRepositorySession(repoSession); // For reusing, save the repository session in the current service context
        }

        return repoSession;
    }

    /**
     * Release repository session. - Package access only.
     *
     * @param repoSession the repo session
     */
    public void releaseRepositorySession(ServiceContext ctx, RepositoryInstance repoSession) throws TransactionException {
        try {
            NuxeoClientEmbedded client = NuxeoConnectorEmbedded.getInstance().getClient();
            // release session
            if (ctx != null) {
                ctx.clearCurrentRepositorySession(); //clear the current context of the now closed repo session
                if (ctx.getCurrentRepositorySession() == null) {
                    client.releaseRepository(repoSession); //release the repo session if the service context's ref count is zeo.
                }
            } else {
                client.releaseRepository(repoSession); //repo session was acquired without a service context
            }
        } catch (TransactionRuntimeException tre) {
            TransactionException te = new TransactionException(tre);
            logger.error(te.getMessage(), tre); // Log the standard transaction exception message, plus an exception-specific stack trace
            throw te;
        } catch (Exception e) {
            logger.error("Could not close the repository session", e);
            // no need to throw this service specific exception
        }
    }

    @Override
    public void doWorkflowTransition(ServiceContext ctx, String id,
            DocumentHandler handler, TransitionDef transitionDef)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        // This is a placeholder for when we change the StorageClient interface to treat workflow transitions as 1st class operations like 'get', 'create', 'update, 'delete', etc
    }

    private String handleProvidedStartingWildcard(String partialTerm) {
        if (Tools.notBlank(partialTerm)) {
            // FIXME: Get this value from an existing constant, if available
            final String USER_SUPPLIED_WILDCARD = "*";
            if (partialTerm.substring(0, 1).equals(USER_SUPPLIED_WILDCARD)) {
                StringBuffer buffer = new StringBuffer(partialTerm);
                buffer.setCharAt(0, JDBCTools.SQL_WILDCARD.charAt(0));
                partialTerm = buffer.toString();
            }
        }
        return partialTerm;
    }

    private int getMaxItemsLimitOnJdbcQueries(String maxListItemsLimit) {
        final int DEFAULT_ITEMS_LIMIT = 40;
        if (maxListItemsLimit == null) {
            return DEFAULT_ITEMS_LIMIT;
        }
        int itemsLimit;
        try {
            itemsLimit = Integer.parseInt(maxListItemsLimit);
            if (itemsLimit < 1) {
                logger.warn("Value of configuration setting "
                        + IQueryManager.MAX_LIST_ITEMS_RETURNED_LIMIT_ON_JDBC_QUERIES
                        + " must be a positive integer; invalid current value is " + maxListItemsLimit);
                logger.warn("Reverting to default value of " + DEFAULT_ITEMS_LIMIT);
                itemsLimit = DEFAULT_ITEMS_LIMIT;
            }
        } catch (NumberFormatException nfe) {
            logger.warn("Value of configuration setting "
                        + IQueryManager.MAX_LIST_ITEMS_RETURNED_LIMIT_ON_JDBC_QUERIES
                        + " must be a positive integer; invalid current value is " + maxListItemsLimit);
            logger.warn("Reverting to default value of " + DEFAULT_ITEMS_LIMIT);
            itemsLimit = DEFAULT_ITEMS_LIMIT;
        }
        return itemsLimit;
    }

    /**
     * Identifies whether a restriction on tenant ID - to return only records
     * pertaining to the current tenant - is required in a JDBC query.
     * 
     * @param tenantBinding a tenant binding configuration.
     * @param ctx a service context.
     * @return true if a restriction on tenant ID is required in the query;
     * false if a restriction is not required.
     */
    private boolean restrictJDBCQueryByTenantID(TenantBindingType tenantBinding, ServiceContext ctx) {
        boolean restrict = true;
        // If data for the current service, in the current tenant, is isolated
        // within its own separate, per-tenant repository, as contrasted with
        // being intermingled with other tenants' data in the default repository,
        // no restriction on Tenant ID is required in the query.
        String repositoryDomainName = ConfigUtils.getRepositoryName(tenantBinding, ctx.getRepositoryDomainName());
        if (!(repositoryDomainName.equals(ConfigUtils.DEFAULT_NUXEO_REPOSITORY_NAME))) {
            restrict = false;
        }
        // If a configuration setting for this tenant identifies that JDBC
        // queries should not be restricted by tenant ID (perhaps because
        // there is always expected to be only one tenant's data present in
        // the system), no restriction on Tenant ID is required in the query.
        String queriesRestrictedByTenantId = TenantBindingUtils.getPropertyValue(tenantBinding,
                IQueryManager.JDBC_QUERIES_ARE_TENANT_ID_RESTRICTED);
        if (Tools.notBlank(queriesRestrictedByTenantId) &&
                queriesRestrictedByTenantId.equalsIgnoreCase(Boolean.FALSE.toString())) {
            restrict = false;
        }
        return restrict;
    }
}
