package org.collectionspace.services.common;

//import java.io.*;
import javax.servlet.http.HttpServletRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;

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
						logger.warn(FILE_FORM_FIELD + ": part is marked as a form field.");
					}
					String fileName = item.getName();
					if (logger.isTraceEnabled() == true) {
						logger.trace("Uploaded File Name:" + (fileName != null ? fileName : "<empty>"));
					}
					if (fileName == null) {
						fileName = DEFAULT_BLOB_NAME; //if there's no file name then set it to an empty string
						logger.warn("File was posted to the services without a file name.");
					}					
					File tmpDir = new File(System.getProperty("java.io.tmpdir"));
					File savedFile = File.createTempFile(TMP_BLOB_PREFIX, fileName, tmpDir);

					item.write(savedFile);
//						item.getInputStream();//FIXME: REM - We should make a version of this method that returns the input stream
					result = savedFile;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
	}
}
