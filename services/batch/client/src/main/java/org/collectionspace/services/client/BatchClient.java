/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

/**
 * BatchClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class BatchClient extends AbstractCommonListPoxServiceClientImpl<BatchProxy> {
	public static final String SERVICE_NAME = "batch";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;	

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<BatchProxy> getProxyClass() {
		// TODO Auto-generated method stub
		return BatchProxy.class;
	}

	/*
	 * Proxied service calls.
	 * 
	
    public BatchCommonList readList2(){
        BatchCommonList list = readList().getEntity(BatchCommonList.class);
        return list;
    }
	 */
}
