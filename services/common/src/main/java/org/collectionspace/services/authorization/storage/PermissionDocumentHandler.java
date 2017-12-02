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
package org.collectionspace.services.authorization.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionClient.ActionCompare;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.authorization_mgt.AuthorizationStore;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for Permission
 * @author 
 */
public class PermissionDocumentHandler
		extends JpaDocumentHandler<Permission, PermissionsList, Permission, List> {

    private final Logger logger = LoggerFactory.getLogger(PermissionDocumentHandler.class);
    private Permission permission;
    private PermissionsList permissionsList;
    
    public CSpaceAction getAction(ActionType action) {
        if (ActionType.CREATE.name().equals(action.name())) {
            return CSpaceAction.CREATE;
        } else if (ActionType.READ.equals(action)) {
            return CSpaceAction.READ;
        } else if (ActionType.UPDATE.equals(action)) {
            return CSpaceAction.UPDATE;
        } else if (ActionType.DELETE.equals(action)) {
            return CSpaceAction.DELETE;
        } else if (ActionType.SEARCH.equals(action)) {
            return CSpaceAction.SEARCH;
        } else if (ActionType.ADMIN.equals(action)) {
            return CSpaceAction.ADMIN;
        } else if (ActionType.START.equals(action)) {
            return CSpaceAction.START;
        } else if (ActionType.STOP.equals(action)) {
            return CSpaceAction.STOP;
        }
        //
        // We could not find a match, so we need to throw an exception.
        //
        throw new IllegalArgumentException("action = " + action.toString());
    }
    
    /*
     * Add the ACE hashed ID to the permission action so we can map the permission to the Spring Security
     * tables.
     */
    private void handlePermissionActions(Permission perm) throws DocumentException {
    	//
    	// Verify the permission actions.  If the action group is missing, create it from the action list and vice versa.
    	//
    	ActionCompare compareResult = PermissionClient.validatePermActions(perm);
    	switch (compareResult) {
	    	case ACTIONS_MISSING:
	    		String msg = "Permission resource encountered with missing action group and action list.";
	    		throw new DocumentException(msg);
	    		
			case ACTION_GROUP_EMPTY:
				String actionGroup = PermissionClient.getActionGroup(perm.getAction());
				perm.setActionGroup(actionGroup);
				break;
				
			case ACTION_LIST_EMPTY:
				List<PermissionAction> actionList = PermissionClient.getActionList(perm.getActionGroup());
				perm.setAction(actionList);
				break;
				
			case MATCHES:
				// all good
				break;
				
			case MISMATCHES:
				msg = String.format("Permission has mismatching action group and action list.  Action group='%s' and Action list = '%s'.",
						perm.getActionGroup(), PermissionClient.getActionGroup(perm.getAction()));
				throw new DocumentException(msg);
    	}
    	
        List<PermissionAction> permActions = perm.getAction();
        for (PermissionAction permAction : permActions) {
            CSpaceAction action = getAction(permAction.getName());
            URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(), perm.getResourceName(), action);
            permAction.setObjectIdentity(uriRes.getHashedId().toString());
            permAction.setObjectIdentityResource(uriRes.getId());
            //PermissionActionUtil.update(perm, permAction);
        }
    }
    
    private Permission findExistingPermission(EntityManager em, Permission perm) {
    	Permission result = null;
		String tenantId = getServiceContext().getTenantId(); // we need a tenant ID 
        
		try {
	    	result = (Permission)JpaStorageUtils.getEntityByDualKeys(em, 
	    			Permission.class.getName(),
	    			PermissionStorageConstants.RESOURCE_NAME, perm.getResourceName(), 
	    			PermissionStorageConstants.ACTION_GROUP, perm.getActionGroup(),
	    			tenantId);
		} catch (NoResultException e) {
			if (logger.isTraceEnabled()) {
				String msg = String.format("Looked for but could not find permission with resource name = '%s', action group = '%s', tenat ID = '%s'.",
						perm.getResourceName(), perm.getActionGroup(), tenantId);
				logger.trace(msg);
			}
		}

    	return result;
    }

    @Override
    public void handleCreate(DocumentWrapper<Permission> wrapDoc) throws EntityExistsException, DocumentException {
    	//
    	// First check to see if an equivalent permission exists
    	//
    	Permission permission = wrapDoc.getWrappedObject();
    	
    	EntityManager em = (EntityManager) this.getServiceContext().getProperty(AuthorizationStore.ENTITY_MANAGER_PROP_KEY);
    	Permission existingPermission = findExistingPermission(em, permission);

    	if (existingPermission == null) {
	        String id = UUID.randomUUID().toString();        
	        permission.setCsid(id);
	        setTenant(permission);
	        handlePermissionActions(permission);
    	} else {
    		String msg = String.format("Found existing permission with resource name = '%s', action group = '%s', and tenant ID = '%s'.",
    				existingPermission.getResourceName(), existingPermission.getActionGroup(), existingPermission.getTenantId());
    		wrapDoc.resetWrapperObject(existingPermission); // update the wrapped document with the existing permission instance
    		throw new EntityExistsException(msg);
    	}
    }

    @Override
    public void completeCreate(DocumentWrapper<Permission> wrapDoc) throws Exception {
    }

    @Override
    public void handleUpdate(DocumentWrapper<Permission> wrapDoc) throws Exception {
        Permission permissionFound = wrapDoc.getWrappedObject();
        Permission permissionReceived = getCommonPart();
        merge(permissionReceived, permissionFound);
    }

    /**
     * merge manually merges the from from to the to permission
     * -this method is created due to inefficiency of JPA EM merge
     * @param from
     * @param to
     * @return merged permission
     */
    private Permission merge(Permission from, Permission to) throws Exception {
        if (!(from.getResourceName().equalsIgnoreCase(to.getResourceName()))) {
            String msg = "Resource name cannot be changed " + to.getResourceName();
            logger.error(msg);
            throw new BadRequestException(msg);
        }
        //resource name, attribute  cannot be changed

        if (from.getDescription() != null) {
            to.setDescription(from.getDescription());
        }
        if (from.getEffect() != null) {
            to.setEffect(from.getEffect());
        }
        List<PermissionAction> fromActions = from.getAction();
        if (!fromActions.isEmpty()) {
            // Override the whole list, no reconciliation by design
            to.setAction(fromActions);
            // Update the actionGroup field to reflect the new action list
            to.setActionGroup(PermissionClient.getActionGroup(fromActions));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("merged permission=" + JaxbUtils.toString(to, Permission.class));
        }

        handlePermissionActions(to);
        return to;
    }

    @Override
    public void completeUpdate(DocumentWrapper<Permission> wrapDoc) throws Exception {
        Permission upAcc = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(upAcc);
        sanitize(upAcc);
        //FIXME update lower-layer authorization (acls)
        //will require deleting old permissions for this resource and adding
        //new based on new actions and effect
    }

    @Override
    public void handleGet(DocumentWrapper<Permission> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        sanitize(getCommonPart());
        getServiceContext().setOutput(permission);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List> wrapDoc) throws Exception {
        PermissionsList permissionsList = extractCommonPartList(wrapDoc);
        setCommonPartList(permissionsList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public void completeDelete(DocumentWrapper<Permission> wrapDoc) throws Exception {
    }

    @Override
    public Permission extractCommonPart(
            DocumentWrapper<Permission> wrapDoc)
            throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public void fillCommonPart(Permission obj, DocumentWrapper<Permission> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountDocumentHandler");
    }

    @Override
    public PermissionsList extractCommonPartList(
            DocumentWrapper<List> wrapDoc)
            throws Exception {

        PermissionsList permissionsList = new PermissionsList();
        List<Permission> list = new ArrayList<Permission>();
        permissionsList.setPermission(list);
        for (Object obj : wrapDoc.getWrappedObject()) {
            Permission permission = (Permission) obj;
            sanitize(permission);
            list.add(permission);
        }
        return permissionsList;
    }

    @Override
    public Permission getCommonPart() {
        return permission;
    }

    @Override
    public void setCommonPart(Permission permission) {
        this.permission = permission;
    }

    @Override
    public PermissionsList getCommonPartList() {
        return permissionsList;
    }

    @Override
    public void setCommonPartList(PermissionsList permissionsList) {
        this.permissionsList = permissionsList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter() {
        DocumentFilter filter = new PermissionJpaFilter(this.getServiceContext());
        return filter;
    }

    /**
     * Sanitize removes data not needed to be sent to the consumer
     * @param permission
     */
    private void sanitize(Permission permission) {
        if (!SecurityUtils.isCSpaceAdmin()) {
            // permission.setTenantId(null); // REM - Why are we removing the tenant ID from the payload? Commenting out this line for now.
        }
    }

    private void setTenant(Permission permission) {
        //set tenant only if not available from input
        if (permission.getTenantId() == null || permission.getTenantId().isEmpty()) {
            permission.setTenantId(getServiceContext().getTenantId());
        }
    }
}
