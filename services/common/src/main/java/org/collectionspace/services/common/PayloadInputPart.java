package org.collectionspace.services.common;

import java.io.IOException;
import java.lang.reflect.Type;

public class PayloadInputPart {
	public String getLabel() {
		return null;
	}
	
	public String getBodyAsString() {
		return null;
	}
	
	public <T> T getBody(Class<T> type, Type genericType) throws IOException {
		return null;
	}

}
