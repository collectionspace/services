package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PoxPayloadOut extends PoxPayload {
	private List<PayloadOutputPart> parts = new ArrayList<PayloadOutputPart>();
	
    public Element toElement(Object jaxbObject) {
    	Element result = null;
    	String text = null;
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
    		String thePackage = jaxbObject.getClass().getPackage().getName();
	    	JAXBContext jc = JAXBContext.newInstance(thePackage);
	    	//Create marshaller
	    	Marshaller m = jc.createMarshaller();
	    	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
	    	//Marshal object into file.
	    	m.marshal(jaxbObject, outputStream);
	    	text = outputStream.toString();

    		Document doc = DocumentHelper.parseText(text);
    		result = doc.getRootElement();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }
	
	public String toXML() {
		String result = null;
        Document document = DocumentHelper.createDocument();
        document.setName(getName());
        Element root = document.addElement( "document" );
        root.addAttribute("name", getName());
		
		Iterator<PayloadOutputPart> it = parts.iterator();
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
		PayloadOutputPart result = new PayloadOutputPart(entity);
		parts.add(result);
		return result;
	}
	
	@Override
	public String toString() {
		return toXML();
	}
}
