package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/*
 * LT - List type
 * ILT - Authority item list type
 * P - Proxy type
 */
public interface AuthorityWithContactsClient<AUTHORITY_ITEM_TYPE, P extends AuthorityProxy>
	extends AuthorityClient<AUTHORITY_ITEM_TYPE, P> {
    /**
     * Creates the contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createContact(String parentcsid,
            String itemcsid, PoxPayloadOut xmlPayload);
    
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
    		PoxPayloadOut xmlPayload);
    
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
    		PoxPayloadOut xmlPayload);
    
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
    		PoxPayloadOut xmlPayload);
    
    /**
     * Read contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readContact(String parentcsid,
            String itemcsid, String csid);
    
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
    		String csid);

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
    		String csid);

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
    		String csid);
            

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the client response
     */
    public ClientResponse<AbstractCommonList> readContactList(String parentcsid,
            String itemcsid);
    
    /**
     * Read contact list.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    public ClientResponse<AbstractCommonList> readContactListForNamedItem(
    		String parentcsid,
    		String itemspecifier);

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @return the client response
     */
    public ClientResponse<AbstractCommonList> readContactListForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid);

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    public ClientResponse<AbstractCommonList> readContactListForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier);

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
            String itemcsid, String csid, PoxPayloadOut xmlPayload);
    
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
    		PoxPayloadOut xmlPayload);

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
    		PoxPayloadOut xmlPayload);

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
    		PoxPayloadOut xmlPayload);

    /**
     * Delete contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContact(String parentcsid,
        String itemcsid, String csid);
    
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
    		String csid);

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
    		String csid);

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
    		String csid);
    
}
