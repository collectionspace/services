package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;


/**
 * @version $Revision: 2108 $
 */
@Path(BlobClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface BlobProxy extends CollectionSpaceCommonListPoxProxy {
    //(C)reate
    @POST
    Response createBlobFromURI(byte[] xmlPayload, 
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri);

    //(C)reate
    @POST
    @Consumes("multipart/form-data")
    Response createBlobFromFormData(MultipartFormDataOutput formDataOutput);

    @GET
    @Path("{csid}/content")
    Response getBlobContent(@PathParam("csid") String csid);
    
    @GET
    @Path("{csid}/derivatives/{derivativeTerm}/content")
    public Response getDerivativeContent(
    		@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm);    
}