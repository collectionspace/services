package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
//import org.jboss.resteasy.client.ClientResponse;

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
    
    public Response readIncludeDeleted(String csid, Boolean includeDeleted);    

    /*
     * GET list with workflow 'deleted' state
     */
    public Response readIncludeDeleted(Boolean includeDeleted);

    public Response keywordSearchIncludeDeleted(String keywords, Boolean includeDeleted);
    
    public Response advancedSearchIncludeDeleted(String whereClause, Boolean includeDeleted);    
}
