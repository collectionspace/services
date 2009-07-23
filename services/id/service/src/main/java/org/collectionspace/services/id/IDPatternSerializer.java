/**
 * IDPatternSerializer
 *
 * Serializer and deserializer for ID patterns.
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
 * Based on work by Richard Millet and Sanjay Dalal.
 *
 * $LastChangedBy: aron $
 * $LastChangedRevision: 414 $
 * $LastChangedDate$
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

public class IDPatternSerializer {

  //////////////////////////////////////////////////////////////////////
  /**
   * Constructor (no-argument).
   */ 
  public void IDPatternSerializer() {
  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Serializes an ID pattern, converting it from a Java object into an XML representation.
   *
   * @param  pattern  An ID pattern.
   *
   * @return  A serialized representation of that ID pattern.
   *
   * @throws  IllegalArgumentException if the ID pattern cannot be serialized.
   */
	public static String serialize(IDPattern pattern) throws IllegalArgumentException {
	
	  if (pattern == null) {
	    throw new IllegalArgumentException("ID pattern cannot be null.");
	  }
  
    XStream xstream = new XStream(new DomDriver()); 
    
    String serializedPattern = "";
    try {
      serializedPattern = xstream.toXML(pattern);
    } catch (XStreamException e) {
	    throw new IllegalArgumentException(
	      "Could not convert ID pattern to XML for storage in database.");
    }
    
    return serializedPattern;
  
  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Deserializes an ID pattern, converting it from an XML representation
   * into a Java object.
   *
   * @param   serializedPattern  A serialized representation of an ID pattern.
   *
   * @return  The ID pattern deserialized as a Java object.
   *
   * @throws  IllegalArgumentException if the ID pattern cannot be deserialized.
   */
	public static IDPattern deserialize(String serializedPattern)
	  throws IllegalArgumentException {

	  if (serializedPattern == null || serializedPattern.equals("")) {
	    throw new IllegalArgumentException("ID pattern cannot be null or empty.");
	  }

    XStream xstream = new XStream(new DomDriver());

    IDPattern pattern;
    try {
      pattern = (IDPattern) xstream.fromXML(serializedPattern);
    } catch (XStreamException e) {
	    throw new IllegalArgumentException(
	      "Could not understand or parse this representation of an ID pattern.");
    }

    return pattern;
  
  }
  
}
