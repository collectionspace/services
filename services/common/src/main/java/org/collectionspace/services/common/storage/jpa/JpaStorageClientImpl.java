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

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.TransactionContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.hibernate.exception.ConstraintViolationException;
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {
        String result = null;

        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();
        try {
            handler.prepare(Action.CREATE);
            Object entity = handler.getCommonPart();
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entity);
            
            jpaConnectionContext.beginTransaction();
            try {
                handler.handle(Action.CREATE, wrapDoc);
                JaxbUtils.setValue(entity, "setCreatedAtItem", Date.class, new Date());
                jpaConnectionContext.persist(entity);
            } catch (EntityExistsException ee) { // FIXME: No, don't allow duplicates
                //
                // We found an existing matching entity in the store, so we don't need to create one.  Just update the transient 'entity' instance with the existing persisted entity we found.
                // An entity's document handler class will throw this exception only if attempting to create (but not actually creating) duplicate is ok -e.g., Permission records.
                //
                entity = wrapDoc.getWrappedObject(); // the handler should have reset the wrapped transient object with the existing persisted entity we just found.
            }
            handler.complete(Action.CREATE, wrapDoc);
            jpaConnectionContext.commitTransaction();
            
            result = (String)JaxbUtils.getValue(entity, "getCsid");
        } catch (BadRequestException bre) {
            jpaConnectionContext.markForRollback();
            throw bre;
        } catch (DocumentException de) {
            jpaConnectionContext.markForRollback();
            throw de;
        } catch (RollbackException rbe) {
            //jpaConnectionContext.markForRollback();
            throw DocumentException.createDocumentException(rbe);
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                throw new DocumentException(DocumentException.DUPLICATE_RECORD_MSG, pe, DocumentException.DUPLICATE_RECORD_ERR);
            } else {
                throw new DocumentException(pe);
            }
        } catch (Exception e) {
            jpaConnectionContext.markForRollback();
            logger.debug("Caught exception ", e);
            throw DocumentException.createDocumentException(e);
        } finally {
            ctx.closeConnection();
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#get(org.collectionspace.services.common.context.ServiceContext, java.util.List, org.collectionspace.services.common.document.DocumentHandler)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void get(ServiceContext ctx, List<String> csidList, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#get(org.collectionspace.services.common.context.ServiceContext, java.lang.String, org.collectionspace.services.common.document.DocumentHandler)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {

        JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();
        try {
            handler.prepare(Action.GET);
            Object o = null;
            o = JpaStorageUtils.getEntity(jpaTransactionContext, handler.getDocumentFilter(), getEntityName(ctx), id, ctx.getTenantId());
            if (null == o) {
                String msg = "Could not find entity with id=" + id;
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
            ctx.closeConnection();
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#getAll(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.document.DocumentHandler)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void getAll(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException("use getFiltered instead");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#getFiltered(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.document.DocumentHandler)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getFiltered(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }

        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();        
        try {
            handler.prepare(Action.GET_ALL);
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(getEntityName(ctx));
            queryStrBldr.append(" a");
            
            String joinFetch = docFilter.getJoinFetchClause();
            if (Tools.notBlank(joinFetch)) {
                queryStrBldr.append(" " + joinFetch);
            }
            
            List<DocumentFilter.ParamBinding> params = docFilter.buildWhereForSearch(queryStrBldr);
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = jpaConnectionContext.createQuery(queryStr);
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

            jpaConnectionContext.beginTransaction();
            List list = q.getResultList();
            long totalItems = getTotalItems(jpaConnectionContext, ctx, handler); // Find out how many items our query would find independent of the paging restrictions            
            docFilter.setTotalItemsResult(totalItems); // Save the items total in the doc filter for later reporting
            DocumentWrapper<List> wrapDoc = new DocumentWrapperImpl<List>(list);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
            jpaConnectionContext.commitTransaction();
        } catch (DocumentException de) {
        	jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }

    /*
     * Return the COUNT for a query to find the total number of matches independent of the paging restrictions.
     */
    @SuppressWarnings("rawtypes")
    private long getTotalItems(JPATransactionContext jpaTransactionContext, ServiceContext ctx, DocumentHandler handler) {
        long result = -1;
        
        DocumentFilter docFilter = handler.getDocumentFilter();
        StringBuilder queryStrBldr = new StringBuilder("SELECT COUNT(*) FROM ");
        queryStrBldr.append(getEntityName(ctx));
        queryStrBldr.append(" a");
        
        List<DocumentFilter.ParamBinding> params = docFilter.buildWhereForSearch(queryStrBldr);
        String queryStr = queryStrBldr.toString();
        Query q = jpaTransactionContext.createQuery(queryStr);
        //bind parameters
        for (DocumentFilter.ParamBinding p : params) {
            q.setParameter(p.getName(), p.getValue());
        }

        result = (long) q.getSingleResult();

        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.storage.StorageClient#update(org.collectionspace.services.common.context.ServiceContext, java.lang.String, org.collectionspace.services.common.document.DocumentHandler)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {

        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();        
        try {
            jpaConnectionContext.beginTransaction();
            
            handler.prepare(Action.UPDATE);
            Object entityReceived = handler.getCommonPart();
            Object entityFound = getEntity(ctx, id, entityReceived.getClass());
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entityFound);
            handler.handle(Action.UPDATE, wrapDoc);
            JaxbUtils.setValue(entityFound, "setUpdatedAtItem", Date.class, new Date());
            handler.complete(Action.UPDATE, wrapDoc);
            
            jpaConnectionContext.commitTransaction();
        } catch (BadRequestException bre) {
            jpaConnectionContext.markForRollback();
            throw bre;
        } catch (DocumentException de) {
            jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            jpaConnectionContext.markForRollback();
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }

    /* 
     * delete removes entity and its child entities
     * cost: a get before delete
     * @see org.collectionspace.services.common.storage.StorageClient#delete(org.collectionspace.services.common.context.ServiceContext, java.lang.String)
     */
    @Override
    public void delete(@SuppressWarnings("rawtypes") ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();        
        try {
            jpaConnectionContext.beginTransaction();
            Object entityFound = getEntity(ctx, id);
            if (entityFound == null) {
                jpaConnectionContext.markForRollback();
                String msg = "delete(ctx, id): could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            jpaConnectionContext.remove(entityFound);
            jpaConnectionContext.commitTransaction();
        } catch (DocumentException de) {
            jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, id): Caught exception ", e);
            }
            jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
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
    public void deleteWhere(@SuppressWarnings("rawtypes") ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();        
        try {
            StringBuilder deleteStr = new StringBuilder("DELETE FROM ");
            deleteStr.append(getEntityName(ctx));
            deleteStr.append(" WHERE csid = :csid and tenantId = :tenantId");
            //TODO: add tenant csidReceived

            Query q = jpaConnectionContext.createQuery(deleteStr.toString());
            q.setParameter("csid", id);
            q.setParameter("tenantId", ctx.getTenantId());

            int rcount = 0;
            jpaConnectionContext.beginTransaction();
            rcount = q.executeUpdate();
            if (rcount != 1) {
                jpaConnectionContext.markForRollback();
                String msg = "deleteWhere(ctx, id) could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            jpaConnectionContext.commitTransaction();
        } catch (DocumentException de) {
            jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("deleteWhere(ctx, id) Caught exception ", e);
            }
            jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }

    /*
     * delete removes entity and its child entities but calls back to given handler
     * cost: a get before delete
     * @see org.collectionspace.services.common.storage.StorageClient#delete(org.collectionspace.services.common.context.ServiceContext, java.lang.String)
     */
    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean delete(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        boolean result = false;
        
        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection(); 
        try {
            jpaConnectionContext.beginTransaction();
            Object entityFound = getEntity(ctx, id);
            if (entityFound == null) {
                String msg = "delete(ctx, ix, handler) could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            result = delete(ctx, entityFound, handler);
            jpaConnectionContext.commitTransaction();
        } catch (DocumentException de) {
            jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, ix, handler): Caught exception ", e);
            }
            jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
        
        return result;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean delete(ServiceContext ctx, Object entity, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        boolean result = false;
        
        JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection(); 
        try {
            jpaConnectionContext.beginTransaction();
            handler.prepare(Action.DELETE);
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(entity);
            handler.handle(Action.DELETE, wrapDoc);
            jpaConnectionContext.remove(entity);
            handler.complete(Action.DELETE, wrapDoc);
            jpaConnectionContext.commitTransaction();
            result = true;
        } catch (DocumentException de) {
            jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, ix, handler): Caught exception ", e);
            }
            jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
        
        return result;
    }    

    /**
     * Gets the entityReceived name.
     * 
     * @param ctx the ctx
     * 
     * @return the entityReceived name
     */
    protected String getEntityName(@SuppressWarnings("rawtypes") ServiceContext ctx) {
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
     * @param ctx service context
     * @param csid received
     * @return
     * @throws DocumentNotFoundException
     * @throws TransactionException 
     */
    protected Object getEntity(@SuppressWarnings("rawtypes") ServiceContext ctx, String id)
            throws DocumentNotFoundException, TransactionException {
        Class<?> entityClazz = (Class<?>) ctx.getProperty(ServiceContextProperties.ENTITY_CLASS);
        if (entityClazz == null) {
            String msg = ServiceContextProperties.ENTITY_CLASS + " property is missing in the context";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        return getEntity(ctx, id, entityClazz);
    }

    /**
     * getEntity retrieves the persistent entity of given class for given id
     * rolls back the transaction if not found
     * @param em
     * @param id entity id
     * @param entityClazz
     * @return
     * @throws DocumentNotFoundException and rollsback the transaction if active
     * @throws TransactionException 
     */
    protected Object getEntity(@SuppressWarnings("rawtypes") ServiceContext ctx, String id, Class<?> entityClazz)
            throws DocumentNotFoundException, TransactionException {
        Object entityFound = null;
        
        JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();
        try {
            entityFound = JpaStorageUtils.getEntity(jpaTransactionContext, id, entityClazz); // FIXME: # Should be qualifying with the tenant ID
            if (entityFound == null) {
                String msg = "could not find entity of type=" + entityClazz.getName()
                        + " with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
        } finally {
            ctx.closeConnection();
        }
        
        return entityFound;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void get(ServiceContext ctx, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void doWorkflowTransition(ServiceContext ctx, String id,
            DocumentHandler handler, TransitionDef transitionDef)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        // Do nothing.  JPA services do not support workflow.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void deleteWithWhereClause(ServiceContext ctx, String whereClause,
            DocumentHandler handler) throws DocumentNotFoundException,
            DocumentException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean synchronize(ServiceContext ctx, Object specifier,
            DocumentHandler handler) throws DocumentNotFoundException,
            TransactionException, DocumentException {
        // TODO Auto-generated method stub
        // Do nothing. Subclasses can override if they want/need to.
        return true;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean synchronizeItem(ServiceContext ctx, AuthorityItemSpecifier itemSpecifier,
            DocumentHandler handler) throws DocumentNotFoundException,
            TransactionException, DocumentException {
        // TODO Auto-generated method stub
        // Do nothing. Subclasses can override if they want/need to.
        return true;
    }

    @Override
    public void releaseRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, Object repoSession)
            throws TransactionException {
        // TODO Auto-generated method stub        
    }

}
