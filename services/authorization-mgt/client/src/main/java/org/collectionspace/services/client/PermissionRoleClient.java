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
 * A PermissionRoleClient.

 * @version $Revision:$
 */
public class PermissionRoleClient extends AbstractServiceClientImpl {

    /**
     *
     */
    private PermissionRoleProxy permissionRoleProxy;

    /* (non-Javadoc)
     * @see 
     */
    public String getServicePathComponent() {
        return "authorization/permissions";
    }

    /**
     *
     * Default constructor for PermissionRoleClient class.
     *
     */
    public PermissionRoleClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.permissionRoleProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            permissionRoleProxy = ProxyFactory.create(PermissionRoleProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            permissionRoleProxy = ProxyFactory.create(PermissionRoleProxy.class,
                    getBaseURL());
        }
    }


    /**
     * @param csid
     * @param prcsid relationship does not have an id, junk is fine
     * @return
     * @see 
     */
    public ClientResponse<PermissionRole> read(String csid, String prcsid) {
        return permissionRoleProxy.read(csid, prcsid);
    }

    /**
     * @param permRole
     * @return
     * @see 
     */
    public ClientResponse<Response> create(String csid, PermissionRole permRole) {
        return permissionRoleProxy.create(csid, permRole);
    }


    /**
     * @param csid
     * @param prcsid relationship does not have an id, junk is fine
     * @return
     * @see 
     */
    public ClientResponse<Response> delete(String csid, String prcsid) {
        return permissionRoleProxy.delete(csid, prcsid);
    }
}
