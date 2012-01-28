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
import javax.ws.rs.core.Response;

import org.collectionspace.services.MovementJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.movement.MovementsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

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
public class MovementSortByTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = MovementSortByTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_NAME = "movements";

    // Instance variables specific to this test.
    private final String DELIMITER_SCHEMA_AND_FIELD = ":";
    private final String KEYWORD_DESCENDING_SEARCH = "DESC";
    private final String SERVICE_PATH_COMPONENT = "movements";
    private final String TEST_SPECIFIC_KEYWORD = "msotebstpfscn";
    private List<String> movementIdsCreated = new ArrayList<String>();
    private final String SORT_FIELD_SEPARATOR = ", ";
    private final Locale LOCALE = Locale.US;
    private final String LOCATION_DATE_EL_NAME = "locationDate";

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
    protected AbstractCommonList getCommonList(
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
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortByStringFieldAscending(String testName) throws Exception {
        String sortFieldName = qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        AbstractCommonList list = readSortedList(sortFieldName);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> values = new ArrayList<String>();
        Collator localeSpecificCollator = Collator.getInstance(LOCALE);
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(AbstractCommonListUtils.ListItemGetCSID(item));
            values.add(i, movement.getMovementNote());
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + values.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is equal to or greater than its value in the previous record,
            // using a locale-specific collator.
            //
            // (Note: when used with certain text, this test case could potentially
            // reflect inconsistencies, if any, between Java's collator and the
            // collator used for ordering by the database.  To help avoid this,
            // it might be useful to keep test strings fairly generic.)
            if (i > 0 && values.get(i) != null && values.get(i - 1) != null) {
                Assert.assertTrue(localeSpecificCollator.compare(values.get(i), values.get(i - 1)) >= 0);
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
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortKeywordSearchResultsByStringFieldAscending(String testName) throws Exception {
        String sortFieldName = qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        AbstractCommonList list = keywordSearchSortedBy(TEST_SPECIFIC_KEYWORD, sortFieldName);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> values = new ArrayList<String>();
        Collator localeSpecificCollator = Collator.getInstance(LOCALE);
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(AbstractCommonListUtils.ListItemGetCSID(item));
            values.add(i, movement.getMovementNote());
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + values.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is equal to or greater than its value in the previous record,
            // using a locale-specific collator.
            //
            // (Note: when used with certain text, this test case could potentially
            // reflect inconsistencies, if any, between Java's collator and the
            // collator used for ordering by the database.  To help avoid this,
            // it might be useful to keep test strings fairly generic.)
            if (i > 0 && values.get(i) != null && values.get(i - 1) != null) {
                Assert.assertTrue(localeSpecificCollator.compare(values.get(i), values.get(i - 1)) >= 0);
            }
            i++;
        }

    }

    /*
     * Tests whether a list of records, sorted by a String field in
     * descending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortByStringFieldDescending(String testName) throws Exception {
        String sortFieldName =
                asDescendingSort(qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE));
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        AbstractCommonList list = readSortedList(sortFieldName);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> values = new ArrayList<String>();
        Collator localeSpecificCollator = Collator.getInstance(LOCALE);
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(AbstractCommonListUtils.ListItemGetCSID(item));
            values.add(i, movement.getMovementNote());
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + values.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is less than or equal to than its value in the previous record,
            // using a locale-specific collator.
            //
            // (Note: when used with certain text, this test case could potentially
            // reflect inconsistencies, if any, between Java's collator and the
            // collator used for ordering by the database.  To help avoid this,
            // it might be useful to keep test strings fairly generic.)
            if (i > 0 && values.get(i) != null && values.get(i - 1) != null) {
                Assert.assertTrue(localeSpecificCollator.compare(values.get(i), values.get(i - 1)) <= 0);
            }
            i++;
        }

    }

    /*
     * Tests whether a list of records, sorted by a dateTime field in
     * ascending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortByDateTimeFieldAscending(String testName) throws Exception {
        String sortFieldName = qualifySortFieldName(MovementJAXBSchema.LOCATION_DATE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        AbstractCommonList list = readSortedList(sortFieldName);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> values = new ArrayList<String>();
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
        	String locDate = 
        		AbstractCommonListUtils.ListItemGetElementValue(item, LOCATION_DATE_EL_NAME);
        	values.add(i, locDate);
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] locationDate=" + values.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is equal to or greater than its value in the previous record.
            if (i > 0 && values.get(i) != null && values.get(i - 1) != null) {
                Assert.assertTrue(comparator.compare(values.get(i), values.get(i - 1)) >= 0);
            }
            i++;
        }
    }

    /*
     * Tests whether a list of records, sorted by a dateTime field in
     * descending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortByDateTimeFieldDescending(String testName) throws Exception {
        String sortFieldName =
                asDescendingSort(qualifySortFieldName(MovementJAXBSchema.LOCATION_DATE));
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field name=" + sortFieldName);
        }
        AbstractCommonList list = readSortedList(sortFieldName);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> values = new ArrayList<String>();
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
        	String locDate = 
        		AbstractCommonListUtils.ListItemGetElementValue(item, LOCATION_DATE_EL_NAME);
        	values.add(i, locDate);
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] locationDate=" + values.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is less than or equal to its value in the previous record.
            if (i > 0 && values.get(i) != null && values.get(i - 1) != null) {
                Assert.assertTrue(comparator.compare(values.get(i), values.get(i - 1)) <= 0);
            }
            i++;
        }
    }

    /*
     * Tests whether a list of records, sorted by two different fields in
     * ascending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortByTwoFieldsAscending(String testName) throws Exception {
        String firstSortFieldName = qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE);
        String secondSortFieldName = qualifySortFieldName(MovementJAXBSchema.LOCATION_DATE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field names=" + firstSortFieldName + " and " + secondSortFieldName);
        }
        String sortExpression = firstSortFieldName + SORT_FIELD_SEPARATOR + secondSortFieldName;
        AbstractCommonList list = readSortedList(sortExpression);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> firstFieldValues = new ArrayList<String>();
        ArrayList<String> secondFieldValues = new ArrayList<String>();
        Collator localeSpecificCollator = Collator.getInstance(LOCALE);
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(AbstractCommonListUtils.ListItemGetCSID(item));
            firstFieldValues.add(i, movement.getMovementNote());
            secondFieldValues.add(i, movement.getLocationDate());
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] movementNote=" + firstFieldValues.get(i));
                logger.debug("list-item[" + i + "] locationDate=" + secondFieldValues.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is less than or greater than its value in the previous record.
            if (i > 0 && firstFieldValues.get(i) != null && firstFieldValues.get(i - 1) != null) {
                Assert.assertTrue(localeSpecificCollator.compare(firstFieldValues.get(i), firstFieldValues.get(i - 1)) >= 0);
                // If the value of the first sort field in the current record is identical to
                // its value in the previous record, verify that the value of the second sort
                // field is equal to or greater than its value in the previous record,
                // using a locale-specific collator.
                if (localeSpecificCollator.compare(firstFieldValues.get(i), firstFieldValues.get(i - 1)) == 0) {
                    if (i > 0 && secondFieldValues.get(i) != null && secondFieldValues.get(i - 1) != null) {
                        Assert.assertTrue(comparator.compare(secondFieldValues.get(i), secondFieldValues.get(i - 1)) >= 0);
                    }
                }
            }
            i++;
        }
    }

    /*
     * Tests whether a list of records, sorted by one different fields in
     * descending order and a second field in ascending order, is returned in the expected order.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createList"})
    public void sortByOneFieldAscendingOneFieldsDescending(String testName) throws Exception {
        String firstSortFieldName =
                asDescendingSort(qualifySortFieldName(MovementJAXBSchema.LOCATION_DATE));
        String secondSortFieldName = qualifySortFieldName(MovementJAXBSchema.MOVEMENT_NOTE);
        if (logger.isDebugEnabled()) {
            logger.debug("Sorting on field names=" + firstSortFieldName + " and " + secondSortFieldName);
        }
        String sortExpression = firstSortFieldName + SORT_FIELD_SEPARATOR + secondSortFieldName;
        AbstractCommonList list = readSortedList(sortExpression);
        List<AbstractCommonList.ListItem> items =
                list.getListItem();

        ArrayList<String> firstFieldValues = new ArrayList<String>();
        ArrayList<String> secondFieldValues = new ArrayList<String>();
        Collator localeSpecificCollator = Collator.getInstance(LOCALE);
        Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
        int i = 0;
        for (AbstractCommonList.ListItem item : items) {
            // Because movementNote is not currently a summary field
            // (returned in summary list items), we will need to verify
            // sort order by retrieving full records, using the
            // IDs provided in the summary list items. amd then retriving
            // the value of that field from each of those records.
            MovementsCommon movement = read(AbstractCommonListUtils.ListItemGetCSID(item));
            firstFieldValues.add(i, movement.getLocationDate());
            secondFieldValues.add(i, movement.getMovementNote());
            if (logger.isDebugEnabled()) {
                logger.debug("list-item[" + i + "] locationDate=" + firstFieldValues.get(i));
                logger.debug("list-item[" + i + "] movementNote=" + secondFieldValues.get(i));
            }
            // Verify that the value of the specified field in the current record
            // is less than or equal to than its value in the previous record.
            if (i > 0 && firstFieldValues.get(i) != null && firstFieldValues.get(i - 1) != null) {
                Assert.assertTrue(comparator.compare(firstFieldValues.get(i), firstFieldValues.get(i - 1)) <= 0);
                // If the value of the first sort field in the current record is identical to
                // its value in the previous record, verify that the value of the second sort
                // field is equal to or greater than its value in the previous record,
                // using a locale-specific collator.
                if (comparator.compare(firstFieldValues.get(i), firstFieldValues.get(i - 1)) == 0) {
                    if (i > 0 && secondFieldValues.get(i) != null && secondFieldValues.get(i - 1) != null) {
                        Assert.assertTrue(localeSpecificCollator.compare(secondFieldValues.get(i), secondFieldValues.get(i - 1)) >= 0);
                    }
                }
            }
            i++;
        }
    }


    /*
     * Tests whether a request to sort by an empty field name is handled
     * as expected: the query parameter is simply ignored, and a list
     * of records is returned, unsorted, with a success result.
     */
    @Test(dataProvider = "testName")
    public void sortWithEmptySortFieldName(String testName) throws Exception {
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        final String EMPTY_SORT_FIELD_NAME = "";
        ClientResponse<AbstractCommonList> res =
                client.readListSortedBy(EMPTY_SORT_FIELD_NAME);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    // Failure outcomes

    /*
     * Tests whether a request to sort by an unqualified field name is
     * handled as expected.  The field name provided in this test is valid,
     * but has not been qualified by being prefixed by a schema name and delimiter.
     */
    @Test(dataProvider = "testName")
    public void sortWithUnqualifiedFieldName(String testName) throws Exception {
        testSetup(STATUS_BAD_REQUEST, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<AbstractCommonList> res =
                client.readListSortedBy(MovementJAXBSchema.LOCATION_DATE);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    /*
     * Tests whether a request to sort by an invalid identifier for the
     * sort order (ascending or descending) is handled as expected.
     */
    @Test(dataProvider = "testName")
    public void sortWithInvalidSortOrderIdentifier(String testName) throws Exception {
        testSetup(STATUS_BAD_REQUEST, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        final String INVALID_SORT_ORDER_IDENTIFIER = "NO_DIRECTION";
        ClientResponse<AbstractCommonList> res =
                client.readListSortedBy(MovementJAXBSchema.LOCATION_DATE
                + " " + INVALID_SORT_ORDER_IDENTIFIER);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
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
        // Delete all Movement resource(s) created during this test.
        MovementClient movementClient = new MovementClient();
        for (String resourceId : movementIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            movementClient.delete(resourceId).releaseConnection();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private String getCommonSchemaName() {
        // FIXME: While this convention - appending a suffix to the name of
        // the service's first unique URL path component - works, it would
        // be preferable to get the common schema name from configuration.
        //
        // Such configuration is provided for example, on the services side, in
        // org.collectionspace.services.common.context.AbstractServiceContextImpl
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
        // FIXME: ADR Add a test record-specific string so we have the option of
        // constraining tests to only test records, in list or search results.
        final String TEST_RECORD_SPECIFIC_STRING = CLASS_NAME + " " + TEST_SPECIFIC_KEYWORD;
        return new Object[][]{
                    {1, "Aardvark and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2009-01-29T00:00:05Z"},
                    {10, "Zounds! " + TEST_RECORD_SPECIFIC_STRING, "2010-08-31T00:00:00Z"},
                    {3, "Aardvark and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2010-08-30T00:00:00Z"},
                    {7, "Bat fling off wall. " + TEST_RECORD_SPECIFIC_STRING, "2010-08-30T00:00:00Z"},
                    {4, "Aardvarks and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2009-01-29T08:00:00Z"},
                    {5, "Aardvarks and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2009-05-29T00:00:00Z"},
                    {2, "Aardvark and plumeria. " + TEST_RECORD_SPECIFIC_STRING, "2009-05-29T00:00:00Z"},
                    {9, "Zounds! " + TEST_RECORD_SPECIFIC_STRING, "2009-05-29T00:00:00Z"}, // Identical to next record
                    {8, "Zounds! " + TEST_RECORD_SPECIFIC_STRING, "2009-05-29T00:00:00Z"},
                    {6, "Bat flies off ball. " + TEST_RECORD_SPECIFIC_STRING, "2009-05-29T00:00:00Z"}
                };
    }

    /*
     * Create multiple test records, initially in unsorted order,
     * using values for various fields obtained from the data provider.
     */
    @Test(dataProvider = "unsortedValues")
    public void createList(int expectedSortOrder, String movementNote,
            String locationDate) throws Exception {

        String testName = "createList";
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Iterates through the sets of values returned by the data provider,
        // and creates a corresponding test record for each set of values.
        create(movementNote, locationDate);
    }

    private void create(String movementNote, String locationDate) throws Exception {
    	String result = null;
    	
        String testName = "create";
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        PoxPayloadOut multipart = createMovementInstance(createIdentifier(),
                movementNote, locationDate);
        MovementClient client = new MovementClient();
        ClientResponse<Response> res = client.create(multipart);
        try {
        	assertStatusCode(res, testName);
            // Store the IDs from every resource created by tests,
            // so they can be deleted after tests have been run.
        	result = extractId(res);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        if (result != null) {
        	movementIdsCreated.add(result);
        }
    }

    private MovementsCommon read(String csid) throws Exception {
        String testName = "read";
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<String> res = client.read(csid);
        MovementsCommon movementCommon = null;
        try {
        	assertStatusCode(res, testName);
	        // Extract and return the common part of the record.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
	        if (payloadInputPart != null) {
	        	movementCommon = (MovementsCommon) payloadInputPart.getBody();
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        return movementCommon;
    }

    private PoxPayloadOut createMovementInstance(
            String movementReferenceNumber,
            String movementNote,
            String locationDate) {
        MovementsCommon movementCommon = new MovementsCommon();
        movementCommon.setMovementReferenceNumber(movementReferenceNumber);
        movementCommon.setMovementNote(movementNote);
        movementCommon.setLocationDate(locationDate);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(new MovementClient().getCommonPartName(), movementCommon);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, movement common");
            logger.debug(objectAsXmlString(movementCommon, MovementsCommon.class));
        }

        return multipart;
    }

    private AbstractCommonList readSortedList(String sortFieldName) throws Exception {
        String testName = "readSortedList";
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();

        ClientResponse<AbstractCommonList> res = client.readListSortedBy(sortFieldName);
        AbstractCommonList list = null;
        try {
        	assertStatusCode(res, testName);
        	list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }        

        return list;

    }

    private AbstractCommonList keywordSearchSortedBy(String keywords,
            String sortFieldName) throws Exception {
    	AbstractCommonList result = null;
    	
        String testName = "keywordSearchSortedBy";
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();

        ClientResponse<AbstractCommonList> res =
                client.keywordSearchSortedBy(keywords, sortFieldName);
        AbstractCommonList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }

        return list;
    }

}
