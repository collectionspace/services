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

import java.util.Map;
import java.util.UUID;

import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.FileUtils;
import org.collectionspace.services.common.ResourceBase;
//import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.blob.nuxeo.BlobDocumentModelHandler; //FIXEME: A resource class should not have a dependency on a specific DocumentHandler
import org.collectionspace.services.blob.BlobsCommon;
//import org.collectionspace.services.blob.BlobsCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

//FIXME: REM - We should not have Nuxeo dependencies in our resource classes.
import org.collectionspace.services.common.imaging.nuxeo.NuxeoImageUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Path(BlobClient.SERVICE_PATH)
@Consumes({"multipart/mixed", "application/xml"})
@Produces({"multipart/mixed", "application/xml"})
public class BlobResource extends ResourceBase {

	@Override
    public String getServiceName(){
        return BlobUtil.BLOB_RESOURCE_NAME;
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

    //FIXME: Is this method used/needed?
    @Deprecated
    private CommonList getBlobList(MultivaluedMap<String, String> queryParams) {
        return (CommonList)getList(queryParams);
    }

    @Deprecated
    public CommonList getBlobList(List<String> csidList) {
        return (CommonList) getList(csidList);
    }

    @Deprecated
    protected CommonList search(MultivaluedMap<String,String> queryParams,String keywords) {
         return (CommonList) super.search(queryParams, keywords);
    }
    
    private CommonList getDerivativeList(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid) throws Exception {
    	CommonList result = null;
    	
    	BlobInput blobInput = new BlobInput();
    	blobInput.setDerivativeListRequested(true);
    	BlobUtil.setBlobInput(ctx, blobInput);

    	PoxPayloadOut response = this.get(csid, ctx);
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
    
    private InputStream getBlobContent(String csid, String derivativeTerm) throws WebApplicationException {
    	InputStream result = null;
    	
    	try {
	    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
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
	    	result = BlobUtil.getBlobInput(ctx).getContentStream();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    	
    	if (result == null) {
	        Response response = Response.status(
	                Response.Status.INTERNAL_SERVER_ERROR).entity(
	                		"Index failed. Could not get the contents for the Blob.").type("text/plain").build();
	        throw new WebApplicationException(response);
    	}
    	
    	return result;
    }
    
    /*
     * This method can replace the 'createBlob' -specifically, this JAX-RS technique can replace the call to
     * the BlobInput.createBlobFile() method.  In theory, this should reduce by 1 the number of time we need to copy
     * bits around.
     */
    @POST
    @Path("{csid}/prototype")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN)
    public Response prototype(@PathParam("csid") String csid,
    		@Context HttpServletRequest req,
    		@QueryParam("blobUri") String blobUri,
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
    			FileUtils.createTmpFile(fileStream, getServiceName() + "_");
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
    		@QueryParam("blobUri") String blobUri) {
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
    
    @GET
    @Path("{csid}/content")
    @Produces({"image/jpeg", "image/png", "image/tiff", "application/pdf"})
    public InputStream getBlobContent(
    		@PathParam("csid") String csid) {
    	InputStream result = null;
	    result = getBlobContent(csid, null /*derivative term*/);    	
    	return result;
    }

    @GET
    @Path("{csid}/derivatives/{derivativeTerm}/content")
    @Produces({"image/jpeg", "image/png", "image/tiff"})
    public InputStream getDerivativeContent(
    		@PathParam("csid") String csid,
    		@PathParam("derivativeTerm") String derivativeTerm) {
    	InputStream result = null;
	    result = getBlobContent(csid, derivativeTerm);
	    
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
                throw new WebApplicationException(response);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }
        
        return result.toXML();
    }
        
    @GET
    @Path("{csid}/derivatives")
    @Produces("application/xml")
    public CommonList getDerivatives(
    		@PathParam("csid") String csid) {
    	CommonList result = null;

    	ensureCSID(csid, READ);
        try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            result = this.getDerivativeList(ctx, csid);
            if (result == null) {
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
                throw new WebApplicationException(response);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }
	    
    	return result;
    }
    
}
