package org.collectionspace.services.batch.nuxeo;

import java.util.Arrays;

import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.batch.BatchCommon;

public class TestBatchJob extends AbstractBatchInvocable {

	public TestBatchJob() {
		super();
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST,
                INVOCATION_MODE_GROUP, INVOCATION_MODE_NO_CONTEXT));
	}
	
	@Override
	public void run() {
		// An empty batch job used just for testing.
	}
	
	@Override
	public void run(BatchCommon batchCommon) {
		// An empty batch job used just for testing.
	}
}
