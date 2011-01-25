package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

public class PayloadOutputPart extends PayloadPart {
	
	PayloadOutputPart(String label, Object body) {
		super(label, body);
	}
	
	@Override
	public String asXML() {
		String result = null;
		Object body = getBody();
		if (body != null) {
			result = PoxPayload.toElement(body).asXML();
		}
		return result;
	}
	
}
