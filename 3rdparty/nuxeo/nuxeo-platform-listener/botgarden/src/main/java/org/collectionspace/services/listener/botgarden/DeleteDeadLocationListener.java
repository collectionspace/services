package org.collectionspace.services.listener.botgarden;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.movement.nuxeo.MovementBotGardenConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventListenerImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDeadLocationListener extends AbstractCSEventListenerImpl {
	final Logger logger = LoggerFactory.getLogger(DeleteDeadLocationListener.class);

    /*
     * Delete dead locations.
     */
    @Override
	public void handleEvent(Event event) {
        EventContext ec = event.getContext();

        if (isRegistered(event) && ec instanceof DocumentEventContext) {
            DocumentEventContext context = (DocumentEventContext) ec;
            DocumentModel doc = context.getSourceDocument();

            if (doc.getType().startsWith(MovementConstants.NUXEO_DOCTYPE) &&
            		!doc.isVersion() &&
            		!doc.isProxy() &&
            		!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            	String actionCode = (String) doc.getProperty(MovementBotGardenConstants.ACTION_CODE_SCHEMA_NAME,
            			MovementBotGardenConstants.ACTION_CODE_FIELD_NAME);

            	logger.debug("actionCode=" + actionCode);

            	if (actionCode != null && actionCode.equals(MovementBotGardenConstants.DEAD_ACTION_CODE)) {
            		CoreSession session = context.getCoreSession();

            		if (session.getAllowedStateTransitions(doc.getRef()).contains(WorkflowClient.WORKFLOWTRANSITION_DELETE)) {
            			session.followTransition(doc.getRef(), WorkflowClient.WORKFLOWTRANSITION_DELETE);
            		}
            	}
            }
        }
    }
}
