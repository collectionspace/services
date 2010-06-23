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

import org.collectionspace.services.OrgAuthorityJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrgauthoritiesCommonList.OrgauthorityListItem;

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrgAuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class OrgAuthorityDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<OrgauthoritiesCommon, OrgauthoritiesCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(OrgAuthorityDocumentModelHandler.class);
    /**
     * orgAuthority is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private OrgauthoritiesCommon orgAuthority;
    /**
     * orgAuthorityList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private OrgauthoritiesCommonList orgAuthorityList;


    /**
     * getCommonPart get associated orgAuthority
     * @return
     */
    @Override
    public OrgauthoritiesCommon getCommonPart() {
        return orgAuthority;
    }

    /**
     * setCommonPart set associated orgAuthority
     * @param orgAuthority
     */
    @Override
    public void setCommonPart(OrgauthoritiesCommon orgAuthority) {
        this.orgAuthority = orgAuthority;
    }

    /**
     * getCommonPartList get associated orgAuthority (for index/GET_ALL)
     * @return
     */
    @Override
    public OrgauthoritiesCommonList getCommonPartList() {
        return orgAuthorityList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(OrgauthoritiesCommonList orgAuthorityList) {
        this.orgAuthorityList = orgAuthorityList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public OrgauthoritiesCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(OrgauthoritiesCommon orgAuthorityObject, 
    		DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public OrgauthoritiesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        OrgauthoritiesCommonList coList = this.extractPagingInfo(new OrgauthoritiesCommonList(), wrapDoc);
    	List<OrgauthoritiesCommonList.OrgauthorityListItem> list = coList.getOrgauthorityListItem();
    	Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            OrgauthorityListItem ilistItem = new OrgauthorityListItem();
            ilistItem.setDisplayName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    OrgAuthorityJAXBSchema.DISPLAY_NAME));
            ilistItem.setRefName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    OrgAuthorityJAXBSchema.REF_NAME));
            ilistItem.setShortIdentifier((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    OrgAuthorityJAXBSchema.SHORT_IDENTIFIER));
            ilistItem.setVocabType((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    OrgAuthorityJAXBSchema.VOCAB_TYPE));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
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

