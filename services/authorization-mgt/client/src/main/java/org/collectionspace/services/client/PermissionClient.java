/**	
 * PermissionClient.java
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
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionsList;

/**
 * A PermissionClient.

 * @version $Revision:$
 */
public class PermissionClient extends AbstractServiceClientImpl<PermissionsList, Permission, Permission, PermissionProxy> {
	public static final String SERVICE_NAME = "authorization/permissions";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
	public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public Class<PermissionProxy> getProxyClass() {
		return PermissionProxy.class;
	}
	
	/*
	 * CRUD+L Methods
	 */
	
    /**
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#readList()
     */
    public ClientResponse<PermissionsList> readList() {
        return getProxy().readList();

    }

    public ClientResponse<PermissionsList> readSearchList(String resourceName) {
        return getProxy().readSearchList(resourceName);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#getAccount(java.lang.String)
     */
    public ClientResponse<Permission> read(String csid) {
        return getProxy().read(csid);
    }

    /**
     * @param permission
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#create(org.collectionspace.services.permission.Permission)
     */
    public ClientResponse<Response> create(Permission permission) {
        return getProxy().create(permission);
    }

    /**
     * @param csid
     * @param permission
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#updateAccount(java.lang.Long, org.collectionspace.services.permission.Permission)
     */
    public ClientResponse<Permission> update(String csid, Permission permission) {
        return getProxy().update(csid, permission);
    }
}
