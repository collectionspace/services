package org.collectionspace.services.structureddate;

public class InvalidDateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	InvalidDateException() {
		super();
	}
	
	InvalidDateException(String message) {
		super(message);
	}
}
