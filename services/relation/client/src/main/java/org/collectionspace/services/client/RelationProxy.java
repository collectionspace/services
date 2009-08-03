package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/relations/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface RelationProxy {

    @GET
    ClientResponse<RelationList> getRelationList();

    //(C)reate
    @POST
    ClientResponse<Response> createRelation(Relation co);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<Relation> getRelation(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<Relation> updateRelation(@PathParam("csid") String csid, Relation co);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> deleteRelation(@PathParam("csid") String csid);
}