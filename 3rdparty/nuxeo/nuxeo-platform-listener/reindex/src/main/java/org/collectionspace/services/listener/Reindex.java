package org.collectionspace.services.listener;

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.nuxeo.listener.AbstractCSEventPostCommitListenerImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.elasticsearch.ElasticSearchComponent;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;

/**
 * Event listener that triggers reindexing of records in Elasticsearch when an associated record
 * is created/updated/deleted. When a record is created or updated Nuxeo will automatically
 * reindex it in ElasticSearch, but Nuxeo does not know about other records that may also need to
 * be reindexed; for example, if a related record denormalizes data from the updated record at
 * index time.
 */
public class Reindex extends AbstractCSEventPostCommitListenerImpl {
	private final static Log logger = LogFactory.getLog(Reindex.class);

    // FIXME: This listener runs asynchronously post-commit, so that reindexing records after a
    // save does not hold up the save.

    public static final String PREV_COVERAGE_KEY = "Reindex.PREV_COVERAGE";
    public static final String PREV_PUBLISH_TO_KEY = "Reindex.PREV_PUBLISH_TO";
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
        
        if (docType.startsWith("Media")) {
        	return true;
        }
        
        return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleCSEvent(Event event) {
        DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
        DocumentModel doc = eventContext.getSourceDocument();
        String eventName = event.getName();

        // When a media record is created, reindex the material item that is referenced by its
        // coverage field.
        
        // When a media record is updated and the coverage changed, reindex both the old and new
        // referenced material items.

        // When a media record is deleted, reindex the material item that was referenced by its
        // coverage field.
        
        // TODO: Make this configurable. This is currently hardcoded to the needs of the material
        // profile/Material Order application.
        
        if (
            eventName.equals(DocumentEventTypes.DOCUMENT_CREATED) ||
            eventName.equals(DocumentEventTypes.DOCUMENT_UPDATED)
        ) {
            String prevCoverage = (String) eventContext.getProperty(PREV_COVERAGE_KEY);
            String coverage = (String) doc.getProperty("media_common", "coverage");

            List<String> prevPublishTo = (List<String>) eventContext.getProperty(PREV_PUBLISH_TO_KEY);
            List<String> publishTo = (List<String>) doc.getProperty("media_materials", "publishToList");

            if (doc.getCurrentLifeCycleState().equals(LifeCycleConstants.DELETED_STATE)) {
                reindex(doc.getRepositoryName(), coverage);
            }
            else if (
                !ListUtils.isEqualList(prevPublishTo, publishTo) ||
                !StringUtils.equals(prevCoverage, coverage)
            ) {
                if (!StringUtils.equals(prevCoverage, coverage)) {
                    reindex(doc.getRepositoryName(), prevCoverage);
                }

                reindex(doc.getRepositoryName(), coverage);
            }
        }
        else if (eventName.equals(DocumentEventTypes.DOCUMENT_REMOVED)) {
            String prevCoverage = (String) eventContext.getProperty(PREV_COVERAGE_KEY);

            reindex(doc.getRepositoryName(), prevCoverage);
        }
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

    private void reindex(String repositoryName, String refName) {
        if (StringUtils.isEmpty(refName)) {
            return;
        }

        String escapedRefName = refName.replace("'", "\\'");
        String query = String.format("SELECT ecm:uuid FROM Materialitem WHERE collectionspace_core:refName = '%s'", escapedRefName);

        ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);
        es.runReindexingWorker(repositoryName, query);
    }
}
