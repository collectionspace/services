package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

/**
 * IDClient.
 *
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class IdClient extends AbstractServiceClientImpl<String, String, String, IdProxy> {

    public static final String SERVICE_NAME = "idgenerators";
    
    public IdClient() {
		super();
	}

    public IdClient(String clientPropertiesFilename) {
		super(clientPropertiesFilename);
	}

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
    
    public Response create(String xmlPayload) {
        return getProxy().create(xmlPayload);
    }

    public Response read(String csid) {
        return getProxy().read(csid);
    }
    
    public Response readList() {
        return getProxy().readList();
    }
    
    @Override
    public Response delete(String csid) {
        return getProxy().delete(csid);
    }
    
    // Operations on IDs

    public Response createId(String csid) {
        return getProxy().createId(csid);
    }

	@Override
	public Response update(String csid, String payload) {
		throw new UnsupportedOperationException("ID client does not support an update operation.");	}
}
