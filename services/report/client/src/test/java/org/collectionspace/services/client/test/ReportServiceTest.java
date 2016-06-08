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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.acquisition.AcquisitionSourceList;
import org.collectionspace.services.acquisition.AcquisitionsCommon;
import org.collectionspace.services.acquisition.StructuredDateGroup;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.report.ReportsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.AcquisitionClient;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
 * ReportServiceTest, carries out tests against a
 * deployed and running Report Service.
 *
 * $LastChangedRevision: 2261 $
 * $LastChangedDate: 2010-05-28 16:52:22 -0700 (Fri, 28 May 2010) $
 */
public class ReportServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ReportsCommon> {

    /** The logger. */
    private final String CLASS_NAME = ReportServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    final String SERVICE_NAME = "reports";
    final String SERVICE_PATH_COMPONENT = "reports";
    // Instance variables specific to this test.    
    private String testDocType = "Acquisition";

    private String createAquisitionResource() throws Exception {
    	String result = null;
    	
    	AcquisitionClient acquisitionClient = new AcquisitionClient();
    	AcquisitionsCommon acquisitionsCommon = new AcquisitionsCommon();
    	acquisitionsCommon.setAcquisitionReason("It was nice.");
    	acquisitionsCommon.setOriginalObjectPurchasePriceValue(new BigDecimal(500));
    	acquisitionsCommon.setAcquisitionReferenceNumber("2013.003.0004");
    	
    	AcquisitionSourceList asl = new AcquisitionSourceList();
    	List<String> sourceList = asl.getAcquisitionSource();
    	sourceList.add("The Jim Henson Legacy");
    	acquisitionsCommon.setAcquisitionSources(asl);
    	
    	StructuredDateGroup sdg = new StructuredDateGroup();
    	sdg.setDateDisplayDate("12/12/2012");
    	acquisitionsCommon.setAccessionDateGroup(sdg);
    	
        PoxPayloadOut poxPayloadOut = new PoxPayloadOut(AcquisitionClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = poxPayloadOut.addPart(acquisitionClient.getCommonPartName(), acquisitionsCommon);
        Response res = acquisitionClient.create(poxPayloadOut);
    	try {
            setupCreate();
	        int statusCode = res.getStatus();
	        if (logger.isDebugEnabled()) {
	            logger.debug(this.getClass().getCanonicalName() + ": HTTP status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        result = extractId(res);
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        allResourceIdsCreated.add(result);
    	} finally {
    		res.close();
    	}
    	
    	return result;
    }
    
    @Test(dataProvider = "testName", dependsOnMethods = {"create"})    
    public void publishReportInstance(String testName) throws Exception {
        // Perform setup.

        // Submit the request to the service and store the response.
        ReportClient client = (ReportClient)this.getClientInstance();
        String reportCsid = createResource(testName, this.getKnowResourceIdentifier());
    	String acquisitionCsid = createAquisitionResource();
    	
    	//
    	// Hard coded for now, but need to create this test in the Integration test area where
    	// we'll create an Acquisition instance for this test
    	//
    	InvocationContext invocationContext = new InvocationContext();
    	invocationContext.setDocType("Acquisition");
    	invocationContext.setMode("single");
    	invocationContext.setSingleCSID(acquisitionCsid);
    	
        Response res = client.publishReport(reportCsid, invocationContext);
        try {
	        int statusCode = res.getStatus();
	        setupCreate();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        String publicItemCsid = extractId(res);
	        Assert.assertNotNull(publicItemCsid);
        } finally {
        	res.close();
        }
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new ReportClient();
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new ReportClient(clientPropertiesFilename);
	}
    
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void readListFiltered(String testName) throws Exception {
    	// Perform setup.
    	setupReadList();

    	// Submit the request to the service and store the response.
    	ReportClient client = new ReportClient();
    	Response res = client.readListFiltered(testDocType, "single");
    	AbstractCommonList list = null;
    	try {
    		assertStatusCode(res, testName);
    		list = res.readEntity(AbstractCommonList.class);
    	} finally {
    		if (res != null) {
                res.close();
            }
    	}
    	
    	List<AbstractCommonList.ListItem> items = list.getListItem();
    	// We must find the basic one we created
    	boolean fFoundBaseItem = false;
		for (AbstractCommonList.ListItem item : items) {
			String itemCsid = AbstractCommonListUtils.ListItemGetCSID(item);
			if (knownResourceId.equalsIgnoreCase(itemCsid)) {
				fFoundBaseItem = true;
				break;
			}
		}
		
		if (!fFoundBaseItem) {
			Assert.fail("readListFiltered failed to return base item");
		}
		
		// Now filter for something else, and ensure it is NOT returned
    	res = client.readListFiltered("Intake", "single");
    	try {
	        assertStatusCode(res, testName);
	    	list = res.readEntity(AbstractCommonList.class);
    	} finally {
    		if (res != null) {
                res.close();
            }
    	}

    	items = list.getListItem();
    	// We must NOT find the basic one we created
		for (AbstractCommonList.ListItem item : items) {
			Assert.assertNotSame(AbstractCommonListUtils.ListItemGetCSID(item), knownResourceId, 
				"readListFiltered(\"Intake\", \"single\") incorrectly returned base item");
		}
		
		// Now filter for something else, and ensure it is NOT returned
    	res = client.readListFiltered(testDocType, "group");
    	try {
	        assertStatusCode(res, testName);
	    	list = res.readEntity(AbstractCommonList.class);
    	} finally {
    		if (res != null) {
                res.close();
            }
    	}

    	items = list.getListItem();
    	// We must NOT find the basic one we created
		for (AbstractCommonList.ListItem item : items) {
			Assert.assertNotSame(AbstractCommonListUtils.ListItemGetCSID(item), knownResourceId, 
				"readListFiltered(\""+testDocType+"\", \"group\") incorrectly returned base item");
		}
    }

    /**
     * This method overrides the delete method in the base class which is marked with the TestNG @Test annotation.
     * Since we don't want the actually delete test to happen until later in the dependency test chain, we're make this
     * an empty method.  Later in the test suite, the method localDelete() will get called and it will call super.delete()
     */
    @Override
    public void delete(String testName) throws Exception {
    	//
    	// Do nothing for now.  The test localDelete() will get called later in the dependency chain.
    	//
    }
    
    /**
     * This test will delete the known resource after the test readListFiltered() is run
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"readListFiltered"})
    public void localDelete(String testName) throws Exception {
    	super.delete(testName);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    /**
     * Creates the report instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createReportInstance(String identifier) throws Exception {
    	List<String> docTypes = new ArrayList<String>();
    	docTypes.add(testDocType);
        return createReportInstance(
                "Acquisition Summary " + identifier, 
                docTypes, true, false, false, true,
                "acq_basic.jasper",
                "application/pdf");
    }

    /**
     * Creates the report instance.
     *
     * @param name the report name
     * @param filename the relative path to the report
     * @param outputMIME the MIME type we will return for this report
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createReportInstance(String name,
    		List<String> forDocTypeList,
    		boolean supportsSingle, boolean supportsList, 
    		boolean supportsGroup, boolean supportsNoContext, 
            String filename,
            String outputMIME) throws Exception {
        ReportsCommon reportCommon = new ReportsCommon();
        reportCommon.setName(name);
        ReportsCommon.ForDocTypes forDocTypes = new ReportsCommon.ForDocTypes(); 
        List<String> docTypeList = forDocTypes.getForDocType();
        docTypeList.addAll(forDocTypeList);
        reportCommon.setForDocTypes(forDocTypes);
        reportCommon.setSupportsSingleDoc(supportsSingle);
        reportCommon.setSupportsDocList(supportsList);
        reportCommon.setSupportsGroup(supportsGroup);
        reportCommon.setSupportsNoContext(supportsNoContext);
        reportCommon.setFilename(filename);
        reportCommon.setOutputMIME(outputMIME);
        reportCommon.setNotes(getUTF8DataFragment()); // For UTF-8 tests

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(new ReportClient().getCommonPartName(), reportCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, report common");
            logger.debug(objectAsXmlString(reportCommon, ReportsCommon.class));
            logger.debug(multipart.toXML());
        }

        return multipart;
    }

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) throws Exception {
        PoxPayloadOut result = createReportInstance(identifier);
		return result;
	}

	@Override
	protected ReportsCommon updateInstance(ReportsCommon reportsCommon) {
		ReportsCommon result = new ReportsCommon();
		
		result.setSupportsSingleDoc(true);
		result.setName("updated-" + reportsCommon.getName());
		result.setOutputMIME("updated-" + reportsCommon.getOutputMIME());
        result.setNotes("updated-" + reportsCommon.getNotes());
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(ReportsCommon original,
			ReportsCommon updated) throws Exception {
        // Check selected fields in the updated common part.
        Assert.assertEquals(updated.getName(),
        		original.getName(),
                "Name in updated object did not match submitted data.");

        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + original.getNotes() + "\n"
                    + "UTF-8 data received=" + updated.getNotes());
        }
        Assert.assertTrue(updated.getNotes().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updated.getNotes()
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updated.getNotes(),
        		original.getNotes(),
                "Notes in updated object did not match submitted data.");
	}
	
	protected void compareReadInstances(ReportsCommon original, ReportsCommon fromRead) throws Exception {
        Assert.assertEquals(fromRead.getNotes(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + fromRead.getNotes()
                + "' does not match expected data '" + getUTF8DataFragment());
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
		// TODO Auto-generated method stub		
	}
}
