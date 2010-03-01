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

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;






/**
 * AbstractServiceTest, abstract base class for the client tests to be performed
 * to test an entity or relation service.
 *
 * For Javadoc descriptions of this class's methods, see the ServiceTest interface.
 */
public abstract class AbstractServiceTestImpl extends BaseServiceTest implements ServiceTest {

	protected final Logger logger = LoggerFactory.getLogger(AbstractServiceTestImpl.class);

    // Success outcomes
    @Override
    public void create(String testName) throws Exception {
    }

    protected void setupCreate() {
        setupCreate("Create");
    }

    protected void setupCreate(String label) {
    	testSetup(Response.Status.CREATED.getStatusCode(), ServiceRequestType.CREATE, label);
    }

    @Override
    public abstract void createList(String testName) throws Exception;

    // No setup required for createList()
    // Failure outcomes
    @Override
    public abstract void createWithEmptyEntityBody(String testName)
            throws Exception;

    protected void setupCreateWithEmptyEntityBody() {
        setupCreateWithEmptyEntityBody("CreateWithEmptyEntityBody");
    }

    protected void setupCreateWithEmptyEntityBody(String label) {
        clearSetup();
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
        if (logger.isDebugEnabled()) {
            banner(label);
        }
    }

    @Override
    public abstract void createWithMalformedXml(String testName) throws Exception;

    protected void setupCreateWithMalformedXml() {
        setupCreateWithMalformedXml("CreateWithMalformedXml");
    }

    protected void setupCreateWithMalformedXml(String label) {
        clearSetup();
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
        if (logger.isDebugEnabled()) {
            banner(label);
        }
    }

    @Override
    public abstract void createWithWrongXmlSchema(String testName) throws Exception;

    protected void setupCreateWithWrongXmlSchema() {
        setupCreateWithWrongXmlSchema("CreateWithWrongXmlSchema");
    }

    protected void setupCreateWithWrongXmlSchema(String label) {
        clearSetup();
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
        if (logger.isDebugEnabled()) {
            banner(label);
        }
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void read(String testName) throws Exception;

    protected void setupRead() {
        setupRead("Read");
    }

    protected void setupRead(String label) {
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.READ, label);
    }

    // Failure outcomes
    @Override
    public abstract void readNonExistent(String testName) throws Exception;

    protected void setupReadNonExistent() {
        setupReadNonExistent("ReadNonExistent");
    }

    protected void setupReadNonExistent(String label) {
        // Expected status code: 404 Not Found
    	testSetup(Response.Status.NOT_FOUND.getStatusCode(), ServiceRequestType.READ, label);
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ (list, or multiple) tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void readList(String testName) throws Exception;

    protected void setupReadList() {
        setupReadList("ReadList");
    }

    protected void setupReadList(String label) {
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.READ_LIST, label);
    }

    // Failure outcomes
    // None tested at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void update(String testName) throws Exception;

    protected void setupUpdate() {
        setupUpdate("Update");
    }

    protected void setupUpdate(String label) {
        // Expected status code: 200 OK
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    // Failure outcomes
    @Override
    public abstract void updateWithEmptyEntityBody(String testName) throws Exception;

    protected void setupUpdateWithEmptyEntityBody() {
        setupUpdateWithEmptyEntityBody("UpdateWithEmptyEntityBody");
    }

    protected void setupUpdateWithEmptyEntityBody(String label) {
        // Expected status code: 400 Bad Request
    	testSetup(Response.Status.BAD_REQUEST.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    @Override
    public abstract void updateWithMalformedXml(String testName) throws Exception;

    protected void setupUpdateWithMalformedXml() {
        setupUpdateWithMalformedXml("UpdateWithMalformedXml");
    }

    protected void setupUpdateWithMalformedXml(String label) {
        // Expected status code: 400 Bad Request
    	testSetup(Response.Status.BAD_REQUEST.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    @Override
    public abstract void updateWithWrongXmlSchema(String testName) throws Exception;

    protected void setupUpdateWithWrongXmlSchema() {
        setupUpdateWithWrongXmlSchema("UpdateWithWrongXmlSchema");
    }

    protected void setupUpdateWithWrongXmlSchema(String label) {
        // Expected status code: 400 Bad Request
    	testSetup(Response.Status.BAD_REQUEST.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    @Override
    public abstract void updateNonExistent(String testName) throws Exception;

    protected void setupUpdateNonExistent() {
        setupUpdateNonExistent("UpdateNonExistent");
    }

    protected void setupUpdateNonExistent(String label) {
        // Expected status code: 404 Not Found
    	testSetup(Response.Status.NOT_FOUND.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    public abstract void delete(String testName) throws Exception;

    protected void setupDelete() {
        setupDelete("Delete");
    }

    protected void setupDelete(String label) {
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.DELETE, label);
    }

    // Failure outcomes
    @Override
    public abstract void deleteNonExistent(String testName) throws Exception;

    protected void setupDeleteNonExistent() {
        setupDeleteNonExistent("DeleteNonExistent");
    }

    protected void setupDeleteNonExistent(String label) {
        clearSetup();
        // Expected status code: 404 Not Found
        EXPECTED_STATUS_CODE = Response.Status.NOT_FOUND.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.DELETE;
        if (logger.isDebugEnabled()) {
            banner(label);
        }
    }
}


