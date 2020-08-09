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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.account.AccountResource;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.report.ResourceActionGroup;
import org.collectionspace.services.report.ResourceActionGroupList;
import org.collectionspace.services.report.ReportsCommon.ForRoles;
import org.collectionspace.services.report.MIMEType;
import org.collectionspace.services.report.MIMETypeItemType;
import org.collectionspace.services.report.ReportsCommon;
import org.collectionspace.services.report.ReportsOuputMimeList;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.JEEServerDeployment;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorization_mgt.ActionGroup;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.jfree.util.Log;

import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.DocumentModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReportDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ReportDocumentModelHandler extends NuxeoDocumentModelHandler<ReportsCommon> {
		private final Logger logger = LoggerFactory.getLogger(ReportDocumentModelHandler.class);

		private static final Pattern INVALID_CSID_PATTERN = Pattern.compile("[^\\w\\-]");
    private static String REPORTS_FOLDER = "reports";
    private static String CSID_LIST_SEPARATOR = ",";

    private static String REPORTS_STD_CSID_PARAM = "csid";
    private static String REPORTS_STD_GROUPCSID_PARAM = "groupcsid";
    private static String REPORTS_STD_CSIDLIST_PARAM = "csidlist";
    private static String REPORTS_STD_TENANTID_PARAM = "tenantid";

    //
    // Map the MIME types from the service bindings to our payload output
    //
    public ReportsOuputMimeList getSupportMIMETypes(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	//
    	// Create a new payload response instance and initialize it
    	//
    	ReportsOuputMimeList result = new ReportsOuputMimeList();
    	MIMEType resultMIMEType = result.getMIMETypeList();
    	if (resultMIMEType == null) {
    		result.setMIMETypeList(resultMIMEType = new MIMEType());
    	}
    	List<MIMETypeItemType> resultMIMETypeItemList = resultMIMEType.getMIMEType();

    	//
    	// Read the MIME type values from the service bindings and put into our response payload
    	//
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        ServiceBindingType reportServiceBinding = tReader.getServiceBinding(ctx.getTenantId(), ctx.getServiceName());
        List<PropertyItemType> bindingsMIMETypeList = ServiceBindingUtils.getPropertyValueList(reportServiceBinding, ServiceBindingUtils.OUTPUT_MIME_PROP);

        if (bindingsMIMETypeList != null) {
        	for (PropertyItemType bindingItemMimeType : bindingsMIMETypeList) {
        		MIMETypeItemType resultMimeTypeItem = new MIMETypeItemType();
        		String displayName = bindingItemMimeType.getDisplayName();
        		if (displayName != null && displayName.trim().isEmpty() == false) {
            		resultMimeTypeItem.setKey(displayName);
        		} else {
            		resultMimeTypeItem.setKey(bindingItemMimeType.getValue());
        		}
        		resultMimeTypeItem.setValue(bindingItemMimeType.getValue());
        		resultMIMETypeItemList.add(resultMimeTypeItem);
        	}
        }

        return result;
    }

    private String getInvocationContextLogging(InvocationContext invContext, Map<String, Object> params) {
		String outputMIME = invContext.getOutputMIME();
		String mode = invContext.getMode();
		String updateCoreValues = invContext.getUpdateCoreValues();
		String docType = invContext.getDocType();
		String singleCSID = invContext.getSingleCSID();
		String groupCSID = invContext.getGroupCSID();
		String listCSIDs = invContext.getListCSIDs() == null ? "" : invContext.getListCSIDs().toString();

		String result =
				"{MIME type: "  + outputMIME +
				"\n \t Context mode: " + mode +
				"\n \t Update Core Values: " + updateCoreValues +
				"\n \t Document type: " + docType +
				"\n \t CSID: " + singleCSID +
				"\n \t Group CSID: " + groupCSID +
				"\n \t List CSIDs: " + listCSIDs +
				"\n \t Parameters: " + params.toString() + "}";
		return result;
	}

	private String assertValidCsid(String csid) throws IllegalArgumentException {
		if (INVALID_CSID_PATTERN.matcher(csid).find()) {
			throw new IllegalArgumentException("Invalid csid: " + csid);
		}

		return csid;
	}

	public InputStream invokeReport(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			String csid,
			ReportsCommon reportsCommon,
			InvocationContext invContext,
			StringBuffer outMimeType,
			StringBuffer outReportFileName) throws Exception {
		CoreSessionInterface repoSession = null;
		boolean releaseRepoSession = false;

		// Ensure the current user has permission to run this report
		if (isAuthoritzed(reportsCommon) == false) {
			String msg = String.format("Report Resource: The user '%s' is not authorized to run the report '%s' CSID='%s'",
					AuthN.get().getUserId(), reportsCommon.getName(), csid);
			throw new PermissionException(msg);
		}

		String invocationMode = invContext.getMode();
		String modeProperty = null;
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(REPORTS_STD_TENANTID_PARAM, ctx.getTenantId());
		boolean checkDocType = true;

		// Note we set before we put in the default ones, so they cannot override tenant or CSID.
		setParamsFromContext(params, invContext);

		if (Invocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_SINGLE_DOC;
			params.put(REPORTS_STD_CSID_PARAM, assertValidCsid(invContext.getSingleCSID()));
		} else if (Invocable.INVOCATION_MODE_LIST.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_DOC_LIST;
			List<String> csids = null;
			InvocationContext.ListCSIDs listThing = invContext.getListCSIDs();
				if (listThing!=null) {
					csids = listThing.getCsid();
				}
				if (csids==null||csids.isEmpty()){
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
	   				sb.append(assertValidCsid(csidItem));
				}
    		params.put(REPORTS_STD_CSIDLIST_PARAM, sb.toString());
		} else if(Invocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_GROUP;
			params.put(REPORTS_STD_GROUPCSID_PARAM, assertValidCsid(invContext.getGroupCSID()));
		} else if(Invocable.INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_NO_CONTEXT;
			checkDocType = false;
		} else {
			throw new BadRequestException("ReportResource: unknown Invocation Mode: "
        			+invocationMode);
		}

		logger.debug("The invocation context is: \n " + getInvocationContextLogging(invContext, params));
		logger.debug("The report is being called with the following parameters, which are being passed to Jasper: \n" + params.toString());
		logger.debug("The mode being passed to Jasper is: " + invocationMode);

		NuxeoRepositoryClientImpl repoClient = (NuxeoRepositoryClientImpl)this.getRepositoryClient(ctx);
		repoSession = this.getRepositorySession();
		if (repoSession == null) {
			repoSession = repoClient.getRepositorySession(ctx);
			releaseRepoSession = true;
		}

		// Get properties from the report docModel, and release the session
		String reportFileNameProperty;
		try {
			DocumentWrapper<DocumentModel> wrapper = repoClient.getDoc(repoSession, ctx, csid);
			DocumentModel docModel = wrapper.getWrappedObject();
			Boolean supports = (Boolean) NuxeoUtils.getProperyValue(docModel, modeProperty); //docModel.getPropertyValue(modeProperty);
			if(supports == null || !supports) {
				throw new BadRequestException(
						"ReportResource: This Report does not support Invocation Mode: "
	        			+invocationMode);
			}
	    	if (checkDocType) {
	    		List<String> forDocTypeList =
	    			(List<String>) NuxeoUtils.getProperyValue(docModel, InvocableJAXBSchema.FOR_DOC_TYPES); //docModel.getPropertyValue(InvocableJAXBSchema.FOR_DOC_TYPES);
	    		if (forDocTypeList==null || !forDocTypeList.contains(invContext.getDocType())) {
	        		throw new BadRequestException(
	        				"ReportResource: Invoked with unsupported document type: "
	        				+invContext.getDocType());
	        	}
	    	}
	    	reportFileNameProperty = (String) NuxeoUtils.getProperyValue(docModel, ReportJAXBSchema.FILENAME); //docModel.getPropertyValue(ReportJAXBSchema.FILENAME)); // Set the outgoing param with the report file name
			//
	    	// If the invocation context contains a MIME type then use it.  Otherwise, look in the report resource.  If no MIME type in the report resource,
	    	// use the default MIME type.
	    	//
	    	if (!Tools.isEmpty(invContext.getOutputMIME())) {
	    		outMimeType.append(invContext.getOutputMIME());
	    	} else if (Tools.isEmpty(outMimeType.toString()) && params.containsKey("OutputMIME")) {
	    		// See UCB - https://github.com/cspace-deployment/services/pull/140/files
	    		outMimeType.append(params.get("OutputMIME"));
	    	} else {
	    		// Use the default
	    		String reportOutputMime = (String) NuxeoUtils.getProperyValue(docModel, ReportJAXBSchema.OUTPUT_MIME); //docModel.getPropertyValue(ReportJAXBSchema.OUTPUT_MIME);
	    		if (!Tools.isEmpty(reportOutputMime)) {
	    			outMimeType.append(reportOutputMime);
	    		} else {
	    			outMimeType.append(ReportClient.DEFAULT_REPORT_OUTPUT_MIME);
	    		}
	    	}
		} catch (PropertyException pe) {
			if (logger.isDebugEnabled()) {
				logger.debug("Property exception getting report values: ", pe);
			}
			throw pe;
		} catch (DocumentException de) {
			if (logger.isDebugEnabled()) {
				logger.debug("Problem getting report report: ", de);
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

       	return buildReportResult(csid, params, reportFileNameProperty, outMimeType.toString(), outReportFileName);
	}

	private void setParamsFromContext(Map<String, Object> params, InvocationContext invContext) {
		InvocationContext.Params icParams = invContext.getParams();
		if(icParams!= null) {
			List<InvocationContext.Params.Param> icParamList = icParams.getParam();
			if(icParamList != null) {
				for(InvocationContext.Params.Param param:icParamList) {
					String key = param.getKey();
					String value = param.getValue();
					if(!Tools.isEmpty(key) && !Tools.isEmpty(value)) {
						params.put(key, value);
					}
				}
			}
		}

	}

    private InputStream buildReportResult(String reportCSID,
    		HashMap<String, Object> params, String reportFileName, String outputMimeType, StringBuffer outReportFileName)
    				throws Exception {
		Connection conn = null;
		InputStream result = null;

    	try {
    		String fileNameBase = Tools.getFilenameBase(reportFileName);
    		String compiledReportFilename = fileNameBase+ReportClient.COMPILED_REPORT_EXTENSION;
    		String reportDescriptionFilename = fileNameBase+ReportClient.REPORT_DECSRIPTION_EXTENSION;

			String basePath = ServiceMain.getInstance().getServerRootDir() +
								File.separator + JEEServerDeployment.CSPACE_DIR_NAME +
								File.separator + REPORTS_FOLDER +
								// File.separator + tenantName +
								File.separator; // + reportFileName;

			String compiledFilePath = basePath+compiledReportFilename;
			File f = new File(compiledFilePath);
			if(!f.exists()) { // Need to compile the file
				// First verify that there is a source file.
				String sourceFilePath = basePath+reportDescriptionFilename;
				File f2 = new File(sourceFilePath);
				if(!f2.exists()) { // Missing source file - error!
					logger.error("Report for csid={} is missing the specified source file: {}",
									reportCSID, sourceFilePath);
					throw new RuntimeException("Report is missing the specified source file!");
				}
            	logger.info("Report for csid={} is not compiled. Compiling first, and saving to: {}",
            			reportCSID, compiledFilePath);
            	JasperCompileManager.compileReportToFile(sourceFilePath, compiledFilePath);
			}

			conn = getConnection();

            if (logger.isTraceEnabled()) {
            	logger.trace("ReportResource for csid=" + reportCSID
            			+" output as "+outputMimeType+" using report file: "+compiledFilePath);
            }
			FileInputStream fileStream = new FileInputStream(compiledFilePath);

			// export report to pdf and build a response with the bytes
			//JasperExportManager.exportReportToPdf(jasperprint);

			JRExporter exporter = null;
			// Strip extension from report filename.
			String outputFilename = reportFileName;
			// Strip extension from report filename.
			int idx = outputFilename.lastIndexOf(".");
			if(idx>0)
				outputFilename = outputFilename.substring(0, idx);
			// Strip any sub-dir from report filename.
			idx = outputFilename.lastIndexOf(File.separator);
			if(idx>0)
				outputFilename = outputFilename.substring(idx+1);
			if(outputMimeType.equals(MediaType.APPLICATION_XML)) {
				params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
				exporter = new JRXmlExporter();
				outputFilename = outputFilename+".xml";
			} else if(outputMimeType.equals(MediaType.TEXT_HTML)) {
				exporter = new JRHtmlExporter();
				outputFilename = outputFilename+".html";
			} else if(outputMimeType.equals(ReportClient.PDF_MIME_TYPE)) {
				exporter = new JRPdfExporter();
				outputFilename = outputFilename+".pdf";
			} else if(outputMimeType.equals(ReportClient.CSV_MIME_TYPE)) {
				params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
				exporter = new JRCsvExporter();
				exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ",");
				outputFilename = outputFilename+".csv";
			} else if(outputMimeType.equals(ReportClient.TSV_MIME_TYPE)) {
				params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
				exporter = new JRCsvExporter();
				exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, "\t");
				outputFilename = outputFilename+".csv";
			} else if(outputMimeType.equals(ReportClient.MSWORD_MIME_TYPE)	// Understand msword as docx
					|| outputMimeType.equals(ReportClient.OPEN_DOCX_MIME_TYPE)) {
				exporter = new JRDocxExporter();
				outputFilename = outputFilename+".docx";
			} else if(outputMimeType.equals(ReportClient.MSEXCEL_MIME_TYPE)	// Understand msexcel as xlsx
					|| outputMimeType.equals(ReportClient.OPEN_XLSX_MIME_TYPE)) {
				exporter = new JRXlsxExporter();
				outputFilename = outputFilename+".xlsx";
			} else if(outputMimeType.equals(ReportClient.MSPPT_MIME_TYPE)	// Understand msppt as xlsx
					|| outputMimeType.equals(ReportClient.OPEN_PPTX_MIME_TYPE)) {
				exporter = new JRPptxExporter();
				outputFilename = outputFilename+".pptx";
			} else {
				logger.error("Reporting: unsupported output MIME type - defaulting to PDF");
				exporter = new JRPdfExporter();
				outputFilename = outputFilename+"-default-to.pdf";
			}
			outReportFileName.append(outputFilename); // Set the out going param to the report's final file name
                        // FIXME: Logging temporarily set to INFO level for CSPACE-5766;
                        // can change to TRACE or DEBUG level as warranted thereafter
                        if (logger.isInfoEnabled()) {
                            logger.info(FileTools.getJavaTmpDirInfo());
                        }
                        // fill the report
			JasperPrint jasperPrint = JasperFillManager.fillReport(fileStream, params,conn);

			// Report will be to a temporary file.
			File tempOutputFile = Files.createTempFile("report-", null).toFile();
			FileOutputStream tempOutputStream = new FileOutputStream(tempOutputFile);
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, tempOutputStream);
			exporter.exportReport();
			tempOutputStream.close();

			result = new FileInputStream(tempOutputFile);
	       	return result;
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
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (SQL problem) on Report csid=" + reportCSID).type("text/plain").build();
            throw new CSWebApplicationException(sqle, response);
        } catch (JRException jre) {
            if (logger.isDebugEnabled()) {
            	logger.debug("JR Exception: " + jre.getLocalizedMessage() + " Cause: "+jre.getCause());
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (Jasper problem) on Report csid=" + reportCSID).type("text/plain").build();
            throw new CSWebApplicationException(jre, response);
        } catch (FileNotFoundException fnfe) {
            if (logger.isDebugEnabled()) {
            	logger.debug("FileNotFoundException: " + fnfe.getLocalizedMessage());
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    		"Invoke failed (SQL problem) on Report csid=" + reportCSID).type("text/plain").build();
            throw new CSWebApplicationException(fnfe, response);
		} finally {
        	if (conn!=null) {
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
    	Connection result = null;

    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
    	try {
    		String repositoryName = ctx.getRepositoryName();
	    	if (repositoryName != null && repositoryName.trim().isEmpty() == false) {
                        String cspaceInstanceId = ServiceMain.getInstance().getCspaceInstanceId();
                        String databaseName = JDBCTools.getDatabaseName(repositoryName, cspaceInstanceId);
	    		result = JDBCTools.getConnection(JDBCTools.NUXEO_READER_DATASOURCE_NAME, databaseName);
	    	}
		} catch (Exception e) {
			Log.error(e);
			throw new NamingException();
		}

    	return result;
    }

	/**
	 * Check to see if the current user is authorized to run/invoke this report.  If the report
	 * did not specify any permissions, we assume that the current user is authorized to run the report.
	 * @param reportsCommon
	 * @return
	 */
	protected boolean isAuthoritzedWithPermissions(ReportsCommon reportsCommon) {
		boolean result = true;

		ResourceActionGroupList resourceActionGroupList = reportsCommon.getResourceActionGroupList();
		if (resourceActionGroupList != null) {
			String tenantId = AuthN.get().getCurrentTenantId();
			for (ResourceActionGroup resourceActionGroup: resourceActionGroupList.getResourceActionGroup()) {
				String resourceName = resourceActionGroup.getResourceName();
				ActionGroup actionGroup = ActionGroup.creatActionGroup(resourceActionGroup.getActionGroup());
				for (ActionType actionType: actionGroup.getActions()) {
					CSpaceResource res = new URIResourceImpl(tenantId, resourceName, AuthZ.getMethod(actionType));
					if (AuthZ.get().isAccessAllowed(res) == false) {
						return false;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns true if we found any required permissions.
	 *
	 * @param reportCommon
	 * @return
	 */
	private boolean hasRequiredPermissions(ReportsCommon reportCommon) {
		boolean result = false;

		try {
			result = reportCommon.getResourceActionGroupList().getResourceActionGroup().size() > 0;
		} catch (NullPointerException e) {
			// ignore exception, we're just testing to see if we have any list elements
		}

		return result;
	}

	/**
	 * Returns true if we found any required roles.
	 *
	 * @param reportCommon
	 * @return
	 */
	private boolean hasRequiredRoles(ReportsCommon reportCommon) {
		boolean result = false;

		try {
			result = reportCommon.getForRoles().getRoleDisplayName().size() > 0;
		} catch (NullPointerException e) {
			// ignore exception, we're just testing to see if we have any list elements
		}

		return result;
	}

	/**
	 * The current user is authorized to run the report if:
	 * 	1. No permissions or roles are specified in the report
	 *  2. No roles are specified, but permissions are specified and the current user has those permissions
	 *  3. Roles are specified and the current user is a member of at least one of the roles.
	 *
	 * @param reportsCommon
	 * @return
	 */
	protected boolean isAuthoritzed(ReportsCommon reportsCommon) {
		boolean result = true;

		if (hasRequiredRoles(reportsCommon)) {
			result = isAuthorizedWithRoles(reportsCommon);
		} else if (hasRequiredPermissions(reportsCommon)) {
			result = isAuthoritzedWithPermissions(reportsCommon);
		}

		return result;
	}

	protected boolean isAuthorizedWithRoles(ReportsCommon reportCommon) {
		boolean result = false;

		ForRoles forRolesList = reportCommon.getForRoles();
		if (forRolesList != null) {
			AccountResource accountResource = new AccountResource();
			List<String> roleDisplayNameList = accountResource.getAccountRoleDisplayNames(AuthN.get().getUserId(), AuthN.get().getCurrentTenantId());
			for (String target : forRolesList.getRoleDisplayName()) {
				if (Tools.listContainsIgnoreCase(roleDisplayNameList, target)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

}
