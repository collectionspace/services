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

package org.collectionspace.services.id.test;

import java.util.Vector;
import junit.framework.TestCase;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.id.*;

/**    
 * BaseIDGeneratorTest, Test class for BaseIDGenerator.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class BaseIDGeneratorTest extends TestCase {

    BaseIDGenerator generator = new BaseIDGenerator();
    IDGeneratorPart part;
    
    final static String CURRENT_YEAR = YearIDGeneratorPart.getCurrentYear();

    // Note: tests may fail with BadRequestException
    // if any initialization of new IDParts fails
    // due to invalid arguments passed to their constructors.

    // Test empty parts lists

    public void testEmptyPartsListCurrentID() {
        assertEquals("", generator.getCurrentID());
    }

    public void testEmptyPartsListNewID() {
        assertEquals("", generator.newID());
    }
    
    // Test adding parts and retrieving current IDs

    public void testCurrentIDViaVector() throws BadRequestException {
        Vector parts = new Vector();
        BaseIDGenerator gen;

        parts.clear();
        parts.add(new AlphabeticIDGeneratorPart("a"));
        gen = new BaseIDGenerator(parts);
        assertEquals("a", gen.getCurrentID());

        parts.clear();
        parts.add(new NumericIDGeneratorPart("1"));
        gen = new BaseIDGenerator(parts);
        assertEquals("1", gen.getCurrentID());

        parts.clear();
        parts.add(new StringIDGeneratorPart("PREFIX"));
        parts.add(new StringIDGeneratorPart("-"));
        parts.add(new StringIDGeneratorPart("SUFFIX"));
        gen = new BaseIDGenerator(parts);
        assertEquals("PREFIX-SUFFIX", gen.getCurrentID());

        parts.clear();
        parts.add(new YearIDGeneratorPart());
        gen = new BaseIDGenerator(parts);
        assertEquals(CURRENT_YEAR, gen.getCurrentID());

        parts.clear();
        parts.add(new UUIDGeneratorPart());
        gen = new BaseIDGenerator(parts);
        assertTrue(gen.getCurrentID().length() ==
            UUIDGeneratorPart.UUID_LENGTH);

        parts.clear();
        parts.add(new YearIDGeneratorPart("2009"));
        parts.add(new StringIDGeneratorPart("."));
        parts.add(new NumericIDGeneratorPart("1"));
        parts.add(new StringIDGeneratorPart("-"));
        parts.add(new AlphabeticIDGeneratorPart("a"));
        gen = new BaseIDGenerator(parts);
        assertEquals("2009.1-a", gen.getCurrentID());
    }

    public void testCurrentIDViaAdd() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));    
        assertEquals("2009.1-a", generator.getCurrentID());

        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("0"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("0"));
        assertEquals("2009.0.0", generator.getCurrentID());
    }
    
    // Test generating new IDs from a single part

    public void testNewAlphabeticLowercaseID() throws BadRequestException {
        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals("a", generator.newID());
        assertEquals("b", generator.newID());
        assertEquals("c", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("aa"));
        assertEquals("aa", generator.newID());
        assertEquals("ab", generator.newID());
        assertEquals("ac", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("az"));
        assertEquals("az", generator.newID());
        assertEquals("ba", generator.newID());
        assertEquals("bb", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("a"));
        generator.add(new AlphabeticIDGeneratorPart("yy"));
        assertEquals("ayy", generator.newID());
        assertEquals("ayz", generator.newID());
        assertEquals("aza", generator.newID());
    }

    public void testNewAlphabeticUppercaseID() throws BadRequestException {
        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "A"));
        assertEquals("A", generator.newID());
        assertEquals("B", generator.newID());
        assertEquals("C", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AA"));
        assertEquals("AA", generator.newID());
        assertEquals("AB", generator.newID());
        assertEquals("AC", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AZ"));
        assertEquals("AZ", generator.newID());
        assertEquals("BA", generator.newID());
        assertEquals("BB", generator.newID());
        
        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "A"));
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "YY"));
        assertEquals("AYY", generator.newID());
        assertEquals("AYZ", generator.newID());
        assertEquals("AZA", generator.newID());
    }

    public void testNewNumericID() throws BadRequestException {
        generator.clear();
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("1", generator.newID());
        assertEquals("2", generator.newID());
        assertEquals("3", generator.newID());
    }

    public void testNewStringID() throws BadRequestException {
        generator.clear();
        generator.add(new StringIDGeneratorPart("PREFIX"));
        assertEquals("PREFIX", generator.newID());
        assertEquals("PREFIX", generator.newID());
        assertEquals("PREFIX", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AA"));
        assertEquals("AA", generator.newID());
        assertEquals("AB", generator.newID());
        assertEquals("AC", generator.newID());

        generator.clear();
        generator.add(new AlphabeticIDGeneratorPart("A", "Z", "AZ"));
        assertEquals("AZ", generator.newID());
        assertEquals("BA", generator.newID());
        assertEquals("BB", generator.newID());
    }

    public void testNewUUID() {
        generator.clear();
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
    
    // Test generating new IDs from multiple, mixed parts

    public void testNewMixedID() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals(CURRENT_YEAR + ".1", generator.newID());
        assertEquals(CURRENT_YEAR + ".2", generator.newID());
        assertEquals(CURRENT_YEAR + ".3", generator.newID());
        
        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals(CURRENT_YEAR + ".1-a", generator.newID());
        assertEquals(CURRENT_YEAR + ".1-b", generator.newID());
        assertEquals(CURRENT_YEAR + ".1-c", generator.newID());

        generator.clear();
        generator.add(new StringIDGeneratorPart("T"));
        generator.add(new NumericIDGeneratorPart("1005"));
        assertEquals("T1005", generator.newID());
        assertEquals("T1006", generator.newID());
        assertEquals("T1007", generator.newID());

        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals(CURRENT_YEAR + ".1.1", generator.newID());
        assertEquals(CURRENT_YEAR + ".1.2", generator.newID());
        assertEquals(CURRENT_YEAR + ".1.3", generator.newID()); 
    }

    public void testNewMixedIDWithTrailingConstantStringID() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        assertEquals(CURRENT_YEAR + ".1-", generator.newID());
        assertEquals(CURRENT_YEAR + ".1-", generator.newID());
    }
    
    // Test validating IDs

    public void testValidUUID() {
        generator.clear();
        generator.add(new UUIDGeneratorPart());
        assertTrue(generator.isValidID("4c9395a8-1669-41f9-806c-920d86e40912"));
        assertFalse(generator.isValidID(null));
        assertFalse(generator.isValidID(""));
        assertFalse(generator.isValidID("not a UUID"));
        assertFalse(generator.isValidID("12345"));
        // Invalid character in 15th position (should be '4').
        assertFalse(generator.isValidID("4c9395a8-1669-31f9-806c-920d86e40912"));
        // Invalid character in 20th position (should be '8', '9', 'a', or 'b').
        assertFalse(generator.isValidID("4c9395a8-1669-41f9-106c-920d86e40912"));
    }

    public void testValidYearID() {
        generator.clear();
        generator.add(new YearIDGeneratorPart());
        assertTrue(generator.isValidID("2009"));
        assertTrue(generator.isValidID("5555"));
        assertFalse(generator.isValidID("456"));
        assertFalse(generator.isValidID("10000"));
    }

    public void testValidMixedID() throws BadRequestException {
        generator.clear();
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

        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("ZZ.AND."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertTrue(generator.isValidID("2009ZZ.AND.1"));
        assertFalse(generator.isValidID("2009ZZ-AND-1"));
    }

    public void testGetRegex() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("(\\d{4})(\\.)(\\d{1,6})", generator.getRegex());
    }
 
}
