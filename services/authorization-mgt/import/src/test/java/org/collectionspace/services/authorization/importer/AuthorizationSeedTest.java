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

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        TransactionStatus status = null;
        try {
            AuthorizationGen authzGen = new AuthorizationGen();
            String tenantBindingFile = getTenantBindingFile();
            authzGen.initialize(tenantBindingFile);
            authzGen.createDefaultServicePermissions();
            //create default role(s) for the tenant and assign permissions
            authzGen.createDefaultPermissionsRoles();
            String exportDir = getExportDir();
            authzGen.exportPermissions(exportDir + PERMISSION_FILE);
            authzGen.exportPermissionRoles(exportDir + PERMISSION_ROLE_FILE);
            if (logger.isDebugEnabled()) {
                logger.debug("authroization generation completed ");
            }
            status = beginTransaction("seedData");
            AuthorizationSeed authzSeed = new AuthorizationSeed();
            String importDir = getImportDir();
            authzSeed.seedPermissions(importDir + PERMISSION_FILE,
                    importDir + PERMISSION_ROLE_FILE);
            if (logger.isDebugEnabled()) {
                logger.debug("authroization seeding completed ");
            }
        } catch (Exception ex) {
            if (status != null) {
                rollbackTransaction(status);
            }
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
            throw new RuntimeException(ex);
        } finally {
            if (status != null) {
                commitTransaction(status);
            }
        }
    }

    private String getTenantBindingFile() {
        String tenantBindingFile = System.getProperty("tenantbindings");
        if (tenantBindingFile == null || tenantBindingFile.isEmpty()) {
            throw new IllegalStateException("tenantbindings are required."
                    + " System property tenantbindings is missing or empty");
        }
        return tenantBindingFile;
    }

    private String getImportDir() {
        String importDir = System.getProperty("importdir");
        if (importDir == null || importDir.isEmpty()) {
            throw new IllegalStateException("importdir required."
                    + " System property importdir is missing or empty");
        }
        return importDir + File.separator;
    }

    private String getExportDir() {
        String exportDir = System.getProperty("exportdir");
        if (exportDir == null || exportDir.isEmpty()) {
            throw new IllegalStateException("exportdir required."
                    + " System property exportdir is missing or empty");
        }
        return exportDir + File.separator;
    }
}
