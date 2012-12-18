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
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.workflow.service.nuxeo.WorkflowDocumentModelHandler;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
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

    /*
     * JAX-RS Annotated methods
     */
    @GET
    @Path(WorkflowClient.SERVICE_PATH)
    public Lifecycle getWorkflow(@Context UriInfo uriInfo) {
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
     * JAX-RS Annotated methods
     */
    
    /*
     * We should change this method.  The RepositoryClient (from call to getRepositoryClient) should support a call getWorkflowTransition() instead.
     */    
    @GET
    @Path("{csid}" + WorkflowClient.SERVICE_PATH)
    public byte[] getWorkflow(
    		@Context UriInfo uriInfo,
            @PathParam("csid") String csid) {
        PoxPayloadOut result = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx = createServiceContext(uriInfo);
            String parentWorkspaceName = parentCtx.getRepositoryWorkspaceName();

            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME, uriInfo);
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
    	
    	try {
			Lifecycle lifecycle = ctx.getDocumentHandler().getLifecycle();
			List<TransitionDef> transitionDefList = lifecycle.getTransitionDefList().getTransitionDef();
			Iterator<TransitionDef> iter = transitionDefList.iterator();
			boolean found = false;
			while (iter.hasNext() && found == false) {
				TransitionDef transitionDef = iter.next();
				if (transitionDef.getName().equalsIgnoreCase(transition)) {
					result = transitionDef;
					found = true;
				}
			}
		} catch (Exception e) {
			logger.error("Exception trying to retreive life cycle information for: " + ctx.getDocumentType());
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
    
    /*
     * We should change this code.  The RepositoryClient (from call to getRepositoryClient) should support a call doWorkflowTransition() instead.
     */
    //FIXME: This method is almost identical to the method org.collectionspace.services.common.vocabulary.updateWorkflowWithTransition() so
    // they should be consolidated -be DRY (don't repeat yourself).

    @PUT
    @Path("{csid}" + WorkflowClient.SERVICE_PATH + "/" + "{transition}")
    public byte[] updateWorkflowWithTransition(
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid,
    		@PathParam("transition") String transition) {
        PoxPayloadOut result = null;
                
        try {
        	//
        	// Create an empty workflow_commons input part and set it into a new "workflow" sub-resource context
        	PoxPayloadIn input = new PoxPayloadIn(WorkflowClient.SERVICE_PAYLOAD_NAME, new WorkflowCommon(), 
        			WorkflowClient.SERVICE_COMMONPART_NAME);
            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME, input, uriInfo);
        	
            // Create a service context and document handler for the parent resource.
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx = createServiceContext(uriInfo);
            DocumentHandler parentDocHandler = this.createDocumentHandler(parentCtx);      
            ctx.setProperty(WorkflowClient.PARENT_DOCHANDLER, parentDocHandler); //added as a context param for the workflow document handler -it will call the parent's dochandler "prepareForWorkflowTranstion" method

            // When looking for the document, we need to use the parent's workspace name -not the "workflow" workspace name
            String parentWorkspaceName = parentCtx.getRepositoryWorkspaceName();
            ctx.setRespositoryWorkspaceName(parentWorkspaceName); //find the document in the parent's workspace
            
        	// Get the type of transition we're being asked to make and store it as a context parameter -used by the workflow document handler
            TransitionDef transitionDef = getTransitionDef(parentCtx, transition);
            ctx.setProperty(WorkflowClient.TRANSITION_ID, transitionDef);

            WorkflowDocumentModelHandler handler = createWorkflowDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }
        return result.getBytes();
    }
    
}
