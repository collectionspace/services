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

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.person.PersonsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * PersonAuthoritySearchTest, carries out search (e.g. partial
 * term matching) tests against a deployed and running PersonAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class PersonAuthoritySearchTest extends BaseServiceTest {

    private final String CLASS_NAME = PersonAuthoritySearchTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    /** The service path component. */
    final String SERVICE_PATH_COMPONENT = "personauthorities";
    
    // Test name for partial term matching: Lech Walesa
    //
    // Forename
    final String TEST_PARTIAL_TERM_FORE_NAME = "Lech";
    //
    // Surname (contains two non-USASCII range Unicode UTF-8 characters)
    final String TEST_PARTIAL_TERM_SUR_NAME = "Wa" + "\u0142" + "\u0119" + "sa";
    //
    // Displayname
    final String TEST_PARTIAL_TERM_DISPLAY_NAME =
            TEST_PARTIAL_TERM_FORE_NAME + " " + TEST_PARTIAL_TERM_SUR_NAME;

    // Non-existent partial term name (first letters of each of the words
    // in a pangram for the English alphabet).
    private static final String TEST_PARTIAL_TERM_NON_EXISTENT = "jlmbsoq";

    /** The known resource id. */
    private String knownResourceId = null;
    
    /** The known resource ref name. */
    private String knownResourceRefName = null;
    
    /** The known item resource id. */
    private String knownItemResourceId = null;

    // The resource ID of an item resource used for partial term matching tests.
    private String knownItemPartialTermResourceId = null;

    private List<String> allResourceIdsCreated = new ArrayList<String>();
    
    /** The all item resource ids created. */
    private Map<String, String> allItemResourceIdsCreated =
        new HashMap<String, String>();

    // The number of matches expected on each partial term.
    final int NUM_MATCHES_EXPECTED = 1;

    // The minimum number of characters that must be included
    // a partial term, in order to permit matching to occur.
    final int PARTIAL_TERM_MIN_LENGTH = 1;

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new PersonAuthorityClient();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getAbstractCommonList(
                    ClientResponse<AbstractCommonList> response) {
    return response.getEntity(PersonsCommonList.class);
    }

    private String getPartialTerm() {
        return TEST_PARTIAL_TERM_FORE_NAME;
    }

    private String getPartialTermUtf8() {
        return TEST_PARTIAL_TERM_SUR_NAME;
    }

    private String getPartialTermNonExistent() {
        return TEST_PARTIAL_TERM_NON_EXISTENT;
    }

    private String getPartialTermMinimumLength() {
        String partialTerm = getPartialTerm();
        if (partialTerm == null || partialTerm.trim().isEmpty()) {
            return partialTerm;
        }
        if (getPartialTerm().length() > PARTIAL_TERM_MIN_LENGTH) {
            return partialTerm.substring(0, PARTIAL_TERM_MIN_LENGTH);
        } else {
          return partialTerm;
        }
    }

    @BeforeClass
    public void setup() {
        try {
            createAuthority();
        } catch (Exception e) {
            Assert.fail("Could not create new Authority for search tests.", e);
        }
        try {
            createItemInAuthorityForPartialTermMatch(knownResourceId, knownResourceRefName);
        } catch (Exception e) {
            Assert.fail("Could not create new item in Authority for search tests.", e);
        }
    }
 
    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests by partial term match.
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Reads an item list by partial term.
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readListByPartialTerm"})
    public void partialTermMatch(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int numMatchesFound = 0;
        String partialTerm = getPartialTerm();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound = readItemListByPartialTerm(knownResourceId, partialTerm);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term, with a partial term that consists
     * of an all-lowercase variation of the expected match, to test case-insensitive
     * matching.
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchCaseInsensitiveLowerCase(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int numMatchesFound = 0;

        final String partialTerm = getPartialTerm().toLowerCase();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound =
            readItemListByPartialTerm(knownResourceId, partialTerm);
                if (logger.isDebugEnabled()) {
        logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term, with a partial term that consists
     * of an all-uppercase variation of the expected match, to test case-insensitive
     * matching.
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchCaseInsensitiveUpperCase(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int numMatchesFound = 0;

        final String partialTerm = getPartialTerm().toUpperCase();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound =
            readItemListByPartialTerm(knownResourceId, partialTerm);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term, with a partial term that is of
     * the minimum character length that may be expected to be matched.
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchMinimumLength(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int numMatchesFound = 0;
        String partialTerm = getPartialTermMinimumLength();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound = readItemListByPartialTerm(knownResourceId, partialTerm);
        // Zero matches are expected on a non-existent term.
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term, with a partial term that contains
     * at least one Unicode UTF-8 character (outside the USASCII range).
     */
    // FIXME: Test currently fails with a true UTF-8 String - need to investigate why.
    // Will be commented out for now until we get this working ...
/*
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchUTF8(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int numMatchesFound = 0;
        String partialTerm = getPartialTermUtf8();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound =
            readItemListByPartialTerm(knownResourceId, partialTerm);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED);
    }
*/
    
    // Failure outcomes

    /**
     * Reads an item list by partial term, with a partial term that is not
     * expected to be matched by any term in any resource.
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchOnNonexistentTerm(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        int numMatchesFound = 0;
        int ZERO_MATCHES_EXPECTED = 0;
        String partialTerm = getPartialTermNonExistent();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound = readItemListByPartialTerm(knownResourceId, partialTerm);
        // Zero matches are expected on a non-existent term.
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                ZERO_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, ZERO_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term, given an authority and a term.
     *
     * @param authorityCsid The CSID of the authority within which partial term matching
     *     will be performed.
     * @param partialTerm A partial term to match item resources.
     * @return The number of item resources matched by the partial term.
     */
    private int readItemListByPartialTerm(String authorityCsid, String partialTerm) {

        String testName = "readItemListByPartialTerm";

        // Perform setup.
        int expectedStatusCode = Response.Status.OK.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.READ_LIST;
        testSetup(expectedStatusCode, requestType);

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        ClientResponse<PersonsCommonList> res = null;
        if (authorityCsid != null) {
	    res = client.readItemList(authorityCsid, partialTerm);
        } else {
            Assert.fail("readItemListByPartialTerm passed null csid!");
        }
        PersonsCommonList list = null;
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if(logger.isDebugEnabled()){
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            list = res.getEntity();
        } finally {
            res.releaseConnection();
        }

        List<PersonsCommonList.PersonListItem> items = list.getPersonListItem();
        int nItemsReturned = items.size();

        return nItemsReturned;
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
        String noTest = System.getProperty("noTestCleanup");
    	if(Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
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
        PersonAuthorityClient client = new PersonAuthorityClient();
        parentResourceId = knownResourceId;
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allItemResourceIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                client.deleteItem(parentResourceId, itemResourceId);
            res.releaseConnection();
        }
        // Clean up authority resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            client.delete(resourceId).releaseConnection();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }


    // ---------------------------------------------------------------
    // Utilities: setup routines for search tests
    // ---------------------------------------------------------------

    public void createAuthority() throws Exception {

        String testName = "createAuthority";

        // Perform setup.
        int expectedStatusCode = Response.Status.CREATED.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.CREATE;
        testSetup(expectedStatusCode, requestType);

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        String identifier = createIdentifier();
    	String displayName = "displayName-" + identifier;
    	String baseRefName = PersonAuthorityClientUtils.createPersonAuthRefName(displayName, false);
    	String fullRefName = PersonAuthorityClientUtils.createPersonAuthRefName(displayName, true);
    	MultipartOutput multipart =
            PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    displayName, fullRefName, client.getCommonPartName());

    	String newID = null;
    	ClientResponse<Response> res = client.create(multipart);
        try {
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match
            // the expected response(s)?
            if(logger.isDebugEnabled()){
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, this.EXPECTED_STATUS_CODE);
            newID = PersonAuthorityClientUtils.extractId(res);
    	} finally {
            res.releaseConnection();
    	}
        // Store the refname from the first resource created
        // for additional tests below.
        knownResourceRefName = baseRefName;
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = newID;
            knownResourceRefName = baseRefName;
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

     /**
     * Creates an item in the authority, used for partial term matching tests.
     *
     * @param authorityCsid The CSID of the Authority in which the term will be created.
     * @param authRefName The refName of the Authority in which the term will be created.
     */
    private void createItemInAuthorityForPartialTermMatch(String authorityCsid, String authRefName)
        throws Exception {
            
        String testName = "createItemInAuthorityForPartialTermMatch";

        int expectedStatusCode = Response.Status.CREATED.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.CREATE;
        testSetup(expectedStatusCode, requestType);

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        String refName = PersonAuthorityClientUtils.createPersonRefName(authRefName,
                TEST_PARTIAL_TERM_DISPLAY_NAME, true);
        Map<String, String> partialTermPersonMap = new HashMap<String,String>();
        //
        // Fill the property map
        //
        partialTermPersonMap.put(PersonJAXBSchema.DISPLAY_NAME_COMPUTED, "false");
        partialTermPersonMap.put(PersonJAXBSchema.DISPLAY_NAME, TEST_PARTIAL_TERM_DISPLAY_NAME);
        partialTermPersonMap.put(PersonJAXBSchema.FORE_NAME, TEST_PARTIAL_TERM_FORE_NAME);
        partialTermPersonMap.put(PersonJAXBSchema.SUR_NAME, TEST_PARTIAL_TERM_SUR_NAME);
        partialTermPersonMap.put(PersonJAXBSchema.GENDER, "male");
        MultipartOutput multipart =
            PersonAuthorityClientUtils.createPersonInstance(authorityCsid, refName, partialTermPersonMap,
                client.getItemCommonPartName() );

        String newID = null;
        ClientResponse<Response> res = client.createItem(authorityCsid, multipart);
        try {
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match
            // the expected response(s)?
            if(logger.isDebugEnabled()){
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
            res.releaseConnection();
        }

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null){
            knownItemResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemPartialTermResourceId=" + knownItemPartialTermResourceId);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allItemResourceIdsCreated.put(newID, authorityCsid);
    }

}
