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
import java.util.List;
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
     * create permRolerole instance with permission as object and role as subject
     * @param pv permvalue
     * @param rvs roleValues
     * @param userPermId
     * @param useRoleId
     * @return
     */
    public static PermissionRole createPermissionRoleInstance(PermissionValue pv,
            List<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        PermissionRole permRole = new PermissionRole();
        //service consume is not required to provide subject as it is determined
        //from URI used
//        permRole.setSubject(SubjectType.ROLE);
        if (usePermId) {
            ArrayList<PermissionValue> pvs = new ArrayList<PermissionValue>();
            pvs.add(pv);
            permRole.setPermission(pvs);
        }
        if (useRoleId) {
            permRole.setRole(rvs);
        }

        return permRole;
    }


    /**
     * create permRolerole instance with role as object and permission as subject
     * @param rv roleValue
     * @param pvs permValues
     * @param userPermId
     * @param useRoleId
     * @return
     */
    public static PermissionRole createPermissionRoleInstance(RoleValue rv,
            List<PermissionValue> pvs,
            boolean usePermId,
            boolean useRoleId) {

        PermissionRole permRole = new PermissionRole();
        //service consume is not required to provide subject as it is determined
        //from URI used
//        permRole.setSubject(SubjectType.ROLE);
        if (useRoleId) {
            ArrayList<RoleValue> rvs = new ArrayList<RoleValue>();
            rvs.add(rv);
            permRole.setRole(rvs);
        }
        if (usePermId) {
            permRole.setPermission(pvs);
        }

        return permRole;
    }
}
