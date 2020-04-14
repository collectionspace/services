package org.collectionspace.services.nuxeo.elasticsearch.anthro;

import java.util.Map;

import org.codehaus.jackson.node.ObjectNode;

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
}
