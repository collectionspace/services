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

import org.apache.http.HttpStatus;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.description.ServiceDescription;

/**
 * A AccountClient.

 * @version $Revision:$
 */
public class AccountClient extends AbstractServiceClientImpl<AccountsCommonList, AccountsCommon,
		AccountsCommon, AccountProxy> {
    public static final String SERVICE_NAME = "accounts";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public final static String IMMUTABLE = "immutable";

	public AccountClient() throws Exception {
		super();
	}

	public AccountClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	/* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_NAME;
    }
    
    public String getTenantId() {
        return getProperty(AccountClient.TENANT_NAME_PROPERTY);
    }

	@Override
	public Class<AccountProxy> getProxyClass() {
		return AccountProxy.class;
	}    

	/*
	 * CRUD+L Methods
	 */
	
    /**
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#readList()
     */
	@Override
    public Response readList() {
        return getProxy().readList();
    }

    public Response readSearchList(String screenName, String uid, String email) {
        return getProxy().readSearchList(screenName, uid, email);
    }

    /**
     * @param csid
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#getAccount(java.lang.String)
     */
    @Override
    public Response read(String csid) {
        return getProxy().read(csid);
    }

    /**
     * @param multipart 
     * @param account
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#create(org.collectionspace.services.account.AccountsCommon)
     */
    @Override
    public Response create(AccountsCommon multipart) {
        return getProxy().create(multipart);
    }

    /**
     * @param csid
     * @param multipart 
     * @param account
     * @return response
     * @see org.collectionspace.hello.client.AccountProxy#updateAccount(java.lang.Long, org.collectionspace.services.account.AccountsCommon)
     */
    @Override
    public Response update(String csid, AccountsCommon multipart) {
        return getProxy().update(csid, multipart);
    }
    
    /**
     * 
     */
	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}
}
