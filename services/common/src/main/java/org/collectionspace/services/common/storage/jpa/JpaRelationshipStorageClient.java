/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.common.storage.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaRelationshipStorageClient deals with a relationship
 * in persistent storage. This storage client deals with bulk operations, i.e.
 * create/post inserts multiple tuples between the given object and subjects
 * get retrieves all subjects for the given object in relationship
 * delete deletes all subjects for the given object in relationship
 * @author 
 */
public class JpaRelationshipStorageClient<T> extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(JpaRelationshipStorageClient.class);

    public JpaRelationshipStorageClient() {
    }

    /**
     * create of a relationship creates one or more relationships between
     * permission and role
     * the object and subjects of the relationship is chosen (by doc handler) from
     * the payload
     * @param ctx
     * @param handler
     * @return
     * @throws BadRequestException
     * @throws DocumentException
     */
    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "JpaRelationshipStorageClient.create : ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "JpaRelationshipStorageClient.create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.CREATE);
            List<T> rl = new ArrayList<T>();
            DocumentWrapper<List<T>> wrapDoc =
                    new DocumentWrapperImpl<List<T>>(rl);
            handler.handle(Action.CREATE, wrapDoc);
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            for (T r : rl) {
                setValue(r, "setCreatedAtItem", Date.class, new Date());
                em.persist(r);
            }
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return UUID.randomUUID().toString(); //filler, not useful
        } catch (BadRequestException bre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw bre;
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

    /**
     * get retrieves all relationships for the object in the relationship
     * identified by the id. the object could be a permission or a role
     * @param ctx
     * @param id of the object in the relationship
     * @param handler
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "JpaRelationshipStorageClient.get: ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "JpaRelationshipStorageClient.get: handler is missing");
        }
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET);
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(getEntityName(ctx));
            queryStrBldr.append(" a");
            String objectId = getObjectId(ctx);
            if (logger.isDebugEnabled()) {
                logger.debug("get: using objectId=" + objectId);
            }
            queryStrBldr.append(" WHERE " + objectId + " = :objectId");
            String where = docFilter.getWhereClause();
            if ((null != where) && (where.length() > 0)) {
                queryStrBldr.append(" AND " + where);
            }
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            if (logger.isDebugEnabled()) {
                logger.debug("get: jql=" + queryStr.toString());
            }
            Query q = em.createQuery(queryStr);
            q.setParameter("objectId", id);

            List<T> rl = new ArrayList<T>();
            try {
                //require transaction for get?
                em.getTransaction().begin();
                rl = q.getResultList();
                em.getTransaction().commit();
            } catch (NoResultException nre) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
                logger.error(msg, nre);
                throw new DocumentNotFoundException(msg, nre);
            }
            if (rl.size() == 0) {
                String msg = "could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            DocumentWrapper<List<T>> wrapDoc =
                    new DocumentWrapperImpl<List<T>>(rl);
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
                releaseEntityManagerFactory(emf);
            }
        }
    }

    /**
     * delete removes all the relationships for the object in the relationship
     * identified by the id. the object could be a permission or a role
     * @param ctx
     * @param id of the object in the relationship
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
    @Override
    public void delete(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("deleting entity with id=" + id);
        }
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "JpaRelationshipStorageClient.delete : ctx is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            StringBuilder deleteStr = new StringBuilder("DELETE FROM ");
            deleteStr.append(getEntityName(ctx));
            String objectId = getObjectId(ctx);
            if (logger.isDebugEnabled()) {
                logger.debug("delete: using objectId=" + objectId);
            }
            deleteStr.append(" WHERE " + objectId + " = :objectId");
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            if (logger.isDebugEnabled()) {
                logger.debug("delete: jql=" + deleteStr.toString());
            }
            Query q = em.createQuery(deleteStr.toString());
            q.setParameter("objectId", id);
            int rcount = 0;
            em.getTransaction().begin();
            rcount = q.executeUpdate();
            if (rcount == 0) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
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

    protected String getObjectId(ServiceContext ctx) {
        String objectId = (String) ctx.getProperty("objectId");
        if (objectId == null) {
            String msg = "objectId is missing in the context";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return objectId;
    }
}
