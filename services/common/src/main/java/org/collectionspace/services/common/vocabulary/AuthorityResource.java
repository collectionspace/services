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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuthorityResource.
 */
@Consumes("application/xml")
@Produces("application/xml")
public abstract class AuthorityResource<AuthCommon, AuthCommonList, AuthItemCommonList, AuthItemHandler> extends
	AbstractMultiPartCollectionSpaceResourceImpl {

	protected Class<AuthCommon> authCommonClass;
	protected Class<?> resourceClass;
	protected String authorityCommonSchemaName;
	protected String authorityItemCommonSchemaName;

	final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();
	
	final static String URN_PREFIX = "urn:cspace:";
	final static int URN_PREFIX_LEN = URN_PREFIX.length();
	final static String URN_PREFIX_NAME = "name(";
	final static int URN_NAME_PREFIX_LEN = URN_PREFIX_LEN + URN_PREFIX_NAME.length();
	final static String URN_PREFIX_ID = "id(";
	final static int URN_ID_PREFIX_LEN = URN_PREFIX_LEN + URN_PREFIX_ID.length();
	
    final Logger logger = LoggerFactory.getLogger(AuthorityResource.class);
    
    public enum SpecifierForm { CSID, URN_NAME };
    
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
			logger.debug("getSpecifier called by: "+method+" with specifier: "+specifierIn);
		}
		if (specifierIn != null) {
			if(!specifierIn.startsWith(URN_PREFIX)) {
				// We'll assume it is a CSID and complain if it does not match
				return new Specifier(SpecifierForm.CSID, specifierIn);
			} else { 
				if(specifierIn.startsWith(URN_PREFIX_NAME, URN_PREFIX_LEN)) {
					int closeParen = specifierIn.indexOf(')', URN_NAME_PREFIX_LEN);
					if(closeParen>=0) {
						return new Specifier(SpecifierForm.URN_NAME,
									specifierIn.substring(URN_NAME_PREFIX_LEN, closeParen));
					}
				} else if(specifierIn.startsWith(URN_PREFIX_ID, URN_PREFIX_LEN)) {
					int closeParen = specifierIn.indexOf(')', URN_ID_PREFIX_LEN);
					if(closeParen>=0) {
						return new Specifier(SpecifierForm.CSID,
								specifierIn.substring(URN_ID_PREFIX_LEN, closeParen));
					}
				}
			}
		}
		logger.error(method+": bad or missing specifier!");
		Response response = Response.status(Response.Status.BAD_REQUEST).entity(
				op+" failed on bad or missing Authority specifier").type(
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

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
	 */
	@Override
	protected String getVersionString() {
		/** The last change revision. */
		final String lastChangeRevision = "$LastChangedRevision: 2617 $";
		return lastChangeRevision;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
	 */
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
	public DocumentHandler createItemDocumentHandler(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			String inAuthority)
	throws Exception {
		AuthItemHandler docHandler;

		docHandler = (AuthItemHandler)createDocumentHandler(ctx,
				ctx.getCommonPartLabel(getItemServiceName()),
				authCommonClass);  	
		((AuthorityItemDocumentModelHandler<?,?>)docHandler).setInAuthority(inAuthority);

		return (DocumentHandler)docHandler;
	}

	/**
	 * Creates the authority.
	 * 
	 * @param input the input
	 * 
	 * @return the response
	 */
	@POST
	public Response createAuthority(String xmlPayload) {
		try {
			PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
			DocumentHandler handler = createDocumentHandler(ctx);
			String csid = getRepositoryClient(ctx).create(ctx, handler);
			UriBuilder path = UriBuilder.fromResource(resourceClass);
			path.path("" + csid);
			Response response = Response.created(path.build()).build();
			return response;
		} catch (BadRequestException bre) {
			Response response = Response.status(
					Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in createVocabulary", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
	}
	
	protected String buildWhereForAuthByName(String name) {
		return authorityCommonSchemaName+
				":"+AuthorityJAXBSchema.SHORT_IDENTIFIER+
				"='"+name+"'";
	}

	protected String buildWhereForAuthItemByName(String name, String parentcsid) {
        return
        	authorityItemCommonSchemaName+
        	":"+AuthorityItemJAXBSchema.SHORT_IDENTIFIER+
        	"='"+name+"' AND "
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
	public byte[] getAuthority(@PathParam("csid") String specifier) {
		PoxPayloadOut result = null;
		try {
			Specifier spec = getSpecifier(specifier, "getAuthority", "GET");
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
			DocumentHandler handler = createDocumentHandler(ctx);
			if(spec.form == SpecifierForm.CSID) {
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
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthority", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed on Authority specifier=" + specifier).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthority", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
			throw new WebApplicationException(response);
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
    public AuthCommonList getAuthorityList(@Context UriInfo ui) {
		try {
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
			DocumentHandler handler = createDocumentHandler(ctx);
			DocumentFilter myFilter = handler.getDocumentFilter();
			String nameQ = queryParams.getFirst("refName");
			if (nameQ != null) {
				myFilter.setWhereClause(authorityCommonSchemaName+":refName='" + nameQ + "'");
			}
			getRepositoryClient(ctx).getFiltered(ctx, handler);
			return (AuthCommonList) handler.getCommonPartList();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in getAuthorityList", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
	}

	/**
	 * Update authority.
	 * 
	 * @param specifier the csid or id
	 * @param theUpdate the the update
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
			DocumentHandler handler = createDocumentHandler(ctx);
			String csid;
			if(spec.form==SpecifierForm.CSID) {
				csid = spec.value;
			} else {
				String whereClause = buildWhereForAuthByName(spec.value);
				csid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
			getRepositoryClient(ctx).update(ctx, csid, handler);
			result = ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught exception in updateAuthority", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Update failed on Authority specifier=" + specifier).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
			throw new WebApplicationException(response);
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
		if (csid == null || "".equals(csid)) {
			logger.error("deleteAuthority: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"delete failed on Authority csid=" + csid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		try {
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
			getRepositoryClient(ctx).delete(ctx, csid);
			return Response.status(HttpResponseCodes.SC_OK).build();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught exception in deleteAuthority", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Delete failed on Authority csid=" + csid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

	}

	/*************************************************************************
	 * Create an AuthorityItem - this is a sub-resource of Authority
	 * @param specifier either a CSID or one of the urn forms
	 * @param input the payload 
	 * @return Authority item response
	 *************************************************************************/
	@POST
	@Path("{csid}/items")
	public Response createAuthorityItem(@PathParam("csid") String specifier, String xmlPayload) {
		try {
			PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
			Specifier spec = getSpecifier(specifier, "createAuthorityItem", "CREATE_ITEM");
			String parentcsid;
			if(spec.form==SpecifierForm.CSID) {
				parentcsid = spec.value;
			} else {
				String whereClause = buildWhereForAuthByName(spec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
			ctx = createServiceContext(getItemServiceName(), input);
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			String itemcsid = getRepositoryClient(ctx).create(ctx, handler);
			UriBuilder path = UriBuilder.fromResource(resourceClass);
			path.path(parentcsid + "/items/" + itemcsid);
			Response response = Response.created(path.build()).build();
			return response;
		} catch (BadRequestException bre) {
			Response response = Response.status(
					Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in createAuthorityItem", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
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
			@PathParam("csid") String parentspecifier,
			@PathParam("itemcsid") String itemspecifier) {
		PoxPayloadOut result = null;
		try {
			Specifier parentSpec = getSpecifier(parentspecifier, "getAuthorityItem(parent)", "GET_ITEM");
			Specifier itemSpec = getSpecifier(itemspecifier, "getAuthorityItem(item)", "GET_ITEM");
			// Note that we have to create the service context for the Items, not the main service
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
				ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
			ctx = createServiceContext(getItemServiceName());
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			if(itemSpec.form==SpecifierForm.CSID) {
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
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthorityItem", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed on AuthorityItem specifier=" + itemspecifier).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthorityItem", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		if (result == null) {
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed, the requested AuthorityItem specifier:" + itemspecifier + ": was not found.").type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		return result.getBytes();
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
	public AuthItemCommonList getAuthorityItemList(
			@PathParam("csid") String specifier,
			@QueryParam(IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
			@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
			@Context UriInfo ui) {
		try {
			Specifier spec = getSpecifier(specifier, "getAuthorityItemList", "LIST");
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			// Note that docType defaults to the ServiceName, so we're fine with that.
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
			String parentcsid;
			if(spec.form==SpecifierForm.CSID) {
				parentcsid = spec.value;
			} else {
				String whereClause = buildWhereForAuthByName(spec.value);
				ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
			ctx = createServiceContext(getItemServiceName(), queryParams);
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			DocumentFilter myFilter = handler.getDocumentFilter();
			myFilter.setWhereClause(
					authorityItemCommonSchemaName + ":"
					+ AuthorityItemJAXBSchema.IN_AUTHORITY + "="
					+ "'" + parentcsid + "'");

			// AND vocabularyitems_common:displayName LIKE '%partialTerm%'
			if (partialTerm != null && !partialTerm.isEmpty()) {
				String ptClause = QueryManager.createWhereClauseForPartialMatch(
				authorityItemCommonSchemaName + ":"
				+ AuthorityItemJAXBSchema.DISPLAY_NAME, partialTerm );
				myFilter.appendWhereClause(ptClause, IQueryManager.SEARCH_QUALIFIER_AND);
			} else if (keywords != null) {
				String kwdClause = QueryManager.createWhereClauseFromKeywords(keywords);
				myFilter.appendWhereClause(kwdClause, IQueryManager.SEARCH_QUALIFIER_AND);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthorityItemList filtered WHERE clause: "
						+ myFilter.getWhereClause());
			}
			getRepositoryClient(ctx).getFiltered(ctx, handler);
			return (AuthItemCommonList) handler.getCommonPartList();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in getAuthorityItemList", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
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
    		@Context UriInfo ui) {
    	AuthorityRefDocList authRefDocList = null;
    	try {
    		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"getReferencingObjects(parent)", "GET_ITEM_REF_OBJS");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"getReferencingObjects(item)", "GET_ITEM_REF_OBJS");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
    		ctx = createServiceContext(getItemServiceName(), queryParams);
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
    		// Note that we have to create the service context for the Items, not the main service
    		DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
    		RepositoryClient repoClient = getRepositoryClient(ctx); 
    		DocumentFilter myFilter = handler.getDocumentFilter();
    		String serviceType = ServiceBindingUtils.SERVICE_TYPE_PROCEDURE;
    		List<String> list = queryParams.remove(ServiceBindingUtils.SERVICE_TYPE_PROP);
    		if (list != null) {
    			serviceType = list.get(0);
    		}
    		DocumentWrapper<DocumentModel> docWrapper = repoClient.getDoc(ctx, itemcsid);
    		DocumentModel docModel = docWrapper.getWrappedObject();
    		String refName = (String)docModel.getPropertyValue(AuthorityItemJAXBSchema.REF_NAME);

    		authRefDocList = RefNameServiceUtils.getAuthorityRefDocs(ctx,
    				repoClient, 
    				serviceType,
    				refName,
    				myFilter.getPageSize(), myFilter.getStartPage(), true /*computeTotal*/ );
    	} catch (UnauthorizedException ue) {
    		Response response = Response.status(
    				Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
    		throw new WebApplicationException(response);
    	} catch (DocumentNotFoundException dnfe) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("getReferencingObjects", dnfe);
    		}
    		Response response = Response.status(Response.Status.NOT_FOUND).entity(
    				"GetReferencingObjects failed with parentspecifier=" 
    				+ parentspecifier + " and itemspecifier=" + itemspecifier).type(
    				"text/plain").build();
    		throw new WebApplicationException(response);
    	} catch (Exception e) {	// Includes DocumentException
    		if (logger.isDebugEnabled()) {
    			logger.debug("GetReferencingObjects", e);
    		}
    		Response response = Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
    		throw new WebApplicationException(response);
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
    		@Context UriInfo ui) {
    	AuthorityRefList authRefList = null;
        try {
   			Specifier parentSpec = getSpecifier(parentspecifier, "getAuthorityItemAuthRefs(parent)", "GET_ITEM_AUTH_REFS");
			Specifier itemSpec = getSpecifier(itemspecifier, "getAuthorityItemAuthRefs(item)", "GET_ITEM_AUTH_REFS");
			// Note that we have to create the service context for the Items, not the main service
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
            ctx = createServiceContext(getItemServiceName(), queryParams);
            RemoteDocumentModelHandlerImpl handler =
                (RemoteDocumentModelHandlerImpl) createItemDocumentHandler(ctx, parentcsid);
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
            DocumentWrapper<DocumentModel> docWrapper =
               getRepositoryClient(ctx).getDoc(ctx, itemcsid);
            List<String> authRefFields =
            	((MultipartServiceContextImpl)ctx).getCommonPartPropertyValues(
            	ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            authRefList = handler.getAuthorityRefs(docWrapper, authRefFields);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Failed to retrieve authority references: reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve authority references").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return authRefList;
    }

	/**
	 * Update authorityItem.
	 * 
	 * @param parentspecifier either a CSID or one of the urn forms
	 * @param itemspecifier either a CSID or one of the urn forms
	 * @param theUpdate the the update
	 * 
	 * @return the multipart output
	 */
	@PUT
	@Path("{csid}/items/{itemcsid}")
	public byte[] updateAuthorityItem(
			@PathParam("csid") String parentspecifier,
			@PathParam("itemcsid") String itemspecifier,
			String xmlPayload) {
		PoxPayloadOut result = null;
		try {
			PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"updateAuthorityItem(parent)", "UPDATE_ITEM");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"updateAuthorityItem(item)", "UPDATE_ITEM");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
			ctx = createServiceContext(getItemServiceName(), theUpdate);
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
			// Note that we have to create the service context for the Items, not the main service
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			getRepositoryClient(ctx).update(ctx, itemcsid, handler);
			result = ctx.getOutput();
		} catch (BadRequestException bre) {
			Response response = Response.status(
					Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught DNF exception in updateAuthorityItem", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Update failed on AuthorityItem csid=" + itemspecifier).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
			throw new WebApplicationException(response);
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
		if (logger.isDebugEnabled()) {
			logger.debug("deleteAuthorityItem with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
		}
		if (parentcsid == null || "".equals(parentcsid)) {
			logger.error("deleteVocabularyItem: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"delete failed on AuthorityItem parentcsid=" + parentcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		if (itemcsid == null || "".equals(itemcsid)) {
			logger.error("deleteVocabularyItem: missing itemcsid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"delete failed on AuthorityItem=" + itemcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		try {
			// Note that we have to create the service context for the Items, not the main service
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getItemServiceName());
			getRepositoryClient(ctx).delete(ctx, itemcsid);
			return Response.status(HttpResponseCodes.SC_OK).build();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught exception in deleteAuthorityItem", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Delete failed on AuthorityItem itemcsid=" + itemcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
	}
    
}
