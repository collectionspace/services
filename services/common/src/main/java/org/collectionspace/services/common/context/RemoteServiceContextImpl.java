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
import org.collectionspace.services.common.security.UnauthorizedException;
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
        //for security reasons, do not allow to set input again (from handlers)
        if (this.input != null) {
            String msg = "Resetting or changing an context's input is not allowed.";
            logger.error(msg);
            throw new IllegalStateException(msg);
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
    public CollectionSpaceResource<IT, OT> getResource(ServiceContext ctx) throws Exception {
    	CollectionSpaceResource<IT, OT> result = null;
    	
    	ResourceMap<IT, OT> resourceMap = ctx.getResourceMap();
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
    public void setResourceMap(ResourceMap map) {
    	this.resourceMap = map;
    }

 
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.RemoteServiceContext#getLocalContext(java.lang.String)
     */
    @Override
    public ServiceContext getLocalContext(String localContextClassName) throws Exception {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        Class<?> ctxClass = cloader.loadClass(localContextClassName);
        if (!ServiceContext.class.isAssignableFrom(ctxClass)) {
            throw new IllegalArgumentException("getLocalContext requires "
                    + " implementation of " + ServiceContext.class.getName());
        }

        Constructor ctor = ctxClass.getConstructor(java.lang.String.class);
        ServiceContext ctx = (ServiceContext) ctor.newInstance(getServiceName());
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
}
