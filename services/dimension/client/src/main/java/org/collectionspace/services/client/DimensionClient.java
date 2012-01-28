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

import org.collectionspace.services.dimension.DimensionsCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * A DimensionClient.

 * @version $Revision:$
 */
public class DimensionClient extends AbstractPoxServiceClientImpl<DimensionsCommonList, DimensionProxy> {
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
	
	@Override
	public Class<DimensionProxy> getProxyClass() {
		return DimensionProxy.class;
	}
	
	/*
	 * Proxied service calls
	 */
	
    public ClientResponse<DimensionsCommonList> readList() {
    	DimensionProxy proxy = (DimensionProxy)getProxy();
    	return proxy.readList();
    }	
}
