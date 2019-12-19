package org.collectionspace.services.listener;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;

import org.nuxeo.ecm.core.api.DocumentModel;
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
    final static Log logger = LogFactory.getLog(ReindexSupport.class);

    @Override
	public boolean shouldHandleEvent(Event event) {
        if (Framework.isBooleanPropertyTrue(Reindex.ELASTICSEARCH_ENABLED_PROP) && event instanceof DocumentEventContext) {
            DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
            DocumentModel doc = eventContext.getSourceDocument();
            String docType = doc.getType();
            if (docType.startsWith("Media")) {
            	return true;
            }
        }

        return false;
    }
    
	@Override
    @SuppressWarnings("unchecked")
    public void handleCSEvent(Event event) {
        // When a media record is about to be updated, store the value of the coverage and
        // publishToList fields.

        // When a media record is about to be removed, store the value of the coverage field.

        // TODO: Make this configurable. This is currently hardcoded to the needs of the material
        // profile/Material Order application.
        DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
        DocumentModel doc = eventContext.getSourceDocument();
        String eventName = event.getName();

        if (eventName.equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
            DocumentModel previousDoc = (DocumentModel) eventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
            String coverage = (String) previousDoc.getProperty("media_common", "coverage");
            List<String> publishTo = (List<String>) previousDoc.getProperty("media_materials", "publishToList");

            eventContext.setProperty(Reindex.PREV_COVERAGE_KEY, coverage);
            eventContext.setProperty(Reindex.PREV_PUBLISH_TO_KEY, (Serializable) publishTo);
        }
        else if (eventName.equals(DocumentEventTypes.ABOUT_TO_REMOVE)) {
            String coverage = (String) doc.getProperty("media_common", "coverage");
            eventContext.setProperty(Reindex.PREV_COVERAGE_KEY, coverage);
        }
    }
    
    @Override
    public Log getLogger() {
    	return logger;
    }
}
