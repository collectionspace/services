package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.location.LocationauthoritiesCommonList;
import org.collectionspace.services.location.LocationsCommonList;

/**
 * @version $Revision:$
 */
@Path(LocationAuthorityClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface LocationAuthorityProxy extends AuthorityProxy<LocationsCommonList> {

    // List Locationauthorities
    @GET
    ClientResponse<LocationauthoritiesCommonList> readList();
    
    /*
     * List results that must be overridden for the RESTEasy proxy generation to work correctly.
     */
    
    // List Items matching a partial term or keywords.
    @Override
	@GET
    @Produces({"application/xml"})
    @Path("/{csid}/items/")
    ClientResponse<LocationsCommonList> readItemList(
    		@PathParam("csid") String vcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);
    
    // List Items for a named authority matching a partial term or keywords.
    @Override
	@GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({specifier})/items/")
    ClientResponse<LocationsCommonList> readItemListForNamedAuthority(
    		@PathParam("specifier") String specifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);
    
}
