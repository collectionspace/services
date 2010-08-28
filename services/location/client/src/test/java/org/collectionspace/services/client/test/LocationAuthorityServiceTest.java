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
    private final String CLASS_NAME = LocationAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    
    /** The SERVICE path component. */
    final String SERVICE_PATH_COMPONENT = "locationauthorities";
    
    /** The ITEM service path component. */
    final String ITEM_SERVICE_PATH_COMPONENT = "items";
    
    /** The CONTACT service path component. */
    final String CONTACT_SERVICE_PATH_COMPONENT = "contacts";
    
    final String TEST_NAME = "Shelf 1";
    final String TEST_SHORTID = "shelf1";
    final String TEST_CONDITION_NOTE = "Basically clean";
    final String TEST_CONDITION_NOTE_DATE = "June 11, 1979";
    final String TEST_SECURITY_NOTE = "Kind of safe";
    final String TEST_ACCESS_NOTE = "Only right-thinkers may see";
    final String TEST_ADDRESS = "123 Main Street, Anytown USA";
    // TODO Make loc type be a controlled vocab term.
    final String TEST_LOCATION_TYPE = "Shelf";
    // TODO Make status type be a controlled vocab term.
    final String TEST_STATUS = "Approved";
    
    /** The known resource id. */
    private String knownResourceId = null;
    private String knownResourceShortIdentifer = null;
    private String knownResourceRefName = null;
    private String knownLocationTypeRefName = null;
    private String knownItemResourceId = null;
    private String knownItemResourceShortIdentifer = null;
    private String knownContactResourceId = null;
    
    /** The n items to create in list. */
    private int nItemsToCreateInList = 3;
    
    /** The all resource ids created. */
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    
    /** The all item resource ids created. */
    private Map<String, String> allItemResourceIdsCreated =
        new HashMap<String, String>();
    
    protected void setKnownResource( String id, String shortIdentifer,
    		String refName ) {
    	knownResourceId = id;
    	knownResourceShortIdentifer = shortIdentifer;
    	knownResourceRefName = refName;
    }

    protected void setKnownItemResource( String id, String shortIdentifer ) {
    	knownItemResourceId = id;
    	knownItemResourceShortIdentifer = shortIdentifer;
    }

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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        String shortId = createIdentifier();
    	String displayName = "displayName-" + shortId;
    	String baseRefName = LocationAuthorityClientUtils.createLocationAuthRefName(shortId, null);
    	MultipartOutput multipart = 
            LocationAuthorityClientUtils.createLocationAuthorityInstance(
    	    displayName, shortId, client.getCommonPartName());
    	String newID = null;
        ClientResponse<Response> res = client.create(multipart);
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
	        Assert.assertTrue(this.REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(this.REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, this.EXPECTED_STATUS_CODE);
	
	        newID = LocationAuthorityClientUtils.extractId(res);
        } finally {
        	res.releaseConnection();
        }
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
        	setKnownResource( newID, shortId, baseRefName );
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
        if(logger.isDebugEnabled()){
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        setupCreate();
        createItemInAuthority(knownResourceId, knownResourceRefName);
    }

    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    private String createItemInAuthority(String vcsid, String authRefName) {

        final String testName = "createItemInAuthority("+vcsid+","+authRefName+")";
        if(logger.isDebugEnabled()){
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        Map<String, String> shelf1Map = new HashMap<String,String>();
        // TODO Make loc type and status be controlled vocabs.
        shelf1Map.put(LocationJAXBSchema.NAME, TEST_NAME);
        shelf1Map.put(LocationJAXBSchema.SHORT_IDENTIFIER, TEST_SHORTID);
        shelf1Map.put(LocationJAXBSchema.CONDITION_NOTE, TEST_CONDITION_NOTE);
        shelf1Map.put(LocationJAXBSchema.CONDITION_NOTE_DATE, TEST_CONDITION_NOTE_DATE);
        shelf1Map.put(LocationJAXBSchema.SECURITY_NOTE, TEST_SECURITY_NOTE);
        shelf1Map.put(LocationJAXBSchema.ACCESS_NOTE, TEST_ACCESS_NOTE);
        shelf1Map.put(LocationJAXBSchema.ADDRESS, TEST_ADDRESS);
        shelf1Map.put(LocationJAXBSchema.LOCATION_TYPE, TEST_LOCATION_TYPE);
        shelf1Map.put(LocationJAXBSchema.TERM_STATUS, TEST_STATUS);
        
        String newID = LocationAuthorityClientUtils.createItemInAuthority(vcsid,
        		authRefName, shelf1Map, client );

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null){
        	setKnownItemResource(newID, TEST_SHORTID);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + newID);
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();
        
        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
    	String newID = null;
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        try {
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
	            LocationauthoritiesCommon locationAuthority = 
	            	(LocationauthoritiesCommon) extractPart(input,
	                    client.getCommonPartName(), LocationauthoritiesCommon.class);
	            Assert.assertNotNull(locationAuthority);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.releaseConnection();
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName+"("+knownResourceShortIdentifer+")", CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readByName(knownResourceShortIdentifer);
        try {
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
        } finally {
        	res.releaseConnection();
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
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
	    } finally {
	    	res.releaseConnection();
	    }

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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
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
	    	res.releaseConnection();
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
	    	res.releaseConnection();
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
	    } finally {
	    	res.releaseConnection();
	    }
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        // FIXME: create a setup configuration for this operation.
    	setupUpdateWithWrongXmlSchema();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
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
	    	res.releaseConnection();
	        res = client.updateItem(knownResourceId, knownItemResourceId, output);
	        statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug("updateItem: status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
    }
   

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
    }

    /**
     * Read item non existent.
     *
     * @param testName the test name
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"read"}, dependsOnMethods = {"readItem"})
    public void readItemNonExistent(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<LocationauthoritiesCommonList> res = client.readList();
        try {
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
	    } finally {
	    	res.releaseConnection();
	    }
    }

    /**
     * Read item list.
     */
    @Test(groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList() {
        String testName = "readItemList";
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        readItemList(knownResourceId, null);
    }

    /**
     * Read item list by authority name.
     */
    @Test(groups = {"readList"}, dependsOnMethods = {"readItemList"})
    public void readItemListByAuthorityName() {
        String testName = "readItemListByAuthorityName";
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        readItemList(null, knownResourceShortIdentifer);
    }
    
    /**
     * Read item list.
     *
     * @param vcsid the vcsid
     * @param name the name
     */
    private void readItemList(String vcsid, String shortId) {

        String testName = "readItemList";

        // Perform setup.
        setupReadList();
        
        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<LocationsCommonList> res = null;
        if(vcsid!= null) {
	        res = client.readItemList(vcsid, null, null);
        } else if(shortId!= null) {
   	        res = client.readItemListForNamedAuthority(shortId, null, null);
        } else {
        	Assert.fail("readItemList passed null csid and name!");
        }
        try {
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
	    } finally {
	    	res.releaseConnection();
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        try {
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
	    	res.releaseConnection();
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
	    } finally {
	    	res.releaseConnection();
	    }
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<MultipartInput> res =
                client.readItem(knownResourceId, knownItemResourceId);
        try {
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
	    	res.releaseConnection();
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
	    } finally {
	    	res.releaseConnection();
	    }
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID(s) used when creating the request payload may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        LocationAuthorityClient client = new LocationAuthorityClient();
   	String displayName = "displayName-NON_EXISTENT_ID";
    	MultipartOutput multipart = LocationAuthorityClientUtils.createLocationAuthorityInstance(
    				displayName, "nonEx", client.getCommonPartName());
        ClientResponse<MultipartInput> res =
                client.update(NON_EXISTENT_ID, multipart);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        LocationAuthorityClient client = new LocationAuthorityClient();
        Map<String, String> nonexMap = new HashMap<String,String>();
        nonexMap.put(LocationJAXBSchema.NAME, TEST_NAME);
        nonexMap.put(LocationJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(LocationJAXBSchema.LOCATION_TYPE, TEST_LOCATION_TYPE);
        nonexMap.put(LocationJAXBSchema.TERM_STATUS, TEST_STATUS);
        MultipartOutput multipart = 
    	LocationAuthorityClientUtils.createLocationInstance(
    			LocationAuthorityClientUtils.createLocationRefName(knownResourceRefName, "nonEx", "Non Existent"), 
    			nonexMap, client.getItemCommonPartName() );
        ClientResponse<MultipartInput> res =
                client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
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

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId +
                " itemcsid = " + knownItemResourceId);
        }

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = 
        	client.deleteItem(knownResourceId, knownItemResourceId);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        if(logger.isDebugEnabled()){
            logger.debug("parentcsid =" + knownResourceId);
        }

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
    }

    /**
     * Delete non existent item.
     *
     * @param testName the test name
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
	    } finally {
	    	res.releaseConnection();
	    }
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
	    	res.releaseConnection();
        }
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId);
	    	res.releaseConnection();
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
