package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class IdClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "idgenerators";
    }
    // FIXME: Is the "instance" member still needed/used?
    /**
     *
     */
//    private static final AcquisitionClient instance = new AcquisitionClient();
    /**
     *
     */
    private IdProxy idProxy;

    /**
     *
     * Default constructor for IntakeClient class.
     *
     */
    public IdClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.idProxy;
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            idProxy = ProxyFactory.create(IdProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            idProxy = ProxyFactory.create(IdProxy.class,
                    getBaseURL());
        }
    }

    public ClientResponse<String> readList() {
        return idProxy.readList();
    }

    public ClientResponse<String> read(String csid) {
        return idProxy.read(csid);
    }

    public ClientResponse<String> createId(String csid) {
        return idProxy.createId(csid);
    }

}
