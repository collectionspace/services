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

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ConfigReader;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ReportResource.
 */
@Path("/reports")
@Consumes("application/xml")
@Produces("application/xml;charset=UTF-8")
public class ReportResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant serviceName. */
    private final static String serviceName = "reports";
    
    private static String repositoryName = "NuxeoDS";
    private static String reportsFolder = "reports";
    
	/** The logger. */
    final Logger logger = LoggerFactory.getLogger(ReportResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    /**
     * Instantiates a new report resource.
     */
    public ReportResource() {
        // do nothing
    }

    public static String getRepositoryName() {
		return repositoryName;
	}

	public static void setRepositoryName(String repositoryName) {
		ReportResource.repositoryName = repositoryName;
	}

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision: 1982 $";
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
    public Class<ReportsCommon> getCommonPartClass() {
    	return ReportsCommon.class;
    }
    
    /**
     * Creates the report.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createReport(String xmlText) {
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlText);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(ReportResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createReport", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the report.
     * 
     * @param csid the csid
     * 
     * @return the report
     */
    @GET
    @Path("{csid}")
    public byte[] getReport(
    		@Context UriInfo ui,
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getReport with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getReport: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Report csid=" + csid).type(
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
                logger.debug("getReport", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Report csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getReport", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Report CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.getBytes();
    }

    /**
     * Gets the report.
     * 
     * @param csid the csid
     * 
     * @return the report
     */
    @GET
    @Path("{csid}/output")
    @Produces("application/pdf")
    public Response invokeReport(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("invokeReport with csid=" + csid);
        }
        Response response = null;
        if (csid == null || "".equals(csid)) {
            logger.error("invokeReport: missing csid!");
            response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "invoke failed on Report csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentWrapper<DocumentModel> docWrapper = getRepositoryClient(ctx).getDoc(ctx, csid);
    		DocumentModel docModel = docWrapper.getWrappedObject();
    		String reportFileName = (String)docModel.getPropertyValue(ReportJAXBSchema.FILENAME);
    		String fullPath = ServiceMain.getInstance().getServerRootDir() +
    							File.separator + ConfigReader.CSPACE_DIR_NAME + 
    							File.separator + reportsFolder +
    							File.separator + reportFileName;
    		Connection conn = getConnection();
    		HashMap params = new HashMap();
    		FileInputStream fileStream = new FileInputStream(fullPath);

            // fill the report
    		JasperPrint jasperprint = JasperFillManager.fillReport(fileStream, params,conn);
    		// export report to pdf and build a response with the bytes
    		byte[] pdfasbytes = JasperExportManager.exportReportToPdf(jasperprint);
    		response = Response.ok(pdfasbytes, "application/pdf").build();
        } catch (UnauthorizedException ue) {
            response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Invoke failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("invokeReport", dnfe);
            }
            response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Invoke failed on Report csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (SQLException sqle) {
            // SQLExceptions can be chained. We have at least one exception, so
            // set up a loop to make sure we let the user know about all of them
            // if there happens to be more than one.
            if (logger.isDebugEnabled()) {
	            SQLException tempException = sqle;
	            while (null != tempException) {
	                	logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
	
	                // loop to the next exception
	                tempException = tempException.getNextException();
	            }
            }
            response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (SQL problem) on Report csid=" + csid).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (JRException jre) {
            if (logger.isDebugEnabled()) {
            	logger.debug("JR Exception: " + jre.getLocalizedMessage() + " Cause: "+jre.getCause());
            }
            response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (Jasper problem) on Report csid=" + csid).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("invokeReport", e);
            }
            response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Invoke failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (response == null) {
            response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Invoke failed, the requested Report CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return response;
    }

    private Connection getConnection() throws LoginException, SQLException {
        InitialContext ctx = null;
        Connection conn = null;
        try {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(repositoryName);
            if (ds == null) {
                throw new IllegalArgumentException("datasource not found: " + repositoryName);
            }
            conn = ds.getConnection();
            return conn;
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + repositoryName);
            le.initCause(ex);
            throw le;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }

    }

    /**
     * Gets the report list.
     * 
     * @param ui the ui
     * @param keywords the keywords
     * 
     * @return the report list
     */
    @GET
    @Produces("application/xml")
    public ReportsCommonList getReportList(@Context UriInfo ui,
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords) {
    	ReportsCommonList result = null;
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    	
    	if (keywords != null) {
    		result = searchReports(queryParams, keywords);
    	} else {
    		result = getReportList(queryParams);
    	}
 
    	return result;
    }
    
    /**
     * Gets the report list.
     * 
     * @return the report list
     */
    private ReportsCommonList getReportList(MultivaluedMap<String, String> queryParams) {
        ReportsCommonList reportObjectList;
        try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            reportObjectList = (ReportsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getReportList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return reportObjectList;
    }


    /**
     * Gets the report list.
     * 
     * @param csidList the csid list
     * 
     * @return the report list
    public ReportsCommonList getReportList(List<String> csidList) {
        ReportsCommonList reportObjectList = new ReportsCommonList();
        try {
        	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csidList, handler);
            reportObjectList = (ReportsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getReportList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return reportObjectList;
    }
     */
    
    /**
     * Update report.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public byte[] updateReport(
            @PathParam("csid") String csid,
            String xmlText) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateReport with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateReport: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Report csid=" + csid).type(
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
                logger.debug("caugth exception in updateReport", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Report csid=" + csid).type(
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
     * Delete report.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteReport(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteReport with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteReport: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Report csid=" + csid).type(
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
                logger.debug("caught exception in deleteReport", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Report csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
    	
    /**
     * Search reports.
     * 
     * @param keywords the keywords
     * 
     * @return the reports common list
     */
    private ReportsCommonList searchReports(MultivaluedMap<String, String> queryParams,
    		String keywords) {
    	ReportsCommonList reportsObjectList;
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
            reportsObjectList = (ReportsCommonList) handler.getCommonPartList();            
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in search for Reports", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return reportsObjectList;
    }
}
