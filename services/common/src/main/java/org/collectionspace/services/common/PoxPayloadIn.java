package org.collectionspace.services.common;

import java.util.List;

public class PoxPayloadIn extends PoxPayload {

	public PoxPayloadIn(String xmlText) {
		super(xmlText);
	}
	
	public List<PayloadInputPart> getParts() {
		return null;
	}

}
