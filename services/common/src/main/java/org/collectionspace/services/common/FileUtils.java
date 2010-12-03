package org.collectionspace.services.common;

//import java.io.*;
import javax.servlet.http.HttpServletRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import java.io.IOException;
import java.io.File;

//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	static public File createTmpFile(HttpServletRequest request) {
		File result = null;
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		try {
			List  items = upload.parseRequest(request);
			Iterator iter = items.iterator();

			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();

				if (item.isFormField()) {
					if (logger.isDebugEnabled() == true) {
						String formFieldName = item.getFieldName();
						logger.debug("FORM FIELD:" + formFieldName);
					}
				} else {
					if (!item.isFormField()) {

						String fileName = item.getName();
						System.out.println("File Name:" + fileName);

						File fullFile  = new File(item.getName());
						String tmpDir = System.getProperty("java.io.tmpdir");
						File savedFile = new File(tmpDir, fullFile.getName());

						item.write(savedFile);
						result = savedFile;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
	}
}
