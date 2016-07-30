package org.collectionspace.services.common.xmljson.test;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.collectionspace.services.common.xmljson.XmlToJsonStreamConverter;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class XmlToJsonStreamConverterTest {
    public final String FILE_PATH = "test-data/xmljson/";
    
    private ObjectMapper mapper = new ObjectMapper();
    private JsonFactory jsonFactory = mapper.getFactory();
    
    @Test
    public void testConvert() throws XMLStreamException, JsonParseException, IOException {
        testConvert("record");
        testConvert("collectionobject");
        testConvert("collectionobject-list");
        testConvert("accountperms");
        testConvert("permissions");
        testConvert("vocabulary-items");
    }
    
    private void testConvert(String fileName) throws XMLStreamException, JsonParseException, IOException {
        System.out.println("-------------------------------------------");
        System.out.println("Converting " + fileName);
        System.out.println("-------------------------------------------");

        ClassLoader classLoader = getClass().getClassLoader();
        File xmlFile = new File(classLoader.getResource(FILE_PATH + fileName + ".xml").getFile());
        File jsonFile = new File(classLoader.getResource(FILE_PATH + fileName + ".json").getFile());
        
        FileInputStream in = new FileInputStream(xmlFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        XmlToJsonStreamConverter converter = new XmlToJsonStreamConverter(in, out);
        converter.convert();
        
        JsonNode actualJson = parseJsonStream(out.toInputStream());
        JsonNode expectedJson = parseJsonStream(new FileInputStream(jsonFile));
        
        System.out.println(actualJson.toString());
        
        assertEquals(actualJson, expectedJson);
    }
    
    private JsonNode parseJsonStream(InputStream in) throws JsonParseException, IOException {
        return jsonFactory.createParser(in).readValueAsTree();
    }
}
