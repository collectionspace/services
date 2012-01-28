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

import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.MovementsCommon;
import org.collectionspace.services.movement.MovementMethodsList;

import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MovementServiceTest, carries out tests against a deployed and running
 * Movement Service.
 * 
 * $LastChangedRevision$ $LastChangedDate: 2011-11-14 23:26:36 -0800
 * (Mon, 14 Nov 2011) $
 */
public class MovementServiceTest extends
		AbstractPoxServiceTestImpl<AbstractCommonList, MovementsCommon> {

	/** The logger. */
	private final String CLASS_NAME = MovementServiceTest.class.getName();
	private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

	final String SERVICE_NAME = "movements";
	final String SERVICE_PATH_COMPONENT = "movements";

	private final static String TIMESTAMP_UTC = GregorianCalendarDateTimeUtils
			.timestampUTC();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.collectionspace.services.client.test.BaseServiceTest#getClientInstance
	 * ()
	 */
	@Override
	protected CollectionSpaceClient getClientInstance() {
		return new MovementClient();
	}

	// ---------------------------------------------------------------
	// Utility methods used by tests above
	// ---------------------------------------------------------------

	@Override
	protected String getServiceName() {
		return SERVICE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.collectionspace.services.client.test.BaseServiceTest#
	 * getServicePathComponent()
	 */
	@Override
	public String getServicePathComponent() {
		return SERVICE_PATH_COMPONENT;
	}

	/**
	 * Creates the movement instance.
	 * 
	 * @param identifier
	 *            the identifier
	 * @return the multipart output
	 */
	private PoxPayloadOut createMovementInstance(String identifier) {
		return createInstance("movementReferenceNumber-" + identifier);
	}

	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
		PoxPayloadOut result = createMovementInstance(identifier);
		return result;
	}

	/**
	 * Creates an instance of a Movement record for testing.
	 * 
	 * @param movementReferenceNumber
	 *            A movement reference number.
	 * @return Multipart output suitable for use as a payload in a create or
	 *         update request.
	 */
	@Override
	protected PoxPayloadOut createInstance(String movementReferenceNumber) {
		MovementsCommon movementCommon = new MovementsCommon();
		// FIXME: Values of currentLocation, normalLocation,
		// and movementContact should be refNames.
		movementCommon.setCurrentLocation("currentLocation value");
		movementCommon
				.setCurrentLocationFitness("currentLocationFitness value");
		movementCommon.setCurrentLocationNote("currentLocationNote value");
		movementCommon.setLocationDate(TIMESTAMP_UTC);
		movementCommon.setNormalLocation("normalLocation value");
		movementCommon.setMovementContact("movementContact value");
		MovementMethodsList movementMethodsList = new MovementMethodsList();
		List<String> methods = movementMethodsList.getMovementMethod();
		// @TODO Use properly formatted refNames for representative movement
		// methods in this example record. The values below are placeholders.
		String identifier = createIdentifier();
		methods.add("First Movement Method-" + identifier);
		methods.add("Second Movement Method-" + identifier);
		movementCommon.setMovementMethods(movementMethodsList);
		movementCommon.setMovementNote(getUTF8DataFragment());
		movementCommon.setMovementReferenceNumber(movementReferenceNumber);
		movementCommon.setPlannedRemovalDate(TIMESTAMP_UTC);
		movementCommon.setRemovalDate(""); // Test empty date value
		movementCommon.setReasonForMove("reasonForMove value");

		PoxPayloadOut multipart = new PoxPayloadOut(
				this.getServicePathComponent());
		PayloadOutputPart commonPart = multipart.addPart(
				new MovementClient().getCommonPartName(), movementCommon);

		if (logger.isDebugEnabled()) {
			logger.debug("to be created, movement common");
			logger.debug(objectAsXmlString(movementCommon,
					MovementsCommon.class));
		}

		return multipart;
	}

	@Override
	protected MovementsCommon updateInstance(MovementsCommon movementsCommon) {
		MovementsCommon result = new MovementsCommon();
		
		result.setMovementReferenceNumber("updated-"
				+ movementsCommon.getMovementReferenceNumber());
		result.setMovementNote("updated movement note-"
				+ movementsCommon.getMovementNote());
		result.setNormalLocation(""); // Test deletion of existing
												// string value

		String currentTimestamp = GregorianCalendarDateTimeUtils.timestampUTC();
		result.setPlannedRemovalDate(""); // Test deletion of existing
													// date or date/time value
		result.setRemovalDate(currentTimestamp);
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(MovementsCommon original,
			MovementsCommon updated) throws Exception {
		// By submitting an empty string in the update payload, the value of
		// this field
		// in the object created from the response payload will be null.
		Assert.assertNull(updated.getNormalLocation(),
				"Normal location in updated object did not match submitted data.");
		if (logger.isDebugEnabled()) {
			logger.debug("Normal location after update=|"
					+ updated.getNormalLocation() + "|");
		}

		Assert.assertEquals(updated.getMovementReferenceNumber(),
				original.getMovementReferenceNumber(),
				"Movement reference number in updated object did not match submitted data.");
		Assert.assertEquals(updated.getMovementNote(),
				original.getMovementNote(),
				"Movement note in updated object did not match submitted data.");
		Assert.assertNull(updated.getPlannedRemovalDate());
		Assert.assertEquals(updated.getRemovalDate(),
				original.getRemovalDate(),
				"Removal date in updated object did not match submitted data.");

		if (logger.isDebugEnabled()) {
			logger.debug("UTF-8 data sent=" + original.getMovementNote()
					+ "\n" + "UTF-8 data received="
					+ updated.getMovementNote());
		}
		Assert.assertTrue(
				updated.getMovementNote().contains(
						getUTF8DataFragment()), "UTF-8 data retrieved '"
						+ updated.getMovementNote()
						+ "' does not contain expected data '"
						+ getUTF8DataFragment());
		Assert.assertEquals(updated.getMovementNote(),
				original.getMovementNote(),
				"Movement note in updated object did not match submitted data.");
	}

	protected void compareReadInstances(MovementsCommon original,
			MovementsCommon fromRead) throws Exception {
		// Check the values of one or more date/time fields.
		if (logger.isDebugEnabled()) {
			logger.debug("locationDate=" + fromRead.getLocationDate());
			logger.debug("TIMESTAMP_UTC=" + TIMESTAMP_UTC);
		}
		Assert.assertTrue(fromRead.getLocationDate().equals(TIMESTAMP_UTC));
		Assert.assertTrue(fromRead.getPlannedRemovalDate().equals(TIMESTAMP_UTC));
		Assert.assertNull(fromRead.getRemovalDate());

		// Check the values of fields containing Unicode UTF-8 (non-Latin-1)
		// characters.
		if (logger.isDebugEnabled()) {
			logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
					+ "UTF-8 data received=" + fromRead.getMovementNote());
		}
		Assert.assertEquals(fromRead.getMovementNote(),
				getUTF8DataFragment(), "UTF-8 data retrieved '"
						+ fromRead.getMovementNote()
						+ "' does not match expected data '"
						+ getUTF8DataFragment());
	}

	/*
	 * For convenience and terseness, this test method is the base of the test
	 * execution dependency chain. Other test methods may refer to this method
	 * in their @Test annotation declarations.
	 */
	@Override
	@Test(dataProvider = "testName", dependsOnMethods = { "org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests" })
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
	}
}
