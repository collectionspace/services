package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.jboss.resteasy.client.ClientResponse;

@Path("/idgenerators/")
@Produces({"application/xml"})
@Consumes({"text/plain"})
public interface IdProxy extends CollectionSpaceProxy {

    @GET
    @Produces({"application/xml"})
    ClientResponse<String> readList();

    //(C)reate
    @POST
    @Path("/{csid}/ids")
    @Produces({"text/plain"})
    ClientResponse<String> createId(@PathParam("csid") String csid);

    //(R)ead
    @GET
    @Path("/{csid}")
    @Produces({"application/xml"})
    ClientResponse<String> read(@PathParam("csid") String csid);
}
