/**	
 * Sample.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.sdk.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.collectionobject.ObjectNameGroup;
import org.collectionspace.services.collectionobject.ObjectNameList;
import org.testng.Assert;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;

/**
 * The Class Sample.
 */
public class Sample {

	/** The collection object client. */
	private static CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Base URL is: "
				+ collectionObjectClient.getBaseURL());

		String csid = createCollectionObject();
		System.out.println("Created a new collection object with CSID=" + csid);

		CollectionobjectsCommon co = readCollectionObject(csid);
		System.out.println("Got a collection object with CSID=" + csid);
		
		int status = updateCollectionObject(csid);
		System.out.println("Updated the collection object with CSID=" + csid);
	}

	/**
	 * Creates the collection object.
	 * 
	 * @return the string
	 */
	static String createCollectionObject() {
		String result = null;

		CollectionobjectsCommon co = new CollectionobjectsCommon();
        ObjectNameList onl = co.getObjectNameList();
        ObjectNameGroup ong = new ObjectNameGroup();
        ong.setObjectName("Keiko CollectionobjectsCommon");
        onl.getObjectNameGroup().add(ong);

		MultipartOutput multipart = new MultipartOutput();
		OutputPart commonPart = multipart.addPart(co,
				MediaType.APPLICATION_XML_TYPE);
		commonPart.getHeaders().add("label",
				collectionObjectClient.getCommonPartName());

		ClientResponse<Response> response = collectionObjectClient
				.create(multipart);
		Assert.assertEquals(response.getStatus(), Response.Status.CREATED
				.getStatusCode());
		result = extractId(response);

		return result;
	}

	/**
	 * Read collection object.
	 * 
	 * @param csid
	 *            the csid
	 * 
	 * @return the collectionobjects common
	 */
	static CollectionobjectsCommon readCollectionObject(String csid) {
		CollectionobjectsCommon result = null;

		ClientResponse<MultipartInput> response = collectionObjectClient
				.read(csid);
		Assert.assertEquals(response.getStatus(), Response.Status.OK
				.getStatusCode());
		try {
			MultipartInput input = (MultipartInput) response.getEntity();
			result = (CollectionobjectsCommon) extractPart(input,
					collectionObjectClient.getCommonPartName(),
					CollectionobjectsCommon.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Update collection object.
	 */
	static int updateCollectionObject(String csid) {
		ClientResponse<MultipartInput> response = collectionObjectClient
				.read(csid);
		Assert.assertEquals(response.getStatus(), Response.Status.OK
				.getStatusCode());

		MultipartInput input = (MultipartInput) response.getEntity();
		CollectionobjectsCommon collectionObject = (CollectionobjectsCommon) extractPart(
				input, collectionObjectClient.getCommonPartName(),
				CollectionobjectsCommon.class);
		Assert.assertNotNull(collectionObject);

		// Update the content of this resource.
		collectionObject.setObjectNumber("updated-"
				+ collectionObject.getObjectNumber());
        String name = collectionObject.getObjectNameList().getObjectNameGroup().get(0).getObjectName();
		collectionObject.getObjectNameList().getObjectNameGroup().get(0).setObjectName("updated-"+ name);

		// Submit the request to the service and store the response.
		MultipartOutput output = new MultipartOutput();
		OutputPart commonPart = output.addPart(collectionObject,
				MediaType.APPLICATION_XML_TYPE);
		commonPart.getHeaders().add("label",
				collectionObjectClient.getCommonPartName());

		response = collectionObjectClient.update(csid, output);
		int statusCode = response.getStatus();

		return statusCode;
	}

	//
	// Utility methods that belong somewhere in the SDK and NOT the sample.
	//
	
	/**
	 * Extract id.
	 * 
	 * @param res
	 *            the res
	 * 
	 * @return the string
	 */
	static String extractId(ClientResponse<Response> res) {
		String result = null;

		try {
			MultivaluedMap mvm = res.getMetadata();
			String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
			String[] segments = uri.split("/");
			result = segments[segments.length - 1];
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Extract part.
	 * 
	 * @param input
	 *            the input
	 * @param label
	 *            the label
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the object
	 * 
	 * @throws Exception
	 *             the exception
	 */
	static Object extractPart(MultipartInput input, String label, Class clazz) {
		Object obj = null;
		
		try {
			for (InputPart part : input.getParts()) {
				String partLabel = part.getHeaders().getFirst("label");
				if (label.equalsIgnoreCase(partLabel)) {
					String partStr = part.getBodyAsString();
					obj = part.getBody(clazz, null);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

}
