package org.collectionspace.services.common;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class SecurityResourceBase extends AbstractCollectionSpaceResourceImpl {

    final Logger logger = LoggerFactory.getLogger(SecurityResourceBase.class);

    public Response create(Object input) {
    	Response response = null;
    	
        try {
            ServiceContext ctx = createServiceContext(input, input.getClass());
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
    
    public Response create(JPATransactionContext jpaTransactionContext, Object input) {
    	Response response = null;
    	
        try {
            ServiceContext ctx = createServiceContext(jpaTransactionContext, input, input.getClass());
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

    private ServiceContext createServiceContext(JPATransactionContext jpaTransactionContext, Object input,
			Class<? extends Object> clazz) throws Exception {
        ServiceContext result = createServiceContext(input, clazz);
        
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
            ServiceContext ctx = createServiceContext((Object) null, objectClass, ui);            
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, csid);
        }
        checkResult(result, csid, ServiceMessages.GET_FAILED);
        return result;
    }
    
    public Object get(JPATransactionContext jpaTransactionContext, String csid, Class objectClass) {
        logger.debug("get with csid=" + csid);
        ensureCSID(csid, ServiceMessages.GET_FAILED + "csid");
        Object result = null;
        try {
            ServiceContext ctx = createServiceContext(jpaTransactionContext, (Object) null, objectClass);   
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
            ServiceContext ctx = createServiceContext((Object) null, objectClass, ui);
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

    public Object update(String csid, Object theUpdate, Class<?> objectClass) {
    	return update((UriInfo)null, csid, theUpdate, objectClass);
    }
    
	public Object update(UriInfo ui, String csid, Object theUpdate, Class objectClass) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateRole with csid=" + csid);
        }
        ensureCSID(csid, ServiceMessages.PUT_FAILED + this.getClass().getName());
        try {
            ServiceContext ctx = createServiceContext(theUpdate, objectClass, ui);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            return ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.PUT_FAILED, csid);
        }
    }
}
