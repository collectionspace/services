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

/**
 * ServiceTest, interface specifying the client tests to be performed
 * to test an entity or relation service.
 *
 * The 'testName' parameter will receive the name of the currently
 * running test by means of reflection, from a TestNG DataProvider.
 *
 * @see org.collectionspace.services.client.test.AbstractServiceTest#testName
 */
public interface ServiceTest {


    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    /*
     * We use this method to force a TestNG execution order for our tests.  The "leaf" methods
     * should look something like this:
     * 
	    //
	    // For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
	    // refer to this method in their @Test annotation declarations.
	    //
	    @Test(dataProvider = "testName",
	    		dependsOnMethods = {
	        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
	    public void CRUDTests() {
	    	// Do nothing.  Simply here to for a TestNG execution order for our tests
	    }
	 *
	 * For an example, see the CollectionObjectServiceTest class.
	 * 
     */    
    public void CRUDTests(String testName);
    
    // Success outcomes

    public void searchWorkflowDeleted(String testName) throws Exception;
    
    /**
     * Tests creation of a new resource.
     *
     * Relied upon by 'read', 'update' and 'delete' tests, below.
     * @param testName 
     * @throws Exception 
     */
    public void create(String testName) throws Exception;
    
    /**
     * Tests creation of a list of two or more new resources by repeatedly
     * calling create(), and relies on the latter's test assertion(s).
     *
     * Relied upon by 'read list' tests, below.
     * @param testName 
     * @throws Exception 
     */
    public void createList(String testName) throws Exception;

    // Failure outcomes

    /**
     * Tests creation of a resource by submitting
     * an empty entity body (aka empty payload).
     * @param testName 
     * @throws Exception 
     */
    public void createWithEmptyEntityBody(String testName) throws Exception;

    /**
     * Tests creation of a resource by submitting
     * a representation with malformed XML data.
     * @param testName 
     * @throws Exception 
     */
    public void createWithMalformedXml(String testName) throws Exception;

    /**
     * Tests creation of a resource by submitting
     * a representation in the wrong XML schema
     * (e.g. not matching the object's schema).
     * @param testName 
     * @throws Exception 
     */
    public void createWithWrongXmlSchema(String testName) throws Exception;

    // @TODO If feasible, implement a negative (failure)
    // test for creation of duplicate resources.


    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Tests reading (i.e. retrieval) of a resource.
     * @param testName 
     * @throws Exception 
     */
    public void read(String testName) throws Exception;

    // Failure outcomes

    /**
     * Tests reading (i.e. retrieval) of a non-existent
     * resource, whose resource identifier does not exist
     * at the specified URL.
     * @param testName 
     * @throws Exception 
     */
    public void readNonExistent(String testName) throws Exception;


    // ---------------------------------------------------------------
    // CRUD tests : READ (list, or multiple) tests
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Tests reading (i.e. retrieval) of a list of
     * multiple resources.
     * @param testName 
     * @throws Exception 
     */
    public void readList(String testName) throws Exception;

    // If feasible, implement a test for reading
    // an empty list returned by the service.

    // Failure outcomes

    // If feasible, implement a negative (failure) test
    // of handling of unrecognized query parameters
    // (e.g. other than filtering or chunking parameters, etc.
    // that may be supported by the service).

    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------

    // Success outcomes
    // ----------------

    /**
     * Tests updating the content of a resource.
     * @param testName 
     * @throws Exception 
     */
    public void update(String testName) throws Exception;

    // Failure outcomes

    /**
     * Tests updating the content of a resource
     * by submitting an empty entity body (aka empty payload).
     * @param testName 
     * @throws Exception 
     */
    public void updateWithEmptyEntityBody(String testName) throws Exception;

    /**
     * Tests updating the content of a resource
     * by submitting a representation with malformed
     * XML data.
     * @param testName 
     * @throws Exception 
     */
    public void updateWithMalformedXml(String testName) throws Exception;

    /**
     * Tests updating the content of a resource
     * by submitting a representation in the wrong
     * XML schema (e.g. not matching the object's schema).
     * @param testName 
     * @throws Exception 
     */
    public void updateWithWrongXmlSchema(String testName) throws Exception;

    /**
     * Tests updating the content of a non-existent
     * resource, whose resource identifier does not exist.
     * @param testName 
     * @throws Exception 
     */
    public void updateNonExistent(String testName) throws Exception;

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Tests deleting a resource.
     * @param testName 
     * @throws Exception 
     */
    public void delete(String testName) throws Exception;

    // Failure outcomes

    /**
     * Tests deleting a non-existent resource, whose resource
     * identifier does not exist at the specified URL.
     * @param testName 
     * @throws Exception 
     */
    public void deleteNonExistent(String testName) throws Exception;

}


