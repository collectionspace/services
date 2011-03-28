package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.dimension.DimensionsCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/dimensions/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface DimensionProxy extends CollectionSpacePoxProxy {
    @GET
    @Produces({"application/xml"})
    ClientResponse<DimensionsCommonList> readList();
}
