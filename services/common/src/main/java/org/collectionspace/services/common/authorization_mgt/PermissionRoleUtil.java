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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.EffectType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.storage.PermissionStorageConstants;
import org.collectionspace.services.authorization.PermissionResource;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
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
    static public void buildPermissionRoleRel(JPATransactionContext jpaTransactionContext, 
    		PermissionRole pr,
    		SubjectType subject,
    		List<PermissionRoleRel> prrl,
    		boolean handleDelete,
    		String tenantId) throws Exception {
    	
        if (subject.equals(SubjectType.ROLE)) {
        	List<PermissionValue> permissionValues = pr.getPermission();
        	if (permissionValues != null && permissionValues.size() > 0) {
	            PermissionValue pv = permissionValues.get(0);
	            for (RoleValue rv : pr.getRole()) {
	                PermissionRoleRel prr = buildPermissonRoleRel(jpaTransactionContext, pv, rv, subject, handleDelete, tenantId);
	                prrl.add(prr);
	            }
        	}
        } else if (subject.equals(SubjectType.PERMISSION)) {
        	List<RoleValue> roleValues = pr.getRole();
        	if (roleValues != null && roleValues.size() > 0) {
	            RoleValue rv = roleValues.get(0);
	            for (PermissionValue pv : pr.getPermission()) {
	                PermissionRoleRel prr = buildPermissonRoleRel(jpaTransactionContext, pv, rv, subject, handleDelete, tenantId);
	                prrl.add(prr);
	            }
        	}
        }
    }
    
    static public void buildPermissionRoleRel(
    		ServiceContext ctx,
    		PermissionRole pr,
    		SubjectType subject,
    		List<PermissionRoleRel> prrl,
    		boolean handleDelete,
    		String tenantId) throws Exception {
    	
        JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();
        try {
            jpaTransactionContext.beginTransaction();            
            buildPermissionRoleRel(jpaTransactionContext, pr, subject, prrl, handleDelete, tenantId);
            jpaTransactionContext.commitTransaction();
        } catch (Exception e) {
        	jpaTransactionContext.markForRollback();
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw e;
        } finally {
            ctx.closeConnection();
        }
    }    

    /*
     * Try to find a persisted Permission record using a PermissionValue instance.
     *
     */
    static private Permission lookupPermission(JPATransactionContext jpaTransactionContext, PermissionValue permissionValue, String tenantId) throws TransactionException {
    	Permission result = null;
    	
    	String actionGroup = permissionValue.getActionGroup() != null ? permissionValue.getActionGroup().trim() : null;
    	String resourceName = permissionValue.getResourceName() != null ? permissionValue.getResourceName().trim() : null;
    	String permissionId = permissionValue.getPermissionId() != null ? permissionValue.getPermissionId().trim() : null;
    	//
    	// If we have a permission ID, use it to try to lookup the persisted permission
    	//
    	if (permissionId != null && !permissionId.isEmpty()) {
	    	try {
		    	result = (Permission)JpaStorageUtils.getEntity(jpaTransactionContext, permissionId, Permission.class);
	    	} catch (Throwable e) {
	    		String msg = String.format("Searched for but couldn't find a permission with CSID='%s'.",
	    				permissionId);
	    		logger.trace(msg);
	    	}
    	} else if ((resourceName != null && !resourceName.isEmpty()) && 
    			(actionGroup != null && !actionGroup.isEmpty())) {
    		//
    		// If there was no permission ID, then we can try to find the permission with the resource name and action group tuple
    		//
	    	try {
		    	result = (Permission)JpaStorageUtils.getEntityByDualKeys(jpaTransactionContext, 
		    			Permission.class.getName(),
		    			PermissionStorageConstants.RESOURCE_NAME, permissionValue.getResourceName(), 
		    			PermissionStorageConstants.ACTION_GROUP, permissionValue.getActionGroup(),
		    			tenantId);
	    	} catch (NoResultException e) {
	    		String msg = String.format("Searched for but couldn't find a permission for resource='%s', action group='%s', and tenant ID='%s'.",
	    				permissionValue.getResourceName(), permissionValue.getActionGroup(), tenantId);
	    		logger.trace(msg);
	    	}
    	} else {
    		String errMsg = String.format("Couldn't perform lookup of permission.  Not enough information provided.  Lookups requires a permission CSID or a resource name and action group tuple.  The provided information was permission ID='%s', resourceName='%s', and actionGroup='%s'.",
    				permissionId, resourceName, actionGroup);
    		logger.warn(errMsg);
    	}
    	
    	return result;
    }
    
    /**
     * Builds a permisson role relationship for either 'create' or 'delete'
     *
     * @param pv the pv (currently using only the ID)
     * @param rv the rv (currently using only the ID)
     * @param handleDelete the handle delete
     * @return the permission role rel
     * @throws DocumentException 
     */
    static private PermissionRoleRel buildPermissonRoleRel(JPATransactionContext jpaTransactionContext, PermissionValue permissionValue,
    		RoleValue roleValue,
    		SubjectType subject,
    		boolean handleDelete,  // if 'true' then we're deleting not building a permission-role record
    		String tenantId) throws DocumentException {

    	PermissionRoleRel result = null;
    	Permission permission = lookupPermission(jpaTransactionContext, permissionValue, tenantId);
    	
    	//
    	// If we couldn't find an existing permission and we're not processing a DELETE request, we need to create
    	// a new permission.
    	//
    	if (permission == null && handleDelete == false) {
    		permission = new Permission();
    		permission.setResourceName(permissionValue.getResourceName());
    		permission.setActionGroup(permissionValue.getActionGroup());
    		permission.setEffect(EffectType.PERMIT); // By default, CollectionSpace currently (11/2017) supports only PERMIT
    		List<PermissionAction> actionList = createPermActionList(permissionValue.getActionGroup());
    		permission.setAction(actionList);
    		permission = createPermission(jpaTransactionContext, permission);
    		if (permission == null) {
    			String errMsg = "Could not create new permission for new permission-role relationship.";
    			throw new DocumentException(errMsg);
    		}
    	}
    	
    	//
    	// Since our permissionValue may not have been supplied by the client with an ID, we need
    	// to add it now.
    	//
    	if (permissionValue.getPermissionId() == null || permissionValue.getPermissionId().trim().isEmpty()) {
    		permissionValue.setPermissionId(permission.getCsid());
    	}
    	
    	//
    	// Create the permission-role to persist
    	//
        result = new PermissionRoleRel();
        result.setPermissionId(permission.getCsid());
        result.setPermissionResource(permission.getResourceName());
        result.setActionGroup(permission.getActionGroup());
        result.setRoleId(roleValue.getRoleId());
        result.setRoleName(roleValue.getRoleName());
        
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

    private static Permission createPermission(JPATransactionContext jpaTransactionContext, Permission permission) {
		Permission result = null;
		
		PermissionResource permissionResource = new PermissionResource();  // Get the PermissionResource singleton instance (RESTEasy ensures it is a singleton)
		result = permissionResource.createPermissionFromInstance(jpaTransactionContext, permission);
		
		return result;
	}

	private static List<PermissionAction> createPermActionList(String actionGroup) throws DocumentException {
    	ArrayList<PermissionAction> result = new ArrayList<PermissionAction>();
    	
    	for (char c : actionGroup.toUpperCase().toCharArray()) {
    		PermissionAction permAction = new PermissionAction();
    		switch (c) {
	    		case 'C':
	    			permAction.setName(ActionType.CREATE);
	    			break;
	    			
	    		case 'R':
	    			permAction.setName(ActionType.READ);
	    			break;
	    			
	    		case 'U':
	    			permAction.setName(ActionType.UPDATE);
	    			break;
	    			
	    		case 'D':
	    			permAction.setName(ActionType.DELETE);
	    			break;
	    			
	    		case 'L':
	    			permAction.setName(ActionType.SEARCH);
	    			break;
	    			
	    		default:
	    			String errMsg = String.format("Illegal action group token '%c' in permission action group '%s'.",
	    					c, actionGroup);
	    			throw new DocumentException(errMsg);
    		}
    		
    		if (result.add(permAction) == false) {
    			String warnMsg = String.format("Illegal or duplicate action group token '%c' in permission action group '%s'.",
    					c, actionGroup);
    			logger.warn(warnMsg);
    		}
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
