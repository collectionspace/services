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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.client.LocationAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.location.LocationauthoritiesCommon;
import org.collectionspace.services.location.LocationsCommon;

import org.jboss.resteasy.client.ClientResponse;

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
public class LocationAuthorityServiceTest extends AbstractAuthorityServiceTest<LocationauthoritiesCommon, LocationsCommon> {

    /** The logger. */
    private final String CLASS_NAME = LocationAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(LocationAuthorityServiceTest.class);
    private final String REFNAME = "refName";
    private final String DISPLAYNAME = "displayName";
    private final static String CURRENT_DATE_UTC =
        GregorianCalendarDateTimeUtils.currentDateUTC();

	@Override
	public String getServicePathComponent() {
		return LocationAuthorityClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return LocationAuthorityClient.SERVICE_NAME;
	}
    
    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }	
    
    // Instance variables specific to this test.
    
//    /** The SERVICE path component. */
//    final String SERVICE_PATH_COMPONENT = "locationauthorities";
//    
//    /** The ITEM service path component. */
//    final String ITEM_SERVICE_PATH_COMPONENT = "items";
//    
//    /** The CONTACT service path component. */
//    final String CONTACT_SERVICE_PATH_COMPONENT = "contacts";
    
    final String TEST_NAME = "Shelf 1";
    final String TEST_SHORTID = "shelf1";
    final String TEST_CONDITION_NOTE = "Basically clean";
    final String TEST_CONDITION_NOTE_DATE = CURRENT_DATE_UTC;
    final String TEST_SECURITY_NOTE = "Kind of safe";
    final String TEST_ACCESS_NOTE = "Only right-thinkers may see";
    final String TEST_ADDRESS = "123 Main Street, Anytown USA";
    // TODO Make loc type be a controlled vocab term.
    final String TEST_LOCATION_TYPE = "Shelf";
    // TODO Make status type be a controlled vocab term.
    final String TEST_STATUS = "Approved";
    
    /** The known resource id. */
    private String knownResourceShortIdentifer = null;
    private String knownResourceRefName = null;
    
    private String knownLocationTypeRefName = null;
    private String knownContactResourceId = null;
        
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new LocationAuthorityClient();
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
        allResourceItemIdsCreated.put(newID, vcsid);

        return newID;
    }

    /**
     * Verify item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName",
        dependsOnMethods = {"readItem", "updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {
        // Perform setup.
        setupRead();
        //
        // First, read our known item resource
        //
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        LocationsCommon location = null;
        try {
            assertStatusCode(res, testName);
	        // Check whether location has expected displayName.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        location = (LocationsCommon) extractPart(input,
	                client.getItemCommonPartName(), LocationsCommon.class);
	        Assert.assertNotNull(location);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Now prepare an updated payload.
        //
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
        PoxPayloadOut output = new PoxPayloadOut(LocationAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), location);

        setupUpdate();        
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        LocationsCommon updatedLocation = null;
        try {
        	assertStatusCode(res, testName);
	        // Retrieve the updated resource and verify that its contents exist.
        	PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedLocation = (LocationsCommon) extractPart(input,
	                        client.getItemCommonPartName(), LocationsCommon.class);
	        Assert.assertNotNull(updatedLocation);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedLocation.getName(), location.getName(),
            "Updated ForeName in Location did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedLocation.getDisplayName(), expectedDisplayName,
            "Updated ForeName in Location not reflected in computed DisplayName.");
        //
        // Now Update the displayName, not computed and verify the computed name is overriden.
        //
        location.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        location.setDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(LocationAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(client.getItemCommonPartName(), location);
        setupUpdate();        
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
	        assertStatusCode(res, testName);
	        // Retrieve the updated resource and verify that its contents exist.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedLocation = (LocationsCommon) extractPart(input,
	        		client.getItemCommonPartName(), LocationsCommon.class);
	        Assert.assertNotNull(updatedLocation);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

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
    @Test(dataProvider="testName",
            dependsOnMethods = {"verifyItemDisplayName"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        // Perform setup for read.
        setupRead();

        // Submit the request to the service and store the response.
        LocationAuthorityClient client = new LocationAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        LocationsCommon location = null;
        try {
            assertStatusCode(res, testName);        
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        location = (LocationsCommon) extractPart(input,
	                client.getItemCommonPartName(), LocationsCommon.class);
	        Assert.assertNotNull(location);
	    } finally {
	    	if (res != null) {
                res.releaseConnection();
            }
	    }
	        
        // Try to Update with computed false and no displayName
        location.setDisplayNameComputed(false);
        location.setDisplayName(null);
        
        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(LocationAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), location);
        setupUpdateWithInvalidBody(); // we expected a failure here.
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
	    	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Read item list.
     */
    @Test(dataProvider = "testName", groups = {"readList"},
    		dependsOnMethods = {"readList"})
    public void readItemList(String testName) {
        readItemList(knownAuthorityWithItems, null);
    }

    /**
     * Read item list by authority name.
     */
    @Test(dataProvider = "testName", groups = {"readList"},
    		dependsOnMethods = {"readItemList"})
    public void readItemListByAuthorityName(String testName) {
        readItemList(null, READITEMS_SHORT_IDENTIFIER);
    }
    
	/**
	 * Read item list.
	 * 
	 * @param vcsid
	 *            the vcsid
	 * @param name
	 *            the name
	 */
	private void readItemList(String vcsid, String shortId) {
		String testName = "readItemList";

		// Perform setup.
		setupReadList();

		// Submit the request to the service and store the response.
		LocationAuthorityClient client = new LocationAuthorityClient();
		ClientResponse<AbstractCommonList> res = null;
		if (vcsid != null) {
			res = client.readItemList(vcsid, null, null);
		} else if (shortId != null) {
			res = client.readItemListForNamedAuthority(shortId, null, null);
		} else {
			Assert.fail("readItemList passed null csid and name!");
		}
		
		AbstractCommonList list = null;
		try {
			assertStatusCode(res, testName);
			list = res.getEntity();
		} finally {
			if (res != null) {
                res.releaseConnection();
            }
		}
		
		List<AbstractCommonList.ListItem> items = list.getListItem();
		int nItemsReturned = items.size();
		// There will be 'nItemsToCreateInList'
		// items created by the createItemList test,
		// all associated with the same parent resource.
		int nExpectedItems = nItemsToCreateInList;
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": Expected " + nExpectedItems
					+ " items; got: " + nItemsReturned);
		}
		Assert.assertEquals(nItemsReturned, nExpectedItems);

		for (AbstractCommonList.ListItem item : items) {
			String value = AbstractCommonListUtils.ListItemGetElementValue(
					item, REFNAME);
			Assert.assertTrue((null != value), "Item refName is null!");
			value = AbstractCommonListUtils.ListItemGetElementValue(item,
					DISPLAYNAME);
			Assert.assertTrue((null != value), "Item displayName is null!");
		}
		if (logger.isTraceEnabled()) {
			AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger,
					testName);
		}
	}

    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing.  See localDelete().  This ensure proper test order.
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"localDeleteItem"})    
    public void localDelete(String testName) throws Exception {
    	super.delete(testName);
    }

    @Override
    public void deleteItem(String testName) throws Exception {
    	// Do nothing.  We need to wait until after the test "localDelete" gets run.  When it does,
    	// its dependencies will get run first and then we can call the base class' delete method.
    }
    
    @Test(dataProvider = "testName", groups = {"delete"},
    	dependsOnMethods = {"verifyIllegalItemDisplayName"})
    public void localDeleteItem(String testName) throws Exception {
    	super.deleteItem(testName);
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
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            client.deleteItem(parentResourceId, itemResourceId).releaseConnection();
        }
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            client.delete(resourceId).releaseConnection();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */

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

	@Override
	public void authorityTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	//
	// Location specific overrides
	//
	
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        // Submit the request to the service and store the response.
        String shortId = identifier;
    	String displayName = "displayName-" + shortId;
    	// String baseRefName = LocationAuthorityClientUtils.createLocationAuthRefName(shortId, null);    	
    	PoxPayloadOut result = 
            LocationAuthorityClientUtils.createLocationAuthorityInstance(
    	    displayName, shortId, commonPartName);
		return result;
	}
	
	@Override
    protected PoxPayloadOut createNonExistenceInstance(String commonPartName, String identifier) {
        String displayName = "displayName-NON_EXISTENT_ID";
    	PoxPayloadOut result = LocationAuthorityClientUtils.createLocationAuthorityInstance(
    				displayName, "nonEx", commonPartName);
    	return result;
    }

	@Override
	protected LocationauthoritiesCommon updateInstance(LocationauthoritiesCommon locationauthoritiesCommon) {
		LocationauthoritiesCommon result = new LocationauthoritiesCommon();
		
		result.setDisplayName("updated-" + locationauthoritiesCommon.getDisplayName());
		result.setVocabType("updated-" + locationauthoritiesCommon.getVocabType());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(LocationauthoritiesCommon original,
			LocationauthoritiesCommon updated) throws Exception {
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
	}

	protected void compareReadInstances(LocationauthoritiesCommon original,
			LocationauthoritiesCommon fromRead) throws Exception {
        Assert.assertNotNull(fromRead.getDisplayName());
        Assert.assertNotNull(fromRead.getShortIdentifier());
        Assert.assertNotNull(fromRead.getRefName());
	}
	
	//
	// Authority item specific overrides
	//
	
	@Override
	protected String createItemInAuthority(String authorityId) {
		return createItemInAuthority(authorityId, null /*refname*/);
	}

	@Override
	protected LocationsCommon updateItemInstance(LocationsCommon locationsCommon) {
		LocationsCommon result = new LocationsCommon();
		
        result.setName("updated-" + locationsCommon.getName());
		result.setDisplayName("updated-" + locationsCommon.getDisplayName());
		
		return result;
	}

	@Override
	protected void compareUpdatedItemInstances(LocationsCommon original,
			LocationsCommon updated) throws Exception {
        Assert.assertEquals(updated.getName(), original.getName(),
                "Data in updated Location did not match submitted data.");
	}

	@Override
	protected void verifyReadItemInstance(LocationsCommon item)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createNonExistenceItemInstance(
			String commonPartName, String identifier) {
        Map<String, String> nonexMap = new HashMap<String,String>();
        nonexMap.put(LocationJAXBSchema.NAME, TEST_NAME);
        nonexMap.put(LocationJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(LocationJAXBSchema.LOCATION_TYPE, TEST_LOCATION_TYPE);
        nonexMap.put(LocationJAXBSchema.TERM_STATUS, TEST_STATUS);
        // PoxPayloadOut multipart = 
    	// LocationAuthorityClientUtils.createLocationInstance(
    	//		LocationAuthorityClientUtils.createLocationRefName(knownResourceRefName, "nonEx", "Non Existent"), 
    	//		nonexMap, client.getItemCommonPartName() );
        final String EMPTY_REFNAME = "";
        PoxPayloadOut result = 
                LocationAuthorityClientUtils.createLocationInstance(EMPTY_REFNAME, 
    			nonexMap, commonPartName);
		return result;
	}
}
