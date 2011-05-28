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
package org.collectionspace.services.batch;

import java.util.List;

import org.collectionspace.services.BatchJAXBSchema;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.Invocable.InvocationError;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.collectionspace.services.common.ResourceMapHolder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(BatchClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class BatchResource extends ResourceBase {
	
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();

    @Override
    public String getServiceName(){
        return BatchClient.SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    //public Class<BatchCommon> getCommonPartClass() {
    public Class getCommonPartClass() {
    	try {
            return Class.forName("org.collectionspace.services.batch.BatchCommon");//.class;
        } catch (ClassNotFoundException e){
            return null;
        }
    }
    
    @POST
    @Path("{csid}")
    public InvocationResults invokeBatchJob(
    		@Context ResourceMap resourceMap, 
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentWrapper<DocumentModel> wrapper = 
            	getRepositoryClient(ctx).getDoc(ctx, csid);
    		DocumentModel docModel = wrapper.getWrappedObject();
    		String invocationMode = invContext.getMode();
    		String modeProperty = null;
    		if(BatchInvocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
    			modeProperty = BatchJAXBSchema.BATCH_SUPPORTS_SINGLE_DOC;
    		} else if(BatchInvocable.INVOCATION_MODE_LIST.equalsIgnoreCase(invocationMode)) {
    			modeProperty = BatchJAXBSchema.BATCH_SUPPORTS_DOC_LIST;
    		} else if(BatchInvocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
    			modeProperty = BatchJAXBSchema.BATCH_SUPPORTS_GROUP;
    		} else {
    			throw new BadRequestException("BatchResource: unknown Invocation Mode: "
            			+invocationMode);
    		}
    		Boolean supports = (Boolean)docModel.getPropertyValue(modeProperty);
    		if(!supports) {
    			throw new BadRequestException("BatchResource: This Batch Job does not support Invocation Mode: "
            			+invocationMode);
    		}
    		String className = 
    			(String)docModel.getPropertyValue(BatchJAXBSchema.BATCH_CLASS_NAME);
    		className = className.trim();
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            Class<?> c = tccl.loadClass(className);
        	// enable validation assertions
        	tccl.setClassAssertionStatus(className, true);
            if(!BatchInvocable.class.isAssignableFrom(c)) {
            	throw new RuntimeException("BatchResource: Class: "
            			+className+" does not implement BatchInvocable!");
            } else {
            	BatchInvocable batchInstance = (BatchInvocable)c.newInstance();
            	List<String> modes = batchInstance.getSupportedInvocationModes();
            	if(!modes.contains(invocationMode)) {
            		throw new BadRequestException(
            				"BatchResource: Invoked with unsupported context mode: "
            				+invocationMode);
            	}
        		String forDocType = 
        			(String)docModel.getPropertyValue(BatchJAXBSchema.BATCH_FOR_DOC_TYPE);
            	if(!forDocType.equalsIgnoreCase(invContext.getDocType())) {
            		throw new BadRequestException(
            				"BatchResource: Invoked with unsupported document type: "
            				+invContext.getDocType());
            	}
            	batchInstance.setInvocationContext(invContext);
            	//ResourceMapHolder csapp = (ResourceMapHolder)app;
            	if(resourceMap!=null) {
            		batchInstance.setResourceMap(resourceMap);
            	} else {
            		resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
            		if(resourceMap!=null) {
            			batchInstance.setResourceMap(resourceMap);
	            	} else {
	            		logger.warn("BatchResource.invoke did not get a resourceMapHolder in Context!");
	            	}
            	}
            	batchInstance.run();
            	int status = batchInstance.getCompletionStatus();
            	if(status == Invocable.STATUS_ERROR) {
            		InvocationError error = batchInstance.getErrorInfo();
            		if(error.getResponseCode() == BAD_REQUEST_STATUS) {
            			throw new BadRequestException(
            				"BatchResouce: batchProcess encountered error: "
            				+batchInstance.getErrorInfo());
            		} else {
            			throw new RuntimeException(
                				"BatchResouce: batchProcess encountered error: "
                				+batchInstance.getErrorInfo());

            		}
            	}
            	InvocationResults results = batchInstance.getResults();
            	return results;
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }
    }
}
