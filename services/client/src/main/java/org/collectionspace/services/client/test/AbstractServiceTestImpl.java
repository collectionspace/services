/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.client.test;

import java.io.File;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.CollectionSpacePoxClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.jboss.resteasy.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

/**
 * AbstractServiceTestImpl
 *
 * Abstract base class for client tests of entity and relation services.
 * Abstract methods are provided for a set of CRUD + List tests to be invoked.
 *
 * For Javadoc descriptions of this class's methods, see the ServiceTest interface.
 *
 * <CLT> - Common list type
 * <CPT> - Common part type
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
// FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
public abstract class AbstractServiceTestImpl<CLT, CPT, REQUEST_TYPE, RESPONSE_TYPE>
		extends BaseServiceTest<CLT> implements ServiceTest {
    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(AbstractServiceTestImpl.class);

    /** The Constant DEFAULT_LIST_SIZE. */
    static protected final int DEFAULT_LIST_SIZE = 10;
    static protected final int DEFAULT_PAGINATEDLIST_SIZE = 10;

    /** The path to test resources */
    static protected final String RESOURCE_PATH = "src" + File.separator
            + "test" + File.separator
            + "resources";
    
    /** Misc constants */
    protected static final String BLOBS_DIR = "blobs";
    static protected final String NO_BLOB_CLEANUP = "noBlobCleanup";
    static protected final String NO_MEDIA_CLEANUP = "noMediaCleanup";
    private final static String NON_EXISTENT_KEYWORD = "jlmbsoqjlmbsoq" + System.currentTimeMillis();
    private String EMPTY_SORT_BY_ORDER = "";
    
    /** Error messages */
    private final static String ERROR_WORKFLOW_TRANSITION = "Workflow transition to 'deleted' did not take place!";
    
    protected String getResourceDir() {
        String result = null;
        String currentDirectory = System.getProperty("user.dir");
        result = currentDirectory + File.separator + RESOURCE_PATH;
        return result;
    }
        
    /*
     * Use this method in TestNG "@Test" methods that need to be overridden.  Because of an issue in TestNG 5.6 (and earlier),
     * we can't just mark the methods as "abstract".  Subclasses must override the @Test methods *without* the "@Test" annotation.
     */
    private void mustOverride(String testName) throws Exception {
    	throw new RuntimeException("This method must be implemented by a subclass.");
    }
        
    /*
     * We use this method to force a TestNG execution order for our tests
     */
	public abstract void CRUDTests(String testName);
    
    /*
     * We use this method to force a TestNG execution order for our tests
     */    
    @Test(dataProvider = "testName", dependsOnMethods = {
    		"create", "read", "update", "delete", "testSubmitRequest", "createList", "readList", "readNonExistent"})
    public void baseCRUDTests(String testName) {
    	// Do nothing -see "dependsOnMethods" in @Test annotation above.
    }
    
    /*
     * Sub-classes can override for the workflow tests.
     */
    protected REQUEST_TYPE createInstance(String identifier) {
    	String commonPartName = getClientInstance().getCommonPartName();
        return createInstance(commonPartName, identifier);
    }
    
    /**
     * Sub-classes must override this method for the "Create" tests to work properly
     */
    protected abstract REQUEST_TYPE createInstance(String commonPartName, String identifier);
    
    protected REQUEST_TYPE createNonExistenceInstance(String commonPartName, String identifier) {
    	return createInstance(commonPartName, identifier);
    }
                    
    @Override
    @Test(dataProvider = "testName")
    public void create(String testName) throws Exception {
    	String identifier = getKnowResourceIdentifier();
    	createWithIdentifier(testName, identifier);
    }
    
    protected String createWithIdentifier(String testName, String identifier) throws Exception {
    	String csid = createResource(testName, identifier);
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (getKnowResourceId() == null) {
            knownResourceId = csid;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + getKnowResourceId());
            }
        }
    	
        return identifier;
    }
    
    protected String createResource(String testName, String identifier) throws Exception {
        String result = null;
        
    	setupCreate();
    	CollectionSpaceClient client = this.getClientInstance();
        REQUEST_TYPE payload = createInstance(client.getCommonPartName(), identifier);
        ClientResponse<Response> res = client.create(payload);
    	try {
	        int statusCode = res.getStatus();
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": HTTP status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        result = extractId(res);
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        allResourceIdsCreated.add(result);
    	} finally {
    		res.releaseConnection();
    	}
    	
    	return result;
    }
    
    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"create"})    
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
    	CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<RESPONSE_TYPE> res = client.read(getKnowResourceId());
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        RESPONSE_TYPE output = (RESPONSE_TYPE) res.getEntity();
        Assert.assertNotNull(output);
        
        //
        // Now compare with the expected field values
        //
        REQUEST_TYPE expectedResult = createInstance("read_test");
        compareReadInstances(extractCommonPartValue(expectedResult), extractCommonPartValue(res));
    }
    
    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"create", "read", "update", "readWorkflow"})    
    public void delete(String testName) throws Exception {
        setupDelete();
    	CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<Response> res = client.delete(getKnowResourceId());
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
    
    @Override
    @Test(dataProvider = "testName")
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
    	CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<RESPONSE_TYPE> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();
        try {
            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            res.releaseConnection();
        }
    }

    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"delete"})    
    public void deleteNonExistent(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
    	CollectionSpaceClient client = this.getClientInstance();
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
    // Abstract CRUD tests : TestNG requires an empty method here. De-
    // claring them as "abstract" will not work.
    //
    // ---------------------------------------------------------------
    
//    protected abstract Class<CPT> getCommonPartTypeClass();
    
    public CPT extractCommonPartValue(ClientResponse<RESPONSE_TYPE> res) throws Exception {
    	CPT result = null;
    	result = (CPT) res.getEntity();
    	return result;
    }
    
    public CPT extractCommonPartValue(REQUEST_TYPE req) throws Exception {
    	return (CPT)req;
    }
        
    public REQUEST_TYPE createRequestTypeInstance(CPT commonPartTypeInstance) {
    	return (REQUEST_TYPE)commonPartTypeInstance;
    }
    
    //
    // This method is called by public void update(String testName).  Subclasses need
    // to override this method that should update the common part -e.g., CollectionObjectsCommon, DimensionsCommon, etc)
    //
    protected abstract CPT updateInstance(final CPT commonPartObject);
    
    protected abstract void compareUpdatedInstances(CPT original, CPT updated) throws Exception;
    
	protected void compareReadInstances(CPT original, CPT fromRead) throws Exception {
		// Do nothing by default.  Subclass can override if they want other behavior.
	}
    
    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"create", "read"})    
    public void update(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
    	CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<RESPONSE_TYPE> res = client.read(getKnowResourceId());
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        if (logger.isDebugEnabled()) {
            logger.debug("Got object to update with CSID= " + getKnowResourceId());
        }

        CPT commonPartObject = this.extractCommonPartValue(res);
        Assert.assertNotNull(commonPartObject);
        CPT theUpdate = updateInstance(commonPartObject);
        if (logger.isDebugEnabled()) {
            logger.debug("\n\nTo be updated fields: CSID = "  + getKnowResourceId() + "\n"
            		+ objectAsXmlString(theUpdate));
        }

        // Submit the request to the service and store the response.
        REQUEST_TYPE output = this.createRequestTypeInstance(theUpdate);
        res = client.update(getKnowResourceId(), output);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        CPT updatedCommonPartObject = this.extractCommonPartValue(res);
        Assert.assertNotNull(updatedCommonPartObject);

        compareUpdatedInstances(theUpdate, updatedCommonPartObject);
    }
        
    // ---------------------------------------------------------------
    //
    // Generic tests that apply to most services
    //
    // ---------------------------------------------------------------

    /**
     * A non-RESTEasy HTTP request test.
     */
    protected void testSubmitRequest(String csid) {
        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(csid);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);
    }
    
    /**
     * A non-RESTEasy HTTP request test.
     */
    @Test(dependsOnMethods = {"create",	"read"})
    public void testSubmitRequest() {
    	testSubmitRequest(getKnowResourceId());
    }

    /**
     * Creates the list.
     *
     * @param testName the test name
     * @param listSize the list size
     * @throws Exception the exception
     */
    protected void createPaginatedList(String testName, int listSize) throws Exception {
        for (int i = 0; i < listSize; i++) {
        	String identifier = createIdentifier();
            createWithIdentifier(testName, identifier);
        }
    }
    
    /**
     * Create a list of records
     */
    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
    	createPaginatedList(testName, DEFAULT_LIST_SIZE);
    }    
    
    protected void printList(String testName, CLT list) {
    	// By default, do nothing.  Tests can override this method to produce additional
    	// output after the "readList" test has run.
    }
        
    @Override
    @Test(dataProvider = "testName", dependsOnMethods = {"read"})    
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<CLT> res = client.readList();
        CLT list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            printList(testName, list);
        }
    }
    
    
    /**
     * Read paginated list.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"read"})
    public void readPaginatedList(String testName) throws Exception {
        // Perform setup.
        setupReadList();
        CollectionSpaceClient client = this.getClientInstance();

        // Get the current total number of items.
        // If there are no items then create some
        AbstractCommonList list = (AbstractCommonList) this.readList(testName,
                client,
                1 /*pgSz*/,
                0 /*pgNum*/,
                testExpectedStatusCode);
        if (list == null || list.getTotalItems() == 0) {
            this.createPaginatedList(testName, DEFAULT_PAGINATEDLIST_SIZE);
            setupReadList();
            list = (AbstractCommonList) this.readList(testName,
                    client,
                    1 /*pgSz*/,
                    0 /*pgNum*/,
                    testExpectedStatusCode);
        }

        // Print out the current list size to be paginated
        Assert.assertNotNull(list);
        long totalItems = list.getTotalItems();
        Assert.assertFalse(totalItems == 0);
        if (getLogger().isDebugEnabled() == true) {
            getLogger().debug(testName + ":" + "created list of "
                    + totalItems + " to be paginated.");
        }

        long pageSize = totalItems / 3; //create up to 3 pages to iterate over
        long pagesTotal = pageSize > 0 ? (totalItems / pageSize) : 0;
        for (int i = 0; i < pagesTotal; i++) {
            list = (AbstractCommonList) this.readList(testName, client, pageSize, i, testExpectedStatusCode);
            assertPaginationInfo(testName,
                    list,
                    i, //expected page number
                    pageSize, //expected page size
                    pageSize, //expected num of items in page
                    totalItems);//expected total num of items
        }

        // if there are any remainders be sure to paginate them as well
        long mod = pageSize != 0 ? totalItems % pageSize : totalItems;
        if (mod != 0) {
            list = (AbstractCommonList) this.readList(testName, client, pageSize, pagesTotal, testExpectedStatusCode);
            assertPaginationInfo(testName,
                    list,
                    pagesTotal, //expected page number
                    pageSize, //expected page size
                    mod, //expected num of items in page
                    totalItems);//expected total num of items
        }
    }

    /*
     * This test assumes that no objects exist yet.
     *
     * http://localhost:8180/cspace-services/intakes?wf_deleted=false
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"update"})
    public void readWorkflow(String testName) throws Exception {
        try {
            //
            // Get the total count of non-deleted existing records
            //
            long existingRecords = readIncludeDeleted(testName, Boolean.FALSE);

            //
            // Create 3 new objects
            //
            final int OBJECTS_TO_CREATE = 3;
            for (int i = 0; i < OBJECTS_TO_CREATE; i++) {
                this.createWorkflowTarget(testName);
            }

            //
            // Mark one as soft deleted
            //
            int existingTestCreated = allResourceIdsCreated.size(); // assumption is that no other test created records were soft deleted
            String csid = allResourceIdsCreated.get(existingTestCreated - 1); //0-based index to get the last one added
            this.setupUpdate();
            this.updateLifeCycleState(testName, csid, WorkflowClient.WORKFLOWSTATE_DELETED);
            //
            // Read the list of existing non-deleted records
            //
            long updatedTotal = readIncludeDeleted(testName, Boolean.FALSE);
            Assert.assertEquals(updatedTotal, existingRecords + OBJECTS_TO_CREATE - 1, "Deleted items seem to be returned in list results.");

            //
            // Next, test that a GET with WorkflowClient.WORKFLOWSTATE_DELETED query param set to 'false' returns a 404
            //
            int trials = 0;
            int result = 0;
            while (trials < 30) {
	            CollectionSpacePoxClient client = this.assertPoxClient();
	            ClientResponse<String> res = client.readIncludeDeleted(csid, Boolean.FALSE);
	            result = res.getStatus();
	            if (result == STATUS_NOT_FOUND) {
	            	logger.info("Workflow transition to 'deleted' is complete");
	            	break;
	            } else {
	            	/*
	            	 * This should never happen, but if it does we need a full stack trace to help track it down.
	            	 */
	            	try {
		            	throw new RuntimeException(ERROR_WORKFLOW_TRANSITION);
	            	} catch (RuntimeException e) {
		            	logger.info(ERROR_WORKFLOW_TRANSITION, e);
	            	}
	            }
	            trials++;
            }
            Assert.assertEquals(result, STATUS_NOT_FOUND);

        } catch (UnsupportedOperationException e) {
            logger.warn(this.getClass().getName() + " did not implement createWorkflowTarget() method.  No workflow tests performed.");
            return;
        }
    }

    /*
     * Test that searches honor the workflow deleted state.
     */
    @Test(dataProvider = "testName")
    public void searchWorkflowDeleted(String testName) throws Exception {

        // FIXME: Temporarily avoid running test if client is of an authority service
        CollectionSpacePoxClient client = this.assertPoxClient();
        if (isAuthorityClient(client)) {
            return;
        }

        try {
            //
            // Create 3 new objects
            //
            final int OBJECTS_TO_CREATE = 3;
            final String KEYWORD = NON_EXISTENT_KEYWORD + createIdentifier();
            for (int i = 0; i < OBJECTS_TO_CREATE; i++) {
                this.createWorkflowTarget(testName, KEYWORD);
            }

            //
            // Mark one as soft deleted
            //
            int existingTestCreated = allResourceIdsCreated.size(); // assumption is that no other test created records were soft deleted
            String csid = allResourceIdsCreated.get(existingTestCreated - 1); //0-based index to get the last one added
            this.setupUpdate();
            this.updateLifeCycleState(testName, csid, WorkflowClient.WORKFLOWSTATE_DELETED);

            //
            // Search for the newly-created records, excluding the soft deleted record.
            //
            // Send the search request and receive a response
            ClientResponse<AbstractCommonList> res = client.keywordSearchIncludeDeleted(KEYWORD, Boolean.FALSE);
            int result = res.getStatus();
            Assert.assertEquals(result, STATUS_OK);

            AbstractCommonList list = res.getEntity();
            long itemsMatchedBySearch = list.getTotalItems();
            Assert.assertEquals(itemsMatchedBySearch, OBJECTS_TO_CREATE - 1,
                    "The number of items marked for delete is not correct.");
            //
            // Search for the newly-created records, including the soft deleted record.
            //
            // Send the search request and receive a response
            res = client.keywordSearchIncludeDeleted(KEYWORD, Boolean.TRUE);
            result = res.getStatus();
            Assert.assertEquals(result, STATUS_OK);

            list = res.getEntity();
            itemsMatchedBySearch = list.getTotalItems();
            Assert.assertEquals(itemsMatchedBySearch, OBJECTS_TO_CREATE,
                    "Deleted item was not returned in list results, even though it was requested to be included.");

        } catch (UnsupportedOperationException e) {
            logger.warn(this.getClass().getName() + " did not implement createWorkflowTarget() method.  No workflow tests performed.");
            return;
        }
    }
    
    // ---------------------------------------------------------------
    // Utility methods to support the test cases.
    //
    // ---------------------------------------------------------------
    
    /**
     * Checks if 'theFile' is something we can turn into a Blob instance.  It can't
     * be read-protected, hidden, or a directory.
     *
     * @param theFile the the file
     * @return true, if is blobable
     */
    protected boolean isBlobbable(File theFile) {
        boolean result = true;
        if (theFile.isDirectory() || theFile.isHidden() || !theFile.canRead()) {
            result = false;
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithEmptyEntityBody(java.lang.String)
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

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName",
    	dependsOnMethods = {"create", "update"})
    public void updateNonExistent(String testName) throws Exception {
    	// Perform setup.
    	setupUpdateNonExistent();

    	// Submit the request to the service and store the response.
    	// Note: The ID used in this 'create' call may be arbitrary.
    	// The only relevant ID may be the one used in update(), below.
        CollectionSpaceClient client = this.getClientInstance();
    	REQUEST_TYPE multipart = createNonExistenceInstance(client.getCommonPartName(), NON_EXISTENT_ID);
    	ClientResponse<String> res =
    			client.update(NON_EXISTENT_ID, multipart);
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
    // Utility methods to clean up resources created during tests.
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
    // Utility methods in support of list tests.
    // ---------------------------------------------------------------
    /**
     * Read list.
     *
     * @param testName the test name
     * @param client the client
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the abstract common list
     * @throws Exception the exception
     */
    private CLT readList(String testName,
            CollectionSpaceClient client,
            long pageSize,
            long pageNumber,
            int expectedStatus) throws Exception {

        return readList(testName, client, EMPTY_SORT_BY_ORDER, pageSize, pageNumber, expectedStatus);
    }

    /**
     * Read list.
     *
     * @param testName the test name
     * @param client the client
     * @param sortBy the sort order
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the abstract common list
     * @throws Exception the exception
     */
    private CLT readList(String testName,
            CollectionSpaceClient client,
            String sortBy,
            long pageSize,
            long pageNumber,
            int expectedStatus) throws Exception {
        ClientResponse<CLT> response =
                client.readList(sortBy, pageSize, pageNumber);
        CLT result = null;
        try {
            int statusCode = response.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(testName + ": status = " + statusCode);
            }
            Assert.assertEquals(statusCode, expectedStatus);

            result = this.getCommonList(response);
        } finally {
            response.releaseConnection();
        }

        return result;
    }

    /*@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void leafCreate(String testName) throws Exception {
    this.create(testName);
    }*/
    private void assertPaginationInfo(String testName,
            AbstractCommonList list,
            long expectedPageNum,
            long expectedPageSize,
            long expectedListSize,
            long expectedTotalItems) {
        Assert.assertNotNull(list);

        long pageNum = list.getPageNum();
        Assert.assertEquals(pageNum, expectedPageNum);
        if (getLogger().isDebugEnabled() == true) {
            getLogger().debug(testName + ":" + "page number is " + pageNum);
        }

        long pageSizeReturned = list.getPageSize();
        Assert.assertEquals(pageSizeReturned, expectedPageSize);
        if (getLogger().isDebugEnabled() == true) {
            getLogger().debug(testName + ":" + "page size is " + list.getPageSize());
        }

        long itemsInPage = list.getItemsInPage();
        Assert.assertEquals(itemsInPage, expectedListSize);
        if (getLogger().isDebugEnabled() == true) {
            getLogger().debug(testName + ":" + "actual items in page was/were " + itemsInPage);
        }

        long totalItemsReturned = list.getTotalItems();
        Assert.assertEquals(totalItemsReturned, expectedTotalItems);
        if (getLogger().isDebugEnabled() == true) {
            getLogger().debug(testName + ":" + "total number of items is " + list.getTotalItems());
        }
    }
    

    @SuppressWarnings("rawtypes")
    protected void updateLifeCycleState(String testName, String resourceId, String lifeCycleState) throws Exception {
        //
        // Read the existing object
        //
        CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<String> res = client.getWorkflow(resourceId);
        WorkflowCommon workflowCommons = null;
        try {
	        assertStatusCode(res, testName);
	        logger.debug("Got object to update life cycle state with ID: " + resourceId);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        workflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
	        Assert.assertNotNull(workflowCommons);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Mark it for a soft delete.
        //
        logger.debug("Current workflow state:" + objectAsXmlString(workflowCommons, WorkflowCommon.class));
        workflowCommons.setCurrentLifeCycleState(lifeCycleState);
        PoxPayloadOut output = new PoxPayloadOut(WorkflowClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(WorkflowClient.SERVICE_COMMONPART_NAME, workflowCommons);
        //
        // Perform the update
        //
        WorkflowCommon updatedWorkflowCommons = null;
        res = client.updateWorkflow(resourceId, output);
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
	        Assert.assertNotNull(updatedWorkflowCommons);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Read the updated object and make sure it was updated correctly.
        //
        int trials = 0;
        while (trials < 30) {
	        res = client.getWorkflow(resourceId);
	        try {
		        assertStatusCode(res, testName);
		        logger.debug("Got workflow state of updated object with ID: " + resourceId);
		        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
		        updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
		        Assert.assertNotNull(workflowCommons);
		        String currentWorkflowState = updatedWorkflowCommons.getCurrentLifeCycleState();
		        if (currentWorkflowState.equalsIgnoreCase(lifeCycleState)) {
		        	logger.debug("Expected workflow state found: " + lifeCycleState);
		        	break;
		        }
	        } finally {
	        	if (res != null) {
                    res.releaseConnection();
                }
	        }
	        trials++;
        }
        //
        // Finally, assert the state change happened as expected.
        //
        Assert.assertEquals(updatedWorkflowCommons.getCurrentLifeCycleState(), lifeCycleState);
    }

    private CollectionSpacePoxClient assertPoxClient() {
        CollectionSpaceClient clientCandidate = this.getClientInstance();
        if (CollectionSpacePoxClient.class.isInstance(clientCandidate) != true) {  //FIXME: REM - We should remove this check and instead make CollectionSpaceClient support the readIncludeDeleted() method.
            String clientCandidateName = "Unknown";
            if (clientCandidate != null) {
                clientCandidateName = clientCandidate.getClass().getName();
            }
            String msg = "Workflow tests are incomplete because "
                    + clientCandidateName + " does not support readIncludeDeleted() method.";
            logger.warn(msg);
            throw new UnsupportedOperationException();
        }
        return (CollectionSpacePoxClient) clientCandidate;
    }

    protected long readIncludeDeleted(String testName, Boolean includeDeleted) {
    	long result = 0;
    	// Perform setup.
    	setupReadList();

    	//
    	// Ask for a list of all resources filtered by the incoming 'includeDeleted' workflow param
    	//
    	CollectionSpacePoxClient client = assertPoxClient();
    	ClientResponse<AbstractCommonList> res = client.readIncludeDeleted(includeDeleted);
    	try {
	    	//
	    	// Check the status code of the response: does it match
	    	// the expected response(s)?
	    	//
	    	assertStatusCode(res, testName);
	    	AbstractCommonList list = res.getEntity();
	    	//
	    	// Now check that list size is correct
	    	//
	    	result = list.getTotalItems();
    	} finally {
    		if (res != null) {
                res.releaseConnection();
            }
    	}

    	return result;
    }

    protected long readItemsIncludeDeleted(String testName, String parentCsid, Boolean includeDeleted) {
        long result = 0;
        // Perform setup.
        setupReadList();
        //
        // Ask for a list of all resources filtered by the incoming 'includeDeleted' workflow param
        //
        AuthorityClient client = (AuthorityClient) this.getClientInstance();
        ClientResponse<AbstractCommonList> res = client.readItemList(parentCsid,
                null, /* partial terms */
                null, /* keywords */
                includeDeleted);
        try {
	        assertStatusCode(res, testName);
	        AbstractCommonList list = res.getEntity();
	        result = list.getTotalItems();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }


    protected String createTestObject() throws Exception {
        String identifier = createIdentifier();
        String result = createTestObject(identifier);
        return result;
    }

    protected String createTestObject(String identifier) throws Exception {
        String result = null;

        CollectionSpaceClient client = getClientInstance();
        REQUEST_TYPE payload = createInstance(identifier);
        ClientResponse<Response> res = client.create(payload);

        int statusCode = res.getStatus();
        Assert.assertEquals(statusCode, STATUS_CREATED);

        result = extractId(res);
        allResourceIdsCreated.add(result);

        return result;
    }

    protected String createWorkflowTarget(String testName) throws Exception {
        String result = null;
        result = createTestObject();
        return result;
    }

    protected String createWorkflowTarget(String testName, String keyword) throws Exception {
        String result = null;
        result = createTestObject(keyword);
        return result;
    }

    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) {
        logger.warn("Sub-class test clients should override this method");
        throw new UnsupportedOperationException();
    }

    final protected String createWorkflowItemTarget(String testName, String parentCsid) throws Exception {
        String result = null;
        result = createTestItemObject(testName, parentCsid);
        return result;
    }

    protected String createTestItemObject(String testName, String parentCsid) throws Exception {
        String result = null;

        AuthorityClient client = (AuthorityClient) getClientInstance();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createItemInstance(parentCsid, identifier);
        ClientResponse<Response> res = client.createItem(parentCsid, multipart);

        int statusCode = res.getStatus();
        Assert.assertEquals(statusCode, STATUS_CREATED);

        result = extractId(res);
        allResourceItemIdsCreated.put(result, parentCsid);

        return result;
    }

    /*
     * This test assumes that no objects exist yet.
     *
     * http://localhost:8180/cspace-services/intakes?wf_deleted=false
     */
    @Test(dataProvider = "testName")
    public void readAuthorityItemWorkflow(String testName) throws Exception {
        //
        // Run this test only if the client is an AuthorityClient //FIXME: REM - Replace this will an AuthorityServiceTest class
        //
        if (this.isAuthorityClient(this.getClientInstance()) == true) {
            try {
                //
                // Get the total count of non-deleted existing records
                //
                String parentCsid = this.createTestObject(testName);

                //
                // Create 3 new items
                //
                final int OBJECTS_TO_CREATE = 3;
                String lastCreatedItem = null;

                for (int i = 0; i < OBJECTS_TO_CREATE; i++) {
                    lastCreatedItem = this.createWorkflowItemTarget(testName, parentCsid);
                } //
                // Mark one item as soft deleted
                //
                String csid = lastCreatedItem;

                this.setupUpdate();

                this.updateItemLifeCycleState(testName, parentCsid, csid, WorkflowClient.WORKFLOWSTATE_DELETED);
                //
                // Read the list of existing non-deleted records
                //

                long updatedTotal = readItemsIncludeDeleted(testName, parentCsid, Boolean.FALSE);
                Assert.assertEquals(updatedTotal, OBJECTS_TO_CREATE - 1, "Deleted items seem to be returned in list results.");

                //
                // Next, test that a GET with WorkflowClient.WORKFLOWSTATE_DELETED query param set to 'false' returns a 404
                //
                AuthorityClient client = (AuthorityClient) this.getClientInstance();
                ClientResponse<String> res = client.readItem(parentCsid, csid, Boolean.FALSE);

                int result = res.getStatus();
                Assert.assertEquals(result, STATUS_NOT_FOUND);

            } catch (UnsupportedOperationException e) {
                logger.warn(this.getClass().getName() + " did not implement createWorkflowTarget() method.  No workflow tests performed.");
                return;
            }
        }
    }

    protected void updateItemLifeCycleState(String testName, String parentCsid, String itemCsid, String lifeCycleState) throws Exception {
        //
        // Read the existing object
        //
        AuthorityClient client = (AuthorityClient) this.getClientInstance();
        ClientResponse<String> res = client.readItemWorkflow(parentCsid, itemCsid);
        WorkflowCommon workflowCommons = null;
        try {
	        assertStatusCode(res, testName);
	        logger.debug("Got object to update life cycle state with ID: " + itemCsid);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        workflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
	        Assert.assertNotNull(workflowCommons);
	        logger.debug("Current workflow state:" + objectAsXmlString(workflowCommons, WorkflowCommon.class));
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Mark it for a state change.
        //
        workflowCommons.setCurrentLifeCycleState(lifeCycleState);
        PoxPayloadOut output = new PoxPayloadOut(WorkflowClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(WorkflowClient.SERVICE_COMMONPART_NAME, workflowCommons);
        //
        // Perform the state change update
        //
        res = client.updateItemWorkflow(parentCsid, itemCsid, output);
        WorkflowCommon updatedWorkflowCommons = null;
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
	        Assert.assertNotNull(updatedWorkflowCommons);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        int trials = 0;
        boolean passed = false;
        while (trials < 30) { //wait to see if the lifecycle transition will happen
	        //
	        // Read the updated object and make sure it was updated correctly.
	        //
	        res = client.readItemWorkflow(parentCsid, itemCsid);
	        try {
		        assertStatusCode(res, testName);
		        logger.debug(
		                "Got workflow state of updated object with ID: " + itemCsid);
		        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
		        updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
		        Assert.assertNotNull(workflowCommons);
		        String currentState = updatedWorkflowCommons.getCurrentLifeCycleState();
		        if (currentState.equalsIgnoreCase(lifeCycleState)) {
		        	logger.debug("Expected workflow state found: " + lifeCycleState);
		        	break;
		        }
		        logger.debug("Workflow state not yet updated for object with id: " + itemCsid + " state is=" +
		        		currentState);
	        } finally {
	        	if (res != null) {
                    res.releaseConnection();
                }
	        }
	        trials++;
        }
        //
        // Finally check to see if the state change was updated as expected.
        //
        Assert.assertEquals(updatedWorkflowCommons.getCurrentLifeCycleState(), lifeCycleState);
    }

}


