package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class AcquisitionClient extends AbstractPoxServiceClientImpl<AbstractCommonList, AcquisitionProxy> {
	public static final String SERVICE_NAME = "acquisitions";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<AcquisitionProxy> getProxyClass() {
		// TODO Auto-generated method stub
		return AcquisitionProxy.class;
	}

	/*
	 * Proxied service calls.
	 */
	
    /**
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return getProxy().readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake(java.lang.String)
     */
    @Override
	public ClientResponse<String> read(String csid) {
        return getProxy().read(csid);
    }
}
