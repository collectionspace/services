package org.collectionspace.services.client;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/*
 * ILT = Item list type
 * LT = List type
 */
public interface AuthorityWithContactsProxy extends AuthorityProxy {
    @GET
    @Produces({"application/xml"})
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    public Response readContactList(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid);

    @GET
    @Produces({"application/xml"})
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/")
    Response readContactListForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier);
    
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/")
    Response readContactListForItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid);
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/")
    Response readContactListForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier);

    //(C)reate Contact
    @POST
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    Response createContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            byte[] xmlPayload);
    @POST
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/")
    Response createContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            byte[] xmlPayload);
    @POST
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/")
    Response createContactForItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            byte[] xmlPayload);
    @POST
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/")
    Response createContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            byte[] xmlPayload);

     //(R)ead Contact
    @GET
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    Response readContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @GET
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    Response readContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
    @GET
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    Response readContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @GET
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    Response readContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);

    //(U)pdate Contact
    @PUT
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    Response updateContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            byte[] xmlPayload);
    @PUT
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    Response updateContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid,
            byte[] xmlPayload);
    @PUT
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    Response updateContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            byte[] xmlPayload);
    @PUT
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    Response updateContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid,
            byte[] xmlPayload);

    //(D)elete Contact
    @DELETE
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    Response deleteContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    
    @DELETE
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    Response deleteContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
    @DELETE
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    Response deleteContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    
    @DELETE
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    Response deleteContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
}
