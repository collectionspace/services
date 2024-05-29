/*
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import org.collectionspace.services.nagprainventory.NagpraInventoriesCommon;

/**
 * NagpraInventoryClient.java
 */
public class NagpraInventoryClient
        extends AbstractCommonListPoxServiceClientImpl<NagpraInventoryProxy, NagpraInventoriesCommon> {

    public static final String SERVICE_NAME = "nagprainventories";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";

    public NagpraInventoryClient() throws Exception {
        super();
    }

    public NagpraInventoryClient(String clientPropertiesFilename) throws Exception {
        super(clientPropertiesFilename);
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public Class<NagpraInventoryProxy> getProxyClass() {
        return NagpraInventoryProxy.class;
    }
}
