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
package org.collectionspace.services.authorization.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.EffectType;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.testng.annotations.Test;

/**
 *
 * @author 
 */
public class AuthorizationGenTest extends AbstractAuthorizationTestImpl {

    final Logger logger = LoggerFactory.getLogger(AuthorizationGenTest.class);

    @Test
    public void genPermissions() {
        PermissionsList pcList = new PermissionsList();
        ArrayList<Permission> apcList = new ArrayList<Permission>();
        pcList.setPermission(apcList);

        Permission accPerm = buildCommonPermission("1", "accounts");
        apcList.add(accPerm);
        Permission coPerm = buildCommonPermission("2", "collectionobjects");
        apcList.add(coPerm);
        toFile(pcList, PermissionsList.class, "./target/test-permissions.xml");

    }

    private Permission buildCommonPermission(String id, String resourceName) {
        Permission perm = new Permission();
        perm.setCsid(id);
        perm.setResourceName(resourceName);
        perm.setEffect(EffectType.PERMIT);

        ArrayList<PermissionAction> pas = new ArrayList<PermissionAction>();
        perm.setAction(pas);

        PermissionAction pa = new PermissionAction();
        pa.setName(ActionType.CREATE);
        pas.add(pa);
        PermissionAction pa1 = new PermissionAction();
        pa1.setName(ActionType.READ);
        pas.add(pa1);
        PermissionAction pa2 = new PermissionAction();
        pa2.setName(ActionType.UPDATE);
        pas.add(pa2);
        PermissionAction pa3 = new PermissionAction();
        pa3.setName(ActionType.DELETE);
        pas.add(pa3);
        return perm;
    }

    @Test
    public void genPermissionsRoles() {
        PermissionsRolesList psrsl = new PermissionsRolesList();
        ArrayList<PermissionRole> prl = buildCommonPermissionRoles("1");
        prl.addAll(buildCommonPermissionRoles("2"));
        psrsl.setPermissionRole(prl);
        toFile(psrsl, PermissionsRolesList.class, "./target/test-permissions-roles.xml");
    }

    private ArrayList<PermissionRole> buildCommonPermissionRoles(String id) {
        ArrayList<PermissionRole> prl = new ArrayList<PermissionRole>();
        PermissionRole pr = new PermissionRole();
        pr.setPermissionId(id);
        //FIXME should using role id
        pr.setRoleId("ROLE_USERS");
        prl.add(pr);
        PermissionRole pr1 = new PermissionRole();
        pr1.setPermissionId(id);
        //FIXME shoudl use role id
        pr1.setRoleId("ROLE_ADMINISTRATOR");
        prl.add(pr1);
        return prl;
    }
}
