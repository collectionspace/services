package org.collectionspace.services.common.xmljson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.collectionspace.services.common.xmljson.parsetree.Node;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class XmlToJsonStreamConverter {
    protected XMLEventReader eventReader;
    protected PrintWriter writer;
    protected Stack<Node> stack = new Stack<Node>();
    protected Node parseResult = null;
    
    public static String nodeNameForQName(QName name) {
        String prefix = name.getPrefix();
        String localPart = name.getLocalPart();
        
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + ":" + localPart;
        }
        
        return localPart;
    }
    
    public XmlToJsonStreamConverter(InputStream in, OutputStream out) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        
        eventReader = factory.createXMLEventReader(in);
        writer = new PrintWriter(out);
    }
    
    public void convert() throws XMLStreamException, JsonGenerationException, JsonMappingException, IOException {
        while(eventReader.hasNext()){
            XMLEvent event = eventReader.nextEvent();
            
            switch(event.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                    onCharacters(event);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    onStartElement(event);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    onEndElement(event);
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    onStartDocument(event);
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    onEndDocument(event);
                    break;
            }
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(writer, parseResult);
        
        writer.flush();
    }
    
    protected void onStartDocument(XMLEvent event) {
        stack.push(new Node());
    }

    protected void onEndDocument(XMLEvent event) {
        parseResult = stack.pop();
    }
    
    @SuppressWarnings("unchecked")
    protected void onStartElement(XMLEvent event) {
        StartElement element = event.asStartElement();
        QName name = element.getName();

        Node node = new Node(nodeNameForQName(name));

        Iterator<Attribute> attrIter = element.getAttributes();
        
        while(attrIter.hasNext()) {
            Attribute attr = attrIter.next();
            
            node.addAttribute(attr.getName().toString(), attr.getValue());
        }
        
        Iterator<Namespace> nsIter = element.getNamespaces();
        
        while(nsIter.hasNext()) {
            Namespace ns = nsIter.next();
            
            node.addNamespace(ns.getPrefix(), ns.getNamespaceURI());
        }
        
        stack.push(node);
    }
    
    protected void onCharacters(XMLEvent event) {
        String text = event.asCharacters().getData();
        Node parent = stack.peek();
        
        parent.addText(text);
    }
    
    protected void onEndElement(XMLEvent event) {
        Node node = stack.pop();
        Node parent = stack.peek();
        
        parent.addChild(node);
    }
}
