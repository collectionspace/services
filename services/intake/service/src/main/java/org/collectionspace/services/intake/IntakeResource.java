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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.intake.IntakeList.*;

import org.collectionspace.services.intake.nuxeo.IntakeConstants;
import org.collectionspace.services.intake.nuxeo.IntakeHandlerFactory;
import org.collectionspace.services.common.NuxeoClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/intakes")
@Consumes("application/xml")
@Produces("application/xml")
public class IntakeResource {

    public final static String INTAKE_SERVICE_NAME = "intakes";
    final Logger logger = LoggerFactory.getLogger(IntakeResource.class);
    //FIXME retrieve client type from configuration
    final static NuxeoClientType CLIENT_TYPE = ServiceMain.getInstance().getNuxeoClientType();

    public IntakeResource() {
        // do nothing
    }

    @POST
    public Response createIntake(
            Intake intakeObject) {

        String csid = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            IntakeHandlerFactory handlerFactory = IntakeHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(intakeObject);
            csid = client.create(INTAKE_SERVICE_NAME, handler);
            intakeObject.setCsid(csid);
            if(logger.isDebugEnabled()){
                verbose("createIntake: ", intakeObject);
            }
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
    public Intake getIntake(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            verbose("getIntake with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getIntake: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Intake intakeObject = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            IntakeHandlerFactory handlerFactory = IntakeHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.get(csid, handler);
            intakeObject = (Intake) handler.getCommonObject();
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

        if(intakeObject == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Intake CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("getIntake: ", intakeObject);
        }
        return intakeObject;
    }

    @GET
    public IntakeList getIntakeList(@Context UriInfo ui) {
        IntakeList intakeObjectList = new IntakeList();
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            IntakeHandlerFactory handlerFactory = IntakeHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.getAll(INTAKE_SERVICE_NAME, handler);
            intakeObjectList = (IntakeList) handler.getCommonObjectList();
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
    public Intake updateIntake(
            @PathParam("csid") String csid,
            Intake theUpdate) {
        if(logger.isDebugEnabled()){
            verbose("updateIntake with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("updateIntake: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("updateIntake with input: ", theUpdate);
        }
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            IntakeHandlerFactory handlerFactory = IntakeHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(theUpdate);
            client.update(csid, handler);
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
        return theUpdate;
    }

    @DELETE
    @Path("{csid}")
    public Response deleteIntake(@PathParam("csid") String csid) {

        if(logger.isDebugEnabled()){
            verbose("deleteIntake with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("deleteIntake: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Intake csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            client.delete(csid);
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

    private void verbose(String msg, Intake intakeObject) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    Intake.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(intakeObject, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void verbose(String msg) {
        System.out.println("IntakeResource. " + msg);
    }
}
