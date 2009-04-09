package org.collectionspace.hello.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.hello.DomainIdentifier;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/domainidentifiers/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface DomainIdentifierProxy {

    /**
     * @param id
     * @return
     */
    @GET
    @Path("/{id}")
    ClientResponse<DomainIdentifier> getIdentifier(@PathParam("id") String id);

    @POST
    ClientResponse<Response> createIdentifier(DomainIdentifier so);
}