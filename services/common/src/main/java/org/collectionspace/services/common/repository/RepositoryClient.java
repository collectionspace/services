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

import org.collectionspace.services.common.context.ServiceContext;


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
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the document
     * @return id in repository of the newly created document
     * @throws BadRequestException data input is bad
     * @throws DocumentException
     */
    String create(ServiceContext ctx, DocumentHandler handler) throws BadRequestException, DocumentException;

    /**
     * delete a document from the Document repository
     * @param ctx service context under which this method is invoked
     * @param id of the document
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentException
     */
    void delete(ServiceContext ctx, String id) throws DocumentNotFoundException, DocumentException;

    /**
     * get document from the Document repository
     * @param ctx service context under which this method is invoked
     * @param id of the document to retrieve
     * @param handler should be used by the caller to provide and transform the document
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentException
     */
    void get(ServiceContext ctx, String id, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * getAll get all documents for an entity entity service from the Document repository
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the document
     * @throws DocumentNotFoundException if workspace not found
     * @throws DocumentException
     */
    void getAll(ServiceContext ctx, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * update given document in the Document repository
     * @param ctx service context under which this method is invoked
     * @param id of the document
     * @param handler should be used by the caller to provide and transform the document
     * @throws BadRequestException data input is bad
     * @throws DocumentNotFoundException if document not found
     * @throws DocumentException
     */
    void update(ServiceContext ctx, String id, DocumentHandler handler) throws BadRequestException, DocumentNotFoundException, DocumentException;
}
