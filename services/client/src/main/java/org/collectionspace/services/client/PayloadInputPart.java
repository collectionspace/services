package org.collectionspace.services.client;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import org.dom4j.Element;

public class PayloadInputPart extends PayloadPart {
//	private Element elementBody;
	
	public PayloadInputPart(String label, Object body) {
		super(label, body);
	}
	
	public PayloadInputPart(String label, Object body, Element elementBody) {
		super(label, body, elementBody);
	}
	
	public PayloadInputPart(String label, Element elementBody) {
		super(label, elementBody);
	}
	
	@Override
	public String asXML() {
		String result = null;
		Object body = getBody();
		if (getElementBody() != null) {
			result = getElementBody().asXML();
		} else if (body != null) {
			result = PoxPayload.toElement(getBody()).asXML();
		}
		return result;
	}
		
	public <T> T getBody(Class<T> type, Type genericType) throws IOException {
		return null;
	}

	public MediaType getMediaType() {
		return MediaType.APPLICATION_XML_TYPE;	
	}
}
