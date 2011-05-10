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

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.BatchProxy;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.batch.BatchCommonList;
import org.collectionspace.services.batch.BatchCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;

/**
 * BatchServiceTest, carries out tests against a deployed and running Batch Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class BatchServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = BatchServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_PATH_COMPONENT = "batch";
    private String knownResourceId = null;

    @Override
	public String getServicePathComponent() {
		return BatchClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return BatchClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new BatchClient();
    }

    @Override
    protected AbstractCommonList getAbstractCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(BatchCommonList.class);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupCreate();
        BatchClient client = new BatchClient();
        PoxPayloadOut multipart = createBatchInstance(createIdentifier());
        ClientResponse<Response> res = client.create(multipart);
        assertStatusCode(res, testName);
        if (knownResourceId == null) {
            knownResourceId = extractId(res);  // Store the ID returned from the first resource created for additional tests below.
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
        allResourceIdsCreated.add(extractId(res)); // Store the IDs from every resource created by tests so they can be deleted after tests have been run.
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        for (int i = 0; i < 3; i++) {
            create(testName);
        }
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupRead();
        BatchClient client = new BatchClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        BatchCommon batch = (BatchCommon) extractPart(input, client.getCommonPartName(), BatchCommon.class);
        Assert.assertNotNull(batch);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadList();
        BatchClient client = new BatchClient();
        ClientResponse<BatchCommonList> res = client.readList();
        String bar = "\r\n\r\n=================================\r\n\r\n";
        System.out.println(bar+" res: "+res);
        BatchCommonList  list = res.getEntity();

        System.out.println(bar+" list: "+list);
        assertStatusCode(res, testName);

        if (logger.isDebugEnabled()) {
            List<AbstractCommonList.ListItem> items =
                list.getListItem();
            int i = 0;
            for(AbstractCommonList.ListItem item : items){
                logger.debug(testName + ": list-item[" + i + "] " +
                        item.toString());
                i++;
            }
        }

        /*
        List<AbstractCommonList.ListItem> items = list.getListItem();
        int i = 0;
        for(AbstractCommonList.ListItem item : items){
            List<Element> elList = item.getAny();
            StringBuilder elementStrings = new StringBuilder();
            for(Element el : elList) {
                Node textEl = el.getFirstChild();
                if (textEl != null){
                    elementStrings.append("["+el.getNodeName()+":"+textEl.getNodeValue()+"] ");
                }
            }
            System.out.println("\r\n\r\n\r\n~~~~~~~~~~~~~~~~~~~~~~~~~~~"+testName + ": list-item[" + i + "]: "+elementStrings.toString());
            i++;
        }
        */
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdate();
        BatchClient client = new BatchClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        logger.debug("got object to update with ID: " + knownResourceId);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        BatchCommon batch = (BatchCommon) extractPart(input, client.getCommonPartName(), BatchCommon.class);
        Assert.assertNotNull(batch);

        batch.setName("updated-" + batch.getName());
        logger.debug("Object to be updated:"+objectAsXmlString(batch, BatchCommon.class));
        PoxPayloadOut output = new PoxPayloadOut(BatchClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(batch, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        BatchCommon updatedBatch = (BatchCommon) extractPart(input, client.getCommonPartName(), BatchCommon.class);
        Assert.assertNotNull(updatedBatch);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdateNonExistent();
        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        BatchClient client = new BatchClient();
        PoxPayloadOut multipart = createBatchInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        assertStatusCode(res, testName);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDelete();
        BatchClient client = new BatchClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        assertStatusCode(res, testName);
    }

    // ---------------------------------------------------------------
    // Failure outcome tests : means we expect response to fail, but test to succeed
    // ---------------------------------------------------------------

    // Failure outcome
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadNonExistent();
        BatchClient client = new BatchClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcome
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDeleteNonExistent();
        BatchClient client = new BatchClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcomes
    // Placeholders until the tests below can be implemented. See Issue CSPACE-401.

    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode(); // Expected status code: 200 OK
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);
        logger.debug("testSubmitRequest: url=" + url + " status=" + statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	BatchClient client = new BatchClient();
    	return createBatchInstance(identifier);
    }
    
    private PoxPayloadOut createBatchInstance(String exitNumber) {
        String identifier = "batchNumber-" + exitNumber;
        BatchCommon batch = new BatchCommon();
        batch.setName(identifier);
        PoxPayloadOut multipart = new PoxPayloadOut(BatchClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(batch, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new BatchClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, batch common");
            logger.debug(objectAsXmlString(batch, BatchCommon.class));
        }

        return multipart;
    }
}
