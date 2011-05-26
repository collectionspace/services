package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;

public class CreateAndLinkLoanOutBatchJob implements BatchInvocable {

	private static ArrayList<String> invocationModes = null;
	private InvocationContext context;
	private int completionStatus;
	private HashMap<String,ResourceBase> resourceMap;
	private InvocationResults results;
	private String errorInfo;
	
	public CreateAndLinkLoanOutBatchJob() {
		CreateAndLinkLoanOutBatchJob.setupClassStatics();
		context = null;
		completionStatus = STATUS_UNSTARTED;
		resourceMap = null;
		results = new InvocationResults();
		errorInfo = "";
	}

	private static void setupClassStatics() {
		if(invocationModes == null ) {
			invocationModes = new ArrayList<String>(1);
			invocationModes.add(INVOCATION_MODE_SINGLE);
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
	}

	/**
	 * The main work logic of the batch job. Will be called after setContext.
	 */
	public void run() {
		completionStatus = STATUS_UNSTARTED;
		try {
			Thread.sleep(1000);
		} catch(Exception e) {}
		results.setPrimaryURICreated(null);
		results.setNumAffected(0);
		results.setUserNote("CreateAndLinkLoanOutBatchJob pretended to do work, and completed");
		completionStatus = STATUS_COMPLETE;
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
	public String getErrorInfo() {
		return errorInfo;
	}


}
