package org.collectionspace.services.batch;

import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;

public interface BatchInvocable extends Invocable {

	/**
	 * Sets the invocation context for the batch job. Called before run().
	 * @param context an instance of InvocationContext.
	 */
	public void setResourceMap(ResourceMap resourceMap);

	public CoreSessionInterface getRepoSession();

	public String getTenantId();
	
    public void run(BatchCommon batchCommon);
}
