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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.ClaimClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.claim.ClaimsCommon;
import org.collectionspace.services.claim.ResponsibleDepartmentsList;
import org.collectionspace.services.claim.ClaimClaimantGroupList;
import org.collectionspace.services.claim.ClaimClaimantGroup;
import org.collectionspace.services.claim.ClaimReceivedGroupList;
import org.collectionspace.services.claim.ClaimReceivedGroup;

import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClaimServiceTest, carries out tests against a
 * deployed and running Claim Service.
 *
 * $LastChangedRevision: 5952 $
 * $LastChangedDate: 2011-11-14 23:26:36 -0800 (Mon, 14 Nov 2011) $
 */
public class ClaimServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ClaimsCommon> {

   /** The logger. */
    private final String CLASS_NAME = ClaimServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    final String SERVICE_NAME = "claims";
    final String SERVICE_PATH_COMPONENT = "claims";

    // Instance variables specific to this test.
    private String knownResourceId = null;
    private final static String TIMESTAMP_UTC =
            GregorianCalendarDateTimeUtils.timestampUTC();
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new ClaimClient();
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        ClaimClient client = new ClaimClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createClaimInstance(identifier);
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        for(int i = 0; i < 3; i++){
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
        String method = testRequestType.httpMethodName();
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
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
        String method = testRequestType.httpMethodName();
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
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
        String method = testRequestType.httpMethodName();
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        ClaimClient client = new ClaimClient();
        ClientResponse<String> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Get the common part of the response and verify that it is not null.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ClaimsCommon claimCommon = null;
        if (payloadInputPart != null) {
        	claimCommon = (ClaimsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(claimCommon);

        // Check selected fields.

        // Check the values of one or more date/time fields.
        String receivedDate = claimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).getClaimReceivedDate();

        if (logger.isDebugEnabled()) {
            logger.debug("receivedDate=" + receivedDate);
            logger.debug("TIMESTAMP_UTC=" + TIMESTAMP_UTC);
        }
        Assert.assertTrue(receivedDate.equals(TIMESTAMP_UTC));
        
        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        String claimNote = claimCommon.getClaimClaimantGroupList().getClaimClaimantGroup().get(0).getClaimantNote();
        
        if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + claimNote);
        }
        Assert.assertEquals(claimNote, getUTF8DataFragment(),
                "UTF-8 data retrieved '" + claimNote
                + "' does not match expected data '" + getUTF8DataFragment());

    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        ClaimClient client = new ClaimClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        ClaimClient client = new ClaimClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Optionally output additional data about list members for debugging.
        if(logger.isTraceEnabled()){
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        ClaimClient client = new ClaimClient();
        ClientResponse<String> res = client.read(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);
        if(logger.isDebugEnabled()){
            logger.debug("got object to update with ID: " + knownResourceId);
        }

        // Extract the common part from the response.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ClaimsCommon claimCommon = null;
        if (payloadInputPart != null) {
            claimCommon = (ClaimsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(claimCommon);

        // Update its content.
        claimCommon.setClaimNumber(""); // Test deletion of existing string value

        String claimNote = claimCommon.getClaimClaimantGroupList().getClaimClaimantGroup().get(0).getClaimantNote();
        claimCommon.getClaimClaimantGroupList().getClaimClaimantGroup().get(0).setClaimantNote("updated claim note-" + claimNote);

        claimCommon.getResponsibleDepartments().getResponsibleDepartment().remove(0); // Test removing a value from a list

        String currentTimestamp = GregorianCalendarDateTimeUtils.timestampUTC();
        claimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).setClaimReceivedDate(currentTimestamp);
        claimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).setClaimReceivedNote(""); 

        if(logger.isDebugEnabled()){
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(claimCommon, ClaimsCommon.class));
        }

        // Submit the request to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(claimCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);

        // Check the status code of the response: does it match the expected response(s)?
        int statusCode = res.getStatus();
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

       // Extract the updated common part from the response.
        input = new PoxPayloadIn(res.getEntity());
        payloadInputPart = input.getPart(client.getCommonPartName());
        ClaimsCommon updatedClaimCommon = null;
        if (payloadInputPart != null) {
            updatedClaimCommon = (ClaimsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(claimCommon);
        if(logger.isDebugEnabled()){
            logger.debug("updated object");
            logger.debug(objectAsXmlString(updatedClaimCommon, ClaimsCommon.class));
        }

        // Check selected fields in the updated common part.
        // By submitting an empty string in the update payload, the value of this field
        // in the object created from the response payload will be null.
        Assert.assertNull(updatedClaimCommon.getClaimNumber(), "Data in updated object did not match submitted data.");
        if(logger.isDebugEnabled()){
            logger.debug("Received claim number after update=|" + updatedClaimCommon.getClaimNumber() + "|");
        }

		String originalClaimNote = claimCommon.getClaimClaimantGroupList().getClaimClaimantGroup().get(0).getClaimantNote();
        String updatedClaimNote = updatedClaimCommon.getClaimClaimantGroupList().getClaimClaimantGroup().get(0).getClaimantNote();

        Assert.assertEquals(updatedClaimNote,
            originalClaimNote,
            "Data in updated object did not match submitted data.");

        List<String> updatedResponsibleDepartments = updatedClaimCommon.getResponsibleDepartments().getResponsibleDepartment();
        Assert.assertEquals(1,
            updatedResponsibleDepartments.size(),
            "Data in updated object did not match submitted data.");

        Assert.assertEquals(updatedResponsibleDepartments.get(0),
            claimCommon.getResponsibleDepartments().getResponsibleDepartment().get(0),
            "Data in updated object did not match submitted data.");

        Assert.assertEquals(updatedClaimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).getClaimReceivedDate(),
            currentTimestamp,
            "Data in updated object did not match submitted data.");

        if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + originalClaimNote + "\n"
                    + "UTF-8 data received=" + updatedClaimNote);
        }
        Assert.assertTrue(updatedClaimNote.contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedClaimNote
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedClaimNote,
                originalClaimNote,
                "Data in updated object did not match submitted data.");
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception{
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
        String method = testRequestType.httpMethodName();
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
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
        String method = testRequestType.httpMethodName();
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
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
        String method = testRequestType.httpMethodName();
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
    }
     */

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        ClaimClient client = new ClaimClient();
        PoxPayloadOut multipart = createClaimInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        /*
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ClaimClient client = new ClaimClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
     *
     */
    }


    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        ClaimClient client = new ClaimClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
    //@Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    /**
     * Creates the claim instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createClaimInstance(String identifier) {
        return createInstance("claimNumber-" + identifier);
    }

    /**
     * Creates an instance of a Claim record for testing.
     *
     * @param claimNumber A claim number.
     * @return Multipart output suitable for use as a payload
     *     in a create or update request.
     */
    @Override
    public PoxPayloadOut createInstance(String claimNumber) {
        ClaimsCommon claimCommon = new ClaimsCommon();

        ResponsibleDepartmentsList responsibleDepartmentsList = new ResponsibleDepartmentsList();
        List<String> responsibleDepartments = responsibleDepartmentsList.getResponsibleDepartment();
        String identifier = createIdentifier();
        responsibleDepartments.add("First Responsible Department-" + identifier);
        responsibleDepartments.add("Second Responsible Department-" + identifier);

        ClaimClaimantGroupList claimClaimantGroupList = new ClaimClaimantGroupList();
        ClaimClaimantGroup claimClaimantGroup = new ClaimClaimantGroup();
        claimClaimantGroup.setFiledBy("urn:cspace:core.collectionspace.org:personauthorities:name(TestPersonAuth):item:name(carrieClaimFiler)'Carrie ClaimFiler'");
        claimClaimantGroup.setFiledOnBehalfOf("urn:cspace:core.collectionspace.org:personauthorities:name(TestPersonAuth):item:name(benBehalfOf)'Ben BehalfOf'");
        claimClaimantGroup.setClaimantNote(getUTF8DataFragment());
        claimClaimantGroupList.getClaimClaimantGroup().add(claimClaimantGroup);

        ClaimReceivedGroupList claimReceivedGroupList = new ClaimReceivedGroupList();
        ClaimReceivedGroup claimReceivedGroup = new ClaimReceivedGroup();
        claimReceivedGroup.setClaimReceivedDate(TIMESTAMP_UTC);
        claimReceivedGroup.setClaimReceivedNote(getUTF8DataFragment());
        claimReceivedGroupList.getClaimReceivedGroup().add(claimReceivedGroup);

        claimCommon.setResponsibleDepartments(responsibleDepartmentsList);
        claimCommon.setClaimClaimantGroupList(claimClaimantGroupList);
        claimCommon.setClaimReceivedGroupList(claimReceivedGroupList);
        claimCommon.setClaimNumber(claimNumber);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(claimCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ClaimClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, claim common");
            logger.debug(objectAsXmlString(claimCommon, ClaimsCommon.class));
        }

        return multipart;
    }

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        PoxPayloadOut result = createClaimInstance(identifier);
        return result;
	}

	@Override
	protected ClaimsCommon updateInstance(ClaimsCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(ClaimsCommon original,
			ClaimsCommon updated) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
