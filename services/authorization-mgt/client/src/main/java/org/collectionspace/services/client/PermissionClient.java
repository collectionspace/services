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


import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionsList;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A PermissionClient.

 * @version $Revision:$
 */
public class PermissionClient extends AbstractServiceClientImpl {

    /**
     *
     */
    private PermissionProxy permissionProxy;

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "authorization/permissions";
    }

    /**
     *
     * Default constructor for PermissionClient class.
     *
     */
    public PermissionClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            permissionProxy = ProxyFactory.create(PermissionProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            permissionProxy = ProxyFactory.create(PermissionProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#readList()
     */
    public ClientResponse<PermissionsList> readList() {
        return permissionProxy.readList();

    }

    public ClientResponse<PermissionsList> readSearchList(String resourceName) {
        return permissionProxy.readSearchList(resourceName);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#getAccount(java.lang.String)
     */
    public ClientResponse<Permission> read(String csid) {
        return permissionProxy.read(csid);
    }

    /**
     * @param permission
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#create(org.collectionspace.services.permission.Permission)
     */
    public ClientResponse<Response> create(Permission permission) {
        return permissionProxy.create(permission);
    }

    /**
     * @param csid
     * @param permission
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#updateAccount(java.lang.Long, org.collectionspace.services.permission.Permission)
     */
    public ClientResponse<Permission> update(String csid, Permission permission) {
        return permissionProxy.update(csid, permission);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#deleteAccount(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return permissionProxy.delete(csid);
    }
}
