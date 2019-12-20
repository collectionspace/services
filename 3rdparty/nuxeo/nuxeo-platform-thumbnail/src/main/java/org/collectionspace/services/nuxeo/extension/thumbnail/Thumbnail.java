package org.collectionspace.services.nuxeo.extension.thumbnail;

import java.io.Serializable;
import org.collectionspace.services.nuxeo.util.ThumbnailConstants;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

public class Thumbnail {
	private DocumentModel docModel;
	
	public Thumbnail(DocumentModel doc) {
		docModel = doc;
	}
	
	public String getDigest() throws ClientException {
		String result = null;
		
		if (docModel != null) {
			result = (String) docModel.getProperty(ThumbnailConstants.THUMBNAIL_SCHEMA_NAME,
					ThumbnailConstants.THUMBNAIL_DIGEST_PROPERTY_NAME);
		}
		
		return result;
	}	
}
