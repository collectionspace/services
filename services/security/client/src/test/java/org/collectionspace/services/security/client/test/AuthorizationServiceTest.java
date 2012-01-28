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
import org.jboss.resteasy.client.ClientResponse;

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
import org.collectionspace.services.client.IntakeClient;
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
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class AuthorizationServiceTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = AuthorizationServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    private Hashtable<String, AccountValue> accValues = new Hashtable<String, AccountValue>();
    //key for permValues is id as there could be several perms for the same resource
    private Hashtable<String, PermissionValue> permValues = new Hashtable<String, PermissionValue>();
    //key for roleValues is roleName
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    private String bigbirdPermId;
    private String elmoPermId;
    private final static String TEST_SERVICE_NAME = "dimensions";
    private boolean accountRolesFlipped = false;
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    @Override
	protected String getServiceName() {
        // no need to return anything but null since no auth resources are
        // accessed
    	throw new UnsupportedOperationException();
	}
	
    @Override
    protected String getServicePathComponent() {
        // no need to return anything but null since no auth resources are
        // accessed
    	throw new UnsupportedOperationException();
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
        String userId1 = "bigbird2010";
        String accId1 = createAccount(userId1, "bigbird@cspace.org");
        AccountValue av1 = new AccountValue();
        av1.setScreenName(userId1);
        av1.setUserId(userId1);
        av1.setAccountId(accId1);
        accValues.put(av1.getUserId(), av1);

        String userId2 = "elmo2010";
        String accId2 = createAccount(userId2, "elmo@cspace.org");
        AccountValue av2 = new AccountValue();
        av2.setScreenName(userId2);
        av2.setUserId(userId2);
        av2.setAccountId(accId2);
        accValues.put(av2.getUserId(), av2);

        String userId3 = "lockedOut";
        String accId3 = createAccount(userId3, "lockedOut@cspace.org");
        AccountValue av3 = new AccountValue();
        av3.setScreenName(userId3);
        av3.setUserId(userId3);
        av3.setAccountId(accId3);
        accValues.put(av3.getUserId(), av3);
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
    	// This method is meaningless to this test.
        return null;
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Test(dataProvider = "testName")
    public void create(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //bigbird allowed to create
        client.setAuth(true, "bigbird2010", true, "bigbird2010", true);
        String identifier = createIdentifier();
        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        dimension.setMeasurementUnit("measurementUnit-" + identifier);
        dimension.setValueDate(new Date().toString());
        PoxPayloadOut multipart = DimensionFactory.createDimensionInstance(client.getCommonPartName(),
                dimension);
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.CREATED.getStatusCode());
        knownResourceId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
        
        // Now verify that elmo cannot create
        client = new DimensionClient();
        client.setAuth(true, "elmo2010", true, "elmo2010", true);
        res = client.create(multipart);

        statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " (verify not allowed): status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.FORBIDDEN.getStatusCode());
        
        //Finally, verify that elmo has no access to Intakes
        // Submit the request to the service and store the response.
        IntakeClient iclient = new IntakeClient();
        iclient.setAuth(true, "elmo2010", true, "elmo2010", true);
        multipart = createIntakeInstance(
                "entryNumber-" + identifier,
                "entryDate-" + identifier,
                "depositor-" + identifier);
        res = iclient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " (verify create intake not allowed): status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.FORBIDDEN.getStatusCode());
        
    }
    
    /**
     * Creates the intake instance.
     *
     * @param entryNumber the entry number
     * @param entryDate the entry date
     * @param depositor the depositor
     * @return the multipart output
     */
    private PoxPayloadOut createIntakeInstance(String entryNumber,
    		String entryDate,
    		String depositor) {
        IntakesCommon intake = new IntakesCommon();
        intake.setEntryNumber(entryNumber);
        intake.setEntryDate(entryDate);
        intake.setDepositor(depositor);

        PoxPayloadOut multipart = new PoxPayloadOut(IntakeClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart =
            multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new IntakeClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, intake common");
            logger.debug(objectAsXmlString(intake, IntakesCommon.class));
        }

        return multipart;
    }
    
    @Test(dataProvider = "testName",
    	    dependsOnMethods = {"delete"})
    public void verifyCreateWithFlippedRoles(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        flipInitialAccountRoles();

        // Now verify that elmo can create
        client.setAuth(true, "elmo2010", true, "elmo2010", true);
        client = new DimensionClient();
        
        String identifier = createIdentifier();
        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        dimension.setMeasurementUnit("measurementUnit-" + identifier);
        dimension.setValueDate(new Date().toString());
        PoxPayloadOut multipart = DimensionFactory.createDimensionInstance(client.getCommonPartName(),
                dimension);
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.CREATED.getStatusCode());
        knownResourceId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
        
        //bigbird no longer allowed to create
        client.setAuth(true, "bigbird2010", true, "bigbird2010", true);
        res = client.create(multipart);

        statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " (verify not allowed): status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.FORBIDDEN.getStatusCode());
        restoreInitialAccountRoles();
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //elmo allowed to read
        client.setAuth(true, "elmo2010", true, "elmo2010", true);
        ClientResponse<String> res = client.read(knownResourceId);
        try {
        	assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        DimensionsCommon dimension = (DimensionsCommon) extractPart(input,
	                client.getCommonPartName(), DimensionsCommon.class);
	        Assert.assertNotNull(dimension);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Test(dataProvider = "testName",
    	    dependsOnMethods = {"read"})
    public void readLockedOut(String testName) throws Exception {
        // Perform setup.
    	setupForbidden();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        //lockedOut allowed to read
        client.setAuth(true, "lockedOut", true, "lockedOut", true);
        ClientResponse<String> res = client.read(knownResourceId);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"read"})
    public void updateNotAllowed(String testName) throws Exception {
        setupForbidden();

        // Create a new client and change its AuthN credentials
        DimensionClient client = new DimensionClient();
        //elmo not allowed to update
        client.setAuth(true, "elmo2010", true, "elmo2010", true);
        //
        // Create a new dimension object
        //
        DimensionsCommon dimension = new DimensionsCommon();
        dimension.setDimension("dimensionType");
        // Update the content of this resource.
        dimension.setMeasurementUnit("updated-" + dimension.getMeasurementUnit());
        dimension.setValueDate("updated-" + dimension.getValueDate());
        //
        // Create and submit the request to the service and store the response.
        //
        PoxPayloadOut output = new PoxPayloadOut(DimensionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), dimension);
        ClientResponse<String> res = client.update(knownResourceId, output);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Create another new client with new credentials
        //
        client = new DimensionClient();
        //lockedOut not allowed to update
        client.setAuth(true, "lockedOut", true, "lockedOut", true);
        //
        // Try the update again.
        //
        res = client.update(knownResourceId, output);
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
    		dependsOnMethods = {"updateNotAllowed"})
    public void deleteNotAllowed(String testName) throws Exception {
        // Perform setup.
        setupForbidden();
        //
        // Create a new client and change the AuthN credentials
        //
        DimensionClient client = new DimensionClient();
        //bigbird can not delete
        client.setAuth(true, "bigbird2010", true, "bigbird2010", true);
        //
        // Try to make a DELETE request
        //
        ClientResponse<Response> res = client.delete(knownResourceId);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Test(dataProvider = "testName",
    	dependsOnMethods = {"deleteNotAllowed"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();

        ClientResponse<Response> res = client.delete(knownResourceId);
        try {
        	assertStatusCode(res, testName);
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

        List<RoleValue> elmoRoleValues = new ArrayList<RoleValue>();
        elmoRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
        if(!accountRolesFlipped) {
	        deleteAccountRole(accValues.get("bigbird2010"), bigbirdRoleValues);
	        deleteAccountRole(accValues.get("elmo2010"), elmoRoleValues);
        } else {
	        deleteAccountRole(accValues.get("bigbird2010"), elmoRoleValues);
	        deleteAccountRole(accValues.get("elmo2010"),bigbirdRoleValues );
        }
    }
    
    private void flipInitialAccountRoles() {
        if(!accountRolesFlipped) {
	        List<RoleValue> cmRoleValues = new ArrayList<RoleValue>();
	        List<RoleValue> internRoleValues = new ArrayList<RoleValue>();
	        cmRoleValues.add(roleValues.get("ROLE_TEST_CM"));
	        internRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
	        
	        deleteAccountRole(accValues.get("bigbird2010"), cmRoleValues);
	        deleteAccountRole(accValues.get("elmo2010"), internRoleValues);

	        createAccountRole(accValues.get("bigbird2010"), internRoleValues);
	        createAccountRole(accValues.get("elmo2010"), cmRoleValues);
	        
	        accountRolesFlipped = true;
        }
    }

    private void restoreInitialAccountRoles() {
        if(accountRolesFlipped) {
	        List<RoleValue> cmRoleValues = new ArrayList<RoleValue>();
	        List<RoleValue> internRoleValues = new ArrayList<RoleValue>();
	        cmRoleValues.add(roleValues.get("ROLE_TEST_CM"));
	        internRoleValues.add(roleValues.get("ROLE_TEST_INTERN"));
	        
	        deleteAccountRole(accValues.get("bigbird2010"), internRoleValues);
	        deleteAccountRole(accValues.get("elmo2010"), cmRoleValues);

	        createAccountRole(accValues.get("bigbird2010"), internRoleValues);
	        createAccountRole(accValues.get("elmo2010"), cmRoleValues);
	        accountRolesFlipped = false;
        }
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
    	String result = null;
    	
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        Permission permission = PermissionFactory.createPermissionInstance(resName,
                "default permissions for " + resName, actions, effect, true, true, true);
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

    private void deletePermission(String permId) {
        setupDelete();
        PermissionClient permClient = new PermissionClient();
        ClientResponse<Response> res = permClient.delete(permId);
        try {
        	assertStatusCode(res, "DeletePermission");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createRole(String roleName) {
    	String result = null;
    	
        setupCreate();
        RoleClient roleClient = new RoleClient();
        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
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

    private void deleteRole(String roleId) {
        setupDelete();
        RoleClient roleClient = new RoleClient();
        ClientResponse<Response> res = roleClient.delete(roleId);
        try {
        	assertStatusCode(res, "DeleteRole");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createAccount(String userName, String email) {
    	String result = null;
    	
        setupCreate();
        AccountClient accountClient = new AccountClient();
        AccountsCommon account = AccountFactory.createAccountInstance(
                userName, userName, userName, email, accountClient.getTenantId(),
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

    private void deleteAccount(String accId) {
        setupDelete();
        AccountClient accClient = new AccountClient();
        ClientResponse<Response> res = accClient.delete(accId);
        try {
        	assertStatusCode(res, "DeleteAccount");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    private String createAccountRole(AccountValue av,
            Collection<RoleValue> rvs) {
    	String result = null;
    	
        setupCreate();
        // Submit the request to the service and store the response.
        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                av, rvs, true, true);
        AccountRoleClient client = new AccountRoleClient();
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

    private void deleteAccountRole(AccountValue av,
            Collection<RoleValue> rvs) {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
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

    private String createPermissionRole(PermissionValue pv, Collection<RoleValue> rvs) {
    	String result = null;
    	
        setupCreate();
        List<RoleValue> rvls = new ArrayList<RoleValue>();
        rvls.addAll(rvs);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                pv, rvls, true, true);
        PermissionRoleClient client = new PermissionRoleClient();
        ClientResponse<Response> res = client.create(pv.getPermissionId(), permRole);
        try {
        	assertStatusCode(res, "CreatePermissionRole");
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
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
        try {
        	assertStatusCode(res, "DeletePermissionRole");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

	@Override
	protected Class<AbstractCommonList> getCommonListType() {
		throw new UnsupportedOperationException();
	}
}
