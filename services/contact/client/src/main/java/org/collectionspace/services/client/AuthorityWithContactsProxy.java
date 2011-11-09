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
    public ClientResponse<AbstractCommonList> readContactList(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid);

    @GET
    @Produces({"application/xml"})
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<AbstractCommonList> readContactListForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier);
    
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/")
    ClientResponse<AbstractCommonList> readContactListForItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid);
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<AbstractCommonList> readContactListForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier);

    //(C)reate Contact
    @POST
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    ClientResponse<Response> createContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            byte[] xmlPayload);
    @POST
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<Response> createContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            byte[] xmlPayload);
    @POST
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/")
    ClientResponse<Response> createContactForItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            byte[] xmlPayload);
    @POST
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<Response> createContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            byte[] xmlPayload);

     //(R)ead Contact
    @GET
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<String> readContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @GET
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<String> readContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
    @GET
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    ClientResponse<String> readContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @GET
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<String> readContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);

    //(U)pdate Contact
    @PUT
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<String> updateContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            byte[] xmlPayload);
    @PUT
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<String> updateContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid,
            byte[] xmlPayload);
    @PUT
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    ClientResponse<String> updateContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            byte[] xmlPayload);
    @PUT
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<String> updateContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid,
            byte[] xmlPayload);

    //(D)elete Contact
    @DELETE
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<Response> deleteContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    
    @DELETE
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<Response> deleteContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
    @DELETE
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    ClientResponse<Response> deleteContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    
    @DELETE
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<Response> deleteContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
}
