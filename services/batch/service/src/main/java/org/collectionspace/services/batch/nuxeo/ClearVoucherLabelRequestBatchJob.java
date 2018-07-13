package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanout.LoanoutResource;
import org.collectionspace.services.loanout.nuxeo.LoanoutBotGardenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearVoucherLabelRequestBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(ClearVoucherLabelRequestBatchJob.class);

	public ClearVoucherLabelRequestBatchJob() {
		this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST, INVOCATION_MODE_NO_CONTEXT));
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
		List<String> loanoutCsids = findLabelRequests();
		InvocationResults results = null;

		if (loanoutCsids.size() > 0) {
			results = clearLabelRequests(loanoutCsids);
		}
		else {
			results = new InvocationResults();
			results.setUserNote("No label requests found");
		}

		return results;
	}

	public InvocationResults clearLabelRequests(String loanoutCsid) throws URISyntaxException  {
		return clearLabelRequests(Arrays.asList(loanoutCsid));
	}

	public InvocationResults clearLabelRequests(List<String> loanoutCsids) throws URISyntaxException  {
		InvocationResults results = new InvocationResults();
		long numAffected = 0;

		for (String loanoutCsid : loanoutCsids) {
			clearLabelRequest(loanoutCsid);
			numAffected = numAffected + 1;
		}

		results.setNumAffected(numAffected);
		results.setUserNote("Removed " + numAffected + " label " + (numAffected == 1 ? "request" : "requests"));

		return results;
	}

	private void clearLabelRequest(String loanoutCsid) throws URISyntaxException {
		logger.debug("clear label request: loanoutCsid=" + loanoutCsid);

		final String updatePayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"loansout\">" +
				"<ns2:loansout_botgarden xmlns:ns2=\"http://collectionspace.org/services/loanout/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml("labelRequested", LoanoutBotGardenConstants.LABEL_REQUESTED_NO_VALUE) +
				"</ns2:loansout_botgarden>" +
			"</document>";

		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(LoanoutClient.SERVICE_NAME);
		resource.update(getServiceContext(), getResourceMap(), createUriInfo(), loanoutCsid, updatePayload);
	}

	private List<String> findLabelRequests() throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		LoanoutResource loanoutResource = (LoanoutResource) getResourceMap().get(LoanoutClient.SERVICE_NAME);
		AbstractCommonList loanoutList = loanoutResource.getList(getServiceContext(), createLabelRequestSearchUriInfo());

		for (AbstractCommonList.ListItem item : loanoutList.getListItem()) {
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
		return createKeywordSearchUriInfo(LoanoutBotGardenConstants.LABEL_REQUESTED_SCHEMA_NAME, LoanoutBotGardenConstants.LABEL_REQUESTED_FIELD_NAME,
				LoanoutBotGardenConstants.LABEL_REQUESTED_YES_VALUE);
	}
}