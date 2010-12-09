package org.collectionspace.services.common.blob;

import java.io.InputStream;
import org.collectionspace.services.blob.BlobsCommon;

public class BlobOutput {
	BlobsCommon blobsCommon;
	InputStream blobInputStream;

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
}
