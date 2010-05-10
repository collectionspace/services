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
package org.collectionspace.services.client;


import java.util.ArrayList;
import java.util.Collection;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.RoleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class PermissionRoleFactory {

    static private final Logger logger = LoggerFactory.getLogger(PermissionRoleFactory.class);
        /**
     * create permRolerole instance
     * @param permId
     * @param roleValues array of role ids
     * @param userPermId
     * @param useRoleId
     * @return
     */
    public static PermissionRole createPermissionRoleInstance(PermissionValue pv,
            Collection<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        PermissionRole permRole = new PermissionRole();
        //service consume is not required to provide subject as it is determined
        //from URI used
//        permRole.setSubject(SubjectType.ROLE);
        if (usePermId) {
            ArrayList<PermissionValue> pvs = new ArrayList<PermissionValue>();
            pvs.add(pv);
            permRole.setPermissions(pvs);
        }
        if (useRoleId) {
            //FIXME is there a better way?
            ArrayList<RoleValue> rvas = new ArrayList<RoleValue>();
            for (RoleValue rv : rvs) {
                rvas.add(rv);
            }
            permRole.setRoles(rvas);
        }

        return permRole;
    }
}
