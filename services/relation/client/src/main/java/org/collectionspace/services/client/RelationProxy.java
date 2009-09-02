package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
    ClientResponse<RelationList> readList();
    
	@GET
	@Path("subject/{subjectCsid}/type/{predicate}/object/{objectCsid}")
	ClientResponse<RelationList> readList_SPO(@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid);

    //(C)reate
    @POST
    ClientResponse<Response> create(Relation co);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<Relation> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<Relation> update(@PathParam("csid") String csid, Relation co);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}