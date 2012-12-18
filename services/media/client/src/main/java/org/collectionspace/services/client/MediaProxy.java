package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.BlobClient;

/**
 * @version $Revision: 2108 $
 */
@Path(MediaClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface MediaProxy extends CollectionSpaceCommonListPoxProxy {

    @POST
    @Path("{csid}")
    @Consumes("multipart/form-data")
    ClientResponse<Response> createBlobFromFormData(@PathParam("csid") String csid,
    		MultipartFormDataOutput formDataOutput);
            
    @POST
    @Path("{csid}")
	@Produces("application/xml")
	@Consumes("application/xml")
    ClientResponse<Response>createBlobFromUri(@PathParam("csid") String csid,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri,
    		String emptyXML); //this "emptyXML" param is needed to force RESTEasy to produce a Content-Type header for this POST    

    @POST
	@Produces("application/xml")
	@Consumes("application/xml")
    ClientResponse<Response>createMediaAndBlobWithUri(byte[] xmlPayload,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri,
    		@QueryParam(BlobClient.BLOB_PURGE_ORIGINAL) boolean purgeOriginal);    
}
