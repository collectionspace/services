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

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.client.IQueryManager;
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
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.invocable.Invocable.InvocationError;
import org.collectionspace.services.common.query.QueryManager;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    protected AbstractCommonList getList(MultivaluedMap<String, String> queryParams) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            String docType = queryParams.getFirst(IQueryManager.SEARCH_TYPE_DOCTYPE);
            String mode = queryParams.getFirst(IQueryManager.SEARCH_TYPE_INVCOATION_MODE);
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
    	InvocationContext invContext = new InvocationContext();
    	invContext.setMode(Invocable.INVOCATION_MODE_NO_CONTEXT);
    	return invokeReport(csid, invContext);
    }
    
    @POST
    @Path("{csid}")
    @Produces("application/pdf")
    public Response invokeReport(
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
        if (csid == null || "".equals(csid)) {
            logger.error("invokeReport: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "invoke failed on Report csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("invokeReport with csid=" + csid);
        }
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentWrapper<DocumentModel> wrapper = 
            	getRepositoryClient(ctx).getDoc(ctx, csid);
    		DocumentModel docModel = wrapper.getWrappedObject();
    		String invocationMode = invContext.getMode();
    		String modeProperty = null;
    		HashMap params = new HashMap();
    		params.put(REPORTS_STD_TENANTID_PARAM, ctx.getTenantId());
    		boolean checkDocType = true;
    		if(Invocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
    			modeProperty = InvocableJAXBSchema.SUPPORTS_SINGLE_DOC;
        		params.put(REPORTS_STD_CSID_PARAM, invContext.getSingleCSID());
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
        		params.put(REPORTS_STD_CSIDLIST_PARAM, sb.toString());
    		} else if(Invocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
    			modeProperty = InvocableJAXBSchema.SUPPORTS_GROUP;
        		params.put(REPORTS_STD_GROUPCSID_PARAM, invContext.getGroupCSID());
    		} else if(Invocable.INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(invocationMode)) {
    			modeProperty = InvocableJAXBSchema.SUPPORTS_NO_CONTEXT;
    			checkDocType = false;
    		} else {
    			throw new BadRequestException("ReportResource: unknown Invocation Mode: "
            			+invocationMode);
    		}
    		Boolean supports = (Boolean)docModel.getPropertyValue(modeProperty);
    		if(supports == null || !supports) {
    			throw new BadRequestException(
    					"ReportResource: This Report does not support Invocation Mode: "
            			+invocationMode);
    		}
        	if(checkDocType) {
        		List<String> forDocTypeList = 
        			(List<String>)docModel.getPropertyValue(InvocableJAXBSchema.FOR_DOC_TYPES);
        		if(forDocTypeList==null
        				|| !forDocTypeList.contains(invContext.getDocType())) {
            		throw new BadRequestException(
            				"ReportResource: Invoked with unsupported document type: "
            				+invContext.getDocType());
            	}
        	}
    		String reportFileName = (String)docModel.getPropertyValue(ReportJAXBSchema.FILENAME);

           	return buildReportResponse(csid, params, reportFileName);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }
    }
    
    private Response buildReportResponse(String reportCSID, HashMap params, String reportFileName) {
		Connection conn = null;
		Response response = null;
    	try {
			String fullPath = ServiceMain.getInstance().getServerRootDir() +
								File.separator + ConfigReader.CSPACE_DIR_NAME + 
								File.separator + REPORTS_FOLDER +
								// File.separator + tenantName +
								File.separator + reportFileName;
			conn = getConnection();
	
            if (logger.isTraceEnabled()) {
            	logger.trace("ReportResource for Report csid=" + reportCSID
            			+" opening report file: "+fullPath);
            }
			FileInputStream fileStream = new FileInputStream(fullPath);
	
	        // fill the report
			JasperPrint jasperprint = JasperFillManager.fillReport(fileStream, params,conn);
			// export report to pdf and build a response with the bytes
			byte[] pdfasbytes = JasperExportManager.exportReportToPdf(jasperprint);
			
			// Need to set response type for what is requested...
	        response = Response.ok(pdfasbytes, "application/pdf").build();
	
	       	return response;    	
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
                    		"Invoke failed (SQL problem) on Report csid=" + reportCSID).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (JRException jre) {
            if (logger.isDebugEnabled()) {
            	logger.debug("JR Exception: " + jre.getLocalizedMessage() + " Cause: "+jre.getCause());
            }
            response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (Jasper problem) on Report csid=" + reportCSID).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (FileNotFoundException fnfe) {
            if (logger.isDebugEnabled()) {
            	logger.debug("FileNotFoundException: " + fnfe.getLocalizedMessage());
            }
            response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (SQL problem) on Report csid=" + reportCSID).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
		} finally {
        	if(conn!=null) {
        		try {
        		conn.close();
                } catch (SQLException sqle) {
                    // SQLExceptions can be chained. We have at least one exception, so
                    // set up a loop to make sure we let the user know about all of them
                    // if there happens to be more than one.
                    if (logger.isDebugEnabled()) {
   	                	logger.debug("SQL Exception closing connection: " 
   	                			+ sqle.getLocalizedMessage());
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Exception closing connection", e);
                    }
                }
        	}
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
