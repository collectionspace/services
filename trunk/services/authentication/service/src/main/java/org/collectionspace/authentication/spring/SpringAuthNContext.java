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

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.spi.AuthNContext;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SpringAuthNContext provides utilities to CSpace services runtime
 * @author 
 */
final public class SpringAuthNContext extends AuthNContext {
    //private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";

    public String getUserId() {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        return authToken.getName();
    }

    /**
     * retrieve tenant ids from Jaas LoginContext
     * @return
     */
    @Override
    public String[] getTenantIds() {

        ArrayList<String> tenantList = new ArrayList<String>();
        CSpaceTenant[] tenants = getTenants();
        for (CSpaceTenant tenant : tenants) {
            tenantList.add(tenant.getId());
        }
        return tenantList.toArray(new String[0]);
    }

    @Override
    public String getCurrentTenantId() {
        //FIXME assumption in 1.0: each user is associated with a single tenant
        String[] tenantIds = getTenantIds();
        if (tenantIds.length < 1) {
            throw new IllegalStateException("No tenant associated with user=" + getUserId());
        }
        return getTenantIds()[0];
    }

    public CSpaceTenant[] getTenants() {
        List<CSpaceTenant> tenants = new ArrayList<CSpaceTenant>();
        Subject caller = getSubject();
        if (caller == null) {
            String msg = "Could not find Subject!";
            //TODO: find out why subject is not null
            //FIXME: if logger is loaded when authn comes up, use it
            //logger.warn(msg);
            System.err.println(msg);
            return tenants.toArray(new CSpaceTenant[0]);
        }
        Set<Group> groups = null;
        groups = caller.getPrincipals(Group.class);
        if (groups != null && groups.size() == 0) {
            String msg = "no role(s)/tenant(s) found!";
            //TODO: find out why no roles / tenants found
            //FIXME: if logger is loaded when authn comes up, use it
            //logger.warn(msg);
            System.err.println(msg);
            return tenants.toArray(new CSpaceTenant[0]);
        }
        for (Group g : groups) {
            if ("Tenants".equals(g.getName())) {
                Enumeration members = g.members();
                while (members.hasMoreElements()) {
                    CSpaceTenant tenant = (CSpaceTenant) members.nextElement();
                    tenants.add(tenant);
                    //FIXME: if logger is loaded when authn comes up, use it
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("found tenant id=" + tenant.getId()
//                                + " name=" + tenant.getName());
//                    }
                }
            }
        }
        return tenants.toArray(new CSpaceTenant[0]);
    }

    @Override
    public String getCurrentTenantName() {
        //FIXME assumption in 1.0: each user is associated with a single tenant
        CSpaceTenant[] tenants = getTenants();
        if (tenants.length < 1) {
            throw new IllegalStateException("No tenant associated with user=" + getUserId());
        }
        return getTenants()[0].getName();
    }

    public Subject getSubject() {
        Subject caller = null;
        //if Spring was not used....
        //caller = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);

        //FIXME the follow call should be protected with a privileged action
        //and must only be available to users with super privileges
        //Spring does not offer any easy mechanism
        //It is a bad idea to ship with a kernel user...kernel user should be
        //created at startup time perhaps and used it here
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        JaasAuthenticationToken jaasToken = null;
        if (authToken instanceof JaasAuthenticationToken) {
            jaasToken = (JaasAuthenticationToken) authToken;
            caller = (Subject) jaasToken.getLoginContext().getSubject();
        }
        return caller;
    }
}
