/**	
 * CollectionObjectClient.java
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
 * Copyright (C) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;


import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.common.context.ServiceContext;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A CollectionObjectClient.

 * @version $Revision:$
 */
public class CollectionObjectClient extends BaseServiceClient {

    /**
     *
     */
    private CollectionObjectProxy collectionObjectProxy;
    
	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
	 */
	public String getServicePathComponent() {
		return "collectionobjects";
	}

    /**
     *
     * Default constructor for CollectionObjectClient class.
     *
     */
    public CollectionObjectClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if(useAuth()){
            collectionObjectProxy = ProxyFactory.create(CollectionObjectProxy.class,
                    getBaseURL(), getHttpClient());
        }else{
            collectionObjectProxy = ProxyFactory.create(CollectionObjectProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#readList()
     */
    public ClientResponse<CollectionobjectsCommonList> readList() {
        return collectionObjectProxy.readList();

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#getCollectionObject(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return collectionObjectProxy.read(csid);
    }

    /**
     * @param collectionobject
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#create(org.collectionspace.services.collectionobject.CollectionobjectsCommon)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return collectionObjectProxy.create(multipart);
    }

    /**
     * @param csid
     * @param collectionobject
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#updateCollectionObject(java.lang.Long, org.collectionspace.services.collectionobject.CollectionobjectsCommon)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return collectionObjectProxy.update(csid, multipart);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#deleteCollectionObject(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return collectionObjectProxy.delete(csid);
    }
}
