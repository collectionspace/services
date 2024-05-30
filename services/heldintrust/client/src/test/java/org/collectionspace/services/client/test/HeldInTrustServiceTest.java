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
package org.collectionspace.services.client.test;

import javax.ws.rs.core.Response;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.HeldInTrustClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.heldintrust.HeldInTrustsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class HeldInTrustServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, HeldInTrustsCommon> {

    private final Logger logger = LoggerFactory.getLogger(HeldInTrustServiceTest.class);

    /** The service path component. */
    final String SERVICE_NAME = "heldintrusts";

    final String SERVICE_PATH_COMPONENT = "heldintrusts";

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new HeldInTrustClient();
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new HeldInTrustClient(clientPropertiesFilename);
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
        HeldInTrustClient client = new HeldInTrustClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createHeldInTrustInstance(identifier);
        String newID = null;
        Response res = client.create(multipart);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            //
            // Specifically:
            // Does it fall within the set of valid status codes?
            // Does it exactly match the expected status code?
            logger.debug(testName + ": status = " + statusCode);
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
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
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
        HeldInTrustClient client = new HeldInTrustClient();
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
        HeldInTrustsCommon heldInTrustCommon = null;
        if (payloadInputPart != null) {
            heldInTrustCommon = (HeldInTrustsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(heldInTrustCommon);
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
        HeldInTrustClient client = new HeldInTrustClient();
        Response res = client.read(NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug(testName + ": status = " + statusCode);
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
        HeldInTrustClient client = new HeldInTrustClient();
        Response res = client.readList();
        assertStatusCode(res, testName);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug(testName + ": status = " + statusCode);
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
        // Perform setup.
        setupRead();

        // Retrieve the contents of a resource to update.
        HeldInTrustClient client = new HeldInTrustClient();
        Response res = client.read(knownResourceId);
        PoxPayloadIn input;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.readEntity(String.class));
            logger.debug("got object to update with ID: " + knownResourceId);
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Extract the common part from the response.
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        HeldInTrustsCommon heldInTrustCommon = null;
        if (payloadInputPart != null) {
            heldInTrustCommon = (HeldInTrustsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(heldInTrustCommon);

        // Update the content of this resource.
        heldInTrustCommon.setHeldInTrustNumber("updated-" + heldInTrustCommon.getHeldInTrustNumber());

        logger.debug("to be updated object");
        logger.debug(objectAsXmlString(heldInTrustCommon, HeldInTrustsCommon.class));

        setupUpdate();

        // Submit the updated common part in an update request to the service
        // and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), heldInTrustCommon);
        res = client.update(knownResourceId, output);
        try {
            assertStatusCode(res, testName);
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match the expected response(s)?
            logger.debug(testName + ": status = " + statusCode);
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
        HeldInTrustsCommon updatedHeldInTrustCommon = null;
        if (payloadInputPart != null) {
            updatedHeldInTrustCommon = (HeldInTrustsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(updatedHeldInTrustCommon);

        // Check selected fields in the updated common part.
        Assert.assertEquals(
                updatedHeldInTrustCommon.getHeldInTrustNumber(),
                heldInTrustCommon.getHeldInTrustNumber(),
                "Data in updated object did not match submitted data.");
    }

    @Override
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        HeldInTrustClient client = new HeldInTrustClient();
        PoxPayloadOut multipart = createHeldInTrustInstance(NON_EXISTENT_ID);
        Response res = client.update(NON_EXISTENT_ID, multipart);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug(testName + ": status = " + statusCode);
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
        HeldInTrustClient client = new HeldInTrustClient();
        Response res = client.delete(knownResourceId);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug(testName + ": status = " + statusCode);
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
        HeldInTrustClient client = new HeldInTrustClient();
        Response res = client.delete(NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            logger.debug(testName + ": status = " + statusCode);
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
        logger.debug("testSubmitRequest: url=" + url + " status=" + statusCode);
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
        return createHeldInTrustInstance(identifier);
    }

    /**
     * Creates the heldInTrust instance.
     *
     * @param heldInTrustNumber the held in trust number
     * @return the multipart output
     * @throws Exception
     */
    private PoxPayloadOut createHeldInTrustInstance(String heldInTrustNumber) throws Exception {
        HeldInTrustsCommon heldInTrustCommon = new HeldInTrustsCommon();
        heldInTrustCommon.setHeldInTrustNumber(heldInTrustNumber);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(new HeldInTrustClient().getCommonPartName(), heldInTrustCommon);

        logger.debug("to be created, heldInTrust common");
        logger.debug(objectAsXmlString(heldInTrustCommon, HeldInTrustsCommon.class));

        return multipart;
    }

    @Override
    public void CRUDTests(String testName) {
        // TODO Auto-generated method stub

    }

    @Override
    protected PoxPayloadOut createInstance(String commonPartName, String identifier) throws Exception {
        return createHeldInTrustInstance(identifier);
    }

    @Override
    protected HeldInTrustsCommon updateInstance(HeldInTrustsCommon commonPartObject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void compareUpdatedInstances(HeldInTrustsCommon original, HeldInTrustsCommon updated) {
        // TODO Auto-generated method stub
    }
}
