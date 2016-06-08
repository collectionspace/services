/**	
 * TenantClient.java
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
import org.collectionspace.services.account.Tenant;
import org.collectionspace.services.account.TenantsList;
import org.collectionspace.services.description.ServiceDescription;

/**
 * A TenantClient.

 * @version $Revision:$
 */
public class TenantClient extends AbstractServiceClientImpl<TenantsList, Tenant,
		Tenant, TenantProxy> {
	
    public TenantClient() throws Exception {
		super();
	}

	public static final String SERVICE_NAME = "tenants";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;

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
    
	@Override
	public Class<TenantProxy> getProxyClass() {
		return TenantProxy.class;
	}    

	/*
	 * CRUD+L Methods
	 */
	
    /**
     * @return response
     * @see org.collectionspace.hello.client.TenantProxy#readList()
     */
	@Override
    public Response readList() {
        return getProxy().readList();
    }

    public Response readSearchList(String name, String disabled) {
        return getProxy().readSearchList(name, disabled);
    }

    /**
     * @param csid
     * @return response
     * @see org.collectionspace.hello.client.TenantProxy#getTenant(java.lang.String)
     */
    @Override
    public Response read(String id) {
        return getProxy().read(id);
    }

    /**
     * @param multipart 
     * @param tenant
     * @return response
     * @see org.collectionspace.hello.client.TenantProxy#create(org.collectionspace.services.account.Tenant)
     */
    @Override
    public Response create(Tenant multipart) {
        return getProxy().create(multipart);
    }

    /**
     * @param csid
     * @param multipart 
     * @param tenant
     * @return response
     * @see org.collectionspace.hello.client.TenantProxy#updateTenant(java.lang.Long, org.collectionspace.services.account.Tenant)
     */
    @Override
    public Response update(String id, Tenant multipart) {
        return getProxy().update(id, multipart);
    }
    
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
