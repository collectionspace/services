package org.collectionspace.services.client;

	abstract class PayloadPart {
	private String label;
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
