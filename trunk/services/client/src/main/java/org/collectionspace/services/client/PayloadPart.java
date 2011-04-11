package org.collectionspace.services.client;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PayloadPart {
	protected final Logger logger = LoggerFactory.getLogger(PayloadPart.class);
		
	private String label;
	private Object body;
	private Element elementBody;

	public PayloadPart(String label) {
		this.label = label;
	}
	
	public PayloadPart(String label, Object body) {
		this(label);
		this.body = body;
	}
		
	/**
	 * Instantiates a new payload part by parsing the XML string 'xmlPayload'
	 *
	 * @param label the label
	 * @param xmlPayload the xml payload
	 * @throws DocumentException the document exception
	 */
	public PayloadPart(String label, String xmlPayload) throws DocumentException {
		this(label);
		Element element = PoxPayload.toElement(xmlPayload);
		this.elementBody = element;
		
	}

	public PayloadPart(String label, Object body, Element elementBody) {
		this(label, body);
		this.elementBody = elementBody;
	}

	public PayloadPart(String label, Element elementBody) {
		this(label);
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
		Object result = body;
		if (result == null) {
			body = PoxPayload.toObject(this.getElementBody());
			result = body;
		}
		return result;
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
