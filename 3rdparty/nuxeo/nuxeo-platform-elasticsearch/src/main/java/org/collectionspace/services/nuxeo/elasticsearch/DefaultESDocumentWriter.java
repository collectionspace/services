package org.collectionspace.services.nuxeo.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.collectionspace.services.common.api.RefNameUtils;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonESDocumentWriter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

public class DefaultESDocumentWriter extends JsonESDocumentWriter {
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void writeDoc(JsonGenerator jg, DocumentModel doc, String[] schemas,
			Map<String, String> contextParameters, HttpHeaders headers)
			throws IOException {

		ObjectNode denormValues = getDenormValues(doc);

		jg.writeStartObject();

		writeSystemProperties(jg, doc);
		writeSchemas(jg, doc, schemas);
		writeContextParameters(jg, doc, contextParameters);
		writeDenormValues(jg, doc, denormValues);

		jg.writeEndObject();
		jg.flush();
	}

	public ObjectNode getDenormValues(DocumentModel doc) {
		ObjectNode denormValues = objectMapper.createObjectNode();
		String docType = doc.getType();

		if (docType.startsWith("CollectionObject")) {
			CoreSession session = doc.getCoreSession();
			String csid = doc.getName();
			String tenantId = (String) doc.getProperty("collectionspace_core", "tenantId");

			denormMediaRecords(session, csid, tenantId, denormValues);
			denormAcquisitionRecords(session, csid, tenantId, denormValues);
			denormExhibitionRecords(session, csid, tenantId, denormValues);

			// Compute the title of the record for the public browser, and store it so that it can
			// be used for sorting ES query results.

			String title = computeTitle(doc);

			if (title != null) {
				denormValues.put("title", title);
			}

			// Create a list of production years from the production date structured dates.

			List<Map<String, Object>> prodDateGroupList = (List<Map<String, Object>>) doc.getProperty("collectionobjects_common", "objectProductionDateGroupList");

			denormValues.putArray("prodYears").addAll(structDatesToYearNodes(prodDateGroupList));
		}

		return denormValues;
	}

	public void writeDenormValues(JsonGenerator jg, DocumentModel doc, ObjectNode denormValues) throws IOException {
		if (denormValues != null && denormValues.size() > 0) {
			if (jg.getCodec() == null) {
				jg.setCodec(objectMapper);
			}

			Iterator<Map.Entry<String, JsonNode>> entries = denormValues.getFields();

			while (entries.hasNext()) {
				Map.Entry<String, JsonNode> entry = entries.next();

				jg.writeFieldName("collectionspace_denorm:" + entry.getKey());
				jg.writeTree(entry.getValue());
			}
		}
	}

	private void denormMediaRecords(CoreSession session, String csid, String tenantId, ObjectNode denormValues) {
		// Store the csid and alt text of media records that are related to this object.

		String relatedRecordQuery = String.format("SELECT * FROM Relation WHERE relations_common:subjectCsid = '%s' AND relations_common:objectDocumentType = 'Media' AND ecm:currentLifeCycleState = 'project' AND collectionspace_core:tenantId = '%s'", csid, tenantId);
		DocumentModelList relationDocs = session.query(relatedRecordQuery);
		List<JsonNode> mediaCsids = new ArrayList<JsonNode>();
		List<JsonNode> mediaAltTexts = new ArrayList<JsonNode>();

		if (relationDocs.size() > 0) {
			Iterator<DocumentModel> iterator = relationDocs.iterator();

			while (iterator.hasNext()) {
				DocumentModel relationDoc = iterator.next();
				String mediaCsid = (String) relationDoc.getProperty("relations_common", "objectCsid");
				DocumentModel mediaDoc = getRecordByCsid(session, tenantId, "Media", mediaCsid);

				if (isMediaPublished(mediaDoc)) {
					mediaCsids.add(new TextNode(mediaCsid));

					String altText = (String) mediaDoc.getProperty("media_common", "altText");

					if (altText == null) {
						altText = "";
					}

					mediaAltTexts.add(new TextNode(altText));
				}
			}
		}

		denormValues.putArray("mediaCsid").addAll(mediaCsids);
		denormValues.putArray("mediaAltText").addAll(mediaAltTexts);
		denormValues.put("hasMedia", mediaCsids.size() > 0);
	}

	private void denormAcquisitionRecords(CoreSession session, String csid, String tenantId, ObjectNode denormValues) {
		// Store the credit lines of acquisition records that are related to this object.

		String relatedRecordQuery = String.format("SELECT * FROM Relation WHERE relations_common:subjectCsid = '%s' AND relations_common:objectDocumentType = 'Acquisition' AND ecm:currentLifeCycleState = 'project' AND collectionspace_core:tenantId = '%s'", csid, tenantId);
		DocumentModelList relationDocs = session.query(relatedRecordQuery);
		List<JsonNode> creditLines = new ArrayList<JsonNode>();

		if (relationDocs.size() > 0) {
			Iterator<DocumentModel> iterator = relationDocs.iterator();

			while (iterator.hasNext()) {
				DocumentModel relationDoc = iterator.next();
				String acquisitionCsid = (String) relationDoc.getProperty("relations_common", "objectCsid");
				String creditLine = getCreditLine(session, tenantId, acquisitionCsid);

				if (creditLine != null && creditLine.length() > 0) {
					creditLines.add(new TextNode(creditLine));
				}
			}
		}

		denormValues.putArray("creditLine").addAll(creditLines);
}

private void denormExhibitionRecords(CoreSession session, String csid, String tenantId, ObjectNode denormValues) {
	// Store the title, general note, and curatorial note of exhibition records that are published, and related to this object.

	String relatedRecordQuery = String.format("SELECT * FROM Relation WHERE relations_common:subjectCsid = '%s' AND relations_common:objectDocumentType = 'Exhibition' AND ecm:currentLifeCycleState = 'project' AND collectionspace_core:tenantId = '%s'", csid, tenantId);
	DocumentModelList relationDocs = session.query(relatedRecordQuery);
	List<JsonNode> exhibitions = new ArrayList<JsonNode>();

	if (relationDocs.size() > 0) {
		Iterator<DocumentModel> iterator = relationDocs.iterator();

		while (iterator.hasNext()) {
			DocumentModel relationDoc = iterator.next();
			String exhibitionCsid = (String) relationDoc.getProperty("relations_common", "objectCsid");
			DocumentModel exhibitionDoc = getRecordByCsid(session, tenantId, "Exhibition", exhibitionCsid);

			if (exhibitionDoc != null && isExhibitionPublished(exhibitionDoc)) {
				ObjectNode exhibitionNode = objectMapper.createObjectNode();

				String title = (String) exhibitionDoc.getProperty("exhibitions_common", "title");
				String generalNote = (String) exhibitionDoc.getProperty("exhibitions_common", "generalNote");
				String curatorialNote = (String) exhibitionDoc.getProperty("exhibitions_common", "curatorialNote");

				exhibitionNode.put("title", title);
				exhibitionNode.put("generalNote", generalNote);
				exhibitionNode.put("curatorialNote", curatorialNote);

				exhibitions.add(exhibitionNode);
			}
		}
	}

	denormValues.putArray("exhibition").addAll(exhibitions);
}

	/**
	 * Compute a title for the public browser. This needs to be indexed in ES so that it can
	 * be used for sorting. (Even if it's just extracting the primary value.)
	 */
	protected String computeTitle(DocumentModel doc) {
		List<Map<String, Object>> titleGroups = (List<Map<String, Object>>) doc.getProperty("collectionobjects_common", "titleGroupList");
		String primaryTitle = null;

		if (titleGroups.size() > 0) {
			Map<String, Object> primaryTitleGroup = titleGroups.get(0);
			primaryTitle = (String) primaryTitleGroup.get("title");
		}

		if (StringUtils.isNotEmpty(primaryTitle)) {
			return primaryTitle;
		}

		List<Map<String, Object>> objectNameGroups = (List<Map<String, Object>>) doc.getProperty("collectionobjects_common", "objectNameList");
		String primaryObjectName = null;

		if (objectNameGroups.size() > 0) {
			Map<String, Object> primaryObjectNameGroup = objectNameGroups.get(0);
			primaryObjectName = (String) primaryObjectNameGroup.get("objectName");

			// The object might be a refname in some profiles/tenants. If it is, use only the display name.

			try {
				String displayName = RefNameUtils.getDisplayName(primaryObjectName);

				if (displayName != null) {
					primaryObjectName = displayName;
				}
			}
			catch (Exception e) {}
		}

		return primaryObjectName;
	}

	private boolean isPublished(DocumentModel doc, String publishedFieldPart, String publishedFieldName) {
		boolean isPublished = false;

		if (doc != null) {
			List<String> publishToValues = (List<String>) doc.getProperty(publishedFieldPart, publishedFieldName);

			if (publishToValues != null) {
				for (int i=0; i<publishToValues.size(); i++) {
					String value = publishToValues.get(i);
					String shortId = RefNameUtils.getItemShortId(value);

					if (shortId.equals("all") || shortId.equals("cspacepub")) {
						isPublished = true;
						break;
					}
				}
			}
		}

		return isPublished;

	}

	private boolean isMediaPublished(DocumentModel mediaDoc) {
		return isPublished(mediaDoc, "media_common", "publishToList");
	}

	private boolean isExhibitionPublished(DocumentModel exhibitionDoc) {
		return isPublished(exhibitionDoc, "exhibitions_common", "publishToList");
	}

	private String getCreditLine(CoreSession session, String tenantId, String acquisitionCsid) {
		String creditLine = null;
		DocumentModel acquisitionDoc = getRecordByCsid(session, tenantId, "Acquisition", acquisitionCsid);

		if (acquisitionDoc != null) {
			creditLine = (String) acquisitionDoc.getProperty("acquisitions_common", "creditLine");
		}

		return creditLine;
	}

	protected DocumentModel getRecordByCsid(CoreSession session, String tenantId, String recordType, String csid) {
		String getRecordQuery = String.format("SELECT * FROM %s WHERE ecm:name = '%s' AND ecm:currentLifeCycleState = 'project' AND collectionspace_core:tenantId = '%s'", recordType, csid, tenantId);

		DocumentModelList docs = session.query(getRecordQuery);

		if (docs != null && docs.size() > 0) {
			return docs.get(0);
		}

		return null;
	}

	protected List<JsonNode> structDateToYearNodes(Map<String, Object> structDate) {
		return structDatesToYearNodes(Arrays.asList(structDate));
	}

	protected List<JsonNode> structDatesToYearNodes(List<Map<String, Object>> structDates) {
		Set<Integer> years = new HashSet<Integer>();

		for (Map<String, Object> structDate : structDates) {
			if (structDate != null) {
				GregorianCalendar earliestCalendar = (GregorianCalendar) structDate.get("dateEarliestScalarValue");
				GregorianCalendar latestCalendar = (GregorianCalendar) structDate.get("dateLatestScalarValue");

				if (earliestCalendar != null && latestCalendar != null) {
					// Grr @ latest scalar value historically being exclusive.
					// Subtract one day to make it inclusive.
					latestCalendar.add(Calendar.DATE, -1);

					Integer earliestYear = earliestCalendar.get(Calendar.YEAR);
					Integer latestYear = latestCalendar.get(Calendar.YEAR);;

					for (int year = earliestYear; year <= latestYear; year++) {
						years.add(year);
					}
				}
			}
		}

		List<Integer> yearList = new ArrayList<Integer>(years);
		Collections.sort(yearList);

		List<JsonNode> yearNodes = new ArrayList<JsonNode>();

		for (Integer year : yearList) {
			yearNodes.add(new IntNode(year));
		}

		return yearNodes;
	}
}
