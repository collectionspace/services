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
package org.collectionspace.services.IntegrationTests.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;

import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.intake.IntakesCommon;

import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationshipType;

/**
 * A ServiceTest.
 * 
 * @version $Revision:$
 */
public class RelationIntegrationTest extends CollectionSpaceIntegrationTest {

	final Logger logger = LoggerFactory
			.getLogger(RelationIntegrationTest.class);
	//
	// Get clients for the CollectionSpace services
	//
	private CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
	private RelationClient relationClient = new RelationClient();
	private IntakeClient intakeClient = new IntakeClient();
	
	private static final int OBJECTS_TO_INTAKE = 1;
	
	@Test
	public void relateCollectionObjectToIntake() {
		//
		// First create a CollectionObject
		//
		CollectionobjectsCommon co = new CollectionobjectsCommon();
		fillCollectionObject(co, createIdentifier());
		
		// Next, create a part object
		PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
		PayloadOutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
		commonPart.setLabel(collectionObjectClient.getCommonPartName());
		
		// Make the create call and check the response
		ClientResponse<Response> response = collectionObjectClient.create(multipart);
		String collectionObjectCsid = null;
		try {
			Assert.assertEquals(response.getStatus(), Response.Status.CREATED
					.getStatusCode());
			collectionObjectCsid = extractId(response);
		} finally {
			response.releaseConnection();
		}
	    
	    
	    // Next, create an Intake object
	    IntakesCommon intake = new IntakesCommon();
	    fillIntake(intake, createIdentifier());
	    // Create the a part object
	    multipart = new PoxPayloadOut(IntakeClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
	    commonPart.setLabel(intakeClient.getCommonPartName());

	    // Make the call to create and check the response
	    response = intakeClient.create(multipart);
	    String intakeCsid = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    intakeCsid = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    // Lastly, relate the two entities, by creating a new relation object
	    RelationsCommon relation = new RelationsCommon();
	    fillRelation(relation, collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(),
	    		intakeCsid, IntakesCommon.class.getSimpleName(),
	    		RelationshipType.COLLECTIONOBJECT_INTAKE.toString());
	    // Create the part and fill it with the relation object
	    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(relation, MediaType.APPLICATION_XML_TYPE);
	    commonPart.setLabel(relationClient.getCommonPartName());

	    // Make the call to crate
	    response = relationClient.create(multipart);
	    String relationCsid = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    relationCsid = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    //
	    // Now try to retrieve the Intake record of the CollectionObject.
	    //
	    String predicate = RelationshipType.COLLECTIONOBJECT_INTAKE.toString();
	    ClientResponse<RelationsCommonList> resultResponse = relationClient.readList(
	    		collectionObjectCsid,
	    		null, //CollectionobjectsCommon.class.getSimpleName(),
	    		predicate,
	    		intakeCsid,
	    		null ); //IntakesCommon.class.getSimpleName());
        RelationsCommonList relationList = null;
	    try {
	    	Assert.assertEquals(resultResponse.getStatus(), Response.Status.OK.getStatusCode());
	        relationList = resultResponse.getEntity();
	    } finally {
	    	resultResponse.releaseConnection();
	    }
	    
	    //
	    // Each relation returned in the list needs to match what we
	    // requested.
	    //
        List<RelationsCommonList.RelationListItem> relationListItems = relationList.getRelationListItem();
        Assert.assertFalse(relationListItems.isEmpty());
        
        int i = 0;
        RelationsCommon resultRelation = null;
        for(RelationsCommonList.RelationListItem listItem : relationListItems){
        	
        	String foundCsid = listItem.getCsid();
        	ClientResponse<String> multiPartResponse = null;
        	try {
        		multiPartResponse = relationClient.read(foundCsid);
        		int responseStatus = multiPartResponse.getStatus();
        		Assert.assertEquals(responseStatus, Response.Status.OK.getStatusCode());
        		PoxPayloadIn input = new PoxPayloadIn(multiPartResponse.getEntity());
	        	resultRelation = (RelationsCommon) extractPart(input,
	        			relationClient.getCommonPartName(),
	        			RelationsCommon.class);
        	} catch (Exception e) {
        		e.printStackTrace();
        	} finally {
        		multiPartResponse.releaseConnection();
        	}
        	
        	Assert.assertEquals(resultRelation.getSubjectCsid(), collectionObjectCsid);
        	Assert.assertEquals(resultRelation.getRelationshipType(), RelationshipType.COLLECTIONOBJECT_INTAKE.toString());
        	Assert.assertEquals(resultRelation.getObjectCsid(), intakeCsid);
            System.out.println();
        	i++;            
        }
	}
	
	@Test
	public void relateCollectionObjectsToIntake() {
		relateCollectionObjectsToIntake(OBJECTS_TO_INTAKE);
	}

	private void relateCollectionObjectsToIntake(int numberOfObjects) {
		
		//
		// First create a list of CollectionObjects
		//
		CollectionobjectsCommon co = new CollectionobjectsCommon();
		ArrayList<String>collectionObjectIDList = new ArrayList<String>(numberOfObjects);
		for (int i = 0; i < numberOfObjects; i++) {
			fillCollectionObject(co, createIdentifier());
			
			// Next, create a part object
			PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
			PayloadOutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
			commonPart.setLabel(collectionObjectClient.getCommonPartName());
			
			// Make the create call and check the response
			ClientResponse<Response> response = collectionObjectClient.create(multipart);
			String collectionObjectCsid = null;
			try {
				Assert.assertEquals(response.getStatus(), Response.Status.CREATED
						.getStatusCode());
				collectionObjectCsid = extractId(response);
				collectionObjectIDList.add(collectionObjectCsid);
			} finally {
				response.releaseConnection();
			}
		}
	    
	    
	    // Next, create an Intake object
	    IntakesCommon intake = new IntakesCommon();
	    fillIntake(intake, createIdentifier());
	    // Create the a part object
	    PoxPayloadOut multipart = new PoxPayloadOut(IntakeClient.SERVICE_PAYLOAD_NAME);
	    PayloadOutputPart commonPart = multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
	    commonPart.setLabel(intakeClient.getCommonPartName());

	    // Make the call to create and check the response
	    ClientResponse<Response> response = intakeClient.create(multipart);
	    String intakeCsid = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    intakeCsid = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    // Lastly, relate the two entities, by creating a new relation object
	    RelationsCommon relation = new RelationsCommon();
	    for (String collectionObjectCsid : collectionObjectIDList) {
		    fillRelation(relation,
		    		intakeCsid, IntakesCommon.class.getSimpleName(), //subject
		    		collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(), //object		    		
		    		RelationshipType.COLLECTIONOBJECT_INTAKE.toString()); //predicate
		    // Create the part and fill it with the relation object
		    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
		    commonPart = multipart.addPart(relation, MediaType.APPLICATION_XML_TYPE);
		    commonPart.setLabel(relationClient.getCommonPartName());
	
		    // Make the call to crate
		    response = relationClient.create(multipart);
		    String relationCsid = null;
		    try {
			    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
			    relationCsid = extractId(response);
		    } finally {
		    	response.releaseConnection();
		    }
	    }
	    
	    //
	    // Now try to retrieve the Intake record of the CollectionObject.
	    //
	    String predicate = RelationshipType.COLLECTIONOBJECT_INTAKE.toString();
		RelationsCommonList relationList = null;
	    for (String collectionObjectCsid : collectionObjectIDList) {
		    ClientResponse<RelationsCommonList> resultResponse = relationClient.readList(
		    		intakeCsid,
		    		null, //IntakesCommon.class.getSimpleName(), //subject
		    		predicate,
		    		collectionObjectCsid,
		    		null); //CollectionobjectsCommon.class.getSimpleName()); //object

		    try {
		    	Assert.assertEquals(resultResponse.getStatus(), Response.Status.OK.getStatusCode());
		        relationList = resultResponse.getEntity();
		    } finally {
		    	resultResponse.releaseConnection();
		    }
	    
		    //
		    // Each relation returned in the list needs to match what we
		    // requested.
		    //
	        List<RelationsCommonList.RelationListItem> relationListItems = relationList.getRelationListItem();
	        Assert.assertFalse(relationListItems.isEmpty());
	        
	        int i = 0;
	        RelationsCommon resultRelation = null;
	        for(RelationsCommonList.RelationListItem listItem : relationListItems){
	        	String foundCsid = listItem.getCsid();
	        	ClientResponse<String> multiPartResponse = null;
	        	try {
	        		multiPartResponse = relationClient.read(foundCsid);
	        		int responseStatus = multiPartResponse.getStatus();
	        		Assert.assertEquals(responseStatus, Response.Status.OK.getStatusCode());
	        		PoxPayloadIn input = new PoxPayloadIn(multiPartResponse.getEntity());
	        		resultRelation = (RelationsCommon) extractPart(input,
	        				relationClient.getCommonPartName(),
	        				RelationsCommon.class);
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	} finally {
	        		multiPartResponse.releaseConnection();
	        	}
	
	        	Assert.assertEquals(resultRelation.getSubjectCsid(), intakeCsid);
	        	Assert.assertEquals(resultRelation.getRelationshipType(), RelationshipType.COLLECTIONOBJECT_INTAKE.toString());
	        	Assert.assertEquals(resultRelation.getObjectCsid(), collectionObjectCsid);
	        	System.out.println();
	        	i++;            
	        }
	    }
	}
	

	/*
	 * Private Methods
	 */

}
