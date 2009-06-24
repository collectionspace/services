/*	
 * AlphabeticIDPartTest
 *
 * Test class for AlphabeticIDPart.
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

public class AlphabeticIDPartTest extends TestCase {

	IDPart part;
	
	public void testGetNextIDLowercase() {

		part = new AlphabeticIDPart("a");
		assertEquals("b", part.getNextID());
		assertEquals("c", part.getNextID());

		part = new AlphabeticIDPart("x");
		assertEquals("y", part.getNextID());
		assertEquals("z", part.getNextID());

}

	public void testGetNextIDLowercase2Chars() {

		part = new AlphabeticIDPart("aa");
		assertEquals("ab", part.getNextID());
		assertEquals("ac", part.getNextID());

		part = new AlphabeticIDPart("zx");
		assertEquals("zy", part.getNextID());
		assertEquals("zz", part.getNextID());

	}

	public void testGetNextIDLowercase2CharsRolloverFirst() {

		part = new AlphabeticIDPart("ay");
		assertEquals("az", part.getNextID());
		assertEquals("ba", part.getNextID());
		assertEquals("bb", part.getNextID());

  }
	
	public void testGetNextIDUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "A");
		assertEquals("B", part.getNextID());
		assertEquals("C", part.getNextID());

		part = new AlphabeticIDPart("A", "Z", "X");
		assertEquals("Y", part.getNextID());
		assertEquals("Z", part.getNextID());

}

	public void testGetNextIDUppercase2Chars() {

		part = new AlphabeticIDPart("A", "Z", "AA");
		assertEquals("AB", part.getNextID());
		assertEquals("AC", part.getNextID());

		part = new AlphabeticIDPart("A", "Z", "ZX");
		assertEquals("ZY", part.getNextID());
		assertEquals("ZZ", part.getNextID());
			
	}

	public void testGetNextIDUppercase2CharsRolloverFirst() {

		part = new AlphabeticIDPart("A", "Z", "AY");
		assertEquals("AZ", part.getNextID());
		assertEquals("BA", part.getNextID());
		assertEquals("BB", part.getNextID());

  }
  
	public void testResetLowercase() {
		
		part = new AlphabeticIDPart("zx");
		assertEquals("zy", part.getNextID());
		assertEquals("zz", part.getNextID());
		part.reset();
		assertEquals("zx", part.getCurrentID());
	
	}

	public void testResetUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "RA");
		assertEquals("RB", part.getNextID());
		assertEquals("RC", part.getNextID());
		part.reset();
		assertEquals("RB", part.getNextID());
	
	}
	
	public void testInitialLowercase() {
		
		part = new AlphabeticIDPart("aaa");
		assertEquals("aaa", part.getInitialID());
		
	}

	public void testInitialUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "AZ");
		assertEquals("AZ", part.getInitialID());
		
	}

	public void testCurrentLowercase() {
		
		part = new AlphabeticIDPart("aaa");
		assertEquals("aaa", part.getCurrentID());
		assertEquals("aab", part.getNextID());
		assertEquals("aac", part.getNextID());
		assertEquals("aac", part.getCurrentID());
		assertEquals("aad", part.getNextID());
		
	}

	public void testCurrentUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "A");
		assertEquals("A", part.getCurrentID());
		assertEquals("B", part.getNextID());
		assertEquals("C", part.getNextID());
		assertEquals("C", part.getCurrentID());
		assertEquals("D", part.getNextID());
		
	}	
	
	public void testOverflowLowercase() {
	
    part = new AlphabeticIDPart("zx");
    assertEquals("zy", part.getNextID());
    assertEquals("zz", part.getNextID());
    assertEquals("aaa", part.getNextID());
		
	}

	public void testOverflowUppercase() {
	
    part = new AlphabeticIDPart("A", "Z", "X");
    assertEquals("Y", part.getNextID());
    assertEquals("Z", part.getNextID());
    assertEquals("AA", part.getNextID());
		
	}

	public void testNonAlphabeticInitialValue() {
		try {
			part = new AlphabeticIDPart("&*432");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testNullInitialValue() {
		try {
			part = new AlphabeticIDPart(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testEmptyStringInitialValue() {
		try {
			part = new AlphabeticIDPart("");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testAllSpaceCharsInitialValue() {
		try {
			part = new AlphabeticIDPart("  ");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testIsValidIDDefaultSeries() {
	
		part = new AlphabeticIDPart();

		assertTrue(part.isValidID("a"));
		assertTrue(part.isValidID("z"));

		assertFalse(part.isValidID("A"));
		assertFalse(part.isValidID("123"));
		
	}

	public void testIsValidIDConstrainedLowerCaseSeries() {
	
		part = new AlphabeticIDPart("a", "f", "a");
		
		assertTrue(part.isValidID("a"));
		assertTrue(part.isValidID("b"));
		assertTrue(part.isValidID("f"));

		assertFalse(part.isValidID("g"));
		assertFalse(part.isValidID("z"));
		assertFalse(part.isValidID("A"));
		assertFalse(part.isValidID("123"));
		
	}

	public void testIsValidIDConstrainedUppercaseSeries() {
	
		part = new AlphabeticIDPart("A", "F", "A");

		assertTrue(part.isValidID("A"));
		assertTrue(part.isValidID("B"));
		assertTrue(part.isValidID("F"));

		assertFalse(part.isValidID("G"));
		assertFalse(part.isValidID("Z"));
		assertFalse(part.isValidID("a"));
		assertFalse(part.isValidID("123"));
		
	}

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
