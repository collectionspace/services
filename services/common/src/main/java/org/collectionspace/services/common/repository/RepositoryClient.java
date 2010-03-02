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
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.storage.StorageClient;
import org.nuxeo.ecm.core.api.DocumentModel;

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
public interface RepositoryClient extends StorageClient {

    /**
     * createWorkspace creates a workspace in default repository under given domain
     * @param tenantDomain domain representing tenant
     * @param workspaceName name of the workspace
     * @return id of newly created workspace
     * @throws java.lang.Exception
     */
    public String createWorkspace(String tenantDomain, String workspaceName) throws Exception;

        /**
     * getWorkspaceId gets an id of given workspace in default repository under given domain
     * @param tenantDomain domain representing tenant
     * @param workspaceName name of the workspace
     * @return id of the workspace
     * @throws java.lang.Exception
     */
    public String getWorkspaceId(String tenantDomain, String workspaceName) throws Exception;
    
    /**
     * get wrapped documentModel from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param id
     *            of the document to retrieve
     * @throws DocumentException
     */
    public DocumentWrapper<DocumentModel> getDoc(
    		ServiceContext ctx, String id)
            throws DocumentNotFoundException, DocumentException;

    /**
     * find wrapped documentModel from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param specifies docType. If null, uses ctx.getDocumentType()
     * @param where NXQL where clause to get the document
     * @throws DocumentException
     */
    public DocumentWrapper<DocumentModel> findDoc(
    		ServiceContext ctx, String where)
            throws DocumentNotFoundException, DocumentException;

    /**
     * find doc and return CSID from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param specifies docType. If null, uses ctx.getDocumentType()
     * @param where NXQL where clause to get the document
     * @throws DocumentException
     */
    public String findDocCSID(
    		ServiceContext ctx, String where)
            throws DocumentNotFoundException, DocumentException;

}
