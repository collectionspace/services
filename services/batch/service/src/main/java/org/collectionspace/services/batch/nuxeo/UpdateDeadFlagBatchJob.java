package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;

public class UpdateDeadFlagBatchJob implements BatchInvocable {
	private static List<String> invocationModes = Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST);

	private final String RELATION_TYPE = "affects"; 
	private final String LOAN_DOCTYPE = "LoanOut"; 
	private final String RELATION_PREDICATE_DISP = "affects"; 
	protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

	private ResourceMap resourceMap;
	private InvocationContext context;
	private int completionStatus;
	private InvocationResults results;	
	private InvocationError errorInfo;	
	
	public UpdateDeadFlagBatchJob() {
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
			String mode = context.getMode();
			List<String> csids = Collections.emptyList();
			
			if (mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				csids = Arrays.asList(context.getSingleCSID());
			}
			else if (mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				csids = context.getListCSIDs().getCsid();
			}
			
			for (String csid : csids) {
				InvocationResults results = updateDeadFlag(csid);
			}
			
			completionStatus = STATUS_COMPLETE;
			
			results.setNumAffected(csids.size());
			results.setUserNote("");
		}
		catch(Exception e) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS, e.getMessage());
			results.setUserNote(e.getLocalizedMessage());
		}
	}
	
	public InvocationResults updateDeadFlag(String collectionObjectCsid) {
		InvocationResults results = new InvocationResults();
		List<String> movementCsids = findRelatedMovements(collectionObjectCsid);
		List<String> deadDates = new ArrayList<String>();
		boolean isDead = true;
		
		for (String movementCsid : movementCsids) {
			String deadDate = getDeadDate(movementCsid);
			
			if (deadDate == null) {
				isDead = false;
				break;
			}
			
			deadDates.add(deadDate);
		}
		
		String deadDate = null;
		
		if (isDead) {
			//sort dead dates, get the latest
		}
		
		setDeadFlag(collectionObjectCsid, isDead, deadDate);
		
		return results;
	}
	
	public InvocationResults setDeadFlag(String collectionObjectCsid, boolean isDead, String deadDate) {
		return null;
	}
	
	private List<String> findRelatedMovements(String collectionObjectCsid) {
		return Collections.emptyList();
	}
	
	private String getDeadDate(String movementCsid) {
		return null;
	}
	
	private int createLoan() {
		String newLoanNumber = "NewLoan-"+ GregorianCalendarDateTimeUtils.timestampUTC();

		String loanoutPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<document name=\"loansout\">"
			  +"<ns2:loansout_common xmlns:ns2=\"http://collectionspace.org/services/loanout\""
			  		+" xmlns:ns3=\"http://collectionspace.org/services/jaxb\">"
		    +"<loanOutNumber>"+newLoanNumber+"</loanOutNumber>"
		  +"</ns2:loansout_common></document>";

		// First, create the Loanout
		// We fetch the resource class by service name
		ResourceBase resource = resourceMap.get( LoanoutClient.SERVICE_NAME); 
		Response response = resource.create(resourceMap, null, loanoutPayload);
		if(response.getStatus() != CREATED_STATUS) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS,
					"CreateAndLinkLoanOutBatchJob problem creating new Loanout!");
			results.setUserNote(errorInfo.getMessage());
		} else {
			String newId = CollectionSpaceClientUtils.extractId(response);
			results.setPrimaryURICreated(newId);
		}
		return completionStatus;
	}
	
	private int createRelation(String loanCSID, String toCSID) {
		// Now, create the relation that links the input object to the loanout
		String relationPayload = "<document name=\"relations\">"
			+ "<ns2:relations_common xmlns:ns2=\"http://collectionspace.org/services/relation\"" 
			+ 		" xmlns:ns3=\"http://collectionspace.org/services/jaxb\">"
			+   "<subjectCsid>"+loanCSID+"</subjectCsid>"
			+   "<subjectDocumentType>"+LOAN_DOCTYPE+"</subjectDocumentType>"
			+   "<objectCsid>"+toCSID+"</objectCsid>"
			+   "<objectDocumentType>"+context.getDocType()+"</objectDocumentType>"
			+   "<relationshipType>"+RELATION_TYPE+"</relationshipType>"
			+   "<predicateDisplayName>"+RELATION_PREDICATE_DISP+"</predicateDisplayName>"
			+ "</ns2:relations_common></document>";
		ResourceBase resource = resourceMap.get(RelationClient.SERVICE_NAME);
		Response response = resource.create(resourceMap, null, relationPayload);
		if(response.getStatus() != CREATED_STATUS) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS, "CreateAndLinkLoanOutBatchJob problem creating new relation!");
			results.setUserNote(errorInfo.getMessage());
		}
		return completionStatus;
	}
}
