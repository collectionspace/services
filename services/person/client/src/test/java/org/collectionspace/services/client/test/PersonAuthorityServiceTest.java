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
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.ContactClientUtils;
import org.collectionspace.services.contact.AddressGroup;
import org.collectionspace.services.contact.AddressGroupList;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonTermGroup;
import org.collectionspace.services.person.PersonTermGroupList;
import org.collectionspace.services.person.PersonsCommon;
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
    
    /**
     * Default constructor.  Used to set the short ID for all tests authority items
     */
    public PersonAuthorityServiceTest() {
    	super();
    	TEST_SHORTID = "johnWayneActor";
    }
    
    @Override
	protected String getTestAuthorityItemShortId() {
		return getTestAuthorityItemShortId(true); // The short ID of every person item we create should be unique
	}

    @Override
    public String getServicePathComponent() {
        return PersonClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return PersonClient.SERVICE_NAME;
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
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new PersonClient();
    }
    
	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new PersonClient(clientPropertiesFilename);
	}

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

	/* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        String shortId = createIdentifier();
        String displayName = "displayName-" + shortId;
        //String baseRefName = PersonAuthorityClientUtils.createPersonAuthRefName(shortId, null);
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, shortId, client.getCommonPartName());
        // Extract the short ID since it might have been randomized by the createPersonAuthRefName() method
        PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(multipart,
                client.getCommonPartName(), PersonauthoritiesCommon.class);
        shortId = personAuthority.getShortIdentifier();

        String newID = null;
        Response res = client.create(multipart);
        try {
        	assertStatusCode(res, testName);
            newID = extractId(res);
        } finally {
        	if (res != null) {
        		res.close();
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
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        PersonClient client = new PersonClient();
        
        String displayName = "displayName-" + identifier;
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, identifier, client.getCommonPartName());
        
        return multipart;
    }

    @Override
    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) throws Exception {
        String headerLabel = new PersonClient().getItemCommonPartName();
        
        HashMap<String, String> personInfo = new HashMap<String, String>();
        String shortId = "MarkTwainAuthor" + identifier;
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        
        List<PersonTermGroup> terms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        term.setTermDisplayName("Mark Twain Primary");
        term.setTermName("MarkTwainPrimary");
        terms.add(term);
        
        term = new PersonTermGroup();
        term.setTermDisplayName("Samuel Langhorne Clemens");
        term.setTermName("SamuelLanghorneClemens");
        terms.add(term);        
        
        term = new PersonTermGroup();
        term.setTermDisplayName("Sam Clemens");
        term.setTermName("SamClemens");
        terms.add(term);   
        
        term = new PersonTermGroup();
        term.setTermDisplayName("Huck Fin");
        term.setTermName("Huck Fin");
        terms.add(term);           
        
        return PersonAuthorityClientUtils.createPersonInstance(parentCsid, identifier, personInfo, terms, headerLabel);
    }

    
    /**
     * Creates an item in an authority, using test data.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     * @throws Exception 
     */
    @Override
    protected String createItemInAuthority(AuthorityClient client, String vcsid, String shortId) throws Exception {

        final String testName = "createItemInAuthority";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":" + vcsid + "...");
        }

        Map<String, String> johnWayneMap = new HashMap<String, String>();
        //
        // Fill the property map
        //
        johnWayneMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
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

        List<PersonTermGroup> johnWayneTerms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        term.setTermDisplayName("John Wayne DisplayName");
        term.setTermName("John Wayne");
        term.setForeName(TEST_FORE_NAME);
        term.setSurName(TEST_SUR_NAME);
        johnWayneTerms.add(term);

        Map<String, List<String>> johnWayneRepeatablesMap = new HashMap<String, List<String>>();
        List<String> johnWayneGroups = new ArrayList<String>();
        johnWayneGroups.add("Irish");
        johnWayneGroups.add("Scottish");
        johnWayneRepeatablesMap.put(PersonJAXBSchema.GROUPS, johnWayneGroups);

        return createItemInAuthority(client, vcsid, null /*authRefName*/, shortId, johnWayneMap, johnWayneTerms, johnWayneRepeatablesMap);

    }

    /**
     * Creates an item in an authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @param itemFieldProperties a set of properties specifying the values of fields.
     * @param itemRepeatableFieldProperties a set of properties specifying the values of repeatable fields.
     * @return the string
     * @throws Exception 
     */
    private String createItemInAuthority(AuthorityClient client, String vcsid, String authRefName, String shortId,
            Map<String, String> itemFieldProperties, List<PersonTermGroup> terms, Map<String, List<String>> itemRepeatableFieldProperties) throws Exception {

        final String testName = "createItemInAuthority";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":" + vcsid + "...");
        }

        // Submit the request to the service and store the response.
        if (client == null) {
        	client = new PersonClient();
        }
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonInstance(vcsid, null /*authRefName*/, itemFieldProperties,
                terms, itemRepeatableFieldProperties, client.getItemCommonPartName());
        setupCreate();
        Response res = client.createItem(vcsid, multipart);
        String newID = null;
        try {
        	assertStatusCode(res, testName);
            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
        		res.close();
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

    /*
     * This override asks for a unique identifier (short ID in the case of authority tests).
     * 
     * (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createResource(java.lang.String, java.lang.String)
     */
    @Override
    protected String createResource(String testName, String identifier) throws Exception {
        String result = null;
        
    	CollectionSpaceClient client = this.getClientInstance();
        result = createResource(client, testName, identifier, true);
    	
    	return result;
    }
    
    /**
     * Creates the contact.
     *
     * @param testName the test name
     * @throws Exception 
     */
    @Test(dataProvider = "testName", groups = {"create"}, dependsOnMethods = {"createItem"})
    public void createContact(String testName) throws Exception {
        setupCreate();
        createContactInItem(knownResourceId, knownItemResourceId);
    }

    /**
     * Creates the contact in item.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the string
     * @throws Exception 
     */
    private String createContactInItem(String parentcsid, String itemcsid) throws Exception {

        final String testName = "createContactInItem";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":...");
        }
        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = ContactClientUtils.createContactInstance(parentcsid,
                itemcsid, identifier, new ContactClient().getCommonPartName());

        setupCreate();
        Response res = client.createContact(parentcsid, itemcsid, multipart);
        String newID = null;
        try {
        	assertStatusCode(res, testName);
            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
        		res.close();
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
        PersonClient client = new PersonClient();
        String shortId = createIdentifier() + "*" + createIdentifier();
        String displayName = "displayName-" + shortId;
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonAuthorityInstance(
                displayName, shortId, client.getCommonPartName());

        // Submit the request to the service and store the response.
        Response res = client.create(multipart);
        // Check the status code of the response: does it match
        // the expected response(s)?  We expect failure here.
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.close();
        	}
        }
    }

    /**
     * Attempts to create an item with an short identifier that contains
     * non-word characters.
     *
     * @param testName the test name
     * @throws Exception 
     */
    @Test(dataProvider = "testName", groups = {"create", "nonWordCharsInShortId"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractServiceTestImpl.create"})
    public void createItemWithShortIdNonWordChars(String testName) throws Exception {
        testExpectedStatusCode = STATUS_BAD_REQUEST;
        testRequestType = ServiceRequestType.CREATE;
        testSetup(testExpectedStatusCode, testRequestType);

        PersonClient client = new PersonClient();
        // Create the payload to be included in the body of the request
        String shortId = "7-Eleven";
        Map<String, String> fieldProperties = new HashMap<String, String>();
        fieldProperties.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        
        List<PersonTermGroup> terms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        term.setTermDisplayName(shortId);
        term.setTermName(shortId);
        terms.add(term);
        
        final Map<String, List<String>> NULL_REPEATABLE_FIELD_PROPERTIES = null;
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonInstance(knownResourceId,
                null /*knownResourceRefName*/, fieldProperties, terms,
                NULL_REPEATABLE_FIELD_PROPERTIES, client.getItemCommonPartName());

        // Send the request and receive a response
        Response res = client.createItem(knownResourceId, multipart);
        // Check the status code of the response: does it match
        // the expected response(s)?  We expect failure here, so there will be no
        // new ID to keep track of for later cleanup.
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.close();
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
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.createItemList",
    			"deleteContact"})
    public void createContactList(String testName) throws Exception {
        // Add contacts to the initially-created, known item record.
    	for (int j = 0; j < 1; j++) { // As of CollectionSpace 7.1, one contact item only can be associated with an authority resource
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

    protected void readInternal(String testName, String CSID, String shortId) throws Exception {
        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        Response res = null;
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
                PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
                PersonauthoritiesCommon personAuthority = (PersonauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), PersonauthoritiesCommon.class);
                Assert.assertNotNull(personAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
        	if (res != null) {
                res.close();
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
        PersonClient client = new PersonClient();
        setupRead();
        Response res = null;
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
            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
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
                res.close();
            }
        }
    }
    
    // Note: This test depends on server-side validation logic to require
    // a non-null (and potentially, non-empty) displayname for each term,
    // and will fail if that validation is not present.

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        // Perform setup for read.
        setupRead();

        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        Response res = client.readItem(knownResourceId, knownItemResourceId);
        PoxPayloadIn input = null;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        //
        // Make an invalid UPDATE request, without a display name
        //
        PersonsCommon person = (PersonsCommon) extractPart(input,
                client.getItemCommonPartName(), PersonsCommon.class);
        Assert.assertNotNull(person);
        // Try to Update with no displayName
        PersonTermGroupList termList = person.getPersonTermGroupList();
        Assert.assertNotNull(termList);
        List<PersonTermGroup> terms = termList.getPersonTermGroup();
        Assert.assertNotNull(terms);
        Assert.assertTrue(terms.size() > 0);
        terms.get(0).setTermDisplayName(null);
        terms.get(0).setTermName(null);

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PersonClient.SERVICE_ITEM_PAYLOAD_NAME);
        output.addPart(client.getItemCommonPartName(), person);
        setupUpdateWithInvalidBody();
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.close();
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
        PersonClient client = new PersonClient();
        PoxPayloadIn input = null;
        Response res = client.readContact(knownResourceId, knownItemResourceId,
                knownContactResourceId);
        try {
            assertStatusCode(res, testName);
            // Check whether we've received a contact.
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
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
     * @throws Exception 
     */
    @Test(dataProvider = "testName", groups = {"readItem"}, dependsOnMethods = {"readContact"})
    public void readContactNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        Response res = client.readContact(knownResourceId, knownItemResourceId,
        		NON_EXISTENT_ID);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.close();
            }
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------

    /**
     * Read contact list.
     * @throws Exception 
     */
    @Test(groups = {"readList"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.readItemList"})
    public void readContactList() throws Exception {
        readContactList(knownResourceId, knownItemResourceId);
    }

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @throws Exception 
     */
    private void readContactList(String parentcsid, String itemcsid) throws Exception {
        final String testName = "readContactList";

        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        AbstractCommonList list = null;
        Response res = client.readContactList(parentcsid, itemcsid);
        try {
            assertStatusCode(res, testName);
            list = res.readEntity(AbstractCommonList.class);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        List<AbstractCommonList.ListItem> listitems =
                list.getListItem();
        int nItemsReturned = listitems.size();
        int nExpectedItems = 1;
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
        PersonClient client = new PersonClient();
        PoxPayloadIn input = null;
        setupRead();
        Response res = client.read(knownResourceId);
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got PersonAuthority to update with ID: " + knownResourceId);
            }
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
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
        PoxPayloadOut output = new PoxPayloadOut(PersonClient.SERVICE_PAYLOAD_NAME);
        output.addPart(client.getCommonPartName(), personAuthority);
        setupUpdate();
        res = client.update(knownResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
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
     * Update item override -see immediate superclass.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Override
    public void updateItem(String testName) throws Exception {
        // Retrieve the contents of a resource to update.
        PersonClient client = new PersonClient();
        PoxPayloadIn input = null;
        setupRead();
        Response res = client.readItem(knownResourceId, knownItemResourceId);
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got Person to update with ID: "
                        + knownItemResourceId
                        + " in PersonAuthority: " + knownResourceId);
            }
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
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
        PersonTermGroupList termList = person.getPersonTermGroupList();
        Assert.assertNotNull(termList);
        List<PersonTermGroup> terms = termList.getPersonTermGroup();
        Assert.assertNotNull(terms);
        Assert.assertTrue(terms.size() > 0);
        String foreName = terms.get(0).getForeName();
        String updatedForeName = "updated-" + foreName;
        terms.get(0).setForeName(updatedForeName);
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated Person");
            logger.debug(objectAsXmlString(person,
                    PersonsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PersonClient.SERVICE_ITEM_PAYLOAD_NAME);
        output.addPart(client.getItemCommonPartName(), person);
        setupUpdate();
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
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
        PersonTermGroupList updatedTermList = person.getPersonTermGroupList();
        Assert.assertNotNull(updatedTermList);
        List<PersonTermGroup> updatedTerms = termList.getPersonTermGroup();
        Assert.assertNotNull(updatedTerms);
        Assert.assertEquals(updatedTerms.get(0).getForeName(), updatedForeName,
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
        PersonClient client = new PersonClient();
        PoxPayloadIn input = null;
        setupRead();
        Response res = client.readContact(knownResourceId, knownItemResourceId,
        		knownContactResourceId);
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got Contact to update with ID: "
                        + knownContactResourceId
                        + " in item: " + knownItemResourceId
                        + " in parent: " + knownResourceId);
            }
            input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	if (res != null) {
                res.close();
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
        output.addPart(contactsCommonLabel, contact);
        setupUpdate();
        res = client.updateContact(knownResourceId, knownItemResourceId, knownContactResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.readEntity(String.class));;
        } finally {
        	if (res != null) {
                res.close();
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
        PersonClient client = new PersonClient();
        setupDelete();
        Response res = client.deleteContact(knownResourceId, knownItemResourceId, 
        		knownContactResourceId);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.close();
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
     * @throws Exception 
     */
    @Test(dataProvider = "testName", groups = {"delete"},
    		dependsOnMethods = {"deleteContact"})
    public void deleteNonExistentContact(String testName) throws Exception {
        // Submit the request to the service and store the response.
        PersonClient client = new PersonClient();
        setupDeleteNonExistent();
        Response res = client.deleteContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
        		res.close();
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
     * @throws Exception 
     */
    @AfterClass(alwaysRun = true)
    @Override
    public void cleanUp() throws Exception {
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
        PersonClient client = new PersonClient();
        parentResourceId = this.getKnowResourceId();
        for (Map.Entry<String, String> entry : allContactResourceIdsCreated.entrySet()) {
            contactResourceId = entry.getKey();
            itemResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            Response res = client.deleteContact(parentResourceId, itemResourceId,
            		contactResourceId);
            res.close();
        }
        //
        // Finally, clean call our superclass' cleanUp method.
        //
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
	protected PersonsCommon updateItemInstance(PersonsCommon personsCommon) {
                            
            PersonTermGroupList termList = personsCommon.getPersonTermGroupList();
            Assert.assertNotNull(termList);
            List<PersonTermGroup> terms = termList.getPersonTermGroup();
            Assert.assertNotNull(terms);
            Assert.assertTrue(terms.size() > 0);
            terms.get(0).setTermDisplayName("updated-" + terms.get(0).getTermDisplayName());
            terms.get(0).setTermName("updated-" + terms.get(0).getTermName());
	    personsCommon.setPersonTermGroupList(termList);

            return personsCommon;
	}

	@Override
	protected void compareUpdatedItemInstances(PersonsCommon original,
			PersonsCommon updated,
			boolean compareRevNumbers) throws Exception {
            
            PersonTermGroupList originalTermList = original.getPersonTermGroupList();
            Assert.assertNotNull(originalTermList);
            List<PersonTermGroup> originalTerms = originalTermList.getPersonTermGroup();
            Assert.assertNotNull(originalTerms);
            Assert.assertTrue(originalTerms.size() > 0);
            
            PersonTermGroupList updatedTermList = updated.getPersonTermGroupList();
            Assert.assertNotNull(updatedTermList);
            List<PersonTermGroup> updatedTerms = updatedTermList.getPersonTermGroup();
            Assert.assertNotNull(updatedTerms);
            Assert.assertTrue(updatedTerms.size() > 0);
            
            Assert.assertEquals(updatedTerms.get(0).getTermDisplayName(),
                originalTerms.get(0).getTermDisplayName(),
                "Value in updated record did not match submitted data.");
            
            if (compareRevNumbers == true) {
            	Assert.assertEquals(original.getRev(), updated.getRev(), "Revision numbers should match.");
            }
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
        nonexMap.put(PersonJAXBSchema.GENDER, "male");
        
        List<PersonTermGroup> terms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        term.setTermDisplayName("John Wayne");
        term.setTermName("John Wayne");
        term.setForeName("John");
        term.setSurName("Wayne");
        terms.add(term);
        
        Map<String, List<String>> nonexRepeatablesMap = new HashMap<String, List<String>>();
        PoxPayloadOut result =
                PersonAuthorityClientUtils.createPersonInstance(NON_EXISTENT_ID,
                null, //PersonAuthorityClientUtils.createPersonAuthRefName(NON_EXISTENT_ID, null),
                nonexMap, terms, nonexRepeatablesMap, commonPartName);
        return result;
    }
	        
    @Override
	protected PersonauthoritiesCommon updateInstance(PersonauthoritiesCommon personauthoritiesCommon) {
		PersonauthoritiesCommon result = new PersonauthoritiesCommon();
		
        result.setDisplayName("updated-" + personauthoritiesCommon.getDisplayName());
        result.setVocabType("updated-" + personauthoritiesCommon.getVocabType());
        
		return result;
	}

	@Override
	protected void compareUpdatedInstances(PersonauthoritiesCommon original,
			PersonauthoritiesCommon updated) throws Exception {
        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
	}

	@Override
	protected void verifyReadItemInstance(PersonsCommon item) throws Exception {
		// Do nothing for now.  Add more 'read' validation checks here if applicable.
	}        
}
