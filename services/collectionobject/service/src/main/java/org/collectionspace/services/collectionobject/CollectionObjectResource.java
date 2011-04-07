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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
//import org.collectionspace.services.common.imaging.nuxeo.NuxeoImageUtils; //FIXME: REM - Please remove all unneeded imports
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
//import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.intake.IntakeResource;
import org.collectionspace.services.intake.IntakesCommonList;
//import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationshipType;
import org.collectionspace.services.common.profile.Profiler;

import org.jboss.resteasy.util.HttpResponseCodes;

//FIXME: There should be no direct dependency on Nuxeo in our resource classes.
import org.nuxeo.ecm.core.api.DocumentModel;
//import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
//import org.nuxeo.ecm.core.api.ClientException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CollectionObjectResource.
 */
@Path(CollectionObjectClient.SERVICE_PATH_COMPONENT)
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionObjectResource
        extends AbstractMultiPartCollectionSpaceResourceImpl {
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    public String getVersionString() {
        /** The last change revision. */
        final String lastChangeRevision = "$LastChangedRevision$";
        return lastChangeRevision;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return CollectionObjectClient.SERVICE_PATH_COMPONENT;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<CollectionobjectsCommon> getCommonPartClass() {
    	return CollectionobjectsCommon.class;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(),
//                    CollectionobjectsCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((CollectionobjectsCommon) obj);
//            }
//        }
//        return docHandler;
//    }

    /**
     * Creates the collection object.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createCollectionObject(@Context HttpServletRequest req,
    		String xmlPayload) {
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(CollectionObjectResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createCollectionObject", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the collection object.
     * 
     * @param csid the csid
     * 
     * @return the collection object
     */
    @GET
    @Path("{csid}")
    public byte[] getCollectionObject(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getCollectionObject with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PoxPayloadOut result = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getCollectionObject", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested CollectionObject CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        
        return result.getBytes();
    }
    
    /**
     * Gets the collection object list.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the collection object list
     */
    @GET
    @Produces("application/xml")
    public CollectionobjectsCommonList getCollectionObjectList(@Context UriInfo ui,
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
    	Profiler profiler = new Profiler("getCollectionObjectList():", 1);
    	profiler.start();
    	CollectionobjectsCommonList result = null;
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	if (keywords != null) {
    		result = searchCollectionObjects(queryParams, keywords);
    	} else {
    		result = getCollectionObjectList(queryParams);
    	}
    	profiler.stop();
    	return result;
    }
    
    /**
     * Gets the collection object list.
     */
    private CollectionobjectsCommonList getCollectionObjectList(MultivaluedMap<String, String> queryParams) {
        CollectionobjectsCommonList collectionObjectList;
        Profiler profiler = new Profiler(this, 1);
        profiler.start();
        
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            collectionObjectList = (CollectionobjectsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getCollectionObjectList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        
        profiler.stop();
        return collectionObjectList;
    }

    /**
     * Update collection object.
     * 
     * @param csid the csid
     * @param xmlText an XML representation of the data to be used in the update
     * 
     * @return an XML representation of the updated object
     */
    @PUT
    @Path("{csid}")
    public String updateCollectionObject(
            @PathParam("csid") String csid,
            String xmlText) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateCollectionObject with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PoxPayloadOut result = null;
        try {
            PoxPayloadIn update = new PoxPayloadIn(xmlText);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(update);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Update failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in updateCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.toXML();
    }

    /**
     * Delete collection object.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteCollectionObject(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteCollectionObject with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteCollectionObject: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteCollectionObject", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on CollectionObject csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    // FIXME AR: should the method below be deprecated?

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
     * Gets the authority refs.
     * 
     * @param csid the csid
     * @param ui the ui
     * 
     * @return the authority refs
     */
    @GET
    @Path("{csid}/authorityrefs")
    @Produces("application/xml")
    public AuthorityRefList getAuthorityRefs(
    		@PathParam("csid") String csid, 
    		@Context UriInfo ui) {
    	AuthorityRefList authRefList = null;
        try {
        	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentWrapper<DocumentModel> docWrapper = 
            	getRepositoryClient(ctx).getDoc(ctx, csid);
            DocumentModelHandler<PoxPayloadIn, PoxPayloadOut> docHandler = 
            	(DocumentModelHandler<PoxPayloadIn, PoxPayloadOut>)createDocumentHandler(ctx);
            List<String> authRefFields = 
            	((MultipartServiceContextImpl)ctx).getCommonPartPropertyValues(
            			ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            authRefList = docHandler.getAuthorityRefs(docWrapper, authRefFields);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return authRefList;
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
    public CollectionobjectsCommonList keywordsSearchCollectionObjects(@Context UriInfo ui,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS) String keywords) {
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	return searchCollectionObjects(queryParams, keywords);
    }    
    
    /**
     * Search collection objects.
     * 
     * @param keywords the keywords
     * 
     * @return the collectionobjects common list
     */
    private CollectionobjectsCommonList searchCollectionObjects(
    		MultivaluedMap<String, String> queryParams,
    		String keywords) {
        CollectionobjectsCommonList collectionObjectList;
        try {
        	Profiler profiler = new Profiler("searchCollectionObjects():", 1);
        	profiler.start();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);

            // perform a keyword search
            if (keywords != null && !keywords.isEmpty()) {
                String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
                DocumentFilter documentFilter = handler.getDocumentFilter();
                documentFilter.setWhereClause(whereClause);
                if (logger.isDebugEnabled()) {
                    logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
                }
            }
            profiler.stop();
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            collectionObjectList = (CollectionobjectsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getCollectionObjectList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return collectionObjectList;
    }
        
}
