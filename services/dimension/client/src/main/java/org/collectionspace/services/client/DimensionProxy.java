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

import org.collectionspace.services.dimension.DimensionsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

/**
 * @version $Revision:$
 */
@Path("/dimensions/")
@Produces({"multipart/mixed"})
@Consumes({"multipart/mixed"})
public interface DimensionProxy extends CollectionSpaceProxy {

    @GET
    @Produces({"application/xml"})
    ClientResponse<DimensionsCommonList> readList();

    //(C)reate
    @POST
    ClientResponse<Response> create(String payload);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<String> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<String> update(@PathParam("csid") String csid, String payload);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}
