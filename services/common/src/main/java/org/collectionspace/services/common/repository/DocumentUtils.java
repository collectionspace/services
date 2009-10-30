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
import java.util.HashMap;
import java.util.Map;
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

    /**
     * parseProperties given payload to create XML document. this
     * method also closes given stream after parsing.
     * @param payload stream
     * @return parsed Document
     * @throws Exception
     */
    public static Document parseDocument(InputStream payload)
            throws Exception {
        try{
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);//TODO take validating value from meta

            // Create the builder and parse the file
            return factory.newDocumentBuilder().parse(payload);

        }finally{
            if(payload != null){
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
        NodeList children = root.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            Node node = (Node) children.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                Node cnode = node.getFirstChild();
                if(cnode == null){
                    //if element is present but no value, set to ""
                    //FIXME what about non-string types?
                    objectProps.put(node.getNodeName(), "");
                }else{
                    if(cnode.getNodeType() != Node.TEXT_NODE){
                        continue;
                    }
                    Node textNode = (Text) cnode;
                    //FIXME what about other native xml types?
                    objectProps.put(node.getNodeName(),
                            textNode.getNodeValue());
                }
            }
        }
        return objectProps;
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
        if(xc == null){
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

        for(String prop : objectProps.keySet()){
            Object value = objectProps.get(prop);
            if(value != null){
                //no need to qualify each element name as namespace is already added
                Element e = document.createElement(prop);
                root.appendChild(e);
                String strValue = objectProps.get(prop).toString();
                Text tNode = document.createTextNode(strValue);
                e.appendChild(tNode);
            }
        }
        return document;
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
