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
import org.apache.commons.httpclient.HttpClient;

/**
 *
 */
public interface CollectionSpaceClient {
    String AUTH_PROPERTY = "cspace.auth";
    String PASSWORD_PROPERTY = "cspace.password";
    String SSL_PROPERTY = "cspace.ssl";
    String URL_PROPERTY = "cspace.url";
    String USER_PROPERTY = "cspace.user";

    String getBaseURL();

    HttpClient getHttpClient();

    String getProperty(String propName);

    Object removeProperty(String propName);

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

    boolean useAuth();

    boolean useSSL();

    /**
     * checks System property cspace.server.secure
     * @return
     */
    boolean isServerSecure();

}
