package org.collectionspace.services.listener.botgarden;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class CreateVersionListener extends AbstractCSEventSyncListenerImpl {
	public static final String SKIP_PROPERTY = "CreateVersionListener.SKIP";

	private static final Log logger = LogFactory.getLog(CreateVersionListener.class);

	@Override
	public boolean shouldHandleEvent(Event event) {
		boolean result = true;
		EventContext ec = event.getContext();
		
		if (ec.hasProperty(SKIP_PROPERTY) && ((Boolean) ec.getProperty(SKIP_PROPERTY))) {
			result = false;
		}

		return result;
	}

	@Override
	public void handleCSEvent(Event event) {
		DocumentEventContext context = (DocumentEventContext) event.getContext();;
		DocumentModel doc = context.getSourceDocument();

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

	@Override
	protected Log getLogger() {
		return logger;
	}
}