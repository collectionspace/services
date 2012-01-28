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
import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.ContactClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.contact.AddressGroup;
import org.collectionspace.services.contact.AddressGroupList;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.EmailGroup;
import org.collectionspace.services.contact.EmailGroupList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContactServiceTest, carries out tests against a
 * deployed and running Contact Service.
 *
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class ContactServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ContactsCommon> {

    private final String CLASS_NAME = ContactServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(ContactServiceTest.class);
    // Instance variables specific to this test.
//    final String SERVICE_PATH_COMPONENT = "contacts";

    @Override
    public String getServicePathComponent() {
        return ContactClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return ContactClient.SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new ContactClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getCommonList(
            ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }

//    @Override
//    protected PoxPayloadOut createInstance(String identifier) {
//    	ContactClient client = new ContactClient();
//        PoxPayloadOut multipart =
//            ContactClientUtils.createContactInstance(identifier, client.getCommonPartName());
//        return multipart;
//    }
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        ContactClient client = new ContactClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart =
                ContactClientUtils.createContactInstance(identifier, client.getCommonPartName());
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
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty?
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
        //Should this really be empty??
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty??
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
    setupCreateWithEmptyEntityBody(testName, logger);
    
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
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        ContactClient client = new ContactClient();
        ClientResponse<String> res = client.read(knownResourceId);
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        ContactsCommon contact = (ContactsCommon) extractPart(input,
	                client.getCommonPartName(), ContactsCommon.class);
	        Assert.assertNotNull(contact);
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
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        ContactClient client = new ContactClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        try {
	        assertStatusCode(res, testName);
	        AbstractCommonList list = res.getEntity();
	
	        // Optionally output additional data about list members for debugging.
	        boolean iterateThroughList = false;
	        if (iterateThroughList && logger.isDebugEnabled()) {
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
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        // Perform setup.
        setupUpdate();
        //
        // First read the object that will be updated
        //
        ContactClient client = new ContactClient();
        ClientResponse<String> res = client.read(knownResourceId);
        ContactsCommon contact = null;
        try {
	        assertStatusCode(res, testName);
	        
	        if (logger.isDebugEnabled()) {
	            logger.debug("got object to update with ID: " + knownResourceId);
	        }
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        contact = (ContactsCommon) extractPart(input,
	                client.getCommonPartName(), ContactsCommon.class);
	        Assert.assertNotNull(contact);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("contact common before updating");
            logger.debug(BaseServiceTest.objectAsXmlString(contact, ContactsCommon.class));
        }

        // Verify the contents of this resource
        EmailGroupList emailGroupList = contact.getEmailGroupList();
        Assert.assertNotNull(emailGroupList);
        List<EmailGroup> emailGroups = emailGroupList.getEmailGroup();
        Assert.assertNotNull(emailGroups);
        Assert.assertTrue(emailGroups.size() > 0);
        String email = emailGroups.get(0).getEmail();
        Assert.assertNotNull(email);

        AddressGroupList addressGroupList = contact.getAddressGroupList();
        Assert.assertNotNull(addressGroupList);
        List<AddressGroup> addressGroups = addressGroupList.getAddressGroup();
        Assert.assertNotNull(addressGroups);
        Assert.assertTrue(addressGroups.size() > 0);
        String addressType = addressGroups.get(0).getAddressType();
        String addressPlace1 = addressGroups.get(0).getAddressPlace1();
        Assert.assertNotNull(addressType);
        Assert.assertNotNull(addressPlace1);

        // Update the contents of this resource.
        emailGroups.get(0).setEmail("updated-" + email);
        contact.setEmailGroupList(emailGroupList);

        addressGroups.get(0).setAddressType("updated-" + addressType);
        addressGroups.get(0).setAddressPlace1("updated-" + addressPlace1);
        contact.setAddressGroupList(addressGroupList);

        if (logger.isDebugEnabled()) {
            logger.debug("to be updated object");
            logger.debug(BaseServiceTest.objectAsXmlString(contact, ContactsCommon.class));
        }
        //
        // Next, send the update to the server
        //
        PoxPayloadOut output = new PoxPayloadOut(ContactClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), contact);

        res = client.update(knownResourceId, output);
        ContactsCommon updatedContact = null;
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedContact = (ContactsCommon) extractPart(input,
	                client.getCommonPartName(), ContactsCommon.class);
	        Assert.assertNotNull(updatedContact);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("object after update");
            logger.debug(objectAsXmlString(updatedContact, ContactsCommon.class));
        }
                
        Assert.assertNotNull(updatedContact.getEmailGroupList().getEmailGroup().get(0));
        Assert.assertEquals(updatedContact.getEmailGroupList().getEmailGroup().get(0).getEmail(),
                contact.getEmailGroupList().getEmailGroup().get(0).getEmail(),
                "Data in updated object did not match submitted data.");

    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty??
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
        //Should this really be empty??
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty??
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
    setupUpdateWithWrongXmlSchema(testName, logger);
    
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
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        ContactClient client = new ContactClient();
        PoxPayloadOut multipart =
                ContactClientUtils.createContactInstance(NON_EXISTENT_ID, client.getCommonPartName());
        ClientResponse<String> res =
                client.update(NON_EXISTENT_ID, multipart);
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
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ContactClient client = new ContactClient();
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
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        ContactClient client = new ContactClient();
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
    
    @Override
    public void readWorkflow(String testName) throws Exception {
    	// Not applicable for the Contact service
    }
    
    @Override
    public void searchWorkflowDeleted(String testName) throws Exception {
    	// Not applicable for the Contact service
    }

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		return ContactClientUtils.createContactInstance(identifier, commonPartName);
	}

	@Override
	protected ContactsCommon updateInstance(ContactsCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(ContactsCommon original,
			ContactsCommon updated) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub		
	}
}
