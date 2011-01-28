package org.collectionspace.services.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

public class PoxPayloadIn extends PoxPayload<PayloadInputPart> {

	/*
	 * Parse the POX 'xmlPayload' into individual parts.  Each part is saved
	 * as a DOM4j Element and, if possible, a JAXB object instance as well.
	 */
	public PoxPayloadIn(String xmlPayload) throws DocumentException {
		super(xmlPayload);
		Iterator<Element> it = getDOMDocument().getRootElement().elementIterator();
		PayloadInputPart payloadInputPart = null;
		while (it.hasNext() == true) {
			Element element = (Element) it.next();
			String label = element.getName();
			Object jaxbObject = PoxPayload.toObject(element);			
			if (jaxbObject != null) {
				payloadInputPart = new PayloadInputPart(label, jaxbObject, element);
			} else {
				payloadInputPart = new PayloadInputPart(label, element);
			}
			if (payloadInputPart != null) {
				this.addPart(payloadInputPart);
			}
		}		
	}

}
