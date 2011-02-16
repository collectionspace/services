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

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * MediaClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class MediaClient extends AbstractServiceClientImpl {
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
    
    /**
     *
     */
    private MediaProxy mediaProxy;

    /**
     *
     * Default constructor for MediaClient class.
     *
     */
    public MediaClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.mediaProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            mediaProxy = ProxyFactory.create(MediaProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            mediaProxy = ProxyFactory.create(MediaProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return
     * @see org.collectionspace.services.client.MediaProxy#getMedia()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return mediaProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.MediaProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return mediaProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.MediaProxy#getMedia(java.lang.String)
     */
    public ClientResponse<String> read(String csid) {
        return mediaProxy.read(csid);
    }

    /**
     * @param media
     * @return
     *
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return mediaProxy.create(xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @param media
     * @return
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return mediaProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.MediaProxy#deleteMedia(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return mediaProxy.delete(csid);
    }
}
