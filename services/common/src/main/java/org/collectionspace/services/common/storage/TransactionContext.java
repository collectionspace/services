package org.collectionspace.services.common.storage;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.TransactionException;

@SuppressWarnings("rawtypes")
public abstract class TransactionContext {

	protected ServiceContext ctx;
	
	public ServiceContext getServiceContext() {
		// TODO Auto-generated method stub
		return ctx;
	}

	abstract public void markForRollback();

	abstract public void close() throws TransactionException;
	
	abstract public void beginTransaction() throws TransactionException;
	
	abstract public void commitTransaction() throws TransactionException;

	abstract public boolean isTransactionActive() throws TransactionException;
}
