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

import java.util.List;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.account.AccountListItem;

import org.collectionspace.services.account.Status;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountServiceTest, carries out tests against a
 * deployed and running Account Service.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class AccountServiceTest extends AbstractServiceTestImpl<AccountsCommonList, AccountsCommon, AccountsCommon, AccountsCommon> {

    /** The Constant logger. */
    private final String CLASS_NAME = AccountServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    private String prebuiltAdminCSID = null;
    private String prebuiltAdminUserId = "admin@core.collectionspace.org";
    private String knownUserId = "barney";
    private String knownUserPassword = "hithere08";
    /** The add tenant. */
    static boolean addTenant = true;

    @Override
    public String getServiceName() {
        return AccountClient.SERVICE_NAME;
    }
    
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return new AccountClient().getServicePathComponent();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new AccountClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AccountsCommonList getCommonList(
            ClientResponse<AccountsCommonList> response) {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
        throw new UnsupportedOperationException();
    }
    
    protected Class<AccountsCommonList> getCommonListType() {
    	return (Class<AccountsCommonList>) AccountsCommonList.class;
    }    

    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    
    /**
     * Creates the for unique user.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void createForUniqueUser(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("barney1", knownUserId, knownUserPassword,
                "barney@dinoland.com",
                client.getTenantId(), true, false, true, true);

        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Creates the with invalid tenant.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithInvalidTenant(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop@dinoland.com",
                client.getTenantId(), true, true, true, true);
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Creates the without user.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithoutUser(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop@dinoland.com",
                client.getTenantId(), true, false, false, true);
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Creates the with invalid email.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithInvalidEmail(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop.dinoland.com",
                client.getTenantId(), true, false, true, true);
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Creates the without screen name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithoutScreenName(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop@dinoland.com",
                client.getTenantId(), false, false, true, true);
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Creates the with invalid password.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithInvalidPassword(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "shpswd", "babybop@dinoland.com",
                client.getTenantId(), true, false, true, true);
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Creates the with most invalid.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithMostInvalid(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop/dinoland.com",
                client.getTenantId(), false, true, false, false);
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    //
    // To avoid uniqueness violations for accounts, createList is removed
    //
    @Override
    public void createList(String testName) throws Exception {
        setupCreate();
        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account1 =
                createAccountInstance("curious", "curious", "hithere08", "curious@george.com",
                client.getTenantId(), true, false, true, true);
        ClientResponse<Response> res = client.create(account1);
        int statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        allResourceIdsCreated.add(extractId(res));

        AccountsCommon account2 =
                createAccountInstance("tom", "tom", "hithere09", "tom@jerry.com",
                client.getTenantId(), true, false, true, true);
        res = client.create(account2);
        statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        allResourceIdsCreated.add(extractId(res));

        AccountsCommon account3 =
                createAccountInstance("mj", "mj", "hithere10", "mj@dinoland.com",
                client.getTenantId(), true, false, true, true);
        res = client.create(account3);
        statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        allResourceIdsCreated.add(extractId(res));
    }

    //
    // Tests with expected failure outcomes
    //
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    /**
     * Search screen name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void searchScreenName(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res =
                client.readSearchList("tom", null, null);
        try {
	        assertStatusCode(res, testName);	        
	        AccountsCommonList list = res.getEntity();	
	        Assert.assertEquals(1, list.getAccountListItem().size());
	        // Optionally output additional data about list members for debugging.
	        boolean iterateThroughList = true;
	        if (iterateThroughList && logger.isDebugEnabled()) {
	            printList(testName, list);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Override
    @Test(dataProvider = "testName")
    public void searchWorkflowDeleted(String testName) throws Exception {
        // Fixme: null test for now, overriding test in base class
    }

    /**
     * Search user id.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void searchUserId(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res = client.readSearchList(null, "tom", null);
        try {
	        assertStatusCode(res, testName);	        
	        AccountsCommonList list = res.getEntity();
	        Assert.assertEquals(1, list.getAccountListItem().size());
	        // Optionally output additional data about list members for debugging.
	        boolean iterateThroughList = true;
	        if (iterateThroughList && logger.isDebugEnabled()) {
	            printList(testName, list);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Search email.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void searchEmail(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res = client.readSearchList(null, null, "dinoland");
        try {
	        assertStatusCode(res, testName);	        
	        AccountsCommonList list = res.getEntity();
	        Assert.assertEquals(2, list.getAccountListItem().size());
	        // Optionally output additional data about list members for debugging.
	        boolean iterateThroughList = true;
	        if (iterateThroughList && logger.isDebugEnabled()) {
	            printList(testName, list);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Search screen name email.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void searchScreenNameEmail(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res = client.readSearchList("tom", null, "jerry");
        try {
	        assertStatusCode(res, testName);
	        AccountsCommonList list = res.getEntity();
	        Assert.assertEquals(1, list.getAccountListItem().size());
	        // Optionally output additional data about list members for debugging.
	        boolean iterateThroughList = true;
	        if (iterateThroughList && logger.isDebugEnabled()) {
	            printList(testName, list);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------

    /**
     * Update password.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void updatePassword(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": got object to update password with ID: " + knownResourceId);
        }
        AccountsCommon accountFound =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(accountFound);

        //create a new account object to test partial updates
        AccountsCommon accountToUpdate = new AccountsCommon();
        accountToUpdate.setCsid(knownResourceId);
        accountToUpdate.setUserId(accountFound.getUserId());
        //change password
        accountToUpdate.setPassword("imagination".getBytes());
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": updated object");
            logger.debug(objectAsXmlString(accountToUpdate,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, accountToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        AccountsCommon accountUpdated = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(accountUpdated);

//        Assert.assertEquals(accountUpdated.getPassword(),
//                accountFound.getPassword(),
//                "Data in updated object did not match submitted data.");
    }

    /**
     * Update password without user.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void updatePasswordWithoutUser(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        AccountsCommon accountToUpdate = new AccountsCommon();
        accountToUpdate.setCsid(knownResourceId);
        accountToUpdate.setUserId(null);
        //change password
        accountToUpdate.setPassword("imagination".getBytes());
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " : updated object");
            logger.debug(objectAsXmlString(accountToUpdate,
                    AccountsCommon.class));
        }

        AccountClient client = new AccountClient();
        // Submit the request to the service and store the response.
        ClientResponse<AccountsCommon> res = client.update(knownResourceId, accountToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

    }

    /**
     * Update invalid password.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void updateInvalidPassword(String testName) throws Exception {
        // Perform setup.
        setupUpdate();
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": got object to update password with ID: " + knownResourceId);
        }
        AccountsCommon accountFound = (AccountsCommon) res.getEntity();

        AccountsCommon accountToUpdate = new AccountsCommon();
        accountToUpdate.setCsid(knownResourceId);
        accountToUpdate.setUserId(accountFound.getUserId());
        Assert.assertNotNull(accountToUpdate);

        //change password
        accountToUpdate.setPassword("abc123".getBytes());
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": updated object");
            logger.debug(objectAsXmlString(accountToUpdate,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, accountToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
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
    
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
	public void verifyMetadataProtection(String testName) throws Exception {
    	findPrebuiltAdminAccount();
    	// Try to update the metadata - it should just get ignored
        // Perform setup.
        setupUpdate();

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(prebuiltAdminCSID);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        if (logger.isDebugEnabled()) {
            logger.debug("Did get on Admin Account to update with ID: " + prebuiltAdminCSID);
        }
        AccountsCommon accountFound = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(accountFound);

        //create a new account object to test partial updates
        AccountsCommon accountToUpdate = new AccountsCommon();
        accountToUpdate.setCsid(prebuiltAdminCSID);
        accountToUpdate.setUserId(accountFound.getUserId());
        // Update the content of this resource.
        accountToUpdate.setEmail("updated-" + accountFound.getEmail());
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(accountFound,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(prebuiltAdminCSID, accountToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        // Note that the error is not returned, it is just ignored
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        AccountsCommon accountUpdated = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(accountUpdated);

    	Assert.assertFalse(accountUpdated.getEmail().equals(accountToUpdate.getEmail()),
    		"Admin Account (with metadata lock) allowed update to change the email!");
    }
    
    
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
	public void verifyProtectionReadOnly(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        AccountsCommon account = createAccountInstance("mdTest", "mdTest", "mdTestPW", "md@test.com", 
        			client.getTenantId(), true, false, true, true);
        account.setMetadataProtection(AccountClient.IMMUTABLE);
        account.setRolesProtection(AccountClient.IMMUTABLE);
        ClientResponse<Response> res = client.create(account);
        String testResourceId = null;
        try {
        	assertStatusCode(res, testName);
	        // Store the ID returned from this create operation
	        // for additional tests below.
	        testResourceId = extractId(res);
	        allResourceIdsCreated.add(testResourceId);
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": testResourceId=" + testResourceId);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        setupRead();

        // Submit the request to the service and store the response.
        ClientResponse<AccountsCommon> accountRes = client.read(testResourceId);
        try {
	        assertStatusCode(accountRes, testName);
	        AccountsCommon accountRead = (AccountsCommon) accountRes.getEntity();
	        Assert.assertNotNull(accountRead);
	        String mdProtection = accountRead.getMetadataProtection();
	        String rolesProtection = accountRead.getRolesProtection();
	        if (logger.isTraceEnabled()) {
	            logger.trace(testName + ": metadataProtection=" + mdProtection);
	            logger.trace(testName + ": rolesProtection=" + rolesProtection);
	        }
	    	Assert.assertFalse(account.getMetadataProtection().equals(mdProtection),
	    			"Account allowed create to set the metadata protection flag.");
	    	Assert.assertFalse(account.getRolesProtection().equals(rolesProtection),
	    			"Account allowed create to set the perms protection flag.");
        } finally {
        	if (accountRes != null) {
        		accountRes.releaseConnection();
            }
        }
        
    	setupUpdate();

    	AccountsCommon accountToUpdate = createAccountInstance("mdTest", "mdTest", "mdTestPW", "md@test.com", 
		    			client.getTenantId(), true, false, true, true);
    	accountToUpdate.setMetadataProtection(AccountClient.IMMUTABLE);
    	accountToUpdate.setRolesProtection(AccountClient.IMMUTABLE);

    	// Submit the request to the service and store the response.
    	accountRes = client.update(testResourceId, accountToUpdate);
    	try {
    		assertStatusCode(accountRes, testName);
	    	AccountsCommon accountUpdated = (AccountsCommon) accountRes.getEntity();
	    	Assert.assertNotNull(accountUpdated);
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + "Updated account: ");
	            logger.debug(objectAsXmlString(accountUpdated,AccountsCommon.class));
	        }
	    	Assert.assertFalse(
	    			AccountClient.IMMUTABLE.equalsIgnoreCase(accountUpdated.getMetadataProtection()),
	    			"Account allowed update of the metadata protection flag.");
	    	Assert.assertFalse(
	    			AccountClient.IMMUTABLE.equalsIgnoreCase(accountUpdated.getRolesProtection()),
	    			"Account allowed update of the roles protection flag.");
    	} finally {
    		if (accountRes != null) {
    			accountRes.releaseConnection();
            }
    	}
    }

    /**
     * Deactivate.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"updatePasswordWithoutUser"})
    public void deactivate(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        AccountsCommon accountFound = (AccountsCommon) res.getEntity();

        //create a new account object to test partial updates
        AccountsCommon accountToUpdate = new AccountsCommon();
        accountToUpdate.setCsid(knownResourceId);
        accountToUpdate.setUserId(accountFound.getUserId());

        // Update the content of this resource.
        accountToUpdate.setStatus(Status.INACTIVE);
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(accountToUpdate,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, accountToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        AccountsCommon accountUpdated = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(accountUpdated);

        Assert.assertEquals(accountUpdated.getStatus(),
                accountToUpdate.getStatus(),
                "Data in updated object did not match submitted data.");
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"deactivate", "CRUDTests"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updateAccount(), below.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("simba", "simba", "tiger", "simba@lionking.com",
                client.getTenantId(), true, false, true, true);
        ClientResponse<AccountsCommon> res =
                client.update(NON_EXISTENT_ID, account);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
    }

    /**
     * Update wrong user.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"deactivate", "CRUDTests"})
    public void updateWrongUser(String testName) throws Exception {
        setupUpdate();

        // Submit the request to the service and store the response.
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updateAccount(), below.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        AccountsCommon accountToUpdate =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(accountToUpdate);

        accountToUpdate.setUserId("barneyFake");
        if (logger.isDebugEnabled()) {
            logger.debug("updated object with wrongUser");
            logger.debug(objectAsXmlString(accountToUpdate,
                    AccountsCommon.class));
        }

        res = client.update(knownResourceId, accountToUpdate);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    
    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing because this test is not ready to delete the "knownResourceId".
    	// Instead, the method localDelete() will get called later in the dependency chain. The
    	// method localDelete() has a dependency on the test "updateWrongUser".  Once the "updateWrongUser"
    	// test is run, the localDelete() test/method will get run.  The localDelete() test/method in turn
    	// calls the inherited delete() test/method.
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests", "updateWrongUser"})
    public void localDelete(String testName) throws Exception {
    	// Because of issues with TestNG not allowing @Test annotations on on override methods,
    	// and because we want the "updateWrongUser" to run before the "delete" test, we need
    	// this method.  This method will call super.delete() after all the dependencies have been
    	// met.
    	super.delete(testName);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * create account instance
     * @param screenName
     * @param userName
     * @param passwd
     * @param email
     * @param useScreenName
     * @param invalidTenant
     * @param useUser
     * @param usePassword
     * @return
     */
    AccountsCommon createAccountInstance(String screenName,
            String userName, String passwd, String email, String tenantId,
            boolean useScreenName, boolean invalidTenant, boolean useUser, boolean usePassword) {

        AccountsCommon account = AccountFactory.createAccountInstance(screenName,
                userName, passwd, email, tenantId, useScreenName,
                invalidTenant, useUser, usePassword);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, account common");
            logger.debug(objectAsXmlString(account,
                    AccountsCommon.class));
        }
        
        return account;
    }


    /**
     * Prints the list.
     *
     * @param testName the test name
     * @param list the list
     */
    @Override
    protected void printList(String testName, AccountsCommonList list) {
    	AccountsCommonList acl = (AccountsCommonList)list;
        List<AccountListItem> items =
                acl.getAccountListItem();
        int i = 0;
        for (AccountListItem item : items) {
            logger.debug(testName + ": list-item[" + i + "] csid="
                    + item.getCsid());
            logger.debug(testName + ": list-item[" + i + "] screenName="
                    + item.getScreenName());
            logger.debug(testName + ": list-item[" + i + "] URI="
                    + item.getUri());
            i++;
        }
    }

	@Override
	protected AccountsCommon createInstance(String commonPartName,
			String identifier) {
		AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance(knownUserId, knownUserId, knownUserPassword,
                "barney@dinoland.com", client.getTenantId(),
                true, false, true, true);
        return account;
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
	protected AccountsCommon updateInstance(AccountsCommon accountsCommon) {
		AccountsCommon result = new AccountsCommon();
		
		result.setCsid(knownResourceId);
		result.setUserId(accountsCommon.getUserId());
        // Update the content of this resource.
		result.setEmail("updated-" + accountsCommon.getEmail());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(AccountsCommon original,
			AccountsCommon updated) throws Exception {
        Assert.assertEquals(original.getEmail(), updated.getEmail(),
                "Data in updated object did not match submitted data.");
	}
	
}
