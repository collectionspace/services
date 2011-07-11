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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.config.ConfigReader;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.invocable.Invocable.InvocationError;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Path(ReportClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
//@Produces("application/xml;charset=UTF-8")
public class ReportResource extends ResourceBase {
    private static String REPOSITORY_NAME = "NuxeoDS";
    private static String REPORTS_FOLDER = "reports";
    private static String CSID_LIST_SEPARATOR = ",";
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
    							File.separator + REPORTS_FOLDER +
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
    
    @POST
    @Path("{csid}")
    @Produces("application/pdf")
    public Response invokeReport(
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentWrapper<DocumentModel> wrapper = 
            	getRepositoryClient(ctx).getDoc(ctx, csid);
    		DocumentModel docModel = wrapper.getWrappedObject();
    		String invocationMode = invContext.getMode();
    		String modeProperty = null;
    		HashMap params = new HashMap();
    		if(Invocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
    			modeProperty = InvocableJAXBSchema.SUPPORTS_SINGLE_DOC;
        		params.put("csid", invContext.getSingleCSID());
    		} else if(Invocable.INVOCATION_MODE_LIST.equalsIgnoreCase(invocationMode)) {
    			modeProperty = InvocableJAXBSchema.SUPPORTS_DOC_LIST;
    			List<String> csids = null;
    			InvocationContext.ListCSIDs listThing = invContext.getListCSIDs();
   				if(listThing!=null) {
   					csids = listThing.getCsid();
   				}
   				if(csids==null||csids.isEmpty()){
   	    			throw new BadRequestException(
   	    					"ReportResource: Report invoked in list mode, with no csids in list." );
   				}
   				StringBuilder sb = new StringBuilder();
   				boolean first = true;
   				for(String csidItem : csids) {
   					if(first)
   						first = false;
   					else
   						sb.append(CSID_LIST_SEPARATOR);
   	   				sb.append(csidItem);
   				}
        		params.put("csidlist", sb.toString());
    		} else if(Invocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
    			modeProperty = InvocableJAXBSchema.SUPPORTS_GROUP;
        		params.put("groupcsid", invContext.getGroupCSID());
    		} else {
    			throw new BadRequestException("ReportResource: unknown Invocation Mode: "
            			+invocationMode);
    		}
    		Boolean supports = (Boolean)docModel.getPropertyValue(modeProperty);
    		if(!supports) {
    			throw new BadRequestException(
    					"ReportResource: This Report does not support Invocation Mode: "
            			+invocationMode);
    		}
    		String reportFileName = (String)docModel.getPropertyValue(ReportJAXBSchema.FILENAME);
    		String fullPath = ServiceMain.getInstance().getServerRootDir() +
    							File.separator + ConfigReader.CSPACE_DIR_NAME + 
    							File.separator + REPORTS_FOLDER +
    							// File.separator + tenantName +
    							File.separator + reportFileName;
    		Connection conn = getConnection();

    		FileInputStream fileStream = new FileInputStream(fullPath);

            // fill the report
    		JasperPrint jasperprint = JasperFillManager.fillReport(fileStream, params,conn);
    		// export report to pdf and build a response with the bytes
    		byte[] pdfasbytes = JasperExportManager.exportReportToPdf(jasperprint);
    		
    		// Need to set response type for what is requested...
            Response response = Response.ok(pdfasbytes, "application/pdf").build();

           	return response;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }
    }

    private Connection getConnection() throws LoginException, SQLException {
        InitialContext ctx = null;
        Connection conn = null;
        try {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(REPOSITORY_NAME);
            if (ds == null) {
                throw new IllegalArgumentException("datasource not found: " + REPOSITORY_NAME);
            }
            conn = ds.getConnection();
            return conn;
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + REPOSITORY_NAME);
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

}
