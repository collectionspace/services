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

//import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PropagationClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.propagation.PropActivityGroup;
import org.collectionspace.services.propagation.PropActivityGroupList;
import org.collectionspace.services.propagation.PropagationsCommon;

import org.testng.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PropagationServiceTest, carries out tests against a
 * deployed and running Propagation (aka Loans In) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class PropagationServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, PropagationsCommon> {

    /** The logger. */
    private final String CLASS_NAME = PropagationServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    /** The service path component. */

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new PropagationClient();
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    
    // Success outcomes
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        PropagationClient client = new PropagationClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createPropagationInstance(identifier);
        String newID = null;
        Response res = client.create(multipart);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            //
            // Specifically:
            // Does it fall within the set of valid status codes?
            // Does it exactly match the expected status code?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            newID = extractId(res);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null) {
            knownResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }
    
    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    
    @Override
	protected void compareReadInstances(PropagationsCommon original, PropagationsCommon fromRead) throws Exception {
        PropActivityGroupList propActivityGroupList = fromRead.getPropActivityGroupList();
        Assert.assertNotNull(propActivityGroupList);
        
        List<PropActivityGroup> propActivityGroups = propActivityGroupList.getPropActivityGroup();
        Assert.assertNotNull(propActivityGroups);
        Assert.assertTrue(propActivityGroups.size() > 0);

        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + fromRead.getPropComments());
        }

        Assert.assertEquals(fromRead.getPropComments(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + fromRead.getPropComments() + "' does not match expected data '" + getUTF8DataFragment());    	
    }

	@Override
	protected void compareUpdatedInstances(PropagationsCommon propagationCommon,
			PropagationsCommon updatedPropagationCommon) throws Exception {
        // Check selected fields in the updated common part.
        Assert.assertEquals(updatedPropagationCommon.getPropNumber(),
                propagationCommon.getPropNumber(),
                "Data in updated object did not match submitted data.");

        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + propagationCommon.getPropComments() + "\n"
                    + "UTF-8 data received=" + updatedPropagationCommon.getPropComments());
        }
        Assert.assertTrue(updatedPropagationCommon.getPropComments().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedPropagationCommon.getPropComments()
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedPropagationCommon.getPropComments(),
                propagationCommon.getPropComments(),
                "Data in updated object did not match submitted data.");
	}
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    public String getServiceName() {
        return PropagationClient.SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return PropagationClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        return createPropagationInstance(identifier);
    }

    /**
     * Creates the propagation instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createPropagationInstance(String identifier) throws Exception {
        return createPropagationInstance(
                "propNumber-" + identifier,
                "returnDate-" + identifier);
    }

    /**
     * Creates the propagation instance.
     *
     * @param propNumber the propagation number
     * @param returnDate the return date
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createPropagationInstance(String propNumber, String returnDate) throws Exception {

        PropagationsCommon propagationCommon = new PropagationsCommon();
        propagationCommon.setPropNumber(propNumber);
        PropActivityGroupList propActivityGroupList = new PropActivityGroupList();
        PropActivityGroup propActivityGroup = new PropActivityGroup();
        propActivityGroupList.getPropActivityGroup().add(propActivityGroup);
        propagationCommon.setPropActivityGroupList(propActivityGroupList);
        propagationCommon.setPropReason("For Surfboards of the 1960s exhibition.");
        propagationCommon.setPropComments(getUTF8DataFragment());

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = multipart.addPart(new PropagationClient().getCommonPartName(), propagationCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, propagation common");
            logger.debug(objectAsXmlString(propagationCommon, PropagationsCommon.class));
        }

        return multipart;
    }

    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */    
	@Override
	public void CRUDTests(String testName) {
		// // Needed for TestNG dependency chain.
		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName, String identifier) throws Exception {
        PoxPayloadOut result = createPropagationInstance(identifier);
        return result;
	}

	@Override
	protected PropagationsCommon updateInstance(PropagationsCommon propagationCommon) {
        // Update the content of this resource.
        propagationCommon.setPropNumber("updated-" + propagationCommon.getPropNumber());
        propagationCommon.setPropComments("updated-" + propagationCommon.getPropComments());
        
        return propagationCommon;
	}

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
		return new PropagationClient();
	}
}
