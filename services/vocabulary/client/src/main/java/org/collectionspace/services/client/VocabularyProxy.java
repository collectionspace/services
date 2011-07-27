package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @version $Revision:$
 */
@Path("/" + VocabularyClient.SERVICE_PATH_COMPONENT + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface VocabularyProxy extends AuthorityProxy {
    
}
