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

public class PoxPayloadIn extends PoxPayload {
	private List<PayloadInputPart> parts;

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
		super(xmlText);
		SAXReader reader = new SAXReader();
		Document doc = reader.read(getInputStream(xmlText));  //FIXME: REM - Use StringReader instead of InputStream
		Iterator it = doc.getRootElement().elementIterator();
		while (it.hasNext()) {
			Element element = (Element) it.next();
			Namespace namespace = (Namespace)element.declaredNamespaces().get(0);
			System.out.println("element name: " + element.getName());
			System.out.println("attributeCount: " + element.attributeCount());
			List<Attribute> attributes = element.attributes();
			for (Attribute  attr : attributes) {
				System.out.println("Attribute: " + attr.getName() + ":" +
						attr.asXML());
			}	
		}		
	}

		public List<PayloadInputPart> getParts() {
			return parts;
		}
	}
