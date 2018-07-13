package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.PottagClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.pottag.PottagResource;
import org.collectionspace.services.pottag.nuxeo.PottagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearPotTagLabelRequestBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(ClearPotTagLabelRequestBatchJob.class);

	public ClearPotTagLabelRequestBatchJob() {
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

	public InvocationResults clearLabelRequests() throws URISyntaxException  {
		List<String> potTagCsids = findLabelRequests();
		InvocationResults results = null;

		if (potTagCsids.size() > 0) {
			results = clearLabelRequests(potTagCsids);
		}
		else {
			results = new InvocationResults();
			results.setUserNote("No label requests found");
		}

		return results;
	}

	public InvocationResults clearLabelRequests(String potTagCsid) throws URISyntaxException  {
		return clearLabelRequests(Arrays.asList(potTagCsid));
	}

	public InvocationResults clearLabelRequests(List<String> potTagCsids) throws URISyntaxException  {
		InvocationResults results = new InvocationResults();
		long numAffected = 0;

		for (String potTagCsid : potTagCsids) {
			clearLabelRequest(potTagCsid);
			numAffected = numAffected + 1;
		}

		results.setNumAffected(numAffected);
		results.setUserNote("Removed " + numAffected + " label " + (numAffected == 1 ? "request" : "requests"));

		return results;
	}

	private void clearLabelRequest(String potTagCsid) throws URISyntaxException {
		logger.debug("clear label request: potTagCsid=" + potTagCsid);

		final String updatePayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"pottags\">" +
				"<ns2:pottags_common xmlns:ns2=\"http://collectionspace.org/services/pottag\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml(PottagConstants.LABEL_REQUESTED_FIELD_NAME, PottagConstants.LABEL_REQUESTED_NO_VALUE) +
				"</ns2:pottags_common>" +
			"</document>";

		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(PottagClient.SERVICE_NAME);
		resource.update(getServiceContext(), getResourceMap(), createUriInfo(), potTagCsid, updatePayload);
	}

	private List<String> findLabelRequests() throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		PottagResource potTagResource = (PottagResource) getResourceMap().get(PottagClient.SERVICE_NAME);
		AbstractCommonList potTagList = potTagResource.getList(getServiceContext(), createLabelRequestSearchUriInfo());

		for (AbstractCommonList.ListItem item : potTagList.getListItem()) {
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
		return createKeywordSearchUriInfo(PottagConstants.LABEL_REQUESTED_SCHEMA_NAME, PottagConstants.LABEL_REQUESTED_FIELD_NAME, PottagConstants.LABEL_REQUESTED_YES_VALUE);
	}
}