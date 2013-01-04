package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob implements BatchInvocable {

    final Logger logger = LoggerFactory.getLogger(UpdateObjectLocationBatchJob.class);
    private static ArrayList<String> invocationModes = null;
    private InvocationContext context;
    private int completionStatus;
    private ResourceMap resourceMap;
    private InvocationResults results;
    private InvocationError errorInfo;
    protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
    protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
    protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

    public UpdateObjectLocationBatchJob() {
        UpdateObjectLocationBatchJob.setupClassStatics();
        context = null;
        completionStatus = STATUS_UNSTARTED;
        resourceMap = null;
        results = new InvocationResults();
        errorInfo = null;
    }

    private static void setupClassStatics() {
        if (invocationModes == null) {
            invocationModes = new ArrayList<String>(1);
            invocationModes.add(INVOCATION_MODE_SINGLE);
            // invocationModes.add(INVOCATION_MODE_LIST);
        }
    }

    /**
     * @return a set of modes that this plugin can support on invocation. Must
     * be non-empty.
     */
    public List<String> getSupportedInvocationModes() {
        return UpdateObjectLocationBatchJob.invocationModes;
    }

    /**
     * Sets the invocation context for the batch job. Called before run().
     *
     * @param context an instance of InvocationContext.
     */
    public void setInvocationContext(InvocationContext context) {
        this.context = context;
    }

    /**
     * Sets the invocation context for the batch job. Called before run().
     *
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
            // FIXME: Placeholder during early development
            if (logger.isInfoEnabled()) {
		 logger.info("Invoking " + this.getClass().getSimpleName() + " ...");
            }
        } catch (Exception e) {
            completionStatus = STATUS_ERROR;
            errorInfo = new InvocationError(INT_ERROR_STATUS,
                    "UpdateObjectLocationBatchJob problem: " + e.getLocalizedMessage());
            results.setUserNote(errorInfo.getMessage());
        }
    }

    /**
     * @return one of the STATUS_* constants, or a value from 1-99 to indicate
     * progress. Implementations need not support partial completion (progress)
     * values, and can transition from STATUS_MIN_PROGRESS to STATUS_COMPLETE.
     */
    public int getCompletionStatus() {
        return completionStatus;
    }

    /**
     * @return information about the batch job actions and results
     */
    public InvocationResults getResults() {
        if (completionStatus != STATUS_COMPLETE) {
            return null;
        }
        return results;
    }

    /**
     * @return a user-presentable note when an error occurs in batch processing.
     * Will only be called if getCompletionStatus() returns STATUS_ERROR.
     */
    public InvocationError getErrorInfo() {
        return errorInfo;
    }
}
