package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * LT - List type
 * ILT - Authority item list type
 * P - Proxy type
 */
public interface AuthorityClient<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE, P extends AuthorityProxy> 
	extends CollectionSpacePoxClient<AbstractCommonList, P> {

    /** The uri path element for items in an authority */
    public static final String ITEMS = "items";    //used to construct uri's in service paths for authorities.
    public static final String REFERENCED = "referenced";
    public static final String SHORT_IDENTIFIER = "shortIdentifier";
    public static final String IN_AUTHORITY = "inAuthority";
    public static final String TERM_DISPLAY_NAME = "termDisplayName";
    public static final String VOCAB_DISPLAY_NAME = "displayName";
    public static final String REF_NAME = "refName";
    
    public static final Boolean INCLUDE_DELETED_ITEMS = true;
    public static final Boolean INCLUDE_RELATIONS = true;
    public static final Boolean DONT_INCLUDE_RELATIONS = !INCLUDE_RELATIONS;
    public static final String MARK_IF_REFERENCED_QP = "markIfReferenced";

	/*
	 * Basic CRUD operations
	 */
	
    String getItemCommonPartName();
    
    // Get the inAuthorityCsid (the item's parent)
    String getInAuthority(AUTHORITY_ITEM_TYPE item);
    
    // Get the inAuthorityCsid (the item's parent)
    void setInAuthority(AUTHORITY_ITEM_TYPE item, String inAuthorityCsid);
	
    //(C)reate Item
    Response createItem(String vcsid, PoxPayloadOut poxPayloadOut);

    //(R)ead Item
    Response readItem(String vcsid, String csid);
    
    //(R)ead Item
    Response readItem(String vcsid, String csid, Boolean includeDeleted);

    //(R)ead Item
    Response readItem(String authShortId, String itemShortId, Boolean includeDeleted, Boolean includeRelations);

    //(U)pdate Item
    Response updateItem(String vcsid, String csid, PoxPayloadOut poxPayloadOut);
    
    //(U)pdate Item
    Response updateNamedItemInNamedAuthority(String authShortId, String itemShortId, PoxPayloadOut poxPayloadOut);

    //(D)elete Item
    Response deleteItem(String vcsid, String csid);
    
    //(D)elete Item
    Response deleteNamedItemInNamedAuthority(String authShortId, String itemShortId);

    
    // Get a list of objects that
    Response getReferencingObjects(
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
    public Response getItemAuthorityRefs(String parentcsid, String itemcsid);    
    
    /*
     * Synchronization methods
     */
    
    public Response syncByName(String name);
    
    public Response sync(String identifier);
    
    public boolean supportsSync();
    
    /*
     * READ/GET by name method
     */
    
    Response readByName(String name);
    
    Response readByName(String name, Boolean includeDeleted);
    
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
    public Response readNamedItem(String vcsid, String shortId);

    public Response readNamedItem(String vcsid, String shortId, Boolean includeDeleted);

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    public Response readItemInNamedAuthority(String authShortId, String csid);

    public Response readItemInNamedAuthority(String authShortId, String csid, Boolean includeDeleted);

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    public Response readNamedItemInNamedAuthority(String authShortId, String itemShortId);
    
    /**
     * Read a named item in a named authority.
     * 
     * @param authShortId
     * @param itemShortId
     * @param includeDeleted
     * @param includeRelations
     * @return
     */
    public Response readNamedItemInNamedAuthority(String authShortId, String itemShortId, Boolean includeDeleted, Boolean includeRelations);
    
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
    public Response readItemList(String inAuthority, String partialTerm, String keywords);

    public Response readItemList(String inAuthority, String partialTerm, String keywords, long pageSize, long pageNum);

    public Response readItemList(String inAuthority, String partialTerm, String keywords, Boolean includeDeleted);
    
    public Response readItemList(String inAuthority, String partialTerm, String keywords, Boolean includeDeleted,
    		long pageSize, long pageNum);

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
    public Response readItemListForNamedAuthority(String specifier, 
    		String partialTerm, String keywords);
    
    public Response readItemListForNamedAuthority(String specifier, 
    		String partialTerm, 
    		String keywords,
    		Boolean includeDeleted);
    
    /*
     * Workflow related methods
     */
    
    public Response readItemWorkflow(String vcsid, String csid);
    
    public Response updateItemWorkflowWithTransition(String vcsid, String csid, String workflowTransition);
    
    //
    // General utils
    //
    
    /*
     * Should return a valid XML payload for creating an authority instance 
     */
    public String createAuthorityInstance(String shortIdentifier, String displayName);
    
    public String createAuthorityItemInstance(String shortIdentifier, String displayName);
    
}
