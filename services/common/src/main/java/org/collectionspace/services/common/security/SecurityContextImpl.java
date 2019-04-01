/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.common.security;

import javax.ws.rs.core.UriInfo;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.spi.AuthNContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author
 */
public class SecurityContextImpl implements SecurityContext {

    final Logger logger = LoggerFactory.getLogger(SecurityContextImpl.class);
    private String userId;
    private String currentTenantName;
    private String currentTenantId;
    
    private String getTenantId(UriInfo uriInfo) throws UnauthorizedException {
    	String result = AuthN.get().getCurrentTenantId();
    	
    	String userId = AuthN.get().getUserId();
        if (userId.equals(AuthN.ANONYMOUS_USER) == true) {
            //
            // If anonymous access is being attempted, then a tenant ID needs to be set as a query param
            //        	
        	if (uriInfo == null) {
        		String errMsg = "Anonymous access attempted with missing or invalid tenant ID query or path paramter. A null 'UriInfo' instance was passed into the service context constructor.";
        		logger.warn(errMsg);
        		throw new UnauthorizedException(errMsg);
        	}
        	
        	String tenantIdQueryParam = uriInfo.getQueryParameters().getFirst(AuthN.TENANT_ID_QUERY_PARAM);
        	String tenantPathParam = uriInfo.getPathParameters().getFirst(AuthN.TENANT_ID_PATH_PARAM);
        	if (tenantIdQueryParam == null && tenantPathParam == null) {
        		String errMsg = String.format("Anonymous access to '%s' attempted without a valid tenant ID query or path paramter.",
        				uriInfo.getPath());
        		logger.error(errMsg);
        		throw new UnauthorizedException(errMsg);
        	}
        	
	        result = tenantIdQueryParam != null ? tenantIdQueryParam : tenantPathParam; // If both have value, user the query param (not path) value
        }
        
        return result;
    }

    public SecurityContextImpl(UriInfo uriInfo) throws UnauthorizedException {
        userId = AuthN.get().getUserId();
        currentTenantId = getTenantId(uriInfo);               
        currentTenantName = AuthN.get().getCurrentTenantName();        
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getCurrentTenantId() {
        return currentTenantId;
    }

    @Override
    public String getCurrentTenantName() {
        return currentTenantName;
    }
}
