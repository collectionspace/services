package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
    Response create(String xmlPayload);
    
    //(R)ead ID Generator
    @GET
    @Path("/{csid}")
    @Produces({"application/xml"})
    Response read(@PathParam("csid") String csid);
    
    // Read (L)ist of ID Generators
    @GET
    @Produces({"application/xml"})
    Response readList();
    
    //(D)elete ID Generator
    @DELETE
    @Path("/{csid}")
    @Override
    Response delete(@PathParam("csid") String csid);
    
    // Operations on IDs
    
    //(C)reate ID
    @POST
    @Path("/{csid}/ids")
    Response createId(@PathParam("csid") String csid);
 
}
