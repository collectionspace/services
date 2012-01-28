/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.account.client.test;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.account.AccountListItem;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.client.AccountRoleClient;
import org.collectionspace.services.client.AccountRoleFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountServiceTest, carries out tests against a
 * deployed and running Account, Role and AccountRole Services.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class AccountRoleServiceTest extends AbstractServiceTestImpl<AccountRole, AccountRole, AccountRole, AccountRole> {

    /** The Constant logger. */
    private final static String CLASS_NAME = AccountRoleServiceTest.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    private String prebuiltAdminCSID = null;
    private String prebuiltAdminUserId = "admin@core.collectionspace.org";
    /** The all resource ids created. */
    /** The acc values. */
    private Hashtable<String, AccountValue> accValues = new Hashtable<String, AccountValue>();
    /** The role values. */
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return new AccountRoleClient().getServicePathComponent();
    }

    /**
     * Seed data.
     */
    @BeforeClass(alwaysRun = true)
    public void seedData() {
        String userId = "acc-role-user1";
        String accId = createAccount(userId, "acc-role-user1-test@cspace.org");
        AccountValue ava = new AccountValue();
        ava.setScreenName(userId);
        ava.setUserId(userId);
        ava.setAccountId(accId);
        accValues.put(ava.getScreenName(), ava);

        String userId2 = "acc-role-user2";
        String coAccId = createAccount(userId2, "acc-role-user2-test@cspace.org");
        AccountValue avc = new AccountValue();
        avc.setScreenName(userId2);
        avc.setUserId(userId2);
        avc.setAccountId(coAccId);
        accValues.put(avc.getScreenName(), avc);

        String rn1 = "ROLE_CO1";
        String r1RoleId = createRole(rn1);
        RoleValue rv1 = new RoleValue();
        rv1.setRoleId(r1RoleId);
        rv1.setRoleName(rn1);
        roleValues.put(rv1.getRoleName(), rv1);

        String rn2 = "ROLE_CO2";
        String r2RoleId = createRole(rn2);
        RoleValue rv2 = new RoleValue();
        rv2.setRoleId(r2RoleId);
        rv2.setRoleName(rn2);
        roleValues.put(rv2.getRoleName(), rv2);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new AccountRoleClient();
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    
    public void createList(String testName) throws Exception {
    	// Do nothing.  We do not support list creation in this test yet.
    }
    
	@Override
	protected AccountRole createInstance(String commonPartName,
			String identifier) {
		AccountRole result = null;
		
        // Use a known Account to associate the AccountRole instance to
        AccountValue av = accValues.get("acc-role-user1");
        AccountRole accRole = createAccountRoleInstance(av,
                roleValues.values(), true, true);
        
        result = accRole;
        return result;
	}
	
	@Override
	public void create(String testName) throws Exception {
		setupCreate();
        AccountValue av = accValues.get("acc-role-user1");
        AccountRole accRole = createAccountRoleInstance(av,
                roleValues.values(), true, true);
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<Response> res = client.create(av.getAccountId(), accRole);
        try {
        	assertStatusCode(res, testName);
            knownResourceId = av.getAccountId();
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": Created an AccountRole instance for account with knownResourceId="
                		+ knownResourceId);
            }
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
	}

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void readNoRelationship(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<AccountRole> res = client.read(
                accValues.get("acc-role-user2").getAccountId());
        try {
            // Check the status code of the response: does it match
            // the expected response(s)?
            assertStatusCode(res, testName);
            AccountRole output = res.getEntity();
            if(logger.isDebugEnabled()) {
            	org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();
            	String sOutput = objectAsXmlString(objectFactory.createAccountRole(output), AccountRole.class);
                logger.debug(testName + " received " + sOutput);
            }
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
    }
    
    /*
     * In this test, for setup, we associate both test roles ("ROLE_CO1", "ROLE_CO2") with the test account "acc-role-user2".
     * After we've performed this setup, our call to "/role/{csid}/accountroles" should contain an AccountRole that has
     * a list of 1 account -the test user account we associated during setup.
     */
    @Test(dataProvider = "testName",
    	    dependsOnMethods = {"CRUDTests"})
    public void readRoleAccounts(String testName) throws Exception {
		/*
		 * Setup a temp local scope for local variables that we need to create the AccountRole for
		 * the setup of the read tests.
		 */
        {
	        // Associate "acc-role-user2" with all the roles.
	        AccountValue av = accValues.get("acc-role-user2");
	        AccountRole accRole = createAccountRoleInstance(av,
	                roleValues.values(), true, true);
	        AccountRoleClient client = new AccountRoleClient();
	        setupCreate();	        
	        ClientResponse<Response> res = client.create(av.getAccountId(), accRole);
	        try {
	        	assertStatusCode(res, testName);
	        } finally {
	        	if (res != null) {
	        		res.releaseConnection();
	        	}
	        }
        }

        //
        // Now read the list of accounts associated with the role "ROLE_CO1".
        // There should be just the "acc-role-user2" account.
        //
        RoleClient roleClient = new RoleClient();
        
        // Submit the request to the service and store the response.
        setupRead();        
        ClientResponse<AccountRole> res = roleClient.readRoleAccounts(
        		roleValues.get("ROLE_CO1").getRoleId());
        try {
            // Check the status code of the response: does it match
            // the expected response(s)?
            assertStatusCode(res, testName);
            AccountRole output = res.getEntity();
            
            // Now verify that the role has 2 accounts associate to it.
            Assert.assertEquals(output.getAccount().size(), 1);
            String sOutput = objectAsXmlString(output, AccountRole.class);
            if(logger.isDebugEnabled()) {
                logger.debug(testName + " received " + sOutput);
            }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }    

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    public void readList(String testName) throws Exception {
        //
    	// There is no such thing as a list of AccountRole resources
    	//
    }
    
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //
    	// There is no such thing as a list of AccountRole resources
    	//
    }    

    // Failure outcomes
    // None at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    public void update(String testName) throws Exception {
        //
    	// AccountRole entities cannot be updated.  You must delete and recreate them
    	//
    }

    @Override
    @Test(dataProvider = "testName",
    	dependsOnMethods = {"CRUDTests"})
    public void updateNonExistent(String testName) throws Exception {
        //
    	// AccountRole entities cannot be updated.  You must delete and recreate them
    	//
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    
    @Override
    public void delete(String testName) throws Exception {
        //
        // First, lookup the known account and delete all of its role relationships
        //
        AccountRoleClient client = new AccountRoleClient();
        setupRead();        
        ClientResponse<AccountRole> readResponse = client.read(
                accValues.get("acc-role-user1").getAccountId());
        AccountRole toDelete = null;
        try {
        	assertStatusCode(readResponse, testName);
        	toDelete = readResponse.getEntity();
        	Assert.assertNotNull(toDelete);
        } finally {
        	if (readResponse != null) {
        		readResponse.releaseConnection();
        	}
        }

        setupDelete();                
        ClientResponse<Response> res = client.delete(
                toDelete.getAccount().get(0).getAccountId(), toDelete); // delete form #1
        try {
        	assertStatusCode(readResponse, testName);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
        //
        // Recreate 'acc-role-user1' account and roles for the next test
        //
        create(testName);
                
        //
        // Lookup a known account and delete all of its role relationships again
        //
        setupRead();
        readResponse = client.read(
        		accValues.get("acc-role-user1").getAccountId());
        toDelete = null;
        try {
        	toDelete = readResponse.getEntity();
        } finally {
        	if (readResponse != null) {
        		readResponse.releaseConnection();
        	}
        }

        setupDelete();        
        res = client.delete(toDelete.getAccount().get(0).getAccountId()); // delete form #2
        try {
            int statusCode = res.getStatus();
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            res.releaseConnection();
        }
    }

    @Test(dataProvider = "testName",
    	    dependsOnMethods = {"CRUDTests"})
	public void deleteLockedAccount(String testName) throws Exception {
    	findPrebuiltAdminAccount();

    	// Perform setup.
        testExpectedStatusCode = Response.Status.FORBIDDEN.getStatusCode();
        testRequestType = ServiceRequestType.DELETE;
        testSetup(testExpectedStatusCode, testRequestType);

    	AccountRoleClient client = new AccountRoleClient();
    	ClientResponse<Response> res = client.delete(prebuiltAdminCSID);
    	try {
    		int statusCode = res.getStatus();
    		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
    				invalidStatusCodeMessage(testRequestType, statusCode));
    		Assert.assertEquals(statusCode, testExpectedStatusCode);
    	} finally {
    		if (res != null) {
    			res.releaseConnection();
    		}
    	}
    }

    //
    // Tests with expected failure outcomes
    //

    @Override
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db
    	
    	//FIXME: REM - 1/9/2012, need to find out why a 200 status code is returned and fix this.
    }
    
    // ---------------------------------------------------------------
    // Search tests
    // ---------------------------------------------------------------
    
    @Override
    public void searchWorkflowDeleted(String testName) throws Exception {
        // Fixme: null test for now, overriding test in base class
    }    

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    /**
     * Creates the account role instance.
     *
     * @param av the av
     * @param rvs the rvs
     * @param usePermId the use perm id
     * @param useRoleId the use role id
     * @return the account role
     */
    static public AccountRole createAccountRoleInstance(AccountValue pv,
            Collection<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                pv, rvs, usePermId, useRoleId);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, accRole common");
            org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();
            logger.debug(objectAsXmlString(objectFactory.createAccountRole(accRole), AccountRole.class));
        }
        return accRole;
    }

    /**
     * Clean up.
     */
    @AfterClass(alwaysRun = true)
    @Override
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

        for (AccountValue pv : accValues.values()) {
            deleteAccount(pv.getAccountId());
        }

        for (RoleValue rv : roleValues.values()) {
            deleteRole(rv.getRoleId());
        }
    }

    /**
     * Creates the account.
     *
     * @param userName the user name
     * @param email the email
     * @return the string
     */
    private String createAccount(String userName, String email) {
        AccountClient accClient = new AccountClient();
        AccountsCommon account = AccountFactory.createAccountInstance(
                userName, userName, userName, email, accClient.getTenantId(),
                true, false, true, true);
        String result = null;
        
        setupCreate();
        ClientResponse<Response> res = accClient.create(account);
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

    private void findPrebuiltAdminAccount() {
    	// Search for the prebuilt admin user and then hold its CSID
    	if (prebuiltAdminCSID == null) {
            setupReadList();
            AccountClient client = new AccountClient();
            ClientResponse<AccountsCommonList> res =
                    client.readSearchList(null, this.prebuiltAdminUserId, null);
            try {
	            assertStatusCode(res, "findPrebuiltAdminAccount");
	            AccountsCommonList list = res.getEntity();
	            List<AccountListItem> items = list.getAccountListItem();
	            Assert.assertEquals(1, items.size(), "Found more than one Admin account!");
	            AccountListItem item = items.get(0);
	            prebuiltAdminCSID = item.getCsid();
	            if (logger.isDebugEnabled()) {
	                logger.debug("Found Admin Account with ID: " + prebuiltAdminCSID);
	            }
            } finally {
                if (res != null) {
                    res.releaseConnection();
                }            	
            }
    	}
    }
    
    /**
     * Delete account.
     *
     * @param accId the acc id
     */
    private void deleteAccount(String accId) {
        AccountClient accClient = new AccountClient();
        setupDelete();
        ClientResponse<Response> res = accClient.delete(accId);
        try {
        	assertStatusCode(res, "DeleteAccount");
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
    }

    /**
     * Creates the role.
     *
     * @param roleName the role name
     * @return the string
     */
    private String createRole(String roleName) {
    	String result = null;
    	
        RoleClient roleClient = new RoleClient();
        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
        setupCreate();
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

    /**
     * Delete role.
     *
     * @param roleId the role id
     */
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

	@Override
	protected String getServiceName() {
		// AccountRoles service is a sub-service of the Account service, so we return Account's service name
		return AccountClient.SERVICE_NAME;
	}

    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */
    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
    public void CRUDTests(String testName) {
    	// Do nothing.  Simply here to for a TestNG execution order for our tests
    }

	@Override
	protected AccountRole updateInstance(AccountRole commonPartObject) {
        //
    	// AccountRole entities cannot be updated.  You must delete and recreate them
    	//
		return null;
	}

	@Override
	protected void compareReadInstances(AccountRole original, AccountRole updated)
			throws Exception {
		// FIXME: Should add field checks here.
	}

	@Override
	protected void compareUpdatedInstances(AccountRole original,
			AccountRole updated) throws Exception {
        //
    	// AccountRole entities cannot be updated.  You must delete and recreate them
    	//
	}

	@Override
	protected Class<AccountRole> getCommonListType() {
		return AccountRole.class;
	}
}
