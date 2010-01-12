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
package org.collectionspace.services.organization;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.AbstractCollectionSpaceResource;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.organization.nuxeo.OrgAuthorityHandlerFactory;
import org.collectionspace.services.organization.nuxeo.OrganizationDocumentModelHandler;
import org.collectionspace.services.organization.nuxeo.OrganizationHandlerFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/orgauthorities")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class OrgAuthorityResource extends AbstractCollectionSpaceResource {

    private final static String orgAuthorityServiceName = "orgauthorities";
    private final static String organizationServiceName = "organizations";
    final Logger logger = LoggerFactory.getLogger(OrgAuthorityResource.class);
    //FIXME retrieve client type from configuration
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    public OrgAuthorityResource() {
        // do nothing
    }

    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision: 1165 $";
    	return lastChangeRevision;
    }
    
    @Override
    public String getServiceName() {
        return orgAuthorityServiceName;
    }

    public String getItemServiceName() {
        return organizationServiceName;
    }

    /*
    public RemoteServiceContext createItemServiceContext(MultipartInput input) throws Exception {
    RemoteServiceContext ctx = new RemoteServiceContextImpl(getItemServiceName());
    ctx.setInput(input);
    return ctx;
    }
     */
    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        DocumentHandler docHandler = OrgAuthorityHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        if (ctx.getInput() != null) {
            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), OrgauthoritiesCommon.class);
            if (obj != null) {
                docHandler.setCommonPart((OrgauthoritiesCommon) obj);
            }
        }
        return docHandler;
    }

    private DocumentHandler createItemDocumentHandler(
            ServiceContext ctx,
            String inAuthority) throws Exception {
        DocumentHandler docHandler = OrganizationHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        ((OrganizationDocumentModelHandler) docHandler).setInAuthority(inAuthority);
        if (ctx.getInput() != null) {
            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(getItemServiceName()),
                    OrganizationsCommon.class);
            if (obj != null) {
                docHandler.setCommonPart((OrganizationsCommon) obj);
            }
        }
        return docHandler;
    }

    @POST
    public Response createOrgAuthority(MultipartInput input) {
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(input, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //orgAuthorityObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(OrgAuthorityResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createOrgAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public MultipartOutput getOrgAuthority(@PathParam("csid") String csid) {
        String idValue = null;
        if (csid == null) {
            logger.error("getOrgAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getOrgAuthority with path(id)=" + csid);
        }
        MultipartOutput result = null;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrgAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested OrgAuthority CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Produces("application/xml")
    public OrgauthoritiesCommonList getOrgAuthorityList(@Context UriInfo ui) {
        OrgauthoritiesCommonList orgAuthorityObjectList = new OrgauthoritiesCommonList();
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentFilter myFilter =
                    DocumentFilter.CreatePaginatedDocumentFilter(queryParams);
            String nameQ = queryParams.getFirst("refName");
            if (nameQ != null) {
                myFilter.setWhereClause("orgauthorities_common:refName='" + nameQ + "'");
            }
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            orgAuthorityObjectList = (OrgauthoritiesCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getOrgAuthorityList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return orgAuthorityObjectList;
    }

    @PUT
    @Path("{csid}")
    public MultipartOutput updateOrgAuthority(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateOrgAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateOrgAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(theUpdate, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updateOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}")
    public Response deleteOrgAuthority(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteOrgAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteOrgAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /*************************************************************************
     * Organization parts - this is a sub-resource of OrgAuthority
     *************************************************************************/
    @POST
    @Path("{csid}/items")
    public Response createOrganization(@PathParam("csid") String parentcsid, MultipartInput input) {
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(input, getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            String itemcsid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(OrgAuthorityResource.class);
            path.path(parentcsid + "/items/" + itemcsid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createOrganization", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput getOrganization(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getOrganization with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("getOrganization: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Organization csid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("getOrganization: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Organization itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).get(ctx, itemcsid, handler);
            // TODO should we assert that the item is in the passed orgAuthority?
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrganization", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Organization csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrganization", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Organization CSID:" + itemcsid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Path("{csid}/items")
    @Produces("application/xml")
    public OrganizationsCommonList getOrganizationList(
            @PathParam("csid") String parentcsid,
            @Context UriInfo ui) {
        OrganizationsCommonList organizationObjectList = new OrganizationsCommonList();
        try {
            // Note that docType defaults to the ServiceName, so we're fine with that.
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            DocumentFilter myFilter =
                DocumentFilter.CreatePaginatedDocumentFilter(queryParams);
            myFilter.setWhereClause(
                    "organizations_common:inAuthority='" + parentcsid + "'");
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            organizationObjectList = (OrganizationsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getOrganizationList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return organizationObjectList;
    }

    @PUT
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput updateOrganization(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateOrganization with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("updateOrganization: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Organization parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("updateOrganization: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Organization=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(theUpdate, getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).update(ctx, itemcsid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updateOrganization", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Organization csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}/items/{itemcsid}")
    public Response deleteOrganization(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteOrganization with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("deleteOrganization: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Organization parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("deleteOrganization: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Organization=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            // Note that we have to create the service context for the Items, not the main service
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getItemServiceName());
            getRepositoryClient(ctx).delete(ctx, itemcsid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteOrganization", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Organization itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
