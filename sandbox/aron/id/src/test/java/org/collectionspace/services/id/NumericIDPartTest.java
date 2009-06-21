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
		assertEquals("1", part.getNextID());
		assertEquals("2", part.getNextID());
		assertEquals("3", part.getNextID());

		part = new NumericIDPart("25");
		assertEquals("26", part.getNextID());
		assertEquals("27", part.getNextID());
		assertEquals("28", part.getNextID());
			
	}

	public void testReset() {
	
		part = new NumericIDPart("25");
		assertEquals("26", part.getNextID());
		assertEquals("27", part.getNextID());
		assertEquals("28", part.getNextID());
		part.reset();
		assertEquals("26", part.getNextID());
			
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
		assertEquals("1", part.getNextID());
		assertEquals("2", part.getNextID());
		assertEquals("2", part.getCurrentID());
		assertEquals("3", part.getNextID());

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
	
	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
