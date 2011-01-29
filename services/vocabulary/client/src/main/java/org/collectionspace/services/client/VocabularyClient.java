/**	
 * VocabularyClient.java
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
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.vocabulary.VocabulariesCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList;
import org.collectionspace.services.client.VocabularyProxy;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class VocabularyClient.
 */
public class VocabularyClient extends AbstractServiceClientImpl {

	public static final String SERVICE_PATH_COMPONENT = "vocabularies"; //FIXME: REM - The JAX-RS proxy, client, and resource classes should ref this value
	public static final String SERVICE_PATH_ITEMS_COMPONENT = "vocabularies"; //FIXME: REM - The JAX-RS proxy, client, and resource classes should ref this value
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    /**
     * Gets the item common part name.
     *
     * @return the item common part name
     */
    public String getItemCommonPartName() {
        return getCommonPartName("vocabularyitems");
    }
    
    /** The Constant instance. */ //FIXME: This is wrong.  There should not be a static instance of the client.
//    private static final VocabularyClient instance = new VocabularyClient();
    
    /** The vocabulary proxy. */
    private VocabularyProxy vocabularyProxy;

    /**
     * Instantiates a new vocabulary client.
     */
    public VocabularyClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.vocabularyProxy;
    }    

    /**
     * Sets the proxy.
     */
    public void setProxy() {
        if (useAuth()) {
            vocabularyProxy = ProxyFactory.create(VocabularyProxy.class, getBaseURL(), 
            		new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            vocabularyProxy = ProxyFactory.create(VocabularyProxy.class, getBaseURL());
        }
    }

    /**
     * Gets the single instance of VocabularyClient.
     *
     * @return single instance of VocabularyClient //FIXME: This is wrong.  There should not be a static instance of the client.
     */
//    public static VocabularyClient getInstance() {
//        return instance;
//    }

    /**
     * Read list.
     *
     * @return the client response
     */
    public ClientResponse<VocabulariesCommonList> readList() {
        return vocabularyProxy.readList();
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> read(String csid) {
        return vocabularyProxy.read(csid);
    }

    /**
     * Read by name.
     *
     * @param name the name
     * @return the client response
     */
    public ClientResponse<String> readByName(String name) {
        return vocabularyProxy.readByName(name);
    }

    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> create(PoxPayloadOut poxPayloadout) {
    	String xmlPayload = poxPayloadout.toXML();
        return vocabularyProxy.create(xmlPayload);
    }

    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut poxPayloadout) {
    	String xmlPayload = poxPayloadout.toXML();
        return vocabularyProxy.update(csid, xmlPayload);

    }

    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> delete(String csid) {
        return vocabularyProxy.delete(csid);
    }

    /**
     * Read item list, filtering by partial term match, or keywords. Only one of
     * partialTerm or keywords should be specified. If both are specified, keywords
     * will be ignored.
     *
     * @param inAuthority the parent authority
     * @param partialTerm A partial term on which to match,
     *     which will filter list results to return only matched resources.
     * @param keywords A set of keywords on which to match,
     *     which will filter list results to return only matched resources.
     * @return the client response
     */
    public ClientResponse<VocabularyitemsCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords) {
        return vocabularyProxy.readItemList(inAuthority, partialTerm, keywords);
    }

    /**
     * Read item list for named vocabulary, filtering by partial term match, or keywords. Only one of
     * partialTerm or keywords should be specified. If both are specified, keywords
     * will be ignored.
     *
     * @param specifier the specifier
     * @param partialTerm A partial term on which to match,
     *     which will filter list results to return only matched resources.
     * @param keywords A set of keywords on which to match,
     *     which will filter list results to return only matched resources.
     * @return the client response
     */
    public ClientResponse<VocabularyitemsCommonList> 
    		readItemListForNamedVocabulary(String specifier, String partialTerm, String keywords) {
        return vocabularyProxy.readItemListForNamedVocabulary(specifier, partialTerm, keywords);
    }

    /**
     * Read item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItem(String vcsid, String csid) {
        return vocabularyProxy.readItem(vcsid, csid);
    }

    /**
     * Creates the item.
     *
     * @param vcsid the vcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createItem(String vcsid, PoxPayloadOut poxPayloadOut) {
    	String xmlPayload = poxPayloadOut.toXML();
        return vocabularyProxy.createItem(vcsid, xmlPayload);
    }

    /**
     * Update item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateItem(String vcsid, String csid, PoxPayloadOut poxPayloadOut) {
    	String xmlPayload = poxPayloadOut.toXML();
        return vocabularyProxy.updateItem(vcsid, csid, xmlPayload);
    }

    /**
     * Delete item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return vocabularyProxy.deleteItem(vcsid, csid);
    }
}
