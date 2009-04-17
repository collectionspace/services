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

import org.collectionspace.hello.PersonNuxeo;
import org.collectionspace.hello.People;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/persons/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface PersonNuxeoProxy {

    @GET
    ClientResponse<People> getPeople();

    @GET
    @Path("/{id}")
    ClientResponse<PersonNuxeo> getPerson(@PathParam("id") String id);

    @POST
    ClientResponse<Response> createPerson(PersonNuxeo so);

    @PUT
    @Path("/{id}")
    ClientResponse<PersonNuxeo> updatePerson(@PathParam("id") String id, PersonNuxeo so);

    @DELETE
    @Path("/{id}")
    ClientResponse<Response> deletePerson(@PathParam("id") String id);
}