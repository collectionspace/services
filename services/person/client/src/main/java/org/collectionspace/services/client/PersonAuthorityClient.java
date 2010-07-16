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
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class PersonAuthorityClient.
 */
public class PersonAuthorityClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return "personauthorities";
    }

    /**
     * Gets the item common part name.
     *
     * @return the item common part name
     */
    public String getItemCommonPartName() {
        return getCommonPartName("persons");
    }

    /** The person authority proxy. */
//    private static final PersonAuthorityClient instance = new PersonAuthorityClient();
    
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
            personAuthorityProxy = ProxyFactory.create(PersonAuthorityProxy.class, //FIXME: This method is deprecated.  We need to use the new "executor" model -see related JavaDocs.
                    getBaseURL(), getHttpClient());
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
    public ClientResponse<MultipartInput> read(String csid) {
        return personAuthorityProxy.read(csid);
    }

    /**
     * Read by name.
     *
     * @param name the name
     * @return the client response
     */
    public ClientResponse<MultipartInput> readByName(String name) {
        return personAuthorityProxy.readByName(name);
    }

    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return personAuthorityProxy.create(multipart);
    }

    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return personAuthorityProxy.update(csid, multipart);

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
    public ClientResponse<MultipartInput> readItem(String vcsid, String csid) {
        return personAuthorityProxy.readItem(vcsid, csid);
    }

    /**
     * Read named item.
     *
     * @param vcsid the vcsid
     * @param shortId the shortIdentifier
     * @return the client response
     */
    public ClientResponse<MultipartInput> readNamedItem(String vcsid, String shortId) {
        return personAuthorityProxy.readNamedItem(vcsid, shortId);
    }

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<MultipartInput> readItemInNamedAuthority(String authShortId, String csid) {
        return personAuthorityProxy.readItemInNamedAuthority(authShortId, csid);
    }

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    public ClientResponse<MultipartInput> readNamedItemInNamedAuthority(String authShortId, String itemShortId) {
        return personAuthorityProxy.readNamedItemInNamedAuthority(authShortId, itemShortId);
    }

    /**
     * Creates the item.
     *
     * @param vcsid the vcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createItem(String vcsid, MultipartOutput multipart) {
        return personAuthorityProxy.createItem(vcsid, multipart);
    }

    /**
     * Update item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<MultipartInput> updateItem(String vcsid, String csid, MultipartOutput multipart) {
        return personAuthorityProxy.updateItem(vcsid, csid, multipart);

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

    /**
     * Creates the contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createContact(String parentcsid,
            String itemcsid, MultipartOutput multipart) {
        return personAuthorityProxy.createContact(parentcsid, itemcsid, multipart);
    }

    /**
     * Read contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<MultipartInput> readContact(String parentcsid,
            String itemcsid, String csid) {
        return personAuthorityProxy.readContact(parentcsid, itemcsid, csid);
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
     * Update contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<MultipartInput> updateContact(String parentcsid,
            String itemcsid, String csid, MultipartOutput multipart) {
        return personAuthorityProxy.updateContact(parentcsid, itemcsid, csid, multipart);
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

}
