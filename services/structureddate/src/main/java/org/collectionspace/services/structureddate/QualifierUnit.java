package org.collectionspace.services.structureddate;

public enum QualifierUnit {
	DAYS   ("days"),
	MONTHS ("month"),
	YEARS  ("years");
	
	private final String value;
	
	private QualifierUnit(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
