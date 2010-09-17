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
//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.objectexit.ObjectexitCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * ObjectExitClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class ObjectExitClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "objectexit"; //Laramie20100824 was objectexits, but label was a mismatch.
    }
    /**
     *
     */
//    private static final ObjectExitClient instance = new ObjectExitClient();
    /**
     *
     */
    private ObjectExitProxy objectexitProxy;

    /**
     *
     * Default constructor for ObjectExitClient class.
     *
     */
    public ObjectExitClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.objectexitProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            objectexitProxy = ProxyFactory.create(ObjectExitProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            objectexitProxy = ProxyFactory.create(ObjectExitProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static ObjectExitClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#getObjectExit()
     */
    public ClientResponse<ObjectexitCommonList> readList() {
        return objectexitProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return objectexitProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#getObjectExit(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return objectexitProxy.read(csid);
    }

    /**
     * @param objectexit
     * @return
     *
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return objectexitProxy.create(multipart);
    }

    /**
     * @param csid
     * @param objectexit
     * @return
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return objectexitProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#deleteObjectExit(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return objectexitProxy.delete(csid);
    }
}
