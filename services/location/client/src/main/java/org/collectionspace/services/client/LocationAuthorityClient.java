/**	
 * LocationAuthorityClient.java
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

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

//import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.location.LocationauthoritiesCommonList;
import org.collectionspace.services.location.LocationsCommonList;
import org.collectionspace.services.client.LocationAuthorityProxy;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class LocationAuthorityClient.
 */
public class LocationAuthorityClient extends AuthorityClientImpl<LocationauthoritiesCommonList, LocationsCommonList, LocationAuthorityProxy> {
	public static final String SERVICE_NAME = "locationauthorities";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	//
	// Subitem constants
	//
	public static final String SERVICE_ITEM_NAME = "locations";
	public static final String SERVICE_PATH_ITEMS_COMPONENT = "items";	//FIXME: REM - This should be defined in an AuthorityClient base class
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
	public Class<LocationAuthorityProxy> getProxyClass() {
		return LocationAuthorityProxy.class;
	}
    
    /*
     * Proxied service calls.
     */
    
    /**
     * @return list
     * @see org.collectionspace.services.client.LocationAuthorityProxy#readList()
     */
    public ClientResponse<LocationauthoritiesCommonList> readList() {
        return getProxy().readList();
    }
}
