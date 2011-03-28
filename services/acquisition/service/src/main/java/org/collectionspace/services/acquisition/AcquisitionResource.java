/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Copyright © 2009 Regents of the University of California
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.AcquisitionClient;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;

import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.nuxeo.client.java.CommonList;

import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcquisitionResource.java
 *
 * Handles requests to the Acquisition service, orchestrates the retrieval
 * of relevant resources, and returns responses to the client.
 */
@Path(AcquisitionClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class AcquisitionResource
        extends AbstractMultiPartCollectionSpaceResourceImpl {

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
        return AcquisitionClient.SERVICE_NAME;
    }

    @Override
    public Class<AcquisitionsCommon> getCommonPartClass() {
        return AcquisitionsCommon.class;
    }

    /* (non-Javadoc) //FIXME: REM - Please remove dead code.
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), AcquisitionsCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((AcquisitionsCommon) obj);
//            }
//        }
//        return docHandler;
//    }
    /**
     * Instantiates a new acquisition resource.
     */
    public AcquisitionResource() {
        // Empty constructor.  Note that all JAX-RS resource classes are singletons.
    }

    /**
     * Creates the acquisition.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createAcquisition(String xmlPayload) {
        try {
        	PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(AcquisitionResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.CREATE_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createAcquisition", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.CREATE_FAILED + e.getMessage()).type("text/plain").build();
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
    public byte[] getAcquisition(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getAcquisition with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getAcquisition:" + ServiceMessages.MISSING_CSID);
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.MISSING_CSID).type("text/plain").build();
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
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.READ_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAcquisition", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.READ_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.getBytes();
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
    public CommonList getAcquisitionList(@Context UriInfo ui,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
        CommonList result = null;
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        if (keywords != null) {
            result = searchAcquisitions(queryParams, keywords);
        } else {
            result = getAcquisitionsList(queryParams);
        }

        return result;
    }

    /**
     * Gets the acquisitions list.
     * 
     * @return the acquisitions list
     */
    private CommonList getAcquisitionsList(MultivaluedMap<String, String> queryParams) {
        CommonList commonList;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            commonList = (CommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.LIST_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAcquisitionList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.LIST_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return commonList;
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
    public byte[] updateAcquisition(
            @PathParam("csid") String csid,
            String xmlPayload) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateAcquisition with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateAcquisition: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.UPDATE_FAILED + ServiceMessages.MISSING_CSID).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        PoxPayloadOut result = null;
        try {
        	PoxPayloadIn theUpdate = new PoxPayloadIn(xmlPayload);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.UPDATE_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.UPDATE_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.UPDATE_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.getBytes();
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
                    ServiceMessages.DELETE_FAILED + ServiceMessages.MISSING_CSID).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.DELETE_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteAcquisition", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.DELETE_FAILED + e.getMessage()).type("text/plain").build();
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
    @Deprecated
    public CommonList keywordsSearchAcquisitions(@Context UriInfo ui,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS) String keywords) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        return searchAcquisitions(queryParams, keywords);
    }

    /**
     * Search acquisitions.
     * 
     * @param keywords the keywords
     * 
     * @return the acquisitions common list
     */
    private CommonList searchAcquisitions(
            MultivaluedMap<String, String> queryParams, String keywords) {
        CommonList commonList;
        try {
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
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            commonList = (CommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.SEARCH_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in search for Acquisitions", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.SEARCH_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return commonList;
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
    public AuthorityRefList getAuthorityRefs(
            @PathParam("csid") String csid,
            @Context UriInfo ui) {
        AuthorityRefList authRefList = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentWrapper<DocumentModel> docWrapper =
                    getRepositoryClient(ctx).getDoc(ctx, csid);
            DocumentModelHandler<PoxPayloadIn, PoxPayloadOut> handler = (DocumentModelHandler<PoxPayloadIn, PoxPayloadOut>) createDocumentHandler(ctx);
            List<String> authRefFields =
                    ((MultipartServiceContextImpl) ctx).getCommonPartPropertyValues(
                    ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            authRefList = handler.getAuthorityRefs(docWrapper, authRefFields);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.AUTH_REFS_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.AUTH_REFS_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return authRefList;
    }
}
