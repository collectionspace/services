package org.collectionspace.services.client;

public abstract class CollectionSpaceClient {
	static final String URL_PROPERTY = "org.collectionspace.url";
	/*
	static final String URL_PROPERTY_SCHEME = "org.collectionspace.url.schme";
	static final String URL_PROPERTY_HOST = "org.collectionspace.url.host";
	static final String URL_PROPERTY_PORT = "org.collectionspace.url.port";
	static final String URL_PROPERTY_CONTEXT = "org.collectionspace.url.context";
	 */
	
	private static final String SCHEME = "http";
	private static final String HOST = "localhost";
	private static final String PORT = "8080";
	private static final String URI = "/CollectionSpace/nuxeo-rest";
	private static final String URL = SCHEME + "://" +
		HOST + ":" +
		PORT +
		URI;
	private String collectionSpaceURL = null;
	
	
	String getURL() {
		String result = collectionSpaceURL;
		
		if (collectionSpaceURL == null) {
			result = collectionSpaceURL = System.getProperty(URL_PROPERTY, URL);
		}
		
		return result;
	}
}
