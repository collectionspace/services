 /*	
 * AlphabeticGeneratorTest
 *
 * Test class for AlphabeticGenerator.
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
 
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
// See <http://radio.javaranch.com/lasse/2007/05/17/1179405760728.html>
// for Exception handling in JUnit.

package org.collectionspace.services.id;

import org.apache.commons.id.StringIdentifierGenerator;
import static org.junit.Assert.fail;
import junit.framework.TestCase;

public class AlphabeticGeneratorTest extends TestCase {

	final boolean NO_WRAP = false;
	final boolean WRAP = true;
	StringIdentifierGenerator generator;
	
	public void testNoWrapAndInitialValue() {

		// @TODO: Split this test into more focused individual tests
		
		// All lowercase initial values
		
		generator = new AlphabeticGenerator(NO_WRAP, "a");
		assertEquals("b", generator.nextStringIdentifier());
		assertEquals("c", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "x");
		assertEquals("y", generator.nextStringIdentifier());
		assertEquals("z", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "aa");
		assertEquals("ab", generator.nextStringIdentifier());
		assertEquals("ac", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "ay");
		assertEquals("az", generator.nextStringIdentifier());
		assertEquals("ba", generator.nextStringIdentifier());
		assertEquals("bb", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "zx");
		assertEquals("zy", generator.nextStringIdentifier());
		assertEquals("zz", generator.nextStringIdentifier());

		// All uppercase initial values
		
		generator = new AlphabeticGenerator(NO_WRAP, "A");
		assertEquals("B", generator.nextStringIdentifier());
		assertEquals("C", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "X");
		assertEquals("Y", generator.nextStringIdentifier());
		assertEquals("Z", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "AA");
		assertEquals("AB", generator.nextStringIdentifier());
		assertEquals("AC", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "AY");
		assertEquals("AZ", generator.nextStringIdentifier());
		assertEquals("BA", generator.nextStringIdentifier());
		assertEquals("BB", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(NO_WRAP, "ZX");
		assertEquals("ZY", generator.nextStringIdentifier());
		assertEquals("ZZ", generator.nextStringIdentifier());
			
	}

	public void testWrapAndInitialLowercaseValue() {
	
		generator = new AlphabeticGenerator(WRAP, "x");
		assertEquals("y", generator.nextStringIdentifier());
		assertEquals("z", generator.nextStringIdentifier());
		assertEquals("a", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(WRAP, "zx");
		assertEquals("zy", generator.nextStringIdentifier());
		assertEquals("zz", generator.nextStringIdentifier());
		assertEquals("aa", generator.nextStringIdentifier());
		
	}
	
	public void testOverflowWithNoWrapAndInitialLowercaseValue()
		throws Exception {
	
		try {
			generator = new AlphabeticGenerator(NO_WRAP, "zx");
			assertEquals("zy", generator.nextStringIdentifier());
			assertEquals("zz", generator.nextStringIdentifier());
			// Should throw IllegalStateException
			assertNotNull(generator.nextStringIdentifier());
			fail("Should have thrown IllegalStateException here");
		} catch (IllegalStateException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testWrapAndInitialUppercaseValue() {

		generator = new AlphabeticGenerator(WRAP, "X");
		assertEquals("Y", generator.nextStringIdentifier());
		assertEquals("Z", generator.nextStringIdentifier());
		assertEquals("A", generator.nextStringIdentifier());

		generator = new AlphabeticGenerator(WRAP, "ZX");
		assertEquals("ZY", generator.nextStringIdentifier());
		assertEquals("ZZ", generator.nextStringIdentifier());
		assertEquals("AA", generator.nextStringIdentifier());

	}

	public void testOverflowWithNoWrapAndInitialUppercaseValue() {
	
		try {
			generator = new AlphabeticGenerator(NO_WRAP, "ZX");
			assertEquals("ZY", generator.nextStringIdentifier());
			assertEquals("ZZ", generator.nextStringIdentifier());
			// Should throw IllegalStateException
			assertNotNull(generator.nextStringIdentifier());
			fail("Should have thrown IllegalStateException here");
		} catch (IllegalStateException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

	public void testNonAlphabeticInitialValue() {
		try {
			generator = new AlphabeticGenerator(NO_WRAP, "&*432");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testNullInitialValue() {
		try {
			generator = new AlphabeticGenerator(NO_WRAP, null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testEmptyStringInitialValue() {
		try {
			generator = new AlphabeticGenerator(NO_WRAP, "");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testAllSpaceCharsInitialValue() {
		try {
			generator = new AlphabeticGenerator(NO_WRAP, "  ");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}
 
}
