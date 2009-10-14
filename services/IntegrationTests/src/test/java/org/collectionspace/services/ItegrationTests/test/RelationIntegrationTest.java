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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		Assert.assertEquals(response.getStatus(), Response.Status.CREATED
				.getStatusCode());
		String collectionObjectCsid = extractId(response);
	    
	    
	    // Next, create an Intake object
	    IntakesCommon intake = new IntakesCommon();
	    fillIntake(intake, createIdentifier());
	    // Create the a part object
	    multipart = new MultipartOutput();
	    commonPart = multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
	    commonPart.getHeaders().add("label", intakeClient.getCommonPartName());
	    // Make the call to create and check the response
	    response = intakeClient.create(multipart);
	    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
	    String intakeCsid = extractId(response);
	    
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
	    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
	    String relationCsid = extractId(response);
	    
	    //
	    // Now try to retrieve the Intake record of the CollectionObject.
	    //
	    String predicate = RelationshipType.COLLECTIONOBJECT_INTAKE.value();
	    ClientResponse<RelationsCommonList> resultResponse = relationClient.readList_SPO(collectionObjectCsid,
	    		predicate,
	    		intakeCsid);
	    Assert.assertEquals(resultResponse.getStatus(), Response.Status.OK.getStatusCode());
	    
	    //
	    // Each relation returned in the list needs to match what we
	    // requested.
	    //
        RelationsCommonList relationList = resultResponse.getEntity();        
        List<RelationsCommonList.RelationListItem> relationListItems = relationList.getRelationListItem();
        Assert.assertFalse(relationListItems.isEmpty());
        
        ClientResponse<RelationsCommon> resultRelationResponse;
        RelationsCommon resultRelation = null;
        int i = 0;
        for(RelationsCommonList.RelationListItem listItem : relationListItems){
        	
        	String foundCsid = listItem.getCsid();
        	try {
        		ClientResponse<MultipartInput> multiPartResponse = relationClient.read(foundCsid);
        		int responseStatus = multiPartResponse.getStatus();
        		Assert.assertEquals(responseStatus, Response.Status.OK.getStatusCode());
	        	MultipartInput input = (MultipartInput) multiPartResponse.getEntity();
	        	resultRelation = (RelationsCommon) extractPart(input,
	        			relationClient.getCommonPartName(),
	        			RelationsCommon.class);
        	} catch (Exception e) {
        		e.printStackTrace();
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
