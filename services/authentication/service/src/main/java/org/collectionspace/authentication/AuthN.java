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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.authentication;

import javax.sql.DataSource;

import org.collectionspace.authentication.spi.AuthNContext;
import org.collectionspace.authentication.spring.SpringAuthNContext;

/**
 * AuthN is a singleton to access various authentication related utilities
 * accessed by services runtime
 * @author 
 */
public class AuthN {

    /**
     * volatile is used here to assume about ordering (post JDK 1.5)
     */
    private static volatile AuthN self = new AuthN();
    private static DataSource dataSource = null;
    private AuthNContext authnContext;
    
    //
    // The "super" role has a predefined ID of "0" and a tenant ID of "0";
    //
    final public static String ROLE_ALL_TENANTS_MANAGER = "ALL_TENANTS_MANAGER";
    final public static String ROLE_ALL_TENANTS_MANAGER_ID = "0";
    final public static String ALL_TENANTS_MANAGER_TENANT_ID = "0";
    
    public static final String ADMIN_TENANT_ID = "0";
	public static final String ADMIN_TENANT_NAME = "tenant_admin";

    public static final String ANONYMOUS_TENANT_ID = "-1";
    public static final String ANONYMOUS_USER = "anonymous";
    public static final String ANONYMOUS_TENANT_NAME = ANONYMOUS_USER;
    
    public static final String SPRING_ADMIN_USER = "SPRING_ADMIN";
    public static final String SPRING_ADMIN_PASSWORD = "SPRING_ADMIN";
    
    public static final String TENANT_ID_QUERY_PARAM = "tid";
    public static final String TENANT_ID_PATH_PARAM = "tenantId";
    
    public static final String ROLE_SPRING_ADMIN_ID = "-1";
    public static final String ROLE_SPRING_ADMIN_NAME = "ROLE_SPRING_ADMIN";
    public static final String ROLE_SPRING_GROUP_NAME = "Spring Security Administrator";

    // Define a special account value for the tenantManager. Yes, this is a hack, but
    // less troublesome than the alternatives.
    public static final String TENANT_MANAGER_ACCT_ID = ALL_TENANTS_MANAGER_TENANT_ID;
    
    // Prefix for description of auto-generated permissions
	public static final String GENERATED_STR = "Generated "; // trailing space is significant

    private AuthN() {
        //hardcoded initialization of a provider
        //FIXME initialize with the help of configuration meta data
        authnContext = new SpringAuthNContext();
    }
    
    public boolean isSystemAdmin() {
    	boolean result = false;
    	
    	String currentUserId = this.getUserId();
    	if (currentUserId.equals(AuthN.SPRING_ADMIN_USER) || currentUserId.equals(AuthN.ADMIN_TENANT_NAME)) {
    		result = true;
    	}
    	
    	return result;
    }

    public final static AuthN get() {
        return self;
    }
    
    public static void setDataSource(DataSource dataSource) {
    	AuthN.dataSource = dataSource;
    }
    
    public static DataSource getDataSource() {
    	return AuthN.dataSource;
    }

    /**
     * getAuthn returns authentication utilities
     * @return
     */
    public AuthNContext getAuthNContext() {
        return authnContext;
    }

    /**
     * getUserId returns authenticated user's id (principal name)
     * @return
     */
    public String getUserId() {
        return authnContext.getUserId();
    }

    public String getCurrentTenantId() {
        return authnContext.getCurrentTenantId();
    }

    public String getCurrentTenantName() {
        return authnContext.getCurrentTenantName();
    }
}
