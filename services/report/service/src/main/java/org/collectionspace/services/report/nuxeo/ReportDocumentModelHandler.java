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
package org.collectionspace.services.report.nuxeo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.report.ReportsCommon;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ConfigReader;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReportDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ReportDocumentModelHandler extends DocHandlerBase<ReportsCommon> {
    private final Logger logger = LoggerFactory.getLogger(ReportDocumentModelHandler.class);
    private static String REPOSITORY_NAME = JDBCTools.NUXEO_REPOSITORY_NAME;
    private static String REPORTS_FOLDER = "reports";
    private static String CSID_LIST_SEPARATOR = ",";
    
    private static String REPORTS_STD_CSID_PARAM = "csid";
    private static String REPORTS_STD_GROUPCSID_PARAM = "groupcsid";
    private static String REPORTS_STD_CSIDLIST_PARAM = "csidlist";
    private static String REPORTS_STD_TENANTID_PARAM = "tenantid";
	
	public Response invokeReport(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			String csid,
			InvocationContext invContext) throws Exception {
		RepositoryInstance repoSession = null;
		boolean releaseRepoSession = false;

		String invocationMode = invContext.getMode();
		String modeProperty = null;
		HashMap<String, Object> params = new HashMap<String, Object>();
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
		
		RepositoryJavaClientImpl repoClient = (RepositoryJavaClientImpl)this.getRepositoryClient(ctx);
		repoSession = this.getRepositorySession();
		if (repoSession == null) {
			repoSession = repoClient.getRepositorySession(ctx);
			releaseRepoSession = true;
		}

		String reportFileName = null;
		// Get properties from the batch docModel, and release the session
		try {
			DocumentWrapper<DocumentModel> wrapper = repoClient.getDoc(repoSession, ctx, csid);
			DocumentModel docModel = wrapper.getWrappedObject();
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
			reportFileName = (String)docModel.getPropertyValue(ReportJAXBSchema.FILENAME);
		} catch (PropertyException pe) {
			if (logger.isDebugEnabled()) {
				logger.debug("Property exception getting batch values: ", pe);
			}
			throw pe;
		} catch (DocumentException de) {
			if (logger.isDebugEnabled()) {
				logger.debug("Problem getting batch doc: ", de);
			}
			throw de;
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (releaseRepoSession && repoSession != null) {
				repoClient.releaseRepositorySession(ctx, repoSession);
			}
		}
       	return buildReportResponse(csid, params, reportFileName);
	}


    private Response buildReportResponse(String reportCSID, HashMap<String, Object> params, String reportFileName)
    				throws Exception {
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

    private Connection getConnection() throws NamingException, SQLException {
    	return JDBCTools.getConnection(REPOSITORY_NAME);
    }

}

