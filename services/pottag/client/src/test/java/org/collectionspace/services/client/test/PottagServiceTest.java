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
import org.collectionspace.services.client.PottagClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.pottag.PottagsCommon;

import org.testng.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PottagServiceTest, carries out tests against a
 * deployed and running Pottag (aka Pot Tag) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class PottagServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, PottagsCommon> {

    /** The logger. */
    private final String CLASS_NAME = PottagServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    private final static String CURRENT_DATE_UTC = GregorianCalendarDateTimeUtils.currentDateUTC();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new PottagClient();
    }

    @Override
	protected void compareReadInstances(PottagsCommon original, PottagsCommon pottagCommon) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + pottagCommon.getLabelData());
        }

        Assert.assertEquals(pottagCommon.getLabelData(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + pottagCommon.getLabelData()
                + "' does not match expected data '" + getUTF8DataFragment());		
	}    
     
	@Override
	protected void compareUpdatedInstances(PottagsCommon pottagCommon,
			PottagsCommon updatedPottagCommon) throws Exception {
        // Check selected fields in the updated common part.
        Assert.assertEquals(updatedPottagCommon.getFamily(), pottagCommon.getFamily(),
                "Data in updated object did not match submitted data.");

        if (logger.isDebugEnabled()) {
            logger.debug("UTF-8 data sent=" + pottagCommon.getLabelData() + "\n"
                    + "UTF-8 data received=" + updatedPottagCommon.getLabelData());
        }
        Assert.assertTrue(updatedPottagCommon.getLabelData().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedPottagCommon.getLabelData() + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedPottagCommon.getLabelData(),
                pottagCommon.getLabelData(), "Data in updated object did not match submitted data.");
	}
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    public String getServiceName() {
        return PottagClient.SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return PottagClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        return createPottagInstance(identifier);
    }

    /**
     * Creates the pottag instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createPottagInstance(String identifier) throws Exception {
        return createPottagInstance(
                "family-" + identifier,
                "returnDate-" + identifier);
    }

    /**
     * Creates the pottag instance.
     *
     * @param familyName the pottag family
     * @param returnDate the return date
     * @return the multipart output
     * @throws Exception 
     */
    private PoxPayloadOut createPottagInstance(String familyName,
            String returnDate) throws Exception {

        PottagsCommon pottagCommon = new PottagsCommon();
        pottagCommon.setFamily(familyName);
        pottagCommon.setLocale("Mexico");
        pottagCommon.setLabelData(getUTF8DataFragment());

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(new PottagClient().getCommonPartName(), pottagCommon);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, pottag common");
            logger.debug(objectAsXmlString(pottagCommon, PottagsCommon.class));
        }

        return multipart;
    }

	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) throws Exception {
        PoxPayloadOut result = createPottagInstance(identifier);
        return result;
	}

	@Override
	protected PottagsCommon updateInstance(PottagsCommon commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
