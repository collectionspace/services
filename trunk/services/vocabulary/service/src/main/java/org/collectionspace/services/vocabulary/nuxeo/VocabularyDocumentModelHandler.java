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

import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityDocumentModelHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.collectionspace.services.vocabulary.VocabulariesCommonList;
import org.collectionspace.services.vocabulary.VocabulariesCommonList.VocabularyListItem;

import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
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
		extends AuthorityDocumentModelHandler<VocabulariesCommon, VocabulariesCommonList> {

    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "vocabularies_common";   
    
    public VocabularyDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }
	
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public VocabulariesCommonList extractCommonPartList(
    		DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
		String label = getServiceContext().getCommonPartLabel();
        VocabulariesCommonList coList = extractPagingInfo(new VocabulariesCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("displayName|refName|shortIdentifier|vocabType|uri|csid");
        List<VocabulariesCommonList.VocabularyListItem> list = coList.getVocabularyListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            VocabularyListItem ilistItem = new VocabularyListItem();
            ilistItem.setDisplayName((String) docModel.getProperty(label,
                    AuthorityJAXBSchema.DISPLAY_NAME));
            ilistItem.setRefName((String) docModel.getProperty(label,
            		AuthorityJAXBSchema.REF_NAME));
            ilistItem.setShortIdentifier((String) docModel.getProperty(label,
            		AuthorityJAXBSchema.SHORT_IDENTIFIER));
            ilistItem.setVocabType((String) docModel.getProperty(label,
            		AuthorityJAXBSchema.VOCAB_TYPE));
            String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri(getServiceContextPath() + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
        }

        return coList;
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

