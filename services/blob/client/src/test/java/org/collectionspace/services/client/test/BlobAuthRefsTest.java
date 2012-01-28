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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.blob.BlobsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BlobAuthRefsTest, carries out Authority References tests against a deployed and running Blob (aka Loans Out) Service.
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class BlobAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = BlobAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String PERSON_AUTHORITY_NAME = "BlobPersonAuth";
    private String knownResourceId = null;
    private List<String> blobIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String depositorRefName = null;
    private String blobName = null;

    @Override
	public String getServicePathComponent() {
		return BlobClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return BlobClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    @Override
    protected AbstractCommonList getCommonList(ClientResponse<AbstractCommonList> response) {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    private PoxPayloadOut createBlobInstance(String depositorRefName) {
    	BlobClient blobClient = new BlobClient();
        this.depositorRefName = depositorRefName;
        this.blobName = "testblob-"+createIdentifier();
        BlobsCommon blob = new BlobsCommon();
        blob.setName(this.blobName);

        PoxPayloadOut multipart = new PoxPayloadOut(BlobClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(blobClient.getCommonPartName(), blob);
        logger.debug("to be created, blob common: " + objectAsXmlString(blob, BlobsCommon.class));
        return multipart;
    }

    @Test(dataProvider = "testName")
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        createPersonRefs();// Create all the person refs and entities
        // Create a new Loans In resource. One or more fields in this resource will be PersonAuthority
        //    references, and will refer to Person resources by their refNames.
        BlobClient blobClient = new BlobClient();
        PoxPayloadOut multipart = createBlobInstance(depositorRefName);
        ClientResponse<Response> res = blobClient.create(multipart);
        try {
	        assertStatusCode(res, testName);
	        if (knownResourceId == null) {// Store the ID returned from the first resource created for additional tests below.
	            knownResourceId = extractId(res);
	        }
	        blobIdsCreated.add(extractId(res));// Store the IDs from every resource created; delete on cleanup
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    protected void createPersonRefs() {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Create a temporary PersonAuthority resource, and its corresponding refName by which it can be identified.
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        try {
	        assertStatusCode(res, "createPersonRefs (not a surefire test)");
	        personAuthCSID = extractId(res);
	        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
	        // Create temporary Person resources, and their corresponding refNames by which they can be identified.
	        String csid = "";
	
	        csid = createPerson("Owen the Cur", "Owner", "owenCurOwner", authRefName);
	        personIdsCreated.add(csid);
	        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
	
	        csid = createPerson("Davenport", "Depositor", "davenportDepositor", authRefName);
	        personIdsCreated.add(csid);
	        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    protected String createPerson(String firstName, String surName, String shortId, String authRefName) {
    	String result = null;
    	
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String, String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonInstance(personAuthCSID, authRefName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        try {
        	assertStatusCode(res, "createPerson (not a surefire test)");
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        return result;
    }

    // @Test annotation commented out by Aron 2010-12-02 until blob payload is set to the
    // actual payload - it currently appears to be cloned from another record type - and
    // it's determined that this payload has at least one authority field.
    //
    // If there are no authref fields in the payload, we might remove this test class outright.
    //
    // @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        testSetup(STATUS_OK, ServiceRequestType.READ);
        BlobClient blobClient = new BlobClient();
        ClientResponse<String> res = blobClient.read(knownResourceId);
        BlobsCommon blob = null;
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        blob = (BlobsCommon) extractPart(input, blobClient.getCommonPartName(), BlobsCommon.class);
	        Assert.assertNotNull(blob);
	        logger.debug(objectAsXmlString(blob, BlobsCommon.class));
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Check a couple of fields
        Assert.assertEquals(blob.getName(), blobName);

        // Get the auth refs and check them
        ClientResponse<AuthorityRefList> res2 = blobClient.getAuthorityRefs(knownResourceId);
        try {
	        assertStatusCode(res2, testName);
	        AuthorityRefList list = res2.getEntity();
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
        } finally {
        	if (res2 != null) {
        		res2.releaseConnection();
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
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            logger.debug("Skipping Cleanup phase ...");
            return;
        }
        logger.debug("Cleaning up temporary resources created for testing ...");
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete Person resource(s) (before PersonAuthority resources).
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId);
        }
        // Delete PersonAuthority resource(s).
        // Note: Any non-success response is ignored and not reported.
        if (personAuthCSID != null) {
            personAuthClient.delete(personAuthCSID);
            // Delete Loans In resource(s).
            BlobClient blobClient = new BlobClient();
            for (String resourceId : blobIdsCreated) {
                // Note: Any non-success responses are ignored and not reported.
                blobClient.delete(resourceId);
            }
        }
    }
}
