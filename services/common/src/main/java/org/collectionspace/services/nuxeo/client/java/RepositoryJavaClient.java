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

import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.DocumentException;
import org.collectionspace.services.common.repository.DocumentHandler.Action;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
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
public class RepositoryJavaClient implements RepositoryClient {

    private final Logger logger = LoggerFactory.getLogger(RepositoryJavaClient.class);

    public RepositoryJavaClient() {
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

        if(handler.getDocumentType() == null){
            throw new IllegalArgumentException(
                    "RemoteRepositoryClient.create: docType is missing");
        }
        if(handler == null){
            throw new IllegalArgumentException(
                    "RemoteRepositoryClient.create: handler is missing");
        }
        String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
        if(nuxeoWspaceId == null){
            throw new DocumentNotFoundException(
                    "Unable to find workspace for service " + ctx.getServiceName() +
                    " check if the mapping exists in service-config.xml or" +
                    " the the mapped workspace exists in the Nuxeo repository");
        }
        RepositoryInstance repoSession = null;
        try{
            handler.prepare(Action.CREATE);
            repoSession = getRepositorySession();
            DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
            DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
            String wspacePath = wspaceDoc.getPathAsString();
            String id = IdUtils.generateId("New " + handler.getDocumentType());
            // create document model
            DocumentModel doc = repoSession.createDocumentModel(wspacePath, id,
                    handler.getDocumentType());
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentModelWrapper wrapDoc = new DocumentModelWrapper(doc);
            handler.handle(Action.CREATE, wrapDoc);
            // create document with documentmodel
            doc = repoSession.createDocument(doc);
            repoSession.save();
            handler.complete(Action.CREATE, wrapDoc);
            return doc.getId();
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
            if(repoSession != null){
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

        if(handler == null){
            throw new IllegalArgumentException(
                    "RemoteRepositoryClient.get: handler is missing");
        }
        RepositoryInstance repoSession = null;

        try{
            handler.prepare(Action.GET);
            repoSession = getRepositorySession();
            //FIXME, there is a potential privacy violation here, one tenant could
            //retrieve doc id of another tenant and could retrieve the document
            //PathRef does not seem to come to rescue as expected. Needs more thoughts.
            DocumentRef docRef = new IdRef(id);
            DocumentModel doc = null;
            try{
                doc = repoSession.getDocument(docRef);
            }catch(ClientException ce){
                String msg = "could not find document with id=" + id;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
            }
            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentModelWrapper wrapDoc = new DocumentModelWrapper(doc);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
        }catch(IllegalArgumentException iae){
            throw iae;
        }catch(DocumentException de){
            throw de;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
            if(repoSession != null){
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
        if(handler == null){
            throw new IllegalArgumentException(
                    "RemoteRepositoryClient.getAll: handler is missing");
        }
        String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
        if(nuxeoWspaceId == null){
            throw new DocumentNotFoundException(
                    "Unable to find workspace for service " + 
                    ctx.getServiceName() + " check if the mapping exists in service-config.xml or " +
                    " the the mapped workspace exists in the Nuxeo repository");
        }
        RepositoryInstance repoSession = null;

        try{
            handler.prepare(Action.GET_ALL);
            repoSession = getRepositorySession();
            DocumentRef wsDocRef = new IdRef(nuxeoWspaceId);
            DocumentModelList docList = repoSession.getChildren(wsDocRef);
            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentModelListWrapper wrapDoc = new DocumentModelListWrapper(
                    docList);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
        }catch(DocumentException de){
            throw de;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
            if(repoSession != null){
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
        if(id == null){
            throw new BadRequestException(
                    "RemoteRepositoryClient.update: id is missing");
        }
        if(handler == null){
            throw new IllegalArgumentException(
                    "RemoteRepositoryClient.update: handler is missing");
        }
        RepositoryInstance repoSession = null;
        try{
            handler.prepare(Action.UPDATE);
            repoSession = getRepositorySession();
            //FIXME, there is a potential privacy violation here, one tenant could
            //retrieve doc id of another tenant and could retrieve the document
            //PathRef does not seem to come to rescue as expected. Needs more thoughts.
            DocumentRef docRef = new IdRef(id);
            DocumentModel doc = null;
            try{
                doc = repoSession.getDocument(docRef);
            }catch(ClientException ce){
                String msg = "Could not find document to update with id=" + id;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
            }
            //set reposession to handle the document
            ((DocumentModelHandler) handler).setRepositorySession(repoSession);
            DocumentModelWrapper wrapDoc = new DocumentModelWrapper(doc);
            handler.handle(Action.UPDATE, wrapDoc);
            repoSession.saveDocument(doc);
            repoSession.save();
            handler.complete(Action.UPDATE, wrapDoc);
        }catch(DocumentException de){
            throw de;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
            if(repoSession != null){
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

        if(logger.isDebugEnabled()){
            logger.debug("deleting document with id=" + id);
        }
        RepositoryInstance repoSession = null;
        try{
            repoSession = getRepositorySession();
            //FIXME, there is a potential privacy violation here, one tenant could
            //retrieve doc id of another tenant and could retrieve the document
            //PathRef does not seem to come to rescue as expected. needs more thoughts.
            DocumentRef docRef = new IdRef(id);
            try{
                repoSession.removeDocument(docRef);
            }catch(ClientException ce){
                String msg = "could not find document to delete with id=" + id;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
            }
            repoSession.save();
        }catch(DocumentException de){
            throw de;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }
    }

    private RepositoryInstance getRepositorySession() throws Exception {
        // FIXME: is it possible to reuse repository session?
        // Authentication failures happen while trying to reuse the session
        NuxeoClient client = NuxeoConnector.getInstance().getClient();
        RepositoryInstance repoSession = client.openRepository();
        if(logger.isDebugEnabled()){
            logger.debug("getRepository() repository root: " + repoSession.getRootDocument());
        }
        return repoSession;
    }

    private void releaseRepositorySession(RepositoryInstance repoSession) {
        try{
            NuxeoClient client = NuxeoConnector.getInstance().getClient();
            // release session
            client.releaseRepository(repoSession);
        }catch(Exception e){
            logger.error("Could not close the repository session", e);
            // no need to throw this service specific exception
        }
    }
}
