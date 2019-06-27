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

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.report.nuxeo.ReportDocumentModelHandler;
import org.collectionspace.services.publicitem.PublicitemsCommon;
import org.collectionspace.services.client.IQueryManager;
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
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.storage.JDBCTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path(ReportClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
//@Produces("application/xml;charset=UTF-8")
public class ReportResource extends NuxeoBasedResource {
    private static String REPOSITORY_NAME = JDBCTools.NUXEO_DATASOURCE_NAME;
    private static String REPORTS_FOLDER = "reports";
    private static String CSID_LIST_SEPARATOR = ",";
    final Logger logger = LoggerFactory.getLogger(ReportResource.class);

    private static String REPORTS_STD_CSID_PARAM = "csid";
    private static String REPORTS_STD_GROUPCSID_PARAM = "groupcsid";
    private static String REPORTS_STD_CSIDLIST_PARAM = "csidlist";
    private static String REPORTS_STD_TENANTID_PARAM = "tenantid";

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
            String mode = queryParams.getFirst(IQueryManager.SEARCH_TYPE_INVOCATION_MODE);
            String whereClause = null;
            DocumentFilter documentFilter = null;
            String common_part =ctx.getCommonPartLabel();
            if (docType != null && !docType.isEmpty()) {
                whereClause = QueryManager.createWhereClauseForInvocableByDocType(
                		common_part, docType);
                documentFilter = handler.getDocumentFilter();
                documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            if (mode != null && !mode.isEmpty()) {
                whereClause = QueryManager.createWhereClauseForInvocableByMode(
                		common_part, mode);
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
//    @GET
//    @Path("{csid}/output")
//    @Produces("application/pdf")
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
            InputStream reportInputStream = invokeReport(ctx, csid, invContext, outMimeType, outReportFileName);
            response = PublicItemUtil.publishToRepository(
            		(PublicitemsCommon)null,
            		resourceMap,
            		uriInfo,
            		getRepositoryClient(ctx),
            		ctx,
            		reportInputStream,
            		outReportFileName.toString());
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }

        return response;
    }

    @POST
    @Path("{csid}")
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
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }

        return response;
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

        ReportDocumentModelHandler handler = (ReportDocumentModelHandler)createDocumentHandler(ctx);
        result = handler.invokeReport(ctx, csid, invContext, outMimeType, outReportFileName);

        return result;
    }

}
