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
package org.collectionspace.services.authorization.test;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.springframework.transaction.TransactionStatus;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author 
 */
public class AuthorizationSeedTest extends AbstractAuthorizationTestImpl {

    final Logger logger = LoggerFactory.getLogger(AuthorizationSeedTest.class);

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        setup();
        TransactionStatus status = beginTransaction("seedData");
        try {
            AuthorizationGen authzGen = new AuthorizationGen();
            authzGen.genPermissions();
            authzGen.genPermissionsRoles();
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
    }

    public void seedPermissions() throws Exception {

        PermissionsList pcList =
                (PermissionsList) fromFile(PermissionsList.class,
                AbstractAuthorizationTestImpl.testDataDir + "test-permissions.xml");
        logger.info("read permissions from "
                + AbstractAuthorizationTestImpl.testDataDir + "test-permissions.xml");
        PermissionsRolesList pcrList =
                (PermissionsRolesList) fromFile(PermissionsRolesList.class,
                AbstractAuthorizationTestImpl.testDataDir + "test-permissions-roles.xml");
        logger.info("read permissions-roles from "
                + AbstractAuthorizationTestImpl.testDataDir + "test-permissions.xml");
        AuthZ authZ = AuthZ.get();
        for (Permission p : pcList.getPermissions()) {
            if (logger.isDebugEnabled()) {
                logger.debug("adding permission for res=" + p.getResourceName());
            }
            for (PermissionRole pr : pcrList.getPermissionRoles()) {
                if (pr.getPermissions().get(0).getPermissionId().equals(p.getCsid())) {
//                    authZ.addPermissionsForUri(p, pr);
                }
            }
        }
    }
}
