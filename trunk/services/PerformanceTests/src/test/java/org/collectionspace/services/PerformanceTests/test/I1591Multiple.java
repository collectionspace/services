/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.PerformanceTests.test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;

/**
 * A test related to Issue CSPACE-1591, which creates a large number of
 * records in a variation where:
 * - A new client object is instantiated for every 'create' request submitted.
 * - The client's HTTP connection is formally closed, and its resources
 *   released, after each 'create' request.
 * 
 * @version $Revision:$
 */
public class I1591Multiple extends CollectionSpacePerformanceTest {

    final Logger logger = LoggerFactory.getLogger(I1591Multiple.class);
    private final String COLLECTION_OBJECT_COMMON_PART_NAME =
        getCollectionObjectCommonPartName();
    private static int MAX_RECORDS = 500;
    String[] coList = new String[MAX_RECORDS];

    private String getCollectionObjectCommonPartName() {
        return new CollectionObjectClient().getCommonPartName();
    }

    @Test
    public void testCreateWithMultipleClientInstantiations() {
        coList = this.createCollectionObjects(MAX_RECORDS);
    }

    /**
     * Creates a single collection object, instantiating a new instance
     * of the CollectionObjectClient (a RESTEasy client proxy)
     * each time this method is called.
     *
     * @param identifier A arbitrary identifier to use when filling
     *                   the CollectionObject's fields with values.
     * @return A resource ID for the newly-created object.
     */
    private String createCollectionObject(long identifier) throws AssertionError {

        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String resourceId = null;

        // Create a CollectionObject instance.
        CollectionobjectsCommon co = new CollectionobjectsCommon();
        fillCollectionObject(co, Long.toString(identifier));

        // Assign it to the Common part of a multipart payload.
        PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(collectionObjectClient.getCommonPartName());

        // Make a create call with that payload and check the response.
        ClientResponse<Response> response = collectionObjectClient.create(multipart);
        try {
            Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
            resourceId = extractId(response);
        // Since failed Asserts can throw an Exception, ensure
        // that the underlying HTTP connection is explicitly closed
        // under all circumstances.
        } finally {
            response.releaseConnection();
        }

        // Return the ID of the newly-created CollectionObject resource.
        return resourceId;
    }

    /**
     * Creates multiple CollectionObject resources.
     *
     * @param numberOfObjects The number of CollectionObject resources to create.
     * @return A list of the resource IDs of the newly-created object resources.
     */
    public String[] createCollectionObjects(int numberOfObjects) {

            long identifier = 0;
            int i = 0;

            try {
                for (i = 0; i <= numberOfObjects; i++) {
                    identifier = System.currentTimeMillis();
                    coList[i] = createCollectionObject(identifier);
                    if (logger.isDebugEnabled() == true) {
                        logger.debug("Created CollectionObject #: " + i);
                    }
                }
            } catch (AssertionError e) {
                if (logger.isDebugEnabled() == true) {
                    logger.debug("FAILURE: Created " + i +
                        " of " + numberOfObjects +
                        " before failing.");
                }
                Assert.assertTrue(false);
            }

            return coList;
    }

    @AfterClass(alwaysRun=true)
    public void cleanUp() {

        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String resourceId = "";

        if (logger.isDebugEnabled() == true) {
            logger.debug("Cleaing up CollectionObject records created during testing ...");
        }

        for (int i = 0; i < coList.length; i++) {
            resourceId = coList[i];
            ClientResponse<Response> res = collectionObjectClient.delete(resourceId);
            if (logger.isDebugEnabled() == true) {
                logger.debug("Deleted CollectionObject #: " + i);
            }
            res.releaseConnection();
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted " + coList.length + " CollectionObjects.");
        }
    }

}
