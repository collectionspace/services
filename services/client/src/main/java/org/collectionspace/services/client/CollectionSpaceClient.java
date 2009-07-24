package org.collectionspace.services.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CollectionSpaceClient {

    static final String USER_PROPERTY = "cspace.user";
    static final String PASSWORD_PROPERTY = "cspace.password";
    static final String AUTH_PROPERTY = "cspace.auth";
    static final String SSL_PROPERTY = "cspace.ssl";
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

        String urlProperty = System.getProperty(URL_PROPERTY, DEFAULT_URL);
        if(urlProperty != null){
            baseURL = urlProperty;
        }
        useAuth = Boolean.getBoolean(AUTH_PROPERTY);
        if(useAuth){
            httpClient = new HttpClient();
            String user = System.getProperty(USER_PROPERTY);
            if(user == null || "".equals(user)){
                user = "test";
            }
            String password = System.getProperty(PASSWORD_PROPERTY);
            if(password == null || "".equals(password)){
                password = "test";
            }
            if(logger.isDebugEnabled()){
                logger.debug("using user=" + user + " password=" + password);
            }
            httpClient.getState().setCredentials(
                    new AuthScope(HOST, PORT, AuthScope.ANY_REALM),
                    new UsernamePasswordCredentials(user, password));
            httpClient.getParams().setAuthenticationPreemptive(true);
            if(logger.isDebugEnabled()){
                logger.debug("set up httpClient for authentication");
            }
        }
        useSSL = Boolean.getBoolean(SSL_PROPERTY);
        if(logger.isDebugEnabled()){
            logger.debug("useSSL=" + useSSL);
        }
        
        // if the urlProperty prop is set, then it overrides.
        if (urlProperty == null) {
        	baseURL = useSSL ? DEFAULT_SSL_URL : DEFAULT_URL;
        }
        
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
