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
    ClientResponse<CollectionObjectList> getCollectionObjectList();

    //(C)reate
    @POST
    ClientResponse<Response> createCollectionObject(CollectionObject co);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<CollectionObject> getCollectionObject(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<CollectionObject> updateCollectionObject(@PathParam("csid") String csid, CollectionObject co);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> deleteCollectionObject(@PathParam("csid") String csid);
}