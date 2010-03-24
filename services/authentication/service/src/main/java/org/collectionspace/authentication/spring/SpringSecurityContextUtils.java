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
package org.collectionspace.authentication.spring;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import javax.security.auth.Subject;
import org.collectionspace.authentication.SecurityContextUtils;
import org.collectionspace.authentication.CSpaceTenant;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SpringSecurityContextUtils provides utilities to CSpace services runtime
 * @author 
 */
final public class SpringSecurityContextUtils extends SecurityContextUtils {
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

        ArrayList<String> tenants = new ArrayList<String>();
        Subject caller = null;
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        JaasAuthenticationToken jaasToken = null;
        if (authToken instanceof JaasAuthenticationToken) {
            jaasToken = (JaasAuthenticationToken) authToken;
            caller = (Subject) jaasToken.getLoginContext().getSubject();
        }
        //caller = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
        if (caller == null) {
            String msg = "security not enabled!";
            //TODO: find out why subject is not null
            //FIXME: if logger is loaded when authn comes up, use it
            //logger.warn(msg);
            System.err.println(msg);
            return tenants.toArray(new String[0]);
        }
        Set<Group> groups = null;
        groups = caller.getPrincipals(Group.class);
        if (groups != null && groups.size() == 0) {
            String msg = "no role(s)/tenant(s) found!";
            //TODO: find out why no roles / tenants found
            //FIXME: if logger is loaded when authn comes up, use it
            //logger.warn(msg);
            System.err.println(msg);
            return tenants.toArray(new String[0]);
        }
        for (Group g : groups) {
            if ("Tenants".equals(g.getName())) {
                Enumeration members = g.members();
                while (members.hasMoreElements()) {
                    CSpaceTenant tenant = (CSpaceTenant) members.nextElement();
                    tenants.add(tenant.getId());
                    //FIXME: if logger is loaded when authn comes up, use it
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("found tenant id=" + tenant.getId()
//                                + " name=" + tenant.getName());
//                    }
                }
            }
        }
        return tenants.toArray(new String[0]);
    }
}
