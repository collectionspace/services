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

import org.collectionspace.services.note.NotesCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/notes/")
@Produces({"application/xml;charset=UTF-8"})
@Consumes({"application/xml"})
public interface NoteProxy extends CollectionSpaceProxy<NotesCommonList> {

    @GET
    @Produces({"application/xml"})
    ClientResponse<NotesCommonList> readList();

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
