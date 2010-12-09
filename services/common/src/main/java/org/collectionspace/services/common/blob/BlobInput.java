package org.collectionspace.services.common.blob;

import java.io.File;

public class BlobInput {
	private String mediaCsid;
	private File blobFile;
	private String blobUri;
	
	public static final String URI_CONTENT_PATH = "content";
	public static final String URI_DERIVATIVES_PATH = "derivatives";
	
	public static final String BLOB_DERIVATIVE_TERM_KEY = "derivative";
	public static final String BLOB_DERIVATIVE_LIST_KEY = "derivative.list";
	public static final String BLOB_CONTENT_KEY = "derivative.content.stream";
	
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

