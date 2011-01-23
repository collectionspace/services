package org.collectionspace.services.client;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

public class PayloadInputPart extends PayloadPart {
	public String getBodyAsString() {
		return null;
	}
	
	public <T> T getBody(Class<T> type, Type genericType) throws IOException {
		return null;
	}

	public MediaType getMediaType() {
		return MediaType.APPLICATION_XML_TYPE;	
	}
}
