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

import org.collectionspace.services.account.AccountRoleSubResource;
import org.collectionspace.services.client.RoleClient;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path(RoleClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class RoleResource extends SecurityResourceBase {

    final Logger logger = LoggerFactory.getLogger(RoleResource.class);
    final StorageClient storageClient = new JpaStorageClientImpl();

    @Override
    protected String getVersionString() {
        return  "$LastChangedRevision: 1165 $";
    }

    @Override
    public String getServiceName() {
        return RoleClient.SERVICE_NAME;
    }

    @Override
    public Class<RoleResource> getCommonPartClass() {
        return RoleResource.class;
    }

    @Override
    public ServiceContextFactory getServiceContextFactory() {
        return RemoteServiceContextFactory.get();
    }

    @Override
    public StorageClient getStorageClient(ServiceContext ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    @POST
    public Response createRole(Role input) {
        return create(input);
    }

    @GET
    @Path("{csid}")
    public Role getRole(@PathParam("csid") String csid) {
        return (Role)get(csid, Role.class);
    }
    
    /*
     * Get a list of accounts associated with this role.
     */
    @GET
    @Path("{csid}/accountroles")
    public AccountRole getRoleAccounts(
            @PathParam("csid") String accCsid) {
        logger.debug("getAccountRole with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.GET_FAILED+ "accountroles role ");
        AccountRole result = null;
        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //get relationships for a role
            result = subResource.getAccountRole(accCsid, SubjectType.ACCOUNT);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, accCsid);
        }
        checkResult(result, accCsid, ServiceMessages.GET_FAILED);
        return result;
    }

    @GET
    @Produces("application/xml")
    public RolesList getRoleList(@Context UriInfo ui) {
        return (RolesList)getList(ui, Role.class);
    }

    @PUT
    @Path("{csid}")
    public Role updateRole(@PathParam("csid") String csid, Role theUpdate) {
        try {
	    	Role role = (Role)get(csid, Role.class);
	        // If marked as metadata immutable, do not update
	        if(RoleClient.IMMUTABLE.equals(role.getMetadataProtection())) {
	            Response response = 
	            	Response.status(Response.Status.FORBIDDEN).entity("Role: "+csid+" is immutable.").type("text/plain").build();
                throw new WebApplicationException(response);
	        }
	        return (Role)update(csid, theUpdate, Role.class);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED, csid);
        }
    }

    @DELETE
    @Path("{csid}")
    public Response deleteRole(@PathParam("csid") String csid) {
        logger.debug("deleteRole with csid=" + csid);
        ensureCSID(csid, ServiceMessages.DELETE_FAILED + "deleteRole ");

        try {
        	Role role = (Role)get(csid, Role.class);
            // If marked as metadata immutable, do not delete
            if(RoleClient.IMMUTABLE.equals(role.getMetadataProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Role: "+csid+" is immutable.").type("text/plain").build();
                return response;
            }
            //FIXME ideally the following three operations should be in the same tx CSPACE-658
            //delete all relationships for this permission
            PermissionRoleSubResource permRoleResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.ROLE_PERMROLE_SERVICE);
            permRoleResource.deletePermissionRole(csid, SubjectType.PERMISSION);
            //delete all the account/role relationships associate with this role
            AccountRoleSubResource accountRoleResource =
                new AccountRoleSubResource(AccountRoleSubResource.ROLE_ACCOUNTROLE_SERVICE);
            accountRoleResource.deleteAccountRole(csid, SubjectType.ACCOUNT);
            //finally, delete the role itself
            ServiceContext<Role, Role> ctx = createServiceContext((Role) null, Role.class);
            ((JpaStorageClientImpl) getStorageClient(ctx)).deleteWhere(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }

    @POST
    @Path("{csid}/permroles")
    public Response createRolePermission(@QueryParam("_method") String method, @PathParam("csid") String roleCsid,
            PermissionRole input) {
        if (method != null) {
            if ("delete".equalsIgnoreCase(method)) {
                return deleteRolePermission(roleCsid, input);
            }
        }
        logger.debug("createRolePermission with roleCsid=" + roleCsid);
        ensureCSID(roleCsid, ServiceMessages.PUT_FAILED + "permroles role ");
        try {
        	Role role = (Role)get(roleCsid, Role.class);
            // If marked as metadata immutable, do not delete
            if(RoleClient.IMMUTABLE.equals(role.getPermsProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Role: "+roleCsid+" is immutable.").type("text/plain").build();
                return response;
            }
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.ROLE_PERMROLE_SERVICE);
            String permrolecsid = subResource.createPermissionRole(input, SubjectType.PERMISSION);
            UriBuilder path = UriBuilder.fromResource(RoleResource.class);
            path.path(roleCsid + "/permroles/" + permrolecsid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, roleCsid);
        }
    }

    @GET
    @Path("{csid}/permroles")
    public PermissionRole getRolePermission(
            @PathParam("csid") String roleCsid) {
        logger.debug("getRolePermission with roleCsid=" + roleCsid);
        ensureCSID(roleCsid, ServiceMessages.GET_FAILED + "permroles role ");
        PermissionRole result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.ROLE_PERMROLE_SERVICE);
            //get relationships for a role
            result = subResource.getPermissionRole(roleCsid, SubjectType.PERMISSION);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, roleCsid);
        }
        checkResult(result, roleCsid, ServiceMessages.GET_FAILED);
        return result;
    }

    @GET
    @Path("{csid}/permroles/{id}")
    public PermissionRoleRel getRolePermission(
            @PathParam("csid") String roleCsid,
            @PathParam("id") String permrolecsid) {
        logger.debug("getRolePermission with roleCsid=" + roleCsid);
        ensureCSID(roleCsid, ServiceMessages.GET_FAILED + "permroles role ");
        PermissionRoleRel result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.ROLE_PERMROLE_SERVICE);
            //get relationships for a role
            result = subResource.getPermissionRoleRel(roleCsid, SubjectType.PERMISSION, permrolecsid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, roleCsid);
        }
        checkResult(result, roleCsid, ServiceMessages.GET_FAILED);
        return result;
    }

    public Response deleteRolePermission(String roleCsid, PermissionRole input) {
        logger.debug("deleteRolePermission with roleCsid=" + roleCsid);
        ensureCSID(roleCsid, ServiceMessages.DELETE_FAILED + "permroles role ");
        try {
        	Role role = (Role)get(roleCsid, Role.class);
            // If marked as metadata immutable, do not delete
            if(RoleClient.IMMUTABLE.equals(role.getPermsProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Role: "+roleCsid+" is immutable.").type("text/plain").build();
                return response;
            }
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.ROLE_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole(roleCsid, SubjectType.PERMISSION, input);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, roleCsid);
        }
    }

    @DELETE
    @Path("{csid}/permroles")
    public Response deleteRolePermission(
    		@PathParam("csid") String roleCsid) {
        logger.debug("deleteRolePermission with roleCsid=" + roleCsid);
        ensureCSID(roleCsid, ServiceMessages.DELETE_FAILED + "permroles role ");
        try {
        	Role role = (Role)get(roleCsid, Role.class);
            // If marked as metadata immutable, do not delete
            if(RoleClient.IMMUTABLE.equals(role.getPermsProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Role: "+roleCsid+" is immutable.").type("text/plain").build();
                return response;
            }
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.ROLE_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole(roleCsid, SubjectType.PERMISSION);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, roleCsid);
        }
    }
}
