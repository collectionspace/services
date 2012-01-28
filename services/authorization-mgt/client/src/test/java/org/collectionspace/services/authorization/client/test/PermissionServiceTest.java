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
import java.util.List;
import javax.ws.rs.core.Response;
//import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.perms.EffectType;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionServiceTest, carries out tests against a
 * deployed and running Permission Service.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class PermissionServiceTest extends AbstractServiceTestImpl<PermissionsList, Permission,
		Permission, Permission> {

    /** The Constant logger. */
    private final static String CLASS_NAME = PermissionServiceTest.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    // Instance variables specific to this test.
    private String knownResource = "accounts-test";

    @Override
    public String getServiceName() { 
    	return PermissionClient.SERVICE_NAME;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return PermissionClient.SERVICE_PATH_COMPONENT;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new PermissionClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readPaginatedList(java.lang.String)
     */
//    @Test(dataProvider = "testName")
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
    }

    @Override
    protected String getKnowResourceIdentifier() {
    	return knownResource;
    }
    
    /**
     * Creates the without resource name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"CRUDTests"})
    public void createWithoutResourceName(String testName) throws Exception {
        setupCreate();

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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Search resource name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void searchResourceName(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        PermissionClient client = new PermissionClient();
        ClientResponse<PermissionsList> res = client.readSearchList("acquisition");
        try {
	        assertStatusCode(res, testName);
	        PermissionsList list = res.getEntity(PermissionsList.class);
	        int EXPECTED_ITEMS = 4; //seeded permissions
	        int actual = list.getPermission().size();
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": received = " + actual
	                    + " expected=" + EXPECTED_ITEMS);
	        }
	        // Optionally output additional data about list members for debugging.
	        boolean iterateThroughList = true;
	        if ((iterateThroughList || (EXPECTED_ITEMS != list.getPermission().size()))
	        		&& logger.isDebugEnabled()) {
	            printList(testName, list);
	        }
	        Assert.assertEquals(list.getPermission().size(), EXPECTED_ITEMS);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }
    
    @Override
    public void delete(String testName) throws Exception {
    	//This method does nothing because we want to postpone the "delete" test until after
    	//the "updateNotAllowed" test gets run.  Our "localDelete" test will call the real "delete" test later.
    }
    
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"updateNotAllowed", "updateActions"})
    public void localDelete(String testName) throws Exception {
    	super.delete(testName);
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void updateNotAllowed(String testName) throws Exception {

        // Perform setup.
        setupUpdate();

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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

    }

    /**
     * Update actions.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"updateNotAllowed"})
    public void updateActions(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        Permission permToUpdate = new Permission();
        permToUpdate.setCsid(knownResourceId);
        permToUpdate.setResourceName(knownResource);
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
        permToUpdate.setAction(actions);
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        Permission permUpdated = (Permission) res.getEntity();
        Assert.assertNotNull(permUpdated);
        int updated_actions = permToUpdate.getAction().size();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " no. of actions to update=" + toUpdate_actions
                    + " updated =" + updated_actions);
        }
        Assert.assertEquals(toUpdate_actions,
                updated_actions,
                "Data in updated object did not match submitted data.");
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    	dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updatePermission(), below.
        PermissionClient client = new PermissionClient();
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = createPermissionInstance("test-acquisitions",
                "default permissions for test-acquisitions",
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
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
     * create permission instance
     * @param resourceName
     * @param description
     * @param actionList list of actions for this permission
     * @param effect effect of the permission
     * @param useResourceName
     * @param useAction
     * @param useEffect
     * @return permission
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

    /**
     * Prints the list.
     *
     * @param testName the test name
     * @param list the list
     * @return the int
     */
    @Override
    protected void printList(String testName, PermissionsList list) {
        for (Permission permission : list.getPermission()) {
            logger.debug(testName + " permission csid=" + permission.getCsid()
                    + " name=" + permission.getResourceName()
                    + " desc=" + permission.getDescription());
        }
    }

	@Override
	protected Permission createInstance(String commonPartName, String identifier) {
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = createPermissionInstance(identifier,
                "default permissions for " + identifier,
                actions,
                EffectType.PERMIT,
                true,
                true,
                true);
        return permission;
	}

	@Override
	protected Permission updateInstance(Permission original) {
		Permission result = new Permission();
		
		result.setCsid(original.getCsid());
		result.setResourceName(original.getResourceName());
        // Update the content of this resource.
		result.setDescription("updated-" + original.getDescription());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(Permission original,
			Permission updated) throws Exception {
        Assert.assertEquals(updated.getCsid(),
        		original.getCsid(),
                "CSID in updated object did not match submitted data.");

        Assert.assertEquals(updated.getResourceName(),
        		original.getResourceName(),
                "Resource name in updated object did not match submitted data.");

        Assert.assertEquals(updated.getDescription(),
        		original.getDescription(),
                "Description in updated object did not match submitted data.");
    }

	@Override
	protected Class<PermissionsList> getCommonListType() {
		return PermissionsList.class;
	}

    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
    public void CRUDTests(String testName) {
    	// Do nothing.  Simply here to for a TestNG execution order for our tests
    }

	@Override
	public void updateWithEmptyEntityBody(String testName) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateWithMalformedXml(String testName) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateWithWrongXmlSchema(String testName) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
