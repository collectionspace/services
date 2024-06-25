/*
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.restrictedmedia.nuxeo;

import javax.ws.rs.core.MultivaluedMap;
import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.restrictedmedia.RestrictedMediaCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * RestrictedMediaDocumentModelHandler
 */
public class RestrictedMediaDocumentModelHandler extends NuxeoDocumentModelHandler<RestrictedMediaCommon> {

    private RestrictedMediaCommon getCommonPartProperties(DocumentModel docModel) {
        String label = getServiceContext().getCommonPartLabel();
        RestrictedMediaCommon result = new RestrictedMediaCommon();

        result.setBlobCsid((String) docModel.getProperty(label, MediaJAXBSchema.blobCsid));
        return result;
    }

    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        ServiceContext ctx = getServiceContext();

        final BlobInput blobInput = BlobUtil.getBlobInput(ctx);
        if (blobInput.isSchemaRequested()) {
            DocumentModel docModel = wrapDoc.getWrappedObject();
            RestrictedMediaCommon mediaCommon = getCommonPartProperties(docModel);
            String blobCsid = mediaCommon.getBlobCsid();
            blobInput.setBlobCsid(blobCsid);
        } else {
            super.extractAllParts(wrapDoc);
        }
    }

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        String blobCsid;
        ServiceContext ctx = getServiceContext();

        BlobInput blobInput = BlobUtil.getBlobInput(ctx);
        if (blobInput.getBlobCsid() != null) {
            blobCsid = blobInput.getBlobCsid();
        } else {
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
            blobCsid = queryParams.getFirst(BlobClient.BLOB_CSID_PARAM);
            // we're creating a blob from a URI and creating a new media resource as well
            // extract all the other fields from the input payload
            super.fillAllParts(wrapDoc, action);
        }

        if (blobCsid != null) {
            DocumentModel documentModel = wrapDoc.getWrappedObject();
            documentModel.setProperty(ctx.getCommonPartLabel(), MediaJAXBSchema.blobCsid, blobCsid);
        }
    }
}
