/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.MovementJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.movement.MovementsCommon;
import org.collectionspace.services.movement.MovementsCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MovementSortByTest, tests sorting of summary lists by fields
 * of various datatypes.
 *
 * $LastChangedRevision: 2562 $
 * $LastChangedDate: 2010-06-22 23:26:51 -0700 (Tue, 22 Jun 2010) $
 */
public class MovementSortByTest extends BaseServiceTest {

    private final String CLASS_NAME = MovementSortByTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final String DELIMITER_SCHEMA_AND_FIELD = ":";
    final String KEYWORD_DESCENDING_SEARCH = "DESC";
    final String SERVICE_PATH_COMPONENT = "movements";
    final String TEST_SPECIFIC_KEYWORD = "msotebstpfscn";
    private List<String> movementIdsCreated = new ArrayList<String>();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getAbstractCommonList(
            ClientResponse<AbstractCommonList> response) {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // Sort tests
    // ---------------------------------------------------------------

    // Success outcomes

    /*
     * Tests whether a list of records, sorted by a String field in
     * ascending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList"})
    public void sortByStringFieldAscending(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        String sortFieldName = qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        MovementsCommonList list = readSortedList(sortFieldName);
        List<MovementsCommonList.MovementListItem> items =
                list.getMovementListItem();

        String[] values = new String[100];
        Collator usEnglishCollator = Collator.getInstance(Locale.US);
        int i = 0;
        for (MovementsCommonList.MovementListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(item.getCsid());
            values[i] = movement.getMovementNote();
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + values[i]);
            }
            // Verify that the value of the specified field in the current record
            // is equal to or greater than its value in the previous record,
            // using a locale-specific collator.
            //
            // (Note: when used with certain text, this test case could potentially
            // reflect inconsistencies, if any, between Java's collator and the
            // collator used for ordering by the database.  To help avoid this,
            // it might be useful to keep test strings fairly generic.)
            if (i > 0) {
                Assert.assertTrue(usEnglishCollator.compare(values[i], values[i - 1]) >= 0);
            }
            i++;
        }

    }

    /*
     * Tests whether a list of records, obtained by a keyword search, and
     * sorted by a String field in ascending order, is returned in the expected order.
     *
     * This verifies that summary list results from keyword searches, in
     * addition to 'read list' requests, can be returned in sorted order.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList"})
    public void sortKeywordSearchResultsByStringFieldAscending(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        String sortFieldName = qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        MovementsCommonList list = keywordSearchSortedBy(TEST_SPECIFIC_KEYWORD, sortFieldName);
        List<MovementsCommonList.MovementListItem> items =
                list.getMovementListItem();

        String[] values = new String[100];
        Collator usEnglishCollator = Collator.getInstance(Locale.US);
        int i = 0;
        for (MovementsCommonList.MovementListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(item.getCsid());
            values[i] = movement.getMovementNote();
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + values[i]);
            }
            // Verify that the value of the specified field in the current record
            // is equal to or greater than its value in the previous record,
            // using a locale-specific collator.
            //
            // (Note: when used with certain text, this test case could potentially
            // reflect inconsistencies, if any, between Java's collator and the
            // collator used for ordering by the database.  To help avoid this,
            // it might be useful to keep test strings fairly generic.)
            if (i > 0) {
                Assert.assertTrue(usEnglishCollator.compare(values[i], values[i - 1]) >= 0);
            }
            i++;
        }

    }

    /*
     * Tests whether a list of records, sorted by a String field in
     * descending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList"})
    public void sortByStringFieldDescending(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        String sortFieldName =
                asDescendingSort(qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE));
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        MovementsCommonList list = readSortedList(sortFieldName);
        List<MovementsCommonList.MovementListItem> items =
                list.getMovementListItem();

        String[] values = new String[100];
        Collator usEnglishCollator = Collator.getInstance(Locale.US);
        int i = 0;
        for (MovementsCommonList.MovementListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(item.getCsid());
            values[i] = movement.getMovementNote();
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + values[i]);
            }
            // Verify that the value of the specified field in the current record
            // is less than or equal to than its value in the previous record,
            // using a locale-specific collator.
            //
            // (Note: when used with certain text, this test case could potentially
            // reflect inconsistencies, if any, between Java's collator and the
            // collator used for ordering by the database.  To help avoid this,
            // it might be useful to keep test strings fairly generic.)
            if (i > 0) {
                Assert.assertTrue(usEnglishCollator.compare(values[i], values[i - 1]) <= 0);
            }
            i++;
        }

    }

    /*
     * Tests whether a list of records, sorted by a dateTime field in
     * ascending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList"})
    public void sortByDateTimeFieldAscending(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        String sortFieldName = qualifySortFieldName(MovementJAXBSchema.LOCATION_DATE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        MovementsCommonList list = readSortedList(sortFieldName);
        List<MovementsCommonList.MovementListItem> items =
                list.getMovementListItem();

        String[] values = new String[100];
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        int i = 0;
        for (MovementsCommonList.MovementListItem item : items) {
            values[i] = item.getLocationDate();
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] locationDate=" + values[i]);
            }
            // Verify that the value of the specified field in the current record
            // is equal to or greater than its value in the previous record.
            if (i > 0) {
                Assert.assertTrue(comparator.compare(values[i], values[i - 1]) >= 0);
            }
            i++;
        }
    }

    /*
     * Tests whether a list of records, sorted by a dateTime field in
     * descending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList"})
    public void sortByDateTimeFieldDescending(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        String sortFieldName =
                asDescendingSort(qualifySortFieldName(MovementJAXBSchema.LOCATION_DATE));
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        MovementsCommonList list = readSortedList(sortFieldName);
        List<MovementsCommonList.MovementListItem> items =
                list.getMovementListItem();

        String[] values = new String[100];
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        int i = 0;
        for (MovementsCommonList.MovementListItem item : items) {
            values[i] = item.getLocationDate();
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] locationDate=" + values[i]);
            }
            // Verify that the value of the specified field in the current record
            // is less than or equal to its value in the previous record.
            if (i > 0) {
                Assert.assertTrue(comparator.compare(values[i], values[i - 1]) <= 1);
            }
            i++;
        }
    }

    /*
     * Tests whether a request to sort by an empty field name is handled
     * as expected: the query parameter is simply ignored, and a list
     * of records is returned, unsorted, with a success result.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void sortWithEmptySortFieldName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        final String EMPTY_SORT_FIELD_NAME = "";
        ClientResponse<MovementsCommonList> res =
                client.readListSortedBy(EMPTY_SORT_FIELD_NAME);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }

    // Failure outcomes

    /*
     * Tests whether a request to sort by an unqualified field name is
     * handled as expected.  The field name provided in this test is valid,
     * but has not been qualified by being prefixed by a schema name and delimiter.
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void sortWithUnqualifiedFieldName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // FIXME: Ultimately, this should return a BAD_REQUEST status.
        testSetup(STATUS_INTERNAL_SERVER_ERROR, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<MovementsCommonList> res =
                client.readListSortedBy(MovementJAXBSchema.LOCATION_DATE);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }

    /*
     * Tests whether a request to sort by a malformed field name is
     * handled as expected.
     */
/*
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void sortWithMalformedFieldName(String testName) throws Exception {

    // FIXME: Implement this stub method.

    // FIXME: Consider splitting this test into various tests, with
    // different malformed field name formats that might confuse parsers
    // and/or validation code.

    // FIXME: Consider fixing DocumentFilter.setSortOrder() to return
    // an error response to this test case, then revise this test case
    // to expect that response.

    }
*/

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
        // Delete all Movement resource(s) created during this test.
        MovementClient movementClient = new MovementClient();
        for (String resourceId : movementIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            movementClient.delete(resourceId);
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private String getCommonSchemaName() {
        // FIXME: While this convention - appending a suffix to the name of
        // the service's first unique URL path component - works, it would
        // be preferable to get the common schema name from configuration.
        return getServicePathComponent() + "_" + "common";
    }

    public String qualifySortFieldName(String fieldName) {
        return getCommonSchemaName() + DELIMITER_SCHEMA_AND_FIELD + fieldName;
    }

    public String asDescendingSort(String qualifiedFieldName) {
        return qualifiedFieldName + " " + KEYWORD_DESCENDING_SEARCH;
    }

    /*
     * A data provider that provides a set of unsorted values, which are
     * to be used in populating (seeding) values in test records.
     *
     * Data elements provided for each test record consist of:
     * * An integer, reflecting expected sort order.
     * * US English text, to populate the value of a free text (String) field.
     * * An ISO 8601 timestamp, to populate the value of a calendar date (dateTime) field.
     */
    @DataProvider(name = "unsortedValues")
    public Object[][] unsortedValues() {
        // Add a test record-specific string so we have the option of
        // constraining tests to only test records, in list or search results.
        final String TEST_RECORD_SPECIFIC_STRING = CLASS_NAME + " " + TEST_SPECIFIC_KEYWORD;
        return new Object[][]{
                    {1, "Aardvark and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2009-01-29T00:00:05Z"},
                    {4, "Bat fling off wall. " + TEST_RECORD_SPECIFIC_STRING, "2010-08-30T00:00:00Z"},
                    {2, "Aardvarks and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2009-01-29T08:00:00Z"},
                    {5, "Zounds! " + TEST_RECORD_SPECIFIC_STRING, "2010-08-31T00:00:00Z"},
                    {3, "Bat flies off ball. " + TEST_RECORD_SPECIFIC_STRING, "2009-05-29T00:00:00Z"}
                };
    }

    @Test(dataProvider = "unsortedValues")
    public void createList(int expectedSortOrder, String movementNote,
            String locationDate) throws Exception {

        String testName = "createList";
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Create each unsorted record provided by the data provider.
        create(movementNote, locationDate);
    }

    private void create(String movementNote, String locationDate) throws Exception {

        String testName = "create";
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        MultipartOutput multipart = createMovementInstance(createIdentifier(),
                movementNote, locationDate);
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        movementIdsCreated.add(extractId(res));
    }

    private MovementsCommon read(String csid) throws Exception {

        String testName = "read";
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<MultipartInput> res = client.read(csid);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Extract and return the common part of the record.
        MultipartInput input = (MultipartInput) res.getEntity();
        MovementsCommon movement = (MovementsCommon) extractPart(input,
                client.getCommonPartName(), MovementsCommon.class);

        return movement;
    }

    private MultipartOutput createMovementInstance(
            String movementReferenceNumber,
            String movementNote,
            String locationDate) {
        MovementsCommon movement = new MovementsCommon();
        movement.setMovementReferenceNumber(movementReferenceNumber);
        movement.setMovementNote(movementNote);
        movement.setLocationDate(locationDate);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
                multipart.addPart(movement, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", new MovementClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, movement common");
            logger.debug(objectAsXmlString(movement, MovementsCommon.class));
        }

        return multipart;
    }

    private MovementsCommonList readSortedList(String sortFieldName) throws Exception {

        String testName = "readSortedList";
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();

        ClientResponse<MovementsCommonList> res =
                client.readListSortedBy(sortFieldName);
        MovementsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        return list;

    }

    private MovementsCommonList keywordSearchSortedBy(String keywords,
            String sortFieldName) throws Exception {

        String testName = "keywordSearchSortedBy";
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();

        ClientResponse<MovementsCommonList> res =
                client.keywordSearchSortedBy(keywords, sortFieldName);
        MovementsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        return list;

    }

}
