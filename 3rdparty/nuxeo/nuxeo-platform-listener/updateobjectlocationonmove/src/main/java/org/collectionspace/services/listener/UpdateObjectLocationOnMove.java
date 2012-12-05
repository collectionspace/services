package org.collectionspace.services.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateObjectLocationOnMove implements EventListener {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);

    @Override
    public void handleEvent(Event event) throws ClientException {

        logger.info("In handleEvent in UpdateObjectLocationOnMove ...");

        EventContext eventContext = event.getContext();
        if (eventContext == null) {
            return;
        }
        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();
        logger.debug("docType=" + docModel.getType());
        if (docModel.getType().startsWith(MovementConstants.NUXEO_DOCTYPE)
                && isActiveDocument(docModel)) {
            logger.info("A create or update event for an active Movement document was received by UpdateObjectLocationOnMove ...");
        }

    }

    /**
     * Identifies whether a document is an active document; that is, if
     * it is not a versioned record; not a proxy (symbolic link to an
     * actual record); and not in the 'deleted' workflow state.
     *
     * (A note relating the latter: Nuxeo appears to send 'documentModified' events
     * even on workflow transitions, such when records are 'soft deleted' by being
     * transitioned to the 'deleted' workflow state.)
     *
     * @param docModel
     * @return true if the document is an active document; false if it
     * is not.
     */
    private boolean isActiveDocument(DocumentModel docModel) {
        boolean isActiveDocument = false;
        try {
            if (!docModel.isVersion()
                    && !docModel.isProxy()
                    && !docModel.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
                isActiveDocument = true;
            }
        } catch (ClientException ce) {
            logger.warn("Error while identifying whether document is an active document: ", ce);
        }
        return isActiveDocument;
    }
}
