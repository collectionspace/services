package org.collectionspace.services.nuxeo.extension.botgarden;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class CreateVersionListener implements EventListener {
	public static final String SKIP_PROPERTY = "CreateVersionListener.SKIP";

	final Log logger = LogFactory.getLog(CreateVersionListener.class);

	public void handleEvent(Event event) throws ClientException {
		EventContext ec = event.getContext();

		if (ec instanceof DocumentEventContext) {
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
						!doc.getCurrentLifeCycleState().equals(MovementConstants.DELETED_STATE)) {
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