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

import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RolesList;
import org.collectionspace.services.common.context.ServiceContext;

import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for Role
 * @author 
 */
public class RoleDocumentHandler
        extends AbstractDocumentHandlerImpl<Role, RolesList, Role, List> {

    private final Logger logger = LoggerFactory.getLogger(RoleDocumentHandler.class);
    private Role role;
    private RolesList rolesList;

    @Override
    public void handleCreate(DocumentWrapper<Role> wrapDoc) throws Exception {
        String id = UUID.randomUUID().toString();
        Role role = wrapDoc.getWrappedObject();
        role.setCsid(id);
        //FIXME: if admin updating the role is a CS admin rather than
        //the tenant admin, tenant id should be retrieved from the request
        role.setTenantId(getServiceContext().getTenantId());
    }

    @Override
    public void handleUpdate(DocumentWrapper<Role> wrapDoc) throws Exception {
        Role role = wrapDoc.getWrappedObject();
        //FIXME: if admin updating the role is a CS admin rather than
        //the tenant admin, tenant id should be retrieved from the request
        role.setTenantId(getServiceContext().getTenantId());
    }

    @Override
    public void completeUpdate(DocumentWrapper<Role> wrapDoc) throws Exception {
        Role upAcc = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(role);
        sanitize(upAcc);
    }

    @Override
    public void handleGet(DocumentWrapper<Role> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        sanitize(getCommonPart());
        getServiceContext().setOutput(role);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List> wrapDoc) throws Exception {
        RolesList rolesList = extractCommonPartList(wrapDoc);
        setCommonPartList(rolesList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public Role extractCommonPart(
            DocumentWrapper<Role> wrapDoc)
            throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public void fillCommonPart(Role obj, DocumentWrapper<Role> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountDocumentHandler");
    }

    @Override
    public RolesList extractCommonPartList(
            DocumentWrapper<List> wrapDoc)
            throws Exception {

        RolesList rolesList = new RolesList();
        List<Role> list = new ArrayList<Role>();
        rolesList.setRoles(list);
        for (Object obj : wrapDoc.getWrappedObject()) {
            Role role = (Role) obj;
            sanitize(role);
            list.add(role);
        }
        return rolesList;
    }

    @Override
    public Role getCommonPart() {
        return role;
    }

    @Override
    public void setCommonPart(Role role) {
        this.role = role;
    }

    @Override
    public RolesList getCommonPartList() {
        return rolesList;
    }

    @Override
    public void setCommonPartList(RolesList rolesList) {
        this.rolesList = rolesList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter(ServiceContext ctx) {
        DocumentFilter filter = new RoleJpaFilter();
        filter.setPageSize(
                ctx.getServiceBindingPropertyValue(
                DocumentFilter.PAGE_SIZE_DEFAULT_PROPERTY));
        return filter;
    }

    /**
     * sanitize removes data not needed to be sent to the consumer
     * @param role
     */
    private void sanitize(Role role) {
        role.setTenantId(null);
    }
}
