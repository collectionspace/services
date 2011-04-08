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
		
    public ClientResponse<String> read(String csid);

    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload);
    
    public ClientResponse<LT> readIncludeDeleted(Boolean includeDeleted);
}
