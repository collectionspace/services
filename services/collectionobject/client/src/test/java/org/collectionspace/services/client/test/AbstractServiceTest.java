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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.collectionspace.services.client.TestServiceClient;
import org.collectionspace.services.client.test.ServiceRequestType;

import org.jboss.resteasy.client.ClientResponse;

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

    private final TestServiceClient serviceClient = new TestServiceClient();
    protected HttpClient httpClient = new HttpClient();

    // The status code expected to be returned by a test method (where relevant).
    int EXPECTED_STATUS_CODE = 0;
    
    // The generic type of service request being tested (e.g. CREATE, UPDATE, DELETE).
    //
    // This makes it possible to check behavior specific to that type of request,
    // such as the set of valid status codes that may be returned.
    ServiceRequestType REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;


    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    // Success outcomes

    @Override
    public abstract void create();
    
    protected void setupCreate() {
        clearSetup();
        // Expected status code: 201 Created
        EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        REQUEST_TYPE = ServiceRequestType.CREATE;
    }

    @Override
    public abstract void createMultiple();
    
    // No setup required for createMultiple()

    // Failure outcomes

    @Override
    public void createNull() {
    }
    
    // No setup required for createNull()

    @Override
    public abstract void createWithMalformedXml();

    protected void setupCreateWithMalformedXml() {
        clearSetup();
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
    }

    @Override
    public abstract void createWithWrongXmlSchema();

    protected void setupCreateWithWrongXmlSchema() {
        clearSetup();
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
        clearSetup();
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.READ;
    }

    // Failure outcomes

    @Override
    public abstract void readNonExistent();

    protected void setupReadNonExistent() {
        clearSetup();
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
        clearSetup();
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.READ_MULTIPLE;
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
        clearSetup();
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    // Failure outcomes

    @Override
    public abstract void updateWithMalformedXml();

    protected void setupUpdateWithMalformedXml() {
        clearSetup();
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    @Override
    public abstract void updateWithWrongXmlSchema();

    protected void setupUpdateWithWrongXmlSchema() {
        clearSetup();
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.UPDATE;
    }

    @Override
    public abstract void updateNonExistent();

    protected void setupUpdateNonExistent() {
        clearSetup();
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
        clearSetup();
        // Expected status code: 200 OK
        EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.DELETE;
    }
    
    // Failure outcomes
    
    @Override
    public abstract void deleteNonExistent();

    protected void setupDeleteNonExistent() {
        clearSetup();
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
    protected abstract String getServicePathComponent();


    // ---------------------------------------------------------------
    // Utility methods
    // ---------------------------------------------------------------

    /**
     * Reinitializes setup values to guard against unintended reuse
     * of those values.
     */
    protected void clearSetup() {
        EXPECTED_STATUS_CODE = 0;
        REQUEST_TYPE = ServiceRequestType.NON_EXISTENT;
    }

    // @TODO Add Javadoc comments to all methods requiring them, below.

    protected String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return 
            "Status code '" + statusCode + "' in response is NOT within the expected set: " +
            requestType.validStatusCodesAsString();
    }
    
    protected String getServiceRootURL() {
        return serviceClient.getBaseURL() + getServicePathComponent();
    }

    protected String getResourceURL(String resourceIdentifier) {
        return getServiceRootURL() + "/" + resourceIdentifier;
    }

    protected int submitRequest(HttpMethod method) {
     int statusCode = 0;
        try {
            statusCode = httpClient.executeMethod(method);
        } catch(HttpException e) {
            logger.error("Fatal protocol violation: ", e);
        } catch(IOException e) {
            logger.error("Fatal transport error: ", e);
        } catch(Exception e) {
            logger.error("Unknown exception: ", e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return statusCode;
    }
    
    protected int submitRequest(EntityEnclosingMethod method, RequestEntity entity) {
        int statusCode = 0;
        try {
            method.setRequestEntity(entity);
            statusCode = httpClient.executeMethod(method);
        } catch(HttpException e) {
            logger.error("Fatal protocol violation: ", e);
        } catch(IOException e) {
            logger.error("Fatal transport error: ", e);
        } catch(Exception e) {
            logger.error("Unknown exception: ", e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return statusCode;
    }
    
    protected StringRequestEntity getXmlEntity(String contents) {
        if (contents == null) {
            contents = "";
        }
        StringRequestEntity entity = null;
        final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        final String XML_CONTENT_TYPE=MediaType.APPLICATION_XML;
        final String UTF8_CHARSET_NAME = "UTF-8";
        try {
            entity =
                new StringRequestEntity(XML_DECLARATION + contents, XML_CONTENT_TYPE, UTF8_CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported character encoding error: ", e);
        }
        return entity;
    }

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
        if (logger.isDebugEnabled()) {
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
    
}


