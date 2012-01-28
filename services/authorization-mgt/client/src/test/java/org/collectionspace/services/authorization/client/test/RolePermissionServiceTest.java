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
 * See the License for the specific language governing permRoles and
 * limitations under the License.
 */
package org.collectionspace.services.authorization.client.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import javax.ws.rs.core.Response;
import org.collectionspace.services.authorization.perms.EffectType;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.RolePermissionClient;
import org.collectionspace.services.client.PermissionRoleFactory;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * PermissionServiceTest, carries out tests against a
 * deployed and running Permission, Role and PermissionRole Services.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class RolePermissionServiceTest extends AbstractServiceTestImpl<PermissionRole, PermissionRole,
		PermissionRole, PermissionRole> {

    /** The Constant logger. */
    private final static String CLASS_NAME = RolePermissionServiceTest.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    final private static String TEST_MARKER = "_RolePermissionServiceTest";
    final private static String TEST_ROLE_NAME = "ROLE";
    final private static String NO_REL_SUFFIX = "-no-rel";
    /** The perm values. */
    private Hashtable<String, PermissionValue> permValues = new Hashtable<String, PermissionValue>();
    /** The role values. */
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    private Date now = new Date();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return new RolePermissionClient().getServicePathComponent();
    }
    
    @Override
    public String getServiceName() { 
    	return RoleClient.SERVICE_NAME; // RolePermission is a sub-resource of 'roles' resource.
    }
    
    private String getRoleName() {
        return TEST_ROLE_NAME + TEST_MARKER + now.toString();
    }

    /**
     * Seed data.
     */
    @BeforeClass(alwaysRun = true)
    public void seedData() {

        String rn1 = getRoleName();
        String r1RoleId = createRole(rn1);
        RoleValue rv1 = new RoleValue();
        rv1.setRoleId(r1RoleId);
        rv1.setRoleName(rn1);
        roleValues.put(rv1.getRoleName(), rv1);

        String rn2 = getRoleName() + NO_REL_SUFFIX;
        String r2RoleId = createRole(rn2);
        RoleValue rv2 = new RoleValue();
        rv2.setRoleId(r2RoleId);
        rv2.setRoleName(rn2);
        roleValues.put(rv2.getRoleName(), rv2);

        String ra1 = "fooService" + TEST_MARKER;
        String permId1 = createPermission(ra1, EffectType.PERMIT);
        PermissionValue pva1 = new PermissionValue();
        pva1.setResourceName(ra1);
        pva1.setPermissionId(permId1);
        permValues.put(pva1.getResourceName(), pva1);

        String ra2 = "barService" + TEST_MARKER;
        String permId2 = createPermission(ra1, EffectType.PERMIT);
        PermissionValue pva2 = new PermissionValue();
        pva2.setResourceName(ra2);
        pva2.setPermissionId(permId2);
        permValues.put(pva2.getResourceName(), pva2);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new RolePermissionClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readPaginatedList(java.lang.String)
     */
//    @Test(dataProvider = "testName")
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
    }
    
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */

    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        RoleValue rv = roleValues.get(getRoleName());
        PermissionRole permRole = createPermissionRoleInstance(rv,
                permValues.values(), true, true);
        RolePermissionClient client = new RolePermissionClient();
        ClientResponse<Response> res = null;
        try {
            res = client.create(rv.getRoleId(), permRole);
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            knownResourceId = extractId(res); //This is meaningless in this test, see getKnowResourceId() method for details
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }
    }

    //to not cause uniqueness violation for permRole, createList is removed
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    	dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        //Should this really be empty?
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty?
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
//    	dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        RolePermissionClient client = new RolePermissionClient();
        ClientResponse<PermissionRole> res = null;
        try {
            res = client.read(roleValues.get(getRoleName()).getRoleId());
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            PermissionRole output = (PermissionRole) res.getEntity();
            Assert.assertNotNull(output);
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        RolePermissionClient client = new RolePermissionClient();
        ClientResponse<PermissionRole> res = null;
        try {

            res = client.read(NON_EXISTENT_ID);
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void readNoRelationship(String testName) throws Exception {
        setupRead();
        // Submit the request to the service and store the response.
        RolePermissionClient client = new RolePermissionClient();
        ClientResponse<PermissionRole> res = null;
        try {

            res = client.read(roleValues.get(getRoleName() + NO_REL_SUFFIX).getRoleId());
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());
            PermissionRole output = (PermissionRole) res.getEntity();

            String sOutput = objectAsXmlString(output, PermissionRole.class);
            if (logger.isDebugEnabled()) {
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
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    	dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        //Should this really be empty?
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
//    	dependsOnMethods = {"read", "readList", "readNonExistent"})
    public void update(String testName) throws Exception {
        //Should this really be empty?
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        //Should this really be empty?
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();
                
        // Submit the request to the service and store the response.
        RolePermissionClient client = new RolePermissionClient();
        RoleValue rv = roleValues.get(getRoleName());
        ClientResponse<Response> delRes = null;
        try {
        	delRes = client.delete(rv.getRoleId());
            int statusCode = delRes.getStatus();
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (delRes != null) {
            	delRes.releaseConnection();
            }
        }

        // reset for next delete
        create(testName);
        setupDelete();
        
        rv = roleValues.get(getRoleName());
        ClientResponse<PermissionRole> readResponse = client.read(rv.getRoleId());
        PermissionRole toDelete = readResponse.getEntity();
        readResponse.releaseConnection();        
        
        rv = toDelete.getRole().get(0);
        ClientResponse<Response> res = null;
        try {
            res = client.delete(
                    rv.getRoleId(), toDelete);
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

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db
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
     */
    @Override
//    @Test(dependsOnMethods = {"create"}) //FIXME: REM - This is not a test of a submit to the permroles service, but to just authorization/roles
    public void testSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(roleValues.get(getRoleName()).getRoleId());
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
     * create PermissionRole instance
     * @param rv rolevalue
     * @param pvs permission value array
     * @param usePermId 
     * @param useRoleId
     * @return PermissionRole
     */
    public static PermissionRole createPermissionRoleInstance(RoleValue rv,
            Collection<PermissionValue> pvs,
            boolean usePermId,
            boolean useRoleId) {
        List<PermissionValue> pvls = new ArrayList<PermissionValue>();
        pvls.addAll(pvs);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                rv, pvls, usePermId, useRoleId);
        if (logger.isDebugEnabled()) {
            logger.debug("" +
                    "permRole");
            logger.debug(objectAsXmlString(permRole, PermissionRole.class));
        }
        return permRole;
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
        for (PermissionValue pv : permValues.values()) {
            deletePermission(pv.getPermissionId());
        }
        for (RoleValue rv : roleValues.values()) {
            deleteRole(rv.getRoleId());
        }
    }

    /**
     * Creates the permission.
     *
     * @param resName the res name
     * @param effect the effect
     * @return the string
     */
    private String createPermission(String resName, EffectType effect) {
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = PermissionFactory.createPermissionInstance(resName,
                "default permissions for " + resName,
                actions, effect, true, true, true);
        ClientResponse<Response> res = null;
        String id = null;
        try {
            res = permClient.create(permission);
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("createPermission: resName=" + resName
                        + " status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
            id = extractId(res);
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }
        return id;
    }

    /**
     * Delete permission.
     *
     * @param permId the perm id
     */
    private void deletePermission(String permId) {
        setupDelete();
        PermissionClient permClient = new PermissionClient();

        ClientResponse<Response> res = null;
        try {
            res = permClient.delete(permId);
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("deletePermission: delete permission id="
                        + permId + " status=" + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

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
        setupCreate();
        RoleClient roleClient = new RoleClient();

        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
        role.setRoleGroup("something");
        ClientResponse<Response> res = null;
        String id = null;
        try {
            res = roleClient.create(role);
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("createRole: name=" + roleName
                        + " status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
            id = extractId(res);
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }
        return id;
    }

    /**
     * Delete role.
     *
     * @param roleId the role id
     */
    private void deleteRole(String roleId) {
        setupDelete();
        RoleClient roleClient = new RoleClient();
        ClientResponse<Response> res = null;
        try {
            res = roleClient.delete(roleId);
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("deleteRole: delete role id=" + roleId
                        + " status=" + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            res.releaseConnection();
        }

    }

	@Override
	protected PermissionRole createInstance(String commonPartName,
			String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PermissionRole updateInstance(PermissionRole commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(PermissionRole original,
			PermissionRole updated) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Class<PermissionRole> getCommonListType() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    protected String getKnowResourceId() {
    	return roleValues.get(getRoleName()).getRoleId();
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
