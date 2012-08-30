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

import org.jboss.resteasy.client.ClientResponse;
import org.collectionspace.services.relation.RelationsCommonList;

/**
 * The Class RelationClient.
 */
public class RelationClient extends AbstractPoxServiceClientImpl<RelationsCommonList, RelationProxy> {
	public static final String SERVICE_DOC_TYPE = IRelationsManager.DOC_TYPE; // Used for CMIS queries only -should be the same as what's in the tenant bindings
	public static final String SERVICE_NAME = IRelationsManager.SERVICE_NAME;
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	public static final String SERVICE_COMMON_LIST_NAME = "relations-common-list";
    public static final String SERVICE_COMMONPART_NAME = IRelationsManager.SERVICE_COMMONPART_NAME;
    
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public String getServicePathComponent() {
		return SERVICE_PATH_COMPONENT;
	}

	@Override
	public Class<RelationProxy> getProxyClass() {
		return RelationProxy.class;
	}

	/*
	 * Proxied service calls
	 */
	
	/**
	 * Read list.
	 *
	 * @return the client response
	 */
	public ClientResponse<RelationsCommonList> readList() {
		return getProxy().readList();
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
		return getProxy().readList(subjectCsid, subjectType, predicate, objectCsid, objectType);
	}

    public ClientResponse<RelationsCommonList> readList(String subjectCsid,
            String subjectType,
            String predicate,
            String objectCsid,
            String objectType,
            String sortBy,
            Long pageSize,
            Long pageNumber) {
        return getProxy().readList(subjectCsid, subjectType, predicate, objectCsid, objectType, sortBy, pageSize, pageNumber);
    }
}
