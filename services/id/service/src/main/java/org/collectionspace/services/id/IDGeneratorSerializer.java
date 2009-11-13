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

import org.collectionspace.services.common.document.BadRequestException;


/**
 * IDGeneratorSerializer
 *
 * Serializer and deserializer for ID generators.
 *
 * $LastChangedRevision: 414 $
 * $LastChangedDate$
 */
public class IDGeneratorSerializer {

  //////////////////////////////////////////////////////////////////////
  /**
   * Constructor (no-argument).
   */ 
  public void IDGeneratorSerializer() {
  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Serializes an ID generator, converting it from a Java object
   * into an XML representation.
   *
   * @param  generator  An ID generator.
   *
   * @return  A serialized representation of that ID generator.
   *
   * @throws  BadRequestException if the ID generator cannot be serialized.
   */
	public static String serialize(SettableIDGenerator generator)
	    throws BadRequestException {
	
	  if (generator == null) {
	    throw new BadRequestException("ID generator cannot be null.");
	  }
  
    XStream xstream = new XStream(new DomDriver()); 
    
    String serializedGenerator = "";
    try {
      serializedGenerator = xstream.toXML(generator);
    } catch (XStreamException e) {
	    throw new BadRequestException(
	      "Could not convert ID generator to XML for storage in database.");
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
   * @throws  BadRequestException if the ID generator cannot be deserialized.
   */
	public static SettableIDGenerator deserialize(String serializedGenerator)
	  throws BadRequestException {

	  if (serializedGenerator == null || serializedGenerator.equals("")) {
	    throw new BadRequestException("ID generator cannot be null or empty.");
	  }

    XStream xstream = new XStream(new DomDriver());

    SettableIDGenerator generator;
    try {
      generator = (SettableIDGenerator) xstream.fromXML(serializedGenerator);
    } catch (XStreamException e) {
	    throw new BadRequestException(
	      "Could not understand or parse this representation of an ID generator.");
    }

    return generator;
  
  }
  
}
