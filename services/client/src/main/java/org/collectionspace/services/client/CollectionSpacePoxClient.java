package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/*
 * <LT> = List type
 * <P> = Proxy type
 */
public interface CollectionSpacePoxClient<LT extends AbstractCommonList, P extends CollectionSpacePoxProxy<LT>>
	extends CollectionSpaceClient<LT, P> {
	/*
	 * Common service calls
	 */
	public ClientResponse<Response> create(PoxPayloadOut xmlPayload);
	
	/*
	 * GET resource with workflow 'deleted' state
	 */
    public ClientResponse<String> read(String csid);
    
    public ClientResponse<String> readIncludeDeleted(String csid, Boolean includeDeleted);    

    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload);
    
    /*
     * GET list with workflow 'deleted' state
     */
    public ClientResponse<LT> readIncludeDeleted(Boolean includeDeleted);

    public ClientResponse<LT> keywordSearchIncludeDeleted(String keywords, Boolean includeDeleted);
    
    public ClientResponse<LT> advancedSearchIncludeDeleted(String whereClause, Boolean includeDeleted);
}
