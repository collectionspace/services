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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractCollectionSpaceResourceImpl.
 *
 * @param <IT> the generic type
 * @param <OT> the generic type
 */
public abstract class AbstractCollectionSpaceResourceImpl<IT, OT>
        implements CollectionSpaceResource<IT, OT> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    // Fields for default client factory and client
    /** The repository client factory. */
    private RepositoryClientFactory repositoryClientFactory;
    
    /** The repository client. */
    private RepositoryClient repositoryClient;
    
    /** The storage client. */
    private StorageClient storageClient;

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    protected static String extractId(Response res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((List<Object>) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        return id;
    }    
    
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
        		null, // The resource map
        		(UriInfo)null, // The query params
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
        		(IT)null, // The input part
        		null, // The resource map
        		(UriInfo)null, // The queryParams
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }
    
    protected ServiceContext<IT, OT> createServiceContext(String serviceName, UriInfo ui) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		serviceName,
        		(IT)null, // The input part
        		null, // The resource map
        		(UriInfo)null, // The queryParams
        		(Class<?>)null  /*input type's Class*/);
        ctx.setUriInfo(ui);
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
        ServiceContext<IT, OT> ctx = createServiceContext(serviceName,
        		input,
        		null, // The resource map
        		(UriInfo)null, /*queryParams*/
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }
    
    protected ServiceContext<IT, OT> createServiceContext(String serviceName,
    		IT input,
    		UriInfo uriInfo) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(serviceName,
        		input,
        		null, // The resource map
        		uriInfo, /*queryParams*/
        		(Class<?>)null  /*input type's Class*/);
        return ctx;
    }
    
    protected ServiceContext<IT, OT> createServiceContext(UriInfo uriInfo) throws Exception {
        ServiceContext<IT, OT> ctx = createServiceContext(
        		(IT)null, /*input*/
        		uriInfo,
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
    
    protected ServiceContext<IT, OT> createServiceContext(IT input, UriInfo uriInfo) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		input,
        		uriInfo,
        		null ); // The class param/argument
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
        		(UriInfo)null, //queryParams,
        		theClass);
        return ctx;
    }
    
    protected ServiceContext<IT, OT> createServiceContext(IT input, Class<?> theClass, UriInfo uriInfo) throws Exception {    	
        ServiceContext<IT, OT> ctx = createServiceContext(
        		input,
        		uriInfo,
        		theClass);
        return ctx;
    }
    
    protected ServiceContext<IT, OT> createServiceContext(
    		String serviceName,
    		ResourceMap resourceMap,
    		UriInfo uriInfo) throws Exception {
    	ServiceContext<IT, OT> ctx = createServiceContext(
    			serviceName,
    			null, // The input object
    			resourceMap,
    			uriInfo,
    			null /* the class of the input type */);
    	return ctx;
    }
        
    protected ServiceContext<IT, OT> createServiceContext(
    		IT input,
    		ResourceMap resourceMap,
    		UriInfo uriInfo) throws Exception {
    	ServiceContext<IT, OT> ctx = createServiceContext(
    			this.getServiceName(),
    			input,
    			resourceMap,
    			uriInfo,
    			null /* the class of the input type */);
    	return ctx;
    }
    
    protected ServiceContext<IT, OT> createServiceContext(
    		String serviceName,
    		IT input,
    		ResourceMap resourceMap,
    		UriInfo uriInfo) throws Exception {
    	ServiceContext<IT, OT> ctx = createServiceContext(
    			serviceName,
    			input,
    			resourceMap,
    			uriInfo,
    			null /* the class of the input type */);
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
    private ServiceContext<IT, OT> createServiceContext(
    		IT input,
    		UriInfo uriInfo,
    		Class<?> theClass) throws Exception {
    	return createServiceContext(this.getServiceName(),
    			input,
    			null, // The resource map
    			uriInfo,
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
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		Class<?> theClass) throws Exception {
        ServiceContext<IT, OT> ctx = getServiceContextFactory().createServiceContext(
        		serviceName,
        		input,
        		resourceMap,
        		uriInfo,
        		theClass != null ? theClass.getPackage().getName() : null,
        		theClass != null ? theClass.getName() : null);
        if (theClass != null) {
            ctx.setProperty(ServiceContextProperties.ENTITY_CLASS, theClass);
        }
        
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

    public void checkResult(Object resultToCheck, String csid, String serviceMessage) throws WebApplicationException {
        if (resultToCheck == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    serviceMessage + "csid=" + csid
                    + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    protected void ensureCSID(String csid, String crudType) throws WebApplicationException {
        ensureCSID(csid, crudType, "csid");
    }

    protected void ensureCSID(String csid, String crudType, String whichCsid) throws WebApplicationException {
           if (logger.isDebugEnabled()) {
               logger.debug(crudType + " for " + getClass().getName() + " with csid=" + csid);
           }
           if (csid == null || "".equals(csid)) {
               logger.error(crudType + " for " + getClass().getName() + " missing csid!");
               Response response = Response.status(Response.Status.BAD_REQUEST).entity(crudType + " failed on " + getClass().getName() + ' '+whichCsid+'=' + csid).type("text/plain").build();
               throw new WebApplicationException(response);
           }
       }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg) throws WebApplicationException {
        return bigReThrow(e, serviceMsg, "");
    }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg, String csid) throws WebApplicationException {
    	boolean logException = true;
    	WebApplicationException result = null;
        Response response;
        
        String detail = Tools.errorToString(e, true);
        String detailNoTrace = Tools.errorToString(e, true, 3);
        if (e instanceof UnauthorizedException) {
            response = Response.status(Response.Status.UNAUTHORIZED).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            result = new WebApplicationException(response);

        } else if (e instanceof DocumentNotFoundException) {
        	//
        	// Don't log this error unless we're in 'trace' mode
        	//
        	logException = false;
            response = Response.status(Response.Status.NOT_FOUND).entity(serviceMsg + " on " + getClass().getName() + " csid=" + csid).type("text/plain").build();
            result = new WebApplicationException(response);

        } else if (e instanceof BadRequestException) {
            int code = ((BadRequestException) e).getErrorCode();
            if (code == 0) {
                code = Response.Status.BAD_REQUEST.getStatusCode();
            }
            // CSPACE-1110
            response = Response.status(code).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            // return new WebApplicationException(e, code);
            result = new WebApplicationException(response);

        } else if (e instanceof DocumentException) {
            int code = ((DocumentException) e).getErrorCode();
            if (code == 0){
               code = Response.Status.BAD_REQUEST.getStatusCode();
            }
            // CSPACE-1110
            response = Response.status(code).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            // return new WebApplicationException(e, code);
            result = new WebApplicationException(response);
           
        } else if (e instanceof WebApplicationException) {
            // subresource may have already thrown this exception
            // so just pass it on
            result = (WebApplicationException) e;

        } else { // e is now instanceof Exception
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serviceMsg + " detail: " + detailNoTrace).type("text/plain").build();
            result = new WebApplicationException(response);
        }
        //
        // Some exceptions like DocumentNotFoundException won't be logged unless we're in 'trace' mode
        //
        boolean traceEnabled = logger.isTraceEnabled();
        if (logException == true || traceEnabled == true) {
        	if (traceEnabled == true) {
        		logger.error(getClass().getName() + " detail: " + detail, e);
        	} else {
        		logger.error(getClass().getName() + " detail: " + detailNoTrace);
        	}
        }
        
        return result;
    }
}
