package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.batch.BatchCommonList;


/**
 * @version $Revision: 2108 $
 */
@Path(BatchClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface BatchProxy extends CollectionSpacePoxProxy<BatchCommonList> {
    // List
    @GET
    ClientResponse<BatchCommonList> readList();
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<BatchCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<BatchCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
}
