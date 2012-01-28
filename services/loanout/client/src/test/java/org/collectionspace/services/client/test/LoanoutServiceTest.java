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

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanout.LoanStatusGroup;
import org.collectionspace.services.loanout.LoanStatusGroupList;
import org.collectionspace.services.loanout.LoansoutCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoanoutServiceTest, carries out tests against a
 * deployed and running Loanout (aka Loans Out) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class LoanoutServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, LoansoutCommon> {

    /** The logger. */
    private final String CLASS_NAME = LoanoutServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    /** The known resource id. */
    private final static String CURRENT_DATE_UTC =
        GregorianCalendarDateTimeUtils.currentDateUTC();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new LoanoutClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getCommonList(
            ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
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
        LoanoutClient client = new LoanoutClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createLoanoutInstance(identifier);
        ClientResponse<Response> res = client.create(multipart);

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

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null) {
            knownResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(extractId(res));
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

    /*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupCreateWithEmptyEntityBody();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = "";
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug("createWithEmptyEntityBody url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupCreateWithMalformedXml();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA; // Constant from base class.
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithWrongXmlSchema(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupCreateWithWrongXmlSchema();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
     */
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
        LoanoutClient client = new LoanoutClient();
        ClientResponse<String> res = client.read(knownResourceId);
        LoansoutCommon loanoutCommon = null;
        try {
	        assertStatusCode(res, testName);
	        // Get the common part of the response and verify that it is not null.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
	        if (payloadInputPart != null) {
	            loanoutCommon = (LoansoutCommon) payloadInputPart.getBody();
	        }
	        Assert.assertNotNull(loanoutCommon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Check selected fields in the common part.
        Assert.assertNotNull(loanoutCommon.getLoanOutNumber());

        LoanStatusGroupList statusGroupList = loanoutCommon.getLoanStatusGroupList();
        Assert.assertNotNull(statusGroupList);
        List<LoanStatusGroup> statusGroups = statusGroupList.getLoanStatusGroup();
        Assert.assertNotNull(statusGroups);
        Assert.assertTrue(statusGroups.size() > 0);
        LoanStatusGroup statusGroup = statusGroups.get(0);
        Assert.assertNotNull(statusGroup);
        Assert.assertNotNull(statusGroup.getLoanStatus());

        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + loanoutCommon.getLoanOutNote());
        }
        Assert.assertEquals(loanoutCommon.getLoanOutNote(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + loanoutCommon.getLoanOutNote()
                + "' does not match expected data '" + getUTF8DataFragment());
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
        LoanoutClient client = new LoanoutClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
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
        LoanoutClient client = new LoanoutClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()){
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
        LoanoutClient client = new LoanoutClient();
        ClientResponse<String> res = client.read(knownResourceId);
        LoansoutCommon loanoutCommon = null;
        try {
	        assertStatusCode(res, testName);
	        // Extract the common part from the response.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
	        if (payloadInputPart != null) {
	            loanoutCommon = (LoansoutCommon) payloadInputPart.getBody();
	        }
	        Assert.assertNotNull(loanoutCommon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Update the content of this resource.
        loanoutCommon.setLoanOutNumber("updated-" + loanoutCommon.getLoanOutNumber());
        LoanStatusGroupList statusGroupList = loanoutCommon.getLoanStatusGroupList();
        Assert.assertNotNull(statusGroupList);
        List<LoanStatusGroup> statusGroups = statusGroupList.getLoanStatusGroup();
        Assert.assertNotNull(statusGroups);
        Assert.assertTrue(statusGroups.size() > 0);
        LoanStatusGroup statusGroup = statusGroups.get(0);
        Assert.assertNotNull(statusGroup);
        String loanStatus = statusGroup.getLoanStatus();
        Assert.assertNotNull(loanStatus);
        String updatedLoanStatus = "updated-" + loanStatus;
        statusGroups.get(0).setLoanStatus(updatedLoanStatus);
        loanoutCommon.setLoanStatusGroupList(statusGroupList);
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(loanoutCommon, LoansoutCommon.class));
        }
        loanoutCommon.setLoanOutNote("updated-" + loanoutCommon.getLoanOutNote());

        setupUpdate();
        
        // Submit the updated resource in an update request to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), loanoutCommon);

        res = client.update(knownResourceId, output);
        LoansoutCommon updatedLoanoutCommon = null;
        try {
	        assertStatusCode(res, testName);
	        // Extract the updated common part from the response.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
	        if (payloadInputPart != null) {
	            updatedLoanoutCommon = (LoansoutCommon) payloadInputPart.getBody();
	        }
	        Assert.assertNotNull(updatedLoanoutCommon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Check selected fields in the updated resource.
        Assert.assertEquals(updatedLoanoutCommon.getLoanOutNumber(),
                loanoutCommon.getLoanOutNumber(),
                "Data in updated object did not match submitted data.");

        LoanStatusGroupList updatedStatusGroupList =
                updatedLoanoutCommon.getLoanStatusGroupList();
        Assert.assertNotNull(updatedStatusGroupList);
        List<LoanStatusGroup> updatedStatusGroups =
                updatedStatusGroupList.getLoanStatusGroup();
        Assert.assertNotNull(updatedStatusGroups);
        Assert.assertTrue(updatedStatusGroups.size() > 0);
        Assert.assertNotNull(updatedStatusGroups.get(0));
        Assert.assertEquals(updatedLoanStatus,
                updatedStatusGroups.get(0).getLoanStatus(),
                "Data in updated object did not match submitted data.");

        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + loanoutCommon.getLoanOutNote() + "\n"
                    + "UTF-8 data received=" + updatedLoanoutCommon.getLoanOutNote());
        }
        Assert.assertTrue(updatedLoanoutCommon.getLoanOutNote().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedLoanoutCommon.getLoanOutNote()
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedLoanoutCommon.getLoanOutNote(),
                loanoutCommon.getLoanOutNote(),
                "Data in updated object did not match submitted data.");
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

    /*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithEmptyEntityBody(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupUpdateWithEmptyEntityBody();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = "";
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithMalformedXml(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupUpdateWithMalformedXml();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupUpdateWithWrongXmlSchema();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
     */

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        LoanoutClient client = new LoanoutClient();
        PoxPayloadOut multipart = createLoanoutInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
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
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        LoanoutClient client = new LoanoutClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
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
        LoanoutClient client = new LoanoutClient();
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
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return LoanoutClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	return createLoanoutInstance(identifier);
    }
    
    /**
     * Creates the loanout instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createLoanoutInstance(String identifier) {
        return createLoanoutInstance(
                "loanoutNumber-" + identifier,
                CURRENT_DATE_UTC);
    }

    /**
     * Creates the loanout instance.
     *
     * @param loanOutNumber the loan out number
     * @param returnDate the return date
     * @return the multipart output
     */
    private PoxPayloadOut createLoanoutInstance(String loanOutNumber,
            String returnDate) {
        LoansoutCommon loanoutCommon = new LoansoutCommon();
        loanoutCommon.setLoanOutNumber(loanOutNumber);
        loanoutCommon.setLoanReturnDate(returnDate);
        loanoutCommon.setBorrower(
                "urn:cspace:org.collectionspace.demo:orgauthorities:name(TestOrgAuth):item:name(NorthernClimesMuseum)'Northern Climes Museum'");
        loanoutCommon.setBorrowersContact(
                "urn:cspace:org.collectionspace.demo:personauthorities:name(TestPersonAuth):item:name(ChrisContact)'Chris Contact'");
        loanoutCommon.setLoanPurpose("Allow people in cold climes to share the magic of Surfboards of the 1960s.");
        LoanStatusGroupList statusGroupList = new LoanStatusGroupList();
        List<LoanStatusGroup> statusGroups = statusGroupList.getLoanStatusGroup();
        LoanStatusGroup statusGroup = new LoanStatusGroup();
        statusGroup.setLoanStatus("returned");
        statusGroup.setLoanStatusNote("Left under the front mat.");
        statusGroups.add(statusGroup);
        loanoutCommon.setLoanStatusGroupList(statusGroupList);
        loanoutCommon.setLoanOutNote(getUTF8DataFragment());  // For UTF-8 tests

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(new LoanoutClient().getCommonPartName(), loanoutCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, loanout common");
            logger.debug(objectAsXmlString(loanoutCommon, LoansoutCommon.class));
            // logger.debug(multipart.toXML());
        }

        return multipart;
    }

    @Override
    protected String getServiceName() {
        return LoanoutClient.SERVICE_NAME;
    }

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        PoxPayloadOut result = createLoanoutInstance(identifier);
        return result;
	}

	@Override
	protected LoansoutCommon updateInstance(LoansoutCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(LoansoutCommon original,
			LoansoutCommon updated) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
