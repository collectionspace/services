package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.workflow.WorkflowClient;

/**
 * @version $Revision: 2108 $
 */
@Path(MediaClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface MediaProxy extends CollectionSpacePoxProxy<AbstractCommonList> {

    // List
    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> readList();
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    @POST
    @Path("/{csid}")
    @Consumes("multipart/form-data")
    ClientResponse<Response> createBlobFromFormData(@PathParam("csid") String csid,
    		MultipartFormDataOutput formDataOutput);
            
    @POST
    @Path("/{csid}")
	@Produces("application/xml")
	@Consumes("application/xml")
    ClientResponse<Response>createBlobFromUri(@PathParam("csid") String csid,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri);
    
}
