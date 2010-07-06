/**	
 * OrgAuthorityClient.java
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

import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class OrgAuthorityClient.
 */
public class OrgAuthorityClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return "orgauthorities";
    }

    /**
     * Gets the item common part name.
     *
     * @return the item common part name
     */
    public String getItemCommonPartName() {
        return getCommonPartName("organizations");
    }
    
    /** The Constant instance. */  //FIXME: This is wrong.  There should NOT be a static instance of the OrgAuthorityClient class
//    private static final OrgAuthorityClient instance = new OrgAuthorityClient();
    
    /** The org authority proxy. */
    private OrgAuthorityProxy orgAuthorityProxy;

    /**
     * Instantiates a new org authority client.
     */
    public OrgAuthorityClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.orgAuthorityProxy;
    }    

    /**
     * Sets the proxy.
     */
    public void setProxy() {
        if (useAuth()) {
            orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class,
                    getBaseURL());
        }
    }

    /**
     * Gets the single instance of OrgAuthorityClient.
     *
     * @return single instance of OrgAuthorityClient //FIXME: This is wrong.  There should NOT be a static instance of the client
     */
//    public static OrgAuthorityClient getInstance() {
//        return instance;
//    }

    /**
     * Read list.
     *
     * @return the client response
     */
    public ClientResponse<OrgauthoritiesCommonList> readList() {
        return orgAuthorityProxy.readList();
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return orgAuthorityProxy.read(csid);
    }

    /**
     * Read by name.
     *
     * @param name the name
     * @return the client response
     */
    public ClientResponse<MultipartInput> readByName(String name) {
        return orgAuthorityProxy.readByName(name);
    }

    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return orgAuthorityProxy.create(multipart);
    }

    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return orgAuthorityProxy.update(csid, multipart);

    }

    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return orgAuthorityProxy.delete(csid);
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
    public ClientResponse<OrganizationsCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords) {
        return orgAuthorityProxy.readItemList(inAuthority, partialTerm, keywords);
    }
    
    /**
     * Gets the referencing objects.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the referencing objects
     */
    public ClientResponse<AuthorityRefDocList> getReferencingObjects(String parentcsid, String csid) {
        return orgAuthorityProxy.getReferencingObjects(parentcsid, csid);
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
    public ClientResponse<OrganizationsCommonList> 
    		readItemListForNamedAuthority(String specifier, String partialTerm, String keywords) {
        return orgAuthorityProxy.readItemListForNamedAuthority(specifier, partialTerm, keywords);
    }

    /**
     * Gets the item authority refs.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the item authority refs
     */
    public ClientResponse<AuthorityRefList> getItemAuthorityRefs(String parentcsid, String csid) {
        return orgAuthorityProxy.getItemAuthorityRefs(parentcsid, csid);
    }

    /**
     * Read item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<MultipartInput> readItem(String vcsid, String csid) {
        return orgAuthorityProxy.readItem(vcsid, csid);
    }

    /**
     * Read named item.
     *
     * @param vcsid the vcsid
     * @param shortId the shortIdentifier
     * @return the client response
     */
    public ClientResponse<MultipartInput> readNamedItem(String vcsid, String shortId) {
        return orgAuthorityProxy.readNamedItem(vcsid, shortId);
    }

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<MultipartInput> readItemInNamedAuthority(String authShortId, String csid) {
        return orgAuthorityProxy.readItemInNamedAuthority(authShortId, csid);
    }

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    public ClientResponse<MultipartInput> readNamedItemInNamedAuthority(String authShortId, String itemShortId) {
        return orgAuthorityProxy.readNamedItem(authShortId, itemShortId);
    }

    /**
     * Creates the item.
     *
     * @param vcsid the vcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createItem(String vcsid, MultipartOutput multipart) {
        return orgAuthorityProxy.createItem(vcsid, multipart);
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
        return orgAuthorityProxy.updateItem(vcsid, csid, multipart);

    }

    /**
     * Delete item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return orgAuthorityProxy.deleteItem(vcsid, csid);
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
        return orgAuthorityProxy.createContact(parentcsid, itemcsid, multipart);
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
        return orgAuthorityProxy.readContact(parentcsid, itemcsid, csid);
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
        return orgAuthorityProxy.readContactList(parentcsid, itemcsid);
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
        return orgAuthorityProxy.updateContact(parentcsid, itemcsid, csid, multipart);
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
        return orgAuthorityProxy.deleteContact(parentcsid,
            itemcsid, csid);
    }

}
