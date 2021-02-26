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
package org.collectionspace.services.common.context;

import java.lang.reflect.Constructor;

import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.CollectionSpaceResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.TransactionContext;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoteServiceContextImpl
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RemoteServiceContextImpl<IT, OT>
        extends AbstractServiceContextImpl<IT, OT>
        implements RemoteServiceContext<IT, OT> {

    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(RemoteServiceContextImpl.class);
    
    //input stores original content as received over the wire
    /** The input. */
    private IT input;    
    /** The output. */
    private OT output;
    /** The target of the HTTP request **/
    
    //
    // Reference count for things like JPA connections
    //
    private int transactionConnectionRefCount = 0;
    
    //
    // RESTEasy context
    //
    JaxRsContext jaxRsContext;    
    ResourceMap resourceMap = null;
    
    @Override
    public void setJaxRsContext(JaxRsContext theJaxRsContext) {
    	this.jaxRsContext = theJaxRsContext;
    }
    
    @Override
    public JaxRsContext getJaxRsContext() {
    	return this.jaxRsContext;
    }

    /**
     * Instantiates a new remote service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected RemoteServiceContextImpl(String serviceName, UriInfo uriInfo) throws UnauthorizedException {
        super(serviceName, uriInfo);
    }

    /**
     * Instantiates a new remote service context impl. (This is "package" protected for the Factory class)
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected RemoteServiceContextImpl(String serviceName, IT theInput, UriInfo uriInfo) throws UnauthorizedException {
    	this(serviceName, uriInfo);
        this.input = theInput;        
    }

    /**
     * Instantiates a new remote service context impl. (This is "package" protected for the Factory class)
     * 
     * @param serviceName the service name
     * @param theInput the the input
     * @param queryParams the query params
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected RemoteServiceContextImpl(String serviceName,
    		IT theInput,
    		ResourceMap resourceMap,
    		UriInfo uriInfo) throws UnauthorizedException {
        this(serviceName, theInput, uriInfo);
        this.setResourceMap(resourceMap);
        this.setUriInfo(uriInfo);
        if (uriInfo != null) {
        	this.setQueryParams(uriInfo.getQueryParameters());
        }
    }

    /*
     * Returns the name of the service's acting repository.  Gets this from the tenant and service bindings files
     */
    @Override
	public String getRepositoryName() throws Exception {
    	String result = null;
    	
    	TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
    	String tenantId = this.getTenantId();
    	TenantBindingType tenantBindingType = tenantBindingConfigReader.getTenantBinding(tenantId);
    	ServiceBindingType serviceBindingType = this.getServiceBinding();
    	String servicesRepoDomainName = serviceBindingType.getRepositoryDomain();
    	if (servicesRepoDomainName != null && servicesRepoDomainName.trim().isEmpty() == false) {
    		result = ConfigUtils.getRepositoryName(tenantBindingType, servicesRepoDomainName);
    	} else {
    		String errMsg = String.format("The '%s' service for tenant ID=%s did not declare a repository domain in its service bindings.", 
    				serviceBindingType.getName(), tenantId);
    		throw new Exception(errMsg);
    	}
    	
    	return result;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.AbstractServiceContextImpl#getInput()
     */
    @Override
    public IT getInput() {
        return input;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.AbstractServiceContextImpl#setInput(java.lang.Object)
     */
    @Override
    public void setInput(IT input) {
    	if (logger.isDebugEnabled()) {
	        if (this.input != null) {
	            String msg = "\n#\n# Resetting or changing an context's input is not advised.\n#";
	            logger.warn(msg);
	        }
    	}
        this.input = input;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.AbstractServiceContextImpl#getOutput()
     */
    @Override
    public OT getOutput() {
        return output;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.AbstractServiceContextImpl#setOutput(java.lang.Object)
     */
    @Override
    public void setOutput(OT output) {
        this.output = output;
    }

    /**
     * Return the JAX-RS resource for the current context.
     * 
     * @param ctx
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
	public CollectionSpaceResource<IT, OT> getResource(ServiceContext<?, ?> ctx) throws Exception {
    	CollectionSpaceResource<IT, OT> result = null;
    	
    	ResourceMap resourceMap = ctx.getResourceMap();
    	String resourceName = ctx.getClient().getServiceName();
    	result = (CollectionSpaceResource<IT, OT>) resourceMap.get(resourceName);
    	
    	return result;
    }
    
    /**
     * @return the map of service names to resource classes.
     */
    @Override
    public ResourceMap getResourceMap() {
    	ResourceMap result = resourceMap;
    	
    	if (result == null) {
    		result = ServiceMain.getInstance().getJaxRSResourceMap();
    	}
    	
    	return result;
    }
    
    /**
     * @param map the map of service names to resource instances.
     */
    @Override
	public void setResourceMap(ResourceMap map) {
    	this.resourceMap = map;
    }

 
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.RemoteServiceContext#getLocalContext(java.lang.String)
     */
    @SuppressWarnings("unchecked")
	@Override
    public ServiceContext<IT, OT> getLocalContext(String localContextClassName) throws Exception {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        Class<?> ctxClass = cloader.loadClass(localContextClassName);
        if (!ServiceContext.class.isAssignableFrom(ctxClass)) {
            throw new IllegalArgumentException("getLocalContext requires "
                    + " implementation of " + ServiceContext.class.getName());
        }

        Constructor<?> ctor = ctxClass.getConstructor(java.lang.String.class);
        ServiceContext<IT, OT> ctx = (ServiceContext<IT, OT>) ctor.newInstance(getServiceName());
        return ctx;
    }

	@Override
	public CollectionSpaceResource<IT, OT> getResource() throws Exception {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public CollectionSpaceResource<IT, OT> getResource(String serviceName)
			throws Exception {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented method.");
	}
	
	//
	// Transaction management methods
	//
	
	@Override
	public TransactionContext getCurrentTransactionContext() {
		return (TransactionContext) this.getProperty(StorageClient.SC_TRANSACTION_CONTEXT_KEY);
	}

	@Override
	synchronized public void closeConnection() throws TransactionException {
		if (transactionConnectionRefCount == 0) {
			throw new TransactionException("Attempted to release a connection that doesn't exist or has already been released.");
		}

		if (isTransactionContextShared() == true) {
			//
			// If it's a shared connection, we can't close it.  Just reduce the refcount by 1
			//
			if (logger.isTraceEnabled()) {
				String traceMsg = "Attempted to release a shared storage connection.  Only the originator can release the connection";
				logger.trace(traceMsg);
			}
			transactionConnectionRefCount--;
		} else {
			TransactionContext transactionCtx = getCurrentTransactionContext();
			if (transactionCtx != null) {
				if (--transactionConnectionRefCount == 0) {
					try {
						transactionCtx.close();
					} finally {
				        this.setProperty(StorageClient.SC_TRANSACTION_CONTEXT_KEY, null);
					}
				}
			} else {
				throw new TransactionException("Attempted to release a non-existent storage connection.  Transaction context missing from service context.");
			}
		}
	}

	@Override
	synchronized public TransactionContext openConnection() throws TransactionException {
		TransactionContext result = getCurrentTransactionContext();
		
		if (result == null) {
			result = new JPATransactionContext(this);
	        this.setProperty(StorageClient.SC_TRANSACTION_CONTEXT_KEY, result);
		}
		transactionConnectionRefCount++;
		
		return result;
	}

	@Override
	public void setTransactionContext(TransactionContext transactionCtx) throws TransactionException {
		TransactionContext currentTransactionCtx = this.getCurrentTransactionContext();
		if (currentTransactionCtx == null) {
			setProperty(StorageClient.SC_TRANSACTION_CONTEXT_KEY, transactionCtx);
		} else if (currentTransactionCtx != transactionCtx) {
			throw new TransactionException("Transaction context already set from service context.");
		}
	}

	/**
	 * Returns true if the TransactionContext is shared with another ServiceContext instance
	 * @throws TransactionException 
	 */
	@Override
	public boolean isTransactionContextShared() throws TransactionException {
		boolean result = true;
		
		TransactionContext transactionCtx = getCurrentTransactionContext();
		if (transactionCtx != null) {
			if (transactionCtx.getServiceContext() == this) {  // check to see if the service context used to create the connection is the same as the current service context
				result = false;
			}
		} else {
			throw new TransactionException("Transaction context missing from service context.");
		}
		
		return result;
	}

	@Override
	public boolean hasActiveConnection() {
		return getCurrentTransactionContext() != null;
	}
}
