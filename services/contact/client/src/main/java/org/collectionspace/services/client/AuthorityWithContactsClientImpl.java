package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * LT - List type
 * ILT - Authority item list type
 * P - Proxy type
 */
public abstract class AuthorityWithContactsClientImpl<AUTHORITY_ITEM_TYPE, P extends AuthorityWithContactsProxy>
	extends AuthorityClientImpl<AUTHORITY_ITEM_TYPE, P>
	implements AuthorityWithContactsClient<AUTHORITY_ITEM_TYPE, P> {
	
	@Override
    public ClientResponse<Response> createContact(String parentcsid,
            String itemcsid, PoxPayloadOut xmlPayload) {
        return getProxy().createContact(parentcsid, itemcsid, xmlPayload.getBytes());
    }
    
    /**
     * Creates the contact.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param multipart
     * @return the client response
     */
    @Override
	public ClientResponse<Response> createContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		PoxPayloadOut xmlPayload) {
    	return getProxy().createContactForNamedItem(parentcsid, itemspecifier, xmlPayload.getBytes());
    }
    /**
     * Creates the contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @param multipart
     * @return the client response
     */
    @Override
	public ClientResponse<Response> createContactForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		PoxPayloadOut xmlPayload) {
    	return getProxy().createContactForItemInNamedAuthority(parentspecifier,
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
    @Override
	public ClientResponse<Response> createContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		PoxPayloadOut xmlPayload) {
    	return getProxy().createContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier,
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
    @Override
	public ClientResponse<String> readContact(String parentcsid,
            String itemcsid, String csid) {
        return getProxy().readContact(parentcsid, itemcsid, csid);
    }
    
    /**
     * Read contact.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid
     * @return the client response
     */
    @Override
	public ClientResponse<String> readContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid){
    	return getProxy().readContactForNamedItem(parentcsid, itemspecifier, csid);
    }

    /**
     * Read contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @param csid
     * @return the client response
     */
    @Override
	public ClientResponse<String> readContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid){
    	return getProxy().readContactInNamedAuthority(parentspecifier, itemcsid, csid);
    }

    /**
     * Read contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid
     * @return the client response
     */
    @Override
	public ClientResponse<String> readContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid){
    	return getProxy().readContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, csid);
    }
            

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the client response
     */
    @Override
	public ClientResponse<AbstractCommonList> readContactList(String parentcsid,
            String itemcsid) {
        return getProxy().readContactList(parentcsid, itemcsid);
    }
    
    /**
     * Read contact list.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    @Override
	public ClientResponse<AbstractCommonList> readContactListForNamedItem(
    		String parentcsid,
    		String itemspecifier){
    	return getProxy().readContactList(parentcsid, itemspecifier);
    }

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @return the client response
     */
    @Override
	public ClientResponse<AbstractCommonList> readContactListForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid){
    	return getProxy().readContactList(parentspecifier, itemcsid);
    }

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    @Override
	public ClientResponse<AbstractCommonList> readContactListForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier){
    	return getProxy().readContactList(parentspecifier, itemspecifier);
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
    @Override
	public ClientResponse<String> updateContact(String parentcsid,
            String itemcsid, String csid, PoxPayloadOut xmlPayload) {
        return getProxy().updateContact(parentcsid, itemcsid, csid, xmlPayload.getBytes());
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
    @Override
	public ClientResponse<String> updateContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return getProxy().updateContactForNamedItem(parentcsid, itemspecifier, csid, xmlPayload.getBytes());
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
    @Override
	public ClientResponse<String> updateContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return getProxy().updateContactInNamedAuthority(parentspecifier, itemcsid, csid, xmlPayload.getBytes());
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
    @Override
	public ClientResponse<String> updateContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return getProxy().updateContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, csid,
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
    @Override
	public ClientResponse<Response> deleteContact(String parentcsid,
        String itemcsid, String csid) {
        return getProxy().deleteContact(parentcsid,
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
    @Override
	public ClientResponse<Response> deleteContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid) {
    	return getProxy().deleteContactForNamedItem(parentcsid,
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
    @Override
	public ClientResponse<Response> deleteContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid) {
    	return getProxy().deleteContactInNamedAuthority(parentspecifier,
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
    @Override
	public ClientResponse<Response> deleteContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid) {
    	return getProxy().deleteContactForNamedItemInNamedAuthority(parentspecifier,
    			itemspecifier, csid);
    }
    
}
