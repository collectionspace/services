package org.collectionspace.services.relation;

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

import org.collectionspace.services.relation.RelationList.RelationListItem;

import org.collectionspace.services.relation.nuxeo.RelationNuxeoConstants;
import org.collectionspace.services.relation.nuxeo.RelationHandlerFactory;
import org.collectionspace.services.common.NuxeoClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/relations")
@Consumes("application/xml")
@Produces("application/xml")
public class NewRelationResource {

    public final static String SERVICE_NAME = "relations";
    final Logger logger = LoggerFactory.getLogger(NewRelationResource.class);
    //FIXME retrieve client type from configuration
    final static NuxeoClientType CLIENT_TYPE = ServiceMain.getInstance().getNuxeoClientType();

    public NewRelationResource() {
        // do nothing
    }

    @POST
    public Response createRelation(
            Relation relation) {

        String csid = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            RelationHandlerFactory handlerFactory = RelationHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(relation);
            csid = client.create(SERVICE_NAME, handler);
            relation.setCsid(csid);
            if(logger.isDebugEnabled()){
                verbose("createRelation: ", relation);
            }
            UriBuilder path = UriBuilder.fromResource(RelationResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in createRelation", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public Relation getRelation(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            verbose("getRelation with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getRelation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        Relation relation = null;
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            RelationHandlerFactory handlerFactory = RelationHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.get(csid, handler);
            relation = (Relation) handler.getCommonObject();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("getRelation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("getRelation", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if(relation == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Relation CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("getRelation: ", relation);
        }
        return relation;
    }

    @GET
    public RelationList getRelationList(@Context UriInfo ui) {
        RelationList relationList = new RelationList();
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            RelationHandlerFactory handlerFactory = RelationHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            client.getAll(SERVICE_NAME, handler);
            relationList = (RelationList) handler.getCommonObjectList();
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in getRelationList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return relationList;
    }

    @PUT
    @Path("{csid}")
    public Relation updateRelation(
            @PathParam("csid") String csid,
            Relation theUpdate) {
        if(logger.isDebugEnabled()){
            verbose("updateRelation with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("updateRelation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if(logger.isDebugEnabled()){
            verbose("updateRelation with input: ", theUpdate);
        }
        try{
            RepositoryClientFactory clientFactory = RepositoryClientFactory.getInstance();
            RepositoryClient client = clientFactory.getClient(CLIENT_TYPE.toString());
            RelationHandlerFactory handlerFactory = RelationHandlerFactory.getInstance();
            DocumentHandler handler = (DocumentHandler) handlerFactory.getHandler(CLIENT_TYPE.toString());
            handler.setCommonObject(theUpdate);
            client.update(csid, handler);
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("caugth exception in updateRelation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Relation csid=" + csid).type(
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
    public Response deleteRelation(@PathParam("csid") String csid) {

        if(logger.isDebugEnabled()){
            verbose("deleteRelation with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("deleteRelation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Relation csid=" + csid).type(
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
                logger.debug("caught exception in deleteRelation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    private void verbose(String msg, Relation relation) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    Relation.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(relation, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void verbose(String msg) {
        System.out.println("RelationResource. " + msg);
    }
}
