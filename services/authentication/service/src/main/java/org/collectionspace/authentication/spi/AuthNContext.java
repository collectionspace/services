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
.
 */
package org.collectionspace.authentication.spi;

import java.util.List;

import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.CSpaceUser;

/**
 * Utilities for accessing the authentication context.
 */
public interface AuthNContext {
    
    public static final String ANONYMOUS_USER = "anonymous";
    public static final String ANONYMOUS_TENANT_ID = "-1";
    public static final String ANONYMOUS_TENANT_NAME = ANONYMOUS_USER;
    public static final String SPRING_ADMIN_USER = "SPRING_ADMIN";
    public static final String TENANT_ID_QUERY_PARAM = "tid";
    public static final String TENANT_ID_PATH_PARAM = "tenantId";

    /**
     * Returns the username of the authenticated user.
     * 
     * @return the username
     */
    public String getUserId();

    /**
     * Returns the authenticated user.
     * 
     * @return the user
     */
    public CSpaceUser getUser();

    /**
     * Returns the id of the primary tenant associated with the authenticated user.
     * 
     * @return the tenant id
     */
    public String getCurrentTenantId();

    /**
     * Returns the name of the primary tenant associated with the authenticated user.
     * 
     * @return the tenant name
     */
    public String getCurrentTenantName();
    
    /**
     * Returns the primary tenant associated with the authenticated user.
     * 
     * @return the tenant
     */
    public CSpaceTenant getCurrentTenant();

    /**
     * Returns all tenants associated with the authenticated user.
     * 
     * @return a list of tenants
     */
    public List<CSpaceTenant> getTenants();

    /**
     * Returns the ids of all tenants associated with the authenticated user.
     * 
     * @return a list of tenant ids
     */
    public List<String> getTenantIds();
}
