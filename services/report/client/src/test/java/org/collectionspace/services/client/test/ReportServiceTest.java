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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.common.AbstractCommonListUtils;
import org.collectionspace.services.report.ReportsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
//import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
 * ReportServiceTest, carries out tests against a
 * deployed and running Report Service.
 *
 * $LastChangedRevision: 2261 $
 * $LastChangedDate: 2010-05-28 16:52:22 -0700 (Fri, 28 May 2010) $
 */
public class ReportServiceTest extends AbstractServiceTestImpl {

    /** The logger. */
    private final String CLASS_NAME = ReportServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_NAME = "reports";
    final String SERVICE_PATH_COMPONENT = "reports";
    // Instance variables specific to this test.
    /** The known resource id. */
    private String knownResourceId = null;
    
    private String testDocType = "Acquisition";

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new ReportClient();
    }

//    @Override
//    protected PoxPayloadOut createInstance(String identifier) {
//        PoxPayloadOut multipart = createReportInstance(identifier);
//        return multipart;
//    }
        
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
        ReportClient client = new ReportClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createReportInstance(identifier);
        ClientResponse<Response> res = client.create(multipart);

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        int statusCode = res.getStatus();
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
        ReportClient client = new ReportClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);

        // Get the common part of the response and verify that it is not null.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ReportsCommon reportCommon = null;
        if (payloadInputPart != null) {
            reportCommon = (ReportsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(reportCommon);

        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + reportCommon.getNotes());
        }
        Assert.assertEquals(reportCommon.getNotes(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + reportCommon.getNotes()
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
        ReportClient client = new ReportClient();
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
        ReportClient client = new ReportClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        assertStatusCode(res, testName);
        AbstractCommonList list = res.getEntity();

        // Optionally output additional data about list members for debugging.
        if(logger.isTraceEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"readList"})
    public void readListFiltered(String testName) throws Exception {
    	if (logger.isDebugEnabled()) {
    		logger.debug(testBanner(testName, CLASS_NAME));
    	}
    	// Perform setup.
    	setupReadList();

    	// Submit the request to the service and store the response.
    	ReportClient client = new ReportClient();
    	ClientResponse<AbstractCommonList> res = client.readListFiltered(
    			testDocType, "single");
        assertStatusCode(res, testName);
    	AbstractCommonList list = res.getEntity();

    	List<AbstractCommonList.ListItem> items =
    		list.getListItem();
    	// We must find the basic one we created
    	boolean fFoundBaseItem = false;
		for (AbstractCommonList.ListItem item : items) {
			if(knownResourceId.equalsIgnoreCase(AbstractCommonListUtils.ListItemGetCSID(item))) {
				fFoundBaseItem = true;
				break;
			}
		}
		if(!fFoundBaseItem)
			Assert.fail("readListFiltered failed to return base item");
		
		// Now filter for something else, and ensure it is NOT returned
    	res = client.readListFiltered("Intake", "single");
        assertStatusCode(res, testName);
    	list = res.getEntity();

    	items = list.getListItem();
    	// We must NOT find the basic one we created
		for (AbstractCommonList.ListItem item : items) {
			Assert.assertNotSame(AbstractCommonListUtils.ListItemGetCSID(item), knownResourceId, 
				"readListFiltered(\"Intake\", \"single\") incorrectly returned base item");
		}
		
		// Now filter for something else, and ensure it is NOT returned
    	res = client.readListFiltered(testDocType, "group");
        assertStatusCode(res, testName);
    	list = res.getEntity();

    	items = list.getListItem();
    	// We must NOT find the basic one we created
		for (AbstractCommonList.ListItem item : items) {
			Assert.assertNotSame(AbstractCommonListUtils.ListItemGetCSID(item), knownResourceId, 
				"readListFiltered(\""+testDocType+"\", \"group\") incorrectly returned base item");
		}
    }

    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#update(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read", "readListFiltered"})
    public void update(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        ReportClient client = new ReportClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        if (logger.isDebugEnabled()) {
            logger.debug("got object to update with ID: " + knownResourceId);
        }

        // Extract the common part from the response.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        ReportsCommon reportCommon = null;
        if (payloadInputPart != null) {
            reportCommon = (ReportsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(reportCommon);

        // Update its content.
        reportCommon.setName("updated-" + reportCommon.getName());
        reportCommon.setOutputMIME("updated-" + reportCommon.getOutputMIME());
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(reportCommon, ReportsCommon.class));
        }
        reportCommon.setNotes("updated-" + reportCommon.getNotes());

        // Submit the updated common part in an update request to the service
        // and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(reportCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);

        // Extract the updated common part from the response.
        input = new PoxPayloadIn(res.getEntity());
        payloadInputPart = input.getPart(client.getCommonPartName());
        ReportsCommon updatedReportCommon = null;
        if (payloadInputPart != null) {
            updatedReportCommon = (ReportsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(updatedReportCommon);
        if (logger.isDebugEnabled()) {
            logger.debug("updated object");
            logger.debug(objectAsXmlString(updatedReportCommon, ReportsCommon.class));
        }

        // Check selected fields in the updated common part.
        Assert.assertEquals(updatedReportCommon.getName(),
                reportCommon.getName(),
                "Data in updated object did not match submitted data.");

        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + reportCommon.getNotes() + "\n"
                    + "UTF-8 data received=" + updatedReportCommon.getNotes());
        }
        Assert.assertTrue(updatedReportCommon.getNotes().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedReportCommon.getNotes()
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedReportCommon.getNotes(),
                reportCommon.getNotes(),
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
        ReportClient client = new ReportClient();
        PoxPayloadOut multipart = createReportInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
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
    dependsOnMethods = {"create", "readListFiltered", "testSubmitRequest", "update", "readWorkflow"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ReportClient client = new ReportClient();
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
        ReportClient client = new ReportClient();
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
     * Creates the report instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createReportInstance(String identifier) {
    	List<String> docTypes = new ArrayList<String>();
    	docTypes.add(testDocType);
        return createReportInstance(
                "Acquisition Summary", 
                docTypes, true, false, false, true,
                "acq_basic.jasper",
                "application/pdf");
    }

    /**
     * Creates the report instance.
     *
     * @param name the report name
     * @param filename the relative path to the report
     * @param outputMIME the MIME type we will return for this report
     * @return the multipart output
     */
    private PoxPayloadOut createReportInstance(String name,
    		List<String> forDocTypeList,
    		boolean supportsSingle, boolean supportsList, 
    		boolean supportsGroup, boolean supportsNoContext, 
            String filename,
            String outputMIME) {
        ReportsCommon reportCommon = new ReportsCommon();
        reportCommon.setName(name);
        ReportsCommon.ForDocTypes forDocTypes = new ReportsCommon.ForDocTypes(); 
        List<String> docTypeList = forDocTypes.getForDocType();
        docTypeList.addAll(forDocTypeList);
        reportCommon.setForDocTypes(forDocTypes);
        reportCommon.setSupportsSingleDoc(supportsSingle);
        reportCommon.setSupportsDocList(supportsList);
        reportCommon.setSupportsGroup(supportsGroup);
        reportCommon.setSupportsNoContext(supportsNoContext);
        reportCommon.setFilename(filename);
        reportCommon.setOutputMIME(outputMIME);
        reportCommon.setNotes(getUTF8DataFragment()); // For UTF-8 tests

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(reportCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ReportClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, report common");
            logger.debug(objectAsXmlString(reportCommon, ReportsCommon.class));
            logger.debug(multipart.toXML());
        }

        return multipart;
    }
}
