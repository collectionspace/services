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

import org.collectionspace.services.TaxonJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.client.TaxonomyAuthorityClientUtils;
import org.collectionspace.services.client.TaxonomyAuthorityProxy;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.taxonomy.TaxonAuthorGroup;
import org.collectionspace.services.taxonomy.TaxonAuthorGroupList;
import org.collectionspace.services.taxonomy.TaxonCitationList;
import org.collectionspace.services.taxonomy.TaxonomyauthorityCommon;
import org.collectionspace.services.taxonomy.TaxonCommon;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * TaxonomyAuthorityServiceTest, carries out tests against a
 * deployed and running TaxonomyAuthority Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class TaxonomyAuthorityServiceTest extends AbstractAuthorityServiceTest<TaxonomyauthorityCommon, TaxonCommon> {

    /** The logger. */
    private final String CLASS_NAME = TaxonomyAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(TaxonomyAuthorityServiceTest.class);
    private final String REFNAME = "refName";
    private final String DISPLAYNAME = "displayName";

    final String TEST_SHORTID = "CentauruspleurexanthemusGreen1832";
    final String TEST_TERM_STATUS = "accepted";
    final String TEST_TAXON_FULL_NAME = "Centaurus pleurexanthemus Green 1832";
    // TODO Re-implement the Taxon Rank field so as to provide an orderable
    // ranking value, as well as a display name.
    final String TEST_TAXON_RANK = "species";
    final String TEST_TAXON_AUTHOR = "J. Green";
    final String TEST_TAXON_AUTHOR_TYPE = "ascribed";
    final String TEST_TAXON_CITATION = "A Monograph of the Trilobites of North America";
    final String TEST_TAXON_CURRENCY = "current";
    final String TEST_TAXON_YEAR = "1832";
    final String TEST_TAXONOMIC_STATUS = "valid";
    final String TEST_TAXON_IS_NAMED_HYBRID = "false";
    final TaxonAuthorGroupList NULL_TAXON_AUTHOR_GROUP_LIST = null;
    final TaxonCitationList NULL_TAXON_CITATION_LIST = null;
    
    private String knownResourceShortIdentifer = null;
    private String knownTaxonomyTypeRefName = null;

    @Override
    public String getServicePathComponent() {
        return TaxonomyAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return TaxonomyAuthorityClient.SERVICE_NAME;
    }

    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }
        
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient<AbstractCommonList, PoxPayloadOut, String, TaxonomyAuthorityProxy> getClientInstance() {
        return new TaxonomyAuthorityClient();
    }

    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    private String createItemInAuthority(String vcsid, String authRefName) {

        final String testName = "createItemInAuthority(" + vcsid + "," + authRefName + ")";
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner(testName, CLASS_NAME));
        }

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        Map<String, String> taxonMap = new HashMap<String, String>();

        // Fields present in all authority records.
        taxonMap.put(TaxonJAXBSchema.SHORT_IDENTIFIER, TEST_SHORTID);
        // TODO Make term status be controlled vocab.
        taxonMap.put(TaxonJAXBSchema.TERM_STATUS, TEST_TERM_STATUS);

        // Fields specific to this specific authority record type.
        taxonMap.put(TaxonJAXBSchema.NAME, TEST_TAXON_FULL_NAME);
        taxonMap.put(TaxonJAXBSchema.TAXON_RANK, TEST_TAXON_RANK);
        taxonMap.put(TaxonJAXBSchema.TAXON_CURRENCY, TEST_TAXON_CURRENCY);
        taxonMap.put(TaxonJAXBSchema.TAXON_YEAR, TEST_TAXON_YEAR);
        taxonMap.put(TaxonJAXBSchema.TAXONOMIC_STATUS, TEST_TAXONOMIC_STATUS);
        taxonMap.put(TaxonJAXBSchema.TAXON_IS_NAMED_HYBRID, TEST_TAXON_IS_NAMED_HYBRID);

        TaxonCitationList taxonCitationList = new TaxonCitationList();
        List<String> taxonCitations = taxonCitationList.getTaxonCitation();
        taxonCitations.add(TEST_TAXON_CITATION);

        TaxonAuthorGroupList taxonAuthorGroupList = new TaxonAuthorGroupList();
        List<TaxonAuthorGroup> taxonAuthorGroups = taxonAuthorGroupList.getTaxonAuthorGroup();
        TaxonAuthorGroup taxonAuthorGroup = new TaxonAuthorGroup();
        taxonAuthorGroup.setTaxonAuthor(TEST_TAXON_AUTHOR);
        taxonAuthorGroup.setTaxonAuthorType(TEST_TAXON_AUTHOR_TYPE);
        taxonAuthorGroups.add(taxonAuthorGroup);

        // FIXME: Add additional fields in the Taxon record here,
        // including at least one each of:
        // * a Boolean field (when implemented)
        // * an authref field (when implemented)

        String newID = TaxonomyAuthorityClientUtils.createItemInAuthority(vcsid,
                authRefName, taxonMap, taxonAuthorGroupList, taxonCitationList, client);

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            setKnownItemResource(newID, TEST_SHORTID);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + newID + " inAuthority=" + vcsid);
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
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"readItem", "updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {
        //
    	// First read in our known resource
    	//
    	setupRead();
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        TaxonCommon taxon = null;
        try {
        	assertStatusCode(res, testName);
            // Check whether taxon has expected displayName.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            taxon = (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(taxon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Now setup for an update
        //
        String displayName = taxon.getDisplayName();
        // Make sure displayName matches computed form
        String expectedDisplayName =
                TaxonomyAuthorityClientUtils.prepareDefaultDisplayName(TEST_TAXON_FULL_NAME);
        Assert.assertNotNull(displayName, "Display name was null.  Expected it to be: " + expectedDisplayName);
        // Update the shortName and verify the computed name is updated.
        taxon.setCsid(null);
        taxon.setDisplayNameComputed(true);
        taxon.setTaxonFullName("updated-" + TEST_TAXON_FULL_NAME);
        expectedDisplayName =
                TaxonomyAuthorityClientUtils.prepareDefaultDisplayName("updated-" + TEST_TAXON_FULL_NAME);

        // Create the update payload.
        PoxPayloadOut output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), taxon);

        setupUpdate();
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        TaxonCommon updatedTaxon = null;
        try {
        	assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
        	PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            updatedTaxon =
                    (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(updatedTaxon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedTaxon.getTaxonFullName(), taxon.getTaxonFullName(),
                "Updated ForeName in Taxonomy did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedTaxon.getDisplayName(), expectedDisplayName,
                "Updated ForeName in Taxonomy not reflected in computed DisplayName.");
        //
        // Now Update the displayName, not computed and verify the computed name is overriden.
        //
        taxon.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        taxon.setDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(client.getItemCommonPartName(), taxon);
        setupUpdate(); // setup expected status code for result
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
        	assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
        	PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            updatedTaxon =
                    (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(updatedTaxon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedTaxon.isDisplayNameComputed(), false,
                "Updated displayNameComputed in Taxonomy did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedTaxon.getDisplayName(),
                expectedDisplayName,
                "Updated DisplayName (not computed) in Taxonomy not stored.");
    }

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"verifyItemDisplayName"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        //
    	// First read in our known resource.
    	//
        setupRead();
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        TaxonCommon taxon = null;
        try {
            assertStatusCode(res, testName);
            // Check whether Taxonomy has expected displayName.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            taxon = (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(taxon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Try to Update with computed false and no displayName
        //
        taxon.setDisplayNameComputed(false);
        taxon.setDisplayName(null);
        
        PoxPayloadOut output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), taxon);
        setupUpdateWithInvalidBody(); // we expect a failure here
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
        	assertStatusCode(res, testName);
        } finally {
            res.releaseConnection();
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
     * @param vcsid the vcsid
     * @param name the name
     */
    private void readItemList(String vcsid, String shortId) {
        String testName = "readItemList";

        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<AbstractCommonList> res = null;
        if (vcsid != null) {
            res = client.readItemList(vcsid, null, null);
        } else if (shortId != null) {
            res = client.readItemListForNamedAuthority(shortId, null, null);
        } else {
            Assert.fail("readItemList passed null csid and name!");
        }
        try {
            assertStatusCode(res, testName);
        	AbstractCommonList list = res.getEntity();
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            List<AbstractCommonList.ListItem> items =
                    list.getListItem();
            int nItemsReturned = items.size();
            // There will be 'nItemsToCreateInList'
            // items created by the createItemList test,
            // all associated with the same parent resource.
            int nExpectedItems = nItemsToCreateInList;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": Expected "
                        + nExpectedItems + " items; got: " + nItemsReturned);
            }
            Assert.assertEquals(nItemsReturned, nExpectedItems);

            for (AbstractCommonList.ListItem item : items) {
            	String value = 
            		AbstractCommonListUtils.ListItemGetElementValue(item, REFNAME);
                Assert.assertTrue((null != value), "Item refName is null!");
            	value = 
            		AbstractCommonListUtils.ListItemGetElementValue(item, DISPLAYNAME);
                Assert.assertTrue((null != value), "Item displayName is null!");
            }
            if(logger.isTraceEnabled()){
            	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
            }
        } finally {
            res.releaseConnection();
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
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
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
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        parentResourceId = knownResourceId;
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
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
	// Taxonomy authority specific instances
	//
	
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        String baseRefName =
                TaxonomyAuthorityClientUtils.createTaxonomyAuthRefName(shortId, null);
        PoxPayloadOut multipart =
                TaxonomyAuthorityClientUtils.createTaxonomyAuthorityInstance(
                displayName, shortId, commonPartName);
        return multipart;
	}
	
	@Override
    protected PoxPayloadOut createNonExistenceInstance(String commonPartName, String identifier) {
        String displayName = "displayName-NON_EXISTENT_ID";
    	PoxPayloadOut result = TaxonomyAuthorityClientUtils.createTaxonomyAuthorityInstance(
    				displayName, "nonEx", commonPartName);
    	return result;
    }		

	@Override
	protected TaxonomyauthorityCommon updateInstance(
			TaxonomyauthorityCommon taxonomyAuthority) {
		TaxonomyauthorityCommon result = new TaxonomyauthorityCommon();
		
		result.setDisplayName("updated-" + taxonomyAuthority.getDisplayName());
		result.setVocabType("updated-" + taxonomyAuthority.getVocabType());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(TaxonomyauthorityCommon original,
			TaxonomyauthorityCommon updated) throws Exception {
        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
	}
	
	//
	// Authority item specific overrides
	//

	@Override
	protected String createItemInAuthority(String authorityId) {
		return createItemInAuthority(authorityId, null /*refname*/);
	}
	
	@Override
	protected TaxonCommon updateItemInstance(TaxonCommon authorityItem) {
		TaxonCommon result = new TaxonCommon();
		
        result.setDisplayName("updated-" + authorityItem.getTaxonFullName());
        result.setTaxonFullName("updated-" + authorityItem.getTaxonFullName());
		
		return result;
	}

	@Override
	protected void compareUpdatedItemInstances(TaxonCommon original,
			TaxonCommon updated) throws Exception {
        Assert.assertEquals(updated.getTaxonFullName(), original.getTaxonFullName(),
                "Taxon full name in updated Taxon did not match submitted data.");
	}

	@Override
	protected void verifyReadItemInstance(TaxonCommon item) throws Exception {
        Assert.assertNotNull(item.getTaxonFullName(), "Field value is unexpectedly null.");
        Assert.assertEquals(item.getTaxonFullName(), TEST_TAXON_FULL_NAME,
                "Field value " + item.getTaxonFullName()
                + "does not match expected value " + TEST_TAXON_FULL_NAME);
	}

	@Override
	protected PoxPayloadOut createNonExistenceItemInstance(
			String commonPartName, String identifier) {
        Map<String, String> nonexMap = new HashMap<String, String>();
        nonexMap.put(TaxonJAXBSchema.NAME, TEST_TAXON_FULL_NAME);
        nonexMap.put(TaxonJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(TaxonJAXBSchema.TERM_STATUS, TEST_TERM_STATUS);
        // PoxPayloadOut multipart =
        //        TaxonomyAuthorityClientUtils.createTaxonInstance(
        //        TaxonomyAuthorityClientUtils.createTaxonomyRefName(knownResourceRefName, "nonEx", "Non Existent"),
        //        nonexMap, NULL_TAXON_AUTHOR_GROUP_LIST, NULL_TAXON_CITATION_LIST,
        //        client.getItemCommonPartName());
        final String EMPTY_REFNAME = "";
        PoxPayloadOut result =
                TaxonomyAuthorityClientUtils.createTaxonInstance(EMPTY_REFNAME,
                nonexMap, NULL_TAXON_AUTHOR_GROUP_LIST, NULL_TAXON_CITATION_LIST,
                commonPartName);
		return result;
	}
}
