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

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.runtime.api.Framework;
//import org.nuxeo.runtime.api.ServiceManager;
//import org.nuxeo.runtime.api.ServiceDescriptor;

//import org.nuxeo.common.utils.FileUtils;

import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.ecm.platform.picture.api.PictureView;

import org.nuxeo.ecm.platform.mimetype.MimetypeDetectionException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolder;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.types.TypeManager;

import org.nuxeo.ecm.core.repository.RepositoryDescriptor;
import org.nuxeo.ecm.core.repository.RepositoryManager;

import org.nuxeo.ecm.core.repository.RepositoryService;
//import org.nuxeo.runtime.model.ComponentManager;
//import org.nuxeo.runtime.model.impl.ComponentManagerImpl;
//import org.nuxeo.ecm.core.api.ejb.DocumentManagerBean;
//import org.nuxeo.ecm.core.storage.sql.RepositoryImpl;
//import org.nuxeo.ecm.core.storage.sql.Repository;
import org.nuxeo.ecm.core.storage.sql.Binary;
import org.nuxeo.ecm.core.storage.sql.BinaryManager;
import org.nuxeo.ecm.core.storage.sql.DefaultBinaryManager;
import org.nuxeo.ecm.core.storage.sql.RepositoryImpl;
import org.nuxeo.ecm.core.storage.sql.RepositoryResolver;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLRepository;
//import org.nuxeo.ecm.core.storage.sql.RepositoryDescriptor;

//import org.nuxeo.ecm.core.api.DocumentResolver;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.DocumentBlobHolder;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;

import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.nuxeo.ecm.core.repository.jcr.testing.RepositoryOSGITestCase;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.blob.DimensionSubGroup;
import org.collectionspace.services.blob.DimensionSubGroupList;
import org.collectionspace.services.blob.MeasuredPartGroup;
import org.collectionspace.services.blob.MeasuredPartGroupList;
//import org.collectionspace.services.blob.BlobsCommonList;
//import org.collectionspace.services.blob.BlobsCommonList.BlobListItem;
import org.collectionspace.services.jaxb.BlobJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.extension.thumbnail.ThumbnailConstants;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.blob.BlobOutput;

import org.collectionspace.services.config.service.ListResultField;


/**
 * The Class NuxeoImageUtils.
 */
public class NuxeoImageUtils {
		
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(NuxeoImageUtils.class);

	public static final String DOCUMENT_PLACEHOLDER_IMAGE = "documentImage.jpg";
	public static final String DOCUMENT_MISSING_PLACEHOLDER_IMAGE = "documentImageMissing.jpg";
	public static final String MIME_JPEG = "image/jpeg";
	/*
	 * FIXME: REM - These constants should be coming from configuration and NOT
	 * hard coded.
	 */
	public static final String DERIVATIVE_ORIGINAL = "Original";
	public static final String DERIVATIVE_ORIGINAL_TAG = DERIVATIVE_ORIGINAL
			+ "_";

	public static final String DERIVATIVE_ORIGINAL_JPEG = "OriginalJpeg";
	public static final String DERIVATIVE_ORIGINAL_JPEG_TAG = DERIVATIVE_ORIGINAL_JPEG
			+ "_";

	public static final String DERIVATIVE_MEDIUM = "Medium";
	public static final String DERIVATIVE_MEDIUM_TAG = DERIVATIVE_MEDIUM + "_";

	public static final String DERIVATIVE_THUMBNAIL = "Thumbnail";
	public static final String DERIVATIVE_THUMBNAIL_TAG = DERIVATIVE_THUMBNAIL
			+ "_";

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

	private static final int THUMB_SIZE_HEIGHT = 100;

	private static final int THUMB_SIZE_WIDTH = 75;

	// static DefaultBinaryManager binaryManager = new DefaultBinaryManager();
	// //can we get this from Nuxeo? i.e.,
	// Framework.getService(BinaryManger.class)

	// /** The temp file name. */
	// static String tempFileName = "sunset.jpg";
	//
	// /** The file separator. */
	// static String fileSeparator = System.getProperty("file.separator");
	//
	// /** The cur dir. */
	// static String curDir = System.getProperty("user.dir");

	/**
	 * Instantiates a new nuxeo image utils.
	 */
	NuxeoImageUtils() {
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

	// FIXME: REM - This needs to be configuration-bases and NOT hard coded!
	// FIXME: REM - Use MultiviewPicture adapter to get some of this information
	static private String getDerivativeUri(String uri, String derivativeName) {
		String result = DERIVATIVE_UNKNOWN;

		if (derivativeName.startsWith(DERIVATIVE_ORIGINAL_TAG) == true) {
			result = DERIVATIVE_ORIGINAL;
		} else if (derivativeName.startsWith(DERIVATIVE_ORIGINAL_JPEG_TAG) == true) {
			result = DERIVATIVE_ORIGINAL_JPEG;
		} else if (derivativeName.startsWith(DERIVATIVE_MEDIUM_TAG) == true) {
			result = DERIVATIVE_MEDIUM;
		} else if (derivativeName.startsWith(DERIVATIVE_THUMBNAIL_TAG) == true) {
			result = DERIVATIVE_THUMBNAIL;
		}

		return uri + result + "/" + BlobInput.URI_CONTENT_PATH;
	}

	static private HashMap<String, Object> createBlobListItem(Blob blob,
			String uri) {
		HashMap<String, Object> item = new HashMap<String, Object>();

		String value = blob.getEncoding();
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
		value = getDerivativeUri(uri, blob.getFilename());
		if (value != null && !value.trim().isEmpty()) {
			item.put(BlobJAXBSchema.uri, value);
		}

		return item;
	}

	static public CommonList getBlobDerivatives(RepositoryInstance repoSession,
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

		IdRef documentRef = new IdRef(repositoryId);
		DocumentModel documentModel = repoSession.getDocument(documentRef);
		DocumentBlobHolder docBlobHolder = (DocumentBlobHolder) documentModel
				.getAdapter(BlobHolder.class);
		List<Blob> docBlobs = docBlobHolder.getBlobs();
		// List<BlobListItem> blobListItems = result.getBlobListItem();
		HashMap<String, Object> item = null;
		for (Blob blob : docBlobs) {
			if (blob != null) {
				item = createBlobListItem(blob, uri);
				commonList.addItem(item);
			}
		}

		return commonList;
	}

	/*
	 * [dublincore, uid, picture, iptc, common, image_metadata]
	 */
	static private Map<String, Object> getMetadata(Blob nuxeoBlob)
			throws Exception {
		ImagingService service = Framework.getService(ImagingService.class);
		Map<String, Object> metadataMap = service.getImageMetadata(nuxeoBlob);
		return metadataMap;
	}

	private static String[] imageTypes = {"jpeg", "bmp", "gif", "png", "tiff", "octet-stream"};
	static private boolean isImageMedia(Blob nuxeoBlob) {
		boolean result = false;
		
		String mimeType = nuxeoBlob.getMimeType().toLowerCase().trim();
		String[] parts = mimeType.split("/"); // split strings like "application/xml" into an array of two strings
		if (parts.length == 2) {
			for (String type : imageTypes) {
				if (parts[1].equalsIgnoreCase(type)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	static private MeasuredPartGroupList getDimensions(
			DocumentModel documentModel, Blob nuxeoBlob) {
		MeasuredPartGroupList result = null;
		
		if (isImageMedia(nuxeoBlob) == true) try {
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

	static private BlobsCommon createBlobsCommon(DocumentModel documentModel,
			Blob nuxeoBlob) {
		BlobsCommon result = new BlobsCommon();

		if (documentModel != null) {
			result.setMimeType(nuxeoBlob.getMimeType());
			result.setName(nuxeoBlob.getFilename());
			result.setLength(Long.toString(nuxeoBlob.getLength()));
			result.setRepositoryId(documentModel.getId());
			MeasuredPartGroupList measuredPartGroupList = getDimensions(
					documentModel, nuxeoBlob);
			if (measuredPartGroupList != null) {
				result.setMeasuredPartGroupList(measuredPartGroupList);
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
					if (logger.isTraceEnabled()) {
						logger.trace(errorMsg, e);
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

	/*
	 * This is a prototype method that is not currently used as of 1/1/2012.  However,
	 * it may be useful now that we've transitioned to using an embedded Nuxeo server.
	 */
	static private File getBlobFile(RepositoryInstance ri,
			DocumentModel documentModel, Blob blob) {
		DefaultBinaryManager binaryManager = null;
		RepositoryDescriptor descriptor = null;
		File file = null;

		try {
			RepositoryService repositoryService1 = (RepositoryService) Framework
					.getRuntime().getComponent(RepositoryService.NAME);

			String repositoryName = documentModel.getRepositoryName();
			RepositoryManager repositoryManager = repositoryService1
					.getRepositoryManager();
			descriptor = repositoryManager.getDescriptor(repositoryName);

// Keep this code around for future work/enhancements			
//			binaryManager = new DefaultBinaryManager();
//
//			File storageDir = binaryManager.getStorageDir();
//			// SQLBlob blob = (SQLBlob)
//			// doc.getPropertyValue("schema:blobField");
//			File file = binaryManager.getFileForDigest(blob.getDigest(), false);

		} catch (Exception e) {
			e.printStackTrace();
		}

// Keep this code around for future work/enhancements
//		try {
//			binaryManager.initialize(SQLRepository.getDescriptor(descriptor));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

// Keep this code around for future work/enhancements
//		File storageDir = binaryManager.getStorageDir();
//		SQLBlob blob = (SQLBlob)
//		documentModel.getPropertyValue("schema:blobField");
//		File file = binaryManager.getFileForDigest(blob.getDigest(), false);

		return file;
	}

	/**
	 * Returns a schema, given the name of a schema.  Possibly usefule in the future
	 * 
	 * @param schemaName
	 *            a schema name.
	 * @return a schema.
	 */
	private static Schema getSchemaFromName(String schemaName) {
		SchemaManager schemaManager = null;
		try {
			schemaManager = Framework.getService(SchemaManager.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return schemaManager != null ? schemaManager.getSchema(schemaName)
				: null;
	}

	/**
	 * Gets the blob.  Not in use now, but might be useful in the future.
	 * 
	 * @param nuxeoSession
	 *            the nuxeo session
	 * @param id
	 *            the id
	 * @return the blob
	 */
	static private Blob getBlob(RepositoryInstance nuxeoSession, String id) {
		Blob result = null;

		try {
			Repository repository = nuxeoSession.getRepository();
			// binaryManager.initialize(new RepositoryDescriptor());
			// binaryManager.getBinary("a4cac052ae0281979f2dcf5ab2e61a6c");
			// DocumentResolver.resolveReference(nuxeoSession, documentRef);
			// binaryManager = repository.getBinaryManager();
			// documentModel.getr
		} catch (Exception x) {
			x.printStackTrace();
		}

		return result;
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
	 * Gets the bytes.
	 * 
	 * @param fis
	 *            the fis
	 * @return the bytes
	 */
	private static byte[] getBytes(InputStream fis) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[128 * 1024];
		try {
			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				bos.write(buf, 0, readNum);
				// no doubt here is 0
				/*
				 * Writes len bytes from the specified byte array starting at
				 * offset off to this byte array output stream.
				 */
				System.out.println("read " + readNum + " bytes,");
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
		byte[] bytes = bos.toByteArray();
		// bytes is the ByteArray we need
		return bytes;
	}

	/**
	 * Creates the serializable blob.  We may need this code, do not remove.
	 * 
	 * @param fileInputStream
	 *            the file input stream
	 * @param filename
	 *            the filename
	 * @param mimeType
	 *            the mime type
	 * @return the blob
	 */
	private static Blob createSerializableBlob(InputStream fileInputStream,
			String filename, String mimeType) {
		Blob blob = null;
		try {
			// persisting the blob makes it possible to read the binary content
			// of the request stream several times (mimetype sniffing, digest
			// computation, core binary storage)
			byte[] bytes = getBytes(fileInputStream);
			blob = new ByteArrayBlob(bytes);
			// filename
			if (filename != null) {
				filename = getCleanFileName(filename);
			}
			blob.setFilename(filename);
			// mimetype detection
			MimetypeRegistry mimeService = Framework
					.getService(MimetypeRegistry.class);
			String detectedMimeType = mimeService
					.getMimetypeFromFilenameAndBlobWithDefault(filename, blob,
							null);
			if (detectedMimeType == null) {
				if (mimeType != null) {
					detectedMimeType = mimeType;
				} else {
					// default
					detectedMimeType = "application/octet-stream";
				}
			}
			blob.setMimeType(detectedMimeType);
		} catch (MimetypeDetectionException e) {
			logger.error(String.format("could not fetch mimetype for file %s",
					filename), e);
		} catch (Exception e) {
			logger.error("", e);
		}
		return blob;
	}

	/**
	 * Creates a serializable blob from a stream, with filename and mimetype
	 * detection.
	 * 
	 * <p>
	 * Creates an in-memory blob if data is under 64K, otherwise constructs a
	 * serializable FileBlob which stores data in a temporary file on the hard
	 * disk.
	 * </p>
	 * 
	 * @param file
	 *            the input stream holding data
	 * @param filename
	 *            the file name. Will be set on the blob and will used for
	 *            mimetype detection.
	 * @param mimeType
	 *            the detected mimetype at upload. Can be null. Will be verified
	 *            by the mimetype service.
	 * @return the blob
	 */
	private static Blob createStreamingBlob(File file, String filename,
			String mimeType) {
		Blob blob = null;
		try {
			// persisting the blob makes it possible to read the binary content
			// of the request stream several times (mimetype sniffing, digest
			// computation, core binary storage)
			blob = StreamingBlob.createFromFile(file, mimeType).persist();
			// filename
			if (filename != null) {
				filename = getCleanFileName(filename);
			}
			blob.setFilename(filename);
			// mimetype detection
			MimetypeRegistry mimeService = Framework
					.getService(MimetypeRegistry.class);
			String detectedMimeType = mimeService
					.getMimetypeFromFilenameAndBlobWithDefault(filename, blob,
							null);
			if (detectedMimeType == null) {
				if (mimeType != null) {
					detectedMimeType = mimeType;
				} else {
					// default
					detectedMimeType = "application/octet-stream";
				}
			}
			blob.setMimeType(detectedMimeType);
		} catch (MimetypeDetectionException e) {
			logger.error(String.format("could not fetch mimetype for file %s",
					filename), e);
		} catch (IOException e) {
			logger.error("", e);
		} catch (Exception e) {
			logger.error("", e);
		}
		return blob;
	}

	private static Blob createNuxeoFileBasedBlob(File file) {
		Blob result = null;

		result = new FileBlob(file);
		return result;
	}

	/**
	 * Returns a clean filename, stripping upload path on client side.
	 * <p>
	 * Fixes NXP-544
	 * </p>
	 * 
	 * @param filename
	 *            the filename
	 * @return the clean file name
	 */
	private static String getCleanFileName(String filename) {
		String res = null;
		int lastWinSeparator = filename.lastIndexOf('\\');
		int lastUnixSeparator = filename.lastIndexOf('/');
		int lastSeparator = Math.max(lastWinSeparator, lastUnixSeparator);
		if (lastSeparator != -1) {
			res = filename.substring(lastSeparator + 1, filename.length());
		} else {
			res = filename;
		}
		return res;
	}

	/**
	 * Gets Nuxeo's file manager service.
	 * 
	 * @return the file manager service
	 * @throws ClientException
	 *             the client exception
	 */
	private static FileManager getFileManagerService() throws ClientException {
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
	
	private static BinaryManager getBinaryManagerService() throws ClientException {
		BinaryManager result = null;
		try {
			result = Framework.getService(BinaryManager.class);
		} catch (Exception e) {
			String msg = "Unable to get Nuxeo's BinaryManager service.";
			logger.error(msg, e);
			throw new ClientException("msg", e);
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
	public static BlobsCommon createBlobInRepository(ServiceContext ctx,
			RepositoryInstance repoSession,
			BlobInput blobInput,
			boolean purgeOriginal) throws Exception {
		BlobsCommon result = null;

		try {
			File blobFile = blobInput.getBlobFile();
			String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
			DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
			DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
			
            if (logger.isTraceEnabled()) {
	            for (String facet : wspaceDoc.getFacets()) {
	            	logger.trace("Facet: " + facet);
	            }
	            for (String docType : wspaceDoc.getDocumentType().getChildrenTypes()) {
	            	logger.trace("Child type: " + docType);
	            }
            }			

			result = createBlobInRepository(repoSession, wspaceDoc, purgeOriginal, blobFile, null /*mime type*/);
		} catch (Exception e) {
			logger.error("Could not create image blob", e);
			throw e;
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
	static public BlobsCommon createBlobInRepository(RepositoryInstance nuxeoSession,
			DocumentModel blobLocation,
			boolean purgeOriginal,
			File file,
			String mimeType) {
		BlobsCommon result = null;

		try {
			// Blob fileBlob = createStreamingBlob(blobFile, blobFile.getName(),
			// mimeType);
			Blob fileBlob = createNuxeoFileBasedBlob(file);
			String digestAlgorithm = getFileManagerService()
					.getDigestAlgorithm(); // Only call this because we seem to need some way of initializing Nuxeo's FileManager with a call.
			
			logger.debug("Start --> Starting call to Nuxeo to create the blob document."); // For example, see Nuxeo's DefaultPictureAdapter class for details
			DocumentModel documentModel = getFileManagerService()
					.createDocumentFromBlob(nuxeoSession, fileBlob,
							blobLocation.getPathAsString(), true,
							file.getName());
			logger.debug("Stop --> Finished calling Nuxeo to create the blob document.");

			result = createBlobsCommon(documentModel, fileBlob); // Now create our metadata resource document

			// If the sender wanted us to generate only derivatives, we need to purge/clear the original contents
			if (purgeOriginal == true && isPurgeAllowed(documentModel) == true) {
				// Empty the document model's "content" property -this does not delete the actual file/blob
				documentModel.setPropertyValue("file:content", (Serializable) null);
				
				if (documentModel.hasFacet(ImagingDocumentConstants.PICTURE_FACET)) {
					// Now with no content, the derivative listener wants to update the derivatives. So to
					// prevent the listener, we remove the "Picture" facet from the document
					NuxeoUtils.removeFacet(documentModel, ImagingDocumentConstants.PICTURE_FACET); // Removing this facet ensures the original derivatives are unchanged.
					nuxeoSession.saveDocument(documentModel);
					// Now that we've emptied the document model's content field, we can add back the Picture facet
					NuxeoUtils.addFacet(documentModel, ImagingDocumentConstants.PICTURE_FACET);
				}
				
				nuxeoSession.saveDocument(documentModel);
				// Next, we need to remove the actual file from Nuxeo's data directory
				DocumentBlobHolder docBlobHolder = (DocumentBlobHolder) documentModel
						.getAdapter(BlobHolder.class);
				boolean deleteSuccess = NuxeoUtils.deleteFileOfBlob(docBlobHolder.getBlob());
			}
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
	 */
	static public BlobOutput getBlobOutput(ServiceContext ctx,
			RepositoryInstance repoSession,
			String repositoryId,
			String derivativeTerm,
			Boolean getContentFlag,
			StringBuffer outMimeType) {
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
						// Nuxeo derivatives are all JPEG
						outMimeType.append(MIME_JPEG); // All Nuxeo image derivatives are JPEG images.
					} else {
						docBlob = pictureBlobHolder.getBlob();
					}
				} else {
					docBlob = docBlobHolder.getBlob();
					if (derivativeTerm != null) { // If its a derivative request on a non-image blob, then return just a document image thumnail
						isNonImageDerivative = true;
					}
				}

				//
				// Create the result instance that will contain the blob metadata
				// and an InputStream with the bits if the 'getContentFlag' is
				// set.
				//
				BlobsCommon blobsCommon = createBlobsCommon(documentModel, docBlob);
				result.setBlobsCommon(blobsCommon);
				if (getContentFlag == true) {
					InputStream remoteStream = null;
					if (isNonImageDerivative == false) {
						remoteStream = docBlob.getStream(); // This will fail if the blob's file has been deleted. FileNotFoundException thrown.
					} else {
						remoteStream = getResource(DOCUMENT_PLACEHOLDER_IMAGE);
						outMimeType.append(MIME_JPEG);
					}
					BufferedInputStream bufferedInputStream = new BufferedInputStream(
							remoteStream); 	
					result.setBlobInputStream(bufferedInputStream);
				}
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
