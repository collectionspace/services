package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.RelationClient;

public class CreateAndLinkLoanOutBatchJob implements BatchInvocable {

	private static ArrayList<String> invocationModes = null;
	private InvocationContext context;
	private int completionStatus;
	private ResourceMap resourceMap;
	private InvocationResults results;
	private InvocationError errorInfo;
	private final String RELATION_TYPE = "affects"; 
	private final String LOAN_DOCTYPE = "LoanOut"; 
	private final String RELATION_PREDICATE_DISP = "affects"; 
	protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
	
	public CreateAndLinkLoanOutBatchJob() {
		CreateAndLinkLoanOutBatchJob.setupClassStatics();
		context = null;
		completionStatus = STATUS_UNSTARTED;
		resourceMap = null;
		results = new InvocationResults();
		errorInfo = null;
	}

	private static void setupClassStatics() {
		if(invocationModes == null ) {
			invocationModes = new ArrayList<String>(1);
			invocationModes.add(INVOCATION_MODE_SINGLE);
			invocationModes.add(INVOCATION_MODE_LIST);
		}
	}

	/**
	 * @return a set of modes that this plugin can support on invocation. Must be non-empty.
	 */
	public List<String> getSupportedInvocationModes() {
		return CreateAndLinkLoanOutBatchJob.invocationModes;
	}
	
	/**
	 * Sets the invocation context for the batch job. Called before run().
	 * @param context an instance of InvocationContext.
	 */
	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}

	/**
	 * Sets the invocation context for the batch job. Called before run().
	 * @param context an instance of InvocationContext.
	 */
	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}

	/**
	 * The main work logic of the batch job. Will be called after setContext.
	 */
	public void run() {
		completionStatus = STATUS_MIN_PROGRESS;

		try {
			// First, create the Loanout
			if(createLoan() != STATUS_ERROR) {
				if(INVOCATION_MODE_SINGLE.equalsIgnoreCase(context.getMode())) {
					if(createRelation(results.getPrimaryURICreated(), 
										context.getSingleCSID()) != STATUS_ERROR) {
						results.setNumAffected(1);
						results.setUserNote("CreateAndLinkLoanOutBatchJob created new Loanout: "
								+results.getPrimaryURICreated()+" with a link to the passed "+context.getDocType());
						completionStatus = STATUS_COMPLETE;
					}
				} else if(INVOCATION_MODE_LIST.equalsIgnoreCase(context.getMode())) {
					InvocationContext.ListCSIDs listWrapper = context.getListCSIDs();
					List<String> csids = listWrapper.getCsid();
					if(csids.size()==0) {
						completionStatus = STATUS_ERROR;
						errorInfo = new InvocationError(BAD_REQUEST_STATUS,
								"CreateAndLinkLoanOutBatchJob: no CSIDs in list of documents!");
						results.setUserNote(errorInfo.getMessage());
					}
					String loanCSID = results.getPrimaryURICreated();
					int nCreated = 0;
					for(String csid:csids) {
						if(createRelation(loanCSID, csid) == STATUS_ERROR) {
							break;
						} else {
							nCreated++;
						}
					}
					if(completionStatus!=STATUS_ERROR) {
						results.setNumAffected(nCreated);
						results.setUserNote("CreateAndLinkLoanOutBatchJob created new Loanout: "
								+results.getPrimaryURICreated()+" with "+nCreated+" link(s) to "+context.getDocType());
						completionStatus = STATUS_COMPLETE;
					}
				}
			}
		} catch(Exception e) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS,
					"CreateAndLinkLoanOutBatchJob problem creating new Loanout: "+e.getLocalizedMessage());
			results.setUserNote(errorInfo.getMessage());
		}
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
			errorInfo = new InvocationError(INT_ERROR_STATUS,
			"CreateAndLinkLoanOutBatchJob problem creating new relation!");
			results.setUserNote(errorInfo.getMessage());
		}
		return completionStatus;
	}

	/**
	 * @return one of the STATUS_* constants, or a value from 1-99 to indicate progress.
	 * Implementations need not support partial completion (progress) values, and can transition
	 * from STATUS_MIN_PROGRESS to STATUS_COMPLETE.
	 */
	public int getCompletionStatus() {
		return completionStatus;
	}

	/**
	 * @return information about the batch job actions and results
	 */
	public InvocationResults getResults() {
		if(completionStatus != STATUS_COMPLETE)
			return null;
		return results;
	}

	/**
	 * @return a user-presentable note when an error occurs in batch processing. Will only
	 * be called if getCompletionStatus() returns STATUS_ERROR.
	 */
	public InvocationError getErrorInfo() {
		return errorInfo;
	}


}
