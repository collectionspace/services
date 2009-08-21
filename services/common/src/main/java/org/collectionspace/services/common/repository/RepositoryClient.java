/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.repository;


/**
 * RepositoryClient is a generic Document Repository client
 *
 * Typical call sequence is:
 * Create handler and repository client
 * Call XXX operation on the repository client and pass the handler
 * repository client calls prepare on the handler
 * The repository client then calls handle on the handler
 * 
 */
public interface RepositoryClient {

    /**
     * create document in the Document repository
     * @param serviceName entity service for which document is created. for example
     * this is used to find mapping
     * to a Nuxeo workspace using service-config.xml
     * @param serviceName entity service for which document is created. this is used to find mapping
     * to a Nuxeo workspace using service-config.xml
     * @param handler should be used by the caller to provide and transform the document
     * @return id in repository of the newly created document
     * @throws BadRequestException data input is bad
     * @throws DocumentException
     */
    String create(String serviceName, DocumentHandler handler) throws BadRequestException, DocumentException;

    /**
     * delete a document from the Document repository
     * @param id of the document
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentException
     */
    void delete(String id) throws DocumentNotFoundException, DocumentException;

    /**
     * get document from the Document repository
     * @param id of the document to retrieve
     * @param handler should be used by the caller to provide and transform the document
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentException
     */
    void get(String id, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * getAll get all documents for an entity entity service from the Document repository
     * @param serviceName entity service for which documents are retrieved. this is used to find mapping
     * to a Nuxeo workspace using service-config.xml
     * @param handler should be used by the caller to provide and transform the document
     * @throws DocumentNotFoundException if workspace not found
     * @throws DocumentException
     */
    void getAll(String serviceName, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * update given document in the Document repository
     * @param id of the document
     * @param handler should be used by the caller to provide and transform the document
     * @throws BadRequestException data input is bad
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentException
     */
    void update(String id, DocumentHandler handler) throws BadRequestException, DocumentNotFoundException, DocumentException;
}
