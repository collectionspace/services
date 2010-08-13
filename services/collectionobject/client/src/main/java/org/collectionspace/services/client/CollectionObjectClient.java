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
import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.common.context.ServiceContext;
//import org.collectionspace.services.common.query.IQueryManager;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import org.slf4j.Logger;

/**
 * The Class CollectionObjectClient.
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
public class CollectionObjectClient extends AbstractServiceClientImpl {

    /** The collection object proxy. */
    private CollectionObjectProxy collectionObjectProxy;
    
	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
	 */
	@Override
    public String getServicePathComponent() {
		return "collectionobjects";
	}

    /**
     * Instantiates a new collection object client.
     */
    public CollectionObjectClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }
    
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.collectionObjectProxy;
    }

    /**
     * Sets the proxy.
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
     * Read list.
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#readList()
     * @return the client response< collectionobjects common list>
     */
    public ClientResponse<CollectionobjectsCommonList> readList() {
        return collectionObjectProxy.readList();

    }

//    @Override
//    public ClientResponse<CollectionobjectsCommonList> readList(String pageSize,
//    		String pageNumber) {
//        return collectionObjectProxy.readList(pageSize, pageNumber);
//    }
    
    /**
     * Roundtrip.
     * 
     * This is an intentionally empty method that is used for performance test 
     * to get a rough time estimate of the client to server response-request overhead.
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#roundtrip()
     * @return the client response< response>
     */
    public ClientResponse<Response> roundtrip(int ms) {
    	getLogger().debug(">>>>Roundtrip start.");
    	ClientResponse<Response> result = collectionObjectProxy.roundtrip(ms);
    	getLogger().debug("<<<<Roundtrip stop.");
    	return result;
    }
    
    /**
     * Keyword search.
     * 
     * @param keywords the keywords
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#keywordSearch()
     * @return the client response< collectionobjects common list>
     */
    public ClientResponse<CollectionobjectsCommonList> keywordSearch(String keywords) {
        return collectionObjectProxy.keywordSearch(keywords);

    }
    
    /**
     * Read.
     * 
     * @param csid the csid
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#keywordSearch()
     * @return the client response< multipart input>
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return collectionObjectProxy.read(csid);
    }

    /**
     * @param csid
     * @return response
     * @see org.collectionspace.services.client.CollectionObjectProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return collectionObjectProxy.getAuthorityRefs(csid);
    }


    /**
     * Creates the.
     * 
     * @param multipart the multipart
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#create()
     * @return the client response< response>
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return collectionObjectProxy.create(multipart);
    }

    /**
     * Update.
     * 
     * @param csid the csid
     * @param multipart the multipart
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#update()
     * @return the client response< multipart input>
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return collectionObjectProxy.update(csid, multipart);
    }

    /**
     * Delete.
     * 
     * @param csid the csid
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#delete()
     * @return the client response< response>
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return collectionObjectProxy.delete(csid);
    }
}
