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
//import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.EffectType;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;

/**
 * PermissionServiceTest, carries out tests against a
 * deployed and running Permission Service.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class PermissionServiceTest extends AbstractServiceTestImpl {

    static private final Logger logger =
            LoggerFactory.getLogger(PermissionServiceTest.class);
    // Instance variables specific to this test.
    private String knownResourceId = null;
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    boolean addTenant = true;
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    @Override
    protected String getServicePathComponent() {
        return new PermissionClient().getServicePathComponent();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new PermissionClient();
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

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

        // Submit the request to the service and store the response.
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = createPermissionInstance("accounts",
                "default permissions for account",
                actions,
                EffectType.PERMIT,
                true,
                true,
                true);
        PermissionClient client = new PermissionClient();
        ClientResponse<Response> res = client.create(permission);
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
    public void createWithoutResourceName(String testName) throws Exception {

        setupCreate(testName);

        // Submit the request to the service and store the response.
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = createPermissionInstance(null,
                "default permissions for account",
                actions,
                EffectType.PERMIT,
                false,
                true,
                true);
        PermissionClient client = new PermissionClient();
        ClientResponse<Response> res = client.create(permission);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    //to not cause uniqueness violation for permission, createList is removed
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {

        setupCreate(testName);
        // Submit the request to the service and store the response.
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission1 = createPermissionInstance("collectionobjects",
                "default permissions for collectionobjects",
                actions,
                EffectType.PERMIT,
                true,
                true,
                true);
        PermissionClient client = new PermissionClient();
        ClientResponse<Response> res = client.create(permission1);
        int statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));

        Permission permission2 = createPermissionInstance("acquisitions",
                "default permissions for acquisitions",
                actions,
                EffectType.PERMIT,
                true,
                true,
                true);
        res = client.create(permission2);
        statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));

        Permission permission3 = createPermissionInstance("ids",
                "default permissions for id service",
                actions,
                EffectType.PERMIT,
                true,
                true,
                true);
        res = client.create(permission3);
        statusCode = res.getStatus();
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        allResourceIdsCreated.add(extractId(res));
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    	//FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    	//FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    	//FIXME: Should this test really be empty?  If so, please comment accordingly.
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
        PermissionClient client = new PermissionClient();
        ClientResponse<Permission> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        Permission output = (Permission) res.getEntity();
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
        PermissionClient client = new PermissionClient();
        ClientResponse<Permission> res = client.read(NON_EXISTENT_ID);
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
        PermissionClient client = new PermissionClient();
        ClientResponse<PermissionsList> res = client.readList();
        PermissionsList list = res.getEntity();
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
    public void searchResourceName(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        PermissionClient client = new PermissionClient();
        ClientResponse<PermissionsList> res = client.readSearchList("acquisition");
        PermissionsList list = res.getEntity();
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
            logger.debug(testName + ": received = " + list.getPermissions().size()
                    + " expected=" + EXPECTED_ITEMS);
        }
        Assert.assertEquals(EXPECTED_ITEMS, list.getPermissions().size());
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

        Permission permToUpdate = new Permission();
        permToUpdate.setCsid(knownResourceId);
        // Update the content of this resource.
        permToUpdate.setResourceName("updated-resource");
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(permToUpdate,
                    Permission.class));
        }
        PermissionClient client = new PermissionClient();
        // Submit the request to the service and store the response.
        ClientResponse<Permission> res = client.update(knownResourceId, permToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


        Permission permUpdated = (Permission) res.getEntity();
        Assert.assertNotNull(permUpdated);

        Assert.assertEquals(permUpdated.getResourceName(),
                permToUpdate.getResourceName(),
                "Data in updated object did not match submitted data.");
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update"})
    public void updateActions(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        Permission permToUpdate = new Permission();
        permToUpdate.setCsid(knownResourceId);
        // Update the content of this resource.
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        int default_actions = actions.size();
        actions.remove(0);
        actions.remove(0);
        int toUpdate_actions = actions.size();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " no. of actions default=" + default_actions
                    + " to update =" + toUpdate_actions);
        }
        permToUpdate.setActions(actions);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " updated object\n"
                    + objectAsXmlString(permToUpdate, Permission.class));
        }
        PermissionClient client = new PermissionClient();
        // Submit the request to the service and store the response.
        ClientResponse<Permission> res = client.update(knownResourceId, permToUpdate);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        Permission permUpdated = (Permission) res.getEntity();
        Assert.assertNotNull(permUpdated);
        int updated_actions = permToUpdate.getActions().size();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " no. of actions to update=" + toUpdate_actions
                    + " updated =" + updated_actions);
        }
        Assert.assertEquals(toUpdate_actions,
                updated_actions,
                "Data in updated object did not match submitted data.");
    }
    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.

    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    	//FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    	//FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    	//FIXME: Should this test really be empty?  If so, please comment accordingly.
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
        // The only relevant ID may be the one used in updatePermission(), below.
        PermissionClient client = new PermissionClient();
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = createPermissionInstance("acquisitions",
                "default permissions for acquisitions",
                actions,
                EffectType.PERMIT,
                true,
                true,
                true);
        ClientResponse<Permission> res =
                client.update(NON_EXISTENT_ID, permission);
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
    dependsOnMethods = {"updateActions"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        PermissionClient client = new PermissionClient();
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
        PermissionClient client = new PermissionClient();
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
     * create permission instance
     * @param resourceName
     * @param description
     * @param actionList list of actions for this permission
     * @param effect effect of the permission
     * @param useResourceName
     * @param useAction
     * @param useEffect
     * @return
     */
    public static Permission createPermissionInstance(String resourceName,
            String description,
            List<PermissionAction> actionList,
            EffectType effect,
            boolean useResourceName,
            boolean useAction,
            boolean useEffect) {

        Permission permission = PermissionFactory.createPermissionInstance(resourceName,
                description, actionList, effect,
                useResourceName, useAction, useEffect);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, permission");
            logger.debug(objectAsXmlString(permission, Permission.class));
        }
        return permission;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        setupDelete("delete");
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
        PermissionClient client = new PermissionClient();
        for (String resourceId : allResourceIdsCreated) {
            ClientResponse<Response> res = client.delete(resourceId);
            int statusCode = res.getStatus();
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        }
    }

    private int printList(String testName, PermissionsList list) {

        int i = 0;

        for (Permission permission : list.getPermissions()) {
            logger.debug(testName + " permission csid=" + permission.getCsid()
                    + " name=" + permission.getResourceName()
                    + " desc=" + permission.getDescription());
            i++;
        }
        return i;
    }
}
