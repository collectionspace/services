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

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.movement.MovementsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * MovementClient.java
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 */
public class MovementClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "movements";
    }
    /**
     *
     */
//    private static final MovementClient instance = new MovementClient();
    /**
     *
     */
    private MovementProxy movementProxy;

    /**
     *
     * Default constructor for MovementClient class.
     *
     */
    public MovementClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.movementProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            movementProxy = ProxyFactory.create(MovementProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            movementProxy = ProxyFactory.create(MovementProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static MovementClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.MovementProxy#getMovement()
     */
    public ClientResponse<MovementsCommonList> readList() {
        return movementProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.MovementProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return movementProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.MovementProxy#getMovement(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return movementProxy.read(csid);
    }

    /**
     * @param movement
     * @return
     * @see org.collectionspace.services.client.MovementProxy#createMovement(org.collectionspace.hello.Movement)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return movementProxy.create(multipart);
    }

    /**
     * @param csid
     * @param movement
     * @return
     * @see org.collectionspace.services.client.MovementProxy#updateMovement(java.lang.Long, org.collectionspace.hello.Movement)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return movementProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.MovementProxy#deleteMovement(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return movementProxy.delete(csid);
    }
}
