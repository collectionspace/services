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

import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.intake.IntakeList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/intakes/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface IntakeProxy {

    @GET
    ClientResponse<IntakeList> readList();

    //(C)reate
    @POST
    ClientResponse<Response> create(Intake co);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<Intake> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<Intake> update(@PathParam("csid") String csid, Intake co);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}