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
package org.collectionspace.services.loanin;

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

import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
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
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LoaninResource.
 */
@Path("/loansin")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class LoaninResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant serviceName. */
    private final static String serviceName = "loansin";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(LoaninResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    /**
     * Instantiates a new loanin resource.
     */
    public LoaninResource() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision: 1627 $";
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
    public Class<LoansinCommon> getCommonPartClass() {
    	return LoansinCommon.class;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), LoansinCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((LoansinCommon) obj);
//            }
//        }
//        return docHandler;
//    }

    /**
     * Creates the loanin.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createLoanin(MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //loaninObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(LoaninResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createLoanin", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the loanin.
     * 
     * @param csid the csid
     * 
     * @return the loanin
     */
    @GET
    @Path("{csid}")
    public MultipartOutput getLoanin(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getLoanin with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getLoanin: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Loanin csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
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
                logger.debug("getLoanin", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Loanin csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getLoanin", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Loanin CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the loanin list.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the loanin list
     */
    @GET
    @Produces("application/xml")
    public LoansinCommonList getLoaninList(@Context UriInfo ui,
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
    	LoansinCommonList result = null;
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	if (keywords != null) {
    		result = searchLoansin(queryParams, keywords);
    	} else {
    		result = getLoaninList(queryParams);
    	}
 
    	return result;
    }
    
    /**
     * Gets the loanin list.
     * 
     * @return the loanin list
     */
    private LoansinCommonList getLoaninList(MultivaluedMap<String, String> queryParams) {
        LoansinCommonList loaninObjectList;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            loaninObjectList = (LoansinCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLoaninList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return loaninObjectList;
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
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentWrapper<DocumentModel> docWrapper = 
            	getRepositoryClient(ctx).getDoc(ctx, csid);
            DocumentModelHandler<MultipartInput, MultipartOutput> handler 
            	= (DocumentModelHandler<MultipartInput, MultipartOutput>)createDocumentHandler(ctx);
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
     * Gets the loanin list.
     * 
     * @param csidList the csid list
     * 
     * @return the loanin list
     */
    @Deprecated
    public LoansinCommonList getLoaninList(List<String> csidList) {
        LoansinCommonList loaninObjectList = new LoansinCommonList();
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csidList, handler);
            loaninObjectList = (LoansinCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getLoaninList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return loaninObjectList;
    }
    
    /**
     * Update loanin.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public MultipartOutput updateLoanin(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateLoanin with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateLoanin: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Loanin csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateLoanin", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Loanin csid=" + csid).type(
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
     * Delete loanin.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteLoanin(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteLoanin with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteLoanin: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Loanin csid=" + csid).type(
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
                logger.debug("caught exception in deleteLoanin", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Loanin csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
        	
    /**
     * Search loansin.
     * 
     * @param keywords the keywords
     * 
     * @return the loansin common list
     */
    private LoansinCommonList searchLoansin(
    		MultivaluedMap<String, String> queryParams,
    		String keywords) {
    	LoansinCommonList loansinObjectList;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
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
            loansinObjectList = (LoansinCommonList) handler.getCommonPartList();            
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in search for Loansin", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return loansinObjectList;
    }
}
