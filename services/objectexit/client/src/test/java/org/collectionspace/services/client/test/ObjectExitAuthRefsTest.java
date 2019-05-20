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
import org.collectionspace.services.client.ObjectExitClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.objectexit.StructuredDateGroup;
import org.collectionspace.services.objectexit.ObjectexitCommon;
import org.collectionspace.services.person.PersonTermGroup;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObjectExitAuthRefsTest, carries out Authority References tests against a deployed and running ObjectExit (aka Loans Out) Service.
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class ObjectExitAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = ObjectExitAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String PERSON_AUTHORITY_NAME = "ObjectexitPersonAuth";
    private List<String> objectexitIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String depositorRefName = null;
    private StructuredDateGroup exitDateGroup = new StructuredDateGroup();
    private String exitNumber = null;
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

    @Override
	public String getServicePathComponent() {
		return ObjectExitClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return ObjectExitClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
	}

	@Override
    protected AbstractCommonList getCommonList(Response response) {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    private PoxPayloadOut createObjectExitInstance(String depositorRefName, String exitNumber, String exitDateDisplayDate) throws Exception {
        this.exitNumber = exitNumber;
        this.depositorRefName = depositorRefName;
        this.exitDateGroup.setDateDisplayDate(exitDateDisplayDate);
        this.exitDateGroup.setDateDisplayDate(exitDateDisplayDate);
        
        ObjectexitCommon objectexit = new ObjectexitCommon();
        StructuredDateGroup oeExitDateGroup = new StructuredDateGroup();

        objectexit.setDepositor(depositorRefName);
        objectexit.setExitNumber(exitNumber);
        oeExitDateGroup.setDateDisplayDate(exitDateDisplayDate);
        objectexit.setExitDateGroup(oeExitDateGroup);

        PoxPayloadOut multipart = new PoxPayloadOut(ObjectExitClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(new ObjectExitClient().getCommonPartName(),
        		objectexit);
        logger.debug("to be created, objectexit common: " + objectAsXmlString(objectexit, ObjectexitCommon.class));
        return multipart;
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        String identifier = createIdentifier(); // Submit the request to the service and store the response.
        createPersonRefs();// Create all the person refs and entities
        // Create a new Loans In resource. One or more fields in this resource will be PersonAuthority
        //    references, and will refer to Person resources by their refNames.
        ObjectExitClient objectexitClient = new ObjectExitClient();
        PoxPayloadOut multipart = createObjectExitInstance(depositorRefName,
                "exitNumber-" + identifier, CURRENT_DATE_UTC);
        Response res = objectexitClient.create(multipart);
        String newId = null;
        try {
	        assertStatusCode(res, testName);
	        newId = extractId(res);
	        Assert.assertNotNull(newId, "Could not create a new ObjectExit record.");
	        if (knownResourceId == null) {// Store the ID returned from the first resource created for additional tests below.
	            knownResourceId = newId;
	        }
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        
       	objectexitIdsCreated.add(newId);// Store the IDs from every resource created; delete on cleanup
    }

    protected void createPersonRefs() throws Exception {
        PersonClient personAuthClient = new PersonClient();
        // Create a temporary PersonAuthority resource, and its corresponding refName by which it can be identified.
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
        		PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        Response res = personAuthClient.create(multipart);
        try {
	        assertStatusCode(res, "createPersonRefs (not a surefire test)");
	        personAuthCSID = extractId(res);
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
        // Create temporary Person resources, and their corresponding refNames by which they can be identified.
        String csid = "";

        csid = createPerson("Owen the Cur", "Owner", "owenCurOwner", authRefName);
        personIdsCreated.add(csid);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Davenport", "Depositor", "davenportDepositor", authRefName);
        personIdsCreated.add(csid);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
    }

    protected String createPerson(String firstName, String surName, String shortId, String authRefName) throws Exception {
    	String result = null;
    	
        PersonClient personAuthClient = new PersonClient();
        Map<String, String> personInfo = new HashMap<String, String>();
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
	        assertStatusCode(res, "createPerson (not a surefire test)");
	        result = extractId(res);
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        
        return result;
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        testSetup(STATUS_OK, ServiceRequestType.READ);
        ObjectExitClient objectexitClient = new ObjectExitClient();
        Response res = objectexitClient.read(knownResourceId);
        ObjectexitCommon objectexit = null;
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	        objectexit = (ObjectexitCommon) extractPart(input, objectexitClient.getCommonPartName(), ObjectexitCommon.class);
	        Assert.assertNotNull(objectexit);
	        logger.debug(objectAsXmlString(objectexit, ObjectexitCommon.class));
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        // Check a couple of fields
        Assert.assertEquals(objectexit.getDepositor(), depositorRefName);
        Assert.assertEquals(objectexit.getExitNumber(), exitNumber);

        // Get the auth refs and check them
        Response res2 = objectexitClient.getAuthorityRefs(knownResourceId);
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
        logger.debug("Authority references, found " + numAuthRefsFound);
        //Assert.assertEquals(numAuthRefsFound, NUM_AUTH_REFS_EXPECTED,
        //                    "Did not find all expected authority references! " +
        //                    "Expected " + NUM_AUTH_REFS_EXPECTED + ", found " + numAuthRefsFound);
        if (logger.isDebugEnabled()) {
            int i = 0;
            for (AuthorityRefList.AuthorityRefItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] Field:" + item.getSourceField() + "= " + item.getAuthDisplayName() + item.getItemDisplayName());
                logger.debug(testName + ": list-item[" + i + "] refName=" + item.getRefName());
                logger.debug(testName + ": list-item[" + i + "] URI=" + item.getUri());
                i++;
            }
        }
    }

    /**
     * Deletes all resources created by tests, after all tests have been run.
     * <p/>
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     * @throws Exception 
     */
    @Override
	@AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            logger.debug("Skipping Cleanup phase ...");
            return;
        }
        logger.debug("Cleaning up temporary resources created for testing ...");
        //
        // First delete the referring records
        //
        ObjectExitClient objectexitClient = new ObjectExitClient();
        for (String resourceId : objectexitIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            objectexitClient.delete(resourceId).close();
        }
        //
        // Next, delete the terms
        //
        PersonClient personAuthClient = new PersonClient();
        // Delete Person resource(s) (before PersonAuthority resources).
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId).close();
        }
        //
        // Lastly, delete PersonAuthority resource(s).
        // Note: Any non-success response is ignored and not reported.
        //
        if (personAuthCSID != null) {
            personAuthClient.delete(personAuthCSID).close();
            // Delete Loans In resource(s).
        }
    }

	@Override
	protected Class<AbstractCommonList> getCommonListType() {
		// TODO Auto-generated method stub
		return null;
	}

}
