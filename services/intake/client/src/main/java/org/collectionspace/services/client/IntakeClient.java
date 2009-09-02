package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.intake.IntakeList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A IntakeClient.

 * @version $Revision:$
 */
public class IntakeClient extends BaseServiceClient {


    /**
     *
     */
    private static final IntakeClient instance = new IntakeClient();
    /**
     *
     */
    private IntakeProxy intakeProxy;

    /**
     *
     * Default constructor for IntakeClient class.
     *
     */
    public IntakeClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        intakeProxy = ProxyFactory.create(IntakeProxy.class, getBaseURL());
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static IntakeClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake()
     */
    public ClientResponse<IntakeList> readList() {
        return intakeProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake(java.lang.String)
     */
    public ClientResponse<Intake> read(String csid) {
        return intakeProxy.read(csid);
    }

    /**
     * @param intake
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#createIntake(org.collectionspace.hello.Intake)
     */
    public ClientResponse<Response> create(Intake intake) {
        return intakeProxy.create(intake);
    }

    /**
     * @param csid
     * @param intake
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#updateIntake(java.lang.Long, org.collectionspace.hello.Intake)
     */
    public ClientResponse<Intake> update(String csid, Intake intake) {
        return intakeProxy.update(csid, intake);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#deleteIntake(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return intakeProxy.delete(csid);
    }
}
