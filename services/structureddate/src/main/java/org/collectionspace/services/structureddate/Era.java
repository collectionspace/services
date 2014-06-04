package org.collectionspace.services.structureddate;

public enum Era {
	BCE ("urn:cspace:bampfa.cspace.berkeley.edu:vocabularies:name(dateera):item:name(bce)'BC'"),
	CE  ("urn:cspace:bampfa.cspace.berkeley.edu:vocabularies:name(dateera):item:name(ce)'AD'");
	
	private final String value;
	
	private Era(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
	
	public String toDisplayString() {
		int index = value.indexOf("'");
		
		return value.substring(index + 1, value.length() - 1);
	}
}
