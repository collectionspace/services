package org.collectionspace.services.structureddate;

public enum Era {
	BCE ("urn:cspace:*:vocabularies:name(dateera):item:name(bce)'BCE'"),
	CE  ("urn:cspace:*:vocabularies:name(dateera):item:name(ce)'CE'");
	
	private final String value;
	
	private Era(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public String toString(String tenantDomain) {
		String result = toString().replace("*", tenantDomain);
		return result;
	}
	
	public String toDisplayString() {
		int index = value.indexOf("'");
		
		return value.substring(index + 1, value.length() - 1);
	}
}
