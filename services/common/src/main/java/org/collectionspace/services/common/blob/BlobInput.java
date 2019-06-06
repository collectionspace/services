package org.collectionspace.services.common.blob;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.common.Download;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoBlobUtils;

import org.apache.commons.io.FileUtils;

public class BlobInput {
	private final Logger logger = LoggerFactory.getLogger(BlobInput.class);
	private final static String FILE_ACCESS_ERROR = "The following file is either missing or cannot be read: ";
	private final static String URL_DOWNLOAD_FAILED = "Could not download file from the following URL: ";
	
	private boolean isTemporaryFile = false;
	private String blobCsid = null;
	private File blobFile = null;
	private String blobUri = null;
	private String blobMimeType = null;
	private String originalFileName = null;
	
	private String derivativeTerm;
	private boolean derivativeListRequested = false;
	private CommonList derivativeList;
	
	private boolean contentRequested = false;
	private InputStream contentStream;
	
	private boolean schemaRequested = false;
	
	public static final String URI_CONTENT_PATH = "content";
	public static final String URI_DERIVATIVES_PATH = "derivatives";

	/*
	 * Constructors
	 */
	public BlobInput() {
		/* Empty constructor */
	}
		
	public BlobInput(File blobFile, String blobUri) {
		this.blobFile = blobFile;
		this.blobUri = blobUri;
	}
	
	/*
	 * Save the original file name in case we rename it because of illegal Nuxeo file name characters
	 */
	private void setBlobFileName(String fileName) throws Exception {
		String sanitizedResult = NuxeoBlobUtils.getSanizitedFilename(fileName);
		if (fileName.equals(sanitizedResult) == true) {
			originalFileName = null; //
		} else {
			originalFileName = fileName;
		}
	}
	
	public String getBlobFilename() throws Exception {
		String result = null;
		
		if (originalFileName != null && originalFileName.trim().isEmpty() == false) {
			result = originalFileName;
		} else {
			File theBlobFile = this.getBlobFile();
			if (theBlobFile != null) {
				result = theBlobFile.getName();
			}
		}
		
		//
		// Add a log warning if the blob file name fails Nuxeo's file name restrictions.
		//
		String sanitizedResult = NuxeoBlobUtils.getSanizitedFilename(result);
		if (result.equals(sanitizedResult) == false) {
			logger.warn(String.format("The file name '%s' contains characters that Nuxeo deems illegal.",
					result));
		}		
		
		return result;
	}
	
	/*
	 * Getters and Setters
	 */
	public boolean isSchemaRequested() {
		return schemaRequested;
	}

	public void setSchemaRequested(boolean schemaRequested) {
		this.schemaRequested = schemaRequested;
	}

	public String getBlobCsid() {
		return blobCsid;
	}

	public void setBlobCsid(String blobCsid) {
		this.blobCsid = blobCsid;
	}

	public File getBlobFile() {
		return blobFile;
	}

	public void setBlobFile(File blobFile) throws Exception {
		this.blobFile = blobFile;
		if (blobFile != null) {
			String fileName = blobFile.getName();
			setBlobFileName(fileName);
		}
	}

	public String getBlobUri() {
		return blobUri;
	}

	public void setBlobUri(String blobUri) {
		this.blobUri = blobUri;
	}

	public String getDerivativeTerm() {
		return derivativeTerm;
	}

	public void setDerivativeTerm(String derivativeTerm) {
		this.derivativeTerm = derivativeTerm;
	}

	public boolean isDerivativeListRequested() {
		return derivativeListRequested;
	}

	public void setDerivativeListRequested(boolean derivativesRequested) {
		this.derivativeListRequested = derivativesRequested;
	}

	public CommonList getDerivativeList() {
		return derivativeList;
	}

	public void setDerivativeList(CommonList derivativeList) {
		this.derivativeList = derivativeList;
	}

	public InputStream getContentStream() {
		return contentStream;
	}

	public void setContentStream(InputStream contentStream) {
		this.contentStream = contentStream;
	}

	public boolean isContentRequested() {
		return contentRequested;
	}

	public void setContentRequested(boolean contentRequested) {
		this.contentRequested = contentRequested;
	}	
	/*
	 * End of setters and getters
	 */
	
	//
	// FIXME: REM - The callers of this method are sending us a multipart form-data post, so why
	// are we also receiving the blobUri?
	//
	public void createBlobFile(HttpServletRequest req, String blobUri) throws Exception {
    	File tmpFile = org.collectionspace.services.common.FileUtilities.createTmpFile(req);
    	this.setIsTemporaryFile(true);
    	this.setBlobFile(tmpFile);
    	this.setBlobUri(blobUri);
	}
	
	private boolean isProtocolHttp(URL url) {
		boolean result = false;
		
		if (url.getProtocol().equalsIgnoreCase("http") ||
				url.getProtocol().equalsIgnoreCase("https")) {
			result = true;
		}
		
		return result;
	}
	
	public void createBlobFile(String theBlobUri) throws MalformedURLException, Exception {
		URL blobUrl = new URL(theBlobUri);
    	File theBlobFile = null;
		if (isProtocolHttp(blobUrl) == true) {
			Download fetchedFile = new Download(blobUrl);
			if (logger.isDebugEnabled() == true) {
				logger.debug("Starting blob download into temp file:" + fetchedFile.getFilePath());
			}
			while (fetchedFile.getStatus() == Download.DOWNLOADING) {
				// Do nothing while we wait for the file to download
			}
			if (logger.isDebugEnabled() == true) {
				logger.debug("Finished blob download into temp file: " + fetchedFile.getFilePath());
			}
			
			int status = fetchedFile.getStatus();
			if (status == Download.COMPLETE) {
				theBlobFile = fetchedFile.getFile();
				setIsTemporaryFile(true); // setting to true ensures the file will get cleanup (removed) when we're done with it
			} else {
				String msg = URL_DOWNLOAD_FAILED + theBlobUri;
				logger.error(msg);
				throw new DocumentException(msg);
			}
		} else if (blobUrl.getProtocol().equalsIgnoreCase("file")) {
			if (blobUrl.getPath().startsWith("/")) {
				// full path
				theBlobFile = FileUtils.toFile(blobUrl);			
			} else {
				// relative to JEE container (e.g. Apache Tomcat) path
				Path theBlobFilePath = Paths.get(ServiceMain.getJeeContainPath(), blobUrl.getPath());
				theBlobFile = new File(theBlobFilePath.toAbsolutePath().toString());
			}
			if (theBlobFile.exists() == false || theBlobFile.canRead() == false) {
				String msg = FILE_ACCESS_ERROR + theBlobFile.getAbsolutePath();
				logger.error(msg);
				throw new DocumentException(msg);
			}
		} else {
			throw new MalformedURLException("Could not create a blob file from: " + blobUrl);
		}
		
    	this.setBlobFile(theBlobFile);
    	this.setBlobUri(blobUri);
	}

	public String getMimeType() {
		return blobMimeType;
	}

	public void setMimeType(String mimeType) {
		this.blobMimeType = mimeType;
	}
	
	public void setIsTemporaryFile(boolean flag) {
		this.isTemporaryFile = flag;
	}
	
	public boolean isTemporaryFile() {
		return this.isTemporaryFile;
	}
	
}

