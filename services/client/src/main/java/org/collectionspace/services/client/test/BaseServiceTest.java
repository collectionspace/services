/**	
 * BaseServiceTest.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.collectionspace.services.client.TestServiceClient;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.w3c.dom.Document;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
 * The Class BaseServiceTest.
 */
public abstract class BaseServiceTest {

    /** The Constant logger. */
    static protected final Logger logger = LoggerFactory.getLogger(BaseServiceTest.class);
    
    /** The Constant serviceClient. */
    protected static final TestServiceClient serviceClient = new TestServiceClient();
    
    /** The NO n_ existen t_ id. */
    protected final String NON_EXISTENT_ID = createNonExistentIdentifier();
    
    /** The EXPECTE d_ statu s_ code. */
    protected int EXPECTED_STATUS_CODE = 0;
    
    /** The REQUES t_ type. */
    protected ServiceRequestType REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
    
    /** The Constant XML_DECLARATION. */
    protected static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    
    /** The Constant MALFORMED_XML_DATA. */
    protected static final String MALFORMED_XML_DATA = XML_DECLARATION
            + "<malformed_xml>wrong schema contents</malformed_xml";
    
    /** The WRON g_ xm l_ schem a_ data. */
    protected final String WRONG_XML_SCHEMA_DATA = XML_DECLARATION
            + "<wrong_schema>wrong schema contents</wrong_schema>";
    
    /** The NUL l_ charset. */
    final String NULL_CHARSET = null;

    /**
     * Gets the client.
     *
     * @return the client
     */
    abstract protected CollectionSpaceClient getClientInstance();
    
	/**
	 * Gets the abstract common list.
	 *
	 * @param response the response
	 * @return the abstract common list
	 */
	abstract protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response);    
    
    /**
     * Returns the name of the currently running test.
     *
     * Note: although the return type is listed as Object[][],
     * this method instead returns a String.
     *
     * @param   m  The currently running test method.
     *
     * @return  The name of the currently running test method.
     */
    @DataProvider(name = "testName")
    public static Object[][] testName(Method m) {
        return new Object[][]{
                    new Object[]{m.getName()}
                };
    }

    /**
     * Returns the URL path component of the service.
     *
     * This component will follow directly after the
     * base path, if any.
     *
     * @return The URL path component of the service.
     */
    protected abstract String getServicePathComponent();

    /**
     * Reinitializes setup values, to help expose any unintended reuse
     * of those values between tests.
     */
    protected void clearSetup() {
        EXPECTED_STATUS_CODE = 0;
        REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
    }

    /**
     * Initializes setup valuesfor a given test.
     */
    protected void testSetup(
            int expectedStatusCode,
            ServiceRequestType reqType,
            String bannerLabel) {
        clearSetup();
        EXPECTED_STATUS_CODE = expectedStatusCode;
        REQUEST_TYPE = reqType;
        // Print a banner identifying the test that will be run.
        if (logger.isDebugEnabled()) {
            banner(bannerLabel);
        }
    }

    /**
     * Instantiates a new base service test.
     */
    public BaseServiceTest() {
        super();
    }

    /**
     * Returns an error message indicating that the status code returned by a
     * specific call to a service does not fall within a set of valid status
     * codes for that service.
     *
     * @param serviceRequestType  A type of service request (e.g. CREATE, DELETE).
     *
     * @param statusCode  The invalid status code that was returned in the response,
     *                    from submitting that type of request to the service.
     *
     * @return An error message.
     */
    protected String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode
                + "' in response is NOT within the expected set: "
                + requestType.validStatusCodesAsString();
    }

    /**
     * Returns the root URL for a service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component (or components) for a service.
     *
     * @return The root URL for a service.
     */
    protected String getServiceRootURL() {
        return serviceClient.getBaseURL() + getServicePathComponent();
    }

    /**
     * Returns the URL of a specific resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  resourceIdentifier  An identifier (such as a UUID) for a resource.
     *
     * @return The URL of a specific resource managed by a service.
     */
    protected String getResourceURL(String resourceIdentifier) {
        return getServiceRootURL() + "/" + resourceIdentifier;
    }

    /**
     * Submits an HTTP request to a specified URL, and returns the
     * status code of the response.  Currently accepts GET and DELETE
     * requests.
     *
     * @param  method  An HTTP method.
     *
     * @param  url     A String representation of a URL.
     *
     * @return The status code received in the HTTP response.
     */
    protected int submitRequest(String method, String url) {
        int statusCode = 0;
        try {
            TestServiceClient client = new TestServiceClient();
            if (method.equals(javax.ws.rs.HttpMethod.DELETE)) {
                DeleteMethod deleteMethod = new DeleteMethod(url);
                statusCode = client.getHttpClient().executeMethod(deleteMethod);
            } else if (method.equals(javax.ws.rs.HttpMethod.GET)) {
                GetMethod getMethod = new GetMethod(url);
                statusCode = client.getHttpClient().executeMethod(getMethod);
            } else {
                // Do nothing - leave status code at default value.
            }
        } catch (Exception e) {
            logger.error(
                    "Exception during HTTP " + method + " request to "
                    + url + ":", e);
        }
        return statusCode;
    }

    /**
     * Submits an HTTP request to a specified URL, with the submitted
     * entity body, and returns the status code of the response.
     * Currently accepts POST and PUT requests.
     *
     * @param  method  An HTTP method.
     *
     * @param  url     A String representation of a URL.
     *
     * @param  mediaType  The media type of the entity body to be submitted.
     *
     * @param  entity     The contents of the entity body to be submitted.
     *
     * @return The status code received in the HTTP response.
     */
    protected int submitRequest(String method, String url, String mediaType,
            String entityStr) {
        int statusCode = 0;
        try {
            TestServiceClient client = new TestServiceClient();
            if (method.equals(javax.ws.rs.HttpMethod.POST)) {
                StringRequestEntity entityBody =
                        new StringRequestEntity(mediaType, entityStr, NULL_CHARSET);
                PostMethod postMethod = new PostMethod(url);
                postMethod.setRequestEntity(entityBody);
                statusCode = client.getHttpClient().executeMethod(postMethod);
            } else if (method.equals(javax.ws.rs.HttpMethod.PUT)) {
                StringRequestEntity entityBody =
                        new StringRequestEntity(mediaType, entityStr, NULL_CHARSET);
                PutMethod putMethod = new PutMethod(url);
                putMethod.setRequestEntity(entityBody);
                statusCode = client.getHttpClient().executeMethod(putMethod);
            } else {
                // Do nothing - leave status code at default value.
            }
        } catch (Exception e) {
            logger.error(
                    "Exception during HTTP " + method + " request to "
                    + url + ":", e);
        }
        return statusCode;
    }

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    static protected String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        if (logger.isDebugEnabled()) {
            logger.debug("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if (logger.isDebugEnabled()) {
            logger.debug("id=" + id);
        }
        return id;
    }

    /**
     * Creates the identifier.
     *
     * @return the string
     */
    static protected String createIdentifier() {
        long identifier = System.currentTimeMillis();
        return Long.toString(identifier);
    }

    /**
     * Creates the non existent identifier.
     *
     * @return the string
     */
    protected String createNonExistentIdentifier() {
        return Long.toString(Long.MAX_VALUE);
    }

    /**
     * Extract part.
     *
     * @param input the input
     * @param label the label
     * @param clazz the clazz
     * @return the object
     * @throws Exception the exception
     */
    static protected Object extractPart(MultipartInput input, String label, Class clazz)
            throws Exception {
        Object obj = null;
        String partLabel = "";
        List<InputPart> parts = input.getParts();
        if (parts.size() == 0) {
            logger.warn("No parts found in multipart body.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Parts:");
            for (InputPart part : parts) {
                partLabel = part.getHeaders().getFirst("label");
                logger.debug("part = " + partLabel);
            }
        }
        boolean partLabelMatched = false;
        for (InputPart part : parts) {
            partLabel = part.getHeaders().getFirst("label");
            if (label.equalsIgnoreCase(partLabel)) {
                partLabelMatched = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("found part" + partLabel);
                }
                String partStr = part.getBodyAsString();
                if (partStr == null || partStr.trim().isEmpty()) {
                    logger.warn("Part '" + label + "' in multipart body is empty.");
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("extracted part as str=\n" + partStr);
                    }
                    obj = part.getBody(clazz, null);
                    if (logger.isDebugEnabled()) {
                        logger.debug("extracted part as obj=\n",
                                objectAsXmlString(obj, clazz));
                    }
                }
                break;
            }
        }
        if (!partLabelMatched) {
            logger.warn("Could not find part '" + label + "' in multipart body.");
            // In the event that getBodyAsString() or getBody(), above, do *not*
            // throw an IOException, but getBody() nonetheless retrieves a null object.
            // This *may* be unreachable.
        } else if (obj == null) {
            logger.warn("Could not extract part '" + label
                    + "' in multipart body as an object.");
        }
        return obj;
    }

    /**
     * Gets the part object.
     *
     * @param partStr the part str
     * @param clazz the clazz
     * @return the part object
     * @throws JAXBException the jAXB exception
     */
    static protected Object getPartObject(String partStr, Class clazz)
            throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        ByteArrayInputStream bais = null;
        Object obj = null;
        try {
            bais = new ByteArrayInputStream(partStr.getBytes());
            Unmarshaller um = jc.createUnmarshaller();
            obj = um.unmarshal(bais);
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (Exception e) {
                }
            }
        }
        return obj;
    }

    /**
     * Object as xml string.
     *
     * @param o the o
     * @param clazz the clazz
     * @return the string
     */
    static protected String objectAsXmlString(Object o, Class clazz) {
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, sw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    /**
     * getObjectFromFile get object of given class from given file (in classpath)
     * @param jaxbClass
     * @param fileName of the file to read to construct the object
     * @return
     * @throws Exception
     */
    static protected Object getObjectFromFile(Class jaxbClass, String fileName)
            throws Exception {

        JAXBContext context = JAXBContext.newInstance(jaxbClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        InputStream is = tccl.getResourceAsStream(fileName);
        return getObjectFromStream(jaxbClass, is);
    }

    /**
     * Gets the xml document.
     *
     * @param fileName the file name
     * @return the xml document
     * @throws Exception the exception
     */
    static protected Document getXmlDocument(String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        File f = new File(fileName);
        if (!f.exists()) {
            throw new IllegalArgumentException("test data file " + fileName + " not found!");
        }
        // Create the builder and parse the file
        return factory.newDocumentBuilder().parse(f);
    }

    /**
     * Gets the xml document as string.
     *
     * @param fileName the file name
     * @return the xml document as string
     * @throws Exception the exception
     */
    static protected String getXmlDocumentAsString(String fileName) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(fileName));
        return new String(b);
    }

    /**
     * getObjectFromStream get object of given class from given inputstream
     * @param jaxbClass
     * @param is stream to read to construct the object
     * @return
     * @throws Exception
     */
    static protected Object getObjectFromStream(Class jaxbClass, InputStream is) throws Exception {
        JAXBContext context = JAXBContext.newInstance(jaxbClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        return jaxbClass.cast(unmarshaller.unmarshal(is));
    }

    /**
     * Map as string.
     *
     * @param map the map
     * @return the string
     */
    protected String mapAsString(MultivaluedMap map) {
        StringBuffer sb = new StringBuffer();
        for (Object entry : map.entrySet()) {
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            sb.append("    name=" + mentry.getKey());
            sb.append(" value=" + mentry.getValue() + "\n");
        }
        return sb.toString();
    }

    /**
     * Banner.
     *
     * @param label the label
     */
    protected void banner(String label) {
        if (logger.isDebugEnabled()) {
            logger.debug("===================================================");
            logger.debug(" Test = " + label);
            logger.debug("===================================================");
        }
    }
}
