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
import org.collectionspace.services.client.OrganizationClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.ContactGroup;
import org.collectionspace.services.organization.OrgTermGroup;
import org.collectionspace.services.organization.OrgTermGroupList;
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

    /**
     * Default constructor.  Used to set the short ID for all tests authority items
     */
    public OrgAuthorityServiceTest() {
    	super();
        TEST_SHORTID = "TestOrg";
    }

    @Override
    public String getServicePathComponent() {
        return OrganizationClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return OrganizationClient.SERVICE_NAME;
    }

    private final String TEST_ORG_NAME = "Test Org";
    private final String TEST_ORG_MAIN_BODY_NAME = "The real official test organization";
    private final String TEST_ORG_FOUNDING_PLACE = "Anytown, USA";
    private final String TEST_ORG_FOUNDING_DATE = "May 26, 1907";

    /** The known item resource short ID. */
    private String knownItemResourceShortIdentifer = null;

    /** The known contact resource id. */
    private String knownContactResourceId = null;

    /** The all contact resource ids created. */
    private Map<String, String> allContactResourceIdsCreated = new HashMap<String, String>();

    /**
     *
     */
    protected void setKnownItemResource(String id, String shortIdentifer) {
        knownItemResourceId = id;
        knownItemResourceShortIdentifer = shortIdentifer;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new OrganizationClient();
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new OrganizationClient(clientPropertiesFilename);
	}

    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        OrganizationClient client = new OrganizationClient();
        String displayName = "displayName-" + identifier;
        PoxPayloadOut multipart = OrgAuthorityClientUtils.createOrgAuthorityInstance(
                displayName, identifier, client.getCommonPartName());
        return multipart;
    }

    @Override
    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) throws Exception {
        String headerLabel = new OrganizationClient().getItemCommonPartName();

        String shortId = TEST_SHORTID + identifier;
        Map<String, String> testOrgMap = new HashMap<String, String>();
        testOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId);
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, TEST_ORG_FOUNDING_DATE);
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, TEST_ORG_FOUNDING_PLACE);

        List<OrgTermGroup> terms = new ArrayList<OrgTermGroup>();
        OrgTermGroup term = new OrgTermGroup();
        term.setTermDisplayName(TEST_ORG_NAME);
        term.setTermName(TEST_ORG_NAME);
        term.setMainBodyName(TEST_ORG_MAIN_BODY_NAME);
        terms.add(term);

        return OrgAuthorityClientUtils.createOrganizationInstance(identifier, testOrgMap, terms, headerLabel);
    }

	@Override
	protected String createItemInAuthority(AuthorityClient client, String authorityId, String shortId) {
		return createItemInAuthority(client, authorityId, shortId, null /*refname*/);
	}

    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    private String createItemInAuthority(AuthorityClient client, String vcsid, String shortId, String authRefName) {

        final String testName = "createItemInAuthority";
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":...");
        }

        Map<String, String> testOrgMap = new HashMap<String, String>();
        testOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId);
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, TEST_ORG_FOUNDING_DATE);
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, TEST_ORG_FOUNDING_PLACE);

        List<OrgTermGroup> terms = new ArrayList<OrgTermGroup>();
        OrgTermGroup term = new OrgTermGroup();
        term.setTermDisplayName(TEST_ORG_NAME);
        term.setTermName(TEST_ORG_NAME);
        term.setMainBodyName(TEST_ORG_MAIN_BODY_NAME);
        terms.add(term);

        Map<String, List<String>> testOrgRepeatablesMap = new HashMap<String, List<String>>();
        List<String> testOrgContactNames = new ArrayList<String>();
        testOrgContactNames.add("joe@example.org");
        testOrgContactNames.add("sally@example.org");
        testOrgRepeatablesMap.put(OrganizationJAXBSchema.CONTACT_NAMES, testOrgContactNames);

        String newID = OrgAuthorityClientUtils.createItemInAuthority(
                vcsid, authRefName, testOrgMap, terms, testOrgRepeatablesMap, (OrganizationClient) client);

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
     * @throws Exception
     */
    @Test(dataProvider = "testName", groups = {"create"},
    		dependsOnMethods = {"createItem"})
    public void createContact(String testName) throws Exception {
        setupCreate();
        String newID = createContactInItem(knownResourceId, knownItemResourceId);
    }

    /**
     * Creates the contact in item.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the string
     * @throws Exception
     */
    private String createContactInItem(String parentcsid, String itemcsid) throws Exception {

        final String testName = "createContactInItem";
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner(testName, CLASS_NAME));
        }
        setupCreate();

        // Submit the request to the service and store the response.
        OrganizationClient client = new OrganizationClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart =
                ContactClientUtils.createContactInstance(parentcsid,
                itemcsid, identifier, new ContactClient().getCommonPartName());

        String newID = null;
        Response res = client.createContact(parentcsid, itemcsid, multipart);
        try {
            assertStatusCode(res, testName);
            newID = OrgAuthorityClientUtils.extractId(res);
        } finally {
        	if (res != null) {
                res.close();
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
    		dependsOnMethods = {"createItemList", "deleteContact"})
    public void createContactList(String testName) throws Exception {
        createContact(testName); // As of CollectionSpace 7.1, only one contact item can be associated with an authority resource
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

    protected void readInternal(String testName, String CSID, String shortId) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        OrganizationClient client = new OrganizationClient();
        Response res = null;
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
                PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
                OrgauthoritiesCommon orgAuthority = (OrgauthoritiesCommon) extractPart(input,
                        new OrganizationClient().getCommonPartName(), OrgauthoritiesCommon.class);
                if (logger.isDebugEnabled()) {
                    logger.debug(objectAsXmlString(orgAuthority, OrgauthoritiesCommon.class));
                }
                Assert.assertNotNull(orgAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
        	if (res != null) {
                res.close();
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
        OrganizationClient client = new OrganizationClient();
        Response res = null;
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
            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
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

            List<ContactGroup> contactGroups = organization.getContactGroupList().getContactGroup();
            Assert.assertTrue(contactGroups.size() > 0);
            Assert.assertNotNull(contactGroups.get(0).getContactName());
        } finally {
        	if (res != null) {
                res.close();
            }
        }
    }

    @Override
    protected void verifyReadItemInstance(OrganizationsCommon item) throws Exception {
        List<ContactGroup> contactGroups = item.getContactGroupList().getContactGroup();
        Assert.assertTrue(contactGroups.size() > 0);
        Assert.assertNotNull(contactGroups.get(0).getContactName());
    }

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName")
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        // Perform setup for read.
        setupRead();
        //
        // First read our known resource.
        //
        OrganizationClient client = new OrganizationClient();
        Response res = client.readItem(knownResourceId, knownItemResourceId);
        OrganizationsCommon organization = null;
        try {
            assertStatusCode(res, testName);
            // Check whether organization has expected displayName.
            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
            organization = (OrganizationsCommon) extractPart(input,
                    client.getItemCommonPartName(), OrganizationsCommon.class);
            Assert.assertNotNull(organization);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        //
        // Make an invalid UPDATE request, without a display name
        //
        OrgTermGroupList termList = organization.getOrgTermGroupList();
        Assert.assertNotNull(termList);
        List<OrgTermGroup> terms = termList.getOrgTermGroup();
        Assert.assertNotNull(terms);
        Assert.assertTrue(terms.size() > 0);
        terms.get(0).setTermDisplayName(null);
        terms.get(0).setTermName(null);

        setupUpdateWithInvalidBody(); // we expect a failure
        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(OrganizationClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(),
        		organization);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.close();
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
        OrganizationClient client = new OrganizationClient();
        Response res =client.readContact(knownResourceId, knownItemResourceId,
                knownContactResourceId);
        try {
            assertStatusCode(res, testName);
            // Check whether we've received a contact.
            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
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
                res.close();
            }
        }
    }

    /**
     * Read contact non existent.
     *
     * @param testName the test name
     * @throws Exception
     */
    @Test(dataProvider = "testName", groups = {"readItem"},
    		dependsOnMethods = {"readContact"})
    public void readContactNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        OrganizationClient client = new OrganizationClient();
        Response res = client.readContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
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
                res.close();
            }
        }
    }

    /**
     * Read item list.
     * @throws Exception
     */
    @Override
//	@Test(groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList(String testName) throws Exception {
        readItemList(knownAuthorityWithItems, null);
    }

    /**
     * Read item list by authority name.
     * @throws Exception
     */
    @Override
//    @Test(dataProvider = "testName",
//    		dependsOnMethods = {"readItem"})
    public void readItemListByName(String testName) throws Exception {
        readItemList(null, READITEMS_SHORT_IDENTIFIER);
    }

    /**
     * Read item list.
     *
     * @param vcsid the vcsid
     * @param name the name
     * @throws Exception
     */
    private void readItemList(String vcsid, String name) throws Exception {

        final String testName = "readItemList";
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        OrganizationClient client = new OrganizationClient();
        Response res = null;
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
            list = res.readEntity(AbstractCommonList.class);
        } finally {
        	if (res != null) {
                res.close();
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
                    AbstractCommonListUtils.ListItemGetElementValue(item, OrganizationJAXBSchema.REF_NAME);
            Assert.assertTrue((null != value), "Item refName is null!");
            value =
                    AbstractCommonListUtils.ListItemGetElementValue(item, OrganizationJAXBSchema.TERM_DISPLAY_NAME);
            Assert.assertTrue((null != value), "Item termDisplayName is null!");
        }
        if (logger.isTraceEnabled()) {
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    /**
     * Read contact list.
     * @throws Exception
     */
    @Test(groups = {"readList"},
    		dependsOnMethods = {"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.readItemList"})
    public void readContactList() throws Exception {
        readContactList(knownResourceId, knownItemResourceId);
    }

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @throws Exception
     */
    private void readContactList(String parentcsid, String itemcsid) throws Exception {
        final String testName = "readContactList";
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        OrganizationClient client = new OrganizationClient();
        Response res = client.readContactList(parentcsid, itemcsid);
        AbstractCommonList list = null;
        try {
            assertStatusCode(res, testName);
            list = res.readEntity(AbstractCommonList.class);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        List<AbstractCommonList.ListItem> listitems =
            list.getListItem();
        int nItemsReturned = listitems.size();
        int nExpectedItems = 1; // As of CollectionSpace 7.1, only one contact item can be associated with an authority resource
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
        OrganizationClient client = new OrganizationClient();
        Response res = client.readContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        ContactsCommon contact = null;
        try {
            assertStatusCode(res, testName);
            if (logger.isDebugEnabled()) {
                logger.debug("got Contact to update with ID: "
                        + knownContactResourceId
                        + " in item: " + knownItemResourceId
                        + " in parent: " + knownResourceId);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
            contact = (ContactsCommon) extractPart(input,
                    new ContactClient().getCommonPartName(), ContactsCommon.class);
            Assert.assertNotNull(contact);
        } finally {
        	if (res != null) {
                res.close();
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
	        PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
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
                res.close();
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
        OrganizationClient client = new OrganizationClient();
        Response res = client.deleteContact(knownResourceId, knownItemResourceId, knownContactResourceId);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.close();
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
     * @throws Exception
     */
    @Test(dataProvider = "testName", groups = {"delete"},
    		dependsOnMethods = {"deleteContact"})
    public void deleteNonExistentContact(String testName) throws Exception {
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        OrganizationClient client = new OrganizationClient();
        Response res = client.deleteContact(knownResourceId, knownItemResourceId, NON_EXISTENT_ID);
        try {
            assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.close();
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
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    @Override
    public void cleanUp() throws Exception {
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
        OrganizationClient client = new OrganizationClient();
        for (Map.Entry<String, String> entry : allContactResourceIdsCreated.entrySet()) {
            contactResourceId = entry.getKey();
            itemResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            client.deleteContact(parentResourceId, itemResourceId, contactResourceId).close();
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
	protected OrganizationsCommon updateItemInstance(OrganizationsCommon organizationsCommon) {

            OrgTermGroupList termList = organizationsCommon.getOrgTermGroupList();
            Assert.assertNotNull(termList);
            List<OrgTermGroup> terms = termList.getOrgTermGroup();
            Assert.assertNotNull(terms);
            Assert.assertTrue(terms.size() > 0);
            terms.get(0).setTermDisplayName("updated-" + terms.get(0).getTermDisplayName());
            terms.get(0).setTermName("updated-" + terms.get(0).getTermName());
	    organizationsCommon.setOrgTermGroupList(termList);

            return organizationsCommon;
	}

	@Override
	protected void compareUpdatedItemInstances(OrganizationsCommon original,
			OrganizationsCommon updated,
			boolean compareRevNumbers) throws Exception {

            OrgTermGroupList originalTermList = original.getOrgTermGroupList();
            Assert.assertNotNull(originalTermList);
            List<OrgTermGroup> originalTerms = originalTermList.getOrgTermGroup();
            Assert.assertNotNull(originalTerms);
            Assert.assertTrue(originalTerms.size() > 0);

            OrgTermGroupList updatedTermList = updated.getOrgTermGroupList();
            Assert.assertNotNull(updatedTermList);
            List<OrgTermGroup> updatedTerms = updatedTermList.getOrgTermGroup();
            Assert.assertNotNull(updatedTerms);
            Assert.assertTrue(updatedTerms.size() > 0);

            Assert.assertEquals(updatedTerms.get(0).getTermDisplayName(),
                originalTerms.get(0).getTermDisplayName(),
                "Value in updated record did not match submitted data.");

            if (compareRevNumbers == true) {
            	Assert.assertEquals(original.getRev(), updated.getRev(), "Revision numbers should match.");
            }
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
        PoxPayloadOut result =
                OrgAuthorityClientUtils.createOrganizationInstance(
                knownResourceRefName,
                nonexOrgMap, OrgAuthorityClientUtils.getTermGroupInstance(TEST_ORG_NAME), commonPartName);
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
