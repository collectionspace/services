package org.collectionspace.services.nuxeo.extension.botgarden;

import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.ACTION_CODE_FIELD_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.ACTION_CODE_SCHEMA_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.CURRENT_LOCATION_FIELD_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.CURRENT_LOCATION_SCHEMA_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.DEAD_ACTION_CODE;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.MOVEMENT_DOCTYPE;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.NONE_LOCATION;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.PREVIOUS_LOCATION_FIELD_NAME;
import static org.collectionspace.services.nuxeo.extension.botgarden.BotGardenConstants.PREVIOUS_LOCATION_SCHEMA_NAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateLocationListener implements EventListener {
	final Log logger = LogFactory.getLog(UpdateLocationListener.class);
	 
    /* 
     * Set the currentLocation and previousLocation fields in a Current Location record
     * to appropriate values:
     * 
     *  - If the plant is dead, set currentLocation to none
     *  - Set the previousLocation field to the previous value of the currentLocation field
     */
    public void handleEvent(Event event) throws ClientException {
        EventContext ec = event.getContext();

        if (ec instanceof DocumentEventContext) {
            DocumentEventContext context = (DocumentEventContext) ec;
            DocumentModel doc = context.getSourceDocument();

            if (doc.getType().startsWith(MOVEMENT_DOCTYPE)) {
            	String actionCode = (String) doc.getProperty(ACTION_CODE_SCHEMA_NAME, ACTION_CODE_FIELD_NAME);
            	
            	logger.debug("actionCode=" + actionCode);
            	
            	if (actionCode.equals(DEAD_ACTION_CODE)) {
             		doc.setProperty(CURRENT_LOCATION_SCHEMA_NAME, CURRENT_LOCATION_FIELD_NAME, NONE_LOCATION);
            	}
            	            	
            	DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);	            	
        		String previousLocation = (String) previousDoc.getProperty(CURRENT_LOCATION_SCHEMA_NAME, CURRENT_LOCATION_FIELD_NAME);
        		
        		logger.debug("previousLocation=" + previousLocation);
        		
        		doc.setProperty(PREVIOUS_LOCATION_SCHEMA_NAME, PREVIOUS_LOCATION_FIELD_NAME, previousLocation);
            }
        }
    }
}