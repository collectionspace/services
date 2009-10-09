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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionObjectNaturalhistory;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;

import org.jboss.resteasy.client.ClientResponse;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
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
    private String knownResourceId = null; 
    
    //FIXME: Remove this method once ALL the services use "_common" instead of "-common"
    public String getCommonPartName() {
        return getServicePathComponent() + "_common";
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test
    public void create() {

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        verbose("create: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation
        // for additional tests below.
        knownResourceId = extractId(res);
        verbose("create: knownResourceId=" + knownResourceId);
    }

    @Override
    @Test(dependsOnMethods = {"create"})
    public void createList() {
        for(int i = 0; i < 3; i++){
            create();
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    public void createWithEmptyEntityBody() {}
    public void createWithMalformedXml() {}
    public void createWithWrongXmlSchema() {}

/*
    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody() {
    
        // Perform setup.
        setupCreateWithEmptyEntityBody();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getServiceRootURL();
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = "";
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("createWithEmptyEntityBody url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml() {
    
        // Perform setup.
        setupCreateWithMalformedXml();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getServiceRootURL();
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = MALFORMED_XML_DATA; // Constant from base class.
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("createWithMalformedXml url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithWrongXmlSchema() {
    
        // Perform setup.
        setupCreateWithWrongXmlSchema();
     
        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getServiceRootURL();
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = WRONG_XML_SCHEMA_DATA;
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("createWithWrongSchema url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
*/

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
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("read: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //FIXME: remove the following try catch once Aron fixes signatures
        try{
            MultipartInput input = (MultipartInput) res.getEntity();
            CollectionobjectsCommon collectionObject = (CollectionobjectsCommon) extractPart(input,
                    getCommonPartName(), CollectionobjectsCommon.class);
            Assert.assertNotNull(collectionObject);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    // Failure outcomes

    @Override
    @Test(dependsOnMethods = {"read"})
    public void readNonExistent() {

        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("readNonExistent: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dependsOnMethods = {"createList", "read"})
    public void readList() {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        ClientResponse<CollectionobjectsCommonList> res = client.readList();
        CollectionobjectsCommonList list = res.getEntity();

        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("readList: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<CollectionobjectsCommonList.CollectionObjectListItem> items =
                list.getCollectionObjectListItem();
            int i = 0;

            for(CollectionobjectsCommonList.CollectionObjectListItem item : items){
                verbose("readList: list-item[" + i + "] csid=" +
                    item.getCsid());
                verbose("readList: list-item[" + i + "] objectNumber=" +
                    item.getObjectNumber());
                verbose("readList: list-item[" + i + "] URI=" +
                    item.getUri());
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
    @Test(dependsOnMethods = {"read"})
    public void update() {

        // Perform setup.
        setupUpdate();
        try{ //ideally, just remove try-catch and let the exception bubble up
            // Retrieve an existing resource that we can update.
            ClientResponse<MultipartInput> res =
                    client.read(knownResourceId);
            verbose("update: read status = " + res.getStatus());
            Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

            verbose("got object to update with ID: " + knownResourceId);
            MultipartInput input = (MultipartInput) res.getEntity();
            CollectionobjectsCommon collectionObject = (CollectionobjectsCommon) extractPart(input,
                    getCommonPartName(), CollectionobjectsCommon.class);
            Assert.assertNotNull(collectionObject);

            // Update the content of this resource.
            collectionObject.setObjectNumber("updated-" + collectionObject.getObjectNumber());
            collectionObject.setObjectName("updated-" + collectionObject.getObjectName());
            verbose("updated object", collectionObject, CollectionobjectsCommon.class);
            // Submit the request to the service and store the response.
            MultipartOutput output = new MultipartOutput();
            OutputPart commonPart = output.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
            commonPart.getHeaders().add("label", getCommonPartName());

            res = client.update(knownResourceId, output);
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match the expected response(s)?
            verbose("update: status = " + res.getStatus());
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


            input = (MultipartInput) res.getEntity();
            CollectionobjectsCommon updatedCollectionObject =
                    (CollectionobjectsCommon) extractPart(input,
                    getCommonPartName(), CollectionobjectsCommon.class);
            Assert.assertNotNull(updatedCollectionObject);

            Assert.assertEquals(updatedCollectionObject.getObjectName(),
                    collectionObject.getObjectName(),
                    "Data in updated object did not match submitted data.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // Failure outcomes

    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    public void updateWithEmptyEntityBody() {}
    public void updateWithMalformedXml() {}
    public void updateWithWrongXmlSchema() {}

/*
    @Override

    @Test(dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithEmptyEntityBody() {
    
        // Perform setup.
        setupUpdateWithEmptyEntityBody();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = "";
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateWithEmptyEntityBody url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithMalformedXml() {

        // Perform setup.
        setupUpdateWithMalformedXml();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        final String entity = MALFORMED_XML_DATA;
        String mediaType = MediaType.APPLICATION_XML;
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateWithMalformedXml: url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema() {
    
        // Perform setup.
        setupUpdateWithWrongXmlSchema();
        
        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = WRONG_XML_SCHEMA_DATA;
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateWithWrongSchema: url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
*/

    @Override
    @Test(dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent() {

        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.

        // The only relevant ID may be the one used in updateCollectionObject(), below.
        MultipartOutput multipart = createCollectionObjectInstance(NON_EXISTENT_ID);
        ClientResponse<MultipartInput> res =
                client.update(NON_EXISTENT_ID, multipart);

        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
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
    @Test(dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete() {

        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
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
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("deleteNonExistent: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("testSubmitRequest: url=" + url + " status=" + statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }		

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        // @TODO Determine if it is possible to obtain this
        // value programmatically.
        //
        // We set this in an annotation in the CollectionObjectProxy
        // interface, for instance.  We also set service-specific
        // constants in each service module, which might also
        // return this value.
        return SERVICE_PATH_COMPONENT;
    }

    private MultipartOutput createCollectionObjectInstance(String identifier) {
        return createCollectionObjectInstance("objectNumber-" + identifier,
                "objectName-" + identifier);
    }

    private MultipartOutput createCollectionObjectInstance(String objectNumber, String objectName) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

        collectionObject.setObjectNumber(objectNumber);
        collectionObject.setObjectName(objectName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", getCommonPartName());

        verbose("to be created, collectionobject common ", collectionObject, CollectionobjectsCommon.class);

        CollectionObjectNaturalhistory conh = new CollectionObjectNaturalhistory();
        conh.setNhString("test-string");
        conh.setNhInt(999);
        conh.setNhLong(9999);
        OutputPart nhPart = multipart.addPart(conh, MediaType.APPLICATION_XML_TYPE);
        nhPart.getHeaders().add("label", getNHPartName());

        verbose("to be created, collectionobject nhistory", conh, CollectionObjectNaturalhistory.class);
        return multipart;

    }

    private String getNHPartName() {
        return "collectionobjects-naturalhistory";
    }
}
