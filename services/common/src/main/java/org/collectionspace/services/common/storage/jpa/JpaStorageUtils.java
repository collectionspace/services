/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at:

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package org.collectionspace.services.common.storage.jpa;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.authorization.AccountPermission;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorization_mgt.AuthorizationRoleRel;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for JpaStorage
 * @author 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JpaStorageUtils {

    final private static Logger logger = LoggerFactory.getLogger(JpaStorageUtils.class);
    
    /** The Constant CS_PERSISTENCE_UNIT. */
    public final static String CS_PERSISTENCE_UNIT = "org.collectionspace.services";
    public static String CS_AUTHZ_PERSISTENCE_UNIT = "org.collectionspace.services.authorization";
    public final static String CS_CURRENT_USER = "0";
    
    // This is the column name for ID field of all the JPA objects
    public static final String CSID_LABEL = "csid";
    
    private static Map<String, EntityManagerFactory> entityManagerFactoryCache = new HashMap<String, EntityManagerFactory>();

    private static boolean useTenantId(String tenantId) {
    	boolean result = true;
    	
        boolean csAdmin = SecurityUtils.isCSpaceAdmin();
        if (csAdmin == true) {
        	logger.trace("Running as the CSAdmin user.");
        	//Thread.dumpStack();
        }
        
    	if (tenantId == null) {
    		result = false;
        	logger.trace("Ignoring tenant ID during .");
        	//Thread.dumpStack();
    	}

    	return result;
    }
    	
	@Deprecated
	public static Object getEntity(String id, Class entityClazz) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object entityFound = null;
        try {
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            //FIXME: it would be nice to verify tenantid as well
            entityFound = em.find(entityClazz, id);
        } catch (Throwable t) {
        	throw t;
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
    @Deprecated
    public static Object getEntity(EntityManager em, String id, Class entityClazz) throws DocumentNotFoundException {
        if (entityClazz == null) {
            String msg = "Not constructed with JpaStorageClientImpl(entityClazz) ctor";
            logger.error(msg);
            throw new UnsupportedOperationException(msg);
        }
        //FIXME: it would be nice to verify tenantid as well
        return em.find(entityClazz, id);
    }
        
    public static Object getEntity(JPATransactionContext jpaTransactionContext, String id, Class entityClazz) throws DocumentNotFoundException {
        if (entityClazz == null) {
            String msg = "Not constructed with JpaStorageClientImpl(entityClazz) ctor";
            logger.error(msg);
            throw new UnsupportedOperationException(msg);
        }
        //FIXME: it would be nice to verify tenantid as well
        return jpaTransactionContext.find(entityClazz, id);
    }    
    
    @Deprecated
    private static String getUserId(String csid)
    		throws DocumentNotFoundException  {
    	String result = null;
    	//
    	// If the CSID is null then return the currently logged in user's ID
    	//
    	if (csid.equals(CS_CURRENT_USER) == true) {
    		return AuthN.get().getUserId();
    	}
    	
    	//FIXME: Why can't the common jar depend on the account service?  Can we move the account
    	//jaxb classes to the common "jaxb" module?
    	try {
			//can't use JAXB here as this runs from the common jar which cannot
			//depend upon the account service
			String whereClause = "where csid = :csid";
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("csid", csid);
	
			Object account = JpaStorageUtils.getEntity(
					"org.collectionspace.services.account.AccountsCommon", whereClause, params);
			if (account == null) {
				String msg = "User's account not found, csid=" + csid;
				throw new DocumentNotFoundException(msg);
			}
			//
			// Retrieve the userId that corresponds to the csid passed in to us
			//
			result = (String)JaxbUtils.getValue(account, "getUserId");
    	} catch (Exception e) {
			String msg = "User's account is in invalid state, csid=" + csid;
			throw new DocumentNotFoundException(msg);    		
    	}
    	    	
    	return result;
    }
    
    private static AccountValue getAccountValue(String csid)
    	throws DocumentNotFoundException  {

    	try {
        	//
        	// If the CSID is null then return the currently logged in user's ID
        	//
    		String whereClause;
    		HashMap<String, Object> params = new HashMap<String, Object>();
        	if (csid.equals(CS_CURRENT_USER) == true) {
        		whereClause = "where userId = :userId";
        		params.put("userId", AuthN.get().getUserId());
        	} else {
        		whereClause = "where csid = :csid";
        		params.put("csid", csid);
        	}

        	Object account = JpaStorageUtils.getEntity(
    				"org.collectionspace.services.account.AccountsCommon", whereClause, params);
    		if (account == null) {
    			String msg = "User's account not found, csid=" + csid;
    			throw new DocumentNotFoundException(msg);
    		}
        	AccountValue av = new AccountValue();
        	av.setAccountId((String)JaxbUtils.getValue(account, "getCsid"));
        	av.setScreenName((String)JaxbUtils.getValue(account, "getScreenName"));
        	av.setUserId((String)JaxbUtils.getValue(account, "getUserId"));
            // Add the currentTenantId to the payload so the client knows the current tenancy.
        	av.setTenantId(AuthN.get().getCurrentTenantId());

    		return av;
    	} catch (Exception e) {
    		String msg = "User's account is in invalid state, csid=" + csid;
    		throw new DocumentNotFoundException(msg);    		
    	}
    }

    //FIXME: REM - This method should probably be moved to the AccountPermissionDocumemntHandler
    /*
     * This is a prototype for the /accounts/{csid}/permissions GET service call.
     */
    public static AccountPermission getAccountPermissions(String csid)
		throws UnauthorizedException, DocumentNotFoundException {
    	return getAccountPermissions(csid, null, null);
    }
    
    //FIXME: REM - This method should probably be moved to the AccountPermissionDocumemntHandler    
    /*
     * This is a prototype for the /accounts/{csid}/permissions GET service call.
     */
    public static AccountPermission getAccountPermissions(String csid, String currentResource, String permissionResource)
    	throws UnauthorizedException, DocumentNotFoundException {
        //
        // Make sure the user asking for this list has the correct
        // permission -that is, the csid's userId match the currently logged in userId or
    	// that they have read access to the "accounts" resource.
        //
    	AccountValue account = getAccountValue(csid);
    	String userId = account.getUserId();
    	String currentUserId = AuthN.get().getUserId(); 
        if (currentUserId.equalsIgnoreCase(userId) == false) {
			CSpaceResource res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), "accounts", "GET");
			if (AuthZ.get().isAccessAllowed(res) == false) {
	        	String msg = "Access to the permissions for the account with csid = " + csid + " is NOT allowed for " +
					" user=" + currentUserId;
	        	if (logger.isDebugEnabled() == true) {
	        		logger.debug(msg);
	        	}
				throw new UnauthorizedException(msg);
			}
        }
        
        AccountPermission result = new AccountPermission();
    	EntityManagerFactory emf = null;
        EntityManager em = null;
        Iterator<Object> resultList = null;
        try {
        	List<AccountValue> accountValues = new ArrayList<AccountValue>();
        	accountValues.add(account);
            result.setAccount(accountValues);

            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
           
            StringBuilder permQueryStrBldr = new StringBuilder(
            		"SELECT DISTINCT pr FROM " + AccountRoleRel.class.getName() + " ar, " 
            		+ PermissionRoleRel.class.getName() + " pr"
            		+ " WHERE ar.roleId = pr.roleId and ar.userId=" + "'" + userId + "'");
            //
            // Filter by the permissionResource param if it is set to something
            //
            if (permissionResource != null && currentResource != null) {
            	permQueryStrBldr.append(" and (pr.permissionResource = " + "'" + currentResource + "'" +
            			" or pr.permissionResource = " + "'" + permissionResource + "'" + ")");
            }
            String queryStr = permQueryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);            
            resultList = q.getResultList().iterator();

            if (resultList.hasNext()) {
            	List<PermissionValue> permissionValues = new ArrayList<PermissionValue>();
	            while (resultList.hasNext()) {
	            	PermissionRoleRel permRolRel = (PermissionRoleRel)resultList.next();
	            	permissionValues.add(AuthorizationRoleRel.buildPermissionValue(permRolRel));
	            }
	            result.setPermission(permissionValues);
            }
        } catch (NoResultException nre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity with id=" + userId, nre);
            }
            //returns null
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity(2) with id=" + userId, e);
            }
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
        return result;
    }
    
    public static Object getEnityByKey(
    		JPATransactionContext jpaTransactionContext,
    		String entityName, 
    		String key, 
    		String value,
            String tenantId) throws TransactionException {
    	return getEnityByKey(jpaTransactionContext, (DocumentFilter)null, entityName, key, value, tenantId);
    }
    
    public static Object getEnityByKey(
    		JPATransactionContext jpaTransactionContext,
    		DocumentFilter docFilter,
    		String entityName, String key, String value,
            String tenantId) throws TransactionException {
        Object result = null;
        
        try {
            boolean useTenantId = useTenantId(tenantId);
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(entityName);
            queryStrBldr.append(" a");
            
            if (docFilter != null) {
	            String joinFetch = docFilter.getJoinFetchClause();
	            if (Tools.notBlank(joinFetch)) {
	            	queryStrBldr.append(" " + joinFetch);
	            }
            }
            
            queryStrBldr.append(" WHERE a." + key + " = :" + key);
            if (useTenantId == true) {
                queryStrBldr.append(" AND a.tenantId = :tenantId");
            }
            String queryStr = queryStrBldr.toString(); //for debugging            
            Query q = jpaTransactionContext.createQuery(queryStr);
            q.setParameter(key, value);
            if (useTenantId == true) {
                q.setParameter("tenantId", tenantId);
            }
            result = q.getSingleResult();
        } catch (NoResultException nre) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find entity with key ={" + key + "=" + value + "}", nre);
            }
            //returns null
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find entity with id=" + key, e);
            }
            //returns null
        }
        
        return result;
    }

    /**
     * 
     * @param em
     * @param entityName
     * @param key1
     * @param value1
     * @param key2
     * @param value2
     * @return
     * @throws TransactionException 
     */
    public static Object getEntityByDualKeys(
    		JPATransactionContext jpaTransactionContext,
    		String entityName, 
    		String key1, String value1,
    		String key2, String value2) throws TransactionException {
    	return getEntityByDualKeys(jpaTransactionContext, entityName, key1, value1, key2, value2, null);
    }
    
    /**
     * 
     * @param em
     * @param entityName
     * @param key1
     * @param value1
     * @param key2
     * @param value2
     * @param tenantId
     * @return
     * @throws TransactionException 
     */
	public static Object getEntityByDualKeys(
    		JPATransactionContext jpaTransactionContext,
    		String entityName, 
    		String key1, String value1,
    		String key2, String value2,
            String tenantId) throws TransactionException {
    	Object result = null;
    	
        boolean useTenantId = useTenantId(tenantId);
        StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
        queryStrBldr.append(entityName);
        queryStrBldr.append(" a");
        queryStrBldr.append(" WHERE " + key1 + " = :" + key1);
        queryStrBldr.append(" AND " + key2 + " = :" + key2);
        if (useTenantId == true) {
            queryStrBldr.append(" AND tenantId = :tenantId");
        }
        String queryStr = queryStrBldr.toString(); //for debugging
        Query q = jpaTransactionContext.createQuery(queryStr);
        q.setParameter(key1, value1);
        q.setParameter(key2, value2);
        if (useTenantId == true) {
            q.setParameter("tenantId", tenantId);
        }
        
        result = q.getSingleResult();

        return result;
    }
	
	public static List getEntityListByDualKeys(JPATransactionContext jpaTransactionContext, String entityName,
			String key1, String value1, String key2, String value2, String tenantId) throws TransactionException {
		List result = null;

		boolean useTenantId = useTenantId(tenantId);
		StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
		queryStrBldr.append(entityName);
		queryStrBldr.append(" a");
		queryStrBldr.append(" WHERE " + key1 + " = :" + key1);
		queryStrBldr.append(" AND " + key2 + " = :" + key2);
		if (useTenantId == true) {
			queryStrBldr.append(" AND tenantId = :tenantId");
		}
		String queryStr = queryStrBldr.toString(); // for debugging
		Query q = jpaTransactionContext.createQuery(queryStr);
		q.setParameter(key1, value1);
		q.setParameter(key2, value2);
		if (useTenantId == true) {
			q.setParameter("tenantId", tenantId);
		}

		result = q.getResultList();

		return result;
	}
    
    /**
     * 
     * @param ctx
     * @param entityName
     * @param id
     * @param tenantId
     * @return
     * @throws TransactionException 
     */
    public static Object getEntity(
    		JPATransactionContext jpaTransactionContext,
    		DocumentFilter docFilter,
    		String entityName,
    		String id,
            String tenantId) throws TransactionException {
    	return getEnityByKey(jpaTransactionContext, docFilter, entityName, CSID_LABEL, id, tenantId);
    }

    /**
     * getEntity 
     * @param entityName fully qualified entity name
     * @param id
     * @param tenantId
     * @return null if entity is not found
     */
    @Deprecated
    public static Object oldgetEntity(String entityName, String id,
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
            
            boolean useTenantId = useTenantId(tenantId);
            if (useTenantId == true) {
                queryStrBldr.append(" AND tenantId = :tenantId");
            }
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);
            q.setParameter("csid", id);
            if (useTenantId) {
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

    public static Object getEntity(JPATransactionContext jpaTransactionContext, String entityName,
            String whereClause, HashMap<String, Object> paramBindings) {
        Object result = null;
        
        StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
        queryStrBldr.append(entityName);
        queryStrBldr.append(" a");
        queryStrBldr.append(" " + whereClause);

        String queryStr = queryStrBldr.toString(); //for debugging
        Query q = jpaTransactionContext.createQuery(queryStr);
        for (String paramName : paramBindings.keySet()) {
            q.setParameter(paramName, paramBindings.get(paramName));
        }
        
       	result = q.getSingleResult();
        
        if (result == null) {
        	logger.debug("Call to getEntity() returned empty set.");
        }
        
        return result;
    }
    
    public static Object getEntity(EntityManager em, String entityName,
            String whereClause, HashMap<String, Object> paramBindings) {
        Object result = null;
        
        StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
        queryStrBldr.append(entityName);
        queryStrBldr.append(" a");
        queryStrBldr.append(" " + whereClause);

        String queryStr = queryStrBldr.toString(); //for debugging
        Query q = em.createQuery(queryStr);
        for (String paramName : paramBindings.keySet()) {
            q.setParameter(paramName, paramBindings.get(paramName));
        }
        
        result = q.getSingleResult();
        
        if (result == null) {
        	logger.debug("Call to getEntity() returned empty set.");
        }
        
        return result;
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
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            o = getEntity(em, entityName, whereClause, paramBindings);
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
            throw e;
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
    	EntityManagerFactory result = null;

        result = getCachedEntityManagerFactory(persistenceUnit);
    	
		//
		// Try using a backup persistence unit if the specified one is not available and log a warning
		//
        if (result == null && !persistenceUnit.equalsIgnoreCase(CS_PERSISTENCE_UNIT)) {
            result = getCachedEntityManagerFactory(CS_PERSISTENCE_UNIT);
        }
    	
    	//
    	// One more try.
    	//
        if (result == null && !persistenceUnit.equalsIgnoreCase(CS_AUTHZ_PERSISTENCE_UNIT)) {
            result = getCachedEntityManagerFactory(CS_AUTHZ_PERSISTENCE_UNIT);
        }

        return result;
    }

    private static EntityManagerFactory getCachedEntityManagerFactory(
            String persistenceUnit) {
        EntityManagerFactory result = null;

        result = entityManagerFactoryCache.get(persistenceUnit);

        if (result == null) {
            try {
                result = Persistence.createEntityManagerFactory(persistenceUnit);
            } catch (javax.persistence.PersistenceException e) {
                logger.warn("Could not find a persistence unit for: " + persistenceUnit);
            }

            if (result != null) {
                entityManagerFactoryCache.put(persistenceUnit, result);
            }
        }

        return result;
    }

    /**
     * Release entity manager factory.
     *
     * @param emf the emf
     */
    public static void releaseEntityManagerFactory(EntityManagerFactory emf) {
        // CSPACE-6823: The conventional usage of this class has been to call
        // getEntityManagerFactory(), do something, and then immediately call
        // releaseEntityManagerFactory(). Now that EntityManagerFactory instances
        // are cached and re-used, they should not be closed after each use.
        // Instead, releaseEntityManagerFactories() should be called when the
        // services layer is stopped.
        
        // if (emf != null) {
        //     emf.close();
        // }
    }

    public static void releaseEntityManagerFactories() {
        for (EntityManagerFactory emf : entityManagerFactoryCache.values()) {
            emf.close();
        }
        
        entityManagerFactoryCache.clear();
    }
}

