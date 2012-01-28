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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.CollectionObjectJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * CollectionObjectSearchTest, carries out tests of keyword search functionality
 * against a deployed and running CollectionObject Service.
 * 
 * $LastChangedRevision: 1327 $ $LastChangedDate: 2010-02-12 10:35:11 -0800
 * (Fri, 12 Feb 2010) $
 */
public class CollectionObjectSearchTest extends BaseServiceTest<AbstractCommonList> {

	/** The logger. */
	private final String CLASS_NAME = CollectionObjectSearchTest.class
			.getName();
	private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
	final static String IDENTIFIER = getSystemTimeIdentifier();
	final static String KEYWORD_SEPARATOR = " ";
	final long numNoiseWordResources = 10;
	final double pctNonNoiseWordResources = 0.5;
	// Use this to keep track of resources to delete
	private List<String> allResourceIdsCreated = new ArrayList<String>();

	// Constants for data used in search testing

	// Test keywords unlikely to be encountered in actual collections data,
	// consisting of the names of mythical creatures in a 1970s role-playing
	// game, which result in very few 'hits' in Google searches.
	final static String KEYWORD = "Tsolyani" + IDENTIFIER;
	final static List<String> TWO_KEYWORDS = Arrays.asList(new String[] {
			"Cheggarra" + IDENTIFIER, "Ahoggya" + IDENTIFIER });
	final static List<String> TWO_MORE_KEYWORDS = Arrays.asList(new String[] {
			"Karihaya" + IDENTIFIER, "Hlikku" + IDENTIFIER });
	final static String NOISE_WORD = "Mihalli + IDENTIFIER";
	// Test Unicode UTF-8 term for keyword searching: a random sequence,
	// unlikely to be encountered in actual collections data, of two USASCII
	// characters followed by four non-USASCII range Unicode UTF-8 characters:
	//
	// Δ : Greek capital letter Delta (U+0394)
	// Ж : Cyrillic capital letter Zhe with breve (U+04C1)
	// Ŵ : Latin capital letter W with circumflex (U+0174)
	// Ω : Greek capital letter Omega (U+03A9)
	final String UTF8_KEYWORD = "to" + '\u0394' + '\u04C1' + '\u0174'
			+ '\u03A9';
	// Non-existent term unlikely to be encountered in actual collections
	// data, consisting of two back-to-back sets of the first letters of
	// each of the words in a short pangram for the English alphabet.
	final static String NON_EXISTENT_KEYWORD = "jlmbsoqjlmbsoq";

	@Override
	protected String getServiceName() {
		throw new UnsupportedOperationException(); // FIXME: REM - See
													// http://issues.collectionspace.org/browse/CSPACE-3498
	}

	@Override
	protected String getServicePathComponent() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(); // FIXME: REM - See
													// http://issues.collectionspace.org/browse/CSPACE-3498
	}

	// /* (non-Javadoc)
	// * @see
	// org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
	// */
	// @Override
	// protected String getServicePathComponent() {
	// return new CollectionObjectClient().getServicePathComponent(); //FIXME:
	// REM = Remove all refs to this method.
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.collectionspace.services.client.test.BaseServiceTest#getClientInstance
	 * ()
	 */
	@Override
	protected CollectionSpaceClient getClientInstance() {
		return new CollectionObjectClient();
	}
	
	/**
	 * Creates one or more resources containing a "noise" keyword, which should
	 * NOT be retrieved by keyword searches.
	 * 
	 * This also helps ensure that searches will not fail, due to a
	 * database-specific constraint or otherwise, if the number of records
	 * containing a particular keyword represent too high a proportion of the
	 * total number of records.
	 */
	@BeforeClass(alwaysRun = true)
	public void setup() {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating " + numNoiseWordResources
					+ " 'noise word' resources ...");
		}
		createCollectionObjects(numNoiseWordResources, NOISE_WORD);
	}

	// ---------------------------------------------------------------
	// Search tests
	// ---------------------------------------------------------------
	// Success outcomes

	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, groups = { "advancedSearch" })
	public void advancedSearch(String testName) throws Exception {
		// Create one or more keyword retrievable resources, each containing
		// a specified keyword.
		String theKeyword = KEYWORD + "COW";
		long numKeywordRetrievableResources = 1;
		createCollectionObjects(numKeywordRetrievableResources, theKeyword);

		// Set the expected status code and group of valid status codes
		testSetup(STATUS_OK, ServiceRequestType.SEARCH);

		// Send the search request and receive a response
		String propertyName = CollectionObjectClient.SERVICE_COMMON_PART_NAME + ":" +
			CollectionObjectJAXBSchema.DISTINGUISHING_FEATURES;
		String propertyValue = theKeyword;
		ClientResponse<AbstractCommonList> res = doAdvancedSearch(propertyName, propertyValue, "=");
		int statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		long NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		long numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);
	}

	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, groups = { "oneKeyword" })
	public void searchWithOneKeyword(String testName) throws Exception {
		// Create one or more keyword retrievable resources, each containing
		// a specified keyword.
		long numKeywordRetrievableResources = (long) (numNoiseWordResources * pctNonNoiseWordResources);
		if (logger.isDebugEnabled()) {
			logger.debug("Creating " + numKeywordRetrievableResources
					+ " keyword-retrievable resources ...");
		}
		createCollectionObjects(numKeywordRetrievableResources, KEYWORD);

		// Set the expected status code and group of valid status codes
		testSetup(STATUS_OK, ServiceRequestType.SEARCH);

		// Send the search request and receive a response
		ClientResponse<AbstractCommonList> res = doSearch(KEYWORD);
		int statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		long NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		long numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);
	}

	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
	public void searchWithTwoKeywordsInSameField(String testName)
			throws Exception {
		// Create one or more keyword retrievable resources, each containing
		// two specified keywords.
		long numKeywordRetrievableResources = (long) (numNoiseWordResources * pctNonNoiseWordResources);
		if (logger.isDebugEnabled()) {
			logger.debug("Creating " + numKeywordRetrievableResources
					+ " keyword-retrievable resources ...");
		}
		boolean keywordsInSameField = true;
		createCollectionObjects(numKeywordRetrievableResources, TWO_KEYWORDS,
				keywordsInSameField);

		// Set the expected status code and group of valid status codes
		testSetup(STATUS_OK, ServiceRequestType.SEARCH);

		// Search using both terms

		// Send the search request and receive a response
		ClientResponse<AbstractCommonList> res = doSearch(TWO_KEYWORDS);
		int statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		long NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		long numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);

		// Search using a single term

		// Send the search request and receive a response
		res = doSearch(TWO_KEYWORDS.get(0));
		statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);

	}

	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
	public void searchWithTwoKeywordsAcrossTwoFields(String testName)
			throws Exception {
		// Create one or more keyword retrievable resources, each containing
		// two specified keywords.
		long numKeywordRetrievableResources = 5;
		if (logger.isDebugEnabled()) {
			logger.debug("Creating " + numKeywordRetrievableResources
					+ " keyword-retrievable resources ...");
		}
		boolean keywordsInSameField = false;
		createCollectionObjects(numKeywordRetrievableResources,
				TWO_MORE_KEYWORDS, keywordsInSameField);

		// Set the expected status code and group of valid status codes
		testSetup(STATUS_OK, ServiceRequestType.SEARCH);

		// Search using both terms

		// Send the search request and receive a response
		ClientResponse<AbstractCommonList> res = doSearch(TWO_MORE_KEYWORDS);
		int statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		long NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		long numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);

		// Search using a single term

		// Send the search request and receive a response
		res = doSearch(TWO_MORE_KEYWORDS.get(0));
		statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);

	}

	// @Test(dataProvider="testName",
	// dataProviderClass=AbstractServiceTestImpl.class)
	// public void searchWithOneKeywordInRepeatableScalarField(String testName)
	// throws Exception {
	// BriefDescriptionList descriptionList = new BriefDescriptionList();
	// List<String> descriptions = descriptionList.getBriefDescription();
	// if (TWO_KEYWORDS.size() >= 2) {
	// descriptions.add(TWO_KEYWORDS.get(0));
	// descriptions.add(TWO_KEYWORDS.get(1));
	// }
	// }
	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, groups = { "utf8" })
	public void searchWithUTF8Keyword(String testName) {
		// Create one or more keyword retrievable resources, each containing
		// two specified keywords.
		long numKeywordRetrievableResources = 2;
		if (logger.isDebugEnabled()) {
			logger.debug("Creating " + numKeywordRetrievableResources
					+ " keyword-retrievable resources ...");
		}
		createCollectionObjects(numKeywordRetrievableResources, UTF8_KEYWORD);

		// Set the expected status code and group of valid status codes
		testSetup(STATUS_OK, ServiceRequestType.SEARCH);

		// Send the search request and receive a response
		ClientResponse<AbstractCommonList> res = doSearch(UTF8_KEYWORD);
		int statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		long NUM_MATCHES_EXPECTED = numKeywordRetrievableResources;
		long numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);
	}

	// Failure outcomes
	// FIXME: Rename to searchWithNonExistentKeyword
	@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
	public void keywordSearchNonExistentKeyword(String testName)
			throws Exception {
		// Set the expected status code and group of valid status codes
		testSetup(STATUS_OK, ServiceRequestType.SEARCH);

		// Send the search request and receive a response
		ClientResponse<AbstractCommonList> res = doSearch(NON_EXISTENT_KEYWORD);
		int statusCode = res.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (logger.isDebugEnabled()) {
			logger.debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(testRequestType, statusCode));
		Assert.assertEquals(statusCode, testExpectedStatusCode);

		// Verify that the number of resources matched by the search
		// is identical to the expected result
		long NUM_MATCHES_EXPECTED = 0;
		long numMatched = getNumMatched(res, NUM_MATCHES_EXPECTED, testName);
		Assert.assertEquals(numMatched, NUM_MATCHES_EXPECTED);

	}

	// ---------------------------------------------------------------
	// Cleanup of resources created during testing
	// ---------------------------------------------------------------
	/**
	 * Deletes all resources created by setup and tests, after all tests have
	 * been run.
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
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		for (String resourceId : allResourceIdsCreated) {
			// Note: Any non-success responses are ignored and not reported.
			collectionObjectClient.delete(resourceId).releaseConnection();
		}
	}

	// ---------------------------------------------------------------
	// Utility methods used by tests above
	// ---------------------------------------------------------------
	private void createCollectionObjects(long numToCreate, String keyword) {
		List keywords = new ArrayList<String>();
		keywords.add(keyword);
		boolean keywordsInSameField = true;
		createCollectionObjects(numToCreate, keywords, keywordsInSameField);
	}

	private void createCollectionObjects(long numToCreate,
			List<String> keywords, boolean keywordsInSameField) {
		testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
		CollectionObjectClient client = new CollectionObjectClient();
		for (long i = 0; i < numToCreate; i++) {
			PoxPayloadOut multipart = createCollectionObjectInstance(i,
					keywords, keywordsInSameField);
			ClientResponse<Response> res = client.create(multipart);
			try {
				int statusCode = res.getStatus();
				Assert.assertEquals(statusCode, testExpectedStatusCode);
				String id = extractId(res);
				allResourceIdsCreated.add(id);
				if (logger.isDebugEnabled()) {
					logger.debug("Created new resource [" + i + "] with ID "
							+ id);
				}
			} finally {
				res.releaseConnection();
			}
		}
	}

	private PoxPayloadOut createCollectionObjectInstance(long i,
			List<String> keywords, boolean keywordsInSameField) {
		CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
		collectionObject.setObjectNumber(createIdentifier());
		if (keywordsInSameField) {
			collectionObject.setDistinguishingFeatures(listToString(keywords,
					KEYWORD_SEPARATOR));
		} else {
			if (keywords.size() == 1) {
				collectionObject.setDistinguishingFeatures(keywords.get(0));
			} else if (keywords.size() == 2) {
				collectionObject.setDistinguishingFeatures(keywords.get(0));
				collectionObject.setPhysicalDescription(keywords.get(1));
			} else {
				Assert.fail("List of keywords must have exactly one or two members.");
			}
		}
		PoxPayloadOut multipart = new PoxPayloadOut(
				CollectionObjectClient.SERVICE_PAYLOAD_NAME);
		PayloadOutputPart commonPart = multipart.addPart(collectionObject,
				MediaType.APPLICATION_XML_TYPE);
		commonPart.setLabel(new CollectionObjectClient().getCommonPartName());
		return multipart;
	}

	private static String listToString(List<String> list, String separator) {
		StringBuffer sb = new StringBuffer();
		if (list.size() > 0) {
			sb.append(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				sb.append(separator);
				sb.append(list.get(i));
			}
		}
		return sb.toString();
	}

	private ClientResponse<AbstractCommonList> doSearch(List<String> keywords) {
		String searchParamValue = listToString(keywords, KEYWORD_SEPARATOR);
		return doSearch(searchParamValue);
	}

	private ClientResponse<AbstractCommonList> doAdvancedSearch(
			String propertyName, String propertyValue, String operator) {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching on property: " + propertyName + "="
					+ "'" + propertyValue + "'");
		}
		String whereClause = propertyName + operator +
			"'" + propertyValue + "'";
		CollectionObjectClient client = new CollectionObjectClient();
		ClientResponse<AbstractCommonList> res = client
				.advancedSearchIncludeDeleted(whereClause, false); // NOT_INCLUDING_DELETED_RESOURCES
		return res;
	}

	private ClientResponse<AbstractCommonList> doSearch(String keyword) {
		String searchParamValue = keyword;
		if (logger.isDebugEnabled()) {
			logger.debug("Searching on keyword(s): " + searchParamValue
					+ " ...");
		}
		CollectionObjectClient client = new CollectionObjectClient();
		final boolean NOT_INCLUDING_DELETED_RESOURCES = false;
		ClientResponse<AbstractCommonList> res = client
				.keywordSearchIncludeDeleted(searchParamValue,
						NOT_INCLUDING_DELETED_RESOURCES);
		return res;
	}

	private long getNumMatched(ClientResponse<AbstractCommonList> res,
			long numExpectedMatches, String testName) {
		AbstractCommonList list = (AbstractCommonList) res
				.getEntity(AbstractCommonList.class);
		long numMatched = list.getTotalItems();
		if (logger.isDebugEnabled()) {
			logger.debug("Keyword search matched " + numMatched
					+ " resources, expected to match " + numExpectedMatches);
		}

		// Optionally output additional data about list members for debugging.
		if (logger.isTraceEnabled()) {
			AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger,
					testName);
		}

		return numMatched;
	}

	private void itemizeListItems(AbstractCommonList list) {
		List<AbstractCommonList.ListItem> items = list.getListItem();
		int i = 0;
		for (AbstractCommonList.ListItem item : items) {
			logger.debug("list-item["
					+ i
					+ "] title="
					+ AbstractCommonListUtils.ListItemGetElementValue(item,
							"title"));
			logger.debug("list-item["
					+ i
					+ "] URI="
					+ AbstractCommonListUtils.ListItemGetElementValue(item,
							"uri"));
			i++;
		}
	}

	public static String getSystemTimeIdentifier() {
		return Long.toString(System.currentTimeMillis());
	}
}
