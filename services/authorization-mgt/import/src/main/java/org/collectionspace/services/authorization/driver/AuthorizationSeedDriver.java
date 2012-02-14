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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.authorization.importer.AuthorizationGen;
import org.collectionspace.services.authorization.importer.AuthorizationSeed;
import org.collectionspace.services.authorization.importer.AuthorizationStore;
import org.collectionspace.services.authorization.storage.PermissionRoleUtil;
import org.hibernate.exception.ConstraintViolationException;
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
    final static private String ROLE_FILE = "import-roles.xml";
    final static private String PERMISSION_FILE = "import-permissions.xml";
    final static private String PERMISSION_ROLE_FILE = "import-permissions-roles.xml";
    private String user;
    private String password;
    private String tenantBindingFile;
    private String exportDir;
    private AuthorizationGen authzGen;
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
            String exportDir) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("username required.");
        }
        this.user = user;

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password required.");
        }
        this.password = password;
        
        if (tenantBindingFile == null || tenantBindingFile.isEmpty()) {
            throw new IllegalArgumentException("tenantbinding file are required.");
        }
        this.tenantBindingFile = tenantBindingFile;
        if (exportDir == null || exportDir.isEmpty()) {
            throw new IllegalArgumentException("exportdir required.");
        }
        this.exportDir = exportDir;

    }

    public void generate() {
        try {
            authzGen = new AuthorizationGen();
            authzGen.initialize(tenantBindingFile);
            authzGen.createDefaultRoles();
            authzGen.createDefaultPermissions();
            authzGen.associateDefaultPermissionsRoles();
            authzGen.exportDefaultRoles(exportDir + File.separator + ROLE_FILE);
            authzGen.exportDefaultPermissions(exportDir + File.separator + PERMISSION_FILE);
            authzGen.exportDefaultPermissionRoles(exportDir + File.separator + PERMISSION_ROLE_FILE);
            if (logger.isDebugEnabled()) {
                logger.debug("Authorization generation completed but not yet persisted.");
            }
        } catch (Exception ex) {
            logger.error("AuthorizationSeedDriver caught an exception: ", ex);
            throw new RuntimeException(ex);
        }
    }

    public void seed() {
        TransactionStatus status = null;
        try {
        	// Push all the authz info into the cspace DB tables.
            store();

            setupSpring();
            status = beginTransaction("seedData");
            AuthorizationSeed authzSeed = new AuthorizationSeed();
            authzSeed.seedPermissions(authzGen.getDefaultPermissions(), authzGen.getDefaultPermissionRoles());
//            authzSeed.seedPermissions(exportDir + File.separator + PERMISSION_FILE,
//                    exportDir + File.separator + PERMISSION_ROLE_FILE);
            if (logger.isDebugEnabled()) {
                logger.debug("authorization seeding completed ");
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

    private void setupSpring() {

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{SPRING_SECURITY_METADATA});
        login();
        System.setProperty("spring-beans-config", SPRING_SECURITY_METADATA);
        // authZ local not used but call to AuthZ.get() has side-effect of initializing our Spring Security context
        AuthZ authZ = AuthZ.get();
        txManager = (org.springframework.jdbc.datasource.DataSourceTransactionManager) appContext.getBean("transactionManager");
        if (logger.isDebugEnabled()) {
            logger.debug("Spring Security setup complete.");
        }
    }

    private void login() {
        //GrantedAuthority cspace_admin = new GrantedAuthorityImpl("ROLE_ADMINISTRATOR");
        GrantedAuthority spring_security_admin = new GrantedAuthorityImpl("ROLE_SPRING_ADMIN");
        HashSet<GrantedAuthority> gauths = new HashSet<GrantedAuthority>();
        //gauths.add(cspace_admin);
        gauths.add(spring_security_admin);
        Authentication authRequest = new UsernamePasswordAuthenticationToken(user, password, gauths);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
        if (logger.isDebugEnabled()) {
            logger.debug("Spring Security login successful for user=" + user);
        }
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        if (logger.isDebugEnabled()) {
            logger.debug("Spring Security logged out user=" + user);
        }
    }

    private void store() throws Exception {
        AuthorizationStore authzStore = new AuthorizationStore();
        for (Role role : authzGen.getDefaultRoles()) {
        	try {
        		authzStore.store(role);
        	} catch (Exception e) {
        		//
        		// If the role already exists, read it in and replace the instance
        		// we're trying to import with the exist one.  This will ensure that the rest
        		// of import uses the correct CSID.
        		if (e.getCause() instanceof ConstraintViolationException) {
        			Role existingRole = authzStore.getRoleByName(role.getRoleName(), role.getTenantId());
        			//
        			role = existingRole;
        		}
        	}
        }

        for (Permission perm : authzGen.getDefaultPermissions()) {
            authzStore.store(perm);
        }

        List<PermissionRoleRel> permRoleRels = new ArrayList<PermissionRoleRel>();
        for (PermissionRole pr : authzGen.getDefaultPermissionRoles()) {
            PermissionRoleUtil.buildPermissionRoleRel(pr, SubjectType.ROLE, permRoleRels, false /*not for delete*/);
        }
        for (PermissionRoleRel permRoleRel : permRoleRels) {
            authzStore.store(permRoleRel);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Authroization metata persisted.");
        }
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
