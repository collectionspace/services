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

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.BlobJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.blob.BlobCommon;
import org.collectionspace.services.blob.BlobCommonList;
import org.collectionspace.services.blob.BlobCommonList.BlobListItem;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BlobDocumentModelHandler.
 */
public class BlobDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<BlobCommon, BlobCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(BlobDocumentModelHandler.class);
    
    /** The blob. */
    private BlobCommon blob;
    
    /** The blob list. */
    private BlobCommonList blobList;


    /**
     * Gets the common part.
     *
     * @return the common part
     */
    @Override
    public BlobCommon getCommonPart() {
        return blob;
    }

    /**
     * Sets the common part.
     *
     * @param blob the new common part
     */
    @Override
    public void setCommonPart(BlobCommon blob) {
        this.blob = blob;
    }

    /**
     * Gets the common part list.
     *
     * @return the common part list
     */
    @Override
    public BlobCommonList getCommonPartList() {
        return blobList;
    }

    /**
     * Sets the common part list.
     *
     * @param blobList the new common part list
     */
    @Override
    public void setCommonPartList(BlobCommonList blobList) {
        this.blobList = blobList;
    }

    /**
     * Extract common part.
     *
     * @param wrapDoc the wrap doc
     * @return the blob common
     * @throws Exception the exception
     */
    @Override
    public BlobCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Fill common part.
     *
     * @param blobObject the blob object
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    @Override
    public void fillCommonPart(BlobCommon blobObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Extract common part list.
     *
     * @param wrapDoc the wrap doc
     * @return the blob common list
     * @throws Exception the exception
     */
    @Override
    public BlobCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        BlobCommonList coList = extractPagingInfo(new BlobCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("name|mimeType|encoding|length|csid");


        List<BlobCommonList.BlobListItem> list = coList.getBlobListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            BlobListItem item = new BlobListItem();

            String label = getServiceContext().getCommonPartLabel();
            item.setEncoding((String) docModel.getProperty(label, BlobJAXBSchema.encoding));
            item.setMimeType((String) docModel.getProperty(label, BlobJAXBSchema.mimeType));
            String theData = (String) docModel.getProperty(label, BlobJAXBSchema.data);
            item.setName((String) docModel.getProperty(label, BlobJAXBSchema.name));
            item.setLength((String) docModel.getProperty(label, BlobJAXBSchema.length));


            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            item.setUri(getServiceContextPath() + id);
            item.setCsid(id);
            list.add(item);
        }

        return coList;
    }

    /**
     * Gets the q property.
     *
     * @param prop the prop
     * @return the q property
     */
    @Override
    public String getQProperty(String prop) {
        return BlobConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

