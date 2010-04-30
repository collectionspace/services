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

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.spi.CSpaceAuthorizationProvider;
import org.collectionspace.services.authorization.spi.CSpacePermissionEvaluator;
import org.collectionspace.services.authorization.spi.CSpacePermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

/**
 * SpringAuthorizationProvider Spring Security provider
 * @author 
 */
public class SpringAuthorizationProvider implements CSpaceAuthorizationProvider {

    final Log log = LogFactory.getLog(SpringPermissionEvaluator.class);
    @Autowired
    private MutableAclService providerAclService;
    @Autowired
    private PermissionEvaluator providerPermissionEvaluator;
    private SpringPermissionEvaluator permissionEvaluator;
    private SpringPermissionManager permissionManager;
    private String version = "1.0";

    public SpringAuthorizationProvider() {
        permissionManager = new SpringPermissionManager(this);
        permissionEvaluator = new SpringPermissionEvaluator(this);
    }

    MutableAclService getProviderAclService() {
        return providerAclService;
    }

    public void setProviderAclService(MutableAclService mutableAclService) {
         this.providerAclService = mutableAclService;
        if (log.isDebugEnabled()) {
            log.debug("mutableAclService set");
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getVersion() {
        return version;
    }

    PermissionEvaluator getProviderPermissionEvaluator() {
        return providerPermissionEvaluator;
    }

    public void setProviderPermissionEvaluator(PermissionEvaluator permEval) {
        this.providerPermissionEvaluator = permEval;
        if (log.isDebugEnabled()) {
            log.debug("permission evaluator set");
        }
    }

    @Override
    public CSpacePermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }

    @Override
    public CSpacePermissionManager getPermissionManager() {
        return permissionManager;
    }

    static ObjectIdentity mapResource(CSpaceResource res) {
        return new ObjectIdentityImpl(res.getType().toString(),
                Long.valueOf(res.getId().hashCode()));
    }

    static Sid[] mapPrincipal(String[] principals) {
        ArrayList<Sid> sids = new ArrayList<Sid>();
        for (String principal : principals) {
            sids.add(new GrantedAuthoritySid(principal));
        }
        return sids.toArray(new Sid[0]);
    }

    static Permission mapPermssion(CSpaceAction perm) {
        switch (perm) {
            case ADMIN:
                return BasePermission.ADMINISTRATION;
            case CREATE:
                return BasePermission.CREATE;
            case READ:
                return BasePermission.READ;
            case UPDATE:
                return BasePermission.WRITE;
            case DELETE:
                return BasePermission.DELETE;
        }
        return null;
    }
}
