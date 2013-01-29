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
package org.collectionspace.services.common.publicitem;

import java.io.InputStream;

import org.collectionspace.authentication.spi.AuthNContext;
import org.collectionspace.services.publicitem.PublicitemsCommon;
import org.collectionspace.services.client.PublicItemClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.blob.BlobOutput;
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoBlobUtils;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.spi.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(PublicItemClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class PublicItemResource extends ResourceBase {

    final Logger logger = LoggerFactory.getLogger(PublicItemResource.class);

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision$";
    	return lastChangeRevision;
    }
    
    @Override
    public String getServiceName() {
        return PublicItemClient.SERVICE_NAME;
    }

    @Override
    public Class<PublicitemsCommon> getCommonPartClass() {
    	return PublicitemsCommon.class;
    }

	@Override
	public boolean allowAnonymousAccess(HttpRequest request,
			ResourceMethod method) {
		return true;
	}
	
    @GET
    @Path("/{csid}/{tenantId}/" + PublicItemClient.PUBLICITEMS_CONTENT_SUFFIX) // "content"
    public Response getPublishedResource(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String csid,
            @PathParam(AuthNContext.TENANT_ID_PATH_PARAM) String tenantId) {
    	Response result = null;

        try {
        	//
        	// First, extract the PublicitemsCommon instance.
        	//
			RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = (RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext(uriInfo);
			PoxPayloadOut poxPayloadOut = get(csid, ctx);
			PublicitemsCommon publicitemsCommon = (PublicitemsCommon)poxPayloadOut.getPart(PublicItemClient.SERVICE_COMMON_PART_NAME).getBody();
			//
			// Get the repository blob ID and retrieve the content as a stream
			//
			String blobContentCsid = publicitemsCommon.getContentId();
			StringBuffer outMimeType = new StringBuffer();
			BlobOutput blobOutput = NuxeoBlobUtils.getBlobOutput(ctx, getRepositoryClient(ctx), blobContentCsid, outMimeType);
			InputStream contentStream = blobOutput.getBlobInputStream();
			//
			// Return the content stream in the response
			//
	    	Response.ResponseBuilder responseBuilder = Response.ok(contentStream, outMimeType.toString());
	    	responseBuilder = responseBuilder.header("Content-Disposition","inline;filename=\""
	    			+ publicitemsCommon.getContentName() +"\"");
	    	result = responseBuilder.build();
		} catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
		}

    	return result;
    }
}



