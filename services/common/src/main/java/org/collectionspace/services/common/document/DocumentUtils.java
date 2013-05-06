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

import java.lang.reflect.Array;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.datetime.DateTimeFormatUtils;
import org.collectionspace.services.config.service.ObjectPartContentType;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.XmlContentType;
import org.dom4j.io.DOMReader;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

import org.nuxeo.ecm.core.io.ExportConstants;
import org.nuxeo.common.collections.PrimitiveArrays;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.TypeConstants;
import org.nuxeo.ecm.core.schema.types.ComplexType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.SimpleType;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.core.schema.types.TypeException;
import org.nuxeo.ecm.core.schema.types.JavaTypes;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.core.schema.types.primitives.DoubleType;
import org.nuxeo.ecm.core.schema.types.primitives.StringType;
import org.nuxeo.ecm.core.schema.types.FieldImpl;
import org.nuxeo.ecm.core.schema.types.QName;
import org.nuxeo.runtime.api.Framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.Node;
//import org.dom4j.NodeList;
//import org.dom4j.Text;

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

	/** The name dateVal separator. */
	private static String NAME_VALUE_SEPARATOR = "|";

	// The delimiter in a schema-qualified field name,
	// between its schema name and field name components.
	/** The SCHEM a_ fiel d_ delimiter. */
	private static String SCHEMA_FIELD_DELIMITER = ":";

	// The delimiter between a parent authRef field name and
	// child authRef field name, if any.
	/** The AUTHRE f_ fiel d_ nam e_ delimiter. */
	private static String AUTHREF_FIELD_NAME_DELIMITER = "|";

	/** The XML elements with this suffix will indicate. */
	private static String STRUCTURED_TYPE_SUFFIX = "List";
       


	/**
	 * The Class NameValue.
	 */
	private static class NameValue {    	
		/**
		 * Instantiates a new name dateVal.
		 */
		NameValue() {
			// default scoped constructor to removed "synthetic accessor" warning
		}        
		/** The name. */
		String name;        
		/** The dateVal. */
		String value;
	};

	/**
	 * Log multipart input.
	 *
	 * @param multipartInput the multipart input
	 */
	public static void logMultipartInput(MultipartInput multipartInput) {
		if (logger.isDebugEnabled() == true) {			
			List<InputPart> parts = multipartInput.getParts();
			for (InputPart part : parts) {
				try {
					logger.debug(part.getBodyAsString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Log byte array input stream.  After logging this method resets the stream and returns it in its original state.
	 *
	 * @param inputStream the input stream
	 * @return the byte array input stream
	 */
	private static ByteArrayInputStream logByteArrayInputStream(ByteArrayInputStream inputStream) {
		ByteArrayInputStream result = inputStream;

		if (logger.isTraceEnabled() == true) {
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
			//
			// Essentially, reset the stream and return it in its original state
			//
			result = new ByteArrayInputStream(buff);
		}

		return result;
	}

	/**
	 * Gets the xML schema.
	 *
	 * @param partMeta the part meta
	 * @return the xML schema
	 * @throws Exception the exception
	 */
	private static File getXMLSchema(ObjectPartType partMeta)
	throws Exception {
		final String FILE_SEPARATOR = System.getProperty("file.separator");
		final String XML_SCHEMA_EXTENSION = ".xsd";
		final String SCHEMAS_DIR = "schemas";

		File schemaFile = null;

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
		try {
			schemaFile = new File(schemaName);
		} catch (Exception e) {
			if (logger.isWarnEnabled() == true) {
				logger.warn("Missing schema file for incoming payload: " + schemaName);
			}
		}

		return schemaFile;
	}
	
	/**
	 * parseProperties given payload to create XML document. this
	 * method also closes given stream after parsing.
	 * @param payload stream
	 * @param partMeta 
	 * @param validate - whether or not to validate the payload with an XML Schema
	 * @return parsed Document
	 * @throws Exception
	 */
	public static Document parseDocument(InputStream payload, ObjectPartType partMeta, Boolean validate)
	throws Exception {
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

		File schemaFile = null;
		if (validate == true) {
			schemaFile = getXMLSchema(partMeta);
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
			// Enable XML validation if we found an XML Schema for the payload
			//
			try {
				if (schemaFile != null) {
					factory.setValidating(true);
					factory.setNamespaceAware(true);
					factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
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
			DocumentBuilder db = factory.newDocumentBuilder();
			db.setErrorHandler(null);
			result = db.parse(payload);

			// Write it to the log so we can see what we've created.
			if (logger.isTraceEnabled() == true) {
				logger.trace(xmlToString(result));
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
	 * @return map key=property name, dateVal=property dateVal
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
				// Set the dateVal even if it's null.
				// A null dateVal implies a clear/delete of the property
				//
				objectProps.put(name, value);
			}
		}
		return objectProps;
	}

	/**
	 * getMultiStringValues retrieve multi-dateVal element values
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
	 * getMultiValues retrieve multi-dateVal element values
	 * assumption: backend does not support more than 1 level deep hierarchy
	 * @param node
	 * @return
	 */
	private static Object getMultiValues(Node node) throws Exception {
		Object result = null;    	

		Node nodeWithoutTextNodes = removeTextNodes(node);
		NodeList children = nodeWithoutTextNodes.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {

			Node grandChild = children.item(j).getFirstChild();

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
	 * getTextNodeValue retrieves text node dateVal
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
	 * isQualified check if the given dateVal is already qualified with given
     * property name e.g. (in the example of a former 'otherNumber' field in
     * CollectionObject) otherNumber|urn:org.collectionspace.id:24082390 is
     * qualified with otherNumber but urn:org.walkerart.id:123 is not qualified
	 * @param name of the property, e.g. otherNumber
	 * @param dateVal of the property e.g. otherNumber
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
	 * qualify qualifies given property dateVal with given property name, e.g.
	 * name=otherNumber and dateVal=urn:org.collectionspace.id:24082390 would be
	 * qualified as otherNumber|urn:org.collectionspace.id:24082390. however,
	 * name=otherNumber and dateVal=otherNumber|urn:org.collectionspace.id:24082390
	 * would be ignored as the given dateVal is already qualified once.
	 * @param name
	 * @param dateVal
	 * @return
	 */
	private static String qualify(String name, String value) {
		/*
        String result = null;
        if (isQualified(name, dateVal)) {
            result = dateVal;
        } else {
        	result = name + NAME_VALUE_SEPARATOR + dateVal;
        }
        return result;
		 */
		return value;
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
	public static org.dom4j.Element buildDocument(ObjectPartType partMeta, String rootElementName,
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
		document.setXmlStandalone(true); //FIXME: REM - Can we set this to false since it is not really standalone?

		/*
		 * JAXB unmarshaller recognizes the following kind of namespace
		 * qualification only. More investigation is needed to use other prefix
		 * 
		 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		 * <ns2:collectionobjects-common xmlns:ns2="http://collectionspace.org/services/collectionobject">
		 * 		<objectNumber>objectNumber-1252960222412</objectNumber>
		 * 		<objectName>objectName-1252960222412</objectName>
		 * </ns2:collectionobjects-common>
		 */

		String ns = "ns2";
		Element root = document.createElementNS(xc.getNamespaceURI(), ns + ":" + rootElementName);
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
//		String getSchemaLocation = xc.getSchemaLocation();					//FIXME: REM - w3c Document to DOM4j Document mangles this attribute
//		root.setAttribute("xsi:schemaLocation", xc.getSchemaLocation());
		
		String getNamespaceURI = xc.getNamespaceURI();
		root.setAttribute("xmlns:" + ns, xc.getNamespaceURI());
		document.appendChild(root);

		Schema schema = getSchemaFromName(partMeta.getLabel());

		buildDocument(document, root, objectProps, schema);
		String w3cDocumentStr = xmlToString(document);
		

		DOMReader reader = new DOMReader();
		org.dom4j.Document dom4jDoc = reader.read(document);
		org.dom4j.Element result = dom4jDoc.getRootElement();
		result.detach(); //return just the root element detached from the DOM document
		
		return result;//FIXME: REM - Add if (logger.isTraceEnabled() == true) logger.trace(document.asXML());
	}

	/**
	 * Builds the document.
	 *
	 * @param document the document
	 * @param e the e
	 * @param objectProps the object props
	 * @throws Exception the exception
	 */
	public static void buildDocument(Document document, Element parent,
			Map<String, Object> objectProps, Schema schema) throws Exception {
		for (String prop : objectProps.keySet()) {
			Object value = objectProps.get(prop);
			if (value != null) {
				Field field = schema.getField(prop);
				// If there is no field, then we added this property to the properties, 
				// and it must be a String (e.g., CSID)
				// TODO - infer the type from the type of Object, if we need more than String
				if(field==null) {
					field = new FieldImpl(new QName(prop), schema, StringType.INSTANCE);
				}
				buildProperty(document, parent, field, value);
			}
		}
	}

	/**
	 * Builds the property.
	 *
	 * @param document the document
	 * @param parent the parent
	 * @param field the field
	 * @param dateVal the dateVal
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void buildProperty(Document document, Element parent, 
            Field field, Object value) throws IOException {
            Type type = field.getType(); 
           //no need to qualify each element name as namespace is already added
            String propName = field.getName().getLocalName();
            Element element = document.createElement(propName);
            parent.appendChild(element);
            // extract the element content
            if (type.isSimpleType()) {
                // Avoid returning scientific notation representations of
                // very large or very small decimal values. See CSPACE-4691.
                if (isNuxeoDecimalType(type) && valueMatchesNuxeoType(type, value)) {
                    element.setTextContent(nuxeoDecimalValueToDecimalString(value));
               /*
                * We need a way to produce just a Date when the specified data
                * type is an xs:date vs. xs:datetime. Nuxeo maps both to a Calendar. Sigh.
                if(logger.isTraceEnabled() && isDateType(type)) {
                    String dateValType = "unknown";
                    if (value instanceof java.util.Date) {
                        dateValType = "java.util.Date";
                    } else if (value instanceof java.util.Calendar) {
                        dateValType = "java.util.Calendar";
                    }
                    logger.trace("building XML for date type: "+type.getName()
                            +" value type: "+dateValType
                            +" encoded: "+encodedVal);
                }
                */
                } else {
                    String encodedVal = type.encode(value);
                    element.setTextContent(encodedVal);
                }
            } else if (type.isComplexType()) {
                ComplexType ctype = (ComplexType) type;
                if (ctype.getName().equals(TypeConstants.CONTENT)) {
                    throw new RuntimeException(
                            "Unexpected schema type: BLOB for field: "+propName);
                } else {
                    buildComplex(document, element, ctype, (Map) value);
                }
            } else if (type.isListType()) {
                if (value instanceof List) {
                    buildList(document, element, (ListType) type, (List) value);
                } else if (value.getClass().getComponentType() != null) {
                    buildList(document, element, (ListType) type,
                            PrimitiveArrays.toList(value));
                } else {
                    throw new IllegalArgumentException(
                            "A value of list type is neither list neither array: "
                            + value);
                }
            }
        }

	/**
	 * Builds the complex.
	 *
	 * @param document the document
	 * @param element the element
	 * @param ctype the ctype
	 * @param map the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void buildComplex(Document document, Element element, 
			ComplexType ctype, Map map) throws IOException {
		Iterator<Map.Entry> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = it.next();
			String propName = entry.getKey().toString();
			buildProperty(document, element, 
					ctype.getField(propName), entry.getValue());
		}
	}

	/**
	 * Builds the list.
	 *
	 * @param document the document
	 * @param element the element
	 * @param ltype the ltype
	 * @param list the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void buildList(Document document, Element element, 
			ListType ltype, List list) throws IOException {
		Field field = ltype.getField();
		for (Object obj : list) {
			buildProperty(document, element, field, obj);
		}
	}

	/**
	 * Returns a schema, given the name of a schema.
	 *
	 * @param schemaName  a schema name.
	 * @return  a schema.
	 */
	public static Schema getSchemaFromName(String schemaName) {
		SchemaManager schemaManager = Framework.getLocalService(SchemaManager.class);
		return schemaManager.getSchema(schemaName);
	}

	/**
	 * Returns the schema part of a presumably schema-qualified field name.
	 *
	 * If the schema part is null or empty, returns an empty string.
	 *
	 * @param schemaQualifiedFieldName  a schema-qualified field name.
	 * @return  the schema part of the field name.
	 */
	//FIXME: Might instead use Nuxeo's built-in QName class.
	public static String getSchemaNamePart(String schemaQualifiedFieldName) {
		if (schemaQualifiedFieldName == null || schemaQualifiedFieldName.trim().isEmpty()) {
			return "";
		}
		if (schemaQualifiedFieldName.indexOf(SCHEMA_FIELD_DELIMITER) > 0) {
			String[] schemaQualifiedFieldNameParts =
				schemaQualifiedFieldName.split(SCHEMA_FIELD_DELIMITER);
			String schemaName = schemaQualifiedFieldNameParts[0];
			return schemaName;
		} else {
			return "";
		}
	}

	/**
	 * Returns a list of delimited strings, by splitting the supplied string
	 * on a supplied delimiter.
	 *
	 * @param str  A string to split on a delimiter.
	 * @param delmiter  A delimiter on which the string will be split into parts.
	 * 
	 * @return  A list of delimited strings.  Returns an empty list if either
	 * the string or delimiter are null or empty, or if the delimiter cannot
	 * be found in the string.
	 */
	public static List<String> getDelimitedParts(String str, String delimiter) {
		List<String> parts = new ArrayList<String>();
		if (str == null || str.trim().isEmpty()) {
			return parts;
		}
		if (delimiter == null || delimiter.trim().isEmpty()) {
			return parts;
		}
		StringTokenizer stz = new StringTokenizer(str, delimiter);
		while (stz.hasMoreTokens()) {
			parts.add(stz.nextToken());
		}
		return parts;
	}

	/**
	 * Gets the ancestor auth ref field name.
	 *
	 * @param str the str
	 * @return the ancestor auth ref field name
	 */
	public static String getAncestorAuthRefFieldName(String str) {
		List<String> parts = getDelimitedParts(str, AUTHREF_FIELD_NAME_DELIMITER);
		if (parts.size() > 0) {
			return parts.get(0).trim();
		} else {
			return str;
		}
	}

	/**
	 * Gets the descendant auth ref field name.
	 *
	 * @param str the str
	 * @return the descendant auth ref field name
	 */
	public static String getDescendantAuthRefFieldName(String str) {
		List<String> parts = getDelimitedParts(str, AUTHREF_FIELD_NAME_DELIMITER);
		if (parts.size() > 1) {
			return parts.get(1).trim();
		} else {
			return str;
		}
	}

	/**
	 * Returns the relevant authRef field name from a fieldName, which may
	 * potentially be in the form of a single field name, or a delimited pair
	 * of field names, that in turn consists of an ancestor field name and a
	 * descendant field name.
	 *
	 * If a delimited pair of names is provided, will return the descendant
	 * field name from that pair, if present.  Otherwise, will return the
	 * ancestor name from that pair.
	 *
	 * Will return the relevant authRef field name as schema-qualified
	 * (i.e. schema prefixed), if the schema name was present, either in
	 * the supplied simple field name or in the ancestor name in the
	 * delimited pair of names.
	 *
	 * @param fieldNameOrNames  A field name or delimited pair of field names.
	 *
	 * @return The relevant authRef field name, as described.
	 */
	public static String getDescendantOrAncestor(String fieldNameOrNames) {
		String fName = "";
		if (fieldNameOrNames == null || fieldNameOrNames.trim().isEmpty()) {
			return fName;
		}
		String descendantAuthRefFieldName = getDescendantAuthRefFieldName(fieldNameOrNames);
		if (descendantAuthRefFieldName != null && !descendantAuthRefFieldName.trim().isEmpty()) {
			fName = descendantAuthRefFieldName;
		} else {
			fName = getAncestorAuthRefFieldName(fieldNameOrNames);
		}
		if (getSchemaNamePart(fName).isEmpty()) {
			String schemaName = getSchemaNamePart(getAncestorAuthRefFieldName(fieldNameOrNames));
			if (! schemaName.trim().isEmpty()) {
				fName = appendSchemaName(schemaName, fName);
			}
		}
		return fName;
	}


	/**
	 * Returns a schema-qualified field name, given a schema name and field name.
	 *
	 * If the schema name is null or empty, returns the supplied field name.
	 *
	 * @param schemaName  a schema name.
	 * @param fieldName  a field name.
	 * @return  a schema-qualified field name.
	 */
	public static String appendSchemaName(String schemaName, String fieldName) {
		if (schemaName == null || schemaName.trim().isEmpty()) {
			return fieldName;
		}
		return schemaName + SCHEMA_FIELD_DELIMITER + fieldName;
	}

	/**
	 * Checks if is simple type.
	 *
	 * @param prop the prop
	 * @return true, if is simple type
	 */
	public static boolean isSimpleType(Property prop) {
		boolean isSimple = false;
		if (prop == null) {
			return isSimple;
		}
		if (prop.getType().isSimpleType()) {
			isSimple = true;
		}
		return isSimple;
	}

	/**
	 * Checks if is list type.
	 *
	 * @param prop the prop
	 * @return true, if is list type
	 */
	public static boolean isListType(Property prop) {
		// TODO simplify this to return (prop!=null && prop.getType().isListType());
		boolean isList = false;
		if (prop == null) {
			return isList;
		}
		if (prop.getType().isListType()) {
			isList = true;
		}
		return isList;
	}

	/**
	 * Checks if is complex type.
	 *
	 * @param prop the prop
	 * @return true, if is complex type
	 */
	public static boolean isComplexType(Property prop) {
		// TODO simplify this to return (prop!=null && prop.getType().isComplexType());
		boolean isComplex = false;
		if (prop == null) {
			return isComplex;
		}
		if (prop.getType().isComplexType()) {
			isComplex = true;
		}
		return isComplex;
	}
        
        /*
         * Identifies whether a property type is a Nuxeo decimal type.
         * 
         * Note: currently, elements declared as xs:decimal in XSD schemas
         * are handled as the Nuxeo primitive DoubleType.  If this type
         * changes, the test below should be changed accordingly.
         *
         * @param type   a type.
	 * @return       true, if is a Nuxeo decimal type;
         *               false, if it is not a Nuxeo decimal type.
         */
        private static boolean isNuxeoDecimalType(Type type) {
            return ((SimpleType)type).getPrimitiveType() instanceof DoubleType;
        }
        
        /**
         * Obtains a String representation of a Nuxeo property value, where
         * the latter is an opaque Object that may or may not be directly
         * convertible to a string.
         * 
         * @param obj an Object containing a property value
         * @param docModel the document model associated with this property.
         * @param propertyPath a path to the property, such as a property name, XPath, etc. 
         * @return a String representation of the Nuxeo property value.
         */
        static public String propertyValueAsString(Object obj, DocumentModel docModel, String propertyPath) {
            if (obj == null) {
                return "";
            }
            if (String.class.isAssignableFrom(obj.getClass())) {
                return (String)obj;
            } else {
                // Handle cases where a property value returned from the repository
                // can't be directly cast to a String.
                //
                // FIXME: This method provides specific, hard-coded formatting
                // for String representations of property values. We might want
                // to add the ability to specify these formats via configuration.
                // - ADR 2013-04-26
                if (obj instanceof GregorianCalendar) {
                    return GregorianCalendarDateTimeUtils.formatAsISO8601Date((GregorianCalendar)obj);
                } else if (obj instanceof Double) {
                    return nuxeoDecimalValueToDecimalString(obj);
                } else {
                   logger.warn("Could not convert value of property " + propertyPath
                            + " in document " + docModel.getPathAsString() + " to a String.");
                   logger.warn("This may be due to a new, as-yet-unhandled datatype returned from the repository");
                   return "";
                }
            }
        }
        
        /*
         * Returns a string representation of the value of a Nuxeo decimal type.
         * 
         * Note: currently, elements declared as xs:decimal in XSD schemas
         * are handled as the Nuxeo primitive DoubleType, and their values
         * are convertible into the Java Double type.  If this type
         * changes, the conversion below should be changed accordingly.
         *
	 * @return  a string representation of the value of a Nuxeo decimal type.
         *     An empty string is returned if the value cannot be cast to the
         *     appropriate type.
         */
        private static String nuxeoDecimalValueToDecimalString(Object value) {
            Double doubleVal;
            try {
                doubleVal = (Double) value;
            } catch (ClassCastException cce) {
                logger.warn("Could not convert a Nuxeo decimal value to its string equivalent: "
                        + cce.getMessage());
                return "";
            }
            // FIXME: Without a Locale supplied as an argument, NumberFormat will
            // use the decimal separator and other numeric formatting conventions
            // for the current default Locale.  In some Locales, this could
            // potentially result in returning values that might not be capable
            // of being round-tripped; this should be invetigated. Alternately,
            // we might standardize on a particular locale whose values are known
            // to be capable of also being ingested on return. - ADR 2012-08-07
            NumberFormat formatter = NumberFormat.getInstance();
            if (formatter instanceof DecimalFormat) {
                // This pattern allows up to 15 decimal digits, and will prepend
                // a '0' if only fractional digits are included in the value.
                ((DecimalFormat) formatter).applyPattern("0.###############");
            }
            return formatter.format(doubleVal.doubleValue());
       }

        /*
         * Identifies whether a property type is a date type.
         *
         * @param type   a type.
	 * @return       true, if is a date type;
         *               false, if it is not a date type.
         */
        private static boolean isNuxeoDateType(Type type) {
            return ((SimpleType)type).getPrimitiveType() instanceof DateType;
        }
                
        private static boolean valueMatchesNuxeoType(Type type, Object value) {
            try {
                return type.validate(value);
            } catch (TypeException te) {
                return false;
            }
        }
        
	/**
	 * Insert multi values.
	 *
	 * @param document the document
	 * @param e the e
	 * @param vals the vals
    private static void insertMultiStringValues(Document document, Element e, ArrayList<String> vals) {
        String parentName = e.getNodeName();

        for (String o : vals) {
            String val = o;
            NameValue nv = unqualify(val);
            Element c = document.createElement(nv.name);
            e.appendChild(c);
            insertTextNode(document, c, nv.dateVal);
        }
    }
	 */

	/**
	 * Create a set of complex/structured elements from an array of Maps.
	 *
	 * @param document the document
	 * @param e the e
	 * @param vals the vals
	 * @throws Exception the exception
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
	 */

	/**
	 * Create a set of elements for an array of values.  Currently, we're assuming the
	 * values can be only of type Map or String.
	 *
	 * @param document the document
	 * @param e the e
	 * @param vals the vals
	 * @throws Exception the exception
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
	 */

	/**
	 * Insert text node.
	 *
	 * @param document the document
	 * @param e the e
	 * @param strValue the str dateVal
    private static void insertTextNode(Document document, Element e, String strValue) {
        Text tNode = document.createTextNode(strValue);
        e.appendChild(tNode);
    }
	 */

	/**
	 * unqualify given dateVal.
	 * input of otherNumber|urn:org.collectionspace.id:24082390 would be unqualified
	 * as name=otherNumber and dateVal=urn:org.collectionspace.id:24082390
	 * @param input
	 * @return name and dateVal
	 * @exception IllegalStateException
    private static NameValue unqualify(String input) {
        NameValue nv = new NameValue();
        StringTokenizer stz = new StringTokenizer(input, NAME_VALUE_SEPARATOR);
        int tokens = stz.countTokens();
        if (tokens == 2) {
            nv.name = stz.nextToken();
            nv.dateVal = stz.nextToken();
            // Allow null or empty values
        } else if (tokens == 1) {
            nv.name = stz.nextToken();
            nv.dateVal = "";
        } else {
            throw new IllegalStateException("Unexpected format for multi valued element: " + input);
        }
        return nv;
    }
	 */
        
    public static String getFirstString(Object list) {
    	if (list==null) {
    		return null;
    	}
    	if (list instanceof List) {
			return ((List)list).size()==0?null:(String)((List)list).get(0);
		}
    	Class<?> arrType = list.getClass().getComponentType();
    	if ((arrType != null) && arrType.isPrimitive()) {
            if (arrType == Integer.TYPE) {
                int[] ar = (int[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            } else if (arrType == Long.TYPE) {
                long[] ar = (long[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            } else if (arrType == Double.TYPE) {
                double[] ar = (double[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            } else if (arrType == Float.TYPE) {
                float[] ar = (float[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            } else if (arrType == Character.TYPE) {
                char[] ar = (char[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            } else if (arrType == Byte.TYPE) {
                byte[] ar = (byte[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            } else if (arrType == Short.TYPE) {
                short[] ar = (short[]) list;
                return ar.length==0?null:String.valueOf(ar[0]);
            }
    		throw new IllegalArgumentException(
    				"Primitive list of unsupported type: "
    				+ list);
		}
		throw new IllegalArgumentException(
				"A value of list type is neither list neither array: "
				+ list);
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
	
	//
	// Added from Nuxeo sources for creating new DocumentModel from an XML payload
	//
	
	/**
	 * Parses the properties.
	 *
	 * @param partMeta the part meta
	 * @param document the document
	 * @return the map
	 */
	public static Map<String, Object> parseProperties(ObjectPartType partMeta,
			org.dom4j.Element document, ServiceContext ctx) throws Exception {
		Map<String, Object> result = null;
		String schemaName = partMeta.getLabel();
		Schema schema = getSchemaFromName(schemaName);

		//		org.dom4j.io.DOMReader xmlReader = new org.dom4j.io.DOMReader();
		//		org.dom4j.Document dom4jDocument = xmlReader.read(document);
		try {
			//                    result = loadSchema(schema, dom4jDocument.getRootElement(), ctx);
			result = loadSchema(schema, document, ctx);
                } catch (IllegalArgumentException iae) {
                    throw iae;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	public static Map<String, Object> parseProperties(String schemaName, org.dom4j.Element element, ServiceContext ctx) throws Exception {
		Map<String, Object> result = null;
		Schema schema = getSchemaFromName(schemaName);
		result = DocumentUtils.loadSchema(schema, element, ctx);
		return result;
	}
	
	/**
	 * Load schema.
	 *
	 * @param schema the schema
	 * @param schemaElement the schema element
	 * @return the map
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	static private Map<String, Object> loadSchema(Schema schema, org.dom4j.Element schemaElement, ServiceContext ctx)
	throws Exception {
		String schemaName1 = schemaElement.attributeValue(ExportConstants.NAME_ATTR);//FIXME: Do we need this local var?
		String schemaName = schema.getName();

		Map<String, Object> data = new HashMap<String, Object>();
		Iterator<org.dom4j.Element> it = schemaElement.elementIterator();
		while (it.hasNext()) {
			org.dom4j.Element element = it.next();
			String name = element.getName();
			Field field = schema.getField(name);
			if (field != null) {
				Object value = getElementData(element, field.getType(), ctx);
				data.put(name, value);
			} else	{
                            // FIXME: substitute an appropriate constant for "csid" below.
                            // One potential class to which to add that constant, if it is not already
                            // declared, might be AbstractCollectionSpaceResourceImpl - ADR 2012-09-24
                            if (! name.equals("csid")) { // 'csid' elements in input payloads can be safely ignored. 
                                logger.warn("Invalid input document. No such property was found [" +
							name + "] in schema " + schemaName);
				}
			}
		}

		return data;
	}

	/**
	 * Gets the element data.
	 *
	 * @param element the element
	 * @param type the type
	 * @return the element data
	 */
	@SuppressWarnings("unchecked")
	static private Object getElementData(org.dom4j.Element element, Type type,
                ServiceContext ctx) throws Exception {
		Object result = null;
                String dateStr = "";
		
		if (type.isSimpleType()) {
                        if (isNuxeoDateType(type)) {
                            String dateVal = element.getText();
                            if (dateVal == null || dateVal.trim().isEmpty()) {
                                result = type.decode("");
                            } else {
                                // Dates or date/times in any ISO 8601-based representations
                                // directly supported by Nuxeo will be successfully decoded.
                                result = type.decode(dateVal);
                                // All other date or date/time values must first be converted
                                // to a supported ISO 8601-based representation.
                                if (result == null) {
                                    dateStr = DateTimeFormatUtils.toIso8601Timestamp(dateVal,
                                            ctx.getTenantId());
                                    if (dateStr != null) {
                                        result = type.decode(dateStr);
                                    } else {
                                        throw new IllegalArgumentException("Unrecognized date value '"
                                                + dateVal + "' in field '" + element.getName() + "'");
                                    }
                                }
                            }
                        } else {
                            String textValue = element.getText();
                            if (textValue != null && textValue.trim().isEmpty()) {
                                result = null;
                            } else {
			        result = type.decode(textValue);
                            }
                        }
		} else if (type.isListType()) {
			ListType ltype = (ListType) type;
			List<Object> list = new ArrayList<Object>();
			Iterator<org.dom4j.Element> it = element.elementIterator();
			while (it.hasNext()) {
				org.dom4j.Element el = it.next();
				list.add(getElementData(el, ltype.getFieldType(), ctx));
			}
			Type ftype = ltype.getFieldType();
			if (ftype.isSimpleType()) { // these are stored as arrays
				Class klass = JavaTypes.getClass(ftype);
				if (klass.isPrimitive()) {
					return PrimitiveArrays.toPrimitiveArray(list, klass);
				} else {
					return list.toArray((Object[])Array.newInstance(klass, list.size()));
				}
			}
			result = list;
		} else {
			ComplexType ctype = (ComplexType) type;
			if (ctype.getName().equals(TypeConstants.CONTENT)) {
//				String mimeType = element.elementText(ExportConstants.BLOB_MIME_TYPE);
//				String encoding = element.elementText(ExportConstants.BLOB_ENCODING);
//				String content = element.elementTextTrim(ExportConstants.BLOB_DATA);
//				if ((content == null || content.length() == 0)
//						&& (mimeType == null || mimeType.length() == 0)) {
//					return null; // remove blob
//				}
//				Blob blob = null;
//				if (xdoc.hasExternalBlobs()) {
//					blob = xdoc.getBlob(content);
//				}
//				if (blob == null) { // may be the blob is embedded like a Base64
//					// encoded data
//					byte[] bytes = Base64.decode(content);
//					blob = new StreamingBlob(new ByteArraySource(bytes));
//				}
//				blob.setMimeType(mimeType);
//				blob.setEncoding(encoding);
//				return blob;
			} else { // a complex type
				Map<String, Object> map = new HashMap<String, Object>();
				Iterator<org.dom4j.Element> it = element.elementIterator();
				while (it.hasNext()) {
					org.dom4j.Element el = it.next();
					String name = el.getName();
					Object value = getElementData(el, ctype.getField(
							el.getName()).getType(), ctx);
					map.put(name, value);
				}
				result = map;
			}
		}		
		return result;
	}	
}
