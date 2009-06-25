/*	
 * IDPatternTest
 *
 * Test class for IDPattern.
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
import java.util.Vector;
import junit.framework.TestCase;

public class IDPatternTest extends TestCase {

	IDPattern pattern;
	IDPart part;

	// Note: tests may fail with IllegalArgumentException
	// if any initialization of new IDParts fails
	// due to invalid arguments passed to their constructors.

	public void testCurrentIDViaVector() {

		Vector parts = new Vector();
		parts.add(new YearIDPart("2009"));
		parts.add(new StringIDPart("."));
		parts.add(new NumericIDPart("1"));
		parts.add(new StringIDPart("-"));
		parts.add(new AlphabeticIDPart("a"));
		pattern = new IDPattern(parts);
		assertEquals("2009.1-a", pattern.getCurrentID());
			
	}

	public void testCurrentIDViaAdd() {

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		pattern.add(new StringIDPart("-"));
		pattern.add(new AlphabeticIDPart("a"));	
		assertEquals("2009.1-a", pattern.getCurrentID());
			
	}

	public void testCurrentIDWithPartialSuppliedID() {
	
		// @TODO: Temporary for testing: ascertain regex patterns
		
		pattern = new IDPattern();
		pattern.add(new StringIDPart("E"));
		pattern.add(new NumericIDPart("1"));
		assertEquals("E1", pattern.getCurrentID("E"));
		assertEquals("E2", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart());
		pattern.add(new StringIDPart("."));
		assertEquals("2009.", pattern.getCurrentID("2009"));
		assertEquals("2009.", pattern.nextID());
		assertEquals("2010.", pattern.getCurrentID("2010"));
		assertEquals("2010.", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart());
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		assertEquals("2009.1", pattern.getCurrentID("2009."));
		assertEquals("2009.2", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart());
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("55"));
		assertEquals("2010.55", pattern.getCurrentID("2010."));
		assertEquals("2010.56", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart());
		assertEquals("2009.1", pattern.getCurrentID("2009."));
		assertEquals("2009.2", pattern.nextID());
		// Test a repeat of the last two operations.
		assertEquals("2009.1", pattern.getCurrentID("2009."));
		assertEquals("2009.2", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		pattern.add(new StringIDPart("-"));
		pattern.add(new AlphabeticIDPart("a"));
		assertEquals("2009.1-a", pattern.getCurrentID("2009.1-"));
		assertEquals("2009.1-b", pattern.nextID());
		assertEquals("2009.3-a", pattern.getCurrentID("2009.3-"));

	}

	public void testCurrentIDWithFullSuppliedID() {
	
		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("55"));
		assertEquals("2009.55", pattern.getCurrentID("2009.55"));
		assertEquals("2009.56", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		pattern.add(new StringIDPart("-"));
		pattern.add(new AlphabeticIDPart("a"));
		assertEquals("2009.1-a", pattern.getCurrentID("2009.1-a"));
		assertEquals("2009.1-b", pattern.nextID());

	}

	public void testNextID() {

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		assertEquals("2009.2", pattern.nextID());
		assertEquals("2009.3", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		pattern.add(new StringIDPart("-"));
		pattern.add(new AlphabeticIDPart("a"));
		assertEquals("2009.1-b", pattern.nextID());
		assertEquals("2009.1-c", pattern.nextID());

		pattern = new IDPattern();
		pattern.add(new StringIDPart("T"));
		pattern.add(new NumericIDPart("1005"));
		assertEquals("T1006", pattern.nextID());
		assertEquals("T1007", pattern.nextID());
			
	}

	public void testNextIDWithConstantStringID() {
	
		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		pattern.add(new StringIDPart("-"));
		assertEquals("2009.1-", pattern.nextID());
		assertEquals("2009.1-", pattern.nextID());

	}

	public void testNextIDWithSuppliedID() {
	
		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		assertEquals("2009.2", pattern.nextID("2009.1"));
		assertEquals("2009.3", pattern.nextID("2009.2"));

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		pattern.add(new StringIDPart("-"));
		pattern.add(new AlphabeticIDPart("a"));
		assertEquals("2009.1-b", pattern.nextID("2009.1-a"));
		assertEquals("2009.3-c", pattern.nextID("2009.3-b"));

	}
	
	public void testEmptyPartsListCurrentID() {

		pattern = new IDPattern();
		assertEquals("", pattern.getCurrentID());
			
	}

	public void testIsValidIDYearPattern() {
	
		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));

		assertTrue(pattern.isValidID("2009"));
		assertTrue(pattern.isValidID("5555"));

		assertFalse(pattern.isValidID("456"));
		assertFalse(pattern.isValidID("10000"));
		
	}


	public void testGetRegex() {
	
		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		assertEquals("(\\d{4})(\\.)(\\d{1,6})", pattern.getRegex());
	
	}

	public void testIsValidIDYearSeparatorItemPattern() {
	
		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		
		assertTrue(pattern.isValidID("2009.1"));
		assertTrue(pattern.isValidID("5555.55"));

		assertFalse(pattern.isValidID("456.1"));
		assertFalse(pattern.isValidID("2009-1"));
		assertFalse(pattern.isValidID("2009.a"));
		assertFalse(pattern.isValidID("2009-a"));
		assertFalse(pattern.isValidID("non-pattern conforming text"));

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("ZZ.AND."));
		pattern.add(new NumericIDPart("1"));

		assertTrue(pattern.isValidID("2009ZZ.AND.1"));
		assertFalse(pattern.isValidID("2009ZZ-AND-1"));
	
	}

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
