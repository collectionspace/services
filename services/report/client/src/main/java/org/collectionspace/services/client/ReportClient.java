/**	
 * ReportClient.java
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

//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.report.ReportsCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * A ReportClient.

 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
public class ReportClient extends AbstractPoxServiceClientImpl<ReportProxy> {

    public static final String SERVICE_NAME = "reports";
    public static final String SERVICE_PATH_COMPONENT = "reports";

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
	public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<ReportProxy> getProxyClass() {
		return ReportProxy.class;
	}
	
	/*
	 * Proxied service calls.
	 */
	
    /**
     * @return
     * @see org.collectionspace.services.client.ReportProxy#getReport()
     */
    public ClientResponse<ReportsCommonList> readList() {
        return getProxy().readList();
    }
    
}
