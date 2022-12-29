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

import java.util.Properties;

import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.collectionspace.services.description.ServiceDescription;

/**
 *	LT - List Type
 *  REQUEST_PT - Request payload type
 *  RESPONSE_PT - Response payload type
 *	P - Proxy type
 */
public interface CollectionSpaceClient<CLT, REQUEST_TYPE, RESPONSE_TYPE, P extends CollectionSpaceProxy<CLT>> {

	public final static String CSID_PATH_PARAM_VAR = "{csid}";
    public final static String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core";
    
    public final static String COLLECTIONSPACE_CORE_CSID = "csid";
    public final static String COLLECTIONSPACE_CORE_TENANTID = "tenantId";
    public final static String CORE_TENANTID = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_TENANTID;
    
    public final static String COLLECTIONSPACE_CORE_URI = "uri";
    public final static String CORE_URI = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_URI;    
    
    public final static String COLLECTIONSPACE_CORE_REFNAME = "refName";
    public final static String CORE_REFNAME = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_REFNAME;
    
    public final static String COLLECTIONSPACE_CORE_CREATED_AT = "createdAt";
    public final static String CORE_CREATED_AT = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_CREATED_AT;
    
    public final static String COLLECTIONSPACE_CORE_UPDATED_AT = "updatedAt";
    public final static String CORE_UPDATED_AT = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_UPDATED_AT;
    
    public final static String COLLECTIONSPACE_CORE_CREATED_BY = "createdBy";
    public final static String CORE_CREATED_BY = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_CREATED_BY;

    public final static String COLLECTIONSPACE_CORE_UPDATED_BY = "updatedBy";
    public final static String CORE_UPDATED_BY = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_UPDATED_BY;

    public final static String COLLECTIONSPACE_CORE_WORKFLOWSTATE = "workflowState";
    public final static String CORE_WORKFLOWSTATE = COLLECTIONSPACE_CORE_SCHEMA + ":" + COLLECTIONSPACE_CORE_WORKFLOWSTATE;
    
    public static final String DEFAULT_CLIENT_PROPERTIES_FILENAME = "collectionspace-client.properties";
    public static final String SAS_CLIENT_PROPERTIES_FILENAME = "sas-collectionspace-client.properties";

    public static final String URL_PROPERTY = "cspace.url";
    public static final String USER_PROPERTY = "cspace.user";
    public static final String PASSWORD_PROPERTY = "cspace.password";
    public static final String SSL_PROPERTY = "cspace.ssl";
    public static final String AUTH_PROPERTY = "cspace.auth";
    public static final String TENANT_NAME_PROPERTY = "cspace.tenant";
    public static final String TENANT_ID_PROPERTY = "cspace.tenantID";
    public static final String TEST_SAS_PROPERTY = "cspace.testSAS";
    
    // JAX-RS path for getting service description meta information
	public static final String SERVICE_DESCRIPTION_PATH = "description";
	
    /**
     * The character used to separate the words in a part label
     */
    public static final String PART_LABEL_SEPARATOR = "_";
    /** The Constant PART_COMMON_LABEL. */
    public static final String PART_COMMON_LABEL = "common";
    //
    // Profile schema name suffixes and extension suffixes
    //
	public static final String BOTGARDEN_PROFILE_NAME = "botgarden";
	public static final String ANTHROPOLOGY_PROFILE_NAME = "anthro";
    public final static String LHMC_PROFILE_NAME = "lhmc";
    public final static String FINEART_PROFILE_NAME = "fcart";
    public final static String HERBARIUM_PROFILE_NAME = "herbarium";
    //
    // Profile schema name suffixes and extension suffixes
    //
    public final static String NATURALHISTORY_EXTENSION_NAME = "naturalhistory";
    public final static String NATURALHISTORY_EXT_EXTENSION_NAME = "naturalhistory_extension";
    public final static String VARIABLEMEDIA_EXTENSION_NAME = "variablemedia";

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
     * @throws Exception 
     */
    void setupHttpClient() throws Exception;

    /**
     * setProxy for the client
     * might be useful to reset proxy (based on auth requirements) that is usually created at the time of
     * constructing a client
     * @throws Exception 
     */
    void setProxy() throws Exception;

    /**
     * setAuth sets up authentication properties based on given parameters
     * @param useAuth
     * @param user user name
     * @param useUser indicates using user name
     * @param password
     * @param usePassword indicates using password
     * @throws Exception 
     */
    void setAuth(boolean useAuth,
            String user, boolean useUser,
            String password, boolean usePassword) throws Exception;

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

    public ServiceDescription getServiceDescription();
    
	public Response create(REQUEST_TYPE payload);
	
	public Response read(String csid);

    public Response update(String csid, REQUEST_TYPE payload);
	
    /**
     * Read list.
     *
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    public Response readList(
    		Long pageSize,
    		Long pageNumber);
    
    public Response readList(); // Formally, ClientResponse<CLT>

    /**
     * Read list.
     *
     * @param sortBy the sort order
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    public Response readList(
            String sortBy,
            Long pageSize,
            Long pageNumber);

    /**
     * Gets the workflow information
     *
     * @param csid the csid of the entity
     * @return the workflow
     */
    public Response getWorkflow(String csid);
    
	public Response updateWorkflowWithTransition(String csid, String workflowTransition);
    
    /**
     * Gets the authority refs.
     *
     * @param csid the csid
     * @return the authority refs
     */
    public Response getAuthorityRefs(String csid); // Response.getEntity returns AuthorityRefList type
    
    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    public Response delete(String csid);

    /**
     * Uses a properties files to set the url and credentials for an HTTP connection.
     * 
     * @param clientPropertiesFilename
     * @throws Exception 
     */
	public void setClientProperties(String clientPropertiesFilename) throws Exception;
	
	public void setClientProperties(Properties clientProperties) throws Exception;
	
	public String getTenantName();
}
