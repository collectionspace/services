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
import org.collectionspace.services.client.GroupClient;
import org.collectionspace.services.client.GroupProxy;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractCommonListUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.group.GroupsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupServiceTest, carries out tests against a deployed and running Group Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class GroupServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = GroupServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_PATH_COMPONENT = "groups";
    private String knownResourceId = null;

    @Override
	public String getServicePathComponent() {
		return GroupClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return GroupClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient<AbstractCommonList, GroupProxy> getClientInstance() {
        return new GroupClient();
    }

    @Override
    protected AbstractCommonList getAbstractCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupCreate();
        GroupClient client = new GroupClient();
        PoxPayloadOut multipart = createGroupInstance(createIdentifier());
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
        GroupClient client = new GroupClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        GroupsCommon group = (GroupsCommon) extractPart(input, client.getCommonPartName(), GroupsCommon.class);
        Assert.assertNotNull(group);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadList();
        GroupClient client = new GroupClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = res.getEntity();
        assertStatusCode(res, testName);
        if(logger.isTraceEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdate();
        GroupClient client = new GroupClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        logger.debug("got object to update with ID: " + knownResourceId);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        GroupsCommon group = (GroupsCommon) extractPart(input, client.getCommonPartName(), GroupsCommon.class);
        Assert.assertNotNull(group);

        group.setTitle("updated-" + group.getTitle());
        logger.debug("Object to be updated:"+objectAsXmlString(group, GroupsCommon.class));
        PoxPayloadOut output = new PoxPayloadOut(GroupClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(group, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        GroupsCommon updatedGroup = (GroupsCommon) extractPart(input, client.getCommonPartName(), GroupsCommon.class);
        Assert.assertNotNull(updatedGroup);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdateNonExistent();
        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        GroupClient client = new GroupClient();
        PoxPayloadOut multipart = createGroupInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        assertStatusCode(res, testName);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDelete();
        GroupClient client = new GroupClient();
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
        GroupClient client = new GroupClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcome
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDeleteNonExistent();
        GroupClient client = new GroupClient();
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
    	GroupClient client = new GroupClient();
    	return createGroupInstance(identifier);
    }
    
    private PoxPayloadOut createGroupInstance(String uid) {
        String identifier = "title-" + uid;
        GroupsCommon group = new GroupsCommon();
        group.setTitle(identifier);
        group.setResponsibleDepartment("antiquities");
        PoxPayloadOut multipart = new PoxPayloadOut(GroupClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(group, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new GroupClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, group common");
            logger.debug(objectAsXmlString(group, GroupsCommon.class));
        }

        return multipart;
    }
}
