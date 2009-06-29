/*	
 * AlphabeticIDPartTest
 *
 * Test class for AlphabeticIDPart.
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
import junit.framework.TestCase;

public class AlphabeticIDPartTest extends TestCase {

	IDPart part;
	
	public void testnextIDLowercase() {

		part = new AlphabeticIDPart("a");
		assertEquals("b", part.nextID());
		assertEquals("c", part.nextID());

		part = new AlphabeticIDPart("x");
		assertEquals("y", part.nextID());
		assertEquals("z", part.nextID());

}

	public void testnextIDLowercase2Chars() {

		part = new AlphabeticIDPart("aa");
		assertEquals("ab", part.nextID());
		assertEquals("ac", part.nextID());

		part = new AlphabeticIDPart("zx");
		assertEquals("zy", part.nextID());
		assertEquals("zz", part.nextID());

	}

	public void testnextIDLowercase2CharsRolloverFirst() {

		part = new AlphabeticIDPart("ay");
		assertEquals("az", part.nextID());
		assertEquals("ba", part.nextID());
		assertEquals("bb", part.nextID());

  }
	
	public void testnextIDUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "A");
		assertEquals("B", part.nextID());
		assertEquals("C", part.nextID());

		part = new AlphabeticIDPart("A", "Z", "X");
		assertEquals("Y", part.nextID());
		assertEquals("Z", part.nextID());

}

	public void testnextIDUppercase2Chars() {

		part = new AlphabeticIDPart("A", "Z", "AA");
		assertEquals("AB", part.nextID());
		assertEquals("AC", part.nextID());

		part = new AlphabeticIDPart("A", "Z", "ZX");
		assertEquals("ZY", part.nextID());
		assertEquals("ZZ", part.nextID());
			
	}

	public void testnextIDUppercase2CharsRolloverFirst() {

		part = new AlphabeticIDPart("A", "Z", "AY");
		assertEquals("AZ", part.nextID());
		assertEquals("BA", part.nextID());
		assertEquals("BB", part.nextID());

  }
  
	public void testresetIDLowercase() {
		
		part = new AlphabeticIDPart("zx");
		assertEquals("zy", part.nextID());
		assertEquals("zz", part.nextID());
		part.resetID();
		assertEquals("zx", part.getCurrentID());
	
	}

	public void testresetIDUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "RA");
		assertEquals("RB", part.nextID());
		assertEquals("RC", part.nextID());
		part.resetID();
		assertEquals("RB", part.nextID());
	
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
		assertEquals("aab", part.nextID());
		assertEquals("aac", part.nextID());
		assertEquals("aac", part.getCurrentID());
		assertEquals("aad", part.nextID());
		
	}

	public void testCurrentUppercase() {
		
		part = new AlphabeticIDPart("A", "Z", "A");
		assertEquals("A", part.getCurrentID());
		assertEquals("B", part.nextID());
		assertEquals("C", part.nextID());
		assertEquals("C", part.getCurrentID());
		assertEquals("D", part.nextID());
		
	}	
	
	public void testOverflowLowercase() {
	
    part = new AlphabeticIDPart("zx");
    assertEquals("zy", part.nextID());
    assertEquals("zz", part.nextID());
    assertEquals("aaa", part.nextID());
		
	}

	public void testOverflowUppercase() {
	
    part = new AlphabeticIDPart("A", "Z", "X");
    assertEquals("Y", part.nextID());
    assertEquals("Z", part.nextID());
    assertEquals("AA", part.nextID());
		
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
