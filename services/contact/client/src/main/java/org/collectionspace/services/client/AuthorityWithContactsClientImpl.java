package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

/*
 * LT - List type
 * ILT - Authority item list type
 * P - Proxy type
 */
public abstract class AuthorityWithContactsClientImpl<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE, P extends AuthorityWithContactsProxy>
	extends AuthorityClientImpl<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE, P>
	implements AuthorityWithContactsClient<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE, P> {
	
	public AuthorityWithContactsClientImpl(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	public AuthorityWithContactsClientImpl() throws Exception {
		super();
	}

	@Override
    public Response createContact(String parentcsid,
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
	public Response createContactForNamedItem(
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
	public Response createContactForItemInNamedAuthority(
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
	public Response createContactForNamedItemInNamedAuthority(
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
	public Response readContact(String parentcsid,
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
	public Response readContactForNamedItem(
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
	public Response readContactInNamedAuthority(
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
	public Response readContactForNamedItemInNamedAuthority(
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
	public Response readContactList(String parentcsid,
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
	public Response readContactListForNamedItem(
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
	public Response readContactListForItemInNamedAuthority(
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
	public Response readContactListForNamedItemInNamedAuthority(
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
	public Response updateContact(String parentcsid,
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
	public Response updateContactForNamedItem(
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
	public Response updateContactInNamedAuthority(
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
	public Response updateContactForNamedItemInNamedAuthority(
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
	public Response deleteContact(String parentcsid,
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
	public Response deleteContactForNamedItem(
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
	public Response deleteContactInNamedAuthority(
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
	public Response deleteContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid) {
    	return getProxy().deleteContactForNamedItemInNamedAuthority(parentspecifier,
    			itemspecifier, csid);
    }
    
}
