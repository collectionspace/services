package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.dom4j.Element;

public class PayloadOutputPart extends PayloadPart {
	
	PayloadOutputPart(String label, Object body) {
		super(label, body);
	}
	
	PayloadOutputPart(String label, Element elementBody) {
		super(label, elementBody);
	}
	
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
