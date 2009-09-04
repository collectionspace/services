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
 */

package org.collectionspace.services.id;

import static org.junit.Assert.fail;
import junit.framework.TestCase;

/**
 * NumericIDGeneratorPartTest
 *
 * Test class for NumericIDGeneratorPart.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class NumericIDGeneratorPartTest extends TestCase {

    IDGeneratorPart part;

    public void testNextID() {

        part = new NumericIDGeneratorPart("0");        
        assertEquals("1", part.nextID());
        assertEquals("2", part.nextID());
        assertEquals("3", part.nextID());
    
        part = new NumericIDGeneratorPart("25");
        assertEquals("26", part.nextID());
        assertEquals("27", part.nextID());
        assertEquals("28", part.nextID());
        
        part = new NumericIDGeneratorPart();
        assertEquals("2", part.nextID());
            
    }

    public void testNextIDOverflow() {

        try {
            part = new NumericIDGeneratorPart("997", "3");        
            assertEquals("998", part.nextID());
            assertEquals("999", part.nextID());
            assertEquals("1000", part.nextID());
            fail("Should have thrown IllegalStateException here");
        } catch (IllegalStateException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

        // Tests default MAX_LENGTH value of 6 decimal places
        try {
            part = new NumericIDGeneratorPart("999997");        
            assertEquals("999998", part.nextID());
            assertEquals("999999", part.nextID());
            assertEquals("1000000", part.nextID());
            fail("Should have thrown IllegalStateException here");
        } catch (IllegalStateException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
            
    }

    public void testResetID() {
    
        part = new NumericIDGeneratorPart("25");
        assertEquals("26", part.nextID());
        assertEquals("27", part.nextID());
        assertEquals("28", part.nextID());
        part.resetID();
        assertEquals("26", part.nextID());
            
    }

    public void testInitialID() {

        part = new NumericIDGeneratorPart("0");
        assertEquals("0", part.getInitialID());

        part = new NumericIDGeneratorPart("25");
        assertEquals("25", part.getInitialID());
        
    }

    public void testCurrentID() {

        part = new NumericIDGeneratorPart("0");
        assertEquals("0", part.getCurrentID());
        assertEquals("1", part.nextID());
        assertEquals("2", part.nextID());
        assertEquals("2", part.getCurrentID());
        assertEquals("3", part.nextID());

        part = new NumericIDGeneratorPart("25");
        assertEquals("25", part.getCurrentID());
        
    }
    
    public void testNullInitialValue() {
    
        try {
            part = new NumericIDGeneratorPart(null);
            fail("Should have thrown IllegalArgumentException here");
        } catch (IllegalArgumentException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
        
    }

    public void testNonLongParseableInitialValue() {
    
        try {
            part = new NumericIDGeneratorPart("not a long parseable value");
            fail("Should have thrown IllegalArgumentException here");
        } catch (IllegalArgumentException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

    }

    public void testNonLongParseableMaxLength() {
    
        try {
            part = new NumericIDGeneratorPart("1", "not an int parseable value");
            fail("Should have thrown IllegalArgumentException here");
        } catch (IllegalArgumentException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

    }

    public void testNegativeInitialValue() {

        try {
            part = new NumericIDGeneratorPart("-1");
            fail("Should have thrown IllegalArgumentException here");
        } catch (IllegalArgumentException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
    }


    public void testIsValidID() {
    
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
