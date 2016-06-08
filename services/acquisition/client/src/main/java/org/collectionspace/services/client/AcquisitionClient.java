package org.collectionspace.services.client;

import org.collectionspace.services.acquisition.AcquisitionsCommon;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class AcquisitionClient extends AbstractCommonListPoxServiceClientImpl<AcquisitionProxy, AcquisitionsCommon> {
	public static final String SERVICE_NAME = "acquisitions";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	public AcquisitionClient() throws Exception {
		super();
	}

	public AcquisitionClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

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

}
