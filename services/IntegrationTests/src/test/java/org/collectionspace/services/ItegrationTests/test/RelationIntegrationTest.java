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

import org.collectionspace.services.client.TestServiceClient;

import org.collectionspace.services.CollectionObjectJAXBSchema;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;

import org.collectionspace.services.IntakeJAXBSchema;
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.intake.IntakeList;

import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;
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
		CollectionObject co = new CollectionObject();
		fillCollectionObject(co, createIdentifier());
	    ClientResponse<Response> coResponse = collectionObjectClient.create(co);
	    Assert.assertEquals(coResponse.getStatus(), Response.Status.CREATED.getStatusCode());
	    String collectionObjectCsid = extractId(coResponse);
	    
	    // Next, create an Intake record
	    Intake intake = new Intake();
	    fillIntake(intake, createIdentifier());
	    ClientResponse<Response> intakeResponse = intakeClient.create(intake);
	    Assert.assertEquals(intakeResponse.getStatus(), Response.Status.CREATED.getStatusCode());
	    String intakeCsid = extractId(intakeResponse);
	    
	    // Lastly, relate the two entities
	    Relation relation = new Relation();
	    fillRelation(relation, collectionObjectCsid, CollectionObject.class.getSimpleName(),
	    		intakeCsid, Intake.class.getSimpleName(),
	    		RelationshipType.COLLECTIONOBJECT_INTAKE);
	    ClientResponse<Response> relationResponse = relationClient.createRelation(relation); 
	    Assert.assertEquals(relationResponse.getStatus(), Response.Status.CREATED.getStatusCode());
	    String relationCsid = extractId(relationResponse);
	    
	    //
	    // Now try to retrieve the Intake record of the CollectionObject.
	    //
	    String predicate = RelationshipType.COLLECTIONOBJECT_INTAKE.value();
	    ClientResponse<RelationList> resultResponse = relationClient.getRelationList_SPO(collectionObjectCsid,
	    		predicate,
	    		intakeCsid);
	    
	    //
	    // Each relation returned in the list needs to match what we
	    // requested.
	    //
        RelationList relationList = resultResponse.getEntity();
        List<RelationList.RelationListItem> relationListItems = relationList.getRelationListItem();
        ClientResponse<Relation> resultRelationResponse;
        Relation resultRelation = null;
        int i = 0;
        for(RelationList.RelationListItem listItem : relationListItems){
        	
        	String foundCsid = listItem.getCsid();
        	try {
	        	resultRelationResponse = relationClient.getRelation(foundCsid);
	        	resultRelation = resultRelationResponse.getEntity();
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
