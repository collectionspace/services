package org.collectionspace.services.client;

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
import org.collectionspace.services.jaxb.AbstractCommonList;

public interface CollectionSpacePoxProxy<LT extends AbstractCommonList> extends
		CollectionSpaceProxy<LT> {

	// (C)reate
	@POST
	ClientResponse<Response> create(byte[] payload);

	// (R)ead
	@GET
	@Path("/{csid}")
	ClientResponse<String> read(@PathParam("csid") String csid);

	// (R)ead
	@GET
	@Path("/{csid}")
	ClientResponse<String> readIncludeDeleted(
			@PathParam("csid") String csid,
			@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

	// (U)pdate
	@PUT
	@Path("/{csid}")
	ClientResponse<String> update(@PathParam("csid") String csid, byte[] payload);

	// (L)ist non-deleted items
	@GET
	@Produces({ "application/xml" })
	ClientResponse<LT> readIncludeDeleted(
			@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

	/**
	 * Keyword search.
	 * 
	 * @param keywords
	 *            keywords on which to search
	 * @param includeDeleted
	 * @return the client response
	 */
	@GET
	@Produces({ "application/xml" })
	ClientResponse<LT> keywordSearchIncludeDeleted(
			@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
			@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

	@GET
	@Produces({ "application/xml" })
	ClientResponse<LT> advancedSearchIncludeDeleted(
			@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_AS) String whereClause,
			@QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

}
