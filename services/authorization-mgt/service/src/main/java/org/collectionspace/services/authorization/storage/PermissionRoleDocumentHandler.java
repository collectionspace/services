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

import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;

import org.collectionspace.services.common.authorization_mgt.AuthorizationRoleRel;
import org.collectionspace.services.common.authorization_mgt.PermissionRoleUtil;

import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;
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
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#completeCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeCreate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        PermissionRole pr = getCommonPart();
        AuthorizationDelegate.addPermissions(getServiceContext(), pr);
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
    public void handleDelete(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc, true);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#completeDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeDelete(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        PermissionRole pr = getCommonPart();
        AuthorizationDelegate.deletePermissions(getServiceContext(), pr);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public PermissionRole extractCommonPart(
            DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
            throws Exception {
        List<PermissionRoleRel> prrl = wrapDoc.getWrappedObject();
        PermissionRole pr = new PermissionRole();
        SubjectType subject = PermissionRoleUtil.getRelationSubject(getServiceContext());
        if (prrl.size() == 0) {
            return pr;
        }
        PermissionRoleRel prr0 = prrl.get(0);
        if (SubjectType.ROLE.equals(subject)) {

            List<PermissionValue> pvs = new ArrayList<PermissionValue>();
            pr.setPermission(pvs);
            PermissionValue pv = AuthorizationRoleRel.buildPermissionValue(prr0);
            pvs.add(pv);

            //add roles
            List<RoleValue> rvs = new ArrayList<RoleValue>();
            pr.setRole(rvs);
            for (PermissionRoleRel prr : prrl) {
                RoleValue rv = AuthorizationRoleRel.buildRoleValue(prr);
                rvs.add(rv);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {

            List<RoleValue> rvs = new ArrayList<RoleValue>();
            pr.setRole(rvs);
            RoleValue rv = AuthorizationRoleRel.buildRoleValue(prr0);
            rvs.add(rv);

            //add permssions
            List<PermissionValue> pvs = new ArrayList<PermissionValue>();
            pr.setPermission(pvs);
            for (PermissionRoleRel prr : prrl) {
                PermissionValue pv = AuthorizationRoleRel.buildPermissionValue(prr);
                pvs.add(pv);
            }
        }
        return pr;
    }

    /**
     * Fill common part.
     *
     * @param pr the pr
     * @param wrapDoc the wrap doc
     * @param handleDelete the handle delete
     * @throws Exception the exception
     */
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
        PermissionRoleUtil.buildPermissionRoleRel(pr, subject, prrl, handleDelete);
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
        return new DocumentFilter(this.getServiceContext());
    }
}
