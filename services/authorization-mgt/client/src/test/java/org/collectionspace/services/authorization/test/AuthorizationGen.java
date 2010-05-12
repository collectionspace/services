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
import java.util.List;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.EffectType;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;

/**
 *
 * @author 
 */
public class AuthorizationGen {

    final Logger logger = LoggerFactory.getLogger(AuthorizationGen.class);

    public void genPermissions() {
        PermissionsList pcList = new PermissionsList();
        ArrayList<Permission> apcList = new ArrayList<Permission>();
        pcList.setPermissions(apcList);

        Permission accPerm = buildCommonPermission("1", "accounts");
        apcList.add(accPerm);
        Permission dimPerm = buildCommonPermission("2", "dimensions");
        apcList.add(dimPerm);
        AbstractAuthorizationTestImpl.toFile(pcList, PermissionsList.class,
                AbstractAuthorizationTestImpl.testDataDir + "test-permissions.xml");
        logger.info("generated permissions to "
                + AbstractAuthorizationTestImpl.testDataDir + "test-permissions.xml");

    }

    private Permission buildCommonPermission(String id, String resourceName) {
        Permission perm = new Permission();
        perm.setCsid(id);
        perm.setResourceName(resourceName);
        perm.setEffect(EffectType.PERMIT);
        perm.setTenantId("1");
        ArrayList<PermissionAction> pas = new ArrayList<PermissionAction>();
        perm.setActions(pas);

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

    public void genPermissionsRoles() {
        PermissionsRolesList psrsl = new PermissionsRolesList();
        ArrayList<PermissionRole> prl = new ArrayList<PermissionRole>();
        prl.add(buildCommonPermissionRoles("1", "accounts"));
        prl.add(buildCommonPermissionRoles("2", "dimensions"));
        psrsl.setPermissionRoles(prl);
        AbstractAuthorizationTestImpl.toFile(psrsl, PermissionsRolesList.class,
                AbstractAuthorizationTestImpl.testDataDir + "test-permissions-roles.xml");
        logger.info("generated permissions-roles to "
                + AbstractAuthorizationTestImpl.testDataDir + "test-permissions-roles.xml");
    }

    private PermissionRole buildCommonPermissionRoles(String id, String resName) {

        PermissionRole pr = new PermissionRole();
        pr.setSubject(SubjectType.ROLE);

        List<PermissionValue> permValues = new ArrayList<PermissionValue>();
        pr.setPermissions(permValues);
        PermissionValue permValue = new PermissionValue();
        permValue.setPermissionId(id);
        permValue.setResourceName(resName);
        permValues.add(permValue);

        List<RoleValue> roleValues = new ArrayList<RoleValue>();
        RoleValue rv1 = new RoleValue();
        rv1.setRoleName("ROLE_USERS");
        rv1.setRoleId("1");
        roleValues.add(rv1);
        RoleValue rv2 = new RoleValue();
        rv2.setRoleName("ROLE_ADMINISTRATOR");
        rv2.setRoleId("2");
        roleValues.add(rv2);
        pr.setRoles(roleValues);

        return pr;

    }
}
