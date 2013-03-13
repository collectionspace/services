package org.collectionspace.services.nuxeo.extension.botgarden;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.batch.nuxeo.UpdateDeadFlagBatchJob;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateDeadFlagListener implements EventListener {
	final Log logger = LogFactory.getLog(UpdateDeadFlagListener.class);

	/* 
	 * Set the dead flag and dead date on collectionobjects related to a new or modified movement record.
	 */
	public void handleEvent(Event event) throws ClientException {
		EventContext ec = event.getContext();

		if (ec instanceof DocumentEventContext) {
			DocumentEventContext context = (DocumentEventContext) ec;
			DocumentModel doc = context.getSourceDocument();

			logger.debug("docType=" + doc.getType());

			if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
				/*
				 * Handle the case where a new movement is created with action code revive, and then related
				 * to a collectionobject. The movement won't have any relations at the time it's created,
				 * so we need to capture the creation of the relation. 
				 */
				if (doc.getType().equals(RelationConstants.NUXEO_DOCTYPE) &&
						!doc.isVersion() && 
						!doc.isProxy()) {
					String subjectDocType = (String) doc.getProperty(RelationConstants.SUBJECT_DOCTYPE_SCHEMA_NAME, RelationConstants.SUBJECT_DOCTYPE_FIELD_NAME);
					String objectDocType = (String) doc.getProperty(RelationConstants.OBJECT_DOCTYPE_SCHEMA_NAME, RelationConstants.OBJECT_DOCTYPE_FIELD_NAME);;

					logger.debug("subjectDocType=" + subjectDocType + " objectDocType=" + objectDocType);

					if (subjectDocType.equals(MovementConstants.NUXEO_DOCTYPE) && objectDocType.equals(CollectionObjectConstants.NUXEO_DOCTYPE)) {
						String movementCsid = (String) doc.getProperty(RelationConstants.SUBJECT_CSID_SCHEMA_NAME, RelationConstants.SUBJECT_CSID_FIELD_NAME);
						String collectionObjectCsid = (String) doc.getProperty(RelationConstants.OBJECT_CSID_SCHEMA_NAME, RelationConstants.OBJECT_CSID_FIELD_NAME);

						try {
							InvocationResults results = createUpdater().updateDeadFlag(collectionObjectCsid, movementCsid);

							logger.debug("updateDeadFlag complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}            		
					}
				}
			}
			else {
				/*
				 * Handle document modification. If the modified document was a movement record, and 
				 * its action code is dead or revived, update the dead flag. We don't actually have to
				 * check the action code here, since it will be checked inside UpdateDeadFlagBatchJob.updateRelatedDeadFlags,
				 * but it is an optimization.
				 */
				if (doc.getType().startsWith(MovementConstants.NUXEO_DOCTYPE) &&
						!doc.isVersion() && 
						!doc.isProxy() && 
						!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
					String actionCode = (String) doc.getProperty(MovementConstants.ACTION_CODE_SCHEMA_NAME, MovementConstants.ACTION_CODE_FIELD_NAME);           	

					logger.debug("actionCode=" + actionCode);

					if (actionCode != null && (actionCode.equals(MovementConstants.DEAD_ACTION_CODE) || actionCode.equals(MovementConstants.REVIVED_ACTION_CODE))) {
						String movementCsid = doc.getName();

						try {
							InvocationResults results = createUpdater().updateRelatedDeadFlags(movementCsid);

							logger.debug("updateRelatedDeadFlags complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	private UpdateDeadFlagBatchJob createUpdater() {
		ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);

		UpdateDeadFlagBatchJob updater = new UpdateDeadFlagBatchJob();
		updater.setResourceMap(resourceMap);

		return updater;
	}
}