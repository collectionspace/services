/**	
 * NuxeoImageUtils.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}.
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.imaging.nuxeo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.nuxeo.runtime.api.Framework;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.mimetype.MimetypeDetectionException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolder;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.filemanager.service.FileManagerService;
import org.nuxeo.ecm.platform.filemanager.service.extension.FileImporter;
import org.nuxeo.ecm.platform.filemanager.utils.FileManagerUtils;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.DocumentBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.binary.metadata.api.BinaryMetadataService;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PropertyException;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.FileUtilities;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.blob.BlobOutput;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.blob.DimensionSubGroup;
import org.collectionspace.services.blob.DimensionSubGroupList;
import org.collectionspace.services.blob.MeasuredPartGroup;
import org.collectionspace.services.blob.MeasuredPartGroupList;
import org.collectionspace.services.jaxb.BlobJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.nuxeo.util.ThumbnailConstants;
import org.collectionspace.services.config.service.ListResultField;

/**
 * Use Nuxeo's FileBlob class to create a temporary file that Nuxeo manages.
 * @author remillet
 *
 */
class CSpaceFileBlob extends FileBlob {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CSpaceFileBlob(File file, boolean isTempFile) {
		super(file);
		this.isTemporary = isTempFile;  // if set to "true", Nuxeo will delete this file once it is finished with it
	}
}

/**
 * The Class NuxeoBlobUtils.
 */
public class NuxeoBlobUtils {
			
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(NuxeoBlobUtils.class);
	
    private static final String VIEWS_PROPERTY = "picture:views";
    private static final String TITLE_PROPERTY = "title";
    private static final String FILENAME_PROPERTY = "filename";

	//
	// A maximum byte size for the byte array used to hold an image.  Images larger than this will
	// be returned as FileInputStreams rather than ByteArrayInputStreams
	//
	private static final int MAX_IMAGE_BUFFER = 256 * 1024; // REM: 11/26/2013 - This should be set in a config/property file.
	

	
	//
	// File name constants
	//
    private static final String NUXEO_FILENAME_BAD_CHARS = "[^a-zA-Z_0-9-.%:/\\ ]";
    private static final String NUXEO_FILENAME_VALID_STRING = "[a-zA-Z_0-9-.%:/\\ ]+";

	public static final String DOCUMENT_PLACEHOLDER_IMAGE = "documentImage.jpg";
	public static final String DOCUMENT_MISSING_PLACEHOLDER_IMAGE = "documentImageMissing.jpg";
	
	public static final String DOCUMENT_PLACEHOLDER_CSV  = "documentCSV.jpg";
	public static final String DOCUMENT_PLACEHOLDER_DOC  = "documentDOC.jpg";
	public static final String DOCUMENT_PLACEHOLDER_DOCX = "documentDOC.jpg";
	public static final String DOCUMENT_PLACEHOLDER_MP3  = "documentMP3.jpg";
	public static final String DOCUMENT_PLACEHOLDER_PDF  = "documentPDF.jpg";
	public static final String DOCUMENT_PLACEHOLDER_PPT  = "documentPPT.jpg";
	public static final String DOCUMENT_PLACEHOLDER_PPTX = "documentPPT.jpg";
	public static final String DOCUMENT_PLACEHOLDER_RTF  = "documentRTF.jpg";
	public static final String DOCUMENT_PLACEHOLDER_XLS  = "documentXLS.jpg";
	public static final String DOCUMENT_PLACEHOLDER_XLSX = "documentXLS.jpg";
	public static final String DOCUMENT_PLACEHOLDER_ZIP  = "documentZIP.jpg";

	public static final String MIME_CSV  = "text/csv";
	public static final String MIME_DOC  = "application/msword";
	public static final String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String MIME_JPEG = "image/jpeg";
	public static final String MIME_MP3  = "audio/mpeg";
	public static final String MIME_PDF  = "application/pdf";
	public static final String MIME_PPT  = "application/vnd.ms-powerpoint";
	public static final String MIME_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String MIME_RTF  = "text/rtf";
	public static final String MIME_XLS  = "application/vnd.ms-excel";
	public static final String MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String MIME_ZIP  = "application/zip";

	/*
	 * FIXME: REM - These constants should be coming from configuration and NOT
	 * hard coded.
	 */
	public static final String DERIVATIVE_ORIGINAL = "Original";
	public static final String DERIVATIVE_ORIGINAL_TAG = DERIVATIVE_ORIGINAL
			+ "_";

	public static final String DERIVATIVE_ORIGINAL_JPEG = "OriginalJpeg";
	public static final String DERIVATIVE_ORIGINAL_JPEG_TAG = DERIVATIVE_ORIGINAL_JPEG + "_";

	public static final String DERIVATIVE_MEDIUM = "Medium";
	public static final String DERIVATIVE_MEDIUM_TAG = DERIVATIVE_MEDIUM + "_";

	public static final String DERIVATIVE_SMALL = "Small";
	public static final String DERIVATIVE_SMALL_TAG = DERIVATIVE_SMALL + "_";

	public static final String DERIVATIVE_THUMBNAIL = "Thumbnail";
	public static final String DERIVATIVE_THUMBNAIL_TAG = DERIVATIVE_THUMBNAIL + "_";

	public static final String DERIVATIVE_UNKNOWN = "_UNKNOWN_DERIVATIVE_NAME_";

	//
	// Image Dimension fields
	//
	public static final String PART_IMAGE = "digitalImage";
	public static final String PART_SUMMARY = "The dimensions of a digital image -width, height, and pixel depth.";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String DEPTH = "depth";
	public static final String UNIT_PIXELS = "pixels";
	public static final String UNIT_BITS = "bits";
	//
	// Image Metadata schemas - These are Nuxeo defined schemas
	//
	public static final String SCHEMA_IPTC = "iptc";
	public static final String SCHEMA_IMAGE_METADATA = "image_metadata";

	/**
	 * Instantiates a new nuxeo image utils.
	 */
	NuxeoBlobUtils() {
		// empty constructor
	}

	/*
	 * Use this for debugging Nuxeo's PictureView class
	 */
	private static String toStringPictureView(PictureView pictureView) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Description: " + pictureView.getDescription() + '\n');
		strBuffer.append("FileName: " + pictureView.getFilename() + '\n');
		strBuffer.append("Height: " + pictureView.getHeight() + '\n');
		strBuffer.append("Width: " + pictureView.getWidth() + '\n');
		strBuffer.append("Tag: " + pictureView.getTag() + '\n');
		strBuffer.append("Title: " + pictureView.getTitle() + '\n');
		return strBuffer.toString();
	}

	static private String getDerivativeUri(String uri, String derivativeName) {
		return uri + derivativeName + "/" + BlobInput.URI_CONTENT_PATH;
	}

	static private HashMap<String, Object> createBlobListItem(Blob blob,
			String uri,
			String derivativeName) {
		HashMap<String, Object> item = new HashMap<String, Object>();

		String value = getDerivativeUri(uri, derivativeName);
		if (!value.trim().isEmpty()) {
			item.put(BlobJAXBSchema.uri, value);
		} else {
			item.put(BlobJAXBSchema.uri, getDerivativeUri(uri, DERIVATIVE_UNKNOWN));
		}

		value = blob.getEncoding();
		if (value != null && !value.trim().isEmpty()) {
			item.put(BlobJAXBSchema.encoding, value);
		}
		value = Long.toString(blob.getLength());
		if (value != null && !value.trim().isEmpty()) {
			item.put(BlobJAXBSchema.length, value);
		}
		value = blob.getMimeType();
		if (value != null && !value.trim().isEmpty()) {
			item.put(BlobJAXBSchema.mimeType, value);
		}
		value = blob.getFilename();
		if (value != null && !value.trim().isEmpty()) {
			item.put(BlobJAXBSchema.name, value);
		}

		return item;
	}
	
	static public String getSanizitedFilename(File srcFile) throws Exception {
		return getSanizitedFilename(srcFile.getName());
	}
	
	/*
	 * Valid Nuxeo file names are a subset of *nix and Windows filenames, so we need to check.
	 */
	static public String getSanizitedFilename(String fileName) throws Exception {
		String result = fileName;
		
		if (fileName != null && fileName.matches(NUXEO_FILENAME_VALID_STRING) == false) {
			String fixedString = fileName.replaceAll(NUXEO_FILENAME_BAD_CHARS, "_");  // Replace "bad" chars with underscore character
			if (fixedString.matches(NUXEO_FILENAME_VALID_STRING) == true) {
				result = fixedString;
			} else {
				String errMsg = String.format("\tSorry, the sanizited string '%s' is still bad.", fixedString);
				throw new Exception(errMsg);
			}
		}
		
		if (result != null && logger.isDebugEnabled() == true) {
			if (result.equals(fileName) == false) {
				logger.debug(String.format("The file name '%s' was sanizitized to '%s'.", fileName, result));
			}
		}

		return result;
	}

	static private CommonList handleGenericBlobs(DocumentBlobHolder docBlobHolder,
			CoreSessionInterface repoSession,
			String repositoryId,
			CommonList commonList,
			String uri) {
		List<Blob> docBlobs = docBlobHolder.getBlobs();			
		// List<BlobListItem> blobListItems = result.getBlobListItem();
		HashMap<String, Object> item = null;
		for (Blob blob : docBlobs) {
			if (blob != null) {
				item = createBlobListItem(blob, uri, blob.getDigest());
				if (item != null) {
					commonList.addItem(item);
				}
			}
		}

		return commonList;
	}
	
	static private CommonList handlePictureViewBlobs(PictureBlobHolder pictureBlobHolder,
			CoreSessionInterface repoSession,
			String repositoryId,
			CommonList commonList,
			String uri) {		
		Set<String> derivativeNameList = NuxeoBlobUtils.getPictureViewNameSet(repoSession, repositoryId);
		for (String viewName : derivativeNameList) {
			Blob blob = pictureBlobHolder.getBlob(viewName);
			if (blob != null) {
				HashMap<String, Object> item = createBlobListItem(blob, uri, viewName);
				if (item != null) {
					commonList.addItem(item);
				}
			} else {
				String msg = String.format("Could not get blob view '%s' for Nuxeo picuture document ID='%s'.",
						viewName, repositoryId);
			}
		}

		return commonList;
	}

	static public CommonList getBlobDerivatives(CoreSessionInterface repoSession,
			String repositoryId, List<ListResultField> resultsFields, String uri)
			throws Exception {
		CommonList commonList = new CommonList();
		int nFields = resultsFields.size() + 2;
		String fields[] = new String[nFields];// FIXME: REM - Patrick needs to fix this hack.  It is a "common list" issue
		fields[0] = "csid";
		fields[1] = "uri";
		for (int i = 2; i < nFields; i++) {
			ListResultField field = resultsFields.get(i - 2);
			fields[i] = field.getElement();
		}
		commonList.setFieldsReturned(fields);

		DocumentModel documentModel = repoSession.getDocument(new IdRef(repositoryId));
		DocumentBlobHolder docBlobHolder = (DocumentBlobHolder) documentModel.getAdapter(BlobHolder.class);
		if (docBlobHolder instanceof PictureBlobHolder) {
			commonList = handlePictureViewBlobs((PictureBlobHolder)docBlobHolder, repoSession, repositoryId, commonList, uri);
		} else {
			commonList = handleGenericBlobs((PictureBlobHolder)docBlobHolder, repoSession, repositoryId, commonList, uri);
		}
		
		return commonList;
	}

    static public boolean isBlobAnImage(Blob input) {
    	boolean result = false;

		FileImporter importer = getFileManagerService().getPluginByName("Imageplugin");

        String normalizedMimeType = getMimeService().getMimetypeEntryByMimeType(input.getMimeType()).getNormalized();
        if (importer.isEnabled()
                && (importer.matches(normalizedMimeType) || importer.matches(input.getMimeType()))) {
        	result = true;
        }

        return result;
    }

	/*
	 * [dublincore, uid, picture, iptc, common, image_metadata]
	 */
	static private Map<String, Object> getMetadata(Blob nuxeoBlob) 
			throws Exception {
		
		BinaryMetadataService binaryMetadataService = Framework.getService(BinaryMetadataService.class); 
		Map<String, Object> blobProperties = binaryMetadataService.readMetadata(nuxeoBlob, false);
		
		ImagingService service = Framework.getService(ImagingService.class);
		Map<String, Object> metadataMap = service.getImageMetadata(nuxeoBlob); // use org.nuxeo.binary.metadata.api.BinaryMetadataService#readMetadata(org.nuxeo.ecm.core.api.Blob)
		return metadataMap;
	}
	
	static private MeasuredPartGroupList getDimensions(
			DocumentModel documentModel, Blob nuxeoBlob) {
		MeasuredPartGroupList result = null;
		
		if (isBlobAnImage(nuxeoBlob) == true) try {
			ImagingService service = Framework.getService(ImagingService.class);
			ImageInfo imageInfo = service.getImageInfo(nuxeoBlob);
			Map<String, Object> metadataMap = getMetadata(nuxeoBlob);

			if (imageInfo != null) {
				//
				// Create a timestamp to add to all the image's dimensions
				//
				String valueDate = GregorianCalendarDateTimeUtils
						.timestampUTC();
				
				result = new MeasuredPartGroupList();
				List<MeasuredPartGroup> measuredPartGroupList = 
						(result).getMeasuredPartGroup();
				//
				// Create a new measured part for the "image"
				//
				MeasuredPartGroup mpGroup = new MeasuredPartGroup();
				mpGroup.setMeasuredPart(PART_IMAGE);
				mpGroup.setDimensionSummary(PART_SUMMARY);
				mpGroup.setDimensionSubGroupList(new DimensionSubGroupList());
				List<DimensionSubGroup> dimensionSubGroupList = mpGroup.getDimensionSubGroupList()
						.getDimensionSubGroup();

				//
				// Set the width
				//
				DimensionSubGroup widthDimension = new DimensionSubGroup();
				widthDimension.setDimension(WIDTH);
				widthDimension.setMeasurementUnit(UNIT_PIXELS);
				widthDimension.setValue(intToBigDecimal(imageInfo.getWidth()));
				widthDimension.setValueDate(valueDate);
				dimensionSubGroupList.add(widthDimension);
				//
				// Set the height
				//
				DimensionSubGroup heightDimension = new DimensionSubGroup();
				heightDimension.setDimension(HEIGHT);
				heightDimension.setMeasurementUnit(UNIT_PIXELS);
				heightDimension
						.setValue(intToBigDecimal(imageInfo.getHeight()));
				heightDimension.setValueDate(valueDate);
				dimensionSubGroupList.add(heightDimension);
				//
				// Set the depth
				//
				DimensionSubGroup depthDimension = new DimensionSubGroup();
				depthDimension.setDimension(DEPTH);
				depthDimension.setMeasurementUnit(UNIT_BITS);
				depthDimension.setValue(intToBigDecimal(imageInfo.getDepth()));
				depthDimension.setValueDate(valueDate);
				dimensionSubGroupList.add(depthDimension);
				//
				// Now set out result
				//
				measuredPartGroupList.add(mpGroup);
			} else {
				if (logger.isWarnEnabled() == true) {
					logger.warn("Could not synthesize a dimension list of the blob: "
							+ documentModel.getName());
				}
			}
		} catch (Exception e) {
			logger.warn("Could not extract image information for blob: "
					+ documentModel.getName(), e);
		}

		return result;
	}

	// FIXME: Add error checking here, as none of these calls return an
	// Exception
	static private BigDecimal intToBigDecimal(int i) {
		BigInteger bigint = BigInteger.valueOf(i);
		BigDecimal bigdec = new BigDecimal(bigint);
		return bigdec;
	}

	static private BlobsCommon createBlobsCommon(ServiceContext ctx, DocumentModel documentModel,
			Blob nuxeoBlob) {
		return createBlobsCommon(ctx, documentModel, nuxeoBlob, false);
	}
	
	static private BlobsCommon createBlobsCommon(ServiceContext ctx, DocumentModel documentModel,
			Blob nuxeoBlob, Boolean getContentFlag) {
		BlobsCommon result = new BlobsCommon();

		if (documentModel != null && nuxeoBlob != null) {
			result.setMimeType(nuxeoBlob.getMimeType());
			result.setName(nuxeoBlob.getFilename());
			result.setLength(Long.toString(nuxeoBlob.getLength()));
			result.setRepositoryId(documentModel.getId());
			// set the blob's digest value
			String digest = nuxeoBlob.getDigest();
			if (digest == null) {
				try {
			        BlobManager blobManager = Framework.getService(BlobManager.class);
			        BlobProvider bp = blobManager.getBlobProvider(ctx.getRepositoryName());
			        digest = bp.getBinaryManager().getBinary(nuxeoBlob).getDigest();
				} catch (Exception e) {
					logger.warn(String.format("Could not compute digest value for uploaded media '%s'.", nuxeoBlob.getFilename()));
				}
			}
			result.setDigest(digest);
			
			//
			// If getContentFlag is true then we're being asked for the blob's content, so we don't
			// need the measurement info.  Getting the measurement info requires a call to Nuxeo which in turn
			// calls ImageMagick.
			//
			if (getContentFlag.booleanValue() == false) {
				MeasuredPartGroupList measuredPartGroupList = getDimensions(
						documentModel, nuxeoBlob);
				if (measuredPartGroupList != null) {
					result.setMeasuredPartGroupList(measuredPartGroupList);
				}
			}

			// Check to see if a thumbnail preview was created by Nuxeo
            if (documentModel.hasFacet(ThumbnailConstants.THUMBNAIL_FACET)) {
    			String errorMsg = null;
            	String thumbnailName = null;
				try {
					thumbnailName = (String)documentModel.getProperty(ThumbnailConstants.THUMBNAIL_SCHEMA_NAME,
					        ThumbnailConstants.THUMBNAIL_FILENAME_PROPERTY_NAME);
					Blob thumbnailBlob = (Blob)documentModel.getProperty(ThumbnailConstants.THUMBNAIL_SCHEMA_NAME,
					        ThumbnailConstants.THUMBNAIL_PROPERTY_NAME);
				} catch (ClientException e) {
					errorMsg = "Could not extract the name of the thumbnail preview image file.";
					if (logger.isDebugEnabled()) {
						logger.debug(errorMsg, e);
					}
				}

				if (errorMsg == null) {
					logger.info("A thumbnail preview was created for this document blob: " + thumbnailName);
				} else {
					logger.warn(errorMsg);
				}
            }
		}

		return result;
	}

    static private Blob checkMimeType(Blob blob, String fullname)
            throws ClientException {
        final String mimeType = blob.getMimeType();
        if (mimeType != null && !mimeType.equals("application/octet-stream")
                && !mimeType.equals("application/octetstream")) {
            return blob;
        }
        String filename = FileManagerUtils.fetchFileName(fullname);
        try {
            blob = getMimeService().updateMimetype(blob, filename);
        } catch (MimetypeDetectionException e) {
            throw new ClientException(e);
        }
        return blob;
    }
	
	/**
	 * Gets the type service.  Not in use, but please keep for future reference
	 * 
	 * @return the type service
	 * @throws ClientException
	 *             the client exception
	 */
	private static TypeManager getTypeService() throws ClientException {
		TypeManager typeService = null;
		
		try {
			typeService = Framework.getService(TypeManager.class);
		} catch (Exception e) {
			throw new ClientException(e);
		}
		
		return typeService;
	}

	/**
	 * Create a temporary Nuxeo FileBlob instance for processing.  Nuxeo will clean this up for us.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private static Blob createFileBlob(File file, boolean isTemporary) throws Exception {
		return new CSpaceFileBlob(file, isTemporary);
	}

	/**
	 * Gets Nuxeo's file manager service.
	 * 
	 * @return the file manager service
	 * @throws ClientException
	 *             the client exception
	 */
	private static FileManager getFileManager() throws ClientException {
		FileManager result = null;
		
		try {
			result = Framework.getService(FileManager.class);
		} catch (Exception e) {
			String msg = "Unable to get Nuxeo's FileManager service.";
			logger.error(msg, e);
			throw new ClientException("msg", e);
		}
		
		return result;
	}
		
	/**
	 * Gets Nuxeo's file manager service.
	 * 
	 * @return the file manager service
	 * @throws ClientException
	 *             the client exception
	 */
	private static FileManagerService getFileManagerService() throws ClientException {
		FileManagerService result = null;
		
		try {
			result = (FileManagerService)getFileManager();
		} catch (Exception e) {
			String msg = "Unable to get Nuxeo's FileManager service.";
			logger.error(msg, e);
			throw new ClientException("msg", e);
		}
		
		return result;
	}	
	
	
	static private CoreSessionInterface getRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient) {
		CoreSessionInterface result = null;		
		NuxeoRepositoryClientImpl nuxeoClient = (NuxeoRepositoryClientImpl)repositoryClient;
		
		try {
			result = nuxeoClient.getRepositorySession(ctx);
		} catch (Exception e) {
            logger.error("Could not get a repository session to the Nuxeo repository", e);
		}
		
		return result;
	}
	
	static private void releaseRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient,
			CoreSessionInterface repoSession) throws TransactionException {
		NuxeoRepositoryClientImpl nuxeoClient = (NuxeoRepositoryClientImpl)repositoryClient;
		nuxeoClient.releaseRepositorySession(ctx, repoSession);
	}
	
    static private MimetypeRegistry getMimeService() throws ClientException {
    	MimetypeRegistry result = null;
    	
        try {
        	result = Framework.getService(MimetypeRegistry.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
	        
        return result;
    }
	
	private static DocumentModel createDocumentFromBlob(
			CoreSessionInterface repoSession,
            Blob inputStreamBlob, 
            String blobLocation, 
            boolean overwrite, 
            String blobName, 
            boolean useNuxeoAdaptors) throws Exception {
		DocumentModel result = null;
		
		boolean createdFromAdaptor = false;		
		if (useNuxeoAdaptors == true) try {
			//
			// Use Nuxeo's high-level create method which looks for plugin adapters that match the MIME type.  For example,
			// for image blobs, Nuxeo's file manager will pick a special image plugin that will automatically generate
			// image derivatives.
			//
			result = getFileManager().createDocumentFromBlob(
					repoSession.getCoreSession(), inputStreamBlob, blobLocation, overwrite, blobName);
			createdFromAdaptor = true;
		} catch (NuxeoException ne) {
			logger.warn(String.format("Tried but failed to use Nuxeo import adaptor to download '%s'.  Falling back to generic file importer",
					blobName));
		}
		
		if (createdFromAdaptor == false) {
			//
			// User Nuxeo's default file importer/adapter explicitly.  This avoids specialized functionality from happening like
			// image derivative creation.
			//
			String digestAlgorithm = getFileManager().getDigestAlgorithm(); // Only call this because we seem to need some way of initializing Nuxeo's FileManager with a call.
			
			FileManagerService fileManagerService = getFileManagerService();
			inputStreamBlob = checkMimeType(inputStreamBlob, blobName);

			FileImporter defaultFileImporter = fileManagerService.getPluginByName("DefaultFileImporter");
			result = defaultFileImporter.create(
					repoSession.getCoreSession(), inputStreamBlob, blobLocation, overwrite, blobName, getTypeService());			
		}
		
		return result;
	}
	
	static public BlobsCommon createBlobInRepository(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient,
			InputStream inputStream,
			String blobName,
			boolean useNuxeoAdaptors) throws TransactionException {
		BlobsCommon result = null;

		boolean repoSessionCleanup = false;
		CoreSessionInterface repoSession = (CoreSessionInterface)ctx.getCurrentRepositorySession();
		if (repoSession == null) {
			repoSession = getRepositorySession(ctx, repositoryClient);
			repoSessionCleanup = true;
		}
				
		try {
			// We'll store the blob inside the workspace directory of the calling service
			String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
			DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
			DocumentModel blobLocation = repoSession.getDocument(nuxeoWspace);
			
			Blob inputStreamBlob = new FileBlob(inputStream); // creates a temp file to hold the stream
			DocumentModel documentModel = createDocumentFromBlob(
					repoSession,
		            inputStreamBlob, 
		            blobLocation.getPathAsString(), 
		            false, 
		            blobName,
		            useNuxeoAdaptors);
			result = createBlobsCommon(ctx, documentModel, inputStreamBlob); // Now create the metadata about the Nuxeo blob document
		} catch (Exception e) {
			result = null;
			logger.error("Could not create new Nuxeo blob document.", e); //FIXME: REM - This should probably be re-throwing the exception?
		} finally {
			if (repoSessionCleanup == true) {
				releaseRepositorySession(ctx, repositoryClient, repoSession);
			}
		}
		
		return result;
	}
	
	/**
	 * Creates the picture.
	 * 
	 * @param ctx
	 *            the ctx
	 * @param repoSession
	 *            the repo session
	 * @param filePath
	 *            the file path
	 * @return the string
	 * @throws Exception 
	 */
	public static BlobsCommon createBlobInRepository(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			CoreSessionInterface repoSession,
			BlobInput blobInput,
			boolean purgeOriginal,
			boolean useNuxeoAdaptors) throws Exception {
		BlobsCommon result = null;

		File originalFile = blobInput.getBlobFile();
		File targetFile = originalFile;
		try {
			// We'll store the blob inside the workspace directory of the calling service
			String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
			DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
			//
			// If the original file's name contains "illegal" characters, then we create a copy of the file to give Nuxeo.
			//
			String sanitizedName = NuxeoBlobUtils.getSanizitedFilename(originalFile);
			if (sanitizedName.equals(originalFile.getName()) == false) {
				targetFile = FileUtilities.createTmpFile(originalFile, sanitizedName);
				if (logger.isDebugEnabled() == true) {
					logger.debug(String.format("The file '%s''s name has characters that Nuxeo can't deal with.  Rather than renaming the file, we created a new temp file at '%s'",
							originalFile.getName(), targetFile.getAbsolutePath()));
				}
			}			
			
			DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
			DocumentModel newBlobFolder = NuxeoUtils.createFolder(wspaceDoc, UUID.randomUUID().toString());
			
			result = createBlobInRepository(ctx,
					repoSession,
					newBlobFolder,
					purgeOriginal,
					targetFile,
					blobInput.isTemporaryFile(),
					null, // MIME type
					useNuxeoAdaptors);
			//
			// Make sure we're using the original file name in our BlobsCommon instance.  If the original file's name
			// contained illegal characters, then we created and handed a copy of the file to Nuxeo.  We don't want the
			// copy's file name stored in the BlobsCommon instance, we want the original file name instead.
			//
			if (targetFile.equals(originalFile) == false) {
				result.setName(originalFile.getName());
			}
			
		} catch (Exception e) {
			logger.error("Could not create image blob.", e);
			throw e;
		} finally {
			//
			// If we created a temp file then we should delete it.
			//
			if (targetFile.equals(originalFile) == false) {
				if (targetFile.delete() == false) {
					logger.warn(String.format("Attempt to delete temporary file '%s' failed.", targetFile.getAbsolutePath()));
				}
			}
		}

		return result;
	}
	
	/*
	 * Find out if this document's blob/file-contents are allowed to be purged.  For instance, we currently
	 * only want to allow the purging the contents of Nuxeo "Picture" documents. 
	 */
	static private boolean isPurgeAllowed(DocumentModel docModel) {
		boolean result = false;
		
		if (docModel.hasFacet(ImagingDocumentConstants.PICTURE_FACET) == true) {
			result = true; // Yes, delete/purge the original content
		}
		
		return result;
	}
	
	/**
	 * Creates the image blob.
	 * 
	 * @param nuxeoSession
	 *            the nuxeo session
	 * @param blobLocation
	 *            the blob location
	 * @param file
	 *            the file
	 * @param fileName
	 *            the file name
	 * @param mimeType
	 *            the mime type
	 * @return the string
	 */
	static private BlobsCommon createBlobInRepository(ServiceContext ctx,
			CoreSessionInterface nuxeoSession,
			DocumentModel blobLocation,
			boolean purgeOriginal,
			File file,
			boolean isFileTemporary,
			String mimeType,
			boolean useNuxeoAdaptors) {
		BlobsCommon result = null;

		try {
			Blob fileBlob = createFileBlob(file, isFileTemporary);
			DocumentModel documentModel = createDocumentFromBlob(
					nuxeoSession, fileBlob,
					blobLocation.getPathAsString(),
					false,
					file.getName(),
					useNuxeoAdaptors);

			result = createBlobsCommon(ctx, documentModel, fileBlob); // Now create our metadata resource document

			// If the requester wants us to generate only derivatives, we need to purge/clear the original image file
			if (purgeOriginal == true && isPurgeAllowed(documentModel) == true) {
				
				// Empty the document model's "content" property -this does not delete the actual file/blob
				//documentModel.setPropertyValue("file:content", (Serializable) null);
				
				if (documentModel.hasFacet(ImagingDocumentConstants.PICTURE_FACET)) {
					//
					// We're going to use the "source" property field of the Dublin Core schema as a way of indicating to
					// our event listener (See UpdateImageDerivatives.java) that the original image needs to be
					// purged.  The "source" property does not seem to be used by Nuxeo for Picture documents as of v6.0.  However, this might
					// break in future releases of Nuxeo, so we'll emit a warning to the logs if we find a value in this
					// property.
					//
					String source = (String)documentModel.getProperty(CommonAPI.NUXEO_DUBLINCORE_SCHEMANAME,
							CommonAPI.NUXEO_DUBLINCORE_SOURCE);
					if (source != null) {
						logger.warn(String.format("The Nuxeo dublin core property '%s' is set to '%s'.  We expected it to be empty. See JIRA issue CSPACE-6679 for details.",
								CommonAPI.NUXEO_DUBLINCORE_SOURCE, source));
					}
					documentModel.setProperty(CommonAPI.NUXEO_DUBLINCORE_SCHEMANAME,
							CommonAPI.NUXEO_DUBLINCORE_SOURCE, CommonAPI.URL_SOURCED_PICTURE);
					
					// Now with no content, the derivative listener wants to update the derivatives. So to
					// prevent the listener, we remove the "Picture" facet from the document
					//NuxeoUtils.removeFacet(documentModel, ImagingDocumentConstants.PICTURE_FACET); // Removing this facet ensures the original derivatives are unchanged.
					// Now that we've emptied the document model's content field, we can add back the Picture facet
					//NuxeoUtils.addFacet(documentModel, ImagingDocumentConstants.PICTURE_FACET);
				}
				
				//nuxeoSession.saveDocument(documentModel);
				// Next, we need to remove the actual file from Nuxeo's data directory
//				Blob blob = docBlobHolder.getBlob();
//				if(blob == null) {
//					logger.error("Could not get blob for original image. Trying to delete original for: {}",
//							file.getName());
//				} else {
//					boolean deleteSuccess = NuxeoUtils.deleteFileOfBlob(docBlobHolder.getBlob());
//				}
			}
			
			//
			// Persist/save any changes.
			//
			nuxeoSession.saveDocument(documentModel);
			nuxeoSession.save();

		} catch (Exception e) {
			result = null;
			logger.error("Could not create new Nuxeo blob document.", e); //FIXME: REM - This should probably be re-throwing the exception?
		}

		return result;
	}

	// /*
	// * This is an alternate approach to getting information about an image
	// * and its corresponding derivatives.
	// */
	// // MultiviewPictureAdapter multiviewPictureAdapter =
	// documentModel.getAdapter(MultiviewPictureAdapter.class);
	// MultiviewPictureAdapterFactory multiviewPictureAdapterFactory = new
	// MultiviewPictureAdapterFactory();
	// MultiviewPictureAdapter multiviewPictureAdapter =
	// (MultiviewPictureAdapter)multiviewPictureAdapterFactory.getAdapter(documentModel,
	// null);
	// if (multiviewPictureAdapter != null) {
	// PictureView[] pictureViewArray = multiviewPictureAdapter.getViews();
	// for (PictureView pictureView : pictureViewArray) {
	// if (logger.isDebugEnabled() == true) {
	// logger.debug("-------------------------------------");
	// logger.debug(toStringPictureView(pictureView));
	// }
	// }
	// }
	
	public static InputStream getResource(String resourceName) {
		InputStream result = null;
		
		try {
			result = ServiceMain.getInstance().getResourceAsStream(resourceName);
		} catch (FileNotFoundException e) {
			logger.error("Missing Services resource: " + resourceName, e);
		}
        
		return result;
	}

	static public BlobOutput getBlobOutput(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient,
			String repositoryId,
			StringBuffer outMimeType) throws TransactionException, DocumentNotFoundException {
		BlobOutput result = null;
		
		boolean repoSessionCleanup = false;
		CoreSessionInterface repoSession = (CoreSessionInterface)ctx.getCurrentRepositorySession();
		if (repoSession == null) {
			repoSession = getRepositorySession(ctx, repositoryClient);
			repoSessionCleanup = true;
		}
		
		try {
			result = getBlobOutput(ctx, repoSession, repositoryId, null, true, outMimeType);
			if (outMimeType.length() == 0) {
				BlobsCommon blobsCommon = result.getBlobsCommon();
				String mimeType = blobsCommon.getMimeType();
				outMimeType.append(mimeType);
			}			
		} catch (DocumentException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			} else {
				logger.error(e.getMessage());
			}
		} finally {
			if (repoSessionCleanup == true) {
				releaseRepositorySession(ctx, repositoryClient, repoSession);
			}
		}
		
		return result;
	}
	
	//
	//  If the blob is not too big, we return a ByteArrayInputStream.  Otherwise, we return Nuxeo's InputStream
	//  which is usually a FileInputStream.
	//
	static private InputStream getInputStream(BlobsCommon blobsCommon, Blob blob) {
		InputStream result = null;
		
		if (blob != null) {
			try {
				InputStream blobStream = blob.getStream(); // By default, the result will be whatever stream Nuxeo returns to us.
				long blobSize = blobsCommon.getLength() != null ? Long.parseLong(blobsCommon.getLength()) : 0;
				if (blobSize > 0 && blobSize < MAX_IMAGE_BUFFER) {
					byte[] bytes = IOUtils.toByteArray(blobStream);
					blobStream.close(); // Close the InputStream that we got from Nuxeo since it's usually a FileInputStream -we definitely want FileInputStreams closed.
					result = new ByteArrayInputStream(bytes);
				} else {
					result = blobStream; // The blob is too large to put into a ByteArrayStream.
				}
			} catch (Exception e) {
				logger.error(String.format("Error getting the InputStream content for file %s.", blobsCommon.getName()), e);
				if (result != null) {
					try {
						result.close();
						result = null;
					} catch (Exception x) {
						logger.debug(String.format("Exception encountered during InputStream cleanup of file %s", blobsCommon.getName()), x);
					}
				}			
			}
		}
		
		return result;
	}
	
    static public Set<String> getPictureViewNameSet(CoreSessionInterface repoSession, String repositoryId) throws PropertyException {
        Set<String> result = null;
        
        DocumentModel docModel = repoSession.getDocument(new IdRef(repositoryId));
    	Collection<Property> views = docModel.getProperty(VIEWS_PROPERTY).getChildren();
    	if (views != null) {
    		result = new HashSet<String>();
	        for (Property property : views) {
	        	result.add((String)property.getValue(TITLE_PROPERTY));
	        }
    	}
        
        return result;
    }
	
	/**
	 * Gets the image.
	 * 
	 * @param repoSession
	 *            the repo session
	 * @param repositoryId
	 *            the repository id
	 * @param derivativeTerm
	 *            the derivative term
	 * @return the image
	 * @throws DocumentNotFoundException 
	 */
	static public BlobOutput getBlobOutput(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			CoreSessionInterface repoSession,
			String repositoryId,
			String derivativeTerm,
			Boolean getContentFlag,
			StringBuffer outMimeType) throws DocumentNotFoundException {
		BlobOutput result = new BlobOutput();
		boolean isNonImageDerivative = false;

		if (repositoryId != null && repositoryId.isEmpty() == false)
			try {
				IdRef documentRef = new IdRef(repositoryId);
				DocumentModel documentModel = repoSession.getDocument(documentRef);

				Blob docBlob = null;
				DocumentBlobHolder docBlobHolder = (DocumentBlobHolder) documentModel
						.getAdapter(BlobHolder.class);
				if (docBlobHolder instanceof PictureBlobHolder) {
					// if it is a PictureDocument then it has these
					// Nuxeo schemas: [dublincore, uid, picture, iptc, common, image_metadata]
					//
					// Need to add the "MultiviewPictureAdapter" support here to
					// get the view data, see above.
					//
					PictureBlobHolder pictureBlobHolder = (PictureBlobHolder) docBlobHolder;
					if (derivativeTerm != null) {
						docBlob = pictureBlobHolder.getBlob(derivativeTerm);
						if (docBlob == null) {
							String msg = String.format("Could not find derivative named '%s' for image/picture named '%s' Nuxeo Picture ID='%s'",
									derivativeTerm, documentModel.getName(), repositoryId);
							logger.error(msg);
							throw new DocumentNotFoundException(msg);
						}
						// Nuxeo derivatives are all JPEG
						outMimeType.append(MIME_JPEG); // All Nuxeo image derivatives are JPEG images.
					} else {
						docBlob = pictureBlobHolder.getBlob();
					}
				} else {
					docBlob = docBlobHolder.getBlob();
					if (derivativeTerm != null) { // If its a derivative request on a non-image blob, then return just a document image thumbnail
						isNonImageDerivative = true;
					}
				}
				
				if (docBlob == null && logger.isWarnEnabled()) {
					String msg = String.format("Could not retrieve document blob from Nuxeo document ID='%s' type='%s' name='%s'",
							repositoryId, documentModel.getType(), documentModel.getName());
					
					if (docBlob == null && docBlobHolder instanceof PictureBlobHolder && derivativeTerm != null) {
						msg = String.format("Could not retrieve image blob for derivative '%s' of Picture document with Nuxeo ID='%s' and name='%s'",
								derivativeTerm, repositoryId, documentModel.getName());
					}
					
					if (msg != null) {
						logger.warn(msg);
					}
				}

				//
				// Create the result instance that will contain the blob metadata
				// and an InputStream with the bits if the 'getContentFlag' is
				// set.
				//
				BlobsCommon blobsCommon = createBlobsCommon(ctx, documentModel, docBlob, getContentFlag);
				result.setBlobsCommon(blobsCommon);
				if (getContentFlag == true) {
					InputStream remoteStream = null;
					if (isNonImageDerivative == false) {
						//remoteStream = docBlob.getStream();
						remoteStream = getInputStream(blobsCommon, docBlob); // CSPACE-6110 - For small files, return a byte array instead of a file stream
					} else { // If its a derivative request on a non-image blob, then return just a document image thumbnail
						String docBlobMimetype = docBlob.getMimeType();
						switch(docBlobMimetype) {
							case MIME_CSV:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_CSV);
								break;
							case MIME_DOC:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_DOC);
								break;
							case MIME_DOCX:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_DOCX);
								break;
							case MIME_MP3:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_MP3);
								break;
							case MIME_PDF:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_PDF);
								break;
							case MIME_PPT:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_PPT);
								break;
							case MIME_PPTX:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_PPTX);
								break;
							case MIME_RTF:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_RTF);
								break;
							case MIME_XLS:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_XLS);
								break;
							case MIME_XLSX:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_XLSX);
								break;
							case MIME_ZIP:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_ZIP);
								break;
							default:
								remoteStream = getResource(DOCUMENT_PLACEHOLDER_IMAGE);
						}
						outMimeType.append(MIME_JPEG);
					}
//					BufferedInputStream bufferedInputStream = new BufferedInputStream(
//							remoteStream); 	
//					result.setBlobInputStream(bufferedInputStream);
					result.setBlobInputStream(remoteStream);
					result.setBlobFile(docBlob.getFile());
				}
			} catch (DocumentNotFoundException d) {
				throw d;
			} catch (Exception e) {
				if (logger.isErrorEnabled() == true) {
					logger.error(e.getMessage(), e);
				}
				result = null;
			}

		return result;
	}
	
}

/*
 * Notes and code snippets about Nuxeo's support for binaries and image
 * documents.
 */

/*
 * 
 * 
 * MultiviewPictureAdapter org.nuxeo.ecm.platform.picture.api.adapters
 * PictureResourceAdapter pictureResourceAdapter = (PictureResourceAdapter)
 * documentModel.getAdapter(PictureResourceAdapter.class); String thumbnailPath
 * = pictureResourceAdapter.getViewXPath("Thumbnail");
 * 
 * Map<String,Serializable> blobHolderProps = docBlobHolder.getProperties();
 * String filePath = docBlobHolder.getFilePath(); List<Blob> docBlobs =
 * docBlobHolder.getBlobs();
 * 
 * stream = new FileInputStream(fileUploadHolder.getTempFile());
 * 
 * public String addFile(InputStream fileUpload, String fileName) fileName =
 * FileUtils.getCleanFileName(fileName); DocumentModel currentDocument =
 * navigationContext.getCurrentDocument(); String path =
 * currentDocument.getPathAsString(); Blob blob =
 * FileUtils.createSerializableBlob(fileUpload, fileName, null);
 * 
 * DocumentModel createdDoc = getFileManagerService().createDocumentFromBlob(
 * documentManager, blob, path, true, fileName);
 * eventManager.raiseEventsOnDocumentSelected(createdDoc);
 * 
 * protected FileManager fileManager;
 * 
 * protected FileManager getFileManagerService() throws ClientException { if
 * (fileManager == null) { try { fileManager =
 * Framework.getService(FileManager.class); } catch (Exception e) {
 * log.error("Unable to get FileManager service ", e); throw new
 * ClientException("Unable to get FileManager service ", e); } } return
 * fileManager; }
 */

/*
 * RepositoryService repositoryService = (RepositoryService)
 * Framework.getRuntime().getComponent( RepositoryService.NAME);
 * RepositoryManager repositoryManager =
 * repositoryService.getRepositoryManager(); RepositoryDescriptor descriptor =
 * repositoryManager.getDescriptor(repositoryName); DefaultBinaryManager
 * binaryManager = new DefaultBinaryManager(
 * SQLRepository.getDescriptor(descriptor)));
 * 
 * File storageDir = binaryManager.getStorageDir(); SQLBlob blob = (SQLBlob)
 * doc.getPropertyValue("schema:blobField"); File file =
 * binaryManager.getFileForDigest( blob.getBinary().getDigest(), false);
 */

/*
 * RepositoryInstance.getStreamURI()
 * 
 * String getStreamURI(String blobPropertyId) throws ClientException
 * 
 * Returns an URI identifying the stream given the blob property id. This method
 * should be used by a client to download the data of a blob property.
 * 
 * The blob is fetched from the repository and the blob stream is registered
 * against the streaming service so the stream will be available remotely
 * through stream service API.
 * 
 * After the client has called this method, it will be able to download the
 * stream using streaming server API.
 * 
 * Returns: an URI identifying the remote stream Throws: ClientException
 */

/*
 * A blob contains usually large data.
 * 
 * Document fields holding Blob data are by default fetched in a lazy manner.
 * 
 * A Blob object hides the data source and it also describes data properties
 * like the encoding or mime-type.
 * 
 * The encoding is used to decode Unicode text content that was stored in an
 * encoded form. If not encoding is specified, the default java encoding is
 * used. The encoding is ignored for binary content.
 * 
 * When retrieving the content from a document, it will be returned as source
 * content instead of returning the content bytes.
 * 
 * The same is true when setting the content for a document: you set a content
 * source and not directly the content bytes. Ex:
 * 
 * File file = new File("/tmp/index.html"); FileBlob fb = new FileBlob(file);
 * fb.setMimeType("text/html"); fb.setEncoding("UTF-8"); // this specifies that
 * content bytes will be stored as UTF-8 document.setProperty("file", "content",
 * fb);
 * 
 * 
 * Then you may want to retrieve the content as follow:
 * 
 * Blob blob = document.getProperty("file:content"); htmlDoc = blob.getString();
 * // the content is decoded from UTF-8 into a java string
 */

