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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class)
    public void create(String testName) throws Exception {

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);

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
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation
        // for additional tests below.
        knownResourceId = extractId(res);
        if(logger.isDebugEnabled()){
            logger.debug("create: knownResourceId=" + knownResourceId);
        }
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create"})
    public void createItem(String testName) {
        setupCreate(testName);

        knownItemResourceId = createItemInVocab(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
        }
    }

    private String createItemInVocab(String vcsid) {

        final String testName = "createItemInVocab";
        if(logger.isDebugEnabled()){
            logger.debug(testName + ":...");
        }

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        MultipartOutput multipart = createVocabularyItemInstance(vcsid, identifier);
        ClientResponse<Response> res = client.createItem(vcsid, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        return extractId(res);
    }

    @Override
    @Test(dependsOnMethods = {"create", "createItem"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < 3; i++) {
            create(testName);
            // Add 3 items to each vocab
            for (int j = 0; j < 3; j++) {
                createItem(testName);
            }
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    /*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throws Exception {

    // Perform setup.
    setupCreateWithEmptyEntityBody(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = "";
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()) {
        logger.debug(testName + ": url=" + url +
            " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml(String testName) throws Exception {

    // Perform setup.
    setupCreateWithMalformedXml(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA; // Constant from base class.
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
        logger.debug(testName + ": url=" + url +
            " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithWrongXmlSchema(String testName) throws Exception {

    // Perform setup.
    setupCreateWithWrongXmlSchema(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
        logger.debug(testName + ": url=" + url +
            " status=" + statusCode);
     }
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {

        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
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

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"createItem", "read"})
    public void readItem(String testName) throws Exception {

        // Perform setup.
        setupRead(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether we've received a vocabulary item.
        MultipartInput input = (MultipartInput) res.getEntity();
        VocabularyitemsCommon vocabularyItem = (VocabularyitemsCommon) extractPart(input,
                client.getItemCommonPartName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(vocabularyItem);

    }

    // Failure outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"read"})
    public void readNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"readItem", "readNonExistent"})
    public void readItemNonExistent(String testName) {

        // Perform setup.
        setupReadNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<MultipartInput> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"read"})
    public void readList(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        ClientResponse<VocabulariesCommonList> res = client.readList();
        VocabulariesCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
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
                logger.debug(testName + ": list-item[" + i + "] csid=" +
                        csid);
                logger.debug(testName + ": list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
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

        final String testName = "readItemList";

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        ClientResponse<VocabularyitemsCommonList> res =
                client.readItemList(vcsid);
        VocabularyitemsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("  " + testName + ": status = " + statusCode);
        }
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
                logger.debug("  " + testName + ": list-item[" + i + "] csid=" +
                        item.getCsid());
                logger.debug("  " + testName + ": list-item[" + i + "] displayName=" +
                        item.getDisplayName());
                logger.debug("  " + testName + ": list-item[" + i + "] URI=" +
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        // Retrieve the contents of a resource to update.
        ClientResponse<MultipartInput> res =
                client.read(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got Vocabulary to update with ID: " + knownResourceId);
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
                client.getCommonPartName(), VocabulariesCommon.class);
        Assert.assertNotNull(vocabulary);

        // Update the contents of this resource.
        vocabulary.setDisplayName("updated-" + vocabulary.getDisplayName());
        vocabulary.setVocabType("updated-" + vocabulary.getVocabType());
        if(logger.isDebugEnabled()){
            verbose("to be updated Vocabulary", vocabulary, VocabulariesCommon.class);
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(vocabulary, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());
        res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("update: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        VocabulariesCommon updatedVocabulary =
                (VocabulariesCommon) extractPart(input,
                        client.getCommonPartName(), VocabulariesCommon.class);
        Assert.assertNotNull(updatedVocabulary);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedVocabulary.getDisplayName(),
                vocabulary.getDisplayName(),
                "Data in updated object did not match submitted data.");
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"readItem", "update"})
    public void updateItem(String testName) throws Exception {

        // Perform setup.
        setupUpdate(testName);

        ClientResponse<MultipartInput> res =
                client.readItem(knownResourceId, knownItemResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if(logger.isDebugEnabled()){
            logger.debug("got VocabularyItem to update with ID: " +
                knownItemResourceId +
                " in Vocab: " + knownResourceId );
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        VocabularyitemsCommon vocabularyItem = (VocabularyitemsCommon) extractPart(input,
                client.getItemCommonPartName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(vocabularyItem);

        // Update the contents of this resource.
        vocabularyItem.setDisplayName("updated-" + vocabularyItem.getDisplayName());
        if(logger.isDebugEnabled()){
            verbose("to be updated VocabularyItem", vocabularyItem,
                VocabularyitemsCommon.class);
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(vocabularyItem, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = (MultipartInput) res.getEntity();
        VocabularyitemsCommon updatedVocabularyItem =
                (VocabularyitemsCommon) extractPart(input,
                        client.getItemCommonPartName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(updatedVocabularyItem);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedVocabularyItem.getDisplayName(),
                vocabularyItem.getDisplayName(),
                "Data in updated VocabularyItem did not match submitted data.");
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

    /*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithEmptyEntityBody(String testName) throws Exception {

    // Perform setup.
    setupUpdateWithEmptyEntityBody(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = "";
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
        logger.debug(testName + ": url=" + url +
            " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithMalformedXml(String testName) throws Exception {

    // Perform setup.
    setupUpdateWithMalformedXml(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
        logger.debug(testName + ": url=" + url +
           " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema(String testName) throws Exception {

    // Perform setup.
    setupUpdateWithWrongXmlSchema(testName);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
        logger.debug("updateWithWrongXmlSchema: url=" + url +
            " status=" + statusCode);
     }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
     */


    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

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
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"updateItem", "testItemSubmitRequest"})
    public void updateNonExistentItem(String testName) throws Exception {

        // Perform setup.
        setupUpdateNonExistent(testName);

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
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

   @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"createItem", "readItemList", "testItemSubmitRequest",
            "updateItem"})
    public void deleteItem(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.deleteItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("delete: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // Failure outcomes
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {

        // Perform setup.
        setupDeleteNonExistent(testName);

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
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
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    @Test(dependsOnMethods = {"createItem", "readItem", "testSubmitRequest"})
    public void testItemSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getItemResourceURL(knownResourceId, knownItemResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testItemSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

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

        if(logger.isDebugEnabled()) {
            verbose("to be created, vocabulary common ", vocabulary, VocabulariesCommon.class);
        }
        return multipart;
    }

    private MultipartOutput createVocabularyItemInstance(String inVocabulary,
        String displayName) {
        VocabularyitemsCommon vocabularyItem = new VocabularyitemsCommon();
        vocabularyItem.setInVocabulary(inVocabulary);
        vocabularyItem.setDisplayName(displayName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(vocabularyItem,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());

        if(logger.isDebugEnabled()){
            verbose("to be created, vocabularyitem common ", vocabularyItem,
                VocabularyitemsCommon.class);
        }

        return multipart;
    }
}
