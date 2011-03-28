package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * @version $Revision: 2108 $
 */
@Path(ObjectExitClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface ObjectExitProxy extends CollectionSpacePoxProxy {
    // List
    @GET
    ClientResponse<AbstractCommonList> readList();
}
