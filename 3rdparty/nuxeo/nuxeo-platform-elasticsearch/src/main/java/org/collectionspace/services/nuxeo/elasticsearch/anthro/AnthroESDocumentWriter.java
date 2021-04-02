package org.collectionspace.services.nuxeo.elasticsearch.anthro;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.nuxeo.elasticsearch.DefaultESDocumentWriter;
import org.nuxeo.ecm.core.api.DocumentModel;

public class AnthroESDocumentWriter extends DefaultESDocumentWriter {

	@Override
	public ObjectNode getDenormValues(DocumentModel doc) {
		ObjectNode denormValues = super.getDenormValues(doc);
		String docType = doc.getType();

		if (docType.startsWith("CollectionObject")) {
			// Create a list of collection years from the field collection date structured date.

			Map<String, Object> fieldCollectionDateGroup = (Map<String, Object>) doc.getProperty("collectionobjects_common", "fieldCollectionDateGroup");

			denormValues.putArray("collectionYears").addAll(structDateToYearNodes(fieldCollectionDateGroup));
		}

		return denormValues;
	}

	@Override
	protected String computeTitle(DocumentModel doc) {
		List<Map<String, Object>> objectNameGroups = (List<Map<String, Object>>) doc.getProperty("collectionobjects_common", "objectNameList");
		String primaryObjectName = null;

		if (objectNameGroups.size() > 0) {
			Map<String, Object> primaryObjectNameGroup = objectNameGroups.get(0);
			primaryObjectName = (String) primaryObjectNameGroup.get("objectName");
		}

		if (StringUtils.isNotEmpty(primaryObjectName)) {
			return primaryObjectName;
		}

		List<Map<String, Object>> titleGroups = (List<Map<String, Object>>) doc.getProperty("collectionobjects_common", "titleGroupList");
		String primaryTitle = null;

		if (titleGroups.size() > 0) {
			Map<String, Object> primaryTitleGroup = titleGroups.get(0);
			primaryTitle = (String) primaryTitleGroup.get("title");
		}

		if (StringUtils.isNotEmpty(primaryTitle)) {
			return primaryTitle;
		}

		List<Map<String, Object>> taxonomicIdentGroups = (List<Map<String, Object>>) doc.getProperty("collectionobjects_naturalhistory_extension", "taxonomicIdentGroupList");
		String primaryTaxon = null;

		if (taxonomicIdentGroups.size() > 0) {
			Map<String, Object> primaryTaxonomicIdentGroup = taxonomicIdentGroups.get(0);
			primaryTaxon = (String) primaryTaxonomicIdentGroup.get("taxon");
		}

		if (StringUtils.isNotEmpty(primaryTaxon)) {
			primaryTaxon = RefNameUtils.getDisplayName(primaryTaxon);
		}

		return primaryTaxon;
	}
}
