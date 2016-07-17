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

import java.util.ArrayList;
import java.util.List;

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
    public String getUserId() {
        String result = ANONYMOUS_USER;
        
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        
        if (authToken != null) {
            result = authToken.getName();
        }
        
        return result;
    }

    /**
     * Returns the authenticated user.
     * 
     * @return the user
     */
    public CSpaceUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        CSpaceUser user = (CSpaceUser) principal;
        
        return user;
    }

    /**
     * Returns the id of the primary tenant associated with the authenticated user.
     * 
     * @return the tenant id
     */
    public String getCurrentTenantId() {
        String username = getUserId();
        
        if (username.equals(ANONYMOUS_USER) || username.equals(SPRING_ADMIN_USER)) {
            return ANONYMOUS_TENANT_ID;
        }

        return getCurrentTenant().getId();
    }

    /**
     * Returns the name of the primary tenant associated with the authenticated user.
     * 
     * @return the tenant name
     */
    public String getCurrentTenantName() {
        if (getUserId().equals(ANONYMOUS_USER)) {
            return ANONYMOUS_TENANT_NAME;
        }

        return getCurrentTenant().getName();
    }

    /**
     * Returns the primary tenant associated with the authenticated user.
     * 
     * @return the tenant
     */
    public CSpaceTenant getCurrentTenant() {
        List<CSpaceTenant> tenants = getTenants();
        
        if (tenants.size() < 1) {
            throw new IllegalStateException("No tenant associated with user " + getUserId());
        }
        
        return tenants.get(0);
    }

    /**
     * Returns all tenants associated with the authenticated user.
     * 
     * @return a list of tenants
     */
    public List<CSpaceTenant> getTenants() {
        return new ArrayList<CSpaceTenant>(getUser().getTenants());
    }

    /**
     * Returns the ids of all tenants associated with the authenticated user.
     * 
     * @return a list of tenant ids
     */
    public List<String> getTenantIds() {
        List<CSpaceTenant> tenants = getTenants();
        List<String> tenantIds = new ArrayList<String>(tenants.size());
        
        for (CSpaceTenant tenant : tenants) {
            tenantIds.add(tenant.getId());
        }
        
        return tenantIds;
    }
}
