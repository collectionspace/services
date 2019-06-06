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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanout.LoansoutCommon;
import org.collectionspace.services.person.PersonTermGroup;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoanoutAuthRefsTest, carries out Authority References tests against a
 * deployed and running Loanout (aka Loans Out) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class LoanoutAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = LoanoutAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final String SERVICE_NAME = "loansout";
    final String SERVICE_PATH_COMPONENT = "loansout";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceId = null;
    private List<String> loanoutIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String borrowerRefName = null;
    private String borrowersContactRefName = null;
    private String lendersAuthorizerRefName = null;
    private String lendersContactRefName = null;

    // FIXME: Can add 'borrower' - likely to be an organization
    // authority - as an authRef to tests below, and increase the
    // number of expected authRefs to 4.
    private final int NUM_AUTH_REFS_EXPECTED = 4;
    
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    @Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
	}
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getCommonList(Response response) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        // Create all the person refs and entities
        createPersonRefs();

        // Create a new Loans In resource.
        //
        // One or more fields in this resource will be PersonAuthority
        // references, and will refer to Person resources by their refNames.
        LoanoutClient loanoutClient = new LoanoutClient();
        PoxPayloadOut multipart = createLoanoutInstance(
                "loanOutNumber-" + identifier,
                CURRENT_DATE_UTC,
                borrowerRefName,
                borrowersContactRefName,
                lendersAuthorizerRefName,
                lendersContactRefName);
        String newId = null;
        Response res = loanoutClient.create(multipart);
        try {
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
	        newId = extractId(res);
	        Assert.assertNotNull(newId, "Could not create a new LoanOut record.");
        } finally {
        	res.close();
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = newId;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        loanoutIdsCreated.add(newId);
    }

    protected void createPersonRefs() throws Exception{

        PersonClient personAuthClient = new PersonClient();
        // Create a temporary PersonAuthority resource, and its corresponding
        // refName by which it can be identified.
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        Response res = personAuthClient.create(multipart);
        try {
	        int statusCode = res.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	            invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	        personAuthCSID = extractId(res);
        } finally {
        	res.close();
        }
        
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);

        // Create temporary Person resources, and their corresponding refNames
        // by which they can be identified.

        String csid = createPerson("Betty", "Borrower", "bettyBorrower", authRefName);
        personIdsCreated.add(csid);
        borrowerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Bradley", "BorrowersContact", "bradleyBorrowersContact", authRefName);
        personIdsCreated.add(csid);
        borrowersContactRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Art", "Lendersauthorizor", "artLendersauthorizor", authRefName);
        personIdsCreated.add(csid);
        lendersAuthorizerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Larry", "Lenderscontact", "larryLenderscontact", authRefName);
        personIdsCreated.add(csid);
        lendersContactRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
    }
    
    protected String createPerson(String firstName, String surName, String shortId, String authRefName ) throws Exception {
    	String result = null;
    	
        PersonClient personAuthClient = new PersonClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId + random.nextInt(1000)); // avoid short ID conflicts with pass test session records that never got cleaned up
        List<PersonTermGroup> personTerms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        String termName = firstName + " " + surName;
        term.setTermDisplayName(termName);
        term.setTermName(termName);
        personTerms.add(term);
        PoxPayloadOut multipart =
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID,
    				authRefName, personInfo, personTerms, personAuthClient.getItemCommonPartName());
        Response res = personAuthClient.createItem(personAuthCSID, multipart);
        try {
	        int statusCode = res.getStatus();
	
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	        result = extractId(res);
        } finally {
        	res.close();
        }
        
    	return result; 
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        LoanoutClient loanoutClient = new LoanoutClient();
        Response res = loanoutClient.read(knownResourceId);
        LoansoutCommon loanoutCommon = null;
        try {
	        assertStatusCode(res, testName);
	        // Extract the common part from the response.
	        PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	        loanoutCommon = (LoansoutCommon) extractPart(input,
	            loanoutClient.getCommonPartName(), LoansoutCommon.class);
	        Assert.assertNotNull(loanoutCommon);
	        if(logger.isDebugEnabled()){
	            logger.debug(objectAsXmlString(loanoutCommon, LoansoutCommon.class));
	        }
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        
        // Check a couple of fields
        Assert.assertEquals(loanoutCommon.getBorrower(), borrowerRefName);
        Assert.assertEquals(loanoutCommon.getBorrowersContact(), borrowersContactRefName);
        Assert.assertEquals(loanoutCommon.getLendersAuthorizer(), lendersAuthorizerRefName);
        Assert.assertEquals(loanoutCommon.getLendersContact(), lendersContactRefName);
        
        // Get the auth refs and check them
        Response res2 = loanoutClient.getAuthorityRefs(knownResourceId);
        AuthorityRefList list = null;
        try {
	        assertStatusCode(res2, testName);
	        list = res2.readEntity(AuthorityRefList.class);
        } finally {
        	if (res2 != null) {
        		res2.close();
            }
        }

        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        if(logger.isDebugEnabled()){
            logger.debug("Expected " + NUM_AUTH_REFS_EXPECTED +
                " authority references, found " + numAuthRefsFound);
        }
        Assert.assertEquals(numAuthRefsFound, NUM_AUTH_REFS_EXPECTED,
            "Did not find all expected authority references! " +
            "Expected " + NUM_AUTH_REFS_EXPECTED + ", found " + numAuthRefsFound);
            
        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){
            int i = 0;
            for(AuthorityRefList.AuthorityRefItem item : items){
                logger.debug(testName + ": list-item[" + i + "] Field:" +
                		item.getSourceField() + "= " +
                        item.getAuthDisplayName() +
                        item.getItemDisplayName());
                logger.debug(testName + ": list-item[" + i + "] refName=" +
                        item.getRefName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
                i++;
            }
        }
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
    @Override
	@AfterClass(alwaysRun=true)
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
        //
        // Delete the loansout record(s) first
        //
        LoanoutClient loanoutClient = new LoanoutClient();
        for (String resourceId : loanoutIdsCreated) {
        	Response res = null;
        	try {
        		res = loanoutClient.delete(resourceId);
        		if (res.getStatus() != Response.Status.OK.getStatusCode()) {
        			throw new Exception(String.format("Could not delete the Loansout record=%s",
        					resourceId));
        		}
        	} catch (Throwable t) {
        		if (res != null) res.close();
        		throw t;
        	}
        }
        //
        // Delete the person terms/items before the person authority
        //
        PersonClient personAuthClient = new PersonClient();
        for (String resourceId : personIdsCreated) {
        	Response res = null;
        	try {
        		res = personAuthClient.deleteItem(personAuthCSID, resourceId);
        		if (res.getStatus() != Response.Status.OK.getStatusCode()) {
        			throw new Exception(String.format("Could not delete person term=%s from authority=%s",
        					resourceId, personAuthCSID));
        		}
        	} catch (Throwable t) {
        		if (res != null) res.close();
        		throw t;
        	}
        }
        //
        // Finally, delete the PersonAuthority resource(s).
        //
        if (personAuthCSID != null) {
        	Response res = null;
        	try {
        		res = personAuthClient.delete(personAuthCSID);
        		if (res.getStatus() != Response.Status.OK.getStatusCode()) {
        			throw new Exception(String.format("Could not delete the Person authority=%s",
        					personAuthCSID));
        		}
        	} catch (Throwable t) {
        		if (res != null) res.close();
        		throw t;
        	}
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
	public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private PoxPayloadOut createLoanoutInstance(String loanoutNumber,
    		String returnDate,
    		String borrower,
    		String borrowersContact,
    		String lendersAuthorizer,
    		String lendersContact) throws Exception {
    	LoansoutCommon loanoutCommon = new LoansoutCommon();
    	loanoutCommon.setLoanOutNumber(loanoutNumber);
    	loanoutCommon.setLoanReturnDate(returnDate);
    	loanoutCommon.setBorrower(borrower);
    	loanoutCommon.setBorrowersContact(borrowersContact);
    	loanoutCommon.setLendersAuthorizer(lendersAuthorizer);
    	loanoutCommon.setLendersContact(lendersContact);

    	PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
    	PayloadOutputPart commonPart =
    			multipart.addPart(new LoanoutClient().getCommonPartName(), loanoutCommon);

    	if(logger.isDebugEnabled()){
    		logger.debug("to be created, loanout common");
    		logger.debug(objectAsXmlString(loanoutCommon, LoansoutCommon.class));
    	}

    	return multipart;
    }

    @Override
    protected Class<AbstractCommonList> getCommonListType() {
    	// TODO Auto-generated method stub
    	return null;
    }
}
