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

import org.collectionspace.services.common.storage.StorageClient;

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
}
