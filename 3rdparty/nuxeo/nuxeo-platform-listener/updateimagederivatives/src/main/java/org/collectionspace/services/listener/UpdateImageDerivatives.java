package org.collectionspace.services.listener;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.DocumentBlobHolder;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants;

public class UpdateImageDerivatives extends AbstractCSEventSyncListenerImpl {

	// All Nuxeo sessions that get passed around to CollectionSpace code need to
	// be wrapped inside of a CoreSessionWrapper. For example:
	// 		CoreSessionInterface coreSession = new CoreSessionWrapper(docEventContext.getCoreSession());
	private static final Log logger = LogFactory.getLog(UpdateImageDerivatives.class);

    @Override
	public boolean shouldHandleEvent(Event event) {
    	return event.getContext() instanceof DocumentEventContext;
    }
    
	@Override
	public void handleCSEvent(Event event) {
		DocumentEventContext docEventContext = (DocumentEventContext) event.getContext();
		DocumentModel docModel = docEventContext.getSourceDocument();

		String source = (String)docModel.getProperty(CommonAPI.NUXEO_DUBLINCORE_SCHEMANAME,
				CommonAPI.NUXEO_DUBLINCORE_SOURCE);

		if (source != null && source.equalsIgnoreCase(CommonAPI.URL_SOURCED_PICTURE)) {
			CoreSessionInterface nuxeoSession = new CoreSessionWrapper(docEventContext.getCoreSession());
			purgeOriginalImage(docModel, nuxeoSession);
			nuxeoSession.save();
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("The Nuxeo document titled '%s' did not need processing by the '%s' Nuxeo listener.",
								docModel.getTitle(), getClass().getName()));
			}
		}	
	}

	private void purgeOriginalImage(DocumentModel docModel, CoreSessionInterface nuxeoSession) {
		//
		// Empty the document model's "content" property -this does not delete the actual file/blob it
		// just disassociates the blob content (aka, the original image) from the document.
		//
		docModel.setPropertyValue("file:content", (Serializable) null);
		
		//
		// Removing this facet ensures the original derivatives are unchanged when
		// we call the save method.  If we didn't remove the face, then all the
		// image derivatives would be disassociated with the document.  We want to keep
		// the derivatives.
		//
		NuxeoUtils.removeFacet(docModel, ImagingDocumentConstants.PICTURE_FACET);
		nuxeoSession.saveDocument(docModel); // persist the disassociation of the original blob/image
		//
		// Now that we've emptied the document model's content field, we can add back the Picture facet so
		// Nuxeo will still tread this document as a Picture document.
		//
		NuxeoUtils.addFacet(docModel, ImagingDocumentConstants.PICTURE_FACET);
		
		//
		// Finally, we need to remove the actual blob/image bits that are store on disk.
		//
		DocumentBlobHolder docBlobHolder = (DocumentBlobHolder) docModel.getAdapter(BlobHolder.class);
		Blob blob = docBlobHolder.getBlob();		
		if (blob == null) {
			logger.error(String.format("Could not get blob for original image. Trying to delete original for: '%s'",
							docModel.getTitle()));
		} else {
			Thread thread = NuxeoUtils.deleteFileOfBlobAsync(blob);
			logger.debug(String.format("Started thread '%s' to delete file of blob '%s'.",
					thread.getId(), blob.getFilename()));
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Exiting handleEvent in '%s'.", getClass().getName()));			
		}	
	}

	@Override
	public Log getLogger() {
		return logger;
	}
}