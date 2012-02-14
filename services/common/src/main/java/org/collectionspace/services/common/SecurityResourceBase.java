package org.collectionspace.services.common;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public abstract class SecurityResourceBase extends AbstractCollectionSpaceResourceImpl {

    final Logger logger = LoggerFactory.getLogger(SecurityResourceBase.class);

    public Response create(Object input) {
            try {
                ServiceContext ctx = createServiceContext(input, input.getClass());
                DocumentHandler handler = createDocumentHandler(ctx);
                String csid = getStorageClient(ctx).create(ctx, handler);
                UriBuilder path = UriBuilder.fromResource(this.getClass());
                path.path("" + csid);
                Response response = Response.created(path.build()).build();
                return response;
            } catch (Exception e) {
                throw bigReThrow(e, ServiceMessages.POST_FAILED+"create in "+this.getClass().getName());
            }
        }

    public Object get(String csid, Class objectClass) {

        logger.debug("get with csid=" + csid);
        ensureCSID(csid, ServiceMessages.GET_FAILED + "csid");
        Object result = null;
        try {
            ServiceContext ctx = createServiceContext((Object) null, objectClass);
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
            ServiceContext ctx = createServiceContext((Object) null, objectClass);
            DocumentHandler handler = createDocumentHandler(ctx);
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
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

    public Object update(@PathParam("csid") String csid, Object theUpdate, Class objectClass) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateRole with csid=" + csid);
        }
        ensureCSID(csid, ServiceMessages.PUT_FAILED + this.getClass().getName());
        try {
            ServiceContext ctx = createServiceContext(theUpdate, objectClass);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            return ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.PUT_FAILED, csid);
        }
    }
}
