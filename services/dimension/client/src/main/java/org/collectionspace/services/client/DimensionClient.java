/**	
 * DimensionClient.java
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
 * Copyright (c) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.dimension.DimensionsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A DimensionClient.

 * @version $Revision:$
 */
public class DimensionClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "dimensions";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	@Override
	public String getServicePathComponent() {
		return SERVICE_PATH_COMPONENT;
	}
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	/**
     *
     */
//    private static final DimensionClient instance = new DimensionClient();
    
    /**
     *
     */
    private DimensionProxy dimensionProxy;

    /**
     *
     * Default constructor for DimensionClient class.
     *
     */
    public DimensionClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.dimensionProxy;
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            dimensionProxy = ProxyFactory.create(DimensionProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            dimensionProxy = ProxyFactory.create(DimensionProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static DimensionClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#getDimension()
     */
    public ClientResponse<DimensionsCommonList> readList() {
        return dimensionProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#getDimension(java.lang.String)
     */

    public ClientResponse<String> read(String csid) {
        return dimensionProxy.read(csid);
    }

    /**
     * @param dimension
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#createDimension(org.collectionspace.services.Dimension)
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return dimensionProxy.create(xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @param dimension
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#updateDimension(java.lang.Long, org.collectionspace.services.Dimension)
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return dimensionProxy.update(csid, xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @return response
     * @see org.collectionspace.services.client.DimensionProxy#deleteDimension(java.lang.Long)
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return dimensionProxy.delete(csid);
    }
}
