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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionSpaceClient is an abstract base client of all service clients
 */
public abstract class CollectionSpaceClient {

    public static final String USER_PROPERTY = "cspace.user";
    public static final String PASSWORD_PROPERTY = "cspace.password";
    public static final String AUTH_PROPERTY = "cspace.auth";
    public static final String SSL_PROPERTY = "cspace.ssl";
    public static final String URL_PROPERTY = "cspace.url";

    
    private static final String PATH = "/cspace-services/";

    protected final Logger logger = LoggerFactory.getLogger(CollectionSpaceClient.class);
    private Properties properties = new Properties();
    private URL url;
    private HttpClient httpClient;

    protected CollectionSpaceClient() {

        readProperties();

        if(useAuth()){
            httpClient = new HttpClient();
            String user = properties.getProperty(USER_PROPERTY);
            String password = properties.getProperty(PASSWORD_PROPERTY);
            if(logger.isDebugEnabled()){
                logger.debug("using user=" + user + " password=" + password);
            }
            httpClient.getState().setCredentials(
                    new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM),
                    new UsernamePasswordCredentials(user, password));
            httpClient.getParams().setAuthenticationPreemptive(true);
            if(logger.isDebugEnabled()){
                logger.debug("set up httpClient for authentication");
            }
        }
    }

    protected String getBaseURL() {
        return (String) properties.getProperty(URL_PROPERTY);
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected boolean useAuth() {
        String auth = properties.getProperty(AUTH_PROPERTY);
        return Boolean.valueOf(auth);
    }

    protected boolean useSSL() {
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
                for(Object kobj : properties.keySet()){
                    String key = (String) kobj;
                    logger.debug("begin property name=" + key + " value=" + properties.get(key));
                }
            }
            String spec = System.getProperty(URL_PROPERTY);
            if(spec != null && !"".equals(spec)){
                properties.setProperty(URL_PROPERTY, spec);
            }
            spec = properties.getProperty(URL_PROPERTY);
            url = new URL(spec);

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
                for(Object kobj : properties.keySet()){
                    String key = (String) kobj;
                    logger.debug("end property name=" + key + " value=" + properties.get(key));
                }
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
}
