package org.collectionspace.services.nuxeo.client.java;

import org.collectionspace.services.common.document.DocumentException;
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
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getCausesClassName() {
		String result = null;
		Throwable cause = super.getCause();
		
		if (cause != null && cause instanceof WrappedException) {
			WrappedException wrappedException = (WrappedException)cause;
			result = wrappedException.getClassName();
		} else {
			result = cause != null ? super.getCausesClassName() : null;
		}
		
		return result;
	}
		
	protected boolean isNuxeoWrappedException(Throwable cause) {
		boolean result = false;
		
		String className = cause.getClass().getCanonicalName();
		if (className.contains("org.nuxeo.ecm.core.api.WrappedException") == true) {
			result = true;
		}
		
		return result;
	}
	
}
