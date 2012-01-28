package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * ILT = Item list type
 * LT = List type
 */
public interface AuthorityProxy extends CollectionSpaceCommonListPoxProxy {
	
	/*
	 * Basic CRUD operations
	 */
	
    //(C)reate Item
    @POST
    @Path("/{vcsid}/items/")
    ClientResponse<Response> createItem(@PathParam("vcsid") String vcsid, byte[] xmlPayload);

    //(R)ead Item
    @GET
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<String> readItem(@PathParam("vcsid") String vcsid,
    		@PathParam("csid") String csid,
    		@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    //(U)pdate Item
    @PUT
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<String> updateItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid, byte[] xmlPayload);

    //(D)elete Item
    @DELETE
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<Response> deleteItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid);
    
    /**
     * Get a list of objects that reference a given authority term.
     * 
     * @param parentcsid 
     * @param itemcsid 
     * @param csid
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#getAuthorityRefs(java.lang.String)
     */
    @GET
    @Path("{csid}/items/{itemcsid}/refObjs")
    @Produces("application/xml")
    ClientResponse<AuthorityRefDocList> getReferencingObjects(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    // List Item Authority References
    @GET
    @Produces({"application/xml"})
    @Path("/{parentcsid}/items/{itemcsid}/authorityrefs/")
    public ClientResponse<AuthorityRefList> getItemAuthorityRefs(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid);
    
    /*
     * 
     */
    
    //(R)ead by name
    @GET
    @Path("/urn:cspace:name({name})")
    ClientResponse<String> readByName(@PathParam("name") String name,
    		@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    /*
     * Item subresource methods
     */
    
    //(R)ead Named Item
    @GET
    @Path("/{vcsid}/items/urn:cspace:name({specifier})")
    ClientResponse<String> readNamedItem(@PathParam("vcsid") String vcsid,
    		@PathParam("specifier") String specifier,
    		@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    //(R)ead Item In Named Authority
    @GET
    @Path("/urn:cspace:name({specifier})/items/{csid}")
    ClientResponse<String> readItemInNamedAuthority(@PathParam("specifier") String specifier,
    		@PathParam("csid") String csid,
    		@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    //(R)ead Named Item In Named Authority
    @GET
    @Path("/urn:cspace:name({specifier})/items/urn:cspace:name({itemspecifier})")
    ClientResponse<String> readNamedItemInNamedAuthority(@PathParam("specifier") String specifier, 
    		@PathParam("itemspecifier") String itemspecifier,
    		@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    /*
     * Item subresource List methods
     */

    // List Items matching a partial term or keywords.
    @GET
    @Produces({"application/xml"})
    @Path("/{csid}/items/")
    ClientResponse<AbstractCommonList> readItemList(
    		@PathParam("csid") String vcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
    
    // List Items for a named authority matching a partial term or keywords.
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({specifier})/items/")
    ClientResponse<AbstractCommonList> readItemListForNamedAuthority(
    		@PathParam("specifier") String specifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    /*
     * Workflow related methods
     * 
     */
    
    //(R)ead Item workflow
    @GET
    @Produces({"application/xml"})
    @Consumes({"application/xml"})    
    @Path("/{vcsid}/items/{csid}" + WorkflowClient.SERVICE_PATH)
    ClientResponse<String> readItemWorkflow(@PathParam("vcsid") String vcsid,
    		@PathParam("csid") String csid);
            
    //(U)pdate Item workflow
    @PUT
    @Path("/{vcsid}/items/{csid}" + WorkflowClient.SERVICE_PATH)
    ClientResponse<String> updateItemWorkflow(@PathParam("vcsid") String vcsid,
    		@PathParam("csid") String csid,
    		byte[] xmlPayload);
    
}
