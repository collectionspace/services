package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

public interface CollectionSpacePoxClient<T extends CollectionSpacePoxProxy> extends CollectionSpaceClient<T> {
	/*
	 * Common service calls
	 */
	public ClientResponse<Response> create(PoxPayloadOut xmlPayload);
		
    public ClientResponse<String> read(String csid);

    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload);
}
