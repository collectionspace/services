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
package org.collectionspace.services.authorization.spring;

import java.util.List;
import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.spi.CSpacePermissionEvaluator;

import org.collectionspace.services.authorization.CSpaceResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SpringPermissionEvaluator evaluates permissions in Spring Security
 * @author 
 */
public class SpringPermissionEvaluator implements CSpacePermissionEvaluator {

    final Log log = LogFactory.getLog(SpringPermissionEvaluator.class);  //FIXEME: REM - Use SLF4J interfaces instead of directly using Apache Commons Logging.
    private SpringAuthorizationProvider provider;

    SpringPermissionEvaluator(SpringAuthorizationProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean hasPermission(CSpaceResource res, CSpaceAction action) {
        Permission perm = SpringAuthorizationProvider.getPermission(action);
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Serializable objectIdId = SpringAuthorizationProvider.getObjectIdentityIdentifier(res);
        String objectIdType = SpringAuthorizationProvider.getObjectIdentityType(res);
        PermissionEvaluator eval = provider.getProviderPermissionEvaluator();
        
        debug(res, authToken, objectIdId, objectIdType, perm);
        return eval.hasPermission(authToken,
                objectIdId, objectIdType, perm);
    }
    
    private void debug(CSpaceResource res,
    		Authentication authToken,
    		Serializable objectIdId,
    		String objectIdType,
    		Permission perm) {
    	if (log.isTraceEnabled() == true) {
    		log.debug(this.getClass().getCanonicalName() + ":" + this);
    		String resourceTarget = "[" + res.getId() + "]" + " | " +
				"[" + "objectIdId: " + objectIdType + "(" + objectIdId + ")]";
    		System.out.println("PERMISSION CHECK FOR: " + resourceTarget);
	    	System.out.println("\tPrincipal: " + authToken.getName() +
	    			"\tTenant ID: " + res.getTenantId());
	    	System.out.println("\tRoles: " + authToken.getAuthorities());
	    	System.out.println("\tPermission Mask: " + perm.getMask() +
	    			" - Permission Pattern: " + perm.getPattern());
	    	System.out.println("");
    	}
    }
}
