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
package org.collectionspace.services.loanout;

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

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
//import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LoanoutResource.
 */
@Path("/loansout")
@Consumes("application/xml")
@Produces("application/xml")
public class LoanoutResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant serviceName. */
    private final static String serviceName = "loansout";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(LoanoutResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    /**
     * Instantiates a new loanout resource.
     */
    public LoanoutResource() {
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
    public Class<LoansoutCommon> getCommonPartClass() {
    	return LoansoutCommon.class;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), LoansoutCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((LoansoutCommon) obj);
//            }
//        }
//        return docHandler;
//    }

    /**
     * Creates the loanout.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createLoanout(String xmlText) {
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlText);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //loanoutObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(LoanoutResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createLoanout", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the loanout.
     * 
     * @param csid the csid
     * 
     * @return the loanout
     */
    @GET
    @Path("{csid}")
    public byte[] getLoanout(
    		@Context UriInfo ui,
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getLoanout with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getLoanout: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Loanout csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PoxPayloadOut result = null;
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLoanout", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Loanout csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLoanout", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Loanout CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.getBytes();
    }

    /**
     * Gets the loanout list.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the loanout list
     */
    @GET
    @Produces("application/xml")
    public LoansoutCommonList getLoanoutList(@Context UriInfo ui,
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
    	LoansoutCommonList result = null;
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	if (keywords != null) {
    		result = searchLoansout(queryParams, keywords);
    	} else {
    		result = getLoanoutList(queryParams);
    	}
 
    	return result;
    }
    
    /**
     * Gets the loanout list.
     * 
     * @return the loanout list
     */
    private LoansoutCommonList getLoanoutList(MultivaluedMap<String, String> queryParams) {
        LoansoutCommonList loanoutObjectList;
        try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            loanoutObjectList = (LoansoutCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLoanoutList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return loanoutObjectList;
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
            DocumentModelHandler<PoxPayloadIn, PoxPayloadOut> handler 
            	= (DocumentModelHandler<PoxPayloadIn, PoxPayloadOut>)createDocumentHandler(ctx);
            List<String> authRefFields = 
            	((MultipartServiceContextImpl)ctx).getCommonPartPropertyValues(
        			ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            authRefList = handler.getAuthorityRefs(docWrapper, authRefFields);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Failed to retrieve authority references: reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve authority references").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return authRefList;
    }

    /**
     * Gets the loanout list.
     * 
     * @param csidList the csid list
     * 
     * @return the loanout list
     */
    @Deprecated
    public LoansoutCommonList getLoanoutList(List<String> csidList) {
        LoansoutCommonList loanoutObjectList = new LoansoutCommonList();
        try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csidList, handler);
            loanoutObjectList = (LoansoutCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLoanoutList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return loanoutObjectList;
    }
    
    /**
     * Update loanout.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public byte[] updateLoanout(
            @PathParam("csid") String csid,
            String xmlText) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateLoanout with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateLoanout: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Loanout csid=" + csid).type(
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
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateLoanout", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Loanout csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.getBytes();
    }

    /**
     * Delete loanout.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteLoanout(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteLoanout with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteLoanout: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Loanout csid=" + csid).type(
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
                logger.debug("caught exception in deleteLoanout", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Loanout csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
        	
    /**
     * Search loansout.
     * 
     * @param keywords the keywords
     * 
     * @return the loansout common list
     */
    private LoansoutCommonList searchLoansout(MultivaluedMap<String, String> queryParams,
    		String keywords) {
    	LoansoutCommonList loansoutObjectList;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);

            // perform a keyword search
            if (keywords != null && !keywords.isEmpty()) {
            	String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
	            DocumentFilter documentFilter = handler.getDocumentFilter();
	            documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
	            if (logger.isDebugEnabled()) {
	            	logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
	            }
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            loansoutObjectList = (LoansoutCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in search for Loansout", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return loansoutObjectList;
    }
}
