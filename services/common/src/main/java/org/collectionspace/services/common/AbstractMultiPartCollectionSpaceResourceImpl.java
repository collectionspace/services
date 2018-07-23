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

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.workflow.service.nuxeo.WorkflowDocumentModelHandler;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractMultiPartCollectionSpaceResourceImpl.
 */
public abstract class AbstractMultiPartCollectionSpaceResourceImpl extends AbstractCollectionSpaceResourceImpl<PoxPayloadIn, PoxPayloadOut> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> getServiceContextFactory() {
        return MultipartServiceContextFactory.get();
    }

    abstract protected String getOrderByField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx);

	abstract protected String getPartialTermMatchField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx);

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

        MultipartServiceContext ctx = (MultipartServiceContext) serviceContext;
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
    protected WorkflowDocumentModelHandler createWorkflowDocumentHandler(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {

        WorkflowDocumentModelHandler docHandler = (WorkflowDocumentModelHandler) createDocumentHandler(ctx,
                WorkflowClient.SERVICE_COMMONPART_NAME,
                WorkflowCommon.class);

        return docHandler;
    }

    /**
     * Get the workflow lifecycle description of a resource
     * @param uriInfo
     * @return
     */
    @GET
    @Path(WorkflowClient.SERVICE_PATH)
    public Lifecycle getWorkflow(@Context Request jaxRsRequest, @Context UriInfo uriInfo) {
    	Lifecycle result;

        String documentType = "undefined";
        MultipartServiceContext ctx = null;
        try {
            ctx = (MultipartServiceContext) createServiceContext(uriInfo);
            DocumentHandler handler = ctx.getDocumentHandler();
            result = handler.getLifecycle();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, ctx.getDocumentType());
        }

        if (result == null) {
        	result = new Lifecycle();
        	result.setName("No life cycle defined for:" + documentType);
        }

        return result;
    }

    /*
     * We should change this method.  The RepositoryClient (from call to getRepositoryClient) should support a call getWorkflowTransition() instead.
     */
    @GET
    @Path("{csid}" + WorkflowClient.SERVICE_PATH)
    public byte[] getWorkflow(
    		@Context UriInfo uriInfo,
            @PathParam("csid") String csid) {
        return getWorkflowWithExistingContext(null, uriInfo, csid);
    }

    public byte[] getWorkflowWithExistingContext(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext,
            UriInfo uriInfo,
            String csid) {
        PoxPayloadOut result = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx = createServiceContext(uriInfo);
            String parentWorkspaceName = parentCtx.getRepositoryWorkspaceName();

            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME, uriInfo);
            if (existingContext != null && existingContext.getCurrentRepositorySession() != null) {
            	ctx.setCurrentRepositorySession(existingContext.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
            WorkflowDocumentModelHandler handler = createWorkflowDocumentHandler(ctx);
            ctx.setRespositoryWorkspaceName(parentWorkspaceName); //find the document in the parent's workspace
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }

        return result.getBytes();
    }

    protected TransitionDef getTransitionDef(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String transition) {
    	TransitionDef result = null;

		Lifecycle lifecycle;
		try {
			lifecycle = ctx.getDocumentHandler().getLifecycle();
			result = NuxeoUtils.getTransitionDef(lifecycle, transition);
		} catch (Exception e) {
			logger.error("Failed to get transition definition.", e);
		}

    	return result;
    }

    private PoxPayloadIn synthEmptyWorkflowInput() {
    	PoxPayloadIn result = null;

        PoxPayloadOut output = new PoxPayloadOut(WorkflowClient.SERVICE_PAYLOAD_NAME);
    	WorkflowCommon workflowCommons = new WorkflowCommon();
        PayloadOutputPart commonPart = output.addPart(WorkflowClient.SERVICE_COMMONPART_NAME, workflowCommons);
        String payloadXML = output.toXML();
        try {
			result = new PoxPayloadIn(payloadXML);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return result;
    }

    public PoxPayloadOut updateWorkflowWithTransition(ServiceContext existingContext,
    		UriInfo uriInfo,
    		String csid,
    		String transition) {

        PoxPayloadOut result = null;

        try {
        	MultipartServiceContextImpl workflowCtx = (MultipartServiceContextImpl)createServiceContext(WorkflowClient.SERVICE_NAME, uriInfo);
        	//
        	// Get properties out of the existing context if one was passed in
        	//
        	if (existingContext != null) {
        		if (existingContext.getCurrentRepositorySession() != null) {
        			workflowCtx.setCurrentRepositorySession(existingContext.getCurrentRepositorySession());
        		}
        		if (existingContext.getProperties() != null) {
        			workflowCtx.setProperties(existingContext.getProperties());
        		}
        	}

        	//
        	// Create an empty workflow_commons input part and set it into a new "workflow" sub-resource context
        	//
        	PoxPayloadIn input = new PoxPayloadIn(WorkflowClient.SERVICE_PAYLOAD_NAME, new WorkflowCommon(),
        			WorkflowClient.SERVICE_COMMONPART_NAME);
            workflowCtx.setInput(input);

            // Create a service context and document handler for the target resource.
            ServiceContext<PoxPayloadIn, PoxPayloadOut> targetCtx = createServiceContext(workflowCtx.getUriInfo());
            DocumentHandler targetDocHandler = createDocumentHandler(targetCtx);
            workflowCtx.setProperty(WorkflowClient.TARGET_DOCHANDLER, targetDocHandler); //added as a context param for the workflow document handler -it will call the parent's dochandler "prepareForWorkflowTranstion" method

            // When looking for the document, we need to use the parent's workspace name -not the "workflow" workspace name
            String targetWorkspaceName = targetCtx.getRepositoryWorkspaceName();
            workflowCtx.setRespositoryWorkspaceName(targetWorkspaceName); //find the document in the parent's workspace

        	// Get the type of transition we're being asked to make and store it as a context parameter -used by the workflow document handler
            TransitionDef transitionDef = getTransitionDef(targetCtx, transition);
            if (transitionDef == null) {
            	throw new DocumentException(String.format("The document with ID='%s' does not support the workflow transition '%s'.",
            			csid, transition));
            }
            workflowCtx.setProperty(WorkflowClient.TRANSITION_ID, transitionDef);

            WorkflowDocumentModelHandler workflowHandler = createWorkflowDocumentHandler(workflowCtx);
            getRepositoryClient(workflowCtx).update(workflowCtx, csid, workflowHandler);
            result = workflowCtx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }

        return result;

    }

    /*
     * We should consider changing this code.  The RepositoryClient (from call to getRepositoryClient) could support a call doWorkflowTransition() instead?
     */
    @PUT
    @Path("{csid}" + WorkflowClient.SERVICE_PATH + "/" + "{transition}")
    public byte[] updateWorkflowWithTransition(
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid,
    		@PathParam("transition") String transition) {
    	PoxPayloadOut result = null;

        try {
        	result = updateWorkflowWithTransition(NULL_CONTEXT, uriInfo, csid, transition);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }

        return result.getBytes();
    }

}
