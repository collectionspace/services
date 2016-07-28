package org.collectionspace.services.common.xmljson.test;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.collectionspace.services.common.xmljson.XmlToJsonStreamConverter;
import org.testng.annotations.Test;

public class XmlToJsonStreamConverterTest {
    public final String FILE_PATH = "test-data/xmljson/";
    
    private ObjectMapper mapper = new ObjectMapper();
    private JsonFactory jsonFactory = mapper.getJsonFactory();
    
    @Test
    public void testConvert() throws XMLStreamException, JsonParseException, IOException {
        testConvert("record.xml", "record.json");
    }
    
    private void testConvert(String xmlFileName, String jsonFileName) throws XMLStreamException, JsonParseException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File xmlFile = new File(classLoader.getResource(FILE_PATH + xmlFileName).getFile());
        File jsonFile = new File(classLoader.getResource(FILE_PATH + jsonFileName).getFile());
        
        FileInputStream in = new FileInputStream(xmlFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        XmlToJsonStreamConverter converter = new XmlToJsonStreamConverter(in, out);
        converter.convert();
        
        JsonNode actualJson = parseJsonStream(out.toInputStream());
        JsonNode expectedJson = parseJsonStream(new FileInputStream(jsonFile));
        
        assertEquals(actualJson, expectedJson);
    }
    
    private JsonNode parseJsonStream(InputStream in) throws JsonParseException, IOException {
        return jsonFactory.createJsonParser(in).readValueAsTree();
    }
}
