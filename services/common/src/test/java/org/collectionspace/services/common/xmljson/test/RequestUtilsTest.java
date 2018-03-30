package org.collectionspace.services.common.xmljson.test;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import static org.collectionspace.services.common.xmljson.RequestUtils.*;
import static org.testng.Assert.*;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class RequestUtilsTest {

    @Test
    public void testIsJsonContent() {
        assertFalse(isJsonContent(requestWithContentType(null)));
        assertFalse(isJsonContent(requestWithContentType("application/xml")));
        assertFalse(isJsonContent(requestWithContentType("application/xml;charset=utf-8")));
        assertFalse(isJsonContent(requestWithContentType("application/*")));
        assertFalse(isJsonContent(requestWithContentType("*/*")));
        assertTrue(isJsonContent(requestWithContentType("application/json")));
        assertTrue(isJsonContent(requestWithContentType("application/json;charset=utf-8")));
    }
    
    @Test
    public void testIsJsonPreferred() {
        assertEquals(
                isJsonPreferred("application/json"),
                true);
        assertEquals(
                isJsonPreferred("application/xml"),
                false);
        assertEquals(
                isJsonPreferred("*/*"),
                false);
        assertEquals(
                isJsonPreferred(""),
                false);
        assertEquals(
                isJsonPreferred((String) null),
                false);
        assertEquals(
                isJsonPreferred("application/json,application/xml;q=0.9"),
                true);
        assertEquals(
                isJsonPreferred("application/json;q=0.8,application/xml;q=0.9"),
                false);
        assertEquals(
                isJsonPreferred("application/json;q=0.8,application/xml;q=0.7"),
                true);
    }

    @Test
    public void testGetXmlEnsuredAccept() {
        assertEquals(
                getXmlEnsuredAccept("application/json"),
                "application/json,application/xml;q=0.001");
        assertEquals(
                getXmlEnsuredAccept("application/xml"),
                "application/xml");
        assertEquals(
                getXmlEnsuredAccept("application/json,application/xml;q=0.9"),
                "application/json,application/xml;q=0.9");
        assertEquals(
                getXmlEnsuredAccept("application/json;q=0.8,application/xml;q=0.9"),
                "application/json;q=0.8,application/xml;q=0.9");
        assertEquals(
                getXmlEnsuredAccept("application/json;q=0.8,application/xml;q=0.7"),
                "application/json;q=0.8,application/xml;q=0.7");
        assertEquals(
                getXmlEnsuredAccept("application/xml;q=0.7,application/json;q=0.8"),
                "application/xml;q=0.7,application/json;q=0.8");
    }

    @Test
    public void testGetAccept() {
        assertEquals(
                getAccept(requestAccepting("application/json")),
                "application/json");
        assertEquals(
                getAccept(requestAccepting("application/xml")),
                "application/xml");
        assertEquals(
                getAccept(requestAccepting("application/json,application/xml")),
                "application/json,application/xml");
        assertEquals(
                getAccept(requestAccepting("application/json", "application/xml")),
                "application/json,application/xml");
        assertEquals(
                getAccept(requestAccepting("*/*", "application/xml;q=0.9", "application/json;q=0.4")),
                "*/*,application/xml;q=0.9,application/json;q=0.4");
    }

    private HttpServletRequest requestAccepting(String... accepts) {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        
        EasyMock.expect(request.getHeaders("Accept"))
            .andReturn(Collections.enumeration(Arrays.asList(accepts)));
        
        EasyMock.replay(request);
        
        return request;
    }
    
    private HttpServletRequest requestWithContentType(String contentType) {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        
        EasyMock.expect(request.getContentType())
            .andReturn(contentType);
        
        EasyMock.replay(request);
        
        return request;
    }
}
