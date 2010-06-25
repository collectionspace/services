/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.document;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerConfigurationException; 
import javax.xml.transform.TransformerException; 

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.service.ObjectPartContentType;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.XmlContentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * DocumentUtils is a collection of utilities related to document management
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class DocumentUtils {
    /** The Constant logger. */
    private static final Logger logger =
        LoggerFactory.getLogger(DocumentUtils.class);

    /** The NAM e_ valu e_ separator. */
    private static String NAME_VALUE_SEPARATOR = "|";
    
    /** The XML elements with this suffix will indicate. */
    private static String STRUCTURED_TYPE_SUFFIX = "List";

    /**
     * The Class NameValue.
     */
    private static class NameValue {    	
	    /**
	     * Instantiates a new name value.
	     */
	    NameValue() {
    		// default scoped constructor to removed "synthetic accessor" warning
    	}        
        /** The name. */
        String name;        
        /** The value. */
        String value;
    };
    
    /**
     * Log byte array input stream.  After logging this method resets the stream and returns it in its original state.
     *
     * @param inputStream the input stream
     * @return the byte array input stream
     */
    private static ByteArrayInputStream logByteArrayInputStream(ByteArrayInputStream inputStream) {
    	ByteArrayInputStream result = null;
    	
    	if (logger.isDebugEnabled() == true) {
	    	ByteArrayInputStream bais = (ByteArrayInputStream)inputStream;
	
			int length = bais.available();
			byte [] buff = new byte[length];
			try {
				bais.read(buff);
			} catch (Exception e) {
				logger.debug("Could not read input stream", e);
			}
	    		
	    	String s = new String(buff);
	    	logger.debug(s);
	    	System.out.println(s); //FIXME: REM - Remove this when we figure out why the logger.debug() call is not working.
	    	//
	    	// Essentially, reset the stream and return it in its original state
	    	//
	    	result = new ByteArrayInputStream(buff);
    	}
    	
    	return result;
    }
    
    /**
     * parseProperties given payload to create XML document. this
     * method also closes given stream after parsing.
     * @param payload stream
     * @param partMeta 
     * @return parsed Document
     * @throws Exception
     */
    public static Document parseDocument(InputStream payload, ObjectPartType partMeta)
            throws Exception {
    	final String FILE_SEPARATOR = System.getProperty("file.separator");
    	final String XML_SCHEMA_EXTENSION = ".xsd";
    	final String SCHEMAS_DIR = "schemas";
    	final String JAXP_SCHEMA_SOURCE =
            "http://java.sun.com/xml/jaxp/properties/schemaSource";
    	final String JAXP_SCHEMA_LANGUAGE =
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        final String W3C_XML_SCHEMA =
            "http://www.w3.org/2001/XMLSchema";    	
    	
        Document result = null;
        // Log the incoming unprocessed payload
    	if (logger.isDebugEnabled() == true) {
    		if (payload instanceof ByteArrayInputStream) {
    			payload = logByteArrayInputStream((ByteArrayInputStream)payload);
    		}
    	}    	
    	//
    	// Look for an XML Schema (.xsd) file for the incoming part payload
    	//
    	String serverRoot = ServiceMain.getInstance().getServerRootDir();
    	String schemasDir = serverRoot + FILE_SEPARATOR + 
    		SCHEMAS_DIR + FILE_SEPARATOR;    	
    	//
    	// Send a warning to the log file if the XML Schema file is missing
    	//
    	String schemaName = schemasDir + partMeta.getLabel() + XML_SCHEMA_EXTENSION;
    	File schemaFile = null;
    	try {
    		schemaFile = new File(schemaName);
    	} catch (Exception e) {
    		if (logger.isWarnEnabled() == true) {
    			logger.warn("Missing schema file for incoming payload: " + schemaName);
    		}
    	}
    	
    	//
    	// Create and setup a DOMBuilder factory.
    	//
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //
            // Lexical Control Settings that focus on content
            //
            factory.setCoalescing(true);
            factory.setExpandEntityReferences(true);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);            
            //
            // Enable XML validation
            //
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            try {
            	factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            	if (schemaFile != null) {
            		factory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
            	}
            } catch (IllegalArgumentException e) {
                String msg = "Error: JAXP DocumentBuilderFactory attribute not recognized: " +
                        JAXP_SCHEMA_LANGUAGE + ". Check to see if parser conforms to JAXP 1.2 spec.";
                if (logger.isWarnEnabled() == true) {
                	logger.warn(msg);
                }
                throw e;
            }
            //
            // Create the builder and parse the file
            //
            result = factory.newDocumentBuilder().parse(payload);
            
            // Write it to the log so we can see what we've created.
            if (logger.isDebugEnabled() == true) {
            	logger.debug(xmlToString(result));
            	System.out.println(xmlToString(result)); //FIXME: REM - Need this until we figure out why messages are not showing up in logger.
            }
        } finally {
            if (payload != null) {
                payload.close();
            }
        }
        
        return result;
    }

    /**
     * parseProperties extract given payload (XML) into Name-Value properties. this
     * @param document to parse
     * @return map key=property name, value=property value
     * @throws Exception
     */
    public static Map<String, Object> parseProperties(Node document)
            throws Exception {
        HashMap<String, Object> objectProps = new HashMap<String, Object>();
        // Get a list of all elements in the document
        Node root = document;//.getFirstChild();
        NodeList rootChildren = root.getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node node = rootChildren.item(i);
            String name = node.getNodeName();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList nodeChildren = node.getChildNodes();
                int nodeChildrenLen = nodeChildren.getLength();
                Node firstChild = nodeChildren.item(0);
                Object value = null;
                if (firstChild != null) {
                    //first child node could be a whitespace char CSPACE-1026
                    //so, check for number of children too
                    if (firstChild.getNodeType() == Node.TEXT_NODE
                            && nodeChildrenLen == 1) {
                        value = getTextNodeValue(node);
                    } else {
                        value = getMultiValues(node);
                    }                    
                }
                //
                // Set the value even if it's null.
                // A null value implies a clear/delete of the property
                //
                objectProps.put(name, value);
            }
        }
        return objectProps;
    }

    /**
     * getMultiStringValues retrieve multi-value element values
     * assumption: backend does not support more than 1 level deep hierarchy
     * @param node
     * @return
     */
    private static String[] getMultiStringValues(Node node) {
        ArrayList<String> vals = new ArrayList<String>();
        NodeList nodeChildren = node.getChildNodes();
        for (int i = 0; i < nodeChildren.getLength(); i++) {
            Node child = nodeChildren.item(i);
            String name = child.getNodeName();
            //assumption: backend does not support more than 2 levels deep
            //hierarchy
            String value = null;
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                value = getTextNodeValue(child);
                vals.add(qualify(name, value));
            }
        }
        return vals.toArray(new String[0]);
    }
    
    /**
     * Removes all the immediate child text nodes.
     *
     * @param parent the parent
     * @return the element
     */
    private static Node removeTextNodes(Node parent) {
    	Node result = parent;
    	
    	NodeList nodeList = parent.getChildNodes();
    	int nodeListSize = nodeList.getLength();
    	for (int i = 0; i < nodeListSize; i++) {
    		Node child = nodeList.item(i);
    		if (child != null && child.getNodeType() == Node.TEXT_NODE) {
    			parent.removeChild(child);
    		}
    	}
    	
    	return result;
    }

    /**
     * getMultiValues retrieve multi-value element values
     * assumption: backend does not support more than 1 level deep hierarchy
     * @param node
     * @return
     */
    private static Object getMultiValues(Node node) throws Exception {
    	Object result = null;    	
    	
        Node child = removeTextNodes(node);
        NodeList grandChildren = child.getChildNodes();
        for (int j = 0; j < grandChildren.getLength(); j++) {

            Node grandChild = grandChildren.item(j).getFirstChild();

            // If any grandchild is non-null, return values for all grandchildren.
            if (grandChild != null) {
                if (grandChild.getNodeType() == Node.TEXT_NODE) {
                        result = getMultiStringValues(node);
                } else {
                    ArrayList<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
                    NodeList nodeChildren = node.getChildNodes();
                    for (int i = 0; i < nodeChildren.getLength(); i++) {
                        Node nodeChild = nodeChildren.item(i);
                        Map<String, Object> hashMap = parseProperties(nodeChild);
                        values.add(hashMap);
                    }
                    result = values;
                }
                break;
            }

        }
        
        return result;
    }

    /**
     * getTextNodeValue retrieves text node value
     * @param cnode
     * @return
     */
    private static String getTextNodeValue(Node cnode) {
        String value = "";
        Node ccnode = cnode.getFirstChild();
        if (ccnode != null && ccnode.getNodeType() == Node.TEXT_NODE) {
            value = ccnode.getNodeValue();
        }
        return value.trim();
    }

    /**
     * isQualified check if the given value is already qualified with given property name
     * e.g.  otherNumber|urn:org.collectionspace.id:24082390 is qualified with otherNumber
     * but urn:org.walkerart.id:123 is not qualified
     * @param name of the property, e.g. otherNumber
     * @param value of the property e.g. otherNumber
     * @return
     */
    private static boolean isQualified(String name, String value) {
        StringTokenizer stz = new StringTokenizer(value, NAME_VALUE_SEPARATOR);
        int tokens = stz.countTokens();
        if (tokens == 2) {
            String n = stz.nextToken();
            return name.equals(n);
        }
        return false;
    }

    /**
     * qualify qualifies given property value with given property name, e.g.
     * name=otherNumber and value=urn:org.collectionspace.id:24082390 would be
     * qualified as otherNumber|urn:org.collectionspace.id:24082390. however,
     * name=otherNumber and value=otherNumber|urn:org.collectionspace.id:24082390
     * would be ignored as the given value is already qualified once.
     * @param name
     * @param value
     * @return
     */
    private static String qualify(String name, String value) {
        String result = null;
        if (isQualified(name, value)) {
            result = value;
        } else {
        	result = name + NAME_VALUE_SEPARATOR + value;
        }
        return result;
    }

    /**
     * buildDocument builds org.w3c.dom.Document from given properties using
     * given metadata for a part
     * @param partMeta
     * @param rootElementName
     * @param objectProps
     * @return Document
     * @throws Exception
     */
    public static Document buildDocument(ObjectPartType partMeta, String rootElementName,
            Map<String, Object> objectProps)
            throws Exception {
        ObjectPartContentType partContentMeta = partMeta.getContent();
        XmlContentType xc = partContentMeta.getXmlContent();
        if (xc == null) {
            return null;
        }

        //FIXME: We support XML validation on the way in, so we should add it here (on the way out) as well.
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlStandalone(true);
        //JAXB unmarshaller recognizes the following kind of namespace qualification
        //only. More investigation is needed to use other prefix
		// <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		// <ns2:collectionobjects-common xmlns:ns2="http://collectionspace.org/services/collectionobject">
		// <objectNumber>objectNumber-1252960222412</objectNumber>
		// <objectName>objectName-1252960222412</objectName>
		// </ns2:collectionobjects-common>

        String ns = "ns2";
        Element root = document.createElementNS(xc.getNamespaceURI(), ns + ":" + rootElementName);
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:schemaLocation", xc.getSchemaLocation());
        root.setAttribute("xmlns:" + ns, xc.getNamespaceURI());
        document.appendChild(root);

        buildDocument(document, root, objectProps);
        
        return document;
    }
    
    /**
     * Builds the document.
     *
     * @param document the document
     * @param e the e
     * @param objectProps the object props
     * @throws Exception the exception
     */
    public static void buildDocument(Document document, Element e,
            Map<String, Object> objectProps) throws Exception {
        for (String prop : objectProps.keySet()) {
            Object value = objectProps.get(prop);
            if (value != null) {
                //no need to qualify each element name as namespace is already added
                Element newElement = document.createElement(prop);
                e.appendChild(newElement);
                if (value instanceof ArrayList<?>) {
                    //multi-value element
                    insertMultiValues(document, newElement, (ArrayList<String>) value);
                } else {
                    String strValue = objectProps.get(prop).toString();
                    insertTextNode(document, newElement, strValue);
                }
            }
        }
    }

    /**
     * Insert multi values.
     *
     * @param document the document
     * @param e the e
     * @param vals the vals
     */
    private static void insertMultiStringValues(Document document, Element e, ArrayList<String> vals) {
        String parentName = e.getNodeName();
        
        for (String o : vals) {
            String val = o;
            NameValue nv = unqualify(val);
            Element c = document.createElement(nv.name);
            e.appendChild(c);
            insertTextNode(document, c, nv.value);
        }
    }
    
    /**
     * Create a set of complex/structured elements from an array of Maps.
     *
     * @param document the document
     * @param e the e
     * @param vals the vals
     * @throws Exception the exception
     */
    private static void insertMultiHashMapValues(Document document, Element e, ArrayList<Map<String, Object>> vals)
    		throws Exception {
        String parentName = e.getNodeName();
        String childName = null;
        //
        // By convention, elements with a structured/complex type should have a parent element with a name ending with the suffix
        // STRUCTURED_TYPE_SUFFIX.  We synthesize the element name for the structured type by stripping the suffix from the parent name.
        // For example, <otherNumberList><otherNumber> <!-- sequence of simple types --> <otherNumber/><otherNumberList/>
        //
        if (parentName.endsWith(STRUCTURED_TYPE_SUFFIX) == true) {
        	int parentNameLen = parentName.length();
        	childName = parentName.substring(0, parentNameLen - STRUCTURED_TYPE_SUFFIX.length());
        } else {
        	String msg = "Unrecognized parent element name. Elements with complex/structured " +
				"types should have a parent element whose name ends with '" +
				STRUCTURED_TYPE_SUFFIX + "'.";
        	if (logger.isErrorEnabled() == true) {
        		logger.error(msg);
        	}
        	throw new Exception(msg);
        }
                
        for (Map<String, Object> map : vals) {
            Element newNode = document.createElement(childName);
            e.appendChild(newNode);
            buildDocument(document, newNode, map);
        }
    }

    /**
     * Create a set of elements for an array of values.  Currently, we're assuming the
     * values can be only of type Map or String.
     *
     * @param document the document
     * @param e the e
     * @param vals the vals
     * @throws Exception the exception
     */
    private static void insertMultiValues(Document document, Element e, ArrayList<?> vals)
    		throws Exception {
    	if (vals != null && vals.size() > 0) {
	    	Object firstElement = vals.get(0);
	    	if (firstElement instanceof String) {
	    		insertMultiStringValues(document, e, (ArrayList<String>)vals);
	    	} else if (firstElement instanceof Map<?, ?>) {
	    		insertMultiHashMapValues(document, e, (ArrayList<Map<String, Object>>)vals);
	    	} else {
	    		String msg = "Unbounded elements need to be arrays of either Strings or Maps.  We encountered an array of " +
	    			firstElement.getClass().getName();
	    		if (logger.isErrorEnabled() == true) {
	    			logger.error(msg);
	    		}
	    		throw new Exception(msg);
	    	}
    	}
    }

    /**
     * Insert text node.
     *
     * @param document the document
     * @param e the e
     * @param strValue the str value
     */
    private static void insertTextNode(Document document, Element e, String strValue) {
        Text tNode = document.createTextNode(strValue);
        e.appendChild(tNode);
    }

    /**
     * unqualify given value.
     * input of otherNumber|urn:org.collectionspace.id:24082390 would be unqualified
     * as name=otherNumber and value=urn:org.collectionspace.id:24082390
     * @param input
     * @return name and value
     * @exception IllegalStateException
     */
    private static NameValue unqualify(String input) {
        NameValue nv = new NameValue();
        StringTokenizer stz = new StringTokenizer(input, NAME_VALUE_SEPARATOR);
        int tokens = stz.countTokens();
        if (tokens == 2) {
            nv.name = stz.nextToken();
            nv.value = stz.nextToken();
            // Allow null or empty values
        } else if (tokens == 1) {
            nv.name = stz.nextToken();
            nv.value = "";
        } else {
            throw new IllegalStateException("Unexpected format for multi valued element: " + input);
        }
        return nv;
    }

    /**
     * writeDocument streams out given document to given output stream
     * @param document
     * @param os
     * @throws Exception
     */
    public static void writeDocument(Document document, OutputStream os) throws Exception {
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(os);
        transformer.transform(source, result);
    }

    /**
     * Xml to string.
     *
     * @param node the node
     * @return the string
     */
    public static String xmlToString(Node node) {
    	String result = null;
    	
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result streamResult = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, streamResult);
            result = stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        
        return result;
    }    

    /**
     * getXmlDocoument retrieve w3c.Document from given file
     * @param fileName
     * @return Document
     * @throws Exception
     */
    public static Document getXmlDocument(String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        File f = new File(fileName);
        if (!f.exists()) {
            throw new IllegalArgumentException("Test data file " + fileName + " not found!");
        }
        // Create the builder and parse the file
        return factory.newDocumentBuilder().parse(f);
    }
}
