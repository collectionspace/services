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

import org.jboss.resteasy.client.ClientResponse;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * LoaninClient.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 *
 */
public class LoaninClient extends AbstractPoxServiceClientImpl<LoaninProxy> {
    public static final String SERVICE_NAME = "loansin";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;

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
	public Class<LoaninProxy> getProxyClass() {
		return LoaninProxy.class;
	}

    /*
     * Proxied service calls
     */

    /**
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#getLoanin()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return getProxy().readList();
    }
}
