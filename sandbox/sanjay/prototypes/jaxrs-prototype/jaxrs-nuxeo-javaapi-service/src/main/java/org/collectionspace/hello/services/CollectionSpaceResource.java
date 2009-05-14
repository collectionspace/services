package org.collectionspace.hello.services;


public abstract class CollectionSpaceResource {
    //replace WORKSPACE_UID for resource workspace
	static String CS_COLLECTIONOBJECT_WORKSPACE_UID = "5a37d40f-59c4-4d15-93ad-e0e6a0c33587";
    //sanjay 6c7881fe-54c5-486e-b144-a025dee3a484
    //demo eae0d7b6-580a-45a3-a0f3-e25e980e03bb
	static String CS_PERSON_WORKSPACE_UID = "6c7881fe-54c5-486e-b144-a025dee3a484";
	
    //replace host if not running on localhost
    //static String CS_NUXEO_HOST = "173.45.234.217";
	static String CS_NUXEO_HOST = "localhost";
    static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";

}
