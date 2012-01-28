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
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.blob.DimensionSubGroup;
import org.collectionspace.services.blob.MeasuredPartGroup;

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
public class BlobServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, BlobsCommon> {

    private final String CLASS_NAME = BlobServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    private final static String KNOWN_IMAGE_FILENAME = "01-03-09_1546.jpg";
    private final static int WIDTH_DIMENSION_INDEX = 0;
    private final static int HEIGHT_DIMENSION_INDEX = 1;
    
    private final static String KNOWN_IMAGE_SIZE = "56261";
    private final static BigDecimal KNOWN_IMAGE_WIDTH = new BigDecimal(640.0);
    private final static BigDecimal KNOWN_IMAGE_HEIGHT = new BigDecimal(480.0);
    
    
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
    protected CollectionSpaceClient getClientInstance() {
        return new BlobClient();
    }

    @Override
    protected AbstractCommonList getCommonList(ClientResponse<AbstractCommonList> response) {
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
		        		try {
				            assertStatusCode(res, testName);
				            if (isBlobCleanup() == true) {
				            	allResourceIdsCreated.add(extractId(res));
				            }
		        		} finally {
		        			if (res != null) {
		                        res.releaseConnection();
		                    }
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
    
    /*
     * For a known image file, make sure we're getting back the correct metadata about it.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createBlobWithURI"})
    public void testImageDimensions(String testName) throws Exception {
        setupCreate();
        
        String currentDir = this.getResourceDir();
        String blobsDirPath = currentDir
        		+ File.separator + BLOBS_DIR + File.separator + KNOWN_IMAGE_FILENAME;
        File file = new File(blobsDirPath);
        URL fileUrl = file.toURI().toURL();
        String uri = fileUrl.toString();
        //
        // Create the blob
        //
        BlobClient client = new BlobClient();
		ClientResponse<Response> res = null;
		res = client.createBlobFromURI(uri);
		String blobCsid = null;
		try {
	        assertStatusCode(res, testName);	        
	        blobCsid = extractId(res);
	        if (isBlobCleanup() == true) {
	        	allResourceIdsCreated.add(blobCsid);
	        }
		} finally {
			if (res != null) {
                res.releaseConnection();
            }
		}
        //
		// Read the blob back to get the new dimension data
		//
        setupRead();
        ClientResponse<String> readResponse = client.read(blobCsid);
        BlobsCommon blobsCommon = null;
        try {
        	assertStatusCode(readResponse, testName);
            blobsCommon = this.extractCommonPartValue(readResponse);
            Assert.assertNotNull(blobsCommon);
        } finally {
        	if (readResponse != null) {
        		readResponse.releaseConnection();
            }
        }
        
        Assert.assertEquals(blobsCommon.getLength(), KNOWN_IMAGE_SIZE, "The known image blob was not the expected size of " + KNOWN_IMAGE_SIZE);
        
        MeasuredPartGroup measuredImagePart = blobsCommon.getMeasuredPartGroupList().getMeasuredPartGroup().get(0);
        Assert.assertEquals(measuredImagePart.getMeasuredPart(), BlobClient.IMAGE_MEASURED_PART_LABEL, "First measured part of the image blob was not the image itself.");
        
        List<DimensionSubGroup> dimensionSubGroupList = measuredImagePart.getDimensionSubGroupList().getDimensionSubGroup();
        DimensionSubGroup widthDimension = dimensionSubGroupList.get(WIDTH_DIMENSION_INDEX);
        Assert.assertEquals(widthDimension.getDimension(), BlobClient.IMAGE_WIDTH_LABEL, "First dimension item of the image blob was not the width.");
        Assert.assertTrue(widthDimension.getValue().compareTo(KNOWN_IMAGE_WIDTH) == 0);
        
        DimensionSubGroup heightDimension = dimensionSubGroupList.get(HEIGHT_DIMENSION_INDEX);
        Assert.assertEquals(heightDimension.getDimension(), BlobClient.IMAGE_HEIGHT_LABEL, "Second dimension item of the image blob was not the height.");
        Assert.assertTrue(heightDimension.getValue().compareTo(KNOWN_IMAGE_HEIGHT) == 0);
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"CRUDTests"})
    public void createBlobWithURI(String testName) throws Exception {
    	createBlob(testName, true /*with URI*/, null);
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"CRUDTests"})
    public void createBlobWithURL(String testName) throws Exception {
    	createBlob(testName, true /*with URI*/, "http://farm6.static.flickr.com/5289/5688023100_15e00cde47_o.jpg");
    }    
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"createBlobWithURI"})
    public void createBlobWithPost(String testName) throws Exception {
    	createBlob(testName, false /*with POST*/, null);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	return createBlobInstance(identifier);
    }    
    
    private PoxPayloadOut createBlobInstance(String exitNumber) {
    	BlobClient client = new BlobClient();
        String identifier = "blobNumber-" + exitNumber;
        BlobsCommon blob = new BlobsCommon();
        blob.setName(identifier);
        PoxPayloadOut multipart = new PoxPayloadOut(BlobClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(client.getCommonPartName(), blob);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, blob common");
            logger.debug(objectAsXmlString(blob, BlobsCommon.class));
        }

        return multipart;
    }

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		return createInstance(identifier);
	}

    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */
    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
    public void CRUDTests(String testName) {
    	// Do nothing.  Simply here to for a TestNG execution order for our tests
    }

	@Override
	protected BlobsCommon updateInstance(BlobsCommon blobsCommon) {
		BlobsCommon result = new BlobsCommon();
		
        result.setName("updated-" + blobsCommon.getName());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(BlobsCommon original,
			BlobsCommon updated) throws Exception {
		Assert.assertEquals(updated.getName(), original.getName());
	}
}
