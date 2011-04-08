package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.loanout.LoansoutCommonList;
import org.collectionspace.services.client.workflow.WorkflowClient;


/**
 * @version $Revision$
 */
@Path("/loansout/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface LoanoutProxy extends CollectionSpacePoxProxy<LoansoutCommonList> {    
    // List
    @GET
    @Produces({"application/xml"})
    ClientResponse<LoansoutCommonList> readList();

    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<LoansoutCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);    
}
