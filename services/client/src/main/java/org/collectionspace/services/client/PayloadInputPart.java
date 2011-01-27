package org.collectionspace.services.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class PayloadInputPart extends PayloadPart {
//	private Element elementBody;
	
	public PayloadInputPart(String label, Object body) {
		super(label, body);
	}
	
	public PayloadInputPart(String label, Object body, Element elementBody) {
		super(label, body);
		this.elementBody = elementBody;
	}
	
	public PayloadInputPart(String label, Element elementBody) {
		super(label);
		this.elementBody = elementBody;
	}
	
	public Element getElementBody() {
		return this.elementBody;
	}
	
	@Override
	public String asXML() {
		String result = null;
		Object body = getBody();
		if (elementBody != null) {
			result = elementBody.asXML();
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
