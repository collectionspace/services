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

import org.collectionspace.services.id.IDGeneratorPart;
import org.collectionspace.services.id.UUIDGeneratorPart;

/**
 * UUIDGeneratorPartTest
 *
 * Test class for UUIDGeneratorPart.
 *
 * $LastChangedRevision: 625 $
 * $LastChangedDate$
 */
public class UUIDGeneratorPartTest extends TestCase {

    IDGeneratorPart part;
    
    public void testnewID() {
        part = new UUIDGeneratorPart(); 
        
        String firstID = part.newID();
        String secondID = part.newID();
        String thirdID = part.newID();

        assertTrue(firstID.length() == UUIDGeneratorPart.UUID_LENGTH);
        assertTrue(secondID.length() == UUIDGeneratorPart.UUID_LENGTH);
        assertTrue(thirdID.length() == UUIDGeneratorPart.UUID_LENGTH);
        
        assertTrue(firstID.compareTo(secondID) != 0);
        assertTrue(firstID.compareTo(thirdID) != 0);
        assertTrue(secondID.compareTo(thirdID) != 0);
        
        assertTrue(part.isValidID(firstID));
        assertTrue(part.isValidID(secondID));
        assertTrue(part.isValidID(thirdID));
    }

    public void testIsValidID() {
        part = new UUIDGeneratorPart();
        assertTrue(part.isValidID("2d5ef3cc-bfb2-4383-a4c6-35645cd5dd81"));

        part = new UUIDGeneratorPart();
        assertFalse(part.isValidID(null));
        part = new UUIDGeneratorPart();
        assertFalse(part.isValidID(""));
        part = new UUIDGeneratorPart();
        assertFalse(part.isValidID("not a UUID"));
        part = new UUIDGeneratorPart();
        assertFalse(part.isValidID("12345"));
        // Invalid character in 15th position (should be '4').
        part = new UUIDGeneratorPart();
        assertFalse(part.isValidID("4c9395a8-1669-31f9-806c-920d86e40912"));
        // Invalid character in 20th position (should be '8', '9', 'a', or 'b').
        part = new UUIDGeneratorPart();
        assertFalse(part.isValidID("4c9395a8-1669-41f9-106c-920d86e40912"));
    }    
    
    // @TODO: Add more tests of boundary conditions, exceptions ...
 
}
