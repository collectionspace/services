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
package org.collectionspace.services.acquisition;

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

import org.collectionspace.services.acquisition.AcquisitionList.*;

import org.collectionspace.services.acquisition.nuxeo.AcquisitionConstants;
import org.collectionspace.services.acquisition.nuxeo.AcquisitionHandlerFactory;
import org.collectionspace.services.common.NuxeoClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/acquisitions")
@Consumes("application/xml")
@Produces("application/xml")
public class AcquisitionResource {

    public final static String ACQUISITION_SERVICE_NAME = "acquisitions";
    final Logger logger = LoggerFactory.getLogger(AcquisitionResource.class);
    //FIXME retrieve client type from configuration
    final static NuxeoClientType CLIENT_TYPE = ServiceMain.getInstance().getNuxeoClientType();

    public AcquisitionResource() {
        // do nothing
    }

    @POST
    public Response createAcquisition(
            Acquisition acquisitionObject) {

        String csid = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            AcquisitionHandlerFactory handlerFactory = AcquisitionHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(acquisitionObject);
            csid = client.create(ACQUISITION_SERVICE_NAME, handler);
            acquisitionObject.setCsid(csid);
            if(logger.isDebugEnabled()){
                verbose("createAcquisition: ", acquisitionObject);
            }
            UriBuilder path = UriBuilder.fromResource(AcquisitionResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in createAcquisition", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public Acquisition getAcquisition(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            verbose("getAcquisition with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Acquisition acquisitionObject = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            AcquisitionHandlerFactory handlerFactory = AcquisitionHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.get(csid, handler);
            acquisitionObject = (Acquisition) handler.getCommonObject();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("getAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("getAcquisition", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if(acquisitionObject == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Acquisition CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("getAcquisition: ", acquisitionObject);
        }
        return acquisitionObject;
    }

    @GET
    public AcquisitionList getAcquisitionList(@Context UriInfo ui) {
        AcquisitionList acquisitionObjectList = new AcquisitionList();
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            AcquisitionHandlerFactory handlerFactory = AcquisitionHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.getAll(ACQUISITION_SERVICE_NAME, handler);
            acquisitionObjectList = (AcquisitionList) handler.getCommonObjectList();
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in getAcquisitionList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return acquisitionObjectList;
    }

    @PUT
    @Path("{csid}")
    public Acquisition updateAcquisition(
            @PathParam("csid") String csid,
            Acquisition theUpdate) {
        if(logger.isDebugEnabled()){
            verbose("updateAcquisition with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("updateAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("updateAcquisition with input: ", theUpdate);
        }
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            AcquisitionHandlerFactory handlerFactory = AcquisitionHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(theUpdate);
            client.update(csid, handler);
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("caugth exception in updateAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Acquisition csid=" + csid).type(
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
    public Response deleteAcquisition(@PathParam("csid") String csid) {

        if(logger.isDebugEnabled()){
            verbose("deleteAcquisition with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("deleteAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Acquisition csid=" + csid).type(
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
                logger.debug("caught exception in deleteAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    private void verbose(String msg, Acquisition acquisitionObject) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    Acquisition.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(acquisitionObject, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void verbose(String msg) {
        System.out.println("AcquisitionResource. " + msg);
    }
}
