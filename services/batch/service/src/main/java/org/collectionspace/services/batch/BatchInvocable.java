package org.collectionspace.services.batch;

import java.util.HashMap;
import java.util.Set;

import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.Invocable;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

public interface BatchInvocable extends Invocable {

	/**
	 * Sets the invocation context for the batch job. Called before run().
	 * @param context an instance of InvocationContext.
	 */
	public void setResourceMap(ResourceMap resourceMap);

    public RepositoryInstance getRepoSession();
    public void setRepoSession(RepositoryInstance repoSession);
}
