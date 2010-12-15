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
package org.collectionspace.services.organization.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrgauthoritiesCommonList.OrgauthorityListItem;

import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityDocumentModelHandler;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * OrgAuthorityDocumentModelHandler
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class OrgAuthorityDocumentModelHandler
        extends AuthorityDocumentModelHandler<OrgauthoritiesCommon, OrgauthoritiesCommonList> {

    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "orgauthorities_common";   
    
    public OrgAuthorityDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }
	
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public OrgauthoritiesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        OrgauthoritiesCommonList coList = this.extractPagingInfo(new OrgauthoritiesCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("displayName|refName|shortIdentifier|vocabType|uri|csid");
    	List<OrgauthoritiesCommonList.OrgauthorityListItem> list = coList.getOrgauthorityListItem();
    	Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        String label = getServiceContext().getCommonPartLabel();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            OrgauthorityListItem ilistItem = new OrgauthorityListItem();
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
        return OrgAuthorityConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

