package org.collectionspace.services.common;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 * Author: laramie
 */
public abstract class ResourceBase
extends AbstractMultiPartCollectionSpaceResourceImpl {

    public static final String CREATE = "create";
    public static final String READ   = "get";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String LIST   = "list";

    //FIXME retrieve client type from configuration
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    protected void ensureCSID(String csid, String crudType) throws WebApplicationException {
        if (logger.isDebugEnabled()) {
            logger.debug(crudType+" for "+getClass().getName()+" with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error(crudType+" for " + getClass().getName() + " missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST)
                                        .entity("update failed on " + getClass().getName() + " csid=" + csid)
                                        .type("text/plain")
                                        .build();
            throw new WebApplicationException(response);
        }
    }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg)
    throws WebApplicationException {
        return bigReThrow(e, serviceMsg, "");
    }

    protected WebApplicationException bigReThrow(Exception e, String serviceMsg, String csid)
        throws WebApplicationException {
                Response response;
        if (logger.isDebugEnabled()) {
             logger.debug(getClass().getName(), e);
        }
        if (e instanceof UnauthorizedException) {
            response = Response.status(Response.Status.UNAUTHORIZED)
                               .entity(serviceMsg + e.getMessage())
                               .type("text/plain")
                               .build();
             return new WebApplicationException(response);
        } else if (e instanceof DocumentNotFoundException) {
            response = Response.status(Response.Status.NOT_FOUND)
                               .entity(serviceMsg + " on "+getClass().getName()+" csid=" + csid)
                               .type("text/plain")
                               .build();
            return new WebApplicationException(response);
        } else {  //e is now instanceof Exception
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity(serviceMsg)
                               .type("text/plain")
                               .build();
            return new WebApplicationException(response);
        }
    }

    //======================= CREATE ====================================================

    @POST
    public Response create(MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
            return create(input, ctx);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }
    }
    
    protected Response create(MultipartInput input, ServiceContext<MultipartInput, MultipartOutput> ctx) {
        try {
            DocumentHandler handler = createDocumentHandler(ctx);
            UriBuilder path = UriBuilder.fromResource(this.getClass());
            return create(input, ctx, handler, path); //==> CALL implementation method, which subclasses may override.
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }
    }

    /** Subclasses may override this overload, which gets called from @see #create(MultipartInput)   */
    protected Response create(MultipartInput input,
                              ServiceContext<MultipartInput, MultipartOutput> ctx,
                              DocumentHandler handler,
                              UriBuilder path)
    throws Exception {
        String csid = getRepositoryClient(ctx).create(ctx, handler);
        path.path("" + csid);
        Response response = Response.created(path.build()).build();
        return response;
    }

    //======================= UPDATE ====================================================

    @PUT
    @Path("{csid}")
    public MultipartOutput update(@PathParam("csid") String csid, MultipartInput theUpdate) {
        ensureCSID(csid, UPDATE);
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            return update(csid, theUpdate, ctx, handler); //==> CALL implementation method, which subclasses may override.
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED, csid);
        }
    }

    /** Subclasses may override this overload, which gets called from #udpate(String,MultipartInput)   */
    protected MultipartOutput update(String csid,
                                     MultipartInput theUpdate,
                                     ServiceContext<MultipartInput, MultipartOutput> ctx,
                                     DocumentHandler handler)
    throws Exception {
        getRepositoryClient(ctx).update(ctx, csid, handler);
        return (MultipartOutput) ctx.getOutput();
    }

    //======================= DELETE ====================================================

    @DELETE
    @Path("{csid}")
    public Response delete(@PathParam("csid") String csid) {
        ensureCSID(csid, DELETE);
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            return delete(csid, ctx);  //==> CALL implementation method, which subclasses may override.
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }

    /** subclasses may override this method, which is called from #delete(String)
     *  which handles setup of ServiceContext, and does Exception handling.  */
    protected Response delete(String csid, ServiceContext<MultipartInput, MultipartOutput> ctx)
    throws Exception {
        getRepositoryClient(ctx).delete(ctx, csid);
        return Response.status(HttpResponseCodes.SC_OK).build();   
    }


    //======================= GET ====================================================

    @GET
    @Path("{csid}")
    public MultipartOutput get(@PathParam("csid") String csid) {
        ensureCSID(csid, READ);
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            MultipartOutput result = get(csid, ctx);// ==> CALL implementation method, which subclasses may override.
            if (result == null) {
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
                throw new WebApplicationException(response);
            }
            return result;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }
    }
    
    protected MultipartOutput get(@PathParam("csid") String csid,
    		ServiceContext<MultipartInput, MultipartOutput> ctx) throws Exception {
    	MultipartOutput result = null;
    	
    	ensureCSID(csid, READ);
        DocumentHandler handler = createDocumentHandler(ctx);
        result = get(csid, ctx, handler);
        if (result == null) {
            String msg = "Could not find document with id = " + csid;
            if (logger.isErrorEnabled() == true) {
            	logger.error(msg);
            }
            throw new DocumentNotFoundException(msg);
        }
        
        return result;
    }
    

    /** subclasses may override this method, which is called from #get(String)
     *  which handles setup of ServiceContext and DocumentHandler, and Exception handling.*/
    protected MultipartOutput get(String csid,
                               ServiceContext<MultipartInput, MultipartOutput> ctx,
                               DocumentHandler handler)
    throws Exception {
        getRepositoryClient(ctx).get(ctx, csid, handler);
        return (MultipartOutput) ctx.getOutput();
    }

    //======================= GET without csid. List, search, etc. =====================================

	@GET
	@Produces("application/xml")
	public AbstractCommonList getList(@Context UriInfo ui,
			@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		if (keywords != null) {
			return search(queryParams, keywords);
		} else {
			return getList(queryParams);
		}
	}

    protected AbstractCommonList getList(MultivaluedMap<String, String> queryParams) {
        try {
            ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            return (AbstractCommonList)handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }

    protected AbstractCommonList search(MultivaluedMap<String, String> queryParams, String keywords) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            // perform a keyword search
            if (keywords != null && !keywords.isEmpty()) {
            	String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
	            DocumentFilter documentFilter = handler.getDocumentFilter();
	            documentFilter.setWhereClause(whereClause);
	            if (logger.isDebugEnabled()) {
	            	logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
	            }
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            return (AbstractCommonList) handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.SEARCH_FAILED);
        }
    }

    //FIXME: REM - This should not be @Deprecated since we may want to implement this -it has been on the wish list.
    @Deprecated
    public AbstractCommonList getList(List<String> csidList) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csidList, handler);
            return (AbstractCommonList)handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }

    //======================== GET : getAuthorityRefs ========================================

    @GET
    @Path("{csid}/authorityrefs")
    @Produces("application/xml")
    public AuthorityRefList getAuthorityRefs(
    		@PathParam("csid") String csid,
    		@Context UriInfo ui) {
    	AuthorityRefList authRefList = null;
        try {
        	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentWrapper<DocumentModel> docWrapper = getRepositoryClient(ctx).getDoc(ctx, csid);
            DocumentModelHandler<MultipartInput, MultipartOutput> handler = (DocumentModelHandler<MultipartInput, MultipartOutput>)createDocumentHandler(ctx);
            List<String> authRefFields =
            	    ((MultipartServiceContextImpl) ctx).getCommonPartPropertyValues(
        			ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            authRefList = handler.getAuthorityRefs(docWrapper, authRefFields);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.AUTH_REFS_FAILED, csid);
        }
        return authRefList;
    }
    
}
