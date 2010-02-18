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

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.collectionspace.services.common.context.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseServiceClient is an abstract base client of all service clients
 */
public abstract class AbstractServiceClientImpl implements CollectionSpaceClient {

    protected final Logger logger = LoggerFactory.getLogger(AbstractServiceClientImpl.class);
    private Properties properties = new Properties();
    private URL url;
    private HttpClient httpClient;

	public String getCommonPartName() {
		return getCommonPartName(getServicePathComponent());
	}

	public String getCommonPartName(String servicePathComponent) {
		return servicePathComponent
		+ ServiceContext.PART_LABEL_SEPERATOR
		+ ServiceContext.PART_COMMON_LABEL;
	}

	abstract public String getServicePathComponent();
    
    protected AbstractServiceClientImpl() {
        readProperties();
        setupHttpClient();
    }

    @Override
    public String getProperty(String propName) {
        return properties.getProperty(propName);
    }

    @Override
    public void setProperty(String propName, String value) {
        properties.setProperty(propName, value);
    }

    @Override
    public Object removeProperty(String propName) {
        return properties.remove(propName);
    }

    public void printProperties() {
        for(Object kobj : properties.keySet()){
            String key = (String) kobj;
            logger.trace("begin property name=" + key + " value=" + properties.get(key));
        }
    }
    
    @Override
    public String getBaseURL() {
        return properties.getProperty(URL_PROPERTY);
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public boolean useAuth() {
        String auth = properties.getProperty(AUTH_PROPERTY);
        return Boolean.valueOf(auth);
    }

    @Override
    public boolean useSSL() {
        String ssl = properties.getProperty(SSL_PROPERTY);
        return Boolean.valueOf(ssl);
    }

    /**
     * readProperties reads properties from system class path as well
     * as it overrides properties made available using command line
     * @exception RuntimeException
     */
    private void readProperties() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = null;
        try{
            is = cl.getResourceAsStream("collectionspace-client.properties");
            properties.load(is);
            if(logger.isDebugEnabled()){
                printProperties();
            }
            String spec = System.getProperty(URL_PROPERTY);
            if(spec != null && !"".equals(spec)){
                properties.setProperty(URL_PROPERTY, spec);
            }

            spec = properties.getProperty(URL_PROPERTY);
            url = new URL(spec);
            if(logger.isInfoEnabled()){
                logger.info("readProperties() using url=" + url);
            }

            String auth = System.getProperty(AUTH_PROPERTY);
            if(auth != null && !"".equals(auth)){
                properties.setProperty(AUTH_PROPERTY, auth);
            }
            String ssl = System.getProperty(SSL_PROPERTY);
            if(ssl != null && !"".equals(ssl)){
                properties.setProperty(AUTH_PROPERTY, ssl);
            }
            String user = System.getProperty(USER_PROPERTY);
            if(user != null && !"".equals(user)){
                properties.setProperty(USER_PROPERTY, user);
            }
            String password = System.getProperty(PASSWORD_PROPERTY);
            if(password != null && !"".equals(password)){
                properties.setProperty(PASSWORD_PROPERTY, password);
            }
            if(logger.isDebugEnabled()){
                printProperties();
            }
        }catch(Exception e){
            logger.debug("Caught exception while reading properties", e);
            throw new RuntimeException(e);
        }finally{
            if(is != null){
                try{
                    is.close();
                }catch(Exception e){
                }
            }
        }
    }

    /**
     * setupHttpClient sets up HTTP client for the service client
     * the setup process relies on the following properties
     * URL_PROPERTY
     * USER_PROPERTY
     * PASSWORD_PROPERTY
     * AUTH_PROPERTY
     * SSL_PROPERTY
     */
    @Override
    public void setupHttpClient() {
        this.httpClient = new HttpClient();
        if(useAuth()){
            String user = properties.getProperty(USER_PROPERTY);
            String password = properties.getProperty(PASSWORD_PROPERTY);
            if(logger.isInfoEnabled()){
                logger.info("setupHttpClient() using url=" + url +
                        " user=" + user + " password=" + password);
            }

            httpClient.getState().setCredentials(
                    new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM),
                    new UsernamePasswordCredentials(user, password));
            //JAXRS client library requires HTTP preemptive authentication
            httpClient.getParams().setAuthenticationPreemptive(true);
            if(logger.isInfoEnabled()){
                logger.info("setupHttpClient: set preemptive authentication");
            }
        } else {
            if(logger.isInfoEnabled()){
                logger.info("setupHttpClient() : no auth mode!");
            }
        }
    }

    @Override
    public boolean isServerSecure() {
        return Boolean.getBoolean("cspace.server.secure");
    }
}
