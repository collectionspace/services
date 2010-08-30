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

import javax.persistence.PersistenceException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.security.SecurityUtils;

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
    private final static String CS_AUTHZ_PERSISTENCE_UNIT = "org.collectionspace.services.authorization";

    /**
     * getEntity for given id and class
     * @param id
     * @param entityClazz
     * @return null if entity is not found
     */
    public static Object getEntity(String id, Class entityClazz)
    		throws DocumentNotFoundException {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object entityFound = null;
        try {
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            //FIXME: it would be nice to verify tenantid as well
            entityFound = em.find(entityClazz, id);
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
        return entityFound;
    }

    public static Object getEntity(long id, Class entityClazz)
    		throws DocumentNotFoundException {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object entityFound = null;
        try {
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            //FIXME: it would be nice to verify tenantid as well
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
        //FIXME: it would be nice to verify tenantid as well
        return em.find(entityClazz, id);
    }

    /**
     * getEntity 
     * @param entityName fully qualified entity name
     * @param id
     * @param tenantId
     * @return null if entity is not found
     */
    public static Object getEntity(String entityName, String id,
            String tenantId) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object o = null;
        if (entityName == null) {
            throw new IllegalArgumentException("entityName is required");
        }
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        try {
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(entityName);
            queryStrBldr.append(" a");
            queryStrBldr.append(" WHERE csid = :csid");
            boolean csAdmin = SecurityUtils.isCSpaceAdmin();
            if (!csAdmin) {
                queryStrBldr.append(" AND tenantId = :tenantId");
            }
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);
            q.setParameter("csid", id);
            if (!csAdmin) {
                q.setParameter("tenantId", tenantId);
            }
            o = q.getSingleResult();
        } catch (NoResultException nre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity with id=" + id, nre);
            }
            //returns null
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity(2) with id=" + id, e);
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
        if (entityName == null) {
            throw new IllegalArgumentException("entityName is required");
        }
        if (whereClause == null) {
            throw new IllegalArgumentException("whereClause is required");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object o = null;
        try {
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(entityName);
            queryStrBldr.append(" a");
            queryStrBldr.append(" " + whereClause);
            //FIXME it would be nice to insert tenant id in the where clause here
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
                logger.debug("could not find entity with where=" + whereClause, nre);
            }
            //returns null
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity (2) with where=" + whereClause, e);
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
    	EntityManagerFactory result = null;
    	PersistenceException persistenceException = null;
    	
    	try {
    		result = getEntityManagerFactory(CS_PERSISTENCE_UNIT);
    	} catch (PersistenceException e) {
    		persistenceException = e;
    	}
    	//
    	// If the CS_PERSISTENCE_UNIT does not exist, our caller may be from
    	// the import utility.
    	// FIXME: REM - EntityManagerFactory should be passed in from the Import utility.
    	//
    	if (result == null) {
	    	try {
	    		result = getEntityManagerFactory(CS_AUTHZ_PERSISTENCE_UNIT);
	    		return result;
	    	} catch (PersistenceException e) {
	    		persistenceException = e;
	    	}
    	}
    	        
        if (result == null) {
        	throw persistenceException;
        }
        
        return result;
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

