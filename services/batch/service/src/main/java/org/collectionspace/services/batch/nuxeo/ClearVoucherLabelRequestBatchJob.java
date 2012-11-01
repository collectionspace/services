package org.collectionspace.services.batch.nuxeo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanout.LoanoutResource;
import org.collectionspace.services.loanout.nuxeo.LoanoutConstants;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearVoucherLabelRequestBatchJob implements BatchInvocable {
	private static List<String> invocationModes = Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST, INVOCATION_MODE_NO_CONTEXT);

	protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

	private ResourceMap resourceMap;
	private InvocationContext context;
	private int completionStatus;
	private InvocationResults results;	
	private InvocationError errorInfo;	

	final Logger logger = LoggerFactory.getLogger(ClearVoucherLabelRequestBatchJob.class);

	public ClearVoucherLabelRequestBatchJob() {
		this.completionStatus = STATUS_UNSTARTED;
		this.results = new InvocationResults();
	}

	public List<String> getSupportedInvocationModes() {
		return invocationModes;
	}

	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}

	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}

	public int getCompletionStatus() {
		return completionStatus;
	}

	public InvocationResults getResults() {
		return (STATUS_COMPLETE == completionStatus) ? results : null;
	}

	public InvocationError getErrorInfo() {
		return errorInfo;
	}
	
	public void run() {
		completionStatus = STATUS_MIN_PROGRESS;
		
		try {
			/*
			 * For now, treat any mode as if it were no context.
			 */
			
			results = clearLabelRequests();
			completionStatus = STATUS_COMPLETE;
		}
		catch(Exception e) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS, e.getMessage());
			results.setUserNote(e.getLocalizedMessage());
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
	
	private void clearLabelRequest(String loanoutCsid) {
		logger.debug("clear label request: loanoutCsid=" + loanoutCsid);

		final String updatePayload = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"loansout\">" +
				"<ns2:loansout_botgarden xmlns:ns2=\"http://collectionspace.org/services/loanout/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					"<labelRequested>" + StringEscapeUtils.escapeXml(LoanoutConstants.LABEL_REQUESTED_NO_VALUE) + "</labelRequested>" +
				"</ns2:loansout_botgarden>" +
			"</document>";
				
		ResourceBase resource = resourceMap.get(LoanoutClient.SERVICE_NAME);
		resource.update(resourceMap, loanoutCsid, updatePayload);
	}
	
	private List<String> findLabelRequests() throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		LoanoutResource loanoutResource = (LoanoutResource) resourceMap.get(LoanoutClient.SERVICE_NAME);
		AbstractCommonList loanoutList = loanoutResource.getList(createLabelRequestSearchUriInfo());

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

	private UriInfo createUriInfo(String queryString) throws URISyntaxException {
		URI	absolutePath = new URI("");
		URI	baseUri = new URI("");

		return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment> emptyList());
	}
	
	private UriInfo createKeywordSearchUriInfo(String schemaName, String fieldName, String value) throws URISyntaxException {
		String queryString = "kw=&as=( (" +schemaName + ":" + fieldName + " ILIKE \"" + value + "\") )&wf_deleted=false";
		URI uri =  new URI(null, null, null, queryString, null);

		return createUriInfo(uri.getRawQuery());		
	}

	private UriInfo createLabelRequestSearchUriInfo() throws URISyntaxException {
		return createKeywordSearchUriInfo(LoanoutConstants.LABEL_REQUESTED_SCHEMA_NAME, LoanoutConstants.LABEL_REQUESTED_FIELD_NAME, LoanoutConstants.LABEL_REQUESTED_YES_VALUE);		
	}
}