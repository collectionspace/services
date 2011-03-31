package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/*
 * LT = List type
 * P = Proxy type
 */
public abstract class AbstractPoxServiceClientImpl<LT extends AbstractCommonList, P extends CollectionSpacePoxProxy<LT>>
	extends AbstractServiceClientImpl<LT, P> 
	implements CollectionSpacePoxClient<LT, P> {
	@Override
	public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return getProxy().create(xmlPayload.getBytes());
    }
		
    @Override
	public ClientResponse<String> read(String csid) {
        return getProxy().read(csid);
    }

    @Override
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return getProxy().update(csid, xmlPayload.getBytes());
    }
}
