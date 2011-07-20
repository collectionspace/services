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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.collectionspace.services.client.AbstractPoxServiceClientImpl;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.Response;

/**
 * AbstractServiceTestImpl
 *
 * Abstract base class for client tests of entity and relation services.
 * Abstract methods are provided for a set of CRUD + List tests to be invoked.
 *
 * For Javadoc descriptions of this class's methods, see the ServiceTest interface.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
// FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
public abstract class AbstractServiceTestImpl extends BaseServiceTest implements ServiceTest {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(AbstractServiceTestImpl.class);
    // A non-existent logger.
    static private final Logger LOGGER_NULL = null;
    /** The Constant DEFAULT_LIST_SIZE. */
    static protected final int DEFAULT_LIST_SIZE = 10;
    static protected final int DEFAULT_PAGINATEDLIST_SIZE = 10;
    static protected final String RESOURCE_PATH = "src" + File.separator
            + "test" + File.separator
            + "resources";
    protected static final String BLOBS_DIR = "blobs";
    static protected final String DEFAULT_MIME = "application/octet-stream; charset=ISO-8859-1";
    static private final String NO_TEST_CLEANUP = "noTestCleanup";
    static protected final String NO_BLOB_CLEANUP = "noBlobCleanup";
    static protected final String NO_MEDIA_CLEANUP = "noMediaCleanup";
    final static String NON_EXISTENT_KEYWORD = "jlmbsoqjlmbsoq";

    protected String getMimeType(File theFile) {
        String result = null;
        result = new MimetypesFileTypeMap().getContentType(theFile);
        if (result == null) {
            logger.debug("Could not get MIME type for file at: " + theFile.getAbsolutePath());
            result = DEFAULT_MIME;
        }

        return result;

    }
    
    protected void ListItemsInAbstractCommonList(
    		AbstractCommonList list, Logger logger, String testName) {
    	List<AbstractCommonList.ListItem> items =
    		list.getListItem();
    	int i = 0;
    	for(AbstractCommonList.ListItem item : items){
    		List<Element> elList = item.getAny();
    		StringBuilder elementStrings = new StringBuilder();
    		for(Element el : elList) {
    			Node textEl = el.getFirstChild();
   				elementStrings.append("["+el.getNodeName()+":"+((textEl!=null)?textEl.getNodeValue():"NULL")+"] ");
    		}
    		logger.debug(testName + ": list-item[" + i + "]: "+elementStrings.toString());
    		i++;
    	}
    }

    protected static String ListItemGetCSID(AbstractCommonList.ListItem item) {
		List<Element> elList = item.getAny();
		for(Element el : elList) {
			if("csid".equalsIgnoreCase(el.getNodeName())) {
    			Node textEl = el.getFirstChild();
    			return textEl.getNodeValue();
			}
		}
		return null;
	}


    /* Use this to keep track of resources to delete */
    protected List<String> allResourceIdsCreated = new ArrayList<String>();
    /* Use this to track authority items */
    protected Map<String, String> allResourceItemIdsCreated = new HashMap<String, String>(); /* itemCsid, parentCsid */

    private String EMPTY_SORT_BY_ORDER = "";

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    private Logger getLogger() {
        return this.logger;
    }

    protected String getResourceDir() {
        String result = null;
        String currentDirectory = System.getProperty("user.dir");
        result = currentDirectory + File.separator + RESOURCE_PATH;
        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getAbstractCommonList(
            ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    //
    // (See below for utility methods in support of create list tests.)
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    public abstract void create(String testName) throws Exception;

    /**
     * Sets up create tests.
     */
    protected void setupCreate() {
        EXPECTED_STATUS_CODE = STATUS_CREATED;
        REQUEST_TYPE = ServiceRequestType.CREATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

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
     * @see org.collectionspace.services.client.test.ServiceTest#createList(java.lang.String)
     */
    @Override
    public abstract void createList(String testName) throws Exception;

    // Note: No setup is required for createList(), as it currently
    // just invokes create() multiple times.
    // Failure outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public abstract void createWithEmptyEntityBody(String testName)
            throws Exception;

    /**
     * Sets up create tests with empty entity body.
     */
    protected void setupCreateWithEmptyEntityBody() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.CREATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    /**
     * Sets up create tests with empty entity body.
     */
    protected void setupCreateWithInvalidBody() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.CREATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithMalformedXml(java.lang.String)
     */
    @Override
    public abstract void createWithMalformedXml(String testName) throws Exception;

    /**
     * Sets up create tests with malformed xml.
     */
    protected void setupCreateWithMalformedXml() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.CREATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public abstract void createWithWrongXmlSchema(String testName) throws Exception;

    /**
     * Sets up create tests with wrong xml schema.
     */
    protected void setupCreateWithWrongXmlSchema() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.CREATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#read(java.lang.String)
     */
    @Override
    public abstract void read(String testName) throws Exception;

    /**
     * Sets up read tests.
     */
    protected void setupRead() {
        EXPECTED_STATUS_CODE = STATUS_OK;
        REQUEST_TYPE = ServiceRequestType.READ;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // Failure outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#readNonExistent(java.lang.String)
     */
    @Override
    public abstract void readNonExistent(String testName) throws Exception;

    /**
     * Sets up read non existent tests.
     */
    protected void setupReadNonExistent() {
        EXPECTED_STATUS_CODE = STATUS_NOT_FOUND;
        REQUEST_TYPE = ServiceRequestType.READ;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ (list, or multiple) tests
    //
    // (See below for utility methods in support of list tests.)
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#readList(java.lang.String)
     */
    @Override
    public abstract void readList(String testName) throws Exception;

    /**
     * Sets up read list tests.
     */
    protected void setupReadList() {
        EXPECTED_STATUS_CODE = STATUS_OK;
        REQUEST_TYPE = ServiceRequestType.READ_LIST;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // Failure outcomes
    // None tested at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#update(java.lang.String)
     */
    @Override
    public abstract void update(String testName) throws Exception;

    /**
     * Sets up update tests.
     */
    protected void setupUpdate() {
        EXPECTED_STATUS_CODE = STATUS_OK;
        REQUEST_TYPE = ServiceRequestType.UPDATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // Failure outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public abstract void updateWithEmptyEntityBody(String testName) throws Exception;

    /**
     * Sets up update tests with an empty entity body.
     */
    protected void setupUpdateWithEmptyEntityBody() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.UPDATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public abstract void updateWithMalformedXml(String testName) throws Exception;

    /**
     * Sets up update tests with malformed xml.
     */
    protected void setupUpdateWithMalformedXml() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.UPDATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public abstract void updateWithWrongXmlSchema(String testName) throws Exception;

    /**
     * Sets up update tests with wrong xml schema.
     */
    protected void setupUpdateWithWrongXmlSchema() {
        EXPECTED_STATUS_CODE = STATUS_BAD_REQUEST;
        REQUEST_TYPE = ServiceRequestType.UPDATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateNonExistent(java.lang.String)
     */
    @Override
    public abstract void updateNonExistent(String testName) throws Exception;

    /**
     * Sets up update non existent tests
     */
    protected void setupUpdateNonExistent() {
        EXPECTED_STATUS_CODE = STATUS_NOT_FOUND;
        REQUEST_TYPE = ServiceRequestType.UPDATE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#delete(java.lang.String)
     */
    @Override
    public abstract void delete(String testName) throws Exception;

    /**
     * Sets up delete tests.
     */
    protected void setupDelete() {
        EXPECTED_STATUS_CODE = STATUS_OK;
        REQUEST_TYPE = ServiceRequestType.DELETE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // Failure outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#deleteNonExistent(java.lang.String)
     */
    @Override
    public abstract void deleteNonExistent(String testName) throws Exception;

    /**
     * Sets up delete non existent tests.
     */
    protected void setupDeleteNonExistent() {
        EXPECTED_STATUS_CODE = STATUS_NOT_FOUND;
        REQUEST_TYPE = ServiceRequestType.DELETE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);
    }

    // ---------------------------------------------------------------
    // Utility methods to clean up resources created during tests.
    // ---------------------------------------------------------------
    /**
     * Deletes all resources created by tests, after all tests have been run.
     *
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        String noTestCleanup = System.getProperty(NO_TEST_CLEANUP);
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTestCleanup)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        CollectionSpaceClient client = this.getClientInstance();
        //
        // First, check to see if we need to cleanup any authority items
        //
        if (this.isAuthorityClient(client) == true) {
            AuthorityClient authorityClient = (AuthorityClient) client;
            for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
                String itemResourceId = entry.getKey();
                String authorityResourceId = entry.getValue();
                // Note: Any non-success responses are ignored and not reported.
                authorityClient.deleteItem(authorityResourceId, itemResourceId).releaseConnection();
            }
        }
        //
        // Next, delete all other entities include possible authorities.
        //
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            client.delete(resourceId).releaseConnection();
        }
    }

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
    private AbstractCommonList readList(String testName,
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
    private AbstractCommonList readList(String testName,
            CollectionSpaceClient client,
            String sortBy,
            long pageSize,
            long pageNumber,
            int expectedStatus) throws Exception {
        ClientResponse<AbstractCommonList> response =
                client.readList(sortBy, pageSize, pageNumber);
        AbstractCommonList result = null;
        try {
            int statusCode = response.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(testName + ": status = " + statusCode);
            }
            Assert.assertEquals(statusCode, expectedStatus);

            result = this.getAbstractCommonList(response);
        } finally {
            response.releaseConnection();
        }

        return result;
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
            create(testName);
        }
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

    /**
     * Read paginated list.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName") /*, dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"leafCreate"}) */

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
                EXPECTED_STATUS_CODE);
        if (list == null || list.getTotalItems() == 0) {
            this.createPaginatedList(testName, DEFAULT_PAGINATEDLIST_SIZE);
            setupReadList();
            list = (AbstractCommonList) this.readList(testName,
                    client,
                    1 /*pgSz*/,
                    0 /*pgNum*/,
                    EXPECTED_STATUS_CODE);
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
            list = (AbstractCommonList) this.readList(testName, client, pageSize, i, EXPECTED_STATUS_CODE);
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
            list = (AbstractCommonList) this.readList(testName, client, pageSize, pagesTotal, EXPECTED_STATUS_CODE);
            assertPaginationInfo(testName,
                    list,
                    pagesTotal, //expected page number
                    pageSize, //expected page size
                    mod, //expected num of items in page
                    totalItems);//expected total num of items
        }
    }

    @SuppressWarnings("rawtypes")
    protected void updateLifeCycleState(String testName, String resourceId, String lifeCycleState) throws Exception {
        //
        // Read the existing object
        //
        CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<String> res = client.getWorkflow(resourceId);
        assertStatusCode(res, testName);
        logger.debug("Got object to update life cycle state with ID: " + resourceId);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        WorkflowCommon workflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
        Assert.assertNotNull(workflowCommons);
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
        res = client.updateWorkflow(resourceId, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        WorkflowCommon updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
        Assert.assertNotNull(updatedWorkflowCommons);
        //
        // Read the updated object and make sure it was updated correctly.
        //
        res = client.getWorkflow(resourceId);
        assertStatusCode(res, testName);
        logger.debug("Got workflow state of updated object with ID: " + resourceId);
        input = new PoxPayloadIn(res.getEntity());
        updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
        Assert.assertNotNull(workflowCommons);
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
        AbstractCommonList list = res.getEntity();
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
        /*
        List<AbstractCommonList.ListItem> items =
        list.getListItem();
        result = items.size();
         */
        result = list.getTotalItems();

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
        AbstractCommonList list = res.getEntity();
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

        result = list.getTotalItems();

        return result;
    }

    /*
     * This test assumes that no objects exist yet.
     *
     * http://localhost:8180/cspace-services/intakes?wf_deleted=false
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
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
            CollectionSpacePoxClient client = this.assertPoxClient();
            ClientResponse<String> res = client.readIncludeDeleted(csid, Boolean.FALSE);
            int result = res.getStatus();
            Assert.assertEquals(result, STATUS_NOT_FOUND);

        } catch (UnsupportedOperationException e) {
            logger.warn(this.getClass().getName() + " did not implement createWorkflowTarget() method.  No workflow tests performed.");
            return;
        }
    }

    /*
     * Test that searches honor the workflow deleted state.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
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
                    "Deleted items seem to be returned in list results.");

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

    protected String createTestObject(String testName) throws Exception {

        String identifier = createIdentifier();
        String result = createTestObject(testName, identifier);
        return result;

    }

    protected String createTestObject(String testName, String identifier) throws Exception {
        String result = null;

        CollectionSpacePoxClient client = (CollectionSpacePoxClient) getClientInstance();
        PoxPayloadOut multipart = createInstance(identifier);
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();
        Assert.assertEquals(statusCode, STATUS_CREATED);

        result = extractId(res);
        allResourceIdsCreated.add(result);

        return result;
    }

    protected String createWorkflowTarget(String testName) throws Exception {
        String result = null;
        result = createTestObject(testName);
        return result;
    }

    protected String createWorkflowTarget(String testName, String keyword) throws Exception {
        String result = null;
        result = createTestObject(testName, keyword);
        return result;
    }

    /*
     * Sub-classes must override for the workflow tests.
     */
    protected PoxPayloadOut createInstance(String identifier) {
        logger.warn("Sub-class test clients should override this method");


        throw new UnsupportedOperationException();



    }

    /*
     * Test classes for authority services should override these method and return 'true'
     */
    protected boolean isAuthorityClient(CollectionSpaceClient theClient) {
        return AuthorityClient.class.isInstance(theClient);
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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
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


                for (int i = 0; i
                        < OBJECTS_TO_CREATE; i++) {
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
        assertStatusCode(
                res, testName);
        logger.debug("Got object to update life cycle state with ID: " + itemCsid);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        WorkflowCommon workflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
        Assert.assertNotNull(workflowCommons);
        //
        // Mark it for a soft delete.
        //
        logger.debug(
                "Current workflow state:" + objectAsXmlString(workflowCommons, WorkflowCommon.class));
        workflowCommons.setCurrentLifeCycleState(lifeCycleState);
        PoxPayloadOut output = new PoxPayloadOut(WorkflowClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(WorkflowClient.SERVICE_COMMONPART_NAME, workflowCommons);
        //
        // Perform the update
        //
        res = client.updateItemWorkflow(parentCsid, itemCsid, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        WorkflowCommon updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
        Assert.assertNotNull(updatedWorkflowCommons);
        //
        // Read the updated object and make sure it was updated correctly.
        //
        res = client.readItemWorkflow(parentCsid, itemCsid);
        assertStatusCode(res, testName);
        logger.debug(
                "Got workflow state of updated object with ID: " + itemCsid);
        input = new PoxPayloadIn(res.getEntity());
        updatedWorkflowCommons = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
        Assert.assertNotNull(workflowCommons);
        Assert.assertEquals(updatedWorkflowCommons.getCurrentLifeCycleState(), lifeCycleState);
    }
}


