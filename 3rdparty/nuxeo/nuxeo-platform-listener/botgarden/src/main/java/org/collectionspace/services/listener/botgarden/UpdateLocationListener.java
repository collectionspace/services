package org.collectionspace.services.listener.botgarden;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.movement.nuxeo.MovementBotGardenConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventListenerImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateLocationListener extends AbstractCSEventListenerImpl {
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

				if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
					/*
					 * Special case for a document that is created with an action code of dead.
					 * In this case, we'll set the currentLocation to none, and the previousLocation to
					 * the current value of currentLocation, since there isn't a previous value. To do
					 * this, we can simply save the document, which will cause the beforeDocumentModification
					 * event to fire, taking us into the other branch of this code, with the current document
					 * becoming the previous document.
					 */
					if (actionCode != null && actionCode.equals(MovementBotGardenConstants.DEAD_ACTION_CODE)) {
						context.getCoreSession().saveDocument(doc);

						/*
						 *  The saveDocument call will have caused the document to be versioned via documentModified,
						 *  so we can skip the versioning that would normally happen on documentCreated.
						 */
						ec.setProperty(CreateVersionListener.SKIP_PROPERTY, true);
					}
				}
				else {
					if (actionCode != null && actionCode.equals(MovementBotGardenConstants.DEAD_ACTION_CODE)) {
						doc.setProperty(MovementConstants.CURRENT_LOCATION_SCHEMA_NAME, MovementConstants.CURRENT_LOCATION_FIELD_NAME, MovementConstants.NONE_LOCATION);
					}

					DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
					String previousLocation = (String) previousDoc.getProperty(MovementConstants.CURRENT_LOCATION_SCHEMA_NAME, MovementConstants.CURRENT_LOCATION_FIELD_NAME);

					logger.debug("previousLocation=" + previousLocation);

					doc.setProperty(MovementBotGardenConstants.PREVIOUS_LOCATION_SCHEMA_NAME, MovementBotGardenConstants.PREVIOUS_LOCATION_FIELD_NAME, previousLocation);
				}
			}
		}
	}
}