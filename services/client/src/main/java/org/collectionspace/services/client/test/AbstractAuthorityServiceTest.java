package org.collectionspace.services.client.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.XmlTools;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.description.ServiceDescription;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;
import org.collectionspace.services.relation.RelationshipType;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;
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
@SuppressWarnings("rawtypes")
public abstract class AbstractAuthorityServiceTest<AUTHORITY_COMMON_TYPE, AUTHORITY_ITEM_TYPE> 
	extends AbstractPoxServiceTestImpl<AbstractCommonList, AUTHORITY_COMMON_TYPE> {

    private final Logger logger = LoggerFactory.getLogger(AbstractAuthorityServiceTest.class);
	
    protected String knownResourceShortIdentifer = null;
	protected static final String READITEMS_SHORT_IDENTIFIER = "resourceWithItems" + random.nextInt(1000); 
	protected String knownAuthorityWithItems = null;
	protected String knownAuthorityWithItemsIdentifier = null;
	
	protected static final String SAS_IDENTIFIER = "SAS";
	protected static final String SAS_WITH_RELATIONS = "_RELATIONS_"; 

	private static final int SAS_ITEMLIST_SIZE = 5;

	protected String knownSASAuthorityResourceId = null;
	protected String knownSASAuthorityResourceShortId = null;
	protected List<String> knownSASItemIdentifiersList = new ArrayList<String>();
	protected String knownSASItemResourceId = null;
	protected HashMap<String, String> allSASResourceItemIdsCreated = new HashMap<String, String>(); /* itemURN, parentURN */;

	protected String knownResourceRefName = null;
    protected String knownItemResourceId = null;
    protected String knownItemResourceShortIdentifer = null;    
    protected int nItemsToCreateInList = 5;
    protected String TEST_SHORTID = "johnWayneActor";

	protected List<String> knownSASItemIdentifiersWithRelationsList = new ArrayList<String>();
	protected String knownSASAuthorityResourceWithRelationsId = null;
	protected String knownSASAuthorityResourceWithRelationsShortId = null;

	//
	// Used to test hierarchical relationship synchronization
	//
	private String continentShortId;
	private String countryShortId;
	private String cityShortId;

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
	 * @throws Exception 
	 */
	abstract protected String createItemInAuthority(AuthorityClient client, String vcsid, String shortId) throws Exception;
	
    
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
    protected abstract void compareUpdatedItemInstances(AUTHORITY_ITEM_TYPE original,
    		AUTHORITY_ITEM_TYPE updated,
    		boolean compareRevNumbers) throws Exception;
    
    /**
     * 
     * @param original
     * @param updated
     * @throws Exception
     */
    protected void compareUpdatedItemInstances(AUTHORITY_ITEM_TYPE original,
    		AUTHORITY_ITEM_TYPE updated) throws Exception {
    	compareUpdatedItemInstances(original, updated, false);
    }
    
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
		return this.getKnowResourceIdentifier() + SAS_IDENTIFIER;
	}
    
    /**
     * 
     * @return
     */
	protected String getSASAuthorityWithRelationsIdentifier() {
		return this.getKnowResourceIdentifier() + SAS_WITH_RELATIONS + SAS_IDENTIFIER;
	}
    
	/**
	 * 
	 * @param shortId
	 * @return
	 */
	protected String getUrnIdentifier(String shortId) {
		return String.format("urn:cspace:name(%s)", shortId);
	}
	
	/*
	 * Returns the CSID using the authority short ID.  Passed in client must be for the correct authority type -i.e., the on
	 * that corresponds to the CSID.
	 */
	protected String getCsidFromShortId(AuthorityClient client, String authShortId, String itemShortId) {
		String result = null;
		
		Response res = client.readNamedItemInNamedAuthority(authShortId, itemShortId);
		try {
			result = extractCsid(res);
		} catch (Exception e) {
			logger.error(String.format("Could not get CSID for authority item '%s' in authority '%s' of type '%s'.",
					itemShortId, authShortId, client.getServiceName()), e);
		} finally {
			res.close();
		}
		
		return result;
	}

	
    /**
     * Sets up create tests.
     */
    protected void setupSync() {
        testExpectedStatusCode = STATUS_OK;
        testRequestType = ServiceRequestType.SYNC;
        testSetup(testExpectedStatusCode, testRequestType);
    }
    
    /**
     * Gets a client to the SAS (Shared Authority Server)
     *
     * @return the client
     * @throws Exception 
     */
	protected AuthorityClient getSASClientInstance() throws Exception {
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
        AuthorityClient client = (AuthorityClient)this.getClientInstance();
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
    public void createItem(String testName) throws Exception {
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
	
	@Test(dataProvider = "testName", dependsOnMethods = {"readItem", "CRUDTests"})
    public void checkAuthorityWorkflowTransitions(String testName) throws Exception {
        //
        // First check to see if the authority supports synchronization -i.e., the "cs_replicating" lifecycle policy.
        //
    	assertSupportsSync();
		
        String itemCsid = createItemInAuthority((AuthorityClient) getClientInstance(), knownResourceId, getTestAuthorityItemShortId() + testName);
        // Add workflow states
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_REPLICATE, WorkflowClient.WORKFLOWSTATE_REPLICATED);
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_DEPRECATE, WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED);
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_DELETE, WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED_DELETED);
        // Substract workflow states
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_UNREPLICATE, WorkflowClient.WORKFLOWSTATE_DEPRECATED_DELETED);
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_UNDELETE, WorkflowClient.WORKFLOWSTATE_DEPRECATED);
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE, WorkflowClient.WORKFLOWSTATE_PROJECT);
        // Add other workflow states
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_DELETE, WorkflowClient.WORKFLOWSTATE_DELETED);
        updateItemLifeCycleState(testName, knownResourceId, itemCsid, WorkflowClient.WORKFLOWTRANSITION_REPLICATE, WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED);
    }

    
    /**
     * Verify that we can test synchronization with this authority.  Otherwise, we skip the test.
     * @throws Exception 
     */
	private void assertSupportsSync() throws Exception {
		AuthorityClient client = (AuthorityClient) this.getClientInstance();

		//
		// Test to see if we should run SAS-related tests
		//
		String testSASProperyValue = System.getProperty(CollectionSpaceClient.TEST_SAS_PROPERTY);
		if (testSASProperyValue != null) {
			Boolean shouldTestSAS = Boolean.valueOf(testSASProperyValue);
			if (shouldTestSAS.booleanValue() == false) {
				throw new SkipException(String.format("Skipped the test system property '%s' has a value of false.",
						shouldTestSAS));
			}
		}

    	//
    	// Test to see if the authority on our locally tenant supports sync
    	//
    	if (client.supportsSync() == false) {
    		throw new SkipException(String.format("Skipped the test since this authority '%s' doesn't support synchronization.",
    				client.getServiceName()));
    	}

    	//
    	// Test to see if the test tenancy's authority supports synchronization.
    	// 
    	AuthorityClient sasClient = (AuthorityClient) this.getSASClientInstance();
    	if (sasClient.supportsSync() == false) {
    		String tenantName = sasClient.getTenantName();
    		throw new SkipException(String.format("Skipped the test since this authority '%s' doesn't support synchronization on the server's tenant '%s'.",
    				sasClient.getServiceName(), tenantName != null ? tenantName : "unknown"));
    	}
    }
        
    /**
     * Sync the local with the SAS
     * @throws Exception 
     */
	@Test(dataProvider = "testName", dependsOnMethods = {"createSASItemList", "CRUDTests"})
    public void syncWithSAS(String testName) throws Exception {
        //
        // First check to see if the authority supports synchronization.
        //
    	assertSupportsSync();
    	
        //
        // Create an empty instance of the authority, so we can sync items with it.  We're
    	// using the short ID of the SAS authority.  The short ID of the local and the SAS will (must) be the same.
        //
        AuthorityClient client = (AuthorityClient) getClientInstance();
    	String localAuthorityId = null;
        try {
			localAuthorityId = createResource(client, testName, knownSASAuthorityResourceShortId, false);
		} catch (Exception e) {
			Assert.assertNotNull(localAuthorityId);
		}

    	//
    	// Now we can try to sync the SAS authority with the local one we just created.
    	//
        setupSync();
    	Response response = client.syncByName(knownSASAuthorityResourceShortId); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	response.close();
        }
        
        //
        // Check to see if the parent authority (local) is in the "Replicated" state.
        //
        setupRead();
    	response = client.readByName(knownSASAuthorityResourceShortId); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
			String workflowState = this.extractAuthorityWorkflowState(response);
			Assert.assertTrue(workflowState.contains(WorkflowClient.WORKFLOWSTATE_REPLICATED));  // After a sync, the parent should be in the "Replicated" state
        } finally {
        	response.close();
        }
    }
    
	@Test(dataProvider = "testName", dependsOnMethods = {"createSASAuthorityWithRelations", "CRUDTests"})
    public void syncWithSASWithRelations(String testName) throws Exception {
        //
        // First check to see if the authority supports synchronization.
        //
    	assertSupportsSync();
    	    	
        //
        // Create an empty instance of the authority, so we can sync items with it.  We're
    	// using the short ID of the SAS authority.  The short ID of the local and the SAS will (must) be the same.
        //
    	AuthorityClient client = (AuthorityClient) this.getClientInstance();
    	String localAuthorityId = null;
        try {
			localAuthorityId = createResource(client, testName, knownSASAuthorityResourceWithRelationsShortId, false);
		} catch (Exception e) {
			Assert.assertNotNull(localAuthorityId);
		}

    	//
    	// Now we can try to sync the SAS authority with the local one we just created.
    	//
        setupSync();
    	Response response = client.syncByName(knownSASAuthorityResourceWithRelationsShortId); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	response.close();
        }
        
        //
        // Check to see if the parent authority (local) is in the "Replicated" state.
        //
        setupRead();
    	response = client.readByName(knownSASAuthorityResourceWithRelationsShortId); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
			String workflowState = this.extractAuthorityWorkflowState(response);
			Assert.assertTrue(workflowState.contains(WorkflowClient.WORKFLOWSTATE_REPLICATED));  // After a sync, the parent should be in the "Replicated" state
        } finally {
        	response.close();
        }
    }
    
    /**
     * Check to make sure the sync with the SAS returned the correct number of items as well as items
     * with the correct short IDs.
     * @throws Exception 
     * 
     */
	@Test(dataProvider = "testName", dependsOnMethods = {"syncWithSAS", "CRUDTests"})
    public void veryifySyncWithSAS(String testName) throws Exception {
        //
    	// First check to see if we support sync.
    	//
    	assertSupportsSync();
    	
    	// Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = null;
        res = client.readItemListForNamedAuthority(knownSASAuthorityResourceShortId, null, null);

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
	        Assert.assertEquals(nItemsTotal, SAS_ITEMLIST_SIZE);
	        //
	        // Verify that each of our local items has a corresponding remote item
	        //
			for (AbstractCommonList.ListItem item : items) {
				String shortId = AbstractCommonListUtils.ListItemGetElementValue(
						item, AuthorityClient.SHORT_IDENTIFIER);
				Assert.assertTrue(knownSASItemIdentifiersList.contains(shortId)); // The local short ID should be in SAS list
				
				String workflowState = AbstractCommonListUtils.ListItemGetElementValue(
						item, CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE);
				Assert.assertTrue(workflowState.contains(WorkflowClient.WORKFLOWSTATE_REPLICATED)); // Workflow state must show up in a "replicated" state
				
				String refName = AbstractCommonListUtils.ListItemGetElementValue(
						item, AuthorityClient.REF_NAME);
				Assert.assertTrue((refName != null), "Item refName is null or not set in the item list!");
				String termDisplayName = AbstractCommonListUtils.ListItemGetElementValue(item,
						AuthorityClient.TERM_DISPLAY_NAME);
				String vocabDisplayName = AbstractCommonListUtils.ListItemGetElementValue(item,
						AuthorityClient.VOCAB_DISPLAY_NAME);
				// One of these names needs to be set.
				Assert.assertTrue(!(termDisplayName == null && vocabDisplayName == null), "The item's display name is null or not set in the item list!");
			}
	        
	        if(logger.isTraceEnabled()){
	        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
	        }
        } finally {
        	res.close();
        }
    }
    
	@Test(dataProvider = "testName", dependsOnMethods = {"syncWithSASWithRelations", "CRUDTests"})
    public void veryifySyncWithSASWithRelations(String testName) throws Exception {
        //
    	// First check to see if we support sync.
    	//
    	assertSupportsSync();
    	
    	// Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        AuthorityClient client = (AuthorityClient) getClientInstance();
        Response res = null;
        res = client.readItemListForNamedAuthority(knownSASAuthorityResourceWithRelationsShortId, null, null);

        try {
	        int statusCode = res.getStatus();
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        AbstractCommonList list = res.readEntity(AbstractCommonList.class);
	        long nItemsTotal = list.getTotalItems();
	        Assert.assertEquals(nItemsTotal, knownSASItemIdentifiersWithRelationsList.size());
	        //
	        // Verify that each of our local items has a corresponding remote item
	        //
	        List<AbstractCommonList.ListItem> itemList = list.getListItem();
			for (AbstractCommonList.ListItem item : itemList) {
				String shortId = AbstractCommonListUtils.ListItemGetElementValue(
						item, AuthorityClient.SHORT_IDENTIFIER);
				Assert.assertTrue(knownSASItemIdentifiersWithRelationsList.contains(shortId)); // The local short ID should be in SAS list
				
				String workflowState = AbstractCommonListUtils.ListItemGetElementValue(
						item, CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE);
				Assert.assertTrue(workflowState.contains(WorkflowClient.WORKFLOWSTATE_REPLICATED)); // Workflow state must show up in a "replicated" state
				
				String refName = AbstractCommonListUtils.ListItemGetElementValue(
						item, AuthorityClient.REF_NAME);
				Assert.assertTrue((refName != null), "Item refName is null or not set in the item list!");
				String termDisplayName = AbstractCommonListUtils.ListItemGetElementValue(item,
						AuthorityClient.TERM_DISPLAY_NAME);
				String vocabDisplayName = AbstractCommonListUtils.ListItemGetElementValue(item,
						AuthorityClient.VOCAB_DISPLAY_NAME);
				// One of these names needs to be set.
				Assert.assertTrue(!(termDisplayName == null && vocabDisplayName == null), "The item's display name is null or not set in the item list!");
			}	        
        } finally {
        	res.close();
        }
        
        //
        // Verify the relationship hierarchy is correct.
        //
    	AuthorityClient authorityClient = (AuthorityClient) getClientInstance();
        ServiceDescription serviceDescription = authorityClient.getServiceDescription();
        final String authorityItemDocumentType = serviceDescription == null ? "unknown" : serviceDescription.getSubresourceDocumentType();
        
        verifyBroaderRelationship(authorityClient, knownSASAuthorityResourceWithRelationsShortId, cityShortId, countryShortId, authorityItemDocumentType);
        verifyBroaderRelationship(authorityClient, knownSASAuthorityResourceWithRelationsShortId, countryShortId, continentShortId, authorityItemDocumentType);
    }
    
    /**
     * Verifies a broader relationship exists between the subject and object.
     * 
     * @param authorityClient
     * @param authorityShortId
     * @param subjectShortId
     * @param objectShortId
     * @param authorityItemDocumentType
     * @throws Exception 
     */
    private void verifyBroaderRelationship(AuthorityClient authorityClient, String authorityShortId, String subjectShortId, String objectShortId, String authorityItemDocumentType) throws Exception {
        setupReadList();
        RelationClient relationClient = new RelationClient();
        Response res = relationClient.readList(getCsidFromShortId(authorityClient, authorityShortId, subjectShortId),
    			authorityItemDocumentType,
    			RelationshipType.HAS_BROADER.value(),
    			getCsidFromShortId(authorityClient, authorityShortId, objectShortId),
    			authorityItemDocumentType);
        try {
	        int statusCode = res.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	        
	        RelationsCommonList list = (RelationsCommonList) res.readEntity(RelationsCommonList.class);
	        Assert.assertEquals(list.getTotalItems(), 1); // Should be exactly one relationship
	        RelationListItem relationListItem = list.getRelationListItem().get(0);
	        //
	        // Check the subject and object refnames to see if the contain the correct short identifiers
	        //
	        Assert.assertTrue(relationListItem.getSubject().getRefName().contains(subjectShortId)); // The subject refname should contain our known short ID
	        Assert.assertTrue(relationListItem.getSubject().getRefName().contains(authorityShortId)); // The subject refname should contain our known short ID
	        // Now check the object refname
	        Assert.assertTrue(relationListItem.getObject().getRefName().contains(objectShortId)); // The object refname should contain our known short ID
	        Assert.assertTrue(relationListItem.getObject().getRefName().contains(authorityShortId)); // The object refname should contain our known short ID
        } finally {
        	res.close();
        }
    }
        
    /**
     * SAS - Create a new authority on the SAS server.
     * @param testName
     * @throws Exception 
     */    
	@Test(dataProvider = "testName", dependsOnMethods = {"createItem", "CRUDTests"})
    public void createSASAuthority(String testName) throws Exception {
        //
        // First check to see if the authority supports synchronization.
        //
    	assertSupportsSync();
    	
        // Perform setup.
        setupCreate();

        try {
        	String newID = createResource(getSASClientInstance(), testName, getSASAuthorityIdentifier(), true);
        	knownSASAuthorityResourceId = newID;
        	knownSASAuthorityResourceShortId = getShortId(getSASClientInstance(), knownSASAuthorityResourceId);
            if (logger.isDebugEnabled()) {
            	String.format("Created SAS authority '%s' with CSID=%s.", getSASAuthorityIdentifier(), newID);
            }
        } catch (Exception e) {
        	logger.info(String.format("Failed to create SAS authority '%s'.", getSASAuthorityIdentifier()));
        }
    }
    
	@Test(dataProvider = "testName", dependsOnMethods = {"deleteLocalItemWithSync", "CRUDTests"})
    public void createSASAuthorityWithRelations(String testName) throws Exception {
        //
        // First check to see if the authority supports synchronization.
        //
		assertSupportsSync();
		
        // Perform setup.
        setupCreate();
        //
        // Create a new authority on the remote server
        //
        AuthorityClient client = (AuthorityClient) getClientInstance();
        try {
        	String newID = createResource(getSASClientInstance(), testName, getSASAuthorityWithRelationsIdentifier(), true);
        	knownSASAuthorityResourceWithRelationsId = newID;
        	knownSASAuthorityResourceWithRelationsShortId = getShortId(getSASClientInstance(), knownSASAuthorityResourceWithRelationsId);
        } catch (Exception e) {
        	logger.info(String.format("Failed to create SAS authority with relations '%s'.", getSASAuthorityWithRelationsIdentifier()));
        }
        //
        // Add three items to the remote authority
        //
        continentShortId = "Continent" + System.currentTimeMillis() + Math.abs(random.nextInt()); // short ID needs to be unique
        String continentCSID = createItemInAuthority(getSASClientInstance(), knownSASAuthorityResourceWithRelationsId, continentShortId);
        knownSASItemIdentifiersWithRelationsList.add(continentShortId);
        allSASResourceItemIdsCreated.put(this.getUrnIdentifier(continentShortId), getUrnIdentifier(getSASAuthorityWithRelationsIdentifier()));
        
        countryShortId = "Country" + System.currentTimeMillis() + Math.abs(random.nextInt()); // short ID needs to be unique
        String countryCSID = createItemInAuthority(getSASClientInstance(), knownSASAuthorityResourceWithRelationsId, countryShortId);
        knownSASItemIdentifiersWithRelationsList.add(countryShortId);
        allSASResourceItemIdsCreated.put(this.getUrnIdentifier(countryShortId), getUrnIdentifier(getSASAuthorityWithRelationsIdentifier()));

        cityShortId = "City" + System.currentTimeMillis() + Math.abs(random.nextInt()); // short ID needs to be unique
        String cityCSID = createItemInAuthority(getSASClientInstance(), knownSASAuthorityResourceWithRelationsId, cityShortId);
        knownSASItemIdentifiersWithRelationsList.add(cityShortId);
        allSASResourceItemIdsCreated.put(this.getUrnIdentifier(cityShortId), getUrnIdentifier(getSASAuthorityWithRelationsIdentifier()));
        //
        // Create a set of hierarchical relationships between the items
        //
        ServiceDescription serviceDescription = client.getServiceDescription();
        final String authorityItemDocumentType = serviceDescription == null ? "unknown" : serviceDescription.getSubresourceDocumentType();        
        RelationClient relationClient = new RelationClient(CollectionSpaceClient.SAS_CLIENT_PROPERTIES_FILENAME); // Get a Relation service client to the SAS
        
        boolean success = false;
        try {
			createBroaderRelationship(relationClient, cityCSID, countryCSID, authorityItemDocumentType);
			createBroaderRelationship(relationClient, countryCSID, continentCSID, authorityItemDocumentType);
			success = true;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

        Assert.assertTrue(success);
    }
    
    /**
     * Creates a broader relationship record between the subject and the object
     * @param relationClient
     * @throws Exception 
     */
    private void createBroaderRelationship(RelationClient relationClient, String subjectCSID, String objectCSID, String docType) throws Exception {
        RelationsCommon relationCommon = new RelationsCommon();
        
        relationCommon.setSubjectCsid(subjectCSID);
        relationCommon.setSubjectDocumentType(docType);
        relationCommon.setObjectCsid(objectCSID);
        relationCommon.setObjectDocumentType(docType);
        relationCommon.setRelationshipType(RelationshipType.HAS_BROADER.value());
        relationCommon.setPredicateDisplayName(RelationshipType.HAS_BROADER.value());
        
        PoxPayloadOut payloadOut = relationClient.createRequestTypeInstance(relationCommon);
        Response res = relationClient.create(payloadOut);
        try {
	        if (res.getStatus() == HttpStatus.SC_CREATED) {
		        String relationCSID = extractId(res);
		        allRelationResourceIdsCreated.add(relationCSID); // keep track of what we create so we can cleanup later
	        } else {
	        	String errMsg = "Could not create Relationship.";
	        	logger.error(errMsg);
	        	throw new Exception(errMsg);
	        }
        } finally {
        	res.close();
        }
    }

    /**
     * SAS - Create an item in the SAS authority on the SAS server.
     * @param testName
     * @throws Exception 
     */
	@Test(dataProvider = "testName", dependsOnMethods = {"createSASAuthority", "CRUDTests"})
    public void createSASItemList(String testName) throws Exception {
        //
        // First check to see if the authority supports synchronization.
        //
    	assertSupportsSync();
    	
        // Perform setup for create.
        setupCreate();
        
        for (int i = 0; i < SAS_ITEMLIST_SIZE; i++) {
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
	        
	        knownSASItemIdentifiersList.add(shortId);

	        //
	        // Keep track of the SAS authority items we create, so we can delete them from
	        // the *local* authority after we perform a sync operation.  We need to keep track
	        // of the URN (not the CSID) since the CSIDs will differ on the SAS vs local.
	        //
	        this.allSASResourceItemIdsCreated.put(this.getUrnIdentifier(shortId), getUrnIdentifier(knownSASAuthorityResourceShortId));
        }
        
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"createItem"})
    public void createItemList(String testName) throws Exception {
    	knownAuthorityWithItems = createResource(testName, READITEMS_SHORT_IDENTIFIER);
    	knownAuthorityWithItemsIdentifier = getShortId(knownAuthorityWithItems);
        for (int j = 0; j < nItemsToCreateInList; j++) {
        	createItemInAuthority((AuthorityClient) getClientInstance(), knownAuthorityWithItems, this.getTestAuthorityItemShortId(true));
        }
    }
    
	private String getShortId(AuthorityClient client, String authorityCsid) throws Exception {
    	String result = null;
    	
        // Submit the request to the service and store the response.
        Response res = client.read(authorityCsid);
        try {
	        int statusCode = res.getStatus();
	        result = this.extractAuthorityShortId(res);
        } finally {
        	res.close();
        }
        
        return result;
    }
    
	private String getShortId(String authorityCsid) throws Exception {
        AuthorityClient client = (AuthorityClient) getClientInstance();
        return getShortId(client, authorityCsid);
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
		
	
    /**
     * Extracts the short ID from a service request payload
     * 
     * @param res
     * @return
     * @throws Exception
     */
	protected String extractAuthorityShortId(Response res) throws Exception {
		String result = null;
		
        PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(getEntityResponseType()));	    	
		Document document = input.getDOMDocument();
		result = XmlTools.getElementValue(document, "//" + AuthorityClient.SHORT_IDENTIFIER);

		return result;
	}    
	
	protected String extractCsid(Response res) throws Exception {
		String result = null;
		
        PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(getEntityResponseType()));	    	
		Document document = input.getDOMDocument();
		result = XmlTools.getElementValue(document, "//" + AuthorityClient.COLLECTIONSPACE_CORE_CSID);

		return result;
	}    
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"readItem"})
    public void readItemNonExistent(String testName) throws Exception {
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
    
    protected void readItemListInt(String vcsid, String shortId, String testName) throws Exception {
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
	
			for (AbstractCommonList.ListItem item : items) {
				String refName = AbstractCommonListUtils.ListItemGetElementValue(
						item, AuthorityClient.REF_NAME);
				Assert.assertTrue((refName != null), "Item refName is null or not set in the item list!");
				String termDisplayName = AbstractCommonListUtils.ListItemGetElementValue(item,
						AuthorityClient.TERM_DISPLAY_NAME);
				String vocabDisplayName = AbstractCommonListUtils.ListItemGetElementValue(item,
						AuthorityClient.VOCAB_DISPLAY_NAME);
				// One of these names needs to be set.
				Assert.assertTrue(!(termDisplayName == null && vocabDisplayName == null), "The item's display name is null or not set in the item list!");
			}
	        
	        if(logger.isTraceEnabled()){
	        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
	        }
        } finally {
        	res.close();
        }
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"createItemList"})
    public void readItemList(String testName) throws Exception {
        readItemListInt(knownAuthorityWithItems, null, testName);
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"readItemList"})
    public void readItemListByName(String testName) throws Exception {
        readItemListInt(null, knownAuthorityWithItemsIdentifier, testName);
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) throws Exception {
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
    
	public PoxPayloadOut createItemRequestTypeInstance(AUTHORITY_ITEM_TYPE itemTypeInstance) throws Exception {
		PoxPayloadOut result = null;
		
        AuthorityClient client = (AuthorityClient) getClientInstance();
        PoxPayloadOut payloadOut = new PoxPayloadOut(this.getServicePathItemsComponent());
        payloadOut.addPart(client.getItemCommonPartName(), itemTypeInstance);
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
        AuthorityClient client = (AuthorityClient)this.getClientInstance();
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
    
	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"updateLocalItemWithSync", "CRUDTests"})
    public void deleteLocalItemWithSync(String testName) throws Exception {
    	final int itemIndexToDelete = 1;
        //
    	// First check to see if we support sync.
    	//
    	assertSupportsSync();
    	
        // Perform test setup for a DELETE.
        setupDelete();

        // Delete an item from the SAS server
        AuthorityClient sasClient = (AuthorityClient) this.getSASClientInstance();
        Response res = sasClient.deleteNamedItemInNamedAuthority(knownSASAuthorityResourceShortId, knownSASItemIdentifiersList.get(itemIndexToDelete));
        try {
	        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);	
        } finally {
        	res.close();
        }
        
        // Synchronize the local item's parent authority and verify the delete we just made
        // to the SAS takes place locally after the sync -i.e., the local item should be deleted as well.
        setupSync();
        AuthorityClient localClient = (AuthorityClient) this.getClientInstance();
    	Response response = localClient.syncByName(knownSASAuthorityResourceShortId); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode), invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	response.close();
        }        
        
        // Verify that the local item has been deleted.
        setupReadNonExistent();
        res = localClient.readNamedItemInNamedAuthority(knownSASAuthorityResourceShortId, knownSASItemIdentifiersList.get(itemIndexToDelete));
        try {
	        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);
	        knownSASItemIdentifiersList.remove(0); // remove it from our known set now that we've deleted it
        } finally {
        	res.close();
        }
    }
    
    /**
     * We create a new item on the SAS, perform a sync with the local authority, and verify the local authority contains a copy
     * of the SAS item. 
     */
	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"veryifySyncWithSAS", "CRUDTests"})
    public void updateLocalItemWithSync(String testName) throws Exception {
        //
    	// First check to see if we support sync.
    	//
    	assertSupportsSync();
    	
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a shared authority item that we're going to update.
        AUTHORITY_ITEM_TYPE theUpdate = null;
        AUTHORITY_ITEM_TYPE sasAuthorityItem = null;
        AuthorityClient sasClient = (AuthorityClient) this.getSASClientInstance();
        Response res = sasClient.readNamedItemInNamedAuthority(knownSASAuthorityResourceShortId, knownSASItemIdentifiersList.get(0));
        try {
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": read status = " + res.getStatus());
	        }
	        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);
	
	        sasAuthorityItem = extractItemCommonPartValue(res);  // This is a SAS authority item
	        Assert.assertNotNull(sasAuthorityItem);
        } finally {
        	res.close();
        }

        // Update the contents of this authority item so we can post an UPDATE to the server
        theUpdate = updateItemInstance(sasAuthorityItem);

        // Submit the updated authority item and check the response.
        PoxPayloadOut output = this.createItemRequestTypeInstance(theUpdate);
        res = sasClient.updateNamedItemInNamedAuthority(knownSASAuthorityResourceShortId, knownSASItemIdentifiersList.get(0), output);
        AUTHORITY_ITEM_TYPE updatedSASAuthorityItem = null;
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug("updateItem: status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        // Retrieve the updated authority item and verify that its contents exist.
	        updatedSASAuthorityItem = extractItemCommonPartValue(res);
	        Assert.assertNotNull(updatedSASAuthorityItem);

	        compareUpdatedItemInstances(theUpdate, updatedSASAuthorityItem);
        } finally {
        	res.close();
        }
        
        // Synchronize the local item's parent authority and verify the update we just made
        // to the SAS item appears locally after the sync
        setupSync();
        AuthorityClient localClient = (AuthorityClient) this.getClientInstance();
    	Response response = localClient.syncByName(knownSASAuthorityResourceShortId); // Notice we're using the Short ID (short ID is the same on the local and SAS)
        try {
	        int statusCode = response.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode), invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	response.close();
        }        
        
        setupRead();
        res = localClient.readNamedItemInNamedAuthority(knownSASAuthorityResourceShortId, knownSASItemIdentifiersList.get(0));
        try {
	        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);
	        AUTHORITY_ITEM_TYPE syncedAuthorityItem = extractItemCommonPartValue(res);
	        Assert.assertNotNull(syncedAuthorityItem);
	        compareUpdatedItemInstances(syncedAuthorityItem, updatedSASAuthorityItem);
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
        AuthorityClient client = (AuthorityClient)this.getClientInstance();
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
    public void cleanUp() throws Exception {
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
        AuthorityClient sasClient = (AuthorityClient) this.getSASClientInstance();
        for (Map.Entry<String, String> entry : allSASResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            sasClient.deleteItem(parentResourceId, itemResourceId).close();
        }
        //
        // Finally, call out superclass's cleanUp method to deleted the local authorities
        //
        super.cleanUp();
        //
        // Call out superclass's cleanUp method to delete the SAS authorities
        //
        super.cleanUp(sasClient);
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
	
    /**
     * Sets up create tests with empty entity body.
     */
    @Override
	protected void setupCreateWithEmptyEntityBody() {
        testExpectedStatusCode = STATUS_INTERNAL_SERVER_ERROR;
        testRequestType = ServiceRequestType.CREATE;
        testSetup(testExpectedStatusCode, testRequestType);
    }
	
    /**
     * Sets up create tests with malformed xml.
     */
    @Override
	protected void setupCreateWithMalformedXml() {
        testExpectedStatusCode = STATUS_INTERNAL_SERVER_ERROR;  // Malformed payload never gets past RESTEasy filter
        testRequestType = ServiceRequestType.CREATE;
        testSetup(testExpectedStatusCode, testRequestType);
    }

    /**
     * Sets up create tests with wrong xml schema.
     */
    @Override
	protected void setupCreateWithWrongXmlSchema() {
        testExpectedStatusCode = STATUS_INTERNAL_SERVER_ERROR;  // Empty payload never gets past RESTEasy filter
        testRequestType = ServiceRequestType.CREATE;
        testSetup(testExpectedStatusCode, testRequestType);
    }    
    
}
