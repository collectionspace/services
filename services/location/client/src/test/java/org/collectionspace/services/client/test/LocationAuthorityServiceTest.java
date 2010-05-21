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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.ContactClientUtils;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.client.LocationAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.location.LocationauthoritiesCommon;
import org.collectionspace.services.location.LocationauthoritiesCommonList;
import org.collectionspace.services.location.LocationsCommon;
import org.collectionspace.services.location.LocationsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * LocationAuthorityServiceTest, carries out tests against a
 * deployed and running LocationAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class LocationAuthorityServiceTest extends AbstractServiceTestImpl {

    /** The logger. */
    private final Logger logger =
        LoggerFactory.getLogger(LocationAuthorityServiceTest.class);

    // Instance variables specific to this test.
    /** The SERVICE path component. */
    final String SERVICE_PATH_COMPONENT = "locationauthorities";
    
    /** The ITEM servicE path component. */
    final String ITEM_SERVICE_PATH_COMPONENT = "items";
    
    /** The CONTACT servicE path component. */
    final String CONTACT_SERVICE_PATH_COMPONENT = "contacts";
    
    /** The TEST name. */
    final String TEST_NAME = "Shelf 1";
    
    /** The TEST conditionNote. */
    final String TEST_CONDITION_NOTE = "Basically clean";
    
    /** The TEST death date. */
    final String TEST_CONDITION_NOTE_DATE = "June 11, 1979";
 
    /** The TEST securityNote. */
    final String TEST_SECURITY_NOTE = "Kind of safe";
    
    /** The TEST location type. */
    // TODO Make loc type be a controlled vocab term.
    final String TEST_LOCATION_TYPE = "Shelf";
    
    /** The TEST location type. */
    // TODO Make status type be a controlled vocab term.
    final String TEST_STATUS = "Approved";
    
    /** The known resource id. */
    private String knownResourceId = null;
    
    /** The known resource display name. */
    private String knownResourceDisplayName = null;
    
    /** The known resource ref name. */
    private String knownResourceRefName = null;
    
    /** The known locationType ref name. */
    private String knownLocationTypeRefName = null;
    
    /** The known item resource id. */
    private String knownItemResourceId = null;
    
    /** The known contact resource id. */
    private String knownContactResourceId = null;
    
    /** The n items to create in list. */
    private int nItemsToCreateInList = 3;
    
    /** The all resource ids created. */
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    
    /** The all item resource ids created. */
    private Map<String, String> allItemResourceIdsCreated =
        new HashMap<String, String>();
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new LocationAuthorityClient();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response) {
        return response.getEntity(LocationsCommonList.class);
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"create"})
    public void create(String testName) throws Exception {

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        String identifier = createIdentifier();
    	String displayName = "displayName-" + identifier;
    	String baseRefName = LocationAuthorityClientUtils.createLocationAuthRefName(displayName, false);
    	String fullRefName = LocationAuthorityClientUtils.createLocationAuthRefName(displayName, true);
    	MultipartOutput multipart = 
            LocationAuthorityClientUtils.createLocationAuthorityInstance(
    	    displayName, fullRefName, client.getCommonPartName());
        ClientResponse<Response> res = client.create(multipart);
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
        Assert.assertTrue(this.REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(this.REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, this.EXPECTED_STATUS_CODE);

        // Store the refname from the first resource created
        // for additional tests below.
        knownResourceRefName = baseRefName;

        String newID = LocationAuthorityClientUtils.extractId(res);
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = newID;
            knownResourceDisplayName = displayName;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

    /**
     * Creates the item.
     *
     * @param testName the test name
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"create"}, dependsOnMethods = {"create"})
    public void createItem(String testName) {
        setupCreate(testName);
        String newID = createItemInAuthority(knownResourceId, knownResourceRefName);
    }

    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    private String createItemInAuthority(String vcsid, String authRefName) {

        final String testName = "createItemInAuthority";
        if(logger.isDebugEnabled()){
            logger.debug(testName + ":...");
        }

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        String identifier = createIdentifier();
        String refName = LocationAuthorityClientUtils.createLocationRefName(authRefName, TEST_NAME, true);
        Map<String, String> shelf1Map = new HashMap<String,String>();
        // TODO Make loc type and status be controlled vocabs.
        shelf1Map.put(LocationJAXBSchema.NAME, TEST_NAME);
        shelf1Map.put(LocationJAXBSchema.CONDITION_NOTE, TEST_CONDITION_NOTE);
        shelf1Map.put(LocationJAXBSchema.CONDITION_NOTE_DATE, TEST_CONDITION_NOTE_DATE);
        shelf1Map.put(LocationJAXBSchema.SECURITY_NOTE, TEST_SECURITY_NOTE);
        shelf1Map.put(LocationJAXBSchema.LOCATION_TYPE, TEST_LOCATION_TYPE);
        shelf1Map.put(LocationJAXBSchema.STATUS, TEST_STATUS);
        MultipartOutput multipart = 
            LocationAuthorityClientUtils.createLocationInstance(vcsid, refName, shelf1Map,
                client.getItemCommonPartName() );
        ClientResponse<Response> res = client.createItem(vcsid, multipart);
        int statusCode = res.getStatus();
        String newID = LocationAuthorityClientUtils.extractId(res);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null){
            knownItemResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allItemResourceIdsCreated.put(newID, vcsid);

        return newID;
    }



    // Failure outcomes

    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }


    // ---------------------------------------------------------------
    // CRUD tests : CREATE LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
	 */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"createList"}, dependsOnGroups = {"create"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < nItemsToCreateInList; i++) {
            create(testName);
        }
    }

    /**
     * Creates the item list.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"createList"}, dependsOnMethods = {"createList"})
    public void createItemList(String testName) throws Exception {
        // Add items to the initially-created, known parent record.
        for (int j = 0; j < nItemsToCreateInList; j++) {
            createItem(testName);
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnGroups = {"create"})
    public void read(String testName) throws Exception {

        // Perform setup.
        setupRead();
        
        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //FIXME: remove the following try catch once Aron fixes signatures
        try {
            MultipartInput input = (MultipartInput) res.getEntity();
            LocationauthoritiesCommon locationAuthority = (LocationauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), LocationauthoritiesCommon.class);
            Assert.assertNotNull(locationAuthority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read by name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
            groups = {"read"}, dependsOnGroups = {"create"})
        public void readByName(String testName) throws Exception {

            // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readByName(knownResourceDisplayName);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //FIXME: remove the following try catch once Aron fixes signatures
        try {
            MultipartInput input = (MultipartInput) res.getEntity();
            LocationauthoritiesCommon locationAuthority = (LocationauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), LocationauthoritiesCommon.class);
            Assert.assertNotNull(locationAuthority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
	 * Read item.
	 *
	 * @param testName the test name
	 * @throws Exception the exception
	 */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"read"})
    public void readItem(String testName) throws Exception {

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether we've received a location.
        MultipartInput input = (MultipartInput) res.getEntity();
        LocationsCommon location = (LocationsCommon) extractPart(input,
                client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(location);
        boolean showFull = true;
        if(showFull && logger.isDebugEnabled()){
            logger.debug(testName + ": returned payload:");
            logger.debug(objectAsXmlString(location, LocationsCommon.class));
        }
        Assert.assertEquals(location.getInAuthority(), knownResourceId);

    }

    /**
     * Verify item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"readItem", "updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether location has expected displayName.
        MultipartInput input = (MultipartInput) res.getEntity();
        LocationsCommon location = (LocationsCommon) extractPart(input,
                client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(location);
        String displayName = location.getDisplayName();
        // Make sure displayName matches computed form
        String expectedDisplayName = 
            LocationAuthorityClientUtils.prepareDefaultDisplayName(TEST_NAME);
        Assert.assertNotNull(displayName, expectedDisplayName);
        
        // Update the shortName and verify the computed name is updated.
        location.setCsid(null);
        location.setDisplayNameComputed(true);
        location.setName("updated-" + TEST_NAME);
        expectedDisplayName = 
            LocationAuthorityClientUtils.prepareDefaultDisplayName("updated-" + TEST_NAME);

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(location, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        LocationsCommon updatedLocation =
                (LocationsCommon) extractPart(input,
                        client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(updatedLocation);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedLocation.getName(), location.getName(),
            "Updated ForeName in Location did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedLocation.getDisplayName(), expectedDisplayName,
            "Updated ForeName in Location not reflected in computed DisplayName.");

        // Now Update the displayName, not computed and verify the computed name is overriden.
        location.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        location.setDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new MultipartOutput();
        commonPart = output.addPart(location, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        updatedLocation =
                (LocationsCommon) extractPart(input,
                        client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(updatedLocation);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedLocation.isDisplayNameComputed(), false,
                "Updated displayNameComputed in Location did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedLocation.getDisplayName(),
        		expectedDisplayName,
                "Updated DisplayName (not computed) in Location not stored.");
    }

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
            dependsOnMethods = {"verifyItemDisplayName"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {

        // Perform setup.
    	setupUpdateWithWrongXmlSchema(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());

        // Check whether Location has expected displayName.
        MultipartInput input = (MultipartInput) res.getEntity();
        LocationsCommon location = (LocationsCommon) extractPart(input,
                client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(location);
        // Try to Update with computed false and no displayName
        location.setDisplayNameComputed(false);
        location.setDisplayName(null);

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(location, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
   

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    /**
     * Read item non existent.
     *
     * @param testName the test name
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"readItem"})
    public void readItemNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }


    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"readList"}, dependsOnGroups = {"createList", "read"})
    public void readList(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<LocationauthoritiesCommonList> res = client.readList();
        LocationauthoritiesCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<LocationauthoritiesCommonList.LocationauthorityListItem> items =
                    list.getLocationauthorityListItem();
            int i = 0;
            for (LocationauthoritiesCommonList.LocationauthorityListItem item : items) {
                String csid = item.getCsid();
                logger.debug(testName + ": list-item[" + i + "] csid=" +
                        csid);
                logger.debug(testName + ": list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
                readItemList(csid, null);
                i++;
            }
        }
    }

    /**
     * Read item list.
     */
    @Test(groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList() {
        readItemList(knownResourceId, null);
    }

    /**
     * Read item list by authority name.
     */
    @Test(groups = {"readList"}, dependsOnMethods = {"readItemList"})
    public void readItemListByAuthorityName() {
        readItemList(null, knownResourceDisplayName);
    }
    
    /**
     * Read item list.
     *
     * @param vcsid the vcsid
     * @param name the name
     */
    private void readItemList(String vcsid, String name) {

        final String testName = "readItemList";

        // Perform setup.
        setupReadList(testName);
        
	// Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<LocationsCommonList> res = null;
        if(vcsid!= null) {
	        res = client.readItemList(vcsid);
        } else if(name!= null) {
   	        res = client.readItemListForNamedAuthority(name);
        } else {
        	Assert.fail("readItemList passed null csid and name!");
        }
        LocationsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        List<LocationsCommonList.LocationListItem> items =
            list.getLocationListItem();
        int nItemsReturned = items.size();
        // There will be one item created, associated with a
        // known parent resource, by the createItem test.
        //
        // In addition, there will be 'nItemsToCreateInList'
        // additional items created by the createItemList test,
        // all associated with the same parent resource.
        int nExpectedItems = nItemsToCreateInList + 1;
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": Expected "
           		+ nExpectedItems +" items; got: "+nItemsReturned);
        }
        Assert.assertEquals(nItemsReturned, nExpectedItems);

        int i = 0;
        for (LocationsCommonList.LocationListItem item : items) {
        	Assert.assertTrue((null != item.getRefName()), "Item refName is null!");
        	Assert.assertTrue((null != item.getDisplayName()), "Item displayName is null!");
        	// Optionally output additional data about list members for debugging.
	        boolean showDetails = true;
	        if (showDetails && logger.isDebugEnabled()) {
                logger.debug("  " + testName + ": list-item[" + i + "] csid=" +
                        item.getCsid());
                logger.debug("  " + testName + ": list-item[" + i + "] refName=" +
                        item.getRefName());
                logger.debug("  " + testName + ": list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                logger.debug("  " + testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
            }
            i++;
        }
    }


    // Failure outcomes
    // None at present.

    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#update(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnGroups = {"read", "readList"})
    public void update(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Retrieve the contents of a resource to update.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res =
                client.read(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got LocationAuthority to update with ID: " + knownResourceId);
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        LocationauthoritiesCommon locationAuthority = (LocationauthoritiesCommon) extractPart(input,
                client.getCommonPartName(), LocationauthoritiesCommon.class);
        Assert.assertNotNull(locationAuthority);

        // Update the contents of this resource.
        locationAuthority.setDisplayName("updated-" + locationAuthority.getDisplayName());
        locationAuthority.setVocabType("updated-" + locationAuthority.getVocabType());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated LocationAuthority");
            logger.debug(objectAsXmlString(locationAuthority, LocationauthoritiesCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(locationAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());
        res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        LocationauthoritiesCommon updatedLocationAuthority =
                (LocationauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), LocationauthoritiesCommon.class);
        Assert.assertNotNull(updatedLocationAuthority);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedLocationAuthority.getDisplayName(),
                locationAuthority.getDisplayName(),
                "Data in updated object did not match submitted data.");
    }

    /**
     * Update item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"update"})
    public void updateItem(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Retrieve the contents of a resource to update.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res =
                client.readItem(knownResourceId, knownItemResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got Location to update with ID: " +
                knownItemResourceId +
                " in LocationAuthority: " + knownResourceId );
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        LocationsCommon location = (LocationsCommon) extractPart(input,
                client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(location);

        // Update the contents of this resource.
        location.setCsid(null);
        location.setName("updated-" + location.getName());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated Location");
            logger.debug(objectAsXmlString(location,
                LocationsCommon.class));
        }        

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(location, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        LocationsCommon updatedLocation =
                (LocationsCommon) extractPart(input,
                        client.getItemCommonPartName(), LocationsCommon.class);
        Assert.assertNotNull(updatedLocation);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedLocation.getName(), location.getName(),
                "Data in updated Location did not match submitted data.");
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }


    /* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
	 */
@Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        // Note: The ID(s) used when creating the request payload may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        LocationAuthorityClient client = new LocationAuthorityClient();
   	String displayName = "displayName-NON_EXISTENT_ID";
    	String fullRefName = LocationAuthorityClientUtils.createLocationAuthRefName(displayName, true);
    	MultipartOutput multipart = LocationAuthorityClientUtils.createLocationAuthorityInstance(
    				displayName, fullRefName, client.getCommonPartName());
        ClientResponse<MultipartInput> res =
                client.update(NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    /**
     * Update non existent item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"update"}, dependsOnMethods = {"updateItem", "testItemSubmitRequest"})
    public void updateNonExistentItem(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        LocationAuthorityClient client = new LocationAuthorityClient();
        Map<String, String> nonexMap = new HashMap<String,String>();
        nonexMap.put(LocationJAXBSchema.NAME, TEST_NAME);
        nonexMap.put(LocationJAXBSchema.LOCATION_TYPE, TEST_LOCATION_TYPE);
        nonexMap.put(LocationJAXBSchema.STATUS, TEST_STATUS);
        MultipartOutput multipart = 
    	LocationAuthorityClientUtils.createLocationInstance(NON_EXISTENT_ID, 
    			LocationAuthorityClientUtils.createLocationRefName(NON_EXISTENT_ID, NON_EXISTENT_ID, true), nonexMap,
    			client.getItemCommonPartName() );
        ClientResponse<MultipartInput> res =
                client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes

    // Note: delete sub-resources in ascending hierarchical order,
    // before deleting their parents.

   /**
    * Delete item.
    *
    * @param testName the test name
    * @throws Exception the exception
    */
   @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        //groups = {"delete"}, dependsOnGroups = {"create", "read", "readList", "readListByPartialTerm", "update"})
        groups = {"delete"}, dependsOnGroups = {"create", "read", "readList", "update"})
    public void deleteItem(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId +
                " itemcsid = " + knownItemResourceId);
        }

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.deleteItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId);
        }

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    /**
     * Delete non existent item.
     *
     * @param testName the test name
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    /**
     * Test item submit request.
     */
    @Test(dependsOnMethods = {"createItem", "readItem", "testSubmitRequest"})
    public void testItemSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getItemResourceURL(knownResourceId, knownItemResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testItemSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

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
        String parentResourceId;
        String itemResourceId;
        // Clean up contact resources.
        LocationAuthorityClient client = new LocationAuthorityClient();
        parentResourceId = knownResourceId;
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allItemResourceIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                client.deleteItem(parentResourceId, itemResourceId);
        }
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId);
        }
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
     * Gets the item service path component.
     *
     * @return the item service path component
     */
    public String getItemServicePathComponent() {
        return ITEM_SERVICE_PATH_COMPONENT;
    }

    /**
     * Returns the root URL for the item service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning parent, followed by the
     * path component for the items.
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @return The root URL for the item service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getItemServicePathComponent();
    }

    /**
     * Returns the URL of a specific item resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param  itemResourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The URL of a specific item resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String itemResourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + itemResourceIdentifier;
    }


}
