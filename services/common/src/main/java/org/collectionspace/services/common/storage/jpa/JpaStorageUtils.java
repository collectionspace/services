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
 *  limitations under the License
 */
package org.collectionspace.services.common.storage.jpa;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.collectionspace.services.common.document.DocumentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for JpaStorage
 * @author 
 */
public class JpaStorageUtils {

    final private static Logger logger = LoggerFactory.getLogger(JpaStorageUtils.class);
    /** The Constant CS_PERSISTENCE_UNIT. */
    public final static String CS_PERSISTENCE_UNIT = "org.collectionspace.services";

    /**
     * getEntity for given id and class
     * @param id
     * @param entityClazz
     * @return null if entity is not found
     */
    public static Object getEntity(String id, Class entityClazz) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object entityFound = null;
        try {
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            entityFound = em.find(entityClazz, id);
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
        return entityFound;
    }

    /**
     * getEntity with given id and class using given entity manager
     * 
     * @param em
     * @param id
     * @param entityClazz
     * @return
     */
    public static Object getEntity(EntityManager em, String id, Class entityClazz) {
        if (entityClazz == null) {
            String msg = "Not constructed with JpaStorageClientImpl(entityClazz) ctor";
            logger.error(msg);
            throw new UnsupportedOperationException(msg);
        }
        return em.find(entityClazz, id);
    }

    /**
     * getEntity using whereClause clause from given docFilter
     * @param entityName fully qualified entity name
     * @param id
     * @param docFilter
     * @return null if entity is not found
     */
    public static Object getEntity(String entityName, String id, DocumentFilter docFilter) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object o = null;
        try {
            if (docFilter == null) {
                docFilter = new DocumentFilter();
            }
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(entityName);
            queryStrBldr.append(" a");
            queryStrBldr.append(" WHERE csid = :csid");
            //TODO: add tenant id
            String where = docFilter.getWhereClause();
            if ((null != where) && (where.length() > 0)) {
                queryStrBldr.append(" AND " + where);
            }
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);
            q.setParameter("csid", id);
            List<DocumentFilter.ParamBinding> params =
                    docFilter.buildWhereForSearch(queryStrBldr);
            for (DocumentFilter.ParamBinding p : params) {
                q.setParameter(p.getName(), p.getValue());
            }
            o = q.getSingleResult();
        } catch (NoResultException nre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity with id=" + id);
            }
            //returns null
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
        return o;
    }

    /**
     * getEntity using given where clause with given param bindings
     * @param entityName
     * @param whereClause
     * @param paramBindings
     * @return
     */
    public static Object getEntity(String entityName,
            String whereClause, HashMap<String, Object> paramBindings) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object o = null;
        try {
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(entityName);
            queryStrBldr.append(" a");
            queryStrBldr.append(" " + whereClause);

            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);
            for (String paramName : paramBindings.keySet()) {
                q.setParameter(paramName, paramBindings.get(paramName));
            }
            o = q.getSingleResult();
        } catch (NoResultException nre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity with where=" + whereClause);
            }
            //returns null
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
        return o;
    }

    /**
     * Gets the entity manager factory.
     *
     * @return the entity manager factory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactory(CS_PERSISTENCE_UNIT);
    }

    /**
     * Gets the entity manager factory.
     *
     * @param persistenceUnit the persistence unit
     *
     * @return the entity manager factory
     */
    public static EntityManagerFactory getEntityManagerFactory(
            String persistenceUnit) {
        return Persistence.createEntityManagerFactory(persistenceUnit);

    }

    /**
     * Release entity manager factory.
     *
     * @param emf the emf
     */
    public static void releaseEntityManagerFactory(EntityManagerFactory emf) {
        if (emf != null) {
            emf.close();
        }

    }
}

