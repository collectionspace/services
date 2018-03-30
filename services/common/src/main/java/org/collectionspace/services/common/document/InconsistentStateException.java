package org.collectionspace.services.common.document;

public class InconsistentStateException extends TransactionException {
	private static final long serialVersionUID = 11L;

	public InconsistentStateException() {
        super(TRANSACTION_FAILED_MSG);
        setErrorCode(HTTP_CODE);
	}

	public InconsistentStateException(String msg) {
		super(msg);
	}

	public InconsistentStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InconsistentStateException(Throwable cause) {
		super(cause);
	}

}
