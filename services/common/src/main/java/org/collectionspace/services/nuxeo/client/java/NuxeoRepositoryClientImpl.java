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
import java.sql.SQLException;
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
import javax.ws.rs.core.MultivaluedMap;

//
// CSPACE-5036 - How to make CMISQL queries from Nuxeo
//
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.index.IndexClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.PreparedStatementBuilder;
import org.collectionspace.services.common.storage.PreparedStatementSimpleBuilder;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.util.CSReindexFulltextRoot;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.opencmis.bindings.NuxeoCmisServiceFactory;
import org.nuxeo.ecm.core.opencmis.impl.server.NuxeoCmisService;
import org.nuxeo.elasticsearch.ElasticSearchComponent;
import org.nuxeo.elasticsearch.api.ESClient;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepositoryClientImpl is used to perform CRUD operations on documents in Nuxeo
 * repository using Remote Java APIs. It uses
 *
 * @see DocumentHandler as IOHandler with the client.
 *
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class NuxeoRepositoryClientImpl implements RepositoryClient<PoxPayloadIn, PoxPayloadOut> {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(NuxeoRepositoryClientImpl.class);
//    private final Logger profilerLogger = LoggerFactory.getLogger("remperf");
//    private String foo = Profiler.createLogger();
    public static final String NUXEO_CORE_TYPE_DOMAIN = "Domain";
    public static final String NUXEO_CORE_TYPE_WORKSPACEROOT = "WorkspaceRoot";
    // FIXME: Get this value from an existing constant, if available
    public static final String BACKSLASH = "\\";
    public static final String USER_SUPPLIED_WILDCARD = "*";
    public static final String USER_SUPPLIED_WILDCARD_REGEX = BACKSLASH + USER_SUPPLIED_WILDCARD;
    public static final String USER_SUPPLIED_ANCHOR_CHAR = "^";
    public static final String USER_SUPPLIED_ANCHOR_CHAR_REGEX = BACKSLASH + USER_SUPPLIED_ANCHOR_CHAR;
    public static final String ENDING_ANCHOR_CHAR = "$";
    public static final String ENDING_ANCHOR_CHAR_REGEX = BACKSLASH + ENDING_ANCHOR_CHAR;


    /**
     * Instantiates a new repository java client impl.
     */
    public NuxeoRepositoryClientImpl() {
        //Empty constructor
    }

    public void assertWorkflowState(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, DocumentModel docModel) throws DocumentNotFoundException, ClientException {
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        if (queryParams != null) {
            //
            // Look for the workflow "delete" query param and see if we need to assert that the
            // docModel is in a non-deleted workflow state.
            //
            String currentState = docModel.getCurrentLifeCycleState();
            String includeDeletedStr = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
            boolean includeDeleted = (includeDeletedStr == null) ? true : Boolean.parseBoolean(includeDeletedStr);
            if (includeDeleted == false) {
                //
                // We don't wanted soft-deleted objects, so throw an exception if this one is soft-deleted.
                //
                if (currentState.contains(WorkflowClient.WORKFLOWSTATE_DELETED)) {
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

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.CREATE);
            repoSession = getRepositorySession(ctx);
            DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
            DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
            String wspacePath = wspaceDoc.getPathAsString();
            //give our own ID so PathRef could be constructed later on
            String id = IdUtils.generateId(UUID.randomUUID().toString(), "-", true, 24);
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
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw bre;
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
        	if (logger.isDebugEnabled()) {
        		logger.debug("Call to low-level Nuxeo document create call failed: ", e);
        	}
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

	@Override
    public boolean reindex(DocumentHandler handler, String indexid) throws DocumentNotFoundException, DocumentException
    {
    	return reindex(handler, null, indexid);
    }

    @Override
    public boolean reindex(DocumentHandler handler, String csid, String indexid) throws DocumentNotFoundException, DocumentException
    {
    	boolean result = true;

    	switch (indexid) {
		case IndexClient.FULLTEXT_ID:
			result = reindexFulltext(handler, csid, indexid);
			break;
		case IndexClient.ELASTICSEARCH_ID:
			result = reindexElasticsearch(handler, csid, indexid);
			break;
		default:
			throw new NuxeoDocumentException(String.format("Unknown index '%s'.  Reindex request failed.",
					indexid));
    	}

    	return result;
    }

    /**
     * Reindex Nuxeo's fulltext index.
     *
     * @param handler
     * @param csid
     * @param indexid
     * @return
     * @throws NuxeoDocumentException
     * @throws TransactionException
     */
    private boolean reindexFulltext(DocumentHandler handler, String csid, String indexid) throws NuxeoDocumentException, TransactionException {
    	boolean result = true;
        CoreSessionInterface repoSession = null;
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = handler.getServiceContext();

        try {
            String queryString = handler.getDocumentsToIndexQuery(indexid, csid);
            repoSession = getRepositorySession(ctx);
            CSReindexFulltextRoot indexer = new CSReindexFulltextRoot(repoSession);
            indexer.reindexFulltext(0, 0, queryString);
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception trying to reindex Nuxeo repository ", e);
            }
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

    	return result;
    }

    /**
     * Reindex Nuxeo's Elasticsearch index.
     *
     * @param handler
     * @param csid
     * @param indexid
     * @return
     * @throws NuxeoDocumentException
     * @throws TransactionException
     */
    private boolean reindexElasticsearch(DocumentHandler handler, String csid, String indexid) throws NuxeoDocumentException, TransactionException {
        boolean result = false;

        if (!Framework.isBooleanPropertyTrue("elasticsearch.enabled")) {
            throw new NuxeoDocumentException("Request to reindex Elasticsearch failed because Elasticsearch is not enabled.");
        }

        CoreSessionInterface repoSession = null;
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = handler.getServiceContext();
        ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);
        ESClient esClient = null;

        try {
            // Ensure an Elasticsearch connection has been established. This should have happened
            // on startup, but may not have, if the Elasticsearch service wasn't reachable when
            // Nuxeo started.

            esClient = es.getClient();
        } catch (Exception e) {
            esClient = null;
        }

        try {
            repoSession = getRepositorySession(ctx);

            if (esClient == null) {
                // The connection to ES has not been established.
                // Attempt to start the ElasticSearchService.

                es.start(null);

                try {
                    // Wait for startup requests to complete.

                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                }
            }

            String repositoryName = repoSession.getRepositoryName();

            logger.info(String.format("Rebuilding Elasticsearch index for repository %s", repositoryName));

            es.dropAndInitRepositoryIndex(repositoryName);

            TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
            TenantBindingType tenantBinding = tReader.getTenantBinding(ctx.getTenantId());

            for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
                Boolean isElasticsearchIndexed = serviceBinding.isElasticsearchIndexed();
                String servicesRepoDomainName = serviceBinding.getRepositoryDomain();

                if (isElasticsearchIndexed && servicesRepoDomainName != null && servicesRepoDomainName.trim().isEmpty() == false) {
                    String docType = NuxeoUtils.getTenantQualifiedDocType(tenantBinding.getId(), serviceBinding.getObject().getName());
                    String queryString = handler.getDocumentsToIndexQuery(indexid, docType, csid);

                    logger.info(String.format("Starting Elasticsearch reindexing for docType %s in repository %s", docType, repositoryName));
                    logger.debug(queryString);

                    es.runReindexingWorker(repositoryName, queryString);
                }
            }

            result = true;
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }

            logger.error("Error reindexing Nuxeo repository", e);

            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

        return result;
    }

    @Override
    public boolean synchronize(ServiceContext ctx, Object specifier, DocumentHandler handler)
            throws DocumentNotFoundException, TransactionException, DocumentException {
    	boolean result = false;

        if (handler == null) {
            throw new IllegalArgumentException("RepositoryJavaClient.get: handler is missing");
        }

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.SYNC);
            repoSession = getRepositorySession(ctx);
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(specifier);
            result = handler.handle(Action.SYNC, wrapDoc);
            handler.complete(Action.SYNC, wrapDoc);
        } catch (IllegalArgumentException iae) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw iae;
        } catch (DocumentException de) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw de;
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

        return result;
    }

    @Override
    public boolean synchronizeItem(ServiceContext ctx, AuthorityItemSpecifier itemSpecifier, DocumentHandler handler)
            throws DocumentNotFoundException, TransactionException, DocumentException {
    	boolean result = false;

        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.get: handler is missing");
        }

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.SYNC);
            repoSession = getRepositorySession(ctx);
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<AuthorityItemSpecifier> wrapDoc = new DocumentWrapperImpl<AuthorityItemSpecifier>(itemSpecifier);
            result = handler.handle(Action.SYNC, wrapDoc);
            handler.complete(Action.SYNC, wrapDoc);
        } catch (IllegalArgumentException iae) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw iae;
        } catch (DocumentException de) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw de;
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

        return result;
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

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.GET);
            repoSession = getRepositorySession(ctx);
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
            DocumentModel docModel = null;
            try {
                docModel = repoSession.getDocument(docRef);
                assertWorkflowState(ctx, docModel);
            } catch (org.nuxeo.ecm.core.api.DocumentNotFoundException ce) {
                String msg = logException(ce,
                		String.format("Could not find %s resource/record with CSID=%s", ctx.getDocumentType(), id));
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
        	if (logger.isDebugEnabled()) {
        		logger.debug(de.getMessage(), de);
        	}
            throw de;
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new NuxeoDocumentException(e);
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
        CoreSessionInterface repoSession = null;

        try {
            handler.prepare(Action.GET);
            repoSession = getRepositorySession(ctx);

            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            String query = NuxeoUtils.buildNXQLQuery(queryContext);
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
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }
    }

    public DocumentWrapper<DocumentModel> getDoc(
    		CoreSessionInterface repoSession,
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
    	CoreSessionInterface repoSession = null;
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
            throw new NuxeoDocumentException(e);
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
    		CoreSessionInterface repoSession,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String whereClause)
            throws DocumentNotFoundException, DocumentException {
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            QueryContext queryContext = new QueryContext(ctx, whereClause);
            DocumentModelList docList = null;
            // force limit to 1, and ignore totalSize
            String query = NuxeoUtils.buildNXQLQuery(queryContext);
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
            throw new NuxeoDocumentException(e);
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
    	CoreSessionInterface repoSession = null;
        DocumentWrapper<DocumentModel> wrapDoc = null;

        try {
            repoSession = getRepositorySession(ctx);
            wrapDoc = findDoc(repoSession, ctx, whereClause);
        } catch (DocumentNotFoundException dnfe) {
        	throw dnfe;
        } catch (DocumentException de) {
        	throw de;
        } catch (Exception e) {
        	if (repoSession == null) {
        		throw new NuxeoDocumentException("Unable to create a Nuxeo repository session.", e);
        	} else {
        		throw new NuxeoDocumentException("Unexpected Nuxeo exception.", e);
        	}
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
    public String findDocCSID(CoreSessionInterface repoSession,
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
            throw new NuxeoDocumentException(e);
        } finally {
            if (releaseSession && (repoSession != null)) {
                this.releaseRepositorySession(ctx, repoSession);
            }
        }
        return csid;
    }

    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            CoreSessionInterface repoSession,
            List<String> docTypes,
            String whereClause,
            String orderByClause,
            int pageNum,
            int pageSize,
            boolean useDefaultOrderByClause,
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
            String query = NuxeoUtils.buildNXQLQuery(docTypes, queryContext, useDefaultOrderByClause);
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
            throw new NuxeoDocumentException(e);
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
            CoreSessionInterface repoSession,
            List<String> docTypes) throws DocumentNotFoundException, DocumentException {
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
                // TODO: If there is an existing SEARCH_RELATED_MATCH_OBJ_DOCTYPES parameter,
                // filter it using the values in inList, instead of adding another value.
                // Just adding inList as another parameter value doesn't do anything to a parameter
                // that was passed in, and that unfiltered list ends up being used for the search.
                if (isSubjectOrObjectQuery(ctx)) {
                	docList = getFilteredCMISForSubjectOrObject(repoSession, ctx, handler, queryContext);
                } else {
                	docList = getFilteredCMIS(repoSession, ctx, handler, queryContext);
                }
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
            throw new NuxeoDocumentException(e);
        }

        return wrapDoc;
    }

    private DocumentModelList getFilteredCMISForSubjectOrObject(CoreSessionInterface repoSession,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, DocumentHandler handler, QueryContext queryContext) throws DocumentNotFoundException, DocumentException {
    	DocumentModelList result = null;

    	if (isSubjectOrObjectQuery(ctx) == true) {
    		MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        	String asEitherCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_EITHER);

    		queryParams.remove(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT);
    		queryParams.remove(IQueryManager.SEARCH_RELATED_TO_CSID_AS_OBJECT);

        	//
        	// First query for subjectCsid results.
        	//
    		queryParams.addFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT, asEitherCsid);
            DocumentModelList subjectDocList = getFilteredCMIS(repoSession, ctx, handler, queryContext);
            queryParams.remove(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT);

        	//
        	// Next query for objectCsid results.
        	//
	    	queryParams.addFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_OBJECT, asEitherCsid);
            DocumentModelList objectDocList = getFilteredCMIS(repoSession, ctx, handler, queryContext);
            queryParams.remove(IQueryManager.SEARCH_RELATED_TO_CSID_AS_OBJECT);

            //
            // Finally, combine the two results
            //
            result = mergeDocumentModelLists(subjectDocList, objectDocList);
    	}

		return result;
	}

	private DocumentModelList mergeDocumentModelLists(DocumentModelList subjectDocList,
			DocumentModelList objectDocList) {
		DocumentModelList result = null;

		if (subjectDocList == null || subjectDocList.isEmpty()) {
			return objectDocList;
		}

		if (objectDocList == null || objectDocList.isEmpty()) {
			return subjectDocList;
		}

        result = new DocumentModelListImpl();

        // Add the subject list
		Iterator<DocumentModel> iterator = subjectDocList.iterator();
		while (iterator.hasNext()) {
			DocumentModel dm = iterator.next();
			addToResults(result, dm);
		}

		// Add the object list
		iterator = objectDocList.iterator();
		while (iterator.hasNext()) {
			DocumentModel dm = iterator.next();
			addToResults(result, dm);
		}

		// Set the 'totalSize' value for book keeping sake
		((DocumentModelListImpl) result).setTotalSize(result.size());

		return result;
	}

	//
	// Only add if it is not already in the list
	private void addToResults(DocumentModelList result, DocumentModel dm) {
		Iterator<DocumentModel> iterator = result.iterator();
		boolean found = false;

		while (iterator.hasNext()) {
			DocumentModel existingDm = iterator.next();
			if (existingDm.getId().equals(dm.getId())) {
				found = true;
				break;
			}
		}

		if (found == false) {
			result.add(dm);
		}
	}

	private boolean isSubjectOrObjectQuery(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
    	String asEitherCsid = (String)queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_AS_EITHER);
    	return asEitherCsid != null && !asEitherCsid.isEmpty();
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
            int pageNum,
            int pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws DocumentNotFoundException, TransactionException, DocumentException {
    	CoreSessionInterface repoSession = null;
        DocumentWrapper<DocumentModelList> wrapDoc = null;

        try {
            repoSession = getRepositorySession(ctx);
            wrapDoc = findDocs(ctx,
                    repoSession,
                    docTypes,
                    whereClause,
                    null,
                    pageNum,
                    pageSize,
                    useDefaultOrderByClause,
                    computeTotal);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new NuxeoDocumentException(e);
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

        CoreSessionInterface repoSession = null;
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
            throw new NuxeoDocumentException(e);
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

        CoreSessionInterface repoSession = null;
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
            throw new NuxeoDocumentException(e);
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
            CoreSessionInterface repoSession,
            String csid)
            throws Exception {
        DocumentWrapper<DocumentModel> result = null;

        result = new DocumentWrapperImpl<DocumentModel>(NuxeoUtils.getDocFromCsid(ctx, repoSession, csid));

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
        CoreSessionInterface repoSession = null;
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
    private IterableQueryResult makeCMISQLQuery(CoreSessionInterface repoSession, String query, QueryContext queryContext) throws DocumentException {
        IterableQueryResult result = null;
        /** Threshold over which temporary files are not kept in memory. */
        final int THRESHOLD = 1024 * 1024;

        try {
            logger.debug(String.format("Performing a CMIS query on Nuxeo repository named %s",
            		repoSession.getRepositoryName()));

            ThresholdOutputStreamFactory streamFactory = ThresholdOutputStreamFactory.newInstance(
                    null, THRESHOLD, -1, false);
            CallContextImpl callContext = new CallContextImpl(
                    CallContext.BINDING_LOCAL,
                    CmisVersion.CMIS_1_1,
                    repoSession.getRepositoryName(),
                    null, // ServletContext
                    null, // HttpServletRequest
                    null, // HttpServletResponse
                    new NuxeoCmisServiceFactory(),
                    streamFactory);
            callContext.put(CallContext.USERNAME, repoSession.getPrincipal().getName());

            NuxeoCmisService cmisService = new NuxeoCmisService(repoSession.getCoreSession());
            result = repoSession.queryAndFetch(query, "CMISQL", cmisService);
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            logger.error("Encounter trouble making the following CMIS query: " + query, e);
            throw new NuxeoDocumentException(e);
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

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession(ctx); //Keeps a refcount here for the repository session so you need to release this when finished

            DocumentModelList docList = null;
            // JDBC query
            if (handler.isJDBCQuery() == true) {
                docList = getFilteredJDBC(repoSession, ctx, handler);
            // CMIS query
            } else if (handler.isCMISQuery() == true) { //FIXME: REM - Need to deal with paging info in CMIS query
                if (isSubjectOrObjectQuery(ctx)) {
                	docList = getFilteredCMISForSubjectOrObject(repoSession, ctx, handler, queryContext);
                } else {
                    docList = getFilteredCMIS(repoSession, ctx, handler, queryContext);
                }
            // NXQL query
            } else {
                String query = NuxeoUtils.buildNXQLQuery(queryContext);
                if (logger.isDebugEnabled()) {
                    logger.debug("Executing NXQL query: " + query.toString());
                }
                Profiler profiler = new Profiler(this, 2);
                profiler.log("Executing NXQL query: " + query.toString());
                profiler.start();
                // If we have a page size and/or offset, then reflect those values
                // when constructing the query, and also pass 'true' to get totalSize
                // in the returned DocumentModelList.
                if ((queryContext.getDocFilter().getOffset() > 0) || (queryContext.getDocFilter().getPageSize() > 0)) {
                    docList = repoSession.query(query, null,
                            queryContext.getDocFilter().getPageSize(), queryContext.getDocFilter().getOffset(), true);
                } else {
                    docList = repoSession.query(query);
                }
                profiler.stop();
            }

            //set repoSession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentWrapper<DocumentModelList> wrapDoc = new DocumentWrapperImpl<DocumentModelList>(docList);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e); // REM - 1/17/2014: Check for org.nuxeo.ecm.core.api.ClientException and re-attempt
            }
            throw new NuxeoDocumentException(e);
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
     * currently dedicated to a specific task: that of improving performance
     * for partial term matching queries on authority items / terms, via
     * the use of a hand-tuned SQL query, rather than via the generated SQL
     * produced by Nuxeo from an NXQL query.  (See CSPACE-6361 for a task
     * to generalize this method.)
     *
     * @param repoSession a repository session.
     * @param ctx the service context.
     * @param handler a relevant document handler.
     * @return a list of document models matching the search criteria.
     * @throws Exception
     */
    private DocumentModelList getFilteredJDBC(CoreSessionInterface repoSession, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            DocumentHandler handler) throws Exception {
        DocumentModelList result = new DocumentModelListImpl();

        // FIXME: Get all of the following values from appropriate external constants.
        //
        // At present, the two constants below are duplicated in both RepositoryClientImpl
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
        // Value for replaceable parameter 1 in the query
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

        // After building the individual parts of the query, set the values
        // of replaceable parameters that will be inserted into that query
        // and optionally add restrictions

        List<String> params = new ArrayList<>();

        if (Tools.notBlank(whereClause)) {

            // Read tenant bindings configuration to determine whether
            // to automatically insert leading, as well as trailing, wildcards
            // into the term matching string.
            String usesStartingWildcard = TenantBindingUtils.getPropertyValue(tenantBinding,
                    IQueryManager.TENANT_USES_STARTING_WILDCARD_FOR_PARTIAL_TERM);
            // Handle user-provided leading wildcard characters, in the
            // configuration where a leading wildcard is not automatically inserted.
            // (The user-provided wildcard must be in the first, or "starting"
            // character position in the partial term value.)
            if (Tools.notBlank(usesStartingWildcard)) {
                if (usesStartingWildcard.equalsIgnoreCase(Boolean.FALSE.toString())) {
                    partialTerm = handleProvidedStartingWildcard(partialTerm);
                    // Otherwise, in the configuration where a leading wildcard
                    // is usually automatically inserted, handle the cases where
                    // a user has entered an anchor character in the first position
                    // in the starting term value. In those cases, strip that
                    // anchor character and don't add a leading wildcard
                } else {
                    if (partialTerm.startsWith(USER_SUPPLIED_ANCHOR_CHAR)) {
                        partialTerm = partialTerm.substring(1, partialTerm.length());
                        // Otherwise, automatically add a leading wildcard
                    } else {
                        partialTerm = JDBCTools.SQL_WILDCARD + partialTerm;
                    }
                }
            }
            // Add SQL wildcards in the midst of the partial term match search
            // expression, whever user-supplied wildcards appear, except in the
            // first or last character positions of the search expression.
            partialTerm = subtituteWildcardsInPartialTerm(partialTerm);

            // If a designated 'anchor character' is present as the last character
            // in the search expression, strip that character and don't add
            // a trailing wildcard
            int lastCharPos = partialTerm.length() - 1;
            if (partialTerm.endsWith(USER_SUPPLIED_ANCHOR_CHAR) && lastCharPos > 0) {
                    partialTerm = partialTerm.substring(0, lastCharPos);
            } else {
                // Otherwise, automatically add a trailing wildcard
                partialTerm = partialTerm + JDBCTools.SQL_WILDCARD;
            }
            params.add(partialTerm);
        }

        // Optionally add restrictions to the default query, based on variables
        // in the current request

        // Restrict the query to filter out deleted records, if requested
        String includeDeleted = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        if (includeDeleted != null && includeDeleted.equalsIgnoreCase(Boolean.FALSE.toString())) {
            whereClause = whereClause
                    + "  AND (misc.lifecyclestate <> '" + WorkflowClient.WORKFLOWSTATE_DELETED + "')"
                    + "  AND (misc.lifecyclestate <> '" + WorkflowClient.WORKFLOWSTATE_LOCKED_DELETED + "')"
                    + "  AND (misc.lifecyclestate <> '" + WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED + "')";
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
        	String cspaceInstanceId = ServiceMain.getInstance().getCspaceInstanceId();
            List<CachedRowSet> resultsList = JDBCTools.executePreparedQueries(builders,
                dataSourceName, repositoryName, cspaceInstanceId, EXECUTE_WITHIN_TRANSACTION);

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

        // Get a list of document models, using the list of IDs obtained from the query
        //
        // FIXME: Check whether we have a 'get document models from list of CSIDs'
        // utility method like this, and if not, add this to the appropriate
        // framework class
        DocumentModel docModel;
        for (String docId : docIds) {
            docModel = NuxeoUtils.getDocumentModel(repoSession, docId);
            if (docModel == null) {
                logger.warn("Could not obtain document model for document with ID " + docId);
            } else {
                result.add(docModel);
            }
        }

        // Order the results
        final String COMMON_PART_SCHEMA = handler.getServiceContext().getCommonPartLabel();
        final String DISPLAY_NAME_XPATH =
                "//" + handler.getJDBCQueryParams().get(TERM_GROUP_LIST_NAME) + "/[0]/termDisplayName";
        Collections.sort(result, new Comparator<DocumentModel>() {
            @Override
            public int compare(DocumentModel doc1, DocumentModel doc2) {
            	String termDisplayName1 = null;
            	String termDisplayName2 = null;
            	try {
	                termDisplayName1 = (String) NuxeoUtils.getXPathValue(doc1, COMMON_PART_SCHEMA, DISPLAY_NAME_XPATH);
	                termDisplayName2 = (String) NuxeoUtils.getXPathValue(doc2, COMMON_PART_SCHEMA, DISPLAY_NAME_XPATH);
            	} catch (NuxeoDocumentException e) {
            		throw new RuntimeException(e);  // We need to throw a RuntimeException because the compare() method of the Comparator interface does not support throwing an Exception
            	}
                return termDisplayName1.compareToIgnoreCase(termDisplayName2);
            }
        });

        return result;
    }


    private DocumentModelList getFilteredCMIS(CoreSessionInterface repoSession,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, DocumentHandler handler, QueryContext queryContext)
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
            throw new NuxeoDocumentException(e);
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

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.UPDATE);
            repoSession = getRepositorySession(ctx);
            DocumentRef docRef = NuxeoUtils.createPathRef(ctx, csid);
            DocumentModel doc = null;
            try {
                doc = repoSession.getDocument(docRef);
            } catch (org.nuxeo.ecm.core.api.DocumentNotFoundException ce) {
                String msg = logException(ce,
                		String.format("Could not find %s resource/record to update with CSID=%s", ctx.getDocumentType(), csid));
                throw new DocumentNotFoundException(msg, ce);
            }
            // Check for a versioned document, and check In and Out before we proceed.
            if (((DocumentModelHandler) handler).supportsVersioning()) {
                /* Once we advance to 5.5 or later, we can add this.
                 * See also https://jira.nuxeo.com/browse/NXP-8506
                 if(!doc.isVersionable()) {
                 throw new NuxeoDocumentException("Configuration for: "
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
            // Refresh the doc after save, in case a documentModified event handler has modified
            // the document post-save. We want those changes to be reflected in the returned document.
            doc.refresh();
            handler.complete(Action.UPDATE, wrapDoc);
        } catch (BadRequestException bre) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw bre;
        } catch (DocumentException de) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }

            throw de;
        } catch (CSWebApplicationException wae) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw wae;
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw new NuxeoDocumentException(e);
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
     * @param fSaveSession if TRUE, will call CoreSessionInterface.save() to save
     * accumulated changes.
     * @throws ClientException
     * @throws DocumentException
     */
	@Deprecated
    public void saveDocWithoutHandlerProcessing(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            CoreSessionInterface repoSession,
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
            throw new NuxeoDocumentException(e);
        }
    }

    /**
     * Save a list of documentModels to the Nuxeo repository.
     *
     * @param ctx service context under which this method is invoked
     * @param repoSession a repository session
     * @param docModelList a list of document models
     * @param fSaveSession if TRUE, will call CoreSessionInterface.save() to save
     * accumulated changes.
     * @throws ClientException
     * @throws DocumentException
     */
    public void saveDocListWithoutHandlerProcessing(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            CoreSessionInterface repoSession,
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
            throw new NuxeoDocumentException(e);
        }
    }

    @Override
	public void deleteWithWhereClause(@SuppressWarnings("rawtypes") ServiceContext ctx, String whereClause,
			@SuppressWarnings("rawtypes") DocumentHandler handler) throws
			DocumentNotFoundException, DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, specifier): ctx is missing");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting document with whereClause=" + whereClause);
        }

        DocumentWrapper<DocumentModel> foundDocWrapper = this.findDoc(ctx, whereClause);
        if (foundDocWrapper != null) {
        	DocumentModel docModel = foundDocWrapper.getWrappedObject();
        	String csid = docModel.getName();
        	this.delete(ctx, csid, handler);
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
    public boolean delete(ServiceContext ctx, List<String> idList, DocumentHandler handler) throws DocumentNotFoundException,
            DocumentException, TransactionException {
    	boolean result = true;

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, ix, handler): ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, ix, handler): handler is missing");
        }

        CoreSessionInterface repoSession = null;
        try {
            handler.prepare(Action.DELETE);
            repoSession = getRepositorySession(ctx);

            for (String id : idList) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleting document with CSID=" + id);
                }
	            DocumentWrapper<DocumentModel> wrapDoc = null;
	            try {
	                DocumentRef docRef = NuxeoUtils.createPathRef(ctx, id);
	                wrapDoc = new DocumentWrapperImpl<DocumentModel>(repoSession.getDocument(docRef));
	                ((DocumentModelHandler) handler).setRepositorySession(repoSession);
	                if (handler.handle(Action.DELETE, wrapDoc) == true) {
	                	repoSession.removeDocument(docRef);
	                	if (logger.isDebugEnabled()) {
	                		String msg = String.format("DELETE - User '%s' hard-deleted document CSID=%s of type %s.",
	                				ctx.getUserId(), id, ctx.getDocumentType());
	                		logger.debug(msg);
	                	}
	                } else {
                		String msg = String.format("Could not delete %s resource with csid=%s.",
                				handler.getServiceContext().getServiceName(), id);
                		throw new DocumentException(msg);
	                }
	            } catch (org.nuxeo.ecm.core.api.DocumentNotFoundException ce) {
	                String msg = logException(ce,
	                		String.format("Could not find %s resource/record to delete with CSID=%s", ctx.getDocumentType(), id));
	                throw new DocumentNotFoundException(msg, ce);
	            }
	            repoSession.save();
	            handler.complete(Action.DELETE, wrapDoc);
            }
        } catch (DocumentException de) {
            if (ctx.isRollbackOnException()) {
        	    rollbackTransaction(repoSession);
            }
            throw de;
        } catch (Throwable e) {
            if (ctx.isRollbackOnException()) {
                rollbackTransaction(repoSession);
            }
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(ctx, repoSession);
            }
        }

        return result;
    }

    /**
     * delete a document from the Nuxeo repository
     *
     * @param ctx service context under which this method is invoked
     * @param id of the document
     * @throws DocumentException
     */
    @Override
    public boolean delete(ServiceContext ctx, String id, DocumentHandler handler) throws DocumentNotFoundException,
            DocumentException, TransactionException {
    	boolean result;

    	List<String> idList = new ArrayList<String>();
    	idList.add(id);
    	result = delete(ctx, idList, handler);

        return result;
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
    	CoreSessionInterface repoSession = null;
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
        } catch (Throwable e) {
            rollbackTransaction(repoSession);
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
        CoreSessionInterface repoSession = null;

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
    private DocumentModel getWorkspacesRoot(CoreSessionInterface repoSession,
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
    	CoreSessionInterface repoSession = null;
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
        } catch (Throwable e) {
            rollbackTransaction(repoSession);
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

        CoreSessionInterface repoSession = null;
        try {
            repoSession = getRepositorySession((ServiceContext<PoxPayloadIn, PoxPayloadOut>) null);
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
            throw new NuxeoDocumentException(e);
        } finally {
            if (repoSession != null) {
                releaseRepositorySession(null, repoSession);
            }
        }

        return workspaceId;
    }

    @Override
    public CoreSessionInterface getRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
        return getRepositorySession(ctx, ctx.getRepositoryName(), ctx.getTimeoutSecs());
    }

    public CoreSessionInterface getRepositorySession(String repoName) throws Exception {
        return getRepositorySession(null, repoName, ServiceContext.DEFAULT_TX_TIMEOUT);
    }

    /**
     * Gets the repository session. - Package access only. If the 'ctx' param is
     * null then the repo name must be non-mull and vice-versa
     *
     * @return the repository session
     * @throws Exception the exception
     */
    public CoreSessionInterface getRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String repoName,
    		int timeoutSeconds) throws Exception {
    	CoreSessionInterface repoSession = null;

        Profiler profiler = new Profiler("getRepositorySession():", 2);
        profiler.start();
        //
        // To get a connection to the Nuxeo repo, we need either a valid ServiceContext instance or a repository name
        //
        if (ctx != null) {
        	repoSession = (CoreSessionInterface) ctx.getCurrentRepositorySession(); // First see if the context already has a repo session
        	if (repoSession == null) {
	            repoName = ctx.getRepositoryName(); // Notice we are overriding the passed in 'repoName' since we have a valid service context passed in to us
        	}
        } else if (Tools.isBlank(repoName)) {
            String errMsg = String.format("Either a valid session context or repository name are required to get a new connection.");
            logger.error(errMsg);
            throw new Exception(errMsg);
        }

        if (repoSession == null) {
            //
            // If we couldn't find a repoSession from the service context (or the context was null) then we need to create a new one using
            // just the repository name.
            //
            NuxeoClientEmbedded client = NuxeoConnectorEmbedded.getInstance().getClient();
            repoSession = client.openRepository(repoName, timeoutSeconds);
        } else {
            if (logger.isTraceEnabled() == true) {
                logger.trace("Reusing the current context's repository session.");
            }
        }
        //
        // Debugging only code
        //
		if (logger.isTraceEnabled()) {
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("Testing call to getRepository() repository root: " + repoSession.getRootDocument());
				}
			} catch (Throwable e) {
				logger.trace("Test call to Nuxeo's getRepository() repository root failed", e);
			}
		}

        profiler.stop();

        if (ctx != null) {
            ctx.setCurrentRepositorySession(repoSession); // For reusing, save the repository session in the current service context.  The context will reference count it.
        }

        return repoSession;
    }

    /**
     * Release repository session. - Package access only.
     *
     * @param repoSession the repo session
     */
    @Override
    public void releaseRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, Object repositorySession) throws TransactionException {
        try {
        	CoreSessionInterface repoSession = (CoreSessionInterface)repositorySession;
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
        	String causeMsg = null;
        	Throwable cause = tre.getCause();
        	if (cause != null) {
        		causeMsg = cause.getMessage();
        	}

            TransactionException te; // a CollectionSpace specific tx exception
            if (causeMsg != null) {
            	te = new TransactionException(causeMsg, tre);
            } else {
            	te = new TransactionException(tre);
            }

            logger.error(te.getMessage(), tre); // Log the standard transaction exception message, plus an exception-specific stack trace
            throw te;
        } catch (Exception e) {
            logger.error("Could not close the repository session.", e);
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
            if (partialTerm.substring(0, 1).equals(USER_SUPPLIED_WILDCARD)) {
                StringBuffer buffer = new StringBuffer(partialTerm);
                buffer.setCharAt(0, JDBCTools.SQL_WILDCARD.charAt(0));
                partialTerm = buffer.toString();
            }
        }
        return partialTerm;
    }

    /**
     * Replaces user-supplied wildcards with SQL wildcards, in a partial term
     * matching search expression.
     *
     * The scope of this replacement excludes the beginning character
     * in that search expression, as that character is treated specially.
     *
     * @param partialTerm
     * @return the partial term, with any user-supplied wildcards replaced
     * by SQL wildcards.
     */
    private String subtituteWildcardsInPartialTerm(String partialTerm) {
        if (Tools.isBlank(partialTerm)) {
            return partialTerm;
        }
        if (! partialTerm.contains(USER_SUPPLIED_WILDCARD)) {
            return partialTerm;
        }
        int len = partialTerm.length();
        // Partial term search expressions of 2 or fewer characters
        // currently aren't amenable to the use of wildcards
        if (len <= 2)  {
            logger.warn("Partial term match search expression of just 1-2 characters in length contains a user-supplied wildcard: " + partialTerm);
            logger.warn("Will handle that character as a literal value, rather than as a wildcard ...");
            return partialTerm;
        }
        return partialTerm.substring(0, 1) // first char
                + partialTerm.substring(1, len).replaceAll(USER_SUPPLIED_WILDCARD_REGEX, JDBCTools.SQL_WILDCARD);

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
    private boolean restrictJDBCQueryByTenantID(TenantBindingType tenantBinding, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
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

    private void rollbackTransaction(CoreSessionInterface repoSession) {
    	if (repoSession != null) {
    		repoSession.setTransactionRollbackOnly();
    	}
    }

    /**
     * Should never get called.
     */
	@Override
	public boolean delete(ServiceContext ctx, Object entityFound, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
		throw new UnsupportedOperationException();
	}
}
