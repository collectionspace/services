/**	
 * PlaceAuthorityClient.java
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

/**
 * The Class PlaceAuthorityClient.
 */
public class PlaceAuthorityClient extends AuthorityClientImpl<PlaceAuthorityProxy> {
	public static final String SERVICE_NAME = "placeauthorities";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	//
	// Subitem constants
	//
	public static final String SERVICE_ITEM_NAME = "places";
	public static final String SERVICE_ITEM_PAYLOAD_NAME = SERVICE_ITEM_NAME;
	//
	// Payload Part/Schema part names
	//
	public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME + 
		PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
	public static final String SERVICE_ITEM_COMMON_PART_NAME = SERVICE_ITEM_NAME + 
		PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public String getItemCommonPartName() {
        return getCommonPartName(SERVICE_ITEM_NAME);
    }

	@Override
	public Class<PlaceAuthorityProxy> getProxyClass() {
		return PlaceAuthorityProxy.class;
	}
}
