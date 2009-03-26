package org.collectionspace.hello.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.hello.CollectionObject;
import org.collectionspace.hello.CollectionObjectList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/collectionobjects/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface CollectionObjectProxy {

    @GET
    @Path("/{id}")
    ClientResponse<CollectionObject> getCollectionObject(@PathParam("id") String id);

    // List
    @GET
    ClientResponse<CollectionObjectList> getCollectionObjectList();

    @POST
    ClientResponse<Response> createCollectionObject(CollectionObject co);

    @PUT
    @Path("/{id}")
    ClientResponse<CollectionObject> 
      updateCollectionObject(@PathParam("id") String id, CollectionObject co);

    @DELETE
    @Path("/{id}")
    ClientResponse<Response> deleteCollectionObject(@PathParam("id") String id);
}