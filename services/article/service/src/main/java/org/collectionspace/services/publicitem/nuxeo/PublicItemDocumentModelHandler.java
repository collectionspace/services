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
package org.collectionspace.services.publicitem.nuxeo;

import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.PublicitemsCommonJAXBSchema;
import org.collectionspace.services.article.PublicitemsCommon;
import org.collectionspace.services.client.PublicItemClient;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.nuxeo.ecm.core.api.DocumentModel;

/** ArticleDocumentModelHandler
 *  $LastChangedRevision$
 *  $LastChangedDate$
 */
public class PublicItemDocumentModelHandler
        extends DocHandlerBase<PublicitemsCommon> {
	
	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		//
		// Call our parent's implementation first to fill out most of the document model properties
		//
		super.fillAllParts(wrapDoc, action);
		
		//
		// Since we didn't know the CSID when we created the publicly accessible URL we need to
		// add it now.
		//
		DocumentModel documentModel = wrapDoc.getWrappedObject();
		String url = (String) documentModel.getProperty(PublicItemClient.SERVICE_COMMON_PART_NAME,
				PublicitemsCommonJAXBSchema.ITEM_CONTENT_URI);
		url = url.replace(PublicItemClient.CSID_PATH_PARAM_VAR, documentModel.getName());
		documentModel.setProperty(PublicItemClient.SERVICE_COMMON_PART_NAME,
				PublicitemsCommonJAXBSchema.ITEM_CONTENT_URI, url);
	}
	
    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
    	//
    	// Replace the URI with a fully qualified URL
    	//
        DocumentModel documentModel = wrapDoc.getWrappedObject();
		String itemContentUri = (String) documentModel.getProperty(PublicItemClient.SERVICE_COMMON_PART_NAME,
				PublicitemsCommonJAXBSchema.ITEM_CONTENT_URI);        
        UriInfo uriInfo = this.getServiceContext().getUriInfo();
        String baseUrl = uriInfo.getBaseUri().toString();
        String url = String.format("%s%s", baseUrl, itemContentUri);
		documentModel.setProperty(PublicItemClient.SERVICE_COMMON_PART_NAME,
				PublicitemsCommonJAXBSchema.ITEM_CONTENT_URI, url);
    }	
}

