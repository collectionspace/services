package org.collectionspace.services.common.xmljson.test;

import static org.testng.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.collectionspace.services.common.xmljson.JsonToXmlStreamConverter;
import org.testng.annotations.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonToXmlStreamConverterTest {
    public final String FILE_PATH = "test-data/xmljson/";

    @Test
    public void testConvert() throws XMLStreamException, JsonParseException, IOException {
        testConvert("record");
        testConvert("collectionobject");
        testConvert("collectionobject-list");
        testConvert("accountperms");
        testConvert("permissions");
        testConvert("vocabulary-items");
        testConvert("numeric-json");
        testConvert("boolean-json");
        testConvert("single-list-item-json");
        testConvert("export-invocation");
        testConvertThrows("empty-json", XMLStreamException.class);
    }

    private void testConvert(String fileName) throws XMLStreamException, IOException {
        System.out.println("---------------------------------------------------------");
        System.out.println("Converting JSON to XML: " + fileName);
        System.out.println("---------------------------------------------------------");

        ClassLoader classLoader = getClass().getClassLoader();
        File jsonFile = new File(classLoader.getResource(FILE_PATH + fileName + ".json").getFile());
        File xmlFile = new File(classLoader.getResource(FILE_PATH + fileName + ".xml").getFile());

        FileInputStream in = new FileInputStream(jsonFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JsonToXmlStreamConverter converter = new JsonToXmlStreamConverter(in, out);
        converter.convert();

        System.out.println(out.toString("UTF-8"));

        Diff diff = DiffBuilder
                .compare(Input.fromStream(out.toInputStream()))
                .withTest(Input.fromFile(xmlFile))
                .ignoreComments()
                .ignoreWhitespace()
                .build();

        System.out.println(diff.toString());

        assertFalse(diff.hasDifferences());
    }

    private void testConvertThrows(String fileName, Class<?> exceptionClass) throws XMLStreamException, IOException {
        boolean caught = false;

        try {
            testConvert(fileName);
        }
        catch(XMLStreamException|IOException e) {
            if (e.getClass().isAssignableFrom(exceptionClass)) {
                caught = true;

                System.out.println(e.toString());
            }
            else {
                throw e;
            }
        }

        assertTrue(caught);
    }
}
