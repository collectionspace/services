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

import org.collectionspace.services.common.FileUtils;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.api.ZipTools;

// The modified Nuxeo ImportCommand from nuxeo's shell:
import org.collectionspace.services.imports.nuxeo.ImportCommand;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Laramie Crocker
 */
@Path(ImportsResource.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class ImportsResource extends ResourceBase {
    
    public static final String SERVICE_PATH = "imports";
    public static final String SERVICE_NAME = "imports";
    
    @Override
    public String getServiceName(){
        return SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    //public Class<ImportsCommon> getCommonPartClass() {
    public Class getCommonPartClass() {
    	try {
            return Class.forName("org.collectionspace.services.imports.ImportsCommon");//.class;
        } catch (ClassNotFoundException e){
            return null;
        }
    }


    /* KRUFT:

      1) here is how you can deal with poxpayloads:
  	        //PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
        	//ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
      2) here are some notes:
            //First, save the import request to a local file.
            // It may be huge. To accept a stream, send it as an upload request; see acceptUpload()
      3) useful for debugging:
              System.out.println("\r\n\r\n\r\n=====================\r\n   RUNNING create with xmlPayload: \r\n"+xmlPayload);
    */


    public static final String TEMPLATE_DIR = "/src/trunk/services/imports/service/src/main/resources/templates";

    /** you can test this with something like:
     * curl -X POST http://localhost:8180/cspace-services/imports -i  -u "Admin@collectionspace.org:Administrator" -H "Content-Type: application/xml" -T in.xml
     * -T /src/trunk/services/imports/service/src/main/resources/templates/authority-request.xml
     */
    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public javax.ws.rs.core.Response create(String xmlPayload) {
        String result;
        javax.ws.rs.core.Response.ResponseBuilder rb;
        try {
            InputSource inputSource = payloadToInputSource(xmlPayload);
            result = createFromInputSource(inputSource);
            rb = javax.ws.rs.core.Response.ok();
	    } catch (Exception e) {
            result = Tools.errorToString(e, true);
            rb = javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
        rb.entity(result);
        return rb.build();
    }

    public static String createFromInputSource(InputSource inputSource) throws Exception {
        // We must expand the request and wrap it with all kinds of Nuxeo baggage, which expandXmlPayloadToDir knows how to do.
        String outputDir = FileTools.createTmpDir("imports-").getCanonicalPath();
        expandXmlPayloadToDir(inputSource, TEMPLATE_DIR, outputDir);

        // Next, call the nuxeo import service, pointing it to our local directory that has the expanded request.
        ImportCommand importCommand = new ImportCommand();
        String destWorkspaces = "/default-domain/workspaces";
        String report = importCommand.run(outputDir, destWorkspaces);
        String result = "<?xml ?><import><msg>SUCCESS</msg><report></report>"+report+"</import>";
        return result;
    }

    /**  @param xmlPayload   A request file has a specific format, you can look at:
     *      trunk/services/imports/service/src/test/resources/requests/authority-request.xml
     */
    public static InputSource payloadToInputSource(String xmlPayload) throws Exception {
        String requestDir = FileTools.createTmpDir("imports-request-").getCanonicalPath();
        File requestFile = FileTools.saveFile(requestDir, "request.xml", xmlPayload, true);
        if (requestFile == null){
            throw new FileNotFoundException("Could not create file in requestDir: "+requestDir);
        }
        String requestFilename = requestFile.getCanonicalPath();
        InputSource inputSource = new InputSource(requestFilename);
        System.out.println("############## REQUEST_FILENAME: "+requestFilename);
        return inputSource;
    }

    /** This method may be called statically from outside this class; there is a test call in
     *   org.collectionspace.services.test.ImportsServiceTest
     *
     * @param inputSource   A wrapper around a request file, either a local file or a stream;
     *      the file has a specific format, you can look at:
     *      trunk/services/imports/service/src/test/resources/requests/authority-request.xml
     * @param templateDir  The local directory where templates are to be found at runtime.
     * @param outputDir    The local directory where expanded files and directories are found, ready to be passed to the Nuxeo importer.
     */
    public static void expandXmlPayloadToDir(InputSource inputSource, String templateDir, String outputDir) throws Exception {
        System.out.println("############## TEMPLATE_DIR: "+templateDir);
        System.out.println("############## OUTPUT_DIR:"+outputDir);
        TemplateExpander.expandInputSource(templateDir, outputDir, inputSource, "/imports/import");
    }

    /** you can test like this:
     * curl -F "file=@out.zip;type=application/zip" --basic -u "Admin@collectionspace.org:Administrator" http://localhost:8280/cspace-services/imports
     */
    @POST
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public javax.ws.rs.core.Response acceptUpload(@Context HttpServletRequest req,
    		                                   MultipartFormDataInput partFormData) {
    	javax.ws.rs.core.Response response = null;
        StringBuffer resultBuf = new StringBuffer();
    	try {
    		InputStream fileStream = null;
    		String preamble = partFormData.getPreamble();
    		System.out.println("Preamble type is:" + preamble);
    		Map<String, List<InputPart>> partsMap = partFormData.getFormDataMap();
    		List<InputPart> fileParts = partsMap.get("file");
    		for (InputPart part : fileParts){
                String mediaType = part.getMediaType().toString();
                System.out.println("Media type is:" + mediaType);
                if (mediaType.equalsIgnoreCase("text/xml")){
                    InputSource inputSource = new InputSource(part.getBody(InputStream.class, null));
                    String result = createFromInputSource(inputSource);
                    resultBuf.append(result);
                    continue;
                }
    			if (mediaType.equalsIgnoreCase("application/zip")){
                    fileStream = part.getBody(InputStream.class, null);

                    File zipfile = FileUtils.createTmpFile(fileStream, getServiceName() + "_");
                    String zipfileName = zipfile.getCanonicalPath();
                    System.out.println("Imports zip file saved to:" + zipfileName);

                    String baseOutputDir = FileTools.createTmpDir("imports-").getCanonicalPath();
                    File indir = new File(baseOutputDir+"/in");
                    indir.mkdir();
                    ZipTools.unzip(zipfileName, indir.getCanonicalPath());
                    String result = "\r\n<zipResult>Zipfile " + zipfileName + "extracted to: " + indir.getCanonicalPath()+"</zipResult>";
                    System.out.println(result);

                    long start = System.currentTimeMillis();
                    //TODO: now call import service...
                    resultBuf.append(result);
                    continue;
                }
    		}
	    	javax.ws.rs.core.Response.ResponseBuilder rb = javax.ws.rs.core.Response.ok();
	    	rb.entity(resultBuf.toString());
	    	response = rb.build();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
		return response;
    }

    String page = "<html><body><form enctype='multipart/form-data' action='/cspace-services/imports?type=xml' method='POST'>"
                + "Choose a file to import: <input name='file' type='file' /><br /><input type='submit' value='Upload File' /></form></body></html>";
    @GET
    @Produces("text/html")
	public String getInputForm(@QueryParam("form") String form) {
        return page;
	}
}
