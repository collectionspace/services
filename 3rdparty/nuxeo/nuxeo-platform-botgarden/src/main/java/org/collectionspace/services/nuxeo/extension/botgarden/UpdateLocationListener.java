package org.collectionspace.services.nuxeo.extension.botgarden;

import static org.collectionspace.services.movement.nuxeo.MovementConstants.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateLocationListener implements EventListener {
	final Log logger = LogFactory.getLog(UpdateLocationListener.class);

	/* 
	 * Set the currentLocation and previousLocation fields in a Current Location record
	 * to appropriate values.
	 * 
	 * <ul>
	 * <li>If the plant is dead, set currentLocation to none</li>
	 * <li>Set the previousLocation field to the previous value of the currentLocation field</li>
	 * </ui>
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

				if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
					/*
					 * Special case for a document that is created with an action code of dead.
					 * In this case, we'll set the currentLocation to none, and the previousLocation to
					 * the current value of currentLocation, since there isn't a previous value. To do 
					 * this, we can simply save the document, which will cause the beforeDocumentModification
					 * event to fire, taking us into the other branch of this code, with the current document
					 * becoming the previous document.
					 */
					if (actionCode != null && actionCode.equals(DEAD_ACTION_CODE)) {
						context.getCoreSession().saveDocument(doc);

						/*
						 *  The saveDocument call will have caused the document to be versioned via documentModified,
						 *  so we can skip the versioning that would normally happen on documentCreated.
						 */
						ec.setProperty(CreateVersionListener.SKIP_PROPERTY, true);
					}
				}
				else {	            	
					if (actionCode != null && actionCode.equals(DEAD_ACTION_CODE)) {
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
}