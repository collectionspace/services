/*
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
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;
import org.collectionspace.services.client.test.AbstractPoxServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.exit.ExitsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class ExitServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ExitsCommon> {

    private final Logger logger = LoggerFactory.getLogger(ExitServiceTest.class);

    /** The service path component. */
    final String SERVICE_NAME = "exits";

    final String SERVICE_PATH_COMPONENT = "exits";

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new ExitClient();
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new ExitClient(clientPropertiesFilename);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getCommonList(Response response) {
        return response.readEntity(AbstractCommonList.class);
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        ExitClient client = new ExitClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createExitInstance(identifier);
        String newID;
        Response res = client.create(multipart);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            //
            // Specifically:
            // Does it fall within the set of valid status codes?
            // Does it exactly match the expected status code?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            newID = extractId(res);
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null) {
            knownResourceId = newID;
            logger.debug("{}: knownResourceId={}", testName, knownResourceId);
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
    public void createList(String testName) throws Exception {
        for (int i = 0; i < 3; i++) {
            create(testName);
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------

    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
    @Override
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        ExitClient client = new ExitClient();
        Response res = client.read(knownResourceId);
        PoxPayloadIn input;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Get the common part of the response and verify that it is not null.
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ExitsCommon exitCommon = null;
        if (payloadInputPart != null) {
            exitCommon = (ExitsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(exitCommon);
    }

    // Failure outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        ExitClient client = new ExitClient();
        Response res = client.read(NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.close();
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
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AbstractCommonList list;
        ExitClient client = new ExitClient();
        Response res = client.readList();
        assertStatusCode(res, testName);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            list = res.readEntity(getCommonListType());
        } finally {
            res.close();
        }

        // Optionally output additional data about list members for debugging.
        AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
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
    public void update(String testName) throws Exception {
        setupRead();

        // Retrieve the contents of a resource to update.
        ExitClient client = new ExitClient();
        Response res = client.read(knownResourceId);
        PoxPayloadIn input;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.readEntity(String.class));
            logger.debug("got object to update with ID: {}", knownResourceId);
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Extract the common part from the response.
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ExitsCommon exitCommon = null;
        if (payloadInputPart != null) {
            exitCommon = (ExitsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(exitCommon);

        // Update the content of this resource.
        exitCommon.setExitNumber("updated-" + exitCommon.getExitNumber());

        logger.debug("to be updated object");
        logger.debug(objectAsXmlString(exitCommon, ExitsCommon.class));

        setupUpdate();

        // Submit the updated common part in an update request to the service
        // and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        output.addPart(client.getCommonPartName(), exitCommon);
        res = client.update(knownResourceId, output);
        try {
            assertStatusCode(res, testName);
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match the expected response(s)?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Extract the updated common part from the response.
        payloadInputPart = input.getPart(client.getCommonPartName());
        ExitsCommon updatedExitCommon = null;
        if (payloadInputPart != null) {
            updatedExitCommon = (ExitsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(updatedExitCommon);

        // Check selected fields in the updated common part.
        Assert.assertEquals(
                updatedExitCommon.getExitNumber(),
                exitCommon.getExitNumber(),
                "Data in updated object did not match submitted data.");
    }

    @Override
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        ExitClient client = new ExitClient();
        PoxPayloadOut multipart = createExitInstance(NON_EXISTENT_ID);
        Response res = client.update(NON_EXISTENT_ID, multipart);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.close();
            }
        }
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
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ExitClient client = new ExitClient();
        Response res = client.delete(knownResourceId);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    // Failure outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    public void deleteNonExistent(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        ExitClient client = new ExitClient();
        Response res = client.delete(NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(
                    testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    public void testSubmitRequest() {
        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        logger.debug("testSubmitRequest: url={} status={}", url, statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        return createExitInstance(identifier);
    }

    /**
     * Creates the exit instance.
     *
     * @param exitNumber the exhibition number
     * @return the multipart output
     * @throws Exception
     */
    private PoxPayloadOut createExitInstance(String exitNumber) throws Exception {
        ExitsCommon exitCommon = new ExitsCommon();
        exitCommon.setExitNumber(exitNumber);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        multipart.addPart(new ExitClient().getCommonPartName(), exitCommon);

        logger.debug("to be created, exit common");
        logger.debug(objectAsXmlString(exitCommon, ExitsCommon.class));

        return multipart;
    }

    @Override
    public void CRUDTests(String testName) {}

    @Override
    protected PoxPayloadOut createInstance(String commonPartName, String identifier) throws Exception {
        return createExitInstance(identifier);
    }

    @Override
    protected ExitsCommon updateInstance(ExitsCommon commonPartObject) {
        return null;
    }

    @Override
    protected void compareUpdatedInstances(ExitsCommon original, ExitsCommon updated) {}
}
