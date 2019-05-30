package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.MovementResource;
import org.collectionspace.services.movement.nuxeo.MovementBotGardenConstants;
import org.collectionspace.services.vocabulary.nuxeo.VocabularyConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearLocationLabelRequestBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(ClearLocationLabelRequestBatchJob.class);

	public ClearLocationLabelRequestBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST, INVOCATION_MODE_NO_CONTEXT));
	}

	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			/*
			 * For now, treat any mode as if it were no context.
			 */

			setResults(clearLabelRequests());
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}

	public InvocationResults clearLabelRequests() throws Exception  {
		List<String> movementCsids = findLabelRequests();
		InvocationResults results = null;

		if (movementCsids.size() > 0) {
			results = clearLabelRequests(movementCsids);
		}
		else {
			results = new InvocationResults();
			results.setUserNote("No label requests found");
		}

		return results;
	}

	public InvocationResults clearLabelRequests(String movementCsid) throws Exception  {
		return clearLabelRequests(Arrays.asList(movementCsid));
	}

	public InvocationResults clearLabelRequests(List<String> movementCsids) throws Exception  {
		InvocationResults results = new InvocationResults();
		long numAffected = 0;

		for (String movementCsid : movementCsids) {
			clearLabelRequest(movementCsid);
			numAffected = numAffected + 1;
		}

		results.setNumAffected(numAffected);
		results.setUserNote("Removed " + numAffected + " label " + (numAffected == 1 ? "request" : "requests"));

		return results;
	}

	private void clearLabelRequest(String movementCsid) throws Exception {
		logger.debug("clear label request: movementCsid=" + movementCsid);
		
		PoxPayloadOut payloadout = findAuthorityItemByRefName(VocabularyClient.SERVICE_NAME, MovementBotGardenConstants.OTHER_ACTION_CODE);
		String termDisplayName = getFieldValue(payloadout, VocabularyConstants.DISPLAY_NAME_SCHEMA_NAME, VocabularyConstants.DISPLAY_NAME_FIELD_NAME);		

		final String updatePayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"movements\">" +
				"<ns2:movements_common xmlns:ns2=\"http://collectionspace.org/services/movement\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml("reasonForMove", MovementBotGardenConstants.OTHER_ACTION_CODE + String.format("'%s'", termDisplayName)) +
				"</ns2:movements_common>" +
				"<ns2:movements_botgarden xmlns:ns2=\"http://collectionspace.org/services/movement/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml("labelRequested", MovementBotGardenConstants.LABEL_REQUESTED_NO_VALUE) +
					getFieldXml("labelSize", "") +
					getFieldXml("labelStandType", "") +
					getFieldXml("labelCount", "") +
				"</ns2:movements_botgarden>" +
			"</document>";

		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(MovementClient.SERVICE_NAME);
		resource.update(getServiceContext(), getResourceMap(), createUriInfo(), movementCsid, updatePayload);
	}

	private List<String> findLabelRequests() throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		MovementResource movementResource = (MovementResource) getResourceMap().get(MovementClient.SERVICE_NAME);
		AbstractCommonList movementList = movementResource.getList(getServiceContext(), createLabelRequestSearchUriInfo());

		for (AbstractCommonList.ListItem item : movementList.getListItem()) {
			for (org.w3c.dom.Element element : item.getAny()) {
				if (element.getTagName().equals("csid")) {
					csids.add(element.getTextContent());
					break;
				}
			}
		}

		return csids;
	}

	private UriInfo createLabelRequestSearchUriInfo() throws URISyntaxException {
		return createKeywordSearchUriInfo(MovementBotGardenConstants.LABEL_REQUESTED_SCHEMA_NAME, MovementBotGardenConstants.LABEL_REQUESTED_FIELD_NAME,
				MovementBotGardenConstants.LABEL_REQUESTED_YES_VALUE);
	}
}