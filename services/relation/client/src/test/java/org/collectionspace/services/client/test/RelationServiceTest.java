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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationshipType;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RelationServiceTest, carries out tests against a
 * deployed and running Relation Service.
 * 
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class RelationServiceTest extends AbstractPoxServiceTestImpl<RelationsCommonList, RelationsCommon> {

   /** The logger. */
    private final String CLASS_NAME = RelationServiceTest.class.getName();
    private final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private List<String> personIdsCreated = new ArrayList<String>();
    
    private static final String UNINITIALIZED_CSID = "-1";
    private static final String UNINITIALIZED_REFNAME = "null";
    
    private static final String PERSONS_DOCUMENT_TYPE = "Person";
    private String samSubjectPersonCSID = UNINITIALIZED_CSID;
    private String oliveObjectPersonCSID = UNINITIALIZED_REFNAME;
    private String samSubjectRefName = UNINITIALIZED_CSID;
    private String oliveObjectRefName = UNINITIALIZED_REFNAME;
    
    private String personAuthCSID = null;
    private String personShortId = PERSON_AUTHORITY_NAME;
    

    /** The SERVICE path component. */
    final String SERVICE_PATH_COMPONENT = "relations";
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new RelationClient();
    }
     
    protected Class<RelationsCommonList> getCommonListType() {
    	return (Class<RelationsCommonList>)RelationsCommonList.class;
    }
        
    /**
     * Creates the person refs as a precondition for running the tests in this class.
     */
    @BeforeSuite
    private void createPersonRefs() {
        setupCreate();

        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
                PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        personAuthCSID = extractId(res);

        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);

        String csid = createPerson("Sam", "Subject", "samSubject", authRefName);
        Assert.assertNotNull(csid);
        samSubjectPersonCSID = csid;
        samSubjectRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        Assert.assertNotNull(samSubjectRefName);
        personIdsCreated.add(csid);

        csid = createPerson("Olive", "Object", "oliveObject", authRefName);
        Assert.assertNotNull(csid);
        oliveObjectRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        oliveObjectPersonCSID = csid;
        Assert.assertNotNull(oliveObjectRefName);
        personIdsCreated.add(csid);
    }
    
    @AfterSuite
    private void deletePersonRefs() {
    	//
    	// Delete all the persons we created for the tests
    	//
    }

    private String createPerson(String firstName, String surName, String shortId, String authRefName) {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String, String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        PoxPayloadOut multipart =
                PersonAuthorityClientUtils.createPersonInstance(personAuthCSID,
                authRefName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        return extractId(res);
    }    
    
    @Test(dataProvider="testName",
            dependsOnMethods = {"create"})
    public void createWithSelfRelation(String testName) throws Exception {
    	// Test result codes setup
        setupCreateWithInvalidBody();

        // Submit the request to the service and store the response.
        RelationClient client = new RelationClient();
        String identifier = createIdentifier();
        RelationsCommon relationsCommon = createRelationsCommon(identifier);
        // Make the subject ID equal to the object ID
        relationsCommon.setSubjectCsid(relationsCommon.getObjectCsid());
        PoxPayloadOut multipart = createRelationInstance(relationsCommon);
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_BAD_REQUEST);   //should be an error: same objectID and subjectID are not allowed by validator.
    }
       
    /**
     * This method is called by the base class method (test) readList().
     * @param testName
     * @param list
     */
    @Override
    protected void printList(String testName, RelationsCommonList list) {
        List<RelationsCommonList.RelationListItem> items =
                list.getRelationListItem();
        int i = 0;
        for(RelationsCommonList.RelationListItem item : items){
            logger.debug(testName + ": list-item[" + i + "] csid=" +
                    item.getCsid());
            logger.debug(testName + ": list-item[" + i + "] URI=" +
                    item.getUri());
            i++;
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private RelationsCommon createRelationsCommon(String identifier) {
        RelationsCommon relationCommon = new RelationsCommon();
        fillRelation(relationCommon, identifier);
        return relationCommon;
    }

    private PoxPayloadOut createRelationInstance(RelationsCommon relation) {
        PoxPayloadOut result = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
        	result.addPart(new RelationClient().getCommonPartName(), relation);
        if(logger.isDebugEnabled()){
          logger.debug("to be created, relation common");
          logger.debug(objectAsXmlString(relation, RelationsCommon.class));
        }
        return result;
    }
    
    /**
     * Creates the relation instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createRelationInstance(String identifier) {
        RelationsCommon relation = createRelationsCommon(identifier);
        PoxPayloadOut result = createRelationInstance(relation);
        return result;
    }

    /**
     * Fills the relation.
     *
     * @param relationCommon the relation
     * @param identifier the identifier
     */
    private void fillRelation(RelationsCommon relationCommon, String identifier) {
        fillRelation(relationCommon, samSubjectPersonCSID, null, oliveObjectPersonCSID, null,
                RelationshipType.COLLECTIONOBJECT_INTAKE.toString(),
                RelationshipType.COLLECTIONOBJECT_INTAKE + ".displayName-" + identifier);
    }

    /**
     * Fills the relation.
     *
     * @param relationCommon the relation
     * @param subjectCsid the subject document id
     * @param subjectDocumentType the subject document type
     * @param objectCsid the object document id
     * @param objectDocumentType the object document type
     * @param rt the rt
     */
    private void fillRelation(RelationsCommon relationCommon,
            String subjectCsid, String subjectDocumentType,
            String objectCsid, String objectDocumentType,
            String rt,
            String rtDisplayName) {
        relationCommon.setSubjectCsid(subjectCsid);
        relationCommon.setSubjectDocumentType(subjectDocumentType);
        relationCommon.setObjectCsid(objectCsid);
        relationCommon.setObjectDocumentType(objectDocumentType);

        relationCommon.setRelationshipType(rt);
        relationCommon.setPredicateDisplayName(rtDisplayName);
    }

	@Override
	protected String getServiceName() {
		return RelationClient.SERVICE_NAME;
	}

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		return createRelationInstance(identifier);
	}

	@Override
	protected RelationsCommon updateInstance(RelationsCommon relationCommon) {
		RelationsCommon result = new RelationsCommon();
		        
        // Update the content of this resource, inverting subject and object
        result.setSubjectCsid(relationCommon.getObjectCsid());
        result.setSubjectDocumentType("Hooey"); // DocumentType changes should be ignored.
        result.setObjectCsid(relationCommon.getSubjectCsid());
        result.setObjectDocumentType("Fooey"); // DocumentType changes should be ignored.
        result.setPredicateDisplayName("updated-" + relationCommon.getPredicateDisplayName());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(RelationsCommon original,
			RelationsCommon updated) throws Exception {
        final String msg =
                "Data in updated object did not match submitted data.";
        final String msg2 =
                "Data in updated object was not correctly computed.";
        Assert.assertEquals(
        		updated.getSubjectCsid(), original.getSubjectCsid(), msg);
        Assert.assertEquals(
        		updated.getSubjectDocumentType(), PERSONS_DOCUMENT_TYPE, msg2); // DocumentType changes should have been ignored.
        Assert.assertEquals(
        		updated.getObjectCsid(), original.getObjectCsid(), msg);
        Assert.assertEquals(
        		updated.getObjectDocumentType(), PERSONS_DOCUMENT_TYPE, msg2); // DocumentType changes should have been ignored.
        Assert.assertEquals(
        		updated.getPredicateDisplayName(), original.getPredicateDisplayName(), msg);
	}
}
