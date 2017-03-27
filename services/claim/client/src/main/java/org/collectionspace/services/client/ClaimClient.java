/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */

package org.collectionspace.services.client;

import org.collectionspace.services.claim.ClaimsCommon;

/**
 * ClaimClient.java
 *
 * $LastChangedRevision: 5284 $
 * $LastChangedDate: 2011-07-22 12:44:36 -0700 (Fri, 22 Jul 2011) $
 *
 */
public class ClaimClient extends AbstractCommonListPoxServiceClientImpl<ClaimProxy, ClaimsCommon> {
	public static final String SERVICE_NAME = "claims";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

    public ClaimClient() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<ClaimProxy> getProxyClass() {
		return ClaimProxy.class;
	}
}
