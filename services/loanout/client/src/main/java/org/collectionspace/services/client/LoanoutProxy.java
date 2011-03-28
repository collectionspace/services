package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.loanout.LoansoutCommonList;

/**
 * @version $Revision$
 */
@Path("/loansout/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface LoanoutProxy extends CollectionSpacePoxProxy {    
    // List
    @GET
    @Produces({"application/xml"})
    ClientResponse<LoansoutCommonList> readList();
}
