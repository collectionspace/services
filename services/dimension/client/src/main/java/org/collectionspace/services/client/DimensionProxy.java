package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.dimension.DimensionsCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/dimensions/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface DimensionProxy extends CollectionSpacePoxProxy<DimensionsCommonList> {
    @GET
    @Produces({"application/xml"})
    ClientResponse<DimensionsCommonList> readList();

	@Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<DimensionsCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);    

    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<DimensionsCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
}
