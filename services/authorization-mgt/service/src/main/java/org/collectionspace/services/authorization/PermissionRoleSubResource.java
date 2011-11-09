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
package org.collectionspace.services.authorization;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.storage.PermissionRoleDocumentHandler;

import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaRelationshipStorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.common.context.ServiceContextProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionRoleSubResource is used to manage permission-role relationship
 * @author
 */
public class PermissionRoleSubResource
        extends AbstractCollectionSpaceResourceImpl<PermissionRole, PermissionRole> {

    public final static String ROLE_PERMROLE_SERVICE = "authorization/roles/permroles";
    public final static String PERMISSION_PERMROLE_SERVICE = "authorization/permissions/permroles";
    //this service is never exposed as standalone RESTful service...just use unique
    //service name to identify binding
    /** The service name. */
    private String serviceName = "authorization/permroles";
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(PermissionRoleSubResource.class);
    /** The storage client. */
    final StorageClient storageClient = new JpaRelationshipStorageClient<PermissionRole>();
    //
    private String permissionRoleCsid = null;

    /**
     * Instantiates a new permission role sub resource.
     *
     * @param serviceName the service name
     */
    public PermissionRoleSubResource(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
        /** The last change revision. */
        final String lastChangeRevision = "$LastChangedRevision: 1165 $";
        return lastChangeRevision;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<PermissionRole> getCommonPartClass() {
        return PermissionRole.class;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getServiceContextFactory()
     */
    @Override
    public ServiceContextFactory<PermissionRole, PermissionRole> getServiceContextFactory() {
        return RemoteServiceContextFactory.get();
    }

    /**
     * Creates the service context.
     * 
     * @param input the input
     * @param subject the subject
     * 
     * @return the service context< permission role, permission role>
     * 
     * @throws Exception the exception
     */
    private ServiceContext<PermissionRole, PermissionRole> createServiceContext(PermissionRole input,
            SubjectType subject) throws Exception {
        ServiceContext<PermissionRole, PermissionRole> ctx = createServiceContext(input);
        ctx.setDocumentType(PermissionRole.class.getPackage().getName()); //persistence unit
        ctx.setProperty(ServiceContextProperties.ENTITY_NAME, PermissionRoleRel.class.getName());
        ctx.setProperty(ServiceContextProperties.ENTITY_CLASS, PermissionRoleRel.class);
        //subject name is necessary to indicate if role or permission is a subject
        ctx.setProperty(ServiceContextProperties.SUBJECT, subject);
        //set context for the relationship query
        if (subject == SubjectType.ROLE) {
            ctx.setProperty(ServiceContextProperties.OBJECT_CLASS, Permission.class);
            ctx.setProperty(ServiceContextProperties.OBJECT_ID, "permission_id");
        } else if (subject == SubjectType.PERMISSION) {
            ctx.setProperty(ServiceContextProperties.OBJECT_CLASS, Role.class);
            ctx.setProperty(ServiceContextProperties.OBJECT_ID, "role_id");
        }
        return ctx;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getStorageClient(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public StorageClient getStorageClient(ServiceContext<PermissionRole, PermissionRole> ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        docHandler.setCommonPart(ctx.getInput());
//        return docHandler;
//    }
    /**
     * createPermissionRole creates one or more permission-role relationships
     * between object (permission/role) and subject (role/permission)
     * @param input
     * @param subject
     * @return
     * @throws Exception
     */
    public String createPermissionRole(PermissionRole input, SubjectType subject)
            throws Exception {

        ServiceContext<PermissionRole, PermissionRole> ctx = createServiceContext(input, subject);
        DocumentHandler handler = createDocumentHandler(ctx);
        return getStorageClient(ctx).create(ctx, handler);
    }

    /**
     * Gets the permission role rel.
     *
     * @param csid the csid
     * @param subject the subject
     * @param permissionRoleCsid the permission role csid
     * @return the permission role rel
     * @throws Exception the exception
     */
    public PermissionRoleRel getPermissionRoleRel(String csid,
    		SubjectType subject,
    		String permissionRoleCsid) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("getAccountRole with csid=" + csid);
        }
//        AccountRolesList result = new AccountRolesList();
        ServiceContext<PermissionRole, PermissionRole> ctx = createServiceContext((PermissionRole) null, subject);
        PermissionRoleDocumentHandler handler = (PermissionRoleDocumentHandler)createDocumentHandler(ctx);
        handler.setPermissionRoleCsid(permissionRoleCsid);
        //getStorageClient(ctx).get(ctx, csid, handler);
        PermissionRoleRel permissionRoleRel = (PermissionRoleRel)JpaStorageUtils.getEntity(
        		new Long(permissionRoleCsid).longValue(), PermissionRoleRel.class);
//        List<AccountRoleListItem> accountRoleList = result.getAccountRoleListItems();
//        AccountRoleListItem listItem = new AccountRoleListItem();
//        // fill the item
//        listItem.setCsid(accountRoleRel.getHjid().toString());
//        listItem.setRoleId(accountRoleRel.getRoleId());
//        listItem.setRoleName(accountRoleRel.getRoleName());
        // add item to result list
//        result = (AccountRolesList) ctx.getOutput();
        
        return permissionRoleRel;
    }
    
    /**
     * getPermissionRole retrieves permission-role relationships using given
     * csid of object (permission/role) and subject (role/permission)
     * @param csid
     * @param subject
     * @return
     * @throws Exception
     */
    public PermissionRole getPermissionRole(
            String csid, SubjectType subject) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("getPermissionRole with csid=" + csid);
        }
        PermissionRole result = null;
        ServiceContext<PermissionRole, PermissionRole> ctx = createServiceContext((PermissionRole) null, subject);
        DocumentHandler handler = createDocumentHandler(ctx);
        getStorageClient(ctx).get(ctx, csid, handler);
        result = (PermissionRole) ctx.getOutput();

        return result;
    }

    /**
     * deletePermissionRole deletes permission-role relationships using given
     * csid of object (permission/role) and subject (role/permission)
     * @param csid
     * @param subject
     * @return
     * @throws Exception
     */
    public void deletePermissionRole(String csid,
            SubjectType subject) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("deletePermissionRole with csid=" + csid);
        }
        PermissionRole permRole = this.getPermissionRole(csid, subject);
        this.deletePermissionRole(csid, subject, permRole);
    }

    /**
     * deletePermissionRole deletes permission-role relationships using given
     * csid of object (permission/role) and subject (role/permission)
     * @param csid
     * @param subject
     * @param input with role and permission relationships to delete
     * @return
     * @throws Exception
     */
    public void deletePermissionRole(String csid,
            SubjectType subject, PermissionRole input) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("deletePermissionRole(input) with csid=" + csid);
        }
        ServiceContext<PermissionRole, PermissionRole> ctx = createServiceContext(input, subject);
        DocumentHandler handler = createDocumentHandler(ctx);
        getStorageClient(ctx).delete(ctx, csid, handler);
    }
}
