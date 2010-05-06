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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionsList;

import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for Permission
 * @author 
 */
public class PermissionDocumentHandler
        extends AbstractDocumentHandlerImpl<Permission, PermissionsList, Permission, List> {

    private final Logger logger = LoggerFactory.getLogger(PermissionDocumentHandler.class);
    private Permission permission;
    private PermissionsList permissionsList;

    @Override
    public void handleCreate(DocumentWrapper<Permission> wrapDoc) throws Exception {
        String id = UUID.randomUUID().toString();
        Permission permission = wrapDoc.getWrappedObject();
        permission.setCsid(id);
        //FIXME: if admin updating the permission is a CS admin rather than
        //the tenant admin, tenant id should be retrieved from the request
        permission.setTenantId(getServiceContext().getTenantId());
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
    private Permission merge(Permission from, Permission to) {
        Date now = new Date();
        to.setUpdatedAtItem(now);
        if (from.getResourceName() != null) {
            to.setResourceName(from.getResourceName());
        }
        if (from.getAttributeName() != null) {
            to.setAttributeName(from.getAttributeName());
        }
        if (from.getDescription() != null) {
            to.setDescription(from.getDescription());
        }
        if (from.getEffect() != null) {
            to.setEffect(from.getEffect());
        }
        List<PermissionAction> fromActions = from.getActions();
        if (!fromActions.isEmpty()) {
            //override the whole list, no reconcilliation by design
            to.setActions(fromActions);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("merged permission=" + JaxbUtils.toString(to, Permission.class));
        }

        return to;
    }

    @Override
    public void completeUpdate(DocumentWrapper<Permission> wrapDoc) throws Exception {
        Permission upAcc = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(permission);
        sanitize(upAcc);
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
        permissionsList.setPermissions(list);
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
     * sanitize removes data not needed to be sent to the consumer
     * @param permission
     */
    private void sanitize(Permission permission) {
        permission.setTenantId(null);
    }
}
