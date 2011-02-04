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
public class LocationAuthorityClient extends AbstractServiceClientImpl {  //FIXME: REM - Client classes for Person, Org, Vocab, and Location should have a common base class
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

    public String getItemCommonPartName() {
        return getCommonPartName(SERVICE_ITEM_NAME);
    }
        
    /**
     *
     */
    private LocationAuthorityProxy locationAuthorityProxy;

    /**
     * Instantiates a new location authority client.
     */
    public LocationAuthorityClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.locationAuthorityProxy;
    }    

    /**
     * Sets the proxy.
     */
    public void setProxy() {
        if (useAuth()) {
            locationAuthorityProxy = ProxyFactory.create(LocationAuthorityProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            locationAuthorityProxy = ProxyFactory.create(LocationAuthorityProxy.class,
                    getBaseURL());
        }
    }

    /**
     * Read list.
     *
     * @return the client response
     */

    /**
     * @return list
     * @see org.collectionspace.services.client.LocationAuthorityProxy#readList()
     */
    public ClientResponse<LocationauthoritiesCommonList> readList() {
        return locationAuthorityProxy.readList();
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> read(String csid) {
        return locationAuthorityProxy.read(csid);
    }

    /**
     * Read by name.
     *
     * @param name the name
     * @return the client response
     */
    public ClientResponse<String> readByName(String name) {
        return locationAuthorityProxy.readByName(name);
    }

    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return locationAuthorityProxy.create(xmlPayload.toXML());
    }

    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return locationAuthorityProxy.update(csid, xmlPayload.toXML());

    }

    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> delete(String csid) {
        return locationAuthorityProxy.delete(csid);
    }

    /**
     * Read item list, filtering by partial term match, or keywords. Only one of
     * partialTerm or keywords should be specified. If both are specified, keywords
     * will be ignored.
     *
     * @param inAuthority the parent authority
     * @param partialTerm A partial term on which to match,
     *     which will filter list results to return only matched resources.
     * @param keywords A set of keywords on which to match,
     *     which will filter list results to return only matched resources.
     * @return the client response
     */
    public ClientResponse<LocationsCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords) {
        return locationAuthorityProxy.readItemList(inAuthority, partialTerm, keywords);
    }

    /**
     * Gets the referencing objects.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the referencing objects
     */
    public ClientResponse<AuthorityRefDocList> getReferencingObjects(String parentcsid, String csid) {
        return locationAuthorityProxy.getReferencingObjects(parentcsid, csid);
    }

    /**
     * Read item list for named vocabulary, filtering by partial term match, or keywords. Only one of
     * partialTerm or keywords should be specified. If both are specified, keywords
     * will be ignored.
     *
     * @param specifier the specifier
     * @param partialTerm A partial term on which to match,
     *     which will filter list results to return only matched resources.
     * @param keywords A set of keywords on which to match,
     *     which will filter list results to return only matched resources.
     * @return the client response
     */
    public ClientResponse<LocationsCommonList> 
    		readItemListForNamedAuthority(String specifier, String partialTerm, String keywords) {
        return locationAuthorityProxy.readItemListForNamedAuthority(specifier, partialTerm, keywords);
    }

    /**
     * Read item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItem(String vcsid, String csid) {
        return locationAuthorityProxy.readItem(vcsid, csid);
    }

    /**
     * Creates the item.
     *
     * @param vcsid the vcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createItem(String vcsid, PoxPayloadOut multipart) {
        return locationAuthorityProxy.createItem(vcsid, multipart.toXML());
    }

    /**
     * Update item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateItem(String vcsid, String csid, PoxPayloadOut xmlPayload) {
        return locationAuthorityProxy.updateItem(vcsid, csid, xmlPayload.toXML());

    }

    /**
     * Delete item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return locationAuthorityProxy.deleteItem(vcsid, csid);
    }

    /**
     * Creates the contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createContact(String parentcsid,
            String itemcsid, PoxPayloadOut xmlPayload) {
        return locationAuthorityProxy.createContact(parentcsid, itemcsid, xmlPayload.toXML());
    }

    /**
     * Read contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readContact(String parentcsid,
            String itemcsid, String csid) {
        return locationAuthorityProxy.readContact(parentcsid, itemcsid, csid);
    }

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactList(String parentcsid,
            String itemcsid) {
        return locationAuthorityProxy.readContactList(parentcsid, itemcsid);
    }

    /**
     * Update contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContact(String parentcsid,
            String itemcsid, String csid, PoxPayloadOut xmlPayload) {
        return locationAuthorityProxy.updateContact(parentcsid, itemcsid, csid, xmlPayload.toXML());
    }

    /**
     * Delete contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContact(String parentcsid,
        String itemcsid, String csid) {
        return locationAuthorityProxy.deleteContact(parentcsid,
            itemcsid, csid);
    }
}
