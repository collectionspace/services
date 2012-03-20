package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @version $Revision:$
 */
@Path(PlaceAuthorityClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface PlaceAuthorityProxy extends AuthorityProxy {
}
