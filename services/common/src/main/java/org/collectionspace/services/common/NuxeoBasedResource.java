/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthRefConfigInfo;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.service.DocHandlerParams;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.description.ServiceDescription;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 * Author: Laramie Crocker
 */
public abstract class NuxeoBasedResource
        extends AbstractMultiPartCollectionSpaceResourceImpl {

    public static final String CREATE = "create";
    public static final String READ = "get";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String LIST = "list";
    //FIXME retrieve client type from configuration
    static ClientType CLIENT_TYPE;

    /*
     * REM - 11/14/2011 - I discovered this static block of code and don't understand why it exists.  However, a side-effect of this static block is that ServiceMain is trying
     * to create a valid instance of entire CSpace services include an embedded Nuxeo instance.  This block of code seems goofy and unnecessary and probably should be removed?
     */
    static {
        try {
            // I put this in a try-catch static block instead of file-level static var initializer so that static methods of
            // *Resource classes may be called statically from test cases.
            // Without this catch, you can't even access static methods of a *Resource class for testing.
            CLIENT_TYPE = ServiceMain.getInstance().getClientType();
            //System.out.println("Static initializer in ResourceBase. CLIENT_TYPE:"+CLIENT_TYPE);
        } catch (Throwable t) {
            System.out.println("Static initializer failed in ResourceBase because not running from deployment.  OK to use Resource classes statically for tests.");
        }
    }

    //======================= REINDEX ====================================================
    @GET
    @Path("{csid}/index/{indexid}")
    public Response reindex(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String csid,
            @PathParam("indexid") String indexid) {
    	uriInfo = new UriInfoWrapper(uriInfo);
       	Response result = Response.status(Response.Status.OK).entity("Reindex complete.").type("text/plain").build();
       	boolean success = false;

        ensureCSID(csid, READ);
        try {
            RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = (RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext(uriInfo);
            DocumentHandler handler = createDocumentHandler(ctx);
            success = getRepositoryClient(ctx).reindex(handler, csid, indexid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.REINDEX_FAILED, csid);
        }

        if (success == false) {
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.REINDEX_FAILED + ServiceMessages.resourceNotReindexedMsg(csid)).type("text/plain").build();
            throw new CSWebApplicationException(response);
        }

       	return result;
    }

    //======================= REINDEX ====================================================
    @GET
    @Path("index/{indexid}")
    public Response reindex(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("indexid") String indexid) {
    	uriInfo = new UriInfoWrapper(uriInfo);
       	Response result = Response.noContent().build();
       	boolean success = false;
       	String docType = null;

        try {
            RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = (RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext(uriInfo);
            docType = ctx.getTenantQualifiedDoctype(); // this will used in the error message if an error occurs
            DocumentHandler handler = createDocumentHandler(ctx);
            success = getRepositoryClient(ctx).reindex(handler, indexid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.REINDEX_FAILED);
        }

        if (success == false) {
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.REINDEX_FAILED + ServiceMessages.resourceNotReindexedMsg(docType)).type("text/plain").build();
            throw new CSWebApplicationException(response);
        }

       	return result;
    }


    //======================= CREATE ====================================================

    @POST
    public Response create(
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
            String xmlPayload) {
    	uriInfo = new UriInfoWrapper(uriInfo);
        return this.create(null, resourceMap, uriInfo, xmlPayload);
    }

    public Response create(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, // REM: 8/13/2012 - Some sub-classes will override this method -e.g., MediaResource does.
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
            String xmlPayload) {
    	Response result = null;

        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input, resourceMap, uriInfo);
            ctx.setResourceMap(resourceMap);
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
            	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
            result = create(input, ctx);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }

        return result;
    }

    protected Response create(PoxPayloadIn input, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        try {
            DocumentHandler<PoxPayloadIn, PoxPayloadOut, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            UriBuilder path = UriBuilder.fromResource(this.getClass());
            return create(input, ctx, handler, path); //==> CALL implementation method, which subclasses may override.
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }
    }

    /** Subclasses may override this overload, which gets called from @see #create(MultipartInput)   */
    protected Response create(PoxPayloadIn input,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            DocumentHandler<PoxPayloadIn, PoxPayloadOut, DocumentModel, DocumentModelList> handler,
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
    public byte[] update(@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid,
    		String xmlPayload) {
    	uriInfo = new UriInfoWrapper(uriInfo);
        return this.update(null, resourceMap, uriInfo, csid, xmlPayload);
    }

    /**
     *
     * @param parentCtx
     * @param resourceMap
     * @param uriInfo
     * @param csid
     * @param xmlPayload
     * @return
     */
    public byte[] update(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, // REM: 8/13/2012 - Some sub-classes will override this method -e.g., MediaResource does.
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid,
    		String xmlPayload) {
        PoxPayloadOut result = null;

        ensureCSID(csid, UPDATE);
        try {
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            result = update(parentCtx, resourceMap, uriInfo, csid, theUpdate);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED, csid);
        }

        return result.getBytes();
    }

    /**
     * This method is used to synchronize data with the SAS (Shared Authority Server).
     *
     * @param parentCtx
     * @param resourceMap
     * @param uriInfo
     * @param csid
     * @param theUpdate
     * @return
     * @throws Exception
     */
    public PoxPayloadOut update(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, // REM: 4/6/2016 - Some sub-classes will override this method -e.g., MediaResource does.
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		String csid,
    		PoxPayloadIn theUpdate) throws Exception {
    	PoxPayloadOut result = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(theUpdate, uriInfo);
        ctx.setResourceMap(resourceMap);
        if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
        	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
        	ctx.setProperties(parentCtx.getProperties()); // transfer all the parent properties to the current context
        }
        result = update(csid, theUpdate, ctx); //==> CALL implementation method, which subclasses may override.

    	return result;
    }

    /** Subclasses may override this overload, which gets called from #udpate(String,MultipartInput)   */
    protected PoxPayloadOut update(String csid,
            PoxPayloadIn theUpdate, // not used in this method, but could be used by an overriding method
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception {
        DocumentHandler handler = createDocumentHandler(ctx);
        getRepositoryClient(ctx).update(ctx, csid, handler);
        return ctx.getOutput();
    }

    /** Subclasses may override this overload, which gets called from #udpate(String,MultipartInput)   */
    protected PoxPayloadOut update(String csid,
            MultipartInput theUpdate,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            DocumentHandler handler)
            throws Exception {
        getRepositoryClient(ctx).update(ctx, csid, handler);
        return ctx.getOutput();
    }

    //======================= DELETE ====================================================
    @DELETE
    @Path("{csid}")
    public Response delete(@PathParam("csid") String csid) {
        ensureCSID(csid, DELETE);
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            return delete(ctx, csid);  //==> CALL implementation method, which subclasses may override.
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }

    /** subclasses may override this method, which is called from #delete(String)
     *  which handles setup of ServiceContext, and does Exception handling.  */
    protected Response delete(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid)
            throws Exception {
    	DocumentHandler<PoxPayloadIn, PoxPayloadOut, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
        getRepositoryClient(ctx).delete(ctx, csid, handler);
        return Response.status(HttpResponseCodes.SC_OK).build();
    }

    public Response deleteWithParentCtx(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
    		String csid)
            throws Exception {
        ensureCSID(csid, DELETE);
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
            	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // reuse the repo session if one exists
            }
            return delete(ctx, csid);  //==> CALL implementation method, which subclasses may override.
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }

    //======================= GET ====================================================
    @GET
    @Path("{csid}")
    public Response get(
            @Context Request request,
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            @PathParam("csid") String csid) {
    	uriInfo = new UriInfoWrapper(uriInfo);
        PoxPayloadOut result = null;

        try {
            result = getResourceFromCsid(request, uriInfo, csid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
            throw new CSWebApplicationException(response);
        }

        return Response.ok(result.getBytes()).build();
    }

    public PoxPayloadOut getResourceFromCsid(
            Request request,
            UriInfo uriInfo,
            String csid) throws Exception {
        PoxPayloadOut result = null;

        ensureCSID(csid, READ);
        RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx =
        		(RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext(request, uriInfo);
        result = get(csid, ctx);// ==> CALL an implementation method, which subclasses may override.

        return result;
    }

    /**
     * Call this method only from other resources (like the Service Groups resource) obtained from the global resource map (ResourceMap).  If the a parent
     * context exists and it has an open Nuxeo repository session, we will use it; otherwise, we will create a new one.
     *
     * @param parentCtx
     * @param csid
     * @return
     */
    public PoxPayloadOut getWithParentCtx(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
    		String csid) {
    	PoxPayloadOut result = null;

        ensureCSID(csid, READ);
        try {
            RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = (RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext();
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
            	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }

            result = get(csid, ctx);// ==> CALL implementation method, which subclasses may override.

            if (result == null) {
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                        ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
                throw new CSWebApplicationException(response);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

    	return result;
    }

    protected PoxPayloadOut get(@PathParam("csid") String csid,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
        PoxPayloadOut result = null;

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
    protected PoxPayloadOut get(String csid,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            DocumentHandler handler)
            throws Exception {
        getRepositoryClient(ctx).get(ctx, csid, handler);
        return ctx.getOutput();
    }

    protected boolean isGetAllRequest(MultivaluedMap<String, String> queryParams) {
    	boolean result = false;

        String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
        String advancedSearch = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_AS);
        String partialTerm = queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);

        if (Tools.isBlank(keywords) && Tools.isBlank(advancedSearch) && Tools.isBlank(partialTerm)) {
        	result = true;
        }

    	return result;
    }

    //======================= GET without csid. List, search, etc. =====================================
    @GET
    public AbstractCommonList getList(@Context UriInfo uriInfo) {
    	uriInfo = new UriInfoWrapper(uriInfo);
        return this.getList(null, uriInfo);
    }

    public AbstractCommonList getList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, UriInfo uriInfo) {
        AbstractCommonList list = null;

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        if (isGetAllRequest(queryParams) == false) {
            String orderBy = queryParams.getFirst(IClientQueryParams.ORDER_BY_PARAM);
            String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
            String advancedSearch = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_AS);
            String partialTerm = queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);
            list = search(parentCtx, uriInfo, orderBy, keywords, advancedSearch, partialTerm);
        } else {
            list = getCommonList(parentCtx, uriInfo);
        }

        return list;
    }

    protected AbstractCommonList getCommonList(UriInfo uriInfo) {
        return getCommonList(null, uriInfo);
    }

    protected AbstractCommonList getCommonList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, UriInfo uriInfo) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
                ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            AbstractCommonList list = (AbstractCommonList) handler.getCommonPartList();
            return list;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }
    protected AbstractCommonList finish_getList(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		DocumentHandler handler) {
        try {
            getRepositoryClient(ctx).getFiltered(ctx, handler); // REM - Side effect of this call sets the handler's common part list value
            return (AbstractCommonList) handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }

    protected AbstractCommonList search(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		DocumentHandler handler,
    		UriInfo ui,
    		String orderBy,
    		String keywords,
    		String advancedSearch,
    		String partialTerm) throws Exception {
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (orderBy == null || orderBy.isEmpty()) {
            String orderByField = getOrderByField(ctx);
            docFilter.setOrderByClause(orderByField);
        }

        //
        // NOTE: Partial-term (PT) queries are mutually exclusive to keyword and
        // partial-term queries trump keyword queries.
        //
        if (partialTerm != null && !partialTerm.isEmpty()) {
        	String partialTermMatchField = getPartialTermMatchField(ctx);
            String ptClause = QueryManager.createWhereClauseForPartialMatch(ctx, partialTermMatchField,
            		partialTerm);
            docFilter.appendWhereClause(ptClause, IQueryManager.SEARCH_QUALIFIER_AND);
        } else if (keywords != null && !keywords.isEmpty()) {
            String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
            if (Tools.isEmpty(whereClause) == false) {
            	docFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
	            if (logger.isDebugEnabled()) {
	                logger.debug("The WHERE clause is: " + docFilter.getWhereClause());
	            }
            } else {
                if (logger.isWarnEnabled()) {
                	logger.warn("The WHERE clause is empty for keywords: ["+keywords+"]");
                }
            }
        }

        //
        // Add an advance search clause if one was specified -even if PT search was requested?
        //
        if (advancedSearch != null && !advancedSearch.isEmpty()) {
            String whereClause = QueryManager.createWhereClauseFromAdvancedSearch(advancedSearch);
            docFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
            if (logger.isDebugEnabled()) {
                logger.debug("The WHERE clause is: " + docFilter.getWhereClause());
            }
        }

        getRepositoryClient(ctx).getFiltered(ctx, handler);
        return (AbstractCommonList) handler.getCommonPartList();
    }

    private AbstractCommonList search(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
            UriInfo uriInfo,
            String orderBy,
            String keywords,
            String advancedSearch,
            String partialTerm) {
        AbstractCommonList result = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx;
        try {
            ctx = createServiceContext(uriInfo);
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
            	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
            DocumentHandler handler = createDocumentHandler(ctx);
            result = search(ctx, handler, uriInfo, orderBy, keywords, advancedSearch, partialTerm);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.SEARCH_FAILED);
        }

        return result;
    }

    //FIXME: REM - This should not be @Deprecated since we may want to implement this -it has been on the wish list.
    @Deprecated
    public AbstractCommonList getList(List<String> csidList) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csidList, handler);
            return (AbstractCommonList) handler.getCommonPartList();
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
            @Context UriInfo uriInfo) {
    	uriInfo = new UriInfoWrapper(uriInfo);
        AuthorityRefList authRefList = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            DocumentModelHandler<PoxPayloadIn, PoxPayloadOut> handler = (DocumentModelHandler<PoxPayloadIn, PoxPayloadOut>) createDocumentHandler(ctx);
            List<AuthRefConfigInfo> authRefsInfo = RefNameServiceUtils.getConfiguredAuthorityRefs(ctx);
            authRefList = handler.getAuthorityRefs(csid, authRefsInfo);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.AUTH_REFS_FAILED, csid);
        }
        return authRefList;
    }

    //======================== UTILITY : getDocModelForRefName ========================================

    /*
     * Used get the order by field for list results if one is not specified with an HTTP query param.
     *
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getOrderByField()
     */
    @Override
    protected String getOrderByField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	String result = null;

    	// We only want to use the partial term field to order the results if a PT query param exists
        String partialTerm = ctx.getQueryParams().getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);
        if (Tools.notBlank(partialTerm) == true) {
	    	DocHandlerParams.Params params = null;
	    	try {
				result = getPartialTermMatchField(ctx);
				if (result == null) {
					throw new Exception();
				}
	    	} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn(String.format("Call failed to getOrderByField() for class %s", this.getClass().getName()));
				}
	    	}
        }

    	return result;
    }

    @Override
	protected String getPartialTermMatchField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	String result = null;

    	DocHandlerParams.Params params = null;
    	try {
			params = ServiceConfigUtils.getDocHandlerParams(ctx);
			ListResultField field = params.getRefnameDisplayNameField();
			String schema = field.getSchema();
			if (schema == null || schema.trim().isEmpty()) {
				schema = ctx.getCommonPartLabel();
			}
			result = schema + ":" + field.getXpath();
    	} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn(String.format("Call failed to getPartialTermMatchField() for class %s", this.getClass().getName()));
			}
    	}

    	return result;
	}

    @Override
    public ServiceDescription getDescription(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	ServiceDescription result = new ServiceDescription();

    	result.setDocumentType(getDocType(ctx.getTenantId()));

    	return result;
    }

    /*
     * ResourceBase create and update calls will set the resourceMap into the service context
     * for all inheriting resource classes. Just use ServiceContext.getResourceMap() to get
     * the map, and pass it in.
     */
    public static DocumentModel getDocModelForRefName(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String refName, ResourceMap resourceMap)
   			throws Exception, DocumentNotFoundException {
    	return NuxeoUtils.getDocModelForRefName(ctx, refName, resourceMap);
    }

    // This is ugly, but prevents us parsing the refName twice. Once we make refName a little more
    // general, and less Authority(Item) specific, this will look better.
   	public DocumentModel getDocModelForAuthorityItem(CoreSessionInterface repoSession, RefName.AuthorityItem item)
   			throws Exception, DocumentNotFoundException {
   		logger.warn("Default (ResourceBase) getDocModelForAuthorityItem called - should not happen!");
   		return null;
   	}

    public DocumentModel getDocModelForRefName(CoreSessionInterface repoSession, String refName)
   			throws Exception, DocumentNotFoundException {
    	return getDocModelForAuthorityItem(repoSession, RefName.AuthorityItem.parse(refName, true));
    }
}
