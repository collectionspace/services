package org.collectionspace.services.batch;

import java.util.HashMap;
import java.util.Set;

import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;

public interface BatchInvocable extends Invocable {

	/**
	 * Sets the invocation context for the batch job. Called before run().
	 * @param context an instance of InvocationContext.
	 */
	public void setResourceMap(ResourceMap resourceMap);
	public void setRepoSession(CoreSessionInterface repoSession);
	public void setTenantId(String tenantId);
}
