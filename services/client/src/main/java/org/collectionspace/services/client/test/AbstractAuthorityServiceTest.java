package org.collectionspace.services.client.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.AuthorityClientImpl;
import org.collectionspace.services.client.AuthorityProxy;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author remillet
 *
 * @param <AUTHORITY_COMMON_TYPE>
 * @param <AUTHORITY_ITEM_TYPE>
 * 
 * All CRUD related authority test classes should extend this class.
 * 
 */
public abstract class AbstractAuthorityServiceTest<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE> 
	extends AbstractPoxServiceTestImpl<AbstractCommonList, AUTHORITY_COMMON_TYPE> {

    private final Logger logger = LoggerFactory.getLogger(AbstractAuthorityServiceTest.class);
	
    protected String knownResourceShortIdentifer = null;
	protected static final String READITEMS_SHORT_IDENTIFIER = "resourceWithItems"; 
	protected String knownAuthorityWithItems = null;
	
	protected static final String SAS_IDENTIFIER = "SAS"; 
	protected String knownSASAuthorityResourceId = null;
	protected String knownSASItemResourceId = null;
	protected HashMap<String, String> allSASResourceItemIdsCreated = new HashMap<String, String>(); /* itemURN, parentURN */;

	protected String knownResourceRefName = null;
    protected String knownItemResourceId = null;
    protected String knownItemResourceShortIdentifer = null;    
    protected int nItemsToCreateInList = 5;
    protected String TEST_SHORTID = "johnWayneActor";

    /*
     * Abstract methods that subclasses must override/implement
     */
    
    /**
     * 
     * @param testName
     */
    public abstract void authorityTests(String testName);
	
	/**
	 * 
	 * @param client
	 * @param vcsid
	 * @return
	 */
	abstract protected String createItemInAuthority(AuthorityClient client, String vcsid, String shortId);
	
    
    /**
     * 
     * @param authorityItem
     * @return
     */
    protected abstract AUTHORITY_ITEM_TYPE updateItemInstance(final AUTHORITY_ITEM_TYPE authorityItem);    
    
    /**
     * 
     * @param original
     * @param updated
     * @throws Exception
     */
    protected abstract void compareUpdatedItemInstances(AUTHORITY_ITEM_TYPE original, AUTHORITY_ITEM_TYPE updated) throws Exception;
    
    /**
     * 
     * @param id
     * @param shortIdentifer
     */
    protected void setKnownItemResource(String id, String shortIdentifer ) {
    	knownItemResourceId = id;
    	knownItemResourceShortIdentifer = shortIdentifer;
    }

    /**
     * 
     * @param id
     * @param shortIdentifer
     * @param refName
     */
    protected void setKnownResource(String id, String shortIdentifer,
            String refName) {
        knownResourceId = id;
        knownResourceShortIdentifer = shortIdentifer;
        knownResourceRefName = refName;
    }

    /**
     * 
     * @return
     */
	protected String getSASAuthorityIdentifier() {
		// TODO Auto-generated method stub
		return this.getKnowResourceIdentifier() + this.SAS_IDENTIFIER;
	}
    
	/**
	 * 
	 * @param shortId
	 * @return
	 */
	protected String getUrnIdentifier(String shortId) {
		return String.format("urn:cspace:name(%s)", shortId);
	}
	
    /**
     * Sets up create tests.
     */
    protected void setupSync() {
        testExpectedStatusCode = this.STATUS_OK;
        testRequestType = ServiceRequestType.SYNC;
        testSetup(testExpectedStatusCode, testRequestType);
    }
    
    /**
     * Gets a client to the SAS (Shared Authority Server)
     *
     * @return the client
     */
    protected AuthorityClient getSASClientInstance() {
    	return (AuthorityClient) this.getClientInstance(CollectionSpaceClient.SAS_CLIENT_PROPERTIES_FILENAME);
    }

    /**
     * Returns the root URL for a service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning vocabulary, followed by the 
     * path component for the items.
     *
     * @return The root URL for a service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getServicePathItemsComponent();
    }

    /**
     * Returns the URL of a specific resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  resourceIdentifier  An identifier (such as a UUID) for a resource.
     *
     * @return The URL of a specific resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String resourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + resourceIdentifier;
    }
        
    /**
     * For authorities we override this method so we can save the shortid.
     */
    @Override
    protected String createWithIdentifier(String testName, String identifier) throws Exception {
    	String csid = createResource(testName, identifier);
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (getKnowResourceId() == null) {
        	setKnownResource(csid, identifier /*shortId*/, null /*refname*/ );
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": Setting knownResourceId=" + getKnowResourceId());
            }
        }
    	
        return identifier;
    }    
    
    @Test(dependsOnMethods = {"readItem", "CRUDTests"})
    public void testItemSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getItemResourceURL(knownResourceId, knownItemResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testItemSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);
    }    

    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    	dependsOnMethods = {"readItem"})
    public void verifyIgnoredUpdateWithInAuthority(String testName) throws Exception {
    	// Perform setup.
    	setupUpdate();

    	// Submit the request to the service and store the response.
        AuthorityClientImpl<AUTHORITY_ITEM_TYPE, AuthorityProxy> client = 
        		(AuthorityClientImpl<AUTHORITY_ITEM_TYPE, AuthorityProxy>)this.getClientInstance();
    	Response res = client.readItem(knownResourceId, knownItemResourceId);
    	AUTHORITY_ITEM_TYPE vitem = null;
    	try {
	    	int statusCode = res.getStatus();
	
	    	// Check the status code of the response: does it match
	    	// the expected response(s)?
	    	if (logger.isDebugEnabled()) {
	    		logger.debug(testName + " read authority:" + knownResourceId + "/Item:"
	    				+ knownItemResourceId + " status = " + statusCode);
	    	}
	    	Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	    			invalidStatusCodeMessage(testRequestType, statusCode));
	    	Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());
	
	        vitem = extractItemCommonPartValue(res);
	    	Assert.assertNotNull(vitem);
	    	// Try to Update with new parent vocab (use self, for test).
	    	Assert.assertEquals(client.getInAuthority(vitem), knownResourceId,
	    			"VocabularyItem inAuthority does not match knownResourceId.");
	    	client.setInAuthority(vitem, knownItemResourceId);

    	} finally {
    		res.close();
    	}
    	
    	// Submit the updated resource to the service and store the response.
        PoxPayloadOut output = this.createItemRequestTypeInstance(vitem);
    	res = client.updateItem(knownResourceId, knownItemResourceId, output);
    	try {
	    	int statusCode = res.getStatus();
	
	    	// Check the status code of the response: does it match the expected response(s)?
	    	if (logger.isDebugEnabled()) {
	    		logger.debug(testName + ": status = " + statusCode);
	    	}
	    	Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	    			invalidStatusCodeMessage(testRequestType, statusCode));
	    	Assert.assertEquals(statusCode, testExpectedStatusCode);
    	} finally {
    		res.close();
    	}
	
    	res = client.readItem(knownResourceId, knownItemResourceId);
    	try {
	    	// Retrieve the updated resource and verify that the parent did not change
	        AUTHORITY_ITEM_TYPE updatedVocabularyItem = extractItemCommonPartValue(res);
	    	Assert.assertNotNull(updatedVocabularyItem);
	
	    	// Verify that the updated resource received the correct data.
	    	Assert.assertEquals(client.getInAuthority(updatedVocabularyItem),
	    			knownResourceId,
	    			"VocabularyItem allowed update to the parent (inAuthority).");
    	} finally {
    		res.close();
    	}
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createItem(String testName) {
        // Perform setup.
        setupCreate();

        String newID = createItemInAuthority((AuthorityClient) getClientInstance(), knownResourceId, getTestAuthorityItemShortId());

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            knownItemResourceId = newID;
            if (null != testName && logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }
    }
    
    /**
     * Sync the local with the SAS
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createSASItem", "CRUDTests"})
    public void syncWithSAS(String testName) {
        //
        // First create an empty instance of the authority, so we can sync items with it.  We're
    	// using the short ID of the SAS authority.  The short ID of the local and the SAS will (must) be the same.
        //
    	AuthorityClient client = (AuthorityClient) this.getClientInstance();
    	String localAuthorityId = null;
        try {
			localAuthorityId = createResource(client, testName, getSASAuthorityIdentifier());
		} catch (Exception e) {
			Assert.assertNotNull(localAuthorityId);
		}

    	//
    	// Now we can try to sync the SAS authority with the local one we just created.
    	//
        setupSync();
    	Response response = client.syncByName(getSASAuthorityIdentifier()); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": HTTP status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	response.close();
        }
    }
    
    /**
     * SAS - Create a new authority on the SAS server.
     * @param testName
     */    
    @Test(dataProvider = "testName", dependsOnMethods = {"createItem", "CRUDTests"})
    public void createSASAuthority(String testName) {
        // Perform setup.
        setupCreate();

        try {
        	String newID = createResource(getSASClientInstance(), testName, getSASAuthorityIdentifier());
        	knownSASAuthorityResourceId = newID;
            if (logger.isDebugEnabled()) {
            	String.format("Created SAS authority '%s' with CSID=%s.", getSASAuthorityIdentifier(), newID);
            }
        } catch (Exception e) {
        	logger.info(String.format("Failed to create SAS authority '%s'.", getSASAuthorityIdentifier()));
        }
    }

    /**
     * SAS - Create an item in the SAS authority on the SAS server.
     * @param testName
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createSASAuthority", "CRUDTests"})
    public void createSASItem(String testName) {
        // Perform setup.
        setupCreate();

        String shortId = "SassyActor" + System.currentTimeMillis() + Math.abs(random.nextInt()); // short ID needs to be unique
        String newID = createItemInAuthority(getSASClientInstance(), knownSASAuthorityResourceId, shortId);

		// Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownSASItemResourceId == null) {
        	knownSASItemResourceId = newID;
            if (null != testName && logger.isDebugEnabled()) {
                logger.debug(testName + ": knownSASItemResourceId=" + knownSASItemResourceId);
            }
        }
        //
        // Keep track of the SAS authority items we create, so we can delete them from
        // the *local* authority after we perform a sync operation.  We need to keep track
        // of the URN (not the CSID) since the CSIDs will differ on the SAS vs local.
        //
        this.allSASResourceItemIdsCreated.put(this.getUrnIdentifier(shortId), getUrnIdentifier(getSASAuthorityIdentifier()));
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"createItem"})
    public void createItemList(String testName) throws Exception {
    	knownAuthorityWithItems = createResource(testName, READITEMS_SHORT_IDENTIFIER);
        for (int j = 0; j < nItemsToCreateInList; j++) {
        	createItemInAuthority((AuthorityClient) getClientInstance(), knownAuthorityWithItems, this.getTestAuthorityItemShortId(true));
        }
    }

    /**
     * Read by name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"CRUDTests"})
    public void readByName(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) this.getClientInstance();
        Response res = client.readByName(getKnowResourceIdentifier());
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	        
	        AUTHORITY_COMMON_TYPE commonPart = extractCommonPartValue(res);
	        Assert.assertNotNull(commonPart);
        } finally {
        	res.close();
        }
    }
    
    /**
     * Extracts the common part item from a service's item payload.
     * 
     * @param res
     * @return
     * @throws Exception
     */
	public AUTHORITY_ITEM_TYPE extractItemCommonPartValue(Response res) throws Exception {
		AUTHORITY_ITEM_TYPE result = null;
		
        AuthorityClient client = (AuthorityClient) getClientInstance();
		PayloadInputPart payloadInputPart = extractPart(res, client.getItemCommonPartName());
		if (payloadInputPart != null) {
			result = (AUTHORITY_ITEM_TYPE) payloadInputPart.getBody();
		}
		Assert.assertNotNull(result,
				"Part or body of part " + client.getCommonPartName() + " was unexpectedly null.");
		
		return result;
	}
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"readItem"})
    public void readItemNonExistent(String testName) {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	res.close();
        }
    }
	
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createItem"})
    public void readItem(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = client.readItem(knownResourceId, knownItemResourceId);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        AUTHORITY_ITEM_TYPE itemCommonPart = extractItemCommonPartValue(res);
	        Assert.assertNotNull(itemCommonPart);
	        Assert.assertEquals(client.getInAuthority(itemCommonPart), knownResourceId);
	        verifyReadItemInstance(itemCommonPart);
        } finally {
        	res.close();
        }
    }
    
    protected abstract void verifyReadItemInstance(AUTHORITY_ITEM_TYPE item) throws Exception;
        
    @Test(dataProvider = "testName",
		dependsOnMethods = {"testItemSubmitRequest", "updateItem", "verifyIgnoredUpdateWithInAuthority"})    
    public void deleteItem(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = client.deleteItem(knownResourceId, knownItemResourceId);
        int statusCode;
        try {
        	statusCode = res.getStatus();
        } finally {
        	res.close();
        }

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("delete: status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
    }
    
    protected void readItemListInt(String vcsid, String shortId, String testName) {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = null;
        if (vcsid != null) {
            res = client.readItemList(vcsid, null, null);
        } else if (shortId != null) {
            res = client.readItemListForNamedAuthority(shortId, null, null);
        } else {
            Assert.fail("Internal Error: readItemList both vcsid and shortId are null!");
        }
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug("  " + testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        AbstractCommonList list = res.readEntity(AbstractCommonList.class);
	        List<AbstractCommonList.ListItem> items = list.getListItem();
	        int nItemsReturned = items.size();
	        long nItemsTotal = list.getTotalItems();
	        if (logger.isDebugEnabled()) {
	            logger.debug("  " + testName + ": Expected "
	                    + nItemsToCreateInList + " items; got: " + nItemsReturned + " of: " + nItemsTotal);
	        }
	        Assert.assertEquals(nItemsTotal, nItemsToCreateInList);
	
	        if(logger.isTraceEnabled()){
	        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
	        }
        } finally {
        	res.close();
        }
    }
    
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createItemList"})
    public void readItemList(String testName) {
        readItemListInt(knownAuthorityWithItems, null, testName);
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"readItem"})
    public void readItemListByName(String testName) {
        readItemListInt(null, READITEMS_SHORT_IDENTIFIER, testName);
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode;
        try {
        	statusCode = res.getStatus();
        } finally {
        	res.close();
        }

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
    }
    
    protected String getServicePathItemsComponent() {
        return AuthorityClient.ITEMS;
    }
    
	public PoxPayloadOut createItemRequestTypeInstance(AUTHORITY_ITEM_TYPE itemTypeInstance) {
		PoxPayloadOut result = null;
		
        AuthorityClient client = (AuthorityClient) getClientInstance();
        PoxPayloadOut payloadOut = new PoxPayloadOut(this.getServicePathItemsComponent());
        PayloadOutputPart part = payloadOut.addPart(client.getItemCommonPartName(), itemTypeInstance);
        result = payloadOut;
		
		return result;
	}

	/**
	 * Update an Authority item.
	 * 
	 * @param testName
	 * @throws Exception
	 */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"readItem", "CRUDTests", "verifyIgnoredUpdateWithInAuthority"})
    public void updateItem(String testName) throws Exception {
        // Perform setup.
        setupUpdate();
        AUTHORITY_ITEM_TYPE theUpdate = null;

        // Retrieve the contents of a resource to update.
        AuthorityClientImpl<AUTHORITY_ITEM_TYPE, AuthorityProxy> client =
        		(AuthorityClientImpl<AUTHORITY_ITEM_TYPE, AuthorityProxy>)this.getClientInstance();
        Response res = client.readItem(knownResourceId, knownItemResourceId);
        try {
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": read status = " + res.getStatus());
	        }
	        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);
	
	        if (logger.isDebugEnabled()) {
	            logger.debug("got Authority item to update with ID: "
	                    + knownItemResourceId
	                    + " in authority: " + knownResourceId);
	        }
	        AUTHORITY_ITEM_TYPE authorityItem = extractItemCommonPartValue(res);
	        Assert.assertNotNull(authorityItem);

	        // Update the contents of this resource.
	        theUpdate = updateItemInstance(authorityItem);
	        if (logger.isDebugEnabled()) {
	            logger.debug("\n\nTo be updated fields: CSID = "  + knownItemResourceId + "\n"
	            		+ objectAsXmlString(theUpdate));
	        }
        } finally {
        	res.close();
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = this.createItemRequestTypeInstance(theUpdate);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug("updateItem: status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        // Retrieve the updated resource and verify that its contents exist.
	        AUTHORITY_ITEM_TYPE updatedVocabularyItem = extractItemCommonPartValue(res);
	        Assert.assertNotNull(updatedVocabularyItem);

	        compareUpdatedItemInstances(theUpdate, updatedVocabularyItem);
        } finally {
        	res.close();
        }
    }
    
    protected abstract PoxPayloadOut createNonExistenceItemInstance(String commonPartName, String identifier);
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateNonExistent(java.lang.String)
     */
    @Test(dataProvider = "testName",
    	dependsOnMethods = {"create", "update", "updateNonExistent"})
    public void updateNonExistentItem(String testName) throws Exception {
    	// Perform setup.
    	setupUpdateNonExistent();

    	// Submit the request to the service and store the response.
    	// Note: The ID used in this 'create' call may be arbitrary.
    	// The only relevant ID may be the one used in update(), below.
        AuthorityClientImpl<AUTHORITY_ITEM_TYPE, AuthorityProxy> client =
        		(AuthorityClientImpl<AUTHORITY_ITEM_TYPE, AuthorityProxy>)this.getClientInstance();
    	PoxPayloadOut multipart = createNonExistenceItemInstance(client.getItemCommonPartName(), NON_EXISTENT_ID);
    	Response res = client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
    	try {
	    	int statusCode = res.getStatus();
	
	    	// Check the status code of the response: does it match
	    	// the expected response(s)?
	    	if (logger.isDebugEnabled()) {
	    		logger.debug(testName + ": status = " + statusCode);
	    	}
	    	Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	    			invalidStatusCodeMessage(testRequestType, statusCode));
	    	Assert.assertEquals(statusCode, testExpectedStatusCode);
    	} finally {
    		res.close();
    	}
    }
        
    //
    // Methods to persuade TestNG to follow the correct test dependency path
    //
    
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createItem"})
    public void baseAuthorityTests(String testName) {
    	// Do nothing.  Here just to setup a test dependency chain.
    }
    
    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */
    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
	}
        
    @Override
    public void cleanUp() {
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
        }
        
        AuthorityClient client = (AuthorityClient) this.getClientInstance();
        String parentResourceId;
        String itemResourceId;
        //
        // Clean up all authority item resources.
        //
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            Response response = client.deleteItem(parentResourceId, itemResourceId);
            try {
            	int status = response.getStatus();
            	if (status != Response.Status.OK.getStatusCode()) {
            		logger.debug(String.format("Could not deleted authority item '%s' in authority '%s'.",
            				itemResourceId, parentResourceId));
            	}
            } finally {
            	response.close();
            }
        }
        //
        // Clean up authority items that were the result of a sync with the SAS
        // all the IDs are URN (not CSIDs).  The URNs work for the local items as well
        // as the SAS items.
        //
        for (Map.Entry<String, String> entry : allSASResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            client.deleteItem(parentResourceId, itemResourceId).close();
        }
        //
        // Clean up authority items on the SAS using the SAS client.
        //
        client = (AuthorityClient) this.getSASClientInstance();
        for (Map.Entry<String, String> entry : allSASResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            client.deleteItem(parentResourceId, itemResourceId).close();
        }
        //
        // Finally, call out superclass's cleanUp method to deleted the local authorities
        //
        super.cleanUp();
        //
        // Call out superclass's cleanUp method to delete the SAS authorities
        //
        super.cleanUp(client);        
    }
    
	protected String getTestAuthorityItemShortId() {
		return getTestAuthorityItemShortId(false);
	}

	protected String getTestAuthorityItemShortId(boolean makeUnique) {
		String result = TEST_SHORTID;
		
		if (makeUnique == true) {
			result = result + System.currentTimeMillis() + Math.abs(random.nextInt());
		}
		
		return result;
	}
}
