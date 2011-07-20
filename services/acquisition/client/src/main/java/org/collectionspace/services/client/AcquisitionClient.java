package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * An AcquisitionClient.

 * @version $Revision:$
 */
public class AcquisitionClient extends AbstractCommonListPoxServiceClientImpl<AcquisitionProxy> {
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

}
