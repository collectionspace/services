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
import javax.persistence.NoResultException;

import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionClient.ActionCompare;

import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for Permission
 * @author 
 */
@SuppressWarnings("rawtypes")
public class PermissionDocumentHandler
		extends JpaDocumentHandler<Permission, PermissionsList, Permission, List<Permission>> {

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
        }
    }
    
	private Permission findExistingPermission(Permission perm) throws TransactionException {
    	Permission result = null;
    	
		ServiceContext ctx = getServiceContext();
		String tenantId = ctx.getTenantId(); // we need a tenant ID 
		JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();
		try {
	    	result = (Permission)JpaStorageUtils.getEntityByDualKeys(jpaTransactionContext, 
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
		} finally {
			ctx.closeConnection();
		}

    	return result;
    }

    @Override
    public void handleCreate(DocumentWrapper<Permission> wrapDoc) throws EntityExistsException, DocumentException {
    	//
    	// First check to see if an equivalent permission exists
    	//
    	ServiceContext<Permission, Permission> ctx = getServiceContext();
    	Permission permission = wrapDoc.getWrappedObject();    	
    	Permission existingPermission = findExistingPermission(permission);

    	if (existingPermission == null) {
    		//
    		// If our call originates from an UPDATE/PUT request, then we can find a CSID in the service context
    		//
    		String id = (String)ctx.getProperty(PermissionClient.PERMISSION_UPDATE_CSID);
    		if (Tools.isEmpty(id) == true) {
    			id = UUID.randomUUID().toString();
    		}
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

    /**
     * Not used.  Due to an issue with the JPA 1.0 update mechanism, we had to perform the update process
     * in the PermissionResource class.  Look there for more details. 
     */
    @Deprecated
    @Override
    public void handleUpdate(DocumentWrapper<Permission> wrapDoc) throws Exception {
    }

    /*
     * Merge two Permission resources for an update/put request.
     */
    public Permission merge(Permission perm, Permission theUpdate) throws DocumentException {
    	Permission result = perm;
    	
        if (!Tools.isEmpty(theUpdate.getResourceName()) && !theUpdate.getResourceName().equalsIgnoreCase(perm.getResourceName())) {
        	String msg = String.format("Failed attempt to change Permission's (CSID='%S') resource name from '%s' to '%s'.",
        			perm.getCsid(), perm.getResourceName(), theUpdate.getResourceName());
            throw new DocumentException(msg);
        }

        if (theUpdate.getDescription() != null) {
            perm.setDescription(theUpdate.getDescription());
        }
        if (theUpdate.getEffect() != null) {
            perm.setEffect(theUpdate.getEffect());
        }
        //
        // Override the whole perm-action list, no reconciliation by design. We've
        // already cleaned-up and removed all the old perm-role relationships
        //
        // If the update didn't provide any new perm-actions, then we leave the
        // existing ones alone.
        //
        if (Tools.isEmpty(theUpdate.getAction()) == false) {
        	perm.setAction(theUpdate.getAction());
        	perm.setActionGroup(PermissionClient.getActionGroup(theUpdate.getAction()));
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("merged permission=" + JaxbUtils.toString(perm, Permission.class));
        }
        
        return result;
    }
    
    /**
     * Because of issues with JPA 1.0 not being able to propagate updates from the 'permissions' table to the related 'permissions_actions'
     * table, we need to handle updates in the PermissionResource class by deleting and creating the Permission resource
     */
    @SuppressWarnings("unchecked")
	@Override
	@Deprecated
    public void completeUpdate(DocumentWrapper<Permission> wrapDoc) throws Exception {
        Permission updatedPerm = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(updatedPerm);
        sanitize(updatedPerm);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void handleGet(DocumentWrapper<Permission> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        sanitize(getCommonPart());
        getServiceContext().setOutput(permission);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void handleGetAll(DocumentWrapper<List<Permission>> wrapDoc) throws Exception {
        PermissionsList permissionsList = extractCommonPartList(wrapDoc);
        setCommonPartList(permissionsList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public void completeDelete(DocumentWrapper<Permission> wrapDoc) throws Exception {
    }

    /*
     * See https://issues.collectionspace.org/browse/DRYD-181
     * 
     * For backward compatibility, we could not change the permission list to be a child class of AbstractCommonList.  This
     * would have change the result payload and would break existing API clients.  So the best we can do, it treat
     * the role list payload as a special case and return the paging information.
     * 
     */
	protected PermissionsList extractPagingInfoForPerms(PermissionsList permList, DocumentWrapper<List<Permission>> wrapDoc)
            throws Exception {

        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        permList.setPageNum(pageNum);
        permList.setPageSize(pageSize);
        List<Permission> docList = wrapDoc.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        permList.setItemsInPage(docList.size());
        // set the total result size
        permList.setTotalItems(docFilter.getTotalItemsResult());

        return permList;
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
            DocumentWrapper<List<Permission>> wrapDoc)
            throws Exception {

    	PermissionsList permissionsList = extractPagingInfoForPerms(new PermissionsList(), wrapDoc);
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
    @Override
    public void sanitize(Permission permission) {
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
