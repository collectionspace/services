package org.collectionspace.services.structureddate;

public enum QualifierType {
	MINUS         ("-"),
	PLUS          ("+"),
	PLUS_OR_MINUS ("+/-");
	
	private final String value;
	
	private QualifierType(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
