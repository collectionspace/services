/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.ContactClientUtils;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * PersonAuthorityServiceTest, carries out tests against a
 * deployed and running PersonAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class PersonAuthorityServiceTest extends AbstractServiceTestImpl {

    private final Logger logger =
        LoggerFactory.getLogger(PersonAuthorityServiceTest.class);

    // Instance variables specific to this test.
    private PersonAuthorityClient client = new PersonAuthorityClient();
    private ContactClient contactClient = new ContactClient();
    final String SERVICE_PATH_COMPONENT = "personauthorities";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";
    final String CONTACT_SERVICE_PATH_COMPONENT = "contacts";
    final String TEST_FORE_NAME = "John";
    final String TEST_MIDDLE_NAME = null;
    final String TEST_SUR_NAME = "Wayne";
    final String TEST_BIRTH_DATE = "May 26, 1907";
    final String TEST_DEATH_DATE = "June 11, 1979";
 
    private String knownResourceId = null;
    private String knownResourceDisplayName = null;
    private String knownResourceRefName = null;
    private String knownItemResourceId = null;
    private String knownContactResourceId = null;
    private int nItemsToCreateInList = 3;
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    private Map<String, String> allItemResourceIdsCreated =
        new HashMap<String, String>();
    private Map<String, String> allContactResourceIdsCreated =
        new HashMap<String, String>();

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"create"})
    public void create(String testName) throws Exception {

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
    	String displayName = "displayName-" + identifier;
    	String baseRefName = PersonAuthorityClientUtils.createPersonAuthRefName(displayName, false);
    	String fullRefName = PersonAuthorityClientUtils.createPersonAuthRefName(displayName, true);
    	MultipartOutput multipart = 
            PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    displayName, fullRefName, client.getCommonPartName());
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
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the refname from the first resource created
        // for additional tests below.
        knownResourceRefName = baseRefName;

        String newID = PersonAuthorityClientUtils.extractId(res);
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = newID;
            knownResourceDisplayName = displayName;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"create"}, dependsOnMethods = {"create"})
    public void createItem(String testName) {
        setupCreate(testName);
        String newID = createItemInAuthority(knownResourceId, knownResourceRefName);
    }

    private String createItemInAuthority(String vcsid, String authRefName) {

        final String testName = "createItemInAuthority";
        if(logger.isDebugEnabled()){
            logger.debug(testName + ":...");
        }

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        String refName = PersonAuthorityClientUtils.createPersonRefName(authRefName, "John Wayne", true);
        Map<String, String> johnWayneMap = new HashMap<String,String>();
        johnWayneMap.put(PersonJAXBSchema.FORE_NAME, TEST_FORE_NAME);
        johnWayneMap.put(PersonJAXBSchema.SUR_NAME, TEST_SUR_NAME);
        johnWayneMap.put(PersonJAXBSchema.GENDER, "male");
        johnWayneMap.put(PersonJAXBSchema.BIRTH_DATE, TEST_BIRTH_DATE);
        johnWayneMap.put(PersonJAXBSchema.BIRTH_PLACE, "Winterset, Iowa");
        johnWayneMap.put(PersonJAXBSchema.DEATH_DATE, TEST_DEATH_DATE);
        johnWayneMap.put(PersonJAXBSchema.BIO_NOTE, "born Marion Robert Morrison and better" +
            "known by his stage name John Wayne, was an American film actor, director " +
            "and producer. He epitomized rugged masculinity and has become an enduring " +
            "American icon. He is famous for his distinctive voice, walk and height. " +
            "He was also known for his conservative political views and his support in " +
            "the 1950s for anti-communist positions.");
        MultipartOutput multipart = 
            PersonAuthorityClientUtils.createPersonInstance(vcsid, refName, johnWayneMap,
                client.getItemCommonPartName() );
        ClientResponse<Response> res = client.createItem(vcsid, multipart);
        int statusCode = res.getStatus();
        String newID = PersonAuthorityClientUtils.extractId(res);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null){
            knownItemResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allItemResourceIdsCreated.put(newID, vcsid);

        return newID;
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"create"}, dependsOnMethods = {"createItem"})
    public void createContact(String testName) {
        setupCreate(testName);
        String newID = createContactInItem(knownResourceId, knownItemResourceId);
    }

   private String createContactInItem(String parentcsid, String itemcsid) {

        final String testName = "createContactInItem";
        setupCreate(testName);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ":...");
        }

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        MultipartOutput multipart =
            ContactClientUtils.createContactInstance(parentcsid,
            itemcsid, identifier, contactClient.getCommonPartName());
        ClientResponse<Response> res =
             client.createContact(parentcsid, itemcsid, multipart);
        int statusCode = res.getStatus();
        String newID = PersonAuthorityClientUtils.extractId(res);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from the first contact resource created
        // for additional tests below.
        if (knownContactResourceId == null){
            knownContactResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownContactResourceId=" + knownContactResourceId);
            }
        }

        // Store the IDs from any contact resources created
        // by tests, along with the IDs of their parent items,
        // so these items can be deleted after all tests have been run.
        allContactResourceIdsCreated.put(newID, itemcsid);

        return newID;
    }

    // Failure outcomes

    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

/*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        groups = {"create"}, dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throws Exception {

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
    if(logger.isDebugEnabled()) {
        logger.debug(testName + ": url=" + url +
            " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        groups = {"create"}, dependsOnMethods = {"create", "testSubmitRequest"})
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
        groups = {"create"}, dependsOnMethods = {"create", "testSubmitRequest"})
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

    // ---------------------------------------------------------------
    // CRUD tests : CREATE LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"createList"}, dependsOnGroups = {"create"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < nItemsToCreateInList; i++) {
            create(testName);
        }
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"createList"}, dependsOnMethods = {"createList"})
    public void createItemList(String testName) throws Exception {
        // Add items to the initially-created, known parent record.
        for (int j = 0; j < nItemsToCreateInList; j++) {
            createItem(testName);
        }
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"createList"}, dependsOnMethods = {"createItemList"})
    public void createContactList(String testName) throws Exception {
        // Add contacts to the initially-created, known item record.
        for (int j = 0; j < nItemsToCreateInList; j++) {
            createContact(testName);
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnGroups = {"create"})
    public void read(String testName) throws Exception {

        // Perform setup.
        setupRead();
        
        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //FIXME: remove the following try catch once Aron fixes signatures
        try {
            MultipartInput input = (MultipartInput) res.getEntity();
            PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), PersonauthoritiesCommon.class);
            Assert.assertNotNull(personAuthority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
            groups = {"read"}, dependsOnGroups = {"create"})
        public void readByName(String testName) throws Exception {

            // Perform setup.
            setupRead();
            
            // Submit the request to the service and store the response.
            ClientResponse<MultipartInput> res = client.readByName(knownResourceDisplayName);
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if(logger.isDebugEnabled()){
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
            //FIXME: remove the following try catch once Aron fixes signatures
            try {
                MultipartInput input = (MultipartInput) res.getEntity();
                PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), PersonauthoritiesCommon.class);
                Assert.assertNotNull(personAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

/*
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        groups = {"read"}, dependsOnMethods = {"read"})
    public void readByName(String testName) throws Exception {

        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //FIXME: remove the following try catch once Aron fixes signatures
        try {
            MultipartInput input = (MultipartInput) res.getEntity();
            PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), PersonauthoritiesCommon.class);
            Assert.assertNotNull(personAuthority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
*/

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"read"})
    public void readItem(String testName) throws Exception {

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether we've received a person.
        MultipartInput input = (MultipartInput) res.getEntity();
        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);
        boolean showFull = true;
        if(showFull && logger.isDebugEnabled()){
            logger.debug(testName + ": returned payload:");
            logger.debug(objectAsXmlString(person, PersonsCommon.class));
        }
        Assert.assertEquals(person.getInAuthority(), knownResourceId);

    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"readItem", "updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether person has expected displayName.
        MultipartInput input = (MultipartInput) res.getEntity();
        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);
        String displayName = person.getDisplayName();
        // Make sure displayName matches computed form
        String expectedDisplayName = 
            PersonAuthorityClientUtils.prepareDefaultDisplayName(
	        TEST_FORE_NAME, null, TEST_SUR_NAME,
	        TEST_BIRTH_DATE, TEST_DEATH_DATE);
        Assert.assertNotNull(displayName, expectedDisplayName);
        
        // Update the shortName and verify the computed name is updated.
        person.setCsid(null);
        person.setDisplayNameComputed(true);
        person.setForeName("updated-" + TEST_FORE_NAME);
        expectedDisplayName = 
            PersonAuthorityClientUtils.prepareDefaultDisplayName(
        	"updated-" + TEST_FORE_NAME, null, TEST_SUR_NAME, 
	       	TEST_BIRTH_DATE, TEST_DEATH_DATE);

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(person, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        PersonsCommon updatedPerson =
                (PersonsCommon) extractPart(input,
                        client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(updatedPerson);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPerson.getForeName(), person.getForeName(),
            "Updated ForeName in Person did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedPerson.getDisplayName(), expectedDisplayName,
            "Updated ForeName in Person not reflected in computed DisplayName.");

        // Now Update the displayName, not computed and verify the computed name is overriden.
        person.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        person.setDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new MultipartOutput();
        commonPart = output.addPart(person, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        updatedPerson =
                (PersonsCommon) extractPart(input,
                        client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(updatedPerson);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPerson.isDisplayNameComputed(), false,
                "Updated displayNameComputed in Person did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedPerson.getDisplayName(),
        		expectedDisplayName,
                "Updated DisplayName (not computed) in Person not stored.");
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
            dependsOnMethods = {"verifyItemDisplayName"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {

        // Perform setup.
    	setupUpdateWithWrongXmlSchema(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());

        // Check whether Person has expected displayName.
        MultipartInput input = (MultipartInput) res.getEntity();
        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);
        // Try to Update with computed false and no displayName
        person.setDisplayNameComputed(false);
        person.setDisplayName(null);

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(person, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
    
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"readItem"})
    public void readContact(String testName) throws Exception {

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res =
            client.readContact(knownResourceId, knownItemResourceId,
            knownContactResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether we've received a contact.
        MultipartInput input = (MultipartInput) res.getEntity();
        ContactsCommon contact = (ContactsCommon) extractPart(input,
                contactClient.getCommonPartName(), ContactsCommon.class);
        Assert.assertNotNull(contact);
        boolean showFull = true;
        if(showFull && logger.isDebugEnabled()){
            logger.debug(testName + ": returned payload:");
            logger.debug(objectAsXmlString(contact, ContactsCommon.class));
        }
        Assert.assertEquals(contact.getInAuthority(), knownResourceId);
        Assert.assertEquals(contact.getInItem(), knownItemResourceId);

    }

    // Failure outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"readItem"})
    public void readItemNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"readContact"})
    public void readContactNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res =
            client.readContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readList"}, dependsOnGroups = {"createList", "read"})
    public void readList(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        ClientResponse<PersonauthoritiesCommonList> res = client.readList();
        PersonauthoritiesCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<PersonauthoritiesCommonList.PersonauthorityListItem> items =
                    list.getPersonauthorityListItem();
            int i = 0;
            for (PersonauthoritiesCommonList.PersonauthorityListItem item : items) {
                String csid = item.getCsid();
                logger.debug(testName + ": list-item[" + i + "] csid=" +
                        csid);
                logger.debug(testName + ": list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
                readItemList(csid, null);
                i++;
            }
        }
    }

    @Test(groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList() {
        readItemList(knownResourceId, null);
    }

    @Test(groups = {"readList"}, dependsOnMethods = {"readItemList"})
    public void readItemListByAuthorityName() {
        readItemList(null, knownResourceDisplayName);
    }
    private void readItemList(String vcsid, String name) {

        final String testName = "readItemList";

        // Perform setup.
        setupReadList(testName);
        
        ClientResponse<PersonsCommonList> res = null;
        
        if(vcsid!= null) {
	        // Submit the request to the service and store the response.
	        res = client.readItemList(vcsid);
        } else if(name!= null) {
    	        // Submit the request to the service and store the response.
   	        res = client.readItemListForNamedAuthority(name);
        } else {
        	Assert.fail("readItemList passed null csid and name!");
        }
        PersonsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        List<PersonsCommonList.PersonListItem> items =
            list.getPersonListItem();
        int nItemsReturned = items.size();
        // There will be one item created, associated with a
        // known parent resource, by the createItem test.
        //
        // In addition, there will be 'nItemsToCreateInList'
        // additional items created by the createItemList test,
        // all associated with the same parent resource.
        int nExpectedItems = nItemsToCreateInList + 1;
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": Expected "
           		+ nExpectedItems +" items; got: "+nItemsReturned);
        }
        Assert.assertEquals(nItemsReturned, nExpectedItems);

        int i = 0;
        for (PersonsCommonList.PersonListItem item : items) {
        	Assert.assertTrue((null != item.getRefName()), "Item refName is null!");
        	Assert.assertTrue((null != item.getDisplayName()), "Item displayName is null!");
        	// Optionally output additional data about list members for debugging.
	        boolean showDetails = true;
	        if (showDetails && logger.isDebugEnabled()) {
                logger.debug("  " + testName + ": list-item[" + i + "] csid=" +
                        item.getCsid());
                logger.debug("  " + testName + ": list-item[" + i + "] refName=" +
                        item.getRefName());
                logger.debug("  " + testName + ": list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                logger.debug("  " + testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
            }
            i++;
        }
    }

    @Test(groups = {"readList"}, dependsOnMethods = {"readItemList"})
    public void readContactList() {
        readContactList(knownResourceId, knownItemResourceId);
    }

    private void readContactList(String parentcsid, String itemcsid) {
        final String testName = "readContactList";

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        ClientResponse<ContactsCommonList> res =
                client.readContactList(parentcsid, itemcsid);
        ContactsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        List<ContactsCommonList.ContactListItem> listitems =
            list.getContactListItem();
        int nItemsReturned = listitems.size();
        // There will be one item created, associated with a
        // known parent resource, by the createItem test.
        //
        // In addition, there will be 'nItemsToCreateInList'
        // additional items created by the createItemList test,
        // all associated with the same parent resource.
        int nExpectedItems = nItemsToCreateInList + 1;
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": Expected "
           		+ nExpectedItems +" items; got: "+nItemsReturned);
        }
        Assert.assertEquals(nItemsReturned, nExpectedItems);

        int i = 0;
        for (ContactsCommonList.ContactListItem listitem : listitems) {
        	// Optionally output additional data about list members for debugging.
	        boolean showDetails = false;
	        if (showDetails && logger.isDebugEnabled()) {
                logger.debug("  " + testName + ": list-item[" + i + "] csid=" +
                        listitem.getCsid());
                logger.debug("  " + testName + ": list-item[" + i + "] addressPlace=" +
                        listitem.getAddressPlace());
                logger.debug("  " + testName + ": list-item[" + i + "] URI=" +
                        listitem.getUri());
            }
            i++;
        }
    }

    // Failure outcomes
    // None at present.

    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnGroups = {"read", "readList"})
    public void update(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Retrieve the contents of a resource to update.
        ClientResponse<MultipartInput> res =
                client.read(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got PersonAuthority to update with ID: " + knownResourceId);
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                client.getCommonPartName(), PersonauthoritiesCommon.class);
        Assert.assertNotNull(personAuthority);

        // Update the contents of this resource.
        personAuthority.setDisplayName("updated-" + personAuthority.getDisplayName());
        personAuthority.setVocabType("updated-" + personAuthority.getVocabType());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated PersonAuthority");
            logger.debug(objectAsXmlString(personAuthority, PersonauthoritiesCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(personAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());
        res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        PersonauthoritiesCommon updatedPersonAuthority =
                (PersonauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), PersonauthoritiesCommon.class);
        Assert.assertNotNull(updatedPersonAuthority);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPersonAuthority.getDisplayName(),
                personAuthority.getDisplayName(),
                "Data in updated object did not match submitted data.");
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"update"})
    public void updateItem(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        ClientResponse<MultipartInput> res =
                client.readItem(knownResourceId, knownItemResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got Person to update with ID: " +
                knownItemResourceId +
                " in PersonAuthority: " + knownResourceId );
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);

        // Update the contents of this resource.
        person.setCsid(null);
        person.setForeName("updated-" + person.getForeName());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated Person");
            logger.debug(objectAsXmlString(person,
                PersonsCommon.class));
        }        

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(person, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        PersonsCommon updatedPerson =
                (PersonsCommon) extractPart(input,
                        client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(updatedPerson);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPerson.getForeName(),
                person.getForeName(),
                "Data in updated Person did not match submitted data.");
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"updateItem"})
    public void updateContact(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        ClientResponse<MultipartInput> res =
                client.readContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got Contact to update with ID: " +
                knownContactResourceId +
                " in item: " + knownItemResourceId +
                " in parent: " + knownResourceId );
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        ContactsCommon contact = (ContactsCommon) extractPart(input,
                contactClient.getCommonPartName(), ContactsCommon.class);
        Assert.assertNotNull(contact);

        // Update the contents of this resource.
        contact.setAddressPlace("updated-" + contact.getAddressPlace());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated Contact");
            logger.debug(objectAsXmlString(contact,
                ContactsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(contact, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", contactClient.getCommonPartName());
        res = client.updateContact(knownResourceId, knownItemResourceId, knownContactResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        ContactsCommon updatedContact =
                (ContactsCommon) extractPart(input,
                        contactClient.getCommonPartName(), ContactsCommon.class);
        Assert.assertNotNull(updatedContact);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedContact.getAddressPlace(),
                contact.getAddressPlace(),
                "Data in updated Contact did not match submitted data.");
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

/*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        groups = {"update"}, dependsOnMethods = {"update", "testSubmitRequest"})
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
        groups = {"update"}, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateWithMalformedXml(String testName) throws Exception {

    // Perform setup.
    setupUpdateWithMalformedXml(testName);

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
        groups = {"update"}, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema(String testName) throws Exception {

    // Perform setup.
    setupUpdateWithWrongXmlSchema(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
        logger.debug("updateWithWrongXmlSchema: url=" + url +
            " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
*/

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        // Note: The ID(s) used when creating the request payload may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
    	String displayName = "displayName-NON_EXISTENT_ID";
    	String fullRefName = PersonAuthorityClientUtils.createPersonAuthRefName(displayName, true);
    	MultipartOutput multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    				displayName, fullRefName, client.getCommonPartName());
        ClientResponse<MultipartInput> res =
                client.update(NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"updateItem", "testItemSubmitRequest"})
    public void updateNonExistentItem(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.

        // The only relevant ID may be the one used in update(), below.
        Map<String, String> nonexMap = new HashMap<String,String>();
        nonexMap.put(PersonJAXBSchema.FORE_NAME, "John");
        nonexMap.put(PersonJAXBSchema.SUR_NAME, "Wayne");
        nonexMap.put(PersonJAXBSchema.GENDER, "male");
        MultipartOutput multipart = 
    	PersonAuthorityClientUtils.createPersonInstance(NON_EXISTENT_ID, 
    			PersonAuthorityClientUtils.createPersonRefName(NON_EXISTENT_ID, NON_EXISTENT_ID, true), nonexMap,
    			client.getItemCommonPartName() );
        ClientResponse<MultipartInput> res =
                client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"updateContact", "testContactSubmitRequest"})
    public void updateNonExistentContact(String testName) throws Exception {
        // Currently a no-op test
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes

    // Note: delete sub-resources in ascending hierarchical order,
    // before deleting their parents.

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class, 
        groups = {"delete"}, dependsOnGroups = {"create", "read", "readList", "update"})
    public void deleteContact(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

         if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId +
                " itemcsid = " + knownItemResourceId +
                " csid = " + knownContactResourceId);
        }

        // Submit the request to the service and store the response.
        ClientResponse<Response> res =
            client.deleteContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

   @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteContact"})
    public void deleteItem(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId +
                " itemcsid = " + knownItemResourceId);
        }

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.deleteItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId);
        }

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // Failure outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteContact"})
    public void deleteNonExistentContact(String testName) {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res =
            client.deleteContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
        if(logger.isDebugEnabled()){
            logger.debug("testSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    @Test(dependsOnMethods = {"createItem", "readItem", "testSubmitRequest"})
    public void testItemSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getItemResourceURL(knownResourceId, knownItemResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testItemSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    @Test(dependsOnMethods = {"createContact", "readContact", "testItemSubmitRequest"})
    public void testContactSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getContactResourceURL(knownResourceId,
            knownItemResourceId, knownContactResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testItemSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }


    // ---------------------------------------------------------------
    // Cleanup of resources created during testing
    // ---------------------------------------------------------------
    
    /**
     * Deletes all resources created by tests, after all tests have been run.
     *
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     */

    @AfterClass(alwaysRun=true)
    public void cleanUp() {
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        String parentResourceId;
        String itemResourceId;
        String contactResourceId;
        // Clean up contact resources.
        parentResourceId = knownResourceId;
        for (Map.Entry<String, String> entry : allContactResourceIdsCreated.entrySet()) {
            contactResourceId = entry.getKey();
            itemResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                client.deleteContact(parentResourceId, itemResourceId, contactResourceId);
        }
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allItemResourceIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                client.deleteItem(parentResourceId, itemResourceId);
        }
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId);
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    public String getItemServicePathComponent() {
        return ITEM_SERVICE_PATH_COMPONENT;
    }

    public String getContactServicePathComponent() {
        return CONTACT_SERVICE_PATH_COMPONENT;
    }

    /**
     * Returns the root URL for the item service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning parent, followed by the
     * path component for the items.
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @return The root URL for the item service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getItemServicePathComponent();
    }

    /**
     * Returns the URL of a specific item resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param  itemResourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The URL of a specific item resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String itemResourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + itemResourceIdentifier;
    }


    /**
     * Returns the root URL for the contact service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning authority, followed by the
     * path component for the owning item, followed by the path component
     * for the contact service.
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param  itemResourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The root URL for the contact service.
     */
    protected String getContactServiceRootURL(String parentResourceIdentifier,
        String itemResourceIdentifier) {
        return getItemResourceURL(parentResourceIdentifier, itemResourceIdentifier) + "/" +
                getContactServicePathComponent();
    }

    /**
     * Returns the URL of a specific contact resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent resource of the relevant item resource.
     *
     * @param  resourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The URL of a specific resource managed by a service.
     */
    protected String getContactResourceURL(String parentResourceIdentifier,
        String itemResourceIdentifier, String contactResourceIdentifier) {
        return getContactServiceRootURL(parentResourceIdentifier,
            itemResourceIdentifier) + "/" + contactResourceIdentifier;
    }
}
