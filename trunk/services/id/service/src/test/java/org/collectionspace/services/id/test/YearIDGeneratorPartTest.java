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
import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.GregorianCalendar;
import junit.framework.TestCase;
import org.collectionspace.services.id.IDGeneratorPart;
import org.collectionspace.services.id.YearIDGeneratorPart;

/**
 * YearIDGeneratorPartTest
 *
 * Test class for YearIDGeneratorPart.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class YearIDGeneratorPartTest extends TestCase {

    IDGeneratorPart part;
    final static String year = "1999";

    public String getCurrentYear() {
        Calendar cal = GregorianCalendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        return Integer.toString(y);
    }


    public void testCurrentID() throws BadRequestException {

        part = new YearIDGeneratorPart();
        assertEquals(getCurrentYear(), part.getCurrentID());

        part = new YearIDGeneratorPart(year);
        assertEquals(year, part.getCurrentID());

    }

    public void testSetCurrentID() throws BadRequestException {

        part = new YearIDGeneratorPart("1999");
        part.setCurrentID("1999");
        assertEquals("1999", part.getCurrentID());
        part.setCurrentID("2000");
        assertEquals("2000", part.getCurrentID());

    }

    public void testSetCurrentIDNullOrEmpty() {

        part = new YearIDGeneratorPart();

        try {
          part.setCurrentID(null);
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

        try {
          part.setCurrentID("");
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

  }

/*
    public void testnewID() {
    
        part = new YearIDGeneratorPart("1999");        
        assertEquals("1999", part.newID());        
        
    }
*/

    public void testnewID() {
    
        part = new YearIDGeneratorPart();        
        assertEquals(getCurrentYear(), part.newID());        
        
    }

/*
    public void testresetID() {
    
        part = new YearIDGeneratorPart("1999");
        assertEquals("1999", part.newID());
        part.resetID();
        assertEquals("1999", part.getCurrentID());
            
    }
*/

/*
    public void testInitialID() {
    
        part = new YearIDGeneratorPart("1999");
        assertEquals("1999", part.getInitialID());
        
    }
*/

    public void testNullInitialValue() throws BadRequestException {
    
        try {
            part = new YearIDGeneratorPart(null);
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }
        
    }

    public void testEmptyInitialValue() {
    
        try {
            part = new YearIDGeneratorPart("");
            fail("Should have thrown BadRequestException here");
        } catch (BadRequestException expected) {
            // This Exception should be thrown, and thus the test should pass.
        }

    }

    public void testIsValidID() {
    
        part = new YearIDGeneratorPart();
        assertTrue(part.isValidID("2009"));

        part = new YearIDGeneratorPart();
        assertFalse(part.isValidID("839"));

        part = new YearIDGeneratorPart();
        assertFalse(part.isValidID("10100"));
        
        part = new YearIDGeneratorPart();
        assertFalse(part.isValidID("non-numeric value"));

        part = new YearIDGeneratorPart();
        assertFalse(part.isValidID(null));
        
        part = new YearIDGeneratorPart();
        assertFalse(part.isValidID(""));

    }

    // @TODO: Add more tests of boundary conditions, exceptions ...
 
}
