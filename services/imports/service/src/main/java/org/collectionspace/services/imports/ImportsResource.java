/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

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
package org.collectionspace.services.imports;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ConfigurationException;
import org.collectionspace.services.common.FileUtilities;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.api.ZipTools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.imports.ImportsCommon;
import org.collectionspace.services.imports.nuxeo.ImportCommand;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

// The modified Nuxeo ImportCommand from nuxeo's shell:

/**
 * @author Laramie Crocker
 */
@Path(ImportsResource.SERVICE_PATH)
@Produces({ "application/xml" })
@Consumes({ "application/xml" })
public class ImportsResource extends AbstractCollectionSpaceResourceImpl<PoxPayloadIn, PoxPayloadOut> {

	private final static Logger logger = LoggerFactory.getLogger(ImportsResource.class);

	public static final String SERVICE_NAME = "imports";
	public static final String SERVICE_PATH = "/" + SERVICE_NAME;
    private static String NUXEO_SPACES_PATH_DELIMITER = "/";

	/*
	 * ASSUMPTION: All Nuxeo services of a given tenancy store their stuff in
	 * a repository domain, under a "Workspaces" space within that domain.
         * (See http://doc.nuxeo.com/display/USERDOC/Document+Management+concepts)
	 *
	 * Using the tenant associated with the currently authenticated user, this
	 * method returns a delimited path to the Workspaces space for that tenancy.
	 */
	private static String getWorkspacesPath() throws ConfigurationException {
		String result = null;
		List<RepositoryDomainType> repositoryDomainList = getRepositoryDomainList();
		if (repositoryDomainList.size() == 1) {
			String domainName = repositoryDomainList.get(0).getStorageName()
					.trim();
			result = NUXEO_SPACES_PATH_DELIMITER + domainName
                                + NUXEO_SPACES_PATH_DELIMITER + NuxeoUtils.Workspaces;
		} else {
                        // Currently, the Imports service places all documents
                        // imported via a single request, into the same repository
                        // and repository domain. It doesn't currently have the
                        // ability to assign individual documents, depending on
                        // their associated service, to different repositories
                        // and/or repository domains. If a tenant is configured with
                        // more than one repository domain, the import will fail here.
			throw new ConfigurationException(
					"Tenant bindings contains 0 or more than 1 repository domains.");
		}
		return result;
	}

        private static String getRepoName() throws ConfigurationException {
		String repoName = null;
		List<RepositoryDomainType> repositoryDomainList = getRepositoryDomainList();
		if (repositoryDomainList.size() == 1) {
	            repoName = repositoryDomainList.get(0).getRepositoryName().trim();
		} else {
                        // See relevant comments in getWorkspacesPath()
			throw new ConfigurationException(
					"Tenant bindings contains 0 or more than 1 repository domains.");
		}
		return repoName;
	}

        private static List<RepositoryDomainType> getRepositoryDomainList() throws ConfigurationException {
            TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance()
				.getTenantBindingConfigReader();
		TenantBindingType tenantBinding = tReader.getTenantBinding(AuthN.get()
				.getCurrentTenantId());
		List<RepositoryDomainType> repositoryDomainList = tenantBinding
				.getRepositoryDomain();
                return repositoryDomainList;
        }


	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	protected String getVersionString() {
		final String lastChangeRevision = "$LastChangedRevision: 2108 $";
		return lastChangeRevision;
	}

	@Override
	public Class<?> getCommonPartClass() {
		return ImportsCommon.class;
	}

	@Override
	public ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> getServiceContextFactory() {
		return MultipartServiceContextFactory.get();
	}

	private static String _templateDir = null;

	public static String getTemplateDir() throws FileNotFoundException {
		if (_templateDir == null) {
			TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance()
					.getTenantBindingConfigReader();
			_templateDir = tReader.getResourcesDir() + File.separator
					+ "templates";
		}

		File templateDir = new File(_templateDir);  // We need to make sure the 'templates' directory is not missing
		if (templateDir.exists() == false) {
			throw new FileNotFoundException("The Import service's template directory is missing: " + _templateDir);
		}
		return _templateDir;
	}

	// @POST
	// public Response create(@Context UriInfo ui, String xmlPayload) {
	// try {
	// return this.create(xmlPayload);
	// } catch (Exception e) {
	// throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
	// }
	// }

	/**
	 * you can test this with something like: curl -X POST
	 * http://localhost:8180/cspace-services/imports -i -u
	 * "Admin@collectionspace.org:Administrator" -H
	 * "Content-Type: application/xml" -T in.xml -T
	 * /src/trunk/services/imports/service
	 * /src/main/resources/templates/authority-request.xml
	 */
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response create(@Context UriInfo ui,
			String xmlPayload) {
		String result = null;
		ResponseBuilder rb = Response.serverError(); // Assume we'll fail to successfully fulfill the request.

		try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
			int timeout = ctx.getTimeoutSecs(); // gets it from query param 'impTimout' or uses default if no query param specified

			// InputSource inputSource = payloadToInputSource(xmlPayload);
			// result = createFromInputSource(inputSource);
			String inputFilename = payloadToFilename(xmlPayload);
			result = createFromFilename(inputFilename, timeout);
			if (result.contains("ERROR") == false) {
				rb = Response.ok(); // SUCCESS
			}
	        rb.entity(result);
		} catch (Exception e) {
	        result = e.getMessage();
	        logger.error(result);
		}

        return rb.build();
	}

	public static String createFromInputSource(InputSource inputSource,
			int timeOut) throws Exception {
		String tenantId = AuthN.get().getCurrentTenantId();
		// We must expand the request and wrap it with all kinds of Nuxeo
		// baggage, which expandXmlPayloadToDir knows how to do.
		String outputDir = FileTools.createTmpDir("imports-")
				.getCanonicalPath();
		File outpd = new File(outputDir);
		outpd.mkdirs();
		expandXmlPayloadToDir(tenantId, inputSource, getTemplateDir(),
				outpd.getCanonicalPath());

		// Next, call the nuxeo import service, pointing it to our local
		// directory that has the expanded request.
		ImportCommand importCommand = new ImportCommand();
		// String destWorkspaces = "/default-domain/workspaces";
		String workspacesPath = getWorkspacesPath();
                String repoName = getRepoName();
		String result = "";
		try {
			String report = "NORESULTS";
			report = importCommand.run(outputDir, repoName, workspacesPath, timeOut);
			result = "<?xml version=\"1.0\"?><import><msg>SUCCESS</msg>"
					+ report + "</import>";
		} catch (Exception e) {
			result = "<?xml version=\"1.0\"?><import><msg>ERROR</msg><report>"
					+ e.getMessage() + "</report></import>";
		}
		return result;
	}

	public static String createFromFilename(String filename, int timeOut)
			throws Exception {
		String tenantId = AuthN.get().getCurrentTenantId();
		// We must expand the request and wrap it with all kinds of Nuxeo
		// baggage, which expandXmlPayloadToDir knows how to do.
		String outputDir = FileTools.createTmpDir("imports-")
				.getCanonicalPath();
		File outpd = new File(outputDir);
		outpd.mkdirs();
		expandXmlPayloadToDir(tenantId, filename, getTemplateDir(),
				outpd.getCanonicalPath());

		// Next, call the nuxeo import service, pointing it to our local
		// directory that has the expanded request.
		ImportCommand importCommand = new ImportCommand();
		// String destWorkspaces = "/default-domain/workspaces";
		String workspacesPath = getWorkspacesPath();
                String repoName = getRepoName();
		String result = "";
		try {
			String report = "NORESULTS";
			report = importCommand.run(outputDir, repoName, workspacesPath, timeOut);
			result = "<?xml version=\"1.0\"?><import><msg>SUCCESS</msg>"
					+ report + "</import>";
		} catch (Exception e) {
			result = "<?xml version=\"1.0\"?><import><msg>ERROR</msg><report>"
					+ e.getMessage() + "</report></import>";
		}
		return result;
	}

	/**
	 * @param xmlPayload
	 *            A request file has a specific format, you can look at:
	 *            trunk/services
	 *            /imports/service/src/test/resources/requests/authority
	 *            -request.xml
	 */
	public static InputSource payloadToInputSource(String xmlPayload)
			throws Exception {
		xmlPayload = encodeAmpersands(xmlPayload);
		String requestDir = FileTools.createTmpDir("imports-request-")
				.getCanonicalPath();
		File requestFile = FileTools.saveFile(requestDir, "request.xml",
				xmlPayload, FileTools.FORCE_CREATE_PARENT_DIRS,
				FileTools.UTF8_ENCODING);
		if (requestFile == null) {
			throw new FileNotFoundException(
					"Could not create file in requestDir: " + requestDir);
		}
		String requestFilename = requestFile.getCanonicalPath();
		InputSource inputSource = new InputSource(requestFilename);
                if (logger.isTraceEnabled()) {
                    logger.trace("############## REQUEST_FILENAME: "
				+ requestFilename);
                }
		return inputSource;
	}

	public static String payloadToFilename(String xmlPayload) throws Exception {
		xmlPayload = encodeAmpersands(xmlPayload);
		String requestDir = FileTools.createTmpDir("imports-request-")
				.getCanonicalPath();
		File requestFile = FileTools.saveFile(requestDir, "request.xml",
				xmlPayload, FileTools.FORCE_CREATE_PARENT_DIRS,
				FileTools.UTF8_ENCODING);
		if (requestFile == null) {
			throw new FileNotFoundException(
					"Could not create file in requestDir: " + requestDir);
		}
		String requestFilename = requestFile.getCanonicalPath();
                if (logger.isTraceEnabled()) {
		    logger.trace("############## REQUEST_FILENAME: "
				+ requestFilename);
                }
		return requestFilename;
	}

	/**
	 * Encodes each ampersand ('&') in the incoming XML payload by replacing it
	 * with the predefined XML entity for an ampersand ('&amp;').
	 *
	 * This is a workaround for the issue described in CSPACE-3911. Its intended
	 * effect is to have these added ampersand XML entities being resolved to
	 * 'bare' ampersands during the initial parse, thus preserving any XML
	 * entities in the payload, which will then be resolved correctly during the
	 * second parse.
	 *
	 * (This is not designed to compensate for instances where the incoming XML
	 * payload contains 'bare' ampersands - that is, used in any other context
	 * than as the initial characters in XML entities. In those cases, the
	 * payload may not be a legal XML document.)
	 *
	 * @param xmlPayload
	 * @return The original XML payload, with each ampersand replaced by the
	 *         predefined XML entity for an ampersand.
	 */
	private static String encodeAmpersands(String xmlPayload) {
		return xmlPayload.replace("&", "&amp;");
	}

	public static void expandXmlPayloadToDir(String tenantId,
			String inputFilename, String templateDir, String outputDir)
			throws Exception {
                if (logger.isTraceEnabled()) {
                    logger.trace("############## TEMPLATE_DIR: " + templateDir);
                    logger.trace("############## OUTPUT_DIR:" + outputDir);
                }
		File file = new File(inputFilename);
		FileInputStream is = new FileInputStream(file);
		InputSource inputSource = new InputSource(is);
		TemplateExpander.expandInputSource(tenantId, templateDir, outputDir,
				inputSource, "/imports/import");
	}

	/**
	 * This method may be called statically from outside this class; there is a
	 * test call in org.collectionspace.services.test.ImportsServiceTest
	 *
	 * @param inputSource
	 *            A wrapper around a request file, either a local file or a
	 *            stream; the file has a specific format, you can look at:
	 *            trunk/services/imports/service/src/test/resources/requests/
	 *            authority-request.xml
	 * @param templateDir
	 *            The local directory where templates are to be found at
	 *            runtime.
	 * @param outputDir
	 *            The local directory where expanded files and directories are
	 *            found, ready to be passed to the Nuxeo importer.
	 */
	public static void expandXmlPayloadToDir(String tenantId,
			InputSource inputSource, String templateDir, String outputDir)
			throws Exception {
                if (logger.isTraceEnabled()) {
                    logger.trace("############## TEMPLATE_DIR: " + templateDir);
                    logger.trace("############## OUTPUT_DIR:" + outputDir);
                }
		TemplateExpander.expandInputSource(tenantId, templateDir, outputDir,
				inputSource, "/imports/import");
		// TemplateExpander.expandInputSource(templateDir, outputDir,
		// inputFilename, "/imports/import");
	}

	/**
	 * you can test like this: curl -F "file=@out.zip;type=application/zip"
	 * --basic -u "admin@core.collectionspace.org:Administrator"
	 * http://localhost:8180/cspace-services/imports
	 */
	@POST
	@Consumes("multipart/form-data")
	@Produces("application/xml")
	public Response acceptUpload(@Context UriInfo ui,
			@Context HttpServletRequest req, MultipartFormDataInput partFormData) {
		Response response = null;
		StringBuffer resultBuf = new StringBuffer();

		try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
			int timeout = ctx.getTimeoutSecs(); // gets it from query param 'impTimout' or uses default if no query param specified

			InputStream fileStream = null;
			String preamble = partFormData.getPreamble();
			logger.trace("Preamble type is:" + preamble);

			Map<String, List<InputPart>> partsMap = partFormData.getFormDataMap();
			List<InputPart> fileParts = partsMap.get("file");
			for (InputPart part : fileParts) {
				String mediaType = part.getMediaType().toString();
				logger.trace("Media type is:" + mediaType);
				if (mediaType.equalsIgnoreCase(MediaType.APPLICATION_XML)
						|| mediaType.equalsIgnoreCase(MediaType.TEXT_XML)) {
					// FIXME For an alternate approach, potentially preferable,
					// see:
					// http://stackoverflow.com/questions/4586222/right-way-of-formatting-an-input-stream
					String str = encodeAmpersands(part.getBodyAsString());
					InputStream stream = new ByteArrayInputStream(
							str.getBytes("UTF8"));
					InputSource inputSource = new InputSource(stream);
					// InputSource inputSource = new
					// InputSource(part.getBody(InputStream.class, null));
					String result = createFromInputSource(inputSource, timeout);
					resultBuf.append(result);
					continue;
				}

				//
				// TODO: This code was never finished to support the import of a zipped directory
				//
				if (mediaType.equalsIgnoreCase("application/zip")) {
					logger.error("The Import service does not yet support .zip files."); // We should also send back a meaningful error message and status code here.

					fileStream = part.getBody(InputStream.class, null);

					File zipfile = FileUtilities.createTmpFile(fileStream,
							getServiceName() + "_");
					String zipfileName = zipfile.getCanonicalPath();
					logger.trace("Imports zip file saved to:" + zipfileName);

					String baseOutputDir = FileTools.createTmpDir("imports-")
							.getCanonicalPath();
					File indir = new File(baseOutputDir + "/in");
					indir.mkdir();
					ZipTools.unzip(zipfileName, indir.getCanonicalPath());
                    String result = "\r\n<zipResult>Zipfile " + zipfileName
                            + "extracted to: " + indir.getCanonicalPath()
                            + "</zipResult>";
                    logger.trace(result);

					// TODO: now call import service...
					resultBuf.append(result);
					continue;
				}
			}

			Response.ResponseBuilder rb = Response.ok();
			rb.entity(resultBuf.toString());
			response = rb.build();
		} catch (Exception e) {
			throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
		}

		return response;
	}

	String page = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
			+ "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>\n"
			+ "  <head>\n"
			+ "    <title>CollectionSpace Import</title>\n"
			+ "    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />\n"
			+ "    <meta http-equiv='Accept' content='multipart/form-data,application/xml,text/xml' />\n"
			+ "    <meta http-equiv='Accept-Charset' content='utf-8' />\n"
			+ "  </head>\n"
			+ "  <body>\n"
			+ "    <form enctype='multipart/form-data' accept-charset='utf-8' \n"
			+ "        action='/cspace-services/imports?type=xml' method='post'>\n"
			+ "      Choose a file to import:"
			+ "      <input name='file' type='file' accept='application/xml,text/xml' />\n"
			+ "      <br />\n"
			+ "      <input type='submit' value='Upload File' />\n"
			+ "    </form>\n"
                        + "  </body>\n"
                        + "</html>\n";

	@GET
	@Produces("text/html")
	public String getInputForm(@QueryParam("form") String form) {
		return page;
	}
}
