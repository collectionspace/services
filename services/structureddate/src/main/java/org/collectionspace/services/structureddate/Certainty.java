package org.collectionspace.services.structureddate;

public enum Certainty {
	AFTER         ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datecertainty):item:name(after)'after'"),
	APPROXIMATELY ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datecertainty):item:name(approximate)'approximate'"),
	BEFORE        ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datecertainty):item:name(before)'before'"),
	CIRCA         ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datecertainty):item:name(circa)'circa'"),
	POSSIBLY      ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datecertainty):item:name(possibly)'possibly'"),
	PROBABLY      ("urn:cspace:pahma.cspace.berkeley.edu:vocabularies:name(datecertainty):item:name(probably)'probably'");
	
	private final String value;
	
	private Certainty(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
