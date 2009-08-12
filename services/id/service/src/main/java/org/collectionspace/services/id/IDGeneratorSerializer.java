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
 */
 
// @TODO: Revise exception handling to return custom Exceptions,
// perhaps mirroring the subset of HTTP status codes returned.
//
// We're currently overloading existing core and extension Java Exceptions
// in ways that are not consistent with their original semantic meaning.

package org.collectionspace.services.id;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * IDGeneratorSerializer
 *
 * Serializer and deserializer for ID patterns.
 *
 * $LastChangedBy: aron $
 * $LastChangedRevision: 414 $
 * $LastChangedDate$
 */
public class IDGeneratorSerializer {

  // *IMPORTANT*
  // @TODO: This class is in an early state of a refactoring to
  // reflect a change from IDPatterns to IDGenerators at the top level
  // of the ID Service.  As a result, there will be some naming
  // inconsistencies throughout this source file.

  //////////////////////////////////////////////////////////////////////
  /**
   * Constructor (no-argument).
   */ 
  public void IDGeneratorSerializer() {
  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Serializes an ID generator, converting it from a Java object into an XML representation.
   *
   * @param  pattern  An ID generator.
   *
   * @return  A serialized representation of that ID generator.
   *
   * @throws  IllegalArgumentException if the ID generator cannot be serialized.
   */
	public static String serialize(IDPattern pattern) throws IllegalArgumentException {
	
	  if (pattern == null) {
	    throw new IllegalArgumentException("ID generator cannot be null.");
	  }
  
    XStream xstream = new XStream(new DomDriver()); 
    
    String serializedGenerator = "";
    try {
      serializedGenerator = xstream.toXML(pattern);
    } catch (XStreamException e) {
	    throw new IllegalArgumentException(
	      "Could not convert ID pattern to XML for storage in database.");
    }
    
    return serializedGenerator;
  
  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Deserializes an ID generator, converting it from an XML representation
   * into a Java object.
   *
   * @param   serializedGenerator  A serialized representation of an ID generator.
   *
   * @return  The ID generator deserialized as a Java object.
   *
   * @throws  IllegalArgumentException if the ID generator cannot be deserialized.
   */
	public static IDPattern deserialize(String serializedGenerator)
	  throws IllegalArgumentException {

	  if (serializedGenerator == null || serializedGenerator.equals("")) {
	    throw new IllegalArgumentException("ID generator cannot be null or empty.");
	  }

    XStream xstream = new XStream(new DomDriver());

    IDPattern pattern;
    try {
      pattern = (IDPattern) xstream.fromXML(serializedPattern);
    } catch (XStreamException e) {
	    throw new IllegalArgumentException(
	      "Could not understand or parse this representation of an ID generator.");
    }

    return pattern;
  
  }
  
}
