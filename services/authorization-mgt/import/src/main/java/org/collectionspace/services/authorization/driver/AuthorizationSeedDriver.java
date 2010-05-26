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
package org.collectionspace.services.authorization.driver;

import java.io.File;
import java.util.HashSet;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.importer.AuthorizationGen;
import org.collectionspace.services.authorization.importer.AuthorizationSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * A driver for seeding authorization
 * @author 
 */
public class AuthorizationSeedDriver {

    final Logger logger = LoggerFactory.getLogger(AuthorizationSeedDriver.class);
    final static private String SPRING_SECURITY_METADATA = "applicationContext-authorization-test.xml";
    final static private String PERMISSION_FILE = "import-permissions.xml";
    final static private String PERMISSION_ROLE_FILE = "import-permissions-roles.xml";
    private String user = "test";
    private String password = "test";
    private String tenantBindingFile;
    private String importDir;
    private String exportDir;
    private org.springframework.jdbc.datasource.DataSourceTransactionManager txManager;

    /**
     * AuthorizationSeedDriver
     * @param user to use to establish security context. should be in ROLE_ADMINISTRATOR
     * @param password
     * @param tenantBindingFile
     * @param importDir dir to import permisison/permission role file from. same as
     * export dir by default
     * @param exportDir dir to export permission/permission role file to
     */
    public AuthorizationSeedDriver(String user, String password,
            String tenantBindingFile,
            String importDir, String exportDir) {
        if (user == null || user.isEmpty()) {
            this.user = user;
        }
        if (password == null || password.isEmpty()) {
            this.password = password;
        }
        if (tenantBindingFile == null || tenantBindingFile.isEmpty()) {
            throw new IllegalStateException("tenantbindings are required.");
        }
        this.tenantBindingFile = tenantBindingFile;
        if (exportDir == null || exportDir.isEmpty()) {
            throw new IllegalStateException("exportdir required.");
        }
        this.exportDir = exportDir;
        if (importDir == null || importDir.isEmpty()) {
            importDir = exportDir;
        } else {
            this.importDir = importDir;
        }

    }

    public void seedData() {
        setup();
        TransactionStatus status = null;
        try {
            AuthorizationGen authzGen = new AuthorizationGen();
            authzGen.initialize(tenantBindingFile);
            authzGen.createDefaultServicePermissions();
            //create default role(s) for the tenant and assign permissions
            authzGen.createDefaultPermissionsRoles();
            authzGen.exportPermissions(exportDir + File.separator + PERMISSION_FILE);
            authzGen.exportPermissionRoles(exportDir + File.separator + PERMISSION_ROLE_FILE);
            if (logger.isDebugEnabled()) {
                logger.debug("authroization generation completed ");
            }
            status = beginTransaction("seedData");
            AuthorizationSeed authzSeed = new AuthorizationSeed();
            authzSeed.seedPermissions(importDir + File.separator + PERMISSION_FILE,
                    importDir + File.separator + PERMISSION_ROLE_FILE);
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
            logout();
        }
    }

    private void setup() {

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{SPRING_SECURITY_METADATA});
        login();
        System.setProperty("spring-beans-config", SPRING_SECURITY_METADATA);
        AuthZ authZ = AuthZ.get();
        txManager = (org.springframework.jdbc.datasource.DataSourceTransactionManager) appContext.getBean("transactionManager");
    }

    private void login() {
        GrantedAuthority gauth = new GrantedAuthorityImpl("ROLE_ADMINISTRATOR");
        HashSet<GrantedAuthority> gauths = new HashSet<GrantedAuthority>();
        gauths.add(gauth);
        Authentication authRequest = new UsernamePasswordAuthenticationToken(user, password, gauths);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private TransactionStatus beginTransaction(String name) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        // explicitly setting the transaction name is something that can only be done programmatically
        def.setName(name);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return txManager.getTransaction(def);
    }

    private void rollbackTransaction(TransactionStatus status) {
        txManager.rollback(status);
    }

    private void commitTransaction(TransactionStatus status) {
        txManager.commit(status);
    }
}
