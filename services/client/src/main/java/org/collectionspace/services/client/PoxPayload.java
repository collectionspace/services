package org.collectionspace.services.client;

public class PoxPayload {
	private String xmlText;
	private String documentName;
	
	protected PoxPayload() {
		//empty
	}
	
	public void setXmlText(String xmlText) {
		this.xmlText = xmlText;
	}
	
	public PoxPayload(String documentName) {
		this.documentName = documentName;
	}
	
	public String getName() {
		return documentName;
	}
	
	public String getXmlText() {
		return xmlText;
	}
}
