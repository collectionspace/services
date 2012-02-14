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

import javax.persistence.PersistenceException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
import org.collectionspace.services.common.authorization_mgt.AuthorizationRoleRel;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.document.JaxbUtils;
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
    public final static String CS_CURRENT_USER = "0";
    
    // This is the column name for ID field of all the JPA objects
    public static final String CSID_LABEL = "csid";

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
			CSpaceResource res = new URIResourceImpl("accounts", "GET");
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
    
    public static Object getEnityByKey(String entityName, String key, String value,
            String tenantId) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Object o = null;
        
        if (entityName == null) {
            throw new IllegalArgumentException("entityName is required");
        }
        if (key == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        try {
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(entityName);
            queryStrBldr.append(" a");
            queryStrBldr.append(" WHERE " + key + " = :" + key);
            boolean csAdmin = SecurityUtils.isCSpaceAdmin();
            if (!csAdmin) {
                queryStrBldr.append(" AND tenantId = :tenantId");
            }
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            Query q = em.createQuery(queryStr);
            q.setParameter(key, value);
            if (!csAdmin) {
                q.setParameter("tenantId", tenantId);
            }
            o = q.getSingleResult();
        } catch (NoResultException nre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find entity with key ={" + key + "=" + value + "}", nre);
            }
            //returns null
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("could not find entity(2) with id=" + key, e);
            }
            //returns null
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
        
        return o;
    }

    public static Object getEntity(String entityName, String id,
            String tenantId) {
    	return getEnityByKey(entityName, CSID_LABEL, id, tenantId);
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

