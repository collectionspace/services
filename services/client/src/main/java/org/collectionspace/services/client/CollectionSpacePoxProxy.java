package org.collectionspace.services.client;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

public interface CollectionSpacePoxProxy<LT extends AbstractCommonList> extends CollectionSpaceProxy<LT> {
    //(C)reate
    @POST
    ClientResponse<Response> create(byte[] payload);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<String> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<String> update(@PathParam("csid") String csid, byte[] payload);
    
    //(L)ist non-deleted items
    @GET
    @Produces({"application/xml"})
    ClientResponse<LT> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
}
