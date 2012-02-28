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
public class ServiceGroupServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ServicegroupsCommon> {

    private final String CLASS_NAME = ServiceGroupServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_PATH_COMPONENT = "servicegroups";
    private String knownResourceId = null;

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

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	ServiceGroupClient client = new ServiceGroupClient();
    	return createInstance(client.getCommonPartName(), identifier);
    }
    
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		return createServiceGroupInstance(identifier);
	}
    
    private PoxPayloadOut createServiceGroupInstance(String uid) {
        String identifier = "name-" + uid;
        ServicegroupsCommon servicegroup = new ServicegroupsCommon();
        servicegroup.setName(identifier);
        PoxPayloadOut multipart = new PoxPayloadOut(ServiceGroupClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(servicegroup, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ServiceGroupClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, servicegroup common");
            logger.debug(objectAsXmlString(servicegroup, ServicegroupsCommon.class));
        }

        return multipart;
    }

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ServicegroupsCommon updateInstance(ServicegroupsCommon servicegroupsCommon) {
		ServicegroupsCommon result = new ServicegroupsCommon();
		
        result.setName("updated-" + servicegroupsCommon.getName());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(ServicegroupsCommon original,
			ServicegroupsCommon updated) throws Exception {
		Assert.assertEquals(updated.getName(), original.getName());
	}
}
