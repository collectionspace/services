package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * LT - List type
 * ILT - Authority item list type
 * P - Proxy type
 */
public interface AuthorityClient<AUTHORITY_ITEM_TYPE, P extends AuthorityProxy> 
	extends CollectionSpacePoxClient<AbstractCommonList, P> {

    /** The uri path element for items in an authority */
    public static String ITEMS = "items";    //used to construct uri's in service paths for authorities.

	/*
	 * Basic CRUD operations
	 */
	
    String getItemCommonPartName();
    
    // Get the inAuthorityCsid (the item's parent)
    String getInAuthority(AUTHORITY_ITEM_TYPE item);
	
    // Get the inAuthorityCsid (the item's parent)
    void setInAuthority(AUTHORITY_ITEM_TYPE item, String inAuthorityCsid);
	
    //(C)reate Item
    ClientResponse<Response> createItem(String vcsid, PoxPayloadOut poxPayloadOut);

    //(R)ead Item
    ClientResponse<String> readItem(String vcsid, String csid);
    
    //(R)ead Item
    ClientResponse<String> readItem(String vcsid, String csid, Boolean includeDeleted);    

    //(U)pdate Item
    ClientResponse<String> updateItem(String vcsid, String csid, PoxPayloadOut poxPayloadOut);

    //(D)elete Item
    ClientResponse<Response> deleteItem(String vcsid, String csid);
    
    // Get a list of objects that
    ClientResponse<AuthorityRefDocList> getReferencingObjects(
            String parentcsid,
            String itemcsid);
    /**
     * Get a list of objects that reference a given authority term.
     * 
     * @param parentcsid 
     * @param itemcsid 
     * @param csid
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getItemAuthorityRefs(String parentcsid, String itemcsid);    
    
    /*
     * 
     */
    
    ClientResponse<String> readByName(String name);
    
    ClientResponse<String> readByName(String name, Boolean includeDeleted);
    
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
    public ClientResponse<String> readNamedItem(String vcsid, String shortId);

    public ClientResponse<String> readNamedItem(String vcsid, String shortId, Boolean includeDeleted);

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItemInNamedAuthority(String authShortId, String csid);

    public ClientResponse<String> readItemInNamedAuthority(String authShortId, String csid, Boolean includeDeleted);

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    public ClientResponse<String> readNamedItemInNamedAuthority(String authShortId, String itemShortId);
    
    public ClientResponse<String> readNamedItemInNamedAuthority(String authShortId, String itemShortId, Boolean includeDeleted);
    
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
    public ClientResponse<AbstractCommonList> readItemList(String inAuthority, String partialTerm, String keywords);
    
    public ClientResponse<AbstractCommonList> readItemList(String inAuthority, String partialTerm, String keywords, Boolean includeDeleted);
    
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
    public ClientResponse<AbstractCommonList> readItemListForNamedAuthority(String specifier, 
    		String partialTerm, String keywords);
    
    public ClientResponse<AbstractCommonList> readItemListForNamedAuthority(String specifier, 
    		String partialTerm, 
    		String keywords,
    		Boolean includeDeleted);
    
    /*
     * Workflow related methods
     */
    
    public ClientResponse<String> readItemWorkflow(String vcsid, String csid);
    
    public ClientResponse<String> updateItemWorkflow(String vcsid, String csid, PoxPayloadOut workflowPayload);
}
