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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.account.Status;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;

/**
 * AccountServiceTest, carries out tests against a
 * deployed and running Account Service.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class AccountServiceTest extends AbstractServiceTestImpl {

    static private final Logger logger =
            LoggerFactory.getLogger(AccountServiceTest.class);
    // Instance variables specific to this test.
    private String knownResourceId = null;
    private List<String> allResourceIdsCreated = new ArrayList();
    static boolean addTenant = true;
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    @Override
    protected String getServicePathComponent() {
        return new AccountClient().getServicePathComponent();
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("barney", "barney", "hithere08", "barney@dinoland.com",
                true, false, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation
        // for additional tests below.
        knownResourceId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createForUniqueUser(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("barney1", "barney", "hithere08", "barney@dinoland.com",
                true, false, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createForUniqueScreenName(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("barney", "otherUser", "hithere08", "barney@dinoland.com",
                true, false, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createWithInvalidTenant(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop@dinoland.com",
                true, true, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createWithoutUser(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop@dinoland.com",
                true, false, false, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createWithInvalidEmail(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop.dinoland.com",
                true, false, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createWithoutScreenName(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop@dinoland.com",
                false, false, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createWithMostInvalid(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("babybop", "babybop", "hithere08", "babybop/dinoland.com",
                false, true, false, false);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    //to not cause uniqueness violation for account, createList is removed
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {

        setupCreate(testName);
        // Submit the request to the service and store the response.
        AccountsCommon account1 =
                createAccountInstance("curious", "curious", "hithere08", "curious@george.com",
                true, false, true, true);
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.create(account1);
        int statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));

        AccountsCommon account2 =
                createAccountInstance("tom", "tom", "hithere09", "tom@jerry.com",
                true, false, true, true);
        res = client.create(account2);
        statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));

        AccountsCommon account3 =
                createAccountInstance("mj", "mj", "hithere10", "mj@dinoland.com",
                true, false, true, true);
        res = client.create(account3);
        statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));
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

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        AccountsCommon output = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(output);
    }

    // Failure outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(NON_EXISTENT_ID);
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

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res = client.readList();
        AccountsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            printList(testName, list);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void searchScreenName(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res =
            client.readSearchList("tom", null, null);
        AccountsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(1, list.getAccountListItem().size());
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            printList(testName, list);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void searchUserId(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res =
            client.readSearchList(null, "tom", null);
        AccountsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(1, list.getAccountListItem().size());
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            printList(testName, list);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void searchEmail(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res =
            client.readSearchList(null, null, "dinoland");
        AccountsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(2, list.getAccountListItem().size());
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            printList(testName, list);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void searchScreenNameEmail(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommonList> res =
            client.readSearchList("tom", null, "jerry");
        AccountsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(1, list.getAccountListItem().size());
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            printList(testName, list);
        }
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

        // Perform setup.
        setupUpdate(testName);

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        AccountsCommon toUpdateAccount =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(toUpdateAccount);

        // Update the content of this resource.
        toUpdateAccount.setEmail("updated-" + toUpdateAccount.getEmail());
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(toUpdateAccount,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, toUpdateAccount);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


        AccountsCommon updatedAccount = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(updatedAccount);

        Assert.assertEquals(updatedAccount.getEmail(),
                toUpdateAccount.getEmail(),
                "Data in updated object did not match submitted data.");
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update"})
    public void updatePassword(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": got object to update password with ID: " + knownResourceId);
        }
        AccountsCommon toUpdateAccount =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(toUpdateAccount);

        //change password
        toUpdateAccount.setPassword("imagination".getBytes());
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": updated object");
            logger.debug(objectAsXmlString(toUpdateAccount,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, toUpdateAccount);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


        AccountsCommon updatedAccount = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(updatedAccount);

//        Assert.assertEquals(updatedAccount.getPassword(),
//                toUpdateAccount.getPassword(),
//                "Data in updated object did not match submitted data.");
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update"})
    public void updatePasswordWithoutUser(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + " : got object to update with ID: " + knownResourceId);
        }
        AccountsCommon toUpdateAccount =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(toUpdateAccount);

        toUpdateAccount.setUserId(null);
        //change password
        toUpdateAccount.setPassword("imagination".getBytes());
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " : updated object");
            logger.debug(objectAsXmlString(toUpdateAccount,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, toUpdateAccount);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"updatePasswordWithoutUser"})
    public void deactivate(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        AccountClient client = new AccountClient();
        ClientResponse<AccountsCommon> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        AccountsCommon toUpdateAccount =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(toUpdateAccount);

        // Update the content of this resource.
        toUpdateAccount.setStatus(Status.INACTIVE);
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(toUpdateAccount,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, toUpdateAccount);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


        AccountsCommon updatedAccount = (AccountsCommon) res.getEntity();
        Assert.assertNotNull(updatedAccount);

        Assert.assertEquals(updatedAccount.getStatus(),
                toUpdateAccount.getStatus(),
                "Data in updated object did not match submitted data.");

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
    dependsOnMethods = {"deactivate", "readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updateAccount(), below.
        AccountClient client = new AccountClient();
        AccountsCommon account =
                createAccountInstance("simba", "simba", "tiger", "simba@lionking.com",
                true, false, true, true);
        ClientResponse<AccountsCommon> res =
            client.update(NON_EXISTENT_ID, account);
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

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"deactivate", "readNonExistent", "testSubmitRequest"})
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
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        AccountsCommon toUpdateAccount =
                (AccountsCommon) res.getEntity();
        Assert.assertNotNull(toUpdateAccount);

        toUpdateAccount.setUserId("barneyFake");
        if (logger.isDebugEnabled()) {
            logger.debug("updated object with wrongUser");
            logger.debug(objectAsXmlString(toUpdateAccount,
                    AccountsCommon.class));
        }

        res = client.update(knownResourceId, toUpdateAccount);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"testSubmitRequest", "updateWrongUser"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        AccountClient client = new AccountClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
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

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() throws Exception {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

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
            String userName, String passwd, String email,
            boolean useScreenName, boolean invalidTenant, boolean useUser, boolean usePassword) {

        AccountsCommon account = AccountFactory.createAccountInstance(screenName,
                userName, passwd, email, useScreenName,
                addTenant, invalidTenant, useUser, usePassword);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, account common");
            logger.debug(objectAsXmlString(account,
                    AccountsCommon.class));
        }
        return account;

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        setupDelete("delete");
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        AccountClient client = new AccountClient();
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId);
            int statusCode = res.getStatus();
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        }
    }

    private void printList(String testName, AccountsCommonList list) {
        List<AccountsCommonList.AccountListItem> items =
                list.getAccountListItem();
        int i = 0;

        for (AccountsCommonList.AccountListItem item : items) {
            logger.debug(testName + ": list-item[" + i + "] csid="
                    + item.getCsid());
            logger.debug(testName + ": list-item[" + i + "] screenName="
                    + item.getScreenName());
            logger.debug(testName + ": list-item[" + i + "] URI="
                    + item.getUri());
            i++;

        }
    }
}
