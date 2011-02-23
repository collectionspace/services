package org.collectionspace.services.client;

//import java.io.ByteArrayInputStream; //FIXME: REM - Remove these unneeded import statements
//import java.io.InputStream;
//import java.io.Reader;
//import java.io.StringReader;
//import java.util.Iterator;
//import java.util.List;

//import javax.xml.transform.Source;
//import javax.xml.transform.stream.StreamSource;

//import org.dom4j.Attribute;
//import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
//import org.dom4j.Namespace;
//import org.dom4j.io.SAXReader;
//import org.xml.sax.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoxPayloadIn extends PoxPayload<PayloadInputPart> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*
	 * Parse the POX 'xmlPayload' into individual parts.  Each part is saved
	 * as a DOM4j Element and, if possible, a JAXB object instance as well.
	 */
	public PoxPayloadIn(String xmlPayload) throws DocumentException {
		super(xmlPayload);
		if (logger.isTraceEnabled() == true) {
			logger.trace("\n\n>>>> Payload In : BEGIN >>>>\n" +
					xmlPayload +
					"\n>>>> Payload In : END   >>>>\n");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.PoxPayload#createPart(java.lang.String, java.lang.Object, org.dom4j.Element)
	 * 
	 * We need this method because the generic base class has no way of calling our constructor.
	 */
	@Override
	protected PayloadInputPart createPart(String label, Object jaxbObject, Element element) {
		return new PayloadInputPart(label, jaxbObject, element);
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.PoxPayload#createPart(java.lang.String, org.dom4j.Element)
	 * 
	 * We need this method because the generic base class has no way of calling our constructor.
	 */
	@Override
	protected PayloadInputPart createPart(String label, Element element) {
		return new PayloadInputPart(label, element);
	}
	
}
