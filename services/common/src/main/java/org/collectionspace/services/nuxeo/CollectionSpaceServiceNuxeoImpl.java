/**
 * 
 */
package org.collectionspace.services.nuxeo;

import org.collectionspace.services.nuxeo.NuxeoRESTClient;


/**
 * @author remillet
 *
 */
public abstract class CollectionSpaceServiceNuxeoImpl {

    //replace host if not running on localhost
    //static String CS_NUXEO_HOST = "173.45.234.217";
	static String CS_NUXEO_HOST = "localhost";
    static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";
	
    public NuxeoRESTClient getClient() {
		NuxeoRESTClient nxClient = new NuxeoRESTClient(CS_NUXEO_URI);
		
		nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
		nxClient.setBasicAuthentication("Administrator", "Administrator");
		
		return nxClient;
	}
}
