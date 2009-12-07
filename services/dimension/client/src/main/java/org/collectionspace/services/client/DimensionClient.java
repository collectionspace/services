/**	
 * DimensionClient.java
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
 * Copyright (c) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.dimension.DimensionsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A DimensionClient.

 * @version $Revision:$
 */
public class DimensionClient extends BaseServiceClient {

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
	 */
	public String getServicePathComponent() {
		return "dimensions";
	}

	/**
     *
     */
    private static final DimensionClient instance = new DimensionClient();
    
    /**
     *
     */
    private DimensionProxy dimensionProxy;

    /**
     *
     * Default constructor for DimensionClient class.
     *
     */
    public DimensionClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        dimensionProxy = ProxyFactory.create(DimensionProxy.class, getBaseURL());
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static DimensionClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#getDimension()
     */
    public ClientResponse<DimensionsCommonList> readList() {
        return dimensionProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#getDimension(java.lang.String)
     */

    public ClientResponse<MultipartInput> read(String csid) {
        return dimensionProxy.read(csid);
    }

    /**
     * @param dimension
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#createDimension(org.collectionspace.services.Dimension)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return dimensionProxy.create(multipart);
    }

    /**
     * @param csid
     * @param dimension
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#updateDimension(java.lang.Long, org.collectionspace.services.Dimension)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return dimensionProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.DimensionProxy#deleteDimension(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return dimensionProxy.delete(csid);
    }
}
