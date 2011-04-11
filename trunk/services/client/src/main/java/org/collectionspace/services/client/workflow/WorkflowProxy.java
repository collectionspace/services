package org.collectionspace.services.client.workflow;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.CollectionSpacePoxProxy;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision: 2108 $
 */
@Path(WorkflowClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface WorkflowProxy extends CollectionSpacePoxProxy<AbstractCommonList> {
}
