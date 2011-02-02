package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.acquisition.AcquisitionsCommonList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class AcquisitionClient extends AbstractServiceClientImpl {
	public static final String SERVICE_PATH_COMPONENT = "acquisitions"; //FIXME: REM - The JAX-RS proxy, client, and resource classes should ref this value
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_PATH_COMPONENT;

	/* (non-Javadoc)
     * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }
    // FIXME: Is the "instance" member still needed/used?
    /**
     *
     */
//    private static final AcquisitionClient instance = new AcquisitionClient();
    /**
     *
     */
    private AcquisitionProxy acquisitionProxy;

    /**
     *
     * Default constructor for IntakeClient class.
     *
     */
    public AcquisitionClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.acquisitionProxy;
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            acquisitionProxy = ProxyFactory.create(AcquisitionProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            acquisitionProxy = ProxyFactory.create(AcquisitionProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Is this method still needed/used?
     *
     * @return
     */
//    public static AcquisitionClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake()
     */
    public ClientResponse<AcquisitionsCommonList> readList() {
        return acquisitionProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake(java.lang.String)
     */
    public ClientResponse<String> read(String csid) {
        return acquisitionProxy.read(csid);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.CollectionObjectProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return acquisitionProxy.getAuthorityRefs(csid);
    }

    /**
     * @param intake
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#createIntake(org.collectionspace.hello.Intake)
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return acquisitionProxy.create(xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @param intake
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#updateIntake(java.lang.Long, org.collectionspace.hello.Intake)
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return acquisitionProxy.update(csid, xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#deleteIntake(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return acquisitionProxy.delete(csid);
    }
}
