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
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 *
 */
public interface CollectionSpaceClient {

    /** The AUTH property. */
    String AUTH_PROPERTY = "cspace.auth";
    /** The PASSWORD property. */
    String PASSWORD_PROPERTY = "cspace.password";
    /** The SSL property. */
    String SSL_PROPERTY = "cspace.ssl";
    /** The URL property. */
    String URL_PROPERTY = "cspace.url";
    /** The USER property. */
    String USER_PROPERTY = "cspace.user";
    /** The tenant property */
    String TENANT_PROPERTY = "cspace.tenant";

    /**
     * Gets the proxy.
     *
     * @return the proxy
     */
    CollectionSpaceProxy getProxy();

    /**
     * Gets the base url.
     *
     * @return the base url
     */
    String getBaseURL();

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

    /**
     * Read list.
     *
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    public ClientResponse<AbstractCommonList> readList(
            String pageSize,
            String pageNumber);

    /**
     * Read list.
     *
     * @param sortBy the sort order
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    public ClientResponse<AbstractCommonList> readList(
            String sortBy,
            String pageSize,
            String pageNumber);

    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> delete(String csid);
}
