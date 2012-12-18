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

import java.util.HashMap;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.client.*;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactConstants;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuthorityResourceWithContacts.
 */
@Consumes("application/xml")
@Produces("application/xml")
public abstract class AuthorityResourceWithContacts<AuthCommon, AuthItemHandler> extends //FIXME: REM - Why is this resource in this package instead of somewhere in 'common'?
        AuthorityResource<AuthCommon, AuthItemHandler> {

    private ContactResource contactResource = new ContactResource(); // Warning: ContactResource is a singleton.
    final Logger logger = LoggerFactory.getLogger(AuthorityResourceWithContacts.class);

    public AuthorityResourceWithContacts(
            Class<AuthCommon> authCommonClass, Class<?> resourceClass,
            String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
        super(authCommonClass, resourceClass,
                authorityCommonSchemaName, authorityItemCommonSchemaName);
    }

    public abstract String getItemServiceName();

    public String getContactServiceName() {
        return contactResource.getServiceName();
    }
    
    private DocumentHandler createContactDocumentHandler(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String inAuthority,
            String inItem) throws Exception {
        UriInfo ui = null;
        return createContactDocumentHandler(ctx, inAuthority, inItem, ui);
    }

    private DocumentHandler createContactDocumentHandler(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String inAuthority,
            String inItem, UriInfo ui) throws Exception {
        ContactDocumentModelHandler docHandler = (ContactDocumentModelHandler) createDocumentHandler(
                ctx,
                ctx.getCommonPartLabel(getContactServiceName()),
                ContactsCommon.class);
        docHandler.setInAuthority(inAuthority);
        docHandler.setInItem(inItem);
        docHandler.getServiceContext().setUriInfo(ui);
        return docHandler;
    }

    /*************************************************************************
     * Contact parts - this is a sub-resource of the AuthorityItem
     * @param parentspecifier either a CSID or one of the urn forms
     * @param itemspecifier either a CSID or one of the urn forms
     * @return contact
     *************************************************************************/
    @POST
    @Path("{parentcsid}/items/{itemcsid}/contacts")
    public Response createContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            String xmlPayload,
            @Context UriInfo ui) {
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
            String parentcsid = lookupParentCSID(parentspecifier, "createContact(authority)", "CREATE_ITEM_CONTACT", null);

            ServiceContext itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "createContact(item)", "CREATE_ITEM_CONTACT", itemCtx);

            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
            ServiceContext ctx = createServiceContext(getContactServiceName(), input);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid, ui);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(resourceClass);
            path.path("" + parentcsid + "/items/" + itemcsid + "/contacts/" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (Exception e) {
            throw bigReThrow(e,
                    "Create Contact failed; one of the requested specifiers for authority:"
                    + parentspecifier + ": and item:" + itemspecifier + ": was not found.",
                    itemspecifier);
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
    public AbstractCommonList getContactList(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @Context UriInfo uriInfo) {
        AbstractCommonList contactObjectList = new AbstractCommonList();

        try {
            ServiceContext ctx = createServiceContext(getContactServiceName(), uriInfo);
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
        	
            String parentcsid = lookupParentCSID(parentspecifier, "getContactList(parent)", "GET_CONTACT_LIST", null);
            ServiceContext itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "getContactList(item)", "GET_CONTACT_LIST", itemCtx);

            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid, uriInfo);
            DocumentFilter myFilter = handler.getDocumentFilter(); //new DocumentFilter();
            myFilter.appendWhereClause(ContactJAXBSchema.CONTACTS_COMMON + ":"
                    + ContactJAXBSchema.IN_AUTHORITY
                    + "='" + parentcsid + "'"
                    + IQueryManager.SEARCH_QUALIFIER_AND
                    + ContactJAXBSchema.CONTACTS_COMMON + ":"
                    + ContactJAXBSchema.IN_ITEM
                    + "='" + itemcsid + "'",
                    IQueryManager.SEARCH_QUALIFIER_AND);  // "AND" this clause to any existing
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            contactObjectList = (AbstractCommonList) handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e,
                    "Get ContactList failed; one of the requested specifiers for authority:"
                    + parentspecifier + ": and item:" + itemspecifier + ": was not found.",
                    itemspecifier);
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
    public String getContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @PathParam("csid") String csid) {
        PoxPayloadOut result = null;
        try {
            String parentcsid = lookupParentCSID(parentspecifier, "getContact(parent)", "GET_ITEM_CONTACT", null);

            ServiceContext itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "getContact(item)", "GET_ITEM_CONTACT", itemCtx);

            // Note that we have to create the service context and document handler for the Contact service, not the main service.
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getContactServiceName());
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, "Get failed, the requested Contact CSID:" + csid
                    + ": or one of the specifiers for authority:" + parentspecifier
                    + ": and item:" + itemspecifier + ": was not found.",
                    csid);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity("Get failed, the requested Contact CSID:" + csid + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.toXML();
    }

    /**
     * Update contact.
     * 
     * @param parentspecifier either a CSID or one of the urn forms
     * @param itemspecifier either a CSID or one of the urn forms
     * @param csid the csid
     *
     * @return the multipart output
     */
    @PUT
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public String updateContact(
            @PathParam("parentcsid") String parentspecifier,
            @PathParam("itemcsid") String itemspecifier,
            @PathParam("csid") String csid,
            String xmlPayload) {
        PoxPayloadOut result = null;
        try {
            PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            String parentcsid = lookupParentCSID(parentspecifier, "updateContact(authority)", "UPDATE_CONTACT", null);

            ServiceContext itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "updateContact(item)", "UPDATE_CONTACT", itemCtx);

            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
            // Note that we have to create the service context and document handler for the Contact service, not the main service.
            ctx = createServiceContext(getContactServiceName(), theUpdate);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, "Update failed, the requested Contact CSID:" + csid
                    + ": or one of the specifiers for authority:" + parentspecifier
                    + ": and item:" + itemspecifier + ": was not found.",
                    csid);
        }
        return result.toXML();
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
            String parentcsid = lookupParentCSID(parentspecifier, "deleteContact(authority)", "DELETE_CONTACT", null);

            ServiceContext itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemspecifier, parentcsid, "deleteContact(item)", "DELETE_CONTACT", itemCtx);
            //NOTE: itemcsid is not used below.  Leaving the above call in for possible side effects???       CSPACE-3175

            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
            // Note that we have to create the service context for the Contact service, not the main service.
            ctx = createServiceContext(getContactServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).delete(ctx, csid, handler);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, "DELETE failed, the requested Contact CSID:" + csid
                    + ": or one of the specifiers for authority:" + parentspecifier
                    + ": and item:" + itemspecifier + ": was not found.", csid);
        }
    }
    
    protected String getContactDocType() {
        return ContactConstants.NUXEO_DOCTYPE;
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
                uriRegistryEntriesMap.putAll(getUriRegistryEntries(tenantId, getContactDocType(), UriTemplateFactory.CONTACT));
        }
        return uriRegistryEntriesMap;
    }

}
