/**	
 * AbstractMultiPartCollectionSpaceResourceImpl.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.workflow.client.WorkflowClient;
import org.collectionspace.services.common.workflow.service.nuxeo.WorkflowDocumentModelHandler;
import org.collectionspace.services.workflow.WorkflowsCommon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractMultiPartCollectionSpaceResourceImpl.
 */
public abstract class AbstractMultiPartCollectionSpaceResourceImpl extends
		AbstractCollectionSpaceResourceImpl<PoxPayloadIn, PoxPayloadOut> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> getServiceContextFactory() {
    	return MultipartServiceContextFactory.get();
    }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg)
    throws WebApplicationException {
        return bigReThrow(e, serviceMsg, "");
    }

	protected WebApplicationException bigReThrow(Exception e,
			String serviceMsg, String csid) throws WebApplicationException {
		Response response;
		if (logger.isDebugEnabled()) {
			logger.debug(getClass().getName(), e);
		}
		if (e instanceof UnauthorizedException) {
			response = Response.status(Response.Status.UNAUTHORIZED)
					.entity(serviceMsg + e.getMessage()).type("text/plain")
					.build();
			return new WebApplicationException(response);
		} else if (e instanceof DocumentNotFoundException) {
			response = Response
					.status(Response.Status.NOT_FOUND)
					.entity(serviceMsg + " on " + getClass().getName()
							+ " csid=" + csid).type("text/plain").build();
			return new WebApplicationException(response);
		} else if (e instanceof WebApplicationException) {
			//
			// subresource may have already thrown this exception
			// so just pass it on
			return (WebApplicationException)e;
		} else { // e is now instanceof Exception
			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(serviceMsg).type("text/plain").build();
			return new WebApplicationException(response);
		}
	}
    
    @Override
    public DocumentHandler createDocumentHandler(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
    	return createDocumentHandler(ctx, ctx.getCommonPartLabel(), getCommonPartClass());
    }
    
    /**
     * Creates the document handler.
     * 
     * @param serviceContext the service context
     * @param schemaName the schema name
     * @param commonClass the common class
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    public DocumentHandler createDocumentHandler(ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
    		String schemaName, 
    		Class<?> commonClass) throws Exception {
    	DocumentHandler result = null;
    	
    	MultipartServiceContext ctx = (MultipartServiceContext)serviceContext;
    	Object commonPart = null;
    	if (ctx.getInput() != null) {
        	commonPart = ctx.getInputPart(schemaName);
        }
        result = super.createDocumentHandler(ctx, commonPart);
        
        return result;
    }
    
    /**
     * Creates the document handler.
     * 
     * @param ctx the ctx
     * @param commonClass the common class
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    public DocumentHandler createDocumentHandler(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		Class<Object> commonClass) throws Exception {
    	return createDocumentHandler(ctx, ctx.getCommonPartLabel(), commonClass);
    }
    
    /**
     * Creates the contact document handler.
     * 
     * @param ctx the ctx
     * @param inAuthority the in authority
     * @param inItem the in item
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    private WorkflowDocumentModelHandler createWorkflowDocumentHandler(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
    	
    	WorkflowDocumentModelHandler docHandler = (WorkflowDocumentModelHandler)createDocumentHandler(ctx,
    			WorkflowClient.SERVICE_COMMONPART_NAME,
    			WorkflowsCommon.class);        	
    	
        return docHandler;
    }
    
    /*
     * JAX-RS Annotated methods
     */
        
    @GET
    @Path("{csid}/workflow")
    public byte[] getWorkflow(
            @PathParam("csid") String csid) {
        PoxPayloadOut result = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx = createServiceContext();
            String parentWorkspaceName = parentCtx.getRepositoryWorkspaceName();
        	
        	MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME);
        	WorkflowDocumentModelHandler handler = createWorkflowDocumentHandler(ctx);
        	ctx.setRespositoryWorkspaceName(parentWorkspaceName); //find the document in the parent's workspace
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }
                
        return result.getBytes();
    }
    
}
