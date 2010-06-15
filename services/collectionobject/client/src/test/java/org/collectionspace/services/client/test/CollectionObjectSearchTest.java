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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * CollectionObjectAuthRefsTest, carries out tests against a
 * deployed and running CollectionObject Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class CollectionObjectSearchTest extends BaseServiceTest {

   /** The logger. */
    private final String CLASS_NAME = CollectionObjectSearchTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    final static String KEYWORD = "Tsolyani";
    // final static String[] TWO_KEYWORDS = {"Cheggarra", "Ahoggya"};
    final static String NOISE_WORD = "Mihalli";
    final static String NON_EXISTENT_KEYWORD = "jlmbsoqjlmbsoq";

    /* Use this to keep track of resources to delete */
    private List<String> allResourceIdsCreated = new ArrayList<String>();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return new CollectionObjectClient().getServicePathComponent();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new CollectionObjectClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getAbstractCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(CollectionobjectsCommonList.class);
    }

    /**
     * Creates one or more resources containing a "noise" keyword,
     * which should NOT be retrieved by keyword searches.
     */
    @BeforeClass(alwaysRun=true)
    public void setup() {
        long numNoiseWordResources = 2;
        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + numNoiseWordResources +
                " 'noise word' resources ...");
        }
        createCollectionObjects(numNoiseWordResources, NOISE_WORD);
    }


    // ---------------------------------------------------------------
    // Search tests
    // ---------------------------------------------------------------

    // Success outcomes

    // FIXME: Rename to searchWithOneKeyword
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void keywordSearchOneWord(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        // Create one or more keyword retrievable resources, each containing
        // a specified keyword.
        long numKeywordRetrievableResources = 3;
        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + numKeywordRetrievableResources +
                " keyword-retrievable resources ...");
        }
        createCollectionObjects(numKeywordRetrievableResources, KEYWORD);

        testSetup(STATUS_OK, ServiceRequestType.SEARCH);
        if (logger.isDebugEnabled()) {
            logger.debug("Searching on keyword(s): " + KEYWORD + " ...");
        }
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<CollectionobjectsCommonList> res =
            client.keywordSearch(KEYWORD);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));

        CollectionobjectsCommonList list = (CollectionobjectsCommonList)
            res.getEntity(CollectionobjectsCommonList.class);
        long numMatched = list.getTotalItems();

        if (logger.isDebugEnabled()) {
            logger.debug("Keyword search matched " + numMatched +
                " resources, expected to match " + numKeywordRetrievableResources);
        }

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            itemizeListItems(list);
        }

        Assert.assertEquals(numMatched, numKeywordRetrievableResources);

    }

    // FIXME: Rename to searchWithOneKeywordInRepeatableScalarField
    // @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void keywordSearchRepeatableScalarField(String testName) throws Exception {
    }

    // Failure outcomes

    // FIXME: Rename to searchWithNonExistentKeyword
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void keywordSearchNonExistentKeyword(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        final long NUM_MATCHES_EXPECTED = 0;

        testSetup(STATUS_OK, ServiceRequestType.SEARCH);
        if (logger.isDebugEnabled()) {
            logger.debug("Searching on keyword(s): " + NON_EXISTENT_KEYWORD + " ...");
        }
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<CollectionobjectsCommonList> res =
            client.keywordSearch(NON_EXISTENT_KEYWORD);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));

        CollectionobjectsCommonList list = (CollectionobjectsCommonList)
            res.getEntity(CollectionobjectsCommonList.class);
        long numMatched = list.getTotalItems();

        if (logger.isDebugEnabled()) {
            logger.debug("Keyword search matched " + numMatched +
                " resources, expected to match " + NUM_MATCHES_EXPECTED);
        }

        Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);

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
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            collectionObjectClient.delete(resourceId).releaseConnection();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    private void createCollectionObjects(long numToCreate, String keywords) {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        CollectionObjectClient client = new CollectionObjectClient();
        for (long i = 0; i < numToCreate; i++) {
            MultipartOutput multipart = createCollectionObjectInstance(keywords);
            ClientResponse<Response> res = client.create(multipart);
            try {
                int statusCode = res.getStatus();
                Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
                allResourceIdsCreated.add(extractId(res));
            } finally {
                res.releaseConnection();
            }
        }
    }

    private MultipartOutput createCollectionObjectInstance(String keywords) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
        collectionObject.setObjectNumber(createIdentifier());
        collectionObject.setTitle(keywords);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(collectionObject,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", new CollectionObjectClient().getCommonPartName());
        return multipart;
    }

    private void itemizeListItems(CollectionobjectsCommonList list) {
        List<CollectionobjectsCommonList.CollectionObjectListItem> items =
            list.getCollectionObjectListItem();
        int i = 0;
        for (CollectionobjectsCommonList.CollectionObjectListItem item : items) {
            logger.debug("list-item[" + i + "] title="
                    + item.getTitle());
            logger.debug("list-item[" + i + "] URI="
                    + item.getUri());
            i++;
        }
    }

}
