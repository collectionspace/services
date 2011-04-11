package org.collectionspace.services.media;

import java.io.File;

public class MediaBlobInput {
	private String mediaCsid;
	private File blobFile;
	private String blobUri;
	
	MediaBlobInput(String mediaCsid, File blobFile, String blobUri) {
		this.mediaCsid = mediaCsid;
		this.blobFile = blobFile;
		this.blobUri = blobUri;
	}
	
	String getMediaCsid() {
		return mediaCsid;
	}
	
	File getBlobFile() {
		return blobFile;
	}
	
	String getBlobUri() {
		return blobUri;
	}
}
