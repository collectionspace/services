package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class IdClient extends AbstractServiceClientImpl<IdProxy> {

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
    
    public ClientResponse<String> readList() {
        return getProxy().readList();
    }

    public ClientResponse<String> read(String csid) {
        return getProxy().read(csid);
    }

    public ClientResponse<String> createId(String csid) {
        return getProxy().createId(csid);
    }
}
