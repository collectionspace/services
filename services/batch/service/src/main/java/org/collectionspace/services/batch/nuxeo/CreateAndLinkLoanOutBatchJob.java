package org.collectionspace.services.batch.nuxeo;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.RelationClient;

public class CreateAndLinkLoanOutBatchJob extends AbstractBatchInvocable {

	private final String RELATION_TYPE = "affects"; 
	private final String LOAN_DOCTYPE = "LoanOut"; 
	private final String RELATION_PREDICATE_DISP = "affects"; 
	
	public CreateAndLinkLoanOutBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
	}
	
	/**
	 * The main work logic of the batch job. Will be called after setContext.
	 */
	@Override
	public void run() {
		completionStatus = STATUS_MIN_PROGRESS;

		try {
			// First, create the Loanout
			if (createLoan() != STATUS_ERROR) {
				if(INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationCtx.getMode())) {
					if(createRelation(results.getPrimaryURICreated(), 
										invocationCtx.getSingleCSID()) != STATUS_ERROR) {
						results.setNumAffected(1);
						results.setUserNote("CreateAndLinkLoanOutBatchJob created new Loanout: "
								+results.getPrimaryURICreated()+" with a link to the passed "+invocationCtx.getDocType());
						completionStatus = STATUS_COMPLETE;
					}
				} else if(INVOCATION_MODE_LIST.equalsIgnoreCase(invocationCtx.getMode())) {
					InvocationContext.ListCSIDs listWrapper = invocationCtx.getListCSIDs();
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
								+results.getPrimaryURICreated()+" with "+nCreated+" link(s) to "+invocationCtx.getDocType());
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
		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get( LoanoutClient.SERVICE_NAME); 
		Response response = resource.create(getResourceMap(), null, loanoutPayload);
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
			+   "<objectDocumentType>"+invocationCtx.getDocType()+"</objectDocumentType>"
			+   "<relationshipType>"+RELATION_TYPE+"</relationshipType>"
			+   "<predicateDisplayName>"+RELATION_PREDICATE_DISP+"</predicateDisplayName>"
			+ "</ns2:relations_common></document>";
		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(RelationClient.SERVICE_NAME);
		Response response = resource.create(getResourceMap(), null, relationPayload);
		if(response.getStatus() != CREATED_STATUS) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS,
			"CreateAndLinkLoanOutBatchJob problem creating new relation!");
			results.setUserNote(errorInfo.getMessage());
		}
		return completionStatus;
	}
}
