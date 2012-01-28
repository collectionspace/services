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

import java.util.HashMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.VocabularyClientUtils;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;

import org.jboss.resteasy.client.ClientResponse;

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
public class VocabularyServiceTest extends AbstractAuthorityServiceTest<VocabulariesCommon, VocabularyitemsCommon> {

    private final String CLASS_NAME = VocabularyServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = VocabularyClient.SERVICE_PATH_COMPONENT;//"vocabularies";
    final String SERVICE_PAYLOAD_NAME = VocabularyClient.SERVICE_PAYLOAD_NAME;
    final String SERVICE_ITEM_PAYLOAD_NAME = VocabularyClient.SERVICE_ITEM_PAYLOAD_NAME;


    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new VocabularyClient();
    }

    @Override
    protected String createItemInAuthority(String authorityId) {
    	String result = null;
    	
        VocabularyClient client = new VocabularyClient();
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        String shortId = createIdentifier();
        itemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, shortId);
        itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "display-" + shortId);
        result = VocabularyClientUtils.createItemInVocabulary(authorityId,
                null /*knownResourceRefName*/, itemInfo, client);
        allResourceItemIdsCreated.put(result, authorityId);
        
        return result;
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"CRUDTests"})
    public void createWithBadShortId(String testName) throws Exception {
        testSetup(STATUS_BAD_REQUEST, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        PoxPayloadOut multipart = VocabularyClientUtils.createEnumerationInstance(
                "Vocab with Bad Short Id", "Bad Short Id!", client.getCommonPartName());
        ClientResponse<Response> res = client.create(multipart);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"authorityTests"})
    public void createItemWithBadShortId(String testName) throws Exception {
        setupCreateWithMalformedXml();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        itemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, "Bad Item Short Id!");
        itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "Bad Item!");
        PoxPayloadOut multipart =
                VocabularyClientUtils.createVocabularyItemInstance(null, //knownResourceRefName,
                itemInfo, client.getItemCommonPartName());
        ClientResponse<Response> res = client.createItem(knownResourceId, multipart);
        try {
        	int statusCode = res.getStatus();

            if (!testRequestType.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \"" + itemInfo.get(AuthorityItemJAXBSchema.DISPLAY_NAME)
                        + "\" in personAuthority: \"" + knownResourceId //knownResourceRefName
                        + "\" " + invalidStatusCodeMessage(testRequestType, statusCode));
            }
            if (statusCode != testExpectedStatusCode) {
                throw new RuntimeException("Unexpected Status when creating Item: \"" + itemInfo.get(AuthorityItemJAXBSchema.DISPLAY_NAME)
                        + "\" in personAuthority: \"" + knownResourceId /*knownResourceRefName*/ + "\", Status:" + statusCode);
            }
       } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }
    
    // Failure outcomes
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    		dependsOnMethods = {"updateItem"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        // Perform setup for read.
        setupRead();
        
        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        VocabularyitemsCommon vitem = null;
        try {
        	assertStatusCode(res, testName);
	        // Check whether Person has expected displayName.
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        vitem = (VocabularyitemsCommon) extractPart(input,
	                client.getItemCommonPartName(), VocabularyitemsCommon.class);
	        Assert.assertNotNull(vitem);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Try to Update with null displayName
        //
        setupUpdateWithInvalidBody();
        vitem.setDisplayName(null);
        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(client.getItemCommonPartName(), vitem);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        //
        // Now try to Update with 1-char displayName (too short)
        //
        setupUpdateWithInvalidBody();
        vitem.setDisplayName("a");
        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(client.getItemCommonPartName(), vitem);
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
        	assertStatusCode(res, testName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"localDeleteItem"})
    public void localDelete(String testName) throws Exception {
    	super.delete(testName);
    }

    @Override
    public void delete(String testName) throws Exception {
    	//
    	// This overrides the base test.  We don't want to do anything at this point
    	// in the test suite.  See the localDelete() method for the actual "delete" test
    	//
    }
        
    @Override
    public void deleteItem(String testName) throws Exception {
    	//Do nothing.  We don't want to delete the known item until all the dependencies of the
    	// localDeleteItem() test have been fulfilled.
    }    

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"authorityTests", "readItemList", "testItemSubmitRequest",
        "updateItem", "verifyIllegalItemDisplayName", "verifyIgnoredUpdateWithInAuthority"})
    public void localDeleteItem(String testName) throws Exception {
    	super.deleteItem(testName);
    }    
    
    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */
    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractAuthorityServiceTest.baseAuthorityTests"})    
	public void authorityTests(String testName) {
		// This method only exists as a dependency target for TestNG
	}
    
    // ---------------------------------------------------------------
    // Vocabulary test specific overrides
    // ---------------------------------------------------------------
    
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return VocabularyClient.SERVICE_NAME;
    }

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        String displayName = "displayName-" + identifier;
        PoxPayloadOut result = VocabularyClientUtils.createEnumerationInstance(
                displayName, identifier, commonPartName);
		return result;
	}
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	VocabularyClient client = new VocabularyClient();
        return createInstance(client.getCommonPartName(), identifier);
    }    

	@Override
	protected VocabulariesCommon updateInstance(
			VocabulariesCommon vocabulariesCommon) {
		VocabulariesCommon result = new VocabulariesCommon();
		
		result.setDisplayName("updated-" + vocabulariesCommon.getDisplayName());
		result.setVocabType("updated-" + vocabulariesCommon.getVocabType());
        
        return result;
	}

	@Override
	protected void compareUpdatedInstances(VocabulariesCommon original,
			VocabulariesCommon updated) throws Exception {
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
        Assert.assertEquals(updated.getVocabType(),
        		original.getVocabType(),
                "Vocabulary tyype name in updated object did not match submitted data.");
	}

    //
    // Vocabulary item specific overrides
    //

    @Override
    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) {
    	String headerLabel = new VocabularyClient().getItemCommonPartName();
        HashMap<String, String> vocabItemInfo = new HashMap<String, String>();
        String shortId = createIdentifier();
        vocabItemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, shortId);
        vocabItemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "display-" + shortId);

    	return VocabularyClientUtils.createVocabularyItemInstance(identifier, vocabItemInfo, headerLabel);
    }    
    	
	@Override
	protected VocabularyitemsCommon updateItemInstance(
			VocabularyitemsCommon authorityItem) {
		VocabularyitemsCommon result = new VocabularyitemsCommon();
		result.setDisplayName("updated-" + authorityItem.getDisplayName());
		return result;
	}

	@Override
	protected void compareUpdatedItemInstances(VocabularyitemsCommon original,
			VocabularyitemsCommon updated) throws Exception {
        Assert.assertEquals(updated.getDisplayName(),
        		original.getDisplayName(),
                "Display name in updated VocabularyItem did not match submitted data.");
	}

	@Override
	protected void verifyReadItemInstance(VocabularyitemsCommon item)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createNonExistenceItemInstance(
			String commonPartName, String identifier) {
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        itemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, "nonex");
        itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "display-nonex");
        PoxPayloadOut result =
                VocabularyClientUtils.createVocabularyItemInstance(
                null, //VocabularyClientUtils.createVocabularyRefName(NON_EXISTENT_ID, null),
                itemInfo, commonPartName);
		return result;
	}
    
}
