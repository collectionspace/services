package org.collectionspace.services.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.bind.api.impl.NameConverter;
import org.apache.commons.io.FileUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class PoxPayload.
 *
 * @param <PT> the generic type
 */
public abstract class PoxPayload<PT extends PayloadPart> {	
	
	/** The Constant logger. */
	protected static final Logger logger = LoggerFactory.getLogger(PayloadPart.class);	
	
	/** The xml text. */
	private String xmlPayload;
	
	private Document domDocument;
	
	/** The payload name. */
	private String payloadName;
	
	// The list of POX parts contained in the xmlText payload
	/** The parts. */
	private List<PT> parts = new ArrayList<PT>();	
	
	/**
	 * Instantiates a new pox payload.
	 */
	protected PoxPayload() {
		//empty
	}
	
	final protected void setPayloadName(String name) {
		this.payloadName = name;
	}
	
	private void setDomDocument(Document dom) throws DocumentException {
		this.domDocument = dom;
		String label = domDocument.getRootElement().getName();
		if (label != null) {
			this.payloadName = label;
		} else if (logger.isWarnEnabled() == true) {
			logger.warn("Incoming message payload is missing a name/label.");
			logger.warn(this.xmlPayload);
		}
		parseParts();
	}
	
	/**
	 * Instantiates a new PoxPayload by parsing the payload into a DOM4j
	 * Document instance
	 *
	 * @param payloadName the payload name
	 */
	protected PoxPayload(String xmlPayload) throws DocumentException {
		this.xmlPayload = xmlPayload;
		SAXReader reader = new SAXReader();
		Document dom  = reader.read(new StringReader(xmlPayload));
		setDomDocument(dom);
	}
	
    /**
     * Instantiates a new payload, saves the original xml, creates a DOM and parses it into parts
     *
     * @param file the file
     * @throws DocumentException the document exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected PoxPayload(File file) throws DocumentException, IOException {
    	this.xmlPayload = FileUtils.readFileToString(file);
        SAXReader reader = new SAXReader();
        Document dom = reader.read(file);
		setDomDocument(dom);
    }	
	
	/**
	 * Creates the part -either an PayloadOutputPart or a PayloadInputPart
	 *
	 * @param label the part label
	 * @param jaxbObject the JAXB object
	 * @param element the DOM4j element
	 * @return the pT
	 */
	abstract protected PT createPart(String label, Object jaxbObject, Element element);
	
	/**
	 * Creates the part -either an PayloadOutputPart or a PayloadInputPart
	 *
	 * @param label the part label
	 * @param element the DOM4j element
	 * @return the pT
	 */
	abstract protected PT createPart(String label, Element element);	
	
	/**
	 * Parse the DOM object into schema parts.
	 *
	 * @throws DocumentException the document exception
	 */
	protected void parseParts() throws DocumentException {
		Iterator<Element> it = getDOMDocument().getRootElement().elementIterator();
		PT payloadPart = null;
		while (it.hasNext() == true) {
			Element element = (Element) it.next();
			String label = element.getName();
			Object jaxbObject = PoxPayload.toObject(element);			
			if (jaxbObject != null) {
				payloadPart = createPart(label, jaxbObject, element);
			} else {
				payloadPart = createPart(label, element);
			}
			if (payloadPart != null) {
				this.addPart(payloadPart);
			}
		}
	}
	
	/**
	 * Gets the name of the payload.
	 *
	 * @return the name
	 */
	public String getName() {
		return payloadName;
	}
	
	/**
	 * Gets the DOM object that we created at init time.  This should never be null;
	 *
	 * @return the dOM document
	 */
	public Document getDOMDocument() {
		return this.domDocument;
	}
		
	/**
	 * Gets the xml text.
	 *
	 * @return the xml text
	 */
	public String getXmlPayload() {
		return xmlPayload;
	}
	
	/**
	 * Gets the POX part with name match 'label'.
	 *
	 * @param label the label
	 * @return the part
	 */
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
	
	/**
	 * Gets a list of the POX parts.
	 *
	 * @return the parts
	 */
	public List<PT> getParts() {
		return parts;
	}
		
	/**
	 * Adds a POX part to the list of existing parts with the label 'label'.
	 *
	 * @param label the label
	 * @param entity the entity
	 * @return the pT
	 */
	public PT addPart(String label, PT entity) {
		parts.add(entity);
		return entity;
	}
	
	/**
	 * Adds a POX part -assuming the part already has a label name.
	 *
	 * @param entity the entity
	 * @return the pT
	 */
	public PT addPart(PT entity) {
		parts.add(entity);
		return entity;
	}	
		
    /**
     * Gets the Java package name from the specified namespace.  This method
     * assumes the Namespace is a xjc (JAXB compiler) generate namespace from
     * which we can extract the Java package name.
     *
     * @param namespace the namespace
     * @return the Java package name
     */
    private static String getPackage(Namespace namespace) {
        NameConverter nc = NameConverter.standard;
		String namespaceURI = namespace.getURI();
        return nc.toPackageName(namespaceURI);
    }
      
    /**
     * Attempts to marshal a DOM4j element (for a part) into an instance of a JAXB object
     *
     * @param elementInput the element input
     * @return the object
     */
    public static Object toObject(Element elementInput) {
    	Object result = null;
    	try {
    		Namespace namespace = elementInput.getNamespace();    		
    		String thePackage = getPackage(namespace);
	    	JAXBContext jc = JAXBContext.newInstance(thePackage);
	    	Unmarshaller um = jc.createUnmarshaller();
	    	result = um.unmarshal(
	    			new StreamSource(new StringReader(elementInput.asXML())));	    			
    	} catch (Exception e) {
    		if (logger.isTraceEnabled() == true) {
    			logger.trace(e.getMessage());
    		}
    	}
    	
    	return result;
    }
	
    /**
     * Attempts to unmarshal a JAXB object (for a part) to a DOM4j element.
     *
     * @param jaxbObject the jaxb object
     * @return the element
     */
    public static Element toElement(Object jaxbObject) {
    	Element result = null;
    	String text = null;
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
    		String thePackage = jaxbObject.getClass().getPackage().getName();
    		if (thePackage.equals(JAXBElement.class.getPackage().getName())) {
    			JAXBElement jaxbElement = (JAXBElement)jaxbObject;
    			thePackage = jaxbElement.getValue().getClass().getPackage().getName();
    		}
	    	JAXBContext jc = JAXBContext.newInstance(thePackage);
	    	//Create marshaller
	    	Marshaller m = jc.createMarshaller();
	    	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
	    	//Marshal object into file.
	    	m.marshal(jaxbObject, outputStream);
	    	text = outputStream.toString("UTF8");

    		Document doc = DocumentHelper.parseText(text);
    		result = doc.getRootElement(); //FIXME: REM - call .detach() to free the element
    	} catch (Exception e) {
    		e.printStackTrace(); //FIXME: REM - Please use proper logger.isWarning() statement
    	}
    	
    	return result;
    }
    
    /**
     * Attempts to unmarshal a JAXB object (for a part) to a DOM4j element.
     *
     * @param jaxbObject the jaxb object
     * @return the element
     */
    public static Element toElement(String xmlPayload) throws DocumentException {
    	Element result = null;
		Document doc = DocumentHelper.parseText(xmlPayload);
		result = doc.getRootElement(); //FIXME: REM - .detach();
    	return result;
    }	
    
	
}
