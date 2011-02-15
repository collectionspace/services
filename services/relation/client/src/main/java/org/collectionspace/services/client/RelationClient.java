/**	
 * RelationClient.java
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
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.relation.RelationsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class RelationClient.
 */
public class RelationClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "relations";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public String getServicePathComponent() {
		return SERVICE_PATH_COMPONENT;
	}

	/** The relation proxy. */
	private RelationProxy relationProxy;

	/**
	 * Instantiates a new relation client.
	 */
	public RelationClient() {
		ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
		RegisterBuiltin.register(factory);
		setProxy();
	}

    @Override
	public CollectionSpaceProxy getProxy() {
    	return this.relationProxy;
    }

    /**
	 * Sets the proxy.
	 */
	@Override
	public void setProxy() {
		if (useAuth()) {
			relationProxy = ProxyFactory.create(RelationProxy.class,
					getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
		} else {
			relationProxy = ProxyFactory.create(RelationProxy.class,
					getBaseURL());
		}
	}

	/**
	 * Read list.
	 *
	 * @return the client response
	 */
	public ClientResponse<RelationsCommonList> readList() {
		return relationProxy.readList();
	}

	/**
	 * Read list.
	 *
	 * @param subjectCsid the subject csid
	 * @param subjectType 
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * @param objectType 
	 * @return the client response
	 */
	public ClientResponse<RelationsCommonList> readList(String subjectCsid,
			String subjectType,
			String predicate,
			String objectCsid,
			String objectType) {
		return relationProxy.readList(subjectCsid, subjectType, predicate, objectCsid, objectType);
	}

    public ClientResponse<RelationsCommonList> readList(String subjectCsid,
            String subjectType,
            String predicate,
            String objectCsid,
            String objectType,
            String sortBy,
            Long pageSize,
            Long pageNumber) {
        return relationProxy.readList(subjectCsid, subjectType, predicate, objectCsid, objectType, sortBy, pageSize, pageNumber);
    }


	/**
	 * Read.
	 *
	 * @param csid the csid
	 * @return the client response
	 */
	public ClientResponse<String> read(String csid) {
		return relationProxy.read(csid);
	}

	/**
	 * Creates the.
	 *
	 * @param multipart the multipart
	 * @return the client response
	 */
	public ClientResponse<Response> create(PoxPayloadOut multipart) {
            String payload = multipart.toXML();
            return relationProxy.create(payload);
	}

	/**
	 * Update.
	 *
	 * @param csid the csid
	 * @param multipart the multipart
	 * @return the client response
	 */
	public ClientResponse<String> update(String csid,
                PoxPayloadOut multipart) {
            String payload = multipart.toXML();
            return relationProxy.update(csid, payload);
	}

	/**
	 * Delete.
	 *
	 * @param csid the csid
	 * @return the client response
	 */
	public ClientResponse<Response> delete(String csid) {
		return relationProxy.delete(csid);
	}
}
