package org.collectionspace.services.nuxeo.listener.relation;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class RelationSubOrObjDeletionListener implements EventListener {

    public static final String WORKFLOW_TRANSITION_TO = "to";
    public static final String WORKFLOW_TRANSITION_DELETED = "deleted";
    // FIXME We might experiment here with using log4j here instead of Apache Commons Logging
    final Log logger = LogFactory.getLog(RelationSubOrObjDeletionListener.class);

    public void handleEvent(Event event) throws ClientException {
        logger.info("In handleEvent in RelationSubOrObjDeletionListener ...");

        EventContext eventContext = event.getContext();

        if (isDocumentSoftDeletedEvent(eventContext)) {
            
            DocumentEventContext docContext = (DocumentEventContext) eventContext;
            DocumentModel docModel = docContext.getSourceDocument();
            
            // FIXME: Temporary for debugging
            logger.debug("docType=" + docModel.getType());
            logger.debug("id=" + docModel.getSourceId());
            logger.debug("transition to=" + docContext.getProperties().get(WORKFLOW_TRANSITION_TO));
            
            // Get a list of relation records where the just-soft-deleted
            // document is either the subject or object of the relation
            
            // Cycle through the list, soft deleting each of these relation records

        }

    }

    /**
     * Identifies whether a supplied event concerns a document that has
     * been transitioned to the 'deleted' workflow state.
     * 
     * @param eventContext an event context
     * 
     * @return true if this event concerns a document that has
     * been transitioned to the 'deleted' workflow state.
     */
    private boolean isDocumentSoftDeletedEvent(EventContext eventContext) {
        boolean isSoftDeletedEvent = false;
        if (eventContext instanceof DocumentEventContext) {
            if (eventContext.getProperties().containsKey(WORKFLOW_TRANSITION_TO)
                    && eventContext.getProperties().get(WORKFLOW_TRANSITION_TO).equals(WORKFLOW_TRANSITION_DELETED)) {
                isSoftDeletedEvent = true;
            }
        }
        return isSoftDeletedEvent;
    }
}
