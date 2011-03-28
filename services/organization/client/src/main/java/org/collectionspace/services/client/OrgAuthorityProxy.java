package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommonList;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision$
 */
@Path(OrgAuthorityClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface OrgAuthorityProxy extends AuthorityWithContactsProxy<OrganizationsCommonList> {
    // List OrgAuthorities
    @GET
    ClientResponse<OrgauthoritiesCommonList> readList();
	
    /*
     * List results that must be overridden for the RESTEasy proxy generation to work correctly.
     */
    
    // List Items matching a partial term or keywords.
    @Override
	@GET
    @Produces({"application/xml"})
    @Path("/{csid}/items/")
    ClientResponse<OrganizationsCommonList> readItemList(
    		@PathParam("csid") String vcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);
    
    // List Items for a named authority matching a partial term or keywords.
    @Override
	@GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({specifier})/items/")
    ClientResponse<OrganizationsCommonList> readItemListForNamedAuthority(
    		@PathParam("specifier") String specifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);
}
