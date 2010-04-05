/**	
 * RoleClient.java
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


import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RolesList;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A RoleClient.

 * @version $Revision:$
 */
public class RoleClient extends AbstractServiceClientImpl {

    /**
     *
     */
    private RoleProxy roleProxy;

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "authorization/roles";
    }

    /**
     *
     * Default constructor for RoleClient class.
     *
     */
    public RoleClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            roleProxy = ProxyFactory.create(RoleProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            roleProxy = ProxyFactory.create(RoleProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.RoleProxy#readList()
     */
    public ClientResponse<RolesList> readList() {
        return roleProxy.readList();

    }

    public ClientResponse<RolesList> readSearchList(String roleName) {
        return roleProxy.readSearchList(roleName);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RoleProxy#getAccount(java.lang.String)
     */
    public ClientResponse<Role> read(String csid) {
        return roleProxy.read(csid);
    }

    /**
     * @param role
     * @return
     * @see org.collectionspace.hello.client.RoleProxy#create(org.collectionspace.services.role.Role)
     */
    public ClientResponse<Response> create(Role role) {
        return roleProxy.create(role);
    }

    /**
     * @param csid
     * @param role
     * @return
     * @see org.collectionspace.hello.client.RoleProxy#updateAccount(java.lang.Long, org.collectionspace.services.role.Role)
     */
    public ClientResponse<Role> update(String csid, Role role) {
        return roleProxy.update(csid, role);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RoleProxy#deleteAccount(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return roleProxy.delete(csid);
    }
}
