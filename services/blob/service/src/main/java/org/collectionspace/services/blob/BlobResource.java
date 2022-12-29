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
package org.collectionspace.services.blob;

import org.collectionspace.services.publicitem.PublicitemsCommon;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PayloadPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.FileUtilities;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.publicitem.PublicItemUtil;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.common.CSWebApplicationException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//FIXME: REM - We should not have Nuxeo dependencies in our resource classes.

@Path(BlobClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class BlobResource extends NuxeoBasedResource {
	
	private static final int DEFAULT_MAX_CACHE_AGE = 86400; // 1 day of seconds.
	private static final String DERIVATIVES_REGEX = "(\\w+)(/blobs/\\*/derivatives/)(\\w+)(/content)"; // matches things like 'GET/blobs/*/derivatives/Medium/content'
	
	@Override
    public String getServiceName(){
        return BlobClient.SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    public Class<BlobsCommon> getCommonPartClass() {
    	return BlobsCommon.class;
    }

    @Deprecated
    public CommonList getBlobList(List<String> csidList) {
        return (CommonList) getList(csidList);
    }

//    @Deprecated
//    protected CommonList search(MultivaluedMap<String,String> queryParams,String keywords) {
//         return (CommonList) super.search(queryParams, keywords);
//    }
    
    private CommonList getDerivativeList(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid) throws Exception {
    	CommonList result = null;
    	
    	BlobInput blobInput = new BlobInput();
    	blobInput.setDerivativeListRequested(true);
    	BlobUtil.setBlobInput(ctx, blobInput);

    	PoxPayloadOut response = this.get(csid, ctx);  //FIXME: Derivatives should get their own document handler -something like DerivativeDocumentModelHandler.
    	if (logger.isDebugEnabled() == true) {
    		logger.debug(response.toString());
    	}
    	//
    	// The result of a successful get should have put the results in the
    	// blobInput instance
    	//
    	result = BlobUtil.getBlobInput(ctx).getDerivativeList();
    	
    	return result;
    }
    
    private InputStream getBlobContent(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, 
    		String derivativeTerm, 
    		StringBuffer outMimeType) throws CSWebApplicationException {
    	InputStream result = null;
    	
    	try {
	    	BlobInput blobInput = BlobUtil.getBlobInput(ctx);
	    	blobInput.setDerivativeTerm(derivativeTerm);
	    	blobInput.setContentRequested(true);
	    	
	    	PoxPayloadOut response = this.get(csid, ctx);
	    	if (logger.isDebugEnabled() == true) {
	    		logger.debug(response.toString());
	    	}
	    	//
	    	// The result of a successful get should have put the results in the
	    	// blobInput instance
	    	//
	    	
	    	String mimeType = blobInput.getMimeType();
	    	if (mimeType != null) {
	    		outMimeType.append(mimeType); // blobInput's mime type was set on call to "get" above by the doc handler
	    	}
	    	result = BlobUtil.getBlobInput(ctx).getContentStream();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    	
    	if (result == null) {
    		String errMsg = String.format("Index failed. Could not get the contents for the Blob with CSID = '%s'.",
    				csid);
	        Response response = Response.status(
	                Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).type("text/plain").build();
	        throw new CSWebApplicationException(response);
    	}
    	
    	return result;
    }

    private File getBlobContentFile(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, 
    		String derivativeTerm, 
    		StringBuffer outMimeType) throws CSWebApplicationException {
    	File result = null;
    	
    	try {
	    	BlobInput blobInput = BlobUtil.getBlobInput(ctx);
	    	blobInput.setDerivativeTerm(derivativeTerm);
	    	blobInput.setContentRequested(true);
	    	
	    	PoxPayloadOut response = this.get(csid, ctx);
	    	if (logger.isDebugEnabled() == true) {
	    		logger.debug(response.toString());
	    	}
	    	//
	    	// The result of a successful get should have put the results in the
	    	// blobInput instance
	    	//
	    	
	    	String mimeType = blobInput.getMimeType();
	    	if (mimeType != null) {
	    		outMimeType.append(mimeType); // blobInput's mime type was set on call to "get" above by the doc handler
	    	}
	    	result = BlobUtil.getBlobInput(ctx).getBlobFile();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    	
    	if (result == null) {
    		String errMsg = String.format("Index failed. Could not get the contents for the Blob with CSID = '%s'.",
    				csid);
	        Response response = Response.status(
	                Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).type("text/plain").build();
	        throw new CSWebApplicationException(response);
    	}
    	
    	return result;
    }
    
    /*
     * This method can replace the 'createBlob' -specifically, this JAX-RS technique can replace the call to
     * the BlobInput.createBlobFile() method.  In theory, this should reduce by 1 the number of times we need to copy
     * bits around.
     */
    @POST
    @Path("{csid}/prototype")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN)
    @Deprecated
    public Response prototype(@PathParam("csid") String csid,
    		@Context HttpServletRequest req,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri,
    		MultipartFormDataInput partFormData) {
    	Response response = null;    	
    	try {
    		InputStream fileStream = null;
    		String preamble = partFormData.getPreamble();
    		System.out.println("Preamble type is:" + preamble);
    		
    		Map<String, List<InputPart>> partsMap = partFormData.getFormDataMap();
    		List<InputPart> fileParts = partsMap.get("file");
    		
    		for (InputPart part : fileParts)
    		{
    			String mediaType = part.getMediaType().toString();
    			System.out.println("Media type is:" + mediaType);
    			fileStream = part.getBody(InputStream.class, null);
    			FileUtilities.createTmpFile(fileStream, getServiceName() + "_");
    		}
    		
	    	ResponseBuilder rb = Response.ok();
	    	rb.entity("Goodbye, world!");
	    	response = rb.build();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    			
		return response;
    }    
    
    @POST
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public Response createBlob(@Context HttpServletRequest req,
    		@QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
    	Response response = null;    	
    	try {
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
	    	BlobInput blobInput = BlobUtil.getBlobInput(ctx);
	    	blobInput.createBlobFile(req, blobUri);
	    	response = this.create(null, ctx);
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    			
		return response;
    }
    
    @POST
    @Override
    public Response create(
    		@Context ResourceMap resourceMap, 
    		@Context UriInfo ui,
    		String xmlPayload) {
    	Response response = null;
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	String blobUri = queryParams.getFirst(BlobClient.BLOB_URI_PARAM);
    	
    	try {
    		if (blobUri != null) { // If we were passed a URI than try to create a blob from it
		    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
		    	BlobInput blobInput = BlobUtil.getBlobInput(ctx); // We're going to store a reference to the blob in the current context for the doc handler to use later
		    	blobInput.createBlobFile(blobUri); // This call creates a temp blob file and points our blobInput to it
		    	response = this.create(null, ctx); // Now finish the create.  We'll ignore the xmlPayload since we'll derive it from the blob itself
    		} else {
    			// No URI was passed in so we're just going to create a blob record
    			response = super.create(resourceMap, ui, xmlPayload);
    		}
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    			
		return response;
    }    
    
    /**
     * If there is no explicit setting in the Blobs service binding, we'll ask the HTTP client to cache blobs for 1 full day.
     * @param ctx
     * @param cacheKey
     * @return
     */
    @Override
    protected CacheControl getCacheControl(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String cacheKey) {
    	CacheControl result = null;
    	
    	if (cacheKey.matches(DERIVATIVES_REGEX)) {
    		Pattern p = Pattern.compile(DERIVATIVES_REGEX);
    		Matcher m = p.matcher(cacheKey);
    		if (m.find()) {
    			cacheKey = String.format("%s%s*%s", m.group(1), m.group(2), m.group(4));  // Converts something like this "GET/blobs/*/derivatives/Medium/content" into this "GET/blobs/*/derivatives/*/content"
    		}
    	}
    	
    	result = super.getCacheControl(ctx, cacheKey);
    	if (result == null) {
    		result = new CacheControl();
    		result.setMaxAge(DEFAULT_MAX_CACHE_AGE);
    	}
    	
    	return result;
    }
    
    @GET
    @Path("{csid}/content")
    public Response getBlobContent(
    		@PathParam("csid") String csid,
    		@Context Request jaxRsRequest,
    		@Context UriInfo uriInfo) {
    	Response result = null;
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null; 
    	
    	try {
	    	ctx = createServiceContext(jaxRsRequest, uriInfo);
			BlobsCommon blobsCommon = getBlobsCommon(csid);
	    	StringBuffer mimeType = new StringBuffer();
//	    	InputStream contentStream = getBlobContent(ctx, csid, null /*derivative term*/, mimeType /*will get set*/);
	    	File contentFile = getBlobContentFile(ctx, csid, null /*derivative term*/, mimeType /*will get set*/);
		    
	    	Response.ResponseBuilder responseBuilder = Response.ok(contentFile, mimeType.toString());
	    	setCacheControl(ctx, responseBuilder);
	    	responseBuilder = responseBuilder.header("Content-Disposition","inline;filename=\""
	    			+ blobsCommon.getName() +"\"");
	    	result = responseBuilder.build();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}

    	return result;
    }

	private BlobsCommon getBlobsCommon(String csid) throws Exception {
    	BlobsCommon result = null;
    	
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
		PoxPayloadOut ppo = this.get(csid, ctx);
		PayloadPart blobsCommonPart = ppo.getPart(BlobClient.SERVICE_COMMON_PART_NAME);
		result = (BlobsCommon)blobsCommonPart.getBody();
		
    	return result;
    }
    
    /*
     * Publish the blob content.
     */
    @POST
    @Path("{csid}/content/publish")
    public Response publishBlobContent(
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid) {
    	Response result = null;
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
    	
    	try {
			ctx = createServiceContext();			
			BlobsCommon blobsCommon = getBlobsCommon(csid);
	    	StringBuffer mimeType = new StringBuffer();
	    	InputStream contentStream = getBlobContent(ctx, csid, null /*derivative term*/, mimeType /*will get set*/);	    	
	    	result = PublicItemUtil.publishToRepository((PublicitemsCommon)null, resourceMap, uriInfo, 
	    			getRepositoryClient(ctx), ctx, contentStream, blobsCommon.getName());
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.PUT_FAILED);
    	}
    	
    	return result;
    }
    
    @POST
    @Path("{csid}/derivatives/{derivativeTerm}/content/publish")
    public Response publishDerivativeContent(
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,    		
    		@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm) {
    	Response result = null;
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
    	
	    	try {
		    	ctx = createServiceContext();
				BlobsCommon blobsCommon = getBlobsCommon(csid);
		    	StringBuffer mimeType = new StringBuffer();
		    	InputStream contentStream = getBlobContent(ctx, csid, derivativeTerm, mimeType);
		    	result = PublicItemUtil.publishToRepository((PublicitemsCommon)null, resourceMap, uriInfo, 
		    			getRepositoryClient(ctx), ctx, contentStream, blobsCommon.getName());
	    	} catch (Exception e) {
	    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
	    	}
	    	
	    return result;
    }
    
    @GET
    @Path("{csid}/derivatives/{derivativeTerm}/content")
    public Response getDerivativeContent(
    		@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm,
    		@Context Request jaxRsRequest,
    		@Context UriInfo uriInfo) {
    	Response result = null;
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
    	
	    	try {
	    		// Setup the call to get the blob derivative
		    	ctx = createServiceContext(jaxRsRequest, uriInfo);
				BlobsCommon blobsCommon = getBlobsCommon(csid);
		    	StringBuffer mimeType = new StringBuffer();
		    	// Get the blob derivative from Nuxeo
		    	InputStream contentStream = getBlobContent(ctx, csid, derivativeTerm, mimeType);
		    	// Build a response
			    Response.ResponseBuilder responseBuilder = Response.ok(contentStream, mimeType.toString());
			    setCacheControl(ctx, responseBuilder);
		    	responseBuilder = responseBuilder.header("Content-Disposition","inline;filename=\""
		    			+ blobsCommon.getName() +"\"");
		    	result = responseBuilder.build();
	    	} catch (Exception e) {
	    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
	    	}

	    return result;
    }
    
    @GET
    @Path("{csid}/derivatives/{derivativeTerm}")
    public String getDerivative(@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm) {
    	PoxPayloadOut result = null;
    	
    	ensureCSID(csid, READ);
        try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
        	BlobInput blobInput = BlobUtil.getBlobInput(ctx);
        	blobInput.setDerivativeTerm(derivativeTerm);
            result = get(csid, ctx);
            if (result == null) {
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
                throw new CSWebApplicationException(response);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }
        
        return result.toXML();
    }
        
    @GET
    @Path("{csid}/derivatives")
    public CommonList getDerivatives(@PathParam("csid") String csid) {
    	CommonList result = null;

    	ensureCSID(csid, READ);
    	try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            result = this.getDerivativeList(ctx, csid);
            if (result == null) {
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
                throw new CSWebApplicationException(response);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }
	    
    	return result;
    }
    
}
