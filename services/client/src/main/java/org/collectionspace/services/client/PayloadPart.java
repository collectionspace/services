package org.collectionspace.services.client;

import org.dom4j.Element;

	abstract class PayloadPart {
	private String label;
	private Object body;
	private Element elementBody;

	public PayloadPart(String label) {
		this.label = label;
	}
	
	public PayloadPart(String label, Object body) {
		this.label = label;
		this.body = body;
	}
	
	public PayloadPart(String label, Element elementBody) {
		this.label = label;
		this.elementBody = elementBody;
	}

	abstract public String asXML();
	
	public Element asElement() {
		Element result = elementBody;
		// if we don't already have an Element, let's try to create one from a JAXB object
		if (result == null) {
			if (body != null) {
				//toElement(body) will return null if not given an JAXB object
				result = PoxPayload.toElement(body);
			}
		}
		return result;
	}
	
	public Object getBody() {
		return body;
	}
	
	public Element getElementBody() {
		return elementBody;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
