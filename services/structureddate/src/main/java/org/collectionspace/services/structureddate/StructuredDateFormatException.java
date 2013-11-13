package org.collectionspace.services.structureddate;

public class StructuredDateFormatException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;
	
	public StructuredDateFormatException() {
		super();
	}
	
	public StructuredDateFormatException(Throwable cause) {
		super(cause);
	}
	
	public StructuredDateFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}