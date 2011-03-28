package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * @version $Revision$
 */
@Path("/loansin/")
@Produces({"application/xml;charset=UTF-8"})
@Consumes({"application/xml"})
public interface LoaninProxy extends CollectionSpacePoxProxy {    
    // List
    @GET
    ClientResponse<AbstractCommonList> readList();
}
