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

import java.util.Hashtable;
import java.util.List;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
//
// All of these Nuxeo specific classes should not be here.  This is supposed to be
// a repository-neutral interface.
//
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

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
public interface RepositoryClient<IT, OT> extends StorageClient {

    /**
     * createDomain creates a doamin in the default repository
     * @param space name
     * @return id of newly created domain space
     * @throws java.lang.Exception
     */
    public String createDomain(RepositoryDomainType repositoryDomain) throws Exception;

    /**
     * getDomainSpaceId gets id of the given domain
     * @param domainName
     * @return
     * @throws Exception
     */
    public String getDomainId(RepositoryDomainType repositoryDomain) throws Exception;

    /**
     * retrieveWorkspaceIds retrieve workspace ids for given domain
     * @param domainName
     * @return Hashtable<workspaceName, workspaceId>
     * @throws Exception
     */
    public Hashtable<String, String> retrieveWorkspaceIds(RepositoryDomainType repositoryDomain) throws Exception;

    /**
     * createWorkspace creates a workspace in default repository under given domain
     * @param tenantDomain domain representing tenant
     * @param workspaceName name of the workspace
     * @return id of newly created workspace
     * @throws java.lang.Exception
     */
    public String createWorkspace(RepositoryDomainType repositoryDomain, String workspaceName) throws Exception;

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
    @Deprecated
    public DocumentWrapper<DocumentModel> getDoc(
            ServiceContext<IT, OT> ctx, String id)
            throws DocumentNotFoundException, DocumentException;

    public DocumentWrapper<DocumentModel> getDocFromCsid(ServiceContext<IT, OT> ctx,
    		String csid)
            throws Exception;

    public String getDocURI(DocumentWrapper<DocumentModel> wrappedDoc) throws ClientException;

    /**
     * Find wrapped documentModel from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param specifies docType. If null, uses ctx.getDocumentType()
     * @param where NXQL where clause to get the document
     * @throws DocumentException
     */
    public DocumentWrapper<DocumentModel> findDoc(
            ServiceContext<IT, OT> ctx, String where)
            throws DocumentNotFoundException, DocumentException;

    /**
     * Find doc and return CSID from the Nuxeo repository
     * @param ctx service context under which this method is invoked
     * @param specifies docType. If null, uses ctx.getDocumentType()
     * @param where NXQL where clause to get the document
     * @throws DocumentException
     */
    public String findDocCSID(CoreSessionInterface repoSession, 
            ServiceContext<IT, OT> ctx, String where)
            throws DocumentNotFoundException, DocumentException;

    /**
     * Find a list of documentModels from the Nuxeo repository
     * @param ctx 
     * @param docTypes a list of DocType names to match
     * @param where the clause to qualify on
     * @param pageSize (0 for all of them)
     * @param pageNum  (0 for the first one)
     * @param computeTotal
     * @param domain the domain for the associated services
     * @return document wrapper
     * @throws DocumentNotFoundException 
     * @throws DocumentException 
     */
    public DocumentWrapper<DocumentModelList> findDocs(
            ServiceContext<IT, OT> ctx,
            List<String> docTypes,
            String where,
            int pageNum,
            int pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws DocumentNotFoundException, DocumentException;

    /**
     * Reindex a single resource/document.
     * 
     * @param ctx
     * @param id
     * @param handler
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
	boolean reindex(DocumentHandler handler, String csid, String indexid)
			throws DocumentNotFoundException, DocumentException;

	/**
	 * Reindex all resources/documents of a specified type
	 * 
	 * @param ctx
	 * @param handler
	 * @throws DocumentNotFoundException
	 * @throws DocumentException
	 */
	boolean reindex(DocumentHandler handler, String indexid)
			throws DocumentNotFoundException, DocumentException;

	/**
	 * Delete a list of objects
	 * 
	 * @param ctx
	 * @param idList
	 * @param handler
	 * @return
	 * @throws DocumentNotFoundException
	 * @throws DocumentException
	 * @throws TransactionException
	 */
	boolean delete(ServiceContext ctx, List<String> idList, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException, TransactionException;

	/**
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception 
	 */
	public CoreSessionInterface getRepositorySession(ServiceContext<IT, OT> ctx) throws Exception;
}
