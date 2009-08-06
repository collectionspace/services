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
 
package org.collectionspace.services.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;
import org.collectionspace.services.client.CollectionObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectServiceTest, carries out tests against a
 * deployed and running CollectionObject Service.
 * 
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class CollectionObjectServiceTest {

  private CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
  private String knownCollectionObjectId = null;
  private final String NON_EXISTENT_ID = createNonExistentIdentifier();
  final Logger logger = LoggerFactory.getLogger(CollectionObjectServiceTest.class);
  
  
  // ---------------------------------------------------------------
  // Service Discovery tests
  // ---------------------------------------------------------------

  // TBA
  
  
  // ---------------------------------------------------------------
  // CRUD tests : CREATE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------
  
  /**
   * Tests creation of a new CollectionObject.
   *
   * Expected status code: 200 OK
   *
   * Also expected: The 'Location' header contains the URL for the newly created object.
   * This is required by the extractId() utility method, below.
   *
   * The newly-created CollectionObject is also used by other test(s)
   * (e.g. update, delete) which follow, below.
   */
  @Test
  public void createCollectionObject() {
    String identifier = this.createIdentifier();

    CollectionObject collectionObject = createCollectionObject(identifier);
    ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
    verbose("createCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());

    // Store the ID returned from this create operation for additional tests below.
    knownCollectionObjectId = extractId(res);
  }

  /**
   * Tests creation of two or more new CollectionObjects.
   *
   * Expected status code: 200 OK
   *
   * The newly-created CollectionObjects are also used by other test(s)
   * (e.g. read multiple/list) which follow, below.
   */
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void createCollection() {
    for(int i = 0; i < 3; i++){
      this.createCollectionObject();
    }
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests creation of a CollectionObject by sending a null to the client proxy.
   *
   * Expected status code: (none)
   *
   * Expected result: IllegalArgumentException 
   * (Make sure this is a reported exception in the called class.)
   */
  @Test(dependsOnMethods = {"createCollectionObject"}, expectedExceptions = IllegalArgumentException.class)
  public void createNullCollectionObject() {
    ClientResponse<Response> res = collectionObjectClient.createCollectionObject(null);
  }
		
  /**
   * Tests creation of a CollectionObject by sending data in the wrong format
   * (i.e. any format that doesn't match the CollectionObject schema) in the
   * entity body of the request.
   *
   * Expected status code: 400 Bad Request
   */
/*
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void createCollectionObjectWithWrongDataFormat() {
    // Currently only a stub
  }
*/
  
  /**
   * Tests creation of a duplicate CollectionObject, whose unique resource identifier
   * duplicates that of an existing CollectionObject.
   * 
   * Expected status code: 409 Conflict
   */
/*
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void createDuplicateCollectionObject() {
    CollectionObject collectionObject = createCollectionObject(knownCollectionObjectId);
    ClientResponse<Response> res = 
      collectionObjectClient.createCollectionObject(collectionObject);
    verbose("createDuplicateCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.CONFLICT.getStatusCode());
  }
*/

  // ---------------------------------------------------------------
  // CRUD tests : READ tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------
  
  /**
   * Tests reading (i.e. retrieval) of a CollectionObject.
   *
   * Expected status code: 200 OK
   */
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void getCollectionObject() {
    ClientResponse<CollectionObject> res = 
      collectionObjectClient.getCollectionObject(knownCollectionObjectId);
    verbose("getCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a non-existent CollectionObject,
   * whose resource identifier does not exist at the specified URL.
   *
   * Expected status code: 404 Not Found
   */
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void getNonExistentCollectionObject() {
    ClientResponse<CollectionObject> res = 
      collectionObjectClient.getCollectionObject(NON_EXISTENT_ID);
    verbose("getNonExistentCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
  }

  // ---------------------------------------------------------------
  // CRUD tests : READ (list, or multiple) tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a list of multiple CollectionObjects.
   *
   * Expected status code: 200 OK
   *
   * Also expected: The entity body in the response contains
   * a representation of the list of CollectionObjects.
   */
  @Test(dependsOnMethods = {"createCollection"})
  public void getCollectionObjectList() {
    // The resource method is expected to return at least an empty list
    ClientResponse<CollectionObjectList> res = collectionObjectClient.getCollectionObjectList();
    CollectionObjectList coList = res.getEntity();
    verbose("getCollectionObjectList: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());

    List<CollectionObjectList.CollectionObjectListItem> coItemList =
      coList.getCollectionObjectListItem();
    int i = 0;
    for(CollectionObjectList.CollectionObjectListItem pli : coItemList){
      verbose("getCollectionObjectList: list-item[" + i + "] csid=" + pli.getCsid());
      verbose("getCollectionObjectList: list-item[" + i + "] objectNumber=" + pli.getObjectNumber());
      verbose("getCollectionObjectList: list-item[" + i + "] URI=" + pli.getUri());
      i++;
    }
  }

  /**
   * Tests reading (i.e. retrieval) of a list of multiple CollectionObjects
   * when the contents of the list are expected to be empty.
   *
   * Expected status code: 200 OK
   * (Note: *not* 204 No Content)
   *
   * Also expected: The entity body in the response contains
   * a representation of an empty list of CollectionObjects.
   */
/*
  @Test(dependsOnMethods = {"createCollection"})
  public void getCollectionObjectEmptyList() {
  }
*/

  // Failure outcomes
  // ----------------
  
  // None known at present.
  

  // ---------------------------------------------------------------
  // CRUD tests : UPDATE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests updating the content of a CollectionObject.
   *
   * Expected status code: 200 OK
   *
   * Also expected: The entity body in the response contains
   * a representation of the updated CollectionObject.
   */
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void updateCollectionObject() {
    ClientResponse<CollectionObject> res = 
      collectionObjectClient.getCollectionObject(knownCollectionObjectId);
    verbose("getCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
    CollectionObject collectionObject = res.getEntity();
    verbose("Got CollectionObject to update with ID: " + knownCollectionObjectId,
        collectionObject, CollectionObject.class);

    //collectionObject.setCsid("updated-" + knownCollectionObjectId);
    collectionObject.setObjectNumber("updated-" + collectionObject.getObjectNumber());
    collectionObject.setObjectName("updated-" + collectionObject.getObjectName());

    // make call to update service
    res = 
      collectionObjectClient.updateCollectionObject(knownCollectionObjectId, collectionObject);
    verbose("updateCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
    
    // check the response
    CollectionObject updatedCollectionObject = res.getEntity();
    Assert.assertEquals(updatedCollectionObject.getObjectName(), 
      collectionObject.getObjectName());
    verbose("updateCollectionObject: ", updatedCollectionObject, CollectionObject.class);
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests updating the content of a non-existent CollectionObject, whose
   * resource identifier does not exist.
   *
   * Expected status code: 404 Not Found
   */
  @Test(dependsOnMethods = {"updateCollectionObject"})
  public void updateNonExistentCollectionObject() {
    // Note: The ID used in this call may not be relevant, only the ID used
    // in updateCollectionObject(), below.
    CollectionObject collectionObject = createCollectionObject(NON_EXISTENT_ID);
    // make call to update service
    ClientResponse<CollectionObject> res =
      collectionObjectClient.updateCollectionObject(NON_EXISTENT_ID, collectionObject);
    verbose("createCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
  }


  // ---------------------------------------------------------------
  // CRUD tests : DELETE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests deleting a CollectionObject.
   *
   * Expected status code: 200 OK
   */
  @Test(dependsOnMethods = {"createCollectionObject"})
  public void deleteCollectionObject() {
    verbose("Calling deleteCollectionObject:" + knownCollectionObjectId);
    ClientResponse<Response> res = collectionObjectClient.deleteCollectionObject(knownCollectionObjectId);
    verbose("deleteCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests deleting a non-existent CollectionObject, whose
   * resource identifier does not exist at the specified URL.
   *
   * Expected status code: 404 Not Found
   */
  @Test(dependsOnMethods = {"deleteCollectionObject"})
  public void deleteNonExistentCollectionObject() {
    verbose("Calling deleteCollectionObject:" + NON_EXISTENT_ID);
    ClientResponse<Response> res =
      collectionObjectClient.deleteCollectionObject(NON_EXISTENT_ID);
    verbose("deleteCollectionObject: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
  }


  // ---------------------------------------------------------------
  // Utility methods used by tests above
  // ---------------------------------------------------------------

  private CollectionObject createCollectionObject(String identifier) {
    CollectionObject collectionObject = createCollectionObject("objectNumber-" + identifier,
        "objectName-" + identifier);

    return collectionObject;
  }

  private CollectionObject createCollectionObject(String objectNumber, String objectName) {
    CollectionObject collectionObject = new CollectionObject();

    collectionObject.setObjectNumber(objectNumber);
    collectionObject.setObjectName(objectName);

    return collectionObject;
  }

  private String extractId(ClientResponse<Response> res) {
    MultivaluedMap mvm = res.getMetadata();
    String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
    verbose("extractId:uri=" + uri);
    String[] segments = uri.split("/");
    String id = segments[segments.length - 1];
    verbose("id=" + id);
    return id;
  }

  private void verbose(String msg) {
//    if(logger.isInfoEnabled()){
//      logger.debug(msg);
//    }
    System.out.println(msg);
  }

  private void verbose(String msg, Object o, Class clazz) {
    try{
      verbose(msg);
      JAXBContext jc = JAXBContext.newInstance(clazz);
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
          Boolean.TRUE);
      m.marshal(o, System.out);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  private void verboseMap(MultivaluedMap map) {
    for(Object entry : map.entrySet()){
      MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
      verbose("  name=" + mentry.getKey() + " value=" + mentry.getValue());
    }
  }

  private String createIdentifier() {
    long identifier = System.currentTimeMillis();
    return Long.toString(identifier);
  }

  private String createNonExistentIdentifier() {
    return Long.toString(Long.MAX_VALUE);
  }
  
}
