package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PoxPayloadOut extends PoxPayload {
		
		
	public String toXML() {
		String result = null;
        Document document = DocumentHelper.createDocument();
        document.setName(getName());
        Element root = document.addElement( "document" );
        root.addAttribute("name", getName());
		
		Iterator<PayloadOutputPart> it = getParts().iterator();
		while (it.hasNext() == true) {
			PayloadOutputPart outPart = it.next();
    		root.add(toElement(outPart.getBody()));
		}
		result = document.asXML();
		return result;
	}
		
	public PoxPayloadOut(String documentName) {
		super(documentName);
	}
	
	public PayloadOutputPart addPart(Object entity, MediaType mediaType) {
		PayloadOutputPart result = new PayloadOutputPart("unlabelled", entity);
		getParts().add(result);
		return result;
	}
	
	@Override
	public String toString() {
		return toXML();
	}
}
