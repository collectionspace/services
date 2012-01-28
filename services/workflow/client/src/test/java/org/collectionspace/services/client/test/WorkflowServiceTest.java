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

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.collectionspace.services.client.DimensionClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.dimension.DimensionsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObjectExitServiceTest, carries out tests against a deployed and running ObjectExit Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class WorkflowServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, WorkflowCommon> {

    private final String CLASS_NAME = WorkflowServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    private String knownResourceId = null;

    @Override
	public String getServicePathComponent() {
		return WorkflowClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return WorkflowClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new DimensionClient();
    }

    //
    // Test overrides
    //
    
	@Override
	public void create(String testName) throws Exception {
		String csid = this.createTestObject(testName);
		if (this.knownResourceId == null) {
			this.knownResourceId = csid;
		}
	}

    @Override
    public void read(String testName) throws Exception {
        setupRead();
        DimensionClient client = new DimensionClient();
        ClientResponse<String> res = client.getWorkflow(knownResourceId);
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        WorkflowCommon workflowsCommon = (WorkflowCommon) extractPart(input, WorkflowClient.SERVICE_COMMONPART_NAME, WorkflowCommon.class);
	        if (logger.isDebugEnabled() == true) {
	        	logger.debug("Workflow payload is: " + input.getXmlPayload());
	        }
	        Assert.assertNotNull(workflowsCommon);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
    }
    
    //
    // FIXME: REM - This test should be a subclass of BaseServiceTest and *not* AbstractPoxServiceTestImpl
    //
    
    @Override
    public void update(String testName) throws Exception {
        setupUpdate();
        updateLifeCycleState(testName, knownResourceId, WorkflowClient.WORKFLOWSTATE_APPROVED);
    }    

    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing.  N/A
    }
        
    public void searchWorkflowDeleted(String testName) throws Exception {
    	// Do nothing.  N/A
    }    

	@Override
	public void readList(String testName) throws Exception {
		// Do nothing.  N/A
	}	
		
	@Override
    public void readPaginatedList(String testName) throws Exception {
		// Do nothing.  N/A
	}

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub		
	}

	@Override
	protected WorkflowCommon updateInstance(WorkflowCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(WorkflowCommon original,
			WorkflowCommon updated) throws Exception {
		// TODO Auto-generated method stub
		
	}
    
	/*
	 * (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
	 */
	@Override
	public void createList(String testName) throws Exception {
		//empty N/A
	}
    
    public void testSubmitRequest() {
    	// Do nothing.  N/A
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
        String dimensionsCommonPartName = new DimensionClient().getCommonPartName();
    	return createInstance(identifier, dimensionsCommonPartName);
    }    
    
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		return createDimensionInstance(commonPartName, identifier);
	}
    
	/*
	 * We're using a DimensionsCommon instance to test the workflow service.
	 */
    private PoxPayloadOut createDimensionInstance(String dimensionValue) {
        String commonPartName = new DimensionClient().getCommonPartName();
        return createDimensionInstance(commonPartName, dimensionValue);
    }
	
	/*
	 * We're using a DimensionsCommon instance to test the workflow service.
	 */
    private PoxPayloadOut createDimensionInstance(String commonPartName,
    		String dimensionValue) {
        String measurementUnit = "measurementUnit-" + dimensionValue;
        DimensionsCommon dimensionsCommon = new DimensionsCommon();
        
        dimensionsCommon.setMeasurementUnit(measurementUnit);
        PoxPayloadOut multipart = new PoxPayloadOut(DimensionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartName, dimensionsCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("To be created, Dimensions common: " + commonPart.asXML());
            logger.debug(objectAsXmlString(dimensionsCommon, DimensionsCommon.class));
        }

        return multipart;
    }
	
}
