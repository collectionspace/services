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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.authorization.storage.PermissionRoleDocumentHandler;
import org.collectionspace.services.authorization.storage.AuthorizationDelegate;

import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.UnauthorizedException;

import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PermissionResource.
 */
@Path("/authorization/permissions")
@Consumes("application/xml")
@Produces("application/xml")
public class PermissionResource
        extends AbstractCollectionSpaceResourceImpl<Permission, Permission> {

    /** The service name. */
    final private String serviceName = "authorization/permissions";
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(PermissionResource.class);
    /** The storage client. */
    final StorageClient storageClient = new JpaStorageClientImpl();

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
    public Class<Permission> getCommonPartClass() {
        return Permission.class;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getServiceContextFactory()
     */
    @Override
    public ServiceContextFactory<Permission, Permission> getServiceContextFactory() {
        return RemoteServiceContextFactory.get();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getStorageClient(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public StorageClient getStorageClient(ServiceContext ctx) {
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
     * Creates the permission.
     *
     * @param input the input
     *
     * @return the response
     */
    @POST
    public Response createPermission(Permission input) {
        try {
            ServiceContext<Permission, Permission> ctx = createServiceContext(input, Permission.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getStorageClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(PermissionResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.POST_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.POST_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createPermission", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.POST_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the permission.
     * 
     * @param csid the csid
     * 
     * @return the permission
     */
    @GET
    @Path("{csid}")
    public Permission getPermission(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPermission with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getPermission: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.GET_FAILED + "permission "
                    + ServiceMessages.MISSING_INVALID_CSID + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Permission result = null;
        try {
            ServiceContext<Permission, Permission> ctx = createServiceContext((Permission) null, Permission.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).get(ctx, csid, handler);
            result = (Permission) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.GET_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermission", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermission", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.GET_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + " permission csid=" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the permission list.
     * 
     * @param ui the ui
     * 
     * @return the permission list
     */
    @GET
    @Produces("application/xml")
    public PermissionsList getPermissionList(
            @Context UriInfo ui) {
        PermissionsList permissionList = new PermissionsList();
        try {
            ServiceContext<Permission, Permission> ctx = createServiceContext((Permission) null, Permission.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            DocumentFilter myFilter = handler.createDocumentFilter();
            myFilter.setPagination(queryParams);
            myFilter.setQueryParams(queryParams);
            handler.setDocumentFilter(myFilter);
            getStorageClient(ctx).getFiltered(ctx, handler);
            permissionList = (PermissionsList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.LIST_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getPermissionsList", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.LIST_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return permissionList;
    }

    /**
     * Update permission.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the permission
     */
    @PUT
    @Path("{csid}")
    public Permission updatePermission(
            @PathParam("csid") String csid,
            Permission theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updatePermission with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updatePermission: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.PUT_FAILED + "permission "
                    + ServiceMessages.MISSING_INVALID_CSID + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Permission result = null;
        try {
            ServiceContext<Permission, Permission> ctx = createServiceContext(theUpdate, Permission.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            result = (Permission) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.PUT_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.PUT_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updatePermission", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.PUT_FAILED + "permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.PUT_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete permission.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deletePermission(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deletePermission with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deletePermission: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.DELETE_FAILED + "permission "
                    + ServiceMessages.MISSING_INVALID_CSID + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
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
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.DELETE_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);

        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deletePermission", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + "permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.DELETE_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
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
        if (logger.isDebugEnabled()) {
            logger.debug("createPermissionRole with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("createPermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.POST_FAILED + "permroles permission "
                    + ServiceMessages.MISSING_INVALID_CSID + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            String permrolecsid = subResource.createPermissionRole(input, SubjectType.ROLE);
            UriBuilder path = UriBuilder.fromResource(PermissionResource.class);
            path.path(permCsid + "/permroles/" + permrolecsid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason "
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createPermissionRole", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.POST_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}/permroles/{id}")
    public PermissionRoleRel getPermissionRole(
            @PathParam("csid") String permCsid,
            @PathParam("id") String permrolecsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPermissionRole with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("getPermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.GET_FAILED + "permroles permission "
                    + ServiceMessages.MISSING_INVALID_CSID + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PermissionRoleRel result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //get relationships for a permission
            result = subResource.getPermissionRoleRel(permCsid, SubjectType.ROLE, permrolecsid);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.GET_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermissionRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "permroles permission csid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermissionRole", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.GET_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "permroles permisison csid=" + permCsid
                    + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Path("{csid}/permroles")
    public PermissionRole getPermissionRole(
            @PathParam("csid") String permCsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPermissionRole with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("getPermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.GET_FAILED + "permroles permission "
                    + ServiceMessages.MISSING_INVALID_CSID + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PermissionRole result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //get relationships for a permission
            result = subResource.getPermissionRole(permCsid, SubjectType.ROLE);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.GET_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermissionRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "permroles permission csid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermissionRole", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.GET_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "permroles permisison csid=" + permCsid
                    + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete permission role.
     *
     * @param permCsid the perm csid
     * @param input the input
     * @return the response
     */
    public Response deletePermissionRole(String permCsid, PermissionRole input) {
        if (logger.isDebugEnabled()) {
            logger.debug("Delete payload of permrole relationships with permission permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("deletePermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.DELETE_FAILED + "permroles permission "
                    + ServiceMessages.MISSING_INVALID_CSID + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole(permCsid, SubjectType.ROLE, input);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.DELETE_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deletePermissionRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + "permisison csid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.DELETE_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
    
    /**
     * Delete permission role.
     *
     * @param permCsid the perm csid
     * @return the response
     */
    @DELETE
    @Path("{csid}/permroles")    
    public Response deletePermissionRole(
            @PathParam("csid") String permCsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("Delete all the role relationships of the permissions with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("deletePermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.DELETE_FAILED + "permroles permission "
                    + ServiceMessages.MISSING_INVALID_CSID + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole(permCsid, SubjectType.ROLE);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.DELETE_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deletePermissionRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + "permisison csid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.DELETE_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
    
}
