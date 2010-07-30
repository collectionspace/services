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
package org.collectionspace.services.ItegrationTests.test;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.collectionspace.services.client.CollectionObjectClient;
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
	
	@Test
	public void relateCollectionObjectToIntake() {
		
		//
		// First create a CollectionObject
		//
		CollectionobjectsCommon co = new CollectionobjectsCommon();
		fillCollectionObject(co, createIdentifier());
		
		// Next, create a part object
		MultipartOutput multipart = new MultipartOutput();
		OutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
		commonPart.getHeaders().add("label", collectionObjectClient.getCommonPartName());
		
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
	    multipart = new MultipartOutput();
	    commonPart = multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
	    commonPart.getHeaders().add("label", intakeClient.getCommonPartName());

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
	    		RelationshipType.COLLECTIONOBJECT_INTAKE);
	    // Create the part and fill it with the relation object
	    multipart = new MultipartOutput();
	    commonPart = multipart.addPart(relation, MediaType.APPLICATION_XML_TYPE);
	    commonPart.getHeaders().add("label", relationClient.getCommonPartName());

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
	    String predicate = RelationshipType.COLLECTIONOBJECT_INTAKE.value();
	    ClientResponse<RelationsCommonList> resultResponse = relationClient.readList(
	    		collectionObjectCsid,
	    		predicate,
	    		intakeCsid);
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
        	ClientResponse<MultipartInput> multiPartResponse = null;
        	try {
        		multiPartResponse = relationClient.read(foundCsid);
        		int responseStatus = multiPartResponse.getStatus();
        		Assert.assertEquals(responseStatus, Response.Status.OK.getStatusCode());
	        	MultipartInput input = (MultipartInput) multiPartResponse.getEntity();
	        	resultRelation = (RelationsCommon) extractPart(input,
	        			relationClient.getCommonPartName(),
	        			RelationsCommon.class);
        	} catch (Exception e) {
        		e.printStackTrace();
        	} finally {
        		multiPartResponse.releaseConnection();
        	}
        	
        	Assert.assertEquals(resultRelation.getDocumentId1(), collectionObjectCsid);
        	Assert.assertEquals(resultRelation.getRelationshipType(), RelationshipType.COLLECTIONOBJECT_INTAKE);
        	Assert.assertEquals(resultRelation.getDocumentId2(), intakeCsid);
            System.out.println();
        	i++;            
        }
	}

	/*
	 * Private Methods
	 */

}
