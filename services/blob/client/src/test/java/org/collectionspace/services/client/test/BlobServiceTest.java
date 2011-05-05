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

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.BlobProxy;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.blob.BlobsCommon;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

/**
 * BlobServiceTest, carries out tests against a deployed and running Blob Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class BlobServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = BlobServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private String knownResourceId = null;
    
    private boolean blobCleanup = true;

    @Override
	public String getServicePathComponent() {
		return BlobClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return BlobClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient<AbstractCommonList, BlobProxy> getClientInstance() {
        return new BlobClient();
    }

    @Override
    protected AbstractCommonList getAbstractCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }

    /**
     * Sets up create tests.
     */
    @Override
	protected void setupCreate() {
        super.setupCreate();
        String noBlobCleanup = System.getProperty(NO_BLOB_CLEANUP);
    	if(Boolean.TRUE.toString().equalsIgnoreCase(noBlobCleanup)) {
    		//
    		// Don't delete the blobs that we created during the test cycle
    		//
            this.blobCleanup = false;
    	}
    }
    
    private boolean isBlobCleanup() {
    	return blobCleanup;
    }
    
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupCreate();
        BlobClient client = new BlobClient();
        PoxPayloadOut multipart = createBlobInstance(createIdentifier());
        ClientResponse<Response> res = client.create(multipart);
        assertStatusCode(res, testName);
        if (knownResourceId == null) {
            knownResourceId = extractId(res);  // Store the ID returned from the first resource created for additional tests below.
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
        allResourceIdsCreated.add(extractId(res)); // Store the IDs from every resource created by tests so they can be deleted after tests have been run.
    }
    
    /**
     * Looks in the .../src/test/resources/blobs directory for files from which to create Blob
     * instances.
     *
     * @param testName the test name
     * @param fromUri - if 'true' then send the service a URI from which to create the blob.
     * @param fromUri - if 'false' then send the service a multipart/form-data POST from which to create the blob.
     * @throws Exception the exception
     */
    protected void createBlob(String testName, boolean fromUri, String uri) throws Exception {
        setupCreate();
        BlobClient client = new BlobClient();
        
        String currentDir = this.getResourceDir();
        String blobsDirPath = currentDir + File.separator + BLOBS_DIR;
        File blobsDir = new File(blobsDirPath);
        if (blobsDir != null && blobsDir.exists()) {
	        File[] children = blobsDir.listFiles();
	        if (children != null && children.length > 0) {
	        	for (File child : children) {
	        		if (isBlobbable(child) == true) {
	        			ClientResponse<Response> res = null;
		        		String mimeType = this.getMimeType(child);
		        		logger.debug("Processing file URI: " + child.getAbsolutePath());
		        		logger.debug("MIME type is: " + mimeType);
		        		if (fromUri == true) {
		        			if (uri != null) {
			        			res = client.createBlobFromURI(uri);
			        			break;
		        			} else {
			        			URL childUrl = child.toURI().toURL();
			        			res = client.createBlobFromURI(childUrl.toString());
		        			}
		        		} else {
				            MultipartFormDataOutput form = new MultipartFormDataOutput();
				            OutputPart outputPart = form.addFormData("file", child, MediaType.valueOf(mimeType));
				            res = client.createBlobFromFormData(form);
		        		}
			            assertStatusCode(res, testName);
			            if (isBlobCleanup() == true) {
			            	allResourceIdsCreated.add(extractId(res));
			            }
	        		}
	        	}
	        } else {
	        	logger.debug("Directory: " + blobsDirPath + " is empty or cannot be read.");
	        }
        } else {
        	logger.debug("Directory: " + blobsDirPath + " is missing or cannot be read.");
        }
    }    
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void createBlobWithURI(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
    	createBlob(testName, true /*with URI*/, null);
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void createBlobWithURL(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
    	createBlob(testName, true /*with URI*/, "http://farm6.static.flickr.com/5289/5688023100_15e00cde47_o.jpg");
    }    
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"createBlobWithURI"})
    public void createBlobWithPost(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
    	createBlob(testName, false /*with POST*/, null);
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
        BlobClient client = new BlobClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        BlobsCommon blob = (BlobsCommon) extractPart(input, client.getCommonPartName(), BlobsCommon.class);
        Assert.assertNotNull(blob);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadList();
        BlobClient client = new BlobClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = res.getEntity();
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
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdate();
        BlobClient client = new BlobClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        logger.debug("got object to update with ID: " + knownResourceId);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        BlobsCommon blob = (BlobsCommon) extractPart(input, client.getCommonPartName(), BlobsCommon.class);
        Assert.assertNotNull(blob);

        blob.setName("updated-" + blob.getName());
        logger.debug("Object to be updated:"+objectAsXmlString(blob, BlobsCommon.class));
        PoxPayloadOut output = new PoxPayloadOut(BlobClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(blob, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        BlobsCommon updatedBlob = (BlobsCommon) extractPart(input, client.getCommonPartName(), BlobsCommon.class);
        Assert.assertNotNull(updatedBlob);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdateNonExistent();
        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        BlobClient client = new BlobClient();
        PoxPayloadOut multipart = createBlobInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        assertStatusCode(res, testName);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDelete();
        BlobClient client = new BlobClient();
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
        BlobClient client = new BlobClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcome
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDeleteNonExistent();
        BlobClient client = new BlobClient();
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
    	return createBlobInstance(identifier);
    }    
    
    private PoxPayloadOut createBlobInstance(String exitNumber) {
        String identifier = "blobNumber-" + exitNumber;
        BlobsCommon blob = new BlobsCommon();
        blob.setName(identifier);
        PoxPayloadOut multipart = new PoxPayloadOut(BlobClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(blob, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new BlobClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, blob common");
            logger.debug(objectAsXmlString(blob, BlobsCommon.class));
        }

        return multipart;
    }
}
