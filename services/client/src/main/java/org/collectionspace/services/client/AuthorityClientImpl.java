package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * P - Proxy type
 */
public abstract class AuthorityClientImpl<AUTHORITY_ITEM_TYPE, P extends AuthorityProxy>
	extends AbstractPoxServiceClientImpl<AbstractCommonList, P>
	implements AuthorityClient<AUTHORITY_ITEM_TYPE, P> {

	private static final String INCLUDE_DELETE_TRUE = Boolean.TRUE.toString();
	/*
	 * Basic CRUD proxied methods
	 */

	//(C)reate Item
	@Override
    public ClientResponse<Response> createItem(String vcsid, PoxPayloadOut poxPayloadOut) {
    	return getProxy().createItem(vcsid, poxPayloadOut.getBytes());
    }

    //(R)ead Item
    @Override
	public ClientResponse<String> readItem(String vcsid, String csid) {
    	return getProxy().readItem(vcsid, csid, INCLUDE_DELETE_TRUE);
    }
    
    @Override
    public ClientResponse<String> readItem(String vcsid, String csid, Boolean includeDeleted) {
    	return getProxy().readItem(vcsid, csid, includeDeleted.toString());
    }

    //(U)pdate Item
    @Override
	public ClientResponse<String> updateItem(String vcsid, String csid, PoxPayloadOut poxPayloadOut) {
    	return getProxy().updateItem(vcsid, csid, poxPayloadOut.getBytes());
    }

    //(D)elete Item
    @Override
	public ClientResponse<Response> deleteItem(String vcsid, String csid) {
    	return getProxy().deleteItem(vcsid, csid);
    }
    
    @Override
	public ClientResponse<AuthorityRefDocList> getReferencingObjects(
            String parentcsid,
            String itemcsid) {
    	return getProxy().getReferencingObjects(parentcsid, itemcsid, Boolean.TRUE.toString());
    }
    
    /**
     * Gets the item authority refs.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the item authority refs
     */
    @Override
	public ClientResponse<AuthorityRefList> getItemAuthorityRefs(String parentcsid, String csid) {
        return getProxy().getItemAuthorityRefs(parentcsid, csid);
    }
    
    /*
     * 
     */
    
    @Override
	public ClientResponse<String> readByName(String name) {
    	return getProxy().readByName(name, INCLUDE_DELETE_TRUE);
    }
    
    @Override
	public ClientResponse<String> readByName(String name, Boolean includeDeleted) {
    	return getProxy().readByName(name, includeDeleted.toString());
    }
    
    /*
     * Item subresource methods
     */
    
    /**
     * Read named item.
     *
     * @param vcsid the vcsid
     * @param shortId the shortIdentifier
     * @return the client response
     */
    @Override
	public ClientResponse<String> readNamedItem(String vcsid, String shortId) {
        return getProxy().readNamedItem(vcsid, shortId, INCLUDE_DELETE_TRUE);
    }

    @Override
	public ClientResponse<String> readNamedItem(String vcsid, String shortId, Boolean includeDeleted) {
        return getProxy().readNamedItem(vcsid, shortId, includeDeleted.toString());
    }

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    @Override
	public ClientResponse<String> readItemInNamedAuthority(String authShortId, String csid) {
        return getProxy().readItemInNamedAuthority(authShortId, csid, INCLUDE_DELETE_TRUE);
    }

    @Override
	public ClientResponse<String> readItemInNamedAuthority(String authShortId, String csid, Boolean includeDeleted) {
        return getProxy().readItemInNamedAuthority(authShortId, csid, includeDeleted.toString());
    }

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    @Override
	public ClientResponse<String> readNamedItemInNamedAuthority(String authShortId, String itemShortId) {
        return getProxy().readNamedItemInNamedAuthority(authShortId, itemShortId, INCLUDE_DELETE_TRUE);
    }

    @Override
	public ClientResponse<String> readNamedItemInNamedAuthority(String authShortId, String itemShortId, Boolean includeDeleted) {
        return getProxy().readNamedItemInNamedAuthority(authShortId, itemShortId, includeDeleted.toString());
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
    @Override
    public ClientResponse<AbstractCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords) {
        return getProxy().readItemList(inAuthority, partialTerm, keywords, INCLUDE_DELETE_TRUE);
    }

    @Override
    public ClientResponse<AbstractCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords, Boolean includeDeleted) {
        return getProxy().readItemList(inAuthority, partialTerm, keywords, includeDeleted.toString());
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

	@Override
	public ClientResponse<AbstractCommonList> readItemListForNamedAuthority(
			String specifier, String partialTerm, String keywords) {
        return getProxy().readItemListForNamedAuthority(specifier, partialTerm, keywords, INCLUDE_DELETE_TRUE);
	}

	@Override
	public ClientResponse<AbstractCommonList> readItemListForNamedAuthority(
			String specifier, 
			String partialTerm, 
			String keywords,
			Boolean includeDeleted) {
        return getProxy().readItemListForNamedAuthority(specifier, partialTerm, keywords, includeDeleted.toString());
	}
	
	/*
	 * Workflow related  methods
	 */
	
	@Override
    public ClientResponse<String> readItemWorkflow(String vcsid, String csid) {
    	return getProxy().readItemWorkflow(vcsid, csid);
    }
    
	@Override
    public ClientResponse<String> updateItemWorkflow(String vcsid, String csid, PoxPayloadOut xmlPayload) {
    	return getProxy().updateItemWorkflow(vcsid, csid, xmlPayload.getBytes());
    }
	
}
