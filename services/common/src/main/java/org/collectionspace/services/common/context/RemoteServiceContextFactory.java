/**	
 * RemoteServiceContextFactory.java
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
package org.collectionspace.services.common.context;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.ResourceMap;


/**
 * A factory for creating RemoteServiceContext objects.
 */
public class RemoteServiceContextFactory<IT, OT>
	implements ServiceContextFactory<IT, OT>{
	
	// create a Factory singleton
	/** The Constant self. */
	final private static RemoteServiceContextFactory self = new RemoteServiceContextFactory();
	
	/**
	 * Instantiates a new remote service context factory.
	 */
	private RemoteServiceContextFactory() {} // private constructor
    
    /**
     * Gets the.
     * 
     * @return the remote service context factory
     */
    public static RemoteServiceContextFactory get() {
        return self;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String)
     */
    @Override
    public ServiceContext<IT, OT> createServiceContext(String serviceName) throws Exception {
    	RemoteServiceContext<IT, OT> ctx = new RemoteServiceContextImpl<IT, OT>(serviceName);
    	return ctx;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String, java.lang.Object)
     */
    @Override
    public ServiceContext<IT, OT> createServiceContext(String serviceName,
    		IT theInput) throws Exception {
    	RemoteServiceContext<IT, OT> ctx = new RemoteServiceContextImpl<IT, OT>(serviceName, theInput);
        return ctx;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String, java.lang.Object, javax.ws.rs.core.MultivaluedMap)
     */
    @Override
    public ServiceContext<IT, OT> createServiceContext(
    		String serviceName,
    		IT theInput,
    		ResourceMap resourceMap,
    		UriInfo uriInfo) throws Exception {
    	ServiceContext<IT, OT> ctx = new RemoteServiceContextImpl<IT, OT>(serviceName,
    			theInput,
    			resourceMap,
    			uriInfo);
    	
        return ctx;
    }

    @Override
    public ServiceContext<IT, OT> createServiceContext(String serviceName,
    		IT input,
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		String documentType,
    		String entityName) throws Exception {
    	ServiceContext<IT, OT> ctx = createServiceContext(
    			serviceName,
    			input,
    			resourceMap,
    			uriInfo);
        ctx.setDocumentType(documentType); //persistence unit
        ctx.setProperty(ServiceContextProperties.ENTITY_NAME, entityName);
        return ctx;
    }
    
}
