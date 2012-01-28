package org.collectionspace.services.client;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/*
 * <LT> = List type
 * <PT> = Payload type
 * <P> = Proxy type
 */
public interface CollectionSpacePoxClient<LT extends AbstractCommonList, P extends CollectionSpacePoxProxy<LT>>
	extends CollectionSpaceClient<LT, PoxPayloadOut, String, P> {
	/*
	 * Common service calls
	 */
    
    public ClientResponse<String> readIncludeDeleted(String csid, Boolean includeDeleted);    

    /*
     * GET list with workflow 'deleted' state
     */
    public ClientResponse<LT> readIncludeDeleted(Boolean includeDeleted);

    public ClientResponse<LT> keywordSearchIncludeDeleted(String keywords, Boolean includeDeleted);
    
    public ClientResponse<LT> advancedSearchIncludeDeleted(String whereClause, Boolean includeDeleted);
}
