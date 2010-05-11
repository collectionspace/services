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

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.intake.IntakesCommon;
//import org.collectionspace.services.intake.IntakesCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
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
public class OrganizationAuthRefDocsTest extends BaseServiceTest {

   private final Logger logger =
       LoggerFactory.getLogger(OrganizationAuthRefDocsTest.class);

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = "intakes";
    final String ORGANIZATION_AUTHORITY_NAME = "TestOrganizationAuth";
    private String knownIntakeId = null;
    private List<String> intakeIdsCreated = new ArrayList<String>();
    private List<String> orgIdsCreated = new ArrayList<String>();
    private int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
    private int OK_STATUS = Response.Status.OK.getStatusCode();
    private String orgAuthCSID = null; 
    private String currentOwnerOrgCSID = null; 
    private String currentOwnerRefName = null;
    private String depositorRefName = null;
    private String conditionCheckAssesorRefName = null;
    private String insurerRefName = null;
    private String fieldCollectorRefName = null;
    private String valuerRefName = null;
    private final int NUM_AUTH_REF_DOCS_EXPECTED = 1;

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
	protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createIntakeWithAuthRefs(String testName) throws Exception {

        testSetup(CREATED_STATUS, ServiceRequestType.CREATE,testName);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the organization refs and entities
        createOrgRefs();

        IntakeClient intakeClient = new IntakeClient();
        MultipartOutput multipart = createIntakeInstance(
                "entryNumber-" + identifier,
                "entryDate-" + identifier,
								currentOwnerRefName,
								depositorRefName,
								conditionCheckAssesorRefName,
								insurerRefName,
								fieldCollectorRefName,
								valuerRefName );

        ClientResponse<Response> res = intakeClient.create(multipart);
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
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
        	res.releaseConnection();
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownIntakeId == null){
            knownIntakeId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownIntakeId=" + knownIntakeId);
            }
        }
        
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        intakeIdsCreated.add(extractId(res));
    }
    
    /**
     * Creates the organization refs.
     */
    protected void createOrgRefs(){
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
    	String authRefName = 
    		OrgAuthorityClientUtils.createOrgAuthRefName(ORGANIZATION_AUTHORITY_NAME, false);
    	MultipartOutput multipart = OrgAuthorityClientUtils.createOrgAuthorityInstance(
    			ORGANIZATION_AUTHORITY_NAME, authRefName, orgAuthClient.getCommonPartName());
        ClientResponse<Response> res = orgAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, CREATED_STATUS);
        orgAuthCSID = extractId(res);
        
        currentOwnerRefName = OrgAuthorityClientUtils.createOrganizationRefName(
        							authRefName, "Olivier Owner", true);
				currentOwnerOrgCSID = createOrganization("Olivier", "Owner", currentOwnerRefName);
        orgIdsCreated.add(currentOwnerOrgCSID);
        
        depositorRefName = OrgAuthorityClientUtils.createOrganizationRefName(
									authRefName, "Debbie Depositor", true);
        orgIdsCreated.add(createOrganization("Debbie", "Depositor", depositorRefName));
        
        conditionCheckAssesorRefName = OrgAuthorityClientUtils.createOrganizationRefName(
									authRefName, "Andrew Assessor", true);
        orgIdsCreated.add(createOrganization("Andrew", "Assessor", conditionCheckAssesorRefName));
        
        insurerRefName = OrgAuthorityClientUtils.createOrganizationRefName(
									authRefName, "Ingrid Insurer", true);
        orgIdsCreated.add(createOrganization("Ingrid", "Insurer", insurerRefName));
        
        fieldCollectorRefName = OrgAuthorityClientUtils.createOrganizationRefName(
									authRefName, "Connie Collector", true);
        orgIdsCreated.add(createOrganization("Connie", "Collector", fieldCollectorRefName));
        
        valuerRefName = OrgAuthorityClientUtils.createOrganizationRefName(
									authRefName, "Vince Valuer", true);
        orgIdsCreated.add(createOrganization("Vince", "Valuer", valuerRefName));
        

    }
    
    protected String createOrganization(String shortName, String longName, String refName ) {
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        Map<String, String> orgInfo = new HashMap<String,String>();
        orgInfo.put(OrganizationJAXBSchema.SHORT_NAME, shortName);
        orgInfo.put(OrganizationJAXBSchema.LONG_NAME, longName);
    	MultipartOutput multipart = 
    		OrgAuthorityClientUtils.createOrganizationInstance(orgAuthCSID, 
    				refName, orgInfo, orgAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = orgAuthClient.createItem(orgAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, CREATED_STATUS);
    	return extractId(res);
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createIntakeWithAuthRefs"})
    public void readAndCheckAuthRefDocs(String testName) throws Exception {

        // Perform setup.
        testSetup(OK_STATUS, ServiceRequestType.READ,testName);
        
        // Get the auth ref docs and check them
       OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
       ClientResponse<AuthorityRefDocList> refDocListResp =
        	orgAuthClient.getReferencingObjects(orgAuthCSID, currentOwnerOrgCSID);

        int statusCode = refDocListResp.getStatus();

        if(logger.isDebugEnabled()){
            logger.debug(testName + ".getReferencingObjects: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        AuthorityRefDocList list = refDocListResp.getEntity();

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        boolean fFoundIntake = false;
        if(iterateThroughList && logger.isDebugEnabled()){
            List<AuthorityRefDocList.AuthorityRefDocItem> items =
                    list.getAuthorityRefDocItem();
            int i = 0;
            logger.debug(testName + ": Docs that use: " + currentOwnerRefName);
            for(AuthorityRefDocList.AuthorityRefDocItem item : items){
                logger.debug(testName + ": list-item[" + i + "] " +
                		item.getDocType() + "(" +
                		item.getDocId() + ") Name:[" +
                		item.getDocName() + "] Number:[" +
                		item.getDocNumber() + "] in field:[" +
                		item.getSourceField() + "]");
                if(!fFoundIntake && knownIntakeId.equalsIgnoreCase(item.getDocId())) {
               		fFoundIntake = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundIntake, "Did not find Intake with authref!");
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
        IntakeClient intakeClient = new IntakeClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : intakeIdsCreated) {
            ClientResponse<Response> res = intakeClient.delete(resourceId);
            res.releaseConnection();
        }
        // Delete persons before PersonAuth
        OrgAuthorityClient personAuthClient = new OrgAuthorityClient();
        for (String resourceId : orgIdsCreated) {
            ClientResponse<Response> res = personAuthClient.deleteItem(orgAuthCSID, resourceId);
            res.releaseConnection();
        }
        if (orgAuthCSID != null) {
        	personAuthClient.delete(orgAuthCSID).releaseConnection();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

   private MultipartOutput createIntakeInstance(String entryNumber,
    		String entryDate,
				String currentOwner,
				String depositor,
				String conditionCheckAssesor,
				String insurer,
				String fieldCollector,
				String Valuer ) {
        IntakesCommon intake = new IntakesCommon();
        intake.setEntryNumber(entryNumber);
        intake.setEntryDate(entryDate);
        intake.setCurrentOwner(currentOwner);
        intake.setDepositor(depositor);
        intake.setConditionCheckAssesor(conditionCheckAssesor);
        intake.setInsurer(insurer);
        intake.setFieldCollector(fieldCollector);
        intake.setValuer(Valuer);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", new IntakeClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, intake common");
            logger.debug(objectAsXmlString(intake, IntakesCommon.class));
        }

        return multipart;
    }
}
