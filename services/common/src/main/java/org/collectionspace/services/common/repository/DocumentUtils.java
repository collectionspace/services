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
package org.collectionspace.services.common.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.collectionspace.services.common.service.ObjectPartContentType;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.XmlContentType;
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

    private static String NAME_VALUE_SEPARATOR = "|";

    private static class NameValue {

        String name;
        String value;
    };

    /**
     * parseProperties given payload to create XML document. this
     * method also closes given stream after parsing.
     * @param payload stream
     * @return parsed Document
     * @throws Exception
     */
    public static Document parseDocument(InputStream payload)
            throws Exception {
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);//TODO take validating value from meta

            // Create the builder and parse the file
            return factory.newDocumentBuilder().parse(payload);

        } finally {
            if (payload != null) {
                payload.close();
            }

        }
    }

    /**
     * parseProperties extract given payload (XML) into Name-Value properties. this
     * @param document to parse
     * @return map key=property name, value=property value
     * @throws Exception
     */
    public static Map<String, Object> parseProperties(Document document)
            throws Exception {
        HashMap<String, Object> objectProps = new HashMap<String, Object>();
        // Get a list of all elements in the document
        Node root = document.getFirstChild();
        NodeList rootChildren = root.getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node node = rootChildren.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList childNodes = node.getChildNodes();
                if (childNodes.getLength() > 1) {
                    //must be multi value element
                    String[] vals = getMultiValues(node);
                    objectProps.put(node.getNodeName(), vals);
                } else if (childNodes.getLength() == 1) {
                    objectProps.put(node.getNodeName(), getTextNodeValue(node));
                }
            }
        }
        return objectProps;
    }

    /**
     * getMultiValues retrieve multi-value element values
     * @param node
     * @return
     */
    private static String[] getMultiValues(Node node) {
        ArrayList<String> vals = new ArrayList<String>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node cnode = children.item(i);
            vals.add(qualify(cnode.getNodeName(), getTextNodeValue(cnode)));
        }
        return vals.toArray(new String[0]);
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
        return value;
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
        if (isQualified(name, value)) {
            return value;
        }
        return name + NAME_VALUE_SEPARATOR + value;

    }

    /**
     * buildDocument builds org.w3c.dom.Document from given properties using
     * given metadata for a part
     * @param partMeta
     * @param rootElementName
     * @param objectProps
     * @return
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

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlStandalone(true);
        //JAXB unmarshaller recognizes the following kind of namespace qualification
        //only. More investigation is needed to use other prefix
//        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
//        <ns2:collectionobjects-common xmlns:ns2="http://collectionspace.org/services/collectionobject">
//        <objectNumber>objectNumber-1252960222412</objectNumber>
//        <objectName>objectName-1252960222412</objectName>
//        </ns2:collectionobjects-common>

        String ns = "ns2";
        Element root = document.createElementNS(xc.getNamespaceURI(), ns + ":" + rootElementName);
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:schemaLocation", xc.getSchemaLocation());
        root.setAttribute("xmlns:" + ns, xc.getNamespaceURI());
        document.appendChild(root);

        for (String prop : objectProps.keySet()) {
            Object value = objectProps.get(prop);
            if (value != null) {
                //no need to qualify each element name as namespace is already added
                Element e = document.createElement(prop);
                root.appendChild(e);
                if (value instanceof ArrayList) {
                    //multi-value element
                    insertMultiValues(document, e, (ArrayList) value);
                } else {
                    String strValue = objectProps.get(prop).toString();
                    insertTextNode(document, e, strValue);
                }
            }
        }
        return document;
    }

    private static void insertMultiValues(Document document, Element e, ArrayList vals) {
        String parentName = e.getNodeName();
        for (Object o : vals) {
            String val = (String) o; //force cast
            NameValue nv = unqualify(val);
            Element c = document.createElement(nv.name);
            e.appendChild(c);
            insertTextNode(document, c, nv.value);
        }
    }

    private static void insertTextNode(Document document, Element e, String strValue) {
        Text tNode = document.createTextNode(strValue);
        e.appendChild(tNode);
    }

    /**
     * unqualify given value. if the given input value is not qualified, throw exception
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
        } else {
            throw new IllegalStateException("Found multi valued element " + input +
                    " without qualification");
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
}
