package org.collectionspace.services.common.xmljson.test;

import static org.testng.Assert.*;

import org.collectionspace.services.common.xmljson.JsonField;
import org.testng.annotations.Test;

public class JsonFieldTest {
    @Test
    public void testJsonField() {
        JsonField field = new JsonField("name");
        
        assertEquals(field.getName(), "name");
        assertTrue(field.isScalar());
        assertFalse(field.isArray());
        assertEquals(field.getType(), JsonField.Type.SCALAR);
        
        field.setName("newName");
        
        assertEquals(field.getName(), "newName");
        
        field.setType(JsonField.Type.ARRAY);
        
        assertFalse(field.isScalar());
        assertTrue(field.isArray());
        assertEquals(field.getType(), JsonField.Type.ARRAY);
        
        field.setType(JsonField.Type.OBJECT);
        
        assertFalse(field.isScalar());
        assertFalse(field.isArray());
        assertEquals(field.getType(), JsonField.Type.OBJECT);
    }
}
