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

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.media.MediaCommon;
import org.collectionspace.services.media.MediaCommonList;
import org.collectionspace.services.media.MediaCommonList.MediaListItem;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class MediaDocumentModelHandler.
 */
public class MediaDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<MediaCommon, MediaCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(MediaDocumentModelHandler.class);
    
    /** The media. */
    private MediaCommon media;
    
    /** The media list. */
    private MediaCommonList mediaList;


    /**
     * Gets the common part.
     *
     * @return the common part
     */
    @Override
    public MediaCommon getCommonPart() {
        return media;
    }

    /**
     * Sets the common part.
     *
     * @param media the new common part
     */
    @Override
    public void setCommonPart(MediaCommon media) {
        this.media = media;
    }

    /**
     * Gets the common part list.
     *
     * @return the common part list
     */
    @Override
    public MediaCommonList getCommonPartList() {
        return mediaList;
    }

    /**
     * Sets the common part list.
     *
     * @param mediaList the new common part list
     */
    @Override
    public void setCommonPartList(MediaCommonList mediaList) {
        this.mediaList = mediaList;
    }

    /**
     * Extract common part.
     *
     * @param wrapDoc the wrap doc
     * @return the media common
     * @throws Exception the exception
     */
    @Override
    public MediaCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Fill common part.
     *
     * @param mediaObject the media object
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    @Override
    public void fillCommonPart(MediaCommon mediaObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Extract common part list.
     *
     * @param wrapDoc the wrap doc
     * @return the media common list
     * @throws Exception the exception
     */
    @Override
    public MediaCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        MediaCommonList coList = extractPagingInfo(new MediaCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        //CSPACE-3209 don't use all fields.  commonList.setFieldsReturned("currentOwner|depositor|exitDate|exitMethod|exitNote|exitNumber|exitReason|packingNote|uri|csid");
        commonList.setFieldsReturned("exitNumber|currentOwner|uri|csid");  //CSPACE-3209 now do this
        List<MediaCommonList.MediaListItem> list = coList.getMediaListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            MediaListItem item = new MediaListItem();

            String label = getServiceContext().getCommonPartLabel();

            item.setTitle((String) docModel.getProperty(label, MediaJAXBSchema.title));
            item.setSource((String) docModel.getProperty(label, MediaJAXBSchema.source));
            item.setFilename((String) docModel.getProperty(label, MediaJAXBSchema.filename));
            item.setIdentificationNumber((String) docModel.getProperty(label, MediaJAXBSchema.identificationNumber));
            
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
        return MediaConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

