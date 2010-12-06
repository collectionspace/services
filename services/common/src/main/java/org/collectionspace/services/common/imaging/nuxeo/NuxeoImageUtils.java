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
 * Copyright © 2009 {Contributing Institution}
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
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.ServiceManager;
import org.nuxeo.runtime.api.ServiceDescriptor;
import org.nuxeo.runtime.services.streaming.RemoteInputStream;

//import org.nuxeo.common.utils.FileUtils;

import org.nuxeo.ecm.platform.picture.api.adapters.PictureResourceAdapter;
import org.nuxeo.ecm.platform.mimetype.MimetypeDetectionException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolder;
import org.nuxeo.ecm.platform.picture.extension.ImagePlugin;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.filemanager.service.FileManagerService;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolderFactory;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolder;

import org.nuxeo.ecm.core.repository.RepositoryDescriptor;
import org.nuxeo.ecm.core.repository.RepositoryManager;

//import org.nuxeo.ecm.core.api.repository.RepositoryManager;
//import org.nuxeo.ecm.core.api.repository.Repository;


import org.nuxeo.ecm.core.repository.RepositoryService;
import org.nuxeo.runtime.model.ComponentManager;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.impl.ComponentManagerImpl;
//import org.nuxeo.ecm.core.api.ejb.DocumentManagerBean;
//import org.nuxeo.ecm.core.storage.sql.RepositoryImpl;
//import org.nuxeo.ecm.core.storage.sql.Repository;
import org.nuxeo.ecm.core.storage.sql.BinaryManager;
import org.nuxeo.ecm.core.storage.sql.DefaultBinaryManager;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLRepository;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;
//import org.nuxeo.ecm.core.storage.sql.RepositoryDescriptor;

//import org.nuxeo.ecm.core.api.DocumentResolver;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.DocumentBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.BlobHolderAdapterService;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;

import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.nuxeo.ecm.core.repository.jcr.testing.RepositoryOSGITestCase;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.FileUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class NuxeoImageUtils.
 */
public class NuxeoImageUtils {
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(NuxeoImageUtils.class);
    	
//	static DefaultBinaryManager binaryManager = new DefaultBinaryManager(); //can we get this from Nuxeo? i.e., Framework.getService(BinaryManger.class)

//	/** The temp file name. */
//static String tempFileName = "sunset.jpg";
//	
//	/** The file separator. */
//	static String fileSeparator = System.getProperty("file.separator");
//	
//	/** The cur dir. */
//	static String curDir = System.getProperty("user.dir");
	
	/**
	 * Instantiates a new nuxeo image utils.
	 */
	NuxeoImageUtils() {
		//empty constructor
	}
	
	/**
	 * Logger setup.
	 */
	public static void loggerSetup() {
		//empty method
	}	
	
	static private File getBlobFile(RepositoryInstance ri, DocumentModel documentModel, Blob blob) {
		DefaultBinaryManager binaryManager = null;
		RepositoryDescriptor descriptor = null;
		
		
		try {
			ServiceManager sm = (ServiceManager) Framework.getService(ServiceManager.class);
			ServiceDescriptor[] sd = sm.getServiceDescriptors();
			
	        RepositoryService repositoryService1 = (RepositoryService) Framework.getRuntime().getComponent(
	                RepositoryService.NAME);
	        RepositoryService repositoryService2 = (RepositoryService) Framework.getRuntime().getService(
	                RepositoryService.class);
	        RepositoryService repositoryService3 = (RepositoryService) Framework.getService(
	                RepositoryService.class);
	        RepositoryService repositoryService4 = (RepositoryService) Framework.getLocalService(
	                RepositoryService.class);
	        ComponentManager componentManager1 = (ComponentManager) Framework.getService(ComponentManager.class);
	        ComponentManager componentManager2 = (ComponentManager) Framework.getService(ComponentManagerImpl.class);
	        
	        
//	        RepositoryManager repositoryManager2 = (RepositoryManager) Framework.getService(RepositoryManager.class);
//	        Repository repository = repositoryManager2.getDefaultRepository();
//	        Map<String, String> repositoryMap = repository.getProperties();
//	        String streamURI = ri.getStreamURI(arg0)

	        String repositoryName = documentModel.getRepositoryName();
//	        RepositoryManager repositoryManager2 = (RepositoryManager) Framework.getService(RepositoryManager.class);	        
	        RepositoryManager repositoryManager = repositoryService1.getRepositoryManager();
	        descriptor = repositoryManager.getDescriptor(repositoryName);
	        
            binaryManager = new DefaultBinaryManager();

            File storageDir = binaryManager.getStorageDir();
//            SQLBlob blob = (SQLBlob) doc.getPropertyValue("schema:blobField");
            File file = binaryManager.getFileForDigest(
                blob.getDigest(), false); 
	        
//	        binaryManager = new DefaultBinaryManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        try {
			binaryManager.initialize(
			            SQLRepository.getDescriptor(descriptor));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        File storageDir = binaryManager.getStorageDir();
//        SQLBlob blob = (SQLBlob) documentModel.getPropertyValue("schema:blobField");
        File file = binaryManager.getFileForDigest(
            blob.getDigest(), false);
        
        return file;
	}
	
	/**
	 * Returns a schema, given the name of a schema.
	 *
	 * @param schemaName  a schema name.
	 * @return  a schema.
	 */
	private static Schema getSchemaFromName(String schemaName) {
		SchemaManager schemaManager = null;
		try {
			schemaManager = Framework.getService(SchemaManager.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return schemaManager != null ? schemaManager.getSchema(schemaName) : null;
	}
		
	/**
	 * Gets the blob.
	 *
	 * @param nuxeoSession the nuxeo session
	 * @param id the id
	 * @return the blob
	 */
	static private Blob getBlob(RepositoryInstance nuxeoSession, String id) {
		Blob result = null;
		
		try {
			Repository repository = nuxeoSession.getRepository();
//			binaryManager.initialize(new RepositoryDescriptor());
//			binaryManager.getBinary("a4cac052ae0281979f2dcf5ab2e61a6c");
	//		DocumentResolver.resolveReference(nuxeoSession, documentRef);
			//binaryManager = repository.getBinaryManager();
	//		documentModel.getr
		} catch (Exception x) {
			x.printStackTrace();
		}
		
		return result;
	}
	
    /**
     * Gets the type service.
     *
     * @return the type service
     * @throws ClientException the client exception
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
	 * @param fis the fis
	 * @return the bytes
	 */
	private static byte[] getBytes(InputStream fis) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[128 * 1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); 
                //no doubt here is 0
                /*Writes len bytes from the specified byte array starting at offset 
                off to this byte array output stream.*/
                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        byte[] bytes = bos.toByteArray();
        //bytes is the ByteArray we need
        return bytes;
	}
	
    /**
     * Creates the serializable blob.
     *
     * @param fileInputStream the file input stream
     * @param filename the filename
     * @param mimeType the mime type
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
            MimetypeRegistry mimeService = Framework.getService(MimetypeRegistry.class);
            String detectedMimeType = mimeService.getMimetypeFromFilenameAndBlobWithDefault(
                    filename, blob, null);
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
     * @param file the input stream holding data
     * @param filename the file name. Will be set on the blob and will used for
     * mimetype detection.
     * @param mimeType the detected mimetype at upload. Can be null. Will be
     * verified by the mimetype service.
     * @return the blob
     */
    private static Blob createStreamingBlob(InputStream file,
            String filename, String mimeType) {
        Blob blob = null;
        try {
            // persisting the blob makes it possible to read the binary content
            // of the request stream several times (mimetype sniffing, digest
            // computation, core binary storage)
            blob = StreamingBlob.createFromStream(file, mimeType).persist();
            // filename
            if (filename != null) {
                filename = getCleanFileName(filename);
            }
            blob.setFilename(filename);
            // mimetype detection
            MimetypeRegistry mimeService = Framework.getService(MimetypeRegistry.class);
            String detectedMimeType = mimeService.getMimetypeFromFilenameAndBlobWithDefault(
                    filename, blob, null);
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
	
    /**
     * Returns a clean filename, stripping upload path on client side.
     * <p>
     * Fixes NXP-544
     * </p>
     *
     * @param filename the filename
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
     * @throws ClientException the client exception
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
    
    /**
     * Creates the picture.
     *
     * @param ctx the ctx
     * @param repoSession the repo session
     * @param filePath the file path
     * @return the string
     */
    public static String createPicture(ServiceContext ctx,
    		RepositoryInstance repoSession,
    		File blobFile) {
		String result = null;
		
		try {
			String nuxeoWspaceId = ctx.getRepositoryWorkspaceId();
            DocumentRef nuxeoWspace = new IdRef(nuxeoWspaceId);
            DocumentModel wspaceDoc = repoSession.getDocument(nuxeoWspace);
            
    		FileInputStream inputStream = new FileInputStream(blobFile);            
    		if (inputStream != null) {
    			result = createImage(repoSession, wspaceDoc,
    					inputStream, blobFile.getName(), null);
    		}            
		} catch (Exception e) {
			logger.error("Could not create image blob", e);
		}		
		
		return result;
    }
	    
	/**
	 * Creates the image blob.
	 *
	 * @param nuxeoSession the nuxeo session
	 * @param blobLocation the blob location
	 * @param file the file
	 * @param fileName the file name
	 * @param mimeType the mime type
	 * @return the string
	 */
	static public String createImage(RepositoryInstance nuxeoSession,
			DocumentModel blobLocation,
			InputStream file,
            String fileName, 
            String mimeType) {
		String result = null;
				
		try {
			Blob fileBlob = createStreamingBlob(file, fileName, mimeType);
			String digestAlgorithm = getFileManagerService().getDigestAlgorithm(); //Need some way on initializing the FileManager with a call.
//			List<String> permissions = nuxeoSession.getAvailableSecurityPermissions();
			DocumentModel documentModel = getFileManagerService().createDocumentFromBlob(nuxeoSession,
					fileBlob, blobLocation.getPathAsString(), true, fileName);
			result = documentModel.getId();
		} catch (Exception e) {
			logger.error("Could not create new image blob", e);
		}
		
		return result;
	}
	
	
    /**
     * Gets the picture.
     *
     * @param ctx the ctx
     * @param repoSession the repo session
     * @param blobId the blob id
     * @param derivativeTerm the derivative term
     * @return the picture
     */
    public static InputStream getPicture(ServiceContext ctx, RepositoryInstance repoSession,
    		String blobId, String derivativeTerm) {
    	return getImage(repoSession, blobId, derivativeTerm);
    }
    	
	/**
	 * Gets the image.
	 *
	 * @param repoSession the repo session
	 * @param repositoryId the repository id
	 * @param derivativeTerm the derivative term
	 * @return the image
	 */
	static public InputStream getImage(RepositoryInstance repoSession,
			String repositoryId, String derivativeTerm) {
		InputStream result = null;

		try {
			IdRef documentRef = new IdRef(repositoryId);
			DocumentModel documentModel = repoSession.getDocument(documentRef);

//			documentModel.getAdapter(PictureResourceAdapter.class);
//			DocumentBlobHolder docBlobHolder = (DocumentBlobHolder)documentModel.getAdapter(BlobHolder.class);
//			Blob docBlob = docBlobHolder.getBlob();
//			Map<String,Serializable> blobHolderProps = docBlobHolder.getProperties();
//			String filePath = docBlobHolder.getFilePath();
//			List<Blob> docBlobs = docBlobHolder.getBlobs();
			
			PictureBlobHolderFactory blobHolderFactory = new PictureBlobHolderFactory();
			PictureBlobHolder pictureBlobHolder = (PictureBlobHolder) blobHolderFactory.getBlobHolder(documentModel);
			Blob pictureBlob = null;
			if (derivativeTerm != null) {
				pictureBlob = pictureBlobHolder.getBlob(derivativeTerm);
			} else {
				pictureBlob = pictureBlobHolder.getBlob();
			}
			
			InputStream remoteStream = pictureBlob.getStream();
			File tmpFile = FileUtils.createTmpFile(remoteStream);
			result = new FileInputStream(tmpFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return result;
	}
}

/*
stream = new FileInputStream(fileUploadHolder.getTempFile());

				public String addFile(InputStream fileUpload, String fileName)
            fileName = FileUtils.getCleanFileName(fileName);
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            String path = currentDocument.getPathAsString();
            Blob blob = FileUtils.createSerializableBlob(fileUpload, fileName,
                    null);

            DocumentModel createdDoc = getFileManagerService().createDocumentFromBlob(
                    documentManager, blob, path, true, fileName);
            eventManager.raiseEventsOnDocumentSelected(createdDoc);

    protected FileManager fileManager;

    protected FileManager getFileManagerService() throws ClientException {
        if (fileManager == null) {
            try {
                fileManager = Framework.getService(FileManager.class);
            } catch (Exception e) {
                log.error("Unable to get FileManager service ", e);
                throw new ClientException("Unable to get FileManager service ",
                        e);
            }
        }
        return fileManager;
    }
*/

/*
           RepositoryService repositoryService = (RepositoryService) Framework.getRuntime().getComponent(
                    RepositoryService.NAME);
            RepositoryManager repositoryManager = repositoryService.getRepositoryManager();
            RepositoryDescriptor descriptor = repositoryManager.getDescriptor(repositoryName);
            DefaultBinaryManager binaryManager = new DefaultBinaryManager(
                        SQLRepository.getDescriptor(descriptor)));

            File storageDir = binaryManager.getStorageDir();
            SQLBlob blob = (SQLBlob) doc.getPropertyValue("schema:blobField");
            File file = binaryManager.getFileForDigest(
                blob.getBinary().getDigest(), false); 
*/

/*
RepositoryInstance.getStreamURI()

String getStreamURI(String blobPropertyId)
                    throws ClientException

    Returns an URI identifying the stream given the blob property id. This method should be used by a client to download the data of a blob property.

    The blob is fetched from the repository and the blob stream is registered against the streaming service so the stream will be available remotely through stream service API.

    After the client has called this method, it will be able to download the stream using streaming server API.

    Returns:
        an URI identifying the remote stream 
    Throws:
        ClientException

 */

/*
A blob contains usually large data.

Document fields holding Blob data are by default fetched in a lazy manner.

A Blob object hides the data source and it also describes data properties like the encoding or mime-type.

The encoding is used to decode Unicode text content that was stored in an encoded form. If not encoding is specified, the default java encoding is used. The encoding is ignored for binary content.

When retrieving the content from a document, it will be returned as source content instead of returning the content bytes.

The same is true when setting the content for a document: you set a content source and not directly the content bytes. Ex:

 File file = new File("/tmp/index.html");
 FileBlob fb = new FileBlob(file);
 fb.setMimeType("text/html");
 fb.setEncoding("UTF-8"); // this specifies that content bytes will be stored as UTF-8
 document.setProperty("file", "content", fb);
 

Then you may want to retrieve the content as follow:

 Blob blob = document.getProperty("file:content");
 htmlDoc = blob.getString(); // the content is decoded from UTF-8 into a java string
  */
