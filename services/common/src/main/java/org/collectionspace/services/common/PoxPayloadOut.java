package org.collectionspace.services.common;

import javax.ws.rs.core.MediaType;

public class PoxPayloadOut extends PoxPayload {
	public PoxPayloadOut(String xmlText) {
		super(xmlText);
	}
	
	public PoxPayloadOut() {
		super();
	}
	
	public PayloadOutputPart addPart(String entity, MediaType mediaType) {
		return null;
	}
}
