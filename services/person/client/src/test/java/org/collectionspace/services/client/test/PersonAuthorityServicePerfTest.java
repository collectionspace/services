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
 * VocabularyServiceTest, carries out tests against a
 * deployed and running Vocabulary Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class PersonAuthorityServicePerfTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = PersonAuthorityServicePerfTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
//    final String SERVICE_PATH_COMPONENT = "personauthorities";
//    final String ITEM_SERVICE_PATH_COMPONENT = "items";
    private String authId = null;
    //private String authRefName = null;
    private List<String> allItemIdsCreated = new ArrayList<String>();
    private String[] firstNames = { 
    		"Ann","Anne","Anno",
    		"George","Geoff","Georgia",
    		"John","Johanna","Jon",
    		"Patrick","Patty","Patrik"};
    private String[] firstNameSuffixes = { 
    		"1","2","3","4","5","6","7","8","9","0"};
    private String[] lastNames = { 
    		"Axis","Black","Cobbler",
    		"Dunne","England","France",
    		"Goldsmith","Hart","Indy",
    		"Jones","Kohn","Lark",
    		"Maven","Newhart","Overdunne",
    		"Plaxo","Queen","Roberts",
    		"Smith","Tate","Underdunne",
    		"Vicious","Waits","Xavier",
    		"Yeats","Zoolander"};
    
    // Keep these two arrays in sync!!!
    private String[] partialTerms = {"Ann","John","Patty2"};
    private int[] nMatches = {
    		4*lastNames.length*firstNameSuffixes.length, // Johanna too!
		    lastNames.length*firstNameSuffixes.length,
		    lastNames.length, };
    private int shortTestLimit = 2; // Just use first three items in suffix and last name arrays
    private int[] nMatchesShort = {
    		4*shortTestLimit*shortTestLimit, // Johanna too!
    		shortTestLimit*shortTestLimit,
    		shortTestLimit, };
    private boolean runFullTest = false;
    
	@Override
	public String getServicePathComponent() {
		return PersonAuthorityClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return PersonAuthorityClient.SERVICE_NAME;
	}
    
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
 
    @BeforeClass
    public void setup() {
        try {
            createAuthority();
        } catch (Exception e) {
            Assert.fail("Could not create new Authority for search tests.", e);
        }
        try {
        	long startTime = System.currentTimeMillis();
            createItems();
        	long stopTime = System.currentTimeMillis();
        	double runTime = (stopTime - startTime)/1000.0;
        	logger.info("Created {} items in {} seconds.", 
        				allItemIdsCreated.size(), runTime);
        } catch (Exception e) {
            Assert.fail("Could not create new item in Authority for search tests.", e);
        }
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
        String shortId = "perfTestPersons";
    	String displayName = "Perf Test Person Auth";
    	//String baseRefName = PersonAuthorityClientUtils.createPersonAuthRefName(shortId, null);
    	PoxPayloadOut multipart =
            PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    displayName, shortId, client.getCommonPartName());

    	String newID = null;
    	ClientResponse<Response> res = client.create(multipart);
        try {
            assertStatusCode(res, testName);
            newID = PersonAuthorityClientUtils.extractId(res);
            logger.info("{}: succeeded.", testName);
    	} finally {
    		if (res != null) {
                res.releaseConnection();
            }
    	}
        // Store the refname from the first resource created
        // for additional tests below.
    	//authRefName = baseRefName;
        // Store the ID returned from the first resource created
        // for additional tests below.
    	authId = newID;
    }

    /**
     * Creates an item in the authority, used for partial term matching tests.
     *
     * @param authorityCsid The CSID of the Authority in which the term will be created.
     * @param authRefName The refName of the Authority in which the term will be created.
     */
    private void createItem(String firstName, String lastName, PersonAuthorityClient client )
        throws Exception {
            
        int expectedStatusCode = Response.Status.CREATED.getStatusCode();
        ServiceRequestType requestType = ServiceRequestType.CREATE;
        testSetup(expectedStatusCode, requestType);
        
        if(client==null) {
            client = new PersonAuthorityClient();
        }

        // Submit the request to the service and store the response.
        Map<String, String> personMap = new HashMap<String,String>();
        //
        // Fill the property map
        //
        String shortId = firstName+lastName;
        personMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId );
        personMap.put(PersonJAXBSchema.DISPLAY_NAME_COMPUTED, "true");
        personMap.put(PersonJAXBSchema.FORE_NAME, firstName);
        personMap.put(PersonJAXBSchema.SUR_NAME, lastName);
        Map<String, List<String>> personRepeatablesMap = new HashMap<String, List<String>>();
        PoxPayloadOut multipart =
            PersonAuthorityClientUtils.createPersonInstance(authId, null, //authRefName, 
            		personMap, personRepeatablesMap, client.getItemCommonPartName() );

        String newID = null;
        ClientResponse<Response> res = client.createItem(authId, multipart);
        try {
            assertStatusCode(res, "createItem");
            newID = PersonAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allItemIdsCreated.add(newID);
    }

    private void createItems()
    	throws Exception {
        PersonAuthorityClient client = new PersonAuthorityClient();
        String fullTest = System.getProperty("runFullItemsTest");
        runFullTest = Boolean.TRUE.toString().equalsIgnoreCase(fullTest); 
		int maxSuff = shortTestLimit;
		int maxLN = shortTestLimit; 
    	if(runFullTest) {
            logger.debug("Creating full items set ...");
    		maxSuff = firstNameSuffixes.length;
    		maxLN = lastNames.length; 
        } else {
            logger.debug("Creating short items set ...");
        }
    	for(String fname:firstNames) {
    		for( int iSuff=0; iSuff<maxSuff; iSuff++ ) {
    			String fns = firstNameSuffixes[iSuff];
        		for( int iLN=0; iLN<maxLN; iLN++ ) {
        			String lname = lastNames[iLN];
            		createItem(fname+fns, lname, null);
            	}    		
        	}    		
    	}
        logger.debug("createItems created {} items.", allItemIdsCreated.size());
    }
    
    /**
     * Reads an item list by partial term.
     */
    @Test(dataProvider="testName")
    public void partialTermMatch(String testName) {
        for(int i=0; i<partialTerms.length; i++) {
        	long startTime = System.currentTimeMillis();
            int numMatchesFound = readItemListWithFilters(testName, authId, 
            						partialTerms[i], null);
            Assert.assertEquals(numMatchesFound, (runFullTest?nMatches[i]:nMatchesShort[i]), 
            	"Did not get expected number of partial term matches for "+partialTerms[i]);
        	long stopTime = System.currentTimeMillis();
        	double runTime = (stopTime - startTime)/1000.0;
            if(logger.isInfoEnabled()){
                logger.info("Got: "+numMatchesFound+
                		" matches for: \""+partialTerms[i]+"\" in "+
                		runTime+" seconds.");
            }
        }
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
            Assert.fail(testName+" passed null csid!");
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
        return (int)list.getTotalItems();
    }
    // ---------------------------------------------------------------
    // Utility methods used by tests above
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
        // Note: Any non-success responses from the delete operations
        // below are ignored and not reported.
        PersonAuthorityClient client = new PersonAuthorityClient();
        // Clean up item resources.
        for (String itemId : allItemIdsCreated) {
            client.deleteItem(authId, itemId).releaseConnection();
        }
        client.delete(authId).releaseConnection();
    }


}
