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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.EffectType;

import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
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
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.PermissionRoleClient;
import org.collectionspace.services.client.PermissionRoleFactory;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.dimension.DimensionsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

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
public class MultiTenancyTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = MultiTenancyTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private final static String TENANT_1_ADMIN_USER = "admin@collectionspace.org";
    private final static String TENANT_2_ADMIN_USER = "admin@hearstmuseum.berkeley.edu";
    private final static String TENANT_ADMIN_PASS = "Administrator";
    private final static String TENANT_1_USER = "user1@museum1.org";
    private final static String TENANT_2_USER = "user2@museum2.org";
    private final static String TENANT_1 = "1";
    private final static String TENANT_2 = "2";
    private final static String TEST_ROLE_NAME = "ROLE_TEST_REGISTRAR";
    private final static String TEST_SERVICE_A = "dimensions";

    private static class UserInfo {

        String userName;
        String password;

        UserInfo(String u, String p) {
            userName = u;
            password = p;
        }
    }
    // Instance variables specific to this test.
    private String TENANT_RESOURCE_1 = null;
    private String TENANT_RESOURCE_2 = null;
    private List<String> allResourceIdsCreated = new ArrayList();
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

//    private String permId1;
//    private String permId2;

    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */
    @Override
    protected String getServicePathComponent() {
        return null;
    }

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        if (logger.isDebugEnabled()) {
            testBanner("seedData", CLASS_NAME);
        }

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
    protected AbstractCommonList getAbstractCommonList(
            ClientResponse<AbstractCommonList> response) {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
        throw new UnsupportedOperationException();
    }

    @Test(dataProvider = "testName")
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
    }
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        ClientResponse<Response> res = create(testName, TENANT_1_USER, TENANT_1);

        TENANT_RESOURCE_1 = extractId(res);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_1
                    + " userId=" + TENANT_1_USER
                    + " TENANT_RESOURCE_1 id=" + TENANT_RESOURCE_1);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create2(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        ClientResponse<Response> res = create(testName, TENANT_2_USER, TENANT_2);

        TENANT_RESOURCE_2 = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_2
                    + " userId=" + TENANT_2_USER
                    + " TENANT_RESOURCE_2 id=" + TENANT_RESOURCE_2);
        }
    }

    private ClientResponse<Response> create(String testName, String userName, String tenatnId) {
        setupCreate();
        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        client.setAuth(true, userName, true, userName, true);
        String identifier = createIdentifier();
        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        dimension.setValue("value-" + identifier);
        dimension.setValueDate(new Date().toString());
        MultipartOutput multipart = DimensionFactory.createDimensionInstance(client.getCommonPartName(),
                dimension);
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.CREATED.getStatusCode());
        return res;

    }

    //to not cause uniqueness violation for permRole, createList is removed
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        DimensionsCommon dimension = read(testName, TENANT_RESOURCE_1, TENANT_1_USER);
        Assert.assertNotNull(dimension);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_1
                    + " userId=" + TENANT_1_USER
                    + " TENANT_RESOURCE_1 retrieved id=" + dimension.getCsid());
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create2"})
    public void read2(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        DimensionsCommon dimension = read(testName, TENANT_RESOURCE_2, TENANT_2_USER);
        Assert.assertNotNull(dimension);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": tenantId= " + TENANT_2
                    + " userId=" + TENANT_2_USER
                    + " TENANT_RESOURCE_1 retrieved id=" + dimension.getCsid());
        }
    }

    private DimensionsCommon read(String testName, String id, String userName) throws Exception {
        setupRead();
        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        client.setAuth(true, userName, true, userName, true);
        ClientResponse<MultipartInput> res = client.read(id);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        MultipartInput input = (MultipartInput) res.getEntity();
        return (DimensionsCommon) extractPart(input,
                client.getCommonPartName(), DimensionsCommon.class);
    }
    // Failure outcomes

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void readNonExistent(String testName) throws Exception {

        // Perform setup.
        setupReadNonExistent();
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        setupReadList();
    }

    // Failure outcomes
    // None at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read", "readList", "readNonExistent"})
    public void update(String testName) throws Exception {
        setupUpdate();

    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read2", "readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        DimensionClient client = new DimensionClient();
        //TENANT_1_USER is not allowed to update the resource of TENANT_2
        client.setAuth(true, TENANT_1_USER, true, TENANT_1_USER, true);

        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        // Update the content of this resource.
        dimension.setValue("updated-" + dimension.getValue());
        dimension.setValueDate("updated-" + dimension.getValueDate());
        // Submit the request to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(dimension, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        ClientResponse<MultipartInput> res = client.update(TENANT_RESOURCE_2, output);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " resource = " + TENANT_RESOURCE_2
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        //going to incorrect Nuxeo domain would give DocumentNotFoundException
        //instead of giving FORBIDDEN
        Assert.assertEquals(statusCode, Response.Status.NOT_FOUND.getStatusCode());
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"deleteNonExistent"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        int statusCode = delete(testName, TENANT_RESOURCE_1, TENANT_1_USER);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"updateNonExistent"})
    public void delete2(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int statusCode = delete(testName, TENANT_RESOURCE_2, TENANT_2_USER);

    }

    private int delete(String testName, String id, String userName) throws Exception {
        // Perform setup.
        setupDelete();
        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        client.setAuth(true, userName, true, userName, true);
        ClientResponse<Response> res = client.delete(id);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        return statusCode;
    }

    // Failure outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //TENANT_2_USER of TENANT_2 is not allowed to delete the resource of TENANT_1
        client.setAuth(true, TENANT_2_USER, true, TENANT_2_USER, true);
        ClientResponse<Response> res = client.delete(TENANT_RESOURCE_1);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        //going to incorrect Nuxeo domain would give DocumentNotFoundException
        //instead of giving FORBIDDEN
        Assert.assertEquals(statusCode, Response.Status.NOT_FOUND.getStatusCode());
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    @Test(dependsOnMethods = {"create"})
    public void testSubmitRequest() throws Exception {
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (logger.isDebugEnabled()) {
            testBanner("cleanUp", CLASS_NAME);
        }
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
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        permClient.setAuth(true, ui.userName, true, ui.password, true);
        Permission permission = PermissionFactory.createPermissionInstance(resName,
                "default permissions for " + resName,
                actions, effect, true, true, true);
        permission.setTenantId(tenantId);
        ClientResponse<Response> res = permClient.create(permission);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("createPermission: resName=" + resName
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
    }

    private void deletePermission(String tenantId, String permId) {
        setupDelete();
        PermissionClient permClient = new PermissionClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        permClient.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = permClient.delete(permId);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("deletePermission: delete permission id="
                    + permId + " status=" + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
    }

    private String createRole(String tenantId, String roleName) {
        setupCreate();
        RoleClient roleClient = new RoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        roleClient.setAuth(true, ui.userName, true, ui.password, true);
        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
        role.setTenantId(tenantId);
        ClientResponse<Response> res = roleClient.create(role);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("createRole: name=" + roleName
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
    }

    private void deleteRole(String tenantId, String roleId) {
        setupDelete();
        RoleClient roleClient = new RoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        roleClient.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = roleClient.delete(roleId);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("deleteRole: delete role id=" + roleId
                    + " status=" + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
    }

    private String createAccount(String tenantId, String userName, String email) {
        setupCreate();
        AccountClient accountClient = new AccountClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        accountClient.setAuth(true, ui.userName, true, ui.password, true);
        AccountsCommon account = AccountFactory.createAccountInstance(
                userName, userName, userName, email, tenantId,
                true, false, true, true);
        ClientResponse<Response> res = accountClient.create(account);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("createAccount: tenantId=" + tenantId + " userName=" + userName
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
    }

    private void deleteAccount(String tenantId, String accId) {
        setupDelete();
        AccountClient accClient = new AccountClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        accClient.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = accClient.delete(accId);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccount: delete account id="
                    + accId + " status=" + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
    }

    private String createAccountRole(String tenantId, AccountValue av,
            Collection<RoleValue> rvs) {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                av, rvs, true, true);
        AccountRoleClient client = new AccountRoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        client.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = client.create(av.getAccountId(), accRole);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug("createAccountRole: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
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
        ClientResponse<Response> res = client.delete(
                av.getAccountId());
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccountRole: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
    }

    private String createPermissionRole(String tenantId, PermissionValue pv,
            Collection<RoleValue> rvs) {
        setupCreate();
        List<RoleValue> rvls = new ArrayList<RoleValue>();
        rvls.addAll(rvs);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                pv, rvls, true, true);
        PermissionRoleClient client = new PermissionRoleClient();
        UserInfo ui = tenantAdminUsers.get(tenantId);
        client.setAuth(true, ui.userName, true, ui.password, true);
        ClientResponse<Response> res = client.create(pv.getPermissionId(), permRole);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug("createPermissionRole: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
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
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("deletePermissionRole : status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
    }
}
