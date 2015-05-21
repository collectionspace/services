package org.collectionspace.services.structureddate;

public enum Certainty {
	AFTER         ("after"),
	APPROXIMATELY ("approximate"),
	BEFORE        ("before"),
	CIRCA         ("circa"),
	POSSIBLY      ("possibly"),
	PROBABLY      ("probably");
	
	private final String value;
	
	private Certainty(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
