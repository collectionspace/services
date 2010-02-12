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
package org.collectionspace.services.common;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;

/**
 * The Class AbstractCollectionSpaceResource.
 */
public abstract class AbstractCollectionSpaceResourceImpl
        implements CollectionSpaceResource {

    // Fields for default client factory and client
    /** The repository client factory. */
    private RepositoryClientFactory repositoryClientFactory;
    
    /** The repository client. */
    private RepositoryClient repositoryClient;
    
    /** The storage client. */
    private StorageClient storageClient;

    /**
     * Instantiates a new abstract collection space resource.
     */
    public AbstractCollectionSpaceResourceImpl() {
        repositoryClientFactory = RepositoryClientFactory.getInstance();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getServiceName()
     */
    @Override
    abstract public String getServiceName();


    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getRepositoryClient(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    synchronized public RepositoryClient getRepositoryClient(ServiceContext ctx) {
        if(repositoryClient != null){
            return repositoryClient;
        }
        repositoryClient = repositoryClientFactory.getClient(ctx.getRepositoryClientName());
        return repositoryClient;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getStorageClient(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    synchronized public StorageClient getStorageClient(ServiceContext ctx) {
        if(storageClient != null) {
            return storageClient;
        }
        storageClient = new JpaStorageClientImpl();
        return storageClient;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    abstract public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception ;
    
    /**
     * Gets the version string.
     * 
     * @return the version string
     */
    abstract protected String getVersionString();
    
    /**
     * Gets the version.
     * 
     * @return the version
     */
    @GET
    @Path("/version")    
    @Produces("application/xml")
    public Version getVersion() {
    	Version result = new Version();
    	
    	result.setVersionString(getVersionString());
    	
    	return result;
    }
    
}
