/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 Regents of the University of California

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
package org.collectionspace.services.contact;

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

import org.collectionspace.services.common.AbstractCollectionSpaceResource;
import org.collectionspace.services.contact.ContactsCommonList.*;

import org.collectionspace.services.contact.nuxeo.ContactHandlerFactory;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/contacts")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class ContactResource extends AbstractCollectionSpaceResource {

    private final static String serviceName = "contacts";
    final Logger logger = LoggerFactory.getLogger(ContactResource.class);
    //FIXME retrieve client type from configuration
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    public ContactResource() {
        // do nothing
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        DocumentHandler docHandler = ContactHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        if (ctx.getInput() != null) {
            Object obj = ((MultipartServiceContext)ctx).getInputPart(ctx.getCommonPartLabel(), ContactsCommon.class);
            if (obj != null) {
                docHandler.setCommonPart((ContactsCommon) obj);
            }
        }
        return docHandler;
    }

    @POST
    public Response createContact(MultipartInput input) {
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(input, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //contactObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(ContactResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createContact", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public MultipartOutput getContact(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getContact with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "Get failed on Contact csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Contact csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getContact", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Contact CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Produces("application/xml")
    public ContactsCommonList getContactList(@Context UriInfo ui) {
        ContactsCommonList contactObjectList = new ContactsCommonList();
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getAll(ctx, handler);
            contactObjectList = (ContactsCommonList) handler.getCommonPartList();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getContactList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return contactObjectList;
    }

    @PUT
    @Path("{csid}")
    public MultipartOutput updateContact(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateContact with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(theUpdate, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in updateContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Contact csid=" + csid).type(
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
    public Response deleteContact(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteContact with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "Delete failed on Contact csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in deleteContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Contact csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
