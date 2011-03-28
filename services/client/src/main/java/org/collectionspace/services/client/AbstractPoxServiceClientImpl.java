package org.collectionspace.services.client;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.workflow.WorkflowsCommon;
import org.jboss.resteasy.client.ClientResponse;

public abstract class AbstractPoxServiceClientImpl<T extends CollectionSpacePoxProxy> extends AbstractServiceClientImpl<T> 
		implements CollectionSpacePoxClient<T> {
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
