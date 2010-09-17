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

import org.collectionspace.services.contact.ContactsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * ContactClient.java
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
 
public class ContactClient extends AbstractServiceClientImpl {

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
	 */
	public String getServicePathComponent() {
		return "contacts";
	}

	/**
     *
     */
//    private static final ContactClient instance = new ContactClient();
    
    /**
     *
     */
    private ContactProxy contactProxy;

    /**
     *
     * Default constructor for ContactClient class.
     *
     */
    public ContactClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.contactProxy;
    }
    
    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            contactProxy = ProxyFactory.create(ContactProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            contactProxy = ProxyFactory.create(ContactProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static ContactClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.Contact#getContact()
     */
    public ClientResponse<ContactsCommonList> readList() {
        return contactProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.Contact#getContact(java.lang.String)
     */

    public ClientResponse<MultipartInput> read(String csid) {
        return contactProxy.read(csid);
    }

    /**
     * @param contact
     * @return
     * @see org.collectionspace.services.client.Contact#createContact(org.collectionspace.services.Contact)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return contactProxy.create(multipart);
    }

    /**
     * @param csid
     * @param contact
     * @return
     * @see org.collectionspace.services.client.Contact#updateContact(java.lang.Long, org.collectionspace.services.Contact)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return contactProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.Contact#deleteContact(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return contactProxy.delete(csid);
    }
}
