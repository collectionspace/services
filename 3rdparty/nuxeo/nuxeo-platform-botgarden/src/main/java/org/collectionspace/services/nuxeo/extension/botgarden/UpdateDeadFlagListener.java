package org.collectionspace.services.nuxeo.extension.botgarden;

import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.ACTION_CODE_FIELD_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.ACTION_CODE_SCHEMA_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.DEAD_ACTION_CODE;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.REVIVED_ACTION_CODE;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.MOVEMENT_DOCTYPE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateDeadFlagListener implements EventListener {
	final Log logger = LogFactory.getLog(UpdateDeadFlagListener.class);

    /* 
     * Set the dead flag and dead date on collectionobjects related to a new or modified movement record.
     *  - If the plant is revived, set the dead flag on the collectionobject to alive, and dead date to null
     *  - If the plant is dead, if there are no other live locations, set the dead flag and dead date on the collectionobject
     */
    public void handleEvent(Event event) throws ClientException {
        EventContext ec = event.getContext();

        if (ec instanceof DocumentEventContext) {
            DocumentEventContext context = (DocumentEventContext) ec;
            DocumentModel doc = context.getSourceDocument();

            if (doc.getType().startsWith(MOVEMENT_DOCTYPE)) {
            	String actionCode = (String) doc.getProperty(ACTION_CODE_SCHEMA_NAME, ACTION_CODE_FIELD_NAME);
            	
            	logger.debug("actionCode=" + actionCode);
            	
            	if (actionCode.equals(DEAD_ACTION_CODE) || actionCode.equals(REVIVED_ACTION_CODE)) {
            		
            	}
            }
        }
    }
}