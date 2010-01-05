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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrganizationsCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * OrgAuthorityServiceTest, carries out tests against a
 * deployed and running OrgAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class OrgAuthorityServiceTest extends AbstractServiceTest {

    private final Logger logger =
        LoggerFactory.getLogger(OrgAuthorityServiceTest.class);

    // Instance variables specific to this test.
    private OrgAuthorityClient client = new OrgAuthorityClient();
    final String SERVICE_PATH_COMPONENT = "orgauthorities";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";
    private String knownResourceId = null;
    private String knownResourceRefName = null;
    private String knownItemResourceId = null;
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    private Map<String, String> allResourceItemIdsCreated =
        new HashMap<String, String>();
    
    protected String createRefName(String displayName) {
    	return displayName.replaceAll("\\W", "");
    }    

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
        String displayName = "displayName-" + identifier;
    	String refName = createRefName(displayName);
    	String typeName = "vocabType-" + identifier;
    	MultipartOutput multipart = 
    		createOrgAuthorityInstance(displayName, refName, typeName);
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

        // Store the refname from the first resource created
        // for additional tests below.
        knownResourceRefName = refName;

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(extractId(res));

    }

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
        dependsOnMethods = {"create"})
    public void createItem(String testName) {
        setupCreate(testName);

        knownItemResourceId = createItemInAuthority(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
        }
    }

    private String createItemInAuthority(String vcsid) {

        final String testName = "createItemInAuthority";
        if(logger.isDebugEnabled()){
            logger.debug(testName + ":...");
        }

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        String refName = createRefName(identifier);
        MultipartOutput multipart = createOrganizationInstance(vcsid, 
    		identifier, refName, "Longer Name for "+identifier,
    		null, "joe@org.org", "1910", null, "Anytown, USA", "testing",  
    		"This is a fake organization that was created by a test method." );
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

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null){
            knownItemResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        //
        // Item resource IDs are unique, so these are used as keys;
        // the non-unique IDs of their parents are stored as associated values.
        allResourceItemIdsCreated.put(extractId(res), vcsid);

        return extractId(res);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
            dependsOnMethods = {"create", "createItem"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < 3; i++) {
            create(testName);
            // Add 3 items to each orgauthority
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
            OrgauthoritiesCommon orgAuthority = (OrgauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), OrgauthoritiesCommon.class);
            Assert.assertNotNull(orgAuthority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
            dependsOnMethods = {"read"})
        public void readByName(String testName) throws Exception {

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
                OrgauthoritiesCommon orgAuthority = (OrgauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), OrgauthoritiesCommon.class);
                Assert.assertNotNull(orgAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    */

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

        // Check whether we've received a organization.
        MultipartInput input = (MultipartInput) res.getEntity();
        OrganizationsCommon organization = (OrganizationsCommon) extractPart(input,
                client.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(organization);

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
        dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        ClientResponse<OrgauthoritiesCommonList> res = client.readList();
        OrgauthoritiesCommonList list = res.getEntity();
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
            List<OrgauthoritiesCommonList.OrgauthorityListItem> items =
                    list.getOrgauthorityListItem();
            int i = 0;
            for (OrgauthoritiesCommonList.OrgauthorityListItem item : items) {
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

    @Test(dependsOnMethods = {"createList", "readItem"})
    public void readItemList() {
        readItemList(knownResourceId);
    }

    private void readItemList(String vcsid) {

        final String testName = "readItemList";

        // Perform setup.
        setupReadList(testName);

        // Submit the request to the service and store the response.
        ClientResponse<OrganizationsCommonList> res =
                client.readItemList(vcsid);
        OrganizationsCommonList list = res.getEntity();
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
            List<OrganizationsCommonList.OrganizationListItem> items =
                    list.getOrganizationListItem();
            int i = 0;
            for (OrganizationsCommonList.OrganizationListItem item : items) {
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
            logger.debug("got OrgAuthority to update with ID: " + knownResourceId);
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        OrgauthoritiesCommon orgAuthority = (OrgauthoritiesCommon) extractPart(input,
                client.getCommonPartName(), OrgauthoritiesCommon.class);
        Assert.assertNotNull(orgAuthority);

        // Update the contents of this resource.
        orgAuthority.setDisplayName("updated-" + orgAuthority.getDisplayName());
        orgAuthority.setVocabType("updated-" + orgAuthority.getVocabType());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated OrgAuthority");
            logger.debug(objectAsXmlString(orgAuthority, OrgauthoritiesCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(orgAuthority, MediaType.APPLICATION_XML_TYPE);
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
        OrgauthoritiesCommon updatedOrgAuthority =
                (OrgauthoritiesCommon) extractPart(input,
                        client.getCommonPartName(), OrgauthoritiesCommon.class);
        Assert.assertNotNull(updatedOrgAuthority);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedOrgAuthority.getDisplayName(),
                orgAuthority.getDisplayName(),
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
            logger.debug("got Organization to update with ID: " +
                knownItemResourceId +
                " in OrgAuthority: " + knownResourceId );
        }
        MultipartInput input = (MultipartInput) res.getEntity();
        OrganizationsCommon organization = (OrganizationsCommon) extractPart(input,
                client.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(organization);

        // Update the contents of this resource.
        organization.setShortName("updated-" + organization.getShortName());
        if(logger.isDebugEnabled()){
            logger.debug("to be updated Organization");
            logger.debug(objectAsXmlString(organization,
                OrganizationsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        MultipartOutput output = new MultipartOutput();
        OutputPart commonPart = output.addPart(organization, MediaType.APPLICATION_XML_TYPE);
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
        OrganizationsCommon updatedOrganization =
                (OrganizationsCommon) extractPart(input,
                        client.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(updatedOrganization);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedOrganization.getShortName(),
                organization.getShortName(),
                "Data in updated Organization did not match submitted data.");
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
        MultipartOutput multipart = createOrgAuthorityInstance(NON_EXISTENT_ID);
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
        MultipartOutput multipart = createOrganizationInstance(
        		knownResourceId, NON_EXISTENT_ID, createRefName(NON_EXISTENT_ID),
        		null, null, null, null, null, null, null, null);
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
    // Cleanup of resources created during testing
    // ---------------------------------------------------------------
    
    /**
     * Deletes all resources created by tests, after all tests have been run.
     *
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     */
    @AfterClass(alwaysRun=true)
    public void cleanUp() {
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        // Clean up organization resources.
        String orgAuthorityResourceId;
        String organizationResourceId;
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
            organizationResourceId = entry.getKey();
            orgAuthorityResourceId = entry.getValue();
            // Note: Any non-success responses are ignored and not reported.
            ClientResponse<Response> res =
                client.deleteItem(orgAuthorityResourceId, organizationResourceId);
        }
        // Clean up orgAuthority resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId);
        }
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
     * a path component for the owning orgAuthority, followed by the 
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

    private MultipartOutput createOrgAuthorityInstance(String identifier) {
    	String displayName = "displayName-" + identifier;
    	String refName = createRefName(displayName);
    	String typeName = "vocabType-" + identifier;
        return createOrgAuthorityInstance(
                displayName, refName,typeName );
    }

    private MultipartOutput createOrgAuthorityInstance(
    		String displayName, String refName, String vocabType) {
        OrgauthoritiesCommon orgAuthority = new OrgauthoritiesCommon();
        orgAuthority.setDisplayName(displayName);
        if(refName!=null)
            orgAuthority.setRefName(refName);
        orgAuthority.setVocabType(vocabType);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(orgAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        if(logger.isDebugEnabled()) {
            logger.debug("to be created, orgAuthority common");
            logger.debug(objectAsXmlString(orgAuthority, OrgauthoritiesCommon.class));
        }
        return multipart;
    }

    private MultipartOutput createOrganizationInstance(String inAuthority,
        String shortName, String refName, String longName, 
        String nameAdditions, String contactName, 
        String foundingDate, String dissolutionDate, String foundingPlace,
        String function, String description ) {
        OrganizationsCommon organization = new OrganizationsCommon();
        organization.setShortName(shortName);
        if(refName!=null)
        	organization.setRefName(refName);
        if(longName!=null)
        	organization.setLongName(longName);
        if(nameAdditions!=null)
        	organization.setNameAdditions(nameAdditions);
        if(contactName!=null)
        	organization.setContactName(contactName);
        if(foundingDate!=null)
        	organization.setFoundingDate(foundingDate);
        if(dissolutionDate!=null)
        	organization.setDissolutionDate(dissolutionDate);
        if(foundingPlace!=null)
        	organization.setFoundingPlace(foundingPlace);
        if(function!=null)
        	organization.setFunction(function);
        if(description!=null)
        	organization.setDescription(description);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(organization,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, organization common");
            logger.debug(objectAsXmlString(organization,
                OrganizationsCommon.class));
        }

        return multipart;
    }
}
