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

import org.dom4j.Element;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractCommonListUtils;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.intake.EntryMethodList;
import org.collectionspace.services.intake.FieldCollectionEventNameList;
import org.collectionspace.services.intake.CurrentLocationGroup;
import org.collectionspace.services.intake.CurrentLocationGroupList;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
/**
 * IntakeServiceTest, carries out tests against a
 * deployed and running Intake Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class IntakeServiceTest extends AbstractServiceTestImpl {

    /** The logger. */
    private final String CLASS_NAME = IntakeServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(IntakeServiceTest.class);
    /** The known resource id. */
    private String knownResourceId = null;
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new IntakeClient();
    }

    @Override
    protected String getServiceName() {
        return IntakeClient.SERVICE_NAME;
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
    // ---------------------------------------------------------------
    
    // Success outcomes
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        IntakeClient client = new IntakeClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createInstance(identifier);
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
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
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
    setupCreateWithWrongXmlSchema(testName, logger);
    
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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        IntakeClient client = new IntakeClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);

        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        IntakesCommon intakeCommons = null;
        if (payloadInputPart != null) {
            intakeCommons = (IntakesCommon) payloadInputPart.getBody();
        }
//        IntakesCommon intake = (IntakesCommon) extractPart(input,
//                client.getCommonPartName(), IntakesCommon.class);
        Assert.assertNotNull(intakeCommons);

        // Verify the number and contents of values in repeatable fields,
        // as created in the instance record used for testing.
        List<String> entryMethods =
                intakeCommons.getEntryMethods().getEntryMethod();
        Assert.assertTrue(entryMethods.size() > 0);
        Assert.assertNotNull(entryMethods.get(0));

        List<String> fieldCollectionEventNames =
                intakeCommons.getFieldCollectionEventNames().getFieldCollectionEventName();
        Assert.assertTrue(fieldCollectionEventNames.size() > 0);
        Assert.assertNotNull(fieldCollectionEventNames.get(0));

        CurrentLocationGroupList currentLocationGroupList = intakeCommons.getCurrentLocationGroupList();
        Assert.assertNotNull(currentLocationGroupList);
        List<CurrentLocationGroup> currentLocationGroups = currentLocationGroupList.getCurrentLocationGroup();
        Assert.assertNotNull(currentLocationGroups);
        Assert.assertTrue(currentLocationGroups.size() > 0);
        CurrentLocationGroup currentLocationGroup = currentLocationGroups.get(0);
        Assert.assertNotNull(currentLocationGroup);
        Assert.assertNotNull(currentLocationGroup.getCurrentLocationNote());

        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + intakeCommons.getEntryNote());
        }
        Assert.assertEquals(intakeCommons.getEntryNote(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + intakeCommons.getEntryNote()
                + "' does not match expected data '" + getUTF8DataFragment());
    }

    // Failure outcomes
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        IntakeClient client = new IntakeClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    
    // Success outcomes
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        IntakeClient client = new IntakeClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        assertStatusCode(res, testName);
        AbstractCommonList list = res.getEntity();

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        IntakeClient client = new IntakeClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);

        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        IntakesCommon intakeCommons = null;
        if (payloadInputPart != null) {
            intakeCommons = (IntakesCommon) payloadInputPart.getBody();
        }
//        IntakesCommon intake = (IntakesCommon) extractPart(input,
//                client.getCommonPartName(), IntakesCommon.class);
        Assert.assertNotNull(intakeCommons);

        // Update the content of this resource.
        intakeCommons.setEntryNumber("updated-" + intakeCommons.getEntryNumber());
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(intakeCommons, IntakesCommon.class));
        }

        CurrentLocationGroupList currentLocationGroupList = intakeCommons.getCurrentLocationGroupList();
        Assert.assertNotNull(currentLocationGroupList);
        List<CurrentLocationGroup> currentLocationGroups = currentLocationGroupList.getCurrentLocationGroup();
        Assert.assertNotNull(currentLocationGroups);
        Assert.assertTrue(currentLocationGroups.size() > 0);
        CurrentLocationGroup currentLocationGroup = currentLocationGroups.get(0);
        Assert.assertNotNull(currentLocationGroup);
        String currentLocationNote = currentLocationGroup.getCurrentLocationNote();
        Assert.assertNotNull(currentLocationNote);
        String updatedCurrentLocationNote = "updated-" + currentLocationNote;
        currentLocationGroups.get(0).setCurrentLocationNote(updatedCurrentLocationNote);
        intakeCommons.setCurrentLocationGroupList(currentLocationGroupList);

        // Create an output payload to send to the service, and add teh common part
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(intakeCommons, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        
        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);
        
        input = new PoxPayloadIn(res.getEntity());
        IntakesCommon updatedIntake =
                (IntakesCommon) extractPart(input,
                client.getCommonPartName(), IntakesCommon.class);

        Assert.assertNotNull(updatedIntake);

        Assert.assertEquals(updatedIntake.getEntryNumber(),
                intakeCommons.getEntryNumber(),
                "Data in updated object did not match submitted data.");

        currentLocationGroupList = updatedIntake.getCurrentLocationGroupList();
        Assert.assertNotNull(currentLocationGroupList);
        currentLocationGroups = currentLocationGroupList.getCurrentLocationGroup();
        Assert.assertNotNull(currentLocationGroups);
        Assert.assertTrue(currentLocationGroups.size() > 0);
        Assert.assertNotNull(currentLocationGroups.get(0));
        Assert.assertEquals(updatedCurrentLocationNote,
                currentLocationGroups.get(0).getCurrentLocationNote(),
                "Data in updated object did not match submitted data.");

        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + intakeCommons.getEntryNote() + "\n"
                    + "UTF-8 data received=" + updatedIntake.getEntryNote());
        }
        Assert.assertTrue(updatedIntake.getEntryNote().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedIntake.getEntryNote()
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedIntake.getEntryNote(),
                intakeCommons.getEntryNote(),
                "Data in updated object did not match submitted data.");

    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update"})
    public void verifyReadOnlyCoreFields(String testName) throws Exception {
        // TODO These should be in some core client utils
        final String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core";
        final String COLLECTIONSPACE_CORE_TENANTID = "tenantId";
        final String COLLECTIONSPACE_CORE_URI = "uri";
        final String COLLECTIONSPACE_CORE_CREATED_AT = "createdAt";
        final String COLLECTIONSPACE_CORE_UPDATED_AT = "updatedAt";
        final String COLLECTIONSPACE_CORE_CREATED_BY = "createdBy";
        final String COLLECTIONSPACE_CORE_UPDATED_BY = "updatedBy";

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        IntakeClient client = new IntakeClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);

        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(COLLECTIONSPACE_CORE_SCHEMA);
        Element coreAsElement = null;
        if (payloadInputPart != null) {
        	coreAsElement = payloadInputPart.getElementBody();
        }
        Assert.assertNotNull(coreAsElement);
        if (logger.isDebugEnabled()) {
            logger.debug("Core part before update:");
            logger.debug(coreAsElement.asXML());
        }

        // Update the read-only elements
        Element tenantId = coreAsElement.element(COLLECTIONSPACE_CORE_TENANTID);
        String originalTenantId = tenantId.getText();
        tenantId.setText("foo");
        Element uri = coreAsElement.element(COLLECTIONSPACE_CORE_URI);
        String originalUri = uri.getText();
        uri.setText("foo");
        Element createdAt = coreAsElement.element(COLLECTIONSPACE_CORE_CREATED_AT);
        String originalCreatedAt = createdAt.getText();
        String now = GregorianCalendarDateTimeUtils.timestampUTC();
        if(originalCreatedAt.equalsIgnoreCase(now) && logger.isWarnEnabled()) {
        		logger.warn("Cannot check createdAt read-only; too fast!");
        }
        createdAt.setText(now);
        Element createdBy = coreAsElement.element(COLLECTIONSPACE_CORE_CREATED_BY);
        String originalCreatedBy = createdBy.getText();
        createdBy.setText("foo");
        
        if (logger.isDebugEnabled()) {
            logger.debug("Core part to be updated:");
            logger.debug(coreAsElement.asXML());
        }

        // Create an output payload to send to the service, and add the common part
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart corePart = output.addPart(
        					COLLECTIONSPACE_CORE_SCHEMA, coreAsElement);
        
        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);

        input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart updatedCorePart = input.getPart(COLLECTIONSPACE_CORE_SCHEMA);
        Element updatedCoreAsElement = null;
        if (updatedCorePart != null) {
        	updatedCoreAsElement = updatedCorePart.getElementBody();
        }
        Assert.assertNotNull(updatedCoreAsElement);
        
        tenantId = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_TENANTID);
        String updatedTenantId = tenantId.getText();
        Assert.assertEquals(updatedTenantId, originalTenantId,
        			"CORE part TenantID was able to update!");
        uri = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_URI);
        String updatedUri = uri.getText();
        Assert.assertEquals(updatedUri, originalUri,
        			"CORE part URI was able to update!");
        createdAt = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_CREATED_AT);
        String updatedCreatedAt = createdAt.getText();
        Assert.assertEquals(updatedCreatedAt, originalCreatedAt,
        			"CORE part CreatedAt was able to update!");
        createdBy = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_CREATED_BY);
        String updatedCreatedBy = createdBy.getText();
        Assert.assertEquals(updatedCreatedBy, originalCreatedBy,
        			"CORE part CreatedBy was able to update!");

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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        IntakeClient client = new IntakeClient();
        PoxPayloadOut multipart = createInstance(NON_EXISTENT_ID);
        ClientResponse<String> res =
                client.update(NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    
    // Success outcomes
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update", "verifyReadOnlyCoreFields"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        IntakeClient client = new IntakeClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // Failure outcomes
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        IntakeClient client = new IntakeClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    @Test(dependsOnMethods = {"create", "read"})
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
        return IntakeClient.SERVICE_PATH_COMPONENT;
    }

    /**
     * Creates the intake instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
        return createIntakeInstance(
                "entryNumber-" + identifier,
                CURRENT_DATE_UTC,
                "depositor-" + identifier);
    }

    /**
     * Creates the intake instance.
     *
     * @param entryNumber the entry number
     * @param entryDate the entry date
     * @param depositor the depositor
     * @return the multipart output
     */
    private PoxPayloadOut createIntakeInstance(String entryNumber,
            String entryDate,
            String depositor) {
        IntakesCommon intake = new IntakesCommon();
        intake.setEntryNumber(entryNumber);
        intake.setEntryDate(entryDate);
        intake.setDepositor(depositor);

        EntryMethodList entryMethodsList = new EntryMethodList();
        List<String> entryMethods = entryMethodsList.getEntryMethod();
        entryMethods.add("Left at doorstep");
        entryMethods.add("Received via post");
        intake.setEntryMethods(entryMethodsList);

        FieldCollectionEventNameList eventNamesList = new FieldCollectionEventNameList();
        List<String> eventNames = eventNamesList.getFieldCollectionEventName();
        // FIXME Use properly formatted refNames for representative event names
        // in this example test record. The following are mere placeholders.
        eventNames.add("Field Collection Event Name-1");
        eventNames.add("Field Collection Event Name-2");
        intake.setFieldCollectionEventNames(eventNamesList);

        CurrentLocationGroupList currentLocationGroupList = new CurrentLocationGroupList();
        List<CurrentLocationGroup> currentLocationGroups = currentLocationGroupList.getCurrentLocationGroup();
        CurrentLocationGroup currentLocationGroup = new CurrentLocationGroup();
        currentLocationGroup.setCurrentLocation("upstairs");
        currentLocationGroup.setCurrentLocationFitness("suitable");
        currentLocationGroup.setCurrentLocationNote("A most suitable location.");
        currentLocationGroups.add(currentLocationGroup);
        intake.setCurrentLocationGroupList(currentLocationGroupList);

        intake.setEntryNote(getUTF8DataFragment());

        PoxPayloadOut multipart = new PoxPayloadOut(IntakeClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart =
                multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new IntakeClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, intake common");
            logger.debug(objectAsXmlString(intake, IntakesCommon.class));
        }

        return multipart;
    }
}
