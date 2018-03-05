package org.collectionspace.services.structureddate;

public enum Certainty {
	AFTER         ("urn:cspace:core.collectionspace.org:vocabularies:name(datecertainty):item:name(after)'After'"),
	APPROXIMATELY ("urn:cspace:core.collectionspace.org:vocabularies:name(datecertainty):item:name(approximate)'Approximate'"),
	BEFORE        ("urn:cspace:core.collectionspace.org:vocabularies:name(datecertainty):item:name(before)'Before'"),
	CIRCA         ("urn:cspace:core.collectionspace.org:vocabularies:name(datecertainty):item:name(circa)'Circa'"),
	POSSIBLY      ("urn:cspace:core.collectionspace.org:vocabularies:name(datecertainty):item:name(possibly)'Possibly'"),
	PROBABLY      ("urn:cspace:core.collectionspace.org:vocabularies:name(datecertainty):item:name(probably)'Probably'");
	
	private final String value;
	
	private Certainty(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
