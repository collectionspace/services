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

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.MovementResource;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearLocationLabelRequestBatchJob implements BatchInvocable {
	private static List<String> invocationModes = Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST, INVOCATION_MODE_NO_CONTEXT);

	protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

	private ResourceMap resourceMap;
	private InvocationContext context;
	private int completionStatus;
	private InvocationResults results;	
	private InvocationError errorInfo;	

	final Logger logger = LoggerFactory.getLogger(ClearLocationLabelRequestBatchJob.class);

	public ClearLocationLabelRequestBatchJob() {
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
	
	public InvocationResults clearLabelRequests(String movementCsid) throws URISyntaxException  {
		return clearLabelRequests(Arrays.asList(movementCsid));
	}
	
	public InvocationResults clearLabelRequests(List<String> movementCsids) throws URISyntaxException  {
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
	
	private void clearLabelRequest(String movementCsid) {
		logger.debug("clear label request: movementCsid=" + movementCsid);

		final String updatePayload = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"movements\">" +
				"<ns2:movements_common xmlns:ns2=\"http://collectionspace.org/services/movement\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					"<reasonForMove>" + MovementConstants.OTHER_ACTION_CODE + "</reasonForMove>" +
				"</ns2:movements_common>" +
				"<ns2:movements_naturalhistory xmlns:ns2=\"http://collectionspace.org/services/movement/domain/naturalhistory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					"<labelRequested>" + MovementConstants.LABEL_REQUESTED_NO_VALUE + "</labelRequested>" +
					"<labelSize></labelSize>" +
					"<labelStandType></labelStandType>" +
					"<labelCount></labelCount>" +
				"</ns2:movements_naturalhistory>" +
			"</document>";
		
		logger.debug(updatePayload);
		
		ResourceBase resource = resourceMap.get(MovementClient.SERVICE_NAME);
		resource.update(resourceMap, movementCsid, updatePayload);
	}
	
	private List<String> findLabelRequests() throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		MovementResource movementResource = (MovementResource) resourceMap.get(MovementClient.SERVICE_NAME);
		AbstractCommonList movementList = movementResource.getList(createLabelRequestSearchUriInfo());

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
		return createKeywordSearchUriInfo(MovementConstants.LABEL_REQUESTED_SCHEMA_NAME, MovementConstants.LABEL_REQUESTED_FIELD_NAME, MovementConstants.LABEL_REQUESTED_YES_VALUE);		
	}
}