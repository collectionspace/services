package org.collectionspace.services.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class PoxPayload<PT extends PayloadPart> {
	private String xmlText;
	private String documentName;
	
	private List<PT> parts = new ArrayList<PT>();	
	
	protected PoxPayload() {
		//empty
	}
	
	protected PoxPayload(String documentName) {
		this.documentName = documentName;
	}
	
	public PT getPart(String label) {
		PT result = null;
		if (parts != null) {
			Iterator<PT> it = parts.iterator();
			while (it.hasNext() == true) {
				PT part = it.next();
				if (part.getLabel().equalsIgnoreCase(label) == true) {
					result = part;
					break;
				}
			}
		}
		return result;
	}
	
	public List<PT> getParts() {
		return parts;
	}
	
	public PT addPart(String label, PT entity) {
		parts.add(entity);
		return entity;
	}
	
	public PT addPart(PT entity) {
		parts.add(entity);
		return entity;
	}	
		
    public static Object toObject(Element elementInput) {
    	Object result = null;
    	try {
    		String thePackage = "org.collectionspace.services.intake";
	    	JAXBContext jc = JAXBContext.newInstance(thePackage);
	    	Unmarshaller um = jc.createUnmarshaller();
	    	result = um.unmarshal(
	    			new ByteArrayInputStream(elementInput.asXML().getBytes())); //FIXME:REM - For efficiency, use org.dom4j.JAXBReader() with a JAXBObjectHandler to get the unmarshalled object
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }
	
    public static Element toElement(Object jaxbObject) {
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
	
	public void setXmlText(String xmlText) {
		this.xmlText = xmlText;
	}
	
	public String getName() {
		return documentName;
	}
	
	public String getXmlText() {
		return xmlText;
	}
}
