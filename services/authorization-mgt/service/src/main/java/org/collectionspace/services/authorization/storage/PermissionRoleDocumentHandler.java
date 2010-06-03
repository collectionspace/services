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

import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for PermissionRole association
 * @author 
 */
public class PermissionRoleDocumentHandler
        extends AbstractDocumentHandlerImpl<PermissionRole, PermissionsRolesList, List<PermissionRoleRel>, List<PermissionRoleRel>> {

    private final Logger logger = LoggerFactory.getLogger(PermissionRoleDocumentHandler.class);
    private PermissionRole permissionRole;
    private PermissionsRolesList permissionRolesList;

    @Override
    public void handleCreate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc);
    }

    @Override
    public void completeCreate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        PermissionRole pr = getCommonPart();
        AuthorizationDelegate.addPermissions(getServiceContext(), pr);
    }

    @Override
    public void handleUpdate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    @Override
    public void completeUpdate(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    @Override
    public void handleGet(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        getServiceContext().setOutput(permissionRole);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    @Override
    public void completeDelete(DocumentWrapper<List<PermissionRoleRel>> wrapDoc) throws Exception {
//
    }

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
            pr.setPermissions(pvs);
            PermissionValue pv = buildPermissionValue(prr0);
            pvs.add(pv);

            //add roles
            List<RoleValue> rvs = new ArrayList<RoleValue>();
            pr.setRoles(rvs);
            for (PermissionRoleRel prr : prrl) {
                RoleValue rv = buildRoleValue(prr);
                rvs.add(rv);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {

            List<RoleValue> rvs = new ArrayList<RoleValue>();
            pr.setRoles(rvs);
            RoleValue rv = buildRoleValue(prr0);
            rvs.add(rv);

            //add permssions
            List<PermissionValue> pvs = new ArrayList<PermissionValue>();
            pr.setPermissions(pvs);
            for (PermissionRoleRel prr : prrl) {
                PermissionValue pv = buildPermissionValue(prr);
                pvs.add(pv);
            }
        }
        return pr;
    }

    @Override
    public void fillCommonPart(PermissionRole pr, DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
            throws Exception {
        List<PermissionRoleRel> prrl = wrapDoc.getWrappedObject();
        SubjectType subject = pr.getSubject();
        if (subject == null) {
            //it is not required to give subject as URI determines the subject
            subject = PermissionRoleUtil.getRelationSubject(getServiceContext());
        } else {
            //subject mismatch should have been checked during validation
        }
        if (subject.equals(SubjectType.ROLE)) {
            //FIXME: potential index out of bounds exception...negative test needed
            PermissionValue pv = pr.getPermissions().get(0);
            for (RoleValue rv : pr.getRoles()) {
                PermissionRoleRel prr = buildPermissonRoleRel(pv, rv);
                prrl.add(prr);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {
            //FIXME: potential index out of bounds exception...negative test needed
            RoleValue rv = pr.getRoles().get(0);
            for (PermissionValue pv : pr.getPermissions()) {
                PermissionRoleRel prr = buildPermissonRoleRel(pv, rv);
                prrl.add(prr);
            }
        }
    }

    @Override
    public PermissionsRolesList extractCommonPartList(
            DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
            throws Exception {

        throw new UnsupportedOperationException("operation not relevant for PermissionRoleDocumentHandler");
    }

    @Override
    public PermissionRole getCommonPart() {
        return permissionRole;
    }

    @Override
    public void setCommonPart(PermissionRole permissionRole) {
        this.permissionRole = permissionRole;
    }

    @Override
    public PermissionsRolesList getCommonPartList() {
        return permissionRolesList;
    }

    @Override
    public void setCommonPartList(PermissionsRolesList permissionRolesList) {
        this.permissionRolesList = permissionRolesList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter() {
        return new DocumentFilter(this.getServiceContext());
    }

    private PermissionValue buildPermissionValue(PermissionRoleRel prr) {
        PermissionValue pv = new PermissionValue();
        pv.setPermissionId(prr.getPermissionId());
        pv.setResourceName(prr.getPermissionResource());
        return pv;
    }

    private RoleValue buildRoleValue(PermissionRoleRel prr) {
        RoleValue rv = new RoleValue();
        rv.setRoleId(prr.getRoleId());
        rv.setRoleName(prr.getRoleName());
        return rv;
    }

    private PermissionRoleRel buildPermissonRoleRel(PermissionValue pv, RoleValue rv) {
        PermissionRoleRel prr = new PermissionRoleRel();
        prr.setPermissionId(pv.getPermissionId());
        prr.setPermissionResource(pv.getResourceName());
        prr.setRoleId(rv.getRoleId());
        prr.setRoleName(rv.getRoleName());
        return prr;
    }
}
