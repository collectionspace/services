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
package org.collectionspace.authentication.spring;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.spi.AuthNContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilities for accessing the Spring Security authentication context.
 */
public class SpringAuthNContext implements AuthNContext {

    /**
     * Returns the username of the authenticated user.
     *
     * @return the username
     */
    @Override
	public String getUserId() {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();

        if (authToken == null) {
            return AuthN.ANONYMOUS_USER;
        }

        return authToken.getName();
    }

    /**
     * Returns the authenticated CSpaceUser user.
     *
     * @return the user
     */
    @Override
	public CSpaceUser getUser() {
    	CSpaceUser result = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = null;
        if (authentication != null) {
            principal = authentication.getPrincipal();
            if (principal instanceof CSpaceUser ) {
            	result = (CSpaceUser) principal;
            }
        }

        return result;
    }

    /**
     * Returns the id of the primary tenant associated with the authenticated user.
     *
     * @return the tenant id
     */
    @Override
	public String getCurrentTenantId() {
    	String result = null;

    	CSpaceUser cspaceUser = getUser();
    	if (cspaceUser != null) {
            if (getCurrentTenant() != null) {
                result = getCurrentTenant().getId();
            }
    	} else {
	        String username = getUserId();
	        if (username.equals(AuthN.ANONYMOUS_USER)) {
	            result = AuthN.ANONYMOUS_TENANT_ID;
	        } else if (username.equals(AuthN.SPRING_ADMIN_USER)) {
	            result = AuthN.ADMIN_TENANT_ID;
	        }
    	}

    	return result;
    }

    /**
     * Returns the name of the primary tenant associated with the authenticated user.
     *
     * @return the tenant name
     */
    @Override
	public String getCurrentTenantName() {
        if (getUserId().equals(AuthN.ANONYMOUS_USER)) {
            return AuthN.ANONYMOUS_TENANT_NAME;
        }

        return getCurrentTenant().getName();
    }

    /**
     * Returns the primary tenant associated with the authenticated user.
     *
     * @return the tenant
     */
    @Override
	public CSpaceTenant getCurrentTenant() {
    	CSpaceTenant result = null;

    	CSpaceUser cspaceUser = getUser();
    	if (cspaceUser != null) {
    		result = getUser().getPrimaryTenant();
    	} else {
    		String username = getUserId();
	        if (username.equals(AuthN.ANONYMOUS_USER)) {
	            result = new CSpaceTenant(AuthN.ANONYMOUS_TENANT_ID, AuthN.ANONYMOUS_TENANT_NAME);
	        } else if (username.equals(AuthN.SPRING_ADMIN_USER)) {
	            result = new CSpaceTenant(AuthN.ADMIN_TENANT_ID, AuthN.ADMIN_TENANT_NAME);
	        }
    	}

    	return result;
    }
}
