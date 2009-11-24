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
package org.collectionspace.services.common.storage.jpa;

import java.lang.reflect.Method;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaStorageClient is used to perform CRUD operations on SQL storage using JPA.
 * It uses @see DocumentHandler as IOHandler with the client.
 * All the operations in this client are carried out under their own transactions.
 * A call to any method would start and commit/rollback a transaction.
 *
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class JpaStorageClient implements StorageClient {

    private final Logger logger = LoggerFactory.getLogger(JpaStorageClient.class);

    public JpaStorageClient() {
    }

    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {

        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "JpaStorageClient.create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.CREATE);
            Object entity = handler.getCommonPart();
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entity);
            handler.handle(Action.CREATE, wrapDoc);
            emf = getEntityManagerFactory(docType);
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return getCsid(entity);
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }

    }

    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "JpaStorageClient.get: handler is missing");
        }
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            throw new IllegalArgumentException(
                    "JpaStorageClient.get: handler has no Filter specified");
        }
        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET);
            StringBuilder queryStr = new StringBuilder("SELECT a FROM ");
            queryStr.append(getEntityName(ctx));
            queryStr.append(" a");
            queryStr.append(" WHERE csid = :csid");
            //TODO: add tenant id
            String where = docFilter.getWhereClause();
            if ((null != where) && (where.length() > 0)) {
                queryStr.append(" AND " + where);
            }
            emf = getEntityManagerFactory(docType);
            em = emf.createEntityManager();
            Query q = em.createQuery(queryStr.toString());
            q.setParameter("csid", id);
            //TODO: add tenant id

            //TODO: get page
            if ((docFilter.getOffset() > 0) || (docFilter.getPageSize() > 0)) {
            } else {
            }
            Object o = null;

            try {
                //require transaction for get?
                em.getTransaction().begin();
                o = q.getSingleResult();
                em.getTransaction().commit();
            } catch (NoResultException nre) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
                logger.error(msg, nre);
                throw new DocumentNotFoundException(msg, nre);
            }
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(o);
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
            if (emf != null) {
                releaseEntityManagerFactory(emf);
            }
        }
    }

    @Override
    public void getAll(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException("use getFiltered instead");
    }

    /**
     * getFiltered get all documents for an entity service from the Document repository,
     * given filter parameters specified by the handler.
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the document
     * @throws DocumentNotFoundException if workspace not found
     * @throws DocumentException
     */
    public void getFiltered(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "JpaStorageClient.getFiltered: handler is missing");
        }
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            throw new IllegalArgumentException(
                    "JpaStorageClient.getFiltered: handler has no Filter specified");
        }
        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }

        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET_ALL);

            StringBuilder queryStr = new StringBuilder("SELECT a FROM ");
            queryStr.append(getEntityName(ctx));
            queryStr.append(" a");
            //TODO: add tenant id
            String where = docFilter.getWhereClause();
            if ((null != where) && (where.length() > 0)) {
                queryStr.append(" AND " + where);
            }
            emf = getEntityManagerFactory(docType);
            em = emf.createEntityManager();
            Query q = em.createQuery(queryStr.toString());
            //TODO: add tenant id
            //TODO: get page
            if ((docFilter.getOffset() > 0) || (docFilter.getPageSize() > 0)) {
            } else {
            }
            //FIXME is transaction required for get?
            em.getTransaction().begin();
            List list = q.getResultList();
            em.getTransaction().commit();
            DocumentWrapper<List> wrapDoc = new DocumentWrapperImpl<List>(list);
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
            if (emf != null) {
                releaseEntityManagerFactory(emf);
            }
        }
    }

    @Override
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "JpaStorageClient.update: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.UPDATE);
            Object entity = handler.getCommonPart();
            setCsid(entity, id);
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entity);
            handler.handle(Action.UPDATE, wrapDoc);
            emf = getEntityManagerFactory(docType);
            em = emf.createEntityManager();
            em.getTransaction().begin();
            Object entityFound = em.find(entity.getClass(), id);
            if(entityFound == null) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            em.merge(entity);
            em.getTransaction().commit();
            handler.complete(Action.UPDATE, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                releaseEntityManagerFactory(emf);
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
    public void delete(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("deleting entity with id=" + id);
        }
        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            StringBuilder deleteStr = new StringBuilder("DELETE FROM ");
            deleteStr.append(getEntityName(ctx));
            deleteStr.append(" WHERE csid = :csid");
            //TODO: add tenant id

            emf = getEntityManagerFactory(docType);
            em = emf.createEntityManager();
            Query q = em.createQuery(deleteStr.toString());
            q.setParameter("csid", id);
            //TODO: add tenant id
            int rcount = 0;
            em.getTransaction().begin();
            rcount = q.executeUpdate();
            if (rcount != 1) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            em.getTransaction().commit();

        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                releaseEntityManagerFactory(emf);
            }
        }
    }

    private EntityManagerFactory getEntityManagerFactory(
            String persistenceUnit) {
        return Persistence.createEntityManagerFactory(persistenceUnit);

    }

    private void releaseEntityManagerFactory(EntityManagerFactory emf) {
        if (emf != null) {
            emf.close();
        }

    }

    private String getCsid(Object o) throws Exception {
        Class c = o.getClass();
        Method m = c.getMethod("getCsid");
        if (m == null) {
            String msg = "Could not find csid in entity of class=" + o.getClass().getCanonicalName();
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Object r = m.invoke(o);
        if (logger.isDebugEnabled()) {
            logger.debug("getCsid returned csid=" + r +
                    " for " + c.getName());
        }

        return (String) r;
    }

    private void setCsid(Object o, String csid) throws Exception {
        //verify csid 
        String id = getCsid(o);
        if (id != null) {
            if (!id.equals(csid)) {
                String msg = "Csids do not match!";
                logger.error(msg);
                throw new BadRequestException(msg);
            } else {
                //no need to set
                return;
            }

        }
        //set csid
        Class c = o.getClass();
        Method m = c.getMethod("setCsid", java.lang.String.class);
        if (m == null) {
            String msg = "Could not find csid in entity of class=" + o.getClass().getCanonicalName();
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Object r = m.invoke(o, csid);
        if (logger.isDebugEnabled()) {
            logger.debug("completed setCsid " +
                    " for " + c.getName());
        }

    }

    private String getEntityName(ServiceContext ctx) {
        Object o = ctx.getProperty("entity-name");
        if (o == null) {
            throw new IllegalArgumentException("property entity-name missing in context " +
                    ctx.toString());
        }

        return (String) o;
    }
}
