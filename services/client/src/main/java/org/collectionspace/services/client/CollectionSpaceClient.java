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

import javax.ws.rs.core.Response;
import org.apache.commons.httpclient.HttpClient;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;

/**
 *	LT - List Type
 *  REQUEST_PT - Request payload type
 *  RESPONSE_PT - Response payload type
 *	P - Proxy type
 */
public interface CollectionSpaceClient<CLT, REQUEST_TYPE, RESPONSE_TYPE, P extends CollectionSpaceProxy<CLT>> {

    public static final String AUTH_PROPERTY = "cspace.auth";
    public static final String PASSWORD_PROPERTY = "cspace.password";
    public static final String SSL_PROPERTY = "cspace.ssl";
    public static final String URL_PROPERTY = "cspace.url";
    public static final String USER_PROPERTY = "cspace.user";
    public static final String TENANT_PROPERTY = "cspace.tenant";

    /**
     * Gets the proxy.
     *
     * @return the proxy
     */
    P getProxy();

    Class<P> getProxyClass();
    
    /**
     * Gets the base url.
     *
     * @return the base url
     */
    String getBaseURL();
    
    /*
     * Returns the name of the service's common part type.
     */
    String getCommonPartName();
    
    String getServiceName();

    /**
     * Gets the http client.
     *
     * @return the http client
     */
    HttpClient getHttpClient();

    /**
     * Gets the property.
     *
     * @param propName the prop name
     * @return the property
     */
    String getProperty(String propName);

    /**
     * Removes the property.
     *
     * @param propName the prop name
     * @return the object
     */
    Object removeProperty(String propName);

    /**
     * Sets the property.
     *
     * @param propName the prop name
     * @param value the value
     */
    void setProperty(String propName, String value);

    /**
     * setupHttpClient sets up HTTP client for the service client
     * the setup process relies on the following properties
     * URL_PROPERTY
     * USER_PROPERTY
     * PASSWORD_PROPERTY
     * AUTH_PROPERTY
     * SSL_PROPERTY
     */
    void setupHttpClient();

    /**
     * setProxy for the client
     * might be useful to reset proxy (based on auth requirements) that is usually created at the time of
     * constructing a client
     */
    void setProxy();

    /**
     * setAuth sets up authentication properties based on given parameters
     * @param useAuth
     * @param user user name
     * @param useUser indicates using user name
     * @param password
     * @param usePassword indicates using password
     */
    void setAuth(boolean useAuth,
            String user, boolean useUser,
            String password, boolean usePassword);

    /**
     * Use auth.
     *
     * @return true, if successful
     */
    boolean useAuth();

    /**
     * Use ssl.
     *
     * @return true, if successful
     */
    boolean useSSL();

    /**
     * checks System property cspace.server.secure
     * @return boolean
     */
    boolean isServerSecure();
    
    /*
     * Common proxied service calls
     */

	public ClientResponse<Response> create(REQUEST_TYPE payload);
	
	public ClientResponse<RESPONSE_TYPE> read(String csid);

    public ClientResponse<RESPONSE_TYPE> update(String csid, REQUEST_TYPE payload);
	
    /**
     * Read list.
     *
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    public ClientResponse<CLT> readList(
    		Long pageSize,
    		Long pageNumber);
    
    public ClientResponse<CLT> readList();

    /**
     * Read list.
     *
     * @param sortBy the sort order
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    public ClientResponse<CLT> readList(
            String sortBy,
            Long pageSize,
            Long pageNumber);

    /**
     * Gets the workflow information
     *
     * @param csid the csid of the entity
     * @return the workflow
     */
    public ClientResponse<String> getWorkflow(String csid);
    
	public ClientResponse<String> updateWorkflow(String csid, PoxPayloadOut xmlPayload);
    
    /**
     * Gets the authority refs.
     *
     * @param csid the csid
     * @return the authority refs
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid);
    
    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> delete(String csid);
}
