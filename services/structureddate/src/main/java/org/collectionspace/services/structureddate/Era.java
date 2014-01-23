package org.collectionspace.services.structureddate;

public enum Era {
	BCE ("urn:cspace:cinefiles.cspace.berkeley.edu:vocabularies:name(dateera):item:name(bce)'BCE'"),
	CE  ("urn:cspace:cinefiles.cspace.berkeley.edu:vocabularies:name(dateera):item:name(ce)'CE'");
	
	private final String value;
	
	private Era(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
