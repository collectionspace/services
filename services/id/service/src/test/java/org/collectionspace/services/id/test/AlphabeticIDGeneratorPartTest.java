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

import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.id.AlphabeticIDGeneratorPart;
import org.collectionspace.services.id.SequenceIDGeneratorPart;

import static org.junit.Assert.fail;
import junit.framework.TestCase;

/**	
 * AlphabeticIDGeneratorPartTest
 *
 * Test class for AlphabeticIDGeneratorPart.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class AlphabeticIDGeneratorPartTest extends TestCase {

	SequenceIDGeneratorPart part;
	
	public void testNextIDLowercase() throws BadRequestException {

		part = new AlphabeticIDGeneratorPart("a");
		assertEquals("a", part.newID());
		assertEquals("b", part.newID());
		assertEquals("c", part.newID());

		part = new AlphabeticIDGeneratorPart("x");
		assertEquals("x", part.newID());
		assertEquals("y", part.newID());
		assertEquals("z", part.newID());

}

	public void testnewIDLowercase2Chars() throws BadRequestException {

		part = new AlphabeticIDGeneratorPart("aa");
		assertEquals("aa", part.newID());
		assertEquals("ab", part.newID());
		assertEquals("ac", part.newID());

		part = new AlphabeticIDGeneratorPart("zx");
		assertEquals("zx", part.newID());
		assertEquals("zy", part.newID());
		assertEquals("zz", part.newID());

	}

	public void testnewIDLowercase2CharsRolloverFirst() throws BadRequestException {

		part = new AlphabeticIDGeneratorPart("ay");
		assertEquals("ay", part.newID());
		assertEquals("az", part.newID());
		assertEquals("ba", part.newID());
		assertEquals("bb", part.newID());

    }
	
	public void testnewIDUppercase() throws BadRequestException {
		
		part = new AlphabeticIDGeneratorPart("A", "Z", "A");
		assertEquals("A", part.newID());
		assertEquals("B", part.newID());
		assertEquals("C", part.newID());

		part = new AlphabeticIDGeneratorPart("A", "Z", "X");
		assertEquals("X", part.newID());
		assertEquals("Y", part.newID());
		assertEquals("Z", part.newID());

    }

	public void testnewIDUppercase2Chars() throws BadRequestException {

		part = new AlphabeticIDGeneratorPart("A", "Z", "AA");
		assertEquals("AA", part.newID());
		assertEquals("AB", part.newID());
		assertEquals("AC", part.newID());

		part = new AlphabeticIDGeneratorPart("A", "Z", "ZX");
		assertEquals("ZX", part.newID());
		assertEquals("ZY", part.newID());
		assertEquals("ZZ", part.newID());
			
	}

	public void testnewIDUppercase2CharsRolloverFirst() throws BadRequestException {

		part = new AlphabeticIDGeneratorPart("A", "Z", "AY");
		assertEquals("AY", part.newID());
		assertEquals("AZ", part.newID());
		assertEquals("BA", part.newID());
		assertEquals("BB", part.newID());

  }

	public void testInitialLowercase() throws BadRequestException {
		
		part = new AlphabeticIDGeneratorPart("aaa");
		assertEquals("aaa", part.getInitialID());
		
	}

	public void testInitialUppercase() throws BadRequestException {
		
		part = new AlphabeticIDGeneratorPart("A", "Z", "AZ");
		assertEquals("AZ", part.getInitialID());
		
	}

	public void testCurrentLowercase() throws BadRequestException {
		
		part = new AlphabeticIDGeneratorPart("aaa");
		assertEquals("aaa", part.getCurrentID());
		assertEquals("aaa", part.newID());
		assertEquals("aab", part.newID());
		assertEquals("aac", part.newID());
		assertEquals("aac", part.getCurrentID());
		assertEquals("aad", part.newID());
		
	}

	public void testCurrentUppercase() throws BadRequestException {
		
		part = new AlphabeticIDGeneratorPart("A", "Z", "A");
		assertEquals("A", part.getCurrentID());
		assertEquals("A", part.newID());
		assertEquals("B", part.newID());
		assertEquals("C", part.newID());
		assertEquals("C", part.getCurrentID());
		assertEquals("D", part.newID());
		
	}	

	public void testOverflowLowercase() throws BadRequestException {
	
        part = new AlphabeticIDGeneratorPart("zx");
        assertEquals("zx", part.newID());
        assertEquals("zy", part.newID());
        assertEquals("zz", part.newID());
        assertEquals("aaa", part.newID());
		
	}

	public void testOverflowUppercase() throws BadRequestException {
	
        part = new AlphabeticIDGeneratorPart("A", "Z", "X");
        assertEquals("X", part.newID());
        assertEquals("Y", part.newID());
        assertEquals("Z", part.newID());
        assertEquals("AA", part.newID());
		
	}

	public void testNonAlphabeticInitialValue() {
		try {
			part = new AlphabeticIDGeneratorPart("&*432");
			fail("Should have thrown BadRequestException here");
		} catch (BadRequestException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testNullInitialValue() {
		try {
			part = new AlphabeticIDGeneratorPart(null);
			fail("Should have thrown BadRequestException here");
		} catch (BadRequestException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testEmptyStringInitialValue() {
		try {
			part = new AlphabeticIDGeneratorPart("");
			fail("Should have thrown BadRequestException here");
		} catch (BadRequestException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testAllSpaceCharsInitialValue() {
		try {
			part = new AlphabeticIDGeneratorPart("  ");
			fail("Should have thrown BadRequestException here");
		} catch (BadRequestException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testIsValidIDDefaultSeries() throws BadRequestException {
	
		part = new AlphabeticIDGeneratorPart();

		assertTrue(part.isValidID("a"));
		assertTrue(part.isValidID("z"));

		assertFalse(part.isValidID("A"));
		assertFalse(part.isValidID("123"));
		
	}

	public void testIsValidIDConstrainedLowerCaseSeries() throws BadRequestException {
	
		part = new AlphabeticIDGeneratorPart("a", "f", "a");
		
		assertTrue(part.isValidID("a"));
		assertTrue(part.isValidID("b"));
		assertTrue(part.isValidID("f"));

		assertFalse(part.isValidID("g"));
		assertFalse(part.isValidID("z"));
		assertFalse(part.isValidID("A"));
		assertFalse(part.isValidID("123"));
		
	}

	public void testIsValidIDConstrainedUppercaseSeries() throws BadRequestException {
	
		part = new AlphabeticIDGeneratorPart("A", "F", "A");

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
