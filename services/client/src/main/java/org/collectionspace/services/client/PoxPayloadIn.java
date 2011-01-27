package org.collectionspace.services.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

public class PoxPayloadIn extends PoxPayload<PayloadInputPart> {

	private InputStream getInputStream(String inputString) {
		return new ByteArrayInputStream(inputString.getBytes());
	}

	public static PoxPayloadIn createInstance(String xmlText) throws DocumentException {
		PoxPayloadIn result = null;
		SAXReader reader = new SAXReader();
		StringReader strReader = new StringReader(xmlText);
		Document doc = reader.read(strReader);        

		return result;
	}

	public PoxPayloadIn(String xmlText) throws DocumentException {
		this.setXmlText(xmlText);
		SAXReader reader = new SAXReader();
		Document doc = reader.read(getInputStream(xmlText));  //FIXME: REM - Use StringReader instead of InputStream
		Iterator<Element> it = doc.getRootElement().elementIterator();
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
			//FIXME: REM - Add this to logger.isTraceEnables()
			Namespace namespace = (Namespace)element.declaredNamespaces().get(0);
			System.out.println("element/label name: " + label);
			System.out.println("attributeCount: " + element.attributeCount());
			List<Attribute> attributes = element.attributes();
			for (Attribute  attr : attributes) {
				System.out.println("Attribute: " + attr.getName() + ":" +
						attr.asXML());
			}	
		}		
	}

}
