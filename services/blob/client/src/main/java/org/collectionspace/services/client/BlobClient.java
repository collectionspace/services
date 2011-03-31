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

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

/**
 * BlobClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class BlobClient extends AbstractPoxServiceClientImpl<AbstractCommonList, BlobProxy> {
	public static final String SERVICE_NAME = "blobs";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	//HTTP query param string for specifying a URI source to blob bits.
	public static final String BLOB_URI_PARAM = "blobUri";
	

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }
    
	@Override
	public Class<BlobProxy> getProxyClass() {
		return BlobProxy.class;
	}

	/**
	 * Blob proxied service calls
	 */
	
    /**
     * @return
     * @see org.collectionspace.services.client.BlobProxy#getBlob()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return getProxy().readList();
    }
    
    public ClientResponse<Response> createBlobFromFormData(MultipartFormDataOutput formDataOutput) {
        return getProxy().createBlobFromFormData(formDataOutput);
    }
    
    public ClientResponse<Response> createBlobFromURI(String blobUri) {
        return getProxy().createBlobFromURI("".getBytes(), blobUri);
    }
}
