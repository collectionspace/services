/*	
 * YearIDPartTest
 *
 * Test class for YearIDPart.
 *
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
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */

package org.collectionspace.services.id;

import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.GregorianCalendar;
import junit.framework.TestCase;

public class YearIDPartTest extends TestCase {

	IDPart part;
	String year = "1999";

	public String getCurrentYear() {
		Calendar cal = GregorianCalendar.getInstance();
    int y = cal.get(Calendar.YEAR);
		return Integer.toString(y);
	}

	public void testCurrentID() {

		part = new YearIDPart();
		assertEquals(getCurrentYear(), part.getCurrentID());

		part = new YearIDPart(year);
		assertEquals(year, part.getCurrentID());

	}

	public void testSetCurrentID() {

		part = new YearIDPart("1999");
		part.setCurrentID("1999");
		assertEquals("1999", part.getCurrentID());
		part.setCurrentID("2000");
		assertEquals("2000", part.getCurrentID());

	}

	public void testSetCurrentIDNullOrEmpty() {

		part = new YearIDPart();

		try {
		  part.setCurrentID(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

		try {
		  part.setCurrentID("");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

  }
	
	public void testNextID() {
	
		part = new YearIDPart("1999");		
		assertEquals("1999", part.nextID());		
		
	}

	public void testresetID() {
	
		part = new YearIDPart("1999");
		assertEquals("1999", part.nextID());
		part.resetID();
		assertEquals("1999", part.getCurrentID());
			
	}

	public void testInitialID() {
	
		part = new YearIDPart("1999");
		assertEquals("1999", part.getInitialID());
		
	}


	public void testNullInitialValue() {
	
		try {
			part = new YearIDPart(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testEmptyInitialValue() {
	
		try {
			part = new YearIDPart("");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

	}

	public void testIsValidID() {
	
		part = new YearIDPart();
		assertTrue(part.isValidID("2009"));

		part = new YearIDPart();
		assertFalse(part.isValidID("839"));

		part = new YearIDPart();
		assertFalse(part.isValidID("10100"));
		
		part = new YearIDPart();
		assertFalse(part.isValidID("non-numeric value"));

    part = new YearIDPart();
    assertFalse(part.isValidID(null));
    
    part = new YearIDPart();
    assertFalse(part.isValidID(""));

	}

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
