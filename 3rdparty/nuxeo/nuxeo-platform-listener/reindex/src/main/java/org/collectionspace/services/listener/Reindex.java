package org.collectionspace.services.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventPostCommitListenerImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.elasticsearch.ElasticSearchComponent;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event listener that triggers reindexing of records in Elasticsearch when an associated record
 * is created/updated/deleted. When a record is created or updated Nuxeo will automatically
 * reindex it in ElasticSearch, but Nuxeo does not know about other records that may also need to
 * be reindexed; for example, if a related record denormalizes data from the updated record at
 * index time.
 */
public class Reindex extends AbstractCSEventPostCommitListenerImpl {
	private static final Logger logger = LoggerFactory.getLogger(Reindex.class);

	// This listener runs asynchronously post-commit, so that reindexing records after a
	// save does not hold up the save.

	public static final String PREV_COVERAGE_KEY = "Reindex.PREV_COVERAGE";
	public static final String PREV_ALT_TEXT_KEY = "Reindex.PREV_ALT_TEXT";
	public static final String PREV_CREDIT_LINE_KEY = "Reindex.PREV_CREDIT_LINE";
	public static final String PREV_PUBLISH_TO_KEY = "Reindex.PREV_PUBLISH_TO";
	public static final String PREV_EXH_TITLE_KEY = "Reindex.PREV_EXH_TITLE";
	public static final String PREV_EXH_GENERAL_NOTE_KEY = "Reindex.PREV_EXH_GENERAL_NOTE";
	public static final String PREV_EXH_CURATORIAL_NOTE_KEY = "Reindex.PREV_EXH_CURATORIAL_NOTE";
	public static final String PREV_EXH_PUBLISH_TO_KEY = "Reindex.PREV_EXH_PUBLISH_TO";
	public static final String PREV_RELATED_COLLECTION_OBJECT_CSID_KEY = "Reindex.PREV_RELATED_COLLECTION_OBJECT_CSID";
	public static final String ELASTICSEARCH_ENABLED_PROP = "elasticsearch.enabled";

	@Override
	public boolean shouldHandleEventBundle(EventBundle eventBundle) {
		if (Framework.isBooleanPropertyTrue(ELASTICSEARCH_ENABLED_PROP) && eventBundle.size() > 0) {
			return true;
		}

		return false;
	}

	@Override
	public boolean shouldHandleEvent(Event event) {
		DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
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

		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleCSEvent(Event event) {
		DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
		DocumentModel doc = eventContext.getSourceDocument();
		String docType = doc.getType();
		String eventName = event.getName();

		// TODO: Make this configurable. This is currently hardcoded to the needs of the standard
		// profiles.

		if (docType.startsWith("Media")) {
			// When a media record is created, reindex the material item that is referenced by its
			// coverage field.

			// When a media record is updated and the coverage changed, reindex both the old and new
			// referenced material items.

			// When a media record is deleted, reindex the material item that was referenced by its
			// coverage field.

			if (
				eventName.equals(DocumentEventTypes.DOCUMENT_CREATED) ||
				eventName.equals(DocumentEventTypes.DOCUMENT_UPDATED)
			) {
				String prevCoverage = (String) eventContext.getProperty(PREV_COVERAGE_KEY);
				String coverage = (String) doc.getProperty("media_common", "coverage");

				List<String> prevPublishTo = (List<String>) eventContext.getProperty(PREV_PUBLISH_TO_KEY);

				// Materials profile had publishToList defined in a local extension schema before
				// that field was added to the common schema.

				List<String> publishTo = (List<String>) doc.getProperty(
					doc.hasSchema("media_materials") ? "media_materials" : "media_common",
					"publishToList");

				String prevAltText = (String) eventContext.getProperty(PREV_ALT_TEXT_KEY);
				String altText = (String) doc.getProperty("media_common", "altText");

				if (
					!ListUtils.isEqualList(prevPublishTo, publishTo) ||
					!StringUtils.equals(prevAltText, altText) ||
					!StringUtils.equals(prevCoverage, coverage)
				) {
					if (!StringUtils.equals(prevCoverage, coverage)) {
						reindexMaterial(doc.getRepositoryName(), prevCoverage);
					}

					reindexMaterial(doc.getRepositoryName(), coverage);

					if (
						!ListUtils.isEqualList(prevPublishTo, publishTo) ||
						!StringUtils.equals(prevAltText, altText)
					) {
						reindexRelatedCollectionObjects(doc);
					}
				}
			}
			else if (eventName.equals("lifecycle_transition_event") && doc.getCurrentLifeCycleState().equals("deleted")) {
				String coverage = (String) doc.getProperty("media_common", "coverage");

				reindexMaterial(doc.getRepositoryName(), coverage);
			}
			else if (eventName.equals(DocumentEventTypes.DOCUMENT_REMOVED)) {
				String prevCoverage = (String) eventContext.getProperty(PREV_COVERAGE_KEY);

				reindexMaterial(doc.getRepositoryName(), prevCoverage);
				reindexPrevRelatedCollectionObjects(eventContext);
			}
		}
		else if (docType.startsWith("Acquisition")) {
			if (eventName.equals(DocumentEventTypes.DOCUMENT_UPDATED)) {
				String prevCreditLine = (String) eventContext.getProperty(PREV_CREDIT_LINE_KEY);
				String creditLine = (String) doc.getProperty("acquisitions_common", "creditLine");

				if (!StringUtils.equals(prevCreditLine, creditLine)) {
					reindexRelatedCollectionObjects(doc);
				}
			}
			else if (eventName.equals(DocumentEventTypes.DOCUMENT_REMOVED)) {
				reindexPrevRelatedCollectionObjects(eventContext);
			}
		}
		else if (docType.startsWith("Exhibition")) {
			if (eventName.equals(DocumentEventTypes.DOCUMENT_UPDATED)) {
				String prevTitle = (String) eventContext.getProperty(PREV_EXH_TITLE_KEY);
				String prevGeneralNote = (String) eventContext.getProperty(PREV_EXH_GENERAL_NOTE_KEY);
				String prevCuratorialNote = (String) eventContext.getProperty(PREV_EXH_CURATORIAL_NOTE_KEY);
				List<String> prevPublishTo = (List<String>) eventContext.getProperty(PREV_EXH_PUBLISH_TO_KEY);

				String title = (String) doc.getProperty("exhibitions_common", "title");
				String generalNote = (String) doc.getProperty("exhibitions_common", "generalNote");
				String curatorialNote = (String) doc.getProperty("exhibitions_common", "curatorialNote");
				List<String> publishTo = (List<String>) doc.getProperty("exhibitions_common", "publishToList");

				if (
					!ListUtils.isEqualList(prevPublishTo, publishTo) ||
					!StringUtils.equals(prevTitle, title) ||
					!StringUtils.equals(prevGeneralNote, generalNote) ||
					!StringUtils.equals(prevCuratorialNote, curatorialNote)
				) {
					reindexRelatedCollectionObjects(doc);
				}
			}
			else if (eventName.equals(DocumentEventTypes.DOCUMENT_REMOVED)) {
				reindexPrevRelatedCollectionObjects(eventContext);
			}
		}
		else if (docType.startsWith("Relation")) {
			if (
				eventName.equals(DocumentEventTypes.DOCUMENT_CREATED)
				|| (eventName.equals("lifecycle_transition_event") && doc.getCurrentLifeCycleState().equals("deleted"))
			) {
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

					reindexCollectionObject(doc.getRepositoryName(), collectionObjectCsid);
				}
			}
			else if (eventName.equals(DocumentEventTypes.DOCUMENT_REMOVED)) {
				reindexPrevRelatedCollectionObjects(eventContext);
			}
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private void reindexMaterial(String repositoryName, String refName) {
		if (StringUtils.isEmpty(refName) || !refName.startsWith(RefNameUtils.URN_PREFIX)) {
			return;
		}

		String escapedRefName = refName.replace("'", "\\'");
		String query = String.format("SELECT ecm:uuid FROM Materialitem WHERE collectionspace_core:refName = '%s'", escapedRefName);

		ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);
		es.runReindexingWorker(repositoryName, query);
	}

	private void reindexPrevRelatedCollectionObjects(DocumentEventContext eventContext) {
		List<String> prevRelatedCollectionObjectCsids = (List<String>) eventContext.getProperty(PREV_RELATED_COLLECTION_OBJECT_CSID_KEY);

		if (prevRelatedCollectionObjectCsids != null) {
			for (String prevRelatedCollectionObjectCsid : prevRelatedCollectionObjectCsids) {
				reindexCollectionObject(eventContext.getRepositoryName(), prevRelatedCollectionObjectCsid);
			}
		}
	}

	private void reindexRelatedCollectionObjects(DocumentModel doc) {
		CoreSession session = doc.getCoreSession();
		String repositoryName = doc.getRepositoryName();
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

		for (String collectionObjectCsid : collectionObjectCsids) {
			reindexCollectionObject(repositoryName, collectionObjectCsid);
		}
	}

	private void reindexCollectionObject(String repositoryName, String csid) {
		if (StringUtils.isEmpty(csid)) {
			return;
		}

		String query = String.format("SELECT ecm:uuid FROM CollectionObject WHERE ecm:name = '%s'", csid);

		ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);
		es.runReindexingWorker(repositoryName, query);
	}
}
