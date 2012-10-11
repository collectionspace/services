package org.collectionspace.services.common.blob;

import java.io.InputStream;
import org.collectionspace.services.blob.BlobsCommon;

public class BlobOutput {
	private String mimeType;
	private BlobsCommon blobsCommon;
	private InputStream blobInputStream;

	public BlobsCommon getBlobsCommon() {
		return blobsCommon;
	}
	public void setBlobsCommon(BlobsCommon blobsCommon) {
		this.blobsCommon = blobsCommon;
	}
	public InputStream getBlobInputStream() {
		return blobInputStream;
	}
	public void setBlobInputStream(InputStream blobInputStream) {
		this.blobInputStream = blobInputStream;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
