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

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;


/**
 * BlobClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class BlobClient extends AbstractServiceClientImpl {
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
    /**
     *
     */
    private BlobProxy blobProxy;

    /**
     *
     * Default constructor for BlobClient class.
     *
     */
    public BlobClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.blobProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    @Override
	public void setProxy() {
        if (useAuth()) {
            blobProxy = ProxyFactory.create(BlobProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            blobProxy = ProxyFactory.create(BlobProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static BlobClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.BlobProxy#getBlob()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return blobProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.BlobProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return blobProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.BlobProxy#getBlob(java.lang.String)
     */
    public ClientResponse<String> read(String csid) {
        return blobProxy.read(csid);
    }

    /**
     * @param blob
     * @return
     *
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return blobProxy.create(xmlPayload.getBytes());
    }    
    
    public ClientResponse<Response> createBlobFromFormData(MultipartFormDataOutput formDataOutput) {
        return blobProxy.createBlobFromFormData(formDataOutput);
    }
    
    public ClientResponse<Response> createBlobFromURI(String blobUri) {
        return blobProxy.createBlobFromURI("".getBytes(), blobUri);
    }
    
    /**
     * @param csid
     * @param blob
     * @return
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return blobProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.BlobProxy#deleteBlob(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return blobProxy.delete(csid);
    }
}
