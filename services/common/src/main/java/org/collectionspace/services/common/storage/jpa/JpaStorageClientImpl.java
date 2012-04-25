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

import java.util.Date;
import java.util.List;
import javax.persistence.RollbackException;
import java.sql.BatchUpdateException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.document.JaxbUtils;

import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.lifecycle.TransitionDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaStorageClient is used to perform CRUD operations on SQL storage using JPA.
 * It uses @see DocumentHandler as IOHandler with the client.
 * All the operations in this client are carried out under their own transactions.
 * A call to any method would start and commit/rollback a transaction.
 * 
 * Assumption: each persistent entityReceived has the following 3 attributes
<xs:element name="createdAt" type="xs:dateTime">
<xs:annotation>
<xs:appinfo>
<hj:basic>
<orm:column name="created_at" nullable="false"/>
</hj:basic>
</xs:appinfo>
</xs:annotation>
</xs:element>
<xs:element name="updatedAt" type="xs:dateTime">
<xs:annotation>
<xs:appinfo>
<hj:basic>
<orm:column name="updated_at" />
</hj:basic>
</xs:appinfo>
</xs:annotation>
</xs:element>
</xs:sequence>
<xs:attribute name="csid" type="xs:string">
<xs:annotation>
<xs:appinfo>
<hj:csidReceived>
<orm:column name="csid" length="128" nullable="false"/>
</hj:csidReceived>
</xs:appinfo>
</xs:annotation>
</xs:attribute>
 *
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class JpaStorageClientImpl implements StorageClient {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(JpaStorageClientImpl.class);

    /**
     * Instantiates a new jpa storage client.
     */
    public JpaStorageClientImpl() {
    	//intentionally empty
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#create(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {
    	boolean rollbackTransaction = false;
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "create: ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.CREATE);
            Object entity = handler.getCommonPart();
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entity);
            handler.handle(Action.CREATE, wrapDoc);
            JaxbUtils.setValue(entity, "setCreatedAtItem", Date.class, new Date());
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin(); { //begin of transaction block
            	em.persist(entity);
            }
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return (String) JaxbUtils.getValue(entity, "getCsid");
        } catch (BadRequestException bre) {
        	rollbackTransaction = true;
            throw bre;
        } catch (DocumentException de) {
        	rollbackTransaction = true;
            throw de;
        } catch (Exception e) {
        	rollbackTransaction = true;
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw DocumentException.createDocumentException(e);
        } finally {
            if (em != null) {
            	if (rollbackTransaction == true) {
            		if (em.getTransaction().isActive() == true) {
            			em.getTransaction().rollback();
            		}
            	}
            	// Don't call this unless "em" is not null -hence the check above.
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#get(org.collectionspace.services.common.context.ServiceContext, java.util.List, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void get(ServiceContext ctx, List<String> csidList, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#get(org.collectionspace.services.common.context.ServiceContext, java.lang.String, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "get: ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "get: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET);
            Object o = null;
            o = JpaStorageUtils.getEntity(getEntityName(ctx), id, 
                    ctx.getTenantId());
            if (null == o) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
                throw new DocumentNotFoundException(msg);
            }
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(o);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#getAll(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void getAll(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException("use getFiltered instead");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#getFiltered(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void getFiltered(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
    	QueryContext queryContext = new QueryContext(ctx, handler);
    	
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET_ALL);
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(getEntityName(ctx));
            queryStrBldr.append(" a");
            
            List<DocumentFilter.ParamBinding> params = docFilter.buildWhereForSearch(queryStrBldr);
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);
            //bind parameters
            for (DocumentFilter.ParamBinding p : params) {
                q.setParameter(p.getName(), p.getValue());
            }
            if (docFilter.getOffset() > 0) {
                q.setFirstResult(docFilter.getOffset());
            }
            if (docFilter.getPageSize() > 0) {
                q.setMaxResults(docFilter.getPageSize());
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
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#update(org.collectionspace.services.common.context.ServiceContext, java.lang.String, org.collectionspace.services.common.document.DocumentHandler)
     */
    @Override
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "update: ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "update: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.UPDATE);
            Object entityReceived = handler.getCommonPart();
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            Object entityFound = getEntity(em, id, entityReceived.getClass());
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entityFound);
            handler.handle(Action.UPDATE, wrapDoc);
            JaxbUtils.setValue(entityFound, "setUpdatedAtItem", Date.class, new Date());
            em.getTransaction().commit();
            handler.complete(Action.UPDATE, wrapDoc);
        } catch (BadRequestException bre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw bre;
        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    /* 
     * delete removes entity and its child entities
     * cost: a get before delete
     * @see org.collectionspace.services.common.storage.StorageClient#delete(org.collectionspace.services.common.context.ServiceContext, java.lang.String)
     */
    @Override
    public void delete(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("delete(ctx, id): deleting entity with id=" + id);
        }

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, id): ctx is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();

            em.getTransaction().begin();
            Object entityFound = getEntity(ctx, em, id);
            if (entityFound == null) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "delete(ctx, id): could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            em.remove(entityFound);
            em.getTransaction().commit();

        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, id): Caught exception ", e);
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    /**
     * deleteWhere uses the where clause to delete an entityReceived represented by the csidReceived
     * it does not delete any child entities.
     * @param ctx
     * @param id
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
    public void deleteWhere(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "deleteWhere(ctx, id) : ctx is missing");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("deleteWhere(ctx, id): deleting entity with id=" + id);
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            StringBuilder deleteStr = new StringBuilder("DELETE FROM ");
            deleteStr.append(getEntityName(ctx));
            deleteStr.append(" WHERE csid = :csid and tenantId = :tenantId");
            //TODO: add tenant csidReceived

            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            Query q = em.createQuery(deleteStr.toString());
            q.setParameter("csid", id);
            q.setParameter("tenantId", ctx.getTenantId());

            int rcount = 0;
            em.getTransaction().begin();
            rcount = q.executeUpdate();
            if (rcount != 1) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "deleteWhere(ctx, id) could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            em.getTransaction().commit();

        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("deleteWhere(ctx, id) Caught exception ", e);
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    /*
     * delete removes entity and its child entities but calls back to given handler
     * cost: a get before delete
     * @see org.collectionspace.services.common.storage.StorageClient#delete(org.collectionspace.services.common.context.ServiceContext, java.lang.String)
     */
    @Override
    public void delete(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, ix, handler): ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "delete(ctx, ix, handler): handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.DELETE);

            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();

            em.getTransaction().begin();
            Object entityFound = getEntity(ctx, em, id);
            if (entityFound == null) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "delete(ctx, ix, handler) could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entityFound);
            handler.handle(Action.DELETE, wrapDoc);
            em.remove(entityFound);
            em.getTransaction().commit();

            handler.complete(Action.DELETE, wrapDoc);
        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, ix, handler): Caught exception ", e);
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    /**
     * Gets the entityReceived name.
     * 
     * @param ctx the ctx
     * 
     * @return the entityReceived name
     */
    protected String getEntityName(ServiceContext ctx) {
        Object o = ctx.getProperty(ServiceContextProperties.ENTITY_NAME);
        if (o == null) {
            throw new IllegalArgumentException(ServiceContextProperties.ENTITY_NAME
                    + "property is missing in context "
                    + ctx.toString());
        }

        return (String) o;
    }

    /**
     * getEntity returns persistent entity for given id. it assumes that
     * service context has property ServiceContextProperties.ENTITY_CLASS set
     * rolls back the transaction if not found
     * @param ctx service context
     * @param em entity manager
     * @param csid received
     * @return
     * @throws DocumentNotFoundException and rollsback the transaction if active
     */
    protected Object getEntity(ServiceContext ctx, EntityManager em, String id)
            throws DocumentNotFoundException {
        Class entityClazz = (Class) ctx.getProperty(ServiceContextProperties.ENTITY_CLASS);
        if (entityClazz == null) {
            String msg = ServiceContextProperties.ENTITY_CLASS
                    + " property is missing in the context";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return getEntity(em, id, entityClazz);
    }

    /**
     * getEntity retrieves the persistent entity of given class for given id
     * rolls back the transaction if not found
     * @param em
     * @param id entity id
     * @param entityClazz
     * @return
     * @throws DocumentNotFoundException and rollsback the transaction if active
     */
    protected Object getEntity(EntityManager em, String id, Class entityClazz)
            throws DocumentNotFoundException {
        Object entityFound = JpaStorageUtils.getEntity(em, id, entityClazz);
        if (entityFound == null) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            String msg = "could not find entity of type=" + entityClazz.getName()
                    + " with id=" + id;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        return entityFound;
    }

    @Override
    public void get(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException();
    }

	@Override
	public void doWorkflowTransition(ServiceContext ctx, String id,
			DocumentHandler handler, TransitionDef transitionDef)
			throws BadRequestException, DocumentNotFoundException,
			DocumentException {
		// Do nothing.  JPA services do not support workflow.
	}
}
