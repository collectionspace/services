/**
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
  
package org.collectionspace.services.id;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

// import org.collectionspace.services.IDService;
// import org.collectionspace.services.id.IDPattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Set the base path component for URLs that access this service.
@Path("/ids")

// Identify the default MIME media types consumed and produced by this service.
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)

public class IDResource {

	final Logger logger = LoggerFactory.getLogger(IDResource.class);

	// Per Richard's comment in the CollectionObject Resource class, from which
	// this class was derived: "This should be a DI wired by a container like
	// Spring, Seam, or EJB3."
	
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
	// @TODO: Temporary during early testing.
	// To be changed to a POST request, and most likely to a different URL.
	// E.g. /idpatterns/{identifier}/ids
	@Path("/next/patterns/{csid}")
	// @TODO: Temporary during testing; to be changed to return XML
  @Produces("text/plain")
	public Response getNextID(@PathParam("csid") String csid) {
	
	  verbose("> in getNextID");
	  
	  // Unless the 'response' variable is explicitly initialized here, the
	  // compiler gives the error: "variable response might not have been initialized."
	  Response response = null;
	  response = response.ok().build();
		String nextId = "";
	
		try {
		
		  // Retrieve the next ID for the requested pattern,
		  // and return it in the entity body of the response.
			nextId = service.nextID(csid);
			
      if (nextId == null || nextId.equals("")) {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("ID Service returned null or empty ID").type(MediaType.TEXT_PLAIN).build();
        return response;
      }
			
			response = Response.status(Response.Status.OK)
			  .entity(nextId).type(MediaType.TEXT_PLAIN).build();
		
		// @TODO: Return an XML-based error results format with the
		// responses below.
		
		// @TODO: An IllegalStateException often indicates an overflow
		// of an IDPart.  Consider whether returning a 400 Bad Request
		// status code is still warranted, or whether returning some other
		// status would be more appropriate.
		} catch (IllegalStateException ise) {
		  response = Response.status(Response.Status.BAD_REQUEST)
			  .entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();

		} catch (IllegalArgumentException iae) {
			response = Response.status(Response.Status.BAD_REQUEST)
			  .entity(iae.getMessage()).type(MediaType.TEXT_PLAIN).build();
	  
	  // This is guard code that should never be reached.
		} catch (Exception e) {
		  response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
			  .entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
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
	protected static void verbose(String msg, IDPattern idPattern) {
	
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
	protected static void verbose(String msg) {
		System.out.println("IDResource. " + msg);
	}

}
