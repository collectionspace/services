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
import org.collectionspace.services.client.AcquisitionClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.acquisition.AcquisitionsCommon;
import org.collectionspace.services.acquisition.AcquisitionFunding;
import org.collectionspace.services.acquisition.AcquisitionFundingList;
import org.collectionspace.services.acquisition.AcquisitionSourceList;
import org.collectionspace.services.acquisition.OwnerList;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcquisitionAuthRefsTest, carries out tests against a
 * deployed and running Acquisition Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class AcquisitionAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

	private final String CLASS_NAME = AcquisitionAuthRefsTest.class.getName();
	private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

	// Instance variables specific to this test.
	//    final String SERVICE_PATH_COMPONENT = AcquisitionClient.SERVICE_PATH_COMPONENT;//"acquisitions";
	final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
	private String knownResourceId = null;
	private List<String> acquisitionIdsCreated = new ArrayList<String>();
	private List<String> personIdsCreated = new ArrayList<String>();
	private String personAuthCSID = null; 
	private String acquisitionAuthorizerRefName = null;
	private List<String> acquisitionFundingSourcesRefNames = new ArrayList<String>();
	private List<String> ownersRefNames = new ArrayList<String>();
	private List<String> acquisitionSourcesRefNames = new ArrayList<String>();
	private final int NUM_AUTH_REFS_EXPECTED = 5;

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
	 */
	@Override
	protected CollectionSpaceClient getClientInstance() {
		throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
	 */
	@Override
	protected AbstractCommonList getCommonList(
			ClientResponse<AbstractCommonList> response) {
		throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
	}

	// ---------------------------------------------------------------
	// CRUD tests : CREATE tests
	// ---------------------------------------------------------------
	// Success outcomes
	@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
	public void createWithAuthRefs(String testName) throws Exception {
		// Perform setup.
		testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

		// Submit the request to the service and store the response.
		String identifier = createIdentifier();

		// Create all the person refs and entities
		createPersonRefs();

		PoxPayloadOut multipart = createAcquisitionInstance(
				"April 1, 2010",
				acquisitionAuthorizerRefName,
				acquisitionFundingSourcesRefNames,
				ownersRefNames,
				acquisitionSourcesRefNames);

		AcquisitionClient acquisitionClient = new AcquisitionClient();
		ClientResponse<Response> res = acquisitionClient.create(multipart);

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

		// Store the ID returned from the first resource created
		// for additional tests below.
		if (knownResourceId == null){
			knownResourceId = extractId(res);
			if (logger.isDebugEnabled()) {
				logger.debug(testName + ": knownResourceId=" + knownResourceId);
			}
		}

		// Store the IDs from every resource created by tests,
		// so they can be deleted after tests have been run.
		acquisitionIdsCreated.add(extractId(res));
	}

	protected void createPersonRefs(){
		PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
		PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
				PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
		ClientResponse<Response> res = personAuthClient.create(multipart);
		int statusCode = res.getStatus();

		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, STATUS_CREATED);
		personAuthCSID = extractId(res);

		String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);

		String csid = createPerson("Annie", "Authorizer", "annieAuth", authRefName);
		acquisitionAuthorizerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
		personIdsCreated.add(csid);

		/*
        csid = createPerson("Fran", "Funding-SourceOne", "franFundingSourceOne", authRefName);
        acquisitionFundingSourcesRefNames.add(PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null));
        personIdsCreated.add(csid);

        csid = createPerson("Fahd", "Funding-SourceTwo", "fahdFundingSourceTwo", authRefName);
        acquisitionFundingSourcesRefNames.add(PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null));
        personIdsCreated.add(csid);
		 */

		csid = createPerson("Owen", "OwnerOne", "owenOwnerOne", authRefName);
		ownersRefNames.add(PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null));
		personIdsCreated.add(csid);

		csid = createPerson("Orelia", "OwnerTwo", "oreliaOwnerTwo", authRefName);
		ownersRefNames.add(PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null));
		personIdsCreated.add(csid);

		csid = createPerson("Sammy", "SourceOne", "sammySourceOne", authRefName);
		acquisitionSourcesRefNames.add(PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null));
		personIdsCreated.add(csid);

		csid = createPerson("Serena", "SourceTwo", "serenaSourceTwo", authRefName);
		acquisitionSourcesRefNames.add(PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null));
		personIdsCreated.add(csid);
	}

	protected String createPerson(String firstName, String surName, String shortId, String authRefName ) {
		Map<String, String> personInfo = new HashMap<String,String>();
		personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
		personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
		personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
		PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
		PoxPayloadOut multipart = 
			PersonAuthorityClientUtils.createPersonInstance(personAuthCSID, 
					authRefName, personInfo, personAuthClient.getItemCommonPartName());
		ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
		int statusCode = res.getStatus();

		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, STATUS_CREATED);
		return extractId(res);
	}

	// Success outcomes
	@Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
			dependsOnMethods = {"createWithAuthRefs"})
			public void readAndCheckAuthRefs(String testName) throws Exception {
		// Perform setup.
		testSetup(STATUS_OK, ServiceRequestType.READ);

		// Submit the request to the service and store the response.
		AcquisitionClient acquisitionClient = new AcquisitionClient();
		ClientResponse<String> res = acquisitionClient.read(knownResourceId);
		AcquisitionsCommon acquisition = null;
		try {
	 		// Check the status code of the response: does it match
			// the expected response(s)?
			assertStatusCode(res, testName);
			PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
			acquisition = (AcquisitionsCommon) extractPart(input,
					acquisitionClient.getCommonPartName(), AcquisitionsCommon.class);
			Assert.assertNotNull(acquisition);
		} finally {
			if (res != null) {
                res.releaseConnection();
            }
		}

		// Check a couple of fields
		// Scalar fields
		Assert.assertEquals(acquisition.getAcquisitionAuthorizer(), acquisitionAuthorizerRefName);

		// In repeatable groups of fields
		/*
        AcquisitionFundingList acqFundingList = acquisition.getAcquisitionFundingList();
        List<AcquisitionFunding> acqFundings = acqFundingList.getAcquisitionFunding();
        List<String> acqFundingSourceRefNamesFound = new ArrayList();
        for (AcquisitionFunding acqFunding : acqFundings) {
            String acqFundingSourceRefName = acqFunding.getAcquisitionFundingSource();
            acqFundingSourceRefNamesFound.add(acqFundingSourceRefName);
        }
        Assert.assertTrue(acqFundingSourceRefNamesFound.containsAll(acquisitionFundingSourcesRefNames));
		 */

		// In scalar repeatable fields
		OwnerList ownersList = acquisition.getOwners();
		List<String> owners = ownersList.getOwner();
		for (String refName : owners) {
			Assert.assertTrue(ownersRefNames.contains(refName));
		}

		AcquisitionSourceList acquisitionSources = acquisition.getAcquisitionSources();
		List<String> sources = acquisitionSources.getAcquisitionSource();
		for (String refName : sources) {
			Assert.assertTrue(acquisitionSourcesRefNames.contains(refName));
		}
		//
		// Get the auth refs and check them
		//
		ClientResponse<AuthorityRefList> res2 =	acquisitionClient.getAuthorityRefs(knownResourceId);
		AuthorityRefList list = null;
		try {
			assertStatusCode(res2, testName);
			list = res2.getEntity();
			Assert.assertNotNull(list);
		} finally {
			if (res2 != null) {
				res2.releaseConnection();
            }
		}

		List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
		int numAuthRefsFound = items.size();
		if (logger.isDebugEnabled()){
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
		AcquisitionClient acquisitionClient = new AcquisitionClient();
		for (String resourceId : acquisitionIdsCreated) {
			// Note: Any non-success responses are ignored and not reported.
			ClientResponse<Response> res = acquisitionClient.delete(resourceId);
			res.releaseConnection();
		}
		PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
		// Delete persons before PersonAuth
		for (String resourceId : personIdsCreated) {
			// Note: Any non-success responses are ignored and not reported.
			ClientResponse<Response> res = personAuthClient.deleteItem(personAuthCSID, resourceId);
			res.releaseConnection();
		}
		// Note: Any non-success response is ignored and not reported.
		ClientResponse<Response> res = personAuthClient.delete(personAuthCSID);
		res.releaseConnection();
	}

	// ---------------------------------------------------------------
	// Utility methods used by tests above
	// ---------------------------------------------------------------
	@Override
	public String getServicePathComponent() {
		return AcquisitionClient.SERVICE_PATH_COMPONENT;
	}


	@Override
	protected String getServiceName() {
		return AcquisitionClient.SERVICE_NAME;
	}

	private PoxPayloadOut createAcquisitionInstance(
			String accessionDate,
			String acquisitionAuthorizer,
			List<String> acquisitionFundingSources,
			List<String> acqOwners,
			List<String> acquisitionSources) {

		AcquisitionsCommon acquisition = new AcquisitionsCommon();
		acquisition.setAccessionDate(accessionDate);
		acquisition.setAcquisitionAuthorizer(acquisitionAuthorizer);

		// AcquisitionFunding-related authrefs fields are *not* currently
		// enabled, as described in issue CSPACE-2818

		/*
        AcquisitionFundingList acqFundingsList = new AcquisitionFundingList();
        List<AcquisitionFunding> acqFundings = acqFundingsList.getAcquisitionFunding();
        int i = 0;
        for (String acqFundingSource: acquisitionFundingSources) {
            i++;
            AcquisitionFunding acqFunding = new AcquisitionFunding();
            acqFunding.setAcquisitionFundingSource(acqFundingSource);
            acqFunding.setAcquisitionFundingSourceProvisos("funding source provisos-" + i);
            acqFundings.add(acqFunding);
        }
        AcquisitionFunding addtlAcqFunding = new AcquisitionFunding();
        addtlAcqFunding.setAcquisitionFundingCurrency("USD");
        acqFundings.add(addtlAcqFunding);
        acquisition.setAcquisitionFundingList(acqFundingsList);
		 */

		OwnerList ownersList = new OwnerList();
		List<String> owners = ownersList.getOwner();
		for (String owner: acqOwners) {
			owners.add(owner);
		}
		acquisition.setOwners(ownersList);

		AcquisitionSourceList acqSourcesList = new AcquisitionSourceList();
		List<String> acqSources = acqSourcesList.getAcquisitionSource();
		for (String acqSource: acquisitionSources) {
			acqSources.add(acqSource);
		}
		acquisition.setAcquisitionSources(acqSourcesList);

		AcquisitionClient acquisitionClient = new AcquisitionClient();
		PoxPayloadOut multipart = new PoxPayloadOut(AcquisitionClient.SERVICE_PAYLOAD_NAME);
		PayloadOutputPart commonPart =
			multipart.addPart(acquisitionClient.getCommonPartName(), acquisition);

		if(logger.isDebugEnabled()){
			logger.debug("to be created, acquisition common");
			logger.debug(objectAsXmlString(acquisition, AcquisitionsCommon.class));
		}

		return multipart;
	}
}
