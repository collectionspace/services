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
package org.collectionspace.services.common.vocabulary;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayload;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.XmlTools;
import org.collectionspace.services.client.workflow.WorkflowClient;

import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.JaxRsContext;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentReferenceException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.Hierarchy;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityDocumentModelHandler;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;

import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentFilter;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.collectionspace.services.common.workflow.service.nuxeo.WorkflowDocumentModelHandler;
import org.collectionspace.services.description.ServiceDescription;

import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * The Class AuthorityResource.
 */

@SuppressWarnings({"rawtypes", "unchecked"})
@Consumes("application/xml")
@Produces("application/xml")
public abstract class AuthorityResource<AuthCommon, AuthItemHandler>
        extends NuxeoBasedResource {

    final Logger logger = LoggerFactory.getLogger(AuthorityResource.class);

    final static String SEARCH_TYPE_TERMSTATUS = "ts";
    public final static String hierarchy = "hierarchy";

    private static final Integer PAGE_NUM_FROM_QUERYPARAMS = null;
    private static final Integer PAGE_SIZE_FROM_QUERYPARAMS = null;

    protected Class<AuthCommon> authCommonClass;
    protected Class<?> resourceClass;
    protected String authorityCommonSchemaName;
    protected String authorityItemCommonSchemaName;
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType(); //FIXME: REM - 3 Why is this field needed?  I see no references to it.

    final static String FETCH_SHORT_ID = "_fetch_";
    public final static String PARENT_WILDCARD = "_ALL_";
    protected static final boolean DONT_INCLUDE_ITEMS = false;
    protected static final boolean INCLUDE_ITEMS = true;

    /**
     * Instantiates a new Authority resource.
     */
    public AuthorityResource(Class<AuthCommon> authCommonClass, Class<?> resourceClass,
            String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
        this.authCommonClass = authCommonClass;
        this.resourceClass = resourceClass;
        this.authorityCommonSchemaName = authorityCommonSchemaName;
        this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }

    public abstract String getItemServiceName();

    public abstract String getItemTermInfoGroupXPathBase();

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision: 2617 $";
    }

    @Override
    public Class<AuthCommon> getCommonPartClass() {
        return authCommonClass;
    }

    /**
     * Creates the item document handler.
     *
     * @param ctx the ctx
     * @param inAuthority the in vocabulary
     *
     * @return the document handler
     *
     * @throws Exception the exception
     */
    protected DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> createItemDocumentHandler(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String inAuthority, String containerShortIdentifier)
            throws Exception {
        String authorityRefNameBase;
        AuthorityItemDocumentModelHandler<?> docHandler;

        if (containerShortIdentifier == null) {
            authorityRefNameBase = null;
        } else {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> containerCtx = createServiceContext(getServiceName());
            if (containerShortIdentifier.equals(FETCH_SHORT_ID)) { // We need to fetch this from the repo
                if (ctx.getCurrentRepositorySession() != null) {
                    containerCtx.setCurrentRepositorySession(ctx.getCurrentRepositorySession()); // We need to use the current repo session if one exists
                }
                // Get from parent document
                containerShortIdentifier = getAuthShortIdentifier(containerCtx, inAuthority);
            }
            authorityRefNameBase = buildAuthorityRefNameBase(containerCtx, containerShortIdentifier);
        }

        docHandler = (AuthorityItemDocumentModelHandler<?>) createDocumentHandler(ctx,
                ctx.getCommonPartLabel(getItemServiceName()),
                authCommonClass);
        // FIXME - Richard and Aron think the following three lines should
        // be in the constructor for the AuthorityItemDocumentModelHandler
        // because all three are required fields.
        docHandler.setInAuthority(inAuthority);
        docHandler.setAuthorityRefNameBase(authorityRefNameBase);
        docHandler.setItemTermInfoGroupXPathBase(getItemTermInfoGroupXPathBase());
        return docHandler;
    }

    public String getAuthShortIdentifier(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String authCSID)
            throws DocumentNotFoundException, DocumentException {
        String shortIdentifier = null;

        try {
            AuthorityDocumentModelHandler<?> handler = (AuthorityDocumentModelHandler<?>) createDocumentHandler(ctx);
            shortIdentifier = handler.getShortIdentifier(ctx, authCSID, authorityCommonSchemaName);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }

        return shortIdentifier;
    }

    protected String buildAuthorityRefNameBase(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String shortIdentifier) {
        RefName.Authority authority = RefName.Authority.buildAuthority(ctx.getTenantName(),
                ctx.getServiceName(),
                null,    // Only use shortId form!!!
                shortIdentifier, null);
        return authority.toString();
    }

    public static class CsidAndShortIdentifier {
        String CSID;
        String shortIdentifier;
    }

    protected String lookupParentCSID(String parentspecifier, String method,
            String op, UriInfo uriInfo) throws Exception {
        CsidAndShortIdentifier tempResult = lookupParentCSIDAndShortIdentifer(NULL_CONTEXT,
                parentspecifier, method, op, uriInfo);
        return tempResult.CSID;
    }

    protected String lookupParentCSID(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String parentspecifier, String method,
            String op, UriInfo uriInfo) throws Exception {
        CsidAndShortIdentifier tempResult = lookupParentCSIDAndShortIdentifer(ctx,
                parentspecifier, method, op, uriInfo);
        return tempResult.CSID;
    }


    private CsidAndShortIdentifier lookupParentCSIDAndShortIdentifer(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx, // Ok to be null
            String parentIdentifier,
            String method,
            String op,
            UriInfo uriInfo)
            throws Exception {
        CsidAndShortIdentifier result = new CsidAndShortIdentifier();
        Specifier parentSpec = Specifier.getSpecifier(parentIdentifier, method, op);

        String parentcsid;
        String parentShortIdentifier;
        if (parentSpec.form == SpecifierForm.CSID) {
            parentShortIdentifier = null;
            parentcsid = parentSpec.value;
            // Uncomment when app layer is ready to integrate
            // Uncommented since refNames are currently only generated if not present - ADR CSPACE-3178
            parentShortIdentifier = FETCH_SHORT_ID;
        } else {
            parentShortIdentifier = parentSpec.value;
            String whereClause = RefNameServiceUtils.buildWhereForAuthByName(authorityCommonSchemaName, parentShortIdentifier);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getServiceName(), uriInfo);
            CoreSessionInterface repoSession = null;
            if (existingCtx != null) {
                repoSession = (CoreSessionInterface) existingCtx.getCurrentRepositorySession();  // We want to use the thread's current repo session
            }
            parentcsid = getRepositoryClient(ctx).findDocCSID(repoSession, ctx, whereClause); //FIXME: REM - If the parent has been soft-deleted, should we be looking for the item?
        }

        result.CSID = parentcsid;
        result.shortIdentifier = parentShortIdentifier;

        return result;
    }

    public String lookupItemCSID(ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext, String itemspecifier, String parentcsid, String method, String op)
            throws Exception {
        String itemcsid;

        Specifier itemSpec = Specifier.getSpecifier(itemspecifier, method, op);
        if (itemSpec.form == SpecifierForm.CSID) {
            itemcsid = itemSpec.value;
        } else {
            String itemWhereClause = RefNameServiceUtils.buildWhereForAuthItemByName(authorityItemCommonSchemaName, itemSpec.value, parentcsid);
            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(getItemServiceName());
            CoreSessionInterface repoSession = null;
            if (existingContext != null) {
                repoSession = (CoreSessionInterface) existingContext.getCurrentRepositorySession();  // We want to use the thread's current repo session
            }
            itemcsid = getRepositoryClient(ctx).findDocCSID(repoSession, ctx, itemWhereClause); //FIXME: REM - Should we be looking for the 'wf_deleted' query param and filtering on it?
        }

        return itemcsid;
    }

    /*
     * Generally, callers will first call RefName.AuthorityItem.parse with a refName, and then
     * use the returned item.inAuthority.resource and a resourceMap to get a service-specific
     * Resource. They then call this method on that resource.
     */
    @Override
       public DocumentModel getDocModelForAuthorityItem(CoreSessionInterface repoSession, RefName.AuthorityItem item)
               throws Exception, DocumentNotFoundException {
        if (item == null) {
            return null;
        }
        String whereClause = RefNameServiceUtils.buildWhereForAuthByName(authorityCommonSchemaName, item.getParentShortIdentifier());
        // Ensure we have the right context.
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(item.inAuthority.resource);

        // HACK - this really must be moved to the doc handler, not here. No Nuxeo specific stuff here!
        NuxeoRepositoryClientImpl client = (NuxeoRepositoryClientImpl)getRepositoryClient(ctx);
        String parentcsid = client.findDocCSID(repoSession, ctx, whereClause);

        String itemWhereClause = RefNameServiceUtils.buildWhereForAuthItemByName(authorityItemCommonSchemaName, item.getShortIdentifier(), parentcsid);
        ctx = createServiceContext(getItemServiceName());
        DocumentWrapper<DocumentModel> docWrapper = client.findDoc(repoSession, ctx, itemWhereClause);
        DocumentModel docModel = docWrapper.getWrappedObject();
        return docModel;
    }


    @POST
    public Response createAuthority(
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            String xmlPayload) {
        //
        // Requests to create new authorities come in on new threads. Unfortunately, we need to synchronize those threads on this block because, as of 8/27/2015, we can't seem to get Nuxeo
        // transaction code to deal with a database level UNIQUE constraint violations on the 'shortidentifier' column of the vocabularies_common table.
        // Therefore, to prevent having multiple authorities with the same shortid, we need to synchronize
        // the code that creates new authorities.  The authority document model handler will first check for authorities with the same short id before
        // trying to create a new authority.
        //
        synchronized(AuthorityResource.class) {
            try {
                PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
                ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
                DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);

                String csid = getRepositoryClient(ctx).create(ctx, handler);
                UriBuilder path = UriBuilder.fromResource(resourceClass);
                path.path("" + csid);
                Response response = Response.created(path.build()).build();
                return response;
            } catch (Exception e) {
                throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
            }
        }
    }

    protected boolean supportsReplicating(String tenantId, String serviceName) {
        boolean result = false;

        ServiceBindingType sb = getTenantBindingsReader().getServiceBinding(tenantId, getServiceName());
        result = sb.isSupportsReplicating();

        return result;
    }

    /**
     * Synchronizes the authority and its items/terms with a Shared Authority Server.
     *
     * @param specifier either a CSID or one of the urn forms
     *
     * @return the authority
     */
    @POST
    @Path("{csid}/sync")
    public byte[] synchronize(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String identifier) {
        uriInfo = new UriInfoWrapper(uriInfo);
        byte[] result;
        boolean neededSync = false;
        PoxPayloadOut payloadOut = null;
        Specifier specifier;

        //
        // Prevent multiple SAS synchronizations from occurring simultaneously by synchronizing this method.
        //
        synchronized(AuthorityResource.class) {
            try {
                ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
                /*
                 * Make sure this authority service supports synchronization
                 */
                if (supportsReplicating(ctx.getTenantId(), ctx.getServiceName()) == false) {
                    throw new DocumentException(Response.Status.FORBIDDEN.getStatusCode());
                }
                AuthorityDocumentModelHandler handler = (AuthorityDocumentModelHandler)createDocumentHandler(ctx);
                specifier = Specifier.getSpecifier(identifier, "getAuthority", "GET");
                handler.setShouldUpdateRevNumber(AuthorityServiceUtils.DONT_UPDATE_REV); // Never update rev number on sync calls
                neededSync = getRepositoryClient(ctx).synchronize(ctx, specifier, handler);
                payloadOut = ctx.getOutput();
            } catch (Exception e) {
                throw bigReThrow(e, ServiceMessages.SYNC_FAILED, identifier);
            }

            //
            // If a sync was needed and was successful, return a copy of the updated resource.  Acts like an UPDATE.
            //
            if (neededSync == true) {
                result = payloadOut.getBytes();
            } else {
                result = String.format("Authority resource '%s' was already in sync with shared authority server.",
                        specifier.value).getBytes();
                Response response = Response.status(Response.Status.NOT_MODIFIED).entity(result).type("text/plain").build();
                throw new CSWebApplicationException(response);
            }
        }

        return result;
    }

    /*
     * Builds a cached JAX-RS response.
     */
    protected Response buildResponse(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, PoxPayloadOut payloadOut) {
        Response result = null;

        ResponseBuilder responseBuilder = Response.ok(payloadOut.getBytes());
        this.setCacheControl(ctx, responseBuilder);
        result = responseBuilder.build();

        return result;
    }

    /**
     * Gets the authority.
     *
     * @param specifier either a CSID or one of the urn forms
     *
     * @return the authority
     */
    @GET
    @Path("{csid}")
    @Override
    public Response get(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String specifier) {
        Response result = null;
        uriInfo = new UriInfoWrapper(uriInfo);

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(request, uriInfo);
            PoxPayloadOut payloadout = getAuthority(ctx, request, uriInfo, specifier, DONT_INCLUDE_ITEMS);
            result = buildResponse(ctx, payloadout);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, specifier);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "GET request failed. The requested Authority specifier:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new CSWebApplicationException(response);
        }

        return result;
    }

    protected PoxPayloadOut getAuthority(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            Request request,
            UriInfo uriInfo,
            String specifier,
            boolean includeItems) throws Exception {
        uriInfo = new UriInfoWrapper(uriInfo);
        PoxPayloadOut payloadout = null;

        DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> docHandler = createDocumentHandler(ctx);
        Specifier spec = Specifier.getSpecifier(specifier, "getAuthority", "GET");
        if (spec.form == SpecifierForm.CSID) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAuthority with csid=" + spec.value);
            }
            getRepositoryClient(ctx).get(ctx, spec.value, docHandler);
        } else {
            String whereClause = RefNameServiceUtils.buildWhereForAuthByName(authorityCommonSchemaName, spec.value);
            DocumentFilter myFilter = new NuxeoDocumentFilter(whereClause, 0, 1);
            docHandler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).get(ctx, docHandler);
        }

        payloadout = ctx.getOutput();
        if (includeItems == true) {
            AbstractCommonList itemsList = this.getAuthorityItemList(ctx, specifier, uriInfo);
            payloadout.addPart(PoxPayload.ABSTRACT_COMMON_LIST_ROOT_ELEMENT_LABEL, itemsList);
        }

        return payloadout;
    }

    /**
     * Finds and populates the authority list.
     *
     * @param ui the ui
     *
     * @return the authority list
     */
    @GET
    @Produces("application/xml")
    public AbstractCommonList getAuthorityList(@Context UriInfo uriInfo) { //FIXME - REM 5/3/2012 - This is not reachable from the JAX-RS dispatcher.  Instead the equivalent method in ResourceBase is getting called.
        uriInfo = new UriInfoWrapper(uriInfo);
        return this.getAuthorityList(null, uriInfo);
    }

    public AbstractCommonList getAuthorityList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, @Context UriInfo uriInfo) {
        AbstractCommonList result = null;

        try {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
            	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            DocumentFilter myFilter = handler.getDocumentFilter();
            // Need to make the default sort order for authority items
            // be on the displayName field
            String orderBy = queryParams.getFirst(IClientQueryParams.ORDER_BY_PARAM);
            if (orderBy == null || orderBy.isEmpty()) {
                String qualifiedDisplayNameField = authorityCommonSchemaName + ":"
                        + AuthorityItemJAXBSchema.DISPLAY_NAME;
                myFilter.setOrderByClause(qualifiedDisplayNameField);
            }
            String nameQ = queryParams.getFirst("refName");
            if (nameQ != null) {
                myFilter.setWhereClause(authorityCommonSchemaName + ":refName='" + nameQ + "'");
            }
            //getRepositoryClient(ctx).getFiltered(ctx, handler); # Something here?
            String advancedSearch = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_AS);
            result = search(ctx, handler, uriInfo, orderBy, null, advancedSearch, null);
            result = handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }

        return result;
    }

    /**
     * Overriding this methods to see if we should update the revision number during the update.  We don't
     * want to update the rev number of synchronization operations.
     */
    @Override
    protected PoxPayloadOut update(String csid,
            PoxPayloadIn theUpdate, // not used in this method, but could be used by an overriding method
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception {
        AuthorityDocumentModelHandler handler = (AuthorityDocumentModelHandler) createDocumentHandler(ctx);
        Boolean shouldUpdateRev = (Boolean) ctx.getProperty(AuthorityServiceUtils.SHOULD_UPDATE_REV_PROPERTY);
        if (shouldUpdateRev != null) {
            handler.setShouldUpdateRevNumber(shouldUpdateRev);
        }
        getRepositoryClient(ctx).update(ctx, csid, handler);
        return ctx.getOutput();
    }

    /**
     * Update authority.
     *
     * @param specifier the csid or id
     *
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public byte[] updateAuthority(
            @Context Request request,
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            @PathParam("csid") String specifier,
            String xmlPayload) {
        PoxPayloadOut result = null;
        try {
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(theUpdate);
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            Specifier spec = Specifier.getSpecifier(specifier, "updateAuthority", "UPDATE");
            String csid = getCsid(ctx, spec);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED);
        }
        return result.getBytes();
    }

    /**
     * Delete all the items in an authority list.
     *
     * @param specifier
     * @param uriInfo
     * @return
     */
    @DELETE
    @Path("{csid}/items")
    public Response deleteAuthorityItemList(@PathParam("csid") String specifier,
            @Context UriInfo uriInfo) {
        uriInfo = new UriInfoWrapper(uriInfo);

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = this.getRepositoryClient(ctx);

            CoreSessionInterface repoSession = repoClient.getRepositorySession(ctx);
            try {
                DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
                //
                // Delete all the items one by one
                //
                AbstractCommonList itemsList = this.getAuthorityItemList(ctx, specifier, uriInfo);
                for (ListItem item : itemsList.getListItem()) {
                    deleteAuthorityItem(ctx, specifier, getCsid(item), AuthorityServiceUtils.UPDATE_REV);
                }
            } catch (Throwable t) {
                repoSession.setTransactionRollbackOnly();
                throw t;
            } finally {
                repoClient.releaseRepositorySession(ctx, repoSession);
            }

            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, specifier);
        }
    }

    /**
     * Delete authority
     *
     * @param csid the csid or a URN specifier form -e.g., urn:cspace:name(OurMuseumPersonAuthority)
     *
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteAuthority( // # Delete this authority and all of it's items.
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String specifier) {
        uriInfo = new UriInfoWrapper(uriInfo);

        if (logger.isDebugEnabled()) {
            logger.debug("deleteAuthority with specifier=" + specifier);
        }

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            Specifier spec = Specifier.getSpecifier(specifier, "getAuthority", "GET");
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = this.getRepositoryClient(ctx);

            CoreSessionInterface repoSession = repoClient.getRepositorySession(ctx);
            try {
                DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
                //
                // First try to delete all the items
                //
                AbstractCommonList itemsList = this.getAuthorityItemList(ctx, specifier, uriInfo);
                for (ListItem item : itemsList.getListItem()) {
                    deleteAuthorityItem(ctx, specifier, getCsid(item), AuthorityServiceUtils.UPDATE_REV);
                }

                //
                // Lastly, delete the parent/container
                //
                if (spec.form == SpecifierForm.CSID) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("deleteAuthority with csid=" + spec.value);
                    }
                    ensureCSID(spec.value, ServiceMessages.DELETE_FAILED, "Authority.csid");
                    getRepositoryClient(ctx).delete(ctx, spec.value, handler);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("deleteAuthority with specifier=" + spec.value);
                    }
                    String whereClause = RefNameServiceUtils.buildWhereForAuthByName(authorityCommonSchemaName, spec.value);
                    getRepositoryClient(ctx).deleteWithWhereClause(ctx, whereClause, handler);
                }
            } catch (Throwable t) {
                repoSession.setTransactionRollbackOnly();
                throw t;
            } finally {
                repoClient.releaseRepositorySession(ctx, repoSession);
            }

            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, specifier);
        }
    }

    protected String getCsid(ListItem item) {
        String result = null;

        for (Element ele : item.getAny()) {
            String elementName = ele.getTagName().toLowerCase();
            if (elementName.equals("csid")) {
                result = ele.getTextContent();
                break;
            }
        }

        return result;
    }

    /**
     *
     * @param ctx
     * @param parentspecifier        - ID of the container. Can be URN or CSID form
     * @param shouldUpdateRevNumber - Indicates if the revision number should be updated on create -won't do this when synching with SAS
     * @param isProposed                - In a shared authority context, indicates if this item just a proposed item and not yet part of the SAS authority
     * @return
     * @throws Exception
     */
    protected Response createAuthorityItem(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String parentIdentifier,
            boolean shouldUpdateRevNumber,
            boolean isProposed,
            boolean isSasItem) throws Exception {
        Response result = null;

        // Note: must have the parentShortId, to do the create.
        CsidAndShortIdentifier parent = lookupParentCSIDAndShortIdentifer(ctx, parentIdentifier, "createAuthorityItem", "CREATE_ITEM", null);
        AuthorityItemDocumentModelHandler handler =
            (AuthorityItemDocumentModelHandler) createItemDocumentHandler(ctx, parent.CSID, parent.shortIdentifier);
        handler.setShouldUpdateRevNumber(shouldUpdateRevNumber);
        handler.setIsProposed(isProposed);
        handler.setIsSASItem(isSasItem);
        // Make the client call
        String itemcsid = getRepositoryClient(ctx).create(ctx, handler);

        // Build the JAX-RS response
        UriBuilder path = UriBuilder.fromResource(resourceClass);
        path.path(parent.CSID + "/items/" + itemcsid);
        result = Response.created(path.build()).build();

        return result;
    }

    public PoxPayloadOut updateAuthorityItem(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> itemServiceCtx, // Ok to be null.  Will be null on PUT calls, but not on sync calls
            ResourceMap resourceMap,
            UriInfo uriInfo,
            String parentspecifier,
            String itemspecifier,
            PoxPayloadIn theUpdate,
            boolean shouldUpdateRevNumber,
            Boolean isProposed,
            Boolean isSASItem
            ) throws Exception {
        return updateAuthorityItem(null, itemServiceCtx, resourceMap, uriInfo, parentspecifier, itemspecifier, theUpdate, shouldUpdateRevNumber, isProposed, isSASItem);
    }

    public PoxPayloadOut updateAuthorityItem(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> itemServiceCtx, // Ok to be null.  Will be null on PUT calls, but not on sync calls
            ResourceMap resourceMap,
            UriInfo uriInfo,
            String parentspecifier,
            String itemspecifier,
            PoxPayloadIn theUpdate,
            boolean shouldUpdateRevNumber,
            Boolean isProposed,
            Boolean isSASItem
            ) throws Exception {
        PoxPayloadOut result = null;

        CsidAndShortIdentifier csidAndShortId = lookupParentCSIDAndShortIdentifer(itemServiceCtx, parentspecifier, "updateAuthorityItem(parent)", "UPDATE_ITEM", null);
        String parentcsid = csidAndShortId.CSID;
        String parentShortId = csidAndShortId.shortIdentifier;
        //
        // If the itemServiceCtx context is not null, use it.  Otherwise, create a new context
        //
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = itemServiceCtx;
        if (ctx == null) {
            ctx = createServiceContext(getItemServiceName(), theUpdate, resourceMap, uriInfo);
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
                ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
        } else {
            ctx.setInput(theUpdate); // the update payload
        }

        String itemcsid = lookupItemCSID(ctx, itemspecifier, parentcsid, "updateAuthorityItem(item)", "UPDATE_ITEM"); //use itemServiceCtx if it is not null

        // We omit the parentShortId, only needed when doing a create...
        AuthorityItemDocumentModelHandler handler = (AuthorityItemDocumentModelHandler)createItemDocumentHandler(ctx, parentcsid, parentShortId);
        handler.setShouldUpdateRevNumber(shouldUpdateRevNumber);
        //
        // Update the SAS fields if either value is non-null
        //
        boolean updateSASFields = isProposed != null || isSASItem != null;
        handler.setshouldUpdateSASFields(updateSASFields);
        if (updateSASFields == true) {
            handler.setshouldUpdateSASFields(true);
            if (isProposed != null) {
                handler.setIsProposed(isProposed);
            }
            if (isSASItem != null) {
                handler.setIsSASItem(isSASItem);
            }
        }

        getRepositoryClient(ctx).update(ctx, itemcsid, handler);
        result = ctx.getOutput();

        return result;
    }

    /**
     * Called with an existing context.
     * @param parentCtx
     * @param parentIdentifier
     * @param input
     * @return
     * @throws Exception
     */
    public Response createAuthorityItemWithParentContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
            String parentIdentifier,
            PoxPayloadIn input,
            boolean shouldUpdateRevNumber,
            boolean isProposed,
            boolean isSASItem) throws Exception {
        Response result = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), input,
                parentCtx.getResourceMap(), parentCtx.getUriInfo());
        if (parentCtx.getCurrentRepositorySession() != null) {
            ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession());
        }
        result = this.createAuthorityItem(ctx, parentIdentifier, shouldUpdateRevNumber, isProposed, isSASItem);

        return result;
    }

    /*************************************************************************
     * Create an AuthorityItem - this is a sub-resource of Authority
     * @param specifier either a CSID or one of the urn forms
     * @return Authority item response
     *************************************************************************/
    @POST
    @Path("{csid}/items")
    public Response createAuthorityItem(
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            @PathParam("csid") String parentIdentifier, // Either a CSID or a URN form -e.g., a8ad38ec-1d7d-4bf2-bd31 or urn:cspace:name(bugsbunny)
            String xmlPayload) {
        uriInfo = new UriInfoWrapper(uriInfo);
        Response result = null;

        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), input, resourceMap, uriInfo);
            result = this.createAuthorityItem(ctx, parentIdentifier, AuthorityServiceUtils.UPDATE_REV,
                    AuthorityServiceUtils.PROPOSED, AuthorityServiceUtils.NOT_SAS_ITEM);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }

        return result;
    }

    @GET
    @Path("{csid}/items/{itemcsid}" + WorkflowClient.SERVICE_PATH)
    public byte[] getItemWorkflow(
            @PathParam("csid") String csid,
            @PathParam("itemcsid") String itemcsid) {
        PoxPayloadOut result = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx = createServiceContext(getItemServiceName());
            String parentWorkspaceName = parentCtx.getRepositoryWorkspaceName();

            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME);
            WorkflowDocumentModelHandler handler = createWorkflowDocumentHandler(ctx);
            ctx.setRespositoryWorkspaceName(parentWorkspaceName); //find the document in the parent's workspace
            getRepositoryClient(ctx).get(ctx, itemcsid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }
        return result.getBytes();
    }

    /*
     * We should consider changing this code.  The RepositoryClient (from call to getRepositoryClient) could support a call doWorkflowTransition() instead?
     */
    @Override
    @PUT
    @Path("{csid}" + WorkflowClient.SERVICE_PATH + "/" + "{transition}")
    public byte[] updateWorkflowWithTransition(
            @Context UriInfo uriInfo,
            @PathParam("csid") String specifier,
            @PathParam("transition") String transition) {
        PoxPayloadOut result = null;

        Specifier spec = Specifier.getSpecifier(specifier, "updateAuthority", "UPDATE");
        String csid = null;
        try {
            csid = getCsid(null, spec);
            result = updateWorkflowWithTransition(NULL_CONTEXT, uriInfo, csid, transition);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }

        return result.getBytes();
    }

    //FIXME: This method is almost identical to the method org.collectionspace.services.common.updateWorkflowWithTransition() so
    // they should be consolidated -be DRY (D)on't (R)epeat (Y)ourself.
    @PUT
    @Path("{csid}/items/{itemcsid}" + WorkflowClient.SERVICE_PATH + "/{transition}")
    public byte[] updateItemWorkflowWithTransition(
            @Context UriInfo uriInfo,
            @PathParam("csid") String parentIdentifier,
            @PathParam("itemcsid") String itemIdentifier,
            @PathParam("transition") String transition) {
        uriInfo = new UriInfoWrapper(uriInfo);
        return updateItemWorkflowWithTransition(null, uriInfo, parentIdentifier, itemIdentifier, transition);
    }

    public byte[] updateItemWorkflowWithTransition(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext,
            UriInfo uriInfo,
            String parentIdentifier,
            String itemIdentifier,
            String transition) {
        PoxPayloadOut result = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
            if (existingContext != null && existingContext.getCurrentRepositorySession() != null) {
                ctx.setCurrentRepositorySession(existingContext.getCurrentRepositorySession());// If a repo session is already open, we need to use it and not create a new one
            }
            result = updateItemWorkflowWithTransition(ctx,
                    parentIdentifier, itemIdentifier, transition, AuthorityServiceUtils.UPDATE_REV);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, parentIdentifier);
        }

        return result.getBytes();
    }

    /**
     * Update an authority item's workflow state.
     * @param existingContext
     * @param csid
     * @param itemcsid
     * @param transition
     * @return
     * @throws DocumentReferenceException
     */
    public PoxPayloadOut updateItemWorkflowWithTransition(ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext,
            String parentIdentifier,
            String itemIdentifier,
            String transition,
            boolean updateRevNumber) throws DocumentReferenceException {
        PoxPayloadOut result = null;

        try {
            //
            // We need CSIDs for both the parent authority and the authority item
            //
            CsidAndShortIdentifier csidAndShortId = lookupParentCSIDAndShortIdentifer(existingContext, parentIdentifier, "updateItemWorkflowWithTransition(parent)", "UPDATE_ITEM", null);
            String itemCsid = lookupItemCSID(existingContext, itemIdentifier, csidAndShortId.CSID, "updateAuthorityItem(item)", "UPDATE_ITEM");

            //
            // Create an empty workflow_commons input part and set it into a new "workflow" sub-resource context
            //
            PoxPayloadIn input = new PoxPayloadIn(WorkflowClient.SERVICE_PAYLOAD_NAME, new WorkflowCommon(),
                    WorkflowClient.SERVICE_COMMONPART_NAME);
            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME, input);
            if (existingContext != null && existingContext.getCurrentRepositorySession() != null) {
                ctx.setCurrentRepositorySession(existingContext.getCurrentRepositorySession());// If a repo session is already open, we need to use it and not create a new one
            }
            //
            // Create a service context and document handler for the target resource -not the workflow resource itself.
            //
            ServiceContext<PoxPayloadIn, PoxPayloadOut> targetCtx = createServiceContext(getItemServiceName(), existingContext.getUriInfo());
            AuthorityItemDocumentModelHandler targetDocHandler = (AuthorityItemDocumentModelHandler) this.createDocumentHandler(targetCtx);
            targetDocHandler.setShouldUpdateRevNumber(updateRevNumber);
            ctx.setProperty(WorkflowClient.TARGET_DOCHANDLER, targetDocHandler); //added as a context param for the workflow document handler -it will call the parent's dochandler "prepareForWorkflowTranstion" method
            //
            // When looking for the document, we need to use the parent/target resource's workspace name -not the "workflow" workspace name
            //
            String targetWorkspaceName = targetCtx.getRepositoryWorkspaceName();
            ctx.setRespositoryWorkspaceName(targetWorkspaceName); //find the document in the parent's workspace

            // Get the type of transition we're being asked to make and store it as a context parameter -used by the workflow document handler
            TransitionDef transitionDef = getTransitionDef(targetCtx, transition);
            if (transitionDef == null) {
                throw new DocumentException(String.format("The document with ID='%s' does not support the workflow transition '%s'.",
                        itemIdentifier, transition));
            }
            ctx.setProperty(WorkflowClient.TRANSITION_ID, transitionDef);

            WorkflowDocumentModelHandler handler = createWorkflowDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, itemCsid, handler);
            result = ctx.getOutput();
        } catch (DocumentReferenceException de) {
            throw de;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, itemIdentifier);
        }

        return result;
    }

    protected PoxPayloadOut getAuthorityItem(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String parentIdentifier,
            String itemIdentifier) throws Exception {
        PoxPayloadOut result = null;

        String parentcsid = lookupParentCSID(ctx, parentIdentifier, "getAuthorityItem(parent)", "GET_ITEM", null);
        // We omit the parentShortId, only needed when doing a create...
        DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createItemDocumentHandler(ctx, parentcsid, null);

        Specifier itemSpec = Specifier.getSpecifier(itemIdentifier, "getAuthorityItem(item)", "GET_ITEM");
        if (itemSpec.form == SpecifierForm.CSID) {
            // TODO should we assert that the item is in the passed vocab?
            getRepositoryClient(ctx).get(ctx, itemSpec.value, handler);
        } else {
            String itemWhereClause =
                    RefNameServiceUtils.buildWhereForAuthItemByName(authorityItemCommonSchemaName, itemSpec.value, parentcsid);
            DocumentFilter myFilter = new NuxeoDocumentFilter(itemWhereClause, 0, 1); // start at page 0 and get 1 item
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).get(ctx, handler);
        }

        result = (PoxPayloadOut) ctx.getOutput();
        if (result != null) {
            String inAuthority = XmlTools.getElementValue(result.getDOMDocument(), "//" + AuthorityItemJAXBSchema.IN_AUTHORITY);
            if (inAuthority.equalsIgnoreCase(parentcsid) == false) {
                throw new Exception(String.format("Looked up item = '%s' and found with inAuthority = '%s', but expected inAuthority = '%s'.",
                        itemSpec.value, inAuthority, parentcsid));
            }
        }

        return result;
    }

    public PoxPayloadOut getAuthorityItemWithExistingContext(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
            String parentIdentifier,
            String itemIdentifier) throws Exception {
        return getAuthorityItemWithExistingContext(existingCtx, existingCtx.getUriInfo(), existingCtx.getResourceMap(), parentIdentifier, itemIdentifier);
    }

    public PoxPayloadOut getAuthorityItemWithExistingContext(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
            UriInfo uriInfo,
            ResourceMap resourceMap,
            String parentIdentifier,
            String itemIdentifier) throws Exception {
        PoxPayloadOut result = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), resourceMap, uriInfo);
        if (existingCtx.getCurrentRepositorySession() != null) {
            ctx.setCurrentRepositorySession(existingCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            ctx.setProperties(existingCtx.getProperties());
        }
        result = getAuthorityItem(ctx, parentIdentifier, itemIdentifier);

        return result;
    }

    /**
     * Gets the authority item.
     *
     * @param parentspecifier either a CSID or one of the urn forms
     * @param itemspecifier either a CSID or one of the urn forms
     *
     * @return the authority item
     */
    @GET
    @Path("{csid}/items/{itemcsid}")
    public byte[] getAuthorityItem(
            @Context Request request,
            @Context UriInfo uriInfo,
            @Context ResourceMap resourceMap,
            @PathParam("csid") String parentIdentifier,
            @PathParam("itemcsid") String itemIdentifier) {
        uriInfo = new UriInfoWrapper(uriInfo);
        PoxPayloadOut result = null;
        try {
            RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx =
                    (RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext(getItemServiceName(), resourceMap, uriInfo);

            JaxRsContext jaxRsContext = new JaxRsContext(request, uriInfo); // Needed for getting account permissions part of the resource
            ctx.setJaxRsContext(jaxRsContext);

            result = getAuthorityItem(ctx, parentIdentifier, itemIdentifier);
        } catch (DocumentNotFoundException dnf) {
            throw bigReThrow(dnf, ServiceMessages.resourceNotFoundMsg(itemIdentifier));
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }

        return result.getBytes();
    }

    /*
     * Most of the authority child classes will/should use this implementation.  However, the Vocabulary service's item schema is
     * different enough that it will have to override this method in it's resource class.
     */
    @Override
    protected String getOrderByField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        String result = null;

        result = NuxeoUtils.getPrimaryElPathPropertyName(
                authorityItemCommonSchemaName, getItemTermInfoGroupXPathBase(),
                AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);

        return result;
    }

    @Override
    protected String getPartialTermMatchField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        String result = null;

        result = NuxeoUtils.getMultiElPathPropertyName(
                authorityItemCommonSchemaName, getItemTermInfoGroupXPathBase(),
                AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);

        return result;
    }

    /**
     * Gets the authorityItem list for the specified authority
     * If partialPerm is specified, keywords will be ignored.
     *
     * @param authorityIdentifier either a CSID or one of the urn forms
     * @param partialTerm if non-null, matches partial terms
     * @param keywords if non-null, matches terms in the keyword index for items
     * @param ui passed to include additional parameters, like pagination controls
     *
     */
    public AbstractCommonList getAuthorityItemList(ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext,
            String authorityIdentifier,
            UriInfo uriInfo) throws Exception {
        AbstractCommonList result = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        if (existingContext != null && existingContext.getCurrentRepositorySession() != null) { // Merge some of the existing context properties with our new context
            ctx.setCurrentRepositorySession(existingContext.getCurrentRepositorySession());
            ctx.setProperties(existingContext.getProperties());
        }

        String orderBy = queryParams.getFirst(IClientQueryParams.ORDER_BY_PARAM);
        String termStatus = queryParams.getFirst(SEARCH_TYPE_TERMSTATUS);
        String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
        String advancedSearch = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_AS);
        String partialTerm = queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);

        // For the wildcard case, parentcsid is null, but docHandler will deal with this.
        // We omit the parentShortId, only needed when doing a create...
        String parentcsid = PARENT_WILDCARD.equals(authorityIdentifier) ? null :
            lookupParentCSID(ctx, authorityIdentifier, "getAuthorityItemList", "LIST", uriInfo);
        DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler =
            createItemDocumentHandler(ctx, parentcsid, null);

        DocumentFilter myFilter = handler.getDocumentFilter();
        // If we are not wildcarding the parent, add a restriction
        if (parentcsid != null) {
            myFilter.appendWhereClause(authorityItemCommonSchemaName + ":"
                    + AuthorityItemJAXBSchema.IN_AUTHORITY + "="
                    + "'" + parentcsid + "'",
                    IQueryManager.SEARCH_QUALIFIER_AND);
        }

        if (Tools.notBlank(termStatus)) {
            // Start with the qualified termStatus field
            String qualifiedTermStatusField = authorityItemCommonSchemaName + ":"
                    + AuthorityItemJAXBSchema.TERM_STATUS;
            String[] filterTerms = termStatus.trim().split("\\|");
            String tsClause = QueryManager.createWhereClauseToFilterFromStringList(qualifiedTermStatusField, filterTerms, IQueryManager.FILTER_EXCLUDE);
            myFilter.appendWhereClause(tsClause, IQueryManager.SEARCH_QUALIFIER_AND);
        }

        result = search(ctx, handler, uriInfo, orderBy, keywords, advancedSearch, partialTerm);

        return result;
    }

    /**
     * Gets the authorityItem list for the specified authority
     * If partialPerm is specified, keywords will be ignored.
     *
     * @param authorityIdentifier either a CSID or one of the urn forms
     * @param partialTerm if non-null, matches partial terms
     * @param keywords if non-null, matches terms in the keyword index for items
     * @param ui passed to include additional parameters, like pagination controls
     *
     * @return the authorityItem list
     */
    @GET
    @Path("{csid}/items")
    @Produces("application/xml")
    public AbstractCommonList getAuthorityItemList(@PathParam("csid") String authorityIdentifier,
            @Context UriInfo uriInfo) {
        uriInfo = new UriInfoWrapper(uriInfo);
        AbstractCommonList result = null;

        try {
            result = getAuthorityItemList(NULL_CONTEXT, authorityIdentifier, uriInfo);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }

        return result;
    }

    /**
     * @return the name of the property used to specify references for items in this type of
     * authority. For most authorities, it is ServiceBindingUtils.AUTH_REF_PROP ("authRef").
     * Some types (like Vocabulary) use a separate property.
     */
    protected String getRefPropName() {
        return ServiceBindingUtils.AUTH_REF_PROP;
    }

    /**
     * Gets the entities referencing this Authority item instance. The service type
     * can be passed as a query param "type", and must match a configured type
     * for the service bindings. If not set, the type defaults to
     * ServiceBindingUtils.SERVICE_TYPE_PROCEDURE.
     *
     * @param parentspecifier either a CSID or one of the urn forms
     * @param itemspecifier either a CSID or one of the urn forms
     * @param ui the ui
     *
     * @return the info for the referencing objects
     */
    @GET
    @Path("{csid}/items/{itemcsid}/refObjs")
    @Produces("application/xml")
    public AuthorityRefDocList getReferencingObjects(
            @PathParam("csid") String parentSpecifier,
            @PathParam("itemcsid") String itemSpecifier,
            @Context UriInfo uriInfo) {
        uriInfo = new UriInfoWrapper(uriInfo);
        AuthorityRefDocList authRefDocList = null;
        try {
            authRefDocList = getReferencingObjects(null, parentSpecifier, itemSpecifier, uriInfo, PAGE_NUM_FROM_QUERYPARAMS, PAGE_SIZE_FROM_QUERYPARAMS, true, true);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }

        if (authRefDocList == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Item CSID:" + itemSpecifier + ": was not found.").type(
                    "text/plain").build();
            throw new CSWebApplicationException(response);
        }
        return authRefDocList;
    }

    public AuthorityRefDocList getReferencingObjects(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext,
            String parentspecifier,
            String itemspecifier,
            UriInfo uriInfo) throws Exception {
        return getReferencingObjects(existingContext, parentspecifier, itemspecifier, uriInfo, PAGE_NUM_FROM_QUERYPARAMS, PAGE_SIZE_FROM_QUERYPARAMS, true, true);
    }

    public AuthorityRefDocList getReferencingObjects(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingContext,
            String parentspecifier,
            String itemspecifier,
            UriInfo uriInfo,
            Integer pageNum,
            Integer pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws Exception {
        AuthorityRefDocList authRefDocList = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
        MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        //
        // Merge parts of existing context with our new context
        //
        if (existingContext != null && existingContext.getCurrentRepositorySession() != null) {
            ctx.setCurrentRepositorySession(existingContext.getCurrentRepositorySession());  // If one exists, use the existing repo session
            ctx.setProperties(existingContext.getProperties());
        }

        String parentcsid = lookupParentCSID(ctx, parentspecifier, "getReferencingObjects(parent)", "GET_ITEM_REF_OBJS", uriInfo);
        String itemcsid = lookupItemCSID(ctx, itemspecifier, parentcsid, "getReferencingObjects(item)", "GET_ITEM_REF_OBJS");

        // Remove the "type" property from the query params
        List<String> serviceTypes = queryParams.remove(ServiceBindingUtils.SERVICE_TYPE_PROP);
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            serviceTypes = ServiceBindingUtils.getCommonServiceTypes(true); //CSPACE-5359: Should now include objects, procedures, and authorities
        }

        AuthorityItemDocumentModelHandler handler = (AuthorityItemDocumentModelHandler)createItemDocumentHandler(ctx, parentcsid, null);
        authRefDocList = handler.getReferencingObjects(ctx, serviceTypes, getRefPropName(), itemcsid, pageNum, pageSize, useDefaultOrderByClause, computeTotal);

        return authRefDocList;
    }

    /**
     * Gets the authority terms used in the indicated Authority item.
     *
     * @param parentspecifier either a CSID or one of the urn forms
     * @param itemspecifier either a CSID or one of the urn forms
     * @param ui passed to include additional parameters, like pagination controls
     *
     * @return the authority refs for the Authority item.
     */
    @GET
    @Path("{csid}/items/{itemcsid}/authorityrefs")
    @Produces("application/xml")
    public AuthorityRefList getAuthorityItemAuthorityRefs(
            @PathParam("csid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @Context UriInfo uriInfo) {
        uriInfo = new UriInfoWrapper(uriInfo);
        AuthorityRefList authRefList = null;

        try {
            // Note that we have to create the service context for the Items, not the main service
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
            String parentcsid = lookupParentCSID(parentspecifier, "getAuthorityItemAuthRefs(parent)", "GET_ITEM_AUTH_REFS", uriInfo);
            // We omit the parentShortId, only needed when doing a create...
            DocumentModelHandler<?, AbstractCommonList> handler =
                    (DocumentModelHandler<?, AbstractCommonList>)createItemDocumentHandler(ctx, parentcsid, null /*no parent short ID*/);

            String itemcsid = lookupItemCSID(ctx, itemspecifier, parentcsid, "getAuthorityItemAuthRefs(item)", "GET_ITEM_AUTH_REFS");

            List<RefNameServiceUtils.AuthRefConfigInfo> authRefsInfo = RefNameServiceUtils.getConfiguredAuthorityRefs(ctx);
            authRefList = handler.getAuthorityRefs(itemcsid, authRefsInfo);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED + " parentspecifier: " + parentspecifier + " itemspecifier:" + itemspecifier);
        }

        return authRefList;
    }

    /**
     * Synchronizes a local authority item with a share authority server (SAS) item.
     * @param ctx
     * @param parentIdentifier
     * @param itemIdentifier
     * @return
     * @throws Exception
     */
    private PoxPayloadOut synchronizeItem(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String parentIdentifier,
            String itemIdentifier,
            boolean syncHierarchicalRelationships) throws Exception {
        PoxPayloadOut result = null;
        AuthorityItemSpecifier specifier;
        boolean neededSync = false;

        CsidAndShortIdentifier parent = lookupParentCSIDAndShortIdentifer(ctx, parentIdentifier, "syncAuthorityItem(parent)", "SYNC_ITEM", null);
        AuthorityItemDocumentModelHandler handler = (AuthorityItemDocumentModelHandler)createItemDocumentHandler(ctx, parent.CSID, parent.shortIdentifier);
        handler.setIsProposed(AuthorityServiceUtils.NOT_PROPOSED); // In case it was formally locally proposed, clear the proposed flag
        handler.setIsSASItem(AuthorityServiceUtils.SAS_ITEM); // Since we're sync'ing, this is now a SAS controlled item
        handler.setShouldSyncHierarchicalRelationships(syncHierarchicalRelationships);
        // Create an authority item specifier
        Specifier parentSpecifier = Specifier.getSpecifier(parent.CSID, "getAuthority", "GET");
        Specifier itemSpecifier = Specifier.getSpecifier(itemIdentifier, "getAuthorityItem", "GET");
        specifier = new AuthorityItemSpecifier(parentSpecifier, itemSpecifier);
        //
        neededSync = getRepositoryClient(ctx).synchronize(ctx, specifier, handler);
        if (neededSync == true) {
            result = (PoxPayloadOut) ctx.getOutput();
        }

        return result;
    }

    /**
     * Using the parent and item ID, sync the local item with the SAS (shared authority server)
     * Used by the AuthorityItemDocumentModelHandler when synchronizing a list of remote authority items with a
     * local authority.  The parent context was created for the authority (parent) because the sync started there.
     * @param existingCtx
     * @param parentIdentifier
     * @param itemIdentifier
     * @return
     * @throws Exception
     */
    public PoxPayloadOut synchronizeItemWithExistingContext(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
            String parentIdentifier,
            String itemIdentifier,
            boolean syncHierarchicalRelationships
            ) throws Exception {
        PoxPayloadOut result = null;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(),
                existingCtx.getResourceMap(),
                existingCtx.getUriInfo());
        if (existingCtx.getCurrentRepositorySession() != null) {
            ctx.setCurrentRepositorySession(existingCtx.getCurrentRepositorySession());

        }
        result = synchronizeItem(ctx, parentIdentifier, itemIdentifier, syncHierarchicalRelationships);

        return result;
    }

    /**
     * Synchronizes an authority item and with a Shared Authority Server (SAS) item.
     *
     * @param specifier either CSIDs and/or one of the urn forms
     *
     * @return the authority item if it was updated/synchronized with SAS item; otherwise empty
     */
    @POST
    @Path("{csid}/items/{itemcsid}/sync")
    public byte[] synchronizeItem(
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            @PathParam("csid") String parentIdentifier,
            @PathParam("itemcsid") String itemIdentifier) {
        uriInfo = new UriInfoWrapper(uriInfo);
        byte[] result;
        boolean neededSync = false;
        PoxPayloadOut payloadOut = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), null, resourceMap, uriInfo);
            payloadOut = this.synchronizeItem(ctx, parentIdentifier, itemIdentifier, true);
            if (payloadOut != null) {
                neededSync = true;
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.SYNC_FAILED, itemIdentifier);
        }

        //
        // If a sync was needed and was successful, return a copy of the updated resource.  Acts like an UPDATE.
        //
        if (neededSync == true) {
            result = payloadOut.getBytes();
        } else {
            result = String.format("Authority item resource '%s' was already in sync with shared authority server.",
                    itemIdentifier).getBytes();
            Response response = Response.status(Response.Status.NOT_MODIFIED).entity(result).type("text/plain").build();
            throw new CSWebApplicationException(response);
        }

        return result;
    }

    /**
     * Update authorityItem.
     *
     * @param parentspecifier either a CSID or one of the urn forms
     * @param itemspecifier either a CSID or one of the urn forms
     *
     * @return the multipart output
     */
    @PUT
    @Path("{csid}/items/{itemcsid}")
    public byte[] updateAuthorityItem(
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            @PathParam("csid") String parentSpecifier,
            @PathParam("itemcsid") String itemSpecifier,
            String xmlPayload) {
        return updateAuthorityItem(null, resourceMap, uriInfo, parentSpecifier, itemSpecifier, xmlPayload);
    }

    public byte[] updateAuthorityItem(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
            ResourceMap resourceMap,
            UriInfo uriInfo,
            String parentSpecifier,
            String itemSpecifier,
            String xmlPayload) {
        uriInfo = new UriInfoWrapper(uriInfo);
        PoxPayloadOut result = null;

        try {
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            result = updateAuthorityItem(parentCtx, null, resourceMap, uriInfo, parentSpecifier, itemSpecifier, theUpdate,
                    AuthorityServiceUtils.UPDATE_REV,            // passing TRUE so rev num increases, passing
                    AuthorityServiceUtils.NO_CHANGE,    // don't change the state of the "proposed" field -we could be performing a sync or just a plain update
                    AuthorityServiceUtils.NO_CHANGE);    // don't change the state of the "sas" field -we could be performing a sync or just a plain update
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED);
        }

        return result.getBytes();
    }

    /**
     * Delete authorityItem.
     *
     * @param parentIdentifier the parentcsid
     * @param itemIdentifier the itemcsid
     *
     * @return the response
     */
    @DELETE
    @Path("{csid}/items/{itemcsid}")
    public Response deleteAuthorityItem(
            @Context UriInfo uriInfo,
            @PathParam("csid") String parentIdentifier,
            @PathParam("itemcsid") String itemIdentifier) {
        uriInfo = new UriInfoWrapper(uriInfo);
        Response result = null;

        ensureCSID(parentIdentifier, ServiceMessages.DELETE_FAILED, "AuthorityItem.parentcsid");
        ensureCSID(itemIdentifier, ServiceMessages.DELETE_FAILED, "AuthorityItem.itemcsid");
        if (logger.isDebugEnabled()) {
            logger.debug("deleteAuthorityItem with parentcsid=" + parentIdentifier + " and itemcsid=" + itemIdentifier);
        }

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
            deleteAuthorityItem(ctx, parentIdentifier, itemIdentifier, AuthorityServiceUtils.UPDATE_REV);
            result = Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED + "  itemcsid: " + itemIdentifier + " parentcsid:" + parentIdentifier);
        }

        return result;
    }

    /**
     *
     * @param existingCtx
     * @param parentIdentifier
     * @param itemIdentifier
     * @throws Exception
     */
    public boolean deleteAuthorityItem(ServiceContext<PoxPayloadIn, PoxPayloadOut> existingCtx,
            String parentIdentifier,
            String itemIdentifier,
            boolean shouldUpdateRevNumber
            ) throws Exception {
        boolean result = true;

        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), existingCtx.getUriInfo());
        if (existingCtx != null && existingCtx.getCurrentRepositorySession() != null) {
            ctx.setCurrentRepositorySession(existingCtx.getCurrentRepositorySession());
            ctx.setProperties(existingCtx.getProperties());
        }

        String parentcsid = null;
        try {
            parentcsid = lookupParentCSID(ctx, parentIdentifier, "deleteAuthorityItem(parent)", "DELETE_ITEM", null);
        } catch (DocumentNotFoundException de) {
            String msg = String.format("Could not find parent with ID='%s' when trying to delete item ID='%s'",
                    parentIdentifier, itemIdentifier);
            logger.warn(msg);
            throw de;
        }
        String itemCsid = lookupItemCSID(ctx, itemIdentifier, parentcsid, "deleteAuthorityItem(item)", "DELETE_ITEM"); //use itemServiceCtx if it is not null

        AuthorityItemDocumentModelHandler handler = (AuthorityItemDocumentModelHandler) createDocumentHandler(ctx);
        handler.setShouldUpdateRevNumber(shouldUpdateRevNumber);
        result = getRepositoryClient(ctx).delete(ctx, itemCsid, handler);

        return result;
    }

    @GET
    @Path("{csid}/items/{itemcsid}/" + hierarchy)
    @Produces("application/xml")
    public String getHierarchy(
            @PathParam("csid") String parentIdentifier,
            @PathParam("itemcsid") String itemIdentifier,
            @Context UriInfo uriInfo) throws Exception {
        uriInfo = new UriInfoWrapper(uriInfo);
        String result = null;

        try {
            //
            // All items in dive can look at their child uri's to get uri.  So we calculate the very first one.  We could also do a GET and look at the common part uri field, but why...?
            //
            String calledUri = uriInfo.getPath();
            String uri = "/" + calledUri.substring(0, (calledUri.length() - ("/" + hierarchy).length()));
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);

            String parentcsid = lookupParentCSID(ctx, parentIdentifier, "deleteAuthorityItem(parent)", "DELETE_ITEM", null);
            String itemcsid = lookupItemCSID(ctx, itemIdentifier, parentcsid, "deleteAuthorityItem(item)", "DELETE_ITEM"); //use itemServiceCtx if it is not null

            String direction = uriInfo.getQueryParameters().getFirst(Hierarchy.directionQP);
            if (Tools.notBlank(direction) && Hierarchy.direction_parents.equals(direction)) {
                result = Hierarchy.surface(ctx, itemcsid, uri);
            } else {
                result = Hierarchy.dive(ctx, itemcsid, uri);
            }
        } catch (Exception e) {
            throw bigReThrow(e, "Error showing hierarchy for authority item: ", itemIdentifier);
        }

        return result;
    }

    /**
     *
     * @param tenantId
     * @return
     */
    public String getItemDocType(String tenantId) {
        return getDocType(tenantId, getItemServiceName());
    }

    /**
     * Returns a UriRegistry entry: a map of tenant-qualified URI templates
     * for the current resource, for all tenants
     *
     * @return a map of URI templates for the current resource, for all tenants
     */
    @Override
    public Map<UriTemplateRegistryKey,StoredValuesUriTemplate> getUriRegistryEntries() {
        Map<UriTemplateRegistryKey,StoredValuesUriTemplate> uriRegistryEntriesMap =
                super.getUriRegistryEntries();
        List<String> tenantIds = getTenantBindingsReader().getTenantIds();
        for (String tenantId : tenantIds) {
                uriRegistryEntriesMap.putAll(getUriRegistryEntries(tenantId, getItemDocType(tenantId), UriTemplateFactory.ITEM));
        }
        return uriRegistryEntriesMap;
    }

    /**
     *
     */
    @Override
    public ServiceDescription getDescription(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        ServiceDescription result = super.getDescription(ctx);
        result.setSubresourceDocumentType(this.getItemDocType(ctx.getTenantId()));
        return result;
    }

    public Response createAuthority(String xmlPayload) {
        return this.createAuthority(null, null, xmlPayload);
    }

    protected String getCsid(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, Specifier specifier) throws Exception {
        String csid;

        if (ctx == null) {
            ctx = createServiceContext(getServiceName());
        }

        if (specifier.form == SpecifierForm.CSID) {
            csid = specifier.value;
        } else {
            String whereClause = RefNameServiceUtils.buildWhereForAuthByName(authorityCommonSchemaName, specifier.value);
            csid = getRepositoryClient(ctx).findDocCSID(null, ctx, whereClause);
        }

        return csid;
    }

}
