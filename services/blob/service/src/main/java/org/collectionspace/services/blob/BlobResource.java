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

import org.collectionspace.services.common.FileUtils;
import org.collectionspace.services.common.ResourceBase;
//import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.blob.BlobsCommonList;

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

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Path("/blobs")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class BlobResource extends ResourceBase {

    @Override
    public String getServiceName(){
        return "blobs";
    };


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
    private BlobsCommonList getBlobList(MultivaluedMap<String, String> queryParams) {
        return (BlobsCommonList)getList(queryParams);
    }

    @Deprecated
    public BlobsCommonList getBlobList(List<String> csidList) {
        return (BlobsCommonList) getList(csidList);
    }

    @Deprecated
    protected BlobsCommonList search(MultivaluedMap<String,String> queryParams,String keywords) {
         return (BlobsCommonList) super.search(queryParams, keywords);
    }
    
    private BlobsCommonList getDerivativeList(String csid) throws Exception {
    	BlobsCommonList result = null;
    	
    	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
    	ctx.setProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY, Boolean.TRUE);
    	MultipartOutput response = this.get(csid, ctx);
    	if (logger.isDebugEnabled() == true) {
    		logger.debug(response.toString());
    	}
    	result = (BlobsCommonList)ctx.getProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY);
    	
    	return result;
    }
    
    private InputStream getBlobContent(String csid, String derivativeTerm) throws WebApplicationException {
    	InputStream result = null;
    	
    	try {
	    	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
	    	ctx.setProperty(BlobInput.BLOB_DERIVATIVE_TERM_KEY, derivativeTerm);
    		ctx.setProperty(BlobInput.BLOB_CONTENT_KEY, Boolean.TRUE);
	    	MultipartOutput response = this.get(csid, ctx);
	    	if (logger.isDebugEnabled() == true) {
	    		logger.debug(response.toString());
	    	}
	    	result = (InputStream)ctx.getProperty(BlobInput.BLOB_CONTENT_KEY);
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
        
    private BlobInput setBlobInput(ServiceContext<MultipartInput, MultipartOutput> ctx,
    		HttpServletRequest req,
    		String blobUri) {
    	File tmpFile = FileUtils.createTmpFile(req);
    	BlobInput blobInput = new BlobInput(tmpFile, blobUri);
    	ctx.setProperty(BlobInput.class.getName(), blobInput);
    	return blobInput;
    }
    
    @POST
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public Response createBlob(@Context HttpServletRequest req,
    		@QueryParam("blobUri") String blobUri) {
    	Response response = null;    	
    	try {
	    	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
	    	setBlobInput(ctx, req, blobUri);
	    	response = this.create(null, ctx);
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
    			
		return response;
    }    
    
    @GET
    @Path("{csid}/content")
    @Produces({"image/jpeg", "image/png", "image/tiff"})
    public InputStream getBlobContent(
    		@PathParam("csid") String csid) {
    	InputStream result = null;
	    result = getBlobContent(csid, null /*derivative term*/);    	
    	return result;
    }

    @GET
    @Path("{csid}/derivatives/{derivative_term}/content")
    @Produces({"image/jpeg", "image/png", "image/tiff"})
    public InputStream getDerivativeContent(
    		@PathParam("csid") String csid,
    		@PathParam("derivative_term") String derivative_term) {
    	InputStream result = null;
	    result = getBlobContent(csid, derivative_term);
	    
    	return result;
    }
    
    @GET
    @Path("{csid}/derivatives/{derivative_term}")
    public MultipartOutput getDerivative(@PathParam("csid") String csid,
    		@PathParam("derivative_term") String derivative_term) {
    	MultipartOutput result = null;
    	
    	ensureCSID(csid, READ);
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
        	ctx.setProperty(BlobInput.BLOB_DERIVATIVE_TERM_KEY, derivative_term);
            result = get(csid, ctx);
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
        
    @GET
    @Path("{csid}/derivatives")
    @Produces("application/xml")
    public BlobsCommonList getDerivatives(
    		@PathParam("csid") String csid) {
    	BlobsCommonList result = null;

    	ensureCSID(csid, READ);
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
        	ctx.setProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY, true);
            result = this.getDerivativeList(csid);
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
