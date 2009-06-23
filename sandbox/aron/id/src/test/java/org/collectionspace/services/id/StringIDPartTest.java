/*	
 * StringIDPartTest
 *
 * Test class for StringIDPart.
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

public class StringIDPartTest extends TestCase {

	IDPart part;

	public void testNextID() {
		part = new StringIDPart("XYZ");		
		assertEquals("XYZ", part.getNextID());			
	}

	public void testReset() {
	
		part = new StringIDPart(".");
		assertEquals(".", part.getNextID());
		part.reset();
		assertEquals(".", part.getNextID());
			
	}

	public void testInitialID() {
		part = new StringIDPart("-");
		assertEquals("-", part.getInitialID());
	}

	public void testCurrentID() {
		part = new StringIDPart("- -");
		assertEquals("- -", part.getCurrentID());
	}
	
	public void testNullInitialValue() {
	
		try {
			part = new StringIDPart(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testEmptyInitialValue() {
	
		try {
			part = new StringIDPart("");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

	}

	public void testIsValidID() {
	
		part = new StringIDPart("-");
		assertTrue(part.isValidID("-"));

		part = new StringIDPart("-");
		assertFalse(part.isValidID("--"));

		// Test chars with special meaning in regexes.
		part = new StringIDPart(".");
		assertTrue(part.isValidID("."));

		part = new StringIDPart("TE");
		assertTrue(part.isValidID("TE"));

		part = new StringIDPart("TE");
		assertFalse(part.isValidID("T"));

		part = new StringIDPart("T");
		assertFalse(part.isValidID("TE"));
	
	}	

	public void testNullValidationValue() {
	
		try {
			part = new StringIDPart("-");
			assertFalse(part.isValidID(null));
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

	}	
	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
