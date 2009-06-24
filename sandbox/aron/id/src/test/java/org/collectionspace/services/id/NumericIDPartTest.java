/*	
 * NumericIDPartTest
 *
 * Test class for NumericIDPart.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */

package org.collectionspace.services.id;

import static org.junit.Assert.fail;
import junit.framework.TestCase;

public class NumericIDPartTest extends TestCase {

	IDPart part;

	public void testNextID() {

		part = new NumericIDPart("0");		
		assertEquals("1", part.nextID());
		assertEquals("2", part.nextID());
		assertEquals("3", part.nextID());

		part = new NumericIDPart("25");
		assertEquals("26", part.nextID());
		assertEquals("27", part.nextID());
		assertEquals("28", part.nextID());
			
	}

	public void testNextIDOverflow() {

		try {
			part = new NumericIDPart("997", "3");		
			assertEquals("998", part.nextID());
			assertEquals("999", part.nextID());
			assertEquals("1000", part.nextID());
			fail("Should have thrown IllegalStateException here");
		} catch (IllegalStateException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

		// Tests default MAX_LENGTH value of 6 decimal places
		try {
			part = new NumericIDPart("999997");		
			assertEquals("999998", part.nextID());
			assertEquals("999999", part.nextID());
			assertEquals("1000000", part.nextID());
			fail("Should have thrown IllegalStateException here");
		} catch (IllegalStateException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
			
	}

	public void testresetID() {
	
		part = new NumericIDPart("25");
		assertEquals("26", part.nextID());
		assertEquals("27", part.nextID());
		assertEquals("28", part.nextID());
		part.resetID();
		assertEquals("26", part.nextID());
			
	}

	public void testInitialID() {

		part = new NumericIDPart("0");
		assertEquals("0", part.getInitialID());

		part = new NumericIDPart("25");
		assertEquals("25", part.getInitialID());
		
	}

	public void testCurrentID() {

		part = new NumericIDPart("0");
		assertEquals("0", part.getCurrentID());
		assertEquals("1", part.nextID());
		assertEquals("2", part.nextID());
		assertEquals("2", part.getCurrentID());
		assertEquals("3", part.nextID());

		part = new NumericIDPart("25");
		assertEquals("25", part.getCurrentID());
		
	}
	
	public void testNullInitialValue() {
	
		try {
			part = new NumericIDPart(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testNonLongParseableInitialValue() {
	
		try {
			part = new NumericIDPart("not a long parseable value");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

	}

	public void testNonLongParseableMaxLength() {
	
		try {
			part = new NumericIDPart("1", "not an int parseable value");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

	}

	public void testIsValidID() {
	
		part = new NumericIDPart("1");
		assertTrue(part.isValidID("1"));

		part = new NumericIDPart("1");
		assertTrue(part.isValidID("123"));

		part = new NumericIDPart("1");
		assertTrue(part.isValidID("123456"));

		part = new NumericIDPart("1");
		assertFalse(part.isValidID("1234567"));
		
		part = new NumericIDPart("1", "3");
		assertTrue(part.isValidID("123"));
		
		part = new NumericIDPart("1", "3");
		assertFalse(part.isValidID("1234"));

		part = new NumericIDPart("1");
		assertFalse(part.isValidID("not a parseable long"));

		part = new NumericIDPart("1", "3");
		assertFalse(part.isValidID("not a parseable long"));
	
	}	
	
	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
