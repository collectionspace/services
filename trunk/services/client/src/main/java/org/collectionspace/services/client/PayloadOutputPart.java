package org.collectionspace.services.client;

import org.dom4j.DocumentException;
import org.dom4j.Element;

public class PayloadOutputPart extends PayloadPart {
	
	//
	// Constructors
	//
	public PayloadOutputPart(String label, Object body) {
		super(label, body);
	}
	
	PayloadOutputPart(String label, Element elementBody) {
		super(label, elementBody);
	}
	
	PayloadOutputPart(String label, String xmlBody) throws DocumentException {
		super(label, xmlBody);
	}	
	
	PayloadOutputPart(String label, Object body, Element elementBody) {
		super(label, body, elementBody);
	}
	
	//
	// Utility Methods
	//
	
	@Override
	public String asXML() {
		String result = null;
				
		Element elementBody = getElementBody();
		if (elementBody != null) {
			result = elementBody.asXML();
		} else { 
			Object body = getBody();
			if (body != null) {
				result = PoxPayload.toElement(body).asXML();
			}
		}
		
		return result;
	}
	
}
