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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;
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
public class PersonAuthoritySearchTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = PersonAuthoritySearchTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
	@Override
	public String getServicePathComponent() {
		return PersonAuthorityClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return PersonAuthorityClient.SERVICE_NAME;
	}
    
    final String UTF8_CHARSET_NAME = "UTF-8";
    
    // Test name for partial term matching: Lech Wałęsa
    //
    // For details regarding the łę characters in the last name, see:
    // http://en.wikipedia.org/wiki/L_with_stroke
    // http://en.wikipedia.org/wiki/%C4%98
    //
    // Forename
    final String TEST_PARTIAL_TERM_FORE_NAME = "Lech";
    //
    // Surname (contains single quote character)
    final String TEST_PARTIAL_TERM_SUR_NAME_QUOTE = "O'Hara";
    //
    // Surname (contains two non-USASCII range Unicode UTF-8 characters)
    final String TEST_PARTIAL_TERM_SUR_NAME_UNICODE = "Wałęsa";
	// Wrong: "Wa" + "\u0142" + "\u0119" + "sa";
	// Should also work: "Wa" + '\u0142' + '\u0119' + "sa";
	// Should also work: "Wa\u0142\u0119sa";
    //
    //
    // Displayname
    final String TEST_PARTIAL_TERM_DISPLAY_NAME_UNICODE =
            TEST_PARTIAL_TERM_FORE_NAME + " " + TEST_PARTIAL_TERM_SUR_NAME_UNICODE;
    //
    // Displayname
    final String TEST_PARTIAL_TERM_DISPLAY_NAME_QUOTE =
            TEST_PARTIAL_TERM_FORE_NAME + " " + TEST_PARTIAL_TERM_SUR_NAME_QUOTE;
    //
    // shortId
    final String TEST_SHORT_ID_UNICODE = "lechWalesa";
    //
    // shortId
    final String TEST_SHORT_ID_QUOTE = "lechOHara";
    
    final String TEST_KWD_BIRTH_PLACE = "Popowo, Poland";

    final String TEST_KWD_UTF8_STYLE = "Appliqu"+'\u00e8'+"d Arts";
    
    final String TEST_KWD_BIO_NOTE_NO_QUOTES = 
    	"This is a silly bionote with no so-called quotes.";
    
    final String TEST_KWD_BIO_NOTE_DBL_QUOTES = 
    	"This is a silly \"bionote\" for testing so called quote_handling";

    final String TEST_KWD_NO_MATCH = "Foobar";

    // Non-existent partial term name (first letters of each of the words
    // in a pangram for the English alphabet).
    private static final String TEST_PARTIAL_TERM_NON_EXISTENT = "jlmbsoq";

    /** The known resource id. */
    private String knownResourceId = null;
    
    /** The known resource ref name. */
    //private String knownResourceRefName = null;
    
    /** The known item resource id. */
    private String knownItemResourceId = null;

    // The resource ID of an item resource used for partial term matching tests.
    private String knownItemPartialTermResourceId = null;

    private List<String> allResourceIdsCreated = new ArrayList<String>();
    
    /** The all item resource ids created. */
    private Map<String, String> allItemResourceIdsCreated =
        new HashMap<String, String>();

    // The number of matches expected on each partial term.
    final int NUM_MATCHES_EXPECTED_COMMON = 2;
    final int NUM_MATCHES_EXPECTED_SPECIFIC = 1;

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
    protected AbstractCommonList getCommonList(
                    ClientResponse<AbstractCommonList> response) {
    return response.getEntity(AbstractCommonList.class);
    }

    private String getPartialTermCommon() {
        return TEST_PARTIAL_TERM_FORE_NAME;
    }

    private String getPartialTermUtf8() {
        return TEST_PARTIAL_TERM_SUR_NAME_UNICODE;
    }

    private String getPartialTermQuote() {
        return TEST_PARTIAL_TERM_SUR_NAME_QUOTE;
    }

    private String getPartialTermNonExistent() {
        return TEST_PARTIAL_TERM_NON_EXISTENT;
    }

    private String getPartialTermMinimumLength() {
        String partialTerm = getPartialTermCommon();
        if (partialTerm == null || partialTerm.trim().isEmpty()) {
            return partialTerm;
        }
        if (partialTerm.length() > PARTIAL_TERM_MIN_LENGTH) {
            return partialTerm.substring(0, PARTIAL_TERM_MIN_LENGTH);
        } else {
          return partialTerm;
        }
    }

    private String getKwdTerm() {
        return TEST_KWD_BIRTH_PLACE;
    }

    private String getKwdTermUTF8() {
        return TEST_KWD_UTF8_STYLE;
    }

    private String getKwdTermNonExistent() {
        return TEST_KWD_NO_MATCH;
    }

    @BeforeClass
    public void setup() {
        try {
            createAuthority();
        } catch (Exception e) {
            Assert.fail("Could not create new Authority for search tests.", e);
        }
        try {
            createItemsInAuthorityForPartialTermMatch(knownResourceId, null ); //knownResourceRefName);
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
    @Test(dataProvider="testName", groups = {"readListByPartialTerm"})
    public void partialTermMatch(String testName) {
        int numMatchesFound = 0;
        String partialTerm = getPartialTermCommon();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound = readItemListWithFilters(testName, knownResourceId, partialTerm, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED_COMMON + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_COMMON);
    }

    /**
     * Reads an item list by partial term, with a partial term that consists
     * of an all-lowercase variation of the expected match, to test case-insensitive
     * matching.
     */
    @Test(dataProvider="testName", groups = {"readListByPartialTerm"},
    		dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchCaseInsensitiveLowerCase(String testName) {
        int numMatchesFound = 0;

        final String partialTerm = getPartialTermCommon().toLowerCase();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound =
        	readItemListWithFilters(testName, knownResourceId, partialTerm, null);
                if (logger.isDebugEnabled()) {
        logger.debug("Found " + numMatchesFound + " match(es), expected " +
        		NUM_MATCHES_EXPECTED_COMMON + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_COMMON);
    }

    /**
     * Reads an item list by partial term, with a partial term that consists
     * of an all-uppercase variation of the expected match, to test case-insensitive
     * matching.
     */
    @Test(dataProvider="testName",
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchCaseInsensitiveUpperCase(String testName) {
        int numMatchesFound = 0;

        final String partialTerm = getPartialTermCommon().toUpperCase();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound =
        	readItemListWithFilters(testName, knownResourceId, partialTerm, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
            		NUM_MATCHES_EXPECTED_COMMON + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_COMMON);
    }

    /**
     * Reads an item list by partial term, with a partial term that is of
     * the minimum character length that may be expected to be matched.
     */
    @Test(dataProvider="testName",
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchMinimumLength(String testName) {
        int numMatchesFound = 0;
        String partialTerm = getPartialTermMinimumLength();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound = readItemListWithFilters(testName, knownResourceId, partialTerm, null);
        // Zero matches are expected on a non-existent term.
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
            		NUM_MATCHES_EXPECTED_COMMON + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_COMMON);
    }

    /**
     * Reads an item list by partial term, with a partial term that contains
     * at least one Unicode UTF-8 character (outside the USASCII range).
     */
    @Test(dataProvider="testName",
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchUTF8(String testName) {
        int numMatchesFound = 0;
        String partialTerm = getPartialTermUtf8();
        String ptEncoded;
        try {
        	ptEncoded = URLEncoder.encode(partialTerm, UTF8_CHARSET_NAME);
        }
        catch (UnsupportedEncodingException ex) {
          throw new RuntimeException("Broken VM does not support UTF-8");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "', Encoded:'"+ptEncoded+"' ...");
        }
        numMatchesFound =
        	readItemListWithFilters(testName, knownResourceId, partialTerm, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED_SPECIFIC + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_SPECIFIC);
    }
    
    /**
     * Reads an item list by partial term, with a partial term that contains
     * at least one Unicode UTF-8 character (outside the USASCII range).
     */
    @Test(dataProvider="testName",
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchQuote(String testName) {
        int numMatchesFound = 0;
        String partialTerm = getPartialTermQuote();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound =
        	readItemListWithFilters(testName, knownResourceId, partialTerm, null);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED_SPECIFIC + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_SPECIFIC);
    }

    /**
     * Finds terms by keywords.
     */
    @Test(dataProvider="testName", groups = {"readListByKwdTerm"})
    public void keywordTermMatch(String testName) {
        int numMatchesFound = 0;
        String kwdTerm = getKwdTerm();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on kwd term '" + kwdTerm + "' ...");
        }
        numMatchesFound = readItemListWithFilters(testName, knownResourceId, null, kwdTerm);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                    NUM_MATCHES_EXPECTED_COMMON + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_COMMON);
    }

    /**
     * Finds terms by keywords.
     */
    @Test(dataProvider="testName",
        groups = {"readListByKwdTerm"}, dependsOnMethods = {"keywordTermMatch"})
    public void keywordTermMatchUTF8(String testName) {
        int numMatchesFound = 0;
        String kwdTerm = getKwdTermUTF8();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on kwd term '" + kwdTerm + "' ...");
        }
        numMatchesFound = readItemListWithFilters(testName, knownResourceId, null, kwdTerm);
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                NUM_MATCHES_EXPECTED_COMMON + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, NUM_MATCHES_EXPECTED_COMMON);
    }

    
    
    // Failure outcomes

    /**
     * Reads an item list by partial term, with a partial term that is not
     * expected to be matched by any term in any resource.
     */
    @Test(dataProvider="testName",
        groups = {"readListByPartialTerm"}, dependsOnMethods = {"partialTermMatch"})
    public void partialTermMatchOnNonexistentTerm(String testName) {
        int numMatchesFound = 0;
        int ZERO_MATCHES_EXPECTED = 0;
        String partialTerm = getPartialTermNonExistent();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on partial term '" + partialTerm + "' ...");
        }
        numMatchesFound = readItemListWithFilters(testName, knownResourceId, partialTerm, null);
        // Zero matches are expected on a non-existent term.
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                ZERO_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, ZERO_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term, with a partial term that is not
     * expected to be matched by any term in any resource.
     */
    @Test(dataProvider="testName", groups = {"readListByKwdTerm"},
    		dependsOnMethods = {"keywordTermMatch"})
    public void keywordTermMatchOnNonexistentTerm(String testName) {
        int numMatchesFound = 0;
        int ZERO_MATCHES_EXPECTED = 0;
        String kwdTerm = getKwdTermNonExistent();
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting match on kwd term '" + kwdTerm + "' ...");
        }
        numMatchesFound = readItemListWithFilters(testName, knownResourceId, null, kwdTerm);
        // Zero matches are expected on a non-existent term.
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + numMatchesFound + " match(es), expected " +
                ZERO_MATCHES_EXPECTED + " match(es).");
        }
        Assert.assertEquals(numMatchesFound, ZERO_MATCHES_EXPECTED);
    }

    /**
     * Reads an item list by partial term or keywords, given an authority and a term.
     * Only one of partialTerm or keywords should be specified. 
     * If both are specified, keywords will be ignored.
     * 
     * @param testName Calling test name
     * @param authorityCsid The CSID of the authority within which partial term matching
     *     will be performed.
     * @param partialTerm A partial term to match item resources.
     * @param partialTerm A keyword list to match item resources.
     * @return The number of item resources matched by the partial term.
     */
    private int readItemListWithFilters(String testName, 
    		String authorityCsid, String partialTerm, String keywords) {

        // Perform setup.
        int expectedStatusCode = Response.Status.OK.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.READ_LIST;
        testSetup(expectedStatusCode, requestType);

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        ClientResponse<AbstractCommonList> res = null;
        if (authorityCsid != null) {
        	res = client.readItemList(authorityCsid, partialTerm, keywords);
        } else {
            Assert.fail("readItemListByPartialTerm passed null csid!");
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

    // ---------------------------------------------------------------
    // Utilities: setup routines for search tests
    // ---------------------------------------------------------------

    public void createAuthority() throws Exception {

        String testName = "createAuthority";
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner(testName, CLASS_NAME));
        }

        // Perform setup.
        int expectedStatusCode = Response.Status.CREATED.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.CREATE;
        testSetup(expectedStatusCode, requestType);

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
            newID = PersonAuthorityClientUtils.extractId(res);
    	} finally {
    		if (res != null) {
                res.releaseConnection();
            }
    	}
        // Store the refname from the first resource created
        // for additional tests below.
        //knownResourceRefName = baseRefName;
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = newID;
            //knownResourceRefName = baseRefName;
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
    private void createItemsInAuthorityForPartialTermMatch(
    		String authorityCsid, String authRefName)
        throws Exception {
            
        String testName = "createItemsInAuthorityForPartialTermMatch";

        int expectedStatusCode = Response.Status.CREATED.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.CREATE;
        testSetup(expectedStatusCode, requestType);

        // Submit the request to the service and store the response.
        PersonAuthorityClient client = new PersonAuthorityClient();
        Map<String, String> partialTermPersonMap = new HashMap<String,String>();
        //
        // Fill the property map for the UNICODE item
        //
        partialTermPersonMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, TEST_SHORT_ID_UNICODE );
        partialTermPersonMap.put(PersonJAXBSchema.DISPLAY_NAME_COMPUTED, "false");
        partialTermPersonMap.put(PersonJAXBSchema.DISPLAY_NAME, TEST_PARTIAL_TERM_DISPLAY_NAME_UNICODE);
        partialTermPersonMap.put(PersonJAXBSchema.FORE_NAME, TEST_PARTIAL_TERM_FORE_NAME);
        partialTermPersonMap.put(PersonJAXBSchema.SUR_NAME, TEST_PARTIAL_TERM_SUR_NAME_UNICODE);
        partialTermPersonMap.put(PersonJAXBSchema.BIRTH_PLACE, TEST_KWD_BIRTH_PLACE);
        partialTermPersonMap.put(PersonJAXBSchema.GENDER, "male");
        partialTermPersonMap.put(PersonJAXBSchema.BIO_NOTE, TEST_KWD_BIO_NOTE_NO_QUOTES);

        Map<String, List<String>> partialTermRepeatablesMap = new HashMap<String, List<String>>();
        ArrayList<String> styles = new ArrayList<String>();
        styles.add(TEST_KWD_UTF8_STYLE);
        partialTermRepeatablesMap.put(PersonJAXBSchema.SCHOOLS_OR_STYLES, styles);

        createItem(testName, authorityCsid, null /*authRefName*/, client, 
                partialTermPersonMap, partialTermRepeatablesMap);
        //
        // Adjust the property map for the QUOTE item
        //
        partialTermPersonMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, TEST_SHORT_ID_QUOTE );
        partialTermPersonMap.put(PersonJAXBSchema.DISPLAY_NAME, TEST_PARTIAL_TERM_DISPLAY_NAME_QUOTE);
        partialTermPersonMap.put(PersonJAXBSchema.SUR_NAME, TEST_PARTIAL_TERM_SUR_NAME_QUOTE);
        partialTermPersonMap.put(PersonJAXBSchema.BIO_NOTE, TEST_KWD_BIO_NOTE_DBL_QUOTES);

        createItem(testName, authorityCsid, null /*authRefName*/, client, 
                partialTermPersonMap, partialTermRepeatablesMap);
    }
    
    private void createItem(
    		String testName, 
    		String authorityCsid, 
    		String authRefName,
    		PersonAuthorityClient client,
    		Map<String, String> partialTermPersonMap,
    		Map<String, List<String>> partialTermRepeatablesMap) throws Exception {
        PoxPayloadOut multipart =
            PersonAuthorityClientUtils.createPersonInstance(authorityCsid, null, //authRefName, 
                partialTermPersonMap, partialTermRepeatablesMap, client.getItemCommonPartName() );

        String newID = null;
        ClientResponse<Response> res = client.createItem(authorityCsid, multipart);
        try {
            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
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
