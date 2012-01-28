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
package org.collectionspace.services.client.workflow;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.AbstractCommonListPoxServiceClientImpl;
import org.jboss.resteasy.client.ClientResponse;

/**
 * WorkflowClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class WorkflowClient extends AbstractCommonListPoxServiceClientImpl<WorkflowProxy> {
	public static final String SERVICE_NAME = "workflow";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	public static final String SERVICE_COMMONPART_NAME = SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
	public static final String SERVICE_AUTHZ_SUFFIX = "/*/" + SERVICE_PATH_COMPONENT + "/";
	//
	// Workflow states
	//
	public static final String WORKFLOWSTATE_DELETED = "deleted";
	public static final String WORKFLOWSTATE_PROJECT = "project";
	public static final String WORKFLOWSTATE_APPROVED = "approved";	
	//
	// Service Query Params
	//
	public static final String WORKFLOW_QUERY_NONDELETED = "wf_deleted";	

	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<WorkflowProxy> getProxyClass() {
		// TODO Auto-generated method stub
		return WorkflowProxy.class;
	}

	/*
	 * Proxied service calls
	 */
	
	@Override
	public ClientResponse<AbstractCommonList> readList() {
        throw new UnsupportedOperationException();
	}
	
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#delete(java.lang.String)
     */
    @Override
	public ClientResponse<Response> delete(String csid) {
        throw new UnsupportedOperationException();
    }
	
}
