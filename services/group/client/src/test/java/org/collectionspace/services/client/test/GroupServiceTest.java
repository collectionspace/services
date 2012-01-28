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
import org.collectionspace.services.client.GroupClient;
import org.collectionspace.services.client.GroupProxy;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
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
public class GroupServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, GroupsCommon> {

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
    protected CollectionSpaceClient<AbstractCommonList, PoxPayloadOut, String, GroupProxy> getClientInstance() {
        return new GroupClient();
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
    	GroupClient client = new GroupClient();
    	return createInstance(client.getCommonPartName(), identifier);
    }
    
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
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

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected GroupsCommon updateInstance(GroupsCommon groupsCommon) {
		GroupsCommon result = new GroupsCommon();
		
        result.setTitle("updated-" + groupsCommon.getTitle());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(GroupsCommon original,
			GroupsCommon updated) throws Exception {
		Assert.assertEquals(updated.getTitle(), original.getTitle());
	}
}
