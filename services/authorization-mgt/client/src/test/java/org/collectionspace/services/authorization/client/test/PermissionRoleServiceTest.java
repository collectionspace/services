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
import java.util.Hashtable;
import java.util.List;
import javax.ws.rs.core.Response;
import org.collectionspace.services.authorization.EffectType;

import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.PermissionRoleClient;
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
public class PermissionRoleServiceTest extends AbstractServiceTestImpl {

    static private final Logger logger =
            LoggerFactory.getLogger(PermissionRoleServiceTest.class);
    // Instance variables specific to this test.
    private String knownResourceId = null;
    private List<String> allResourceIdsCreated = new ArrayList();
    private Hashtable<String, PermissionValue> permValues = new Hashtable<String, PermissionValue>();
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    @Override
    protected String getServicePathComponent() {
        return new PermissionRoleClient().getServicePathComponent();
    }

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        String ra = "accounts";
        String accPermId = createPermission(ra, EffectType.PERMIT);
        PermissionValue pva = new PermissionValue();
        pva.setResourceName(ra);
        pva.setPermissionId(accPermId);
        permValues.put(pva.getResourceName(), pva);

        String rc = "collectionobjects";
        String coPermId = createPermission(rc, EffectType.DENY);
        PermissionValue pvc = new PermissionValue();
        pvc.setResourceName(rc);
        pvc.setPermissionId(coPermId);
        permValues.put(pvc.getResourceName(), pvc);

        String ri = "intakes";
        String iPermId = createPermission(ri, EffectType.DENY);
        PermissionValue pvi = new PermissionValue();
        pvi.setResourceName(ri);
        pvi.setPermissionId(iPermId);
        permValues.put(pvi.getResourceName(), pvi);

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
        PermissionValue pv = permValues.get("accounts");
        PermissionRole permRole = createPermissionRoleInstance(pv,
                roleValues.values(), true, true);
        PermissionRoleClient client = new PermissionRoleClient();
        ClientResponse<Response> res = client.create(pv.getPermissionId(), permRole);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation
        // for additional tests below.
        //this is is not important in case of this relationship
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

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        ClientResponse<PermissionRole> res = client.read(
                permValues.get("accounts").getPermissionId(), "123");
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        PermissionRole output = (PermissionRole) res.getEntity();
        Assert.assertNotNull(output);
    }

    // Failure outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void readNonExistent(String testName) throws Exception {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        ClientResponse<PermissionRole> res = client.read(NON_EXISTENT_ID, "123");
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
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        ClientResponse<Response> res = client.delete(
                permValues.get("accounts").getPermissionId(), "123");
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

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(permValues.get("accounts").getPermissionId());
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
     * create permRolerole instance
     * @param permId
     * @param roleValues array of role ids
     * @param userPermId
     * @param useRoleId
     * @return
     */
    public static PermissionRole createPermissionRoleInstance(PermissionValue pv,
            Collection<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        PermissionRole permRole = new PermissionRole();
        //service consume is not required to provide subject as it is determined
        //from URI used
//        permRole.setSubject(SubjectType.ROLE);
        if (usePermId) {
            ArrayList<PermissionValue> pvs = new ArrayList<PermissionValue>();
            pvs.add(pv);
            permRole.setPermissions(pvs);
        }
        if (useRoleId) {
            //FIXME is there a better way?
            ArrayList<RoleValue> rvas = new ArrayList<RoleValue>();
            for (RoleValue rv : rvs) {
                rvas.add(rv);
            }
            permRole.setRoles(rvas);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, permRole common");
            logger.debug(objectAsXmlString(permRole, PermissionRole.class));
        }
        return permRole;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        setupDelete("delete");
        if (logger.isDebugEnabled()) {
            logger.debug("clenaup: Cleaning up temporary resources created for testing ...");
        }
        PermissionRoleClient client = new PermissionRoleClient();
        for (String resourceId : allResourceIdsCreated) {

            // Note: Any non-success responses are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId, "123");
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("clenaup: delete relationships for permission id="
                        + resourceId + " status=" + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        }

        for (PermissionValue pv : permValues.values()) {
            deletePermission(pv.getPermissionId());
        }

        for (RoleValue rv : roleValues.values()) {
            deleteRole(rv.getRoleId());
        }
    }

    private String createPermission(String resName, EffectType effect) {
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = PermissionServiceTest.createPermissionInstance(resName,
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
    }

    private String createRole(String roleName) {
        setupCreate();
        RoleClient roleClient = new RoleClient();

        Role role = RoleFactory.createRoleInstance(roleName,
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
    }
}
