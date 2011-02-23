package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

// TODO: Auto-generated Javadoc
/**
 * The Class PoxPayloadOut.
 */
public class PoxPayloadOut extends PoxPayload<PayloadOutputPart> {
			
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
		
	public PoxPayloadOut(byte[] xmlPayload) throws DocumentException {
		super(new String(xmlPayload));
	}
	
	/**
	 * Instantiates a new PoxPayloadOut, saves the xml, creates a DOM, and parses the parts.
	 *
	 * @param file the file
	 * @throws DocumentException the document exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected PoxPayloadOut(File file) throws DocumentException, IOException {
		super(file);		
	}
		
	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.PoxPayload#createPart(java.lang.String, java.lang.Object, org.dom4j.Element)
	 * 
	 * We need this method because the generic base class has no way of calling our constructor.
	 */
	@Override
	protected PayloadOutputPart createPart(String label, Object jaxbObject, Element element) {
		return new PayloadOutputPart(label, jaxbObject, element);
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.PoxPayload#createPart(java.lang.String, org.dom4j.Element)
	 * 
	 * We need this method because the generic base class has no way of calling our constructor.
	 */
	@Override
	protected PayloadOutputPart createPart(String label, Element element) {
		return new PayloadOutputPart(label, element);
	}
	
	/**
	 * Creates and returns an XML string representation of ourself.
	 *
	 * @return the string
	 */
	public String toXML() {
		String result = null;
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("UTF-8");
        document.setName(getName());
        Element root = document.addElement( "document" );
        root.addAttribute("name", getName());        
		
		Iterator<PayloadOutputPart> it = getParts().iterator();
		while (it.hasNext() == true) {
			PayloadOutputPart outPart = it.next();
			Element element = outPart.asElement();			
			if (element != null) {
				root.add(element.detach());
			} else {
				//Add if (logger.isTraceEnabled() == true) logger.trace("Output part: " + outPart.getLabel() + " was empty.");
			}
		}
		result = document.asXML();
		
		if (logger.isTraceEnabled() == true) {
			logger.trace("\n\n<<<< Payload Out : BEGIN <<<<\n" +
					result +
					"\n<<<< Payload Out : END   <<<<\n");
		}
		
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
	
	@Deprecated
	public PayloadOutputPart addPart(String xmlPayload, MediaType mediaType) throws DocumentException {
		PayloadOutputPart result = addPart("unlabelled", xmlPayload);
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
	 * Adds a DOM4j Element part.
	 *
	 * @param label the label
	 * @param elementBody the element body
	 * @return the payload output part
	 */
	public PayloadOutputPart addPart(String label, String xmlBody) throws DocumentException { 
		PayloadOutputPart result = new PayloadOutputPart(label, xmlBody);
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
	
	public byte[] getBytes() {
		byte[] result = null;
		try {
			result = toString().getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); //FIXME: REM - Add proper logging statement here
		}
		return result;
	}
}
