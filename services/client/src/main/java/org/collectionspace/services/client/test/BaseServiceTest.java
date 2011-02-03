/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.client.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.w3c.dom.Document;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.TestServiceClient;

import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * BaseServiceTest.
 *
 * Base abstract class on which client tests of services are based.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */

// FIXME: http://issues.collectionspace.org/browse/CSPACE-1685

public abstract class BaseServiceTest {

    //Maven's base directory -i.e., the one containing the current pom.xml
    protected static final String MAVEN_BASEDIR_PROPERTY = "maven.basedir";
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceTest.class);
    /** The Constant serviceClient. */
    protected static final TestServiceClient serviceClient = new TestServiceClient();
    /** The non-existent id. */
    protected final String NON_EXISTENT_ID = createNonExistentIdentifier();
    /** The expected status code. */
    protected int EXPECTED_STATUS_CODE = 0;
    /** The request type type. */
    protected ServiceRequestType REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
    /** The Constant XML_DECLARATION. */
    protected static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    /** The Constant MALFORMED_XML_DATA. */
    protected static final String MALFORMED_XML_DATA = XML_DECLARATION
            + "<malformed_xml>wrong schema contents</malformed_xml";
    /** The wrong XML schema data. */
    protected static final String WRONG_XML_SCHEMA_DATA = XML_DECLARATION
            + "<wrong_schema>wrong schema contents</wrong_schema>";
    /** The null charset. */
    private static final String NULL_CHARSET = null;

    private static final String BANNER_SEPARATOR_LINE =
        "===================================================";
    private static final String BANNER_PREFIX =
        "\n" + BANNER_SEPARATOR_LINE + "\n";
    private static final String BANNER_SUFFIX =
        "\n" + BANNER_SEPARATOR_LINE;

    // A Unicode UTF-8 data fragment for use in test payloads: a random sequence,
    // unlikely to be encountered in actual collections data, and capable of
    // being rendered by the default fonts in many modern operating systems.
    //
    // This fragment consists of a run of USASCII characters, followed by
    // four non-USASCII range Unicode UTF-8 characters:
    //
    // Δ : Greek capital letter Delta (U+0394)
    // Ж : Cyrillic capital letter Zhe with breve (U+04C1)
    // Ŵ : Latin capital letter W with circumflex (U+0174)
    // Ω : Greek capital letter Omega (U+03A9)
    private final static String UTF8_DATA_FRAGMENT = "utf-8-data-fragment:"
            + '\u0394' + '\u04C1' + '\u0174' +'\u03A9';
    
    protected static final int STATUS_BAD_REQUEST =
        Response.Status.BAD_REQUEST.getStatusCode();
    protected static final int STATUS_CREATED =
        Response.Status.CREATED.getStatusCode();
    protected static final int STATUS_INTERNAL_SERVER_ERROR =
        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    protected static final int STATUS_NOT_FOUND =
        Response.Status.NOT_FOUND.getStatusCode();
    protected static final int STATUS_OK =
        Response.Status.OK.getStatusCode();

    /**
     * Instantiates a new base service test.
     */
    public BaseServiceTest() {
        super();
    }

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
    
    protected abstract String getServiceName();

    /**
     * Reinitializes setup values, to help expose any unintended reuse
     * of those values between tests.
     */
    protected void clearSetup() {
        EXPECTED_STATUS_CODE = 0;
        REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
    }

    /**
     * Initializes setup values for a given test.
     *
     * @param expectedStatusCode  A status code expected to be returned in the response.
     *
     * @param reqType  A type of service request (e.g. CREATE, DELETE).
     */
    protected void testSetup(
            int expectedStatusCode,
            ServiceRequestType reqType) {
        clearSetup();
        EXPECTED_STATUS_CODE = expectedStatusCode;
        REQUEST_TYPE = reqType;
    }

    /**
     * Returns an error message indicating that the status code returned by a
     * specific call to a service does not fall within a set of valid status
     * codes for that service.
     *
     * @param requestType  A type of service request (e.g. CREATE, DELETE).
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
        return serviceClient.getBaseURL() + getServiceName();//getServicePathComponent();
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
        HttpMethodBase httpMethod = null;
        try {
            TestServiceClient client = new TestServiceClient();
            if (method.equals(javax.ws.rs.HttpMethod.DELETE)) {
            	httpMethod = new DeleteMethod(url);
            } else if (method.equals(javax.ws.rs.HttpMethod.GET)) {
            	httpMethod = new GetMethod(url);
            }
            if (httpMethod != null) {
                statusCode = client.getHttpClient().executeMethod(httpMethod);
            }
        } catch (Exception e) {
            logger.error(
                    "Exception during HTTP " + method + " request to "
                    + url + ":", e);
        } finally {
        	if (httpMethod != null) httpMethod.releaseConnection();
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
     * @param  entityStr     The contents of the entity body to be submitted.
     *
     * @return The status code received in the HTTP response.
     */
    protected int submitRequest(String method, String url, String mediaType, String entityStr) {
        int statusCode = 0;
        EntityEnclosingMethod httpMethod = null;
        try {
            TestServiceClient client = new TestServiceClient();
            if (method.equals(javax.ws.rs.HttpMethod.POST)) {
                httpMethod = new PostMethod(url);
            } else if (method.equals(javax.ws.rs.HttpMethod.PUT)) {
                httpMethod = new PutMethod(url);
            }
            if (httpMethod != null) {
                StringRequestEntity entityBody =
                    new StringRequestEntity(mediaType, entityStr, NULL_CHARSET);
            	httpMethod.setRequestEntity(entityBody);
            	statusCode = client.getHttpClient().executeMethod(httpMethod);
            }
        } catch (Exception e) {
            logger.error(
                    "Exception during HTTP " + method + " request to "
                    + url + ":", e);
        } finally {
        	if (httpMethod != null) httpMethod.releaseConnection();
        }
        return statusCode;
    }

    // FIXME: Move some or all of the methods below to a common client and
    // server utilities package, when this package becomes available.

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    static protected String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((List<Object>) mvm.get("Location")).get(0);
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
    static protected Object extractPart(PoxPayloadIn input, String label, Class<?> clazz)
            throws Exception {
    	Object result = null;
    	PayloadInputPart payloadInputPart = input.getPart(label);
        if (payloadInputPart != null) {
        	result = payloadInputPart.getBody();
        } else if (logger.isWarnEnabled() == true) {
        	logger.warn("Payload part: " + label +
        			" is missing from payload: " + input.getName());
        }
        return result;
    }

    /**
     * Gets the part object.
     *
     * @param partStr the part str
     * @param clazz the clazz
     * @return the part object
     * @throws JAXBException the jAXB exception
     */
    @Deprecated
    static protected Object getPartObject(String partStr, Class<?> clazz)
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
                	if (logger.isDebugEnabled()) {
                		e.printStackTrace();
                	}
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
    static protected String objectAsXmlString(Object o, Class<?> clazz) {
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
    static protected Object getObjectFromFile(Class<?> jaxbClass, String fileName)
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
        String result = FileUtils.readFileToString(new File(fileName), "UTF8");
        return result;
    }

    /**
     * getObjectFromStream get object of given class from given inputstream
     * @param jaxbClass
     * @param is stream to read to construct the object
     * @return
     * @throws Exception
     */
    static protected Object getObjectFromStream(Class<?> jaxbClass, InputStream is) throws Exception {
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
    protected String mapAsString(MultivaluedMap<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        for (Object entry : map.entrySet()) {
            MultivaluedMap.Entry<String, Object> mentry = (MultivaluedMap.Entry<String, Object>) entry;
            sb.append("    name=" + mentry.getKey());
            sb.append(" value=" + mentry.getValue() + "\n");
        }
        return sb.toString();
    }

    /**
     * Returns a test-specific banner.
     *
     * @param testName The name of a test method.
     *
     * @return A test-specific banner.
     */
    protected static String testBanner(String testName) {
        testName = (testName == null || testName.trim().isEmpty()) ?
            " Test = no test name specified" : " Test = " + testName;
        return banner(testName);
    }

    /**
     * Returns a test-specific banner.
     *
     * @param testName The name of a test method.
     *
     * @param testClass The name of a test class.
     *
     * @return A test-specific banner.
     */
    protected static String testBanner(String testName, String testClass) {
        testName = (testName == null || testName.trim().isEmpty()) ?
            " Test = no test name specified" : " Test = " + testName;
        testClass = (testClass == null || testClass.trim().isEmpty()) ?
            "Class = no test class specified" : "Class = " + classNameFromPackageName(testClass);
        String testLabel = testClass + "\n" + testName;
        return banner(testLabel);
    }

    /**
     * Returns a 'banner', consisting of a text label inside a pair of prefix
     * and suffix strings.
     *
     * @param label The label to be output inside the banner.
     *
     * @return The banner.
     */
    protected static String banner(String label) {
        StringBuffer sb = new StringBuffer();
        sb.append(BANNER_PREFIX);
        sb.append(label);
        sb.append(BANNER_SUFFIX);
        return sb.toString();
    }

    protected static String classNameFromPackageName(String className) {
        if (className == null || className.trim().isEmpty()) {
            return className;
        }
        final char PKG_SEPARATOR = '.';
        int pos = className.lastIndexOf(PKG_SEPARATOR) + 1;
        if (pos > 0) {
            className = className.substring(pos);
        }
        return className;
    }

    public void assertStatusCode(ClientResponse<?> res, String testName) {
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        logger.debug(testName + ": status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode), invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    public static String getUTF8DataFragment() {
        return UTF8_DATA_FRAGMENT;
    }

}
