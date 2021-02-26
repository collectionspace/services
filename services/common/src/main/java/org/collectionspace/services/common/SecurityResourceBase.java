package org.collectionspace.services.common;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.storage.TransactionContext;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@SuppressWarnings("rawtypes")
public abstract class SecurityResourceBase<IT, OT> extends AbstractCollectionSpaceResourceImpl<IT, OT> {

    final Logger logger = LoggerFactory.getLogger(SecurityResourceBase.class);

    public Response create(IT input) {
        Response response = null;
        
        try {
            ServiceContext<IT, OT> ctx = createServiceContext(input, input.getClass());
            response = create(ctx, input);
        } catch (CSWebApplicationException we) {
            throw we;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED+"create in "+this.getClass().getName());
        }
        
        return response;
    }
    
    protected Response create(ServiceContext<IT, OT> ctx, IT input) {
        Response response = null;
        
        try {
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getStorageClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(this.getClass());
            path.path("" + csid);
            response = Response.created(path.build()).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED+"create in "+this.getClass().getName());
        }
        
        return response;
    }
    
    public Response create(JPATransactionContext jpaTransactionContext, IT input) {
        Response response = null;
        
        try {
            ServiceContext<IT, OT> ctx = createServiceContext(jpaTransactionContext, input, input.getClass());
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getStorageClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(this.getClass());
            path.path("" + csid);
            response = Response.created(path.build()).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED+"create in "+this.getClass().getName());
        }
        
        return response;
    }    

    private ServiceContext<IT, OT> createServiceContext(JPATransactionContext jpaTransactionContext, IT input,
            Class<? extends Object> clazz) throws Exception {
        ServiceContext<IT, OT> result = createServiceContext(input, clazz);
        
        if (jpaTransactionContext != null) {
               result.setTransactionContext(jpaTransactionContext);
        }
        
        return result;
    }

    public Object get(String csid, Class objectClass) {
        return get((UriInfo)null, csid, objectClass);
    }

    public Object get(UriInfo ui, String csid, Class objectClass) {
        logger.debug("get with csid=" + csid);
        ensureCSID(csid, ServiceMessages.GET_FAILED + "csid");
        Object result = null;
        
        try {
            ServiceContext<IT, OT> ctx = createServiceContext((IT) null, objectClass, ui);            
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (DocumentException e) {
            Exception cause = (Exception) e.getCause();
            if (cause instanceof NoResultException) {
                Response response = Response.status(Response.Status.NOT_FOUND).entity(result).type("text/plain").build();
                throw new CSWebApplicationException(response);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, csid);
        }
        
        checkResult(result, csid, ServiceMessages.GET_FAILED);
        return result;
    }
    
    protected Object get(TransactionContext transactionContext, String csid, Class<?> objectClass) {
        logger.debug("get with csid=" + csid);
        JPATransactionContext jpaTransactionContext = (JPATransactionContext)transactionContext;
        ensureCSID(csid, ServiceMessages.GET_FAILED + "csid");
        Object result = null;
        try {
            ServiceContext<IT, OT> ctx = createServiceContext(jpaTransactionContext, (IT) null, objectClass);   
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, csid);
        }
        checkResult(result, csid, ServiceMessages.GET_FAILED);
        return result;
    }    

    public Object getList(UriInfo ui, Class objectClass) {
        try {
            ServiceContext<IT, OT> ctx = createServiceContext((IT) null, objectClass, ui);
            DocumentHandler handler = createDocumentHandler(ctx);
            MultivaluedMap<String, String> queryParams = (ui != null ? ui.getQueryParameters() : null);
            DocumentFilter myFilter = handler.createDocumentFilter();
            myFilter.setPagination(queryParams);
            myFilter.setQueryParams(queryParams);
            handler.setDocumentFilter(myFilter);
            getStorageClient(ctx).getFiltered(ctx, handler);
            //RolesList roleList = (RolesList) handler.getCommonPartList();
            return handler.getCommonPartList();
            //return roleList;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }
    
    protected OT sanitize(DocumentHandler handler, OT outputObject) {
        DocumentWrapper<OT> wrapDoc = new DocumentWrapperImpl<OT>(outputObject);
        handler.sanitize(wrapDoc);
        return outputObject;
    }    

    public Object update(String csid, IT theUpdate, Class<?> objectClass) {
        return update((UriInfo)null, csid, theUpdate, objectClass);
    }
    
    public Object update(UriInfo ui, String csid, IT theUpdate, Class objectClass) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateRole with csid=" + csid);
        }
        ensureCSID(csid, ServiceMessages.PUT_FAILED + this.getClass().getName());
        try {
            ServiceContext<IT, OT> ctx = createServiceContext(theUpdate, objectClass, ui);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            return sanitize(handler, ctx.getOutput());
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.PUT_FAILED, csid);
        }
    }
    
    public Object update(ServiceContext<?, ?> parentCtx, UriInfo ui, String csid, IT theUpdate, Class objectClass) {
    	return update(parentCtx, ui, csid, theUpdate, objectClass, true);
    }

    public Object update(ServiceContext<?, ?> parentCtx, UriInfo ui, String csid, IT theUpdate, Class objectClass, boolean sanitize) {
    	Object result = null;
    	
        if (logger.isDebugEnabled()) {
            logger.debug("updateRole with csid=" + csid);
        }
        ensureCSID(csid, ServiceMessages.PUT_FAILED + this.getClass().getName());
        
        try {
            ServiceContext<IT, OT> ctx = createServiceContext(parentCtx, theUpdate, objectClass, ui);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            if (sanitize == true) {
            	result = sanitize(handler, ctx.getOutput());
            } else {
            	result = ctx.getOutput();
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.PUT_FAILED, csid);
        }
        
        return result;
    }

    protected ServiceContext<IT, OT> createServiceContext(
            ServiceContext<?, ?> parentCtx,
            IT input,
            Class<?> theClass,
            UriInfo uriInfo) throws Exception {
        ServiceContext<IT, OT> ctx = createServiceContext(input, theClass, uriInfo);
        JPATransactionContext parentTransactionContext = parentCtx != null ? (JPATransactionContext)parentCtx.getCurrentTransactionContext() : null;
        //
        // If the parent context has an active JPA connection then we'll use it.
        //
        if (parentTransactionContext != null) {
            ctx.setTransactionContext(parentTransactionContext);
        }
        
        return ctx;
    }    
}
