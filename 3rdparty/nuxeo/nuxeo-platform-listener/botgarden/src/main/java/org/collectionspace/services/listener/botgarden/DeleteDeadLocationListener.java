package org.collectionspace.services.listener.botgarden;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.movement.nuxeo.MovementBotGardenConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/* 
 * Delete dead locations.
 */
public class DeleteDeadLocationListener extends AbstractCSEventSyncListenerImpl {
	private static final Log logger = LogFactory.getLog(DeleteDeadLocationListener.class);

	@Override
	public boolean shouldHandleEvent(Event event) {
		boolean result = false;

        EventContext ec = event.getContext();
        if (ec instanceof DocumentEventContext) {
            DocumentEventContext context = (DocumentEventContext) ec;
            DocumentModel doc = context.getSourceDocument();

            if (doc.getType().startsWith(MovementConstants.NUXEO_DOCTYPE) &&
            		!doc.isVersion() && 
            		!doc.isProxy() && 
            		!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            	result = true;
            }
        }

        return result;
    }

	@Override
	public void handleCSEvent(Event event) {
        DocumentEventContext context = (DocumentEventContext) event;
        DocumentModel doc = context.getSourceDocument();

    	String actionCode = (String) doc.getProperty(MovementBotGardenConstants.ACTION_CODE_SCHEMA_NAME, 
    			MovementBotGardenConstants.ACTION_CODE_FIELD_NAME);

    	if (actionCode != null && actionCode.contains(MovementBotGardenConstants.DEAD_ACTION_CODE)) {
    		CoreSession session = context.getCoreSession();
    		
    		if (session.getAllowedStateTransitions(doc.getRef()).contains(WorkflowClient.WORKFLOWTRANSITION_DELETE)) {
    			session.followTransition(doc.getRef(), WorkflowClient.WORKFLOWTRANSITION_DELETE);
    		}
    	}
    }

	@Override
	protected Log getLogger() {
		return logger;
	}
}