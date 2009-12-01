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
package org.collectionspace.services.collectionobject;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectHandlerFactory;
import org.collectionspace.services.common.AbstractCollectionSpaceResource;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/collectionobjects")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class CollectionObjectResource
        extends AbstractCollectionSpaceResource {

    final private String serviceName = "collectionobjects";
    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        DocumentHandler docHandler = CollectionObjectHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        if (ctx.getInput() != null) {
            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), CollectionobjectsCommon.class);
            if (obj != null) {
                docHandler.setCommonPart((CollectionobjectsCommon) obj);
            }
        }
        return docHandler;
    }

    @POST
    public Response createCollectionObject(MultipartInput input) {
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(input, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(CollectionObjectResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createCollectionObject", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public MultipartOutput getCollectionObject(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getCollectionObject with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
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
                logger.debug("getCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getCollectionObject", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested CollectionObject CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Produces("application/xml")
    public CollectionobjectsCommonList getCollectionObjectList(@Context UriInfo ui) {
        CollectionobjectsCommonList collectionObjectList = new CollectionobjectsCommonList();
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getAll(ctx, handler);
            collectionObjectList = (CollectionobjectsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getCollectionObjectList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return collectionObjectList;
    }

    @PUT
    @Path("{csid}")
    public MultipartOutput updateCollectionObject(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateCollectionObject with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on CollectionObject csid=" + csid).type(
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
                logger.debug("caugth exception in updateCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on CollectionObject csid=" + csid).type(
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
    public Response deleteCollectionObject(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteCollectionObject with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on CollectionObject csid=" + csid).type(
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
                logger.debug("caught exception in deleteCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
