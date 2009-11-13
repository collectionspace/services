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
package org.collectionspace.services.intake;

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
import org.collectionspace.services.intake.IntakesCommonList.*;

import org.collectionspace.services.intake.nuxeo.IntakeHandlerFactory;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/intakes")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class IntakeResource extends AbstractCollectionSpaceResource {

    private final static String serviceName = "intakes";
    final Logger logger = LoggerFactory.getLogger(IntakeResource.class);
    //FIXME retrieve client type from configuration
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    public IntakeResource() {
        // do nothing
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public DocumentHandler createDocumentHandler(RemoteServiceContext ctx) throws Exception {
        DocumentHandler docHandler = IntakeHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        if(ctx.getInput() != null){
            Object obj = ctx.getInputPart(ctx.getCommonPartLabel(), IntakesCommon.class);
            if(obj != null){
                docHandler.setCommonPart((IntakesCommon) obj);
            }
        }
        return docHandler;
    }

    @POST
    public Response createIntake(MultipartInput input) {
        try{
            RemoteServiceContext ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //intakeObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(IntakeResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in createIntake", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public MultipartOutput getIntake(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            logger.debug("getIntake with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getIntake: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try{
            RemoteServiceContext ctx = createServiceContext(null);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("getIntake", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("getIntake", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if(result == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Intake CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @GET
    @Produces("application/xml")
    public IntakesCommonList getIntakeList(@Context UriInfo ui) {
        IntakesCommonList intakeObjectList = new IntakesCommonList();
        try{
            RemoteServiceContext ctx = createServiceContext(null);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getAll(ctx, handler);
            intakeObjectList = (IntakesCommonList) handler.getCommonPartList();
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in getIntakeList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return intakeObjectList;
    }

    @PUT
    @Path("{csid}")
    public MultipartOutput updateIntake(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if(logger.isDebugEnabled()){
            logger.debug("updateIntake with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("updateIntake: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try{
            RemoteServiceContext ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("caugth exception in updateIntake", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}")
    public Response deleteIntake(@PathParam("csid") String csid) {

        if(logger.isDebugEnabled()){
            logger.debug("deleteIntake with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("deleteIntake: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try{
            ServiceContext ctx = createServiceContext(null);
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("caught exception in deleteIntake", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
