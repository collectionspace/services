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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.MaterialJAXBSchema;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.MaterialAuthorityClient;
import org.collectionspace.services.client.MaterialAuthorityClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.material.MaterialTermGroup;
import org.collectionspace.services.material.MaterialTermGroupList;
import org.collectionspace.services.material.MaterialauthoritiesCommon;
import org.collectionspace.services.material.MaterialsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * MaterialAuthorityServiceTest, carries out tests against a
 * deployed and running MaterialAuthority Service.
 *
 */
public class MaterialAuthorityServiceTest extends AbstractAuthorityServiceTest<MaterialauthoritiesCommon, MaterialsCommon> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(MaterialAuthorityServiceTest.class);

    /**
     * Default constructor.  Used to set the short ID for all tests authority items
     */
    public MaterialAuthorityServiceTest() {
    	super();
    	TEST_SHORTID = "superglass";
    }
    
    @Override
    public String getServicePathComponent() {
        return MaterialAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return MaterialAuthorityClient.SERVICE_NAME;
    }
    
    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }   
    
    final String TEST_MATERIAL_TERM_DISPLAY_NAME = "SuperGlass 2";
    final String TEST_MATERIAL_TERM_NAME = "SuperGlass";
    final String TEST_MATERIAL_TERM_STATUS = "accepted";
    final String TEST_MATERIAL_TERM_SOURCE = "source";
    final String TEST_MATERIAL_TERM_SOURCE_DETAIL = "internal";
    final String TEST_MATERIAL_DESCRIPTION = "Really strong glass";
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new MaterialAuthorityClient();
    }
    
    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new MaterialAuthorityClient(clientPropertiesFilename);
    }

    @Override
    protected String createItemInAuthority(AuthorityClient client, String authorityId, String shortId) {
        return createItemInAuthority(client, authorityId, shortId, null /*refname*/);
    }
    
    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    private String createItemInAuthority(AuthorityClient client, String vcsid, String shortId, String authRefName) {
        final String testName = "createItemInAuthority("+vcsid+","+authRefName+")"; 
    
        // Submit the request to the service and store the response.
        Map<String, String> materialMap = new HashMap<String,String>();
        // TODO Make material type and status be controlled vocabs.
        materialMap.put(MaterialJAXBSchema.SHORT_IDENTIFIER, shortId);
        materialMap.put(MaterialJAXBSchema.MATERIAL_DESCRIPTION, TEST_MATERIAL_DESCRIPTION);
        
        List<MaterialTermGroup> terms = new ArrayList<MaterialTermGroup>();
        MaterialTermGroup term = new MaterialTermGroup();
        term.setTermDisplayName(TEST_MATERIAL_TERM_DISPLAY_NAME);
        term.setTermName(TEST_MATERIAL_TERM_NAME);
        term.setTermSource(TEST_MATERIAL_TERM_SOURCE);
        term.setTermSourceDetail(TEST_MATERIAL_TERM_SOURCE_DETAIL);
        term.setTermStatus(TEST_MATERIAL_TERM_STATUS);
        terms.add(term);
        
        String newID = MaterialAuthorityClientUtils.createItemInAuthority(vcsid,
                authRefName, materialMap, terms, (MaterialAuthorityClient) client);    

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null){
            setKnownItemResource(newID, shortId);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + newID);
            }
        }
        
        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allResourceItemIdsCreated.put(newID, vcsid);

        return newID;
    }

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName")
	public void verifyIllegalItemDisplayName(String testName) throws Exception {
		// Perform setup for read.
		setupRead();

		// Submit the request to the service and store the response.
		MaterialAuthorityClient client = new MaterialAuthorityClient();
		Response res = client.readItem(knownResourceId, knownItemResourceId);
		MaterialsCommon material = null;
		try {
			assertStatusCode(res, testName);
			PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
			material = (MaterialsCommon) extractPart(input,
					client.getItemCommonPartName(), MaterialsCommon.class);
			Assert.assertNotNull(material);
		} finally {
			if (res != null) {
				res.close();
			}
		}

		//
		// Make an invalid UPDATE request, without a display name
		//
		MaterialTermGroupList termList = material.getMaterialTermGroupList();
		Assert.assertNotNull(termList);
		List<MaterialTermGroup> terms = termList.getMaterialTermGroup();
		Assert.assertNotNull(terms);
		Assert.assertTrue(terms.size() > 0);
		terms.get(0).setTermDisplayName(null);
		terms.get(0).setTermName(null);

		setupUpdateWithInvalidBody(); // we expect a failure

		// Submit the updated resource to the service and store the response.
		PoxPayloadOut output = new PoxPayloadOut(
				MaterialAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
		PayloadOutputPart commonPart = output.addPart(
				client.getItemCommonPartName(), material);
		setupUpdateWithInvalidBody(); // we expected a failure here.
		res = client.updateItem(knownResourceId, knownItemResourceId, output);
		try {
			assertStatusCode(res, testName);
		} finally {
			if (res != null) {
				res.close();
			}
		}
	}

    @Override
    public void delete(String testName) throws Exception {
        // Do nothing.  See localDelete().  This ensure proper test order.
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"localDeleteItem"})    
    public void localDelete(String testName) throws Exception {
        super.delete(testName);
    }

    @Override
    public void deleteItem(String testName) throws Exception {
        // Do nothing.  We need to wait until after the test "localDelete" gets run.  When it does,
        // its dependencies will get run first and then we can call the base class' delete method.
    }
    
    @Test(dataProvider = "testName", groups = {"delete"},
        dependsOnMethods = {"verifyIllegalItemDisplayName"})
    public void localDeleteItem(String testName) throws Exception {
        super.deleteItem(testName);
    }
    
    // ---------------------------------------------------------------
    // Cleanup of resources created during testing
    // ---------------------------------------------------------------
    
    /**
     * Deletes all resources created by tests, after all tests have been run.
     *
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     * @throws Exception 
     */

    @AfterClass(alwaysRun=true)
	public void cleanUp() throws Exception {
		String noTest = System.getProperty("noTestCleanup");
		if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping Cleanup phase ...");
			}
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Cleaning up temporary resources created for testing ...");
		}
		String parentResourceId;
		String itemResourceId;
		// Clean up contact resources.
		MaterialAuthorityClient client = new MaterialAuthorityClient();
		parentResourceId = knownResourceId;
		// Clean up item resources.
		for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
			itemResourceId = entry.getKey();
			parentResourceId = entry.getValue();
			// Note: Any non-success responses from the delete operation
			// below are ignored and not reported.
			client.deleteItem(parentResourceId, itemResourceId).close();
		}
		// Clean up parent resources.
		for (String resourceId : allResourceIdsCreated) {
			// Note: Any non-success responses from the delete operation
			// below are ignored and not reported.
			client.delete(resourceId).close();
		}
	}

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */

    /**
     * Returns the root URL for the item service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning parent, followed by the
     * path component for the items.
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @return The root URL for the item service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getItemServicePathComponent();
    }

    /**
     * Returns the URL of a specific item resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param  itemResourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The URL of a specific item resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String itemResourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + itemResourceIdentifier;
    }

        @Override
    public void authorityTests(String testName) {
        // TODO Auto-generated method stub
        
    }

    //
    // Material specific overrides
    //
    
    @Override
    protected PoxPayloadOut createInstance(String commonPartName,
            String identifier) {
        // Submit the request to the service and store the response.
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        // String baseRefName = MaterialAuthorityClientUtils.createMaterialAuthRefName(shortId, null);      
        PoxPayloadOut result = 
            MaterialAuthorityClientUtils.createMaterialAuthorityInstance(
            displayName, shortId, commonPartName);
        return result;
    }
    
    @Override
    protected PoxPayloadOut createNonExistenceInstance(String commonPartName, String identifier) {
        String displayName = "displayName-NON_EXISTENT_ID";
        PoxPayloadOut result = MaterialAuthorityClientUtils.createMaterialAuthorityInstance(
                    displayName, "nonEx", commonPartName);
        return result;
    }

    @Override
    protected MaterialauthoritiesCommon updateInstance(MaterialauthoritiesCommon materialauthoritiesCommon) {
        MaterialauthoritiesCommon result = new MaterialauthoritiesCommon();
        
        result.setDisplayName("updated-" + materialauthoritiesCommon.getDisplayName());
        result.setVocabType("updated-" + materialauthoritiesCommon.getVocabType());
        
        return result;
    }

    @Override
    protected void compareUpdatedInstances(MaterialauthoritiesCommon original,
            MaterialauthoritiesCommon updated) throws Exception {
        Assert.assertEquals(updated.getDisplayName(),
                original.getDisplayName(),
                "Display name in updated object did not match submitted data.");
    }

    protected void compareReadInstances(MaterialauthoritiesCommon original,
            MaterialauthoritiesCommon fromRead) throws Exception {
        Assert.assertNotNull(fromRead.getDisplayName());
        Assert.assertNotNull(fromRead.getShortIdentifier());
        Assert.assertNotNull(fromRead.getRefName());
    }
    
    //
    // Authority item specific overrides
    //
    
    @Override
    protected MaterialsCommon updateItemInstance(MaterialsCommon materialsCommon) {
                            
            MaterialTermGroupList termList = materialsCommon.getMaterialTermGroupList();
            Assert.assertNotNull(termList);
            List<MaterialTermGroup> terms = termList.getMaterialTermGroup();
            Assert.assertNotNull(terms);
            Assert.assertTrue(terms.size() > 0);
            terms.get(0).setTermDisplayName("updated-" + terms.get(0).getTermDisplayName());
            terms.get(0).setTermName("updated-" + terms.get(0).getTermName());
            materialsCommon.setMaterialTermGroupList(termList);

            return materialsCommon;
    }

    @Override
    protected void compareUpdatedItemInstances(MaterialsCommon original,
            MaterialsCommon updated,
			boolean compareRevNumbers) throws Exception {
            
            MaterialTermGroupList originalTermList = original.getMaterialTermGroupList();
            Assert.assertNotNull(originalTermList);
            List<MaterialTermGroup> originalTerms = originalTermList.getMaterialTermGroup();
            Assert.assertNotNull(originalTerms);
            Assert.assertTrue(originalTerms.size() > 0);
            
            MaterialTermGroupList updatedTermList = updated.getMaterialTermGroupList();
            Assert.assertNotNull(updatedTermList);
            List<MaterialTermGroup> updatedTerms = updatedTermList.getMaterialTermGroup();
            Assert.assertNotNull(updatedTerms);
            Assert.assertTrue(updatedTerms.size() > 0);
            
            Assert.assertEquals(updatedTerms.get(0).getTermDisplayName(),
                originalTerms.get(0).getTermDisplayName(),
                "Value in updated record did not match submitted data.");
            
            if (compareRevNumbers == true) {
            	Assert.assertEquals(original.getRev(), updated.getRev(), "Revision numbers should match.");
            }
    }

    @Override
    protected void verifyReadItemInstance(MaterialsCommon item)
            throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected PoxPayloadOut createNonExistenceItemInstance(
            String commonPartName, String identifier) {
        Map<String, String> nonexMap = new HashMap<String,String>();
        nonexMap.put(MaterialJAXBSchema.MATERIAL_TERM_DISPLAY_NAME, TEST_MATERIAL_TERM_DISPLAY_NAME);
        nonexMap.put(MaterialJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(MaterialJAXBSchema.MATERIAL_TERM_STATUS, TEST_MATERIAL_TERM_STATUS);
        final String EMPTY_REFNAME = "";
        PoxPayloadOut result = 
                MaterialAuthorityClientUtils.createMaterialInstance(EMPTY_REFNAME, nonexMap,
                MaterialAuthorityClientUtils.getTermGroupInstance(TEST_MATERIAL_TERM_DISPLAY_NAME), commonPartName);
        return result;
    }
}
