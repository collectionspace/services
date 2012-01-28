package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

/**
 * IDClient.
 *
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class IdClient extends AbstractServiceClientImpl<String, String, String, IdProxy> {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return "idgenerators";
    }

    @Override
    public String getServiceName() {
        return null; //FIXME: REM - See http://issues.collectionspace.org/browse/CSPACE-3497
    }

    @Override
    public Class<IdProxy> getProxyClass() {
        return IdProxy.class;
    }

    /*
     * Proxied service calls
     */
    
    // Operations on ID Generators
    
    public ClientResponse<Response> create(String xmlPayload) {
        return getProxy().create(xmlPayload);
    }

    public ClientResponse<String> read(String csid) {
        return getProxy().read(csid);
    }
    
    public ClientResponse<String> readList() {
        return getProxy().readList();
    }
    
    @Override
    public ClientResponse<Response> delete(String csid) {
        return getProxy().delete(csid);
    }
    
    // Operations on IDs

    public ClientResponse<String> createId(String csid) {
        return getProxy().createId(csid);
    }

	@Override
	public ClientResponse<String> update(String csid, String payload) {
		throw new UnsupportedOperationException("ID client does not support an update operation.");	}
}
