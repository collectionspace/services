package org.collectionspace.hello.services;

import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;

public abstract class CollectionSpaceResource {
    //replace WORKSPACE_UID for resource workspace
	static String CS_COLLECTIONOBJECT_WORKSPACE_UID = "5a37d40f-59c4-4d15-93ad-e0e6a0c33587";
	static String CS_PERSON_WORKSPACE_UID = "eae0d7b6-580a-45a3-a0f3-e25e980e03bb";
	
    //replace host if not running on localhost
    //static String CS_NUXEO_HOST = "173.45.234.217";
	static String CS_NUXEO_HOST = "localhost";
    static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";
    
    NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient(CS_NUXEO_URI);
        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");
        return nxClient;
    }
    
}
