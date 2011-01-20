package org.collectionspace.services.common;

public class PoxPayload {
	private String xmlText;
	
	protected PoxPayload() {
		//empty
	}
	
	public void setXmlText(String xmlText) {
		this.xmlText = xmlText;
	}
	
	public PoxPayload(String xmlText) {
		this.xmlText = xmlText;
	}
	
	public String getXmlText() {
		return xmlText;
	}
}
