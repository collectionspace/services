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

//import java.util.ArrayList;
//import java.util.List;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RolesList;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RoleServiceTest, carries out tests against a
 * deployed and running Role Service.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class RoleServiceTest extends AbstractServiceTestImpl<RolesList, Role, Role, Role> {

    /** The logger. */
    private final static String CLASS_NAME = RoleServiceTest.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    // Instance variables specific to this test.
    /** The known resource id. */
    private String knownRoleName = "ROLE_USERS_MOCK-1";
    private String knownRoleDisplayName = "ROLE_DISPLAYNAME_USERS_MOCK-1";
    private String verifyResourceId = null;
    private String verifyRoleName = "collections_manager_mock-1";
//    private List<String> allResourceIdsCreated = new ArrayList<String>();

    @Override
    public String getServiceName() { 
    	return RoleClient.SERVICE_NAME;
    }
    
    @Override
    protected String getServicePathComponent() {
        return new RoleClient().getServicePathComponent();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new RoleClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readPaginatedList(java.lang.String)
     */
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance(knownRoleName,
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Store the ID returned from this create operation
        // for additional tests below.
        knownResourceId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
    }
    
    @Test(dataProvider = "testName", 
    		dependsOnMethods = {"CRUDTests"})
    public void createWithDisplayname(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance(knownRoleName + "_" + knownRoleDisplayName,
                "all users are required to be in this role",
                true);
        role.setDisplayName(knownRoleDisplayName);
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Store the ID returned from this create operation
        // for additional tests below.
        String csid = extractId(res);
        allResourceIdsCreated.add(csid);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": csid=" + csid);
        }
    }
    

    /**
     * Creates the for unique role.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void createForUniqueRole(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance(knownRoleName,
                "role users",
                true);
        ClientResponse<Response> res = client.create(role);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
        	logger.debug(testName + ": Role with name \"" +
        			knownRoleName + "\" should already exist, so this request should fail.");
            logger.debug(testName + ": status = " + statusCode);
            logger.debug(testName + ": " + res);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    /**
     * Creates the for unique display name of role.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createWithDisplayname"})
    public void createForUniqueDisplayRole(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance(knownRoleName + System.currentTimeMillis(),
                "role users with non-unique display name",
                true);
        role.setDisplayName(knownRoleDisplayName);
        ClientResponse<Response> res = client.create(role);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
        	logger.debug(testName + ": Role with name \"" +
        			knownRoleName + "\" should already exist, so this request should fail.");
            logger.debug(testName + ": status = " + statusCode);
            logger.debug(testName + ": " + res);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        if (statusCode != Response.Status.BAD_REQUEST.getStatusCode()) {
            // If the test fails then we've just created a Role that we need to delete, so
            // store the ID returned from this create operation.
            String csid = extractId(res);
            allResourceIdsCreated.add(csid);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": csid=" + csid);
            }
        	Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    /**
     * Creates the without role name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void createWithoutRoleName(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance("",
                "role for users",
                false);
        ClientResponse<Response> res = client.create(role);
        int statusCode = res.getStatus();
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    //to not cause uniqueness violation for role, createList is removed
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName",
//    	dependsOnMethods = {"createWithDisplayname"})
    public void createList(String testName) throws Exception {
        setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        //create a role with lowercase role name without role prefix
        //the service should make it upper case and add the role prefix
        Role role1 = createRoleInstance(verifyRoleName,
                "collection manager",
                true);
        ClientResponse<Response> res = client.create(role1);
        int statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        verifyResourceId = extractId(res);
        allResourceIdsCreated.add(verifyResourceId);

        Role role2 = createRoleInstance("ROLE_COLLECTIONS_CURATOR_TEST",
                "collections curator",
                true);
        res = client.create(role2);
        statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        allResourceIdsCreated.add(extractId(res));

        Role role3 = createRoleInstance("ROLE_MOVINGIMAGE_ADMIN_TEST",
                "moving image admin",
                true);
        res = client.create(role3);
        statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        allResourceIdsCreated.add(extractId(res));
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
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
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"createForUniqueRole"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<Role> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        Role output = (Role) res.getEntity();
        Assert.assertNotNull(output);
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void readToVerify(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<Role> res = client.read(verifyResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        Role output = (Role) res.getEntity();
        Assert.assertNotNull(output);

        //FIXME: Tenant ID of "1" should not be hard coded
        String roleNameToVerify = "ROLE_" +
        	"1_" +
        	verifyRoleName.toUpperCase();
        Assert.assertEquals(output.getRoleName(), roleNameToVerify,
                "RoleName fix did not work!");
    }
    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */

    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<Role> res = client.read(NON_EXISTENT_ID);
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

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<RolesList> res = client.readList();
        try {
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        assertStatusCode(res, testName);
	        RolesList list = res.getEntity();
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
     * Search role name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void searchRoleName(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<RolesList> res = client.readSearchList("movingImage");
        try {
	        assertStatusCode(res, testName);
	        RolesList list = res.getEntity();
	        int EXPECTED_ITEMS = 1;
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": received = " + list.getRole().size()
	                    + " expected=" + EXPECTED_ITEMS);
	        }
	        Assert.assertEquals(EXPECTED_ITEMS, list.getRole().size());
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

    // Failure outcomes
    // None at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#update(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read", "readList", "readNonExistent"})
    public void update(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        Role roleToUpdate = new Role();
        roleToUpdate.setCsid(knownResourceId);
        roleToUpdate.setRoleName(knownRoleName);
        roleToUpdate.setDisplayName(knownRoleName);
        
        // Update the content of this resource.
        roleToUpdate.setDescription("updated role description");
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();            
            logger.debug(objectAsXmlString(objectFactory.createRole(roleToUpdate),
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);


        Role roleUpdated = (Role) res.getEntity();
        Assert.assertNotNull(roleUpdated);

        Assert.assertEquals(roleUpdated.getDescription(),
                roleToUpdate.getDescription(),
                "Data in updated object did not match submitted data.");
    }
    
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
	public void verifyProtectionReadOnly(String testName) throws Exception {

    	setupCreate();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        Role role = createRoleInstance(knownRoleName+"_PT", "Just a temp", true);
        role.setMetadataProtection(RoleClient.IMMUTABLE);
        role.setPermsProtection(RoleClient.IMMUTABLE);
        ClientResponse<Response> res = client.create(role);
        int statusCode = res.getStatus();
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Store the ID returned from this create operation
        // for additional tests below.
        String testResourceId = extractId(res);
        allResourceIdsCreated.add(testResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": testResourceId=" + testResourceId);
        }
        setupRead();

        // Submit the request to the service and store the response.
        ClientResponse<Role> roleRes = client.read(testResourceId);
        statusCode = roleRes.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        Role roleRead = (Role) roleRes.getEntity();
        Assert.assertNotNull(roleRead);
        String mdProtection = roleRead.getMetadataProtection();
        String permsProtection = roleRead.getPermsProtection();
        if (logger.isTraceEnabled()) {
            logger.trace(testName + ": metadataProtection=" + mdProtection);
            logger.trace(testName + ": permsProtection=" + permsProtection);
        }
    	Assert.assertFalse(role.getMetadataProtection().equals(mdProtection),
    			"Role allowed create to set the metadata protection flag.");
    	Assert.assertFalse(role.getPermsProtection().equals(permsProtection),
    			"Role allowed create to set the perms protection flag.");
        
    	setupUpdate();

    	Role roleToUpdate = createRoleInstance(knownRoleName+"_PT", "Just a temp", true);
    	roleToUpdate.setMetadataProtection(RoleClient.IMMUTABLE);
    	roleToUpdate.setPermsProtection(RoleClient.IMMUTABLE);

    	// Submit the request to the service and store the response.
    	roleRes = client.update(testResourceId, roleToUpdate);
    	statusCode = roleRes.getStatus();
    	// Check the status code of the response: does it match the expected response(s)?
    	if (logger.isDebugEnabled()) {
    		logger.debug(testName + ": status = " + statusCode);
    	}
    	Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
    			invalidStatusCodeMessage(testRequestType, statusCode));
    	Assert.assertEquals(statusCode, testExpectedStatusCode);


    	Role roleUpdated = (Role) roleRes.getEntity();
    	Assert.assertNotNull(roleUpdated);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + "Updated role: ");
            org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();            
            logger.debug(objectAsXmlString(objectFactory.createRole(roleUpdated),
                    Role.class));            
        }

    	Assert.assertFalse(
    			RoleClient.IMMUTABLE.equalsIgnoreCase(roleUpdated.getMetadataProtection()),
    			"Role allowed update of the metadata protection flag.");
    	Assert.assertFalse(
    			RoleClient.IMMUTABLE.equalsIgnoreCase(roleUpdated.getPermsProtection()),
    			"Role allowed update of the perms protection flag.");
    }

	@Test(dataProvider = "testName",
			dependsOnMethods = {"CRUDTests"})
    public void updateNotAllowed(String testName) throws Exception {

        // Perform setup.
        setupUpdate();

        Role roleToUpdate = new Role();
        roleToUpdate.setCsid(knownResourceId);
        // Update the content of this resource.
        roleToUpdate.setRoleName("UPDATED-ROLE_USERS_TEST");
        roleToUpdate.setDisplayName("UPDATED-ROLE_USERS_TEST");
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();            
            logger.debug(objectAsXmlString(objectFactory.createRole(roleToUpdate),
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

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

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing since other tests like "updateNotAllowed" need the "known resource" that this test
    	// deletes.  Once all those tests get run, the "localDelete" method will call the base class' delete
    	// method that will delete the "known resource".
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"updateNotAllowed", "createForUniqueRole", "createForUniqueDisplayRole"})
    public void localDelete(String testName) throws Exception {
    	super.delete(testName);
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        RoleClient client = new RoleClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
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
    
    // ---------------------------------------------------------------
    // Search tests
    // ---------------------------------------------------------------
    
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void searchWorkflowDeleted(String testName) throws Exception {
        // Fixme: null test for now, overriding test in base class
    }    

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     * @throws Exception 
     */

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * create role instance
     * @param roleName
     * @param description
     * @param useRoleName
     * @return role
     */
    public Role createRoleInstance(String roleName,
            String description,
            boolean useRoleName) {

        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
        		description,
                useRoleName);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, role");
            org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();            
            logger.debug(objectAsXmlString(objectFactory.createRole(role),
                    Role.class));
        }
        return role;

    }

    /**
     * Prints the list.
     *
     * @param testName the test name
     * @param list the list
     * @return the int
     */
    protected void printList(String testName, RolesList list) {
        for (Role role : list.getRole()) {
            logger.debug(testName + " role csid=" + role.getCsid()
                    + " name=" + role.getRoleName()
                    + " desc=" + role.getDescription());
        }
    }

	@Override
	protected Role createInstance(String commonPartName, String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Role updateInstance(Role commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(Role original, Role updated)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Class<RolesList> getCommonListType() {
		// TODO Auto-generated method stub
		return null;
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
}
