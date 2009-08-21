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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.collectionobject.CollectionObjectList.*;

import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectHandlerFactory;
import org.collectionspace.services.common.NuxeoClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/collectionobjects")
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionObjectResource {

    public final static String CO_SERVICE_NAME = "collectionobjects";
    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);
    //FIXME retrieve client type from configuration
    final static NuxeoClientType CLIENT_TYPE = ServiceMain.getInstance().getNuxeoClientType();

    public CollectionObjectResource() {
        // do nothing
    }

    @POST
    public Response createCollectionObject(
            CollectionObject collectionObject) {

        String csid = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            CollectionObjectHandlerFactory handlerFactory = CollectionObjectHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(collectionObject);
            csid = client.create(CO_SERVICE_NAME, handler);
            collectionObject.setCsid(csid);
            if(logger.isDebugEnabled()){
                verbose("createCollectionObject: ", collectionObject);
            }
            UriBuilder path = UriBuilder.fromResource(CollectionObjectResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in createCollectionObject", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public CollectionObject getCollectionObject(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            verbose("getCollectionObject with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        CollectionObject collectionObject = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            CollectionObjectHandlerFactory handlerFactory = CollectionObjectHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.get(csid, handler);
            collectionObject = (CollectionObject) handler.getCommonObject();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("getCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("getCollectionObject", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if(collectionObject == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested CollectionObject CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("getCollectionObject: ", collectionObject);
        }
        return collectionObject;
    }

    @GET
    public CollectionObjectList getCollectionObjectList(@Context UriInfo ui) {
        CollectionObjectList collectionObjectList = new CollectionObjectList();
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            CollectionObjectHandlerFactory handlerFactory = CollectionObjectHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.getAll(CO_SERVICE_NAME, handler);
            collectionObjectList = (CollectionObjectList) handler.getCommonObjectList();
        }catch(Exception e){
            if(logger.isDebugEnabled()){
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
    public CollectionObject updateCollectionObject(
            @PathParam("csid") String csid,
            CollectionObject theUpdate) {
        if(logger.isDebugEnabled()){
            verbose("updateCollectionObject with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("updateCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("updateCollectionObject with input: ", theUpdate);
        }
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            CollectionObjectHandlerFactory handlerFactory = CollectionObjectHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(theUpdate);
            client.update(csid, handler);
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("caugth exception in updateCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on CollectionObject csid=" + csid).type(
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
    public Response deleteCollectionObject(@PathParam("csid") String csid) {

        if(logger.isDebugEnabled()){
            verbose("deleteCollectionObject with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("deleteCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on CollectionObject csid=" + csid).type(
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
                logger.debug("caught exception in deleteCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    private void verbose(String msg, CollectionObject collectionObject) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    CollectionObject.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(collectionObject, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void verbose(String msg) {
        System.out.println("CollectionObjectResource. " + msg);
    }
}
