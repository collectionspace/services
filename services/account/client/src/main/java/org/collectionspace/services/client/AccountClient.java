/**	
 * AccountClient.java
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


import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A AccountClient.

 * @version $Revision:$
 */
public class AccountClient extends AbstractServiceClientImpl {

    /**
     *
     */
    private AccountProxy accountProxy;

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return "accounts";
    }

    /**
     *
     * Default constructor for AccountClient class.
     *
     */
    public AccountClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
        return this.accountProxy;
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            accountProxy = ProxyFactory.create(AccountProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            accountProxy = ProxyFactory.create(AccountProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#readList()
     */
    public ClientResponse<AccountsCommonList> readList() {
        return accountProxy.readList();

    }

    public ClientResponse<AccountsCommonList> readSearchList(String screenName, String uid, String email) {
        return accountProxy.readSearchList(screenName, uid, email);

    }

    /**
     * @param csid
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#getAccount(java.lang.String)
     */
    public ClientResponse<AccountsCommon> read(String csid) {
        return accountProxy.read(csid);
    }

    /**
     * @param multipart 
     * @param account
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#create(org.collectionspace.services.account.AccountsCommon)
     */
    public ClientResponse<Response> create(AccountsCommon multipart) {
        return accountProxy.create(multipart);
    }

    /**
     * @param csid
     * @param multipart 
     * @param account
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#updateAccount(java.lang.Long, org.collectionspace.services.account.AccountsCommon)
     */
    public ClientResponse<AccountsCommon> update(String csid, AccountsCommon multipart) {
        return accountProxy.update(csid, multipart);
    }

    /**
     * @param csid
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#deleteAccount(java.lang.Long)
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return accountProxy.delete(csid);
    }
    
    
    public String getTenantId() {
        return getProperty(AccountClient.TENANT_PROPERTY);
    }
}
