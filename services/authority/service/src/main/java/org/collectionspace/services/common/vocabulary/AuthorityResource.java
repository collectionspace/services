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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistry;
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
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.Hierarchy;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityDocumentModelHandler;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.common.workflow.service.nuxeo.WorkflowDocumentModelHandler;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuthorityResource.
 */
/**
 * @author pschmitz
 *
 * @param <AuthCommon>
 * @param <AuthItemHandler>
 */
/**
 * @author pschmitz
 *
 * @param <AuthCommon>
 * @param <AuthItemHandler>
 */
@Consumes("application/xml")
@Produces("application/xml")
public abstract class AuthorityResource<AuthCommon, AuthItemHandler>
        extends ResourceBase {
	
	final static String SEARCH_TYPE_TERMSTATUS = "ts";

    protected Class<AuthCommon> authCommonClass;
    protected Class<?> resourceClass;
    protected String authorityCommonSchemaName;
    protected String authorityItemCommonSchemaName;
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType(); //FIXME: REM - 3 Why is this field needed?  I see no references to it.
    final static String URN_PREFIX = "urn:cspace:";
    final static int URN_PREFIX_LEN = URN_PREFIX.length();
    final static String URN_PREFIX_NAME = "name(";
    final static int URN_NAME_PREFIX_LEN = URN_PREFIX_LEN + URN_PREFIX_NAME.length();
    final static String URN_PREFIX_ID = "id(";
    final static int URN_ID_PREFIX_LEN = URN_PREFIX_LEN + URN_PREFIX_ID.length();
    final static String FETCH_SHORT_ID = "_fetch_";
	final static String PARENT_WILDCARD = "_ALL_";
	
    final Logger logger = LoggerFactory.getLogger(AuthorityResource.class);

    public enum SpecifierForm {

        CSID, URN_NAME
    };

    public class Specifier {

        public SpecifierForm form;
        public String value;

        Specifier(SpecifierForm form, String value) {
            this.form = form;
            this.value = value;
        }
    }

    protected Specifier getSpecifier(String specifierIn, String method, String op) throws WebApplicationException {
        if (logger.isDebugEnabled()) {
            logger.debug("getSpecifier called by: " + method + " with specifier: " + specifierIn);
        }
        if (specifierIn != null) {
            if (!specifierIn.startsWith(URN_PREFIX)) {
                // We'll assume it is a CSID and complain if it does not match
                return new Specifier(SpecifierForm.CSID, specifierIn);
            } else {
                if (specifierIn.startsWith(URN_PREFIX_NAME, URN_PREFIX_LEN)) {
                    int closeParen = specifierIn.indexOf(')', URN_NAME_PREFIX_LEN);
                    if (closeParen >= 0) {
                        return new Specifier(SpecifierForm.URN_NAME,
                                specifierIn.substring(URN_NAME_PREFIX_LEN, closeParen));
                    }
                } else if (specifierIn.startsWith(URN_PREFIX_ID, URN_PREFIX_LEN)) {
                    int closeParen = specifierIn.indexOf(')', URN_ID_PREFIX_LEN);
                    if (closeParen >= 0) {
                        return new Specifier(SpecifierForm.CSID,
                                specifierIn.substring(URN_ID_PREFIX_LEN, closeParen));
                    }
                }
            }
        }
        logger.error(method + ": bad or missing specifier!");
        Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                op + " failed on bad or missing Authority specifier").type(
                "text/plain").build();
        throw new WebApplicationException(response);
    }

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
            String inAuthority, String parentShortIdentifier)
            throws Exception {
        String authorityRefNameBase;
        AuthorityItemDocumentModelHandler<?> docHandler;

        if (parentShortIdentifier == null) {
            authorityRefNameBase = null;
        } else {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx =
                    createServiceContext(getServiceName());
            if (parentShortIdentifier.equals(FETCH_SHORT_ID)) {
                // Get from parent document
                parentShortIdentifier = getAuthShortIdentifier(parentCtx, inAuthority);
            }
            authorityRefNameBase = buildAuthorityRefNameBase(parentCtx, parentShortIdentifier);
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

    public String getAuthShortIdentifier(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String authCSID)
            throws DocumentNotFoundException, DocumentException {
        String shortIdentifier = null;
        try {
            AuthorityDocumentModelHandler<?> handler =
                    (AuthorityDocumentModelHandler<?>) createDocumentHandler(ctx);
            shortIdentifier = handler.getShortIdentifier(authCSID, authorityCommonSchemaName);
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
                null,	// Only use shortId form!!!
                shortIdentifier, null);
        return authority.toString();
    }

    public static class CsidAndShortIdentifier {

        String CSID;
        String shortIdentifier;
    }

	protected String lookupParentCSID(String parentspecifier, String method,
			String op, UriInfo uriInfo) throws Exception {
		CsidAndShortIdentifier tempResult = lookupParentCSIDAndShortIdentifer(
				parentspecifier, method, op, uriInfo);
		return tempResult.CSID;
	}

    private CsidAndShortIdentifier lookupParentCSIDAndShortIdentifer(
    		String parentspecifier,
    		String method,
    		String op,
    		UriInfo uriInfo)
            throws Exception {
        CsidAndShortIdentifier result = new CsidAndShortIdentifier();
        Specifier parentSpec = getSpecifier(parentspecifier, method, op);
        // Note that we have to create the service context for the Items, not the main service
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
            String whereClause = buildWhereForAuthByName(parentSpec.value);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getServiceName(), uriInfo);
            parentcsid = getRepositoryClient(ctx).findDocCSID(null, ctx, whereClause); //FIXME: REM - If the parent has been soft-deleted, should we be looking for the item?
        }
        result.CSID = parentcsid;
        result.shortIdentifier = parentShortIdentifier;
        return result;
    }

    public String lookupItemCSID(String itemspecifier, String parentcsid, String method, String op, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws DocumentException {
        String itemcsid;
        Specifier itemSpec = getSpecifier(itemspecifier, method, op);
        if (itemSpec.form == SpecifierForm.CSID) {
            itemcsid = itemSpec.value;
        } else {
            String itemWhereClause = buildWhereForAuthItemByName(itemSpec.value, parentcsid);
            itemcsid = getRepositoryClient(ctx).findDocCSID(null, ctx, itemWhereClause); //FIXME: REM - Should we be looking for the 'wf_deleted' query param and filtering on it?
        }
        return itemcsid;
    }

    /*
     * Generally, callers will first call RefName.AuthorityItem.parse with a refName, and then 
     * use the returned item.inAuthority.resource and a resourceMap to get a service-specific
     * Resource. They then call this method on that resource.
     */
    @Override
   	public DocumentModel getDocModelForAuthorityItem(RepositoryInstance repoSession, RefName.AuthorityItem item) 
   			throws Exception, DocumentNotFoundException {
    	if(item == null) {
    		return null;
    	}
        String whereClause = buildWhereForAuthByName(item.getParentShortIdentifier());
        // Ensure we have the right context.
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(item.inAuthority.resource);
        
        // HACK - this really must be moved to the doc handler, not here. No Nuxeo specific stuff here!
        RepositoryJavaClientImpl client = (RepositoryJavaClientImpl)getRepositoryClient(ctx);
        String parentcsid = client.findDocCSID(repoSession, ctx, whereClause);

        String itemWhereClause = buildWhereForAuthItemByName(item.getShortIdentifier(), parentcsid);
        ctx = createServiceContext(getItemServiceName());
        DocumentWrapper<DocumentModel> docWrapper = client.findDoc(repoSession, ctx, itemWhereClause);
        DocumentModel docModel = docWrapper.getWrappedObject();
        return docModel;
    }


    @POST													//FIXME: REM - 5/1/2012 - We can probably remove this method.
    public Response createAuthority(String xmlPayload) { 	//REM - This method is never reached by the JAX-RS client -instead the "create" method in ResourceBase.java is getting called.
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

    protected String buildWhereForAuthByName(String name) {
        return authorityCommonSchemaName
                + ":" + AuthorityJAXBSchema.SHORT_IDENTIFIER
                + "='" + name + "'";
    }

    protected String buildWhereForAuthItemByName(String name, String parentcsid) {
        return authorityItemCommonSchemaName
                + ":" + AuthorityItemJAXBSchema.SHORT_IDENTIFIER
                + "='" + name + "' AND "
                + authorityItemCommonSchemaName + ":"
                + AuthorityItemJAXBSchema.IN_AUTHORITY + "="
                + "'" + parentcsid + "'";
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
    public byte[] get(
            @Context Request request,
            @Context UriInfo ui,
            @PathParam("csid") String specifier) {
        PoxPayloadOut result = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);

            Specifier spec = getSpecifier(specifier, "getAuthority", "GET");
            if (spec.form == SpecifierForm.CSID) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAuthority with csid=" + spec.value);
                }
                getRepositoryClient(ctx).get(ctx, spec.value, handler);
            } else {
                String whereClause = buildWhereForAuthByName(spec.value);
                DocumentFilter myFilter = new DocumentFilter(whereClause, 0, 1);
                handler.setDocumentFilter(myFilter);
                getRepositoryClient(ctx).get(ctx, handler);
            }
            result = ctx.getOutput();

        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, specifier);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Authority specifier:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        return result.getBytes();
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
    	AbstractCommonList result = null;
    	
    	try {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            DocumentFilter myFilter = handler.getDocumentFilter();
            // Need to make the default sort order for authority items
            // be on the displayName field
            String sortBy = queryParams.getFirst(IClientQueryParams.ORDER_BY_PARAM);
            if (sortBy == null || sortBy.isEmpty()) {
                String qualifiedDisplayNameField = authorityCommonSchemaName + ":"
                        + AuthorityItemJAXBSchema.DISPLAY_NAME;
                myFilter.setOrderByClause(qualifiedDisplayNameField);
            }
            String nameQ = queryParams.getFirst("refName");
            if (nameQ != null) {
                myFilter.setWhereClause(authorityCommonSchemaName + ":refName='" + nameQ + "'");
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            result = handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }
        
        return result;
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
            @PathParam("csid") String specifier,
            String xmlPayload) {
        PoxPayloadOut result = null;
        try {
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            Specifier spec = getSpecifier(specifier, "updateAuthority", "UPDATE");
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(theUpdate);
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            String csid;
            if (spec.form == SpecifierForm.CSID) {
                csid = spec.value;
            } else {
                String whereClause = buildWhereForAuthByName(spec.value);
                csid = getRepositoryClient(ctx).findDocCSID(null, ctx, whereClause);
            }
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED);
        }
        return result.getBytes();
    }

    /**
     * Delete authority.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteAuthority(@PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteAuthority with csid=" + csid);
        }
        try {
            ensureCSID(csid, ServiceMessages.DELETE_FAILED, "Authority.csid");
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).delete(ctx, csid, handler);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
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
    		@PathParam("csid") String parentspecifier,
    		String xmlPayload) {
    	Response result = null;
    	
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), input, resourceMap, uriInfo);

            // Note: must have the parentShortId, to do the create.
            CsidAndShortIdentifier parent = lookupParentCSIDAndShortIdentifer(parentspecifier, "createAuthorityItem", "CREATE_ITEM", null);
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = 
            	createItemDocumentHandler(ctx, parent.CSID, parent.shortIdentifier);
            String itemcsid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(resourceClass);
            path.path(parent.CSID + "/items/" + itemcsid);
            result = Response.created(path.build()).build();
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

    //FIXME: This method is almost identical to the method org.collectionspace.services.common.updateWorkflowWithTransition() so
    // they should be consolidated -be DRY (don't repeat yourself).
    @PUT
    @Path("{csid}/items/{itemcsid}" + WorkflowClient.SERVICE_PATH + "/{transition}")
    public byte[] updateItemWorkflowWithTransition(
            @PathParam("csid") String csid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("transition") String transition) {
        PoxPayloadOut result = null;
        
        try {
        	//
        	// Create an empty workflow_commons input part and set it into a new "workflow" sub-resource context
        	PoxPayloadIn input = new PoxPayloadIn(WorkflowClient.SERVICE_PAYLOAD_NAME, new WorkflowCommon(), 
        			WorkflowClient.SERVICE_COMMONPART_NAME);
            MultipartServiceContext ctx = (MultipartServiceContext) createServiceContext(WorkflowClient.SERVICE_NAME, input);

            // Create a service context and document handler for the parent resource.
            ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx = createServiceContext(getItemServiceName());
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> parentDocHandler = this.createDocumentHandler(parentCtx);
            ctx.setProperty(WorkflowClient.PARENT_DOCHANDLER, parentDocHandler); //added as a context param for the workflow document handler -it will call the parent's dochandler "prepareForWorkflowTranstion" method

            // When looking for the document, we need to use the parent's workspace name -not the "workflow" workspace name
            String parentWorkspaceName = parentCtx.getRepositoryWorkspaceName();
            ctx.setRespositoryWorkspaceName(parentWorkspaceName); //find the document in the parent's workspace
            
        	// Get the type of transition we're being asked to make and store it as a context parameter -used by the workflow document handler
            TransitionDef transitionDef = getTransitionDef(parentCtx, transition);
            ctx.setProperty(WorkflowClient.TRANSITION_ID, transitionDef);
            
            WorkflowDocumentModelHandler handler = createWorkflowDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, itemcsid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED + WorkflowClient.SERVICE_PAYLOAD_NAME, csid);
        }
        
        return result.getBytes();
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
            @PathParam("csid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier) {
        PoxPayloadOut result = null;
        try {
            String parentcsid = lookupParentCSID(parentspecifier, "getAuthorityItem(parent)", "GET_ITEM", uriInfo);

            RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = 
            	(RemoteServiceContext<PoxPayloadIn, PoxPayloadOut>) createServiceContext(getItemServiceName(), resourceMap, uriInfo);
            
            JaxRsContext jaxRsContext = new JaxRsContext(request, uriInfo); // REM - Why are we setting this?  Who is using the getter?
            ctx.setJaxRsContext(jaxRsContext);

            // We omit the parentShortId, only needed when doing a create...
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createItemDocumentHandler(ctx, parentcsid, null);

            Specifier itemSpec = getSpecifier(itemspecifier, "getAuthorityItem(item)", "GET_ITEM");
            if (itemSpec.form == SpecifierForm.CSID) {
                getRepositoryClient(ctx).get(ctx, itemSpec.value, handler);
            } else {
                String itemWhereClause =
                        buildWhereForAuthItemByName(itemSpec.value, parentcsid);
                DocumentFilter myFilter = new DocumentFilter(itemWhereClause, 0, 1);
                handler.setDocumentFilter(myFilter);
                getRepositoryClient(ctx).get(ctx, handler);
            }
            // TODO should we assert that the item is in the passed vocab?
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested AuthorityItem specifier:" + itemspecifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
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
     * @param specifier either a CSID or one of the urn forms
     * @param partialTerm if non-null, matches partial terms
     * @param keywords if non-null, matches terms in the keyword index for items
     * @param ui passed to include additional parameters, like pagination controls
     * 
     * @return the authorityItem list
     */
    @GET
    @Path("{csid}/items")
    @Produces("application/xml")
    public AbstractCommonList getAuthorityItemList(@PathParam("csid") String specifier,
            @Context UriInfo uriInfo) {
    	AbstractCommonList result = null;
    	
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
            
            String orderBy = queryParams.getFirst(IClientQueryParams.ORDER_BY_PARAM);
            String termStatus = queryParams.getFirst(SEARCH_TYPE_TERMSTATUS);
            String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
            String advancedSearch = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_AS);
            String partialTerm = queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);

            // For the wildcard case, parentcsid is null, but docHandler will deal with this.
            // We omit the parentShortId, only needed when doing a create...
            String parentcsid = PARENT_WILDCARD.equals(specifier) ? null :
				lookupParentCSID(specifier, "getAuthorityItemList", "LIST", uriInfo);
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
            @PathParam("csid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @Context UriTemplateRegistry uriTemplateRegistry,
            @Context UriInfo uriInfo) {
        AuthorityRefDocList authRefDocList = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();

            String parentcsid = lookupParentCSID(parentspecifier, "getReferencingObjects(parent)", "GET_ITEM_REF_OBJS", uriInfo);
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "getReferencingObjects(item)", "GET_ITEM_REF_OBJS", ctx);

            List<String> serviceTypes = queryParams.remove(ServiceBindingUtils.SERVICE_TYPE_PROP);
            if(serviceTypes == null || serviceTypes.isEmpty()) {
            	serviceTypes = ServiceBindingUtils.getCommonServiceTypes(true); //CSPACE-5359: Should now include objects, procedures, and authorities
            }
            
            // Note that we have to create the service context for the Items, not the main service
            // We omit the parentShortId, only needed when doing a create...
            AuthorityItemDocumentModelHandler<?> handler = (AuthorityItemDocumentModelHandler<?>)
            											createItemDocumentHandler(ctx, parentcsid, null);

            authRefDocList = handler.getReferencingObjects(ctx, uriTemplateRegistry, serviceTypes, getRefPropName(), itemcsid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }
        if (authRefDocList == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Item CSID:" + itemspecifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
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
        AuthorityRefList authRefList = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), uriInfo);
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
            String parentcsid = lookupParentCSID(parentspecifier, "getAuthorityItemAuthRefs(parent)", "GET_ITEM_AUTH_REFS", uriInfo);
            // We omit the parentShortId, only needed when doing a create...
            DocumentModelHandler<?, AbstractCommonList> handler =
                    (DocumentModelHandler<?, AbstractCommonList>)createItemDocumentHandler(ctx, parentcsid, null /*no parent short ID*/);

            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "getAuthorityItemAuthRefs(item)", "GET_ITEM_AUTH_REFS", ctx);

            List<RefNameServiceUtils.AuthRefConfigInfo> authRefsInfo = RefNameServiceUtils.getConfiguredAuthorityRefs(ctx);
            authRefList = handler.getAuthorityRefs(itemcsid, authRefsInfo);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED + " parentspecifier: " + parentspecifier + " itemspecifier:" + itemspecifier);
        }
        return authRefList;
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
            @PathParam("csid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            String xmlPayload) {
        PoxPayloadOut result = null;
        try {
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            // Note that we have to create the service context for the Items, not the main service
            // Laramie CSPACE-3175.  passing null for queryParams, because prior to this refactor, the code moved to lookupParentCSID in this instance called the version of getServiceContext() that passes null
            CsidAndShortIdentifier csidAndShortId = lookupParentCSIDAndShortIdentifer(parentspecifier, "updateAuthorityItem(parent)", "UPDATE_ITEM", null);
            String parentcsid = csidAndShortId.CSID;
            String parentShortId = csidAndShortId.shortIdentifier;

            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), theUpdate, resourceMap, uriInfo);
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "updateAuthorityItem(item)", "UPDATE_ITEM", ctx);

            // We omit the parentShortId, only needed when doing a create...
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createItemDocumentHandler(ctx, parentcsid, parentShortId);
            getRepositoryClient(ctx).update(ctx, itemcsid, handler);
            result = ctx.getOutput();

        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED);
        }
        return result.getBytes();
    }

    /**
     * Delete authorityItem.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}/items/{itemcsid}")
    public Response deleteAuthorityItem(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        //try{
        if (logger.isDebugEnabled()) {
            logger.debug("deleteAuthorityItem with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        try {
            ensureCSID(parentcsid, ServiceMessages.DELETE_FAILED, "AuthorityItem.parentcsid");
            ensureCSID(itemcsid, ServiceMessages.DELETE_FAILED, "AuthorityItem.itemcsid");
            //Laramie, removing this catch, since it will surely fail below, since itemcsid or parentcsid will be null.
            // }catch (Throwable t){
            //    System.out.println("ERROR in setting up DELETE: "+t);
            // }
            // try {
            // Note that we have to create the service context for the Items, not the main service
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName());
            DocumentHandler<?, AbstractCommonList, DocumentModel, DocumentModelList> handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).delete(ctx, itemcsid, handler);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED + "  itemcsid: " + itemcsid + " parentcsid:" + parentcsid);
        }
    }
    public final static String hierarchy = "hierarchy";

    @GET
    @Path("{csid}/items/{itemcsid}/" + hierarchy)
    @Produces("application/xml")
    public String getHierarchy(@PathParam("csid") String csid,
            @PathParam("itemcsid") String itemcsid,
            @Context UriInfo ui) throws Exception {
        try {
            // All items in dive can look at their child uri's to get uri.  So we calculate the very first one.  We could also do a GET and look at the common part uri field, but why...?
            String calledUri = ui.getPath();
            String uri = "/" + calledUri.substring(0, (calledUri.length() - ("/" + hierarchy).length()));
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName(), ui);
            ctx.setUriInfo(ui);
            String direction = ui.getQueryParameters().getFirst(Hierarchy.directionQP);
            if (Tools.notBlank(direction) && Hierarchy.direction_parents.equals(direction)) {
                return Hierarchy.surface(ctx, itemcsid, uri);
            } else {
                return Hierarchy.dive(ctx, itemcsid, uri);
            }
        } catch (Exception e) {
            throw bigReThrow(e, "Error showing hierarchy", itemcsid);
        }
    }
    
    protected String getItemDocType(String tenantId) {
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
  
}
