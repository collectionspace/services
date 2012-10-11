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
package org.collectionspace.services.media.nuxeo;

import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.media.MediaCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * The Class MediaDocumentModelHandler.
 */
public class MediaDocumentModelHandler
        extends DocHandlerBase<MediaCommon> {

    //==============================================================================

	private MediaCommon getCommonPartProperties(DocumentModel docModel) throws Exception {
		String label = getServiceContext().getCommonPartLabel();
		MediaCommon result = new MediaCommon();
		
		result.setBlobCsid((String)	
				docModel.getProperty(label, MediaJAXBSchema.blobCsid));
		
		return result;
	}
    
    @Override
	public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
			throws Exception {
		ServiceContext ctx = this.getServiceContext();
		
		BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		if (blobInput != null && blobInput.isSchemaRequested()) { //Extract the blob info instead of the media info
			DocumentModel docModel = wrapDoc.getWrappedObject();
			MediaCommon mediaCommon = this.getCommonPartProperties(docModel);		
			String blobCsid = mediaCommon.getBlobCsid(); //cache the value to pass to the blob retriever
			blobInput.setBlobCsid(blobCsid);
		} else {
			super.extractAllParts(wrapDoc);
		}		
	}
    
	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		ServiceContext ctx = this.getServiceContext();
		String blobCsid = null;
		
		BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		if (blobInput != null && blobInput.getBlobCsid() != null) {
			blobCsid = blobInput.getBlobCsid(); // If getBlobCsid has a value then we just finishing a multipart/form-data file post, created a blob, and now associating it with an existing media resource 
		} else {
			MultivaluedMap<String, String> queryParams = this.getServiceContext().getQueryParams();
			blobCsid = queryParams.getFirst(BlobClient.BLOB_CSID_PARAM); // if the blobUri query param is set, it's probably set from the MediaResource.create method -we're creating a blob from a URI and creating a new media resource as well
			// extract all the other fields from the input payload
			super.fillAllParts(wrapDoc, action);
		}
		
		//
		if (blobCsid != null) {
			DocumentModel documentModel = wrapDoc.getWrappedObject();
			documentModel.setProperty(ctx.getCommonPartLabel(), MediaJAXBSchema.blobCsid, blobCsid);
		}
	}    
    
}

