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
package org.collectionspace.services.report;

import java.io.InputStream;
import java.util.List;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.report.nuxeo.ReportDocumentModelHandler;
import org.collectionspace.services.publicitem.PublicitemsCommon;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.publicitem.PublicItemUtil;
import org.collectionspace.services.common.query.QueryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

@Path(ReportClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
//@Produces("application/xml;charset=UTF-8")
public class ReportResource extends NuxeoBasedResource {
    final Logger logger = LoggerFactory.getLogger(ReportResource.class);

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 1982 $";
    	return lastChangeRevision;
    }

    @Override
    public String getServiceName() {
        return ReportClient.SERVICE_NAME;
    }

    @Override
    public Class<ReportsCommon> getCommonPartClass() {
    	return ReportsCommon.class;
    }

    @Override
    protected AbstractCommonList getCommonList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, UriInfo ui) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
            if (parentCtx != null && parentCtx.getCurrentRepositorySession() != null) {
                ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession()); // Reuse the current repo session if one exists
            }
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
            DocumentHandler handler = createDocumentHandler(ctx);
            String docType = queryParams.getFirst(IQueryManager.SEARCH_TYPE_DOCTYPE);
            List<String> modes = queryParams.get(IQueryManager.SEARCH_TYPE_INVOCATION_MODE);
            String whereClause = null;
            DocumentFilter documentFilter = null;
            String common_part =ctx.getCommonPartLabel();
            if (docType != null && !docType.isEmpty()) {
                whereClause = QueryManager.createWhereClauseForInvocableByDocType(
                		common_part, docType);
                documentFilter = handler.getDocumentFilter();
                documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            if (modes != null && !modes.isEmpty()) {
                whereClause = QueryManager.createWhereClauseForInvocableByMode(
                		common_part, modes);
                documentFilter = handler.getDocumentFilter();
                documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            if (whereClause !=null && logger.isDebugEnabled()) {
                logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            AbstractCommonList list = (AbstractCommonList) handler.getCommonPartList();
            return list;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }

    @GET
    @Path("mimetypes")
    public ReportsOuputMimeList getSupportMimeTypes(
    		@Context UriInfo ui) {
    	try {
	        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
	        ReportDocumentModelHandler handler = (ReportDocumentModelHandler)createDocumentHandler(ctx);
	
	    	return handler.getSupportMIMETypes(ctx);
    	} catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_MIMETYPES_FAILED);
        } 
    }
    
    /*
     * TODO: provide a static utility that will load a report, interrogate it
     * for information about the properties, and return that information.
     * See: http://jasperreports.sourceforge.net/api/net/sf/jasperreports/engine/JasperManager.html#loadReport%28java.lang.String%29
     * to get the report from the file.
     * Use: http://jasperreports.sourceforge.net/api/net/sf/jasperreports/engine/base/JRBaseReport.html#getParameters%28%29
     *  to get an array of http://jasperreports.sourceforge.net/api/net/sf/jasperreports/engine/JRParameter.html
     *  Cast each to JRBaseParameter and use isSystemDefined to filter out
     *    the system defined parameters.
     */

    /**
     * Gets the report.
     * @param csid the csid
     * @return the report
     */
    public Response invokeReport(
    		@Context UriInfo ui,
            @PathParam("csid") String csid) {
    	InvocationContext invContext = new InvocationContext();
    	invContext.setMode(Invocable.INVOCATION_MODE_NO_CONTEXT);
    	return invokeReport(ui, csid, invContext);
    }

    /*
     * Publishes the report to the PublicItem service.  The response is a URI to the corresponding PublicItem resource instance in
     * the form of /publicitems/{csid}.
     * To access the contents of the report use a form like /publicitems/{tenantId}/{csid}/content.  For example,
     * http://localhost:8180/cspace-services/publicitems/2991da78-6001-4f34-b02/1/content
     */
    @POST
    @Path("{csid}/publish")
    public Response invokeReportAndPublish(
    		@Context ResourceMap resourceMap,
    		@Context UriInfo uriInfo,
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
    	Response response = null;

        try {
            StringBuffer outMimeType = new StringBuffer();
            StringBuffer outReportFileName = new StringBuffer();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            
            if (isAuthorizedToInvokeReports(ctx) == true) {
	            InputStream reportInputStream = invokeReport(ctx, csid, invContext, outMimeType, outReportFileName);
	            response = PublicItemUtil.publishToRepository(
	            		(PublicitemsCommon)null,
	            		resourceMap,
	            		uriInfo,
	            		getRepositoryClient(ctx),
	            		ctx,
	            		reportInputStream,
	            		outReportFileName.toString());
            } else {
				ResponseBuilder builder = Response.status(Status.FORBIDDEN);
		        response = builder.build();
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }

        return response;
    }

    /*
     * This method allows backward compatibility with the old API for running reports.
     */
    private boolean isAuthorizedToInvokeReports(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	boolean result = true;
    			
		//
		// Until we enforce a user having POST perms on "/reports/*/invoke", we will continue to allow users with
		// POST perms on "/reports" to run reports -see JIRA issue https://collectionspace.atlassian.net/browse/DRYD-732
    	//
    	// To start enforcing POST perms on "/reports/*/invoke", uncomment the following block of code
		//

    	/*
    	CSpaceResource res = new URIResourceImpl(ctx.getTenantId(), REPORT_INVOKE_RESNAME, AuthZ.getMethod(ActionType.CREATE));
		if (AuthZ.get().isAccessAllowed(res) == false) {
			result = false;
		}
		*/

		return result;
    }

    /**
     * This method is deprecated at of CollectionSpace v5.3.
     * @param ui
     * @param csid
     * @param invContext
     * @return
     */
    @POST
    @Path("{csid}")
    @Deprecated
    public Response invokeReportDeprecated(
    		@Context UriInfo ui,
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
    	Response response = null;

        try {
            StringBuffer outMimeType = new StringBuffer();
            StringBuffer outFileName = new StringBuffer();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            
            if (isAuthorizedToInvokeReports(ctx) == true) {
	            InputStream reportInputStream = invokeReport(ctx, csid, invContext, outMimeType, outFileName);
				// Need to set response type for what is requested...
				ResponseBuilder builder = Response.ok(reportInputStream, outMimeType.toString());
				builder = builder.header("Content-Disposition","inline;filename=\""+ outFileName.toString() +"\"");
		        response = builder.build();
            } else {
				ResponseBuilder builder = Response.status(Status.FORBIDDEN);
		        response = builder.build();
            }
        } catch (Exception e) {
        	String msg = e.getMessage();
            throw bigReThrow(e, ServiceMessages.POST_FAILED + msg != null ? msg : "");
        }

        return response;
    }

	private ReportsCommon getReportsCommon(String csid) throws Exception {
		ReportsCommon result = null;

    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
		PoxPayloadOut ppo = get(csid, ctx);
		PayloadPart reportsCommonPart = ppo.getPart(ReportClient.SERVICE_COMMON_PART_NAME);
		result = (ReportsCommon)reportsCommonPart.getBody();

    	return result;
    }
	
    /*
     * Does the actual report generation and returns an InputStream with the results.
     */
    private InputStream invokeReport(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid,
    		InvocationContext invContext,
    		StringBuffer outMimeType,
    		StringBuffer outReportFileName) throws Exception {
    	InputStream result = null;

        if (csid == null || "".equals(csid)) {
            logger.error("invokeReport: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "invoke failed on Report csid=" + csid).type(
                    "text/plain").build();
            throw new CSWebApplicationException(response);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("invokeReport with csid=" + csid);
        }

        ReportsCommon reportsCommon = getReportsCommon(csid);
        ReportDocumentModelHandler handler = (ReportDocumentModelHandler)createDocumentHandler(ctx);
        result = handler.invokeReport(ctx, csid, reportsCommon, invContext, outMimeType, outReportFileName);

        return result;
    }

    @POST
    @Path("{csid}/invoke")
    public Response invokeReport(
    		@Context UriInfo ui,
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
    	Response response = null;

        try {
            StringBuffer outMimeType = new StringBuffer();
            StringBuffer outFileName = new StringBuffer();
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            InputStream reportInputStream = invokeReport(ctx, csid, invContext, outMimeType, outFileName);

			// Need to set response type for what is requested...
			ResponseBuilder builder = Response.ok(reportInputStream, outMimeType.toString());
			builder = builder.header("Content-Disposition","inline;filename=\""+ outFileName.toString() +"\"");
	        response = builder.build();
        } catch (Exception e) {
        	String msg = e.getMessage();
            throw bigReThrow(e, ServiceMessages.POST_FAILED + msg != null ? msg : "");
        }

        return response;
    }

}
