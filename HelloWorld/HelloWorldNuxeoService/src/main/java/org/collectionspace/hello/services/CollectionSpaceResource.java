package org.collectionspace.hello.services;

public interface CollectionSpaceResource {
    //replace WORKSPACE_UID for resource workspace
	static String CS_NUXEO_WORKSPACE_UID = "6c7881fe-54c5-486e-b144-a025dee3a484";
	static String CS_NUXEO_DEFAULT_REPOS = "default";
    //replace host if not running on localhost
    static String CS_NUXEO_HOST = "localhost";
    static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";
}
