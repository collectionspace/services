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
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.ServiceGroupClient;
import org.collectionspace.services.client.ServiceGroupProxy;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.servicegroup.ServicegroupsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServiceGroupServiceTest, carries out tests against a deployed and running ServiceGroup Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class ServiceGroupServiceTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = ServiceGroupServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_PATH_COMPONENT = "servicegroups";
    private String readGroupName = "procedure";

    @Override
	public String getServicePathComponent() {
		return ServiceGroupClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return ServiceGroupClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient<AbstractCommonList, PoxPayloadOut, String, ServiceGroupProxy> getClientInstance() {
        return new ServiceGroupClient();
    }

    @Override
    protected AbstractCommonList getCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }
    
	public ServicegroupsCommon extractCommonPartValue(CollectionSpaceClient client,
			ClientResponse<String> res) throws Exception {
		
		ServicegroupsCommon result = null;
		PayloadInputPart payloadInputPart = extractPart(res, client.getCommonPartName());
		if (payloadInputPart != null) {
			result = (ServicegroupsCommon) payloadInputPart.getBody();
		}
		Assert.assertNotNull(result,
				"Part or body of part " + client.getCommonPartName() + " was unexpectedly null.");
		
		return result;
	}

    protected PayloadInputPart extractPart(ClientResponse<String> res, String partLabel)
            throws Exception {
            if (getLogger().isDebugEnabled()) {
            	getLogger().debug("Reading part " + partLabel + " ...");
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            PayloadInputPart payloadInputPart = input.getPart(partLabel);
            Assert.assertNotNull(payloadInputPart,
                    "Part " + partLabel + " was unexpectedly null.");
            return payloadInputPart;
    }


    @Test(dataProvider = "testName", dependsOnMethods = {"readList"})    
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
    	CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<String> res = client.read(readGroupName);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        ServicegroupsCommon output = extractCommonPartValue(client, res);
        Assert.assertNotNull(output);

        //
        // Now compare with the expected field values
        //
        Assert.assertEquals(output.getName(), readGroupName, 
                "Display name in updated object did not match submitted data.");
    }
    
        
    @Test(dataProvider = "testName")    
    public void readList(String testName) throws Exception {
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        CollectionSpaceClient client = this.getClientInstance();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isTraceEnabled()) {
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, getLogger(), testName);
        }
    }
    
    
}
