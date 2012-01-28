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

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.ContactClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.contact.AddressGroup;
import org.collectionspace.services.contact.AddressGroupList;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.organization.MainBodyGroup;
import org.collectionspace.services.organization.MainBodyGroupList;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrganizationsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * OrgAuthorityServiceTest, carries out tests against a
 * deployed and running OrgAuthority Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class OrgAuthorityServiceTest extends AbstractAuthorityServiceTest<OrgauthoritiesCommon, OrganizationsCommon> {

    /** The logger. */
    private final String CLASS_NAME = OrgAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private final String REFNAME = "refName";
    private final String DISPLAYNAME = "displayName";

    @Override
    public String getServicePathComponent() {
        return OrgAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return OrgAuthorityClient.SERVICE_NAME;
    }
    /** The test organization shortname. */
    private final String TEST_ORG_SHORTNAME = "Test Org";
    
    /** The test organization founding place. */
    private final String TEST_ORG_FOUNDING_PLACE = "Anytown, USA";
    
    private String knownItemResourceShortIdentifer = null;
    
    /** The known contact resource id. */
    private String knownContactResourceId = null;
    
    /** The all contact resource ids created. */
    private Map<String, String> allContactResourceIdsCreated =
            new HashMap<String, String>();

    protected void setKnownItemResource(String id, String shortIdentifer) {
        knownItemResourceId = id;
        knownItemResourceShortIdentifer = shortIdentifer;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new OrgAuthorityClient();
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
        OrgAuthorityClient client = new OrgAuthorityClient();
        String displayName = "displayName-" + identifier;
        PoxPayloadOut multipart = OrgAuthorityClientUtils.createOrgAuthorityInstance(
                displayName, identifier, client.getCommonPartName());
        return multipart;
    }

    @Override
    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) {
        String headerLabel = new OrgAuthorityClient().getItemCommonPartName();
        String shortId = "testOrg";
        Map<String, String> testOrgMap = new HashMap<String, String>();
        testOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId);
        testOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, TEST_ORG_SHORTNAME);
        testOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "The real official test organization");
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "May 26, 1907");
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, TEST_ORG_FOUNDING_PLACE);

        return OrgAuthorityClientUtils.createOrganizationInstance(identifier, testOrgMap, headerLabel);
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
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":...");
        }

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        String shortId = "testOrg";
        Map<String, String> testOrgMap = new HashMap<String, String>();
        testOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId);
        testOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, TEST_ORG_SHORTNAME);
        testOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "The real official test organization");
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "May 26, 1907");
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, TEST_ORG_FOUNDING_PLACE);

        Map<String, List<String>> testOrgRepeatablesMap = new HashMap<String, List<String>>();
        List<String> testOrgContactNames = new ArrayList<String>();
        testOrgContactNames.add("joe@example.org");
        testOrgContactNames.add("sally@example.org");
        testOrgRepeatablesMap.put(OrganizationJAXBSchema.CONTACT_NAMES, testOrgContactNames);

        MainBodyGroupList mainBodyList = new MainBodyGroupList();
        List<MainBodyGroup> mainBodyGroups = mainBodyList.getMainBodyGroup();
        MainBodyGroup mainBodyGroup = new MainBodyGroup();
        mainBodyGroup.setShortName(TEST_ORG_SHORTNAME);
        mainBodyGroup.setLongName("The real official test organization");
        mainBodyGroups.add(mainBodyGroup);

        String newID = OrgAuthorityClientUtils.createItemInAuthority(
                vcsid, authRefName, testOrgMap, testOrgRepeatablesMap, mainBodyList, client);

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            setKnownItemResource(newID, shortId);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allResourceItemIdsCreated.put(newID, vcsid);

        return newID;
    }

    /**
     * Creates the contact.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"create"},
    		dependsOnMethods = {"createItem"})
    public void createContact(String testName) {
        setupCreate();
        String newID = createContactInItem(knownResourceId, knownItemResourceId);
    }

    /**
     * Creates the contact in item.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the string
     */
    private String createContactInItem(String parentcsid, String itemcsid) {

        final String testName = "createContactInItem";
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner(testName, CLASS_NAME));
        }
        setupCreate();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart =
                ContactClientUtils.createContactInstance(parentcsid,
                itemcsid, identifier, new ContactClient().getCommonPartName());

        String newID = null;
        ClientResponse<Response> res =
                client.createContact(parentcsid, itemcsid, multipart);
        try {
            assertStatusCode(res, testName);
            newID = OrgAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Store the ID returned from the first contact resource created
        // for additional tests below.
        if (knownContactResourceId == null) {
            knownContactResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownContactResourceId=" + knownContactResourceId);
            }
        }

        // Store the IDs from any contact resources created
        // by tests, along with the IDs of their parent items,
        // so these items can be deleted after all tests have been run.
        allContactResourceIdsCreated.put(newID, itemcsid);

        return newID;
    }

    /**
     * Creates the contact list.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"createList"},
    		dependsOnMethods = {"createItemList"})
    public void createContactList(String testName) throws Exception {
        // Add contacts to the initially-created, known item record.
        for (int j = 0; j < nItemsToCreateInList; j++) {
            createContact(testName);
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
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"read"}, dependsOnGroups = {"create"})
    public void read(String testName) throws Exception {
        readInternal(testName, knownResourceId, null);
    }

    /**
     * Read by name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    groups = {"read"}, dependsOnGroups = {"create"})
    public void readByName(String testName) throws Exception {
        readInternal(testName, null, knownResourceShortIdentifer);
    }

    protected void readInternal(String testName, String CSID, String shortId) {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<String> res = null;
        if (CSID != null) {
            res = client.read(CSID);
        } else if (shortId != null) {
            res = client.readByName(shortId);
        } else {
            Assert.fail("readInternal: Internal error. One of CSID or shortId must be non-null");
        }
        try {
            assertStatusCode(res, testName);        	
            //FIXME: remove the following try catch once Aron fixes signatures
            try {
                PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
                OrgauthoritiesCommon orgAuthority = (OrgauthoritiesCommon) extractPart(input,
                        new OrgAuthorityClient().getCommonPartName(), OrgauthoritiesCommon.class);
                if (logger.isDebugEnabled()) {
                    logger.debug(objectAsXmlString(orgAuthority, OrgauthoritiesCommon.class));
                }
                Assert.assertNotNull(orgAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Read item in Named Auth.
     * 
     * TODO Enable this if we really need this - it is a funky case, where we would have
     * the shortId of the item, but the CSID of the parent authority!? Unlikely.
     *
     * @param testName the test name
     * @throws Exception the exception
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
    groups = {"readItem"}, dependsOnGroups = {"read"})
    public void readItemInNamedAuth(String testName) throws Exception {
    readItemInternal(testName, null, knownResourceShortIdentifer, knownItemResourceId, null);
    }
     */
    
    /**
     * Read named item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readItemInNamedAuth"})
    public void readNamedItem(String testName) throws Exception {
        readItemInternal(testName, knownResourceId, null, null, knownItemResourceShortIdentifer);
    }
    
    /**
     * Read item in Named Auth.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readItem"})
    public void readItemInNamedAuth(String testName) throws Exception {
        readItemInternal(testName, null, knownResourceShortIdentifer, knownItemResourceId, null);
    }

    /**
     * Read Named item in Named Auth.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readItem"})
    public void readNamedItemInNamedAuth(String testName) throws Exception {
        readItemInternal(testName, null, knownResourceShortIdentifer, null, knownItemResourceShortIdentifer);
    }

    protected void readItemInternal(String testName,
            String authCSID, String authShortId, String itemCSID, String itemShortId)
            throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<String> res = null;
        if (authCSID != null) {
            if (itemCSID != null) {
                res = client.readItem(authCSID, itemCSID);
            } else if (itemShortId != null) {
                res = client.readNamedItem(authCSID, itemShortId);
            } else {
                Assert.fail("readInternal: Internal error. One of CSID or shortId must be non-null");
            }
        } else if (authShortId != null) {
            if (itemCSID != null) {
                res = client.readItemInNamedAuthority(authShortId, itemCSID);
            } else if (itemShortId != null) {
                res = client.readNamedItemInNamedAuthority(authShortId, itemShortId);
            } else {
                Assert.fail("readInternal: Internal error. One of CSID or shortId must be non-null");
            }
        } else {
            Assert.fail("readInternal: Internal error. One of authCSID or authShortId must be non-null");
        }
        try {
            assertStatusCode(res, testName);
            // Check whether we've received a organization.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            OrganizationsCommon organization = (OrganizationsCommon) extractPart(input,
                    client.getItemCommonPartName(), OrganizationsCommon.class);
            Assert.assertNotNull(organization);
            boolean showFull = true;
            if (showFull && logger.isDebugEnabled()) {
                logger.debug(testName + ": returned payload:");
                logger.debug(objectAsXmlString(organization, OrganizationsCommon.class));
            }

            // Check that the organization item is within the expected OrgAuthority.
            Assert.assertEquals(organization.getInAuthority(), knownResourceId);

            // Verify the number and contents of values in a repeatable field,
            // as created in the instance record used for testing.
            List<String> contactNames = organization.getContactNames().getContactName();
            Assert.assertTrue(contactNames.size() > 0);
            Assert.assertNotNull(contactNames.get(0));

        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }
    
    @Override
    protected void verifyReadItemInstance(OrganizationsCommon item) throws Exception {
        List<String> contactNames = item.getContactNames().getContactName();
        Assert.assertTrue(contactNames.size() > 0);
        Assert.assertNotNull(contactNames.get(0));
    }

    /**
     * Verify item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {
        // Perform setup.
        setupRead();
        //
        // First, read our known resource.
        //
        OrgAuthorityClient client = new OrgAuthorityClient();
        PoxPayloadIn input = null;
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
            assertStatusCode(res, testName);
            // Check whether organization has expected displayName.
            input = new PoxPayloadIn(res.getEntity());
            Assert.assertNotNull(input);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Check whether organization has expected displayName.
        OrganizationsCommon organization = (OrganizationsCommon) extractPart(input,
                client.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(organization);
        String displayName = organization.getDisplayName();
        // Make sure displayName matches computed form
        String expectedDisplayName =
                OrgAuthorityClientUtils.prepareDefaultDisplayName(
                TEST_ORG_SHORTNAME, TEST_ORG_FOUNDING_PLACE);
        Assert.assertNotNull(displayName, expectedDisplayName);

        // Update the shortName and verify the computed name is updated.
        organization.setCsid(null);
        organization.setDisplayNameComputed(true);

        MainBodyGroupList mainBodyList = organization.getMainBodyGroupList();
        List<MainBodyGroup> mainBodyGroups = mainBodyList.getMainBodyGroup();
        MainBodyGroup mainBodyGroup = new MainBodyGroup();
        String updatedShortName = "updated-" + TEST_ORG_SHORTNAME;
        mainBodyGroup.setShortName(updatedShortName);
        mainBodyGroups.clear(); //clear all the elements and do a sparse update
        mainBodyGroups.add(mainBodyGroup);
        organization.setMainBodyGroupList(mainBodyList);

        expectedDisplayName =
                OrgAuthorityClientUtils.prepareDefaultDisplayName(
                updatedShortName, TEST_ORG_FOUNDING_PLACE);
        //
        // Next, submit the updated resource to the service and store the response.
        //
        setupUpdate();
        PoxPayloadOut output = new PoxPayloadOut(OrgAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), organization);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Now verify the update was correct.
        //
        OrganizationsCommon updatedOrganization =
                (OrganizationsCommon) extractPart(input,
                client.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(updatedOrganization);

        // Verify that the updated resource received the correct data.
        mainBodyList = organization.getMainBodyGroupList();
        Assert.assertNotNull(mainBodyList);
        Assert.assertTrue(mainBodyList.getMainBodyGroup().size() > 0);
        Assert.assertEquals(updatedOrganization.getMainBodyGroupList().getMainBodyGroup().get(0).getShortName(),
                updatedShortName, "Updated ShortName in Organization did not match submitted data.");

        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedOrganization.getDisplayName(), expectedDisplayName,
                "Updated ShortName in Organization not reflected in computed DisplayName.");
        //
        // Now Update the displayName, not computed and verify the computed name is overriden.
        //
        organization.setDisplayNameComputed(false);
        expectedDisplayName = "TestName";
        organization.setDisplayName(expectedDisplayName);

        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(OrgAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(client.getItemCommonPartName(), organization);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        input = null;
        try {
            assertStatusCode(res, testName);
            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        updatedOrganization =
                (OrganizationsCommon) extractPart(input,
                client.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(updatedOrganization);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedOrganization.isDisplayNameComputed(), false,
                "Updated displayNameComputed in Organization did not match submitted data.");
        // Verify that the updated resource computes the right displayName.
        Assert.assertEquals(updatedOrganization.getDisplayName(),
                expectedDisplayName,
                "Updated DisplayName (not computed) in Organization not stored.");
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
        // Perform setup for read.
        setupRead();
        //
        // First read our known resource.
        //
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        OrganizationsCommon organization = null;
        try {
            assertStatusCode(res, testName);        	
            // Check whether organization has expected displayName.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            organization = (OrganizationsCommon) extractPart(input,
                    client.getItemCommonPartName(), OrganizationsCommon.class);
            Assert.assertNotNull(organization);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Try to Update with 'displayNameComputed' flag set to false and no displayName
        //
        organization.setDisplayNameComputed(false);
        organization.setDisplayName(null);
        
        setupUpdateWithInvalidBody(); // we expect a failure
        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(OrgAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(),
        		organization);
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
     * Read contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readItem"})
    public void readContact(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<String> res =
                client.readContact(knownResourceId, knownItemResourceId,
                knownContactResourceId);
        try {
            assertStatusCode(res, testName);        	
            // Check whether we've received a contact.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            ContactsCommon contact = (ContactsCommon) extractPart(input,
                    new ContactClient().getCommonPartName(), ContactsCommon.class);
            Assert.assertNotNull(contact);
            boolean showFull = true;
            if (showFull && logger.isDebugEnabled()) {
                logger.debug(testName + ": returned payload:");
                logger.debug(objectAsXmlString(contact, ContactsCommon.class));
            }
            Assert.assertEquals(contact.getInAuthority(), knownResourceId);
            Assert.assertEquals(contact.getInItem(), knownItemResourceId);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Read contact non existent.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readContact"})
    public void readContactNonExistent(String testName) {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<String> res =
                client.readContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
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
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Read item list.
     */
    @Override
//	@Test(groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList(String testName) {
        readItemList(knownAuthorityWithItems, null);
    }

    /**
     * Read item list by authority name.
     */
    @Override
//    @Test(dataProvider = "testName",
//    		dependsOnMethods = {"readItem"})
    public void readItemListByName(String testName) {
        readItemList(null, READITEMS_SHORT_IDENTIFIER);
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
        setupReadList();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<AbstractCommonList> res = null;
        if (vcsid != null) {
            res = client.readItemList(vcsid, null, null);
        } else if (name != null) {
            res = client.readItemListForNamedAuthority(name, null, null);
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
        if (logger.isTraceEnabled()) {
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    /**
     * Read contact list.
     */
    @Test(groups = {"readList"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.readItemList"})
    public void readContactList() {
        readContactList(knownResourceId, knownItemResourceId);
    }

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     */
    private void readContactList(String parentcsid, String itemcsid) {
        final String testName = "readContactList";
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<AbstractCommonList> res =
                client.readContactList(parentcsid, itemcsid);
        AbstractCommonList list = null;
        try {
            assertStatusCode(res, testName);
            list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        List<AbstractCommonList.ListItem> listitems =
            list.getListItem();
        int nItemsReturned = listitems.size();
        // There will be one item created, associated with a
        // known parent resource, by the createItem test.
        //
        // In addition, there will be 'nItemsToCreateInList'
        // additional items created by the createItemList test,
        // all associated with the same parent resource.
        int nExpectedItems = nItemsToCreateInList + 1;
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Expected "
                    + nExpectedItems + " items; got: " + nItemsReturned);
        }
        Assert.assertEquals(nItemsReturned, nExpectedItems);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    /**
     * Update contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.updateItem"})
    public void updateContact(String testName) throws Exception {
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<String> res =
                client.readContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        ContactsCommon contact = null;
        try {
            assertStatusCode(res, testName);        	
            if (logger.isDebugEnabled()) {
                logger.debug("got Contact to update with ID: "
                        + knownContactResourceId
                        + " in item: " + knownItemResourceId
                        + " in parent: " + knownResourceId);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            contact = (ContactsCommon) extractPart(input,
                    new ContactClient().getCommonPartName(), ContactsCommon.class);
            Assert.assertNotNull(contact);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        // Verify the contents of this resource
        AddressGroupList addressGroupList = contact.getAddressGroupList();
        Assert.assertNotNull(addressGroupList);
        List<AddressGroup> addressGroups = addressGroupList.getAddressGroup();
        Assert.assertNotNull(addressGroups);
        Assert.assertTrue(addressGroups.size() > 0);
        String addressPlace1 = addressGroups.get(0).getAddressPlace1();
        Assert.assertNotNull(addressPlace1);

        // Update the contents of this resource.
        addressGroups.get(0).setAddressPlace1("updated-" + addressPlace1);
        contact.setAddressGroupList(addressGroupList);
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated Contact");
            logger.debug(objectAsXmlString(contact,
                    ContactsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(ContactClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(contact, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ContactClient().getCommonPartName());

        res = client.updateContact(knownResourceId, knownItemResourceId, knownContactResourceId, output);
        try {
	        assertStatusCode(res, testName);
	        // Retrieve the updated resource and verify that its contents exist.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        ContactsCommon updatedContact =
	                (ContactsCommon) extractPart(input,
	                new ContactClient().getCommonPartName(), ContactsCommon.class);
	        Assert.assertNotNull(updatedContact);
	
	        // Verify that the updated resource received the correct data.
	        Assert.assertEquals(updatedContact.getAddressGroupList().getAddressGroup().get(0).getAddressPlace1(),
	                contact.getAddressGroupList().getAddressGroup().get(0).getAddressPlace1(),
	                "Data in updated object did not match submitted data.");
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Update non existent contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"update"},
    		dependsOnMethods = {"updateContact", "testContactSubmitRequest"})
    public void updateNonExistentContact(String testName) throws Exception {
        // Currently a no-op test
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    // Note: delete sub-resources in ascending hierarchical order,
    // before deleting their parents.
    /**
     * Delete contact.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", groups = {"delete"},
    		dependsOnMethods = {"updateContact"})
    public void deleteContact(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        if (logger.isDebugEnabled()) {
            logger.debug("parentcsid =" + knownResourceId
                    + " itemcsid = " + knownItemResourceId
                    + " csid = " + knownContactResourceId);
        }

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<Response> res =
                client.deleteContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
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
    	dependsOnMethods = {"verifyIllegalItemDisplayName", "testContactSubmitRequest", "deleteContact"})
    public void localDeleteItem(String testName) throws Exception {
    	super.deleteItem(testName);
    }    
    
    /**
     * Delete non existent contact.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", groups = {"delete"},
    		dependsOnMethods = {"deleteContact"})
    public void deleteNonExistentContact(String testName) {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        OrgAuthorityClient client = new OrgAuthorityClient();
        ClientResponse<Response> res =
                client.deleteContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /**
     * Test contact submit request.
     */
    @Test(dependsOnMethods = {"createContact", "readContact", "testItemSubmitRequest"})
    public void testContactSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getContactResourceURL(knownResourceId,
                knownItemResourceId, knownContactResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testContactSubmitRequest: url=" + url
                    + " status=" + statusCode);
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
    @AfterClass(alwaysRun = true)
    @Override
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
        String contactResourceId;
        // Clean up contact resources.
        parentResourceId = knownResourceId;
        OrgAuthorityClient client = new OrgAuthorityClient();
        for (Map.Entry<String, String> entry : allContactResourceIdsCreated.entrySet()) {
            contactResourceId = entry.getKey();
            itemResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                    client.deleteContact(parentResourceId, itemResourceId, contactResourceId);
            res.releaseConnection();
        }
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
        super.cleanUp();

    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    /**
     * Gets the item service path component.
     *
     * @return the item service path component
     */
    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }

    /**
     * Gets the contact service path component.
     *
     * @return the contact service path component
     */
    public String getContactServicePathComponent() {
        return ContactClient.SERVICE_PATH_COMPONENT;
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

    /**
     * Returns the root URL for the contact service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning authority, followed by the
     * path component for the owning item, followed by the path component
     * for the contact service.
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param  itemResourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The root URL for the contact service.
     */
    protected String getContactServiceRootURL(String parentResourceIdentifier,
            String itemResourceIdentifier) {
        return getItemResourceURL(parentResourceIdentifier, itemResourceIdentifier) + "/"
                + getContactServicePathComponent();
    }

    /**
     * Returns the URL of a specific contact resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent resource of the relevant item resource.
     *
     * @param  resourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The URL of a specific resource managed by a service.
     */
    protected String getContactResourceURL(String parentResourceIdentifier,
            String itemResourceIdentifier, String contactResourceIdentifier) {
        return getContactServiceRootURL(parentResourceIdentifier,
                itemResourceIdentifier) + "/" + contactResourceIdentifier;
    }

	@Override
	public void authorityTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String createItemInAuthority(String authorityId) {
		return createItemInAuthority(authorityId, null /*refname*/);
	}

	@Override
	protected OrganizationsCommon updateItemInstance(OrganizationsCommon organizationsCommon) {
		OrganizationsCommon result = organizationsCommon; //new OrganizationsCommon();
		
        MainBodyGroupList mainBodyList = organizationsCommon.getMainBodyGroupList();
        Assert.assertNotNull(mainBodyList);
        List<MainBodyGroup> mainBodyGroups = mainBodyList.getMainBodyGroup();
        Assert.assertTrue(mainBodyList.getMainBodyGroup().size() > 0);
        String updatedShortName = "updated-" + mainBodyGroups.get(0).getShortName();
        mainBodyGroups.get(0).setShortName(updatedShortName);
        
        return result;
	}

	@Override
	protected void compareUpdatedItemInstances(OrganizationsCommon original,
			OrganizationsCommon updated) throws Exception {
		MainBodyGroupList mainBodyList = original.getMainBodyGroupList();
        Assert.assertNotNull(mainBodyList);
        Assert.assertTrue(mainBodyList.getMainBodyGroup().size() > 0);
        Assert.assertEquals(updated.getMainBodyGroupList().getMainBodyGroup().get(0).getShortName(),
                original.getMainBodyGroupList().getMainBodyGroup().get(0).getShortName(),
                "Short name in updated Organization did not match submitted data.");
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        //String baseRefName = OrgAuthorityClientUtils.createOrgAuthRefName(shortId, null);
        PoxPayloadOut result = OrgAuthorityClientUtils.createOrgAuthorityInstance(
                displayName, shortId, commonPartName);
        return result;
	}

	@Override
	protected PoxPayloadOut createNonExistenceInstance(String commonPartName,
			String identifier) {
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        //String baseRefName = OrgAuthorityClientUtils.createOrgAuthRefName(shortId, null);
        PoxPayloadOut result = OrgAuthorityClientUtils.createOrgAuthorityInstance(
                displayName, shortId, commonPartName);
        return result;
	}
	
    protected PoxPayloadOut createNonExistenceItemInstance(String commonPartName,
    		String identifier) {
        Map<String, String> nonexOrgMap = new HashMap<String, String>();
        nonexOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, "nonExistent");
        nonexOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "Non-existent");
        PoxPayloadOut result =
                OrgAuthorityClientUtils.createOrganizationInstance(
                knownResourceRefName,
                nonexOrgMap, commonPartName);
        return result;
    }

	@Override
	protected OrgauthoritiesCommon updateInstance(OrgauthoritiesCommon orgauthoritiesCommon) {
		OrgauthoritiesCommon result = new OrgauthoritiesCommon();
		
        result.setDisplayName("updated-" + orgauthoritiesCommon.getDisplayName());
        result.setVocabType("updated-" + orgauthoritiesCommon.getVocabType());
        
		return result;
	}

	@Override
	protected void compareUpdatedInstances(OrgauthoritiesCommon original,
			OrgauthoritiesCommon updated) throws Exception {
        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
	}
}
