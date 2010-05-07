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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.spi.CSpacePermissionEvaluator;

import org.collectionspace.services.authorization.CSpaceResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SpringPermissionEvaluator evaluates permissions in Spring Security
 * @author 
 */
public class SpringPermissionEvaluator implements CSpacePermissionEvaluator {

    final Log log = LogFactory.getLog(SpringPermissionEvaluator.class);
    private SpringAuthorizationProvider provider;

    SpringPermissionEvaluator(SpringAuthorizationProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean hasPermission(CSpaceResource res, CSpaceAction perm) {
        PermissionEvaluator eval = provider.getProviderPermissionEvaluator();
        Permission p = SpringAuthorizationProvider.mapPermission(perm);
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        return eval.hasPermission(authToken,
                Long.valueOf(res.getId().hashCode()),
                res.getType().toString(),
                p);
    }
}
