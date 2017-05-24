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
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.ClaimClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.claim.ClaimsCommon;
import org.collectionspace.services.claim.ResponsibleDepartmentsList;
import org.collectionspace.services.claim.ClaimantGroupList;
import org.collectionspace.services.claim.ClaimantGroup;
import org.collectionspace.services.claim.ClaimReceivedGroupList;
import org.collectionspace.services.claim.ClaimReceivedGroup;

import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClaimServiceTest, carries out tests against a
 * deployed and running Claim Service.
 *
 * $LastChangedRevision: 5952 $
 * $LastChangedDate: 2011-11-14 23:26:36 -0800 (Mon, 14 Nov 2011) $
 */
public class ClaimServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, ClaimsCommon> {

   /** The logger. */
    private final String CLASS_NAME = ClaimServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.timestampUTC();
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
    	return new ClaimClient();
    }
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    @Override
    protected String getServiceName() {
        return ClaimClient.SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return ClaimClient.SERVICE_PATH_COMPONENT;
    }

    /**
     * Creates the claim instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createClaimInstance(String identifier) throws Exception {
        return createInstance("claimNumber-" + identifier);
    }

    /**
     * Creates an instance of a Claim record for testing.
     *
     * @param claimNumber A claim number.
     * @return Multipart output suitable for use as a payload
     *     in a create or update request.
     * @throws Exception 
     */
    @Override
    protected PoxPayloadOut createInstance(String claimNumber) throws Exception {
        ClaimsCommon claimCommon = new ClaimsCommon();

        ResponsibleDepartmentsList responsibleDepartmentsList = new ResponsibleDepartmentsList();
        List<String> responsibleDepartments = responsibleDepartmentsList.getResponsibleDepartment();
        String identifier = createIdentifier();
        responsibleDepartments.add("First Responsible Department-" + identifier);
        responsibleDepartments.add("Second Responsible Department-" + identifier);

        ClaimantGroupList claimantGroupList = new ClaimantGroupList();
        ClaimantGroup claimantGroup = new ClaimantGroup();
        claimantGroup.setClaimFiledBy("urn:cspace:core.collectionspace.org:personauthorities:name(TestPersonAuth):item:name(carrieClaimFiler)'Carrie ClaimFiler'");
        claimantGroup.setClaimFiledOnBehalfOf("urn:cspace:core.collectionspace.org:personauthorities:name(TestPersonAuth):item:name(benBehalfOf)'Ben BehalfOf'");
        claimantGroup.setClaimantNote(getUTF8DataFragment());
        claimantGroupList.getClaimantGroup().add(claimantGroup);

        ClaimReceivedGroupList claimReceivedGroupList = new ClaimReceivedGroupList();
        ClaimReceivedGroup claimReceivedGroup = new ClaimReceivedGroup();
        claimReceivedGroup.setClaimReceivedDate(CURRENT_DATE_UTC);
        claimReceivedGroup.setClaimReceivedNote(getUTF8DataFragment());
        claimReceivedGroupList.getClaimReceivedGroup().add(claimReceivedGroup);

        claimCommon.setResponsibleDepartments(responsibleDepartmentsList);
        claimCommon.setClaimantGroupList(claimantGroupList);
        claimCommon.setClaimReceivedGroupList(claimReceivedGroupList);
        claimCommon.setClaimNumber(claimNumber);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = multipart.addPart(claimCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ClaimClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, claim common");
            logger.debug(objectAsXmlString(claimCommon, ClaimsCommon.class));
        }

        return multipart;
    }

    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */    
	@Override
	public void CRUDTests(String testName) {
		// Needed for TestNG dependency chain.
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName, String identifier) throws Exception {
        PoxPayloadOut result = createClaimInstance(identifier);
        return result;
	}

	@Override
	protected ClaimsCommon updateInstance(ClaimsCommon claimCommon) {
	    // Update its content.
	    claimCommon.setClaimNumber(""); // Test deletion of existing string value
	
	    String claimNote = claimCommon.getClaimantGroupList().getClaimantGroup().get(0).getClaimantNote();
	    claimCommon.getClaimantGroupList().getClaimantGroup().get(0).setClaimantNote("updated claim note-" + claimNote);
	
	    String currentTimestamp = GregorianCalendarDateTimeUtils.timestampUTC();
	    claimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).setClaimReceivedDate(currentTimestamp);
	    claimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).setClaimReceivedNote("");
	    
	    return claimCommon;
	}

    @Override
	protected void compareReadInstances(ClaimsCommon original, ClaimsCommon fromRead) throws Exception {
    	// Check selected fields.

        // Check the values of one or more date/time fields.
        String receivedDate = fromRead.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).getClaimReceivedDate();

        if (logger.isDebugEnabled()) {
            logger.debug("receivedDate=" + receivedDate);
            logger.debug("TIMESTAMP_UTC=" + CURRENT_DATE_UTC);
        }
        Assert.assertTrue(receivedDate.equals(CURRENT_DATE_UTC));
        
        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
        String claimNote = fromRead.getClaimantGroupList().getClaimantGroup().get(0).getClaimantNote();
        
        if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + claimNote);
        }
        Assert.assertEquals(claimNote, getUTF8DataFragment(),
                "UTF-8 data retrieved '" + claimNote
                + "' does not match expected data '" + getUTF8DataFragment());
    }
	
	@Override
	protected void compareUpdatedInstances(ClaimsCommon claimCommon, ClaimsCommon updatedClaimCommon) throws Exception {
		String originalClaimNote = claimCommon.getClaimantGroupList().getClaimantGroup().get(0).getClaimantNote();
        String updatedClaimNote = updatedClaimCommon.getClaimantGroupList().getClaimantGroup().get(0).getClaimantNote();

        Assert.assertEquals(updatedClaimNote, originalClaimNote,
            "Data in updated object did not match submitted data.");

        Assert.assertNotSame(claimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).getClaimReceivedDate(),
        		updatedClaimCommon.getClaimReceivedGroupList().getClaimReceivedGroup().get(0).getClaimReceivedDate(),
            "Data in updated object did not match submitted data.");

        if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + originalClaimNote + "\n"
                    + "UTF-8 data received=" + updatedClaimNote);
        }
        Assert.assertTrue(updatedClaimNote.contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedClaimNote
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedClaimNote,
                originalClaimNote,
                "Data in updated object did not match submitted data.");
}

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
		// TODO Auto-generated method stub
		return new ClaimClient();
	}
}
