package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.objectexit.ObjectexitCommonList;


/**
 * @version $Revision: 2108 $
 */
@Path(ObjectExitClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface ObjectExitProxy extends CollectionSpacePoxProxy<ObjectexitCommonList> {
    // List
    @GET
    ClientResponse<ObjectexitCommonList> readList();
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<ObjectexitCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<ObjectexitCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);
}
