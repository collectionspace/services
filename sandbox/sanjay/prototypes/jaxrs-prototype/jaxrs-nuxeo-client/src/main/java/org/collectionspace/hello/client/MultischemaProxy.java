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
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

/**
 * @version $Revision:$
 */
@Path("/multischema/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface MultischemaProxy {


    @GET
    @Path("/{id}")
    @Produces("multipart/form-data")
    ClientResponse<MultipartFormDataInput> getPerson(@PathParam("id") String id);

    @POST
    @Consumes("multipart/form-data")
    ClientResponse<Response> createPerson(MultipartFormDataOutput multipartPerson);

    @PUT
    @Path("/{id}")
    ClientResponse<PersonNuxeo> updatePerson(@PathParam("id") String id, PersonNuxeo so);

    @DELETE
    @Path("/{id}")
    ClientResponse<Response> deletePerson(@PathParam("id") String id);
}