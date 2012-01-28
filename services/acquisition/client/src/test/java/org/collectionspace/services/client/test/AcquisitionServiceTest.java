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
import org.collectionspace.services.client.AcquisitionClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.collectionspace.services.acquisition.AcquisitionsCommon;
import org.collectionspace.services.acquisition.AcquisitionDateList;
import org.collectionspace.services.acquisition.AcquisitionSourceList;
import org.collectionspace.services.acquisition.OwnerList;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcquisitionServiceTest, carries out tests against a
 * deployed and running Acquisition Service.
 * 
 * $LastChangedRevision: 621 $
 * $LastChangedDate: 2009-09-02 16:49:01 -0700 (Wed, 02 Sep 2009) $
 */
public class AcquisitionServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, AcquisitionsCommon> {

    /** The logger. */
    private final String CLASS_NAME = AcquisitionServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.timestampUTC();

    @Override
    public String getServicePathComponent() {
        return AcquisitionClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return AcquisitionClient.SERVICE_NAME;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new AcquisitionClient();
    }
    
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        AcquisitionClient client = new AcquisitionClient();
        PoxPayloadOut multipart = createAcquisitionInstance(identifier);
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
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//       dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        for(int i = 0; i < 3; i++){
            create(testName);
        }
    }

    /*
    * Tests to diagnose and fix CSPACE-2578.
    *
    * This is a bug identified in release 1.0 alpha, after first implementing an
    * Acquisition Funding repeatable group of fields, in which record creation
    * fails if there is whitespace (or more generally, a text node) between
    * the acquisitionFunding container element and its first child field.
    */

    // Verify that record creation occurs successfully when there is NO whitespace
    // between the acquisitionFunding tag and its first child element tag
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"CRUDTests"}, groups = {"cspace2578group"})
    public void createFromXmlNoWhitespaceAfterRepeatableGroupTag(String testName) throws Exception {
        String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            createFromXmlFile(testName, testDataDir + "/cspace-2578-no-whitespace.xml", false);
        testSubmitRequest(newId);
    }

    // Verify that record creation occurs successfully when there is whitespace
    // between the acquisitionFunding tag and its first child element tag

    // FIXME: This test currently fails.  @Test annotation is currently commented
    // out for check-in, to prevent service tests from failing.  Can uncomment to test
    // fixes, and also after the issue is resolved, to help detect any regressions.

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"CRUDTests"}, groups = {"cspace2578group"})
    public void createFromXmlWhitespaceAfterRepeatableGroupTag(String testName) throws Exception {
        String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            createFromXmlFile(testName, testDataDir + "/cspace-2578-whitespace.xml", false);
        AcquisitionsCommon acquisition = readAcquisitionCommonPart(newId);
        testSubmitRequest(newId);
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
    public void createWithMalformedXml(String testName) throws Exception {
    
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        };
            
        // Perform setup.
        setupCreateWithMalformedXml();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getServiceRootURL();
        final String entity = MALFORMED_XML_DATA; // Constant from base class.
        int statusCode = submitRequest(method, url, entity);

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
        };
        
        // Perform setup.
        setupCreateWithWrongXmlSchema();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getServiceRootURL();
        final String entity = WRONG_XML_SCHEMA_DATA;
        int statusCode = submitRequest(method, url, entity);

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
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        AcquisitionClient client = new AcquisitionClient();

        // Submit the request to the service and store the response.
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

        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        AcquisitionsCommon acquisitionObject = (AcquisitionsCommon) extractPart(input,
                client.getCommonPartName(), AcquisitionsCommon.class);
        Assert.assertNotNull(acquisitionObject);

        // Verify the number and contents of values in repeatable fields,
        // as created in the instance record used for testing.
        List<String> acqSources =
                acquisitionObject.getAcquisitionSources().getAcquisitionSource();
        Assert.assertTrue(acqSources.size() > 0);
        Assert.assertNotNull(acqSources.get(0));

        List<String> acqDates =
                acquisitionObject.getAcquisitionDates().getAcquisitionDate();
        Assert.assertTrue(acqDates.size() > 0);
        Assert.assertNotNull(acqDates.get(0));

        List<String> owners =
                acquisitionObject.getOwners().getOwner();
        Assert.assertTrue(owners.size() > 0);
        Assert.assertNotNull(owners.get(0));
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        AcquisitionClient client = new AcquisitionClient();
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
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AcquisitionClient client = new AcquisitionClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        try {
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        assertStatusCode(res, testName);
	        AbstractCommonList list = res.getEntity();
	
	        // Optionally output additional data about list members for debugging.
	        if (logger.isTraceEnabled() == true){
	        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
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
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        AcquisitionClient client = new AcquisitionClient();
        ClientResponse<String> res = client.read(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());

        AcquisitionsCommon acquisition = (AcquisitionsCommon) extractPart(input,
                client.getCommonPartName(), AcquisitionsCommon.class);
        Assert.assertNotNull(acquisition);

        // Update the content of this resource.
        acquisition.setAcquisitionReferenceNumber("updated-" + acquisition.getAcquisitionReferenceNumber());
        if(logger.isDebugEnabled()){
            logger.debug("updated object");
            logger.debug(objectAsXmlString(acquisition, AcquisitionsCommon.class));
        }
        // Submit the request to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(AcquisitionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(acquisition, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());

        res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        input = new PoxPayloadIn(res.getEntity());
        AcquisitionsCommon updatedAcquisition =
                (AcquisitionsCommon) extractPart(input,
                        client.getCommonPartName(), AcquisitionsCommon.class);
        Assert.assertNotNull(updatedAcquisition);

        Assert.assertEquals(updatedAcquisition.getAcquisitionReferenceNumber(),
                acquisition.getAcquisitionReferenceNumber(),
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
        };
            
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
            (testName + ": url=" + url + " status=" + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throws Exception {
    
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        };
        
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
        };
        
        // Perform setup.
        setupUpdateWithMalformedXml();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        final String entity = MALFORMED_XML_DATA;
        int statusCode = submitRequest(method, url, entity);

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
    @Test(dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema(String testName) {
    
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        };
        
        // Perform setup.
        setupUpdateWithWrongXmlSchema();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        final String entity = WRONG_XML_SCHEMA_DATA;
        int statusCode = submitRequest(method, url, entity);

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
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        AcquisitionClient client = new AcquisitionClient();
        PoxPayloadOut multipart = createAcquisitionInstance(NON_EXISTENT_ID);
        ClientResponse<String> res =
            client.update(NON_EXISTENT_ID, multipart);
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
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"create", "read", "update"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        AcquisitionClient client = new AcquisitionClient();
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
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
//        dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        AcquisitionClient client = new AcquisitionClient();
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

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	return createAcquisitionInstance(identifier);
    }
        
    /**
     * Creates the acquisition instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createAcquisitionInstance(String identifier) {
        AcquisitionsCommon acquisition = new AcquisitionsCommon();
        acquisition.setAcquisitionReferenceNumber("acquisitionReferenceNumber-"  + identifier);

        AcquisitionSourceList acqSourcesList = new AcquisitionSourceList();
        List<String> acqSources = acqSourcesList.getAcquisitionSource();
        // FIXME Use properly formatted refNames for representative acquisition
        // sources in this example test record. The following are mere placeholders.
        acqSources.add("Donor Acquisition Source-" + identifier);
        acqSources.add("Museum Acquisition Source-" + identifier);
        acquisition.setAcquisitionSources(acqSourcesList);

        AcquisitionDateList acqDatesList = new AcquisitionDateList();
        List<String> acqDates = acqDatesList.getAcquisitionDate();
        acqDates.add(CURRENT_DATE_UTC);
        acqDates.add(CURRENT_DATE_UTC);
        acquisition.setAcquisitionDates(acqDatesList);

        OwnerList ownersList = new OwnerList();
        List<String> owners = ownersList.getOwner();
        // FIXME Use properly formatted refNames for representative owners
        // in this example test record. The following are mere placeholders.
        owners.add("First Owner -" + identifier);
        owners.add("Second Owner-" + identifier);
        acquisition.setOwners(ownersList);

        PoxPayloadOut multipart = new PoxPayloadOut(AcquisitionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(acquisition,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new AcquisitionClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, acquisition common");
            logger.debug(objectAsXmlString(acquisition, AcquisitionsCommon.class));
        }
        return multipart;
    }

    // FIXME: The following methods might be made generic and moved to a common package.

    /**
     * Retrives an XML document from the given file, and uses
     * the JAXB unmarshaller to create a Java object representation
     * and ultimately a multipart payload that can be submitted in
     * a create or update request.
     *
     * @param commonPartName
     * @param commonPartFileName
     * @return
     * @throws Exception
     */
    private PoxPayloadOut createAcquisitionInstanceFromXml(String testName, String commonPartName,
            String commonPartFileName) throws Exception {

        AcquisitionsCommon acquisition =
                (AcquisitionsCommon) getObjectFromFile(AcquisitionsCommon.class,
                commonPartFileName);
        PoxPayloadOut multipart = new PoxPayloadOut(AcquisitionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(acquisition,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(commonPartName);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + " to be created, acquisitions common");
            logger.debug(objectAsXmlString(acquisition,
                    AcquisitionsCommon.class));
        }
        return multipart;

    }

    /**
     * Creates a record / resource from the data in an XML file.
     *
     * @param testName the test name
     * @param fileName the file name
     * @param useJaxb the use jaxb
     * @return the string
     * @throws Exception the exception
     */
    private String createFromXmlFile(String testName, String fileName, boolean useJaxb) throws Exception {

        // Perform setup.
        setupCreate();

        PoxPayloadOut multipart = null;

        AcquisitionClient client = new AcquisitionClient();
        if (useJaxb) {
            multipart = createAcquisitionInstanceFromXml(testName,
                    client.getCommonPartName(), fileName);
        } else {

            multipart = createAcquisitionInstanceFromRawXml(testName,
                    client.getCommonPartName(), fileName);
        }
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        String newId = extractId(res);
        allResourceIdsCreated.add(newId);
        return newId;
    }

     /**
     * Returns a multipart payload that can be submitted with a
     * create or update request, by reading from an XML file.
     *
     * @param commonPartName
     * @param commonPartFileName
     * @return
     * @throws Exception
     */
    private PoxPayloadOut createAcquisitionInstanceFromRawXml(String testName, String commonPartName,
            String commonPartFileName) throws Exception {

        PoxPayloadOut multipart = new PoxPayloadOut(AcquisitionClient.SERVICE_PAYLOAD_NAME);
        String stringObject = getXmlDocumentAsString(commonPartFileName);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " to be created, acquisition common " + "\n" + stringObject);
        }
        PayloadOutputPart commonPart = multipart.addPart(stringObject, stringObject);
        commonPart.setLabel(commonPartName);

        return multipart;

    }

    // FIXME: This duplicates code in read(), and should be consolidated.
    // This is an expedient to support reading and verifying the contents
    // of resources that have been created from test data XML files.
    private AcquisitionsCommon readAcquisitionCommonPart(String csid)
        throws Exception {

        String testName = "readAcquisitionCommonPart";

        setupRead();

        // Submit the request to the service and store the response.
        AcquisitionClient client = new AcquisitionClient();
        ClientResponse<String> res = client.read(csid);
        AcquisitionsCommon acquisition = null;
        try {
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": Reading Common part ...");
	        }
	        acquisition = (AcquisitionsCommon) extractPart(input,
	                client.getCommonPartName(), AcquisitionsCommon.class);
	        Assert.assertNotNull(acquisition);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        return acquisition;
     }


	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AcquisitionsCommon updateInstance(
			AcquisitionsCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(AcquisitionsCommon original,
			AcquisitionsCommon updated) throws Exception {
		// TODO Auto-generated method stub
		
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
    	// Do nothing.  Simply here to for a TestNG execution order for our tests
    }	
}

