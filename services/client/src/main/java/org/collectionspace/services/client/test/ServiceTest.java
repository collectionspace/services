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

/**
 * ServiceTest, interface specifying the client tests to be performed
 * to test an entity or relation service.
 */
public interface ServiceTest {

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Tests creation of a new resource.
     *
     * Relied upon by 'read', 'update' and 'delete' tests, below.
     */
    public void create(); 

    /**
     * Tests creation of a list of two or more new resources by repeatedly
     * calling create(), and relies on the latter's test assertion(s).
     *
     * Relied upon by 'read list' tests, below.
     */
    public void createList();

    // Failure outcomes

    /**
     * Tests creation of a null resource via the
     * Java Client Library. 
     */
    public void createNull();

    /**
     * Tests creation of a resource by submitting
     * a representation with malformed XML data. 
     */
    public void createWithMalformedXml();

    /**
     * Tests creation of a resource by submitting
     * a representation in the wrong XML schema
     * (e.g. not matching the object's schema). 
     */
    public void createWithWrongXmlSchema();
        
    // @TODO If feasible, implement a negative (failure)
    // test for creation of duplicate resources.


    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------

    // Success outcomes
    
    /**
     * Tests reading (i.e. retrieval) of a resource. 
     */
    public void read();

    // Failure outcomes

    /**
     * Tests reading (i.e. retrieval) of a non-existent
     * resource, whose resource identifier does not exist
     * at the specified URL.
     */
    public void readNonExistent(); 


    // ---------------------------------------------------------------
    // CRUD tests : READ (list, or multiple) tests
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Tests reading (i.e. retrieval) of a list of
     * multiple resources.
     */
    public void readList(); 

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
     */
    public void update();

    // Failure outcomes

    /**
     * Tests updating the content of a resource 
     * by submitting a representation with malformed
     * XML data. 
     */
    public void updateWithMalformedXml();

    /**
     * Tests updating the content of a resource
     * by submitting a representation in the wrong
     * XML schema (e.g. not matching the object's schema). 
     */
    public void updateWithWrongXmlSchema();

    /**
     * Tests updating the content of a non-existent
     * resource, whose resource identifier does not exist.
     */
    public void updateNonExistent();


    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------

    // Success outcomes

    /**
     * Tests deleting a resource.
     */
    public void delete(); 
    
    // Failure outcomes
    
    /**
     * Tests deleting a non-existent resource, whose resource
     * identifier does not exist at the specified URL.
     */
    public void deleteNonExistent();
    
}


