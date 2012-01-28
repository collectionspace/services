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
import org.collectionspace.services.client.ObjectExitClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.objectexit.ObjectexitCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ObjectExitServiceTest, carries out tests against a deployed and running ObjectExit Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class ObjectExitServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ObjectexitCommon> {

    private final String CLASS_NAME = ObjectExitServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_PATH_COMPONENT = "objectexit";

    @Override
	public String getServicePathComponent() {
		return ObjectExitClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return ObjectExitClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new ObjectExitClient();
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
    	ObjectExitClient client = new ObjectExitClient();
    	return createObjectExitInstance(identifier);
    }
    
    private PoxPayloadOut createObjectExitInstance(String exitNumber) {
        String identifier = "objectexitNumber-" + exitNumber;
        ObjectexitCommon objectexit = new ObjectexitCommon();
        objectexit.setExitNumber(identifier);
        objectexit.setDepositor("urn:cspace:org.collectionspace.demo:orgauthority:name(TestOrgAuth):organization:name(Northern Climes Museum)'Northern Climes Museum'");
        PoxPayloadOut multipart = new PoxPayloadOut(ObjectExitClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(objectexit, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ObjectExitClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, objectexit common");
            logger.debug(objectAsXmlString(objectexit, ObjectexitCommon.class));
        }

        return multipart;
    }

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        PoxPayloadOut result = createObjectExitInstance(createIdentifier());
		return result;
	}

	@Override
	protected ObjectexitCommon updateInstance(ObjectexitCommon objectexitCommon) {
		ObjectexitCommon result = new ObjectexitCommon();

		result.setExitNumber("updated-" + objectexitCommon.getExitNumber());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(ObjectexitCommon original,
			ObjectexitCommon updated) throws Exception {
		Assert.assertEquals(updated.getExitNumber(), original.getExitNumber());
	}
}
