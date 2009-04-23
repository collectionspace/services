package org.collectionspace.hello.services;

/**
 *  PingResource
 *	
 *  Service that returns a brief "ping"-type response.
 *  Allows clients to test basic connectivity.
 *
 *	@author $Author: aron $
 *	@version $Revision: 547 $
 *  @version $Date: 2009-01-30 12:48:42 -0800 (Fri, 30 Jan 2009) $
 *
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("")
@Produces("text/plain")
public class PingResource {

  // Paths relative to the root path, defined above.
  final static String ECHO_RELATIVE_PATH = "echo";
  final static String PING_RELATIVE_PATH = "ping";
  
  // Ping or echo message to return.
  final static String SERVICE_NAME = "CollectionSpace";
  final static String SERVICE_PING_MESSAGE =
    "The " + SERVICE_NAME + " system is alive and listening.\n";

  /////////////////////////////////////////////////////////////////
  /**
   * Class constructor (no argument).
   */
  public PingResource() {
    // do nothing
  }
    
  /////////////////////////////////////////////////////////////////
	/**
	 * Returns a brief "ping"-type message.
	 *
	 * @return  A brief "ping"-type message.
	 */
  @Path(PING_RELATIVE_PATH)
  @GET
  public String getPing() {
    return SERVICE_PING_MESSAGE;
  }

  /////////////////////////////////////////////////////////////////
	/**
	 * Returns a brief "ping"-type message.  Allows this message to be retrieved
	 * from an "echo" URI, as well as a "ping" URI.
	 *
	 * @return  A brief "ping"-type message.
	 */
  @Path(ECHO_RELATIVE_PATH)
  @GET
  public String getEcho() {
    return getPing();
  }
  s
}
