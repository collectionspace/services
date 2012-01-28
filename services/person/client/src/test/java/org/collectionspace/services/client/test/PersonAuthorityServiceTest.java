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
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.ContactClientUtils;
import org.collectionspace.services.contact.AddressGroup;
import org.collectionspace.services.contact.AddressGroupList;
import org.collectionspace.services.contact.ContactsCommon;

import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonsCommon;

import org.jboss.resteasy.client.ClientResponse;
//import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
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
public class PersonAuthorityServiceTest extends AbstractAuthorityServiceTest<PersonauthoritiesCommon, PersonsCommon> { //FIXME: Test classes for Vocab, Person, Org, and Location should have a base class!

    /** The logger. */
    private final String CLASS_NAME = PersonAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private final String REFNAME = "refName";
    private final String DISPLAYNAME = "displayName";

    @Override
    public String getServicePathComponent() {
        return PersonAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return PersonAuthorityClient.SERVICE_NAME;
    }

    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }
    /** The test forename. */
    final String TEST_FORE_NAME = "John";
    /** The test middle name. */
    final String TEST_MIDDLE_NAME = null;
    /** The test surname. */
    final String TEST_SUR_NAME = "Wayne";
    /** The test birthdate. */
    final String TEST_BIRTH_DATE = "May 26, 1907";
    /** The test death date. */
    final String TEST_DEATH_DATE = "June 11, 1979";
    //private String knownResourceRefName = null;
    private String knownItemResourceShortIdentifer = null;
    // The resource ID of an item resource used for partial term matching tests.
    private String knownItemPartialTermResourceId = null;
    /** The known contact resource id. */
    private String knownContactResourceId = null;
    /** The all contact resource ids created. */
    private Map<String, String> allContactResourceIdsCreated =
            new HashMap<String, String>();

    protected void setKnownResource(String id, String shortIdentifer,
            String refName) {
        knownResourceId = id;
        knownResourceShortIdentifer = shortIdentifer;
        //knownResourceRefName = refName;
    }

    protected void setKnownItemResource(String id, String shortIdentifer) {
        knownItemResourceId = id;
        knownItemResourceShortIdentifer = shortIdentifer;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new PersonAuthorityClient();
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"create"})
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        String shortId = createIdentifier();
        String displayName = "displayName-" + shortId;
        //String baseRefName = PersonAuthorityClientUtils.createPersonAuthRefName(shortId, null);
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, shortId, client.getCommonPartName());

        String newID = null;
        ClientResponse<Response> res = client.create(multipart);
        try {
        	assertStatusCode(res, testName);
            newID = extractId(res);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
        // Save values for additional tests
        if (knownResourceId == null) {
            setKnownResource(newID, shortId, null ); //baseRefName);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
        PersonAuthorityClient client = new PersonAuthorityClient();
        String displayName = "displayName-" + identifier;
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, identifier, client.getCommonPartName());
        return multipart;
    }

    @Override
    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) {
        String headerLabel = new PersonAuthorityClient().getItemCommonPartName();
        HashMap<String, String> personInfo = new HashMap<String, String>();
        String shortId = "johnWayneTempActor";
        personInfo.put(PersonJAXBSchema.DISPLAY_NAME_COMPUTED, "false");
        personInfo.put(PersonJAXBSchema.DISPLAY_NAME, "John Wayne Temp");
        personInfo.put(PersonJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED, "false");
        personInfo.put(PersonJAXBSchema.SHORT_DISPLAY_NAME, "JohnWayneTemp");
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);

        return PersonAuthorityClientUtils.createPersonInstance(parentCsid, identifier, personInfo, headerLabel);
    }

    /**
     * Creates an item in an authority, using test data.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    @Override
    protected String createItemInAuthority(String vcsid) {

        final String testName = "createItemInAuthority";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":" + vcsid + "...");
        }

        Map<String, String> johnWayneMap = new HashMap<String, String>();
        //
        // Fill the property map
        //
        String shortId = "johnWayneActor";
        johnWayneMap.put(PersonJAXBSchema.DISPLAY_NAME_COMPUTED, "false");
        johnWayneMap.put(PersonJAXBSchema.DISPLAY_NAME, "John Wayne");
        johnWayneMap.put(PersonJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED, "false");
        johnWayneMap.put(PersonJAXBSchema.SHORT_DISPLAY_NAME, "JohnWayne");
        johnWayneMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);

        johnWayneMap.put(PersonJAXBSchema.FORE_NAME, TEST_FORE_NAME);
        johnWayneMap.put(PersonJAXBSchema.SUR_NAME, TEST_SUR_NAME);
        johnWayneMap.put(PersonJAXBSchema.GENDER, "male");
        johnWayneMap.put(PersonJAXBSchema.BIRTH_DATE, TEST_BIRTH_DATE);
        johnWayneMap.put(PersonJAXBSchema.BIRTH_PLACE, "Winterset, Iowa");
        johnWayneMap.put(PersonJAXBSchema.DEATH_DATE, TEST_DEATH_DATE);
        johnWayneMap.put(PersonJAXBSchema.BIO_NOTE, "born Marion Robert Morrison and better"
                + "known by his stage name John Wayne, was an American film actor, director "
                + "and producer. He epitomized rugged masculinity and has become an enduring "
                + "American icon. He is famous for his distinctive voice, walk and height. "
                + "He was also known for his conservative political views and his support in "
                + "the 1950s for anti-communist positions.");

        Map<String, List<String>> johnWayneRepeatablesMap = new HashMap<String, List<String>>();
        List<String> johnWayneGroups = new ArrayList<String>();
        johnWayneGroups.add("Irish");
        johnWayneGroups.add("Scottish");
        johnWayneRepeatablesMap.put(PersonJAXBSchema.GROUPS, johnWayneGroups);

        return createItemInAuthority(vcsid, null /*authRefName*/, shortId, johnWayneMap, johnWayneRepeatablesMap);

    }

    /**
     * Creates an item in an authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @param itemFieldProperties a set of properties specifying the values of fields.
     * @param itemRepeatableFieldProperties a set of properties specifying the values of repeatable fields.
     * @return the string
     */
    private String createItemInAuthority(String vcsid, String authRefName, String shortId,
            Map itemFieldProperties, Map itemRepeatableFieldProperties) {

        final String testName = "createItemInAuthority";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":" + vcsid + "...");
        }

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonInstance(vcsid, null /*authRefName*/, itemFieldProperties,
                itemRepeatableFieldProperties, client.getItemCommonPartName());
        setupCreate();
        ClientResponse<Response> res = client.createItem(vcsid, multipart);
        String newID = null;
        try {
        	assertStatusCode(res, testName);
            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            setKnownItemResource(newID, shortId);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " (created):" + vcsid + "/(" + newID + "," + shortId + ")");
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allResourceItemIdsCreated.put(newID, vcsid);

        return newID;
    }

    /**
     * Creates the contact.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"create"},
    		dependsOnMethods = {"createItem"})
    public void createContact(String testName) {
        setupCreate();
        String newID = createContactInItem(knownResourceId, knownItemResourceId);
    }

    /**
     * Creates the contact in item.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the string
     */
    private String createContactInItem(String parentcsid, String itemcsid) {

        final String testName = "createContactInItem";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":...");
        }
        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = ContactClientUtils.createContactInstance(parentcsid,
                itemcsid, identifier, new ContactClient().getCommonPartName());

        setupCreate();
        ClientResponse<Response> res =
                client.createContact(parentcsid, itemcsid, multipart);
        String newID = null;
        try {
        	assertStatusCode(res, testName);
            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }

        // Store the ID returned from the first contact resource created
        // for additional tests below.
        if (knownContactResourceId == null) {
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

    /**
     * Attempts to create an authority with an short identifier that contains
     * non-word characters.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"create", "nonWordCharsInShortId"})
    public void createWithShortIdNonWordChars(String testName) throws Exception {
        testExpectedStatusCode = STATUS_BAD_REQUEST;
        testRequestType = ServiceRequestType.CREATE;
        testSetup(testExpectedStatusCode, testRequestType);

        // Create the payload to be included in the body of the request
        PersonAuthorityClient client = new PersonAuthorityClient();
        String shortId = createIdentifier() + "*" + createIdentifier();
        String displayName = "displayName-" + shortId;
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, shortId, client.getCommonPartName());

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.create(multipart);

        // Check the status code of the response: does it match
        // the expected response(s)?
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
    }

    /**
     * Attempts to create an item with an short identifier that contains
     * non-word characters.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"create", "nonWordCharsInShortId"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractServiceTestImpl.create"})
    public void createItemWithShortIdNonWordChars(String testName) {
        testExpectedStatusCode = STATUS_BAD_REQUEST;
        testRequestType = ServiceRequestType.CREATE;
        testSetup(testExpectedStatusCode, testRequestType);

        PersonAuthorityClient client = new PersonAuthorityClient();
        // Create the payload to be included in the body of the request
        String shortId = "7-Eleven";
        Map<String, String> fieldProperties = new HashMap<String, String>();
        fieldProperties.put(PersonJAXBSchema.DISPLAY_NAME_COMPUTED, "false");
        fieldProperties.put(PersonJAXBSchema.DISPLAY_NAME, shortId);
        fieldProperties.put(PersonJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED, "false");
        fieldProperties.put(PersonJAXBSchema.SHORT_DISPLAY_NAME, shortId);
        fieldProperties.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        final Map NULL_REPEATABLE_FIELD_PROPERTIES = null;
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonInstance(knownResourceId,
                null /*knownResourceRefName*/, fieldProperties,
                NULL_REPEATABLE_FIELD_PROPERTIES, client.getItemCommonPartName());

        // Send the request and receive a response
        ClientResponse<Response> res = client.createItem(knownResourceId, multipart);
        // Check the status code of the response: does it match
        // the expected response(s)?
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"createList"}, dependsOnGroups = {"create"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < nItemsToCreateInList; i++) {
            create(testName);
        }
    }

    /**
     * Creates the contact list.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"createList"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.createItemList"})
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
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"read"}, dependsOnGroups = {"create"})
    public void read(String testName) throws Exception {
        readInternal(testName, knownResourceId, null);
    }

    /**
     * Read by name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"read"}, dependsOnMethods = {"read"})
    public void readByName(String testName) throws Exception {
        readInternal(testName, null, knownResourceShortIdentifer);
    }

    protected void readInternal(String testName, String CSID, String shortId) {
        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        ClientResponse<String> res = null;
        setupRead();
        if (CSID != null) {
            res = client.read(CSID);
        } else if (shortId != null) {
            res = client.readByName(shortId);
        } else {
            Assert.fail("readInternal: Internal error. One of CSID or shortId must be non-null");
        }
        try {
            assertStatusCode(res, testName);
            //FIXME: remove the following try catch once Aron fixes signatures
            try {
                PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
                PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), PersonauthoritiesCommon.class);
                Assert.assertNotNull(personAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Read named item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem", "readNamedItemInNamedAuth"},
    		dependsOnMethods = {"readItemInNamedAuth"})
    public void readNamedItem(String testName) throws Exception {
        readItemInternal(testName, knownResourceId, null, null, knownItemResourceShortIdentifer);
    }

    /**
     * Read item in Named Auth.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readItem"})
    public void readItemInNamedAuth(String testName) throws Exception {
        readItemInternal(testName, null, knownResourceShortIdentifer, knownItemResourceId, null);
    }

    
    /**
     * Read Named item in Named Auth.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readNamedItem"})
    public void readNamedItemInNamedAuth(String testName) throws Exception {
        readItemInternal(testName, null, knownResourceShortIdentifer, null, knownItemResourceShortIdentifer);
    }

    protected void readItemInternal(String testName,
            String authCSID, String authShortId, String itemCSID, String itemShortId)
            throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Reading:" + ((authCSID != null) ? authCSID : authShortId) + "/"
                    + ((itemCSID != null) ? authCSID : itemShortId));
        }

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        setupRead();
        ClientResponse<String> res = null;
        if (authCSID != null) {
            if (itemCSID != null) {
                res = client.readItem(authCSID, itemCSID);
            } else if (itemShortId != null) {
                res = client.readNamedItem(authCSID, itemShortId);
            } else {
                Assert.fail("readInternal: Internal error. One of CSID or shortId must be non-null");
            }
        } else if (authShortId != null) {
            if (itemCSID != null) {
                res = client.readItemInNamedAuthority(authShortId, itemCSID);
            } else if (itemShortId != null) {
                res = client.readNamedItemInNamedAuthority(authShortId, itemShortId);
            } else {
                Assert.fail("readInternal: Internal error. One of CSID or shortId must be non-null");
            }
        } else {
            Assert.fail("readInternal: Internal error. One of authCSID or authShortId must be non-null");
        }
        try {
            assertStatusCode(res, testName);
            // Check whether we've received a person.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            PersonsCommon person = (PersonsCommon) extractPart(input,
                    client.getItemCommonPartName(), PersonsCommon.class);
            Assert.assertNotNull(person);
            boolean showFull = true;
            if (showFull && logger.isDebugEnabled()) {
                logger.debug(testName + ": returned payload:");
                logger.debug(objectAsXmlString(person, PersonsCommon.class));
            }

            // Check that the person item is within the expected Person Authority.
            Assert.assertEquals(person.getInAuthority(), knownResourceId);

            // Verify the number and contents of values in a repeatable field,
            // as created in the instance record used for testing.
            List<String> groups = person.getGroups().getGroup();
            Assert.assertTrue(groups.size() > 0);
            Assert.assertNotNull(groups.get(0));
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Verify item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        PoxPayloadIn input = null;
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
            assertStatusCode(res, testName);
            // Check whether person has expected displayName.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);
        // Check whether person has expected displayName.
        // Make sure displayName matches computed form
        String displayName = person.getDisplayName();
        String expectedDisplayName =
                PersonAuthorityClientUtils.prepareDefaultDisplayName(
                TEST_FORE_NAME, null, TEST_SUR_NAME,
                TEST_BIRTH_DATE, TEST_DEATH_DATE);
        Assert.assertFalse(displayName.equals(expectedDisplayName));

        // Make sure short displayName matches computed form
        String shortDisplayName = person.getShortDisplayName();
        String expectedShortDisplayName =
                PersonAuthorityClientUtils.prepareDefaultDisplayName(
                TEST_FORE_NAME, null, TEST_SUR_NAME, null, null);
        Assert.assertFalse(expectedShortDisplayName.equals(shortDisplayName));

        // Update the forename and verify the computed name is updated.
        person.setCsid(null);
        person.setDisplayNameComputed(true);
        person.setShortDisplayNameComputed(true);
        person.setForeName("updated-" + TEST_FORE_NAME);
        expectedDisplayName =
                PersonAuthorityClientUtils.prepareDefaultDisplayName(
                "updated-" + TEST_FORE_NAME, null, TEST_SUR_NAME,
                TEST_BIRTH_DATE, TEST_DEATH_DATE);
        expectedShortDisplayName =
                PersonAuthorityClientUtils.prepareDefaultDisplayName(
                "updated-" + TEST_FORE_NAME, null, TEST_SUR_NAME, null, null);

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PersonAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), person);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

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
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedPerson.getShortDisplayName(), expectedShortDisplayName,
                "Updated ForeName in Person not reflected in computed ShortDisplayName.");

        // Now Update the displayName, not computed and verify the computed name is overriden.
        person.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        person.setDisplayName(expectedDisplayName);
        person.setShortDisplayNameComputed(false);
        person.setShortDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(PersonAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(client.getItemCommonPartName(), person);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

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
        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPerson.isShortDisplayNameComputed(), false,
                "Updated shortDisplayNameComputed in Person did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedPerson.getShortDisplayName(),
                expectedDisplayName,
                "Updated ShortDisplayName (not computed) in Person not stored.");
    }

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"},
    		dependsOnMethods = {"verifyItemDisplayName"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        // Perform setup for read.
        setupRead();

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        PoxPayloadIn input = null;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Make an invalid UPDATE request -i.e., if we don't want the display name computed, then
        // we need to supply one.
        //
        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);
        // Try to Update with computed false and no displayName
        person.setDisplayNameComputed(false);
        person.setDisplayName(null);

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PersonAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), person);
        setupUpdateWithInvalidBody();
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Read contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"createContact", "org.collectionspace.services.client.test.AbstractAuthorityServiceTest.readItem"})
    public void readContact(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        PoxPayloadIn input = null;
        ClientResponse<String> res =
                client.readContact(knownResourceId, knownItemResourceId,
                knownContactResourceId);
        try {
            assertStatusCode(res, testName);
            // Check whether we've received a contact.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        ContactsCommon contact = (ContactsCommon) extractPart(input,
                new ContactClient().getCommonPartName(), ContactsCommon.class);
        Assert.assertNotNull(contact);
        boolean showFull = true;
        if (showFull && logger.isDebugEnabled()) {
            logger.debug(testName + ": returned payload:");
            logger.debug(objectAsXmlString(contact, ContactsCommon.class));
        }
        Assert.assertEquals(contact.getInAuthority(), knownResourceId);
        Assert.assertEquals(contact.getInItem(), knownItemResourceId);

    }

    /**
     * Read contact non existent.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readContact"})
    public void readContactNonExistent(String testName) {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        ClientResponse<String> res =
                client.readContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------

    /**
     * Read item list.
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList(String testName) {
        readItemList(knownAuthorityWithItems, null, testName);
    }

    /**
     * Read item list by authority name.
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"readList"}, dependsOnMethods = {"readItemList"})
    public void readItemListByName(String testName) {
        readItemList(null, READITEMS_SHORT_IDENTIFIER, testName);
    }

    /**
     * Read item list.
     *
     * @param vcsid the vcsid
     * @param name the name
     */
    private void readItemList(String vcsid, String name, String testName) {
        setupReadList();
        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        ClientResponse<AbstractCommonList> res = null;
        if (vcsid != null) {
            res = client.readItemList(vcsid, null, null);
        } else if (name != null) {
            res = client.readItemListForNamedAuthority(name, null, null);
        } else {
            Assert.fail("readItemList passed null csid and name!");
        }
        AbstractCommonList list = null;
        try {
            assertStatusCode(res, testName);
            list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        List<AbstractCommonList.ListItem> items = list.getListItem();
        int nItemsReturned = items.size();
        // There will be 'nItemsToCreateInList'
        // items created by the createItemList test,
        // all associated with the same parent resource.
        int nExpectedItems = nItemsToCreateInList;
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Expected "
                    + nExpectedItems + " items; got: " + nItemsReturned);
        }
        Assert.assertEquals(nItemsReturned, nExpectedItems);

        for (AbstractCommonList.ListItem item : items) {
            String value =
                    AbstractCommonListUtils.ListItemGetElementValue(item, REFNAME);
            Assert.assertTrue((null != value), "Item refName is null!");
            value =
                    AbstractCommonListUtils.ListItemGetElementValue(item, DISPLAYNAME);
            Assert.assertTrue((null != value), "Item displayName is null!");
        }
        if (logger.isTraceEnabled()) {
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    /**
     * Read contact list.
     */
    @Test(groups = {"readList"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.readItemList"})
    public void readContactList() {
        readContactList(knownResourceId, knownItemResourceId);
    }

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     */
    private void readContactList(String parentcsid, String itemcsid) {
        final String testName = "readContactList";

        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        AbstractCommonList list = null;
        ClientResponse<AbstractCommonList> res =
                client.readContactList(parentcsid, itemcsid);
        try {
            assertStatusCode(res, testName);
            list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        List<AbstractCommonList.ListItem> listitems =
                list.getListItem();
        int nItemsReturned = listitems.size();
        // There will be one item created, associated with a
        // known parent resource, by the createItem test.
        //
        // In addition, there will be 'nItemsToCreateInList'
        // additional items created by the createItemList test,
        // all associated with the same parent resource.
        int nExpectedItems = nItemsToCreateInList + 1;
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Expected "
                    + nExpectedItems + " items; got: " + nItemsReturned);
        }
        Assert.assertEquals(nItemsReturned, nExpectedItems);
        
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    // Failure outcomes
    // There are no failure outcome tests at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#update(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"update"}, dependsOnGroups = {"readItem", "readList"})
    public void update(String testName) throws Exception {
        // Retrieve the contents of a resource to update.
        PersonAuthorityClient client = new PersonAuthorityClient();
        PoxPayloadIn input = null;
        setupRead();
        ClientResponse<String> res = client.read(knownResourceId);
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got PersonAuthority to update with ID: " + knownResourceId);
            }
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                client.getCommonPartName(), PersonauthoritiesCommon.class);
        Assert.assertNotNull(personAuthority);

        // Update the contents of this resource.
        personAuthority.setDisplayName("updated-" + personAuthority.getDisplayName());
        personAuthority.setVocabType("updated-" + personAuthority.getVocabType());
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated PersonAuthority");
            logger.debug(objectAsXmlString(personAuthority, PersonauthoritiesCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PersonAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getCommonPartName(), personAuthority);
        setupUpdate();
        res = client.update(knownResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        PersonauthoritiesCommon updatedPersonAuthority =
                (PersonauthoritiesCommon) extractPart(input,
                client.getCommonPartName(), PersonauthoritiesCommon.class);
        Assert.assertNotNull(updatedPersonAuthority);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPersonAuthority.getDisplayName(),
                personAuthority.getDisplayName(),
                "Data in updated object did not match submitted data.");
    }

    /**
     * Update item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"update"}, dependsOnMethods = {"update"})
    public void updateItem(String testName) throws Exception {
        // Retrieve the contents of a resource to update.
        PersonAuthorityClient client = new PersonAuthorityClient();
        PoxPayloadIn input = null;
        setupRead();
        ClientResponse<String> res =
                client.readItem(knownResourceId, knownItemResourceId);
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got Person to update with ID: "
                        + knownItemResourceId
                        + " in PersonAuthority: " + knownResourceId);
            }
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);

        if (logger.isDebugEnabled() == true) {
            logger.debug("About to update the following person...");
            logger.debug(objectAsXmlString(person, PersonsCommon.class));
        }

        // Update the contents of this resource.
        person.setCsid(null);
        person.setForeName("updated-" + person.getForeName());
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated Person");
            logger.debug(objectAsXmlString(person,
                    PersonsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PersonAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), person);
        setupUpdate();
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        PersonsCommon updatedPerson =
                (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(updatedPerson);

        if (logger.isDebugEnabled() == true) {
            logger.debug("Updated to following person to:");
            logger.debug(objectAsXmlString(updatedPerson, PersonsCommon.class));
        }

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPerson.getForeName(),
                person.getForeName(),
                "Data in updated Person did not match submitted data.");
    }

    /**
     * Update contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"},
    		dependsOnMethods = {"readContact", "testContactSubmitRequest",
    			"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.updateItem"})
    public void updateContact(String testName) throws Exception {
    	String contactsCommonLabel = new ContactClient().getCommonPartName();

        // Retrieve the contents of a resource to update.
        PersonAuthorityClient client = new PersonAuthorityClient();
        PoxPayloadIn input = null;
        setupRead();
        ClientResponse<String> res =
                client.readContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got Contact to update with ID: "
                        + knownContactResourceId
                        + " in item: " + knownItemResourceId
                        + " in parent: " + knownResourceId);
            }
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        ContactsCommon contact = (ContactsCommon) extractPart(input,
                contactsCommonLabel, ContactsCommon.class);
        Assert.assertNotNull(contact);

        // Verify the contents of this resource
        AddressGroupList addressGroupList = contact.getAddressGroupList();
        Assert.assertNotNull(addressGroupList);
        List<AddressGroup> addressGroups = addressGroupList.getAddressGroup();
        Assert.assertNotNull(addressGroups);
        Assert.assertTrue(addressGroups.size() > 0);
        String addressPlace1 = addressGroups.get(0).getAddressPlace1();
        Assert.assertNotNull(addressPlace1);

        // Update the contents of this resource.
        addressGroups.get(0).setAddressPlace1("updated-" + addressPlace1);
        contact.setAddressGroupList(addressGroupList);
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated Contact");
            logger.debug(objectAsXmlString(contact,
                    ContactsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(ContactClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(contactsCommonLabel, contact);
        setupUpdate();
        res = client.updateContact(knownResourceId, knownItemResourceId, knownContactResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());;
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        ContactsCommon updatedContact = (ContactsCommon) extractPart(input,
                contactsCommonLabel, ContactsCommon.class);
        Assert.assertNotNull(updatedContact);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedContact.getAddressGroupList().getAddressGroup().get(0).getAddressPlace1(),
                contact.getAddressGroupList().getAddressGroup().get(0).getAddressPlace1(),
                "Data in updated object did not match submitted data.");
    }

    /**
     * Update non existent contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"},
    		dependsOnMethods = {"updateContact", "testContactSubmitRequest"})
    public void updateNonExistentContact(String testName) throws Exception {
        // Currently a no-op test
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    // Note: delete sub-resources in ascending hierarchical order,
    // before deleting their parents.
    /**
     * Delete contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"delete"},
    		dependsOnMethods = {"updateContact"})
    public void deleteContact(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("parentcsid =" + knownResourceId
                    + " itemcsid = " + knownItemResourceId
                    + " csid = " + knownContactResourceId);
        }

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        setupDelete();
        ClientResponse<Response> res =
                client.deleteContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
    }

    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing.  See localDelete().  This ensure proper test order.
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"localDeleteItem"})    
    public void localDelete(String testName) throws Exception {
    	super.delete(testName);
    }

    @Override
    public void deleteItem(String testName) throws Exception {
    	// Do nothing.  We need to wait until after the test "localDelete" gets run.  When it does,
    	// its dependencies will get run first and then we can call the base class' delete method.
    }
    
    @Test(dataProvider = "testName", groups = {"delete"},
    	dependsOnMethods = {"verifyIllegalItemDisplayName", "testContactSubmitRequest", "deleteContact"})
    public void localDeleteItem(String testName) throws Exception {
    	super.deleteItem(testName);
    }

    /**
     * Delete non existent contact.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"delete"},
    		dependsOnMethods = {"deleteContact"})
    public void deleteNonExistentContact(String testName) {
        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        setupDeleteNonExistent();
        ClientResponse<Response> res =
                client.deleteContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
    }

    /**
     * Test contact submit request.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createContact", "readContact", "testItemSubmitRequest"})
    public void testContactSubmitRequest(String testName) {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getContactResourceURL(knownResourceId,
                knownItemResourceId, knownContactResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testContactSubmitRequest: url=" + url
                    + " status=" + statusCode);
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
    @AfterClass(alwaysRun = true)
    @Override
    public void cleanUp() {
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        String parentResourceId;
        String itemResourceId;
        String contactResourceId;
        // Clean up contact resources.
        PersonAuthorityClient client = new PersonAuthorityClient();
        parentResourceId = knownResourceId;
        for (Map.Entry<String, String> entry : allContactResourceIdsCreated.entrySet()) {
            contactResourceId = entry.getKey();
            itemResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                    client.deleteContact(parentResourceId, itemResourceId, contactResourceId);
            res.releaseConnection();
        }
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                    client.deleteItem(parentResourceId, itemResourceId);
            res.releaseConnection();
        }
        // Clean up parent resources.
        super.cleanUp();
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * Gets the contact service path component.
     *
     * @return the contact service path component
     */
    public String getContactServicePathComponent() {
        return ContactClient.SERVICE_PATH_COMPONENT;
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
        return getItemResourceURL(parentResourceIdentifier, itemResourceIdentifier) + "/"
                + getContactServicePathComponent();
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

	@Override
	public void authorityTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PersonsCommon updateItemInstance(PersonsCommon authorityItem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedItemInstances(PersonsCommon original,
			PersonsCommon updated) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        PoxPayloadOut result =
                PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, shortId, commonPartName);
        return result;
	}
	
	@Override
	protected PoxPayloadOut createNonExistenceInstance(String commonPartName,
			String identifier) {
        String displayName = "displayName-NON_EXISTENT_ID";
        PoxPayloadOut result = PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, "NON_EXISTENT_SHORT_ID", commonPartName);
        return result;
	}
	
    protected PoxPayloadOut createNonExistenceItemInstance(String commonPartName, String identifier) {
        Map<String, String> nonexMap = new HashMap<String, String>();
        nonexMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, "nonEX");
        nonexMap.put(PersonJAXBSchema.FORE_NAME, "John");
        nonexMap.put(PersonJAXBSchema.SUR_NAME, "Wayne");
        nonexMap.put(PersonJAXBSchema.GENDER, "male");
        Map<String, List<String>> nonexRepeatablesMap = new HashMap<String, List<String>>();
        PoxPayloadOut result =
                PersonAuthorityClientUtils.createPersonInstance(NON_EXISTENT_ID,
                null, //PersonAuthorityClientUtils.createPersonAuthRefName(NON_EXISTENT_ID, null),
                nonexMap, nonexRepeatablesMap, commonPartName);
        return result;
    }
	

	@Override
	protected PersonauthoritiesCommon updateInstance(
			PersonauthoritiesCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(PersonauthoritiesCommon original,
			PersonauthoritiesCommon updated) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void verifyReadItemInstance(PersonsCommon item) throws Exception {
		// Do nothing for now.  Add more 'read' validation checks here if applicable.
	}
}
