package org.collectionspace.services.common.xmljson.test;

import static org.collectionspace.services.common.xmljson.ConversionUtils.*;
import static org.testng.Assert.*;

import javax.xml.namespace.QName;

import org.testng.annotations.Test;

public class ConversionUtilsTest {

    @Test
    public void testXmlAttributeNameToJsonFieldName() {
        assertEquals(xmlAttributeNameToJsonFieldName("xyz"), "@xyz");
    }
    
    @Test
    public void testJsonFieldNameToXmlAttributeName() {
        assertEquals(jsonFieldNameToXmlAttributeName("@xyz"), "xyz");
    }
    
    @Test
    public void testXmlNamespacePrefixToJsonFieldName() {
        assertEquals(xmlNamespacePrefixToJsonFieldName("ns2"), "@xmlns:ns2");
    }

    @Test
    public void testJsonFieldNameToXmlNamespacePrefix() {
        assertEquals(jsonFieldNameToXmlNamespacePrefix("@xmlns:ns2"), "ns2");
    }
    
    @Test
    public void testIsXmlAttribute() {
        assertTrue(isXmlAttribute("@name"));
        assertTrue(isXmlAttribute("@xmlns:hello"));

        assertFalse(isXmlAttribute("name"));
        assertFalse(isXmlAttribute("xmlns:hello"));
    }
    
    @Test
    public void testIsXmlNamespace() {
        assertTrue(isXmlNamespace("@xmlns:hello"));

        assertFalse(isXmlNamespace("@name"));
        assertFalse(isXmlNamespace("name"));
        assertFalse(isXmlNamespace("xmlns:hello"));
    }
    
    @Test
    public void testJsonFieldNameFromXMLQName() {
        assertEquals(jsonFieldNameFromXMLQName(new QName("foo")), "foo");
        assertEquals(jsonFieldNameFromXMLQName(new QName("http://foo.com", "foo", "ns")), "ns:foo");
    }
}
