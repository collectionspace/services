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
package org.collectionspace.services.acquisition;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AcquisitionResource.
 */
@Path("/acquisitions")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class AcquisitionResource
        extends AbstractCollectionSpaceResourceImpl {

    /** The service name. */
    final private String serviceName = "acquisitions";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(AcquisitionResource.class);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision$";
    	return lastChangeRevision;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        DocumentHandler docHandler = ctx.getDocumentHandler();
        if (ctx.getInput() != null) {
            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), AcquisitionsCommon.class);
            if (obj != null) {
                docHandler.setCommonPart((AcquisitionsCommon) obj);
            }
        }
        return docHandler;
    }

    /**
     * Instantiates a new acquisition resource.
     */
    public AcquisitionResource() {
        // do nothing
    }

    /**
     * Creates the acquisition.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createAcquisition(MultipartInput input) {

        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(input, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(AcquisitionResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createAcquisition", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the acquisition.
     * 
     * @param csid the csid
     * 
     * @return the acquisition
     */
    @GET
    @Path("{csid}")
    public MultipartOutput getAcquisition(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getAcquisition with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAcquisition", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Acquisition CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the acquisition list.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the acquisition list
     */
    @GET
    @Produces("application/xml")
    public AcquisitionsCommonList getAcquisitionList(@Context UriInfo ui,
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
    	AcquisitionsCommonList result = null;
    	if (keywords != null) {
    		result = searchAcquisitions(keywords);
    	} else {
    		result = getAcquisitionsList();
    	}
    	
    	return result;
    }
    
    /**
     * Gets the acquisitions list.
     * 
     * @return the acquisitions list
     */
    private AcquisitionsCommonList getAcquisitionsList() {
        AcquisitionsCommonList acquisitionObjectList;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getAll(ctx, handler);
            acquisitionObjectList = (AcquisitionsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAcquisitionList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return acquisitionObjectList;
    }

    /**
     * Update acquisition.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public MultipartOutput updateAcquisition(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateAcquisition with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("updateAcquisition with input: ", theUpdate);
        }
        MultipartOutput result = null;
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(theUpdate, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updateAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Acquisition csid=" + csid).type(
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
     * Delete acquisition.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteAcquisition(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteAcquisition with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Acquisition csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
    
    /**
     * Keywords search acquisitions.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the acquisitions common list
     */
    @GET
    @Path("/search")    
    @Produces("application/xml")
    public AcquisitionsCommonList keywordsSearchAcquisitions(@Context UriInfo ui,
    		@QueryParam (IQueryManager.SEARCH_TYPE_KEYWORDS) String keywords) {
    	return searchAcquisitions(keywords);
    }
    
    /**
     * Search acquisitions.
     * 
     * @param keywords the keywords
     * 
     * @return the acquisitions common list
     */
    private AcquisitionsCommonList searchAcquisitions(String keywords) {
    	AcquisitionsCommonList acquisitionObjectList;    	
        try {
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentHandler handler = createDocumentHandler(ctx);

            // perform a keyword search
            if (keywords != null && !keywords.isEmpty()) {
            	String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
	            DocumentFilter documentFilter = handler.getDocumentFilter();
	            documentFilter.setWhereClause(whereClause);
	            if (logger.isDebugEnabled()) {
	            	logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
	            }
	            getRepositoryClient(ctx).getFiltered(ctx, handler);
            } else {
            	getRepositoryClient(ctx).getAll(ctx, handler);
            }            
            acquisitionObjectList = (AcquisitionsCommonList) handler.getCommonPartList();
            
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in search for Acquisitions", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return acquisitionObjectList;
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
            ServiceContext ctx = MultipartServiceContextFactory.get().createServiceContext(null, getServiceName());
            DocumentWrapper<DocumentModel> docWrapper = 
            	getRepositoryClient(ctx).getDoc(ctx, csid);
            RemoteDocumentModelHandlerImpl handler 
            	= (RemoteDocumentModelHandlerImpl)createDocumentHandler(ctx);
            List<String> authRefFields = ((MultipartServiceContextImpl)ctx).getCommonPartPropertyValues(RefNameServiceUtils.AUTH_REF_PROP);
            authRefList = handler.getAuthorityRefs(docWrapper, authRefFields);
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
    
    
}
