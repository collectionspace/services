package org.collectionspace.services.common.blob;

import java.io.File;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

//import org.collectionspace.services.blob.BlobsCommonList; 
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.common.FileUtils;

public class BlobInput {
	private String blobCsid = null;
	private File blobFile = null;
	private String blobUri = null;
	
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

	public void setBlobFile(File blobFile) {
		this.blobFile = blobFile;
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
	
	public void createBlobFile(HttpServletRequest req, String blobUri) {
    	File tmpFile = FileUtils.createTmpFile(req);
    	this.setBlobFile(tmpFile);
    	this.setBlobUri(blobUri);
	}
	
	public void createBlobFile(String theBlobUri) {
    	File theBlobFile = new File(theBlobUri);
    	this.setBlobFile(theBlobFile);
    	this.setBlobUri(blobUri);
	}	
	
}

