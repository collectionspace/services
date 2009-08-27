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

import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CollectionObjectServiceTest, carries out tests against a
 * deployed and running CollectionObject Service.
 * 
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class CollectionObjectServiceTest extends AbstractServiceTest {

    // Instance variables specific to this test.
    private CollectionObjectClient client = new CollectionObjectClient();
    final String SERVICE_PATH_COMPONENT = "collectionobjects";
    private String knownObjectId = null;
 
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    // Success outcomes
    
    @Override
    @Test
    public void create() {

        // Perform setup, such as initializing the type of service request
        // and its valid and expected status codes.
        setupCreate();

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        ClientResponse<Response> res = client.createCollectionObject(collectionObject);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        //
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        verbose("create: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation for additional tests below.
        knownObjectId = extractId(res);
    }

    @Override
    @Test(dependsOnMethods = {"create"})
    public void createMultiple() {
        for(int i = 0; i < 3; i++){
            create   ();
        }
    }

    // Failure outcomes

    @Override
    @Test(dependsOnMethods = {"create"}, expectedExceptions = IllegalArgumentException.class)
    public void createNull() {
        ClientResponse<Response> res = client.createCollectionObject(null);
    }
    
    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml() {
    
        // Perform setup.
        setupCreateWithMalformedXml();

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

    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"}) //, "createWithMalformedXml"})
    public void createWithWrongXmlSchema() {
    
        // Perform setup.
        setupCreateWithWrongXmlSchema();
     
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

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------

    // Success outcomes

    @Override
    @Test(dependsOnMethods = {"create"})
    public void read() {
    
        // Perform setup.
        setupRead();

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

    @Override
    @Test(dependsOnMethods = {"read"})
    public void readNonExistent() {

        // Perform setup.
        setupReadNonExistent();
        
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

    @Override
    @Test(dependsOnMethods = {"createMultiple"})
    public void readList() {
    
        // Perform setup.
        setupReadList();

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

    // Failure outcomes
    
    // None at present.


    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------

    // Success outcomes

    @Override
    @Test(dependsOnMethods = {"create"})
    public void update() {
    
        // Perform setup.
        setupUpdate();

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

    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void updateWithMalformedXml() {

        // Perform setup.
        setupUpdateWithMalformedXml();

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

    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"}) // , "createWithMalformedXml"})
    public void updateWithWrongXmlSchema() {
    
        // Perform setup.
        setupUpdateWithWrongXmlSchema();
        
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

    @Override
    @Test(dependsOnMethods = {"update"})
    public void updateNonExistent() {

        // Perform setup.
        setupUpdateNonExistent();

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

    @Override
    @Test(dependsOnMethods = 
        {"create", "read", "testSubmitRequest", "update"})
    public void delete() {

        // Perform setup.
        setupDelete();

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

    @Override
    @Test(dependsOnMethods = {"delete"})
    public void deleteNonExistent() {

        // Perform setup.
        setupDeleteNonExistent();

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

    @Override
    public String getServicePathComponent() {
        // @TODO Determine if it is possible to obtain this value programmatically.
        // We set this in an annotation in the CollectionObjectProxy interface, for instance.
        // We also set service-specific constants in each service module.
        return SERVICE_PATH_COMPONENT;
    }
    
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
    
    
}
