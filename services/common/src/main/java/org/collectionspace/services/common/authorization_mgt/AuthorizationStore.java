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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.common.authorization_mgt;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.common.authorization_mgt.RoleStorageConstants;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorizationStore stores persistent entities during import
 * @author
 */
public class AuthorizationStore {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationStore.class);
    private final static String PERSISTENCE_UNIT = "org.collectionspace.services.authorization";

    static public Role getRoleByName(String roleName, String tenantId) {
    	Role theRole = null;
    	
    	try {
    		theRole = (Role)JpaStorageUtils.getEnityByKey(Role.class.getName(),
    				RoleStorageConstants.ROLE_NAME, roleName, tenantId);
    	} catch (Throwable e) {
    		if (logger.isTraceEnabled() == true) {
    			logger.trace("Could not retrieve role with name =" + roleName, e);
    		}
    	}
    	
    	return theRole;
    }
    
    static public Role getRoleByName(EntityManager em, String roleName, String tenantId) {
    	Role theRole = null;
    	
    	try {
    		theRole = (Role)JpaStorageUtils.getEnityByKey(em, Role.class.getName(),
    				RoleStorageConstants.ROLE_NAME, roleName, tenantId);
    	} catch (Throwable e) {
    		if (logger.isTraceEnabled() == true) {
    			logger.trace("Could not retrieve role with name =" + roleName, e);
    		}
    	}
    	
    	return theRole;
    }
    
    
    static public PermissionRoleRel getPermRoleRel(EntityManager em, String permId, String roleId) {
    	PermissionRoleRel permRoleRel = null;
    	
    	try {
    		permRoleRel = (PermissionRoleRel)JpaStorageUtils.getEntityByDualKeys(em, 
    				PermissionRoleRel.class.getName(),
    				RoleStorageConstants.PERM_ROLE_REL_PERM_ID, permId, 
    				RoleStorageConstants.PERM_ROLE_REL_ROLE_ID, roleId);
    	} catch (Throwable e) {
    		if (logger.isTraceEnabled()) {
    			logger.trace("Could not retrieve permissionRoleRel with permId =" + permId 
    					+" and roleId="+roleId, e);
    		}
    	}
    	
    	return permRoleRel;
    }
    
    
    static public Permission getPermission(Permission permission) {
    	Permission result = null;
    	//
    	// We need to perform a DB lookup to see if this permission already exists.  If so,
    	// we should return the existing permission.
    	//
    	result = permission;
    	
    	return result;
    }
    
    /**
     * store the given entity
     * @param entity
     * @return csid of the entity
     * @throws Exception
     */
    public String store(Object entity) throws Exception {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = JpaStorageUtils.getEntityManagerFactory(PERSISTENCE_UNIT);
            em = emf.createEntityManager();
            //FIXME: more efficient would be to participate in transaction already started
            //by the caller
            em.getTransaction().begin();
            if (JaxbUtils.getValue(entity, "getCreatedAt") == null) {
                JaxbUtils.setValue(entity, "setCreatedAtItem", Date.class, new Date());
            }
            em.persist(entity);
            em.getTransaction().commit();
            String id = null;
            try{
                id = (String) JaxbUtils.getValue(entity, "getCsid"); //NOTE: Not all entities have a CSID attribute
            } catch(NoSuchMethodException nsme) {
                //do nothing ok, relationship does not have csid
            }
            return id;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw e;
        } finally {
            if (em != null) {
            	em.clear();
            	em.close();
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }
    
    private boolean exists(EntityManager em, Object entity) {
    	boolean result = false;
    	
    	try {
    		if(entity instanceof Role) {
    			// If find by name, exists
    			Role roleEntity = (Role)entity;
    			String roleName = roleEntity.getRoleName();
    			String tenantId = roleEntity.getTenantId();
    			if(getRoleByName(em, roleName, tenantId)!=null) {
    				result = true;
    	    		logger.trace("Role {} already exists in tenant {}.", roleName, tenantId);
    			} else {
    	    		logger.trace("Role {} does not exist in tenant {}.", roleName, tenantId);
    			}
    		} else if(entity instanceof PermissionRoleRel) {
    			// If find by name, exists
    			PermissionRoleRel permRoleEntity = (PermissionRoleRel)entity;
    			String roleId = permRoleEntity.getRoleId();
    			String permId = permRoleEntity.getPermissionId();
    			if(getPermRoleRel(em, permId, roleId)!=null) {
    				result = true;
    	    		logger.trace("PermRoleRel for {}, {} already exists.", permId, roleId);
    			} else {
    	    		logger.trace("PermRoleRel for {}, {} does not exist.", permId, roleId);
    			}
    		} else {	// Default case; also best test for Permission
    			String csid = (String)JaxbUtils.getValue(entity, "getCsid");
    			Object existingEntity = em.find(entity.getClass(), csid);
    			if (existingEntity != null) {
    				result = true;
    	    		logger.trace("Entity with csid {} already exists.", csid);
    			} else {
    	    		logger.trace("Entity with csid {} does not exist.", csid);
    			}
    		}
    	} catch (Exception e) {
    		//NOTE: Not all entities have a CSID attribute
    	}
    	
    	return result;
    }
    /*
     * Use this method if you've already started a transaction with an EntityManager
     */
    public String store(EntityManager em, Object entity) throws Exception {
    	boolean entityExists = exists(em, entity);
    	/* 
    	 * Logging moved to exists, for better detail
    	if (entityExists == true) {
    		logger.trace("Entity to persist already exists.");
    	}
    	 */
        if (JaxbUtils.getValue(entity, "getCreatedAt") == null) {
            JaxbUtils.setValue(entity, "setCreatedAtItem", Date.class, new Date());
        }
        
        if (entityExists == true) {
        	//em.merge(entity); FIXME: Leave commented out until we address CSPACE-5031
        	// PLS: Question: why merge? what might be new to change, and is this really a good idea?
        	// Shouldn't we define them once and leave them alone?
        } else {
        	em.persist(entity);
        }
        
        // look for a CSID
        String id = null;
        try{
            id = (String) JaxbUtils.getValue(entity, "getCsid"); //NOTE: Not all entities have a CSID attribute
        } catch(NoSuchMethodException nsme) {
            //do nothing ok, relationship does not have csid
        }
        return id;
    }
}
