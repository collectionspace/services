package org.collectionspace.services.structureddate;

public enum Qualifier {
	MINUS         ("-"),
	PLUS          ("+"),
	PLUS_OR_MINUS ("+/-");
	
	private final String value;
	
	private Qualifier(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
