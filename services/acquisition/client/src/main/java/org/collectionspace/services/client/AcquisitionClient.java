package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.acquisition.Acquisition;
import org.collectionspace.services.acquisition.AcquisitionList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class AcquisitionClient extends BaseServiceClient {


    /**
     *
     */
    private static final AcquisitionClient instance = new AcquisitionClient();
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
        acquisitionProxy = ProxyFactory.create(AcquisitionProxy.class, getBaseURL());
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static AcquisitionClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake()
     */
    public ClientResponse<AcquisitionList> readList() {
        return acquisitionProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake(java.lang.String)
     */
    public ClientResponse<Acquisition> read(String csid) {
        return acquisitionProxy.read(csid);
    }

    /**
     * @param intake
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#createIntake(org.collectionspace.hello.Intake)
     */
    public ClientResponse<Response> create(Acquisition intake) {
        return acquisitionProxy.create(intake);
    }

    /**
     * @param csid
     * @param intake
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#updateIntake(java.lang.Long, org.collectionspace.hello.Intake)
     */
    public ClientResponse<Acquisition> update(String csid, Acquisition intake) {
        return acquisitionProxy.update(csid, intake);
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
