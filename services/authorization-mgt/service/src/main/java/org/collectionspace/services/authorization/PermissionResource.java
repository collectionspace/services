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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/authorization/permissions")
@Consumes("application/xml")
@Produces("application/xml")
public class PermissionResource
        extends AbstractCollectionSpaceResourceImpl {

    final private String serviceName = "authorization/permissions";
    final Logger logger = LoggerFactory.getLogger(PermissionResource.class);
    final StorageClient storageClient = new JpaStorageClientImpl(Permission.class);

    @Override
    protected String getVersionString() {
        /** The last change revision. */
        final String lastChangeRevision = "$LastChangedRevision: 1165 $";
        return lastChangeRevision;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    private <T> ServiceContext createServiceContext(T obj) throws Exception {
        ServiceContext ctx = new RemoteServiceContextImpl<T, T>(getServiceName());
        ctx.setInput(obj);
        ctx.setDocumentType(Permission.class.getPackage().getName()); //persistence unit
        ctx.setProperty("entity-name", Permission.class.getName());
        return ctx;
    }

    @Override
    public StorageClient getStorageClient(ServiceContext ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        DocumentHandler docHandler = ctx.getDocumentHandler();
        docHandler.setCommonPart(ctx.getInput());
        return docHandler;
    }

    @POST
    public Response createPermission(Permission input) {
        try {
            ServiceContext ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getStorageClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(PermissionResource.class);
            path.path("" + csid);
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
                logger.debug("Caught exception in createPermission", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

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
                    "get failed on Permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Permission result = null;
        try {
            ServiceContext ctx = createServiceContext((Permission) null);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).get(ctx, csid, handler);
            result = (Permission) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermission", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermission", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Permission CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Produces("application/xml")
    public PermissionsList getPermissionList(
            @Context UriInfo ui) {
        PermissionsList permissionList = new PermissionsList();
        try {
            ServiceContext ctx = createServiceContext((PermissionsList) null);
            DocumentHandler handler = createDocumentHandler(ctx);
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            DocumentFilter myFilter = handler.createDocumentFilter(ctx);
            myFilter.setPagination(queryParams);
            myFilter.setQueryParams(queryParams);
            handler.setDocumentFilter(myFilter);
            getStorageClient(ctx).getFiltered(ctx, handler);
            permissionList = (PermissionsList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getPermissionsList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return permissionList;
    }

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
                    "update failed on Permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Permission result = null;
        try {
            ServiceContext ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            result = (Permission) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Update failed reason "
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updatePermission", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}")
    public Response deletePermission(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deletePermission with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deletePermission: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext ctx = createServiceContext((Permission) null);
            getStorageClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);

        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deletePermission", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Permission csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    @POST
    @Path("{csid}/permroles")
    public Response createPermissionRole(@PathParam("csid") String permCsid,
            PermissionRole input) {
        if (logger.isDebugEnabled()) {
            logger.debug("createPermissionRole with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("createPermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "create failed on PermissionRole permCsid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            PermissionRoleSubResource subResource = new PermissionRoleSubResource();
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
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}/permroles/{permrolecsid}")
    public PermissionRole getPermissionRole(
            @PathParam("csid") String permCsid,
            @PathParam("permrolecsid") String permrolecsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPermissionRole with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("getPermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on PermissionRole permCsid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PermissionRole result = null;
        try {
            PermissionRoleSubResource subResource = new PermissionRoleSubResource();
            //get relationships for a permission
            result = subResource.getPermissionRole(permCsid, SubjectType.ROLE);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermissionRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on PermissionRole permrolecsid=" + permrolecsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPermissionRole", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested PermissionRole permrolecsid:" + permrolecsid
                    + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}/permroles/{permrolecsid}")
    public Response deletePermissionRole(
            @PathParam("csid") String permCsid,
            @PathParam("permrolecsid") String permrolecsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deletePermissionRole with permCsid=" + permCsid);
        }
        if (permCsid == null || "".equals(permCsid)) {
            logger.error("deletePermissionRole: missing permCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on PermissionRole permCsid=" + permCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            PermissionRoleSubResource subResource = new PermissionRoleSubResource();
            //delete all relationships for a permission
            subResource.deletePermissionRole(permCsid, SubjectType.ROLE);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason "
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deletePermissionRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on PermissionRole permrolecsid=" + permrolecsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
