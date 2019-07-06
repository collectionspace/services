package org.collectionspace.services.listener.botgarden;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventListenerImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateVersionListener extends AbstractCSEventListenerImpl {
	public static final String SKIP_PROPERTY = "CreateVersionListener.SKIP";

	final Logger logger = LoggerFactory.getLogger(CreateVersionListener.class);

	@Override
	public void handleEvent(Event event) {
		EventContext ec = event.getContext();

		if (isRegistered(event) && ec instanceof DocumentEventContext) {
			DocumentEventContext context = (DocumentEventContext) ec;

			if (ec.hasProperty(SKIP_PROPERTY) && ((Boolean) ec.getProperty(SKIP_PROPERTY))) {
				logger.debug("Skipping create version");
			}
			else {
				DocumentModel doc = context.getSourceDocument();

				logger.debug("docType=" + doc.getType());

				if (doc.getType().startsWith(MovementConstants.NUXEO_DOCTYPE) &&
						!doc.isVersion() &&
						!doc.isProxy() &&
						!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
					// Version the document
					DocumentRef versionRef = doc.checkIn(VersioningOption.MINOR, null);
					DocumentModel versionDoc = context.getCoreSession().getDocument(versionRef);

					logger.debug("created version: id=" + versionDoc.getId() + " csid=" + versionDoc.getName());

					// Check out the document, so it can be modified
					doc.checkOut();
				}
			}
		}
	}
}
