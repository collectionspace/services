package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.collectionspace.services.description.ServiceDescription;

/**
 * IDClient.
 *
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class IdClient extends AbstractServiceClientImpl<String, String, String, IdProxy> {

    public static final String SERVICE_NAME = "idgenerators";
    
    public IdClient() throws Exception {
		super();
	}

    public IdClient(String clientPropertiesFilename) throws Exception {
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
    
    @Override
    public Response create(String xmlPayload) {
        return getProxy().create(xmlPayload);
    }

    @Override
    public Response read(String csid) {
        return getProxy().read(csid);
    }
    
    @Override
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
		throw new UnsupportedOperationException("ID client does not support an update operation.");
	}
	
	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}
}
