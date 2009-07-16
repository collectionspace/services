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
 
// @TODO: A large fraction of the current code will be used by
// multiple service tests.  It should be refactored; it could
// go into a "RESTClient" or similarly-named class in the 'common'
// package, along with a corresponding test class in that package.

// @TODO: The name of this class should likely reflect that
// this is a test of a service via its REST APIs, rather than
// a test of that service using a Java client library.

// @TODO: We may want to run our own HTTP/HTTPS server for
// verifying basic functionality, rather than relying on
// public Internet services, like example.com.

package org.collectionspace.services.test;

import junit.framework.TestCase;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class IDServiceTest extends TestCase {

	final static String DEFAULT_REFERRER_URL = "http://collectionspace.org";
	
	final static String SUCCESS_URL_STRING = "http://www.example.com/";
	final static String FAILURE_URL_STRING = "http://www.example.com/nonexistent";
	final static String NON_PARSEABLE_URL = "example.com";
	final static String NON_HTTP_PROTOCOL = "ftp://example.com";
	
	// @TODO Construct a parameterized base path, rather than hard-coding it here.
	final static String SERVICES_BASE_PATH = "http://localhost:8180/cspace-services";
	
	Response response;

  protected void setUp() {
  	response = null;
  }
  
	// Stub tests to run first, to verify the basic functionality of this test class.
	
	public void testSuccessfulRequest() {
	  response = sendGetRequest(SUCCESS_URL_STRING);
		assertTrue(isSuccessResponse(response));		
	}

	public void testFailureRequest() {
	  response = sendGetRequest(FAILURE_URL_STRING);
		assertFalse(isSuccessResponse(response));		
	}

	public void testNonParseableURL() {
	  try {
      response = sendGetRequest(NON_PARSEABLE_URL);
      fail("Should have thrown IllegalArgumentException here.");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testNonHttpProtocol() {
	  try {
      response = sendGetRequest(NON_HTTP_PROTOCOL);
      fail("Should have thrown IllegalArgumentException here.");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}
	
	// Tests specific to the ID Service
	
	// Stubbed example, just as a beachhead ...
	
	// @TODO: Look into whether there may be a way to at least partly
	// automate the task of building these tests.
	
	// @TODO: Retrieve the service schema(s) and validate the received
	// payloads against the schema(s).
	//
	// If there is a multi-part payload, decide which payload parts
	// to validate.
	
	// @TODO: When the time comes, include support for HTTP Basic Authentication.
	
	public void testNextID() {
	  final String NEXT_ID_URL = SERVICES_BASE_PATH + "/ids/next/patterns/2";
	  response = sendGetRequest(NEXT_ID_URL);
		assertTrue(isSuccessResponse(response));
	}

  //////////////////////////////////////////////////////////////////////
  /*
   * Tests whether a response from an HTTP or HTTPS service request
   * represents a successful outcome.
   *
   * @param  response  A response from an HTTP or HTTP service request.
   *
   * @return  True if the response represents a successful outcome;
   *          false if the response represents a failure (i.e. error) outcome.
   */
	public boolean isSuccessResponse(Response response) {
	
		if (response == null || response.getStatus() == null) {
			return false;
		}

		// Note: If needed, we can also test specifically for a 200 OK response via
		// 'if (response.getStatus() == Status.SUCCESS_OK) ...'
		if (response.getStatus().isSuccess()) {
			return true;
		} else {
			return false;
		}
		
	}

  //////////////////////////////////////////////////////////////////////
  /*
   * Sends (or submits) a GET request to an HTTP- or HTTPS-based service.
   *
   * @param  urlStr  A String representation of an HTTP or HTTPS URL.
   *
   * @return  The response received from sending a GET request to that URL.
   *
   * @throws IllegalArgumentException  If the URL string could not be parsed
   *   or does not contain a legal protocol, or if the protocol name in the URL
   *   is not HTTP or HTTPS.
   */
	public Response sendGetRequest(String urlStr) throws IllegalArgumentException {

		// Adapted from the Restlet 1.1 tutorial
		// http://www.restlet.org/documentation/1.1/tutorial
		//
		// Note that if we later migrate to using the Restlet 2.0
		// framework, it uses a resource model on the client-side,
		// via the ClientResource class:
		// http://www.restlet.org/documentation/2.0/tutorial
		
    URL url;
    Protocol protocol;
    
    try {
      url = new URL(urlStr);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("URL string could not be parsed successfully");
    }
    
    if (url.getProtocol().equals(Protocol.HTTP.getSchemeName())) {
      protocol = Protocol.HTTP;
    } else if (url.getProtocol().equals(Protocol.HTTPS.getSchemeName())) {
      protocol = Protocol.HTTPS;
    } else {
      throw new IllegalArgumentException("Protocol of submitted URL must be http:// or https://");
    }
    
		// Prepare the request.
		Request request = new Request(Method.GET, urlStr);
		request.setReferrerRef(DEFAULT_REFERRER_URL);
		
		// Handle it using an HTTP or HTTPS client connector.
		Client client = new Client(protocol);
		Response response = client.handle(request);
		
		return response;
		
	}
	
}
