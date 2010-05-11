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

//import org.collectionspace.services.client.AbstractServiceClientImpl;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionObjectFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionobjectsNaturalhistory;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.collectionobject.ResponsibleDepartmentList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectServiceTest, carries out tests against a
 * deployed and running CollectionObject Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class CollectionObjectServiceTest extends AbstractServiceTestImpl {

    /** The logger. */
    private final Logger logger =
            LoggerFactory.getLogger(CollectionObjectServiceTest.class);
    // Instance variables specific to this test.
    /** The known resource id. */
    private String knownResourceId = null;
    
    /** The multivalue. */
    private boolean multivalue; //toggle

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return new CollectionObjectClient().getServicePathComponent();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new CollectionObjectClient();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response) {
        return response.getEntity(CollectionobjectsCommonList.class);
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

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        String identifier = createIdentifier();
        MultipartOutput multipart =
                createCollectionObjectInstance(client.getCommonPartName(), identifier);
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


    /*
     * Tests to diagnose and verify the fixed status of CSPACE-1026,
     * "Whitespace at certain points in payload cause failure"
     */
    /**
     * Creates the from xml cambridge.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlCambridge(String testName) throws Exception {
        String newId =
            createFromXmlFile(testName, "./test-data/testCambridge.xml", true);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s1.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS1(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            //createFromXmlFile(testName, "./target/test-classes/test-data/repfield_whitesp1.xml", false);
        	createFromXmlFile(testName, testDataDir + "/repfield_whitesp1.xml", false);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s2.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS2(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            //createFromXmlFile(testName, "./target/test-classes/test-data/repfield_whitesp2.xml", false);
        	createFromXmlFile(testName, testDataDir + "/repfield_whitesp2.xml", false);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s3.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS3(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            //createFromXmlFile(testName, "./target/test-classes/test-data/repfield_whitesp3.xml", false);
        	createFromXmlFile(testName, testDataDir + "/repfield_whitesp3.xml", false);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s4.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS4(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            createFromXmlFile(testName, testDataDir + "/repfield_whitesp4.xml", false);
        testSubmitRequest(newId);
    }

    /*
     * Tests to diagnose and verify the fixed status of CSPACE-1248,
     * "Wedged records created!" (i.e. records with child repeatable
     * fields, which contain null values, can be successfully created
     * but an error occurs on trying to retrieve those records).
     */
    /**
     * Creates the with null value repeatable field.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithNullValueRepeatableField(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
    	String newId =
            createFromXmlFile(testName, testDataDir + "/repfield_null1.xml", false);
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully created record with null value repeatable field.");
            logger.debug("Attempting to retrieve just-created record ...");
        }
        testSubmitRequest(newId);
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createList()
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
    	this.createPaginatedList(testName, DEFAULT_LIST_SIZE);
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithEmptyEntityBody(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

   /**
    * Test how the service handles XML that is not well formed,
    * when sent in the payload of a Create request.
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
    */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithMalformedXml(String testName) throws Exception {
        setupCreate(testName);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithWrongXmlSchema(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }


/*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throwsException {

    // Perform setup.
    setupCreateWithEmptyEntityBody(testName);

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
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml(String testName) throws Exception {

    // Perform setup.
    setupCreateWithMalformedXml(testName);

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

    // Perform setup.
    setupCreateWithWrongXmlSchema(testName);

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

   /**
    * Test how the service handles, in a Create request, payloads
    * containing null values (or, in the case of String fields,
    * empty String values) in one or more fields which must be
    * present and are required to contain non-empty values.
    *
    * This is a test of code and/or configuration in the service's
    * validation routine(s).
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
    * @throws Exception 
    */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithRequiredValuesNullOrEmpty(String testName) throws Exception {
        setupCreate(testName);

        // Build a payload with invalid content, by omitting a
        // field (objectNumber) which must be present, and in which
        // a non-empty value is required, as enforced by the service's
        // validation routine(s).
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
        collectionObject.setTitle("atitle");
        collectionObject.setObjectName("some name");

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        MultipartOutput multipart =
                createCollectionObjectInstance(client.getCommonPartName(), collectionObject, null);
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        // Read the response and verify that the create attempt failed.
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

        // FIXME: Consider splitting off the following into its own test method.
        
        // Build a payload with invalid content, by setting a value to the
        // empty String, in a field that requires a non-empty value,
        // as enforced by the service's validation routine(s).
        collectionObject = new CollectionobjectsCommon();
        collectionObject.setTitle("atitle");
        collectionObject.setObjectName("some name");
        collectionObject.setObjectNumber("");

        // Submit the request to the service and store the response.
        multipart =
            createCollectionObjectInstance(client.getCommonPartName(), collectionObject, null);
        res = client.create(multipart);
        statusCode = res.getStatus();

        // Read the response and verify that the create attempt failed.
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

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

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        MultipartInput input = (MultipartInput) res.getEntity();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Reading Common part ...");
        }
        CollectionobjectsCommon collectionObject =
                (CollectionobjectsCommon) extractPart(input,
                client.getCommonPartName(), CollectionobjectsCommon.class);
        Assert.assertNotNull(collectionObject);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Reading Natural History part ...");
        }
        CollectionobjectsNaturalhistory conh =
                (CollectionobjectsNaturalhistory) extractPart(input,
                getNHPartName(), CollectionobjectsNaturalhistory.class);
        Assert.assertNotNull(conh);
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
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

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<CollectionobjectsCommonList> res = client.readList();
        CollectionobjectsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<CollectionobjectsCommonList.CollectionObjectListItem> items =
                    list.getCollectionObjectListItem();
            int i = 0;

            for (CollectionobjectsCommonList.CollectionObjectListItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] csid="
                        + item.getCsid());
                logger.debug(testName + ": list-item[" + i + "] objectNumber="
                        + item.getObjectNumber());
                logger.debug(testName + ": list-item[" + i + "] URI="
                        + item.getUri());
                i++;

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
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Read an existing resource that will be updated.
        ClientResponse<MultipartInput> res = updateRetrieve(testName, knownResourceId);

        // Extract its common part.
        CollectionObjectClient client = new CollectionObjectClient();
        MultipartInput input = (MultipartInput) res.getEntity();
        CollectionobjectsCommon collectionObject =
                (CollectionobjectsCommon) extractPart(input,
                client.getCommonPartName(), CollectionobjectsCommon.class);
        Assert.assertNotNull(collectionObject);

        // Change the content of one or more fields in the common part.
        collectionObject.setObjectNumber("updated-" + collectionObject.getObjectNumber());
        collectionObject.setObjectName("updated-" + collectionObject.getObjectName());
        if (logger.isDebugEnabled()) {
            logger.debug("sparse update that will be sent in update request:");
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }

        // Send the changed resource to be updated.
        res = updateSend(testName, knownResourceId, collectionObject);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Read the response and verify that the resource was correctly updated.
        input = (MultipartInput) res.getEntity();
        CollectionobjectsCommon updatedCollectionObject =
                (CollectionobjectsCommon) extractPart(input,
                client.getCommonPartName(), CollectionobjectsCommon.class);
        Assert.assertNotNull(updatedCollectionObject);
        Assert.assertEquals(updatedCollectionObject.getObjectName(),
                collectionObject.getObjectName(),
                "Data in updated object did not match submitted data.");

    }

    /**
     * Update retrieve.
     *
     * @param testName the test name
     * @param id the id
     * @return the client response
     */
    private ClientResponse<MultipartInput> updateRetrieve(String testName, String id) {
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<MultipartInput> res = client.read(id);
        if (logger.isDebugEnabled()) {
            logger.debug("read in updateRetrieve for " + testName + " status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS);
        if (logger.isDebugEnabled()) {
            logger.debug("got object to updateRetrieve for " + testName + " with ID: " + id);
        }
        return res;
    }

    /**
     * Update send.
     *
     * @param testName the test name
     * @param id the id
     * @param collectionObject the collection object
     * @return the client response
     */
    private ClientResponse<MultipartInput> updateSend(String testName, String id,
            CollectionobjectsCommon collectionObject) {
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
        CollectionObjectClient client = new CollectionObjectClient();
        commonPart.getHeaders().add("label", client.getCommonPartName());
        ClientResponse<MultipartInput> res = client.update(knownResourceId, output);
        return res;
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

   /**
    * Test how the service handles XML that is not well formed,
    * when sent in the payload of an Update request.
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
    */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithMalformedXml(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

/*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithEmptyEntityBody(String testName) throws Exception {

    // Perform setup.
    setupUpdateWithEmptyEntityBody(testName);

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
    public void updateWithMalformedXml() throws Exception {

    // Perform setup.
    setupUpdateWithMalformedXml(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    final String entity = MALFORMED_XML_DATA;
    String mediaType = MediaType.APPLICATION_XML;
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

    // Perform setup.
    setupUpdateWithWrongXmlSchema(String testName);

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

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updateCollectionObject(), below.
        CollectionObjectClient client = new CollectionObjectClient();
        MultipartOutput multipart =
                createCollectionObjectInstance(client.getCommonPartName(),
                NON_EXISTENT_ID);
        ClientResponse<MultipartInput> res =
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

   /**
    * Test how the service handles, in an Update request, payloads
    * containing null values (or, in the case of String fields,
    * empty String values) in one or more fields in which non-empty
    * values are required.
    *
    * This is a test of code and/or configuration in the service's
    * validation routine(s).
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
 * @throws Exception 
    */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithRequiredValuesNullOrEmpty(String testName) throws Exception {
        // Perform setup.
        setupUpdate(testName);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " got object to update with ID: " + knownResourceId);
        }

        // Read an existing record for updating.
        ClientResponse<MultipartInput> res = updateRetrieve(testName, knownResourceId);

        CollectionObjectClient client = new CollectionObjectClient();
        MultipartInput input = (MultipartInput) res.getEntity();
        CollectionobjectsCommon collectionObject =
                (CollectionobjectsCommon) extractPart(input,
                client.getCommonPartName(), CollectionobjectsCommon.class);
        Assert.assertNotNull(collectionObject);

        // Update with invalid content, by setting a value to the
        // empty String, in a field that requires a non-empty value,
        // as enforced by the service's validation routine(s).
        collectionObject.setObjectNumber("");

        if (logger.isDebugEnabled()) {
            logger.debug(testName + " updated object");
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }

        // Submit the request to the service and store the response.
        res = updateSend(testName, knownResourceId, collectionObject);
        int statusCode = res.getStatus();

        // Read the response and verify that the update attempt failed.
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

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
    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
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

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
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
     * @throws Exception 
     */

    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() throws Exception {
        testSubmitRequest(knownResourceId);
    }

    /**
     * Test submit request.
     *
     * @param resourceId the resource id
     * @throws Exception the exception
     */
    private void testSubmitRequest(String resourceId) throws Exception {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(resourceId);
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
    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param identifier the identifier
     * @return the multipart output
     */
    private MultipartOutput createCollectionObjectInstance(String commonPartName,
            String identifier) {
        return createCollectionObjectInstance(commonPartName,
                "objectNumber-" + identifier,
                "objectName-" + identifier);
    }

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param objectNumber the object number
     * @param objectName the object name
     * @return the multipart output
     */
    private MultipartOutput createCollectionObjectInstance(String commonPartName,
            String objectNumber, String objectName) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
        ResponsibleDepartmentList deptList = new ResponsibleDepartmentList();
        List<String> depts = deptList.getResponsibleDepartment();
        // @TODO Use properly formatted refNames for representative departments
        // in this example test record. The following are mere placeholders.
        depts.add("urn:org.collectionspace.services.department:Registrar");
        if (multivalue) {
            depts.add("urn:org.walkerart.department:Fine Art");
        }
        multivalue = !multivalue;
        //FIXME: Title does not need to be set.
        collectionObject.setTitle("atitle");
        collectionObject.setResponsibleDepartments(deptList);
        collectionObject.setObjectNumber(objectNumber);
        collectionObject.setOtherNumber("urn:org.walkerart.id:123");
        collectionObject.setObjectName(objectName);
        collectionObject.setAge(""); //test for null string
        collectionObject.setBriefDescription("Papier mache bird cow mask with horns, "
                + "painted red with black and yellow spots. "
                + "Puerto Rico. ca. 8&quot; high, 6&quot; wide, projects 10&quot; (with horns).");

        CollectionobjectsNaturalhistory conh = new CollectionobjectsNaturalhistory();
        conh.setNhString("test-string");
        conh.setNhInt(999);
        conh.setNhLong(9999);


        MultipartOutput multipart = createCollectionObjectInstance(commonPartName, collectionObject, conh);
        return multipart;
    }

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param collectionObject the collection object
     * @param conh the conh
     * @return the multipart output
     */
    private MultipartOutput createCollectionObjectInstance(String commonPartName,
            CollectionobjectsCommon collectionObject, CollectionobjectsNaturalhistory conh) {

        MultipartOutput multipart = CollectionObjectFactory.createCollectionObjectInstance(
                commonPartName, collectionObject, getNHPartName(), conh);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, collectionobject common");
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }

        if (conh != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("to be created, collectionobject nhistory");
                logger.debug(objectAsXmlString(conh,
                        CollectionobjectsNaturalhistory.class));
            }
        }
        return multipart;

    }

    /**
     * createCollectionObjectInstanceFromXml uses JAXB unmarshaller to retrieve
     * collectionobject from given file
     * @param commonPartName
     * @param commonPartFileName
     * @return
     * @throws Exception
     */
    private MultipartOutput createCollectionObjectInstanceFromXml(String testName, String commonPartName,
            String commonPartFileName) throws Exception {

        CollectionobjectsCommon collectionObject =
                (CollectionobjectsCommon) getObjectFromFile(CollectionobjectsCommon.class,
                commonPartFileName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(collectionObject,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", commonPartName);

        if (logger.isDebugEnabled()) {
            logger.debug(testName + " to be created, collectionobject common");
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }
        return multipart;

    }

    /**
     * createCollectionObjectInstanceFromRawXml uses stringified collectionobject
     * retrieve from given file
     * @param commonPartName
     * @param commonPartFileName
     * @return
     * @throws Exception
     */
    private MultipartOutput createCollectionObjectInstanceFromRawXml(String testName, String commonPartName,
            String commonPartFileName) throws Exception {

        MultipartOutput multipart = new MultipartOutput();
        String stringObject = getXmlDocumentAsString(commonPartFileName);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " to be created, collectionobject common " + "\n" + stringObject);
        }
        OutputPart commonPart = multipart.addPart(stringObject,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", commonPartName);

        return multipart;

    }

    /**
     * Gets the nH part name.
     *
     * @return the nH part name
     */
    private String getNHPartName() {
        return "collectionobjects_naturalhistory";
    }

    /**
     * Creates the from xml file.
     *
     * @param testName the test name
     * @param fileName the file name
     * @param useJaxb the use jaxb
     * @return the string
     * @throws Exception the exception
     */
    private String createFromXmlFile(String testName, String fileName, boolean useJaxb) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

        MultipartOutput multipart = null;

        CollectionObjectClient client = new CollectionObjectClient();
        if (useJaxb) {
            multipart = createCollectionObjectInstanceFromXml(testName,
                    client.getCommonPartName(), fileName);
        } else {
            multipart = createCollectionObjectInstanceFromRawXml(testName,
                    client.getCommonPartName(), fileName);
        }
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        String newId = extractId(res);
        allResourceIdsCreated.add(newId);
        return newId;
    }
}
