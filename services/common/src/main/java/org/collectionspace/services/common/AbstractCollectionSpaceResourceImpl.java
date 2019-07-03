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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;
import org.collectionspace.services.config.service.CacheControlConfig;
import org.collectionspace.services.config.service.DocHandlerParams.Params.CacheControlConfigElement;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.DocHandlerParams.Params;
import org.collectionspace.services.description.ServiceDescription;

import org.jboss.resteasy.spi.HttpRequest;
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

    protected final ServiceContext<IT, OT> NULL_CONTEXT = null;
    // Fields for default client factory and client
    /** The repository client factory. */
    private RepositoryClientFactory<IT, OT> repositoryClientFactory;
    
    /** The repository client. */
    private RepositoryClient<IT, OT> repositoryClient;
    
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
        repositoryClientFactory = (RepositoryClientFactory<IT, OT>) RepositoryClientFactory.getInstance();
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
    synchronized public RepositoryClient<IT, OT> getRepositoryClient(ServiceContext<IT, OT> ctx) {
        if (repositoryClient != null){
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
    
    protected ServiceContext<IT, OT> createServiceContext(Request requestInfo, UriInfo uriInfo) throws Exception {
    	ServiceContext<IT, OT> result = this.createServiceContext(uriInfo);

    	result.setRequestInfo(requestInfo);

    	return result;
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
    public ServiceContext<IT, OT> createServiceContext(String serviceName) throws Exception {    	
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
        		ui, // The queryParams
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
    
    /*
     * Get the service description
     */
    @GET
    @Path(CollectionSpaceClient.SERVICE_DESCRIPTION_PATH)
    public ServiceDescription getDescription(@Context UriInfo uriInfo) {
    	ServiceDescription result = null;

    	ServiceContext<IT, OT>  ctx = null;
        try {
            ctx = createServiceContext(uriInfo);
            result = getDescription(ctx);
        } catch (Exception e) {
        	String errMsg = String.format("Request to get service description information for the '%s' service failed.",
        			this.getServiceContextFactory());
            throw bigReThrow(e, errMsg);
        }
        
        return result;
    }
    
    /**
     * Each resource can override this method if they need to.
     * 
     * @param ctx
     * @return
     */
    public ServiceDescription getDescription(ServiceContext<IT, OT> ctx) {
    	ServiceDescription result = new ServiceDescription();
    	
    	result.setDocumentType(getDocType(ctx.getTenantId()));
    	
    	return result;
    }    

    public void checkResult(Object resultToCheck, String csid, String serviceMessage) throws CSWebApplicationException {
        if (resultToCheck == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    serviceMessage + "csid=" + csid
                    + ": was not found.").type(
                    "text/plain").build();
            throw new CSWebApplicationException(response);
        }
    }

    protected void ensureCSID(String csid, String crudType) throws CSWebApplicationException {
        ensureCSID(csid, crudType, "csid");
    }

    protected void ensureCSID(String csid, String crudType, String whichCsid) throws CSWebApplicationException {
           if (logger.isDebugEnabled()) {
               logger.debug(crudType + " for " + getClass().getName() + " with csid=" + csid);
           }
           if (csid == null || "".equals(csid)) {
               logger.error(crudType + " for " + getClass().getName() + " missing csid!");
               Response response = Response.status(Response.Status.BAD_REQUEST).entity(crudType + " failed on " + getClass().getName() + ' '+whichCsid+'=' + csid).type("text/plain").build();
               throw new CSWebApplicationException(response);
           }
       }

    protected CSWebApplicationException bigReThrow(Throwable e, String serviceMsg) throws CSWebApplicationException {
        return bigReThrow(e, serviceMsg, "");
    }

    protected CSWebApplicationException bigReThrow(Throwable e, String serviceMsg, String csid) throws CSWebApplicationException {
    	boolean logException = true;
    	CSWebApplicationException result = null;
        Response response;
        String detail = Tools.errorToString(e, true);
        String detailNoTrace = Tools.errorToString(e, true, 3);
        
        if (e instanceof UnauthorizedException) {
            response = Response.status(Response.Status.UNAUTHORIZED).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            result = new CSWebApplicationException(e, response);

        } else if (e instanceof PermissionException) {
            response = Response.status(Response.Status.FORBIDDEN).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            result = new CSWebApplicationException(e, response);

        } else if (e instanceof DocumentNotFoundException) {
        	//
        	// Don't log this error unless we're in 'trace' mode
        	//
        	logException = false;
            response = Response.status(Response.Status.NOT_FOUND).entity(serviceMsg + " on " + getClass().getName() + " csid=" + csid).type("text/plain").build();
            result = new CSWebApplicationException(e, response);
            
        } else if (e instanceof TransactionException) {
            int code = ((TransactionException) e).getErrorCode();
            response = Response.status(code).entity(e.getMessage()).type("text/plain").build();
            result = new CSWebApplicationException(e, response);

        } else if (e instanceof BadRequestException) {
            int code = ((BadRequestException) e).getErrorCode();
            if (code == 0) {
                code = Response.Status.BAD_REQUEST.getStatusCode();
            }
            // CSPACE-1110
            response = Response.status(code).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            // return new WebApplicationException(e, code);
            result = new CSWebApplicationException(e, response);

        } else if (e instanceof DocumentException) {
            int code = ((DocumentException) e).getErrorCode();
            if (code == 0){
               code = Response.Status.BAD_REQUEST.getStatusCode();
            }
            // CSPACE-1110
            response = Response.status(code).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            // return new WebApplicationException(e, code);
            result = new CSWebApplicationException(e, response);
           
        } else if (e instanceof org.dom4j.DocumentException) {
            int code = Response.Status.BAD_REQUEST.getStatusCode();
            response = Response.status(code).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            result = new CSWebApplicationException(e, response);     
        } else if (e instanceof CSWebApplicationException) {
            // subresource may have already thrown this exception
            // so just pass it on
            result = (CSWebApplicationException) e;

        } else { // e is now instanceof Exception
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serviceMsg + " detail: " + detailNoTrace).type("text/plain").build();
            result = new CSWebApplicationException(e, response);
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
    
	@Override
	public boolean allowAnonymousAccess(HttpRequest request,
			Class<?> resourceClass) {
		return false;
	}
	
    /**
     * Returns a UriRegistry entry: a map of tenant-qualified URI templates
     * for the current resource, for all tenants
     * 
     * @return a map of URI templates for the current resource, for all tenants
     */
    public Map<UriTemplateRegistryKey,StoredValuesUriTemplate> getUriRegistryEntries() {
        Map<UriTemplateRegistryKey,StoredValuesUriTemplate> uriRegistryEntriesMap =
                new HashMap<UriTemplateRegistryKey,StoredValuesUriTemplate>();
        List<String> tenantIds = getTenantBindingsReader().getTenantIds();
        for (String tenantId : tenantIds) {
                uriRegistryEntriesMap.putAll(getUriRegistryEntries(tenantId, getDocType(tenantId), UriTemplateFactory.RESOURCE));
        }
        return uriRegistryEntriesMap;
    }
    
    /**
     * Returns a resource's document type.
     * 
     * @param tenantId
     * @return
     */
    @Override
    public String getDocType(String tenantId) {
        return getDocType(tenantId, getServiceName());
    }

    /**
     * Returns the document type associated with a specified service, within a specified tenant.
     * 
     * @param tenantId a tenant ID
     * @param serviceName a service name
     * @return the Nuxeo document type associated with that service and tenant.
     */
    // FIXME: This method may properly belong in a different services package or class.
    // Also, we need to check for any existing methods that may duplicate this one.
    protected String getDocType(String tenantId, String serviceName) {
        String docType = "";
        if (Tools.isBlank(tenantId)) {
            return docType;
        }
        ServiceBindingType sb = getTenantBindingsReader().getServiceBinding(tenantId, serviceName);
        if (sb == null) {
            return docType;
        }
        docType = sb.getObject().getName(); // Reads the Document Type from tenant bindings configuration
        return docType;
    }

	/**
     * Returns a UriRegistry entry: a map of tenant-qualified URI templates
     * for the current resource, for a specified tenants
     * 
     * @return a map of URI templates for the current resource, for a specified tenant
     */
    @Override
    public Map<UriTemplateRegistryKey,StoredValuesUriTemplate> getUriRegistryEntries(String tenantId,
            String docType, UriTemplateFactory.UriTemplateType type) {
        Map<UriTemplateRegistryKey,StoredValuesUriTemplate> uriRegistryEntriesMap =
                new HashMap<UriTemplateRegistryKey,StoredValuesUriTemplate>();
        UriTemplateRegistryKey key;
        if (Tools.isBlank(tenantId) || Tools.isBlank(docType)) {
            return uriRegistryEntriesMap;
        }
        key = new UriTemplateRegistryKey();
        key.setTenantId(tenantId);
        key.setDocType(docType); 
        uriRegistryEntriesMap.put(key, getUriTemplate(type));
        return uriRegistryEntriesMap;
    }
    
    /**
     * Returns a URI template of the appropriate type, populated with the
     * current service name as one of its stored values.
     *      * 
     * @param type a URI template type
     * @return a URI template of the appropriate type.
     */
    @Override
    public StoredValuesUriTemplate getUriTemplate(UriTemplateFactory.UriTemplateType type) {
        Map<String,String> storedValuesMap = new HashMap<String,String>();
        storedValuesMap.put(UriTemplateFactory.SERVICENAME_VAR, getServiceName());
        StoredValuesUriTemplate template =
                UriTemplateFactory.getURITemplate(type, storedValuesMap);
        return template;
    }

    /**
     * Returns a reader for reading values from tenant bindings configuration
     * 
     * @return a tenant bindings configuration reader
     */
    @Override
    public TenantBindingConfigReaderImpl getTenantBindingsReader() {
        return ServiceMain.getInstance().getTenantBindingConfigReader();
    }
    
    /**
     * Find a named CacheControlConfig instance.
     * @param element
     * @param cacheKey
     * @return
     */
    private CacheControlConfig getCacheControl(CacheControlConfigElement element, String cacheKey) {
    	CacheControlConfig result = null;
    	
    	List<CacheControlConfig> list = element.getCacheControlConfigList();
    	for (CacheControlConfig cacheControlConfig : list) {
    		if (cacheControlConfig.getKey().equalsIgnoreCase(cacheKey)) {
    			result = cacheControlConfig;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    /*
     * By default, use the request's URI and HTTP method to form the cache-key to use when looking up the
     * cache control configuration from the service bindings
     */
    protected CacheControl getDefaultCacheControl(ServiceContext<IT, OT> ctx) {
    	UriInfo uriInfo = ctx.getUriInfo();
    	Request requestInfo = ctx.getRequestInfo();
    	
    	if (uriInfo != null && requestInfo != null) try {
    		String resName = SecurityUtils.getResourceName(uriInfo);
    		String requestMethod = requestInfo.getMethod();
    		return getCacheControl(ctx, String.format("%s/%s", requestMethod, resName));  // example, "GET/blobs/*/content"
    	} catch (Exception e) {
    		logger.debug(e.getMessage(), e);
    	}
    	
    	return getCacheControl(ctx, "default"); // Look for a default one if we couldn't find based on the resource request
    }
    
    /**
     * FIXME: This code around cache control needs some documentation.
     * 
     * @param ctx
     * @param cacheKey
     * @return
     */
    protected CacheControl getCacheControl(ServiceContext<IT, OT> ctx, String cacheKey) {
    	CacheControl result = null;
    	
    	try {
			Params docHandlerParams = ServiceConfigUtils.getDocHandlerParams(ctx.getTenantId(), ctx.getServiceName());
			CacheControlConfig cacheControlConfig = getCacheControl(docHandlerParams.getCacheControlConfigElement(), cacheKey);
			if (cacheControlConfig != null) {
				result = new CacheControl();
				
				if (cacheControlConfig.isPrivate() != null) {
					result.setPrivate(cacheControlConfig.isPrivate());
				}
				
				if (cacheControlConfig.isNoCache() != null) {
					result.setNoCache(cacheControlConfig.isNoCache());
				}
				
				if (cacheControlConfig.isProxyRevalidate() != null) {
					result.setProxyRevalidate(cacheControlConfig.isProxyRevalidate());
				}
				
				if (cacheControlConfig.isMustRevalidate() != null) {
					result.setMustRevalidate(cacheControlConfig.isMustRevalidate());
				}
				
				if (cacheControlConfig.isNoStore() != null) {
					result.setNoStore(cacheControlConfig.isNoStore());
				}
				
				if (cacheControlConfig.isNoTransform() != null) {
					result.setNoTransform(cacheControlConfig.isNoTransform());
				}
				
				if (cacheControlConfig.getMaxAge() != null) { 
					result.setMaxAge(cacheControlConfig.getMaxAge().intValue());
				}
				
				if (cacheControlConfig.getSMaxAge() != null) {
					result.setSMaxAge(cacheControlConfig.getSMaxAge().intValue());
				}
			}
		} catch (DocumentException e) {
			result = null;
			logger.debug(String.format("Failed to retrieve CacheControlConfig with key '%s' from service bindings '%s'.", cacheKey, ctx.getServiceName()), e);
		} catch (NullPointerException npe) {
			result = null;
			//
			// NPE might mean optional cache-control config is missing -that's usually ok.
			logger.trace(npe.getLocalizedMessage(), npe);
		}

    	return result;
    }
    
    protected Response.ResponseBuilder setCacheControl(ServiceContext<IT, OT> ctx, Response.ResponseBuilder responseBuilder) {
    	CacheControl cacheControl = getDefaultCacheControl(ctx);
    	
    	if (cacheControl != null) {
			responseBuilder.cacheControl(cacheControl);
	    	logger.debug(String.format("Setting default CacheControl for service '%s' responses from the service bindings for tenant ID='%s'.",
	    			ctx.getServiceName(), ctx.getTenantId()));
    	}
    	
    	return responseBuilder;
    }	
}
