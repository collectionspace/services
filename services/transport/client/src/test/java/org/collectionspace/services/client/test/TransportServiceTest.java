/*
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.client.test;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TransportClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.transport.TransportsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class TransportServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, TransportsCommon> {

    /** The logger. */
    private final String CLASS_NAME = TransportServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    /** The service path component. */
    final String SERVICE_NAME = "transports";
    final String SERVICE_PATH_COMPONENT = "transports";


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
    //    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        TransportClient client = new TransportClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createTransportInstance(identifier);
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
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
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
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
    //    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create"})
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
    //    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        TransportClient client = new TransportClient();
        Response res = client.read(knownResourceId);
        PoxPayloadIn input = null;
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
        TransportsCommon transportCommon = null;
        if (payloadInputPart != null) {
            transportCommon = (TransportsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(transportCommon);
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
        TransportClient client = new TransportClient();
        Response res = client.read(NON_EXISTENT_ID);
        try {
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
    //    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AbstractCommonList list = null;
        TransportClient client = new TransportClient();
        Response res = client.readList();
        assertStatusCode(res, testName);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            list = res.readEntity(getCommonListType());
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
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
    //    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Retrieve the contents of a resource to update.
        TransportClient client = new TransportClient();
        Response res = client.read(knownResourceId);
        PoxPayloadIn input = null;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.readEntity(String.class));
            if (logger.isDebugEnabled()) {
                logger.debug("got object to update with ID: " + knownResourceId);
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Extract the common part from the response.
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        TransportsCommon transportCommon = null;
        if (payloadInputPart != null) {
            transportCommon = (TransportsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(transportCommon);

        // Update the content of this resource.
        transportCommon.setTransportReferenceNumber("updated-" + transportCommon.getTransportReferenceNumber());

        if (logger.isDebugEnabled()) {
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(transportCommon, TransportsCommon.class));
        }

        setupUpdate();

        // Submit the updated common part in an update request to the service
        // and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), transportCommon);
        res = client.update(knownResourceId, output);
        try {
            assertStatusCode(res, testName);
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
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
        TransportsCommon updatedTransportCommon = null;
        if (payloadInputPart != null) {
            updatedTransportCommon = (TransportsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(updatedTransportCommon);

        // Check selected fields in the updated common part.
        Assert.assertEquals(updatedTransportCommon.getTransportReferenceNumber(),
                transportCommon.getTransportReferenceNumber(),
                "Data in updated object did not match submitted data.");
    }

    @Override
    //    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        TransportClient client = new TransportClient();
        PoxPayloadOut multipart = createTransportInstance(NON_EXISTENT_ID);
        Response res = client.update(NON_EXISTENT_ID, multipart);
        try {
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
    //    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        TransportClient client = new TransportClient();
        Response res = client.delete(knownResourceId);
        try {
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
                res.close();
            }
        }
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
        TransportClient client = new TransportClient();
        Response res = client.delete(NON_EXISTENT_ID);
        try {
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
    //    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {

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

    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        return createTransportInstance(identifier);
    }

    /**
     * Creates the transport instance.
     *
     * @param transportNumber the transport number
     * @return the multipart output
     * @throws Exception
     */
    private PoxPayloadOut createTransportInstance(String transportNumber) throws Exception {

        TransportsCommon transportCommon = new TransportsCommon();
        transportCommon.setTransportReferenceNumber(transportNumber);
        // transportCommon.setDestination(getUTF8DataFragment());

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(new TransportClient().getCommonPartName(), transportCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, transport common");
            logger.debug(objectAsXmlString(transportCommon, TransportsCommon.class));
        }

        return multipart;
    }

    @Override
    public void CRUDTests(String testName) {
        // TODO Auto-generated method stub

    }

    @Override
    protected PoxPayloadOut createInstance(String commonPartName,
            String identifier) throws Exception {
        PoxPayloadOut result = createTransportInstance(identifier);
        return result;
    }

    @Override
    protected TransportsCommon updateInstance(TransportsCommon commonPartObject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void compareUpdatedInstances(TransportsCommon original,
            TransportsCommon updated) throws Exception {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new TransportClient();
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new TransportClient(clientPropertiesFilename);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() throws Exception {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }
}
