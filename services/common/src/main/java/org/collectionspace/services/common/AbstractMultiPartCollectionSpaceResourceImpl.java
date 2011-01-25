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

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
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

    @Override
    public DocumentHandler createDocumentHandler(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
    	return createDocumentHandler(ctx, ctx.getCommonPartLabel(),getCommonPartClass());
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
    
}
