/**
 * IDPatternSerializerTest
 *
 * Unit tests of the ID Service's IDPatternSerializer class.
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
 * $LastChangedBy: aron $
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */

package org.collectionspace.services.id.test;

import org.collectionspace.services.id.*;

import junit.framework.TestCase;
import static org.junit.Assert.*;

public class IDPatternSerializerTest extends TestCase {

  String serializedPattern;
  IDPattern pattern;
  
	final static String DEFAULT_CSID = "TEST-1";

  final static String DEFAULT_SERIALIZED_ID_PATTERN =
    "<org.collectionspace.services.id.IDPattern>\n" +
    "  <csid>" + DEFAULT_CSID + "</csid>\n" +
    "  <uri></uri>\n" +
    "  <description></description>\n" +
    "  <parts/>\n" +
    "</org.collectionspace.services.id.IDPattern>";

  // @TODO We may want to canonicalize (or otherwise normalize) the expected and
  // actual XML in these tests, to avoid failures resulting from differences in
  // whitespace, etc.
	public void testSerializeIDPattern() {
	  IDPattern pattern = new IDPattern(DEFAULT_CSID);
		assertEquals(DEFAULT_SERIALIZED_ID_PATTERN, IDPatternSerializer.serialize(pattern));
	}

	public void testSerializeNullIDPattern() {
	  try {
	    String serializedPattern = IDPatternSerializer.serialize(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testDeserializeIDPattern() {
	  // This test will fail with different hash codes unless we add an IDPattern.equals()
	  // method that explicitly defines object equality as, for instance, having identical values
	  // in each of its instance variables.
	  // IDPattern pattern = IDPatternSerializer.deserialize(DEFAULT_SERIALIZED_ID_PATTERN);
	  // assertEquals(pattern, new IDPattern(DEFAULT_CSID));
	}

	public void testDeserializeNullSerializedIDPattern() {
	  try {
	    IDPattern pattern = IDPatternSerializer.deserialize(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testDeserializeInvalidSerializedIDPattern() {
	  try {
	    IDPattern pattern = IDPatternSerializer.deserialize("<invalid_serialized_pattern/>");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}
	
}
