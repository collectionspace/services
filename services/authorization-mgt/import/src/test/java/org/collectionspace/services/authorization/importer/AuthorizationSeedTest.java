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
package org.collectionspace.services.authorization.importer;

//import java.util.ArrayList;
//import java.util.List;
import org.collectionspace.services.authorization.generator.AuthorizationGen;
import org.collectionspace.services.authorization.importer.AbstractAuthorizationTestImpl;
import java.util.ArrayList;
import java.util.List;
import org.collectionspace.services.authorization.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.collectionspace.services.client.test.BaseServiceTest;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.springframework.transaction.TransactionStatus;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author 
 */
public class AuthorizationSeedTest extends AbstractAuthorizationTestImpl {

    final Logger logger = LoggerFactory.getLogger(AuthorizationSeedTest.class);
    final static String PERMISSION_FILE = "import-permissions.xml";
    final static String PERMISSION_ROLE_FILE = "import-permissions-roles.xml";

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        setup();
        TransactionStatus status = beginTransaction("seedData");
        try {
            AuthorizationGen authzGen = new AuthorizationGen();
            PermissionsList pl = authzGen.genPermissions();
            writePermissions(pl, PERMISSION_FILE);
            PermissionsRolesList prl = authzGen.genPermissionsRoles(pl);
            writePermissionRoles(prl, PERMISSION_ROLE_FILE);
            seedRoles();
            seedPermissions();
        } catch (Exception ex) {
            rollbackTransaction(status);
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        commitTransaction(status);
    }

    public void seedRoles() throws Exception {
        //Should this test really be empty?
    }

    public void seedPermissions() throws Exception {
        PermissionsList pcList =
                (PermissionsList) fromFile(PermissionsList.class, baseDir
                + AbstractAuthorizationTestImpl.importDataDir + PERMISSION_FILE);
        logger.info("read permissions from "
                + baseDir + AbstractAuthorizationTestImpl.importDataDir +  PERMISSION_FILE);
        PermissionsRolesList pcrList =
                (PermissionsRolesList) fromFile(PermissionsRolesList.class, baseDir
                + AbstractAuthorizationTestImpl.importDataDir + PERMISSION_ROLE_FILE);
        logger.info("read permissions-roles from "
                + baseDir + AbstractAuthorizationTestImpl.importDataDir +  PERMISSION_ROLE_FILE);
        AuthZ authZ = AuthZ.get();
        for (Permission p : pcList.getPermissions()) {
            if (logger.isDebugEnabled()) {
                logger.debug("adding permission for res=" + p.getResourceName());
            }
            for (PermissionRole pr : pcrList.getPermissionRoles()) {
                if (pr.getPermissions().get(0).getPermissionId().equals(p.getCsid())) {
                    addPermissionsForUri(p, pr);
                }
            }
        }
    }

    /**
     * addPermissionsForUri add permissions from given permission configuration
     * with assumption that resource is of type URI
     * @param permission configuration
     */
    //FIXME this method should be in the restful web service resource of authz
    private void addPermissionsForUri(Permission perm,
            PermissionRole permRole) throws PermissionException {
        List<String> principals = new ArrayList<String>();
        if (!perm.getCsid().equals(permRole.getPermissions().get(0).getPermissionId())) {
            throw new IllegalArgumentException("permission ids do not"
                    + " match for role=" + permRole.getRoles().get(0).getRoleName()
                    + " with permissionId=" + permRole.getPermissions().get(0).getPermissionId()
                    + " for permission with csid=" + perm.getCsid());
        }
        for (RoleValue roleValue : permRole.getRoles()) {
            principals.add(roleValue.getRoleName());
        }
        List<PermissionAction> permActions = perm.getActions();
        for (PermissionAction permAction : permActions) {
            CSpaceAction action = getAction(permAction.getName());
            URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(),
                    perm.getResourceName(), action);
            AuthZ.get().addPermissions(uriRes, principals.toArray(new String[0]));
        }
    }

    /**
     * getAction is a convenience method to get corresponding action for
     * given ActionType
     * @param action
     * @return
     */
    private CSpaceAction getAction(ActionType action) {
        if (ActionType.CREATE.equals(action)) {
            return CSpaceAction.CREATE;
        } else if (ActionType.READ.equals(action)) {
            return CSpaceAction.READ;
        } else if (ActionType.UPDATE.equals(action)) {
            return CSpaceAction.UPDATE;
        } else if (ActionType.DELETE.equals(action)) {
            return CSpaceAction.DELETE;
        } else if (ActionType.SEARCH.equals(action)) {
            return CSpaceAction.SEARCH;
        } else if (ActionType.ADMIN.equals(action)) {
            return CSpaceAction.ADMIN;
        } else if (ActionType.START.equals(action)) {
            return CSpaceAction.START;
        } else if (ActionType.STOP.equals(action)) {
            return CSpaceAction.STOP;
        }
        throw new IllegalArgumentException("action = " + action.toString());
    }
}
