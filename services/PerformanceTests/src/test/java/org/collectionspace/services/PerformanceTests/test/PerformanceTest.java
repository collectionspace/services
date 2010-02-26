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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Random;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.TraceMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.collectionspace.services.client.TestServiceClient;

import org.collectionspace.services.CollectionObjectJAXBSchema;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList.CollectionObjectListItem;

import org.collectionspace.services.IntakeJAXBSchema;
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.intake.IntakesCommonList;

import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationshipType;

/**
 * A ServiceTest.
 * 
 * @version $Revision:$
 */
public class PerformanceTest extends CollectionSpacePerformanceTest {

	private static final int MAX_KEYWORDS = 10;
	private static final int MAX_SEARCHES = 10;
	final Logger logger = LoggerFactory
			.getLogger(PerformanceTest.class);
	//
	// Get clients for the CollectionSpace services
	//
	private static int MAX_RECORDS = 10000;

	@Test
	public void performanceTest() {
		roundTripOverhead(10);
		deleteCollectionObjects();
		String[] coList = this.createCollectionObjects(MAX_RECORDS);
		this.searchCollectionObjects(MAX_RECORDS);
//		this.deleteCollectionObjects(coList);
		roundTripOverhead(10);
	}
	
	private long roundTripOverhead(int numOfCalls) {
		long result = 0;
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		
		long totalTime = 0;
		for (int i = 0; i < numOfCalls; i++) {
			Date startTime = new Date();
			collectionObjectClient.roundtrip();
			Date stopTime = new Date();
			totalTime = totalTime + (stopTime.getTime() - startTime.getTime());
			System.out.println("Overhead roundtrip time is: " + (stopTime.getTime() - startTime.getTime()));
		}
		
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("Client to server roundtrip overhead: " + (float)(totalTime / numOfCalls) / 1000);
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("");
		
		return result;
	}
	
	private void searchCollectionObjects(int numberOfObjects) {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		Random randomGenerator = new Random(System.currentTimeMillis());				
		ClientResponse<CollectionobjectsCommonList> searchResults;
		
		long totalTime = 0;
		long totalSearchResults = 0;
		String keywords = "";
		String times = "";
		for (int numOfKeywords = 0; numOfKeywords < MAX_KEYWORDS;
				numOfKeywords++, totalTime = 0, totalSearchResults = 0, times = "") {
			keywords = keywords + " " + OBJECT_NAME + randomGenerator.nextInt(numberOfObjects);
			for (int i = 0; i < MAX_SEARCHES; i++) {
				Date startTime = new Date();
				searchResults = collectionObjectClient.keywordSearch(keywords);
				Date stopTime = new Date();
				long time = stopTime.getTime() - startTime.getTime();
				times = times + " " + ((float)time / 1000);
				totalTime = totalTime + time;				
				totalSearchResults = totalSearchResults +
					searchResults.getEntity().getCollectionObjectListItem().size();
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
	
	private String createCollectionObject(CollectionObjectClient collectionObjectClient,
			int identifier) {
		String result = null;
		//
		// First create a CollectionObject
		//
		CollectionobjectsCommon co = new CollectionobjectsCommon();
		fillCollectionObject(co, Integer.toString(identifier));
		
		// Next, create a part object
		MultipartOutput multipart = new MultipartOutput();
		OutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
		commonPart.getHeaders().add("label", collectionObjectClient.getCommonPartName());
		// Make the create call and check the response
		ClientResponse<Response> response = collectionObjectClient.create(multipart);
		
		int responseStatus = response.getStatus();
		if (logger.isDebugEnabled() == true) {
			if (responseStatus != Response.Status.CREATED.getStatusCode())
				logger.debug("Status of call to create CollectionObject was: " +
						responseStatus);
		}
		
		Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());		
		result = extractId(response);
		
		return result;
	}
	
	public String[] createCollectionObjects(int numberOfObjects) {
		Random randomGenerator = new Random(System.currentTimeMillis());
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String[] coList = new String[numberOfObjects];		
		
		int createdObjects = 0;
		try {
			Date startTime = new Date();
			for (int i = 0; i < numberOfObjects; i++, createdObjects++) {
				coList[i] = createCollectionObject(collectionObjectClient, i + 1);
			}
			Date stopTime = new Date();
			if (logger.isDebugEnabled()) {
				System.out.println("Created " + numberOfObjects + " CollectionObjects" +
						" in " + (stopTime.getTime() - startTime.getTime())/1000.0 + " seconds.");
			}
		} catch (AssertionError e) {
			System.out.println("FAILURE: Created " + createdObjects + " of " + numberOfObjects +
					" before failing.");
			Assert.assertTrue(false);
		}
		
		return coList;
	}
	
	private void deleteCollectionObject(CollectionObjectClient collectionObjectClient,
			String resourceId) {
		ClientResponse<Response> res = collectionObjectClient.delete(resourceId);			
	}

	public void deleteCollectionObjects(String[] arrayOfObjects) {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

		Date startTime = new Date();		
		for (int i = 0; i < arrayOfObjects.length; i++) {
			deleteCollectionObject(collectionObjectClient, arrayOfObjects[i]);
		}
		
		Date stopTime = new Date();
		if (logger.isDebugEnabled()) {
			System.out.println("Deleted " + arrayOfObjects.length + " CollectionObjects" +
					" in " + (stopTime.getTime() - startTime.getTime())/1000.0 + " seconds.");
		}
	}
	
	public void deleteCollectionObjects() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		ClientResponse<CollectionobjectsCommonList> commonsList;
		commonsList = collectionObjectClient.readList();
		List<CollectionObjectListItem> coListItems = commonsList.getEntity().getCollectionObjectListItem();
		
		Date startTime = new Date();
		for (CollectionObjectListItem i:coListItems) {
			deleteCollectionObject(collectionObjectClient, i.getCsid());
		}
		Date stopTime = new Date();
		
		if (logger.isDebugEnabled()) {
			System.out.println("Deleted " + coListItems.size() + " CollectionObjects" +
					" in " + (stopTime.getTime() - startTime.getTime())/1000.0 + " seconds.");
		}
	}

}
