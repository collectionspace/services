/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  $LastChangedRevision$
 */
package org.collectionspace.services.collectionobject;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationshipType;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;


@Path(CollectionObjectClient.SERVICE_PATH_COMPONENT)
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionObjectResource extends ResourceBase {
    
    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);

    @Override
    public String getVersionString() {
        final String lastChangeRevision = "$LastChangedRevision$";
        return lastChangeRevision;
    }

    @Override
    public String getServiceName() {
        return CollectionObjectClient.SERVICE_PATH_COMPONENT;
    }
    
    @Override
    public Class<CollectionobjectsCommon> getCommonPartClass() {
    	return CollectionobjectsCommon.class;
    }

    /**
     * Roundtrip.
     * 
     * This is an intentionally empty method used for getting a rough time estimate
     * of the overhead required for a client->server request/response cycle.
     * @param ms - milliseconds to delay
     * 
     * @return the response
     */
    @GET
    @Path("/{ms}/roundtrip")
    @Produces("application/xml")
    public Response roundtrip(
    		@PathParam("ms") String ms) {
    	Response result = null;
    	
    	Profiler profiler = new Profiler("roundtrip():", 1);
    	profiler.start();
		result = Response.status(HttpResponseCodes.SC_OK).build();
		profiler.stop();
		
		return result;
    }

    /**
     * This method is deprecated.  Use SearchCollectionObjects() method instead.
     * Keywords search collection objects.
     * @param ui 
     * 
     * @param keywords the keywords
     * 
     * @return the collectionobjects common list
     */
    /*
    @GET
    @Path("/search")
    @Produces("application/xml")
    @Deprecated
    public AbstractCommonList keywordsSearchCollectionObjects(@Context UriInfo ui,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS) String keywords) {
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	return search(queryParams, keywords);
    }  
     * 
     */

}
