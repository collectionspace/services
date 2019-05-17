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

import org.collectionspace.services.client.*;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.context.JaxRsContext;
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactConstants;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

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
            String itemcsid = lookupItemCSID(itemCtx, itemspecifier, parentcsid, "createContact(item)", "CREATE_ITEM_CONTACT");

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
    
    public String createContact(ServiceContext existingCtx, String parentCsid, String itemCsid, PoxPayloadIn input,
            UriInfo ui) throws Exception {
        ServiceContext ctx = createServiceContext(getContactServiceName(), input);
        if (existingCtx != null) {
            Object repoSession = existingCtx.getCurrentRepositorySession();
            if (repoSession != null) {
                ctx.setCurrentRepositorySession(repoSession);
                ctx.setProperties(existingCtx.getProperties());
            }
        }
        
        DocumentHandler handler = createContactDocumentHandler(ctx, parentCsid, itemCsid, ui);
        String csid = getRepositoryClient(ctx).create(ctx, handler);

        return csid;
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

        contactObjectList = getContactList(null, parentspecifier, itemspecifier, uriInfo);
        
        return contactObjectList;
    }
    
    public AbstractCommonList getContactList(
            ServiceContext existingCtx,
            String parentspecifier,
            String itemspecifier,
            UriInfo uriInfo) {
        AbstractCommonList contactObjectList = new AbstractCommonList();

        try {
            ServiceContext ctx = createServiceContext(getContactServiceName(), uriInfo);
            if (existingCtx != null) {
                Object repoSession = existingCtx.getCurrentRepositorySession();
                if (repoSession != null) {
                    ctx.setCurrentRepositorySession(repoSession);
                    ctx.setProperties(existingCtx.getProperties());
                }
            }
            
            String parentcsid = lookupParentCSID(parentspecifier, "getContactList(parent)", "GET_CONTACT_LIST", null);
            ServiceContext itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemCtx, itemspecifier, parentcsid, "getContactList(item)", "GET_CONTACT_LIST");

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
            
            //
            // Include the Contact subresource(s) as part of the payload.  The current UI supports a single contact resource only, so
            // this code will return only the first contact resource
            //
            
            //FIXME: Need to support paging
            AbstractCommonList contactObjectList = getContactList(ctx, parentIdentifier, itemIdentifier, uriInfo);
            if (contactObjectList.getTotalItems() > 1) {
            		String errMsg = String.format("Can't get complete list of contacts for authority term '%s' in authority '%s'.", parentIdentifier, itemIdentifier);
            		logger.warn(errMsg);
            }
            
            if (contactObjectList.getTotalItems() > 0) {
            		ListItem item = contactObjectList.getListItem().get(0);
            		String csid = this.getCsid(item);
            		PoxPayloadOut contactPayloadOut = getContactPayload(parentIdentifier, itemIdentifier, csid);
            		PayloadOutputPart contactCommonPart = contactPayloadOut.getPart(ContactClient.SERVICE_COMMON_PART_NAME);
            		result.addPart(contactCommonPart);
            }
            
        } catch (DocumentNotFoundException dnf) {
            throw bigReThrow(dnf, ServiceMessages.resourceNotFoundMsg(itemIdentifier));
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED);
        }
                
        return result.getBytes();
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
            result = getContactPayload(parentspecifier, itemspecifier, csid);
        } catch (Exception e) {
            throw bigReThrow(e, "Get failed, the requested Contact CSID:" + csid
                    + ": or one of the specifiers for authority:" + parentspecifier
                    + ": and item:" + itemspecifier + ": was not found.",
                    csid);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity("Get failed, the requested Contact CSID:" + csid + ": was not found.").type("text/plain").build();
            throw new CSWebApplicationException(response);
        }
        return result.toXML();
    }
    
    protected PoxPayloadOut getContactPayload(
            String parentspecifier,
            String itemspecifier,
            String csid) throws Exception {
        PoxPayloadOut result = null;

        String parentcsid = lookupParentCSID(parentspecifier, "getContact(parent)", "GET_ITEM_CONTACT", null);

        ServiceContext<PoxPayloadIn, PoxPayloadOut> itemCtx = createServiceContext(getItemServiceName());
        String itemcsid = lookupItemCSID(itemCtx, itemspecifier, parentcsid, "getContact(item)", "GET_ITEM_CONTACT");

        // Note that we have to create the service context and document handler for the Contact service, not the main service.
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getContactServiceName());
        DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
        getRepositoryClient(ctx).get(ctx, csid, handler);
        result = (PoxPayloadOut) ctx.getOutput();

        return result;
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

            ServiceContext<PoxPayloadIn, PoxPayloadOut> itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemCtx, itemspecifier, parentcsid, "updateContact(item)", "UPDATE_CONTACT");

            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = null;
            // Note that we have to create the service context and document handler for the Contact service, not the main service.
            ctx = createServiceContext(getContactServiceName(), theUpdate);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (PoxPayloadOut) ctx.getOutput();
        } catch (Exception e) {
            throw bigReThrow(e, "Update failed, the requested Contact CSID:" + csid
                    + ": or one of the specifiers for authority:" + parentspecifier
                    + ": and item:" + itemspecifier + ": was not found.",
                    csid);
        }
        
        return result.toXML();
    }
    
    public void updateContact(ServiceContext existingCtx, String parentCsid, String itemCsid, String csid,
            PayloadInputPart theUpdate) throws Exception {
        PoxPayloadOut result = null;
        
        String payloadTemplate = "<?xml version='1.0' encoding='UTF-8'?><document>%s</document>";
        String xmlPayload = String.format(payloadTemplate, theUpdate.asXML());
        PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
        
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(getContactServiceName(), input);
        if (existingCtx != null) {
            Object repoSession = existingCtx.getCurrentRepositorySession();
            if (repoSession != null) {
                ctx.setCurrentRepositorySession(repoSession);
                ctx.setProperties(existingCtx.getProperties());
            }
        }
        
        DocumentHandler handler = createContactDocumentHandler(ctx, parentCsid, itemCsid);
        getRepositoryClient(ctx).update(ctx, csid, handler);
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

            ServiceContext<PoxPayloadIn, PoxPayloadOut> itemCtx = createServiceContext(getItemServiceName());
            String itemcsid = lookupItemCSID(itemCtx, itemspecifier, parentcsid, "deleteContact(item)", "DELETE_CONTACT");
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
