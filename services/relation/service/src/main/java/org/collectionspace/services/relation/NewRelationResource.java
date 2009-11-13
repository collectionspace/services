/**	
 * NewRelationResource.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright � 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.relation;

import java.util.Map;

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
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.relation.nuxeo.RelationHandlerFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/relations")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class NewRelationResource extends AbstractCollectionSpaceResource {

    public final static String serviceName = "relations";
    final Logger logger = LoggerFactory.getLogger(NewRelationResource.class);

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public DocumentHandler createDocumentHandler(RemoteServiceContext ctx) throws Exception {
        DocumentHandler docHandler = RelationHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        if(ctx.getInput() != null){
            Object obj = ctx.getInputPart(ctx.getCommonPartLabel(), RelationsCommon.class);
            if(obj != null){
                docHandler.setCommonPart((RelationsCommon) obj);
            }
        }
        return docHandler;
    }

    @POST
    public Response createRelation(MultipartInput input) {

        try{
            RemoteServiceContext ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(NewRelationResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in createRelation", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}")
    public MultipartOutput getRelation(@PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            logger.debug("getRelation with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getRelation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity("get failed on Relation csid=" + csid).type(
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
                logger.debug("getRelation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity("Get failed on Relation csid=" + csid).type(
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

        if(result == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Relation CSID:" + csid + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /*
     * BEGIN OF GET LIST
     */
    @GET
    @Produces("application/xml")
    public RelationsCommonList getRelationList(@Context UriInfo ui) {
        return this.getRelationList(null, null, null);
    }

    @GET
    @Path("subject/{subjectCsid}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_S(@Context UriInfo ui,
            @PathParam("subjectCsid") String subjectCsid) {
        return this.getRelationList(subjectCsid, null, null);
    }

    @GET
    @Path("type/{predicate}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_P(@Context UriInfo ui,
            @PathParam("predicate") String predicate) {
        return this.getRelationList(null, predicate, null);
    }

    @GET
    @Path("object/{objectCsid}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_O(@Context UriInfo ui,
            @PathParam("objectCsid") String objectCsid) {
        return this.getRelationList(null, null, objectCsid);
    }

    @GET
    @Path("type/{predicate}/subject/{subjectCsid}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_PS(@Context UriInfo ui,
            @PathParam("predicate") String predicate,
            @PathParam("subjectCsid") String subjectCsid) {
        return this.getRelationList(subjectCsid, predicate, null);
    }

    @GET
    @Path("subject/{subjectCsid}/type/{predicate}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_SP(@Context UriInfo ui,
            @PathParam("subjectCsid") String subjectCsid,
            @PathParam("predicate") String predicate) {
        return this.getRelationList(subjectCsid, predicate, null);
    }

    @GET
    @Path("type/{predicate}/object/{objectCsid}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_PO(@Context UriInfo ui,
            @PathParam("predicate") String predicate,
            @PathParam("objectCsid") String objectCsid) {
        return this.getRelationList(null, predicate, objectCsid);
    }

    @GET
    @Path("object/{objectCsid}/type/{predicate}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_OP(@Context UriInfo ui,
            @PathParam("objectCsid") String objectCsid,
            @PathParam("predicate") String predicate) {
        return this.getRelationList(null, predicate, objectCsid);
    }

    @GET
    @Path("type/{predicate}/subject/{subjectCsid}/object/{objectCsid}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_PSO(@Context UriInfo ui,
            @PathParam("predicate") String predicate,
            @PathParam("subjectCsid") String subjectCsid,
            @PathParam("objectCsid") String objectCsid) {
        return this.getRelationList(subjectCsid, predicate, objectCsid);
    }

    @GET
    @Path("subject/{subjectCsid}/type/{predicate}/object/{objectCsid}")
    @Produces("application/xml")
    public RelationsCommonList getRelationList_SPO(@Context UriInfo ui,
            @PathParam("subjectCsid") String subjectCsid,
            @PathParam("predicate") String predicate,
            @PathParam("objectCsid") String objectCsid) {
        return this.getRelationList(subjectCsid, predicate, objectCsid);
    }
    /*
     * END OF GET LIST
     */

    @PUT
    @Path("{csid}")
    public MultipartOutput updateRelation(@PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if(logger.isDebugEnabled()){
            logger.debug("updateRelation with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("updateRelation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity("update failed on Relation csid=" + csid).type(
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
                logger.debug("caugth exception in updateRelation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity("Update failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}")
    public Response deleteRelation(@PathParam("csid") String csid) {

        if(logger.isDebugEnabled()){
            logger.debug("deleteRelation with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("deleteRelation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity("delete failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try{
            ServiceContext ctx = createServiceContext(null);
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        }catch(DocumentNotFoundException dnfe){
            if(logger.isDebugEnabled()){
                logger.debug("caught exception in deleteRelation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity("Delete failed on Relation csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }catch(Exception e){
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /*
     * Private Methods
     */
    /**
     * Gets the relation list request.
     *
     * @return the relation list request
     *
     * @throws WebApplicationException the web application exception
     */
    private RelationsCommonList getRelationList(String subjectCsid,
            String predicate,
            String objectCsid)
            throws WebApplicationException {
        RelationsCommonList relationList = new RelationsCommonList();
        try{
            RemoteServiceContext ctx = createServiceContext(null);
            DocumentHandler handler = createDocumentHandler(ctx);
            Map propsFromPath = handler.getProperties();
            propsFromPath.put(IRelationsManager.SUBJECT, subjectCsid);
            propsFromPath.put(IRelationsManager.PREDICATE, predicate);
            propsFromPath.put(IRelationsManager.OBJECT, objectCsid);
            getRepositoryClient(ctx).getAll(ctx, handler);
            relationList = (RelationsCommonList) handler.getCommonPartList();
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in getRelationList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return relationList;
    }
}
