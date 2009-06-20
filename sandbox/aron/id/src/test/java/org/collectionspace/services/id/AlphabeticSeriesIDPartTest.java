 /*	
 * AlphabeticSeriesIDPartTest
 *
 * Test class for AlphabeticSeriesIDPart.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author$
 * @version $Revision$
 * $Date$
 */

package org.collectionspace.services.id;

import junit.framework.TestCase;

public class AlphabeticSeriesIDPartTest extends TestCase {

	AlphabeticSeriesIDPart part;
	
	public void testInitialValue() {

		part = new AlphabeticSeriesIDPart("a");
		assertEquals("a", part.nextIdentifier());
		assertEquals("b", part.nextIdentifier());

		part = new AlphabeticSeriesIDPart("x");
		assertEquals("x", part.nextIdentifier());
		assertEquals("y", part.nextIdentifier());
		assertEquals("z", part.nextIdentifier());
		part.reset();
		assertEquals("x", part.nextIdentifier());
			
	}
	
	// Add tests of boundary conditions, exceptions ...
 
}
