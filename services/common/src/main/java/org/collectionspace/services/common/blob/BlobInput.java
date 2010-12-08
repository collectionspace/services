package org.collectionspace.services.common.blob;

import java.io.File;

public class BlobInput {
	private String mediaCsid;
	private File blobFile;
	private String blobUri;
	
	public static final String DERIVATIVE_TERM_KEY = "Derivative";
	public static final String DERIVATIVE_ORIGINAL_VALUE = "Original";
	public static final String DERIVATIVE_CONTENT_KEY = "Derivative_Content_Stream";
	
	public BlobInput(File blobFile, String blobUri) {
		this.blobFile = blobFile;
		this.blobUri = blobUri;
	}
	
	public String getMediaCsid() {
		return mediaCsid;
	}
	
	public File getBlobFile() {
		return blobFile;
	}
	
	public String getBlobUri() {
		return blobUri;
	}
}

