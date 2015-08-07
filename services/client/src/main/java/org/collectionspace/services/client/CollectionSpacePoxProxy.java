package org.collectionspace.services.client;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
//import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;

public interface CollectionSpacePoxProxy<LT extends AbstractCommonList> extends
		CollectionSpaceProxy<LT> {

	// (C)reate
	@POST
	Response create(byte[] payload);

	// (R)ead
	@GET
	@Path("/{csid}")
	Response read(@PathParam("csid") String csid); // Returned entity type in response is String

	// (R)ead
	@GET
	@Path("/{csid}")
	Response readIncludeDeleted(
			@PathParam("csid") String csid,
			@QueryParam(WorkflowClient.WORKFLOWSTATE_QUERY) String workflowState);

	// (U)pdate
	@PUT
	@Path("/{csid}")
	Response update(@PathParam("csid") String csid, byte[] payload);

	// (L)ist non-deleted items
	@GET
	@Produces({ "application/xml" })
	Response readIncludeDeleted(
			@QueryParam(WorkflowClient.WORKFLOWSTATE_QUERY) String workflowState);

	/**
	 * Keyword search.
	 * 
	 * @param keywords
	 *            keywords on which to search
	 * @param workflowState
	 * @return the client response
	 */
	@GET
	@Produces({ "application/xml" })
	Response keywordSearchIncludeDeleted(
			@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
			@QueryParam(WorkflowClient.WORKFLOWSTATE_QUERY) String workflowState);

	@GET
	@Produces({ "application/xml" })
	Response advancedSearchIncludeDeleted(
			@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_AS) String whereClause,
			@QueryParam(WorkflowClient.WORKFLOWSTATE_QUERY) String workflowState);

}
