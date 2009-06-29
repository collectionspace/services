/*	
 * IDResource
 *
 * Resource class to handle requests to the ID Service.
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
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */
  
package org.collectionspace.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.IDService;
import org.collectionspace.services.id.IDPattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Set the base path component for URLs that access this service.
@Path("/ids")
// Identify the default MIME media types consumed and produced by this service.
@Consumes("application/xml")
@Produces("application/xml")
public class IDResource {

	final Logger logger = LoggerFactory.getLogger(IDResource.class);

	// Richard's comment in the CollectionObject Resource class, from which
	// this class was derived: This should be a DI wired by a container like
	// Spring, Seam, or EJB3.
	final static IDService service = new IDServiceJdbcImpl();

  //////////////////////////////////////////////////////////////////////
	/* 
	 * Constructor (no argument).
   */
	public IDResource() {
		// do nothing
	}

  //////////////////////////////////////////////////////////////////////
  /*
   * Returns the next available ID associated with a specified ID pattern.
   *
   * @param  csid  An identifier for an ID pattern.
   *
   * @return  The next available ID associated with the specified ID pattern.
   * 
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   */
	@GET
	@Path("/next/patterns/{id}")
	// @TODO: Temporary during testing; to be changed to return XML
  @Produces("text/plain")
	public Response getNextID(@PathParam("csid") String csid) {
	
		Response response = null;
		String nextId = "";
	
		try {
		
			nextId = service.nextID(csid);

		// @TODO: Return our XML-based error results format.
		
		// @TODO: An IllegalStateException often indicates an overflow
		// of an IDPart.  Consider whether returning a 400 Bad Request
		// status code is warranted, or whether returning some other
		// status would be more appropriate.
		} catch (IllegalStateException ise) {
			response = Response.status(Response.Status.BAD_REQUEST)
				.entity(ise.getMessage()).type("text/plain").build();
				
		} catch (IllegalArgumentException iae) {
			response = Response.status(Response.Status.BAD_REQUEST)
				.entity(iae.getMessage()).type("text/plain").build();
				
		} catch (Exception e) {
			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(e.getMessage()).type("text/plain").build();
		}
		
    return response;
    
  }

  //////////////////////////////////////////////////////////////////////
  /*
   * Prints a serialized ID pattern to the console (stdout).
   *
   * @param	msg  A message.
   *
   * @param  idPattern  An ID Pattern.
   *
   */
	private void verbose(String msg, IDPattern idPattern) {
	
		try {
			verbose(msg);
			JAXBContext jc = JAXBContext.newInstance(IDPattern.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(idPattern, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

  //////////////////////////////////////////////////////////////////////
  /*
   * Prints a message to the console (stdout).
   *
   * @param	msg  A message.
   *
   */
	private void verbose(String msg) {
		System.out.println("IDResource. " + msg);
	}

}
