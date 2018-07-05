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
package org.collectionspace.services.workflow;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.jaxb.AbstractCommonList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(WorkflowClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class WorkflowResource extends NuxeoBasedResource {

    @Override
    public String getServiceName(){
        return WorkflowClient.SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    //public Class<ObjectexitCommon> getCommonPartClass() {
    public Class<?> getCommonPartClass() {
    	try {
            return Class.forName("org.collectionspace.services.objectexit.WorkflowCommon");//.class;
        } catch (ClassNotFoundException e){
            return null;
        }
    }

	/*
	 * HTTP Methods
	 */

	@Override
	@POST
	public Response create(@Context ResourceMap resourceMap, @Context UriInfo ui, String xmlPayload) {
		Response response = Response.status(Response.Status.BAD_REQUEST)
				.entity(ServiceMessages.POST_UNSUPPORTED).type("text/plain").build();
		return response;
	}

    @Override
	@DELETE
    @Path("{csid}")
    public Response delete(@PathParam("csid") String csid) {
		Response response = Response.status(Response.Status.BAD_REQUEST)
				.entity(ServiceMessages.DELETE_UNSUPPORTED).type("text/plain")
				.build();
		return response;
    }


	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.ResourceBase#getList(javax.ws.rs.core.UriInfo, java.lang.String)
	 *
	 * The workflow sub-resource does not support a getList operation.
	 */
	@Override
	@GET
	public AbstractCommonList getList(@Context UriInfo ui) {
		Response response = Response.status(Response.Status.BAD_REQUEST)
				.entity(ServiceMessages.GET_LIST_UNSUPPORTED).type("text/plain")
				.build();
		throw new CSWebApplicationException(response);
	}

	@Override
	public AbstractCommonList getList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, UriInfo uriInfo) {
		throw new UnsupportedOperationException();
	}
}
