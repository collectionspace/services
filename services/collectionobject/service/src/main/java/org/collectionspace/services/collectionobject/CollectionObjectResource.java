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
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.profile.Profiler;
import org.collectionspace.services.intake.IntakeResource;
import org.collectionspace.services.intake.IntakesCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.relation.RelationResource;
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
     * Gets the intakes common list.
     * 
     * @param ui the ui
     * @param csid the csid
     * 
     * @return the intakes common list
     */
    @GET
    @Path("{csid}/intakes")
    @Produces("application/xml")
    public IntakesCommonList getIntakesCommonList(@Context UriInfo ui,
    		@PathParam("csid") String csid) {
        IntakesCommonList result = null;  	
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        
        try {
        	//
        	// Find all the intake-related relation records.
        	//
        	String subjectCsid = csid;
        	String predicate = RelationshipType.COLLECTIONOBJECT_INTAKE.value();
        	String objectCsid = null;
        	RelationResource relationResource = new RelationResource();
        	RelationsCommonList relationsCommonList = relationResource.getRelationList(queryParams,
        			subjectCsid,
        			null, /*subjectType*/
        			predicate,
        			objectCsid,
        			null /*objectType*/);
        	
        	//
        	// Create an array of Intake csid's
        	//
        	List<RelationsCommonList.RelationListItem> relationsListItems = relationsCommonList.getRelationListItem();
        	List<String> intakeCsidList = new ArrayList<String>();
            for (RelationsCommonList.RelationListItem relationsListItem : relationsListItems) {
            	intakeCsidList.add(relationsListItem.getObjectCsid());
        	}
            
            //
            // Get a response list for the Intake records from the Intake resource
            //
        	IntakeResource intakeResource = new IntakeResource();
        	result = intakeResource.getIntakeList(intakeCsidList);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getIntakeList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        
        return result;
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
    @GET
    @Path("/search")
    @Produces("application/xml")
    @Deprecated
    public AbstractCommonList keywordsSearchCollectionObjects(@Context UriInfo ui,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS) String keywords) {
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	return search(queryParams, keywords);
    }    

}
