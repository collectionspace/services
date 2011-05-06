package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.taxonomy.TaxonomyauthorityCommonList;
import org.collectionspace.services.taxonomy.TaxonomyCommonList;

/**
 * @version $Revision$
 * ILT = Item list type
 * LT = List type
 */
@Path(TaxonomyAuthorityClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface TaxonomyAuthorityProxy extends AuthorityProxy<TaxonomyauthorityCommonList, TaxonomyCommonList> {

    // List Taxonomyauthority
    @GET
    ClientResponse<TaxonomyauthorityCommonList> readList();
    
    /*
     * List results that must be overridden for the RESTEasy proxy generation to work correctly.
     */
    
    // List Items matching a partial term or keywords.
    @Override
	@GET
    @Produces({"application/xml"})
    @Path("/{csid}/items/")
    ClientResponse<TaxonomyCommonList> readItemList(
    		@PathParam("csid") String vcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    // List Items for a named authority matching a partial term or keywords.
    @Override
	@GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({specifier})/items/")
    ClientResponse<TaxonomyCommonList> readItemListForNamedAuthority(
    		@PathParam("specifier") String specifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<TaxonomyauthorityCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);    
 
    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<TaxonomyauthorityCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);   
}
