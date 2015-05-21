package org.collectionspace.services.structureddate;

public enum QualifierUnit {
	DAYS   ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datequalifier):item:name(days)'day(s)'"),
	MONTHS ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datequalifier):item:name(month)'month(s)'"),
	YEARS  ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datequalifier):item:name(years)'year(s)'");
	
	private final String value;
	
	private QualifierUnit(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
