/*	
 * IDServiceNuxeoImpl
 *
 * Implementation of Nuxeo-specific portions of the ID Service,
 * such as storing and retrieving documents from the Nuxeo repository.
 *
 * NOTE: Placeholder class - not currently used.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Based on work by Richard Millet and Sanjay Dalal.
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */
 
package org.collectionspace.services.id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class IDServiceNuxeoImpl {      // implements IDService {

  // Placeholder constructor.
  public void IDServiceNuxeoImpl() {
  }

/*

  // May need doctypes, schema names for both ID Patterns and IDs
  
	final static String ID_NUXEO_DOCTYPE = "CollectionObject";
	final static String ID_NUXEO_SCHEMA_NAME = "collectionobject";
	final static String ID_NUXEO_DC_TITLE = "CollectionSpace-CollectionObject";

	// replace WORKSPACE_UID for resource workspace
	static String CS_COLLECTIONOBJECT_WORKSPACE_UID = "e4a8e3d4-0954-4c10-963a-cc4ed09d5112";

	public Document deleteCollectionObject(String csid)
			throws DocumentException, IOException {

		NuxeoRESTClient nxClient = getClient();
		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();

		pathParams.add("default");
		pathParams.add(csid);
		pathParams.add("deleteDocumentRestlet");
		Representation res = nxClient.get(pathParams, queryParams);
		SAXReader reader = new SAXReader();
		Document document = reader.read(res.getStream());
		
		return document;
	}

	public Document getCollectionObject(String csid) throws DocumentException,
			IOException {
		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();

		pathParams.add("default");
		pathParams.add(csid);
		pathParams.add("export");
		queryParams.put("format", "XML");

		NuxeoRESTClient nxClient = getClient();
		Representation res = nxClient.get(pathParams, queryParams);

		SAXReader reader = new SAXReader();
		Document document = reader.read(res.getStream());

		return document;
	}

	public Document getCollectionObjectList() throws DocumentException,
			IOException {
		Document result = null;

		NuxeoRESTClient nxClient = getClient();
		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();
		pathParams = Arrays.asList("default",
				CS_COLLECTIONOBJECT_WORKSPACE_UID, "browse");
		Representation res = nxClient.get(pathParams, queryParams);
		SAXReader reader = new SAXReader();
		result = reader.read(res.getStream());

		return result;
	}

	public Document postCollectionObject(CollectionObject co)
			throws DocumentException, IOException {
		NuxeoRESTClient nxClient = getClient();

		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();
		pathParams.add("default");
		pathParams.add(CS_COLLECTIONOBJECT_WORKSPACE_UID);
		pathParams.add("createDocument");
		queryParams.put("docType", CO_NUXEO_DOCTYPE);

		// a default title for the Dublin Core schema
		queryParams.put("dublincore:title", CO_NUXEO_DC_TITLE);

		// CollectionObject core values
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.OBJECT_NUMBER, co
				.getObjectNumber());
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.BRIEF_DESCRIPTION, co
				.getBriefDescription());
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.COMMENTS, co.getComments());
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.DIST_FEATURES, co
				.getDistFeatures());
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.OBJECT_NAME, co.getObjectName());
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.RESPONSIBLE_DEPT, co
				.getResponsibleDept());
		queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
				+ CollectionObjectJAXBSchema.TITLE, co.getTitle());

		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		Representation res = nxClient.post(pathParams, queryParams, bais);

		SAXReader reader = new SAXReader();
		Document document = reader.read(res.getStream());

		return document;
	}

	public Document putCollectionObject(String csid, CollectionObject theUpdate)
			throws DocumentException, IOException {
		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();
		pathParams.add("default");
		pathParams.add(csid);
		pathParams.add("updateDocumentRestlet");

		// todo: intelligent merge needed
		if (theUpdate.getObjectNumber() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.OBJECT_NUMBER, theUpdate
					.getObjectNumber());
		}

		if (theUpdate.getOtherNumber() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.OTHER_NUMBER, theUpdate
					.getOtherNumber());
		}

		if (theUpdate.getBriefDescription() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.BRIEF_DESCRIPTION, theUpdate
					.getBriefDescription());
		}

		if (theUpdate.getComments() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.COMMENTS, theUpdate
					.getComments());
		}

		if (theUpdate.getDistFeatures() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.DIST_FEATURES, theUpdate
					.getDistFeatures());
		}

		if (theUpdate.getObjectName() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.OBJECT_NAME, theUpdate
					.getObjectName());
		}

		if (theUpdate.getResponsibleDept() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.RESPONSIBLE_DEPT, theUpdate
					.getResponsibleDept());
		}

		if (theUpdate.getTitle() != null) {
			queryParams.put(CO_NUXEO_SCHEMA_NAME + ":"
					+ CollectionObjectJAXBSchema.TITLE, theUpdate.getTitle());
		}

		NuxeoRESTClient nxClient = getClient();
		Representation res = nxClient.get(pathParams, queryParams);
		SAXReader reader = new SAXReader();
		Document document = reader.read(res.getStream());

		return document;
	}

*/

}
