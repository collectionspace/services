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

import java.util.Hashtable;
import java.util.UUID;
import java.util.List;

import org.collectionspace.services.common.context.ServiceContext;

import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;

import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.profile.Profiler;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepositoryJavaClient is used to perform CRUD operations on documents in Nuxeo
 * repository using Remote Java APIs. It uses @see DocumentHandler as IOHandler
 * with the client.
 * 
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class RepositoryJavaClientImpl implements RepositoryClient {

    /**
     * The Class QueryContext.
     */
    private class QueryContext {

        /** The doc type. */
        String docType;
        /** The doc filter. */
        DocumentFilter docFilter;
        /** The where clause. */
        String whereClause;
        /** The order by clause. */
        String orderByClause;
        /** The domain. */
        String domain;
        /** The tenant id. */
        String tenantId;

        /**
         * Instantiates a new query context.
         *
         * @param ctx the ctx
         * @throws DocumentNotFoundException the document not found exception
         * @throws DocumentException the document exception
         */
        QueryContext(ServiceContext<MultipartInput, MultipartOutput> ctx) throws DocumentNotFoundException, DocumentException {
            docType = ctx.getDocumentType();
            if (docType == null) {
                throw new DocumentNotFoundException(
                        "Unable to find DocumentType for service " + ctx.getServiceName());
            }
            domain = ctx.getRepositoryDomainName();
            if (domain == null) {
                throw new DocumentNotFoundException(
                        "Unable to find Domain for service " + ctx.getServiceName());
            }
            tenantId = ctx.getTenantId();
            if (tenantId == null) {
                throw new IllegalArgumentException(
                        "Service context has no Tenant ID specified.");
            }
        }

        /**
         * Instantiates a new query context.
         *
         * @param ctx the ctx
         * @param theWhereClause the where clause
         * @throws DocumentNotFoundException the document not found exception
         * @throws DocumentException the document exception
         */
        QueryContext(ServiceContext<MultipartInput, MultipartOutput> ctx,
                String theWhereClause) throws DocumentNotFoundException, DocumentException {
            this(ctx);
            whereClause = theWhereClause;
        }

        /**
         * Instantiates a new query context.
         *
         * @param ctx the ctx
         * @param handler the handler
         * @throws DocumentNotFoundException the document not found exception
         * @throws DocumentException the document exception
         */
        QueryContext(ServiceContext<MultipartInput, MultipartOutput> ctx,
                DocumentHandler handler) throws DocumentNotFoundException, DocumentException {
            this(ctx);
            if (handler == null) {
                throw new IllegalArgumentException(
                        "Document handler is missing.");
            }
            docFilter = handler.getDocumentFilter();
            if (docFilter == null) {
                throw new IllegalArgumentException(
                        "Document handler has no Filter specified.");
            }
            whereClause = docFilter.getWhereClause();
            orderByClause = docFilter.getOrderByClause();
        }
    }
    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RepositoryJavaClientImpl.class);
//    private final Logger profilerLogger = LoggerFactory.getLogger("remperf");
//    private String foo = Profiler.createLogger();
    /**
     * Instantiates a new repository java client impl.
     */
    public RepositoryJavaClientImpl() {
        //Empty constructor
    	
    }

    /**
     * Sets the collection space core values.
     *
     * @param ctx the ctx
     * @param documentModel the document model
     * @throws ClientException the client exception
     */
    private void setCollectionSpaceCoreValues(ServiceContext<MultipartInput, MultipartOutput> ctx,
            DocumentModel documentModel,
            Action action) throws ClientException {
        //
        // Add the tenant ID value to the new entity
        //
        documentModel.setProperty(DocumentModelHandler.COLLECTIONSPACE_CORE_SCHEMA,
                DocumentModelHandler.COLLECTIONSPACE_CORE_TENANTID,
                ctx.getTenantId());
        switch (action) {
            case CREATE:
                //add creation date value
                break;
            case UPDATE:
                //add update value
                break;
            default:
        }
    }

    /**
     * create document in the Nuxeo repository
     *
     * @param ctx service context under which this method is invoked
     * @param docType
     *            of the document created
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

        if (ctx.getDocumentType() == null) {
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
            DocumentModel doc = repoSession.createDocumentModel(wspacePath, id,
                    ctx.getDocumentType());
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModel> wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
            handler.handle(Action.CREATE, wrapDoc);
            // create document with documentmodel
            setCollectionSpaceCoreValues(ctx, doc, Action.CREATE);
            doc = repoSession.createDocument(doc);
            repoSession.save();
// TODO for sub-docs need to call into the handler to let it deal with subitems. Pass in the id,
// and assume the handler has the state it needs (doc fragments). 
            handler.complete(Action.CREATE, wrapDoc);
            return id;
        } catch (BadRequestException bre) {
            throw bre;
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
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (ClientException ce) {
                String msg = "could not find document with id=" + id;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
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
            String query = buildNXQLQuery(queryContext);
            docList = repoSession.query(query, null, 1, 0, false);
            if (docList.size() != 1) {
                throw new DocumentNotFoundException("No document found matching filter params.");
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

    /**
     * get wrapped documentModel from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param id
     *            of the document to retrieve
     * @throws DocumentException
     */
    @Override
    public DocumentWrapper<DocumentModel> getDoc(
            ServiceContext ctx, String id)
            throws DocumentNotFoundException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            repoSession = getRepositorySession();
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (ClientException ce) {
                String msg = "could not find document with id=" + id;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
            }
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
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        return wrapDoc;
    }

    /**
     * find wrapped documentModel from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param where NXQL where clause to get the document
     * @throws DocumentException
     */
    @Override
    public DocumentWrapper<DocumentModel> findDoc(
            ServiceContext ctx, String whereClause)
            throws DocumentNotFoundException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            QueryContext queryContext = new QueryContext(ctx, whereClause);
            repoSession = getRepositorySession();
            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            String query = buildNXQLQuery(queryContext);
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
                throw new DocumentNotFoundException("No document found matching filter params.");
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
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        return wrapDoc;
    }

    /**
     * find doc and return CSID from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param where NXQL where clause to get the document
     * @throws DocumentException
     */
    @Override
    public String findDocCSID(
            ServiceContext ctx, String whereClause)
            throws DocumentNotFoundException, DocumentException {
        String csid = null;
        try {
            DocumentWrapper<DocumentModel> wrapDoc = findDoc(ctx, whereClause);
            DocumentModel docModel = wrapDoc.getWrappedObject();
            csid = NuxeoUtils.extractId(docModel.getPathAsString());
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
        }
        return csid;
    }

    /**
     * Find a list of documentModels from the Nuxeo repository
     * @param docTypes a list of DocType names to match
     * @param where the clause to qualify on
     * @param domain the domain for the associated services
     * @return
     */
    @Override
    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext ctx,
            List<String> docTypes,
            String whereClause,
            int pageSize, int pageNum, boolean computeTotal)
            throws DocumentNotFoundException, DocumentException {
        RepositoryInstance repoSession = null;
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        try {
            if (docTypes == null || docTypes.size() < 1) {
                throw new DocumentNotFoundException(
                        "findDocs must specify at least one DocumentType.");
            }
            repoSession = getRepositorySession();
            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            QueryContext queryContext = new QueryContext(ctx, whereClause);
            String query = buildNXQLQuery(docTypes, queryContext);
            docList = repoSession.query(query, null, pageSize, pageNum, computeTotal);
            wrapDoc = new DocumentWrapperImpl<DocumentModelList>(docList);
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
                    + " check if the workspace exists in the Nuxeo repository");
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

        QueryContext queryContext = new QueryContext(ctx, handler);

        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession();
            DocumentModelList docList = null;
            String query = buildNXQLQuery(queryContext);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing NXQL query: " + query.toString());
            }

            // If we have limit and/or offset, then pass true to get totalSize
            // in returned DocumentModelList.
        	Profiler profiler = new Profiler(this, 2);
        	profiler.log("Executing NXQL query: " + query.toString());
        	profiler.start();
            if ((queryContext.docFilter.getOffset() > 0) || (queryContext.docFilter.getPageSize() > 0)) {
                docList = repoSession.query(query, null,
                        queryContext.docFilter.getPageSize(), queryContext.docFilter.getOffset(), true);
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
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.update: handler is missing");
        }
        RepositoryInstance repoSession = null;
        try {
            handler.prepare(Action.UPDATE);
            repoSession = getRepositorySession();
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (ClientException ce) {
                String msg = "Could not find document to update with id=" + id;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
            }
            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModel> wrapDoc = new DocumentWrapperImpl<DocumentModel>(doc);
            handler.handle(Action.UPDATE, wrapDoc);
            setCollectionSpaceCoreValues(ctx, doc, Action.CREATE);
            repoSession.saveDocument(doc);
            repoSession.save();
            handler.complete(Action.UPDATE, wrapDoc);
        } catch (BadRequestException bre) {
            throw bre;
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
            logger.debug("deleting document with id=" + id);
        }
        RepositoryInstance repoSession = null;
        try {
            repoSession = getRepositorySession();
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
            try {
                repoSession.removeDocument(docRef);
            } catch (ClientException ce) {
                String msg = "could not find document to delete with id=" + id;
                logger.error(msg, ce);
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
        return NuxeoConnector.getInstance().retrieveWorkspaceIds(domainName);
    }

    @Override
    public String createDomain(String domainName) throws Exception {
        RepositoryInstance repoSession = null;
        String domainId = null;
        try {
            repoSession = getRepositorySession();
            DocumentRef parentDocRef = new PathRef("/");
            DocumentModel parentDoc = repoSession.getDocument(parentDocRef);
            DocumentModel doc = repoSession.createDocumentModel(parentDoc.getPathAsString(),
                    domainName, "Domain");
            doc.setPropertyValue("dc:title", domainName);
            doc.setPropertyValue("dc:description", "A CollectionSpace domain "
                    + domainName);
            doc = repoSession.createDocument(doc);
            domainId = doc.getId();
            repoSession.save();
            if (logger.isDebugEnabled()) {
                logger.debug("created tenant domain name=" + domainName
                        + " id=" + domainId);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("createTenantSpace caught exception ", e);
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
        try {
            repoSession = getRepositorySession();
            DocumentRef docRef = new PathRef(
                    "/" + domainName);
            DocumentModel domain = repoSession.getDocument(docRef);
            domainId = domain.getId();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            //there is no way to identify if document does not exist due to
            //lack of typed exception for getDocument method
            return null;
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(repoSession);
            }
        }
        return domainId;
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
            DocumentRef parentDocRef = new PathRef(
                    "/" + domainName
                    + "/" + "workspaces");
            DocumentModel parentDoc = repoSession.getDocument(parentDocRef);
            DocumentModel doc = repoSession.createDocumentModel(parentDoc.getPathAsString(),
                    workspaceName, "Workspace");
            doc.setPropertyValue("dc:title", workspaceName);
            doc.setPropertyValue("dc:description", "A CollectionSpace workspace for "
                    + workspaceName);
            doc = repoSession.createDocument(doc);
            workspaceId = doc.getId();
            repoSession.save();
            if (logger.isDebugEnabled()) {
                logger.debug("created workspace name=" + workspaceName
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
                    + "/" + "workspaces"
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
     * Append a WHERE clause to the NXQL query.
     *
     * @param query         The NXQL query to which the WHERE clause will be appended.
     * @param querycontext  The query context, which provides the WHERE clause to append.
     */
    private final void appendNXQLWhere(StringBuilder query, QueryContext queryContext) {
        //
        // Restrict search to a specific Nuxeo domain
        // TODO This is a slow method for tenant-filter
        // We should make this a property that is indexed.
        //
//        query.append(" WHERE ecm:path STARTSWITH '/" + queryContext.domain + "'");

        //
        // Restrict search to the current tenant ID.  Is the domain path filter (above) still needed?
        //
        query.append(/*IQueryManager.SEARCH_QUALIFIER_AND +*/ " WHERE " + DocumentModelHandler.COLLECTIONSPACE_CORE_SCHEMA + ":"
                + DocumentModelHandler.COLLECTIONSPACE_CORE_TENANTID
                + " = " + queryContext.tenantId);
        //
        // Finally, append the incoming where clause
        //
        String whereClause = queryContext.whereClause;
        if (whereClause != null && ! whereClause.trim().isEmpty()) {
            // Due to an apparent bug/issue in how Nuxeo translates the NXQL query string
            // into SQL, we need to parenthesize our 'where' clause
            query.append(IQueryManager.SEARCH_QUALIFIER_AND + "(" + whereClause + ")");
        }
        //
        // Please lookup this use in Nuxeo support and document here
        //
        query.append(IQueryManager.SEARCH_QUALIFIER_AND + "ecm:isProxy = 0");
    }

    /**
     * Append an ORDER BY clause to the NXQL query.
     *
     * @param query         The NXQL query to which the ORDER BY clause will be appended.
     * @param querycontext  The query context, which provides the ORDER BY clause to append.
     */
    private final void appendNXQLOrderBy(StringBuilder query, QueryContext queryContext) {
        // Append the incoming ORDER BY clause
        String orderByClause = queryContext.orderByClause;
        if (orderByClause != null && ! orderByClause.trim().isEmpty()) {
            // FIXME Verify whether enclosing parentheses may be required, and add
            // them if so, as is being done in appendNXQLWhere.
            query.append(" ORDER BY ");
            query.append(orderByClause);
        }

        // FIXME Determine where and how to handle ASC[ending] and DESC[ending] qualifiers:
        //
        // Will these be included in the value of the relevant 'order by' query param?
        //
        // Will the format of the order by clause be verified, including placement of
        // the 'order by' qualifiers?
    }


    /**
     * Builds an NXQL SELECT query for a single document type.
     *
     * @param queryContext The query context
     * @return an NXQL query
     */
    private final String buildNXQLQuery(QueryContext queryContext) {
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(queryContext.docType);
        appendNXQLWhere(query, queryContext);
        appendNXQLOrderBy(query, queryContext);
        return query.toString();
    }

    /**
     * Builds an NXQL SELECT query across multiple document types.
     *
     * @param docTypes     a list of document types to be queried
     * @param queryContext the query context
     * @return an NXQL query
     */
    private final String buildNXQLQuery(List<String> docTypes, QueryContext queryContext) {
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        boolean fFirst = true;
        for (String docType : docTypes) {
            if (fFirst) {
                fFirst = false;
            } else {
                query.append(",");
            }
            query.append(docType);
        }
        appendNXQLWhere(query, queryContext);
        // FIXME add 'order by' clause here, if appropriate
        return query.toString();
    }

    /**
     * Gets the repository session.
     *
     * @return the repository session
     * @throws Exception the exception
     */
    private RepositoryInstance getRepositorySession() throws Exception {
        // FIXME: is it possible to reuse repository session?
        // Authentication failures happen while trying to reuse the session
    	Profiler profiler = new Profiler("getRepositorySession():", 2);
    	profiler.start();
        NuxeoClient client = NuxeoConnector.getInstance().getClient();
        RepositoryInstance repoSession = client.openRepository();
        if (logger.isTraceEnabled()) {
            logger.debug("getRepository() repository root: " + repoSession.getRootDocument());
        }
        profiler.stop();
        return repoSession;
    }

    /**
     * Release repository session.
     *
     * @param repoSession the repo session
     */
    private void releaseRepositorySession(RepositoryInstance repoSession) {
        try {
            NuxeoClient client = NuxeoConnector.getInstance().getClient();
            // release session
            client.releaseRepository(repoSession);
        } catch (Exception e) {
            logger.error("Could not close the repository session", e);
            // no need to throw this service specific exception
        }
    }
}
