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

    private AuthN() {
        //hardcoded initialization of a provider
        //FIXME initialize with the help of configuration meta data
        authnContext = new SpringAuthNContext();
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

    /**
     * getTenantIds returns a list of tenant ids the user is associated with
     * @return
     */
    public String[] getTenantIds() {
        return authnContext.getTenantIds();
    }

    public String getCurrentTenantId() {
        return authnContext.getCurrentTenantId();
    }

    public String getCurrentTenantName() {
        return authnContext.getCurrentTenantName();
    }

    /**
     * getTenants returns tenants associated with user
     * @see CSpaceTenant
     * @return
     */
    public CSpaceTenant[] getTenants() {
        return authnContext.getTenants();
    }
}
