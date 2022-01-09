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

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.HitClient;
import org.collectionspace.services.client.OrganizationClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.hit.HitsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.organization.OrgTermGroup;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrganizationAuthRefDocsTest, carries out tests against a
 * deployed and running Organization Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class OrganizationAuthRefDocsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = OrganizationAuthRefDocsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = "hits";
    final String ORGANIZATION_AUTHORITY_NAME = "TestOrganizationAuth";
    private String knownHitId = null;
    private List<String> hitIdsCreated = new ArrayList<String>();
    private List<String> orgIdsCreated = new ArrayList<String>();
    private String orgAuthCSID = null;
    //private String orgAuthRefName = null;
    private String currentOwnerOrgCSID = null;
    private String currentOwnerRefName = null;
    private String depositorRefName = null;
    private String conditionCheckerAssessorRefName = null;
    private String insurerRefName = null;
    private String valuerRefName = null;
    private final int NUM_AUTH_REF_DOCS_EXPECTED = 1;
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

	@Override
	protected String getServiceName() {
		throw new UnsupportedOperationException(); //FIXME: REM - See http://issues.collectionspace.org/browse/CSPACE-3498
	}

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
    @Test(dataProvider="testName", dataProviderClass=BaseServiceTest.class)
    public void createHitWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        // Create all the organization refs and entities
        createOrgRefs();

        HitClient hitClient = new HitClient();
        PoxPayloadOut hitPayload = createHitInstance(
                "entryNumber-" + identifier,
                CURRENT_DATE_UTC,
                currentOwnerRefName,
                // Use currentOwnerRefName twice to test fix for CSPACE-2863
                currentOwnerRefName,    //depositorRefName,
                conditionCheckerAssessorRefName,
                insurerRefName,
                valuerRefName );

        Response res = hitClient.create(hitPayload);
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
	        String newHitId = extractId(res);
            Assert.assertNotNull(newHitId, "Could not create a new Hit record.");

	        // Store the ID returned from the first resource created
	        // for additional tests below.
	        if (knownHitId == null) {
	            knownHitId = newHitId;
	            if (logger.isDebugEnabled()) {
	                logger.debug(testName + ": knownHitId=" + knownHitId);
	            }
	        }

	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        hitIdsCreated.add(newHitId);
        } finally {
        	res.close();
        }

    }

    /**
     * Creates the organization refs.
     * @throws Exception
     */
    protected void createOrgRefs() throws Exception{
        OrganizationClient orgAuthClient = new OrganizationClient();
        //orgAuthRefName =
    	//	OrgAuthorityClientUtils.createOrgAuthRefName(ORGANIZATION_AUTHORITY_NAME, null);
        PoxPayloadOut multipart = OrgAuthorityClientUtils.createOrgAuthorityInstance(
    			ORGANIZATION_AUTHORITY_NAME, ORGANIZATION_AUTHORITY_NAME, orgAuthClient.getCommonPartName());
        Response res = orgAuthClient.create(multipart);
        try {
	        int statusCode = res.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	        orgAuthCSID = extractId(res);
	        Assert.assertNotNull(orgAuthCSID, "Could not create a new Organization authority record.");
        } finally {
        	res.close();
        }

		currentOwnerOrgCSID = createOrganization("olivierOwnerCompany", "Olivier Owner Company", "Olivier Owner Company");
        orgIdsCreated.add(currentOwnerOrgCSID);
        currentOwnerRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, currentOwnerOrgCSID, orgAuthClient);

		String newOrgCSID =
                        createOrganization("debbieDepositorAssocs", "Debbie Depositor & Associates", "Debbie Depositor & Associates");
        depositorRefName =
        	OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, newOrgCSID, orgAuthClient);
        orgIdsCreated.add(newOrgCSID);

		newOrgCSID = createOrganization("andrewCheckerAssessorLtd", "Andrew Checker-Assessor Ltd.", "Andrew Checker-Assessor Ltd.");
		conditionCheckerAssessorRefName =
        	OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, newOrgCSID, orgAuthClient);
        orgIdsCreated.add(newOrgCSID);

		newOrgCSID = createOrganization("ingridInsurerBureau", "Ingrid Insurer Bureau", "Ingrid Insurer Bureau");
		insurerRefName =
        	OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, newOrgCSID, orgAuthClient);
        orgIdsCreated.add(newOrgCSID);

		newOrgCSID = createOrganization("vinceValuerLLC", "Vince Valuer LLC", "Vince Valuer LLC");
		valuerRefName =
        	OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, newOrgCSID, orgAuthClient);
        orgIdsCreated.add(newOrgCSID);
    }

    protected String createOrganization(String shortId, String shortName, String longName) throws Exception {
    	String result = null;

        OrganizationClient orgAuthClient = new OrganizationClient();
        Map<String, String> orgInfo = new HashMap<String,String>();
        orgInfo.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId);

        List<OrgTermGroup> orgTerms = new ArrayList<OrgTermGroup>();
        OrgTermGroup term = new OrgTermGroup();
        term.setTermDisplayName(shortName);
        term.setTermName(shortName);
        term.setMainBodyName(longName);
        orgTerms.add(term);
        PoxPayloadOut multipart =
                OrgAuthorityClientUtils.createOrganizationInstance(null, //orgAuthRefName
                orgInfo, orgTerms, orgAuthClient.getItemCommonPartName());

        Response res = orgAuthClient.createItem(orgAuthCSID, multipart);
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

    // Success outcomes here
    @Test(dataProvider="testName", dataProviderClass=BaseServiceTest.class,
        dependsOnMethods = {"createHitWithAuthRefs"})
    public void readAndCheckAuthRefDocs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Get the auth ref docs and check them
       OrganizationClient orgAuthClient = new OrganizationClient();
       Response refDocListResp = orgAuthClient.getReferencingObjects(orgAuthCSID, currentOwnerOrgCSID);
       AuthorityRefDocList list = null;
       try {
    	   assertStatusCode(refDocListResp, testName);
    	   list = refDocListResp.readEntity(AuthorityRefDocList.class);
    	   Assert.assertNotNull(list);
       } finally {
    	   if (refDocListResp != null) {
    		   refDocListResp.close();
           }
       }

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        int nHitsFound = 0;
        final int EXPECTED_HITS = 3;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<AuthorityRefDocList.AuthorityRefDocItem> items =
                    list.getAuthorityRefDocItem();
            int i = 0;
            logger.debug(testName + ": Docs that use: " + currentOwnerRefName);
            for (AuthorityRefDocList.AuthorityRefDocItem item : items){
                logger.debug(testName + ": list-item[" + i + "] " +
                		item.getDocType() + "(" +
                		item.getDocId() + ") Name:[" +
                		item.getDocName() + "] Number:[" +
                		item.getDocNumber() + "] in field:[" +
                		item.getSourceField() + "]");
                if(knownHitId.equalsIgnoreCase(item.getDocId())) {
                	nHitsFound++;
                }
                i++;
            }
            //
            Assert.assertTrue((nHitsFound == EXPECTED_HITS), "Did not find Hit (twice more) with authref!");
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
    @AfterClass(alwaysRun=true)
    public void cleanUp() throws Exception {
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
        HitClient hitClient = new HitClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : hitIdsCreated) {
            hitClient.delete(resourceId).close();
        }
        // Delete persons before PersonAuth
        OrganizationClient personAuthClient = new OrganizationClient();
        for (String resourceId : orgIdsCreated) {
            personAuthClient.deleteItem(orgAuthCSID, resourceId).close();
        }
        if (orgAuthCSID != null) {
        	personAuthClient.delete(orgAuthCSID).close();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	private PoxPayloadOut createHitInstance(String entryNumber, String entryDate, String currentOwner, String depositor,
			String conditionCheckerAssessor, String insurer, String Valuer) throws Exception {
		HitsCommon hit = HitClientTestUtil.createHitInstance(entryNumber, currentOwner, depositor,
				conditionCheckerAssessor, insurer); // new HitsCommon();

		PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(new HitClient().getCommonPartName(), hit);

		if (logger.isDebugEnabled()) {
			logger.debug("to be created, hit common");
			logger.debug(objectAsXmlString(hit, HitsCommon.class));
		}

		return multipart;
	}
}
