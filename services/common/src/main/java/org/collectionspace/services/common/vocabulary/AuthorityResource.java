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
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VocabularyResource.
 */
@Path("/vocabularies")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public abstract class AuthorityResource<AuthCommon, AuthCommonList, AuthItemCommonList, AuthItemHandler> extends
AbstractMultiPartCollectionSpaceResourceImpl {

	private Class<AuthCommon> authCommonClass;
	private Class<?> resourceClass;
	private String authorityCommonSchemaName;
	private String authorityItemCommonSchemaName;

	final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    final Logger logger = LoggerFactory.getLogger(AuthorityResource.class);

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
	 * @param inVocabulary the in vocabulary
	 * 
	 * @return the document handler
	 * 
	 * @throws Exception the exception
	 */
	public DocumentHandler createItemDocumentHandler(
			ServiceContext<MultipartInput, MultipartOutput> ctx,
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
	public Response createAuthority(MultipartInput input) {
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
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

	/**
	 * Gets the authority.
	 * 
	 * @param csid the csid
	 * 
	 * @return the authority
	 */
	@GET
	@Path("{csid}")
	public MultipartOutput getAuthority(@PathParam("csid") String csid) {
		if (csid == null) {
			logger.error("getAuthority: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"get failed on Authority csid=" + csid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("getAuthority with path(id)=" + csid);
		}
		MultipartOutput result = null;
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
			DocumentHandler handler = createDocumentHandler(ctx);
			getRepositoryClient(ctx).get(ctx, csid, handler);
			result = (MultipartOutput) ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthority", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed on Authority csid=" + csid).type(
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
					"Get failed, the requested Authority CSID:" + csid + ": was not found.").type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}

		return result;
	}

	/**
	 * Gets the authority by name.
	 * 
	 * @param specifier the specifier
	 * 
	 * @return the authority
	 */
	@GET
	@Path("urn:cspace:name({specifier})")
	public MultipartOutput getAuthorityByName(@PathParam("specifier") String specifier) {
		if (specifier == null) {
			logger.error("getAuthorityByName: missing name!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
			"get failed on Authority (missing specifier)").type(
			"text/plain").build();
			throw new WebApplicationException(response);
		}
		String whereClause =
			authorityCommonSchemaName+
			":"+AuthorityJAXBSchema.SHORT_IDENTIFIER+
			"='"+specifier+"'";
		// We only get a single doc - if there are multiple,
		// it is an error in use.

		if (logger.isDebugEnabled()) {
			logger.debug("getAuthorityByName with name=" + specifier);
		} 
		MultipartOutput result = null;
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
			DocumentHandler handler = createDocumentHandler(ctx);
			DocumentFilter myFilter = new DocumentFilter(whereClause, 0, 1);
			handler.setDocumentFilter(myFilter);
			getRepositoryClient(ctx).get(ctx, handler);
			result = (MultipartOutput) ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthorityByName", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed on Authority spec=" + specifier).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthorityByName", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		if (result == null) {
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed, the requested Authority spec:" + specifier + ": was not found.").type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		return result;
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
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
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
	 * @param csid the csid
	 * @param theUpdate the the update
	 * 
	 * @return the multipart output
	 */
	@PUT
	@Path("{csid}")
	public MultipartOutput updateAuthority(
			@PathParam("csid") String csid,
			MultipartInput theUpdate) {
		if (logger.isDebugEnabled()) {
			logger.debug("updateAuthority with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("updateAuthority: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"update failed on Authority csid=" + csid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		MultipartOutput result = null;
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(theUpdate);
			DocumentHandler handler = createDocumentHandler(ctx);
			getRepositoryClient(ctx).update(ctx, csid, handler);
			result = (MultipartOutput) ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught exception in updateAuthority", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Update failed on Authority csid=" + csid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		return result;
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
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
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
	 * AuthorityItem parts - this is a sub-resource of Authority
	 * @param parentcsid 
	 * @param input 
	 * @return Authority item response
	 *************************************************************************/
	@POST
	@Path("{csid}/items")
	public Response createAuthorityItem(@PathParam("csid") String parentcsid, MultipartInput input) {
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
					input);
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
	 * @param parentcsid the parentcsid
	 * @param itemcsid the itemcsid
	 * 
	 * @return the authority item
	 */
	@GET
	@Path("{csid}/items/{itemcsid}")
	public MultipartOutput getAuthorityItem(
			@PathParam("csid") String parentcsid,
			@PathParam("itemcsid") String itemcsid) {
		if (logger.isDebugEnabled()) {
			logger.debug("getAuthorityItem with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
		}
		if (parentcsid == null || "".equals(parentcsid)) {
			logger.error("getAuthorityItem: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"get failed on AuthorityItem csid=" + parentcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		if (itemcsid == null || "".equals(itemcsid)) {
			logger.error("getAuthorityItem: missing itemcsid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"get failed on AuthorityItem itemcsid=" + itemcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		MultipartOutput result = null;
		try {
			// Note that we have to create the service context for the Items, not the main service
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			getRepositoryClient(ctx).get(ctx, itemcsid, handler);
			// TODO should we assert that the item is in the passed vocab?
			result = (MultipartOutput) ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("getAuthorityItem", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND).entity(
					"Get failed on AuthorityItem csid=" + itemcsid).type(
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
					"Get failed, the requested AuthorityItem CSID:" + itemcsid + ": was not found.").type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		return result;
	}

    /**
     * Gets the authority item by name.
     * 
     * @param parentcsid the parentcsid
     * @param itemspecifier the shortId of the person
     * 
     * @return the authority item
     */
    @GET
    @Path("{csid}/items/urn:cspace:name({itemspecifier})")
    public MultipartOutput getAuthorityItemByName(
            @PathParam("csid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier) {
        if (parentcsid == null || "".equals(parentcsid)
            || itemspecifier == null || "".equals(itemspecifier)) {
            logger.error("getAuthorityItemByName: missing parentcsid or itemspecifier!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on AuthorityItem with parentcsid=" 
            		+ parentcsid + " and itemspecifier=" + itemspecifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        String whereClause =
        	authorityItemCommonSchemaName+
        	":"+AuthorityJAXBSchema.SHORT_IDENTIFIER+
        	"='"+itemspecifier+"'";
                
        if (logger.isDebugEnabled()) {
            logger.debug("getAuthorityItemByName with parentcsid=" + parentcsid + " and itemspecifier=" + itemspecifier);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            DocumentFilter myFilter = new DocumentFilter(whereClause, 0, 1);
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).get(ctx, handler);
            // TODO should we assert that the item is in the passed personAuthority?
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAuthorityItemByName", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on AuthorityItem itemspecifier=" + itemspecifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAuthorityItemByName", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested AuthorityItem itemspecifier:" + itemspecifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the AuthorityItem by name, in a named authority.
     * 
     * @param parentspecifier the shortId of the parent
     * @param itemspecifier the shortId of the person
     * 
     * @return the person
     */
    @GET
    @Path("urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})")
    public MultipartOutput getAuthorityItemByNameInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier) {
        if (parentspecifier == null || "".equals(parentspecifier)
            || itemspecifier == null || "".equals(itemspecifier)) {
            logger.error("getAuthorityItemByNameInNamedAuthority: missing parentcsid or itemspecifier!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on AuthorityItem with parentspecifier=" 
            		+ parentspecifier + " and itemspecifier=" + itemspecifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        // TODO REWRITE to get the CSID for the parent by name, and then call getAuthorityItemByName 
        String whereClause =
        	authorityItemCommonSchemaName+
        	":"+AuthorityJAXBSchema.SHORT_IDENTIFIER+
        	"='"+itemspecifier+"'";
        if (logger.isDebugEnabled()) {
            logger.debug("getAuthorityItemByNameInNamedAuthority with parentspecifier=" 
            		+ parentspecifier + " and itemspecifier=" + itemspecifier);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
        	// HACK HACK Since we do not use the parent CSID yet this should work.
            DocumentHandler handler = createItemDocumentHandler(ctx, parentspecifier);
            DocumentFilter myFilter = new DocumentFilter(whereClause, 0, 1);
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).get(ctx, handler);
            // TODO should we assert that the item is in the passed personAuthority?
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAuthorityItemByNameInNamedAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on AuthorityItem itemspecifier=" + itemspecifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAuthorityItemByNameInNamedAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested AuthorityItem itemspecifier:" + itemspecifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

	/**
	 * Gets the authorityItem list.
	 * 
	 * @param parentcsid the parentcsid
	 * @param partialTerm the partial term
	 * @param ui the ui
	 * 
	 * @return the authorityItem list
	 */
	@GET
	@Path("{csid}/items")
	@Produces("application/xml")
	public AuthItemCommonList getAuthorityItemList(
			@PathParam("csid") String parentcsid,
			@QueryParam(IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
			@Context UriInfo ui) {
		try {
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			// Note that docType defaults to the ServiceName, so we're fine with that.
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
					queryParams);
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			DocumentFilter myFilter = handler.getDocumentFilter();
			myFilter.setWhereClause(
					authorityItemCommonSchemaName + ":"
					+ AuthorityItemJAXBSchema.IN_AUTHORITY + "="
					+ "'" + parentcsid + "'");

			// AND vocabularyitems_common:displayName LIKE '%partialTerm%'
			if (partialTerm != null && !partialTerm.isEmpty()) {
				String ptClause = authorityItemCommonSchemaName + ":"
				+ AuthorityItemJAXBSchema.DISPLAY_NAME
				+ IQueryManager.SEARCH_LIKE
				+ "'%" + partialTerm + "%'";
				myFilter.appendWhereClause(ptClause, IQueryManager.SEARCH_QUALIFIER_AND);
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
	 * Gets the authorityItem list using the shortIdentifier.
	 * 
	 * @param specifier the shortIdentifier
	 * @param partialTerm the partial term
	 * @param ui the ui
	 * 
	 * @return the authorityItem list
	 */
	@GET
	@Path("urn:cspace:name({specifier})/items")
	@Produces("application/xml")
	public AuthItemCommonList getAuthorityItemListByAuthName(
			@PathParam("specifier") String specifier,
			@QueryParam(IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
			@Context UriInfo ui) {
		try {
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			String whereClause =
				authorityCommonSchemaName+
				":"+AuthorityJAXBSchema.SHORT_IDENTIFIER+
				"='"+specifier+"'";
			// Need to get an Authority by name
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
			String parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			return getAuthorityItemList(parentcsid, partialTerm, ui);
		} catch (UnauthorizedException ue) {
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in getVocabularyItemListByVocabName", e);
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
     * @param parentcsid 
     * 
     * @param csid the parent csid
     * @param itemcsid the person csid
     * @param ui the ui
     * 
     * @return the info for the referencing objects
     */
    @GET
    @Path("{csid}/items/{itemcsid}/refObjs")
    @Produces("application/xml")
    public AuthorityRefDocList getReferencingObjects(
    		@PathParam("csid") String parentcsid,
    		@PathParam("itemcsid") String itemcsid,
    		@Context UriInfo ui) {
    	AuthorityRefDocList authRefDocList = null;
    	if (logger.isDebugEnabled()) {
    		logger.debug("getReferencingObjects with parentcsid=" 
    				+ parentcsid + " and itemcsid=" + itemcsid);
    	}
    	if (parentcsid == null || "".equals(parentcsid)
    			|| itemcsid == null || "".equals(itemcsid)) {
    		logger.error("getReferencingObjects: missing parentcsid or itemcsid!");
    		Response response = Response.status(Response.Status.BAD_REQUEST).entity(
    				"get failed on ReferencingObjects with parentcsid=" 
    				+ parentcsid + " and itemcsid=" + itemcsid).type(
    				"text/plain").build();
    		throw new WebApplicationException(response);
    	}
    	try {
    		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    		// Note that we have to create the service context for the Items, not the main service
    		ServiceContext<MultipartInput, MultipartOutput> ctx = 
    			createServiceContext(getItemServiceName(), queryParams);
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
    				"GetReferencingObjects failed with parentcsid=" 
    				+ parentcsid + " and itemcsid=" + itemcsid).type(
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
    				"Get failed, the requested Item CSID:" + itemcsid + ": was not found.").type(
    				"text/plain").build();
    		throw new WebApplicationException(response);
    	}
    	return authRefDocList;
    }

    /**
     * Gets the authority refs for an Authority item.
     * @param parentcsid 
     *
     * @param csid The authority (parent) CSID.
     * @param itemcsid The item CSID.
     * @param ui 
     *
     * @return the authority refs for the Authority item.
     */
    @GET
    @Path("{csid}/items/{itemcsid}/authorityrefs")
    @Produces("application/xml")
    public AuthorityRefList getAuthorityItemAuthorityRefs(
    		@PathParam("csid") String parentcsid,
                @PathParam("itemcsid") String itemcsid,
    		@Context UriInfo ui) {
    	AuthorityRefList authRefList = null;
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            ServiceContext<MultipartInput, MultipartOutput> ctx =
                createServiceContext(getItemServiceName(), queryParams);
            RemoteDocumentModelHandlerImpl handler =
                (RemoteDocumentModelHandlerImpl) createItemDocumentHandler(ctx, parentcsid);
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
	 * @param parentcsid the parentcsid
	 * @param itemcsid the itemcsid
	 * @param theUpdate the the update
	 * 
	 * @return the multipart output
	 */
	@PUT
	@Path("{csid}/items/{itemcsid}")
	public MultipartOutput updateAuthorityItem(
			@PathParam("csid") String parentcsid,
			@PathParam("itemcsid") String itemcsid,
			MultipartInput theUpdate) {
		if (logger.isDebugEnabled()) {
			logger.debug("updateAuthorityItem with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
		}
		if (parentcsid == null || "".equals(parentcsid)) {
			logger.error("updateVocabularyItem: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"update failed on AuthorityItem parentcsid=" + parentcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		if (itemcsid == null || "".equals(itemcsid)) {
			logger.error("updateVocabularyItem: missing itemcsid!");
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(
					"update failed on AuthorityItem=" + itemcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		}
		MultipartOutput result = null;
		try {
			// Note that we have to create the service context for the Items, not the main service
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
					theUpdate);
			DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
			getRepositoryClient(ctx).update(ctx, itemcsid, handler);
			result = (MultipartOutput) ctx.getOutput();
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
					"Update failed on AuthorityItem csid=" + itemcsid).type(
					"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		return result;
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
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
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
