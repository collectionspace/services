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
package org.collectionspace.services.vocabulary.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.VocabularyJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.collectionspace.services.vocabulary.VocabulariesCommonList;
import org.collectionspace.services.vocabulary.VocabulariesCommonList.VocabularyListItem;

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandler;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VocabularyDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class VocabularyDocumentModelHandler
        extends RemoteDocumentModelHandler<VocabulariesCommon, VocabulariesCommonList> {

    private final Logger logger = LoggerFactory.getLogger(VocabularyDocumentModelHandler.class);
    /**
     * vocabulary is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private VocabulariesCommon vocabulary;
    /**
     * vocabularyList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private VocabulariesCommonList vocabularyList;

    @Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

    /**
     * getCommonPart get associated vocabulary
     * @return
     */
    @Override
    public VocabulariesCommon getCommonPart() {
        return vocabulary;
    }

    /**
     * setCommonPart set associated vocabulary
     * @param vocabulary
     */
    @Override
    public void setCommonPart(VocabulariesCommon vocabulary) {
        this.vocabulary = vocabulary;
    }

    /**
     * getCommonPartList get associated vocabulary (for index/GET_ALL)
     * @return
     */
    @Override
    public VocabulariesCommonList getCommonPartList() {
        return vocabularyList;
    }

    @Override
    public void setCommonPartList(VocabulariesCommonList vocabularyList) {
        this.vocabularyList = vocabularyList;
    }

    @Override
    public VocabulariesCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(VocabulariesCommon vocabularyObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public VocabulariesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        DocumentModelList docList = wrapDoc.getWrappedObject();

        VocabulariesCommonList coList = new VocabulariesCommonList();
        List<VocabulariesCommonList.VocabularyListItem> list = coList.getVocabularyListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            VocabularyListItem ilistItem = new VocabularyListItem();
            ilistItem.setDisplayName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    VocabularyJAXBSchema.DISPLAY_NAME));
            ilistItem.setVocabType((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    VocabularyJAXBSchema.VOCAB_TYPE));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri(getServiceContextPath() + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
        }

        return coList;
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getDocumentType()
     */
    @Override
    public String getDocumentType() {
        return VocabularyConstants.NUXEO_DOCTYPE;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return VocabularyConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

