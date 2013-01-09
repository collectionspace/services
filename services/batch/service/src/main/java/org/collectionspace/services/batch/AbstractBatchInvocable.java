package org.collectionspace.services.batch;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class was originally created by Ray Lee as
 * org.collectionspace.services.batch.nuxeo.AbstractBatchJob for his work on the
 * UC Berkeley Botanical Garden version 2.4 implementation of CollectionSpace:
 *
 * https://github.com/cspace-deployment/services/blob/botgarden_2.4/services/batch/service/src/main/java/org/collectionspace/services/batch/nuxeo/AbstractBatchJob.java
 *
 * Ray's original implementation included other convenience methods that are not
 * directly pertinent to the batch service but would be widely useful to batch
 * job creators. It it suggested that some or all of those methods might be
 * implemented within the CollectionSpace services framework, if they do not
 * already exist.
 *
 * - ADR 2013-01-04
 */
public abstract class AbstractBatchInvocable implements BatchInvocable {

    public final int OK_STATUS = Response.Status.OK.getStatusCode();
    public final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
    public final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
    public final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    protected final String CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT =
            "Could not find required CSID values in the invocation context for this batch job.";
    private List<String> invocationModes;
    private ResourceMap resourceMap;
    private InvocationContext context;
    private int completionStatus;
    private InvocationResults results;
    private InvocationError errorInfo;
    final Logger logger = LoggerFactory.getLogger(AbstractBatchInvocable.class);

    public AbstractBatchInvocable() {
        init();
    }

    private void init() {
        this.invocationModes = Collections.emptyList();
        this.resourceMap = null;
        this.context = null;
        this.completionStatus = STATUS_UNSTARTED;
        this.results = new InvocationResults();
        this.errorInfo = null;
    }

    protected void logInvocationContext() {
        if (logger.isInfoEnabled()) {
            logger.info("Invocation mode=" + this.getInvocationContext().getMode());
            logger.info("Invocation doc type=" + this.getInvocationContext().getDocType());
            logger.info("Invocation single CSID=" + this.getInvocationContext().getSingleCSID());
            logger.info("Invocation group CSID=" + this.getInvocationContext().getGroupCSID());
            InvocationContext.ListCSIDs lcsids = this.getInvocationContext().getListCSIDs();
            if (lcsids == null) {
                logger.info("Invocation list CSIDs are null.");
            } else {
                List<String> csidsList = lcsids.getCsid();
                if (csidsList != null) {
                    for (String listcsid : csidsList) {
                        logger.info("List CSID=" + listcsid);
                    }
                }
            }
            InvocationContext.Params params = this.getInvocationContext().getParams();
            if (params == null) {
                logger.info("Invocation list Params are null.");
            } else {
                List<InvocationContext.Params.Param> paramsList = params.getParam();
                if (paramsList != null) {
                    for (InvocationContext.Params.Param param : paramsList) {
                        logger.info("Param key=" + param.getKey());
                        logger.info("Param value=" + param.getValue());
                    }
                }
            }
        }
    }

    @Override
    public List<String> getSupportedInvocationModes() {
        return invocationModes;
    }

    protected void setSupportedInvocationModes(List<String> invocationModes) {
        this.invocationModes = invocationModes;
    }

    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    @Override
    public void setResourceMap(ResourceMap resourceMap) {
        this.resourceMap = resourceMap;
    }

    public InvocationContext getInvocationContext() {
        return context;
    }

    @Override
    public void setInvocationContext(InvocationContext context) {
        this.context = context;
    }

    @Override
    public int getCompletionStatus() {
        return completionStatus;
    }

    protected void setCompletionStatus(int completionStatus) {
        this.completionStatus = completionStatus;
    }

    @Override
    public InvocationResults getResults() {
        return results;
    }

    protected void setResults(InvocationResults results) {
        this.results = results;
    }

    @Override
    public InvocationError getErrorInfo() {
        return errorInfo;
    }

    protected void setErrorInfo(InvocationError errorInfo) {
        this.errorInfo = errorInfo;
    }

    protected boolean requestIsForInvocationModeSingle() {
        return (INVOCATION_MODE_SINGLE.equalsIgnoreCase(getInvocationContext().getMode()) ? true : false);
    }

    protected boolean requestIsForInvocationModeList() {
        return (INVOCATION_MODE_LIST.equalsIgnoreCase(getInvocationContext().getMode()) ? true : false);
    }

    protected boolean requestIsForInvocationModeGroup() {
        return (INVOCATION_MODE_GROUP.equalsIgnoreCase(getInvocationContext().getMode()) ? true : false);
    }

    protected boolean requestIsForInvocationModeNoContext() {
        return (INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(getInvocationContext().getMode()) ? true : false);
    }

    protected void setErrorResult(String message) {
        setErrorResult(message, INT_ERROR_STATUS);
    }

    protected void setErrorResult(String message, int errorStatus) {

        setCompletionStatus(STATUS_ERROR);

        InvocationError errInfo = getErrorInfo();
        if (errInfo == null) {
            errInfo = new InvocationError(errorStatus, message);
        } else {
            errInfo.setMessage(message);
            errInfo.setResponseCode(errorStatus);
        }
        setErrorInfo(errInfo);

        InvocationResults invResults = getResults();
        if (invResults == null) {
            invResults = new InvocationResults();
        }
        invResults.setUserNote(getErrorInfo().getMessage());
        setResults(invResults);
    }

    @Override
    public abstract void run();
}
