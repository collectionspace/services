package org.collectionspace.services.common;

import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.CollectionSpaceHandlerFactory;
//import org.collectionspace.services.relation.nuxeo.RelationHandlerFactory;

public abstract class CollectionSpaceResource {

	// Fields for default client factory and client
	private RepositoryClientFactory defaultClientFactory;
	private RepositoryClient defaultClient;
	
	// Fields for default document handler factory and handler
	private CollectionSpaceHandlerFactory defaultHandlerFactory;
	private DocumentHandler defaultHandler;
	
	// Methods that subclasses must implement
	abstract protected String getClientType();
	abstract protected RepositoryClientFactory getDefaultClientFactory();
	abstract protected CollectionSpaceHandlerFactory getDefaultHandlerFactory();
	
	protected RepositoryClient getDefaultClient() {
		return this.defaultClient;
	}
	
	protected DocumentHandler getDefaultHandler() {
		return this.defaultHandler;
	}
	
	public CollectionSpaceResource() {
		defaultClientFactory = getDefaultClientFactory(); //implemented by subclasses
		defaultClient = defaultClientFactory.getClient(getClientType());
		defaultHandlerFactory = getDefaultHandlerFactory(); //implemented by subclasses
		defaultHandler = defaultHandlerFactory.getHandler(getClientType());
	}	
}
