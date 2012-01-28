/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.PerformanceTests.test;

import java.util.List;
import java.util.Date;
import java.util.Random;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.HttpResponseCodes;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * A ServiceTest.
 * 
 * @version $Revision:$
 */
public class PerformanceTest extends CollectionSpacePerformanceTest {

    /** The Constant MAX_KEYWORDS. */
    private static final int MAX_KEYWORDS = 10;
    /** The Constant MAX_SEARCHES. */
    private static final int MAX_SEARCHES = 10;
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);
    //
    // Get clients for the CollectionSpace services
    //
    /** The MA x_ records. */
    private static int MAX_RECORDS = 100;

    /**
     * Performance test.
     */
    @Test
    public void performanceTest() {
        roundTripOverhead(10);
        deleteCollectionObjects();
        String[] coList = this.createCollectionObjects(MAX_RECORDS);
        this.searchCollectionObjects(MAX_RECORDS);
        this.readCollectionObjects(coList);
        //this.deleteCollectionObjects(coList);
        roundTripOverhead(10);
    }

    /**
     * Round trip overhead.
     *
     * @param numOfCalls the num of calls
     * @return the long
     */
    private long roundTripOverhead(int numOfCalls) {
        long result = 0;
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

        long totalTime = 0;
        ClientResponse<Response> response;
        for (int i = 0; i < numOfCalls; i++) {
            Date startTime = new Date();
            response = collectionObjectClient.roundtrip(0);
            try {
                Assert.assertEquals(response.getStatus(), HttpResponseCodes.SC_OK);
            } finally {
                response.releaseConnection();
            }
            Date stopTime = new Date();
            totalTime = totalTime + (stopTime.getTime() - startTime.getTime());
            System.out.println("Overhead roundtrip time is: " + (stopTime.getTime() - startTime.getTime()));
        }

        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Client to server roundtrip overhead: " + (float) (totalTime / numOfCalls) / 1000);
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("");

        return result;
    }

    /**
     * Search collection objects.
     *
     * @param numberOfObjects the number of objects
     */
    private void searchCollectionObjects(int numberOfObjects) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        Random randomGenerator = new Random(System.currentTimeMillis());
        ClientResponse<AbstractCommonList> searchResults;

        long totalTime = 0;
        long totalSearchResults = 0;
        String keywords = "";
        String times = "";
        final boolean NOT_INCLUDING_DELETED_RESOURCES = false;
        for (int numOfKeywords = 0; numOfKeywords < MAX_KEYWORDS;
                numOfKeywords++, totalTime = 0, totalSearchResults = 0, times = "") {
            keywords = keywords + " " + OBJECT_TITLE + randomGenerator.nextInt(numberOfObjects);
            for (int i = 0; i < MAX_SEARCHES; i++) {
                //sandwich the call with timestamps
                Date startTime = new Date();
                searchResults = collectionObjectClient.keywordSearchIncludeDeleted(keywords, NOT_INCLUDING_DELETED_RESOURCES);
                Date stopTime = new Date();

                //extract the result list and release the ClientResponse
                AbstractCommonList coListItem = null;
                try {
                    coListItem = searchResults.getEntity();
                } finally {
                    searchResults.releaseConnection();
                }

                long time = stopTime.getTime() - startTime.getTime();
                times = times + " " + ((float) time / 1000);
                totalTime = totalTime + time;
                totalSearchResults = totalSearchResults
                        + coListItem.getListItem().size();
            }
            if (logger.isDebugEnabled()) {
                System.out.println("------------------------------------------------------------------------------");
                System.out.println("Searched Objects: " + numberOfObjects);
                System.out.println("Number of keywords: " + numOfKeywords);
                System.out.println("List of keywords: " + keywords);
                System.out.println("Number of results: " + totalSearchResults / MAX_SEARCHES);
                System.out.println("Result times: " + times);
                System.out.println("Average Retreive time: " + (totalTime / MAX_SEARCHES) / 1000.0 + " seconds.");
                System.out.println("------------------------------------------------------------------------------");
            }
        }
        return;
    }

    /**
     * Creates the collection object.
     *
     * @param collectionObjectClient the collection object client
     * @param identifier the identifier
     * @return the string
     */
    private String createCollectionObject(CollectionObjectClient collectionObjectClient,
            PoxPayloadOut multipart) {
        String result = null;
        // Make the create call and check the response
        ClientResponse<Response> response = collectionObjectClient.create(multipart);
        try {
            int responseStatus = response.getStatus();
            if (logger.isDebugEnabled() == true) {
                if (responseStatus != Response.Status.CREATED.getStatusCode()) {
                    logger.debug("Status of call to create CollectionObject was: "
                            + responseStatus);
                }
            }

            Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
            result = extractId(response);
        } finally {
            response.releaseConnection();
        }

        return result;
    }

    /**
     * Creates the collection objects.
     *
     * @param numberOfObjects the number of objects
     * @return the string[]
     */
    public String[] createCollectionObjects(int numberOfObjects) {
        Random randomGenerator = new Random(System.currentTimeMillis());
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String[] coList = new String[numberOfObjects];

        //
        // First create a CollectionObject
        //
        CollectionobjectsCommon co = new CollectionobjectsCommon();
        fillCollectionObject(co, Long.toString(System.currentTimeMillis()));

        // Next, create a part object
        PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(collectionObjectClient.getCommonPartName());

        int createdObjects = 0;
        try {
            Date startTime = new Date();
            for (int i = 0; i < numberOfObjects; i++, createdObjects++) {
                coList[i] = createCollectionObject(collectionObjectClient, multipart);
                if (logger.isDebugEnabled() == true) {
                    //
                    // Print out a status every 10 operations
                    if (i % 10 == 0) {
                        logger.debug("Created CollectionObject #: " + i);
                    }
                }
            }
            Date stopTime = new Date();
            if (logger.isDebugEnabled()) {
                System.out.println("Created " + numberOfObjects + " CollectionObjects"
                        + " in " + (stopTime.getTime() - startTime.getTime()) / 1000.0 + " seconds.");
            }
        } catch (AssertionError e) {
            System.out.println("FAILURE: Created " + createdObjects + " of " + numberOfObjects
                    + " before failing.");
            Assert.assertTrue(false);
        }

        return coList;
    }
    //
    //
    //

    /**
     * Delete collection object.
     *
     * @param collectionObjectClient the collection object client
     * @param resourceId the resource id
     */
    private void readCollectionObject(CollectionObjectClient collectionObjectClient,
            String resourceId) {
        ClientResponse<String> res = collectionObjectClient.read(resourceId);
        res.releaseConnection();
    }

    /**
     * Delete collection objects.
     *
     * @param arrayOfObjects the array of objects
     */
    public void readCollectionObjects(String[] arrayOfObjects) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

        Date startTime = new Date();
        for (int i = 0; i < arrayOfObjects.length; i++) {
            readCollectionObject(collectionObjectClient, arrayOfObjects[i]);
        }
        Date stopTime = new Date();

        if (logger.isDebugEnabled()) {
            System.out.println("Read " + arrayOfObjects.length + " CollectionObjects"
                    + " in " + (stopTime.getTime() - startTime.getTime()) / 1000.0 + " seconds.");
        }
    }

    /**
     * Delete collection objects.
     * FIXME: Deletes a page at a time until there are no more CollectionObjects.
     */
    public void readCollectionObjects() {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        ClientResponse<AbstractCommonList> response;

        List<AbstractCommonList.ListItem> coListItems = null;
        do {
            response = collectionObjectClient.readList(new Long(MAX_RECORDS),
                    new Long(0));
            try {
                AbstractCommonList commonListElement =
                        (AbstractCommonList) response.getEntity(AbstractCommonList.class);
                coListItems = commonListElement.getListItem();
            } finally {
                response.releaseConnection();
            }

            Date startTime = new Date();
            for (AbstractCommonList.ListItem i : coListItems) {
                readCollectionObject(collectionObjectClient, AbstractCommonListUtils.ListItemGetElementValue(i, "csid"));
            }
            Date stopTime = new Date();

            if (logger.isDebugEnabled()) {
                System.out.println("Read " + coListItems.size() + " CollectionObjects"
                        + " in " + (stopTime.getTime() - startTime.getTime()) / 1000.0 + " seconds.");
            }
        } while (coListItems.size() > 0);
    }

    //
    //
    //
    /**
     * Delete collection object.
     *
     * @param collectionObjectClient the collection object client
     * @param resourceId the resource id
     */
    private void deleteCollectionObject(CollectionObjectClient collectionObjectClient,
            String resourceId) {
        ClientResponse<Response> res = collectionObjectClient.delete(resourceId);
        res.releaseConnection();
    }

    /**
     * Delete collection objects.
     *
     * @param arrayOfObjects the array of objects
     */
    private void deleteCollectionObjects(String[] arrayOfObjects) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

        Date startTime = new Date();
        for (int i = 0; i < arrayOfObjects.length; i++) {
            deleteCollectionObject(collectionObjectClient, arrayOfObjects[i]);
        }
        Date stopTime = new Date();

        if (logger.isDebugEnabled()) {
            System.out.println("Deleted " + arrayOfObjects.length + " CollectionObjects"
                    + " in " + (stopTime.getTime() - startTime.getTime()) / 1000.0 + " seconds.");
        }
    }

    /**
     * Delete collection objects.
     * FIXME: Deletes a page at a time until there are no more CollectionObjects.
     */
    private void deleteCollectionObjects() {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        ClientResponse<AbstractCommonList> response;

        List<AbstractCommonList.ListItem> coListItems = null;
        do {
            response = collectionObjectClient.readList(new Long(MAX_RECORDS),
                    new Long(0));
            try {
                AbstractCommonList commonListElement =
                        (AbstractCommonList) response.getEntity(AbstractCommonList.class);
                coListItems = commonListElement.getListItem();
            } finally {
                response.releaseConnection();
            }

            Date startTime = new Date();
            for (AbstractCommonList.ListItem i : coListItems) {
                deleteCollectionObject(collectionObjectClient, AbstractCommonListUtils.ListItemGetElementValue(i, "csid"));
            }
            Date stopTime = new Date();

            if (logger.isDebugEnabled()) {
                System.out.println("Deleted " + coListItems.size() + " CollectionObjects"
                        + " in " + (stopTime.getTime() - startTime.getTime()) / 1000.0 + " seconds.");
            }
        } while (coListItems.size() > 0);
    }
}
