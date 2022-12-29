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

import org.dom4j.Element;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.HitClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.hit.HitsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
/**
 * HitServiceTest, carries out tests against a
 * deployed and running Hit Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class HitServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, HitsCommon> {

    /** The logger. */
    private final String CLASS_NAME = HitServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(HitServiceTest.class);
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new HitClient();
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new HitClient(clientPropertiesFilename);
	}

    @Override
    protected String getServiceName() {
        return HitClient.SERVICE_NAME;
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    @Override
	protected void compareReadInstances(HitsCommon original, HitsCommon fromRead) throws Exception {
    	// Add test here
    }

    @Override
    public void delete(String testName) throws Exception {
    	// Do nothing because this test is not ready to delete the "knownResourceId".
    	// Instead, the method localDelete() will get called later in the dependency chain. The
    	// method localDelete() has a dependency on the test "verifyReadOnlyCoreFields".  Once the "verifyReadOnlyCoreFields"
    	// test is run, the localDelete() test/method will get run.  The localDelete() test/method in turn
    	// calls the inherited delete() test/method.
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests", "verifyReadOnlyCoreFields"})
    public void localDelete(String testName) throws Exception {
    	// Because of issues with TestNG not allowing @Test annotations on on override methods,
    	// and because we want the "updateWrongUser" to run before the "delete" test, we need
    	// this method.  This method will call super.delete() after all the dependencies have been
    	// met.
    	super.delete(testName);
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void verifyReadOnlyCoreFields(String testName) throws Exception {
        // TODO These should be in some core client utils
        final String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core";
        final String COLLECTIONSPACE_CORE_TENANTID = "tenantId";
        final String COLLECTIONSPACE_CORE_URI = "uri";
        final String COLLECTIONSPACE_CORE_CREATED_AT = "createdAt";
        final String COLLECTIONSPACE_CORE_UPDATED_AT = "updatedAt";
        final String COLLECTIONSPACE_CORE_CREATED_BY = "createdBy";
        final String COLLECTIONSPACE_CORE_UPDATED_BY = "updatedBy";

        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        HitClient client = new HitClient();
        PoxPayloadIn input = null;
        Response res = client.read(knownResourceId);
        try {
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": read status = " + res.getStatus());
	        }
	        Assert.assertEquals(res.getStatus(), testExpectedStatusCode);

	        input = new PoxPayloadIn(res.readEntity(String.class));
        } finally {
        	res.close();
        }

        PayloadInputPart payloadInputPart = input.getPart(COLLECTIONSPACE_CORE_SCHEMA);
        Element coreAsElement = null;
        if (payloadInputPart != null) {
        	coreAsElement = payloadInputPart.getElementBody();
        }
        Assert.assertNotNull(coreAsElement);
        if (logger.isDebugEnabled()) {
            logger.debug("Core part before update:");
            logger.debug(coreAsElement.asXML());
        }

        // Update the read-only elements
        Element tenantId = coreAsElement.element(COLLECTIONSPACE_CORE_TENANTID);
        String originalTenantId = tenantId.getText();
        tenantId.setText("foo");
        Element uri = coreAsElement.element(COLLECTIONSPACE_CORE_URI);
        String originalUri = uri.getText();
        uri.setText("foo");
        Element createdAt = coreAsElement.element(COLLECTIONSPACE_CORE_CREATED_AT);
        String originalCreatedAt = createdAt.getText();
        String now = GregorianCalendarDateTimeUtils.timestampUTC();
        if(originalCreatedAt.equalsIgnoreCase(now) && logger.isWarnEnabled()) {
        		logger.warn("Cannot check createdAt read-only; too fast!");
        }
        createdAt.setText(now);
        Element createdBy = coreAsElement.element(COLLECTIONSPACE_CORE_CREATED_BY);
        String originalCreatedBy = createdBy.getText();
        createdBy.setText("foo");

        if (logger.isDebugEnabled()) {
            logger.debug("Core part to be updated:");
            logger.debug(coreAsElement.asXML());
        }

        // Create an output payload to send to the service, and add the common part
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart corePart = output.addPart(COLLECTIONSPACE_CORE_SCHEMA, coreAsElement);

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, output);
	    try {
	        int statusCode = res.getStatus();
	        // Check the status code of the response: does it match the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);

	        input = new PoxPayloadIn(res.readEntity(String.class));
	    } finally {
	    	res.close();
	    }

        PayloadInputPart updatedCorePart = input.getPart(COLLECTIONSPACE_CORE_SCHEMA);
        Element updatedCoreAsElement = null;
        if (updatedCorePart != null) {
        	updatedCoreAsElement = updatedCorePart.getElementBody();
        }
        Assert.assertNotNull(updatedCoreAsElement);

        tenantId = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_TENANTID);
        String updatedTenantId = tenantId.getText();
        Assert.assertEquals(updatedTenantId, originalTenantId,
        			"CORE part TenantID was able to update!");
        uri = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_URI);
        String updatedUri = uri.getText();
        Assert.assertEquals(updatedUri, originalUri,
        			"CORE part URI was able to update!");
        createdAt = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_CREATED_AT);
        String updatedCreatedAt = createdAt.getText();
        Assert.assertEquals(updatedCreatedAt, originalCreatedAt,
        			"CORE part CreatedAt was able to update!");
        createdBy = updatedCoreAsElement.element(COLLECTIONSPACE_CORE_CREATED_BY);
        String updatedCreatedBy = createdBy.getText();
        Assert.assertEquals(updatedCreatedBy, originalCreatedBy,
        			"CORE part CreatedBy was able to update!");

    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return HitClient.SERVICE_PATH_COMPONENT;
    }

    /**
     * Creates the hit instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     * @throws Exception
     */
    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        return createHitInstance(
                "entryNumber-" + identifier,
                CURRENT_DATE_UTC,
                "depositor-" + identifier);
    }

    /**
     * Creates the hit instance.
     *
     * @param entryNumber the entry number
     * @param entryDate the entry date
     * @param depositor the depositor
     * @return the multipart output
     * @throws Exception
     */
    private PoxPayloadOut createHitInstance(String entryNumber,
            String entryDate,
            String depositor) throws Exception {
        HitsCommon hit = new HitsCommon();
        hit.setHitNumber(entryNumber);

        PoxPayloadOut multipart = new PoxPayloadOut(HitClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart =
                multipart.addPart(hit, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new HitClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, HitsCommon instance");
            logger.debug(objectAsXmlString(hit, HitsCommon.class));
        }

        return multipart;
    }

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) throws Exception {
		return this.createInstance(identifier);
	}

	@Override
	protected HitsCommon updateInstance(HitsCommon hitsCommon) {
		HitsCommon result = new HitsCommon();

		result.setHitNumber("hits");

        return result;
	}

	@Override
	protected void compareUpdatedInstances(HitsCommon original,
			HitsCommon updated) throws Exception {
		// put test here
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
		// Needed for TestNG dependency chain.
	}
}
