package org.collectionspace.services.common.workflow.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.client.CollectionSpacePoxProxy;

/**
 * @version $Revision: 2108 $
 */
@Path(WorkflowClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface WorkflowProxy extends CollectionSpacePoxProxy {	
}
