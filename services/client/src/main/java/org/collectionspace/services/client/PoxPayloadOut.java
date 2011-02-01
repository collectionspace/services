package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;

// TODO: Auto-generated Javadoc
/**
 * The Class PoxPayloadOut.
 */
public class PoxPayloadOut extends PoxPayload<PayloadOutputPart> {
			
	/**
	 * Instantiates a new pox payload out.
	 *
	 * @param payloadName the payload name
	 * @throws DocumentException the document exception
	 */
	public PoxPayloadOut(String payloadName) {
		super();
		setPayloadName(payloadName);
	}
		
	
	/**
	 * Creates and returns an XML string representation of ourself.
	 *
	 * @return the string
	 */
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
		
	/**
	 * Adds the part.
	 *
	 * @param entity the entity
	 * @param mediaType the media type
	 * @return the payload output part
	 */
	@Deprecated
	public PayloadOutputPart addPart(Object entity, MediaType mediaType) {
		PayloadOutputPart result = addPart("unlabelled", entity);
		return result;
	}
	
	/**
	 * Adds a DOM4j Element part.
	 *
	 * @param label the label
	 * @param elementBody the element body
	 * @return the payload output part
	 */
	public PayloadOutputPart addPart(String label, Element elementBody) { 
		PayloadOutputPart result = new PayloadOutputPart(label, elementBody);
		getParts().add(result);
		return result;
	}
	
	/**
	 * Adds a JAXB object part.
	 *
	 * @param label the label
	 * @param entity the entity
	 * @return the payload output part
	 */
	public PayloadOutputPart addPart(String label, Object entity) {
		PayloadOutputPart result = new PayloadOutputPart(label, entity);
		getParts().add(result);
		return result;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * This method calls our "toXML()" method to return an XML representation of ourself.
	 */
	@Override
	public String toString() {
		return toXML();
	}
}
