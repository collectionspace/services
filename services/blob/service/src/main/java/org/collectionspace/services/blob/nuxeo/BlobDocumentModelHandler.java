/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.blob.nuxeo;

import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobOutput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoBlobUtils;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.jaxb.BlobJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.dom4j.Element;

/**
 * The Class BlobDocumentModelHandler.
 */
public class BlobDocumentModelHandler
extends NuxeoDocumentModelHandler<BlobsCommon> {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(BlobDocumentModelHandler.class);

    //==============================================================================

	private String getDerivativePathBase(DocumentModel docModel) {
		return getServiceContextPath() + docModel.getName() + "/" +
			BlobInput.URI_DERIVATIVES_PATH + "/";
	}

	private BlobsCommon getCommonPartProperties(DocumentModel docModel) throws Exception {
		String label = getServiceContext().getCommonPartLabel();
		BlobsCommon result = new BlobsCommon();
		
		result.setData((String)	
				docModel.getProperty(label, BlobJAXBSchema.data));
		result.setDigest((String)
				docModel.getProperty(label, BlobJAXBSchema.digest));
		result.setEncoding((String)
				docModel.getProperty(label, BlobJAXBSchema.encoding));
		result.setLength((String)
				docModel.getProperty(label, BlobJAXBSchema.length));
		result.setMimeType((String)
				docModel.getProperty(label, BlobJAXBSchema.mimeType));
		result.setName((String)
				docModel.getProperty(label, BlobJAXBSchema.name));
		result.setRepositoryId((String)
				docModel.getProperty(label, BlobJAXBSchema.repositoryId));
		result.setUri(getServiceContextPath() + docModel.getName() + "/" +
				BlobInput.URI_CONTENT_PATH);
		
		return result;
	}
	
	private void setCommonPartProperties(DocumentModel documentModel,
			BlobsCommon blobsCommon) throws ClientException {
		try {
			String schemaName = getServiceContext().getCommonPartLabel();
			PayloadOutputPart outputPart = new PayloadOutputPart(schemaName, blobsCommon);
			Element element = outputPart.asElement();
			Map<String, Object> propertyMap = DocumentUtils.parseProperties(schemaName, element, getServiceContext());
			documentModel.setProperties(schemaName, propertyMap);
		} catch (Exception e) {
			throw new ClientException(e);
		}		
	}
	
	private void extractMetadata(String nuxeoImageID, String metadataLabel) {		
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        ObjectPartType partMeta = partsMetaMap.get(metadataLabel);

        if (partMeta != null) {
        	CoreSessionInterface repoSession = this.getRepositorySession();
			if (nuxeoImageID != null && nuxeoImageID.isEmpty() == false) try {
				IdRef documentRef = new IdRef(nuxeoImageID);
				DocumentModel docModel = repoSession.getDocument(documentRef);
	            Map<String, Object> unQObjectProperties = extractPart(docModel, metadataLabel);
	            if (unQObjectProperties != null) {
	            	addOutputPart(unQObjectProperties, metadataLabel, partMeta);
	            }
			} catch (Exception e) {
				logger.warn("Metadata extraction failed: " + e.getMessage());
			}
        } else {
        	logger.warn("Metadata extraction failed: Could not find tenant binding for schema type = " + metadataLabel);
        }
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractAllParts(org.collectionspace.services.common.document.DocumentWrapper)
	 */
	@Override
	public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
			throws Exception {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		BlobInput blobInput = BlobUtil.getBlobInput(ctx); // the blobInput was set by the Blob JAX-RS resource code and put into the service context
		CoreSessionInterface repoSession = this.getRepositorySession();
		DocumentModel docModel = wrapDoc.getWrappedObject();
		BlobsCommon blobsCommon = this.getCommonPartProperties(docModel);		
		String blobRepositoryId = blobsCommon.getRepositoryId(); //cache the value to pass to the blob retriever
		//
		// We're being asked for a list of blob derivatives, not the payload for a blob record.  FIXME: REM - This should be handled in a class called DerivativeDocumentHandler (need to create).
		//
		if (blobInput.isDerivativeListRequested() == true) {
	        List<ListResultField> resultsFields = getListItemsArray();
			CommonList blobsCommonList = NuxeoBlobUtils.getBlobDerivatives( //FIXME: REM - Need to replace "NuxeoImageUtils" with something more general like "BlobUtils" since we may support other blob types.
					repoSession, blobRepositoryId, resultsFields, getDerivativePathBase(docModel));
//			ctx.setProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY, blobsCommonList);
			blobInput.setDerivativeList(blobsCommonList);
			return;  //FIXME: REM - Don't like this exit point.  Perhaps derivatives should be a sub-resource with its own DerivativeDocumentHandler doc handler?
		}		

		String derivativeTerm = blobInput.getDerivativeTerm();
		Boolean getContentFlag = blobInput.isContentRequested();
		//
		// If we're being asked for either the content of the blob, the content of a derivative, or the payload for a derivative then
		// fall into this block of code.  Otherwise, we'll just call our parent to deal with a plain-old-blob payload.
		//
		if (derivativeTerm != null || getContentFlag == true) {
			StringBuffer mimeTypeBuffer = new StringBuffer();
			BlobOutput blobOutput = NuxeoBlobUtils.getBlobOutput(ctx, repoSession,
					blobRepositoryId, derivativeTerm, getContentFlag, mimeTypeBuffer);
			if (getContentFlag == true) {
				if (blobOutput != null) {
					blobInput.setContentStream(blobOutput.getBlobInputStream());
					blobInput.setBlobFile(blobOutput.getBlobFile());
				} else {
					blobInput.setContentStream(null);
				}
			}
	
			if (derivativeTerm != null) {
				// reset 'blobsCommon' if we have a derivative request
				blobsCommon = blobOutput.getBlobsCommon();
				blobsCommon.setUri(getDerivativePathBase(docModel) +
						derivativeTerm + "/" + BlobInput.URI_CONTENT_PATH);				
			}
			
			String mimeType = mimeTypeBuffer.toString();
			if (mimeType != null && !mimeType.isEmpty()) { // MIME type for derivatives might be different from original
				blobInput.setMimeType(mimeType);
				blobsCommon.setMimeType(mimeType);
			} else {
				blobInput.setMimeType(blobsCommon.getMimeType());  // Set the MIME type to the one in blobsCommon
			}
			
			blobsCommon.setRepositoryId(null); //hide the repository id from the GET results payload since it is private
			this.setCommonPartProperties(docModel, blobsCommon);
			// finish extracting the other parts by calling the parent
		} else {
			extractMetadata(blobRepositoryId, NuxeoBlobUtils.SCHEMA_IMAGE_METADATA);
			extractMetadata(blobRepositoryId, NuxeoBlobUtils.SCHEMA_IPTC);
		}
		
		//
		// Hide the Nuxeo repository ID of the Nuxeo blob since this is private
		//
		docModel.setProperty(ctx.getCommonPartLabel(), BlobJAXBSchema.repositoryId, null);	
		super.extractAllParts(wrapDoc);
	}

	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		BlobInput blobInput = BlobUtil.getBlobInput(ctx); // The blobInput should have been put into the context by the Blob or Media resource
		if (blobInput != null && blobInput.getBlobFile() != null) {    		
			boolean purgeOriginal = false;
			MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
			String purgeOriginalStr = queryParams.getFirst(BlobClient.BLOB_PURGE_ORIGINAL);
			if (purgeOriginalStr != null && purgeOriginalStr.isEmpty() == false) { // Find our if the caller wants us to purge/delete the original
				purgeOriginal = true;
			}
			//
			// If blobInput has a file then we just received a multipart/form-data file post or a URI query parameter
			//
			DocumentModel documentModel = wrapDoc.getWrappedObject();
			CoreSessionInterface repoSession = this.getRepositorySession();
	        
			BlobsCommon blobsCommon = NuxeoBlobUtils.createBlobInRepository(ctx, repoSession, blobInput, purgeOriginal, true);
			blobInput.setBlobCsid(documentModel.getName()); //Assumption here is that the documentModel "name" field is storing a CSID
	
	        PoxPayloadIn input = ctx.getInput();
	        //
	        // If the input payload is null, then we're creating a new blob from a post or a uri.  This means there
	        // is no "input" payload for our framework to process.  Therefore we need to synthesize a payload from
	        // the BlobsCommon instance we just filled out.
	        //
	        if (input == null) {
	        	PoxPayloadOut output = new PoxPayloadOut(BlobClient.SERVICE_PAYLOAD_NAME);
		        PayloadOutputPart commonPart = new PayloadOutputPart(BlobClient.SERVICE_COMMON_PART_NAME, blobsCommon);
		        output.addPart(commonPart);
		        input = new PoxPayloadIn(output.toXML());
		        ctx.setInput(input);
	        } else {
	        	// At this point, we've created a blob document in the Nuxeo repository.  Usually, we use the blob to create and instance of BlobsCommon and use
	        	// that to populate the resource record.  However, since the "input" var is not null the requester provided their own resource record data
	        	// so we'll use it rather than deriving one from the blob.
	        	logger.warn("A resource record payload was provided along with the actually blob binary file.  This payload is usually derived from the blob binary.  Since a payload was provided, we're creating the resource record from the payload and not from the corresponding blob binary." +
	        			" The data in blob resource record fields may not correspond completely with the persisted blob binary file.");
	        }	        
		}
	
		super.fillAllParts(wrapDoc, action);
	}    
}

