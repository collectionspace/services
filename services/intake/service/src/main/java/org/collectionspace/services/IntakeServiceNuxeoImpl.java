/**
 * 
 */
package org.collectionspace.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.nuxeo.client.rest.NuxeoRESTClient;
import org.collectionspace.services.nuxeo.CollectionSpaceServiceNuxeoImpl;
import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.IntakeJAXBSchema;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.restlet.resource.Representation;

/**
 * @author remillet
 * 
 */
public class IntakeServiceNuxeoImpl extends
		CollectionSpaceServiceNuxeoImpl implements IntakeService {

	final static String INTAKE_NUXEO_DOCTYPE = "Intake";
	final static String INTAKE_NUXEO_SCHEMA_NAME = "intake";
	final static String INTAKE_NUXEO_DC_TITLE = "CollectionSpace-Intake";

	// replace WORKSPACE_UID for resource workspace
	static String CS_INTAKE_WORKSPACE_UID = "c04210c4-9426-475f-b4ee-aa3d6aa4b97c";

	public Document deleteIntake(String csid)
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

	public Document getIntake(String csid) throws DocumentException,
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

	public Document getIntakeList() throws DocumentException,
			IOException {
		Document result = null;

		NuxeoRESTClient nxClient = getClient();
		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();
		pathParams = Arrays.asList("default",
				CS_INTAKE_WORKSPACE_UID, "browse");
		Representation res = nxClient.get(pathParams, queryParams);
		SAXReader reader = new SAXReader();
		result = reader.read(res.getStream());

		return result;
	}

	public Document postIntake(Intake co)
			throws DocumentException, IOException {
		NuxeoRESTClient nxClient = getClient();

		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();
		pathParams.add("default");
		pathParams.add(CS_INTAKE_WORKSPACE_UID);
		pathParams.add("createDocument");
		queryParams.put("docType", INTAKE_NUXEO_DOCTYPE);

		// a default title for the Dublin Core schema
		queryParams.put("dublincore:title", INTAKE_NUXEO_DC_TITLE);

		// Intake core values
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.CURRENT_OWNER, co
				.getCurrentOwner());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.DEPOSITOR, co.getDepositor());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS, co
				.getDepositorsRequirements());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.ENTRY_DATE, co.getEntryDate());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.ENTRY_METHOD, co
				.getEntryMethod());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.ENTRY_NOTE, co.getEntryNote());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.ENTRY_NUMBER, co
				.getEntryNumber());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.ENTRY_REASON, co.getEntryReason());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.PACKING_NOTE, co.getPackingNote());
		queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
				+ IntakeJAXBSchema.RETURN_DATE, co.getReturnDate());

		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
		Representation res = nxClient.post(pathParams, queryParams, bais);

		SAXReader reader = new SAXReader();
		Document document = reader.read(res.getStream());

		return document;
	}

	public Document putIntake(String csid, Intake theUpdate)
			throws DocumentException, IOException {
		List<String> pathParams = new ArrayList<String>();
		Map<String, String> queryParams = new HashMap<String, String>();
		pathParams.add("default");
		pathParams.add(csid);
		pathParams.add("updateDocumentRestlet");

		// todo: intelligent merge needed
		if (theUpdate.getCurrentOwner() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.CURRENT_OWNER, theUpdate.getCurrentOwner());
		}

		if (theUpdate.getDepositor() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.DEPOSITOR, theUpdate.getDepositor());
		}

		if (theUpdate.getDepositorsRequirements() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS, theUpdate.getDepositorsRequirements());
		}

		if (theUpdate.getEntryDate() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.ENTRY_DATE, theUpdate.getEntryDate());
		}

		if (theUpdate.getEntryMethod() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.ENTRY_METHOD, theUpdate.getEntryMethod());
		}

		if (theUpdate.getEntryNote() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.ENTRY_NOTE, theUpdate.getEntryNote());
		}

		if (theUpdate.getEntryNumber() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.ENTRY_NUMBER, theUpdate.getEntryNumber());
		}

		if (theUpdate.getEntryReason() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.ENTRY_REASON, theUpdate.getEntryReason());
		}

		if (theUpdate.getPackingNote() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.PACKING_NOTE, theUpdate.getPackingNote());
		}

		if (theUpdate.getReturnDate() != null) {
			queryParams.put(INTAKE_NUXEO_SCHEMA_NAME + ":"
					+ IntakeJAXBSchema.RETURN_DATE, theUpdate.getReturnDate());
		}

		NuxeoRESTClient nxClient = getClient();
		Representation res = nxClient.get(pathParams, queryParams);
		SAXReader reader = new SAXReader();
		Document document = reader.read(res.getStream());

		return document;
	}

}
