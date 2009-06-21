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

	public void testCurrentID() {

		Vector parts = new Vector();
		parts.add(new YearIDPart("2009"));
		parts.add(new StringIDPart("."));
		parts.add(new NumericIDPart("1"));
		pattern = new IDPattern(parts);
		
		assertEquals("2009.1", pattern.getCurrentID());
			
	}

	public void testAddCurrentID() {

		pattern = new IDPattern();
		pattern.add(new YearIDPart("2009"));
		pattern.add(new StringIDPart("."));
		pattern.add(new NumericIDPart("1"));
		
		assertEquals("2009.1", pattern.getCurrentID());
			
	}

	public void testEmptyPartsListCurrentID() {

		pattern = new IDPattern();
		assertEquals("", pattern.getCurrentID());
			
	}

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
