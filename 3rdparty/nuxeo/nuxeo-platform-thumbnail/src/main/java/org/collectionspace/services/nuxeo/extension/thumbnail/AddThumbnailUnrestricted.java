package org.collectionspace.services.nuxeo.extension.thumbnail;

/*
 * An example Nuxeo event "listener".
 */

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.nuxeo.util.ThumbnailConstants;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.runtime.api.Framework;

public class AddThumbnailUnrestricted extends UnrestrictedSessionRunner {
 
    private static final Log logger = LogFactory
            .getLog(AddThumbnailUnrestricted.class);
 
    protected ConversionService cs;
 
    protected DocumentModel doc;
 
    protected BlobHolder blobHolder;
 
    protected Thumbnail thumbnail = null;
 
    public AddThumbnailUnrestricted(CoreSession coreSession, DocumentModel doc,
            BlobHolder blobHolder) {
        super(coreSession);
        this.doc = doc;
        this.blobHolder = blobHolder;
    }
 
    /*
     * (non-Javadoc)
     * @see org.nuxeo.ecm.core.api.UnrestrictedSessionRunner#run()
     * 
     * Creates a new thumbnail image and associates it with the document blob by adding a "Thumbnail" facet
     * to the document blob.
     */
    @Override
    public void run() throws ClientException {
    	String errMsg = "Error while adding preview thumbnail.";
    	String documentId = doc.getId();
    	
        try {
            Blob blob = blobHolder.getBlob();
            if (blob != null) {
                if (doc.hasFacet(ThumbnailConstants.THUMBNAIL_FACET) == false) { // Make sure we don't already have a "Thumbnail" facet
	                cs = Framework.getService(ConversionService.class);                
	                ensureModificationDateExists(doc); // For some reason, the ConversionService service requires the modification date of the blob is not null so we need to ensure it is not null. 
	                BlobHolder thumbnailBlobHolder = cs.convert(ThumbnailConstants.THUMBNAIL_CONVERTER_NAME,
	                        blobHolder, null /*no params*/);
	                if (thumbnailBlobHolder != null && thumbnailBlobHolder.getBlob() != null) {
	                    Blob thumbnailBlob = thumbnailBlobHolder.getBlob();
	                	doc.addFacet(ThumbnailConstants.THUMBNAIL_FACET); // Add the "Thumbnail" facet since we were able to create a thumnail image
	                	// Give the thumbnail blob a name.
	                    String thumbnailName = documentId + ThumbnailConstants.THUMBNAIL_PROPERTY_NAME;
	                    thumbnailBlobHolder.getBlob().setFilename(thumbnailName); // Give it a name so we can manually search for it in the "nuxeo" database
	                    
	                    doc.setProperty(ThumbnailConstants.THUMBNAIL_SCHEMA_NAME,
	                            ThumbnailConstants.THUMBNAIL_FILENAME_PROPERTY_NAME,
	                            (Serializable) thumbnailName);
	                    doc.setProperty(ThumbnailConstants.THUMBNAIL_SCHEMA_NAME,
	                            ThumbnailConstants.THUMBNAIL_PROPERTY_NAME,
	                            (Serializable) thumbnailBlob);
	                    //
	                    // Save the new Thumnail facet data (including the new thumbnail image).  The save triggers a new create event and recurses us back to
	                    // this method, but the next time we'll have a Thumbnail facet and bypass this save -sparing us from an infinite event loop.
	                    //
	                    doc = session.saveDocument(doc); 
	                } else {
	                	logger.warn("Could not create a preview thumbnail image for Nuxeo blob document: " + doc.getId());
	                }
                }
            } else {
            	logger.warn(errMsg + " " + "The Nuxeo blob holder had an empty blob object.  Document ID:" + doc.getId());
            }
        } catch (Exception e) {
            logger.warn(errMsg, e);
        }
    }
 
    private String computeDigest(FileManager fileManager, Blob blob) throws Exception {
    	String result = null;
    	
    	// Compute the digest
//        result = fileManager.computeDigest(blob); // REM - Warning: Why is this operation so slow?
        result = blob.getDigest();
        
        return result;
    }
    
    private String ensureModificationDateExists(DocumentModel docModel) throws Exception {
        Calendar modificationDate = (Calendar)doc.getProperty("dublincore", "modified");
        if (modificationDate == null) {
        	// If the 'modified' field is null then try the 'created' field
        	Calendar creationDate = (Calendar)doc.getProperty("dublincore", "created");
        	if (creationDate != null) {
        		modificationDate = creationDate;
        	} else {
        		// We *need* a 'modified' date, so let's use the current date
        		modificationDate = new GregorianCalendar();
        	}
    		doc.setProperty("dublincore", "modified", modificationDate);
        }
        
        return modificationDate.toString();
    }
    
    public Thumbnail getAdapter() {
        return thumbnail;
    }
 
}
