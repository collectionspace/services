/*    
 * NumericIDGeneratorPartTest
 *
 * Test class for NumericIDGeneratorPart.
 *
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
import static org.junit.Assert.fail;
import junit.framework.TestCase;
import org.collectionspace.services.id.NumericIDGeneratorPart;
import org.collectionspace.services.id.SequenceIDGeneratorPart;


/**
 * NumericIDGeneratorPartTest
 *
 * Test class for NumericIDGeneratorPart.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class NumericIDGeneratorPartTest extends TestCase {

    SequenceIDGeneratorPart part;

    public void testInitialID() throws BadRequestException {
        part = new NumericIDGeneratorPart("0");
        assertEquals("0", part.getInitialID());

        part = new NumericIDGeneratorPart("25");
        assertEquals("25", part.getInitialID());
    }

    public void testNullInitialValue() {
        try {
            part = new NumericIDGeneratorPart(null);
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
    }

    public void testNonLongParseableInitialValue() {
        try {
            part = new NumericIDGeneratorPart("not a long parseable value");
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
    }

    public void testNonLongParseableMaxLength() {
        try {
            part = new NumericIDGeneratorPart("1", "not an int parseable value");
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
    }

    public void testNegativeInitialValue() {
       try {
            part = new NumericIDGeneratorPart("-1");
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
    }

    public void testGetCurrentID() throws BadRequestException {
        part = new NumericIDGeneratorPart("0");
        assertEquals("0", part.getCurrentID());
        assertEquals("0", part.newID());
        assertEquals("1", part.newID());
        assertEquals("2", part.newID());
        assertEquals("2", part.getCurrentID());
        assertEquals("3", part.newID());

        part = new NumericIDGeneratorPart("25");
        assertEquals("25", part.getCurrentID());
    }

    public void testNewID() throws BadRequestException {
        part = new NumericIDGeneratorPart("0");        
        assertEquals("0", part.newID());
        assertEquals("1", part.newID());
        assertEquals("2", part.newID());
        assertEquals("3", part.newID());
    
        part = new NumericIDGeneratorPart("25");
        assertEquals("25", part.newID());
        assertEquals("26", part.newID());
        assertEquals("27", part.newID());
        assertEquals("28", part.newID());
        
        part = new NumericIDGeneratorPart();
        assertEquals("1", part.newID());
        assertEquals("2", part.newID());
        assertEquals("3", part.newID());
    }

    public void testNewIDOverflow() throws BadRequestException {

        try {
            part = new NumericIDGeneratorPart("997", "3");        
            assertEquals("997", part.newID());
            assertEquals("998", part.newID());
            assertEquals("999", part.newID());
            assertEquals("1000", part.newID());
            fail("Should have thrown IllegalStateException here");
        } catch (IllegalStateException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

        // Tests default MAX_LENGTH value of 6 decimal places
        try {
            part = new NumericIDGeneratorPart("999997");        
            assertEquals("999997", part.newID());
            assertEquals("999998", part.newID());
            assertEquals("999999", part.newID());
            assertEquals("1000000", part.newID());
            fail("Should have thrown IllegalStateException here");
        } catch (IllegalStateException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
            
    }

    public void testIsValidID() throws BadRequestException {
        part = new NumericIDGeneratorPart("1");
        assertTrue(part.isValidID("1"));
        part = new NumericIDGeneratorPart("1");
        assertTrue(part.isValidID("123"));
        part = new NumericIDGeneratorPart("1");
        assertTrue(part.isValidID("123456"));

        part = new NumericIDGeneratorPart("1");
        assertFalse(part.isValidID("1234567"));
        part = new NumericIDGeneratorPart("1", "3");
        assertTrue(part.isValidID("123"));
        part = new NumericIDGeneratorPart("1", "3");
        assertFalse(part.isValidID("1234"));
        part = new NumericIDGeneratorPart("1");
        assertFalse(part.isValidID("not a parseable long"));
        part = new NumericIDGeneratorPart("1", "3");
        assertFalse(part.isValidID("not a parseable long"));
        part = new NumericIDGeneratorPart("1", "3");
        assertFalse(part.isValidID(null));
        part = new NumericIDGeneratorPart("1", "3");
        assertFalse(part.isValidID(""));
    }    
    
    // @TODO: Add more tests of boundary conditions, exceptions ...
 
}
