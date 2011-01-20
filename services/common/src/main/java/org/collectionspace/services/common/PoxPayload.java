package org.collectionspace.services.common;

public class PoxPayload {
	private String xmlText;
	
	private PoxPayload() {
		//empty
	}
	
	public PoxPayload(String xmlText) {
		this.xmlText = xmlText;
	}
	
	public String getXmlText() {
		return xmlText;
	}
}
