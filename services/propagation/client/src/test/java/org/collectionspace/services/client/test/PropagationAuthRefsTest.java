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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PropagationClient;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.propagation.PropActivityGroup;
import org.collectionspace.services.propagation.PropActivityGroupList;
import org.collectionspace.services.propagation.PropagationsCommon;
import org.collectionspace.services.person.PersonTermGroup;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PropagationAuthRefsTest, carries out Authority References tests against a
 * deployed and running Propagation (aka Loans In) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class PropagationAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = PropagationAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    // Instance variables specific to this test.
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceId = null;
    private List<String> propagationIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
	private String propagatedByRefName = null; // an authRef field
    private final static String CURRENT_DATE_UTC = GregorianCalendarDateTimeUtils.currentDateUTC();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
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
        PropagationClient propagationClient = new PropagationClient();
        PoxPayloadOut propagationInstance = createPropagationInstance(
                "propagationNumber-" + identifier,
                this.propagatedByRefName,
                CURRENT_DATE_UTC);
        Response response = propagationClient.create(propagationInstance);
        try {
        	int statusCode = response.getStatus();
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode), invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        // Store the ID returned from the first resource created
	        // for additional tests below.
	        if (knownResourceId == null) {
	            knownResourceId = extractId(response);
	        }
	        
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        propagationIdsCreated.add(extractId(response));
        } finally {
        	response.close();
        }
    }
    
    /**
     * Create one or more Person records that will be used to create refNames (referenced terms) in our
     * test propagation records.
     * 
     * @throws Exception
     */
    protected void createPersonRefs() throws Exception {
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
        
        // Create temporary Person resources, and their corresponding refNames
        // by which they can be identified.
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, personAuthClient);
       	String csid = createPerson("Propye", "ThePropagator", "proppy", authRefName);
        personIdsCreated.add(csid);
        
        // Safe the refName for later use -see createWithAuthRefs() method
        this.propagatedByRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, personAuthClient);
    }
    
    protected String createPerson(String firstName, String surName, String shortId, String authRefName ) throws Exception {
        PersonClient personAuthClient = new PersonClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
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
	    	return extractId(res);
        } finally {
        	res.close();
        }
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        PropagationClient propagationClient = new PropagationClient();
        Response res = propagationClient.read(knownResourceId);
        try {
	        assertStatusCode(res, testName);
	        // Extract the common part from the response.
	        PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(String.class));
	        PropagationsCommon propagationCommon = (PropagationsCommon) extractPart(input,
	            propagationClient.getCommonPartName(), PropagationsCommon.class);
	        Assert.assertNotNull(propagationCommon);
	        if (logger.isDebugEnabled()){
	            logger.debug(objectAsXmlString(propagationCommon, PropagationsCommon.class));
	        }
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        
        // Get the authority references
        res = propagationClient.getAuthorityRefs(knownResourceId); // AuthorityRefList
        AuthorityRefList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = (AuthorityRefList)res.readEntity(AuthorityRefList.class);
	        Assert.assertNotNull(list);
        } finally {
        	if (res != null) {
        		res.close();
            }
        }
        
        int expectedAuthRefs = personIdsCreated.size();
        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        if(logger.isDebugEnabled()){
            logger.debug("Expected " + expectedAuthRefs + " authority references, found " + numAuthRefsFound);
        }

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

        Assert.assertEquals(numAuthRefsFound, expectedAuthRefs,
            "Did not find all expected authority references! " + "Expected " + expectedAuthRefs + ", found " + numAuthRefsFound);
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
        // Delete all the propagation records we created
    	PropagationClient propagationClient = new PropagationClient();
        for (String resourceId : propagationIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            propagationClient.delete(resourceId).close(); // alternative to propagationClient.delete(resourceId).releaseConnection();
        }

        //
        // Delete Person resource(s) (before PersonAuthority resources). 
        PersonClient personAuthClient = new PersonClient();
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
        	personAuthClient.deleteItem(personAuthCSID, resourceId).close();
        }
        if (personAuthCSID != null) {
        	personAuthClient.delete(personAuthCSID).close();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
	public String getServiceName() {
        return PropagationClient.SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return PropagationClient.SERVICE_PATH_COMPONENT;
    }

    private PoxPayloadOut createPropagationInstance(String propagationNumber,
    		String propagatedBy,
    		String returnDate) throws Exception {
    	PropagationsCommon propagationCommon = new PropagationsCommon();
    	propagationCommon.setPropNumber(propagationNumber);
    	propagationCommon.setPropBy(propagatedBy);
    	propagationCommon.setPropNumber(returnDate);
    	
    	PropActivityGroupList propActivityGroupList =  new PropActivityGroupList();
    	PropActivityGroup propActivityGroup = new PropActivityGroup();
    	propActivityGroup.setOrder(BigInteger.valueOf(42));
    	propActivityGroupList.getPropActivityGroup().add(propActivityGroup);
    	propagationCommon.setPropActivityGroupList(propActivityGroupList);

    	PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
    	PayloadOutputPart commonPart = multipart.addPart(new PropagationClient().getCommonPartName(), propagationCommon);

    	if (logger.isDebugEnabled()) {
    		logger.debug("to be created, propagation common");
    		logger.debug(objectAsXmlString(propagationCommon, PropagationsCommon.class));
    	}

    	return multipart;
    }

    @Override
    protected Class<AbstractCommonList> getCommonListType() {
    	return AbstractCommonList.class;
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
	}
}
