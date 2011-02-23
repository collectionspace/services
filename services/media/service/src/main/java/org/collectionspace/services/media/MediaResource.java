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

import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.MediaClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.blob.BlobResource;
import org.collectionspace.services.nuxeo.client.java.CommonList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.InputStream;
import java.util.List;

@Path(MediaClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class MediaResource extends ResourceBase {
    final Logger logger = LoggerFactory.getLogger(MediaResource.class);

    @Override
    public String getServiceName(){
        return MediaClient.SERVICE_NAME;
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
		String result = null;
		
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();
    	BlobInput blobInput = BlobUtil.getBlobInput(mediaContext);
    	blobInput.setSchemaRequested(true);
        get(mediaCsid, mediaContext); //this call sets the blobInput.blobCsid field for us
        result = blobInput.getBlobCsid();
    	ensureCSID(result, READ);
		
        return result;
	}	
	
    //FIXME retrieve client type from configuration
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    public Class<MediaCommon> getCommonPartClass() {
    	return MediaCommon.class;
    }

    @POST
    @Override
    public Response create(@Context UriInfo ui,
    		String xmlPayload) {
    	Response response = null;
    	PoxPayloadIn input = null;
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	String blobUri = queryParams.getFirst(BlobClient.BLOB_URI_PARAM);
    	
    	try {
    		if (blobUri != null) {
		    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(BlobUtil.BLOB_RESOURCE_NAME, input);
		    	BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		    	blobInput.createBlobFile(blobUri);
		    	response = this.create(input, ctx);
    		} else {
    			response = super.create(ui, xmlPayload);
    		}
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    			
		return response;
    }    
    
    @POST
    @Path("{csid}")
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public Response createBlob(@Context HttpServletRequest req,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri,
    		@PathParam("csid") String csid) {
    	PoxPayloadIn input = null;
    	Response response = null;    	
    	try {
    		//
    		// First, create the blob
    		//
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobUtil.BLOB_RESOURCE_NAME, input);
	    	BlobInput blobInput = BlobUtil.getBlobInput(blobContext);
	    	blobInput.createBlobFile(req, blobUri);
	    	response = this.create(input, blobContext);
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

    @GET
    @Path("{csid}/blob")
    public byte[] getBlobInfo(@PathParam("csid") String csid) {
    	PoxPayloadOut result = null;
    	
	    try {
	        String blobCsid = this.getBlobCsid(csid);
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobUtil.BLOB_RESOURCE_NAME);
	    	result = this.get(blobCsid, blobContext);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
	    return result.getBytes();
    }
    
    @GET
    @Path("{csid}/blob/content")
    @Produces({"image/jpeg", "image/png", "image/tiff"})
    public InputStream getBlobContent(
    		@PathParam("csid") String csid) {
    	InputStream result = null;
    	
	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	result = getBlobResource().getBlobContent(blobCsid);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result;
    }
    
    @GET
    @Path("{csid}/blob/derivatives/{derivativeTerm}/content")
    @Produces({"image/jpeg", "image/png", "image/tiff"})
    public InputStream getDerivativeContent(
    		@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm) {
    	InputStream result = null;
    	
	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	result = getBlobResource().getDerivativeContent(blobCsid, derivativeTerm);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result;
    }
            
    @GET
    @Path("{csid}/blob/derivatives/{derivativeTerm}")
    public byte[] getDerivative(@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm) {
    	PoxPayloadOut result = null;

	    try {
	    	ensureCSID(csid, READ);
	        String blobCsid = this.getBlobCsid(csid);
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobUtil.BLOB_RESOURCE_NAME);
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
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobUtil.BLOB_RESOURCE_NAME);
	    	result = getBlobResource().getDerivatives(blobCsid);	        
	    } catch (Exception e) {
	        throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
	    }
	    
    	return result;
    }
}
