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
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobOutput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoImageUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.BlobJAXBSchema;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
		String label = getServiceContext().getCommonPartLabel();
		documentModel.setProperty(label, BlobJAXBSchema.data, blobsCommon.getData());
		documentModel.setProperty(label, BlobJAXBSchema.digest,	blobsCommon.getDigest());
		documentModel.setProperty(label, BlobJAXBSchema.encoding, blobsCommon.getEncoding());
		documentModel.setProperty(label, BlobJAXBSchema.length, blobsCommon.getLength());
		documentModel.setProperty(label, BlobJAXBSchema.mimeType, blobsCommon.getMimeType());
		documentModel.setProperty(label, BlobJAXBSchema.name, blobsCommon.getName());
		documentModel.setProperty(label, BlobJAXBSchema.uri, blobsCommon.getUri());
		documentModel.setProperty(label, BlobJAXBSchema.repositoryId, blobsCommon.getRepositoryId());
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
		
		if (blobInput.isDerivativeListRequested() == true) {
			CommonList blobsCommonList = NuxeoImageUtils.getBlobDerivatives(
					repoSession, blobRepositoryId, getDerivativePathBase(docModel));
//			ctx.setProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY, blobsCommonList);
			blobInput.setDerivativeList(blobsCommonList);
			return;  //FIXME: Don't like this exit point.  Perhaps derivatives should be a sub-resource?
		}		

		String derivativeTerm = blobInput.getDerivativeTerm();
		Boolean getContentFlag = blobInput.isContentRequested();
		BlobOutput blobOutput = NuxeoImageUtils.getBlobOutput(ctx, repoSession,
				blobRepositoryId, derivativeTerm, getContentFlag);
		if (getContentFlag == true) {
			blobInput.setContentStream(blobOutput.getBlobInputStream());
//			ctx.setProperty(BlobInput.BLOB_CONTENT_KEY, blobOutput.getBlobInputStream());
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
		super.extractAllParts(wrapDoc);
	}

	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		ServiceContext ctx = this.getServiceContext();
		BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		if (blobInput.getBlobFile() != null) {    		
			//
			// If blobInput has a file then we just received a multipart/form-data file post
			//
			DocumentModel documentModel = wrapDoc.getWrappedObject();
			RepositoryInstance repoSession = this.getRepositorySession();    	
			BlobsCommon blobsCommon = NuxeoImageUtils.createPicture(ctx, repoSession, blobInput);
			this.setCommonPartProperties(documentModel, blobsCommon);
			blobInput.setBlobCsid(documentModel.getName());
		} else {
			super.fillAllParts(wrapDoc, action);
		}
	}    
}

