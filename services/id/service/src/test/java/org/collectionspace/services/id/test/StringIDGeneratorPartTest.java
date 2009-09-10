/**
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.collectionspace.services.id;

import static org.junit.Assert.fail;
import junit.framework.TestCase;

/**	
 * StringIDGeneratorPartTest
 *
 * Test class for StringIDGeneratorPart.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class StringIDGeneratorPartTest extends TestCase {

	StoredValueIDGeneratorPart part;

	public void testnewID() {

		part = new StringIDGeneratorPart("E");		
		assertEquals("E", part.newID());	
		
		part = new StringIDGeneratorPart("XYZ");		
		assertEquals("XYZ", part.newID());		
		
	}

/*
	public void testresetID() {
	
		part = new StringIDGeneratorPart(".");
		assertEquals(".", part.newID());
		part.resetID();
		assertEquals(".", part.newID());
			
	}
*/

	public void testInitialID() {
		part = new StringIDGeneratorPart("-");
		assertEquals("-", part.getInitialID());
	}

	public void testCurrentID() {
		part = new StringIDGeneratorPart("- -");
		assertEquals("- -", part.getCurrentID());
	}
	
	public void testNullInitialValue() {
	
		try {
			part = new StringIDGeneratorPart(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testEmptyInitialValue() {
	
		try {
			part = new StringIDGeneratorPart("");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}

	}

	public void testIsValidID() {
	
		part = new StringIDGeneratorPart("-");
		assertTrue(part.isValidID("-"));

		part = new StringIDGeneratorPart("-");
		assertFalse(part.isValidID("--"));

		// Test chars with special meaning in regexes.
		part = new StringIDGeneratorPart(".");
		assertTrue(part.isValidID("."));

		part = new StringIDGeneratorPart("TE");
		assertTrue(part.isValidID("TE"));

		part = new StringIDGeneratorPart("TE");
		assertFalse(part.isValidID("T"));

		part = new StringIDGeneratorPart("T");
		assertFalse(part.isValidID("TE"));

        part = new StringIDGeneratorPart("-");
        assertFalse(part.isValidID(null));
        
        part = new StringIDGeneratorPart("-");
        assertFalse(part.isValidID(""));
	
	}	

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
