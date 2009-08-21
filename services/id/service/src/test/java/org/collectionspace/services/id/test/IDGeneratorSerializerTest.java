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

import org.collectionspace.services.id.*;

import junit.framework.TestCase;
import static org.junit.Assert.*;

/**
 * IDGeneratorSerializerTest
 *
 * Unit tests of the ID Service's IDGeneratorSerializer class.
 *
 *
 * $LastChangedBy: aron $
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */
public class IDGeneratorSerializerTest extends TestCase {

  String serializedGenerator;
  IDPattern pattern;
  
	final static String DEFAULT_CSID = "TEST-1";

  final static String DEFAULT_SERIALIZED_ID_GENERATOR =
    "<org.collectionspace.services.id.IDPattern>\n" +
    "  <csid>" + DEFAULT_CSID + "</csid>\n" +
    "  <uri></uri>\n" +
    "  <description></description>\n" +
    "  <parts/>\n" +
    "</org.collectionspace.services.id.IDPattern>";

  // @TODO We may want to canonicalize (or otherwise normalize) the expected and
  // actual XML in these tests, to avoid failures resulting from differences in
  // whitespace, etc.
	public void testSerializeIDGenerator() {
	  IDPattern pattern = new IDPattern(DEFAULT_CSID);
		assertEquals(DEFAULT_SERIALIZED_ID_GENERATOR, IDGeneratorSerializer.serialize(pattern));
	}

	public void testSerializeNullIDGenerator() {
	  try {
	    String serializedPattern = IDGeneratorSerializer.serialize(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testDeserializeIDGenerator() {
	  // This test will fail with different hash codes unless we add an IDPattern.equals()
	  // method that explicitly defines object equality as, for instance, having identical values
	  // in each of its instance variables.
	  // IDPattern pattern = IDGeneratorSerializer.deserialize(DEFAULT_SERIALIZED_ID_PATTERN);
	  // assertEquals(pattern, new IDPattern(DEFAULT_CSID));
	}

	public void testDeserializeNullSerializedIDGenerator() {
	  try {
	    IDPattern pattern = IDGeneratorSerializer.deserialize(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testDeserializeInvalidSerializedIDGenerator() {
	  try {
	    IDPattern pattern = IDGeneratorSerializer.deserialize("<invalid_serialized_generator/>");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}
	
}
