/**	
 * CollectionObjectClient.java
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

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.core.Response;

// FIXME: http://issues.collectionspace.org/browse/CSPACE-1684

/**
 * CollectionObjectClient.java
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class CollectionObjectClient extends AbstractCommonListPoxServiceClientImpl<CollectionObjectProxy> {

    public static final String SERVICE_NAME = "collectionobjects";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";
    public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
    public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public Class<CollectionObjectProxy> getProxyClass() {
        return CollectionObjectProxy.class;
    }

    /**
     * Roundtrip.
     * 
     * This is an intentionally empty method that is used for performance test 
     * to get a rough time estimate of the client to server response-request overhead.
     * 
     * @see org.collectionspace.services.client.CollectionObjectProxy#roundtrip()
     * @return the client response< response>
     */
    public ClientResponse<Response> roundtrip(int ms) {
        getLogger().debug(">>>>Roundtrip start.");
        ClientResponse<Response> result = getProxy().roundtrip(ms);
        getLogger().debug("<<<<Roundtrip stop.");
        return result;
    }
}
