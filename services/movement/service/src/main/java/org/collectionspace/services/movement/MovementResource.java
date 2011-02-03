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
package org.collectionspace.services.movement;

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

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.movement.MovementsCommon;
import org.collectionspace.services.movement.MovementsCommonList;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MovementResource.java
 *
 * Handles requests to the Movement service, orchestrates the retrieval
 * of relevant resources, and returns responses to the client.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
@Path("/movements")
@Consumes("application/xml")
@Produces("application/xml;charset=UTF-8")
public class MovementResource extends AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant serviceName. */
    private final static String serviceName = "movements";
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(MovementResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    /**
     * Instantiates a new movement resource.
     */
    public MovementResource() {
        // do nothing
    }

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
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<MovementsCommon> getCommonPartClass() {
        return MovementsCommon.class;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), MovementsCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((MovementsCommon) obj);
//            }
//        }
//        return docHandler;
//    }
    /**
     * Creates the movement.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createMovement(String xmlText) {
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlText);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //movementObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(MovementResource.class);
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
                logger.debug("Caught exception in createMovement", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.CREATE_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the movement.
     * 
     * @param csid the csid
     * 
     * @return the movement
     */
    @GET
    @Path("{csid}")
    public String getMovement(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getMovement with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getMovement:" + ServiceMessages.MISSING_CSID);
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
                logger.debug("getMovement", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getMovement", e);
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
        return result.toXML();
    }

    /**
     * Gets the movement list.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the movement list
     */
    @GET
    @Produces("application/xml")
    public MovementsCommonList getMovementList(@Context UriInfo ui,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
        MovementsCommonList result = null;
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        if (keywords != null) {
            result = searchMovements(queryParams, keywords);
        } else {
            result = getMovementList(queryParams);
        }

        return result;
    }

    /**
     * Gets the movement list.
     * 
     * @return the movement list
     */
    private MovementsCommonList getMovementList(MultivaluedMap<String, String> queryParams) {
        MovementsCommonList movementObjectList;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            movementObjectList = (MovementsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.LIST_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getMovementList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.LIST_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return movementObjectList;
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

    /**
     * Gets the movement list.
     * 
     * @param csidList the csid list
     * 
     * @return the movement list
     */
    @Deprecated
    public MovementsCommonList getMovementList(List<String> csidList) {
        MovementsCommonList movementObjectList = new MovementsCommonList();
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csidList, handler);
            movementObjectList = (MovementsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.LIST_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getMovementList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.LIST_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return movementObjectList;
    }

    /**
     * Update movement.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public String updateMovement(
            @PathParam("csid") String csid,
            String xmlText) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateMovement with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateMovement: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.UPDATE_FAILED + ServiceMessages.MISSING_CSID).type(
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
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.UPDATE_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateMovement", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.UPDATE_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.UPDATE_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.toXML();
    }

    /**
     * Delete movement.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteMovement(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteMovement with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteMovement: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Movement csid=" + csid).type(
                    "text/plain").build();
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
                logger.debug("caught exception in deleteMovement", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.DELETE_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Search movements.
     * 
     * @param keywords the keywords
     * 
     * @return the movements common list
     */
    private MovementsCommonList searchMovements(MultivaluedMap<String, String> queryParams,
            String keywords) {
        MovementsCommonList movementsObjectList;
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
            movementsObjectList = (MovementsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(
                    ServiceMessages.SEARCH_FAILED + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in search for Movements", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.SEARCH_FAILED + e.getMessage()).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return movementsObjectList;
    }
}
