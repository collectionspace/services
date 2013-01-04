package org.collectionspace.services.batch.nuxeo;

import java.util.Arrays;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.common.api.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob extends AbstractBatchInvocable {

    final String CLASSNAME = this.getClass().getSimpleName();
    final Logger logger = LoggerFactory.getLogger(UpdateObjectLocationBatchJob.class);

    // Initialization tasks
    public UpdateObjectLocationBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE));
    }

    /**
     * The main work logic of the batch job. Will be called after setContext.
     */
    @Override
    public void run() {

        setCompletionStatus(STATUS_MIN_PROGRESS);
        try {
            // FIXME: Placeholder during early development
            if (logger.isInfoEnabled()) {
                logger.info("Invoking " + CLASSNAME + " ...");
                logger.info("Invocation context is: " + getInvocationContext().getMode());
            }
            if (!requestedInvocationModeIsSupported()) {
                setInvocationModeNotSupportedResult();
            }
            String csid;
            if (requestIsForInvocationModeSingle()) {
                csid = getInvocationContext().getSingleCSID();
                if (Tools.isBlank(csid)) {
                    throw new Exception("Could not find required CSID value in the context for this batch job.");
                }
                logger.info("CSID value is: " + csid);
            }
            String docType = getInvocationContext().getDocType();
        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
        }
        setCompletionStatus(STATUS_COMPLETE);
    }
}
