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

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
// import org.jboss.resteasy.client.ClientRequest;
import org.collectionspace.services.client.TestServiceClient;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

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

  // Instance variables specific to this test.
  final Logger logger = LoggerFactory.getLogger(CollectionObjectServiceTest.class);
  private CollectionObjectClient client = new CollectionObjectClient();

  // Instance variables common to all entity service test classes.
  private String knownObjectId = null;
  private final String NON_EXISTENT_ID = createNonExistentIdentifier();
  private HttpClient httpClient = new HttpClient();
  private TestServiceClient serviceClient = new TestServiceClient();
 
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
   * Tests creation of a new resource of the specified type.
   *
   * The 'Location' header will contain the URL for the newly created object.
   * This is required by the extractId() utility method, below.
   *
   * The newly-created resource is also used by other test(s)
   * (e.g. update, delete) which follow, below.
   */
  @Test
  public void create() {

     // Expected status code: 201 Created
    final int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    // Submit the request to the service and store the response.
    String identifier = this.createIdentifier();
    CollectionObject collectionObject = createCollectionObject(identifier);
    ClientResponse<Response> res = client.createCollectionObject(collectionObject);
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("create: status = " + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    // Store the ID returned from this create operation for additional tests below.
    knownObjectId = extractId(res);
  }

  /**
   * Creates two or more new objects of the specified type.
   *
   * Repeatedly calls the create test, above, and relies on its
   * test assertions.
   *
   * The newly-created objects are also used by other test(s)
   * (e.g. read multiple/list) which follow, below.
   */
  @Test(dependsOnMethods = {"create"})
  public void createMultiple() {
    for(int i = 0; i < 3; i++){
      this.create();
    }
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests creation of a resource of the specified type by sending a null to the client proxy.
   *
   */
  @Test(dependsOnMethods = {"create"}, expectedExceptions = IllegalArgumentException.class)
  public void createNull() {

   // Expected result: IllegalArgumentException
    ClientResponse<Response> res = client.createCollectionObject(null);
  }
  
  /**
   * Tests creation of a resource of the specified type by sending malformed XML data
   * in the entity body of the request.
   */
/*
  @Test(dependsOnMethods = {"create", "testSubmitRequest"})
  public void createWithMalformedXml() {

    // Expected status code: 400 Bad Request
    final int EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    // @TODO This test is currently commented out, because it returns a
    // 500 Internal Server Error status code, rather than the expected status code.

    // Submit the request to the service and store the response.
    String url = getServiceRootURL();
    PostMethod method = new PostMethod(url);
    final String MALFORMED_XML_DATA =
      "<malformed_xml>wrong schema contents</malformed_xml"; // Note: intentionally missing bracket.
    StringRequestEntity entity = getXmlEntity(MALFORMED_XML_DATA);
    int statusCode = submitRequest(method, entity);
    
    // Check the status code of the response: does it match the expected response(s)?
    verbose("createWithMalformedXml url=" + url + " status=" + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }
*/

  /**
   * Tests creation of a resource of the specified type by sending data
   * in the wrong schema (e.g. in a format that doesn't match the object's schema)
   * in the entity body of the request.
    */
/*
  @Test(dependsOnMethods = {"create", "testSubmitRequest", "createWithMalformedXml"})
  public void createWithWrongSchema() {
  
    // Expected status code: 400 Bad Request
    final int EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    // @TODO This test is currently commented out, because it returns a
    // 500 Internal Server Error status code, rather than the expected status code.
   
    // Submit the request to the service and store the response.
    String url = getServiceRootURL();
    PostMethod method = new PostMethod(url);
    final String WRONG_SCHEMA_DATA = "<wrong_schema>wrong schema contents</wrong_schema>";
    StringRequestEntity entity = getXmlEntity(WRONG_SCHEMA_DATA);
    int statusCode = submitRequest(method, entity);
    
    // Check the status code of the response: does it match the expected response(s)?
    verbose("createWithWrongSchema url=" + url + " status=" + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }
*/  

  /**
   * Tests creation of a resource of the specified type,
   * by a user who is not authorized to perform this action.
   */
/*
  @Test(dependsOnMethods = {"create"})
  public void createWithoutAuthorization() {

    // Expected status code: 403 Forbidden
    final int EXPECTED_STATUS_CODE = Response.Status.FORBIDDEN.getStatusCode();
    
    // @TODO Currently only a stub.  This test can be implemented
    // when the service is revised to require authorization. 
  }
*/

  /**
   * Tests creation of a duplicate object of the specified type,
   * whose unique resource identifier duplicates that of an existing object.
   */
/*
  @Test(dependsOnMethods = {"create"})
  public void createDuplicate() {

    // Expected status code: 409 Conflict
    final int EXPECTED_STATUS_CODE = Response.Status.CONFLICT.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    
    // @TODO This test is currently commented out because our current
    // services do not appear to permit creation of duplicate records.
    // Please see below for more details.
    
    // Note: there doesn't appear to be a way to create a duplicate
    // resource (object) by POSTing:
    //    
    // 1. We can't POST to a specific resource by ID; that returns a
    // response with a 405 Method Not Allowed status code.
    //
    // 2. If we POST to the container in which new resources are created,
    // it doesn't appear that we can specify the CSID that the newly-created
    // resource (object) will receive.
    //
    // If the two points above are accurate, this test is thus unneeded, until
    // and unless, in our service(s), we begin detecting duplicates via a
    // technique that isn't dependent on CSIDs; for instance, by checking for
    // duplicate data in other information units (fields) whose values must be unique.
    // 
    // One possible example of the above: checking for duplicate Accession numbers
    // in the "Object entry" information unit in CollectionObject resources.
  }
*/

  // ---------------------------------------------------------------
  // CRUD tests : READ tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------
  
  /**
   * Tests reading (i.e. retrieval) of a resource of the specified type.
   */
  @Test(dependsOnMethods = {"create"})
  public void read() {

    // Expected status code: 200 OK
    final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

    // Submit the request to the service and store the response.
    ClientResponse<CollectionObject> res = 
      client.getCollectionObject(knownObjectId);
    int statusCode = res.getStatus();
      
    // Check the status code of the response: does it match the expected response(s)?
    verbose("read: status = " + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a resource of the specified type by a user who
   * is not authorized to perform this action.
   */
/*
  @Test(dependsOnMethods = {"read"})
  public void readWithoutAuthorization() {

    // Expected status code: 403 Forbidden
    final int EXPECTED_STATUS_CODE = Response.Status.FORBIDDEN.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;
    
    // @TODO Currently only a stub.  This test can be implemented
    // when the service is revised to require authorization. 
  }
*/

  /**
   * Tests reading (i.e. retrieval) of a non-existent object of the specified type,
   * whose resource identifier does not exist at the specified URL.
   */
  @Test(dependsOnMethods = {"read"})
  public void readNonExistent() {

    // Expected status code: 404 Not Found
    final int EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

    // Submit the request to the service and store the response.
    ClientResponse<CollectionObject> res = 
      client.getCollectionObject(NON_EXISTENT_ID);
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("readNonExistent: status = " + res.getStatus());
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }


  // ---------------------------------------------------------------
  // CRUD tests : READ (list, or multiple) tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a list of multiple objects of the specified type.
   *
   * Also expected: The entity body in the response contains
   * a representation of a list of objects of the specified type.
   */
  @Test(dependsOnMethods = {"createMultiple"})
  public void readList() {

    // Expected status code: 200 OK
    final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ_MULTIPLE;
  
    // Submit the request to the service and store the response.
    ClientResponse<CollectionObjectList> res = client.getCollectionObjectList();
    CollectionObjectList coList = res.getEntity();
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("readList: status = " + res.getStatus());
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    // Optionally output additional data about list members for debugging.
    boolean iterateThroughList = false;
    if (iterateThroughList && logger.isDebugEnabled()) {
      List<CollectionObjectList.CollectionObjectListItem> coItemList =
        coList.getCollectionObjectListItem();
      int i = 0;
      for(CollectionObjectList.CollectionObjectListItem pli : coItemList){
        verbose("readList: list-item[" + i + "] csid=" + pli.getCsid());
        verbose("readList: list-item[" + i + "] objectNumber=" + pli.getObjectNumber());
        verbose("readList: list-item[" + i + "] URI=" + pli.getUri());
        i++;
      }
    }
    
  }

  /**
   * Tests reading (i.e. retrieval) of a list of multiple objects of the specified type
   * when the contents of the list are expected to be empty.
   *
   * Also expected: The entity body in the response contains
   * a representation of an empty list of objects of the specified type.
   */
/*
  @Test(dependsOnMethods = {"readList"})
  public void readEmptyList() {

    // Expected status code: 200 OK
    // (NOTE: *not* 204 No Content)
    final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ_MULTIPLE;

    // @TODO Currently only a stub.  Consider how to implement this.
  }
*/
  
  // Failure outcomes
  // ----------------

  /**
   * Tests reading (i.e. retrieval) of a list of objects of the specified type
   * when sending unrecognized query parameters with the request.
   */
/*
  @Test(dependsOnMethods = {"readList"})
  public void readListWithBadParams() {

    // Expected status code: 400 Bad Request
    final int EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ_MULTIPLE;

    // @TODO This test is currently commented out, because it returns a
    // 200 OK status code, rather than the expected status code.
   
    // @TODO Another variant of this test should use a URL for the service
    // root that ends in a trailing slash.

    // Submit the request to the service and store the response.
    String url = getServiceRootURL() + "?param=nonexistent";
    GetMethod method = new GetMethod(url);
    int statusCode = submitRequest(method);
    
    // Check the status code of the response: does it match the expected response(s)?
    verbose("readListWithBadParams: url=" + url + " status=" + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }
*/

  /**
   * Tests reading (i.e. retrieval) of a list of objects of the specified type by a user who
   * is not authorized to perform this action.
   *
   */
/*
  @Test(dependsOnMethods = {"readList"})
  public void readListWithoutAuthorization() {

    // Expected status code: 403 Forbidden
    final int EXPECTED_STATUS_CODE = Response.Status.FORBIDDEN.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ_MULTIPLE;

    // @TODO Currently only a stub.  This test can be implemented
    // when the service is revised to require authorization. 
  }
*/
 

  // ---------------------------------------------------------------
  // CRUD tests : UPDATE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests updating the content of a resource of the specified type.
   *
   * Also expected: The entity body in the response contains
   * a representation of the updated object of the specified type.
   */
  @Test(dependsOnMethods = {"create"})
  public void update() {

    // Expected status code: 200 OK
    final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.UPDATE;

    // Retrieve an existing resource that we can update.
    ClientResponse<CollectionObject> res = 
      client.getCollectionObject(knownObjectId);
    verbose("read: status = " + res.getStatus());
    Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);
    CollectionObject collectionObject = res.getEntity();
    verbose("Got object to update with ID: " + knownObjectId,
        collectionObject, CollectionObject.class);

    // Update the content of this resource.
    //collectionObject.setCsid("updated-" + knownObjectId);
    collectionObject.setObjectNumber("updated-" + collectionObject.getObjectNumber());
    collectionObject.setObjectName("updated-" + collectionObject.getObjectName());

    // Submit the request to the service and store the response.
    res = client.updateCollectionObject(knownObjectId, collectionObject);
    int statusCode = res.getStatus();
    CollectionObject updatedCollectionObject = res.getEntity();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("update: status = " + res.getStatus());
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    
    // Check the contents of the response: does it match what was submitted?
    verbose("update: ", updatedCollectionObject, CollectionObject.class);
    Assert.assertEquals(updatedCollectionObject.getObjectName(), 
      collectionObject.getObjectName(), "Data in updated object did not match submitted data.");
  }

  /**
   * Tests updating the content of a resource of the specified type
   * by sending malformed XML data in the entity body of the request.
   */
/*
  @Test(dependsOnMethods = {"create", "testSubmitRequest"})
  public void updateWithMalformedXml() {

    // Expected status code: 400 Bad Request
    final int EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.UPDATE;

    // @TODO This test is currently commented out, because it returns a
    // 500 Internal Server Error status code, rather than the expected status code.

    // Submit the request to the service and store the response.
    String url = getResourceURL(knownObjectId);
    PutMethod method = new PutMethod(url);
    final String MALFORMED_XML_DATA =
      "<malformed_xml>wrong schema contents</malformed_xml"; // Note: intentionally missing bracket.
    StringRequestEntity entity = getXmlEntity(MALFORMED_XML_DATA);
    int statusCode = submitRequest(method, entity);
    
    // Check the status code of the response: does it match the expected response(s)?
    verbose("updateWithMalformedXml: url=" + url + " status=" + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }
*/

  /**
   * Tests updating the content of a resource of the specified type
   * by sending data in the wrong schema (e.g. in a format that
   * doesn't match the object's schema) in the entity body of the request.
   */
/*
  @Test(dependsOnMethods = {"create", "testSubmitRequest", "createWithMalformedXml"})
  public void updateWithWrongSchema() {
  
    // Expected status code: 400 Bad Request
    final int EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.UPDATE;
    
    // @TODO This test is currently commented out, because it returns a
    // 500 Internal Server Error status code, rather than the expected status code.

    // Submit the request to the service and store the response.
    String url = getResourceURL(knownObjectId);
    PutMethod method = new PutMethod(url);
    final String WRONG_SCHEMA_DATA = "<wrong_schema>wrong schema contents</wrong_schema>";
    StringRequestEntity entity = getXmlEntity(WRONG_SCHEMA_DATA);
    int statusCode = submitRequest(method, entity);
    
    // Check the status code of the response: does it match the expected response(s)?
    verbose("updateWithWrongSchema: url=" + url + " status=" + statusCode);
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }
*/

  /**
   * Tests updating the content of a resource of the specified type,
   * by a user who is not authorized to perform this action.
   */
/*
  @Test(dependsOnMethods = {"update"})
  public void updateWithoutAuthorization() {

    // Expected status code: 403 Forbidden
    final int EXPECTED_STATUS_CODE = Response.Status.FORBIDDEN.getStatusCode();

    // @TODO Currently only a stub.  This test can be implemented
    // when the service is revised to require authorization. 
  }
*/

  /**
   * Tests updating the content of a non-existent object of the specified type,
   * whose resource identifier does not exist.
   */
  @Test(dependsOnMethods = {"update"})
  public void updateNonExistent() {

    // Expected status code: 404 Not Found
    final int EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.UPDATE;

    // Submit the request to the service and store the response.
    // Note: The ID used in this 'create' call may be arbitrary.
    // The only relevant ID may be the one used in updateCollectionObject(), below.
    CollectionObject collectionObject = createCollectionObject(NON_EXISTENT_ID);
    ClientResponse<CollectionObject> res =
      client.updateCollectionObject(NON_EXISTENT_ID, collectionObject);
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("updateNonExistent: status = " + res.getStatus());
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }


  // ---------------------------------------------------------------
  // CRUD tests : DELETE tests
  // ---------------------------------------------------------------

  // Success outcomes
  // ----------------

  /**
   * Tests deleting an object of the specified type.
   *
   * Expected status code: 200 OK
   */
  @Test(dependsOnMethods = 
    {"create", "read", "testSubmitRequest", "update"})
  public void delete() {

    // Expected status code: 200 OK
    final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

    // Submit the request to the service and store the response.
    ClientResponse<Response> res = client.deleteCollectionObject(knownObjectId);
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("delete: status = " + res.getStatus());
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }

  // Failure outcomes
  // ----------------

  /**
   * Tests deleting an object of the specified type,
   * by a user who is not authorized to perform this action.
   */
/*
  @Test(dependsOnMethods = {"delete"})
  public void deleteWithoutAuthorization() {

    // Expected status code: 403 Forbidden
    final int EXPECTED_STATUS_CODE = Response.Status.FORBIDDEN.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

    // @TODO Currently only a stub.  This test can be implemented
    // when the service is revised to require authorization. 
  }
*/

  /**
   * Tests deleting a non-existent object of the specified type,
   * whose resource identifier does not exist at the specified URL.
   */
  @Test(dependsOnMethods = {"delete"})
  public void deleteNonExistent() {

    // Expected status code: 404 Not Found
    final int EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();

    // Type of service request being tested
    final ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

    // Submit the request to the service and store the response.
    ClientResponse<Response> res =
      client.deleteCollectionObject(NON_EXISTENT_ID);
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match the expected response(s)?
    verbose("deleteNonExistent: status = " + res.getStatus());
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
      invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
  }


  // ---------------------------------------------------------------
  // Utility tests : tests of code used in tests above
  // ---------------------------------------------------------------

  /**
   * Tests the HttpClient-based code used to submit data, in various methods below.
   */
  @Test(dependsOnMethods = {"create", "read"})
  public void testSubmitRequest() {

    // Expected status code: 200 OK
    final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

    // Submit the request to the service and store the response.
    String url = getResourceURL(knownObjectId);
    GetMethod method = new GetMethod(url);
    int statusCode = submitRequest(method);
    
    // Check the status code of the response: does it match the expected response(s)?
    verbose("testSubmitRequest: url=" + url + " status=" + statusCode);
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

  }
		

  // ---------------------------------------------------------------
  // Utility methods used by tests above
  // ---------------------------------------------------------------
  
  // @TODO Add Javadoc comments to all of these methods.

  // -----------------------------
  // Methods specific to this test
  // -----------------------------

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
  
  private String getServicePathComponent() {
    // @TODO Determine if it is possible to obtain this value programmatically.
    // We set this in an annotation in the CollectionObjectProxy interface, for instance.
    final String SERVICE_PATH_COMPONENT = "collectionobjects";
    return SERVICE_PATH_COMPONENT;
  }
  
  // -------------------------------------------------------------
  // Methods common to all entity service test classes.
  //
  // These can be moved out of individual service test classes
  // into a common class, perhaps at the top-level 'client' module.
  // -------------------------------------------------------------

  protected String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
    return 
      "Status code '" + statusCode + "' in response is NOT within the expected set: " +
      requestType.validStatusCodesAsString();
  }
  
  private String getServiceRootURL() {
    return serviceClient.getBaseURL() + getServicePathComponent();
  }

  private String getResourceURL(String resourceIdentifier) {
    return getServiceRootURL() + "/" + resourceIdentifier;
  }

  private int submitRequest(HttpMethod method) {
   int statusCode = 0;
    try {
      statusCode = httpClient.executeMethod(method);
    } catch(HttpException e) {
      logger.error("Fatal protocol violation: ", e);
    } catch(IOException e) {
      logger.error("Fatal transport error: ", e);
    } catch(Exception e) {
      logger.error("Unknown exception: ", e);
    } finally {
      // Release the connection.
      method.releaseConnection();
    }
    return statusCode;
  }
  
  private int submitRequest(EntityEnclosingMethod method, RequestEntity entity) {
    int statusCode = 0;
    try {
      method.setRequestEntity(entity);
      statusCode = httpClient.executeMethod(method);
    } catch(HttpException e) {
      logger.error("Fatal protocol violation: ", e);
    } catch(IOException e) {
      logger.error("Fatal transport error: ", e);
    } catch(Exception e) {
      logger.error("Unknown exception: ", e);
    } finally {
      // Release the connection.
      method.releaseConnection();
    }
    return statusCode;
  }
  
  private StringRequestEntity getXmlEntity(String contents) {
    if (contents == null) {
      contents = "";
    }
    StringRequestEntity entity = null;
    final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    final String XML_CONTENT_TYPE=MediaType.APPLICATION_XML;
    final String UTF8_CHARSET_NAME = "UTF-8";
    try {
      entity =
        new StringRequestEntity(XML_DECLARATION + contents, XML_CONTENT_TYPE, UTF8_CHARSET_NAME);
    } catch (UnsupportedEncodingException e) {
      logger.error("Unsupported character encoding error: ", e);
    }
    return entity;
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
    if (logger.isDebugEnabled()) {
      logger.debug(msg);
    }
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
