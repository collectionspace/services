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
 */
package org.collectionspace.services.location;

import java.util.List;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.LocationAuthorityJAXBSchema;
import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameUtils;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.location.nuxeo.LocationDocumentModelHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LocationAuthorityResource.
 */
@Path("/locationauthorities")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class LocationAuthorityResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant locationAuthorityServiceName. */
    private final static String locationAuthorityServiceName = "locationauthorities";
    
    /** The Constant locationServiceName. */
    private final static String locationServiceName = "locations";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(LocationAuthorityResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();
    
    /**
     * Instantiates a new location authority resource.
     */
    public LocationAuthorityResource() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision: 1850 $";
    	return lastChangeRevision;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return locationAuthorityServiceName;
    }

    @Override
    public Class<LocationauthoritiesCommon> getCommonPartClass() {
    	return LocationauthoritiesCommon.class;
    }
    
    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    public String getItemServiceName() {
        return locationServiceName;
    }

    /**
 * Creates the item document handler.
 * 
 * @param ctx the ctx
 * @param inAuthority the in authority
 * 
 * @return the document handler
 * 
 * @throws Exception the exception
 */
    private DocumentHandler createItemDocumentHandler(ServiceContext<MultipartInput, MultipartOutput> ctx,
            String inAuthority) throws Exception {    
        LocationDocumentModelHandler docHandler = (LocationDocumentModelHandler)createDocumentHandler(ctx,
    			ctx.getCommonPartLabel(getItemServiceName()),
    			LocationsCommon.class);        	
        docHandler.setInAuthority(inAuthority);
        
        return docHandler;
    }

    /**
     * Creates the location authority.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createLocationAuthority(MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //locationAuthorityObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(LocationAuthorityResource.class);
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
                logger.debug("Caught exception in createLocationAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the location authority by name.
     * 
     * @param specifier the specifier
     * 
     * @return the location authority by name
     */
    @GET
    @Path("urn:cspace:name({specifier})")
    public MultipartOutput getLocationAuthorityByName(@PathParam("specifier") String specifier) {
        if (specifier == null) {
            logger.error("getLocationAuthority: missing name!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on LocationAuthority (missing specifier)").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        String whereClause =
        	LocationAuthorityJAXBSchema.LOCATIONAUTHORITIES_COMMON+
        	":"+LocationAuthorityJAXBSchema.DISPLAY_NAME+
        	"='"+specifier+"'";
        // We only get a single doc - if there are multiple,
        // it is an error in use.

        if (logger.isDebugEnabled()) {
            logger.debug("getLocationAuthority with name=" + specifier);
        } 
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentFilter myFilter = new DocumentFilter(whereClause, 0, 1);
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).get(ctx, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLocationAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on LocationAuthority spec=" + specifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLocationAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested LocationAuthority spec:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the entities referencing this Location instance. The service type
     * can be passed as a query param "type", and must match a configured type
     * for the service bindings. If not set, the type defaults to
     * ServiceBindingUtils.SERVICE_TYPE_PROCEDURE.
     * 
     * @param csid the parent csid
     * @param itemcsid the location csid
     * @param ui the ui
     * 
     * @return the info for the referencing objects
     */
    @GET
    @Path("{csid}/items/{itemcsid}/refObjs")
    @Produces("application/xml")
    public AuthorityRefDocList getReferencingObjects(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
    		@Context UriInfo ui) {
    	AuthorityRefDocList authRefDocList = null;
        if (logger.isDebugEnabled()) {
            logger.debug("getReferencingObjects with parentcsid=" 
            		+ parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)
                || itemcsid == null || "".equals(itemcsid)) {
            logger.error("getLocation: missing parentcsid or itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Location with parentcsid=" 
            		+ parentcsid + " and itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
    try {
        // Note that we have to create the service context for the Items, not the main service
        ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(getItemServiceName());
        DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
        RepositoryClient repoClient = getRepositoryClient(ctx); 
        DocumentFilter myFilter = handler.createDocumentFilter();
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        myFilter.setPagination(queryParams);
    	String serviceType = ServiceBindingUtils.SERVICE_TYPE_PROCEDURE;
        List<String> list = queryParams.remove(ServiceBindingUtils.SERVICE_TYPE_PROP);
        if (list != null) {
        	serviceType = list.get(0);
        }
        DocumentWrapper<DocumentModel> docWrapper = repoClient.getDoc(ctx, itemcsid);
        DocumentModel docModel = docWrapper.getWrappedObject();
        String refName = (String)docModel.getPropertyValue(LocationJAXBSchema.REF_NAME);

        authRefDocList = RefNameServiceUtils.getAuthorityRefDocs(ctx, repoClient, 
        		serviceType, refName,
        		myFilter.getPageSize(), myFilter.getStartPage(), true );
    } catch (UnauthorizedException ue) {
        Response response = Response.status(
                Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
        throw new WebApplicationException(response);
    } catch (DocumentNotFoundException dnfe) {
        if (logger.isDebugEnabled()) {
            logger.debug("getReferencingObjects", dnfe);
        }
        Response response = Response.status(Response.Status.NOT_FOUND).entity(
                "GetReferencingObjects failed with parentcsid=" 
            		+ parentcsid + " and itemcsid=" + itemcsid).type(
                "text/plain").build();
        throw new WebApplicationException(response);
    } catch (Exception e) {	// Includes DocumentException
        if (logger.isDebugEnabled()) {
            logger.debug("GetReferencingObjects", e);
        }
        Response response = Response.status(
                Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
        throw new WebApplicationException(response);
    }
    if (authRefDocList == null) {
        Response response = Response.status(Response.Status.NOT_FOUND).entity(
                "Get failed, the requested Location CSID:" + itemcsid + ": was not found.").type(
                "text/plain").build();
        throw new WebApplicationException(response);
    }
    return authRefDocList;
    }

    @GET
    @Path("{csid}")
    public MultipartOutput getLocationAuthority(@PathParam("csid") String csid) {
        if (csid == null) {
            logger.error("getLocationAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on LocationAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getLocationAuthority with path(id)=" + csid);
        } 
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLocationAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on LocationAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLocationAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested LocationAuthority CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the location authority list.
     * 
     * @param ui the ui
     * 
     * @return the location authority list
     */
    @GET
    @Produces("application/xml")
    public LocationauthoritiesCommonList getLocationAuthorityList(@Context UriInfo ui) {
        LocationauthoritiesCommonList locationAuthorityObjectList = new LocationauthoritiesCommonList();
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentFilter myFilter = handler.createDocumentFilter(); //new DocumentFilter();
            myFilter.setPagination(queryParams); //FIXME
            String nameQ = queryParams.getFirst("refName");
            if (nameQ != null) {
                myFilter.setWhereClause("locationauthorities_common:refName='" + nameQ + "'");
            }
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            locationAuthorityObjectList = (LocationauthoritiesCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLocationAuthorityList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return locationAuthorityObjectList;
    }

    /**
     * Update location authority.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public MultipartOutput updateLocationAuthority(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateLocationAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateLocationAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on LocationAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updateLocationAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on LocationAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete location authority.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteLocationAuthority(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteLocationAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteLocationAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on LocationAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteLocationAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on LocationAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /*************************************************************************
     * Location parts - this is a sub-resource of LocationAuthority
     *************************************************************************/
    @POST
    @Path("{csid}/items")
    public Response createLocation(@PathParam("csid") String parentcsid, MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(), input);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            String itemcsid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(LocationAuthorityResource.class);
            path.path(parentcsid + "/items/" + itemcsid);
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
                logger.debug("Caught exception in createLocation", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the location.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * 
     * @return the location
     */
    @GET
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput getLocation(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getLocation with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)
            || itemcsid == null || "".equals(itemcsid)) {
            logger.error("getLocation: missing parentcsid or itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Location with parentcsid=" 
            		+ parentcsid + " and itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).get(ctx, itemcsid, handler);
            // TODO should we assert that the item is in the passed locationAuthority?
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLocation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Location csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLocation", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Location CSID:" + itemcsid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the location list.
     * 
     * @param parentcsid the parentcsid
     * @param partialTerm the partial term
     * @param ui the ui
     * 
     * @return the location list
     */
    @GET
    @Path("{csid}/items")
    @Produces("application/xml")
    public LocationsCommonList getLocationList(
            @PathParam("csid") String parentcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,            
            @Context UriInfo ui) {
        LocationsCommonList locationObjectList = new LocationsCommonList();
        try {
            // Note that docType defaults to the ServiceName, so we're fine with that.
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
        			queryParams);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            DocumentFilter myFilter = handler.getDocumentFilter();

            // Add the where clause "locations_common:inAuthority='" + parentcsid + "'"
            myFilter.setWhereClause(LocationJAXBSchema.LOCATIONS_COMMON + ":" +
            		LocationJAXBSchema.IN_AUTHORITY + "='" + parentcsid + "'");
            
            // AND locations_common:displayName LIKE '%partialTerm%'
            if (partialTerm != null && !partialTerm.isEmpty()) {
            	String ptClause = 
            	LocationJAXBSchema.LOCATIONS_COMMON + ":" +
            		LocationJAXBSchema.DISPLAY_NAME +
            		" LIKE " +
            		"'%" + partialTerm + "%'";
            	myFilter.appendWhereClause(ptClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            locationObjectList = (LocationsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLocationList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return locationObjectList;
    }
    

    /**
     * Gets the location list by auth name.
     * 
     * @param parentSpecifier the parent specifier
     * @param partialTerm the partial term
     * @param ui the ui
     * 
     * @return the location list by auth name
     */
    @GET
    @Path("urn:cspace:name({specifier})/items")
    @Produces("application/xml")
    public LocationsCommonList getLocationListByAuthName(
    		@PathParam("specifier") String parentSpecifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,            
            @Context UriInfo ui) {
        LocationsCommonList locationObjectList = new LocationsCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            String whereClause =
            	LocationAuthorityJAXBSchema.LOCATIONAUTHORITIES_COMMON+
            	":"+LocationAuthorityJAXBSchema.DISPLAY_NAME+
            	"='"+parentSpecifier+"'";
            // Need to get an Authority by name
            ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            String parentcsid = 
            	getRepositoryClient(ctx).findDocCSID(ctx, whereClause);

            ctx = createServiceContext(getItemServiceName(), queryParams);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            DocumentFilter myFilter = handler.createDocumentFilter(); //new DocumentFilter();
            myFilter.setPagination(queryParams); //FIXME

            // Add the where clause "locations_common:inAuthority='" + parentcsid + "'"
            myFilter.setWhereClause(LocationJAXBSchema.LOCATIONS_COMMON + ":" +
            		LocationJAXBSchema.IN_AUTHORITY + "='" + parentcsid + "'");
            
            // AND locations_common:displayName LIKE '%partialTerm%'
            if (partialTerm != null && !partialTerm.isEmpty()) {
            	String ptClause = // "AND " +
            	LocationJAXBSchema.LOCATIONS_COMMON + ":" +
            		LocationJAXBSchema.DISPLAY_NAME +
            		" LIKE " +
            		"'%" + partialTerm + "%'";
            	myFilter.appendWhereClause(ptClause, "AND");
            }
            
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            locationObjectList = (LocationsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLocationList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return locationObjectList;
    }

    /**
     * Update location.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput updateLocation(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateLocation with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("updateLocation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Location parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("updateLocation: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Location=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
        			theUpdate);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).update(ctx, itemcsid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateLocation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Location csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete location.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}/items/{itemcsid}")
    public Response deleteLocation(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteLocation with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("deleteLocation: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Location parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("deleteLocation: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Location=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
            getRepositoryClient(ctx).delete(ctx, itemcsid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteLocation", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Location itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

}
