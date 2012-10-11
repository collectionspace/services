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
package org.collectionspace.services.common;

//import java.io.*;
import javax.servlet.http.HttpServletRequest;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//import java.io.IOException;

//import java.util.UUID;
//import java.util.regex.Pattern;
//import java.util.regex.Matcher;

//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FileUtils.
 */
public class FileUtils {
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	/** The Constant TMP_FILE_PREFIX. */
	public static final String TMP_BLOB_PREFIX = "cspace_blob_";
	public static final String DEFAULT_BLOB_NAME = "blob";
	private static final String FILE_FORM_FIELD = "file";
	
	/**
	 * Creates the tmp file.
	 *
	 * @param streamIn the stream in
	 * @param filePrefix the file prefix
	 * @return the file
	 */
	static public File createTmpFile(InputStream streamIn,
			String filePrefix) {
		File result = null;
		
		filePrefix = filePrefix != null ? filePrefix : "";
		String tmpDir = System.getProperty("java.io.tmpdir");
		result = new File(tmpDir, filePrefix + UUID.randomUUID().toString());
		if (logger.isDebugEnabled() == true) {
			logger.debug("Creating temp file at:" + result.getAbsolutePath());
		}
		
		try {
	        FileOutputStream streamOut = new FileOutputStream(result);
			int c;
	        while ((c = streamIn.read()) != -1) 
	        {
	           streamOut.write(c);
	        }
	
	        streamIn.close();
	        streamOut.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
	}
	
	static public String getMimeType(File file) {
		String result = null;
		
		Magic parser = new Magic() ;
		// getMagicMatch accepts Files or byte[],
		// which is nice if you want to test streams
		MagicMatch match = null;
		try {
			match = parser.getMagicMatch(file, true);
		} catch (MagicParseException e) {
			logger.debug("MagicParseException encountered trying to get MIME type for "
					+ file.getAbsolutePath(), e);
		} catch (MagicMatchNotFoundException e) {
			logger.debug("MagicMatchNotFoundException encountered trying to get MIME type for "
					+ file.getAbsolutePath(), e);
		} catch (MagicException e) {
			logger.debug("MagicException encountered trying to get MIME type for "
					+ file.getAbsolutePath(), e);
		}
		
		if (match != null) {
			result = match.getMimeType();
		}
		
		return result;
	}
	
	/**
	 * Look for an uploaded file from the HTTP request of type "multipart/form-data".
	 *
	 * @param request the request
	 * @return the file
	 */
	static public File createTmpFile(HttpServletRequest request) {
		File result = null;
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		try {
			List<FileItem>  items = upload.parseRequest(request);
			Iterator<FileItem> iter = items.iterator();

			while (iter.hasNext()) {
				FileItem item = iter.next();
				String formFieldName = item.getFieldName();
				if (logger.isTraceEnabled() == true) {
					logger.trace("HTTP Request form field:" + formFieldName);
				}
				if (formFieldName.equalsIgnoreCase(FILE_FORM_FIELD)) {
					if (item.isFormField() == true) {
						logger.warn("Form field '" + FILE_FORM_FIELD + "': part is marked as a form field.");
					}
					String fileName = item.getName();
					if (logger.isTraceEnabled() == true) {
						logger.trace("Uploaded File Name:" + (fileName != null ? fileName : "<empty>"));
					}
					if (fileName == null) {
						fileName = DEFAULT_BLOB_NAME; //if there's no file name then set it to an empty string
						logger.warn("File was posted to the services without a file name.");
					}
					//
					// To avoid name collisions and to preserve the posted file name, create a temp directory for the
					// file.
					//
					File tmpDir = new File(System.getProperty("java.io.tmpdir"));
					String fileTmpDirPath = tmpDir.getPath() + File.separatorChar + UUID.randomUUID();
					File fileTmpDir = new File(fileTmpDirPath);
					File savedFile = null;
					if (fileTmpDir.mkdir() == true) {
						savedFile = new File(fileTmpDirPath + File.separatorChar + fileName);
						if (savedFile.createNewFile() == false) {
							savedFile = null;
						}
					}
					
					if (savedFile != null) {
						item.write(savedFile);
//						item.getInputStream();//FIXME: REM - We should make a version of this method that returns the input stream
						result = savedFile;
					} else {
						logger.error("Could not create temporary file: " + fileTmpDirPath +
								File.separatorChar + fileName);						
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
	}

}
