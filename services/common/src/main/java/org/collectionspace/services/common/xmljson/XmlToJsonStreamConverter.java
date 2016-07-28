package org.collectionspace.services.common.xmljson;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

public class XmlToJsonStreamConverter {
    protected XMLEventReader eventReader;
    protected PrintWriter writer;
    
    public XmlToJsonStreamConverter(InputStream in, OutputStream out) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        
        this.eventReader = factory.createXMLEventReader(in);
        this.writer = new PrintWriter(out);
    }
    
    public void convert() throws XMLStreamException {
        writer.print("{\"foo\": \"bar\"}");
        writer.flush();
//        while(eventReader.hasNext()){
//            XMLEvent event = eventReader.nextEvent();
//            
//            switch(event.getEventType()) {
//            
//            }
//        }
    }
}
