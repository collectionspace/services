/**	
 * PermissionRoleClient.java
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


import org.collectionspace.services.authorization.PermissionRole;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A RolePermissionClient.

 * @version $Revision:$
 */
public class RolePermissionClient extends AbstractServiceClientImpl {

    /** The role permission proxy. */
    private RolePermissionProxy rolePermissionProxy;

    /* (non-Javadoc)
     * @see 
     */
    public String getServicePathComponent() {
        return "authorization/roles";
    }

    /**
     *
     * Default constructor for PermissionRoleClient class.
     *
     */
    public RolePermissionClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.rolePermissionProxy;
    }    

    /**
     * allow to reset proxy as per security needs.
     */
    public void setProxy() {
        if (useAuth()) {
            rolePermissionProxy = ProxyFactory.create(RolePermissionProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            rolePermissionProxy = ProxyFactory.create(RolePermissionProxy.class,
                    getBaseURL());
        }
    }


    /**
     * Read.
     *
     * @param csid the csid
     * @param prcsid relationship does not have an id, junk is fine
     * @return the client response
     * @see
     */
    public ClientResponse<PermissionRole> read(String csid, String prcsid) {
        return rolePermissionProxy.read(csid, prcsid);
    }

    /**
     * Creates the.
     *
     * @param csid the csid
     * @param permRole the perm role
     * @return the client response
     * @see
     */
    public ClientResponse<Response> create(String csid, PermissionRole permRole) {
        return rolePermissionProxy.create(csid, permRole);
    }

    /**
     * Delete.
     *
     * @param csid the csid
     * @param prcsid relationship does not have an id, junk is fine
     * @return response
     * @see
     */
    public ClientResponse<Response> delete(String csid, String prcsid) {
        return rolePermissionProxy.delete(csid, prcsid);
    }
        
}
