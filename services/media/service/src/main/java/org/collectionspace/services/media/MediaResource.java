/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.media;

import org.collectionspace.services.blob.BlobResource;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.MediaClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(MediaClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class MediaResource extends NuxeoBasedResource {
    final static Logger logger = LoggerFactory.getLogger(MediaResource.class);
    final static MediaClient mediaClient = createMediaClient();
    
    private static MediaClient createMediaClient() {
    	MediaClient result = null;
    	
    	try {
			result = new MediaClient();
		} catch (Exception e) {
			String errMsg = "Could not create a new media client for the Media resource.";
			logger.error(errMsg, e);
			throw new RuntimeException();
		}
    	
    	return result;
    }

    @Override
    public String getServiceName(){
        return MediaClient.SERVICE_NAME;
    }
    
    public String getCommonPartName() {
    	return mediaClient.getCommonPartName();
    }
    
    private BlobResource blobResource = new BlobResource();
    
    BlobResource getBlobResource() {
    	return blobResource;
    }
    
//	/*
//	 * This member is used to get and set context for the blob document handler
//	 */
//	private BlobInput blobInput = new BlobInput();
//	
//    public BlobInput getBlobInput(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
//    	//
//    	// Publish the blobInput to the current context on every get.  Even though
//    	// it might already be published.
//    	//
//    	BlobUtil.setBlobInput(ctx, blobInput);
//		return blobInput;
//	}

	private String getBlobCsid(String mediaCsid) throws Exception {
		return getBlobCsid(mediaCsid, true);
	}

	private String getBlobCsid(String mediaCsid, boolean requireNotNull) throws Exception {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();

		BlobInput blobInput = BlobUtil.getBlobInput(mediaContext);
		blobInput.setSchemaRequested(true);

		get(mediaCsid, mediaContext); // This call sets the blobInput.blobCsid field for us

		String result = blobInput.getBlobCsid();

		if (requireNotNull) {
			ensureCSID(result, READ);
		}

		return result;
	}	
	
    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    public Class<MediaCommon> getCommonPartClass() {
    	return MediaCommon.class;
    }
    
    /*
     * Creates a new media record/resource AND creates a new blob (using a URL pointing to a media file/resource) and associates
     * it with the new media record/resource.
     */
    protected Response createBlobWithUri(ResourceMap resourceMap, UriInfo uriInfo, String xmlPayload, String blobUri) {
    	Response response = null;
    	
    	try {
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(BlobClient.SERVICE_NAME,
	    			uriInfo);
	    	BlobInput blobInput = BlobUtil.getBlobInput(ctx); // the blob doc handler will look for this in the context
	    	blobInput.createBlobFile(blobUri); // The blobUri argument is our payload
	    	response = this.create((PoxPayloadIn)null, ctx); // By now the binary bits have been created and we just need to create the metadata blob record -this info is in the blobInput var
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}

		return response;
    }
    
    /*
     * Looks for a blobUri query param from a POST.  If it finds one then it creates a blob AND a media resource and associates them.
     * (non-Javadoc)
     * @see org.collectionspace.services.common.ResourceBase#create(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.ResourceMap, javax.ws.rs.core.UriInfo, java.lang.String)
     */
    @Override
    public Response create(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
            String xmlPayload) {
    	Response result = null;
    	uriInfo = new UriInfoWrapper(uriInfo);
    	
    	//
    	// If we find a "blobUri" query param, then we need to create a blob resource/record first and then the media resource/record
    	//
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String blobUri = queryParams.getFirst(BlobClient.BLOB_URI_PARAM);
        if (blobUri != null && blobUri.isEmpty() == false) {
        	result = createBlobWithUri(resourceMap, uriInfo, xmlPayload, blobUri); // uses the blob resource and doc handler to create the blob
        	String blobCsid = CollectionSpaceClientUtils.extractId(result);
        	queryParams.add(BlobClient.BLOB_CSID_PARAM, blobCsid); // Add the new blob's csid as an artificial query param -the media doc handler will look for this
        }
        
       	result = super.create(parentCtx, resourceMap, uriInfo, xmlPayload); // Now call the parent to finish the media resource POST request
        
        return result;
    }
    
    @Override
    public byte[] update(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid,
    		String xmlPayload) {
    	uriInfo = new UriInfoWrapper(uriInfo);
    	//
    	// If we find a "blobUri" query param, then we need to create a blob resource/record first and then the media resource/record
    	//
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String blobUri = queryParams.getFirst(BlobClient.BLOB_URI_PARAM);
        if (blobUri != null && blobUri.isEmpty() == false) {
        	Response blobresult = createBlobWithUri(resourceMap, uriInfo, xmlPayload, blobUri); // uses the blob resource and doc handler to create the blob
        	String blobCsid = CollectionSpaceClientUtils.extractId(blobresult);
        	queryParams.add(BlobClient.BLOB_CSID_PARAM, blobCsid); // Add the new blob's csid as an artificial query param -the media doc handler will look for this
        }
       	return super.update(parentCtx, resourceMap, uriInfo, csid, xmlPayload); // Now call the parent to finish the media resource PUT request
    }

    /*
     * Creates a new blob (using a URL pointing to a media file/resource) and associates it with an existing media record/resource.
     */
    @POST
    @Path("{csid}")
    @Consumes("application/xml")
    @Produces("application/xml")    
    public Response createBlobWithUriAndUpdateMedia(@PathParam("csid") String csid,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
    	Response response = null;
    	PoxPayloadIn input = null;
    	
    	try {
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(BlobClient.SERVICE_NAME, input);
	    	BlobInput blobInput = BlobUtil.getBlobInput(ctx);
	    	blobInput.createBlobFile(blobUri);
	    	response = this.create(input, ctx); // calls the blob resource/doc-handler to create the blob
	    	//
	    	// Next, update the Media record to be linked to the blob
	    	//
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();
	    	BlobUtil.setBlobInput(mediaContext, blobInput); //and put the blobInput into the Media context
	    	this.update(csid, input, mediaContext);
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}

		return response;
    }    
   
    @PUT
    @Path("{csid}/blob")
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public Response updateMediaByCreatingBlob(@Context HttpServletRequest req,
    		@PathParam("csid") String csid,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
    	return this.createBlob(req, csid, blobUri);
    }
    
    /*
     * Creates a new blob (using the incoming multipart form data) and associates it with an existing media record/resource.
     * If a URL query param is passed in as well, we use the URL to create the new blob instead of the multipart form data.
     */
    @POST
    @Path("{csid}")
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    @Deprecated
    public Response createBlob(@Context HttpServletRequest req,
    		@PathParam("csid") String csid,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
    	PoxPayloadIn input = null;
    	Response response = null;
    	try {
    		if (blobUri == null) {
	    		//
	    		// First, create the blob
	    		//
		    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobClient.SERVICE_NAME, input);
		    	BlobInput blobInput = BlobUtil.getBlobInput(blobContext);
		    	blobInput.createBlobFile(req, null);
		    	response = this.create(input, blobContext);
		    	//
		    	// Next, update the Media record to be linked to the blob
		    	//
		    	ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();
		    	BlobUtil.setBlobInput(mediaContext, blobInput); //and put the blobInput into the Media context
		    	this.update(csid, input, mediaContext);
    		} else {
    			//A URI query param overrides the incoming multipart/form-data payload in the request
    			response = createBlobWithUriAndUpdateMedia(csid, blobUri);
    		}
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    			
		return response;
    }    

    @GET
    @Path("{csid}/blob")
    public byte[] getBlobInfo(@PathParam("csid") String csid) {
    	PoxPayloadOut result = null;
    	
	    try {
	        String blobCsid = this.getBlobCsid(csid);
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobClient.SERVICE_NAME);
	    	result = this.get(blobCsid, blobContext);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
	    return result.getBytes();
    }
    
    @GET
    @Path("{csid}/blob/content")
    public Response getBlobContent(
    		@PathParam("csid") String csid,
    		@Context Request requestInfo,
    		@Context UriInfo uriInfo) {
    	Response result = null;
    	
	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	result = getBlobResource().getBlobContent(blobCsid, requestInfo, uriInfo);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result;
    }
    
    @GET
    @Path("{csid}/blob/derivatives/{derivativeTerm}/content")
    public Response getDerivativeContent(
    		@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm,
    		@Context Request requestInfo,
    		@Context UriInfo uriInfo) {
    	Response result = null;
    	
	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	result = getBlobResource().getDerivativeContent(blobCsid, derivativeTerm, requestInfo, uriInfo);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result;
    }
            
		public InputStream getDerivativeContent(String csid, String derivativeTerm) {
			InputStream contentStream = null;

			try {
				ensureCSID(csid, READ);

				String blobCsid = this.getBlobCsid(csid, false);

				if (blobCsid != null) {
					ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobClient.SERVICE_NAME);
					StringBuffer mimeType = new StringBuffer();

					contentStream = getBlobResource().getBlobContent(blobContext, blobCsid, derivativeTerm, mimeType);
				}
			} catch (Exception e) {
					throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
			}

			return contentStream;
		}

    @GET
    @Path("{csid}/blob/derivatives/{derivativeTerm}")
    public byte[] getDerivative(@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm) {
    	PoxPayloadOut result = null;

	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobClient.SERVICE_NAME);
	    	String xmlPayload = getBlobResource().getDerivative(blobCsid, derivativeTerm);
	    	result = new PoxPayloadOut(xmlPayload.getBytes());
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result.getBytes();
    }
    
    @GET
    @Path("{csid}/blob/derivatives")
    @Produces("application/xml")    
    public CommonList getDerivatives(
    		@PathParam("csid") String csid) {
    	CommonList result = null;

	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobClient.SERVICE_NAME);
	    	result = getBlobResource().getDerivatives(blobCsid);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result;
    }
    
    @DELETE
    @Path("{csid}")
    @Override
    public Response delete(@PathParam("csid") String csid) {
        try {
        	//
        	// First, the media record so we can find and delete any related
        	// Blob instances.
        	//
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
        	PoxPayloadOut mediaPayload = this.get(csid, ctx);
        	PayloadOutputPart mediaPayloadPart = mediaPayload.getPart(getCommonPartName());
        	MediaCommon mediaCommon = (MediaCommon) mediaPayloadPart.getBody();
        	String blobCsid = mediaCommon.getBlobCsid();
        	//
        	// Delete the blob if it exists.
        	//
        	if (blobCsid != null && !blobCsid.isEmpty()) {
        		Response response = getBlobResource().delete(blobCsid);
        		if (logger.isDebugEnabled() == true) {
        			if (response.getStatus() != HttpResponseCodes.SC_OK) {
        				logger.debug("Problem deleting related blob record of Media record: " +
        						"Media CSID=" + csid + " " +
        						"Blob CSID=" + blobCsid);
        			}
        		}
        	}
        	//
        	// Now that we've handled the related Blob record, delete the Media record
        	//
            return super.delete(ctx, csid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }
    
}
