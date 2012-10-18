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
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.ConditioncheckClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.conditioncheck.ConditionchecksCommon;
import org.collectionspace.services.conditioncheck.ConditionCheckersList;
import org.collectionspace.services.conditioncheck.HazardGroupList;
import org.collectionspace.services.conditioncheck.HazardGroup;

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
 * ConditioncheckServiceTest, carries out tests against a
 * deployed and running Conditioncheck Service.
 *
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class ConditioncheckServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ConditionchecksCommon> {

   /** The logger. */
    private final String CLASS_NAME = ConditioncheckServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    final String SERVICE_NAME = "conditionchecks";
    final String SERVICE_PATH_COMPONENT = "conditionchecks";
    
    // Instance variables specific to this test.
    private String knownResourceId = null;
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.timestampUTC();
    private String CONDITIONCHECKER_REF_NAME = 
            "urn:cspace:org.collectionspace.demo:personauthorities:name(TestPersonAuth):item:name(JimChecker)'Jim Checker'";
    private String CONDITIONCHECK_HAZARD = "urn:cspace:core.collectionspace.org:vocabularies:name(TestTermList):item:name(hazard1)'multi-dimensional'";
    private String CONDITIONCHECK_HAZARD_NOTE = "Object needs to be extinguished occasionally.";
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new ConditioncheckClient();
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
        ConditioncheckClient client = new ConditioncheckClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createConditioncheckInstance(identifier);
        String newID = null;
        ClientResponse<Response> res = client.create(multipart);

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
                res.releaseConnection();
            }
        }

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
    /*@Override
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        for(int i = 0; i < 3; i++){
            create(testName);
        }
    }*/

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    /*@Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty?
    }*/

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    /*@Override
    public void createWithMalformedXml(String testName) throws Exception {
        //Should this really be empty?
    }*/

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    /*@Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty?
    }*/

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
        ConditioncheckClient client = new ConditioncheckClient();
        ClientResponse<String> res = client.read(knownResourceId);

        PoxPayloadIn input = null;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.getEntity());
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }

        // Get the common part of the response and verify that it is not null.
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ConditionchecksCommon conditioncheckCommon = null;
        if (payloadInputPart != null) {
            conditioncheckCommon = (ConditionchecksCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(conditioncheckCommon);

        if(logger.isDebugEnabled()){
            logger.debug("read, conditioncheck common");
            logger.debug(objectAsXmlString(conditioncheckCommon, ConditionchecksCommon.class));
        }

        // Check selected fields.
        // Repeatable group
        HazardGroupList hazardGroupList = conditioncheckCommon.getHazardGroupList();
        Assert.assertNotNull(hazardGroupList);
        List<HazardGroup> hazardGroups = hazardGroupList.getHazardGroup();
        Assert.assertNotNull(hazardGroups);
        String hazard = hazardGroups.get(0).getHazard();
        Assert.assertEquals(hazard, CONDITIONCHECK_HAZARD);
        String hazardNote = hazardGroups.get(0).getHazardNote();
        Assert.assertEquals(hazardNote, CONDITIONCHECK_HAZARD_NOTE);
        String hazardDate = hazardGroups.get(0).getHazardDate();
        Assert.assertEquals(hazardDate, CURRENT_DATE_UTC);

        // Repeatable field
        List<String> conditionCheckers =
                conditioncheckCommon.getConditionCheckers().getConditionChecker();
        Assert.assertTrue(conditionCheckers.size() > 0);
        Assert.assertEquals(conditionCheckers.get(0), CONDITIONCHECKER_REF_NAME);

        
        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        String conditionCheckNote = conditioncheckCommon.getConditionCheckNote();
        
        if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + conditionCheckNote);
        }
        Assert.assertEquals(conditionCheckNote, getUTF8DataFragment(),
                "UTF-8 data retrieved '" + conditionCheckNote
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
        ConditioncheckClient client = new ConditioncheckClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);

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
                res.releaseConnection();
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AbstractCommonList list = null;
        ConditioncheckClient client = new ConditioncheckClient();
        ClientResponse<AbstractCommonList> res = client.readList();
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

            list = res.getEntity();
        } finally {
            if (res != null) {
                res.releaseConnection();
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        ConditioncheckClient client = new ConditioncheckClient();
        ClientResponse<String> res = client.read(knownResourceId);
        PoxPayloadIn input = null;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.getEntity());
            if (logger.isDebugEnabled()) {
                logger.debug("got object to update with ID: " + knownResourceId);
            }
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }

        // Extract the common part from the response.
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ConditionchecksCommon conditioncheckCommon = null;
        if (payloadInputPart != null) {
            conditioncheckCommon = (ConditionchecksCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(conditioncheckCommon);

        // Update the content of this resource.
        conditioncheckCommon.setConditionCheckRefNumber("updated-" + conditioncheckCommon.getConditionCheckRefNumber());
        conditioncheckCommon.setConditionCheckNote("updated-conditionCheckNote");

        if(logger.isDebugEnabled()){
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(conditioncheckCommon, ConditionchecksCommon.class));
        }

        // Submit the updated common part in an update request to the service
        // and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), conditioncheckCommon);
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
            input = new PoxPayloadIn(res.getEntity());
        } finally {
            if (res != null) {
                res.releaseConnection();
            }
        }

        // Extract the updated common part from the response.
        payloadInputPart = input.getPart(client.getCommonPartName());
        ConditionchecksCommon updatedConditioncheckCommon = null;
        if (payloadInputPart != null) {
            updatedConditioncheckCommon = (ConditionchecksCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(updatedConditioncheckCommon);

        // Check selected fields in the updated common part.
        Assert.assertEquals(updatedConditioncheckCommon.getConditionCheckRefNumber(), conditioncheckCommon.getConditionCheckRefNumber(),
                "Data in updated object did not match submitted data.");

        Assert.assertEquals(conditioncheckCommon.getConditionCheckNote(), updatedConditioncheckCommon.getConditionCheckNote(),
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
        ConditioncheckClient client = new ConditioncheckClient();
        PoxPayloadOut multipart = createConditioncheckInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
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
                res.releaseConnection();
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
    //@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    //    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ConditioncheckClient client = new ConditioncheckClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
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
                res.releaseConnection();
            }
        }
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
        ConditioncheckClient client = new ConditioncheckClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
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
                res.releaseConnection();
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
     * Creates the conditioncheck instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createConditioncheckInstance(String identifier) {
        return createInstance("conditionCheckRefNumber-" + identifier);
    }

    /**
     * Creates an instance of a Conditioncheck record for testing.
     *
     * @param conditionCheckRefNumber A conditioncheck number.
     * @return Multipart output suitable for use as a payload
     *     in a create or update request.
     */
    @Override
    public PoxPayloadOut createInstance(String conditionCheckRefNumber) {
        ConditionchecksCommon conditioncheckCommon = new ConditionchecksCommon();
        conditioncheckCommon.setConditionCheckRefNumber(conditionCheckRefNumber);

        ConditionCheckersList conditionCheckersList = new ConditionCheckersList();
        List<String> conditionCheckers = conditionCheckersList.getConditionChecker();
        conditionCheckers.add(CONDITIONCHECKER_REF_NAME);
        conditioncheckCommon.setConditionCheckers(conditionCheckersList);

        conditioncheckCommon.setConditionCheckNote(getUTF8DataFragment());

        HazardGroupList hazardGroupList = new HazardGroupList();
        List<HazardGroup> hazardGroups = hazardGroupList.getHazardGroup();
        HazardGroup hazardGroup = new HazardGroup();
        hazardGroup.setHazard(CONDITIONCHECK_HAZARD);
        hazardGroup.setHazardDate(CURRENT_DATE_UTC);
        hazardGroup.setHazardNote(CONDITIONCHECK_HAZARD_NOTE);
        hazardGroups.add(hazardGroup);
        conditioncheckCommon.setHazardGroupList(hazardGroupList);
        
        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(new ConditioncheckClient().getCommonPartName(), conditioncheckCommon);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, conditioncheck common");
            logger.debug(objectAsXmlString(conditioncheckCommon, ConditionchecksCommon.class));
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
        PoxPayloadOut result = createConditioncheckInstance(identifier);
        return result;
    }

    @Override
    protected ConditionchecksCommon updateInstance(ConditionchecksCommon commonPartObject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void compareUpdatedInstances(ConditionchecksCommon original,
            ConditionchecksCommon updated) throws Exception {
        // TODO Auto-generated method stub
        
    }
}