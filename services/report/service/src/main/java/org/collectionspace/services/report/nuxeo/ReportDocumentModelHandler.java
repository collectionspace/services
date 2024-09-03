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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.jasperreports.engine.JRBreak;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.account.AccountResource;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMain;
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
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.report.MIMEType;
import org.collectionspace.services.report.MIMETypeItemType;
import org.collectionspace.services.report.ReportResource;
import org.collectionspace.services.report.ReportsCommon;
import org.collectionspace.services.report.ReportsCommon.ForRoles;
import org.collectionspace.services.report.ReportsOuputMimeList;
import org.collectionspace.services.report.ResourceActionGroup;
import org.collectionspace.services.report.ResourceActionGroupList;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
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
		if (!isAuthorized(reportsCommon)) {
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
				for (String csidItem : csids) {
					if (first) {
						first = false;
					} else {
						sb.append(CSID_LIST_SEPARATOR);
					}
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
				String invDocType = invContext.getDocType();
				List<String> forDocTypeList = (List<String>) NuxeoUtils.getProperyValue(docModel, InvocableJAXBSchema.FOR_DOC_TYPES);

				if (forDocTypeList == null) {
					forDocTypeList = new ArrayList<String>();
				}

				if (Invocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
					// In group mode, allow the context doc type to be Group or null, even if the report wasn't registered
					// with those types.

					forDocTypeList.add("Group");
					forDocTypeList.add(null);
				}

				if (!forDocTypeList.contains(invDocType)) {
					throw new BadRequestException(
						"ReportResource: Invoked with unsupported document type: "
						+invDocType);
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

	private InputStream buildReportResult(
		String reportCSID,
		HashMap<String, Object> params,
		String reportFileName,
		String outputMimeType,
		StringBuffer outReportFileName) throws Exception {

		try (Connection conn = getConnection()) {
			String reportName = Tools.getFilenameBase(reportFileName);
			File reportCompiledFile = ReportResource.getReportCompiledFile(reportName);

			if (!reportCompiledFile.exists()) {
				// Need to compile the file.

				File reportSourceFile = ReportResource.getReportSourceFile(reportName);

				if (!reportSourceFile.exists()) {
					logger.error("Report for csid={} is missing source file: {}",
								 reportCSID, reportSourceFile.getAbsolutePath());

					throw new RuntimeException("Report is missing source file");
				}

				logger.info("Report for csid={} is not compiled. Compiling first, and saving to: {}",
							reportCSID, reportCompiledFile.getAbsolutePath());

				JasperDesign design = JRXmlLoader.load(reportSourceFile.getAbsolutePath());

				design.setScriptletClass("org.collectionspace.services.report.jasperreports.CSpaceReportScriptlet");

				JasperCompileManager.compileReportToFile(design, reportCompiledFile.getAbsolutePath());
			}

			logger.trace("ReportResource for csid={} output as {} using report file: {}", reportCSID, outputMimeType,
						 reportCompiledFile.getAbsolutePath());

			FileInputStream fileStream = new FileInputStream(reportCompiledFile);
			// Report will be to a temporary file.
			File tempOutputFile = Files.createTempFile("report-", null).toFile();
			FileOutputStream tempOutputStream = new FileOutputStream(tempOutputFile);

			// Strip extension from report filename.
			String outputFilename = reportFileName;
			// Strip extension from report filename.
			int idx = outputFilename.lastIndexOf(".");
			if (idx > 0) {
				outputFilename = outputFilename.substring(0, idx);
			}
			// Strip any sub-dir from report filename.
			idx = outputFilename.lastIndexOf(File.separator);
			if (idx > 0) {
				outputFilename = outputFilename.substring(idx + 1);
			}

			Exporter exporter;
			switch (outputMimeType) {
				case MediaType.APPLICATION_XML:
					params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
					exporter = xmlExporter(tempOutputStream);
					outputFilename = outputFilename + ".xml";
					break;
				case MediaType.TEXT_HTML:
					exporter = htmlExporter(tempOutputStream);
					outputFilename = outputFilename + ".html";
					break;
				case ReportClient.PDF_MIME_TYPE:
					exporter = pdfExporter(tempOutputStream);
					outputFilename = outputFilename + ".pdf";
					break;
				case ReportClient.CSV_MIME_TYPE:
					params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
					exporter = csvExporter(tempOutputStream);
					outputFilename = outputFilename + ".csv";
					break;
				case ReportClient.TSV_MIME_TYPE:
					params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
					exporter = tsvExporter(tempOutputStream);
					outputFilename = outputFilename + ".csv";
					break;
				case ReportClient.MSWORD_MIME_TYPE:
				case ReportClient.OPEN_DOCX_MIME_TYPE:
					exporter = docxExporter(tempOutputStream);
					outputFilename = outputFilename + ".docx";
					break;
				case ReportClient.MSEXCEL_MIME_TYPE:
				case ReportClient.OPEN_XLSX_MIME_TYPE:
					params.put(JRParameter.IS_IGNORE_PAGINATION, true);
					params.put(JRBreak.PROPERTY_PAGE_BREAK_NO_PAGINATION, JRBreak.PAGE_BREAK_NO_PAGINATION_APPLY);
					params.put(JRTextElement.PROPERTY_PRINT_KEEP_FULL_TEXT, true);
					exporter = xlsxExporter(tempOutputStream, outputFilename);
					outputFilename = outputFilename + ".xlsx";
					break;
				case ReportClient.MSPPT_MIME_TYPE:
				case ReportClient.OPEN_PPTX_MIME_TYPE:
					exporter = pptxExporter(tempOutputStream);
					outputFilename = outputFilename + ".pptx";
					break;
				default:
					logger.error("Reporting: unsupported output MIME type - defaulting to PDF");
					exporter = pdfExporter(tempOutputStream);
					outputFilename = outputFilename + "-default-to.pdf";
					break;
			}

			outReportFileName.append(outputFilename); // Set the outgoing param to the report's final file name

			// fill the report
			JasperPrint jasperPrint = JasperFillManager.fillReport(fileStream, params, conn);
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.exportReport();
			tempOutputStream.close();

			return new FileInputStream(tempOutputFile);
		} catch (SQLException sqle) {
			logger.error("SQL Exception in report {}", reportCSID, sqle);
			Response response = Response.serverError()
				.entity("Invoke failed (SQL problem) on Report csid=" + reportCSID)
				.type("text/plain").build();
			throw new CSWebApplicationException(sqle, response);
		} catch (JRException jre) {
			logger.error("JasperReports Exception: {} Cause: {}", jre.getLocalizedMessage(), jre.getCause());
			Response response = Response.serverError()
				.entity("Invoke failed (Jasper problem) on Report csid=" + reportCSID)
				.type("text/plain").build();
			throw new CSWebApplicationException(jre, response);
		} catch (FileNotFoundException fnfe) {
			logger.error("FileNotFoundException: {}", fnfe.getLocalizedMessage());
			Response response = Response.serverError()
				.entity("Invoke failed (FileNotFound) on Report csid=" + reportCSID)
				.type("text/plain").build();
			throw new CSWebApplicationException(fnfe, response);
		}
	}

	private JRPptxExporter pptxExporter(FileOutputStream outputStream) {
		JRPptxExporter exporter = new JRPptxExporter();
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
		return exporter;
	}

	private JRXlsxExporter xlsxExporter(FileOutputStream outputStream, String outputFilename) {
		JRXlsxExporter exporter = new JRXlsxExporter();
		SimpleXlsxReportConfiguration reportConfig = new SimpleXlsxReportConfiguration();
		reportConfig.setCollapseRowSpan(true);
		reportConfig.setDetectCellType(true);
		reportConfig.setRemoveEmptySpaceBetweenRows(true);
		reportConfig.setRemoveEmptySpaceBetweenColumns(true);
		reportConfig.setOnePagePerSheet(false);
		reportConfig.setFontSizeFixEnabled(false);
		reportConfig.setWhitePageBackground(false);
		reportConfig.setFreezeRow(2);
		reportConfig.setSheetNames(new String[] {outputFilename});

		exporter.setConfiguration(reportConfig);
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
		return exporter;
	}

	private JRDocxExporter docxExporter(FileOutputStream outputStream) {
		JRDocxExporter exporter = new JRDocxExporter();
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
		return exporter;
	}

	private JRCsvExporter tsvExporter(FileOutputStream outputStream) {
		JRCsvExporter exporter = new JRCsvExporter();
		final SimpleCsvExporterConfiguration exportConfig = new SimpleCsvExporterConfiguration();
		exportConfig.setFieldDelimiter("\t");
		exporter.setConfiguration(exportConfig);
		exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
		return exporter;
	}

	private JRCsvExporter csvExporter(FileOutputStream outputStream) {
		JRCsvExporter exporter = new JRCsvExporter();
		exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
		return exporter;
	}

	private JRPdfExporter pdfExporter(FileOutputStream outputStream) {
		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
		return exporter;
	}

	private HtmlExporter htmlExporter(FileOutputStream outputStream) {
		HtmlExporter exporter = new HtmlExporter();
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
		return exporter;
	}

	private JRXmlExporter xmlExporter(FileOutputStream outputStream) {
		JRXmlExporter exporter = new JRXmlExporter();
		exporter.setExporterOutput(new SimpleXmlExporterOutput(outputStream));
		return exporter;
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
			logger.error("Error getting database connection", e);
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
	protected boolean isAuthorizedWithPermissions(ReportsCommon reportsCommon) {
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
	protected boolean isAuthorized(ReportsCommon reportsCommon) {
		boolean result = true;

		if (hasRequiredRoles(reportsCommon)) {
			result = isAuthorizedWithRoles(reportsCommon);
		} else if (hasRequiredPermissions(reportsCommon)) {
			result = isAuthorizedWithPermissions(reportsCommon);
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
