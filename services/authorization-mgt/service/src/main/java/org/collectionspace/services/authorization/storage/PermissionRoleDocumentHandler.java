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
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.context.ServiceContext;

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
    public PermissionRole extractCommonPart(
            DocumentWrapper<List<PermissionRoleRel>> wrapDoc)
            throws Exception {
        List<PermissionRoleRel> prrl = wrapDoc.getWrappedObject();
        PermissionRole pr = new PermissionRole();
        SubjectType subject = PermissionRoleUtil.getSubject(getServiceContext());
        pr.setSubject(subject);
        if (SubjectType.ROLE.equals(subject)) {
            List<String> permIds = new ArrayList<String>();
            permIds.add(prrl.get(0).getPermissionId());
            pr.setPermissionIds(permIds);
            List<String> roleIds = new ArrayList<String>();
            for (PermissionRoleRel prr : prrl) {
                roleIds.add(prr.getRoleId());
                pr.setCreatedAt(prr.getCreatedAt());
                pr.setUpdatedAt(prr.getUpdatedAt());
            }
            pr.setRoleIds(roleIds);
        } else {
            List<String> roleIds = new ArrayList<String>();
            roleIds.add(prrl.get(0).getRoleId());
            pr.setRoleIds(roleIds);
            List<String> permIds = new ArrayList<String>();
            for (PermissionRoleRel prr : prrl) {
                permIds.add(prr.getPermissionId());
                pr.setCreatedAt(prr.getCreatedAt());
                pr.setUpdatedAt(prr.getUpdatedAt());
            }
            pr.setPermissionIds(permIds);
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
            subject = PermissionRoleUtil.getSubject(getServiceContext());
        } else {
            //subject mismatch should have been checked during validation
        }
        if (subject.equals(SubjectType.ROLE)) {
            String permId = pr.getPermissionIds().get(0);
            for (String roleId : pr.getRoleIds()) {
                PermissionRoleRel prr = new PermissionRoleRel();
                prr.setPermissionId(permId);
                prr.setRoleId(roleId);
                prrl.add(prr);
            }
        } else {
            String roleId = pr.getRoleIds().get(0);
            for (String permId : pr.getPermissionIds()) {
                PermissionRoleRel prr = new PermissionRoleRel();
                prr.setPermissionId(permId);
                prr.setRoleId(roleId);
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
}
