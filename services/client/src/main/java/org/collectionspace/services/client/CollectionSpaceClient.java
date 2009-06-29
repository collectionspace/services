package org.collectionspace.services.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CollectionSpaceClient {

    static final String AUTH_PROPERTY = "org.collectionspace.auth";
    static final String SSL_PROPERTY = "org.collectionspace.ssl";
    static final String URL_PROPERTY = "org.collectionspace.url";
    /*
    static final String URL_PROPERTY_SCHEME = "org.collectionspace.url.schme";
    static final String URL_PROPERTY_HOST = "org.collectionspace.url.host";
    static final String URL_PROPERTY_PORT = "org.collectionspace.url.port";
    static final String URL_PROPERTY_CONTEXT = "org.collectionspace.url.context";
     */
    private static final String HOST = "localhost";
    private static final int PORT = 8180;
    private static final int SSL_PORT = 8543;
    private static final String PATH = "/cspace-services/";
    private static final String DEFAULT_URL = "http://" +
            HOST + ":" + PORT + PATH;
    private static final String DEFAULT_SSL_URL = "https://" +
            HOST + ":" + SSL_PORT + PATH;
    private String baseURL = null;
    private HttpClient httpClient;
    private boolean useAuth;
    private boolean useSSL;
    final Logger logger = LoggerFactory.getLogger(CollectionSpaceClient.class);

    protected CollectionSpaceClient() {

        String url = System.getProperty(URL_PROPERTY, DEFAULT_URL);
        if(url != null){
            baseURL = url;
        }
        useAuth = Boolean.getBoolean(AUTH_PROPERTY);
        if(useAuth){
            httpClient = new HttpClient();
            httpClient.getState().setCredentials(
                    new AuthScope(HOST, PORT, AuthScope.ANY_REALM),
                    new UsernamePasswordCredentials("test", "test"));
            httpClient.getParams().setAuthenticationPreemptive(true);
            if(logger.isDebugEnabled()){
                logger.debug("set up httpClient for authentication");
            }
        }
        useSSL = Boolean.getBoolean(SSL_PROPERTY);
        if(logger.isDebugEnabled()){
            logger.debug("useSSL=" + useSSL);
        }
        baseURL = useSSL ? DEFAULT_SSL_URL : DEFAULT_URL;
        if(logger.isDebugEnabled()){
            logger.debug("using baseURL=" + baseURL);
        }
    }

    protected String getBaseURL() {
        return baseURL;
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected boolean useAuth() {
        return useAuth;
    }

    protected boolean useSSL() {
        return useSSL;
    }
}
