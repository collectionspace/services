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
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.media.MediaCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.util.List;

/**
 * The Class MediaDocumentModelHandler.
 */
public class MediaDocumentModelHandler
        extends DocHandlerBase<MediaCommon, AbstractCommonList> {

    public static DocHandlerBase.CommonListReflection clr;
    static {
        clr = new DocHandlerBase.CommonListReflection();
        clr.SchemaName= "media";
        clr.SummaryFields = "title|source|filename|identificationNumber|uri|csid";
        clr.AbstractCommonListClassname = "org.collectionspace.services.media.MediaCommonList";
        clr.CommonListItemClassname =     "org.collectionspace.services.media.MediaCommonList$MediaListItem";
        clr.ListResultsItemMethodName = "getMediaListItem";
        clr.ListItemsArray =   new String[][]   {{"setTitle", "title"},
                                                 {"setSource", "source"},
                                                 {"setFilename", "filename"},
                                                 {"setIdentificationNumber", "identificationNumber"}
                                                 };
    }
    public DocHandlerBase.CommonListReflection getCommonListReflection(){
        return clr;
    }
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
		BlobInput blobInput = BlobUtil.getBlobInput(ctx);
		if (blobInput != null && blobInput.getBlobCsid() != null) {
			String blobCsid = blobInput.getBlobCsid();
			//
			// If getBlobCsid has a value then we just received a multipart/form-data file post
			//
			DocumentModel documentModel = wrapDoc.getWrappedObject();
			documentModel.setProperty(ctx.getCommonPartLabel(), MediaJAXBSchema.blobCsid, blobCsid);
		} else {
			super.fillAllParts(wrapDoc, action);
		}
	}    
    
}

