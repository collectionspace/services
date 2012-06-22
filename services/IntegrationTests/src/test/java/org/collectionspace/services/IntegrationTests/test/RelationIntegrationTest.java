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

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.DimensionClient;
import org.collectionspace.services.client.DimensionFactory;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;

import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.intake.IntakesCommon;

import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.dimension.DimensionsCommon;
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
	private DimensionClient dimensionClient = new DimensionClient();
	
	private static final int OBJECTS_TO_INTAKE = 1;
	
	
    /**
     * Object as xml string.
     *
     * @param o the o
     * @param clazz the clazz
     * @return the string
     */
    static protected String objectAsXmlString(Object o, Class<?> clazz) {
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, sw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }
	
    private PoxPayloadOut createDimensionInstance(String commonPartName, String dimensionType, String dimensionValue, String entryDate) {
        DimensionsCommon dimensionsCommon = new DimensionsCommon();
        dimensionsCommon.setDimension(dimensionType);
        dimensionsCommon.setValue(new BigDecimal(dimensionValue));
        dimensionsCommon.setValueDate(entryDate);
        PoxPayloadOut multipart = DimensionFactory.createDimensionInstance(
                commonPartName, dimensionsCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, dimension common");
            logger.debug(objectAsXmlString(dimensionsCommon,
                    DimensionsCommon.class));
        }

        return multipart;
    }
	
    protected PoxPayloadOut createDimensionInstance(String identifier) {
    	DimensionClient client = new DimensionClient();
    	return createDimensionInstance(client.getCommonPartName(), identifier);
    }
    
    /**
     * Creates the dimension instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    protected PoxPayloadOut createDimensionInstance(String commonPartName, String identifier) {
        final String dimensionValue = Long.toString(System.currentTimeMillis());
    	
        return createDimensionInstance(commonPartName, 
                "dimensionType-" + identifier,
                dimensionValue,
                "entryDate-" + identifier);
    }
    
	@Test void deleteCollectionObjectRelationshipToLockedDimension() {
		// First create a CollectionObject
		CollectionobjectsCommon co = new CollectionobjectsCommon();
		fillCollectionObject(co, createIdentifier());		
		PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
		PayloadOutputPart commonPart = multipart.addPart(collectionObjectClient.getCommonPartName(), co);
		
		// Make the "create" call and check the response
		ClientResponse<Response> response = collectionObjectClient.create(multipart);
		String collectionObjectCsid = null;
		try {
			Assert.assertEquals(response.getStatus(), Response.Status.CREATED
					.getStatusCode());
			collectionObjectCsid = extractId(response);
		} finally {
			response.releaseConnection();
		}
		
		// Create a new dimension record
	    multipart = this.createDimensionInstance(createIdentifier());
	    // Make the call to create and check the response
	    response = dimensionClient.create(multipart);
	    String dimensionCsid1 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    dimensionCsid1 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    // Relate the entities, by creating a new relation object
	    RelationsCommon relation = new RelationsCommon();
	    fillRelation(relation, collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(),
	    		dimensionCsid1, DimensionsCommon.class.getSimpleName(),
	    		"collectionobject-dimension");
	    // Create the part and fill it with the relation object
	    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(relationClient.getCommonPartName(), relation);
	    // Create the relationship
	    response = relationClient.create(multipart);
	    String relationCsid1 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    relationCsid1 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }	    
	    
	    // Now lock the dimension record.
	    
		@SuppressWarnings("unused")
		ClientResponse<String> workflowResponse = dimensionClient.updateWorkflowWithTransition(dimensionCsid1, WorkflowClient.WORKFLOWTRANSITION_LOCK);
	    System.out.println("Locked dimension record with CSID=" + dimensionCsid1);
	    
	    // Finally, try to delete the relationship
	    
	    // Try to delete the relationship -should fail because we don't allow delete if one of the sides is locked.
	    response = relationClient.delete(relationCsid1);
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    // Also, try to soft-delete.  This should also fail.
		workflowResponse = dimensionClient.updateWorkflowWithTransition(dimensionCsid1, WorkflowClient.WORKFLOWTRANSITION_DELETE);
	    System.out.println("Locked dimension record with CSID=" + dimensionCsid1);
	}
	
	@Test void createCollectionObjectRelationshipToManyDimensions() {
		//
		// First create a CollectionObject
		//
		CollectionobjectsCommon co = new CollectionobjectsCommon();
		fillCollectionObject(co, createIdentifier());		
		PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
		PayloadOutputPart commonPart = multipart.addPart(collectionObjectClient.getCommonPartName(), co);
		
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
		
		//Next, create the first of three Dimension records to relate the collection object record
	    multipart = this.createDimensionInstance(createIdentifier());
	    // Make the call to create and check the response
	    response = dimensionClient.create(multipart);
	    String dimensionCsid1 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    dimensionCsid1 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
		//Next, create a the second Dimension record
	    multipart = this.createDimensionInstance(createIdentifier());
	    // Make the call to create and check the response
	    response = dimensionClient.create(multipart);
	    String dimensionCsid2 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    dimensionCsid2 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
		//Next, create a the 3rd Dimension record
	    multipart = this.createDimensionInstance(createIdentifier());
	    // Make the call to create and check the response
	    response = dimensionClient.create(multipart);
	    String dimensionCsid3 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    dimensionCsid3 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    // Relate the entities, by creating a new relation object
	    RelationsCommon relation = new RelationsCommon();
	    fillRelation(relation,
	    		collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(), // the subject of the relationship
	    		dimensionCsid1, DimensionsCommon.class.getSimpleName(), // the object of the relationship
	    		"collectionobject-dimension");
	    // Create the part and fill it with the relation object
	    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(relationClient.getCommonPartName(), relation);
	    // Create the relationship
	    response = relationClient.create(multipart);
	    String relationCsid1 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    relationCsid1 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    // Wait 1 second and create the second relationship
	    try {
			Thread.sleep(1001);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    relation = new RelationsCommon();
	    fillRelation(relation,
	    		collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(), // the subject of the relationship
	    		dimensionCsid2, DimensionsCommon.class.getSimpleName(), // the object of the relationship
	    		"collectionobject-dimension");
	    // Create the part and fill it with the relation object
	    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(relationClient.getCommonPartName(), relation);
	    // Create the relationship
	    response = relationClient.create(multipart);
	    @SuppressWarnings("unused")
		String relationCsid2 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    relationCsid2 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    // Wait 1 second and create the 3rd relationship
	    try {
			Thread.sleep(1001);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    relation = new RelationsCommon();
	    fillRelation(relation,
	    		collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(), // the subject of the relationship
	    		dimensionCsid3, DimensionsCommon.class.getSimpleName(), // the object of the relationship
	    		"collectionobject-dimension");
	    // Create the part and fill it with the relation object
	    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(relationClient.getCommonPartName(), relation);
	    // Create the relationship
	    response = relationClient.create(multipart);
	    @SuppressWarnings("unused")
		String relationCsid3 = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    relationCsid3 = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }	    	    
	}    
	
	@Test void releteCollectionObjectToLockedDimension() {
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
		
		//Next, create a Dimension record to relate the collection object to
	    multipart = this.createDimensionInstance(createIdentifier());
	    // Make the call to create and check the response
	    response = dimensionClient.create(multipart);
	    String dimensionCsid = null;
	    try {
		    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
		    dimensionCsid = extractId(response);
	    } finally {
	    	response.releaseConnection();
	    }
	    
	    @SuppressWarnings("unused")
		ClientResponse<String> workflowResponse = dimensionClient.updateWorkflowWithTransition(dimensionCsid, WorkflowClient.WORKFLOWTRANSITION_LOCK);
	    System.out.println("Locked dimension record with CSID=" + dimensionCsid);
	    
	    // Lastly, relate the two entities, by creating a new relation object
	    RelationsCommon relation = new RelationsCommon();
	    fillRelation(relation, collectionObjectCsid, CollectionobjectsCommon.class.getSimpleName(),
	    		dimensionCsid, DimensionsCommon.class.getSimpleName(),
	    		"collectionobject-dimension");
	    // Create the part and fill it with the relation object
	    multipart = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
	    commonPart = multipart.addPart(relation, MediaType.APPLICATION_XML_TYPE);
	    commonPart.setLabel(relationClient.getCommonPartName());

	    // Make the call to crate
	    ClientResponse<Response> relationresponse = relationClient.create(multipart);
	    @SuppressWarnings("unused")
		String relationCsid = null;
	    try {
		    Assert.assertEquals(relationresponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		    relationCsid = extractId(response);
	    } finally {
	    	relationresponse.releaseConnection();
	    }
	    
	}
	
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
