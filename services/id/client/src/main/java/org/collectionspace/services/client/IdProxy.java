package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

/**
 * IDProxy.
 *
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
 
@Path("/idgenerators/")
@Produces({"text/plain"})
public interface IdProxy extends CollectionSpaceProxy<String> {
    
    // Operations on ID Generators
       
    //(C)reate ID generator
    @POST
    @Consumes({"application/xml"})
    @Produces({"*/*"})
    ClientResponse<Response> create(String xmlPayload);
    
    //(R)ead ID Generator
    @GET
    @Path("/{csid}")
    @Produces({"application/xml"})
    ClientResponse<String> read(@PathParam("csid") String csid);
    
    // Read (L)ist of ID Generators
    @GET
    @Produces({"application/xml"})
    ClientResponse<String> readList();
    
    //(D)elete ID Generator
    @DELETE
    @Path("/{csid}")
    @Override
    ClientResponse<Response> delete(@PathParam("csid") String csid);
    
    // Operations on IDs
    
    //(C)reate ID
    @POST
    @Path("/{csid}/ids")
    ClientResponse<String> createId(@PathParam("csid") String csid);
 
}
