/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: Deprecated classes that need to be updated
import org.jboss.resteasy.client.ProxyFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.conn.ssl.SSLContexts;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Private class for SSL support
 */
class HttpsTrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[]{};
	}

}

/**
 * Private class for JAX-RS authentication
 */
class Authenticator implements ClientRequestFilter {

    private final String user;
    private final String password;

    public Authenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        final String basicAuthentication = getBasicAuthentication();
        headers.add("Authorization", basicAuthentication);

    }

    private String getBasicAuthentication() {
    	String result = null;
        String token = this.user + ":" + this.password;
        try {
            result = "Basic " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Cannot encode with UTF-8", ex);
        }
        
        return result;
    }
}


/**
 * BaseServiceClient is an abstract base client of all service clients FIXME:
 * http://issues.collectionspace.org/browse/CSPACE-1684
 * @param <CommonListType> 
 * @param <ListType> 
 * 
 * CLT - List type
 * REQUEST_PT - Request payload type
 * RESPONSE_PT - Response payload type
 * P - Proxy type
 */
public abstract class AbstractServiceClientImpl<CLT, REQUEST_PT, RESPONSE_PT, P extends CollectionSpaceProxy<CLT>>
	implements CollectionSpaceClient<CLT, REQUEST_PT, RESPONSE_PT, P> {

    /** The logger. */
    static protected final Logger logger = LoggerFactory.getLogger(AbstractServiceClientImpl.class);
    /**
     * The character used to separate the words in a part label
     */
    public static final String PART_LABEL_SEPARATOR = "_";
    /** The Constant PART_COMMON_LABEL. */
    public static final String PART_COMMON_LABEL = "common";
    /** The properties. */
    private Properties properties = new Properties();
    /** The url. */
    private URL url;
    /** The http client. */
    private HttpClient httpClient;
    private org.apache.http.client.HttpClient httpClient4;

    /** The RESTEasy proxy */
    private P proxy;

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
    	return logger;
    }
    
    /**
     * Instantiates a new abstract service client impl.
     * @throws Exception 
     */
    public AbstractServiceClientImpl() throws Exception {
    	this(CollectionSpaceClient.DEFAULT_CLIENT_PROPERTIES_FILENAME);
    }
    
    /**
     * Instantiates a new abstract service client impl.
     * @throws Exception 
     */
    public AbstractServiceClientImpl(String propertiesFileName) throws Exception {
        setClientProperties(propertiesFileName);
    }
    
    /**
     * Instantiates a new abstract service client impl.
     * @throws Exception 
     */
    public AbstractServiceClientImpl(Properties properties) throws Exception {
        setClientProperties(properties, false);
    }
    
    /**
     * Helps initialize a new abstract service client impl instance.
     * @throws Exception 
     */
    private void init() throws Exception {
    	if (properties.isEmpty() == true) {
    		throw new Exception("Client connection properties are empty.  Cannot proceed.");
    	}
    	
    	try {
	        setupHttpClient();
	        setupHttpClient4(); // temp fix for CSPACE-6281
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage());
    	}
    	
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();        
    }

    /**
     * 
     * @return
     */
    abstract public String getServicePathComponent();
    
    /**
     * Returns a UTF-8 encode byte array from 'string'
     *
     * @return UTF-8 encoded byte array
     */
    protected byte[] getBytes(String string) {
    	byte[] result = null;
    	try {
			result = string.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			if (logger.isWarnEnabled() == true) {
				logger.warn(e.getMessage(), e);
			}
		}
		return result;
    }
    
    /*
     * Subclasses can override this method to return their AbstractCommonList subclass
     */
    protected Class<CLT> getCommonListType() {
    	return (Class<CLT>) AbstractCommonList.class;
    }
    
    /**
     * Gets the common part name.
     *
     * @return the common part name
     */
    @Override
    public String getCommonPartName() {
        return getCommonPartName(getServiceName());
    }

    /**
     * Gets the common part name.
     *
     * @param servicePathComponent
     *            the service path component
     * @return the common part name
     */
    protected String getCommonPartName(String commonPrefix) {
        return commonPrefix + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
    }

//    /**
//     * Gets the service path component.
//     *
//     * @return the service path component
//     */
//    abstract public String getServicePathComponent();


    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#getProperty
     * (java.lang.String)
     */
    @Override
    public String getProperty(String propName) {
        return properties.getProperty(propName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#setProperty
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void setProperty(String propName, String value) {
        properties.setProperty(propName, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#removeProperty
     * (java.lang.String)
     */
    @Override
    public Object removeProperty(String propName) {
        return properties.remove(propName);
    }

    /**
     * Prints the properties.
     */
    public void printProperties() {
        for (Object kobj : properties.keySet()) {
            String key = (String) kobj;
            String value = (String) properties.get(key);
            logger.debug("begin property name=" + key + " value=" + value );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#getBaseURL()
     */
    @Override
    public String getBaseURL() {
        return properties.getProperty(URL_PROPERTY);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#getHttpClient()
     */
    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public org.apache.http.client.HttpClient getHttpClient4() {
        return httpClient4;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.collectionspace.services.client.CollectionSpaceClient#useAuth()
     */
    @Override
    public boolean useAuth() {
        String auth = properties.getProperty(AUTH_PROPERTY);
        return Boolean.valueOf(auth);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.collectionspace.services.client.CollectionSpaceClient#useSSL()
     */
    @Override
    public boolean useSSL() {
        String ssl = properties.getProperty(SSL_PROPERTY);
        return Boolean.valueOf(ssl);
    }

    /**
     * readProperties reads properties from system class path as well as it
     * overrides properties made available using command line
     * @throws Exception 
     *
     * @exception RuntimeException
     */
    @Override
    public void setClientProperties(String clientPropertiesFilename) throws Exception {
    	Properties inProperties = Tools.loadProperties(clientPropertiesFilename, true);
        setClientProperties(inProperties, true);
    }
    
    @Override
    public void setClientProperties(Properties inProperties) throws Exception {
    	setClientProperties(inProperties, false);
    }

    /**
     * Set our instance's properties to the in coming values.  But if 'overrideWithSyste' param is set to true then
     * only use the incoming values if the values don't already exist as System properties.
     * 
     * @param inProperties
     * @throws Exception 
     */
    protected void setClientProperties(Properties inProperties, boolean overrideWithSystemValues) throws Exception {
        properties = Tools.filterPropertiesWithEnvVars(inProperties); // Look for environment variables and substitute values if found
        
        if (overrideWithSystemValues == true) {
	        String spec = System.getProperty(URL_PROPERTY);
	        if (spec != null && !"".equals(spec)) {
	            properties.setProperty(URL_PROPERTY, spec);
	        }
		
	        String auth = System.getProperty(AUTH_PROPERTY);
	        if (auth != null && !"".equals(auth)) {
	            properties.setProperty(AUTH_PROPERTY, auth);
	        }
	        String ssl = System.getProperty(SSL_PROPERTY);
	        if (ssl != null && !"".equals(ssl)) {
	            properties.setProperty(AUTH_PROPERTY, ssl);
	        }
	        String user = System.getProperty(USER_PROPERTY);
	        if (user != null && !"".equals(user)) {
	            properties.setProperty(USER_PROPERTY, user);
	        }
	        String password = System.getProperty(PASSWORD_PROPERTY);
	        if (password != null && !"".equals(password)) {
	            properties.setProperty(PASSWORD_PROPERTY, password);
	        }
	        String tenant = System.getProperty(TENANT_NAME_PROPERTY);
	        if (tenant != null && !"".equals(tenant)) {
	            properties.setProperty(TENANT_NAME_PROPERTY, tenant);
	        }
        }
        //
        // Verify the URL is well formed.
        //
        String urlString = properties.getProperty(URL_PROPERTY);
        try {
			url = new URL(urlString);
	        logger.debug("Client properties using url=" + url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.error(String.format("Found malformed URL property value of '%s' for client URL settings.", urlString));
			throw new RuntimeException(e.getMessage());
		}
        
        if (logger.isDebugEnabled()) {
            printProperties();
        }
        //
        // Now setup the connection.
        //
        init();
    }    

    /**
     * setupHttpClient sets up HTTP client for the service client the setup
     * process relies on the following properties URL_PROPERTY USER_PROPERTY
     * PASSWORD_PROPERTY AUTH_PROPERTY SSL_PROPERTY
     * 
     * @throws Exception 
     */
    @Override
    public void setupHttpClient() throws Exception {
    	try {
	        this.httpClient = new HttpClient();
	        if (useAuth()) {
	            String user = properties.getProperty(USER_PROPERTY);
	            String password = properties.getProperty(PASSWORD_PROPERTY);
	            if (logger.isDebugEnabled()) {
	                logger.debug("setupHttpClient() using url=" + url + " user="
	                        + user + " password=" + password);
	            }
	
	            httpClient.getState().setCredentials(
	                    new AuthScope(url.getHost(), url.getPort(),
	                    AuthScope.ANY_REALM),
	                    new UsernamePasswordCredentials(user, password));
	            // JAXRS client library requires HTTP preemptive authentication
	            httpClient.getParams().setAuthenticationPreemptive(true);
	            if (logger.isDebugEnabled()) {
	                logger.debug("setupHttpClient: set preemptive authentication");
	            }
	        } else {
	            if (logger.isDebugEnabled()) {
	                logger.debug("setupHttpClient() : no auth mode!");
	            }
	        }
    	} catch (Throwable e) {
    		throw new Exception("Could not setup an HTTP client as requested with ", e);
    	}
    }
    
    /*
     * This is a temp fix for RESTEasy upgrade in CSPACE-6281.  The long-term solution will be to use
     * the non-deprecated approach per the RESTEasy documentation.
     */
    public void setupHttpClient4() throws Exception {
    	try {
	        this.httpClient4 = new DefaultHttpClient();
	        if (useAuth()) {
	            String user = properties.getProperty(USER_PROPERTY);
	            String password = properties.getProperty(PASSWORD_PROPERTY);
	            if (logger.isDebugEnabled()) {
	                logger.debug("setupHttpClient() using url=" + url + " user="
	                        + user + " password=" + password);
	            }
	
	            httpClient.getState().setCredentials(
	                    new AuthScope(url.getHost(), url.getPort(),
	                    AuthScope.ANY_REALM),
	                    new UsernamePasswordCredentials(user, password));
	            // JAXRS client library requires HTTP preemptive authentication
	            httpClient.getParams().setAuthenticationPreemptive(true);
	            if (logger.isDebugEnabled()) {
	                logger.debug("setupHttpClient: set preemptive authentication");
	            }
	        } else {
	            if (logger.isDebugEnabled()) {
	                logger.debug("setupHttpClient() : no auth mode!");
	            }
	        }
    	} catch (Throwable e) {
    		throw new Exception("Could not setup an HTTP client as requested with ", e);
    	}
    }    

    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#isServerSecure
     * ()
     */
    @Override
    public boolean isServerSecure() {
        return Boolean.getBoolean("cspace.server.secure");
    }

    @Override
    public P getProxy() {
    	return proxy;
    }
    
    /**
     * allow to reset proxy as per security needs
     */
    @Override
	public void setProxy() throws Exception {
    	ResteasyClient client = null;
        String urlString = url.toString();
    	Class<P> proxyClass = this.getProxyClass();
    	
//    	if (useSSL()) {
//    		SSLContext sslcontext = SSLContexts.custom().useSSL().build();
//            sslcontext.init(null, new X509TrustManager[]{new HttpsTrustManager()}, new SecureRandom());
//            client = (ResteasyClient)ClientBuilder.newBuilder().sslContext(sslcontext).build();
//    	} else {
//        	client = (ResteasyClient)ClientBuilder.newClient();
//    	}
    	
    	client = new ResteasyClientBuilder().httpEngine(new URLConnectionEngine()).build();
    	
        if (useAuth()) {
            String user = properties.getProperty(USER_PROPERTY);
            String password = properties.getProperty(PASSWORD_PROPERTY);
        	client = client.register(new Authenticator(user, password));
        }
        
        proxy = client.target(urlString).proxy(proxyClass);
    }
    
    /**
     * allow to reset proxy as per security needs
     */
    @Deprecated
	public void _setProxy() {
    	Class<P> proxyClass = this.getProxyClass();
        if (useAuth()) {
            proxy = ProxyFactory.create(proxyClass,
                    getBaseURL(), new ApacheHttpClient4Executor(getHttpClient4()));
        } else {
        	proxy = ProxyFactory.create(proxyClass,
                    getBaseURL());
        }
    }

    @Override
	public void setAuth(boolean useAuth,
            String user, boolean useUser,
            String password, boolean usePassword) throws Exception {
        if (useAuth == true) {
            setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
            if (useUser) {
                setProperty(CollectionSpaceClient.USER_PROPERTY,
                        user);
            } else {
                removeProperty(CollectionSpaceClient.USER_PROPERTY);
            }
            if (usePassword) {
                setProperty(CollectionSpaceClient.PASSWORD_PROPERTY,
                        password);
            } else {
                removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
            }
        } else {
            removeProperty(CollectionSpaceClient.AUTH_PROPERTY);
        }
        
        try {
	        setupHttpClient();
	        setupHttpClient(); // temp fix for CSPACE-6281
        } catch (Exception e) {
    		throw new RuntimeException(e.getMessage());
        }
        
        setProxy();
    }
    
	/*
	 * 
	 * Common Proxied service calls
	 * 
	 */
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#delete(java.lang.String)
     */
    @Override
	public Response delete(String csid) {
        return getProxy().delete(csid);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.BlobProxy#getAuthorityRefs(java.lang.String)
     */
    @Override
	public Response getAuthorityRefs(String csid) { // Response.getEntity returns AuthorityRefList type
        return getProxy().getAuthorityRefs(csid);
    }
    
    @Override
	public Response getWorkflow(String csid) {
    	return getProxy().getWorkflow(csid);
    }
    
    @Override
	public Response updateWorkflowWithTransition(String csid, String workflowTransition) {
    	return getProxy().updateWorkflowWithTransition(csid, workflowTransition);
    }        
    
    /*
     * Because of how RESTEasy creates proxy classes, sub-interfaces will need to override
     * these methods with their specific "common" list return types.  Otherwise, only the info
     * in the AbstractCommonList type will be returned to the callers
     */

    
    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#readList(java
     * .lang.String, java.lang.String)
     */
    @Override
    public Response readList(Long pageSize,
    		Long pageNumber) {
        return getProxy().readList(pageSize, pageNumber);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.collectionspace.services.client.CollectionSpaceClient#readList(java
     * .lang.String, java.lang.String)
     */
    @Override
    public Response readList(String sortBy, Long pageSize,
            Long pageNumber) {
        return getProxy().readList(sortBy, pageSize, pageNumber);
    }
    
    @Override
    public String getTenantName() {
    	return this.getProperty(TENANT_ID_PROPERTY);
    }
}
