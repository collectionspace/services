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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.collectionspace.services.id;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import junit.framework.TestCase;
import static org.junit.Assert.fail;


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

    public void testEmptyPartsListCurrentID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        assertEquals("", generator.getCurrentID());
    }

    public void testEmptyPartsListNewID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        assertEquals("", generator.newID());
    }

    public void testCurrentIDViaVector() {

        Vector parts = new Vector();

        parts.clear();
        parts.add(new AlphabeticIDGeneratorPart("a"));
        generator = new BaseIDGenerator(DEFAULT_CSID, parts);
        assertEquals("a", generator.getCurrentID());

        parts.clear();
        parts.add(new NumericIDGeneratorPart("1"));
        generator = new BaseIDGenerator(DEFAULT_CSID, parts);
        assertEquals("1", generator.getCurrentID());

        parts.clear();
        parts.add(new StringIDGeneratorPart("PREFIX"));
        parts.add(new StringIDGeneratorPart("-"));
        parts.add(new StringIDGeneratorPart("SUFFIX"));
        generator = new BaseIDGenerator(DEFAULT_CSID, parts);
        assertEquals("PREFIX-SUFFIX", generator.getCurrentID());

        parts.clear();
        parts.add(new YearIDGeneratorPart());
        generator = new BaseIDGenerator(DEFAULT_CSID, parts);
        assertEquals(getCurrentYear(), generator.getCurrentID());

        parts.clear();
        parts.add(new UUIDGeneratorPart());
        generator = new BaseIDGenerator(DEFAULT_CSID, parts);
        assertTrue(generator.getCurrentID().length() ==
            UUIDGeneratorPart.UUID_LENGTH);

        parts.clear();
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
        assertEquals("E1", generator.getCurrentID("E"));

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        assertEquals("2009.", generator.getCurrentID("2009"));
        assertEquals("2009.", generator.getCurrentID("2009"));
        assertEquals("2010.", generator.getCurrentID("2010"));
        assertEquals("2010.", generator.getCurrentID("2010"));

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("2009.1", generator.getCurrentID("2009."));
        assertEquals("2009.1", generator.getCurrentID("2009."));

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("55"));
        assertEquals("2010.55", generator.getCurrentID("2010."));
        assertEquals("2010.55", generator.getCurrentID("2010."));

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart());
        assertEquals("2009.1", generator.getCurrentID("2009."));
        assertEquals("2009.1", generator.getCurrentID("2009."));

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals("2009.1-a", generator.getCurrentID("2009.1-"));
        assertEquals("2009.1-a", generator.getCurrentID("2009.1-"));
        assertEquals("2009.3-a", generator.getCurrentID("2009.3-"));
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

    public void testNewAlphabeticLowercaseID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals("a", generator.newID());
        assertEquals("b", generator.newID());
        assertEquals("c", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("aa"));
        assertEquals("aa", generator.newID());
        assertEquals("ab", generator.newID());
        assertEquals("ac", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("az"));
        assertEquals("az", generator.newID());
        assertEquals("ba", generator.newID());
        assertEquals("bb", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("a"));
        generator.add(new AlphabeticIDGeneratorPart("yy"));
        assertEquals("ayy", generator.newID());
        assertEquals("ayz", generator.newID());
        assertEquals("aza", generator.newID());
    }

    public void testNewAlphabeticUppercaseID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "A"));
        assertEquals("A", generator.newID());
        assertEquals("B", generator.newID());
        assertEquals("C", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AA"));
        assertEquals("AA", generator.newID());
        assertEquals("AB", generator.newID());
        assertEquals("AC", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AZ"));
        assertEquals("AZ", generator.newID());
        assertEquals("BA", generator.newID());
        assertEquals("BB", generator.newID());
        
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "A"));
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "YY"));
        assertEquals("AYY", generator.newID());
        assertEquals("AYZ", generator.newID());
        assertEquals("AZA", generator.newID());
    }

    public void testNewStringID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new StringIDGeneratorPart("PREFIX"));
        assertEquals("PREFIX", generator.newID());
        assertEquals("PREFIX", generator.newID());
        assertEquals("PREFIX", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AA"));
        assertEquals("AA", generator.newID());
        assertEquals("AB", generator.newID());
        assertEquals("AC", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AZ"));
        assertEquals("AZ", generator.newID());
        assertEquals("BA", generator.newID());
        assertEquals("BB", generator.newID());
    }

    public void testNewUUID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new UUIDGeneratorPart());
        String firstID = generator.newID();
        String secondID = generator.newID();
        String thirdID = generator.newID();
        assertTrue(firstID.length() == UUIDGeneratorPart.UUID_LENGTH);
        assertTrue(secondID.length() == UUIDGeneratorPart.UUID_LENGTH);
        assertTrue(thirdID.length() == UUIDGeneratorPart.UUID_LENGTH);
        assertTrue(firstID.compareTo(secondID) != 0);
        assertTrue(firstID.compareTo(thirdID) != 0);
        assertTrue(secondID.compareTo(thirdID) != 0);
    }

    public void testNewID() {

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("1", generator.newID());
        assertEquals("2", generator.newID());
        assertEquals("3", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals(getCurrentYear() + ".1", generator.newID());
        assertEquals(getCurrentYear() + ".2", generator.newID());
        assertEquals(getCurrentYear() + ".3", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals(getCurrentYear() + ".1-a", generator.newID());
        assertEquals(getCurrentYear() + ".1-b", generator.newID());
        assertEquals(getCurrentYear() + ".1-c", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new StringIDGeneratorPart("T"));
        generator.add(new NumericIDGeneratorPart("1005"));
        assertEquals("T1005", generator.newID());
        assertEquals("T1006", generator.newID());
        assertEquals("T1007", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals(getCurrentYear() + ".1.1", generator.newID());
        assertEquals(getCurrentYear() + ".1.2", generator.newID());
        assertEquals(getCurrentYear() + ".1.3", generator.newID());
            
    }

    public void testNewIDWithTrailingConstantStringID() {
    
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        assertEquals(getCurrentYear() + ".1-", generator.newID());
        assertEquals(getCurrentYear() + ".1-", generator.newID());

    }

    public void testNewIDWithSuppliedID() {
    
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("2009.1", generator.newID("2009.1"));
        assertEquals("2009.2", generator.newID());
        assertEquals("2009.3", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals("2009.1-a", generator.newID("2009.1-a"));
        assertEquals("2009.1-b", generator.newID());
        assertEquals("2009.1-c", generator.newID());

        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("3"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("b"));        
        assertEquals("2009.3-b", generator.newID("2009.3-b"));
        assertEquals("2009.3-c", generator.newID());
        assertEquals("2009.3-d", generator.newID());

    }

    public void testValidUUID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new UUIDGeneratorPart());

        assertTrue(generator.isValidID("4c9395a8-1669-41f9-806c-920d86e40912"));

        // Invalid character in 15th position (should be '4').
        assertFalse(generator.isValidID("4c9395a8-1669-31f9-806c-920d86e40912"));
        // Invalid character in 20th position (should be '8', '9', 'a', or 'b').
        assertFalse(generator.isValidID("4c9395a8-1669-41f9-106c-920d86e40912"));
        assertFalse(generator.isValidID(null));
        assertFalse(generator.isValidID(""));
        assertFalse(generator.isValidID("not a UUID"));
        assertFalse(generator.isValidID("12345"));
    }

    public void testValidYearID() {
        generator = new BaseIDGenerator(DEFAULT_CSID);
        generator.add(new YearIDGeneratorPart());

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

    public static String getCurrentYear() {
        Calendar cal = GregorianCalendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        return Integer.toString(y);
    }    

    // @TODO: Add more tests of boundary conditions, exceptions ...
 
}
