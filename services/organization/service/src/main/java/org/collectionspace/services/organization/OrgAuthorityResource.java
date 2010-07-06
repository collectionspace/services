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
package org.collectionspace.services.organization;

import java.util.List;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
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
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.organization.nuxeo.OrganizationDocumentModelHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OrgAuthorityResource.
 */
@Path("/orgauthorities")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class OrgAuthorityResource extends
	AuthorityResource<OrgauthoritiesCommon, OrgauthoritiesCommonList, OrganizationsCommon,
	OrganizationDocumentModelHandler> {

    private final static String orgAuthorityServiceName = "orgauthorities";
	private final static String ORGAUTHORITIES_COMMON = "orgauthorities_common";
    
    private final static String organizationServiceName = "organizations";
	private final static String ORGANIZATIONS_COMMON = "organizations_common";
    
    final Logger logger = LoggerFactory.getLogger(OrgAuthorityResource.class);
    
    /** The contact resource. */
    private ContactResource contactResource = new ContactResource();

    /**
     * Instantiates a new org authority resource.
     */
    public OrgAuthorityResource() {
		super(OrgauthoritiesCommon.class, OrgAuthorityResource.class,
				ORGAUTHORITIES_COMMON, ORGANIZATIONS_COMMON);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return orgAuthorityServiceName;
    }
    
    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    public String getItemServiceName() {
        return organizationServiceName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<OrgauthoritiesCommon> getCommonPartClass() {
    	return OrgauthoritiesCommon.class;
    }    

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
     * Contact parts - this is a sub-resource of Organization (or "item")
     * @param parentcsid 
     * @param itemcsid 
     * @param input 
     * @return contact
     *************************************************************************/
    @POST
    @Path("{parentcsid}/items/{itemcsid}/contacts")
    public Response createContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartInput input) {
        try {
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(), input);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(OrgAuthorityResource.class);
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
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param ui the ui
     * 
     * @return the contact list
     */
    @GET
    @Produces({"application/xml"})
    @Path("{parentcsid}/items/{itemcsid}/contacts/")
    public ContactsCommonList getContactList(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @Context UriInfo ui) {
        ContactsCommonList contactObjectList = new ContactsCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(),
        			queryParams);
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
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * 
     * @return the contact
     */
    @GET
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public MultipartOutput getContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid) {
        MultipartOutput result = null;
       if (logger.isDebugEnabled()) {
            logger.debug("getContact with parentCsid=" + parentcsid +
            " itemcsid=" + itemcsid + " csid=" + csid);
        }
        try {
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName());
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
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public MultipartOutput updateContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
       if (logger.isDebugEnabled()) {
            logger.debug("updateContact with parentcsid=" + parentcsid +
            " itemcsid=" + itemcsid + " csid=" + csid);
        }
       if (parentcsid == null || parentcsid.trim().isEmpty()) {
            logger.error("updateContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || itemcsid.trim().isEmpty()) {
            logger.error("updateContact: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (csid == null || csid.trim().isEmpty()) {
            logger.error("updateContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(), theUpdate);
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
                    "Update failed on Contact csid=" + itemcsid).type(
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
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public Response deleteContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteContact with parentCsid=" + parentcsid +
            " itemcsid=" + itemcsid + " csid=" + csid);
        }
        if (parentcsid == null || parentcsid.trim().isEmpty()) {
            logger.error("deleteContact: missing parentcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete contact failed on parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || itemcsid.trim().isEmpty()) {
            logger.error("deleteContact: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete contact failed on itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (csid == null || csid.trim().isEmpty()) {
            logger.error("deleteContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete contact failed on csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            // Note that we have to create the service context for the
            // Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName());
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
