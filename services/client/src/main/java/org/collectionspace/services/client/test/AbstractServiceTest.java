/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
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
import java.util.ArrayList;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import javax.xml.bind.Unmarshaller;
import org.collectionspace.services.client.TestServiceClient;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractServiceTest, abstract base class for the client tests to be performed
 * to test an entity or relation service.
 *
 * For Javadoc descriptions of this class's methods, see the ServiceTest interface.
 */
public abstract class AbstractServiceTest implements ServiceTest {

    final Logger logger = LoggerFactory.getLogger(AbstractServiceTest.class);
    // A base-level client, used (only) to obtain the base service URL.
    private static final TestServiceClient serviceClient = new TestServiceClient();
    // A resource identifier believed to be non-existent in actual use,
    // used when testing service calls that reference non-existent resources.
    protected final String NON_EXISTENT_ID = createNonExistentIdentifier();
    // The HTTP status code expected to be returned in the response,
    // from a request made to a service (where relevant).
    int EXPECTED_STATUS_CODE = 0;
    // The generic type of service request being tested (e.g. CREATE, UPDATE, DELETE).
    //
    // This makes it possible to check behavior specific to that type of request,
    // such as the set of valid status codes that may be returned.
    //
    // Note that the default value is set to a guard value.
    protected ServiceRequestType REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
    // Static data to be submitted in various tests
    protected final static String XML_DECLARATION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    // Note: this constant is intentionally missing its last angle bracket.
    protected final static String MALFORMED_XML_DATA =
            XML_DECLARATION +
            "<malformed_xml>wrong schema contents</malformed_xml";
    protected final String WRONG_XML_SCHEMA_DATA =
            XML_DECLARATION +
            "<wrong_schema>wrong schema contents</wrong_schema>";

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public void create() {
        
    }

    protected void setupCreate() {
        clearSetup("Create");
        // Expected status code: 201 Created
        EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        REQUEST_TYPE = ServiceRequestType.CREATE;
    }

    @Override
    public abstract void createList();

    // No setup required for createList()
    // Failure outcomes
    @Override
    public abstract void createWithEmptyEntityBody();

    protected void setupCreateWithEmptyEntityBody() {
        clearSetup("CreateWithEmptyEntityBody");
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
    }

    @Override
    public abstract void createWithMalformedXml();

    protected void setupCreateWithMalformedXml() {
        clearSetup("CreateWithMalformedXml");
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
    }

    @Override
    public abstract void createWithWrongXmlSchema();

    protected void setupCreateWithWrongXmlSchema() {
        clearSetup("CreateWithWrongXmlSchema");
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void read();

    protected void setupRead() {
        clearSetup("Read");
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.READ;
    }

    // Failure outcomes
    @Override
    public abstract void readNonExistent();

    protected void setupReadNonExistent() {
        clearSetup("ReadNonExistent");
        // Expected status code: 404 Not Found
        EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.READ;
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ (list, or multiple) tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void readList();

    protected void setupReadList() {
        clearSetup("ReadList");
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.READ_LIST;
    }

    // Failure outcomes
    // None tested at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    // ----------------
    @Override
    public abstract void update();

    protected void setupUpdate() {
        clearSetup("Update");
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    // Failure outcomes
    @Override
    public abstract void updateWithEmptyEntityBody();

    protected void setupUpdateWithEmptyEntityBody() {
        clearSetup("UpdateWithEmptyEntityBody");
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    @Override
    public abstract void updateWithMalformedXml();

    protected void setupUpdateWithMalformedXml() {
        clearSetup("UpdateWithMalformedXml");
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    @Override
    public abstract void updateWithWrongXmlSchema();

    protected void setupUpdateWithWrongXmlSchema() {
        clearSetup("UpdateWithWrongXmlSchema");
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    @Override
    public abstract void updateNonExistent();

    protected void setupUpdateNonExistent() {
        clearSetup("UpdateNonExistent");
        // Expected status code: 404 Not Found
        EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void delete();

    protected void setupDelete() {
        clearSetup("Delete");
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.DELETE;
    }

    // Failure outcomes
    @Override
    public abstract void deleteNonExistent();

    protected void setupDeleteNonExistent() {
        clearSetup("DeleteNonExistent");
        // Expected status code: 404 Not Found
        EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.DELETE;
    }

    // ---------------------------------------------------------------
    // Abstract utility methods
    //
    // Must be implemented by classes that extend
    // this abstract base class.
    // ---------------------------------------------------------------
    /**
     * Returns the URL path component of the service.
     *
     * This component will follow directly after the
     * base path, if any.
     */
    @Override
    public abstract String getServicePathComponent();

    @Override
    public String getCommonPartName() {
        return getServicePathComponent() + "-common";
    }

    // ---------------------------------------------------------------
    // Utility methods
    // ---------------------------------------------------------------
    /**
     * Reinitializes setup values, to help expose any unintended reuse
     * of those values between tests.
     */
    protected void clearSetup(String testName) {
        EXPECTED_STATUS_CODE = 0;
        REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
        logger.debug("========================================================");
        logger.debug(" Test = " + testName);
        logger.debug("========================================================");
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
        return "Status code '" + statusCode + "' in response is NOT within the expected set: " +
                requestType.validStatusCodesAsString();
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
        try{
            ClientRequest request = new ClientRequest(url);
            if(method.equals(javax.ws.rs.HttpMethod.DELETE)){
                ClientResponse res = request.delete();
                statusCode = res.getStatus();
            }else if(method.equals(javax.ws.rs.HttpMethod.GET)){
                ClientResponse res = request.get();
                statusCode = res.getStatus();
            }else{
                // Do nothing - leave status code at default value.
            }
        }catch(Exception e){
            logger.error(
                    "Exception during HTTP " + method + " request to " + url + ":",
                    e);
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
    protected int submitRequest(String method, String url,
            String mediaType, String entityStr) {
        int statusCode = 0;
        try{
            ClientRequest request = new ClientRequest(url);
            request.body(mediaType, entityStr);
            if(method.equals(javax.ws.rs.HttpMethod.POST)){
                ClientResponse res = request.post(java.lang.String.class);
                statusCode = res.getStatus();
            }else if(method.equals(javax.ws.rs.HttpMethod.PUT)){
                ClientResponse res = request.put(java.lang.String.class);
                statusCode = res.getStatus();
            }else{
                // Do nothing - leave status code at default value.
            }
        }catch(Exception e){
            logger.error(
                    "Exception during HTTP " + method + " request to " + url + ":",
                    e);
        }
        return statusCode;
    }

    // @TODO Add Javadoc comments to all methods requiring them, below.
    protected String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        verbose("extractId:uri=" + uri);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("id=" + id);
        return id;
    }

    protected void verbose(String msg) {
        if(logger.isDebugEnabled()){
            logger.debug(msg);
        }
    }

    protected void verbose(String msg, Object o, Class clazz) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void verboseMap(MultivaluedMap map) {
        for(Object entry : map.entrySet()){
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            verbose("    name=" + mentry.getKey() + " value=" + mentry.getValue());
        }
    }

    protected String createIdentifier() {
        long identifier = System.currentTimeMillis();
        return Long.toString(identifier);
    }

    protected String createNonExistentIdentifier() {
        return Long.toString(Long.MAX_VALUE);
    }

    protected Object extractPart(MultipartInput input, String label, Class clazz) throws Exception {
        Object obj = null;
        for(InputPart part : input.getParts()){
            String partLabel = part.getHeaders().getFirst("label");
            if(label.equalsIgnoreCase(partLabel)){
                String partStr = part.getBodyAsString();
                verbose("extracted part str=\n" + partStr);
                obj = part.getBody(clazz, null);
                verbose("extracted part obj=\n", obj, clazz);
                break;
            }
        }
        return obj;
    }

    protected Object getPartObject(String partStr, Class clazz) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        ByteArrayInputStream bais = null;
        Object obj = null;
        try{
            bais = new ByteArrayInputStream(partStr.getBytes());
            Unmarshaller um = jc.createUnmarshaller();
            obj = um.unmarshal(bais);
        }finally{
            if(bais != null){
                try{
                    bais.close();
                }catch(Exception e){
                }
            }
        }
        return obj;
    }
}


