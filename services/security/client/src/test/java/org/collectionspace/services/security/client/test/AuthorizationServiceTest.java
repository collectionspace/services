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
public class AuthorizationServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = AuthorizationServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    private String knownResourceId = null;
    private List<String> allResourceIdsCreated = new ArrayList();
    //key for accValues is userId
    private Hashtable<String, AccountValue> accValues = new Hashtable<String, AccountValue>();
    //key for permValues is id as there could be several perms for the same resource
    private Hashtable<String, PermissionValue> permValues = new Hashtable<String, PermissionValue>();
    //key for roleValues is roleName
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    private String bigbirdPermId;
    private String elmoPermId;
    private final static String TEST_SERVICE_NAME = "dimensions";
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    @Override
    protected String getServicePathComponent() {
        return null;
    }

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        seedPermissions();
        seedRoles();
        seedAccounts();
        seedAccountRoles();
        seedPermissionRoles();
    }

    private void seedPermissions() {
        String res = TEST_SERVICE_NAME;

        PermissionAction pac = new PermissionAction();
        pac.setName(ActionType.CREATE);
        PermissionAction par = new PermissionAction();
        par.setName(ActionType.READ);
        PermissionAction pau = new PermissionAction();
        pau.setName(ActionType.UPDATE);


        //bigbird can create, read and update but not delete
        List<PermissionAction> bbactions = new ArrayList<PermissionAction>();
        bbactions.add(pac);
        bbactions.add(par);
        bbactions.add(pau);
        bigbirdPermId = createPermission(res, bbactions, EffectType.PERMIT);
        PermissionValue bbpv = new PermissionValue();
        bbpv.setResourceName(res);
        bbpv.setPermissionId(bigbirdPermId);
        permValues.put(bbpv.getPermissionId(), bbpv);

        //elmo can only read
        List<PermissionAction> eactions = new ArrayList<PermissionAction>();
        eactions.add(par);
        elmoPermId = createPermission(res, eactions, EffectType.PERMIT);
        PermissionValue epv = new PermissionValue();
        epv.setResourceName(res);
        epv.setPermissionId(elmoPermId);
        permValues.put(epv.getPermissionId(), epv);
    }

    private void seedRoles() {
        String rn1 = "ROLE_TEST_CM";
        String r1RoleId = createRole(rn1);
        RoleValue rv1 = new RoleValue();
        rv1.setRoleId(r1RoleId);
        rv1.setRoleName(rn1);
        roleValues.put(rv1.getRoleName(), rv1);

        String rn2 = "ROLE_TEST_INTERN";
        String r2RoleId = createRole(rn2);
        RoleValue rv2 = new RoleValue();
        rv2.setRoleId(r2RoleId);
        rv2.setRoleName(rn2);
        roleValues.put(rv2.getRoleName(), rv2);
    }

    private void seedAccounts() {
        String userId = "bigbird2010";
        String accId = createAccount(userId, "bigbird@cspace.org");
        AccountValue ava = new AccountValue();
        ava.setScreenName(userId);
        ava.setUserId(userId);
        ava.setAccountId(accId);
        accValues.put(ava.getUserId(), ava);

        String userId2 = "elmo2010";
        String coAccId = createAccount(userId2, "elmo@cspace.org");
        AccountValue avc = new AccountValue();
        avc.setScreenName(userId2);
        avc.setUserId(userId2);
        avc.setAccountId(coAccId);
        accValues.put(avc.getUserId(), avc);
    }

    private void seedAccountRoles() {

        List<RoleValue> bigbirdRoleValues = new ArrayList<RoleValue>();
        bigbirdRoleValues.add(roleValues.get("ROLE_TEST_CM"));
        createAccountRole(accValues.get("bigbird2010"), bigbirdRoleValues);

        List<RoleValue> elmoRoleValues = new ArrayList<RoleValue>();
        elmoRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
        createAccountRole(accValues.get("elmo2010"), elmoRoleValues);
    }

    private void seedPermissionRoles() {

        List<RoleValue> bigbirdRoleValues = new ArrayList<RoleValue>();
        bigbirdRoleValues.add(roleValues.get("ROLE_TEST_CM"));
        createPermissionRole(permValues.get(bigbirdPermId), bigbirdRoleValues);

        List<RoleValue> elmoRoleValues = new ArrayList<RoleValue>();
        elmoRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
        createPermissionRole(permValues.get(elmoPermId), elmoRoleValues);

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
        setupCreate();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //bigbird allowed to create
        client.setAuth(true, "bigbird2010", true, "bigbird2010", true);
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
        knownResourceId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
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
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //elmo allowed to read
        client.setAuth(true, "elmo2010", true, "elmo2010", true);
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
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
        DimensionsCommon dimension = (DimensionsCommon) extractPart(input,
                client.getCommonPartName(), DimensionsCommon.class);
        Assert.assertNotNull(dimension);

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

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read", "readList", "readNonExistent"})
    public void updateNotAllowed(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        setupUpdate();

        DimensionClient client = new DimensionClient();

        //elmo not allowed to update
        client.setAuth(true, "elmo2010", true, "elmo2010", true);

        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        // Update the content of this resource.
        dimension.setValue("updated-" + dimension.getValue());
        dimension.setValueDate("updated-" + dimension.getValueDate());
        // Submit the request to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(dimension, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        ClientResponse<MultipartInput> res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.FORBIDDEN.getStatusCode());
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
    dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"updateNotAllowed"})
    public void deleteNotAllowed(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //bigbird can not delete
        client.setAuth(true, "bigbird2010", true, "bigbird2010", true);
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.FORBIDDEN.getStatusCode());

    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"deleteNotAllowed"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();

        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }

    // Failure outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db
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

        deletePermissionRoles();
        deleteAccountRoles();
        //FIXME delete on permission deletes all associations with roles
        //this would delete association with ROLE_ADMINISTRATOR too
        //deletePermissions();
        deleteRoles();
        deleteAccounts();
    }

    private void deletePermissionRoles() {

        List<RoleValue> bigbirdRoleValues = new ArrayList<RoleValue>();
        bigbirdRoleValues.add(roleValues.get("ROLE_TEST_CM"));
        deletePermissionRole(permValues.get(bigbirdPermId), bigbirdRoleValues);

        List<RoleValue> elmoRoleValues = new ArrayList<RoleValue>();
        elmoRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
        deletePermissionRole(permValues.get(elmoPermId), elmoRoleValues);

    }

    private void deleteAccountRoles() {
        List<RoleValue> bigbirdRoleValues = new ArrayList<RoleValue>();
        bigbirdRoleValues.add(roleValues.get("ROLE_TEST_CM"));
        deleteAccountRole(accValues.get("bigbird2010"), bigbirdRoleValues);

        List<RoleValue> elmoRoleValues = new ArrayList<RoleValue>();
        elmoRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
        deleteAccountRole(accValues.get("elmo2010"), elmoRoleValues);
    }

    private void deletePermissions() {
        //delete entities
        for (PermissionValue pv : permValues.values()) {
            deletePermission(pv.getPermissionId());
        }
    }

    private void deleteRoles() {
        for (RoleValue rv : roleValues.values()) {
            deleteRole(rv.getRoleId());
        }
    }

    private void deleteAccounts() {

        for (AccountValue av1 : accValues.values()) {
            deleteAccount(av1.getAccountId());
        }
    }

    private String createPermission(String resName, EffectType effect) {
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        return createPermission(resName, actions, effect);
    }

    private String createPermission(String resName,
            List<PermissionAction> actions, EffectType effect) {
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        Permission permission = PermissionFactory.createPermissionInstance(resName,
                "default permissions for " + resName,
                actions, effect, true, true, true);
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

    private void deletePermission(String permId) {
        setupDelete();
        PermissionClient permClient = new PermissionClient();
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

    private String createRole(String roleName) {
        setupCreate();
        RoleClient roleClient = new RoleClient();

        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
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

    private void deleteRole(String roleId) {
        setupDelete();
        RoleClient roleClient = new RoleClient();
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

    private String createAccount(String userName, String email) {
        setupCreate();
        AccountClient accountClient = new AccountClient();
        AccountsCommon account = AccountFactory.createAccountInstance(
                userName, userName, userName, email, accountClient.getTenantId(),
                true, false, true, true);
        ClientResponse<Response> res = accountClient.create(account);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("createAccount: userName=" + userName
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
    }

    private void deleteAccount(String accId) {
        setupDelete();
        AccountClient accClient = new AccountClient();
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

    private String createAccountRole(AccountValue av,
            Collection<RoleValue> rvs) {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                av, rvs, true, true);
        AccountRoleClient client = new AccountRoleClient();
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

    private void deleteAccountRole(AccountValue av,
            Collection<RoleValue> rvs) {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
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

    private String createPermissionRole(PermissionValue pv,
            Collection<RoleValue> rvs) {
        setupCreate();
        List<RoleValue> rvls = new ArrayList<RoleValue>();
        rvls.addAll(rvs);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                pv, rvls, true, true);
        PermissionRoleClient client = new PermissionRoleClient();
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

    private void deletePermissionRole(PermissionValue pv,
            Collection<RoleValue> rvs) {
        List<RoleValue> rvls = new ArrayList<RoleValue>();
        rvls.addAll(rvs);

        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
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
