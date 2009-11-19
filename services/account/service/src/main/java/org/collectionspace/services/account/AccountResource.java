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
package org.collectionspace.services.account;

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
import org.collectionspace.services.common.context.RemoteServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/accounts")
@Consumes("application/xml")
@Produces("application/xml")
public class AccountResource
        extends AbstractCollectionSpaceResource {

    final private String serviceName = "accounts";
    final Logger logger = LoggerFactory.getLogger(AccountResource.class);

    @Override
    public String getServiceName() {
        return serviceName;
    }

    private <T> ServiceContext createServiceContext(T obj) {
        ServiceContext ctx = new RemoteServiceContextImpl<T, T>(getServiceName());
        ctx.setInput(obj);
        return ctx;
    }

    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        throw new IllegalStateException();
    }

    @POST
    public Response createAccount(AccountsCommon input) {
        try {
            ServiceContext ctx = createServiceContext(input);
            String csid = "";
            UriBuilder path = UriBuilder.fromResource(AccountResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createAccount", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public AccountsCommon getAccount(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getAccount with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getAccount: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Account csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        AccountsCommon result = null;
        try {
            ServiceContext ctx = createServiceContext((AccountsCommon)null);

            result = (AccountsCommon) ctx.getOutput();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAccount", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Account csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAccount", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Account CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Produces("application/xml")
    public AccountsCommonList getAccountList(@Context UriInfo ui) {
        AccountsCommonList accountList = new AccountsCommonList();
        try {
            ServiceContext ctx = createServiceContext((AccountsCommonList)null);

            accountList = null;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAccountList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return accountList;
    }

    @PUT
    @Path("{csid}")
    public AccountsCommon updateAccount(
            @PathParam("csid") String csid,
            AccountsCommon theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateAccount with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateAccount: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Account csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        AccountsCommon result = null;
        try {
            ServiceContext ctx = createServiceContext(theUpdate);

            result = (AccountsCommon)ctx.getOutput();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updateAccount", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Account csid=" + csid).type(
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
    public Response deleteAccount(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccount with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteAccount: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Account csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext ctx = createServiceContext((AccountsCommon)null);

            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteAccount", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Account csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
