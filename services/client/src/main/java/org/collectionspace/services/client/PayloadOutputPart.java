package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

public class PayloadOutputPart extends PayloadPart {
	private Object body;
	
	PayloadOutputPart(Object body) {
		this.body = body;
	}
	
	public Object getBody() {
		return body;
	}
}
