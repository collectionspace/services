package org.collectionspace.services.listener;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
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
public class Reindex implements PostCommitEventListener {
    // FIXME: This listener runs asynchronously post-commit, so that reindexing records after a
    // save does not hold up the save. In order to make it async, this class does not extend
    // AbstractCSEventListenerImpl, because AbstractCSEventListenerImpl does not implement
    // PostCommitEventListener (DRYD-477). Therefore this listener is not able to use the
    // isRegistered method of AbstractCSEventListenerImpl to determine if it has been registered to
    // run for the current tenant. Instead, it relies on the ReindexSupport listener, which does
    // extend AbstractCSEventListenerImpl, to set a property in the event context that is used to
    // determine if this listener should run. This means that this listener will be considered to
    // be registered if and only if the ReindexSupport listener is registered.

    public static final String IS_REGISTERED_KEY = "Reindex.IS_REGISTERED";
    public static final String PREV_COVERAGE_KEY = "Reindex.PREV_COVERAGE";
    public static final String PREV_PUBLISH_TO_KEY = "Reindex.PREV_PUBLISH_TO";

    @Override
    public void handleEvent(EventBundle events) {
        // When a media record is created, reindex the material item that is referenced by its
        // coverage field.
        
        // When a media record is updated and the coverage changed, reindex both the old and new
        // referenced material items.

        // When a media record is deleted, reindex the material item that was referenced by its
        // coverage field.
        
        // TODO: Make this configurable. This is currently hardcoded to the needs of the material
        // profile/Material Order application.

        if (Framework.isBooleanPropertyTrue("elasticsearch.enabled") && events.size() > 0) {
            Iterator<Event> iter = events.iterator();

            while (iter.hasNext()) {
                Event event = iter.next();
                DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
                Boolean isRegistered = (Boolean) eventContext.getProperty(IS_REGISTERED_KEY);

                if (isRegistered != null && isRegistered == true) {
                    DocumentModel doc = eventContext.getSourceDocument();
                    String docType = doc.getType();
                    String eventName = event.getName();
    
                    if (docType.startsWith("Media")) {
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
                }
            }
        }
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
