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

import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList.VocabularyitemListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VocabularyItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class VocabularyItemDocumentModelHandler
		extends AuthorityItemDocumentModelHandler<VocabularyitemsCommon, VocabularyitemsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(VocabularyItemDocumentModelHandler.class);
    
    private static final String COMMON_PART_LABEL = "vocabularyitems_common";   
    
    public VocabularyItemDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return VocabularyClient.SERVICE_PATH_COMPONENT;    // CSPACE-3932
    }
	
    
    @Override
	public VocabularyitemsCommonList extractCommonPartList(
			DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
		VocabularyitemsCommonList coList = extractPagingInfo(new VocabularyitemsCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("displayName|refName|shortIdentifier|order|uri|csid");
		
		List<VocabularyitemsCommonList.VocabularyitemListItem> list = coList.getVocabularyitemListItem();
		Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
		String commonPartLabel = getServiceContext().getCommonPartLabel("vocabularyItems");
		while (iter.hasNext()) {
			DocumentModel docModel = iter.next();
			VocabularyitemListItem ilistItem = new VocabularyitemListItem();
			ilistItem.setDisplayName((String) docModel.getProperty(commonPartLabel, 
					AuthorityItemJAXBSchema.DISPLAY_NAME));
			ilistItem.setShortIdentifier((String) docModel.getProperty(commonPartLabel,
					AuthorityItemJAXBSchema.SHORT_IDENTIFIER));
            ilistItem.setRefName((String) docModel.getProperty(commonPartLabel,
                                AuthorityItemJAXBSchema.REF_NAME));
            ilistItem.setOrder((String) docModel.getProperty(commonPartLabel,
                              AuthorityItemJAXBSchema.ORDER));
			String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
			ilistItem.setUri("/vocabularies/" + inAuthority + "/items/" + id);
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
        return VocabularyItemConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

