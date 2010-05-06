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

//import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.CollectionSpaceClient;
//import org.collectionspace.services.client.AbstractServiceClientImpl;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1685
 * AbstractServiceTest, abstract base class for the client tests to be performed
 * to test an entity or relation service.
 *
 * For Javadoc descriptions of this class's methods, see the ServiceTest interface.
 */
public abstract class AbstractServiceTestImpl extends BaseServiceTest implements ServiceTest {

	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(AbstractServiceTestImpl.class);
	
	/** The Constant DEFAULT_LIST_SIZE. */
	static protected final int DEFAULT_LIST_SIZE = 10;
	static protected final int DEFAULT_PAGINATEDLIST_SIZE = 10;

//    // Success outcomes
//    /* (non-Javadoc)
//     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
//     */
//    @Override
//    public void create(String testName) throws Exception {
//    	//empty?
//    }
    
    /**
     * Gets the logger.
     *
     * @return the logger
     */
    private Logger getLogger() {
    	return this.logger;
    }

    /**
     * Setup create.
     */
    protected void setupCreate() {
        setupCreate("Create");
    }

    /**
     * Sets the up create.
     *
     * @param label the new up create
     */
    protected void setupCreate(String label) {
    	testSetup(Response.Status.CREATED.getStatusCode(), ServiceRequestType.CREATE, label);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createList(java.lang.String)
     */
    @Override
    public abstract void createList(String testName) throws Exception;

    // No setup required for createList()
    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public abstract void createWithEmptyEntityBody(String testName)
            throws Exception;

    /**
     * Setup create with empty entity body.
     */
    protected void setupCreateWithEmptyEntityBody() {
        setupCreateWithEmptyEntityBody("CreateWithEmptyEntityBody");
    }

    /**
     * Sets the up create with empty entity body.
     *
     * @param label the new up create with empty entity body
     */
    protected void setupCreateWithEmptyEntityBody(String label) {
        clearSetup();
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
        if (logger.isDebugEnabled()) {
            banner(label);
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithMalformedXml(java.lang.String)
     */
    @Override
    public abstract void createWithMalformedXml(String testName) throws Exception;

    /**
     * Setup create with malformed xml.
     */
    protected void setupCreateWithMalformedXml() {
        setupCreateWithMalformedXml("CreateWithMalformedXml");
    }

    /**
     * Sets the up create with malformed xml.
     *
     * @param label the new up create with malformed xml
     */
    protected void setupCreateWithMalformedXml(String label) {
        clearSetup();
        // Expected status code: 400 Bad Request
        EXPECTED_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.CREATE;
        if (logger.isDebugEnabled()) {
            banner(label);
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public abstract void createWithWrongXmlSchema(String testName) throws Exception;

    /**
     * Setup create with wrong xml schema.
     */
    protected void setupCreateWithWrongXmlSchema() {
        setupCreateWithWrongXmlSchema("CreateWithWrongXmlSchema");
    }

    /**
     * Sets the up create with wrong xml schema.
     *
     * @param label the new up create with wrong xml schema
     */
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
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#read(java.lang.String)
     */
    @Override
    public abstract void read(String testName) throws Exception;

    /**
     * Setup read.
     */
    protected void setupRead() {
        setupRead("Read");
    }

    /**
     * Sets the up read.
     *
     * @param label the new up read
     */
    protected void setupRead(String label) {
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.READ, label);
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#readNonExistent(java.lang.String)
     */
    @Override
    public abstract void readNonExistent(String testName) throws Exception;

    /**
     * Setup read non existent.
     */
    protected void setupReadNonExistent() {
        setupReadNonExistent("ReadNonExistent");
    }

    /**
     * Sets the up read non existent.
     *
     * @param label the new up read non existent
     */
    protected void setupReadNonExistent(String label) {
        // Expected status code: 404 Not Found
    	testSetup(Response.Status.NOT_FOUND.getStatusCode(), ServiceRequestType.READ, label);
    }

	/**
	 * Read list.
	 *
	 * @param testName the test name
	 * @param client the client
	 * @param pageSize the page size
	 * @param pageNumber the page number
	 * @return the abstract common list
	 * @throws Exception the exception
	 */
	private AbstractCommonList readList(String testName,
			CollectionSpaceClient client,
			long pageSize, long pageNumber) throws Exception {
		ClientResponse<AbstractCommonList> response =
			(ClientResponse<AbstractCommonList>)client.readList(Long.toString(pageSize),
					Long.toString(pageNumber));
		int statusCode = response.getStatus();

		// Check the status code of the response: does it match
		// the expected response(s)?
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(testName + ": status = " + statusCode);
		}
		Assert.assertTrue(this.REQUEST_TYPE.isValidStatusCode(statusCode),
				invalidStatusCodeMessage(this.REQUEST_TYPE, statusCode));
		Assert.assertEquals(statusCode, this.EXPECTED_STATUS_CODE);

		AbstractCommonList list = this.getAbstractCommonList(response);		
		return list;
	}

    /**
     * Creates the list.
     *
     * @param testName the test name
     * @param listSize the list size
     * @throws Exception the exception
     */
    protected void createPaginatedList(String testName, int listSize) throws Exception {
        for (int i = 0; i < listSize; i++) {
            create(testName);
        }
    }
	
    /*@Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void leafCeate(String testName) throws Exception {
    	this.create(testName);
    }*/
    
    private void assertPaginationInfo(String testName,
    		AbstractCommonList list,
    		long expectedPageNum,
    		long expectedPageSize,
    		long expectedListSize,
    		long expectedTotalItems) {
    	Assert.assertNotNull(list);
    	
    	long pageNum = list.getPageNum();
    	Assert.assertEquals(pageNum, expectedPageNum);
    	if (getLogger().isDebugEnabled() == true) {
    		getLogger().debug(testName + ":" + "page number is " + pageNum);
    	}
    	
    	long pageSizeReturned = list.getPageSize();
    	Assert.assertEquals(pageSizeReturned, expectedPageSize);
    	if (getLogger().isDebugEnabled() == true) {
    		getLogger().debug(testName + ":" + "page size is " + list.getPageSize());
    	}
    	
    	long itemsInPage = list.getItemsInPage();
    	Assert.assertEquals(itemsInPage, expectedListSize);
    	if (getLogger().isDebugEnabled() == true) {
    		getLogger().debug(testName + ":" + "actual items in page was/were " + itemsInPage);
    	}        	
    	
    	long totalItemsReturned = list.getTotalItems();
    	Assert.assertEquals(totalItemsReturned, expectedTotalItems);
    	if (getLogger().isDebugEnabled() == true) {
    		getLogger().debug(testName + ":" + "total number of items is " + list.getTotalItems());
    	}    	 
    }
    
	/**
	 * Read paginated list.
	 *
	 * @param testName the test name
	 * @throws Exception the exception
	 */
    @Test(dataProvider = "testName") /*, dataProviderClass = AbstractServiceTestImpl.class,
    	    dependsOnMethods = {"leafCeate"}) */
    public void readPaginatedList(String testName) throws Exception {
        // Perform setup.
        setupReadList(testName);
        CollectionSpaceClient client = this.getClientInstance();
        
        // Get the current total number of items.
        // If there are no items then create some
        AbstractCommonList list = this.readList(testName, client, 1 /*pgSz*/, 0 /*pgNum*/);
        if (list == null || list.getTotalItems() == 0) {
        	this.createPaginatedList(testName, DEFAULT_PAGINATEDLIST_SIZE);
        	list = this.readList(testName, client, 1 /*pgSz*/, 0 /*pgNum*/);
        }

        // Print out the current list size to be paginated
        Assert.assertNotNull(list);
        long totalItems = list.getTotalItems();
        Assert.assertFalse(totalItems == 0);
        if (getLogger().isDebugEnabled() == true) {
        	getLogger().debug(testName + ":" + "created list of " + 
        			totalItems + " to be paginated.");
        }
        
        long pageSize = totalItems / 3; //create up to 3 pages to iterate over
        long pagesTotal = pageSize > 0 ? (totalItems / pageSize) : 0;
        for (int i = 0; i < pagesTotal; i++) {
        	list = this.readList(testName, client, pageSize, i);
        	assertPaginationInfo(testName,
        			list,
        			i,			//expected page number
        			pageSize,	//expected page size
        			pageSize,	//expected num of items in page
        			totalItems);//expected total num of items
        }
        
        // if there are any remainders be sure to paginate them as well
        long mod = totalItems % pageSize;
        if (mod != 0) {
        	list = this.readList(testName, client, pageSize, pagesTotal);
        	assertPaginationInfo(testName,
        			list, 
        			pagesTotal, //expected page number
        			pageSize, 	//expected page size
        			mod, 		//expected num of items in page
        			totalItems);//expected total num of items
        }
    }

	// ---------------------------------------------------------------
    // CRUD tests : READ (list, or multiple) tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.ServiceTest#readList(java.lang.String)
	 */
	@Override
    public abstract void readList(String testName) throws Exception;

    /**
     * Setup read list.
     */
    protected void setupReadList() {
        setupReadList("ReadList");
    }

    /**
     * Sets the up read list.
     *
     * @param label the new up read list
     */
    protected void setupReadList(String label) {
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.READ_LIST, label);
    }

    // Failure outcomes
    // None tested at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#update(java.lang.String)
     */
    @Override
    public abstract void update(String testName) throws Exception;

    /**
     * Setup update.
     */
    protected void setupUpdate() {
        setupUpdate("Update");
    }

    /**
     * Sets the up update.
     *
     * @param label the new up update
     */
    protected void setupUpdate(String label) {
        // Expected status code: 200 OK
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public abstract void updateWithEmptyEntityBody(String testName) throws Exception;

    /**
     * Setup update with empty entity body.
     */
    protected void setupUpdateWithEmptyEntityBody() {
        setupUpdateWithEmptyEntityBody("UpdateWithEmptyEntityBody");
    }

    /**
     * Sets the up update with empty entity body.
     *
     * @param label the new up update with empty entity body
     */
    protected void setupUpdateWithEmptyEntityBody(String label) {
        // Expected status code: 400 Bad Request
    	testSetup(Response.Status.BAD_REQUEST.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public abstract void updateWithMalformedXml(String testName) throws Exception;

    /**
     * Setup update with malformed xml.
     */
    protected void setupUpdateWithMalformedXml() {
        setupUpdateWithMalformedXml("UpdateWithMalformedXml");
    }

    /**
     * Sets the up update with malformed xml.
     *
     * @param label the new up update with malformed xml
     */
    protected void setupUpdateWithMalformedXml(String label) {
        // Expected status code: 400 Bad Request
    	testSetup(Response.Status.BAD_REQUEST.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public abstract void updateWithWrongXmlSchema(String testName) throws Exception;

    /**
     * Setup update with wrong xml schema.
     */
    protected void setupUpdateWithWrongXmlSchema() {
        setupUpdateWithWrongXmlSchema("UpdateWithWrongXmlSchema");
    }

    /**
     * Sets the up update with wrong xml schema.
     *
     * @param label the new up update with wrong xml schema
     */
    protected void setupUpdateWithWrongXmlSchema(String label) {
        // Expected status code: 400 Bad Request
    	testSetup(Response.Status.BAD_REQUEST.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#updateNonExistent(java.lang.String)
     */
    @Override
    public abstract void updateNonExistent(String testName) throws Exception;

    /**
     * Setup update non existent.
     */
    protected void setupUpdateNonExistent() {
        setupUpdateNonExistent("UpdateNonExistent");
    }

    /**
     * Sets the up update non existent.
     *
     * @param label the new up update non existent
     */
    protected void setupUpdateNonExistent(String label) {
        // Expected status code: 404 Not Found
    	testSetup(Response.Status.NOT_FOUND.getStatusCode(), ServiceRequestType.UPDATE, label);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#delete(java.lang.String)
     */
    @Override
    public abstract void delete(String testName) throws Exception;

    /**
     * Setup delete.
     */
    protected void setupDelete() {
        setupDelete("Delete");
    }

    /**
     * Sets the up delete.
     *
     * @param label the new up delete
     */
    protected void setupDelete(String label) {
    	testSetup(Response.Status.OK.getStatusCode(), ServiceRequestType.DELETE, label);
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#deleteNonExistent(java.lang.String)
     */
    @Override
    public abstract void deleteNonExistent(String testName) throws Exception;

    /**
     * Setup delete non existent.
     */
    protected void setupDeleteNonExistent() {
        setupDeleteNonExistent("DeleteNonExistent");
    }

    /**
     * Sets the up delete non existent.
     *
     * @param label the new up delete non existent
     */
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


