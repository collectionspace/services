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
package org.collectionspace.services.authorization.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RolesList;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;

/**
 * RoleServiceTest, carries out tests against a
 * deployed and running Role Service.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class RoleServiceTest extends AbstractServiceTestImpl {

    private final Logger logger =
            LoggerFactory.getLogger(RoleServiceTest.class);
    // Instance variables specific to this test.
    private String knownResourceId = null;
    private List<String> allResourceIdsCreated = new ArrayList();
    boolean addTenant = true;
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    @Override
    protected String getServicePathComponent() {
        return new RoleClient().getServicePathComponent();
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
        RoleClient client = new RoleClient();
        Role role = createRoleInstance("ROLE_USERS_TEST",
                "all users are required to be in this role",
                true);
        ClientResponse<Response> res = client.create(role);
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
    public void createForUniqueRole(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance("ROLE_USERS",
                "role users",
                true);
        ClientResponse<Response> res = client.create(role);
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
    public void createWithoutRoleName(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance("ROLE_USERS",
                "role for users",
                false);
        ClientResponse<Response> res = client.create(role);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    //to not cause uniqueness violation for role, createList is removed
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
         RoleClient client = new RoleClient();
       Role role1 = createRoleInstance("ROLE_COLLECTIONS_MANGER_TEST",
                "collection manager",
                true);
        ClientResponse<Response> res = client.create(role1);
        int statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));

        Role role2 = createRoleInstance("ROLE_COLLECTIONS_CURATOR_TEST",
                "collections curator",
                true);
        res = client.create(role2);
        statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));

        Role role3 = createRoleInstance("ROLE_MOVINGIMAGE_ADMIN_TEST",
                "moving image admin",
                true);
        res = client.create(role3);
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
        RoleClient client = new RoleClient();
        ClientResponse<Role> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        Role output = (Role) res.getEntity();
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
        RoleClient client = new RoleClient();
        ClientResponse<Role> res = client.read(NON_EXISTENT_ID);
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
        RoleClient client = new RoleClient();
        ClientResponse<RolesList> res = client.readList();
        RolesList list = res.getEntity();
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
    public void searchRoleName(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<RolesList> res = client.readSearchList("movingImage");
        RolesList list = res.getEntity();
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        int EXPECTED_ITEMS = 1;
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": received = " + list.getRoles().size()
                    + " expected=" + EXPECTED_ITEMS);
        }
        Assert.assertEquals(EXPECTED_ITEMS, list.getRoles().size());
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

        Role roleToUpdate = new Role();
        roleToUpdate.setCsid(knownResourceId);

        // Update the content of this resource.
        roleToUpdate.setRoleName("updated-role");
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(roleToUpdate,
                    Role.class));
        }
        RoleClient client = new RoleClient();
        // Submit the request to the service and store the response.
        ClientResponse<Role> res = client.update(knownResourceId, roleToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


        Role roleUpdated = (Role) res.getEntity();
        Assert.assertNotNull(roleUpdated);

        Assert.assertEquals(roleUpdated.getRoleName(),
                roleToUpdate.getRoleName(),
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
    dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updateRole(), below.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance("ROLE_XXX",
                "xxx",
                true);
        ClientResponse<Role> res =
                client.update(NON_EXISTENT_ID, role);
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
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
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
        RoleClient client = new RoleClient();
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
     * create role instance
     * @param roleName
     * @param description
     * @param useRoleName
     * @return
     */
    public Role createRoleInstance(String roleName,
            String description,
            boolean useRoleName) {

        Role role = RoleFactory.createRoleInstance(roleName, description,
                useRoleName);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, role common");
            logger.debug(objectAsXmlString(role, Role.class));
        }
        return role;

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        setupDelete("delete");
        String noTest = System.getProperty("noTestCleanup");
    	if(Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
    	}
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        RoleClient client = new RoleClient();
        for (String resourceId : allResourceIdsCreated) {
            ClientResponse<Response> res = client.delete(resourceId);
            int statusCode = res.getStatus();
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        }
    }

    private int printList(String testName, RolesList list) {

        int i = 0;

        for (Role role : list.getRoles()) {
            logger.debug(testName + " role csid=" + role.getCsid()
                    + " name=" + role.getRoleName()
                    + " desc=" + role.getDescription());
            i++;
        }
        return i;
    }
}
