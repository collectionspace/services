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
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.client.ClientResponse;

/**
 * MediaClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class MediaClient extends AbstractCommonListPoxServiceClientImpl<MediaProxy> {
	public static final String SERVICE_NAME = "media";
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
	public Class<MediaProxy> getProxyClass() {
		return MediaProxy.class;
	}
	
	/*
	 * Proxied service calls
	 */

    /**
     * @param media
     * @return
     * 
     * Creates a new blob resource from the form data and associates it with an existing media resource
     *
     */
    public ClientResponse<Response> createBlobFromFormData(String csid, // this is the Media resource CSID
    		MultipartFormDataOutput formDataOutput) {
        return getProxy().createBlobFromFormData(csid, formDataOutput);
    }    

    /**
     * @param media
     * @return
     * 
     * Creates a new blob
     *
     */
    public ClientResponse<Response> createBlobFromUri(String csid, String blobUri) {
        return getProxy().createBlobFromUri(csid, blobUri, blobUri); //send the URI as both a query param and as content
    }
    
    /*
     * Create both a new media record
     */
    public ClientResponse<Response> createMediaAndBlobWithUri(PoxPayloadOut xmlPayload, String blobUri, boolean purgeOriginal) {
    	return getProxy().createMediaAndBlobWithUri(xmlPayload.getBytes(), blobUri, purgeOriginal);
    }
        
    /**
     * @param csid
     * @param media
     * @return
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload, String URI) {
        return getProxy().update(csid, xmlPayload.getBytes());

    }
}
