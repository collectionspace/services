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
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobOutput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoImageUtils;
import org.collectionspace.services.common.service.ListResultField;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.BlobJAXBSchema;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.CommonList;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.schema.types.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

/**
 * The Class BlobDocumentModelHandler.
 */
public class BlobDocumentModelHandler
extends DocHandlerBase<BlobsCommon> {

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
		PayloadOutputPart result = null;
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        ObjectPartType partMeta = partsMetaMap.get(metadataLabel);

        if (partMeta != null) {
			RepositoryInstance repoSession = this.getRepositorySession();
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
		ServiceContext ctx = this.getServiceContext();
		BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		RepositoryInstance repoSession = this.getRepositorySession();
		DocumentModel docModel = wrapDoc.getWrappedObject();
		BlobsCommon blobsCommon = this.getCommonPartProperties(docModel);		
		String blobRepositoryId = blobsCommon.getRepositoryId(); //cache the value to pass to the blob retriever
		//
		// We're being asked for a list of blob derivatives, not the payload for a blob record.  FIXME: REM - This should be handled in a class called DerivativeDocumentHandler (need to create).
		//
		if (blobInput.isDerivativeListRequested() == true) {
	        List<ListResultField> resultsFields = getListItemsArray();
			CommonList blobsCommonList = NuxeoImageUtils.getBlobDerivatives( //FIXME: REM - Need to replace "NuxeoImageUtils" with something more general like "BlobUtils" since we may support other blob types.
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
			BlobOutput blobOutput = NuxeoImageUtils.getBlobOutput(ctx, repoSession, //FIXME: REM - If the blob's binary has been removed from the file system, then this call will return null.  We need to at least spit out a meaningful error/warning message
					blobRepositoryId, derivativeTerm, getContentFlag);
			if (getContentFlag == true) {
				blobInput.setContentStream(blobOutput.getBlobInputStream());
			}
	
			if (derivativeTerm != null) {
				// reset 'blobsCommon' if we have a derivative request
				blobsCommon = blobOutput.getBlobsCommon();
				blobsCommon.setUri(getDerivativePathBase(docModel) +
						derivativeTerm + "/" + BlobInput.URI_CONTENT_PATH);				
			}
			
			blobsCommon.setRepositoryId(null); //hide the repository id from the GET results payload since it is private
			this.setCommonPartProperties(docModel, blobsCommon);
			// finish extracting the other parts by calling the parent
		} else {
			extractMetadata(blobRepositoryId, NuxeoImageUtils.SCHEMA_IMAGE_METADATA);
			extractMetadata(blobRepositoryId, NuxeoImageUtils.SCHEMA_IPTC);
		}
		
		//
		// Hide the Nuxeo repository ID of the Nuxeo blob since this is private
		//
		docModel.setProperty(ctx.getCommonPartLabel(), BlobJAXBSchema.repositoryId, null);	
		super.extractAllParts(wrapDoc);
	}

	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		ServiceContext ctx = this.getServiceContext();
		BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		if (blobInput.getBlobFile() != null) {    		
			//
			// If blobInput has a file then we just received a multipart/form-data file post or a URI query parameter
			//
			DocumentModel documentModel = wrapDoc.getWrappedObject();
			RepositoryInstance repoSession = this.getRepositorySession();    	
			BlobsCommon blobsCommon = NuxeoImageUtils.createPicture(ctx, repoSession, blobInput);
	        PoxPayloadIn input = (PoxPayloadIn)ctx.getInput();
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
	        }
//			this.setCommonPartProperties(documentModel, blobsCommon);
			blobInput.setBlobCsid(documentModel.getName()); //Assumption here is that the documentModel "name" field is storing a CSID
		}

		super.fillAllParts(wrapDoc, action);
	}    
}

