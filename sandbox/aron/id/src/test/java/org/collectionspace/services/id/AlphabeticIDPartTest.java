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

		part = new AlphabeticIDPart("aa");
		assertEquals("ab", part.getNextID());
		assertEquals("ac", part.getNextID());

		part = new AlphabeticIDPart("ay");
		assertEquals("az", part.getNextID());
		assertEquals("ba", part.getNextID());
		assertEquals("bb", part.getNextID());

		part = new AlphabeticIDPart("zx");
		assertEquals("zy", part.getNextID());
		assertEquals("zz", part.getNextID());

	}
	
	public void testGetNextIDUppercase() {
		
		part = new AlphabeticIDPart("A");
		assertEquals("B", part.getNextID());
		assertEquals("C", part.getNextID());

		part = new AlphabeticIDPart("X");
		assertEquals("Y", part.getNextID());
		assertEquals("Z", part.getNextID());

		part = new AlphabeticIDPart("AA");
		assertEquals("AB", part.getNextID());
		assertEquals("AC", part.getNextID());

		part = new AlphabeticIDPart("AY");
		assertEquals("AZ", part.getNextID());
		assertEquals("BA", part.getNextID());
		assertEquals("BB", part.getNextID());

		part = new AlphabeticIDPart("ZX");
		assertEquals("ZY", part.getNextID());
		assertEquals("ZZ", part.getNextID());
			
	}

	public void testResetLowercase() {
		
		part = new AlphabeticIDPart("zx");
		assertEquals("zy", part.getNextID());
		assertEquals("zz", part.getNextID());
		part.reset();
		assertEquals("zx", part.getCurrentID());
	
	}

	public void testResetUppercase() {
		
		part = new AlphabeticIDPart("RA");
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
		
		part = new AlphabeticIDPart("AZ");
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
		
		part = new AlphabeticIDPart("A");
		assertEquals("A", part.getCurrentID());
		assertEquals("B", part.getNextID());
		assertEquals("C", part.getNextID());
		assertEquals("C", part.getCurrentID());
		assertEquals("D", part.getNextID());
		
	}	
	
	public void testOverflowLowercase() {
	
		try {
			part = new AlphabeticIDPart("zx");
			assertEquals("zy", part.getNextID());
			assertEquals("zz", part.getNextID());
			// Should throw IllegalStateException
			assertNotNull(part.getNextID());
			fail("Should have thrown IllegalStateException here");
		} catch (IllegalStateException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testOverflowUppercase() {
	
		try {
			part = new AlphabeticIDPart("X");
			assertEquals("Y", part.getNextID());
			assertEquals("Z", part.getNextID());
			// Should throw IllegalStateException
			assertNotNull(part.getNextID());
			fail("Should have thrown IllegalStateException here");
		} catch (IllegalStateException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
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

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
