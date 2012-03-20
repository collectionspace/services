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

import org.collectionspace.services.PlaceJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.PlaceAuthorityClient;
import org.collectionspace.services.client.PlaceAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.place.PlaceauthoritiesCommon;
import org.collectionspace.services.place.PlacesCommon;
import org.collectionspace.services.place.PlaceNameGroup;
import org.collectionspace.services.place.PlaceNameGroupList;

import org.jboss.resteasy.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * PlaceAuthorityServiceTest, carries out tests against a
 * deployed and running PlaceAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class PlaceAuthorityServiceTest extends AbstractAuthorityServiceTest<PlaceauthoritiesCommon, PlacesCommon> {

    /** The logger. */
    private final String CLASS_NAME = PlaceAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(PlaceAuthorityServiceTest.class);
    private final String REFNAME = "refName";
    private final static String EMPTY_REFNAME = "";
    private final String DISPLAYNAME = "displayName";

	@Override
	public String getServicePathComponent() {
		return PlaceAuthorityClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return PlaceAuthorityClient.SERVICE_NAME;
	}
    
    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }	
    
    // Instance variables specific to this test.
    
//    /** The SERVICE path component. */
//    final String SERVICE_PATH_COMPONENT = "placeauthorities";
//    
//    /** The ITEM service path component. */
//    final String ITEM_SERVICE_PATH_COMPONENT = "items";
//    
    
    final String TEST_DNAME = "San Jose, CA";
    final String TEST_NAME = "San Jose";
    final String TEST_SHORTID = "sanjose";
    // TODO Make place type be a controlled vocab term.
    final String TEST_PLACE_TYPE = "City";
    // TODO Make status type be a controlled vocab term.
    final String TEST_STATUS = "Approved";
    final String TEST_NOTE = "My hometown";
    final String TEST_SOURCE = "Peralta's Places of California";
    final String TEST_SOURCE_PAGE = "p.21";
    final String TEST_DISPLAY_DATE = "This year";
    final String TEST_EARLIEST_SINGLE_YEAR = "2012";
    
    /** The known resource id. */
    private String knownResourceShortIdentifer = null;
    private String knownResourceRefName = null;
    
    private String knownPlaceTypeRefName = null;
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new PlaceAuthorityClient();
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
        PlaceAuthorityClient client = new PlaceAuthorityClient();
        Map<String, String> sanjoseMap = new HashMap<String,String>();
        // TODO Make place type and status be controlled vocabs.
        sanjoseMap.put(PlaceJAXBSchema.DISPLAY_NAME, TEST_DNAME);
        sanjoseMap.put(PlaceJAXBSchema.SHORT_IDENTIFIER, TEST_SHORTID);
        sanjoseMap.put(PlaceJAXBSchema.NAME, TEST_NAME);
        sanjoseMap.put(PlaceJAXBSchema.NOTE, TEST_NOTE);
        sanjoseMap.put(PlaceJAXBSchema.SOURCE, TEST_SOURCE);
        sanjoseMap.put(PlaceJAXBSchema.SOURCE_PAGE, TEST_SOURCE_PAGE);
        sanjoseMap.put(PlaceJAXBSchema.PLACE_TYPE, TEST_PLACE_TYPE);
        sanjoseMap.put(PlaceJAXBSchema.TERM_STATUS, TEST_STATUS);
        
        String newID = PlaceAuthorityClientUtils.createItemInAuthority(vcsid,
        		authRefName, sanjoseMap, client );    

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
        PlaceAuthorityClient client = new PlaceAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        PlacesCommon place = null;
        try {
            assertStatusCode(res, testName);
	        // Check whether place has expected displayName.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        place = (PlacesCommon) extractPart(input,
	                client.getItemCommonPartName(), PlacesCommon.class);
	        Assert.assertNotNull(place);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Now prepare an updated payload.
        //
        String displayName = place.getDisplayName();
        // Make sure displayName matches computed form
        String expectedDisplayName = 
            PlaceAuthorityClientUtils.prepareDefaultDisplayName(TEST_NAME);
        Assert.assertNotNull(displayName, expectedDisplayName);
        
        // Update the shortName and verify the computed name is updated.
        place.setCsid(null);
        place.setDisplayNameComputed(true);
        
         // Verify the contents of this resource
        PlaceNameGroupList placeNameGroupList = place.getPlaceNameGroupList();
        Assert.assertNotNull(placeNameGroupList);
        List<PlaceNameGroup> placeNameGroups = placeNameGroupList.getPlaceNameGroup();
        Assert.assertNotNull(placeNameGroups);
        Assert.assertTrue(placeNameGroups.size() > 0);
        String name = placeNameGroups.get(0).getName();
        Assert.assertNotNull(name);

        // Update the contents of this resource.
        final String PLACE_NAME_ADDITION = "verify-item-place-name-updated";
        placeNameGroups.get(0).setName(PLACE_NAME_ADDITION + TEST_NAME);
        place.setPlaceNameGroupList(placeNameGroupList);
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated Place");
            logger.debug(objectAsXmlString(place,
                    PlacesCommon.class));
        }
        expectedDisplayName = 
            PlaceAuthorityClientUtils.prepareDefaultDisplayName(PLACE_NAME_ADDITION + TEST_NAME);

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PlaceAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), place);

        setupUpdate();        
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        PlacesCommon updatedPlace = null;
        try {
        	assertStatusCode(res, testName);
	        // Retrieve the updated resource and verify that its contents exist.
        	PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedPlace = (PlacesCommon) extractPart(input,
	                        client.getItemCommonPartName(), PlacesCommon.class);
	        Assert.assertNotNull(updatedPlace);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedPlace.getDisplayName(), expectedDisplayName,
            "Updated DisplayName in Place not reflected in computed DisplayName.");
        //
        // Now Update the displayName, not computed and verify the computed name is overriden.
        //
        place.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        place.setDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(PlaceAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(client.getItemCommonPartName(), place);
        setupUpdate();        
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
	        assertStatusCode(res, testName);
	        // Retrieve the updated resource and verify that its contents exist.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        updatedPlace = (PlacesCommon) extractPart(input,
	        		client.getItemCommonPartName(), PlacesCommon.class);
	        Assert.assertNotNull(updatedPlace);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedPlace.isDisplayNameComputed(), false,
                "Updated displayNameComputed in Place did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedPlace.getDisplayName(),
        		expectedDisplayName,
                "Updated DisplayName (not computed) in Place not stored.");
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
        PlaceAuthorityClient client = new PlaceAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        PlacesCommon place= null;
        try {
            assertStatusCode(res, testName);        
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        place = (PlacesCommon) extractPart(input,
	                client.getItemCommonPartName(), PlacesCommon.class);
	        Assert.assertNotNull(place);
	    } finally {
                if (res != null) {
                res.releaseConnection();
	    }
        }

    // Try to Update with computed false and no displayName
        place.setDisplayNameComputed(false);
        place.setDisplayName(null);
        
        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(PlaceAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), place);
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
		PlaceAuthorityClient client = new PlaceAuthorityClient();
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
        PlaceAuthorityClient client = new PlaceAuthorityClient();
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
	// Place specific overrides
	//
	
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        // Submit the request to the service and store the response.
        String shortId = identifier;
    	String displayName = "displayName-" + shortId;
    	// String baseRefName = PlaceAuthorityClientUtils.createPlaceAuthRefName(shortId, null);    	
    	PoxPayloadOut result = 
            PlaceAuthorityClientUtils.createPlaceAuthorityInstance(
    	    displayName, shortId, commonPartName);
		return result;
	}
	
	@Override
    protected PoxPayloadOut createNonExistenceInstance(String commonPartName, String identifier) {
        String displayName = "displayName-NON_EXISTENT_ID";
    	PoxPayloadOut result = PlaceAuthorityClientUtils.createPlaceAuthorityInstance(
    				displayName, "nonEx", commonPartName);
    	return result;
    }

	@Override
	protected PlaceauthoritiesCommon updateInstance(PlaceauthoritiesCommon placeauthoritiesCommon) {
		PlaceauthoritiesCommon result = new PlaceauthoritiesCommon();
		
		result.setDisplayName("updated-" + placeauthoritiesCommon.getDisplayName());
		result.setVocabType("updated-" + placeauthoritiesCommon.getVocabType());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(PlaceauthoritiesCommon original,
			PlaceauthoritiesCommon updated) throws Exception {
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
	}

	protected void compareReadInstances(PlaceauthoritiesCommon original,
			PlaceauthoritiesCommon fromRead) throws Exception {
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
	protected PlacesCommon updateItemInstance(PlacesCommon placesCommon) {
		
                // Get the Name field from the resource passed in      
                PlaceNameGroupList placeNameGroupList = placesCommon.getPlaceNameGroupList();
                Assert.assertNotNull(placeNameGroupList);
                List<PlaceNameGroup> placeNameGroups = placeNameGroupList.getPlaceNameGroup();
                Assert.assertNotNull(placeNameGroups);
                Assert.assertTrue(placeNameGroups.size() > 0);
                String originalName = placeNameGroups.get(0).getName();
                Assert.assertNotNull(originalName);
                
                // Update the contents of the new resource based on original value
                PlaceNameGroupList updatedPlaceNameGroupList = new PlaceNameGroupList();
                List<PlaceNameGroup> updatedPlaceNameGroups = updatedPlaceNameGroupList.getPlaceNameGroup();
                PlaceNameGroup updatedPlaceNameGroup = new PlaceNameGroup();
                updatedPlaceNameGroup.setName("updated-" + originalName);
                updatedPlaceNameGroups.add(updatedPlaceNameGroup);
                PlacesCommon result = new PlacesCommon();
                result.setPlaceNameGroupList(updatedPlaceNameGroupList);
        
		result.setDisplayName("updated-" + placesCommon.getDisplayName());
		
		return result;
	}

	@Override
	protected void compareUpdatedItemInstances(PlacesCommon original,
			PlacesCommon updated) throws Exception {
        
                // Get the Name fields each resource passed in      
                PlaceNameGroupList placeNameGroupList = original.getPlaceNameGroupList();
                Assert.assertNotNull(placeNameGroupList);
                List<PlaceNameGroup> placeNameGroups = placeNameGroupList.getPlaceNameGroup();
                Assert.assertNotNull(placeNameGroups);
                Assert.assertTrue(placeNameGroups.size() > 0);
                String originalName = placeNameGroups.get(0).getName();
                Assert.assertNotNull(originalName);
            
                PlaceNameGroupList updatedPlaceNameGroupList = updated.getPlaceNameGroupList();
                Assert.assertNotNull(updatedPlaceNameGroupList);
                List<PlaceNameGroup> updatedPlaceNameGroups = updatedPlaceNameGroupList.getPlaceNameGroup();
                Assert.assertNotNull(updatedPlaceNameGroups);
                Assert.assertTrue(updatedPlaceNameGroups.size() > 0);
                String updatedName = updatedPlaceNameGroups.get(0).getName();
                Assert.assertNotNull(updatedName);
            
            
            
            
                Assert.assertEquals(updatedName, originalName,
                    "Data in updated Place did not match submitted data.");
	}

	@Override
	protected void verifyReadItemInstance(PlacesCommon item)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createNonExistenceItemInstance(
			String commonPartName, String identifier) {
        Map<String, String> nonexMap = new HashMap<String,String>();
        nonexMap.put(PlaceJAXBSchema.NAME, TEST_NAME);
        nonexMap.put(PlaceJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(PlaceJAXBSchema.PLACE_TYPE, TEST_PLACE_TYPE);
        nonexMap.put(PlaceJAXBSchema.TERM_STATUS, TEST_STATUS);
        // PoxPayloadOut multipart = 
    	// PlaceAuthorityClientUtils.createPlaceInstance(
    	//		PlaceAuthorityClientUtils.createPlaceRefName(knownResourceRefName, "nonEx", "Non Existent"), 
    	//		nonexMap, client.getItemCommonPartName() );
        final String EMPTY_REFNAME = "";
        PoxPayloadOut result = 
                PlaceAuthorityClientUtils.createPlaceInstance(EMPTY_REFNAME, 
    			nonexMap, commonPartName);
		return result;
	}
}
