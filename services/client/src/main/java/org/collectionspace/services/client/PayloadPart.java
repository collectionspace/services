package org.collectionspace.services.client;

	abstract class PayloadPart {
	private String label;
	private Object body;

	public PayloadPart(String label) {
		this.label = label;
	}
	
	public PayloadPart(String label, Object body) {
		this.label = label;
		this.body = body;
	}
	
	abstract public String asXML();
	
	public Object getBody() {
		return body;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
