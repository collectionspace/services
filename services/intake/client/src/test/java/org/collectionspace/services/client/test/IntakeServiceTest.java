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

import org.dom4j.Element;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
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
public class IntakeServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, IntakesCommon> {

    /** The logger. */
    private final String CLASS_NAME = IntakeServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(IntakeServiceTest.class);
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

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    
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
    
	protected void compareReadInstances(IntakesCommon original, IntakesCommon fromRead) throws Exception {
        // Verify the number and contents of values in repeatable fields,
        // as created in the instance record used for testing.
        List<String> entryMethods =
        		fromRead.getEntryMethods().getEntryMethod();
        Assert.assertTrue(entryMethods.size() > 0);
        Assert.assertNotNull(entryMethods.get(0));

        List<String> fieldCollectionEventNames =
        		fromRead.getFieldCollectionEventNames().getFieldCollectionEventName();
        Assert.assertTrue(fieldCollectionEventNames.size() > 0);
        Assert.assertNotNull(fieldCollectionEventNames.get(0));

        CurrentLocationGroupList currentLocationGroupList = fromRead.getCurrentLocationGroupList();
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
                    + "UTF-8 data received=" + fromRead.getEntryNote());
        }
        Assert.assertEquals(fromRead.getEntryNote(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + fromRead.getEntryNote()
                + "' does not match expected data '" + getUTF8DataFragment());
	}
    
    // Failure outcomes

    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing because this test is not ready to delete the "knownResourceId".
    	// Instead, the method localDelete() will get called later in the dependency chain. The
    	// method localDelete() has a dependency on the test "verifyReadOnlyCoreFields".  Once the "verifyReadOnlyCoreFields"
    	// test is run, the localDelete() test/method will get run.  The localDelete() test/method in turn
    	// calls the inherited delete() test/method.
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests", "verifyReadOnlyCoreFields"})
    public void localDelete(String testName) throws Exception {
    	// Because of issues with TestNG not allowing @Test annotations on on override methods,
    	// and because we want the "updateWrongUser" to run before the "delete" test, we need
    	// this method.  This method will call super.delete() after all the dependencies have been
    	// met.
    	super.delete(testName);
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void verifyReadOnlyCoreFields(String testName) throws Exception {
        // TODO These should be in some core client utils
        final String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core";
        final String COLLECTIONSPACE_CORE_TENANTID = "tenantId";
        final String COLLECTIONSPACE_CORE_URI = "uri";
        final String COLLECTIONSPACE_CORE_CREATED_AT = "createdAt";
        final String COLLECTIONSPACE_CORE_UPDATED_AT = "updatedAt";
        final String COLLECTIONSPACE_CORE_CREATED_BY = "createdBy";
        final String COLLECTIONSPACE_CORE_UPDATED_BY = "updatedBy";

        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        IntakeClient client = new IntakeClient();
        ClientResponse<String> res = client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

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
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

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

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
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

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		return this.createInstance(identifier);
	}

	@Override
	protected IntakesCommon updateInstance(IntakesCommon intakesCommon) {
		IntakesCommon result = new IntakesCommon();
		
		result.setEntryNumber("updated-" + intakesCommon.getEntryNumber());
		result.setEntryNote(intakesCommon.getEntryNote());

        CurrentLocationGroupList currentLocationGroupList = intakesCommon.getCurrentLocationGroupList();
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
        result.setCurrentLocationGroupList(currentLocationGroupList);
        
        return result;
	}

	@Override
	protected void compareUpdatedInstances(IntakesCommon original,
			IntakesCommon updated) throws Exception {
        Assert.assertEquals(updated.getEntryNumber(),
        		original.getEntryNumber(),
                "Data in updated object did not match submitted data.");
        
        CurrentLocationGroupList currentLocationGroupList = updated.getCurrentLocationGroupList();
        Assert.assertNotNull(currentLocationGroupList);
        List<CurrentLocationGroup> currentLocationGroups = currentLocationGroupList.getCurrentLocationGroup();
        Assert.assertNotNull(currentLocationGroups);
        Assert.assertTrue(currentLocationGroups.size() > 0);
        Assert.assertNotNull(currentLocationGroups.get(0));
        
        String updatedCurrentLocationNote = original.getCurrentLocationGroupList()
        		.getCurrentLocationGroup().get(0).getCurrentLocationNote();
        Assert.assertEquals(updatedCurrentLocationNote,
                currentLocationGroups.get(0).getCurrentLocationNote(),
                "Data in updated object did not match submitted data.");
        
        Assert.assertEquals(updated.getEntryNote(), original.getEntryNote(),
                "Data in updated object did not match submitted data.");
        //
        // UTF-8 Checks
        //
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + original.getEntryNote() + "\n"
                    + "UTF-8 data received=" + updated.getEntryNote());
        }
        Assert.assertTrue(updated.getEntryNote().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updated.getEntryNote()
                + "' does not contain expected data '" + getUTF8DataFragment());        
	}

    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */
    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub		
	}
}
