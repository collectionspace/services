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
import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;

/**
 * The Class AbstractCollectionSpaceResource.
 */
public abstract class AbstractCollectionSpaceResourceImpl<IT, OT>
        implements CollectionSpaceResource<IT, OT> {

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
    synchronized public RepositoryClient getRepositoryClient(ServiceContext<IT, OT> ctx) {
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
    synchronized public StorageClient getStorageClient(ServiceContext<IT, OT> ctx) {
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
    public DocumentHandler createDocumentHandler(ServiceContext<IT, OT> ctx) throws Exception {
        DocumentHandler docHandler = createDocumentHandler(ctx, ctx.getInput());
        return docHandler;
    }
    
    /**
     * Creates the document handler.
     * 
     * @param ctx the ctx
     * @param commonPart the common part
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    public DocumentHandler createDocumentHandler(ServiceContext<IT, OT> ctx,
    		Object commonPart) throws Exception {
        DocumentHandler docHandler = ctx.getDocumentHandler();
        docHandler.setCommonPart(commonPart);
        return docHandler;
    }    
    
    /**
     * Creates the service context.
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext() throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(this.getServiceName(),
        		(IT)null, //inputType
        		(MultivaluedMap<String, String>)null, /*queryParams*/
        		this.getCommonPartClass());
        return ctx;
    }    
    
    /**
     * Creates the service context.
     * 
     * @param serviceName the service name
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(String serviceName) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		serviceName,
        		(IT)null, /*input*/
        		(MultivaluedMap<String, String>)null, /*queryParams*/
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }
    
    /**
     * Creates the service context.
     * 
     * @param serviceName the service name
     * @param input the input
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(String serviceName,
    		IT input) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(serviceName, input,
        		(MultivaluedMap<String, String>)null, /*queryParams*/
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }
    
    /**
     * Creates the service context.
     * 
     * @param serviceName the service name
     * @param input the input
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(String serviceName,
    		MultivaluedMap<String, String> queryParams) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(serviceName,
        		(IT)null,
        		queryParams,
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }    

    /**
     * Creates the service context.
     * 
     * @param queryParams the query params
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(MultivaluedMap<String, String> queryParams) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		(IT)null, /*input*/
        		queryParams,
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }    
        
    /**
     * Creates the service context.
     * 
     * @param input the input
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(IT input) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		input,
        		(Class<?>)null /*input type's Class*/);
        return ctx;
    }
    
    /**
     * Creates the service context.
     * 
     * @param input the input
     * @param theClass the the class
     * 
     * @return the service context
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(IT input, Class<?> theClass) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		input,
        		(MultivaluedMap<String, String>)null, //queryParams,
        		theClass);
        return ctx;
    }
    
    /**
     * Creates the service context.
     * 
     * @param input the input
     * @param queryParams the query params
     * @param theClass the the class
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    protected ServiceContext<IT, OT> createServiceContext(
    		IT input,
    		MultivaluedMap<String, String> queryParams,
    		Class<?> theClass) throws Exception {
    	return createServiceContext(this.getServiceName(),
    			input,
    			queryParams,
    			theClass);
    }

    /**
     * Creates the service context.
     * 
     * @param serviceName the service name
     * @param input the input
     * @param queryParams the query params
     * @param theClass the the class
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    private ServiceContext<IT, OT> createServiceContext(
    		String serviceName,
    		IT input,
    		MultivaluedMap<String, String> queryParams,
    		Class<?> theClass) throws Exception {
        ServiceContext<IT, OT> ctx = getServiceContextFactory().createServiceContext(
        		serviceName,
        		input,
        		queryParams,
        		theClass != null ? theClass.getPackage().getName() : null,
        		theClass != null ? theClass.getName() : null);
        return ctx;
    }
        
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
