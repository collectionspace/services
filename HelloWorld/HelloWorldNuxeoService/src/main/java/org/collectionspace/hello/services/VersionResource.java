package org.collectionspace.hello.services;

/**
 *	<p>
 *  Service that returns the overall version of CollectionSpace services.
 *  Can also be called as a "ping"-type service to test basic connectivity.
 *  </p>
 *
 *	@author $Author: aron $
 *	@version $Revision: 547 $
 *  @version $Date: 2009-01-30 12:48:42 -0800 (Fri, 30 Jan 2009) $
 *
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/version")
@Produces("text/plain")
public class VersionResource {

  final static String CO_VERSION = "0.1";

  /////////////////////////////////////////////////////////////////
  /**
   * Class constructor (no argument).
   */
  public VersionResource() {
    // do nothing
  }
    
  /////////////////////////////////////////////////////////////////
	/**
	 * Returns the version of the CollectionSpace system.
	 *
	 * @return  The version of the CollectionSpace system.
	 */
  @GET
  public String getVersion() {
    return CO_VERSION;
  }

}
