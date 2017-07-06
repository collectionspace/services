package org.collectionspace.services.common.xmljson.test;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.collectionspace.services.common.xmljson.XmlNode;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class XmlNodeTest {
    
    @Test void testConstructor() {
        XmlNode node = new XmlNode();
        
        assertNull(node.getName());

        XmlNode namedNode = new XmlNode("name");

        assertEquals(namedNode.getName(), "name");
    }
    
    @Test
    public void testXmlNodeWithText() {
        XmlNode node = new XmlNode("collectionspace_core");
        
        assertEquals(node.getName(), "collectionspace_core");
        
        node.setName("collectionobjects_common");
        
        assertEquals(node.getName(), "collectionobjects_common");
        assertTrue(node.isEmpty());
        assertFalse(node.hasChildren());
        assertFalse(node.hasText());
        assertTrue(node.isTextAllowed());
        
        node.setText("some text");
        
        assertEquals(node.getText(), "some text");
        assertTrue(node.hasText());
        
        node.addText(" and more");
        
        assertEquals(node.getText(), "some text and more");
        assertTrue(node.hasText());

        node.addAttribute("name", "value");
        node.addNamespace("ns", "http://collectionspace.org");
        
        assertEquals(node.getAttributes().get("name"), "value");
        assertEquals(node.getNamespaces().get("ns"), "http://collectionspace.org");
        
        assertEquals(node.getCombinedMap(), new HashMap<String, Object>() {{
            put("@name", "value");
            put("@xmlns:ns", "http://collectionspace.org");
        }});
        
        assertEquals(node.getValue(), "some text and more");
        
        node.setTextAllowed(false);
        
        assertFalse(node.isTextAllowed());
        assertTrue(StringUtils.isEmpty(node.getText()));
        
        node.addText("hello");

        assertTrue(StringUtils.isEmpty(node.getText()));

        node.setText("hello");

        assertTrue(StringUtils.isEmpty(node.getText()));
        assertNull(node.getValue());
    }
    
    @Test
    public void testXmlNodeWithChildren() {
        XmlNode node = new XmlNode("collectionspace_core");

        assertFalse(node.hasChildren());
        assertEquals(node.getCombinedMap(), new HashMap<String, Object>());
        assertNull(node.getValue());
        assertTrue(node.isRetainEmptyChildren());
        
        final XmlNode descNode = new XmlNode("description");
        
        node.addChild(descNode);

        assertEquals(node.getChildren(), new HashMap<String, Object>() {{
            put("description", descNode);
        }});
        
        assertEquals(node.getCombinedMap(), new HashMap<String, Object>() {{
            put("description", descNode);
        }});
        
        assertEquals(node.getValue(), new HashMap<String, Object>() {{
            put("description", descNode);
        }});
        
        assertFalse(node.isTextAllowed());
        
        node.addAttribute("name", "value");
        node.addNamespace("ns", "http://collectionspace.org");

        assertEquals(node.getCombinedMap(), new HashMap<String, Object>() {{
            put("@name", "value");
            put("@xmlns:ns", "http://collectionspace.org");
            put("description", descNode);
        }});

        assertEquals(node.getValue(), new HashMap<String, Object>() {{
            put("@name", "value");
            put("@xmlns:ns", "http://collectionspace.org");
            put("description", descNode);
        }});
        
        node.setRetainEmptyChildren(false);
        
        assertFalse(node.isRetainEmptyChildren());
        
        final XmlNode nameNode = new XmlNode("name");
        
        assertTrue(nameNode.isEmpty());
        
        node.addChild(nameNode);
        
        // Should not have been retained
        
        assertEquals(node.getChildren(), new HashMap<String, Object>() {{
            put("description", descNode);
        }});
    }
}
