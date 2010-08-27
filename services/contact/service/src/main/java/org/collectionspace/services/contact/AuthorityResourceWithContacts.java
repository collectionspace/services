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
package org.collectionspace.services.contact;

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

import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource.Specifier;
import org.collectionspace.services.common.vocabulary.AuthorityResource.SpecifierForm;
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
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.remoting.samples.chat.exceptions.InvalidArgumentException;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuthorityResourceWithContacts.
 */
@Path("/vocabularies")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public abstract class AuthorityResourceWithContacts<AuthCommon, AuthCommonList, AuthItemCommonList, AuthItemHandler> extends 
     AuthorityResource<AuthCommon, AuthCommonList, AuthItemCommonList, AuthItemHandler> {

    /** The contact resource. */
    private ContactResource contactResource = new ContactResource();

    final Logger logger = LoggerFactory.getLogger(AuthorityResourceWithContacts.class);

    /**
	 * Instantiates a new Authority resource.
	 */
	public AuthorityResourceWithContacts(
			Class<AuthCommon> authCommonClass, Class<?> resourceClass,
			String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
		super(authCommonClass, resourceClass,
			authorityCommonSchemaName, authorityItemCommonSchemaName);
	}

	public abstract String getItemServiceName();

    /**
     * Gets the contact service name.
     * 
     * @return the contact service name
     */
    public String getContactServiceName() {
        return contactResource.getServiceName();
    }

    /**
     * Creates the contact document handler.
     * 
     * @param ctx the ctx
     * @param inAuthority the in authority
     * @param inItem the in item
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    private DocumentHandler createContactDocumentHandler(
    		ServiceContext<MultipartInput, MultipartOutput> ctx, String inAuthority,
            String inItem) throws Exception {
    	
    	ContactDocumentModelHandler docHandler = (ContactDocumentModelHandler)createDocumentHandler(
    			ctx,
    			ctx.getCommonPartLabel(getContactServiceName()),
    			ContactsCommon.class);        	
        docHandler.setInAuthority(inAuthority);
        docHandler.setInItem(inItem);
    	
        return docHandler;
    }

    /*************************************************************************
     * Contact parts - this is a sub-resource of the AuthorityItem
	 * @param parentspecifier either a CSID or one of the urn forms
	 * @param itemspecifier either a CSID or one of the urn forms
     * @param input 
     * @return contact
     *************************************************************************/
    @POST
    @Path("{parentcsid}/items/{itemcsid}/contacts")
    public Response createContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            MultipartInput input) {
        try {
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"createContact(parent)", "CREATE_ITEM_CONTACT");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"createContact(item)", "CREATE_ITEM_CONTACT");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<MultipartInput, MultipartOutput> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
	            ctx = createServiceContext(getItemServiceName());
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ctx = createServiceContext(getContactServiceName(), input);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(resourceClass);
            path.path("" + parentcsid + "/items/" + itemcsid + "/contacts/" + csid);
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
                logger.debug("Caught exception in createContact", e);
            }
            Response response = Response.status(
                Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Attempt to create Contact failed.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /**
     * Gets the contact list.
     * 
	 * @param parentspecifier either a CSID or one of the urn forms
	 * @param itemspecifier either a CSID or one of the urn forms
     * @param ui the ui
     * 
     * @return the contact list
     */
    @GET
    @Produces({"application/xml"})
    @Path("{parentcsid}/items/{itemcsid}/contacts/")
    public ContactsCommonList getContactList(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @Context UriInfo ui) {
        ContactsCommonList contactObjectList = new ContactsCommonList();
        try {
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"createContact(parent)", "CREATE_ITEM_CONTACT");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"createContact(item)", "CREATE_ITEM_CONTACT");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<MultipartInput, MultipartOutput> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
	            ctx = createServiceContext(getItemServiceName());
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ctx = createServiceContext(getContactServiceName(), queryParams);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            DocumentFilter myFilter = handler.getDocumentFilter(); //new DocumentFilter();
            myFilter.setWhereClause(ContactJAXBSchema.CONTACTS_COMMON + ":" +
                ContactJAXBSchema.IN_AUTHORITY +
                "='" + parentcsid + "'" +
                IQueryManager.SEARCH_QUALIFIER_AND +
                ContactJAXBSchema.CONTACTS_COMMON + ":" +
                ContactJAXBSchema.IN_ITEM +
                "='" + itemcsid + "'" );
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            contactObjectList = (ContactsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getContactsList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return contactObjectList;
    }

    /**
     * Gets the contact.
     * 
	 * @param parentspecifier either a CSID or one of the urn forms
	 * @param itemspecifier either a CSID or one of the urn forms
     * @param csid the csid
     * 
     * @return the contact
     */
    @GET
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public MultipartOutput getContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @PathParam("csid") String csid) {
        MultipartOutput result = null;
        try {
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"getContact(parent)", "GET_ITEM_CONTACT");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"getContact(item)", "GET_ITEM_CONTACT");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<MultipartInput, MultipartOutput> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
	            ctx = createServiceContext(getItemServiceName());
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ctx = createServiceContext(getContactServiceName());
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND)
                .entity("Get failed, the requested Contact CSID:" + csid + ": was not found.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getContact", e);
            }
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Get contact failed")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                .entity("Get failed, the requested Contact CSID:" + csid + ": was not found.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;

    }

    /**
     * Update contact.
     * 
	 * @param parentspecifier either a CSID or one of the urn forms
	 * @param itemspecifier either a CSID or one of the urn forms
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public MultipartOutput updateContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        MultipartOutput result = null;
        try {
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"updateContact(parent)", "UPDATE_ITEM_CONTACT");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"updateContact(item)", "UPDATE_ITEM_CONTACT");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<MultipartInput, MultipartOutput> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
	            ctx = createServiceContext(getItemServiceName());
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ctx = createServiceContext(getContactServiceName(), theUpdate);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).update(ctx, csid, handler);
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
                logger.debug("caught exception in updateContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Contact csid=" + itemspecifier).type(
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
     * Delete contact.
     * 
	 * @param parentspecifier either a CSID or one of the urn forms
	 * @param itemspecifier either a CSID or one of the urn forms
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public Response deleteContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @PathParam("csid") String csid) {
        try {
   			Specifier parentSpec = getSpecifier(parentspecifier, 
   					"updateContact(parent)", "UPDATE_ITEM_CONTACT");
			Specifier itemSpec = getSpecifier(itemspecifier, 
					"updateContact(item)", "UPDATE_ITEM_CONTACT");
			// Note that we have to create the service context for the Items, not the main service
            ServiceContext<MultipartInput, MultipartOutput> ctx = null;
			String parentcsid;
			if(parentSpec.form==SpecifierForm.CSID) {
				parentcsid = parentSpec.value;
			} else {
				String whereClause = buildWhereForAuthByName(parentSpec.value);
	            ctx = createServiceContext(getServiceName());
				parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
			}
            String itemcsid;
			if(itemSpec.form==SpecifierForm.CSID) {
				itemcsid = itemSpec.value;
			} else {
				String itemWhereClause = 
					buildWhereForAuthItemByName(itemSpec.value, parentcsid);
	            ctx = createServiceContext(getItemServiceName());
				itemcsid = getRepositoryClient(ctx).findDocCSID(ctx, itemWhereClause);
			}
            // Note that we have to create the service context for the
            // Contact service, not the main service.
        	ctx = createServiceContext(getContactServiceName());
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
         } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
         } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in deleteContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND)
                .entity("Delete failed, the requested Contact CSID:" + csid + ": was not found.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
       } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
    
}
