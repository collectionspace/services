package org.collectionspace.services.common.storage.jpa;

import org.collectionspace.services.common.document.TransactionException;

public class JPATransactionException extends TransactionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2018758347488796620L;

    public JPATransactionException(String msg) {
    	super(msg);
    }
}
