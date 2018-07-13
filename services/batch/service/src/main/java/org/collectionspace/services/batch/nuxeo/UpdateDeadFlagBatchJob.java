package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectBotGardenConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.movement.nuxeo.MovementBotGardenConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDeadFlagBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(UpdateDeadFlagBatchJob.class);

	public UpdateDeadFlagBatchJob() {
		this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE));
	}

	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			String mode = getInvocationContext().getMode();

			if (!mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				throw new Exception("Unsupported invocation mode: " + mode);
			}

			String movementCsid = getInvocationContext().getSingleCSID();

			if (StringUtils.isEmpty(movementCsid)) {
				throw new Exception("Missing context csid");
			}

			setResults(updateRelatedDeadFlags(movementCsid));
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}

	/**
	 * Update the dead flag for all collectionobjects related to the given movement record,
	 * based on the assumption that the action code of the specified movement record has just changed.
	 *
	 * @param movementCsid	the csid of the movement that was updated
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateRelatedDeadFlags(String movementCsid) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		long numAffected = 0;
		List<String> userNotes = new ArrayList<String>();

		PoxPayloadOut payload = findMovementByCsid(movementCsid);

		String actionCode = getFieldValue(payload, MovementBotGardenConstants.ACTION_CODE_SCHEMA_NAME, MovementBotGardenConstants.ACTION_CODE_FIELD_NAME);
		logger.debug("actionCode=" + actionCode);

		if (actionCode.equals(MovementBotGardenConstants.DEAD_ACTION_CODE) || actionCode.equals(MovementBotGardenConstants.REVIVED_ACTION_CODE)) {
			String actionDate = getFieldValue(payload, MovementBotGardenConstants.ACTION_DATE_SCHEMA_NAME,
					MovementBotGardenConstants.ACTION_DATE_FIELD_NAME);
			logger.debug("actionDate=" + actionDate);

			List<String> collectionObjectCsids = findRelatedCollectionObjects(movementCsid);

			for (String collectionObjectCsid : collectionObjectCsids) {
				logger.debug("found related collectionobject: " + collectionObjectCsid);

				InvocationResults collectionObjectResults = updateDeadFlag(collectionObjectCsid, movementCsid, actionCode, actionDate);

				if (collectionObjectResults.getNumAffected() > 0) {
					numAffected = numAffected + collectionObjectResults.getNumAffected();
					userNotes.add(collectionObjectResults.getUserNote());
				}
			}
		}

		if (numAffected > 0) {
			results.setNumAffected(numAffected);
			results.setUserNote(StringUtils.join(userNotes, ", "));
		}

		return results;
	}

	/**
	 * Update the dead flag for the given collectionobject, based on the assumption that the action code
	 * of the specified movement record has just changed, and that the movement record is related to
	 * the collectionobject.
	 *
	 * @param collectionObjectCsid	the csid of the collectionobject to update
	 * @param updatedMovementCsid	the csid of the related movement that was updated
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateDeadFlag(String collectionObjectCsid, String updatedMovementCsid) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		PoxPayloadOut payload = findMovementByCsid(updatedMovementCsid);

		String actionCode = getFieldValue(payload, MovementBotGardenConstants.ACTION_CODE_SCHEMA_NAME,
				MovementBotGardenConstants.ACTION_CODE_FIELD_NAME);
		logger.debug("actionCode=" + actionCode);

		if (actionCode.equals(MovementBotGardenConstants.DEAD_ACTION_CODE) || actionCode.equals(MovementBotGardenConstants.REVIVED_ACTION_CODE)) {
			String actionDate = getFieldValue(payload, MovementBotGardenConstants.ACTION_DATE_SCHEMA_NAME,
					MovementBotGardenConstants.ACTION_DATE_FIELD_NAME);
			logger.debug("actionDate=" + actionDate);

			results = updateDeadFlag(collectionObjectCsid, updatedMovementCsid, actionCode, actionDate);
		}

		return results;
	}

	/**
	 * Update the dead flag for the given collectionobject, based on the assumption that the action code
	 * of the specified movement record has just changed, and that the movement record is related to
	 * the collectionobject.
	 *
	 * @param collectionObjectCsid	the csid of the collectionobject to update
	 * @param updatedMovementCsid	the csid of the related movement that was updated
	 * @param actionCode			the action code of the movement
	 * @param actionDate			the action date of the movement
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	private InvocationResults updateDeadFlag(String collectionObjectCsid, String updatedMovementCsid, String actionCode, String actionDate) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		PoxPayloadOut payload = findCollectionObjectByCsid(collectionObjectCsid);

		String workflowState = getFieldValue(payload, CollectionObjectConstants.WORKFLOW_STATE_SCHEMA_NAME, CollectionObjectConstants.WORKFLOW_STATE_FIELD_NAME);

		if (workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			logger.debug("skipping deleted collectionobject: " + collectionObjectCsid);
		}
		else {
			String deadFlag = getFieldValue(payload, CollectionObjectBotGardenConstants.DEAD_FLAG_SCHEMA_NAME,
					CollectionObjectBotGardenConstants.DEAD_FLAG_FIELD_NAME);
			boolean isDead = (deadFlag != null) && (deadFlag.equalsIgnoreCase("true"));

			logger.debug("updating dead flag: collectionObjectCsid=" + collectionObjectCsid + " actionCode=" + actionCode + " isDead=" + isDead);

			if (actionCode.equals(MovementBotGardenConstants.REVIVED_ACTION_CODE)) {
				if (isDead) {
					/*
					 * The object is dead, but a location was revived. Unset the dead flag and date on the object.
					 */
					setDeadFlag(collectionObjectCsid, false, null);

					results.setNumAffected(1);
					results.setUserNote(collectionObjectCsid + " set to alive");
				}
			}
			else if (actionCode.equals(MovementBotGardenConstants.DEAD_ACTION_CODE)) {
				if (!isDead) {
					/*
					 * The object is not dead, but a location was marked dead. If there are no remaining live locations,
					 * set the dead flag and date on the object. Any movement record that is not deleted represents
					 * a live location, with one exception: the movement record that was just marked dead may not have
					 * been deleted yet, but it should not count as a live location.
					 */
					List<String> movementCsids = findRelatedMovements(collectionObjectCsid);
					boolean liveLocationExists = false;

					for (String movementCsid : movementCsids) {
						logger.debug("found related movement: movementCsid=" + movementCsid);

						if (!movementCsid.equals(updatedMovementCsid)) {
							PoxPayloadOut movementPayload = findMovementByCsid(movementCsid);
							String movementWorkflowState = getFieldValue(movementPayload, MovementConstants.WORKFLOW_STATE_SCHEMA_NAME, MovementConstants.WORKFLOW_STATE_FIELD_NAME);

							if (!movementWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
								logger.debug("found live location: movementCsid=" + movementCsid);

								liveLocationExists = true;
								break;
							}
						}
					}

					if (!liveLocationExists) {
						setDeadFlag(collectionObjectCsid, true, actionDate);

						results.setNumAffected(1);
						results.setUserNote(collectionObjectCsid + " set to dead");
					}
				}
			}
		}

		return results;
	}

	/**
	 * Update the dead flag and dead date of the specified collectionobject.
	 *
	 * @param collectionObjectCsid	the csid of the collectionobject to update
	 * @param deadFlag				the new value of the dead flag field
	 * @param deadDate				the new value of the dead date field
	 * @throws URISyntaxException
	 */
	private void setDeadFlag(String collectionObjectCsid, boolean deadFlag, String deadDate) throws URISyntaxException {
		String updatePayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"collectionobjects\">" +
				"<ns2:collectionobjects_botgarden xmlns:ns2=\"http://collectionspace.org/services/collectionobject/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml("deadFlag", (deadFlag ? "true" : "false")) +
					getFieldXml("deadDate", deadDate) +
				"</ns2:collectionobjects_botgarden>" +
				"<ns2:collectionobjects_common xmlns:ns2=\"http://collectionspace.org/services/collectionobject\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
				"</ns2:collectionobjects_common>" +
			"</document>";

		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(CollectionObjectClient.SERVICE_NAME);
		resource.update(getServiceContext(), getResourceMap(), createUriInfo(), collectionObjectCsid, updatePayload);
	}
}
