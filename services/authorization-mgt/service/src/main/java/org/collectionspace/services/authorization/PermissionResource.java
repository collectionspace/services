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
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.authorization.storage.AuthorizationDelegate;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.common.SecurityResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path(PermissionClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class PermissionResource extends SecurityResourceBase {

    final Logger logger = LoggerFactory.getLogger(PermissionResource.class);
    final StorageClient storageClient = new JpaStorageClientImpl();

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision: 1165 $";
    }

    @Override
    public String getServiceName() {
        return  PermissionClient.SERVICE_NAME;
    }

    @Override
    public Class<Permission> getCommonPartClass() {
        return Permission.class;
    }

    @Override
    public ServiceContextFactory<Permission, Permission> getServiceContextFactory() {
        return RemoteServiceContextFactory.get();
    }

    @Override
    public StorageClient getStorageClient(ServiceContext ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    @POST
    public Response createPermission(Permission input) {
        return create(input);
    }

    @GET
    @Path("{csid}")
    public Permission getPermission(@PathParam("csid") String csid) {
        return (Permission)get(csid, Permission.class);
    }

    @GET
    @Produces("application/xml")
    public PermissionsList getPermissionList(@Context UriInfo ui) {
    	PermissionsList result = (PermissionsList)getList(ui, Permission.class);
    	PayloadOutputPart ppo = new PayloadOutputPart(PermissionsList.class.getName(), result);
    	System.out.println(ppo.asXML());
    	
    	return result;
    }

    @PUT
    @Path("{csid}")
    public Permission updatePermission(@PathParam("csid") String csid,Permission theUpdate) {
         return (Permission)update(csid, theUpdate, Permission.class);
    }

    @DELETE
    @Path("{csid}")
    public Response deletePermission(@PathParam("csid") String csid) {
        logger.debug("deletePermission with csid=" + csid);
        ensureCSID(csid, ServiceMessages.DELETE_FAILED + "permission ");
        try {
            //FIXME ideally the following two ops should be in the same tx CSPACE-658
            //delete all relationships for this permission
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            subResource.deletePermissionRole(csid, SubjectType.ROLE);
            //NOTE for delete permissions in the authz provider
            //at the PermissionRoleSubResource/DocHandler level, there is no visibility
            //if permission is deleted, so do it here, however,
            //this is a very dangerous operation as it deletes the Spring ACL instead of ACE(s)
            //the ACL might be needed for other ACEs roles...
            AuthorizationDelegate.deletePermissions(csid);

            ServiceContext<Permission, Permission> ctx = createServiceContext((Permission) null, Permission.class);
            getStorageClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }

    @POST
    @Path("{csid}/permroles")
    public Response createPermissionRole(@QueryParam("_method") String method,
            @PathParam("csid") String permCsid,
            PermissionRole input) {
                if (method != null) {
            if ("delete".equalsIgnoreCase(method)) {
                return deletePermissionRole(permCsid, input);
            }
        }
        logger.debug("createPermissionRole with permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.POST_FAILED + "permroles permission ");
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            String permrolecsid = subResource.createPermissionRole(input, SubjectType.ROLE);
            UriBuilder path = UriBuilder.fromResource(PermissionResource.class);
            path.path(permCsid + "/permroles/" + permrolecsid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED, permCsid);
        }
    }

    @GET
    @Path("{csid}/permroles/{id}")
    public PermissionRoleRel getPermissionRole(
            @PathParam("csid") String permCsid,
            @PathParam("id") String permrolecsid) {
        logger.debug("getPermissionRole with permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.GET_FAILED + "permroles permission ");
        PermissionRoleRel result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //get relationships for a permission
            result = subResource.getPermissionRoleRel(permCsid, SubjectType.ROLE, permrolecsid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, permCsid);
        }
        checkResult(result, permCsid, ServiceMessages.GET_FAILED);
        return result;
    }

    @GET
    @Path("{csid}/permroles")
    public PermissionRole getPermissionRole(
            @PathParam("csid") String permCsid) {
        logger.debug("getPermissionRole with permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.GET_FAILED + "permroles permission ");
        PermissionRole result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //get relationships for a permission
            result = subResource.getPermissionRole(permCsid, SubjectType.ROLE);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, permCsid);
        }
        checkResult(result, permCsid, ServiceMessages.GET_FAILED);
        return result;
    }

    public Response deletePermissionRole(String permCsid, PermissionRole input) {
        logger.debug("Delete payload of permrole relationships with permission permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.DELETE_FAILED + "permroles permission ");
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole(permCsid, SubjectType.ROLE, input);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, permCsid);
        }
    }
    
    @DELETE
    @Path("{csid}/permroles")    
    public Response deletePermissionRole(
            @PathParam("csid") String permCsid) {
        logger.debug("Delete all the role relationships of the permissions with permCsid=" + permCsid);
         ensureCSID(permCsid, ServiceMessages.DELETE_FAILED + "permroles permission ");
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole(permCsid, SubjectType.ROLE);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, permCsid);
        }
    }
    
}
