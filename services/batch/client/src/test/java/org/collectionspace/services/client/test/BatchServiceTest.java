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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
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
public class BatchServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, BatchCommon> {

    private final String CLASS_NAME = BatchServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_PATH_COMPONENT = "batch";

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

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	return createBatchInstance(identifier);
    }
    
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		PoxPayloadOut result = createBatchInstance(identifier);
		return result;
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

	@Override
	protected BatchCommon updateInstance(BatchCommon batchCommon) {
		BatchCommon result = new BatchCommon();
		
		result.setName("updated-" + batchCommon.getName());
		result.setNotes("updated-" + batchCommon.getNotes());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(BatchCommon original,
			BatchCommon updated) throws Exception {
		Assert.assertEquals(updated.getName(), original.getName());
		Assert.assertEquals(updated.getNotes(), original.getNotes());
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
}
