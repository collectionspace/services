package org.collectionspace.services.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownloadUtility {
    private static final Logger logger = LoggerFactory.getLogger(HttpDownloadUtility.class);

	private static final int BUFFER_SIZE = 4096;

    private static String getDestDir(String destDir) {
        if (destDir.endsWith(File.separator) != true) {
        	destDir = destDir + File.separator;
        }
        //
        // Check if the destDir exists, if not try to create it.
        //
        File destDirFile = new File(destDir);
        boolean destDirExists = destDirFile.exists();
        boolean triedToCreateDir = false;
        if (destDirExists == false) {
        	triedToCreateDir = true;
        	destDirExists = destDirFile.mkdir();
        } else {
        	destDirExists = destDirFile.isDirectory();
        }
        //
        // If we couldn't create the directory or if it is already a file then fail.
        //
        if (destDirExists == false) {
        	if (triedToCreateDir) {
        		logger.error(String.format("Tried and failed to create a temp directory '%s' for storing a downloaded file.",
        				destDir));
        	}
        	return null;
        }
        
        return destDir;
    }

	public static File downloadFile(String fileURL) throws IOException {
		File result = null;

    	try {
        	String tmpdir = System.getProperty("java.io.tmpdir");
        	if (tmpdir.endsWith(File.separator) == false) {
        		tmpdir = tmpdir + File.separator;
        	}
        	
        	String destDir = getDestDir(tmpdir + UUID.randomUUID() + File.separator);
        	String filePath = downloadFile(fileURL, destDir);
        	result = new File(filePath);
    	} catch (Exception e) {
    		String msg = String.format("Could not download file use this URL: %s", fileURL);
    		logger.error(msg, e);
    	}
    	
    	return result;
	}
	
	/**
	 * Downloads a file from a URL
	 * 
	 * @param fileURL HTTP URL of the file to be downloaded
	 * @param saveDir path of the directory to save the file
	 * @throws IOException
	 */
	private static String downloadFile(String fileURL, String saveDir) throws IOException {
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int httpResponseCode = 0;
		String result = null;

		try {
			httpResponseCode = httpConn.getResponseCode();
	
			// always check HTTP response code first
			if (httpResponseCode == HttpURLConnection.HTTP_OK) {
				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");

				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 9, disposition.length());
					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
				}
				
				if (logger.isDebugEnabled()) {
					String contentType = httpConn.getContentType();
					int contentLength = httpConn.getContentLength();
					logger.debug("File name is:" + fileName);
					logger.debug("Disposition is:" + disposition != null ? disposition : "<empty>");
					logger.debug("Content type is:" + contentType != null ? contentType : "<empty>");
					logger.debug("Content length is:" + contentLength);
				}				

				// opens input stream from the HTTP connection
				InputStream inputStream = httpConn.getInputStream();
				String saveFilePath = saveDir + File.separator + fileName; //FIXME: File.separator NOT needed
	
				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);
	
				try {
					int bytesRead = -1;
					byte[] buffer = new byte[BUFFER_SIZE];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
				} finally {
					outputStream.close();
					inputStream.close();
				}

				result = saveFilePath;
			}
		} finally {
			httpConn.disconnect();
		}

		return result;
	}
}
