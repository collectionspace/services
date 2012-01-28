package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * CLT = List type
 * P = Proxy type
 */
public abstract class AbstractPoxServiceClientImpl<CLT extends AbstractCommonList, P extends CollectionSpacePoxProxy<CLT>>
	extends AbstractServiceClientImpl<CLT, PoxPayloadOut, String, P> 
	implements CollectionSpacePoxClient<CLT, P> {
	
    @Override
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return getProxy().create(xmlPayload.getBytes());
    }
		
    @Override
	public ClientResponse<String> read(String csid) {
        return getProxy().read(csid);
    }
    
    public ClientResponse<CLT> readList() {
    	CollectionSpaceProxy<CLT> proxy = (CollectionSpaceProxy<CLT>)getProxy();
    	return proxy.readList();
    }    
    
    @Override
    public ClientResponse<CLT> readIncludeDeleted(Boolean includeDeleted) {
    	CollectionSpacePoxProxy<CLT> proxy = getProxy();
    	return proxy.readIncludeDeleted(includeDeleted.toString());
    }
    
    @Override
	public ClientResponse<String> readIncludeDeleted(String csid, Boolean includeDeleted) {
        return getProxy().readIncludeDeleted(csid, includeDeleted.toString());
    }

    @Override
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return getProxy().update(csid, xmlPayload.getBytes());
    }
    

    @Override
    public ClientResponse<CLT> keywordSearchIncludeDeleted(String keywords, Boolean includeDeleted) {
        CollectionSpacePoxProxy<CLT> proxy = getProxy();
        return proxy.keywordSearchIncludeDeleted(keywords, includeDeleted.toString());
    }

    @Override
    public ClientResponse<CLT> advancedSearchIncludeDeleted(String whereClause, Boolean includeDeleted) {
        CollectionSpacePoxProxy<CLT> proxy = getProxy();
        return proxy.advancedSearchIncludeDeleted(whereClause, includeDeleted.toString());
    }

}
