/*	
 * IDServiceTest
 *
 * Test class for the ID Service.
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
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */

// @TODO: Use a URL builder in core Java or the Restlet framework,
// rather than String objects.

// @TODO: Consider using client-side RESTeasy, rather than Restlet,
// if there is a desire to reduce the number of dependencies.
//
// (Note also Sanjay's comment c. June 2009 re RESTEasy's client-side
// behavior around having to send authentication credentials in
// advance, rather than via a challenge - if that was understood correctly.)

package org.collectionspace.services.test;

//import org.collectionspace.services.id.Id;
//import org.collectionspace.services.id.IdList;
//import org.collectionspace.services.id.IdPattern;
//import org.collectionspace.services.id.IdPatternList;

import junit.framework.TestCase;
import static org.junit.Assert.*;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class IDServiceTest extends TestCase {

	final static String DEFAULT_REFERRER_URL = "http://collectionspace.org";
	final static String DEFAULT_SUCCESS_URL_STRING = "http://www.example.com/";
	final static String DEFAULT_FAILURE_URL_STRING = "http://www.example.com/nonexistent";

	// Stub test to verify basic functionality.
	public void testSuccessfulRequest() {
		Response response = submitRequest(DEFAULT_SUCCESS_URL_STRING);
		assertTrue(isSuccessResponse(response));		
	}

	// Stub test to verify basic functionality.
	public void testFailureRequest() {
		Response response = submitRequest(DEFAULT_FAILURE_URL_STRING);
		assertFalse(isSuccessResponse(response));		
	}

	// Return a flag indicating whether a response from a
	// service request represents a successful outcome.
	public boolean isSuccessResponse(Response response) {
	
		if (response == null || response.getStatus() == null) {
			return false;
		}

		// Note: we can also test specifically for a 200 OK response via
		// 'if (response.getStatus() == Status.SUCCESS_OK) ...'
		if (response.getStatus().isSuccess()) {
			return true;
		} else {
			return false;
		}
		
	}

	// Submit a request to a service.
	//
	// @TODO: Remove hard-coding of HTTP protocol requests.
	public Response submitRequest(String urlStr) {

		// Adapted from the Restlet 1.1 tutorial
		// http://www.restlet.org/documentation/1.1/tutorial
		//
		// Note that if we later migrate to using the Restlet 2.0
		// framework, it uses a resource model on the client-side,
		// via the ClientResource class:
		// http://www.restlet.org/documentation/2.0/tutorial

		// @TODO: Validate the submitted URL here. 
		
		// Prepare the request.
		Request request = new Request(Method.GET, urlStr);
		request.setReferrerRef(DEFAULT_REFERRER_URL);
		
		// Handle it using an HTTP client connector.
		//
		// @TODO: We may need to derive the protocol,
		// such as HTTP v. HTTPS, from the submitted URL.
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		
		return response;
		
	}
	
}
