/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
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

import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.collectionspace.services.vocabulary.VocabulariesCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * VocabularyServiceTest, carries out tests against a
 * deployed and running Vocabulary Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class VocabularyServiceTest extends AbstractServiceTest {

    private final Logger logger =
            LoggerFactory.getLogger(VocabularyServiceTest.class);
    // Instance variables specific to this test.
    private VocabularyClient client = new VocabularyClient();
    final String SERVICE_PATH_COMPONENT = "vocabularies";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";
    private String knownResourceId = null;
    private String knownItemResourceId = null;

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

        MultipartOutput multipart = createVocabularyInstance(identifier);
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

    @Test(dependsOnMethods = {"create"})
    public void createItem() {
        setupCreate("Create Item");

        knownItemResourceId = createItemInVocab(knownResourceId);
        verbose("createItem: knownItemResourceId=" + knownItemResourceId);
    }

    private String createItemInVocab(String vcsid) {
        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        verbose("createItem:...");
        MultipartOutput multipart = createVocabularyItemInstance(vcsid, identifier);
        ClientResponse<Response> res = client.createItem(vcsid, multipart);

        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        verbose("createItem: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        return extractId(res);
    }

    @Override
    @Test(dependsOnMethods = {"create", "createItem"})
    public void createList() {
        for (int i = 0; i < 3; i++) {
            create();
            // Add 3 items to each vocab
            for (int j = 0; j < 3; j++) {
                createItem();
            }
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    public void createWithEmptyEntityBody() {
    }

    public void createWithMalformedXml() {
    }

    public void createWithWrongXmlSchema() {
    }

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
        try {
            MultipartInput input = (MultipartInput) res.getEntity();
            VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
                    client.getCommonPartName(), VocabulariesCommon.class);
            Assert.assertNotNull(vocabulary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(dependsOnMethods = {"createItem", "read"})
    public void readItem() {

        // Perform setup.
        setupRead("Read Item");

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("readItem: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        //FIXME: remove the following try catch once Aron fixes signatures
        try {
            MultipartInput input = (MultipartInput) res.getEntity();
            VocabularyitemsCommon vocabularyItem = (VocabularyitemsCommon) extractPart(input,
                    client.getItemCommonPartName(), VocabularyitemsCommon.class);
            Assert.assertNotNull(vocabularyItem);
        } catch (Exception e) {
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

    @Test(dependsOnMethods = {"readItem", "readNonExistent"})
    public void readItemNonExistent() {

        // Perform setup.
        setupReadNonExistent("Read Non-Existent Item");

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("readItemNonExistent: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    @Test(dependsOnMethods = {"read"})
    public void readList() {

        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        ClientResponse<VocabulariesCommonList> res = client.readList();
        VocabulariesCommonList list = res.getEntity();
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
            List<VocabulariesCommonList.VocabularyListItem> items =
                    list.getVocabularyListItem();
            int i = 0;
            for (VocabulariesCommonList.VocabularyListItem item : items) {
                String csid = item.getCsid();
                verbose("readList: list-item[" + i + "] csid=" +
                        csid);
                verbose("readList: list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                verbose("readList: list-item[" + i + "] URI=" +
                        item.getUri());
                readItemList(csid);
                i++;
            }
        }
    }

    @Test(dependsOnMethods = {"readItem"})
    public void readItemList() {
        readItemList(knownResourceId);
    }

    private void readItemList(String vcsid) {
        // Perform setup.
        setupReadList("Read Item List");

        // Submit the request to the service and store the response.
        ClientResponse<VocabularyitemsCommonList> res =
                client.readItemList(vcsid);
        VocabularyitemsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("  readItemList: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<VocabularyitemsCommonList.VocabularyitemListItem> items =
                    list.getVocabularyitemListItem();
            int i = 0;
            for (VocabularyitemsCommonList.VocabularyitemListItem item : items) {
                verbose("  readItemList: list-item[" + i + "] csid=" +
                        item.getCsid());
                verbose("  readItemList: list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                verbose("  readItemList: list-item[" + i + "] URI=" +
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

        try { //ideally, just remove try-catch and let the exception bubble up
            // Retrieve an existing resource that we can update.
            ClientResponse<MultipartInput> res =
                    client.read(knownResourceId);
            verbose("update: read status = " + res.getStatus());
            Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

            verbose("got Vocabulary to update with ID: " + knownResourceId);
            MultipartInput input = (MultipartInput) res.getEntity();
            VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
                    client.getCommonPartName(), VocabulariesCommon.class);
            Assert.assertNotNull(vocabulary);

            // Update the content of this resource.
            vocabulary.setDisplayName("updated-" + vocabulary.getDisplayName());
            vocabulary.setVocabType("updated-" + vocabulary.getVocabType());
            verbose("to be updated Vocabulary", vocabulary, VocabulariesCommon.class);
            // Submit the request to the service and store the response.
            MultipartOutput output = new MultipartOutput();
            OutputPart commonPart = output.addPart(vocabulary, MediaType.APPLICATION_XML_TYPE);
            commonPart.getHeaders().add("label", client.getCommonPartName());

            res = client.update(knownResourceId, output);
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match the expected response(s)?
            verbose("update: status = " + res.getStatus());
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


            input = (MultipartInput) res.getEntity();
            VocabulariesCommon updatedVocabulary =
                    (VocabulariesCommon) extractPart(input,
                    client.getCommonPartName(), VocabulariesCommon.class);
            Assert.assertNotNull(updatedVocabulary);

            Assert.assertEquals(updatedVocabulary.getDisplayName(),
                    vocabulary.getDisplayName(),
                    "Data in updated object did not match submitted data.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = {"readItem", "update"})
    public void updateItem() {

        // Perform setup.
        setupUpdate("Update Item");

        try { //ideally, just remove try-catch and let the exception bubble up
            // Retrieve an existing resource that we can update.
            ClientResponse<MultipartInput> res =
                    client.readItem(knownResourceId, knownItemResourceId);
            verbose("updateItem: read status = " + res.getStatus());
            Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

            verbose("got VocabularyItem to update with ID: " + knownItemResourceId + " in Vocab: " + knownResourceId);
            MultipartInput input = (MultipartInput) res.getEntity();
            VocabularyitemsCommon vocabularyItem = (VocabularyitemsCommon) extractPart(input,
                    client.getItemCommonPartName(), VocabularyitemsCommon.class);
            Assert.assertNotNull(vocabularyItem);

            // Update the content of this resource.
            vocabularyItem.setDisplayName("updated-" + vocabularyItem.getDisplayName());
            verbose("to be updated VocabularyItem", vocabularyItem, VocabularyitemsCommon.class);
            // Submit the request to the service and store the response.
            MultipartOutput output = new MultipartOutput();
            OutputPart commonPart = output.addPart(vocabularyItem, MediaType.APPLICATION_XML_TYPE);
            commonPart.getHeaders().add("label", client.getItemCommonPartName());

            res = client.updateItem(knownResourceId, knownItemResourceId, output);
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match the expected response(s)?
            verbose("updateItem: status = " + res.getStatus());
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);


            input = (MultipartInput) res.getEntity();
            VocabularyitemsCommon updatedVocabularyItem =
                    (VocabularyitemsCommon) extractPart(input,
                    client.getItemCommonPartName(), VocabularyitemsCommon.class);
            Assert.assertNotNull(updatedVocabularyItem);

            Assert.assertEquals(updatedVocabularyItem.getDisplayName(),
                    vocabularyItem.getDisplayName(),
                    "Data in updated VocabularyItem did not match submitted data.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    public void updateWithEmptyEntityBody() {
    }

    public void updateWithMalformedXml() {
    }

    public void updateWithWrongXmlSchema() {
    }

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
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA;
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
        // The only relevant ID may be the one used in update(), below.

        // The only relevant ID may be the one used in update(), below.
        MultipartOutput multipart = createVocabularyInstance(NON_EXISTENT_ID);
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

    @Test(dependsOnMethods = {"updateItem", "testItemSubmitRequest"})
    public void updateNonExistentItem() {

        // Perform setup.
        setupUpdateNonExistent("Update Non-Existent Item");

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.

        // The only relevant ID may be the one used in update(), below.
        MultipartOutput multipart = createVocabularyItemInstance(knownResourceId, NON_EXISTENT_ID);
        ClientResponse<MultipartInput> res =
                client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateNonExistentItem: status = " + res.getStatus());
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

    @Test(dependsOnMethods = {"createItem", "readItemList", "testItemSubmitRequest", "updateItem"})
    public void deleteItem() {

        // Perform setup.
        setupDelete("Delete Item");

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.deleteItem(knownResourceId, knownItemResourceId);
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

    @Test(dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem() {

        // Perform setup.
        setupDeleteNonExistent("Delete Non-Existent Item");

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
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

    @Test(dependsOnMethods = {"createItem", "readItem", "testSubmitRequest"})
    public void testItemSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getItemResourceURL(knownResourceId, knownItemResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("testItemSubmitRequest: url=" + url + " status=" + statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    public String getItemServicePathComponent() {
        return ITEM_SERVICE_PATH_COMPONENT;
    }

    /**
     * Returns the root URL for a service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning vocabulary, followed by the 
     * path component for the items.
     *
     * @return The root URL for a service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getItemServicePathComponent();
    }

    /**
     * Returns the URL of a specific resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  resourceIdentifier  An identifier (such as a UUID) for a resource.
     *
     * @return The URL of a specific resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String resourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + resourceIdentifier;
    }

    private MultipartOutput createVocabularyInstance(String identifier) {
        return createVocabularyInstance(
                "displayName-" + identifier,
                "vocabType-" + identifier);
    }

    private MultipartOutput createVocabularyInstance(String displayName, String vocabType) {
        VocabulariesCommon vocabulary = new VocabulariesCommon();
        vocabulary.setDisplayName(displayName);
        vocabulary.setVocabType(vocabType);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(vocabulary, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        verbose("to be created, vocabulary common ", vocabulary, VocabulariesCommon.class);

        return multipart;
    }

    private MultipartOutput createVocabularyItemInstance(String inVocabulary, String displayName) {
        VocabularyitemsCommon vocabularyItem = new VocabularyitemsCommon();
        vocabularyItem.setInVocabulary(inVocabulary);
        vocabularyItem.setDisplayName(displayName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(vocabularyItem, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());

        verbose("to be created, vocabularyitem common ", vocabularyItem, VocabularyitemsCommon.class);

        return multipart;
    }
}
