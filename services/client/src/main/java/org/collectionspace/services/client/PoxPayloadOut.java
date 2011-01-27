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
	private Document document;
		
	public PoxPayloadOut(String documentName) {
		super(documentName);
	}
	
//    static public Document createDocument() {
//    	List<DimensionsCommon> dimensionList = new ArrayList<DimensionsCommon>();
//    	DimensionsCommon dimension = null;
//    	for (int i = 0; i < 3; i++) {
//    		dimension = new DimensionsCommon();
//    		dimension.setCsid(Integer.toString(i));
//    		dimensionList.add(dimension);
//    	}
//    	
//        document.setName("Dimmensions");
//        Element root = document.addElement( "document" );
//        root.addAttribute("name", "Dimmensions");
//    	Iterator<DimensionsCommon> it = dimensionList.iterator();
//    	while (it.hasNext()) {
//    		DimensionsCommon dim = it.next();
//    		root.add(toElement(dim));
//    	}
//    		
//        return document;
//    }
	
	public String toXML() {
		String result = null;
        Document document = DocumentHelper.createDocument();
        document.setName(getName());
        Element root = document.addElement( "document" );
        root.addAttribute("name", getName());        
		
		Iterator<PayloadOutputPart> it = getParts().iterator();
		while (it.hasNext() == true) {
			PayloadOutputPart outPart = it.next();
			Element element = outPart.asElement();			
			if (element != null) {
				root.add(element);
			} else {
				//Add if (logger.isTraceEnabled() == true) logger.trace("Output part: " + outPart.getLabel() + " was empty.");
			}
		}
		result = document.asXML();
		return result;
	}
		
	@Deprecated
	public PayloadOutputPart addPart(Object entity, MediaType mediaType) {
		PayloadOutputPart result = addPart("unlabelled", entity);
		return result;
	}
	
	public PayloadOutputPart addPart(String label, Element elementBody) {
		PayloadOutputPart result = new PayloadOutputPart(label, elementBody);
		getParts().add(result);
		return result;
	}
	
	public PayloadOutputPart addPart(String label, Object entity) {
		PayloadOutputPart result = new PayloadOutputPart(label, entity);
		getParts().add(result);
		return result;
	}	
	
	@Override
	public String toString() {
		return toXML();
	}
}
