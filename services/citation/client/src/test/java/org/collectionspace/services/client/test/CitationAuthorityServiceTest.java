/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.client.test;

import java.util.List;
import java.util.Map;
import org.collectionspace.services.CitationJAXBSchema;
import org.collectionspace.services.citation.CitationTermGroup;
import org.collectionspace.services.citation.CitationTermGroupList;
import org.collectionspace.services.citation.CitationauthoritiesCommon;
import org.collectionspace.services.citation.CitationsCommon;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.CitationAuthorityClient;
import org.collectionspace.services.client.CitationAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * ConceptAuthorityServiceTest, carries out tests against a deployed and running
 * ConceptAuthority Service.
 *
 * $LastChangedRevision: 753 $ $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed,
 * 23 Sep 2009) $
 */
public class CitationAuthorityServiceTest extends AbstractAuthorityServiceTest<CitationauthoritiesCommon, CitationsCommon> {

    /**
     * The logger.
     */
    private final String CLASS_NAME = CitationAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CitationAuthorityServiceTest.class);
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();
    // Instance variables specific to this test.
    final String TEST_NAME = "Citation 1";
    final String TEST_SHORTID = "citation1";
    final String TEST_SCOPE_NOTE = "A representative citation";
    // TODO Make status type be a controlled vocab term.
    final String TEST_STATUS = "Approved";

    @Override
    public String getServicePathComponent() {
        return CitationAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return CitationAuthorityClient.SERVICE_NAME;
    }

    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new CitationAuthorityClient();
    }

    /**
     * Creates an item in an authority.
     *
     * @param authorityId an identifier for the authority item
     * @return the string
     */
    @Override
    protected String createItemInAuthority(String authorityId) {

        final String testName = "createItemInAuthority(" + authorityId + ")";
        if (logger.isDebugEnabled()) {
            logger.debug(testName);
        }

        // Submit the request to the service and store the response.
        CitationAuthorityClient client = new CitationAuthorityClient();

        String commonPartXML = createCommonPartXMLForItem(TEST_SHORTID, TEST_NAME);

        String newID;
        try {
            newID = CitationAuthorityClientUtils.createItemInAuthority(authorityId,
                    commonPartXML, client);
        } catch (Exception e) {
            logger.error("Problem creating item from XML: " + e.getLocalizedMessage());
            logger.debug("commonPartXML: " + commonPartXML);
            return null;
        }

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            setKnownItemResource(newID, TEST_SHORTID);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + newID);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allResourceItemIdsCreated.put(newID, authorityId);

        return newID;
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
        CitationAuthorityClient client = new CitationAuthorityClient();
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
            res.releaseConnection();
        }
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
                    AbstractCommonListUtils.ListItemGetElementValue(item, CitationJAXBSchema.REF_NAME);
            Assert.assertTrue((null != value), "Item refName is null!");
            value =
                    AbstractCommonListUtils.ListItemGetElementValue(item, CitationJAXBSchema.TERM_DISPLAY_NAME);
            Assert.assertTrue((null != value), "Item termDisplayName is null!");
        }
        if (logger.isTraceEnabled()) {
            AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
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
            dependsOnMethods = {"readItem", "updateItem"})
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
     * For this reason, it attempts to remove all resources created at any point
     * during testing, even if some of those resources may be expected to be
     * deleted by certain tests.
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
        CitationAuthorityClient client = new CitationAuthorityClient();
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
     * This URL consists of a base URL for all services, followed by a path
     * component for the owning parent, followed by the path component for the
     * items.
     *
     * @param parentResourceIdentifier An identifier (such as a UUID) for the
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
     * @param parentResourceIdentifier An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param itemResourceIdentifier An identifier (such as a UUID) for an item
     * resource.
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
    // Concept specific overrides
    //
    @Override
    protected PoxPayloadOut createInstance(String commonPartName,
            String identifier) {
        CitationAuthorityClient client = new CitationAuthorityClient();
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        PoxPayloadOut multipart =
                CitationAuthorityClientUtils.createCitationAuthorityInstance(
                displayName, shortId, commonPartName);
        return multipart;
    }

    private String createCommonPartXMLForItem(String shortId, String name) {

        StringBuilder commonPartXML = new StringBuilder("");
        commonPartXML.append("<ns2:citations_common xmlns:ns2=\"http://collectionspace.org/services/citation\">");
        commonPartXML.append("    <shortIdentifier>" + shortId + "</shortIdentifier>");
        commonPartXML.append("    <citationTermGroupList>");
        commonPartXML.append("        <citationTermGroup>");
        commonPartXML.append("            <termDisplayName>" + name + "</termDisplayName>");
        commonPartXML.append("            <termName>" + name + "</termName>");
        commonPartXML.append("            <termStatus>" + name + "</termStatus>");
        commonPartXML.append("        </citationTermGroup>");
        commonPartXML.append("    </citationTermGroupList>");
        commonPartXML.append("</ns2:citations_common>");
        return commonPartXML.toString();
    }

    @Override
    protected PoxPayloadOut createNonExistenceInstance(String commonPartName, String identifier) {
        String displayName = "displayName-NON_EXISTENT_ID";
        PoxPayloadOut result = CitationAuthorityClientUtils.createCitationAuthorityInstance(
                displayName, "nonEx", commonPartName);
        return result;
    }

    @Override
    protected CitationauthoritiesCommon updateInstance(CitationauthoritiesCommon citationauthoritiesCommon) {
        CitationauthoritiesCommon result = new CitationauthoritiesCommon();

        result.setDisplayName("updated-" + citationauthoritiesCommon.getDisplayName());
        result.setVocabType("updated-" + citationauthoritiesCommon.getVocabType());

        return result;
    }

    @Override
    protected void compareUpdatedInstances(CitationauthoritiesCommon original,
            CitationauthoritiesCommon updated) throws Exception {
        Assert.assertEquals(updated.getDisplayName(),
                original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
    }

    @Override
    protected void compareReadInstances(CitationauthoritiesCommon original,
            CitationauthoritiesCommon fromRead) throws Exception {
        Assert.assertNotNull(fromRead.getDisplayName());
        Assert.assertNotNull(fromRead.getShortIdentifier());
        Assert.assertNotNull(fromRead.getRefName());
    }

    @Override
    protected CitationsCommon updateItemInstance(CitationsCommon citationsCommon) {
        CitationsCommon result = citationsCommon;
        CitationTermGroupList termList = citationsCommon.getCitationTermGroupList();
        Assert.assertNotNull(termList);
        List<CitationTermGroup> terms = termList.getCitationTermGroup();
        Assert.assertNotNull(terms);
        Assert.assertTrue(terms.size() > 0);
        terms.get(0).setTermDisplayName("updated-" + terms.get(0).getTermDisplayName());
        terms.get(0).setTermName("updated-" + terms.get(0).getTermName());
        terms.get(0).setTermStatus("updated-" + terms.get(0).getTermStatus());
        result.setCitationTermGroupList(termList);
        return result;
    }

    @Override
    protected void compareUpdatedItemInstances(CitationsCommon original,
            CitationsCommon updated) throws Exception {
        CitationTermGroupList originalTermList = original.getCitationTermGroupList();
        Assert.assertNotNull(originalTermList);
        List<CitationTermGroup> originalTerms = originalTermList.getCitationTermGroup();
        Assert.assertNotNull(originalTerms);
        Assert.assertTrue(originalTerms.size() > 0);

        CitationTermGroupList updatedTermList = updated.getCitationTermGroupList();
        Assert.assertNotNull(updatedTermList);
        List<CitationTermGroup> updatedTerms = updatedTermList.getCitationTermGroup();
        Assert.assertNotNull(updatedTerms);
        Assert.assertTrue(updatedTerms.size() > 0);

        Assert.assertEquals(updatedTerms.get(0).getTermDisplayName(),
                originalTerms.get(0).getTermDisplayName(),
                "Value in updated record did not match submitted data.");
        Assert.assertEquals(updatedTerms.get(0).getTermStatus(),
                originalTerms.get(0).getTermDisplayName(),
                "Value in updated record did not match submitted data.");
    }

    @Override
    protected void verifyReadItemInstance(CitationsCommon item)
            throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    protected PoxPayloadOut createNonExistenceItemInstance(
            String commonPartName, String identifier) {

        String commonPartXML = createCommonPartXMLForItem("nonExShortId", "nonExItem");

        try {
            PoxPayloadOut result =
                    CitationAuthorityClientUtils.createCitationInstance(
                    commonPartXML, commonPartName);
            return result;
        } catch (DocumentException de) {
            logger.error("Problem creating item from XML: " + de.getLocalizedMessage());
            logger.debug("commonPartXML: " + commonPartXML);
        }
        return null;
    }
}
