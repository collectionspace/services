package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.contact.ContactsCommonList;

/**
 * @version $Revision:$
 */
@Path(ContactClient.SERVICE_PATH_PROXY)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface ContactProxy extends CollectionSpacePoxProxy<ContactsCommonList> {
    @GET
    ClientResponse<ContactsCommonList> readList();

    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<ContactsCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);    
}
