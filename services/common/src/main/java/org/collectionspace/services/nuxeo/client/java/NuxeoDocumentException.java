package org.collectionspace.services.nuxeo.client.java;

import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.document.DocumentException;
import org.apache.http.HttpStatus;

import org.nuxeo.ecm.core.api.ConcurrentUpdateException;
import org.nuxeo.ecm.core.api.WrappedException;

public class NuxeoDocumentException extends DocumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NuxeoDocumentException() {
		super();
	}

	public NuxeoDocumentException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	public NuxeoDocumentException(int errorCode) {
		super(errorCode);
		// TODO Auto-generated constructor stub
	}

	public NuxeoDocumentException(int errorCode, String errorReason) {
		super(errorCode, errorReason);
		// TODO Auto-generated constructor stub
	}

	public NuxeoDocumentException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NuxeoDocumentException(Throwable cause) {
		super(cause);
		if (cause instanceof ConcurrentUpdateException) {
			this.setErrorCode(HttpStatus.SC_CONFLICT); // HttpStatus.CONFLICT_409
		}
	}
	
	private static String getExceptionClassName(Throwable exception) {
		String result = null;
		
		if (exception != null) {
			result = exception.getClass().getCanonicalName();
			if (exception instanceof WrappedException) {
				result = ((WrappedException)exception).getClassName();  // Nuxeo wraps the original exception, so we need to get the name of it.
			}
		}
		
		return result;
	}

	@Override
	public boolean exceptionChainContainsNetworkError() {
		boolean result = false;
		
		Throwable cause = this;
		while (cause != null) {
			String exceptionClassName = getExceptionClassName(cause);
			if (CSWebApplicationException.isExceptionNetworkRelated(exceptionClassName) == true) {
				result = true;
				break;
			}
			
			cause = cause.getCause();
		}

		return result;
	}
	
}
