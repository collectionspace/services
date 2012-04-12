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

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.authorization_mgt;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.common.storage.jpa.JpaRelationshipStorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class PermissionRoleUtil.
 *
 * @author
 */
public class PermissionRoleUtil {

    static final Logger logger = LoggerFactory.getLogger(PermissionRoleUtil.class);

    /**
     * Gets the relation subject.
     *
     * @param ctx the ctx
     * @return the relation subject
     */
    static public SubjectType getRelationSubject(ServiceContext ctx) {
        Object o = ctx.getProperty(ServiceContextProperties.SUBJECT);
        if (o == null) {
            throw new IllegalArgumentException(ServiceContextProperties.SUBJECT
                    + " property is missing in context "
                    + ctx.toString());
        }
        return (SubjectType) o;
    }

    /**
     * Gets the relation subject.
     *
     * @param ctx the ctx
     * @param pr the pr
     * @return the relation subject
     */
    static public SubjectType getRelationSubject(ServiceContext ctx, PermissionRole pr) {
        SubjectType subject = pr.getSubject();
        if (subject == null) {
            //it is not required to give subject as URI determines the subject
            subject = getRelationSubject(ctx);
        }
        return subject;
    }

    /**
     * buildPermissionRoleRel builds persistent relationship entities from given
     * permissionrole.
     *
     * @param pr permissionrole
     * @param subject the subject
     * @param prrl persistent entities built are inserted into this list
     * @param toDelete the to delete
     */
    static public void buildPermissionRoleRel(EntityManager em, 
    		PermissionRole pr,
    		SubjectType subject,
    		List<PermissionRoleRel> prrl,
    		boolean handleDelete) throws Exception {
        if (subject.equals(SubjectType.ROLE)) {
        	List<PermissionValue> permissionValues = pr.getPermission();
        	if (permissionValues != null && permissionValues.size() > 0) {
	            PermissionValue pv = permissionValues.get(0);
	            for (RoleValue rv : pr.getRole()) {
	                PermissionRoleRel prr = buildPermissonRoleRel(em, pv, rv, subject, handleDelete);
	                prrl.add(prr);
	            }
        	}
        } else if (subject.equals(SubjectType.PERMISSION)) {
        	List<RoleValue> roleValues = pr.getRole();
        	if (roleValues != null && roleValues.size() > 0) {
	            RoleValue rv = roleValues.get(0);
	            for (PermissionValue pv : pr.getPermission()) {
	                PermissionRoleRel prr = buildPermissonRoleRel(em, pv, rv, subject, handleDelete);
	                prrl.add(prr);
	            }
        	}
        }
    }
    
    static public void buildPermissionRoleRel( 
    		PermissionRole pr,
    		SubjectType subject,
    		List<PermissionRoleRel> prrl,
    		boolean handleDelete) throws Exception {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = JpaStorageUtils.getEntityManagerFactory(JpaStorageUtils.CS_PERSISTENCE_UNIT);
            em = emf.createEntityManager();
            em.getTransaction().begin();
            
            buildPermissionRoleRel(em, pr, subject, prrl, handleDelete);
            
            em.getTransaction().commit();
        	em.close();            
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
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }    

    /**
     * Builds a permisson role relationship for either 'create' or 'delete'
     *
     * @param pv the pv (currently using only the ID)
     * @param rv the rv (currently using only the ID)
     * @param handleDelete the handle delete
     * @return the permission role rel
     */
    static private PermissionRoleRel buildPermissonRoleRel(EntityManager em, PermissionValue permissionValue,
    		RoleValue roleValue,
    		SubjectType subject,
    		boolean handleDelete)
    			throws DocumentNotFoundException {

    	PermissionRoleRel result = null;
    	
    	//
    	// Ensure we can find both the Permission and Role to relate.
    	// FIXME: REM - This is a workaround until the Import utility creates Perm/Role relationships
    	// correctly.  The import utility should create and store the permissions and roles BEFORE creating the relationships
    	//
    	PermissionValue pv = permissionValue;
    	
    	//
    	// This lookup is slow, do we really need it?
    	//
    	/*
    	try {
	    	Permission permission = (Permission)JpaStorageUtils.getEntity(em, pv.getPermissionId(), //FIXME: REM 4/5/2012 - To improve performance, we should use a passed in Permission instance
	    			Permission.class);
	    	if (permission != null) {
	    		// If the permission already exists, then use it to fill our the relation record
	    		pv = JpaRelationshipStorageClient.createPermissionValue(permission);
	    	}
    	} catch (DocumentNotFoundException e) {
    		// ignore this exception, pv is set to permissionValue;
    	}
    	*/
    	
    	//
    	// Ensure we can find both the Permission and Role to relate.
    	// FIXME: REM - This is a workaround until the Import utility creates Perm/Role relationships
    	// correctly.  The import utility should create and store the permissions and roles BEFORE creating the relationships
    	//
    	RoleValue rv = roleValue;
    	
    	/*
    	 * This lookup is slow, can we avoid it?
    	try {
	    	Role role = (Role)JpaStorageUtils.getEntity(em, rv.getRoleId(),
	    			Role.class);
	    	if (role != null) {
	    		// If the role already exists, then use it to fill out the relation record
	    		rv = JpaRelationshipStorageClient.createRoleValue(role);
	    	}
    	} catch (DocumentNotFoundException e) {
    		// ignore this exception, rv is set to roleValue
    	}
    	 */
    	
        result = new PermissionRoleRel();
        result.setPermissionId(pv.getPermissionId());
        result.setPermissionResource(pv.getResourceName());
        result.setActionGroup(pv.getActionGroup());
        result.setRoleId(rv.getRoleId());
        result.setRoleName(rv.getRoleName());
        //
        // For 'delete' we need to set the hjid of the existing relstionship
        //
        String relationshipId = null;
        if (subject.equals(SubjectType.ROLE) == true) {
        	relationshipId = roleValue.getRoleRelationshipId();
        } else if (subject.equals(SubjectType.PERMISSION) == true) {
        	relationshipId = permissionValue.getPermRelationshipId();
        }
        if (relationshipId != null && handleDelete == true) {
        	result.setHjid(Long.parseLong(relationshipId));  // set this so we can convince JPA to del the relation
        }
    	
        return result;
    }

    /**
     * Checks if is invalid tenant.
     *
     * @param tenantId the tenant id
     * @param msgBldr the msg bldr
     * @return true, if is invalid tenant
     */
    static boolean isInvalidTenant(String tenantId, StringBuilder msgBldr) {
        boolean invalid = false;

        if (tenantId == null || tenantId.isEmpty()) {
            invalid = true;
            msgBldr.append("\n tenant : tenantId is missing");
        }
        String whereClause = "where id = :id";
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("id", tenantId);

        Object tenantFound = JpaStorageUtils.getEntity(
                "org.collectionspace.services.account.Tenant", whereClause, params);
        if (tenantFound == null) {
            invalid = true;
            msgBldr.append("\n tenant : tenantId=" + tenantId
                    + " not found");
        }
        return invalid;
    }
}
