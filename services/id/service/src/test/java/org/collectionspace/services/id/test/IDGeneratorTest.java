/*	
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
 */

package org.collectionspace.services.id;

import static org.junit.Assert.fail;
import java.util.Vector;
import junit.framework.TestCase;

/**	
 * IDGeneratorTest, Test class for IDGenerator.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class IDGeneratorTest extends TestCase {

	BaseIDGenerator generator;
	IDGeneratorPart part;
	
	final static String DEFAULT_CSID = "1";

	// Note: tests may fail with IllegalArgumentException
	// if any initialization of new IDParts fails
	// due to invalid arguments passed to their constructors.

	public void testCurrentIDViaVector() {

		Vector parts = new Vector();
		parts.add(new YearIDGeneratorPart("2009"));
		parts.add(new StringIDGeneratorPart("."));
		parts.add(new NumericIDGeneratorPart("1"));
		parts.add(new StringIDGeneratorPart("-"));
		parts.add(new AlphabeticIDGeneratorPart("a"));
		generator = new BaseIDGenerator(DEFAULT_CSID, parts);
		assertEquals("2009.1-a", generator.getCurrentID());
			
	}

	public void testCurrentIDViaAdd() {

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("-"));
		generator.add(new AlphabeticIDGeneratorPart("a"));	
		assertEquals("2009.1-a", generator.getCurrentID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("0"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("0"));
		assertEquals("2009.0.0", generator.getCurrentID());
			
	}

	public void testCurrentIDWithPartialSuppliedID() {
			
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new StringIDGeneratorPart("E"));
		generator.add(new NumericIDGeneratorPart("1"));
		assertEquals("E1", generator.getCurrentID("E"));
		assertEquals("E2", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart());
		generator.add(new StringIDGeneratorPart("."));
		assertEquals("2009.", generator.getCurrentID("2009"));
		assertEquals("2009.", generator.newID());
		assertEquals("2010.", generator.getCurrentID("2010"));
		assertEquals("2010.", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart());
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		assertEquals("2009.1", generator.getCurrentID("2009."));
		assertEquals("2009.2", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart());
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("55"));
		assertEquals("2010.55", generator.getCurrentID("2010."));
		assertEquals("2010.56", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart());
		assertEquals("2009.1", generator.getCurrentID("2009."));
		assertEquals("2009.2", generator.newID());
		// Test a repeat of the last two operations.
		assertEquals("2009.1", generator.getCurrentID("2009."));
		assertEquals("2009.2", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("-"));
		generator.add(new AlphabeticIDGeneratorPart("a"));
		assertEquals("2009.1-a", generator.getCurrentID("2009.1-"));
		assertEquals("2009.1-b", generator.newID());
		assertEquals("2009.3-a", generator.getCurrentID("2009.3-"));

	}

	public void testCurrentIDWithFullSuppliedID() {
	
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("55"));
		assertEquals("2009.55", generator.getCurrentID("2009.55"));
		assertEquals("2009.56", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("-"));
		generator.add(new AlphabeticIDGeneratorPart("a"));
		assertEquals("2009.1-a", generator.getCurrentID("2009.1-a"));
		assertEquals("2009.1-b", generator.newID());

	}

	public void testNewID() {

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		assertEquals("2009.2", generator.newID());
		assertEquals("2009.3", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("-"));
		generator.add(new AlphabeticIDGeneratorPart("a"));
		assertEquals("2009.1-b", generator.newID());
		assertEquals("2009.1-c", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new StringIDGeneratorPart("T"));
		generator.add(new NumericIDGeneratorPart("1005"));
		assertEquals("T1006", generator.newID());
		assertEquals("T1007", generator.newID());

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		assertEquals("2009.1.2", generator.newID());
			
	}

	public void testNewIDWithConstantStringID() {
	
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("-"));
		assertEquals("2009.1-", generator.newID());
		assertEquals("2009.1-", generator.newID());

	}

	public void testNewIDWithSuppliedID() {
	
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		assertEquals("2009.2", generator.newID("2009.1"));
		assertEquals("2009.3", generator.newID("2009.2"));

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		generator.add(new StringIDGeneratorPart("-"));
		generator.add(new AlphabeticIDGeneratorPart("a"));
		assertEquals("2009.1-b", generator.newID("2009.1-a"));
		assertEquals("2009.3-c", generator.newID("2009.3-b"));

	}

	public void testEmptyPartsListCurrentID() {

		generator = new BaseIDGenerator(DEFAULT_CSID);
		assertEquals("", generator.getCurrentID());
			
	}

	public void testIsValidIDYearPattern() {
	
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));

		assertTrue(generator.isValidID("2009"));
		assertTrue(generator.isValidID("5555"));

		assertFalse(generator.isValidID("456"));
		assertFalse(generator.isValidID("10000"));
		
	}


	public void testGetRegex() {
	
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		assertEquals("(\\d{4})(\\.)(\\d{1,6})", generator.getRegex());
	
	}

	public void testIsValidIDYearSeparatorItemPattern() {
	
		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("."));
		generator.add(new NumericIDGeneratorPart("1"));
		
		assertTrue(generator.isValidID("2009.1"));
		assertTrue(generator.isValidID("5555.55"));

		assertFalse(generator.isValidID("456.1"));
		assertFalse(generator.isValidID("2009-1"));
		assertFalse(generator.isValidID("2009.a"));
		assertFalse(generator.isValidID("2009-a"));
		assertFalse(generator.isValidID("non-generator conforming text"));

		generator = new BaseIDGenerator(DEFAULT_CSID);
		generator.add(new YearIDGeneratorPart("2009"));
		generator.add(new StringIDGeneratorPart("ZZ.AND."));
		generator.add(new NumericIDGeneratorPart("1"));

		assertTrue(generator.isValidID("2009ZZ.AND.1"));
		assertFalse(generator.isValidID("2009ZZ-AND-1"));
	
	}

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
