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

import org.collectionspace.hello.Person;
import org.collectionspace.hello.Persons;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/persons/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface PersonProxy {

    @GET
    ClientResponse<Persons> getPersons();

    @GET
    @Path("/{id}")
    ClientResponse<Person> getPerson(@PathParam("id") Long id);

    @POST
    ClientResponse<Response> createPerson(Person so);

    @PUT
    @Path("/{id}")
    ClientResponse<Person> updatePerson(@PathParam("id") Long id, Person so);

    @DELETE
    @Path("/{id}")
    ClientResponse<Response> deletePerson(@PathParam("id") Long id);
}