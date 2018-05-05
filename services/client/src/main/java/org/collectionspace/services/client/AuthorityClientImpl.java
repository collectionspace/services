package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * P - Proxy type
 */
public abstract class AuthorityClientImpl<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE, P extends AuthorityProxy>
	extends AbstractPoxServiceClientImpl<AbstractCommonList, P, AUTHORITY_ITEM_TYPE>
	implements AuthorityClient<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE, P> {

	private static final String INCLUDE_DELETE_TRUE = Boolean.TRUE.toString();
	private static final String INCLUDE_RELATIONS_TRUE = Boolean.TRUE.toString();
	private static final String INCLUDE_RELATIONS_FALSE = Boolean.FALSE.toString();
	
	/*
	 * Basic CRUD proxied methods
	 */

	public AuthorityClientImpl(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	public AuthorityClientImpl() throws Exception {
		super();
	}

	//(C)reate Item
	@Override
    public Response createItem(String vcsid, PoxPayloadOut poxPayloadOut) {
    	return getProxy().createItem(vcsid, poxPayloadOut.getBytes());
    }

    //(R)ead Item
    @Override
	public Response readItem(String vcsid, String csid) {
    	return getProxy().readItem(vcsid, csid, INCLUDE_DELETE_TRUE, INCLUDE_RELATIONS_FALSE);
    }
    
    @Override
    public Response readItem(String vcsid, String csid, Boolean includeDeleted) {
    	return getProxy().readItem(vcsid, csid, includeDeleted.toString(), INCLUDE_RELATIONS_FALSE);
    }
    
    @Override
    public Response readItem(String vcsid, String csid, Boolean includeDeleted, Boolean includeRelations) {
    	return getProxy().readItem(vcsid, csid, includeDeleted.toString(), includeRelations.toString());
    }

    //(U)pdate Item
    @Override
	public Response updateItem(String vcsid, String csid, PoxPayloadOut poxPayloadOut) {
    	return getProxy().updateItem(vcsid, csid, poxPayloadOut.getBytes());
    }
    
    @Override
    public Response updateNamedItemInNamedAuthority(String authShortId, String itemShortId, PoxPayloadOut poxPayloadOut) {
    	return getProxy().updateNamedItemInNamedAuthority(authShortId, itemShortId, poxPayloadOut.getBytes());
    }

    //(D)elete Item
    @Override
	public Response deleteItem(String vcsid, String csid) {
    	return getProxy().deleteItem(vcsid, csid);
    }
    
    //(D)elete Item
    @Override    
    public Response deleteNamedItemInNamedAuthority(String authShortId, String itemShortId) {
    	return getProxy().deleteNamedItemInNamedAuthority(authShortId, itemShortId);
    }
    
    
    @Override
	public Response getReferencingObjects( // ClientResponse<AuthorityRefDocList>
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
	public Response getItemAuthorityRefs(String parentcsid, String csid) {
        return getProxy().getItemAuthorityRefs(parentcsid, csid);
    }
    
    /*
     * Synchronization methods
     */
    
    @Override
    public Response syncByName(String name) {
    	return getProxy().syncByName(name);
    }
    
    @Override
    public Response sync(String identifier) {
    	return getProxy().sync(identifier);
    }
    
    @Override
    public boolean supportsSync() {
    	boolean result = true;
    	
    	Response response = getProxy().sync("-1"); // Check to see if the Authority (in general) supports sync for any of its instances
    	try {
	    	int status = response.getStatus();
	    	if (status == Response.Status.FORBIDDEN.getStatusCode()) {
	    		result = false;
	    	}
    	} finally {
	    	response.close();
    	}
    	
    	return result;
    }
    
    /*
     * 
     */
    
    @Override
	public Response readByName(String name) {
    	return getProxy().readByName(name, INCLUDE_DELETE_TRUE);
    }
    
    @Override
	public Response readByName(String name, Boolean includeDeleted) {
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
	public Response readNamedItem(String vcsid, String shortId) {
        return getProxy().readNamedItem(vcsid, shortId, INCLUDE_DELETE_TRUE);
    }

    @Override
	public Response readNamedItem(String vcsid, String shortId, Boolean includeDeleted) {
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
	public Response readItemInNamedAuthority(String authShortId, String csid) {
        return getProxy().readItemInNamedAuthority(authShortId, csid, INCLUDE_DELETE_TRUE);
    }

    @Override
	public Response readItemInNamedAuthority(String authShortId, String csid, Boolean includeDeleted) {
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
	public Response readNamedItemInNamedAuthority(String authShortId, String itemShortId) {
        return getProxy().readNamedItemInNamedAuthority(authShortId, itemShortId, INCLUDE_DELETE_TRUE, INCLUDE_RELATIONS_FALSE);
    }

    @Override
	public Response readNamedItemInNamedAuthority(String authShortId, String itemShortId, Boolean includeDeleted, Boolean includeRelations) {
        return getProxy().readNamedItemInNamedAuthority(authShortId, itemShortId, includeDeleted.toString(), includeRelations.toString());
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
    public Response readItemList(String inAuthority, String partialTerm, String keywords) {
        return getProxy().readItemList(inAuthority, partialTerm, keywords, INCLUDE_DELETE_TRUE);
    }

    @Override
    public Response readItemList(String inAuthority, String partialTerm, String keywords, long pageSize, long pageNum) {
        return getProxy().readItemList(inAuthority, partialTerm, keywords, INCLUDE_DELETE_TRUE, pageSize, pageNum);
    }

    @Override
    public Response readItemList(String inAuthority, String partialTerm, String keywords, Boolean includeDeleted) {
        return getProxy().readItemList(inAuthority, partialTerm, keywords, includeDeleted.toString());
    }
    
    @Override
    public Response readItemList(String inAuthority, String partialTerm, String keywords, Boolean includeDeleted,
    		long pageSize, long pageNum) {
        return getProxy().readItemList(inAuthority, partialTerm, keywords, includeDeleted.toString(), pageSize, pageNum);
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
	public Response readItemListForNamedAuthority(
			String specifier, String partialTerm, String keywords) {
        return getProxy().readItemListForNamedAuthority(specifier, partialTerm, keywords, INCLUDE_DELETE_TRUE);
	}

	@Override
	public Response readItemListForNamedAuthority(
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
    public Response readItemWorkflow(String vcsid, String csid) {
    	return getProxy().readItemWorkflow(vcsid, csid);
    }
    
	@Override
    public Response updateItemWorkflowWithTransition(String vcsid, String csid, String workflowTransition) {
    	return getProxy().updateItemWorkflowWithTransition(vcsid, csid, workflowTransition);
    }
	
}
