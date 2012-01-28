/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.security.client.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import javax.ws.rs.core.Response;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.EffectType;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.client.AccountRoleClient;
import org.collectionspace.services.client.AccountRoleFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.DimensionClient;
import org.collectionspace.services.client.DimensionFactory;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.PermissionRoleClient;
import org.collectionspace.services.client.PermissionRoleFactory;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.BaseServiceTest;
import org.collectionspace.services.dimension.DimensionsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * AuthorizationServiceTest, carries out tests against a
 * deployed and running Permission, Role, AccountRole, PermissionRole and
 * CollectionObject Services.
 *
 * Pre-requisite : authorization-mgt/client tests seed some permissions used
 * by this test
 *
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class MultiTenancyTest extends BaseServiceTest<AbstractCommonList> {

    private static class UserInfo {
        String userName;
        String password;

        UserInfo(String u, String p) {
            userName = u;
            password = p;
        }
    }
    
    private final String CLASS_NAME = MultiTenancyTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private final static String TENANT_1_ADMIN_USER = "admin@core.collectionspace.org";
    private final static String TENANT_2_ADMIN_USER = "admin@lifesci.collectionspace.org";
    private final static String TENANT_ADMIN_PASS = "Administrator";
    private final static String TENANT_1_USER = "user1@museum1.org";
    private final static String TENANT_2_USER = "user2@museum2.org";
    private final static String TENANT_1 = "1";
    private final static String TENANT_2 = "2";
    private final static String TEST_ROLE_NAME = "ROLE_TEST_REGISTRAR";
    private final static String TEST_SERVICE_A = "dimensions";

    // Instance variables specific to this test.
    private String TENANT_RESOURCE_1 = null;
    private String TENANT_RESOURCE_2 = null;
    //key for userAccounts is userId
    private Hashtable<String, AccountValue> userAccounts = new Hashtable<String, AccountValue>();
    //key for permValues is id as there could be several perms for the same resource
    private Hashtable<String, PermissionValue> permValues = new Hashtable<String, PermissionValue>();
    //key for all tenantXXX tables is tenant id, expecting only one entity per tenant for this test
    private Hashtable<String, UserInfo> tenantAdminUsers = new Hashtable<String, UserInfo>();
    private Hashtable<String, AccountValue> tenantAccounts = new Hashtable<String, AccountValue>();
    private Hashtable<String, RoleValue> tenantRoles = new Hashtable<String, RoleValue>();
    private Hashtable<String, Role> tenantAdminRoles = new Hashtable<String, Role>();
    private Hashtable<String, PermissionValue> tenantPermissions = new Hashtable<String, PermissionValue>();

    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */
    @Override
    protected String getServicePathComponent() {
        return null;
    }

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        //tenant admin users are used to create accounts, roles and permissions and relationships
        //assumption : two tenant admin users exist before running this test
        tenantAdminUsers.put(TENANT_1, new UserInfo(TENANT_1_ADMIN_USER, TENANT_ADMIN_PASS));
        tenantAdminUsers.put(TENANT_2, new UserInfo(TENANT_2_ADMIN_USER, TENANT_ADMIN_PASS));

        seedAccounts();
        seedPermissions();
        seedRoles();
        seedAccountRoles();
        seedPermissionRoles();
    }

    private void seedAccounts() {
        seedAccount(TENANT_1, TENANT_1_USER);
        seedAccount(TENANT_2, TENANT_2_USER);
    }

    private void seedAccount(String tenantId, String userId) {
        //create account using default user in admin role but assign tenant id
        //create username, email and password same for simplicity
        String accId = createAccount(tenantId, userId, userId);
        AccountValue ava = new AccountValue();
        ava.setScreenName(userId);
        ava.setUserId(userId);
        ava.setAccountId(accId);
        userAccounts.put(ava.getUserId(), ava);
        tenantAccounts.put(tenantId, ava);
        if (logger.isDebugEnabled()) {
            logger.debug("seedAccount tenantId=" + tenantId + " userId=" + userId);
        }
    }

    private void seedPermissions() {
        String resource = TEST_SERVICE_A;

        PermissionAction pac = new PermissionAction();
        pac.setName(ActionType.CREATE);
        PermissionAction par = new PermissionAction();
        par.setName(ActionType.READ);
        PermissionAction pau = new PermissionAction();
        pau.setName(ActionType.UPDATE);
        PermissionAction pad = new PermissionAction();
        pad.setName(ActionType.DELETE);

        //both users can create, read and update and delete
        List<PermissionAction> testActions = new ArrayList<PermissionAction>();
        testActions.add(pac);
        testActions.add(par);
        testActions.add(pau);
        testActions.add(pad);

        seedPermission(TENANT_1, resource, testActions, EffectType.PERMIT);
        seedPermission(TENANT_2, resource, testActions, EffectType.PERMIT);
    }

    private void seedPermission(String tenantId,
            String resource, List<PermissionAction> testActions, EffectType effect) {
        //create permission using default user in admin role but assign tenant id
        String id = createPermission(tenantId, resource, testActions, effect);
        PermissionValue pv = new PermissionValue();
        pv.setResourceName(resource);
        pv.setPermissionId(id);
        permValues.put(pv.getPermissionId(), pv);
        tenantPermissions.put(tenantId, pv);
        if (logger.isDebugEnabled()) {
            logger.debug("seedPermission tenantId=" + tenantId
                    + " permId=" + id + " resource=" + resource);
        }
    }

    private void seedRoles() {
        //create role using default user in admin role but assign tenant id
        //use the same role name to check constraints
        seedRole(TENANT_1, TEST_ROLE_NAME);
        seedRole(TENANT_2, TEST_ROLE_NAME);
    }

    private void seedRole(String tenantId, String roleName) {
        String rid = createRole(tenantId, roleName);
        RoleValue rv = new RoleValue();
        rv.setRoleId(rid);
        rv.setRoleName(roleName);
        tenantRoles.put(tenantId, rv);
        if (logger.isDebugEnabled()) {
            logger.debug("seedRole tenantId=" + tenantId + " roleName=" + roleName);
        }
    }

    private void seedAccountRoles() {
        for (String tenantId : tenantAccounts.keySet()) {
            AccountValue av = (AccountValue) tenantAccounts.get(tenantId);
            seedAccountRole(tenantId, av.getUserId());
        }
    }

    private void seedAccountRole(String tenantId, String userId) {
        List<RoleValue> tenantRoleValues = new ArrayList<RoleValue>();
        tenantRoleValues.add(tenantRoles.get(tenantId));
        createAccountRole(tenantId, userAccounts.get(userId), tenantRoleValues);
        if (logger.isDebugEnabled()) {
            logger.debug("seedAccountRole tenantId=" + tenantId + " userId=" + userId);
        }
    }

    private void seedPermissionRoles() {
        for (String tenantId : tenantPermissions.keySet()) {
            PermissionValue pv = tenantPermissions.get(tenantId);
            seedPermissionRole(tenantId, pv.getPermissionId());
        }
    }

    private void seedPermissionRole(String tenantId, String permId) {
        List<RoleValue> tenantRoleValues = new ArrayList<RoleValue>();
        tenantRoleValues.add(tenantRoles.get(tenantId));
        PermissionValue pv = permValues.get(permId);
        createPermissionRole(tenantId, permValues.get(permId), tenantRoleValues);
        if (logger.isDebugEnabled()) {
            logger.debug("seedPermissionRole tenantId=" + tenantId
                    + " permId=" + permId + " resource=" + pv.getResourceName());
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getCommonList(
            ClientResponse<AbstractCommonList> response) {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
        throw new UnsupportedOperationException();
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Test(dataProvider = "testName")
    public void create(String testName) throws Exception {
    	TENANT_RESOURCE_1 = create(testName, TENANT_1_USER, TENANT_1);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_1
                    + " userId=" + TENANT_1_USER
                    + " TENANT_RESOURCE_1 id=" + TENANT_RESOURCE_1);
        }
    }

    @Test(dataProvider = "testName")
    public void create2(String testName) throws Exception {
    	TENANT_RESOURCE_2 = create(testName, TENANT_2_USER, TENANT_2);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_2
                    + " userId=" + TENANT_2_USER
                    + " TENANT_RESOURCE_2 id=" + TENANT_RESOURCE_2);
        }
    }

    private String create(String testName, String userName, String tenatnId) {
    	String result = null;
    	
        setupCreate();
        //
        // Create a new client and change the default AuthN credentials
        //
        DimensionClient client = new DimensionClient();
        client.setAuth(true, userName, true, userName, true);
        //
        // Setup a dimension object to create
        //
        String identifier = createIdentifier();
        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        dimension.setMeasurementUnit("measurementUnit-" + identifier);
        dimension.setValueDate(new Date().toString());
        //
        // Create a payload and send the POST request
        //
        PoxPayloadOut multipart = DimensionFactory.createDimensionInstance(client.getCommonPartName(),
                dimension);
        ClientResponse<Response> res = client.create(multipart);
        try {
        	assertStatusCode(res, testName);
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        return result;

    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        DimensionsCommon dimension = read(testName, TENANT_RESOURCE_1, TENANT_1_USER);
        Assert.assertNotNull(dimension);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_1
                    + " userId=" + TENANT_1_USER
                    + " TENANT_RESOURCE_1 retrieved id=" + dimension.getCsid());
        }
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"create2"})
    public void read2(String testName) throws Exception {
        DimensionsCommon dimension = read(testName, TENANT_RESOURCE_2, TENANT_2_USER);
        Assert.assertNotNull(dimension);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_2
                    + " userId=" + TENANT_2_USER
                    + " TENANT_RESOURCE_1 retrieved id=" + dimension.getCsid());
        }
    }

    private DimensionsCommon read(String testName, String id, String userName) throws Exception {
    	DimensionsCommon result = null;
    	
    	setupRead();
        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        client.setAuth(true, userName, true, userName, true);
        ClientResponse<String> res = client.read(id);
        try {
        	assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        result = (DimensionsCommon) extractPart(input,
	                client.getCommonPartName(), DimensionsCommon.class);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"read2"})
    public void updateNonExistent(String testName) throws Exception {
    	
    	setupUpdateNonExistent();
    	//
    	// Create a new client and change the default AuthN credentials
    	//
        DimensionClient client = new DimensionClient();
        //TENANT_1_USER is not allowed to update the resource of TENANT_2
        client.setAuth(true, TENANT_1_USER, true, TENANT_1_USER, true);
        //
        // Create a new dimension object to try to update
        //
        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        // Update the content of this resource.
        dimension.setMeasurementUnit("updated-" + dimension.getMeasurementUnit());
        dimension.setValueDate("updated-" + dimension.getValueDate());
        //
        // Create and send a dimension payload for the UPDATE request
        //
        PoxPayloadOut output = new PoxPayloadOut(DimensionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), dimension);
        ClientResponse<String> res = client.update(TENANT_RESOURCE_2, output);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"deleteNonExistent"})
    public void delete(String testName) throws Exception {
        int statusCode = delete(testName, TENANT_RESOURCE_1, TENANT_1_USER);
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"updateNonExistent"})
    public void delete2(String testName) throws Exception {
        int statusCode = delete(testName, TENANT_RESOURCE_2, TENANT_2_USER);

    }

    private int delete(String testName, String id, String userName) throws Exception {
    	int result = -1;
    	
        // Perform setup.
        setupDelete();
        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        client.setAuth(true, userName, true, userName, true);
        ClientResponse<Response> res = client.delete(id);
        try {
        	result = assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    // Failure outcomes
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"read"})
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db

    	// Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //TENANT_2_USER of TENANT_2 is not allowed to delete the resource of TENANT_1
        client.setAuth(true, TENANT_2_USER, true, TENANT_2_USER, true);
        ClientResponse<Response> res = client.delete(TENANT_RESOURCE_1);
        try {
	        int statusCode = res.getStatus();
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        //going to incorrect Nuxeo domain would give DocumentNotFoundException
	        //instead of giving FORBIDDEN
	        Assert.assertEquals(statusCode, Response.Status.NOT_FOUND.getStatusCode());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        setupDelete();
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }

        //tenant admin users are used to create accounts, roles and permissions and relationships
        //assumption : two tenant admin users exist before running this test

        deletePermissionRoles();
        deleteAccountRoles();
        //deletePermissions would delete association with ROLE_XXX_ADMINISTRTOR too
        //deletePermissions();
        deleteRoles();
        deleteAccounts();
    }

    private void deletePermissionRoles() {
        for (String tenantId : tenantPermissions.keySet()) {
            List<RoleValue> tenantRoleValues = new ArrayList<RoleValue>();
            tenantRoleValues.add(tenantRoles.get(tenantId));
            PermissionValue pv = tenantPermissions.get(tenantId);
            deletePermissionRole(tenantId, pv, tenantRoleValues);
        }
    }

    private void deleteAccountRoles() {
        for (String tenantId : tenantAccounts.keySet()) {
            List<RoleValue> tenantRoleValues = new ArrayList<RoleValue>();
            tenantRoleValues.add(tenantRoles.get(tenantId));
            AccountValue av = tenantAccounts.get(tenantId);
            deleteAccountRole(tenantId, av, tenantRoleValues);
        }
    }

    private void deletePermissions() {
        for (String tenantId : tenantPermissions.keySet()) {
            PermissionValue pv = tenantPermissions.get(tenantId);
            deletePermission(tenantId, pv.getPermissionId());
        }
    }

    private void deleteRoles() {
        for (String tenantId : tenantRoles.keySet()) {
            RoleValue rv = tenantRoles.get(tenantId);
            deleteRole(tenantId, rv.getRoleId());
        }
    }

    private void deleteAccounts() {
        for (String tenantId : tenantAccounts.keySet()) {
            AccountValue av = tenantAccounts.get(tenantId);
            deleteAccount(tenantId, av.getAccountId());
        }
    }

    private String createPermission(String tenantId, String resName,
            List<PermissionAction> actions, EffectType effect) {
    	String result = null;
    	
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        permClient.setAuth(true, ui.userName, true, ui.password, true);
        Permission permission = PermissionFactory.createPermissionInstance(resName,
                "default permissions for " + resName,
                actions, effect, true, true, true);
        permission.setTenantId(tenantId);
        ClientResponse<Response> res = permClient.create(permission);
        try {
        	assertStatusCode(res, "CreatePermission");
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    private void deletePermission(String tenantId, String permId) {
        setupDelete();
        PermissionClient permClient = new PermissionClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        permClient.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = permClient.delete(permId);
        try {
        	assertStatusCode(res, "DeletePermission");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createRole(String tenantId, String roleName) {
    	String result = null;
    	
        setupCreate();
        RoleClient roleClient = new RoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        roleClient.setAuth(true, ui.userName, true, ui.password, true);
        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
        role.setTenantId(tenantId);
        ClientResponse<Response> res = roleClient.create(role);
        try {
        	assertStatusCode(res, "CreateRole");
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    private void deleteRole(String tenantId, String roleId) {
        setupDelete();
        RoleClient roleClient = new RoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        roleClient.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = roleClient.delete(roleId);
        try {
        	assertStatusCode(res, "DeleteRole");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createAccount(String tenantId, String userName, String email) {
    	String result = null;
    	
        setupCreate();
        AccountClient accountClient = new AccountClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        accountClient.setAuth(true, ui.userName, true, ui.password, true);
        AccountsCommon account = AccountFactory.createAccountInstance(
                userName, userName, userName, email, tenantId,
                true, false, true, true);
        ClientResponse<Response> res = accountClient.create(account);
        try {
        	assertStatusCode(res, "CreateAccount");
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    private void deleteAccount(String tenantId, String accId) {
        setupDelete();
        AccountClient accClient = new AccountClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        accClient.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = accClient.delete(accId);
        try {
        	assertStatusCode(res, "DeleteAccount");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createAccountRole(String tenantId, AccountValue av,
            Collection<RoleValue> rvs) {
    	String result = null;
    	
        setupCreate();
        // Submit the request to the service and store the response.
        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                av, rvs, true, true);
        AccountRoleClient client = new AccountRoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        client.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = client.create(av.getAccountId(), accRole);
        try {
        	assertStatusCode(res, "CreateAccountRole");
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        return result;
    }

    private void deleteAccountRole(String tenantId, AccountValue av,
            List<RoleValue> rvs) {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        client.setAuth(true, ui.userName, true, ui.password, true);
        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                av, rvs, true, true);
        ClientResponse<Response> res = client.delete(av.getAccountId());
        try {
        	assertStatusCode(res, "DeleteAccountRole");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createPermissionRole(String tenantId, PermissionValue pv,
            Collection<RoleValue> rvs) {
    	String result = null;
    	
        setupCreate();
        List<RoleValue> rvls = new ArrayList<RoleValue>();
        rvls.addAll(rvs);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                pv, rvls, true, true);
        PermissionRoleClient client = new PermissionRoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        client.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = client.create(pv.getPermissionId(), permRole);
        try {
        	assertStatusCode(res, "createPermissionRole");
        	result = extractId(res);
        }  finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    private void deletePermissionRole(String tenantId, PermissionValue pv, List<RoleValue> rvls) {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        client.setAuth(true, ui.userName, true, ui.password, true);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                pv, rvls, true, true);
        ClientResponse<Response> res = client.delete(pv.getPermissionId());
        try {
        	assertStatusCode(res, "DeletePermissionRole");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

	@Override
	protected String getServiceName() {
		// TODO Auto-generated method stub
    	throw new UnsupportedOperationException();
	}

	@Override
	protected Class<AbstractCommonList> getCommonListType() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
