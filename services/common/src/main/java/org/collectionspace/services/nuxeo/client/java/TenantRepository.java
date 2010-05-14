/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.nuxeo.client.java;

import java.util.Hashtable;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.RepositoryClientConfigType;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TenatnRepository is used to retrieve workspaces for a tenant as well as to
 * create workspaces for each service used by a teant
 * @author
 */
public class TenantRepository {

    final private static TenantRepository self = new TenantRepository();
    
    final Logger logger = LoggerFactory.getLogger(TenantRepository.class);
    //tenant-qualified service, workspace
    private Hashtable<String, String> serviceWorkspaces = new Hashtable<String, String>();

    private TenantRepository() {
        
    }

    public static TenantRepository get() {
        return self;
    }
    
    /**
     * retrieveWorkspaceIds is called at initialization time to retrieve
     * workspace ids of all the tenants
     * @param hashtable <tenant name, tenantbinding>
     * @throws Exception
     */
    synchronized public void retrieveAllWorkspaceIds(Hashtable<String, TenantBindingType> tenantBindings)
            throws Exception {
        for (TenantBindingType tenantBinding : tenantBindings.values()) {
            retrieveWorkspaceIds(tenantBinding);
        }
    }

    /**
     * retrieveWorkspaceIds retrieves workspace ids for services used by
     * the given tenant
     * @param tenantBinding
     * @throws Exception
     */
    synchronized public void retrieveWorkspaceIds(TenantBindingType tenantBinding) throws Exception {
        Hashtable<String, String> workspaceIds = new Hashtable<String, String>();
        ServiceMain svcMain = ServiceMain.getInstance();
        RepositoryClientConfigType rclientConfig = svcMain.getServicesConfigReader().getConfiguration().getRepositoryClient();
        ClientType clientType = svcMain.getClientType();
        if (clientType.equals(ClientType.JAVA)
                && rclientConfig.getName().equalsIgnoreCase("nuxeo-java")) {
            //FIXME only one repository client is recognized
            workspaceIds = svcMain.getNuxeoConnector().retrieveWorkspaceIds(
                    tenantBinding.getRepositoryDomain());
        }
        //verify if workspace exists for each service in the tenant binding
        for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
            String serviceName = serviceBinding.getName();
            String repositoryClientName = serviceBinding.getRepositoryClient();
            if (repositoryClientName == null) {
                //no repository needed for this service...skip
                if (logger.isInfoEnabled()) {
                    logger.info("The service " + serviceName
                            + " does not seem to require a document repository.");
                }
                continue;
            }

            if (repositoryClientName.isEmpty()) {
                String msg = "Invalid repositoryClient " + serviceName;
                logger.error(msg);
                continue;
            }
            repositoryClientName = repositoryClientName.trim();
            RepositoryClient repositoryClient = getRepositoryClient(
                    repositoryClientName);
            if (repositoryClient == null) {
                String msg = "Could not find repositoryClient " + repositoryClientName
                        + " for service=" + serviceName;
                logger.error(msg);
                continue;
            }
            String workspaceId = null;
            //workspace name is service name by convention
            String workspace = serviceName.toLowerCase();
            if (clientType.equals(ClientType.JAVA)) {
                workspaceId = workspaceIds.get(workspace);
                if (workspaceId == null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to retrieve workspace ID for " + workspace
                                + " from repository, trying to create a new workspace ...");
                    }
                    workspaceId = repositoryClient.createWorkspace(
                            tenantBinding.getRepositoryDomain(),
                            serviceBinding.getName());
                    if (workspaceId == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Failed to create workspace in repository"
                                    + " for service=" + workspace);
                        }
                        continue;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully created workspace in repository"
                                + " id=" + workspaceId + " for service=" + workspace);
                    }
                }
            } else {
                workspaceId = serviceBinding.getRepositoryWorkspaceId();
                if (workspaceId == null || "".equals(workspaceId)) {
                    logger.error("Could not find workspace in repository for"
                            + " service=" + workspace);
                    //FIXME: should we throw an exception here?
                    continue;
                }
            }
            String tenantService =
                    TenantBindingConfigReaderImpl.getTenantQualifiedServiceName(tenantBinding.getId(), serviceName);
            serviceWorkspaces.put(tenantService, workspaceId);
            if (logger.isInfoEnabled()) {
                logger.info("Created/retrieved repository workspace="
                        + workspace + " id=" + workspaceId
                        + " for service=" + serviceName);
            }
        }
    }

    /**
     * getWorkspaceId for a tenant's service
     * @param tenantId
     * @param serviceName
     * @return workspace id
     */
    public String getWorkspaceId(String tenantId, String serviceName) {
        String tenantService =
                TenantBindingConfigReaderImpl.getTenantQualifiedServiceName(tenantId, serviceName);
        return serviceWorkspaces.get(tenantService);
    }

    
    private RepositoryClient getRepositoryClient(String clientName) {
        return RepositoryClientFactory.getInstance().getClient(clientName);
    }
}
