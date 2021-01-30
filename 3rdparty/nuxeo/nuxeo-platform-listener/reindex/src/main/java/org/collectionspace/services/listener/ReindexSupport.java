package org.collectionspace.services.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

/**
 * Event listener that stores the values of fields of interest before documents are updated or
 * deleted. This is necessary because the previous/deleted document model will not be available
 * to a post-modification/deletion event listener. Storing the previous/deleted values allows
 * the post-modification/deletion event listener to take action if a field value was changed,
 * or if a document was deleted that had a certain field value.
 *
 * This is a separate class from the Reindex listener, because the Reindex listener should be
 * async and post-commit, so it must implement PostCommitEventListener. This listener must be
 * synchronous and pre-commit, so it must implement EventListener. Nuxeo does not support
 * a single class that implements both PostCommitEventListener and EventListener (such a listener
 * will only run synchronously).
 */
public class ReindexSupport extends AbstractCSEventSyncListenerImpl {
	private static final Logger logger = LoggerFactory.getLogger(ReindexSupport.class);

	@Override
	public boolean shouldHandleEvent(Event event) {
		DocumentEventContext eventContext = (DocumentEventContext) event.getContext();

		if (Framework.isBooleanPropertyTrue(Reindex.ELASTICSEARCH_ENABLED_PROP) && eventContext instanceof DocumentEventContext) {
			DocumentModel doc = eventContext.getSourceDocument();
			String docType = doc.getType();

			if (
				docType.startsWith("Media")
				|| docType.startsWith("Relation")
				|| docType.startsWith("Acquisition")
				|| docType.startsWith("Exhibition")
				) {
				return true;
			}
		}

		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleCSEvent(Event event) {
		// TODO: Make this configurable. This is currently hardcoded to the needs of the standard
		// profiles.

		// For core/all profiles:
		// - When a media record is about to be updated, store the value of the publishToList
		//   and altText fields.
		// - When an exhibition record is about to be updated, store the value of the title,
		//   generalNote, and curatorialNote fields.

		// For materials profile:
		// - When a media record is about to be updated, store the value of the coverage field.
		// - When a media record is about to be removed, store the value of the coverage field.

		DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
		DocumentModel doc = eventContext.getSourceDocument();
		String docType = doc.getType();
		String eventName = event.getName();

		if (docType.startsWith("Media")) {
			if (eventName.equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
				DocumentModel previousDoc = (DocumentModel) eventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
				String coverage = (String) previousDoc.getProperty("media_common", "coverage");
				String altText = (String) previousDoc.getProperty("media_common", "altText");

				// Materials profile had publishToList defined in a local extension schema before
				// that field was added to the common schema.

				List<String> publishTo = (List<String>) previousDoc.getProperty(
					previousDoc.hasSchema("media_materials") ? "media_materials" : "media_common",
					"publishToList");

				eventContext.setProperty(Reindex.PREV_ALT_TEXT_KEY, altText);
				eventContext.setProperty(Reindex.PREV_COVERAGE_KEY, coverage);
				eventContext.setProperty(Reindex.PREV_PUBLISH_TO_KEY, (Serializable) publishTo);
			}
			else if (eventName.equals(DocumentEventTypes.ABOUT_TO_REMOVE)) {
				String coverage = (String) doc.getProperty("media_common", "coverage");

				eventContext.setProperty(Reindex.PREV_COVERAGE_KEY, coverage);

				storePrevRelatedCollectionObjects(eventContext, doc);
			}
		}
		else if (docType.startsWith("Acquisition")) {
			if (eventName.equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
				DocumentModel previousDoc = (DocumentModel) eventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
				String creditLine = (String) previousDoc.getProperty("acquisitions_common", "creditLine");

				eventContext.setProperty(Reindex.PREV_CREDIT_LINE_KEY, creditLine);
			}
			else if (eventName.equals(DocumentEventTypes.ABOUT_TO_REMOVE)) {
				storePrevRelatedCollectionObjects(eventContext, doc);
			}
		}
		else if (docType.startsWith("Exhibition")) {
			if (eventName.equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
				DocumentModel previousDoc = (DocumentModel) eventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
				String title = (String) previousDoc.getProperty("exhibitions_common", "title");
				String generalNote = (String) previousDoc.getProperty("exhibitions_common", "generalNote");
				String curatorialNote = (String) previousDoc.getProperty("exhibitions_common", "curatorialNote");
				List<String> publishTo = (List<String>) previousDoc.getProperty("exhibitions_common", "publishToList");

				eventContext.setProperty(Reindex.PREV_EXH_TITLE_KEY, title);
				eventContext.setProperty(Reindex.PREV_EXH_GENERAL_NOTE_KEY, generalNote);
				eventContext.setProperty(Reindex.PREV_EXH_CURATORIAL_NOTE_KEY, curatorialNote);
				eventContext.setProperty(Reindex.PREV_EXH_PUBLISH_TO_KEY, (Serializable) publishTo);
			}
			else if (eventName.equals(DocumentEventTypes.ABOUT_TO_REMOVE)) {
				storePrevRelatedCollectionObjects(eventContext, doc);
			}
		}
		else if (docType.startsWith("Relation")) {
			if (eventName.equals(DocumentEventTypes.ABOUT_TO_REMOVE)) {
				String subjectDocumentType = (String) doc.getProperty("relations_common", "subjectDocumentType");
				String objectDocumentType = (String) doc.getProperty("relations_common", "objectDocumentType");

				if (
					(
						subjectDocumentType.equals("Media") ||
						subjectDocumentType.equals("Acquisition") ||
						subjectDocumentType.equals("Exhibition")
					)
					&& objectDocumentType.equals("CollectionObject")
				) {
					String collectionObjectCsid = (String) doc.getProperty("relations_common", "objectCsid");

					eventContext.setProperty(Reindex.PREV_RELATED_COLLECTION_OBJECT_CSID_KEY, (Serializable) Arrays.asList(collectionObjectCsid));
				}
			}
		}
	}

	private void storePrevRelatedCollectionObjects(DocumentEventContext eventContext, DocumentModel doc) {
		CoreSession session = doc.getCoreSession();
		String tenantId = (String) doc.getProperty("collectionspace_core", "tenantId");
		String csid = doc.getName();

		String relatedRecordQuery = String.format("SELECT * FROM Relation WHERE relations_common:subjectCsid = '%s' AND relations_common:objectDocumentType = 'CollectionObject' AND ecm:currentLifeCycleState = 'project' AND collectionspace_core:tenantId = '%s'", csid, tenantId);
		DocumentModelList relationDocs = session.query(relatedRecordQuery);
		List<String> collectionObjectCsids = new ArrayList<String>();

		if (relationDocs.size() > 0) {
			Iterator<DocumentModel> iterator = relationDocs.iterator();

			while (iterator.hasNext()) {
				DocumentModel relationDoc = iterator.next();
				String collectionObjectCsid = (String) relationDoc.getProperty("relations_common", "objectCsid");

				collectionObjectCsids.add(collectionObjectCsid);
			}
		}

		eventContext.setProperty(Reindex.PREV_RELATED_COLLECTION_OBJECT_CSID_KEY, (Serializable) collectionObjectCsids);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
