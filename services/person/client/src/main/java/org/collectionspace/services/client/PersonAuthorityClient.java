/**	
 * PersonAuthorityClient.java
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

//import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

//import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.collectionspace.services.person.PersonsCommonList;
import org.collectionspace.services.client.PersonAuthorityProxy;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class PersonAuthorityClient.
 */
public class PersonAuthorityClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "personauthorities";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	//
	// Subitem constants
	//
	public static final String SERVICE_ITEM_NAME = "persons";
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
    private PersonAuthorityProxy personAuthorityProxy;

    /**
     * Instantiates a new person authority client.
     */
    public PersonAuthorityClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.personAuthorityProxy;
    }    

    /**
     * Sets the proxy.
     */
    public void setProxy() {
        if (useAuth()) {
            personAuthorityProxy = ProxyFactory.create(PersonAuthorityProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            personAuthorityProxy = ProxyFactory.create(PersonAuthorityProxy.class,
                    getBaseURL());
        }
    }

    /**
     * Read list.
     *
     * @return the client response
     */
//    public static PersonAuthorityClient getInstance() {
//        return instance;
//    }

    /**
     * @return list
     * @see org.collectionspace.services.client.PersonAuthorityProxy#readList()
     */
    public ClientResponse<PersonauthoritiesCommonList> readList() {
        return personAuthorityProxy.readList();
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> read(String csid) {
        return personAuthorityProxy.read(csid);
    }

    /**
     * Read by name.
     *
     * @param name the name
     * @return the client response
     */
    public ClientResponse<String> readByName(String name) {
        return personAuthorityProxy.readByName(name);
    }

    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return personAuthorityProxy.create(xmlPayload.getBytes());
    }

    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return personAuthorityProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> delete(String csid) {
        return personAuthorityProxy.delete(csid);
    }

    /**
     * Gets the referencing objects.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the referencing objects
     */
    public ClientResponse<AuthorityRefDocList> getReferencingObjects(String parentcsid, String csid) {
        return personAuthorityProxy.getReferencingObjects(parentcsid, csid);
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
    public ClientResponse<PersonsCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords) {
        return personAuthorityProxy.readItemList(inAuthority, partialTerm, keywords);
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
    public ClientResponse<PersonsCommonList> 
    		readItemListForNamedAuthority(String specifier, String partialTerm, String keywords) {
        return personAuthorityProxy.readItemListForNamedAuthority(specifier, partialTerm, keywords);
    }

    /**
     * Read item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItem(String vcsid, String csid) {
        return personAuthorityProxy.readItem(vcsid, csid);
    }

    /**
     * Read named item.
     *
     * @param vcsid the vcsid
     * @param shortId the shortIdentifier
     * @return the client response
     */
    public ClientResponse<String> readNamedItem(String vcsid, String shortId) {
        return personAuthorityProxy.readNamedItem(vcsid, shortId);
    }

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItemInNamedAuthority(String authShortId, String csid) {
        return personAuthorityProxy.readItemInNamedAuthority(authShortId, csid);
    }

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    public ClientResponse<String> readNamedItemInNamedAuthority(String authShortId, String itemShortId) {
        return personAuthorityProxy.readNamedItemInNamedAuthority(authShortId, itemShortId);
    }

    /**
     * Creates the item.
     *
     * @param vcsid the vcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createItem(String vcsid, PoxPayloadOut xmlPayload) {
        return personAuthorityProxy.createItem(vcsid, xmlPayload.getBytes());
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
        return personAuthorityProxy.updateItem(vcsid, csid, xmlPayload.getBytes());

    }

    /**
     * Delete item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return personAuthorityProxy.deleteItem(vcsid, csid);
    }

    /***************************************************************************
     * 
     * Contact sub-resource interfaces
     * 
     ***************************************************************************/
    
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
        return personAuthorityProxy.createContact(parentcsid, itemcsid, xmlPayload.getBytes());
    }

    /**
     * Creates the contact.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param multipart
     * @return the client response
     */
    public ClientResponse<Response> createContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		PoxPayloadOut xmlPayload) {
    	return personAuthorityProxy.createContactForNamedItem(parentcsid, itemspecifier, xmlPayload.getBytes());
    }
    /**
     * Creates the contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @param multipart
     * @return the client response
     */
    public ClientResponse<Response> createContactForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		PoxPayloadOut xmlPayload) {
    	return personAuthorityProxy.createContactForItemInNamedAuthority(parentspecifier,
    			itemcsid, xmlPayload.getBytes());
    }
    /**
     * Creates the contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param multipart
     * @return the client response
     */
    public ClientResponse<Response> createContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		PoxPayloadOut xmlPayload) {
    	return personAuthorityProxy.createContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier,
    			xmlPayload.getBytes());
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
        return personAuthorityProxy.readContact(parentcsid, itemcsid, csid);
    }
    
    /**
     * Read contact.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid
     * @return the client response
     */
    public ClientResponse<String> readContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid){
    	return personAuthorityProxy.readContactForNamedItem(parentcsid, itemspecifier, csid);
    }

    /**
     * Read contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @param csid
     * @return the client response
     */
    public ClientResponse<String> readContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid){
    	return personAuthorityProxy.readContactInNamedAuthority(parentspecifier, itemcsid, csid);
    }

    /**
     * Read contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid
     * @return the client response
     */
    public ClientResponse<String> readContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid){
    	return personAuthorityProxy.readContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, csid);
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
        return personAuthorityProxy.readContactList(parentcsid, itemcsid);
    }
    
    /**
     * Read contact list.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactListForNamedItem(
    		String parentcsid,
    		String itemspecifier){
    	return personAuthorityProxy.readContactList(parentcsid, itemspecifier);
    }

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactListForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid){
    	return personAuthorityProxy.readContactList(parentspecifier, itemcsid);
    }

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactListForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier){
    	return personAuthorityProxy.readContactList(parentspecifier, itemspecifier);
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
        return personAuthorityProxy.updateContact(parentcsid, itemcsid, csid, xmlPayload.getBytes());
    }
    
    /**
     * Update contact.
     *
     * @param parentcsid the parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return personAuthorityProxy.updateContactForNamedItem(parentcsid, itemspecifier, csid, xmlPayload.getBytes());
    }

    /**
     * Update contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return personAuthorityProxy.updateContactInNamedAuthority(parentspecifier, itemcsid, csid, xmlPayload.getBytes());
    }

    /**
     * Update contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return personAuthorityProxy.updateContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, csid,
    			xmlPayload.getBytes());
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
        return personAuthorityProxy.deleteContact(parentcsid,
            itemcsid, csid);
    }
    
    /**
     * Delete contact.
     *
     * @param parentcsid the parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid) {
    	return personAuthorityProxy.deleteContactForNamedItem(parentcsid,
    			itemspecifier, csid);
    }

    /**
     * Delete contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid) {
    	return personAuthorityProxy.deleteContactInNamedAuthority(parentspecifier,
    			itemcsid, csid);
    }

    /**
     * Delete contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid) {
    	return personAuthorityProxy.deleteContactForNamedItemInNamedAuthority(parentspecifier,
    			itemspecifier, csid);
    }

}
