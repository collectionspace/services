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
 *  See the License for the specific language governing permissionRoles and
 *  limitations under the License.
 */
package org.collectionspace.services.authorization.storage;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;

import org.collectionspace.services.common.authorization_mgt.AuthorizationRoleRel;
import org.collectionspace.services.common.authorization_mgt.PermissionRoleUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaDocumentFilter;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Document handler for PermissionRole association.
 *
 * @author
 */
public class PermissionRoleDocumentHandler
		extends JpaDocumentHandler<PermissionRole, PermissionsRolesList, List<PermissionRoleRel>, List<PermissionRoleRel>> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(PermissionRoleDocumentHandler.class);
    
    /** The permission role. */
    private PermissionRole permissionRole;
    
    /** The permission roles list. */
    private PermissionsRolesList permissionRolesList;

    //
    /** The permission role csid. */
    private String permissionRoleCsid = null;

    /**
     * Sets the permission role csid.
     *
     * @param thePermissionRoleCsid the new permission role csid
     */
    public void setPermissionRoleCsid(String thePermissionRoleCsid) {
    	this.permissionRoleCsid = thePermissionRoleCsid;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc);
        filterOutExisting(wrapDoc);
    }
    
    private boolean permRoleRelExists(JPATransactionContext jpaTransactionContext, PermissionRoleRel permRoleRel) throws TransactionException {
    	boolean result = false;
    	
    	PermissionRoleRel queryResult = null;
    	try {
	    	queryResult = (PermissionRoleRel) JpaStorageUtils.getEntityByDualKeys(jpaTransactionContext, 
	    			PermissionRoleRel.class.getName(),
	    			PermissionStorageConstants.PERMREL_ROLE_ID, permRoleRel.getRoleId(), 
	    			PermissionStorageConstants.PERMREL_PERM_ID, permRoleRel.getPermissionId());
    	} catch (NoResultException e) {
    		// Ignore exception.  Just means the permission hasn't been stored/persisted yet.
    	}
    	
    	if (queryResult != null) {
    		result = true;
    	}
    	
    	return result;
    }
    
    /**
     * Find the existing (already persisted) 
     * @param wrapDoc
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	private void filterOutExisting(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
    	List<PermissionRoleRel> permRoleRelList = wrapDoc.getWrappedObject();
    	
    	ServiceContext ctx = getServiceContext();
        JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();
        try {
        	jpaTransactionContext.beginTransaction();
            
            for (PermissionRoleRel permRoleRel : permRoleRelList) {
            	if (permRoleRelExists(jpaTransactionContext, permRoleRel) == true) {
            		//
            		// Remove the item from the list since it already exists
            		//
            		permRoleRelList.remove(permRoleRel);
            	}
            }
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

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#completeCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeCreate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        PermissionRole pr = getCommonPart();
        AuthorizationDelegate.addRelationships(getServiceContext(), pr);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeUpdate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleGet(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void handleGet(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        getServiceContext().setOutput(permissionRole);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleGetAll(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleGetAll(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public boolean handleDelete(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc, true);
        return true;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#completeDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeDelete(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        PermissionRole pr = getCommonPart();
        AuthorizationDelegate.deletePermissionsFromRoles(getServiceContext(), pr);
    }

    /*
     * Turns a list of permission-role rows from the database into a PermissionRole object.  The list of rows
     * was the result of a query where the subject was either a Role or a Permission.
     * 
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public PermissionRole extractCommonPart(DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
            throws Exception {
        PermissionRole result = new PermissionRole();

        List<PermissionRoleRel> permissionRoleRel = wrapDoc.getWrappedObject();
        if (permissionRoleRel.size() == 0) {
            return result;
        }
        
        SubjectType subject = PermissionRoleUtil.getRelationSubject(getServiceContext());
        result.setSubject(subject);
        
        PermissionRoleRel prr0 = permissionRoleRel.get(0);
        if (SubjectType.ROLE.equals(subject)) {
        	//
        	// Since ROLE is the subject, they'll be just one Permission
        	//
            List<PermissionValue> permissionValueList = new ArrayList<PermissionValue>();
            result.setPermission(permissionValueList);
            PermissionValue pv = AuthorizationRoleRel.buildPermissionValue(prr0);
            permissionValueList.add(pv);
            //
            // Add role values
            //
            List<RoleValue> roleValueList = new ArrayList<RoleValue>();
            result.setRole(roleValueList);
            for (PermissionRoleRel prr : permissionRoleRel) {
                RoleValue rv = AuthorizationRoleRel.buildRoleValue(prr);
                roleValueList.add(rv);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {
        	//
        	// Since PERMISSION is the subject, they'll be just one Role and one or more Permissions
        	//
            List<RoleValue> roleValueList = new ArrayList<RoleValue>();
            result.setRole(roleValueList);
            RoleValue rv = AuthorizationRoleRel.buildRoleValue(prr0);
            roleValueList.add(rv);
            //
            // Add permssions values
            //
            List<PermissionValue> permissionValueList = new ArrayList<PermissionValue>();
            result.setPermission(permissionValueList);
            for (PermissionRoleRel prr : permissionRoleRel) {
                PermissionValue pv = AuthorizationRoleRel.buildPermissionValue(prr);
                permissionValueList.add(pv);
            }
        }
        
        return result;
    }

    /**
     * Fill common part.
     *
     * @param pr the pr
     * @param wrapDoc the wrap doc
     * @param handleDelete the handle delete
     * @throws Exception the exception
     */
    @SuppressWarnings("rawtypes")
	public void fillCommonPart(PermissionRole pr,
    			DocumentWrapper<List<PermissionRoleRel>> wrapDoc,
    			boolean handleDelete)
            throws Exception {
        List<PermissionRoleRel> prrl = wrapDoc.getWrappedObject();
        SubjectType subject = pr.getSubject();
        if (subject == null) {
            //it is not required to give subject as URI determines the subject
            subject = PermissionRoleUtil.getRelationSubject(getServiceContext());
        } else {
            //subject mismatch should have been checked during validation
        }
        
        ServiceContext ctx = this.getServiceContext();
        String tenantId = ctx.getTenantId();
        try {
        	PermissionRoleUtil.buildPermissionRoleRel(ctx, pr, subject, prrl, handleDelete, tenantId);
        } catch (DocumentNotFoundException dnf) {
        	String msg = String.format("The following perm-role payload references permissions and/or roles that do not exist: \n%s",
        			JaxbUtils.toString(pr, PermissionRole.class));
        	throw new DocumentException(msg);
        }
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(PermissionRole ar,
    		DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
    			throws Exception {
    	fillCommonPart(ar, wrapDoc, false);
    }    

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public PermissionsRolesList extractCommonPartList(
            DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
            throws Exception {

        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getCommonPart()
     */
    @Override
    public PermissionRole getCommonPart() {
        return permissionRole;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#setCommonPart(java.lang.Object)
     */
    @Override
    public void setCommonPart(PermissionRole permissionRole) {
        this.permissionRole = permissionRole;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getCommonPartList()
     */
    @Override
    public PermissionsRolesList getCommonPartList() {
        return permissionRolesList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(PermissionsRolesList permissionRolesList) {
        this.permissionRolesList = permissionRolesList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#createDocumentFilter()
     */
    @Override
    public DocumentFilter createDocumentFilter() {
        return new JpaDocumentFilter(this.getServiceContext());
    }
}
