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
     * @return the service context< i t, o t>
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
        if(theClass != null) {
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
           if (logger.isDebugEnabled()) {
               logger.debug(crudType + " for " + getClass().getName() + " with csid=" + csid);
           }
           if (csid == null || "".equals(csid)) {
               logger.error(crudType + " for " + getClass().getName() + " missing csid!");
               Response response = Response.status(Response.Status.BAD_REQUEST).entity(crudType + " failed on " + getClass().getName() + " csid=" + csid).type("text/plain").build();
               throw new WebApplicationException(response);
           }
       }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg) throws WebApplicationException {
        return bigReThrow(e, serviceMsg, "");
    }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg, String csid) throws WebApplicationException {
        Response response;
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getName(), e);
        }
        /*    ===== how RoleResource does it: =======
        } catch (BadRequestException bre) {
            response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.POST_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentException bre) {
            response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.POST_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.POST_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
         */

        if (e instanceof UnauthorizedException) {
            response = Response.status(Response.Status.UNAUTHORIZED).entity(serviceMsg + e.getMessage()).type("text/plain").build();
            return new WebApplicationException(response);

        } else if (e instanceof DocumentNotFoundException) {
            response = Response.status(Response.Status.NOT_FOUND).entity(serviceMsg + " on " + getClass().getName() + " csid=" + csid).type("text/plain").build();
            return new WebApplicationException(response);

        } else if (e instanceof BadRequestException) {
            int code = ((BadRequestException) e).getErrorCode();
            if (code == 0){
                code = Response.Status.BAD_REQUEST.getStatusCode();
            }
            return new WebApplicationException(e, code);

        } else if (e instanceof DocumentException){
            int code = ((DocumentException) e).getErrorCode();
            if (code == 0){
               code = Response.Status.BAD_REQUEST.getStatusCode();
            }
            return new WebApplicationException(e, code);

        } else if (e instanceof WebApplicationException) {
            // subresource may have already thrown this exception
            // so just pass it on
            return (WebApplicationException) e;

        } else { // e is now instanceof Exception
            String detail = Tools.errorToString(e, true);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serviceMsg + " detail: " + detail).type("text/plain").build();
            return new WebApplicationException(response);
        }
    }
}
