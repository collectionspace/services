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
package org.collectionspace.services.client.test;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.workflow.WorkflowsCommon;
import org.collectionspace.services.client.DimensionClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.dimension.DimensionsCommon;
import org.collectionspace.services.dimension.DimensionsCommonList;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObjectExitServiceTest, carries out tests against a deployed and running ObjectExit Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class WorkflowServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = WorkflowServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private String knownResourceId = null;

    @Override
	public String getServicePathComponent() {
		return WorkflowClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return WorkflowClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new DimensionClient();
    }

    @Override
    protected AbstractCommonList getAbstractCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }

//    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
//    public void create(String testName) throws Exception {
//        logger.debug(testBanner(testName, CLASS_NAME));
//        setupCreate();
//        WorkflowClient client = new WorkflowClient();
//        PoxPayloadOut multipart = createObjectExitInstance(createIdentifier());
//        ClientResponse<Response> res = client.create(multipart);
//        assertStatusCode(res, testName);
//        if (knownResourceId == null) {
//            knownResourceId = extractId(res);  // Store the ID returned from the first resource created for additional tests below.
//            logger.debug(testName + ": knownResourceId=" + knownResourceId);
//        }
//        allResourceIdsCreated.add(extractId(res)); // Store the IDs from every resource created by tests so they can be deleted after tests have been run.
//    }
    
    
    /*
     * Create a Dimension instance to use as our test target.
     */
    public void createTestObject(String testName) throws Exception {
    	logger.debug(testBanner(testName, CLASS_NAME));
    	setupCreate();
    	DimensionClient client = new DimensionClient();
    	PoxPayloadOut multipart = createDimensionInstance(createIdentifier());
    	ClientResponse<Response> res = client.create(multipart);
    	assertStatusCode(res, testName);
    	if (knownResourceId == null) {
    		knownResourceId = extractId(res);  // Store the ID returned from the first resource created for additional tests below.
    		logger.debug(testName + ": knownResourceId=" + knownResourceId);
    	}
    	allResourceIdsCreated.add(extractId(res)); // Store the IDs from every resource created by tests so they can be deleted after tests have been run.
    }

    @Override
    public void createList(String testName) throws Exception {
    	//empty N/A
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupRead();
        DimensionClient client = new DimensionClient();
        ClientResponse<String> res = client.getWorkflow(knownResourceId);
        assertStatusCode(res, testName);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        WorkflowsCommon workflowsCommon = (WorkflowsCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowsCommon.class);
        if (logger.isDebugEnabled() == true) {
        	logger.debug("Workflow payload is: " + input.getXmlPayload());
        }
        Assert.assertNotNull(workflowsCommon);
    }

//    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"createList", "read"})
//    public void readList(String testName) throws Exception {
//        logger.debug(testBanner(testName, CLASS_NAME));
//        setupReadList();
//        WorkflowClient client = new WorkflowClient();
//        ClientResponse<AbstractCommonList> res = client.readList();
//        AbstractCommonList list = res.getEntity();
//        assertStatusCode(res, testName);
//        if (logger.isDebugEnabled()) {
//            List<AbstractCommonList.ListItem> items =
//                list.getListItem();
//            int i = 0;
//            for(AbstractCommonList.ListItem item : items){
//                logger.debug(testName + ": list-item[" + i + "] " +
//                        item.toString());
//                i++;
//            }
//        }
//    }
    
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdate();
        updateLifeCycleState(testName, knownResourceId, WorkflowClient.WORKFLOWSTATE_APPROVED);
    }    

    private void updateLifeCycleState(String testName, String resourceId, String lifeCycleState) throws Exception {
        //
        // Read the existing object
        //
        DimensionClient client = new DimensionClient();
        ClientResponse<String> res = client.getWorkflow(resourceId);
        assertStatusCode(res, testName);
        logger.debug("Got object to update life cycle state with ID: " + resourceId);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        WorkflowsCommon workflowCommons = (WorkflowsCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowsCommon.class);
        Assert.assertNotNull(workflowCommons);
        //
        // Mark it for a soft delete.
        //
        logger.debug("Current workflow state:" + objectAsXmlString(workflowCommons, WorkflowsCommon.class));
        workflowCommons.setCurrentLifeCycleState(lifeCycleState);
        PoxPayloadOut output = new PoxPayloadOut(WorkflowClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(workflowCommons, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(WorkflowClient.SERVICE_COMMONPART_NAME);
        //
        // Perform the update
        //
        res = client.updateWorkflow(resourceId, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        WorkflowsCommon updatedWorkflowCommons = (WorkflowsCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowsCommon.class);
        Assert.assertNotNull(updatedWorkflowCommons);
        //
        // Read the updated object and make sure it was updated correctly.
        //
        res = client.getWorkflow(resourceId);
        assertStatusCode(res, testName);
        logger.debug("Got workflow state of updated object with ID: " + resourceId);
        input = new PoxPayloadIn(res.getEntity());
        updatedWorkflowCommons = (WorkflowsCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowsCommon.class);
        Assert.assertNotNull(workflowCommons);
        Assert.assertEquals(updatedWorkflowCommons.getCurrentLifeCycleState(), lifeCycleState);
    }

    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdateNonExistent();
        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        WorkflowClient client = new WorkflowClient();
        PoxPayloadOut multipart = createDimensionInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        assertStatusCode(res, testName);
    }

    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDelete();
        WorkflowClient client = new WorkflowClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        assertStatusCode(res, testName);
    }

    // ---------------------------------------------------------------
    // Failure outcome tests : means we expect response to fail, but test to succeed
    // ---------------------------------------------------------------

    // Failure outcome
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadNonExistent();
        WorkflowClient client = new WorkflowClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcome
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDeleteNonExistent();
        WorkflowClient client = new WorkflowClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcomes
    // Placeholders until the tests below can be implemented. See Issue CSPACE-401.

    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

//    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode(); // Expected status code: 200 OK
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);
        logger.debug("testSubmitRequest: url=" + url + " status=" + statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    private PoxPayloadOut createDimensionInstance(String dimensionValue) {
        String value = "dimensionValue-" + dimensionValue;
        String dimensionsCommonPartName = new DimensionClient().getCommonPartName();
        DimensionsCommon dimensionsCommon = new DimensionsCommon();
        
        dimensionsCommon.setValue(value);
        PoxPayloadOut multipart = new PoxPayloadOut(DimensionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(dimensionsCommonPartName, dimensionsCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("To be created, Dimensions common: " + commonPart.asXML());
            logger.debug(objectAsXmlString(dimensionsCommon, DimensionsCommon.class));
        }

        return multipart;
    }

	@Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
	public void create(String testName) throws Exception {
		this.createTestObject(testName);
	}

	private int readIncludeDeleted(String testName, Boolean includeDeleted) {
		int result = 0;
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        DimensionClient client = new DimensionClient();
        ClientResponse<DimensionsCommonList> res = client.readIncludeDeleted(includeDeleted);
        DimensionsCommonList list = res.getEntity();
        int statusCode = res.getStatus();
        //
        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //
        // Now check that list size is correct
        //
        List<DimensionsCommonList.DimensionListItem> items =
            list.getDimensionListItem();
        result = items.size();
        
        return result;
	}
	
	/*
	 * This test assumes that no objects exist yet.
	 * 
	 * http://localhost:8180/cspace-services/intakes?wf_deleted=false
	 */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update"})
	public void readList(String testName) throws Exception {
    	//
    	// Create 3 new objects
    	//
    	final int OBJECTS_TOTAL = 3;
    	for (int i = 0; i < OBJECTS_TOTAL; i++) {
    		this.createTestObject(testName);
    	}
    	//
    	// Mark one as soft deleted
    	//
    	int currentTotal = allResourceIdsCreated.size();
    	String csid = allResourceIdsCreated.get(currentTotal - 1); //0-based index to get the last one added
    	this.setupUpdate();
    	this.updateLifeCycleState(testName, csid, WorkflowClient.WORKFLOWSTATE_DELETED);
    	//
    	// Read the list back.  The deleted item should not be in the list
    	//
    	int updatedTotal = readIncludeDeleted(testName, Boolean.FALSE);
    	Assert.assertEquals(updatedTotal, currentTotal - 1, "Deleted items seem to be returned in list results.");
	}
	
	@Override
    public void readPaginatedList(String testName) throws Exception {
		//empty N/A
	}
	
}
