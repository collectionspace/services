package org.collectionspace.services.nuxeo.extension.botgarden;

import static org.collectionspace.services.movement.nuxeo.MovementConstants.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class DeleteDeadLocationListener implements EventListener {
	final Log logger = LogFactory.getLog(DeleteDeadLocationListener.class);

    /* 
     * Delete dead locations. 
     */
    public void handleEvent(Event event) throws ClientException {
        EventContext ec = event.getContext();

        if (ec instanceof DocumentEventContext) {
            DocumentEventContext context = (DocumentEventContext) ec;
            DocumentModel doc = context.getSourceDocument();

            if (doc.getType().startsWith(NUXEO_DOCTYPE) &&
            		!doc.isVersion() && 
            		!doc.isProxy() && 
            		!doc.getCurrentLifeCycleState().equals(DELETED_STATE)) {
            	String actionCode = (String) doc.getProperty(ACTION_CODE_SCHEMA_NAME, ACTION_CODE_FIELD_NAME);
            	
            	logger.debug("actionCode=" + actionCode);
            	
            	if (actionCode.equals(DEAD_ACTION_CODE)) {
            		CoreSession session = context.getCoreSession();
            		
            		if (session.getAllowedStateTransitions(doc.getRef()).contains(DELETE_TRANSITION)) {
            			session.followTransition(doc.getRef(), DELETE_TRANSITION);
            		}
            	}
            }
        }
    }
}