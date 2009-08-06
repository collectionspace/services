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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.intake.IntakeList;
import org.collectionspace.services.client.IntakeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntakeServiceTest, carries out tests against a
 * deployed and running Intake Service.
 * 
 * $LastChangedRevision: 511 $
 * $LastChangedDate: 2009-08-06 20:16:16 +0000 (Thu, 06 Aug 2009) $
 */
public class IntakeServiceTest {

  private IntakeClient intakeClient = new IntakeClient();
  private String knownIntakeId = null;
  private final String NON_EXISTENT_ID = createNonExistentIdentifier();
  final Logger logger = LoggerFactory.getLogger(IntakeServiceTest.class);

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
   * Tests creation of a new Intake.
   *
   * Expected status code: 201 Created
   *
   * Also expected: The 'Location' header contains the URL for the newly created object.
   * This is required by the extractId() utility method, below.
   *
   * The newly-created Intake is also used by other test(s)
   * (e.g. update, delete) which follow, below.
   */
  @Test
  public void createIntake() {
    String identifier = this.createIdentifier();

    Intake intake = createIntake(identifier);
    ClientResponse<Response> res = intakeClient.createIntake(intake);
    verbose("createIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());

    // Store the ID returned from this create operation for additional tests below.
    knownIntakeId = extractId(res);
  }

  /**
   * Creates two or more new Intakes.
   *
   * Repeatedly calls the createIntake test, above, and relies on its
   * test assertions.
   *
   * Expected status code: 201 Created
   *
   * The newly-created Intakes are also used by other test(s)
   * (e.g. read multiple/list) which follow, below.
   */
  @Test(dependsOnMethods = {"createIntake"})
  public void createCollection() {
    for(int i = 0; i < 3; i++){
      this.createIntake();
    }
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests creation of a Intake by sending a null to the client proxy.
   *
   * Expected status code: (none)
   *
   * Expected result: IllegalArgumentException 
   * (Make sure this is a reported exception in the called class.)
   */
  @Test(dependsOnMethods = {"createIntake"}, expectedExceptions = IllegalArgumentException.class)
  public void createNullIntake() {
    ClientResponse<Response> res = intakeClient.createIntake(null);
  }
		
  /**
   * Tests creation of an Intake by sending bad data
   * (e.g. in a format that doesn't match the Intake schema)
   * in the entity body of the request.
   *
   * Expected status code: 400 Bad Request
   */
/*
  @Test(dependsOnMethods = {"createIntake"})
  public void createIntakeWithBadData() {
    // Currently only a stub.
  }
*/

  /**
   * Tests creation of an Intake by a user who
   * is not authorized to perform this action.
   *
   * Expected status code: 403 Forbidden
   */
/*
  @Test(dependsOnMethods = {"createIntake"})
  public void createIntakeWithUnauthorizedUser() {
    // Currently only a stub.
  }
*/

  /**
   * Tests creation of a duplicate Intake, whose unique resource identifier
   * duplicates that of an existing Intake.
   * 
   * Expected status code: 409 Conflict
   */
/*
  @Test(dependsOnMethods = {"createIntake"})
  public void createDuplicateIntake() {
    Intake intake = createIntake(knownIntakeId);
    ClientResponse<Response> res = 
      intakeClient.createIntake(intake);
    verbose("createDuplicateIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.CONFLICT.getStatusCode());
  }
*/

  // ---------------------------------------------------------------
  // CRUD tests : READ tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------
  
  /**
   * Tests reading (i.e. retrieval) of a Intake.
   *
   * Expected status code: 200 OK
   */
  @Test(dependsOnMethods = {"createIntake"})
  public void getIntake() {
    ClientResponse<Intake> res = 
      intakeClient.getIntake(knownIntakeId);
    verbose("getIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a Intake by a user who
   * is not authorized to perform this action.
   *
   * Expected status code: 403 Forbidden
   */
/*
  @Test(dependsOnMethods = {"getIntake"})
  public void getIntakeWithUnauthorizedUser() {
    // Currently only a stub
  }
*/

  /**
   * Tests reading (i.e. retrieval) of a non-existent Intake,
   * whose resource identifier does not exist at the specified URL.
   *
   * Expected status code: 404 Not Found
   */
  @Test(dependsOnMethods = {"getIntake"})
  public void getNonExistentIntake() {
    ClientResponse<Intake> res = 
      intakeClient.getIntake(NON_EXISTENT_ID);
    verbose("getNonExistentIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
  }


  // ---------------------------------------------------------------
  // CRUD tests : READ (list, or multiple) tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a list of multiple Intakes.
   *
   * Expected status code: 200 OK
   *
   * Also expected: The entity body in the response contains
   * a representation of the list of Intakes.
   */
  @Test(dependsOnMethods = {"createCollection"})
  public void getIntakeList() {
    // The resource method is expected to return at least an empty list
    ClientResponse<IntakeList> res = intakeClient.getIntakeList();
    IntakeList coList = res.getEntity();
    verbose("getIntakeList: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());

    List<IntakeList.IntakeListItem> coItemList =
      coList.getIntakeListItem();
    int i = 0;
    for(IntakeList.IntakeListItem pli : coItemList){
      verbose("getIntakeList: list-item[" + i + "] csid=" + pli.getCsid());
      verbose("getIntakeList: list-item[" + i + "] entryNumber=" + pli.getEntryNumber());
      verbose("getIntakeList: list-item[" + i + "] URI=" + pli.getUri());
      i++;
    }
  }

  /**
   * Tests reading (i.e. retrieval) of a list of multiple Intakes
   * when the contents of the list are expected to be empty.
   *
   * Expected status code: 200 OK
   * (Note: *not* 204 No Content)
   *
   * Also expected: The entity body in the response contains
   * a representation of an empty list of Intakes.
   */
/*
  @Test(dependsOnMethods = {"getIntakeList"})
  public void getIntakeEmptyList() {
    // Currently only a stub.
  }
*/
  
  // Failure outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a list of Intakes
   * when sending unrecognized query parameters with the request.
   *
   * Expected status code: 400 Bad Request
   */
/*
  @Test(dependsOnMethods = {"getIntakeList"})
  public void getIntakeListWithBadParams() {
    // Currently only a stub.
  }
*/

  /**
   * Tests reading (i.e. retrieval) of a list of Intakes by a user who
   * is not authorized to perform this action.
   *
   * Expected status code: 403 Forbidden
   */
/*
  @Test(dependsOnMethods = {"getIntakeList"})
  public void getIntakeListWithUnauthorizedUser() {
    // Currently only a stub.
  }
*/

  

  // ---------------------------------------------------------------
  // CRUD tests : UPDATE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests updating the content of a Intake.
   *
   * Expected status code: 200 OK
   *
   * Also expected: The entity body in the response contains
   * a representation of the updated Intake.
   */
  @Test(dependsOnMethods = {"createIntake"})
  public void updateIntake() {
    ClientResponse<Intake> res = 
      intakeClient.getIntake(knownIntakeId);
    verbose("getIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
    Intake intake = res.getEntity();
    verbose("Got Intake to update with ID: " + knownIntakeId,
        intake, Intake.class);

    //intake.setCsid("updated-" + knownIntakeId);
    intake.setEntryNumber("updated-" + intake.getEntryNumber());
    intake.setEntryDate("updated-" + intake.getEntryDate());
    
    // make call to update service
    res = 
      intakeClient.updateIntake(knownIntakeId, intake);
    verbose("updateIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
    
    // check the response
    Intake updatedIntake = res.getEntity();
    Assert.assertEquals(updatedIntake.getEntryDate(), intake.getEntryDate());
    verbose("updateIntake ", updatedIntake, Intake.class);
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests updating the content of a Intake by sending bad data
   * (e.g. in a format that doesn't match the Intake schema)
   * in the entity body of the request.
   *
   * Expected status code: 400 Bad Request
   */
/*
  @Test(dependsOnMethods = {"updateIntake"})
  public void updateIntakeWithBadData() {
    // Currently only a stub.
  }
*/

  /**
   * Tests updating the content of a Intake by a user who
   * is not authorized to perform this action.
   *
   * Expected status code: 403 Forbidden
   */
/*
  @Test(dependsOnMethods = {"updateIntake"})
  public void updateIntakeWithUnauthorizedUser() {
    // Currently only a stub.
  }
*/

  /**
   * Tests updating the content of a non-existent Intake, whose
   * resource identifier does not exist.
   *
   * Expected status code: 404 Not Found
   */
  @Test(dependsOnMethods = {"updateIntake"})
  public void updateNonExistentIntake() {
    // Note: The ID used in this 'create' call may be arbitrary.
    // The only relevant ID may be the one used in updateIntake(), below.
    Intake intake = createIntake(NON_EXISTENT_ID);
    // make call to update service
    ClientResponse<Intake> res =
      intakeClient.updateIntake(NON_EXISTENT_ID, intake);
    verbose("createIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
  }


  // ---------------------------------------------------------------
  // CRUD tests : DELETE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests deleting a Intake.
   *
   * Expected status code: 200 OK
   */
  @Test(dependsOnMethods = {"createIntake", "getIntake"})
  public void deleteIntake() {
    verbose("Calling deleteIntake: " + knownIntakeId);
    ClientResponse<Response> res = intakeClient.deleteIntake(knownIntakeId);
    verbose("deleteIntake csid=" + knownIntakeId);
    verbose("deleteIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests deleting a Intake by a user who
   * is not authorized to perform this action.
   *
   * Expected status code: 403 Forbidden
   */
/*
  @Test(dependsOnMethods = {"deleteIntake"})
  public void deleteIntakeWithUnauthorizedUser() {
    // Currently only a stub.
  }
*/

  /**
   * Tests deleting a non-existent Intake, whose
   * resource identifier does not exist at the specified URL.
   *
   * Expected status code: 404 Not Found
   */
  @Test(dependsOnMethods = {"deleteIntake"})
  public void deleteNonExistentIntake() {
    verbose("Calling deleteIntake: " + NON_EXISTENT_ID);
    ClientResponse<Response> res =
      intakeClient.deleteIntake(NON_EXISTENT_ID);
    verbose("deleteIntake: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
  }


  // ---------------------------------------------------------------
  // Utility methods used by tests above
  // ---------------------------------------------------------------

  private Intake createIntake(String identifier) {
    Intake intake = createIntake("entryNumber-" + identifier,
        "entryDate-" + identifier);

    return intake;
  }

  private Intake createIntake(String entryNumber, String entryDate) {
    Intake intake = new Intake();

    intake.setEntryNumber(entryNumber);
    intake.setEntryDate(entryDate);

    return intake;
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
