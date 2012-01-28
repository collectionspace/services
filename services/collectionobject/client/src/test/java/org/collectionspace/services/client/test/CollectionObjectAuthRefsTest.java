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
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.collectionobject.AssocEventOrganizationList;
import org.collectionspace.services.collectionobject.AssocEventPersonList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ContentOrganizationList;
import org.collectionspace.services.collectionobject.ContentPersonList;
import org.collectionspace.services.collectionobject.OwnerList;
import org.collectionspace.services.collectionobject.FieldCollectionSourceList;
import org.collectionspace.services.collectionobject.FieldCollectorList;
import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

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
public class CollectionObjectAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    @Override
    protected CollectionSpaceClient getClientInstance() {
    	throw new UnsupportedOperationException(); //FIXME: REM - See http://issues.collectionspace.org/browse/CSPACE-3498
    }
    
	@Override
	protected String getServiceName() {
		throw new UnsupportedOperationException(); //FIXME: REM - See http://issues.collectionspace.org/browse/CSPACE-3498
	}

   /** The logger. */
    private final String CLASS_NAME = CollectionObjectAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    /** The service path component. */
    final String SERVICE_PATH_COMPONENT = "collectionobjects";
    
    /** The person authority name. */
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    final String PERSON_AUTHORITY_NAME_DISPLAY = "TestPersonAuth_DisplayName";

    /** The organization authority name. */
    final String ORG_AUTHORITY_NAME = "TestOrgAuth";
    
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
    private String assocEventOrganizationRefName = null;
    private String assocEventPersonRefName = null;
    private String ownerRefName = null;
    private String fieldCollectionSourceRefName = null;
    private String fieldCollectorRefName = null;

    // FIXME: As of 2012-01-04, the two assocEvent... fields
    // and the ownerRefName field have been commented out in
    // the list of authRef fields in CollectionObject, in tenant bindings,
    // because those fields fall within to-be-created repeatable groups,
    // per CSPACE-3229.
    // As a result, the number of authority references expected to be found
    // is currently 4, rather than 7. - Aron
    public String toString(){
        String result = "CollectionObjectauthRefsTest: "
                        + "\r\npersonAuthCSID: "+personAuthCSID
                        + "\r\npersonAuthRefName: "+personAuthRefName
                        + "\r\norgAuthCSID: "+orgAuthCSID
                        + "\r\norgAuthRefName: "+orgAuthRefName
                        + "\r\n"
                        + "\r\n contentOrganizationRefName: "+contentOrganizationRefName
                        + "\r\n contentPersonRefName: "+contentPersonRefName
                        + "\r\n assocEventOrganizationRefName: "+assocEventOrganizationRefName
                        + "\r\n assocEventPersonRefName: "+assocEventPersonRefName
                        + "\r\n ownerRefName: "+ownerRefName
                        + "\r\n fieldCollectionSourceRefName: "+fieldCollectionSourceRefName
                        + "\r\n fieldCollectorRefName: "+fieldCollectorRefName;
        StringBuffer buff = new StringBuffer();

        return result;
    }

    /** The number of authority references expected. */
    private final int NUM_AUTH_REFS_EXPECTED = 4;
    
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
    @Test(dataProvider="testName")
    public void createWithAuthRefs(String testName) throws Exception {        
        // Create all the person refs and entities
        createPersonRefs();

        // Create all the organization refs and entities
        createOrganizationRefs();

        // Create an object record payload, containing
        // authority reference values in a number of its fields
        String identifier = createIdentifier();
        PoxPayloadOut multipart =
            createCollectionObjectInstance(
                "Obj Title",
                "ObjNum" + "-" + identifier,
                contentOrganizationRefName,
                contentPersonRefName,
                assocEventOrganizationRefName,
                assocEventPersonRefName,
                ownerRefName,
                fieldCollectionSourceRefName,
                fieldCollectorRefName
            );

        // Submit the request to the service and store the response.
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        String newCsid = null;
        try {
        	assertStatusCode(res, testName);
        	newCsid = extractId(res);
        } finally {
        	if (res != null) {
        		res.releaseConnection();
        	}
        }
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = newCsid;
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
    	PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    			displayName, shortIdentifier, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
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
    	PoxPayloadOut multipart =
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID,
    				personAuthRefName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }

    /**
     * Creates multiple Person items within a Person Authority,
     * and stores the refNames referring to each.
     */
    protected void createPersonRefs(){

        createPersonAuthority(PERSON_AUTHORITY_NAME_DISPLAY, PERSON_AUTHORITY_NAME);

        String csid = "";
        
        csid = createPerson("Connie", "ContactPerson", "connieContactPerson");
        contentPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
//        csid = createPerson("Ingrid", "ContentInscriber", "ingridContentInscriber");
//        contentInscriberRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
//        personIdsCreated.add(csid);
//
//        csid = createPerson("Pacifico", "ProductionPerson", "pacificoProductionPerson");
//        objectProductionPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
//        personIdsCreated.add(csid);
//
//        csid = createPerson("Dessie", "DescriptionInscriber", "dessieDescriptionInscriber");
//        descriptionInscriberRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
//        personIdsCreated.add(csid);

        csid = createPerson("Asok", "AssociatedEventPerson", "asokAssociatedEventPerson");
        assocEventPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
//        csid = createPerson("Andrew", "AssociatedPerson", "andrewAssociatedPerson");
//        assocPersonRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
//        personIdsCreated.add(csid);

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
        PoxPayloadOut multipart = OrgAuthorityClientUtils.createOrgAuthorityInstance(
    			displayName, shortIdentifier, orgAuthClient.getCommonPartName());
        ClientResponse<Response> res = orgAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
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
    	PoxPayloadOut multipart =
    		OrgAuthorityClientUtils.createOrganizationInstance(
    				orgAuthRefName, orgInfo, orgAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = orgAuthClient.createItem(orgAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
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

//        csid = createOrganization("Production Org", "Production Org Town", "productionOrg");
//        objectProductionOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
//        orgIdsCreated.add(csid);

        csid = createOrganization("Associated Event Org", "Associated Event Org City", "associatedEventOrg");
        assocEventOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
        orgIdsCreated.add(csid);

//        csid = createOrganization("Associated Org", "Associated Org City", "associatedOrg");
//        assocOrganizationRefName = OrgAuthorityClientUtils.getOrgRefName(orgAuthCSID, csid, null);
//        orgIdsCreated.add(csid);
    }


    // Success outcomes
    /**
     * Read and check auth refs.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName",
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);
        //
        // First read the object
        //
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        ClientResponse<String> res = collectionObjectClient.read(knownResourceId);
        CollectionobjectsCommon collectionObject = null;
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        collectionObject = (CollectionobjectsCommon) extractPart(input,
	        		collectionObjectClient.getCommonPartName(), CollectionobjectsCommon.class);
	        Assert.assertNotNull(collectionObject);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Next, get all of the auth refs and check that the expected number is returned
        //
        ClientResponse<AuthorityRefList> res2 = collectionObjectClient.getAuthorityRefs(knownResourceId);
        AuthorityRefList list = null;
        try {
	        assertStatusCode(res2, testName);        
	        list = res2.getEntity();
        } finally {
        	if (res2 != null) {
        		res2.releaseConnection();
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
               
        // Check a sample of one or more person authority ref fields
        // Assert.assertEquals(collectionObject.getAssocPersons().getAssocPerson().get(0), assocPersonRefName);
        Assert.assertEquals(collectionObject.getOwners().getOwner().get(0), ownerRefName);
        Assert.assertEquals(collectionObject.getFieldCollectionSources().getFieldCollectionSource().get(0), fieldCollectionSourceRefName);

        // Check a sample of one or more organization authority ref fields
        Assert.assertEquals(collectionObject.getContentOrganizations().getContentOrganization().get(0), contentOrganizationRefName);
        Assert.assertEquals(collectionObject.getAssocEventOrganizations().getAssocEventOrganization().get(0), assocEventOrganizationRefName);

        // Optionally output additional data about list members for debugging.
        logger.info(this.toString());
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
   private PoxPayloadOut createCollectionObjectInstance(
                String title,
                String objNum,
                String contentOrganization,
                String contentPerson,
                String assocEventOrganization,
                String assocEventPerson,
                String owner,
                String fieldCollectionSource,
                String fieldCollector ) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
        TitleGroupList titleGroupList = new TitleGroupList();
        List<TitleGroup> titleGroups = titleGroupList.getTitleGroup();
        TitleGroup titleGroup = new TitleGroup();
        titleGroup.setTitle("a title");
        titleGroups.add(titleGroup);
        collectionObject.setTitleGroupList(titleGroupList);
        collectionObject.setObjectNumber(objNum);

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

//        AssocOrganizationList assocOrganizationList = new AssocOrganizationList();
//        List<String> assocOrganizations = assocOrganizationList.getAssocOrganization();
//        assocOrganizations.add(assocOrganization);
//        collectionObject.setAssocOrganizations(assocOrganizationList);
//
//        AssocPersonList assocPersonList = new AssocPersonList();
//        List<String> assocPersons = assocPersonList.getAssocPerson();
//        assocPersons.add(assocPerson);
//        collectionObject.setAssocPersons(assocPersonList);
        
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

        PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart =
            multipart.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new CollectionObjectClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, collectionObject common");
            logger.debug(objectAsXmlString(collectionObject, CollectionobjectsCommon.class));
        }

        return multipart;
    }

}
