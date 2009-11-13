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

import junit.framework.TestCase;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.id.*;

/**    
 * SettableIDGeneratorTest, Test class for SettableIDGenerator.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class SettableIDGeneratorTest extends TestCase {

    SettableIDGenerator generator = new SettableIDGenerator();
    IDGeneratorPart part;
    
    final static String CURRENT_YEAR = YearIDGeneratorPart.getCurrentYear();

    // Note: tests may fail with IllegalArgumentException
    // if any initialization of new IDParts fails
    // due to invalid arguments passed to their constructors.

    public void testCurrentIDWithPartialSuppliedID() throws BadRequestException {
        generator.clear();
        generator.add(new StringIDGeneratorPart("E"));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("E1", generator.getCurrentID("E"));
        assertEquals("E1", generator.getCurrentID("E"));

        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        assertEquals("2009.", generator.getCurrentID("2009"));
        assertEquals("2009.", generator.getCurrentID("2009"));
        assertEquals("2010.", generator.getCurrentID("2010"));
        assertEquals("2010.", generator.getCurrentID("2010"));

        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("2009.1", generator.getCurrentID("2009."));
        assertEquals("2009.1", generator.getCurrentID("2009."));

        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("55"));
        assertEquals("2010.55", generator.getCurrentID("2010."));
        assertEquals("2010.55", generator.getCurrentID("2010."));

        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart());
        assertEquals("2009.1", generator.getCurrentID("2009."));
        assertEquals("2009.1", generator.getCurrentID("2009."));

        generator.clear();
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

    public void testCurrentIDWithFullSuppliedID() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("55"));
        assertEquals("2009.55", generator.getCurrentID("2009.55"));
        assertEquals("2009.56", generator.newID());

        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals("2009.1-a", generator.getCurrentID("2009.1-a"));
        assertEquals("2009.1-b", generator.newID());
    }

    public void testNewIDWithTrailingConstantStringID() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        assertEquals(CURRENT_YEAR + ".1-", generator.newID());
        assertEquals(CURRENT_YEAR + ".1-", generator.newID());
    }

    public void testNewIDWithSuppliedID() throws BadRequestException {
        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        assertEquals("2009.1", generator.newID("2009.1"));
        assertEquals("2009.2", generator.newID());
        assertEquals("2009.3", generator.newID());

        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("a"));
        assertEquals("2009.1-a", generator.newID("2009.1-a"));
        assertEquals("2009.1-b", generator.newID());
        assertEquals("2009.1-c", generator.newID());

        generator.clear();
        generator.add(new YearIDGeneratorPart("2009"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("3"));
        generator.add(new StringIDGeneratorPart("-"));
        generator.add(new AlphabeticIDGeneratorPart("b"));        
        assertEquals("2009.3-b", generator.newID("2009.3-b"));
        assertEquals("2009.3-c", generator.newID());
        assertEquals("2009.3-d", generator.newID());
    }

    // @TODO Add more tests of boundary conditions, exceptions ...
 
}
