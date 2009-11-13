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

package org.collectionspace.services.id.test;

import junit.framework.TestCase;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.id.StoredValueIDGeneratorPart;
import org.collectionspace.services.id.StringIDGeneratorPart;

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

	public void testNullInitialValue() {
		try {
			part = new StringIDGeneratorPart(null);
			fail("Should have thrown BadRequestException here");
		} catch (BadRequestException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testEmptyInitialValue() {
		try {
			part = new StringIDGeneratorPart("");
			fail("Should have thrown BadRequestException here");
		} catch (BadRequestException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testGetInitialID() throws BadRequestException {
		part = new StringIDGeneratorPart("-");
		assertEquals("-", part.getInitialID());
	}

	public void testGetCurrentID() throws BadRequestException {
		part = new StringIDGeneratorPart("- -");
		assertEquals("- -", part.getCurrentID());
	}
	
	public void testNewID() throws BadRequestException {
		part = new StringIDGeneratorPart("E");		
		assertEquals("E", part.newID());	
		part = new StringIDGeneratorPart("XYZ");		
		assertEquals("XYZ", part.newID());		
	}

	public void testIsValidID() throws BadRequestException, BadRequestException {
		part = new StringIDGeneratorPart("-");
		assertTrue(part.isValidID("-"));
		part = new StringIDGeneratorPart("TE");
		assertTrue(part.isValidID("TE"));
		
        part = new StringIDGeneratorPart("-");
        assertFalse(part.isValidID(null));       
        part = new StringIDGeneratorPart("-");
        assertFalse(part.isValidID(""));
		part = new StringIDGeneratorPart("-");
		assertFalse(part.isValidID("--"));
		part = new StringIDGeneratorPart("TE");
		assertFalse(part.isValidID("T"));
		part = new StringIDGeneratorPart("T");
		assertFalse(part.isValidID("TE"));	
	}	

/*
	public void testIsValidIDWithRegexChars() {
		// Test chars with special meaning in regexes.
		// @TODO Will throw a PatternSyntaxException; we need to catch this.
		part = new StringIDGeneratorPart("*");
		assertTrue(part.isValidID("*"));
		part = new StringIDGeneratorPart("T*E");
		assertTrue(part.isValidID("T*E"));
    }
*/

	// @TODO: Add more tests of boundary conditions, exceptions ...
 
}
