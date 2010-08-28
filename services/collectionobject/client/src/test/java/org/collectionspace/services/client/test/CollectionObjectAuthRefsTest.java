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
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.collectionobject.AssocEventOrganizationList;
import org.collectionspace.services.collectionobject.AssocEventPersonList;
import org.collectionspace.services.collectionobject.AssocOrganizationList;
import org.collectionspace.services.collectionobject.AssocPersonList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ContentOrganizationList;
import org.collectionspace.services.collectionobject.ContentPersonList;
import org.collectionspace.services.collectionobject.FieldCollectionSourceList;
import org.collectionspace.services.collectionobject.FieldCollectorList;
import org.collectionspace.services.collectionobject.OwnerList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectAuthRefsTest, carries out tests against a
 * deployed and running CollectionObject Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class CollectionObjectAuthRefsTest extends BaseServiceTest {

   /** The logger. */
    private final String CLASS_NAME = CollectionObjectAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    /** The service path component. */
    final String SERVICE_PATH_COMPONENT = "collectionobjects";
    
    /** The person authority name. */
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";

    /** The organization authority name. */
    final String ORG_AUTHORITY_NAME = "TestOrgAuth";
    
    /** The known resource id. */
    private String knownResourceId = null;
    
    /** The collection object ids created. */
    private List<String> collectionObjectIdsCreated = new ArrayList<String>();
    
    /** The person ids created. */
    private List<String> personIdsCreated = new ArrayList<String>();
    
    /** The person authority csid and refName. */
    private String personAuthCSID = null; 
    private String personAuthRefName = null;
    
    /** The organization ids created. */
    private List<String> orgIdsCreated = new ArrayList<String>();

    /** The org authority csid and refName. */
    private String orgAuthCSID = null;
    private String orgAuthRefName = null;
    
    private String contentOrganizationRefName = null;
    private String contentPersonRefName = null;
    private String contentInscriberRefName = null;
    private String descriptionInscriberRefName = null;
    private String objectProductionPersonRefName = null;
    private String objectProductionOrganizationRefName = null;
    private String assocEventOrganizationRefName = null;
    private String assocEventPersonRefName = null;
    private String assocOrganizationRefName = null;
    private String assocPersonRefName = null;
    private String ownerRefName = null;
    private String fieldCollectionSourceRefName = null;
    private String fieldCollectorRefName = null;

    /** The number of authority references expected. */
    private final int NUM_AUTH_REFS_EXPECTED = 13;

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
    /**
     * Creates the with auth refs.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        
        // Create all the person refs and entities
        createPersonRefs();

        // Create all the organization refs and entities
        createOrganizationRefs();

        // Create an object record payload, containing
        // authority reference values in a number of its fields
        String identifier = createIdentifier();
        MultipartOutput multipart =
            createCollectionObjectInstance(
                "Obj Title",
                "ObjNum" + "-" + identifier,
                contentOrganizationRefName,
                contentPersonRefName,
                contentInscriberRefName,
                descriptionInscriberRefName,
                objectProductionPersonRefName,
                objectProductionOrganizationRefName,
                assocEventOrganizationRefName,
                assocEventPersonRefName,
                assocOrganizationRefName,
                assocPersonRefName,
                ownerRefName,
                fieldCollectionSourceRefName,
                fieldCollectorRefName
            );

        // Submit the request to the service and store the response.
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        ClientResponse<Response> res = collectionObjectClient.create(multipart);

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
        collectionObjectIdsCreated.add(extractId(res));
    }

    /**
     * Creates a Person Authority.
     *
     * @param displayName the display name of the authority
     * @param shortIdentifier the short identifier for the authority
     */
    private void createPersonAuthority(String displayName, String shortIdentifier) {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
    	MultipartOutput multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    			displayName, shortIdentifier, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        personAuthCSID = extractId(res);
        personAuthRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
    }

    /**
     * Creates a person item.
     *
     * @param firstName the person's first name
     * @param surName the person's surname
     * @param shortIdentifier the short identifier for the item
     * @return the CSID of the newly-created person record
     */
    protected String createPerson(String firstName, String surName, String shortIdentifier ) {
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
    	MultipartOutput multipart =
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID,
    				personAuthRefName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }

    /**
     * Creates multiple Person items within a Person Authority,
     * and stores the refNames referring to each.
     */
    protected void createPersonRefs(){

        createPersonAuthority(PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME);

        String csid = "";
        
        csid = createPerson("Connie", "ContactPerson", "connieContactPerson");
        contentPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
        csid = createPerson("Ingrid", "ContentInscriber", "ingridContentInscriber");
        contentInscriberRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Pacifico", "ProductionPerson", "pacificoProductionPerson");
        objectProductionPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Dessie", "DescriptionInscriber", "dessieDescriptionInscriber");
        descriptionInscriberRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Asok", "AssociatedEventPerson", "asokAssociatedEventPerson");
        assocEventPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
        csid = createPerson("Andrew", "AssociatedPerson", "andrewAssociatedPerson");
        assocPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Owen", "Owner", "owenOwner");
        ownerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Sally", "Field-CollectionSource", "sallyFieldCollectionSource");
        fieldCollectionSourceRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Fred", "Lector", "fredLector");
        fieldCollectorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
    }
    
    /**
     * Creates an organization authority.
     *
     * @param displayName the display name of the authority
     * @param shortIdentifier the short identifier for the authority
     */
    private void createOrgAuthority(String displayName, String shortIdentifier) {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        MultipartOutput multipart = OrgAuthorityClientUtils.createOrgAuthorityInstance(
    			displayName, shortIdentifier, orgAuthClient.getCommonPartName());
        ClientResponse<Response> res = orgAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        orgAuthCSID = extractId(res);
        orgAuthRefName = OrgAuthorityClientUtils.getAuthorityRefName(orgAuthCSID, null);
    }

    /**
     * Creates an organization item.
     *
     * @param shortName the organization's short name
     * @param foundingPlace the organization's founding place
     * @param shortIdentifier the short identifier for the item
     * @return the CSID of the newly-created organization record
     */
    protected String createOrganization(String shortName, String foundingPlace, String shortIdentifier ) {
        Map<String, String> orgInfo = new HashMap<String,String>();
        orgInfo.put(OrganizationJAXBSchema.SHORT_NAME, shortName);
        orgInfo.put(OrganizationJAXBSchema.FOUNDING_PLACE, foundingPlace);
        orgInfo.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
    	MultipartOutput multipart =
    		OrgAuthorityClientUtils.createOrganizationInstance(
    				orgAuthRefName, orgInfo, orgAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = orgAuthClient.createItem(orgAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }
    
   /**
     * Creates multiple Organization items within an Organization Authority,
     * and stores the refNames referring to each.
     */
    private void createOrganizationRefs() {

        createOrgAuthority(ORG_AUTHORITY_NAME, ORG_AUTHORITY_NAME);

        String csid = "";

        csid = createOrganization("Content Org", "Content Org Town", "contentOrg");
        contentOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
        orgIdsCreated.add(csid);

        csid = createOrganization("Production Org", "Production Org Town", "productionOrg");
        objectProductionOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
        orgIdsCreated.add(csid);

        csid = createOrganization("Associated Event Org", "Associated Event Org City", "associatedEventOrg");
        assocEventOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
        orgIdsCreated.add(csid);

        csid = createOrganization("Associated Org", "Associated Org City", "associatedOrg");
        assocOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
        orgIdsCreated.add(csid);
    }


    // Success outcomes
    /**
     * Read and check auth refs.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        ClientResponse<MultipartInput> res = collectionObjectClient.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ".read: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        MultipartInput input = (MultipartInput) res.getEntity();
        CollectionobjectsCommon collectionObject = (CollectionobjectsCommon) extractPart(input,
        		collectionObjectClient.getCommonPartName(), CollectionobjectsCommon.class);
        Assert.assertNotNull(collectionObject);

        // Get all of the auth refs and check that the expected number is returned
        ClientResponse<AuthorityRefList> res2 = collectionObjectClient.getAuthorityRefs(knownResourceId);
        statusCode = res2.getStatus();
        
        if(logger.isDebugEnabled()){
            logger.debug(testName + ".getAuthorityRefs: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        AuthorityRefList list = res2.getEntity();
        
        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        if(logger.isDebugEnabled()){
            logger.debug("Expected " + NUM_AUTH_REFS_EXPECTED +
                " authority references, found " + numAuthRefsFound);
        }
        Assert.assertEquals(numAuthRefsFound, NUM_AUTH_REFS_EXPECTED,
            "Did not find all expected authority references! " +
            "Expected " + NUM_AUTH_REFS_EXPECTED + ", found " + numAuthRefsFound);
               
        // Check a sample of one or more person authority ref fields
        Assert.assertEquals(collectionObject.getInscriptionContentInscriber(), contentInscriberRefName);
        Assert.assertEquals(collectionObject.getAssocPersons().getAssocPerson().get(0), assocPersonRefName);
        Assert.assertEquals(collectionObject.getOwners().getOwner().get(0), ownerRefName);
        Assert.assertEquals(collectionObject.getFieldCollectionSources().getFieldCollectionSource().get(0), fieldCollectionSourceRefName);

        // Check a sample of one or more organization authority ref fields
        Assert.assertEquals(collectionObject.getContentOrganizations().getContentOrganization().get(0), contentOrganizationRefName);
        Assert.assertEquals(collectionObject.getAssocEventOrganizations().getAssocEventOrganization().get(0), assocEventOrganizationRefName);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){;
            int i = 0;
            for(AuthorityRefList.AuthorityRefItem item : items){
                logger.debug(testName + ": list-item[" + i + "] Field:" +
                		item.getSourceField() + " =" +
                        " item display name = " + item.getAuthDisplayName() +
                        " auth display name = " + item.getItemDisplayName());
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
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        for (String resourceId : collectionObjectIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            collectionObjectClient.delete(resourceId).releaseConnection();
        }
        // Note: Any non-success response is ignored and not reported.
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete persons before PersonAuth
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId).releaseConnection();
        }
        personAuthClient.delete(personAuthCSID).releaseConnection();
        // Note: Any non-success response is ignored and not reported.
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        // Delete organizations before OrgAuth
        for (String resourceId : orgIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            orgAuthClient.deleteItem(orgAuthCSID, resourceId).releaseConnection();
        }
        orgAuthClient.delete(orgAuthCSID).releaseConnection();
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

   /**
    * Creates the collection object instance.
    *
    * @param title the title
    * @param objNum the obj num
    * @param contentOrganization the content organization
    * @param contentPeople the content people
    * @param contentPerson the content person
    * @param inscriber the inscriber
    * @return the multipart output
    */
   private MultipartOutput createCollectionObjectInstance(
                String title,
                String objNum,
                String contentOrganization,
                String contentPerson,
                String contentInscriber,
                String descriptionInscriber,
                String objectProductionPerson,
                String objectProductionOrganization,
                String assocEventOrganization,
                String assocEventPerson,
                String assocOrganization,
                String assocPerson,
                String owner,
                String fieldCollectionSource,
                String fieldCollector ) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
        collectionObject.setTitle(title);
        collectionObject.setObjectNumber(objNum);
        collectionObject.setInscriptionContentInscriber(contentInscriber);
        collectionObject.setInscriptionDescriptionInscriber(descriptionInscriber);
        collectionObject.setObjectProductionPerson(objectProductionPerson);
        collectionObject.setObjectProductionOrganization(objectProductionOrganization);

        ContentOrganizationList contentOrganizationList = new ContentOrganizationList();
        List<String> contentOrganizations = contentOrganizationList.getContentOrganization();
        contentOrganizations.add(contentOrganization);
        collectionObject.setContentOrganizations(contentOrganizationList);

        ContentPersonList contentPersonList = new ContentPersonList();
        List<String> contentPersons = contentPersonList.getContentPerson();
        contentPersons.add(contentPerson);
        collectionObject.setContentPersons(contentPersonList);

        AssocEventOrganizationList assocEventOrganizationList = new AssocEventOrganizationList();
        List<String> assocEventOrganizations = assocEventOrganizationList.getAssocEventOrganization();
        assocEventOrganizations.add(assocEventOrganization);
        collectionObject.setAssocEventOrganizations(assocEventOrganizationList);

        AssocEventPersonList assocEventPersonList = new AssocEventPersonList();
        List<String> assocEventPersons = assocEventPersonList.getAssocEventPerson();
        assocEventPersons.add(assocEventPerson);
        collectionObject.setAssocEventPersons(assocEventPersonList);

        AssocOrganizationList assocOrganizationList = new AssocOrganizationList();
        List<String> assocOrganizations = assocOrganizationList.getAssocOrganization();
        assocOrganizations.add(assocOrganization);
        collectionObject.setAssocOrganizations(assocOrganizationList);

        AssocPersonList assocPersonList = new AssocPersonList();
        List<String> assocPersons = assocPersonList.getAssocPerson();
        assocPersons.add(assocPerson);
        collectionObject.setAssocPersons(assocPersonList);
        
        OwnerList ownerList = new OwnerList();
        List<String> owners = ownerList.getOwner();
        owners.add(owner);
        collectionObject.setOwners(ownerList);
        
        FieldCollectionSourceList fieldCollectionSourceList = new FieldCollectionSourceList();
        List<String> fieldCollectionSources = fieldCollectionSourceList.getFieldCollectionSource();
        fieldCollectionSources.add(fieldCollectionSource);
        collectionObject.setFieldCollectionSources(fieldCollectionSourceList);
        
        FieldCollectorList FieldCollectorList = new FieldCollectorList();
        List<String> fieldCollectors = FieldCollectorList.getFieldCollector();
        fieldCollectors.add(fieldCollector);
        collectionObject.setFieldCollectors(FieldCollectorList);

        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", new CollectionObjectClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, collectionObject common");
            logger.debug(objectAsXmlString(collectionObject, CollectionobjectsCommon.class));
        }

        return multipart;
    }

}
